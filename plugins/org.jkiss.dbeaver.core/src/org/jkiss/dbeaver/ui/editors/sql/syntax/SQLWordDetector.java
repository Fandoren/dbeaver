/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.editors.sql.syntax;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 * Determines whether a given character is valid as part of an SQL keyword in
 * the current context.
 */
public class SQLWordDetector implements IWordDetector
{

    @Override
    public boolean isWordStart(char c) {
        return Character.isUnicodeIdentifierStart(c);
    }

    @Override
    public boolean isWordPart(char c) {
        return Character.isUnicodeIdentifierPart(c) || c == '$';
    }

}