/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.controls.itemlist;

import org.jkiss.utils.CommonUtils;
import org.jkiss.dbeaver.ext.ui.ISearchExecutor;
import org.jkiss.dbeaver.model.DBPNamedObject;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public abstract class ObjectSearcher<OBJECT_TYPE extends DBPNamedObject> implements ISearchExecutor {

    private Pattern curSearchPattern;
    private int curSearchIndex;
    private Set<OBJECT_TYPE> curSearchResult = null;

    @Override
    public boolean performSearch(String searchString, int options)
    {
        boolean caseSensitiveSearch = (options & SEARCH_CASE_SENSITIVE) != 0;
        if (!CommonUtils.isEmpty(searchString) && curSearchPattern == null || !CommonUtils.equalObjects(curSearchPattern.pattern(), makeLikePattern(searchString))) {
            try {
                curSearchPattern = Pattern.compile(makeLikePattern(searchString), caseSensitiveSearch ? 0 : Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException e) {
                setInfo(e.getMessage());
                return false;
            }
            curSearchIndex = -1;
            Set<OBJECT_TYPE> oldSearchResult = curSearchResult;
            curSearchResult = null;
            boolean found = false;
            Collection<OBJECT_TYPE> nodes = getContent();
            if (!CommonUtils.isEmpty(nodes)) {
                for (OBJECT_TYPE node : nodes) {
                    if (matchesSearch(node)) {
                        if (curSearchResult == null) {
                            curSearchResult = new LinkedHashSet<OBJECT_TYPE>(50);
                        }
                        curSearchResult.add(node);
                        updateObject(node);
                        if (!found) {
                            curSearchIndex++;
                            selectObject(node);
                            revealObject(node);
                        }
                        found = true;
                    }
                }
            }
            if (!CommonUtils.isEmpty(oldSearchResult)) {
                for (OBJECT_TYPE oldNode : oldSearchResult) {
                    if (curSearchResult == null || !curSearchResult.contains(oldNode)) {
                        updateObject(oldNode);
                    }
                }
            }
            return found;
        } else {
            boolean findNext = ((options & SEARCH_NEXT) != 0);
            boolean findPrev = ((options & SEARCH_PREVIOUS) != 0);
            if ((findNext || findPrev) && !CommonUtils.isEmpty(curSearchResult)) {
                if (findNext) {
                    curSearchIndex++;
                    if (curSearchIndex >= curSearchResult.size()) {
                        curSearchIndex = 0;
                    }
                } else {
                    curSearchIndex--;
                    if (curSearchIndex < 0) {
                        curSearchIndex = curSearchResult.size() - 1;
                    }
                }
                int index = 0;
                for (OBJECT_TYPE node : curSearchResult) {
                    if (index++ == curSearchIndex) {
                        selectObject(node);
                        revealObject(node);
                        break;
                    }
                }
            }
            return !CommonUtils.isEmpty(curSearchResult);
        }
    }

    @Override
    public void cancelSearch()
    {
        if (curSearchPattern != null) {
            curSearchPattern = null;
            curSearchIndex = 0;
            if (curSearchResult != null) {
                Set<OBJECT_TYPE> oldSearchResult = curSearchResult;
                curSearchResult = null;
                for (OBJECT_TYPE oldNode : oldSearchResult) {
                    updateObject(oldNode);
                }
                selectObject(null);
            }
        }
    }

    private boolean matchesSearch(DBPNamedObject element)
    {
        if (curSearchPattern == null) {
            return false;
        }
        return curSearchPattern.matcher(element.getName()).find();
    }

    private static String makeLikePattern(String like)
    {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < like.length(); i++) {
            char c = like.charAt(i);
            if (c == '*') result.append(".*");
            else if (c == '?') result.append(".");
            else if (Character.isLetterOrDigit(c)) result.append(c);
            else result.append("\\").append(c);
        }

        return result.toString();
    }

    public boolean hasObject(OBJECT_TYPE object)
    {
        return curSearchResult != null && curSearchResult.contains(object);
    }

    protected abstract void setInfo(String message);

    protected abstract Collection<OBJECT_TYPE> getContent();

    protected abstract void selectObject(OBJECT_TYPE object);

    protected abstract void updateObject(OBJECT_TYPE object);

    protected abstract void revealObject(OBJECT_TYPE object);

}
