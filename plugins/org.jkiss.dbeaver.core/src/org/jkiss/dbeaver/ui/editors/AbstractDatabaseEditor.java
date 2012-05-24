/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.editors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.jkiss.dbeaver.ext.IDataSourceProvider;
import org.jkiss.dbeaver.ext.IDatabaseEditor;
import org.jkiss.dbeaver.ext.IDatabaseEditorInput;

/**
 * AbstractDatabaseEditor
 */
public abstract class AbstractDatabaseEditor<INPUT_TYPE extends IDatabaseEditorInput> extends EditorPart implements IDatabaseEditor, IDataSourceProvider
{
    private DatabaseEditorListener listener;
    private Image editorImage;

    @Override
    public void init(IEditorSite site, IEditorInput input)
        throws PartInitException
    {
        super.setSite(site);
        super.setInput(input);
        this.setPartName(input.getName());
        editorImage = input.getImageDescriptor().createImage();
        this.setTitleImage(editorImage);

        listener = new DatabaseEditorListener(this);
    }

    @Override
    public void dispose()
    {
        if (editorImage != null) {
            editorImage.dispose();
            editorImage = null;
        }
        listener.dispose();
        super.dispose();
    }

    @Override
    @SuppressWarnings("unchecked")
    public INPUT_TYPE getEditorInput()
    {
        return (INPUT_TYPE)super.getEditorInput();
    }

    @Override
    public void doSave(IProgressMonitor monitor)
    {
    }

    @Override
    public void doSaveAs()
    {
    }

    @Override
    public boolean isDirty()
    {
        return false;
    }

    @Override
    public boolean isSaveAsAllowed()
    {
        return false;
    }

    @Override
    public void setFocus() {

    }

}