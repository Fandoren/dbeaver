/*
 * Copyright (C) 2010-2012 Serge Rieder
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.*;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.part.MultiPageEditorActionBarContributor;
import org.eclipse.ui.texteditor.BasicTextEditorActionContributor;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.utils.ContentUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

/**
 * Content Editor contributor.
 * Uses text editor contributor to fill status bar and menu for possible integrated text editors.
 */
public class ContentEditorContributor extends MultiPageEditorActionBarContributor
{
    static final Log log = LogFactory.getLog(ContentEditorContributor.class);

    private final BasicTextEditorActionContributor textContributor;
    private ContentEditor activeEditor;
    //private IEditorPart activePage;

    private final IAction saveAction = new FileExportAction();
    private final IAction loadAction = new FileImportAction();
    private final IAction infoAction = new InfoAction();
    private final IAction applyAction = new ApplyAction();
    private final IAction closeAction = new CloseAction();
    private Combo encodingCombo;

    private IPropertyListener dirtyListener = new IPropertyListener() {
        @Override
        public void propertyChanged(Object source, int propId)
        {
            if (propId == ContentEditor.PROP_DIRTY) {
                if (applyAction != null && activeEditor != null) {
                    applyAction.setEnabled(activeEditor.isDirty());
                }
            }
        }
    };

    public ContentEditorContributor()
    {
        textContributor = new BasicTextEditorActionContributor();
    }

    ContentEditor getEditor()
    {
        return activeEditor;
    }

    @Override
    public void init(IActionBars bars, IWorkbenchPage page)
    {
        super.init(bars, page);
        textContributor.init(bars, page);
    }

    @Override
    public void init(IActionBars bars)
    {
        super.init(bars);
        textContributor.init(bars);
    }

    @Override
    public void dispose()
    {
        textContributor.dispose();
        if (activeEditor != null) {
            activeEditor.removePropertyListener(dirtyListener);
        }
        super.dispose();
    }

    @Override
    public void setActiveEditor(IEditorPart part)
    {
        super.setActiveEditor(part);
        //textContributor.setActiveEditor(part);
        if (activeEditor != null) {
            activeEditor.removePropertyListener(dirtyListener);
        }
        this.activeEditor = (ContentEditor) part;
        this.activeEditor.addPropertyListener(dirtyListener);

        if (this.activeEditor != null) {
            if (encodingCombo != null && !encodingCombo.isDisposed()) {
                try {
                    String curCharset = this.activeEditor.getEditorInput().getFile().getCharset();
                    int charsetCount = encodingCombo.getItemCount();
                    for (int i = 0; i < charsetCount; i++) {
                        if (encodingCombo.getItem(i).equals(curCharset)) {
                            encodingCombo.select(i);
                            break;
                        }
                    }
                } catch (CoreException e) {
                    log.error(e);
                }
            }
            if (applyAction != null) {
                applyAction.setEnabled(activeEditor.isDirty());
            }
            if (loadAction != null) {
                loadAction.setEnabled(!activeEditor.getEditorInput().isReadOnly());
            }

        }
    }

    @Override
    public void setActivePage(IEditorPart activeEditor)
    {
        //this.activePage = activeEditor;
        this.textContributor.setActiveEditor(activeEditor);
    }

    @Override
    public void contributeToMenu(IMenuManager manager)
    {
        super.contributeToMenu(manager);
        textContributor.contributeToMenu(manager);

        IMenuManager menu = new MenuManager("L&OB Editor");
        manager.prependToGroup(IWorkbenchActionConstants.MB_ADDITIONS, menu);
        menu.add(loadAction);
        menu.add(saveAction);
        menu.add(new Separator());
        menu.add(infoAction);
        menu.add(new Separator());
        menu.add(applyAction);
        menu.add(closeAction);
    }

    @Override
    public void contributeToStatusLine(IStatusLineManager statusLineManager)
    {
        super.contributeToStatusLine(statusLineManager);
        textContributor.contributeToStatusLine(statusLineManager);
    }

    @Override
    public void contributeToToolBar(IToolBarManager manager)
    {
        super.contributeToToolBar(manager);
        textContributor.contributeToToolBar(manager);
        // Execution
        manager.add(loadAction);
        manager.add(saveAction);
        manager.add(new Separator());
        manager.add(infoAction);
        manager.add(new Separator());
        manager.add(applyAction);
        manager.add(closeAction);
        manager.add(new Separator());
        manager.add(new ControlContribution("Encoding")
        {
            @Override
            protected Control createControl(Composite parent)
            {
                String curCharset = null;
                if (getEditor() != null) {
                    try {
                        curCharset = getEditor().getEditorInput().getFile().getCharset();
                    } catch (CoreException e) {
                        log.error(e);
                    }
                }
                encodingCombo = UIUtils.createEncodingCombo(parent, curCharset);
                encodingCombo.setToolTipText("Content Encoding");
                encodingCombo.addSelectionListener(new SelectionAdapter() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        final ContentEditor contentEditor = getEditor();
                        if (contentEditor != null) {
                            final ContentEditorInput contentEditorInput = contentEditor.getEditorInput();
                            Combo combo = (Combo) e.widget;
                            final String charset = combo.getItem(combo.getSelectionIndex());
                            try {
                                contentEditor.getSite().getWorkbenchWindow().run(false, false, new IRunnableWithProgress() {
                                    @Override
                                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                                        try {
                                            contentEditorInput.getFile().setCharset(charset, monitor);
                                        } catch (CoreException e1) {
                                            throw new InvocationTargetException(e1);
                                        }
                                    }
                                });
                            } catch (InvocationTargetException e1) {
                                log.error(e1.getTargetException());
                            } catch (InterruptedException e1) {
                                // do nothing
                            }
                        }

                    }
                });
                return encodingCombo;
            }

