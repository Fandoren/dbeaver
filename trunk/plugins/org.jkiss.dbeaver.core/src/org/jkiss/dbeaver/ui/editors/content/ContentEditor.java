/*
 * Copyright (C) 2010-2014 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ui.editors.content;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.*;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.DBeaverUI;
import org.jkiss.dbeaver.ext.IDataSourceProvider;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.data.DBDContent;
import org.jkiss.dbeaver.model.data.DBDValueController;
import org.jkiss.dbeaver.model.data.DBDValueEditorStandalone;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.runtime.VoidProgressMonitor;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.ColumnInfoPanel;
import org.jkiss.dbeaver.ui.editors.MultiPageAbstractEditor;
import org.jkiss.dbeaver.utils.ContentUtils;

import javax.activation.MimeType;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * LOBEditor
 */
public class ContentEditor extends MultiPageAbstractEditor implements IDataSourceProvider, DBDValueEditorStandalone, IResourceChangeListener
{
    @Override
    public ContentEditorInput getEditorInput()
    {
        return (ContentEditorInput)super.getEditorInput();
    }

    public static ContentEditor openEditor(DBDValueController valueController, ContentEditorPart[] editorParts)
    {
        ContentEditorInput editorInput;
        // Save data to file
        try {
            LOBInitializer initializer = new LOBInitializer(valueController, editorParts, null);
            //valueController.getValueSite().getWorkbenchWindow().run(true, true, initializer);
            DBeaverUI.runInProgressService(initializer);
            editorInput = initializer.editorInput;
        } catch (Throwable e) {
            if (e instanceof InvocationTargetException) {
                e = ((InvocationTargetException)e).getTargetException();
            }
            UIUtils.showErrorDialog(valueController.getValueSite().getShell(), "Cannot open content editor", null, e);
            return null;
        }
        try {
            return (ContentEditor) valueController.getValueSite().getWorkbenchWindow().getActivePage().openEditor(
                editorInput,
                ContentEditor.class.getName());
        }
        catch (PartInitException e) {
            log.error("Could not open LOB editorPart", e);
            return null;
        }
    }

    //public static final long MAX_TEXT_LENGTH = 10 * 1024 * 1024;
    //public static final long MAX_IMAGE_LENGTH = 10 * 1024 * 1024;

    static final Log log = LogFactory.getLog(ContentEditor.class);

    @Override
    public DBPDataSource getDataSource()
    {
        return getEditorInput().getDataSource();
    }

    static class ContentPartInfo {
        ContentEditorPart editorPart;
        boolean activated;
        public int index = -1;

        private ContentPartInfo(ContentEditorPart editorPart) {
            this.editorPart = editorPart;
        }
    }

    private static class LOBInitializer implements DBRRunnableWithProgress {
        DBDValueController valueController;
        ContentEditorPart[] editorParts;
        ContentEditorInput editorInput;

        private LOBInitializer(DBDValueController valueController, ContentEditorPart[] editorParts, ContentEditorInput editorInput)
        {
            this.valueController = valueController;
            this.editorParts = editorParts;
            this.editorInput = editorInput;
        }

        @Override
        public void run(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException
        {
            try {
                if (editorInput == null) {
                    editorInput = new ContentEditorInput(
                        valueController,
                        editorParts,
                        monitor);
                } else {
                    editorInput.refreshContent(monitor, valueController);
                }
            } catch (DBException e) {
                throw new InvocationTargetException(e);
            }
        }

    }

    private boolean valueEditorRegistered = false;

    private List<ContentPartInfo> contentParts = new ArrayList<ContentPartInfo>();
    private ColumnInfoPanel infoPanel;
    private boolean dirty;
    private boolean partsLoaded;
    private boolean saveInProgress;

    public ContentEditor()
    {
    }

    public ContentPartInfo getContentEditor(IEditorPart editor) {
        for (ContentPartInfo contentPart : contentParts) {
            if (contentPart.editorPart == editor) {
                return contentPart;
            }
        }
        return null;
    }

    @Override
    public void doSave(final IProgressMonitor monitor)
    {
        if (!isDirty()) {
            // Nothing to save
            return;
        }
        // Execute save in UI thread
        UIUtils.runInUI(getSite().getShell(), new Runnable() {
            @Override
            public void run()
            {
                try {
                    // Check for dirty parts
                    final List<ContentEditorPart> dirtyParts = new ArrayList<ContentEditorPart>();
                    for (ContentPartInfo partInfo : contentParts) {
                        if (partInfo.activated && partInfo.editorPart.isDirty()) {
                            dirtyParts.add(partInfo.editorPart);
                        }
                    }

                    ContentEditorPart dirtyPart = null;
                    if (dirtyParts.isEmpty()) {
                        // No modified parts - no additional save required
                    } else if (dirtyParts.size() == 1) {
                        // Single part modified - save it
                        dirtyPart = dirtyParts.get(0);
                    } else {
                        // Multiple parts modified - need to choose one
                        dirtyPart = SelectContentPartDialog.selectContentPart(getSite().getShell(), dirtyParts);
                    }

                    if (dirtyPart != null) {
                        saveInProgress = true;
                        try {
                            dirtyPart.doSave(monitor);
                        }
                        finally {
                            saveInProgress = false;
                        }
                    }
                    // Set dirty flag - if error will occure during content save
                    // then document remains dirty
                    ContentEditor.this.dirty = true;

                    ContentEditorInput editorInput = getEditorInput();
                    editorInput.updateContentFromFile(monitor);
                    editorInput.getValueController().updateValue(editorInput.getContent());

                    // Close editor
                    closeValueEditor();
                }
                catch (Exception e) {
                    UIUtils.showErrorDialog(
                        getSite().getShell(),
                        "Could not save content",
                        "Could not save content to database",
                        e);
                }
            }
        });
    }

    @Override
    public void doSaveAs()
    {

    }

    @Override
    public void init(IEditorSite site, IEditorInput input)
        throws PartInitException
    {
        super.init(site, input);
        setPartName(input.getName());

        valueEditorRegistered = true;

        DBDContent content = getContent();
        if (content == null) {
            return;
        }

        MimeType mimeType = ContentUtils.getMimeType(content.getContentType());

        // Fill nested editorParts info
        ContentEditorPart[] editorParts = getEditorInput().getEditors();
        for (ContentEditorPart editorPart : editorParts) {
            contentParts.add(new ContentPartInfo(editorPart));
            editorPart.initPart(this, mimeType);
        }

        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
    }

    @Override
    public void dispose()
    {
        this.partsLoaded = true;
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);

        if (valueEditorRegistered) {
            getValueController().unregisterEditor(this);
            valueEditorRegistered = false;
        }
        if (getEditorInput() != null) {
            // Release LOB input resources
            try {
                getEditorInput().release(VoidProgressMonitor.INSTANCE);
            } catch (Throwable e) {
                log.warn("Error releasing LOB input", e);
            }
        }
        super.dispose();
    }