            @Override
            public void dispose() {
                encodingCombo = null;
                super.dispose();
            }
        });
    }

    /////////////////////////////////////////////////////////
    // Actions
    /////////////////////////////////////////////////////////

    public abstract class SimpleAction extends Action {

        public SimpleAction(String id, String text, String toolTip, DBIcon icon)
        {
            super(text, icon.getImageDescriptor());
            setId(id);
            //setActionDefinitionId(id);
            setToolTipText(toolTip);
        }

        @Override
        public abstract void run();

    }

    private class FileExportAction extends SimpleAction
    {
        public FileExportAction()
        {
            super(IWorkbenchCommandConstants.FILE_EXPORT, "Export", "Save to File", DBIcon.EXPORT);
        }

        @Override
        public void run()
        {
            Shell shell = getEditor().getSite().getShell();
            final File saveFile = ContentUtils.selectFileForSave(shell);
            if (saveFile == null) {
                return;
            }
            try {
                getEditor().getSite().getWorkbenchWindow().run(true, true, new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException
                    {
                        try {
                            getEditor().getEditorInput().saveToExternalFile(saveFile, monitor);
                        }
                        catch (CoreException e) {
                            throw new InvocationTargetException(e);
                        }
                    }
                });
            }
            catch (InvocationTargetException e) {
                UIUtils.showErrorDialog(
                    shell,
                    "Could not save content",
                    "Could not save content to file '" + saveFile.getAbsolutePath() + "'",
                    e.getTargetException());
            }
            catch (InterruptedException e) {
                // do nothing
            }
        }
    }

    private class FileImportAction extends SimpleAction
    {
        public FileImportAction()
        {
            super(IWorkbenchCommandConstants.FILE_IMPORT, "Import", "Load from File", DBIcon.IMPORT);
        }

        @Override
        public void run()
        {
            Shell shell = getEditor().getSite().getShell();
            final File loadFile = ContentUtils.openFile(shell);
            if (loadFile == null) {
                return;
            }
            try {
                getEditor().getSite().getWorkbenchWindow().run(true, true, new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException
                    {
                        try {
                            getEditor().getEditorInput().loadFromExternalFile(loadFile, monitor);
                        }
                        catch (CoreException e) {
                            throw new InvocationTargetException(e);
                        }
                    }
                });
            }
            catch (InvocationTargetException e) {
                UIUtils.showErrorDialog(
                    shell,
                    "Could not load content",
                    "Could not load content from file '" + loadFile.getAbsolutePath() + "'",
                    e.getTargetException());
            }
            catch (InterruptedException e) {
                // do nothing
            }
        }
    }

    private class InfoAction extends SimpleAction
    {
        public InfoAction()
        {
            super("org.jkiss.dbeaver.lob.actions.info", "Info", "Show column information", DBIcon.TREE_INFO);
        }

        @Override
        public void run()
        {
            getEditor().toggleInfoBar();
        }
    }

    private class ApplyAction extends SimpleAction
    {
        public ApplyAction()
        {
            super("org.jkiss.dbeaver.lob.actions.apply", "Apply Changes", "Apply Changes", DBIcon.SAVE);
        }

        @Override
        public void run()
        {
            try {
                getEditor().getSite().getWorkbenchWindow().run(true, true, new IRunnableWithProgress() {
                    @Override
                    public void run(IProgressMonitor monitor)
                        throws InvocationTargetException, InterruptedException
                    {
                        getEditor().doSave(monitor);
                    }
                });
            }
            catch (InvocationTargetException e) {
                UIUtils.showErrorDialog(
                    getEditor().getSite().getShell(),
                    "Could not apply content changes",
                    "Could not apply content changes",
                    e.getTargetException());
            }
            catch (InterruptedException e) {
                // do nothing
            }

        }
    }

    private class CloseAction extends SimpleAction
    {
        public CloseAction()
        {
            super("org.jkiss.dbeaver.lob.actions.close", "Close", "Reject changes", DBIcon.REJECT);
        }

        @Override
        public void run()
        {
            ContentEditor contentEditor = getEditor();
            if (contentEditor != null) {
                contentEditor.closeValueEditor();
            }
        }
    }

}