    @Override
    public boolean isDirty()
    {
        if (dirty) {
            return true;
        }
        for (ContentPartInfo contentPart : contentParts) {
            if (contentPart.activated && contentPart.editorPart.isDirty()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isSaveAsAllowed()
    {
        return false;
    }

    @Override
    protected IEditorSite createSite(IEditorPart editor)
    {
        return new ContentEditorSite(this, editor);
    }

    @Override
    protected void createPages() {
        super.createPages();
        DBDContent content = getContent();
        if (content == null) {
            return;
        }
        String contentType = null;
        try {
            contentType = content.getContentType();
        } catch (Exception e) {
            log.error("Could not determine value content type", e);
        }
        long contentLength;
        try {
            contentLength = content.getContentLength();
        } catch (Exception e) {
            log.warn("Could not determine value content length", e);
            // Get file length
            contentLength = getEditorInput().getFile().getFullPath().toFile().length();
        }
        MimeType mimeType = ContentUtils.getMimeType(contentType);
        IEditorPart defaultPage = null, preferredPage = null;
        for (ContentPartInfo contentPart : contentParts) {
            ContentEditorPart editorPart = contentPart.editorPart;
            if (contentLength > editorPart.getMaxContentLength()) {
                continue;
            }
            if (preferredPage != null && editorPart.isOptionalContent()) {
                // Do not add optional parts if we already have prefered one
                continue;
            }
            try {
                int index = addPage(editorPart, getEditorInput());
                setPageText(index, editorPart.getContentTypeTitle());
                setPageImage(index, editorPart.getContentTypeImage());
                contentPart.activated = true;
                contentPart.index = index;
                // Check MIME type
                if (mimeType != null && mimeType.getPrimaryType().equals(editorPart.getPreferredMimeType())) {
                    defaultPage = editorPart;
                }
                if (editorPart.isPreferredContent()) {
                    preferredPage = editorPart;
                }
            } catch (PartInitException e) {
                log.error(e);
            }
        }
        if (preferredPage != null) {
            // Remove all optional pages
            for (ContentPartInfo contentPart : contentParts) {
                if (contentPart.activated && contentPart.editorPart != preferredPage && contentPart.editorPart.isOptionalContent()) {
                    removePage(contentPart.index);
                }
            }

            // Set default page
            setActiveEditor(preferredPage);
        } else if (defaultPage != null) {
            setActiveEditor(defaultPage);
        }

        this.partsLoaded = true;
    }

    @Override
    public void removePage(int pageIndex) {
        for (ContentPartInfo contentPart : contentParts) {
            if (contentPart.index == pageIndex) {
                contentPart.index = -1;
            } else if (contentPart.index > pageIndex) {
                contentPart.index--;
            }
        }
        super.removePage(pageIndex);
    }

    @Override
    protected Composite createPageContainer(Composite parent)
    {
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        panel.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_BOTH);
        panel.setLayoutData(gd);

        {
            infoPanel = new ColumnInfoPanel(panel, SWT.NONE, getValueController());
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.exclude = true;
            infoPanel.setLayoutData(gd);
            infoPanel.setVisible(false);
        }

        Composite editotPanel = new Composite(panel, SWT.NONE);
        layout = new GridLayout(1, false);
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        editotPanel.setLayout(layout);
        gd = new GridData(GridData.FILL_BOTH);
        editotPanel.setLayoutData(gd);

        return editotPanel;
    }

    void toggleInfoBar()
    {
        boolean visible = infoPanel.isVisible();
        visible = !visible;
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.exclude = !visible;
        infoPanel.setLayoutData(gd);
        infoPanel.setVisible(visible);
        infoPanel.getParent().layout();
    }

    DBDContent getContent()
    {
        Object value = getValueController().getValue();
        if (value instanceof DBDContent) {
            return (DBDContent) value;
        } else {
            return null;
        }
    }

    public DBDValueController getValueController()
    {
        ContentEditorInput input = getEditorInput();
        return input == null ? null : input.getValueController();
    }

    @Override
    public Control getControl()
    {
        return getControl(getActivePage());
    }

    @Override
    public Object extractEditorValue() throws DBException
    {
        DBeaverUI.runInUI(DBeaverUI.getActiveWorkbenchWindow(), new DBRRunnableWithProgress() {
            @Override
            public void run(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                try {
                    getEditorInput().updateContentFromFile(monitor.getNestedMonitor());
                } catch (DBException e) {
                    throw new InvocationTargetException(e);
                }
            }
        });

        return getEditorInput().getContent();
    }

    @Override
    public void primeEditorValue(Object value) throws DBException
    {
        DBDValueController valueController = getEditorInput().getValueController();
        LOBInitializer initializer = new LOBInitializer(valueController, getEditorInput().getEditors(), getEditorInput());
        try {
            //valueController.getValueSite().getWorkbenchWindow().run(true, true, initializer);
            DBeaverUI.runInProgressService(initializer);
        } catch (InvocationTargetException e) {
            UIUtils.showErrorDialog(valueController.getValueSite().getShell(), "Cannot refresh content editor", null, e);
        } catch (InterruptedException e) {
            // ignore
        }
    }

    @Override
    public void showValueEditor()
    {
        this.getEditorSite().getWorkbenchWindow().getActivePage().activate(this);
    }

    @Override
    public void closeValueEditor()
    {
        IWorkbenchPage workbenchPage = this.getEditorSite().getWorkbenchWindow().getActivePage();
        if (workbenchPage != null) {
            workbenchPage.closeEditor(this, false);
        } else {
            // Special case - occurred when entire workbench is closed
            // We need to unregister editor and release all resource here
            if (valueEditorRegistered) {
                getValueController().unregisterEditor(this);
                valueEditorRegistered = false;
            }
        }
    }

    @Override
    public void resourceChanged(IResourceChangeEvent event)
    {
        if (!partsLoaded || saveInProgress) {
            // No content change before all parts are loaded
            return;
        }
        IResourceDelta delta= event.getDelta();
        if (delta == null) {
            return;
        }
        delta = delta.findMember(ContentUtils.convertPathToWorkspacePath(getEditorInput().getPath()));
        if (delta == null) {
            return;
        }
        if (delta.getKind() == IResourceDelta.CHANGED &&
            (delta.getFlags() & IResourceDelta.CONTENT) != 0)
        {
            // Content was changed somehow so mark editor as dirty
            dirty = true;
            getSite().getShell().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run()
                {
                    firePropertyChange(PROP_DIRTY);
                }
            });
        }
    }

}