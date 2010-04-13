package org.jkiss.dbeaver.ui.editors.folder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.jface.viewers.Viewer;
import org.jkiss.dbeaver.registry.DataSourceRegistry;
import org.jkiss.dbeaver.ui.controls.itemlist.ItemListControl;
import org.jkiss.dbeaver.model.meta.DBMModel;
import org.jkiss.dbeaver.ext.ui.IMetaModelView;
import org.jkiss.dbeaver.ext.ui.IDataSourceUser;
import org.jkiss.dbeaver.utils.ViewUtils;
import org.jkiss.dbeaver.model.meta.IDBMListener;
import org.jkiss.dbeaver.model.meta.DBMEvent;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.model.dbc.DBCSession;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.DBException;

/**
 * FolderEditor
 */
public class FolderEditor extends EditorPart implements IDBMListener, IMetaModelView, IDataSourceUser
{
    static Log log = LogFactory.getLog(FolderEditor.class);

    private FolderEditorInput folderInput;
    private ItemListControl itemControl;

    public void doSave(IProgressMonitor monitor)
    {
    }

    public void doSaveAs()
    {
    }

    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        setSite(site);
        setInput(input);
        this.folderInput = (FolderEditorInput)input;
        setPartName(this.folderInput.getName());
        setTitleImage(this.folderInput.getImageDescriptor().createImage());

        DBeaverCore.getInstance().getMetaModel().addListener(this);
    }

    public void dispose()
    {
        DBeaverCore.getInstance().getMetaModel().removeListener(this);
        super.dispose();
    }

    public boolean isDirty()
    {
        return false;
    }

    public boolean isSaveAsAllowed()
    {
        return false;
    }

    public void createPartControl(Composite parent)
    {
        itemControl = new ItemListControl(parent, SWT.NONE, this, folderInput.getFolder());
        itemControl.fillData();
        // Hook context menu
        ViewUtils.addContextMenu(this);
        // Add drag and drop support
        ViewUtils.addDragAndDropSupport(this);
        getSite().setSelectionProvider(itemControl.getSelectionProvider());
    }

    public void setFocus()
    {
    }

    public void nodeChanged(DBMEvent event)
    {
        if (event.getNode() == folderInput.getFolder()) {
            if (event.getAction() == DBMEvent.Action.REMOVE) {
                getSite().getShell().getDisplay().asyncExec(new Runnable() { public void run() {
                    getSite().getWorkbenchWindow().getActivePage().closeEditor(FolderEditor.this, false);
                }});
            }
        }
    }

    public DBMModel getMetaModel()
    {
        return folderInput.getFolder().getModel();
    }

    public Viewer getViewer()
    {
        return itemControl.getViewer();
    }

    public IWorkbenchPart getWorkbenchPart()
    {
        return this;
    }

    public DBSDataSourceContainer getDataSourceContainer() {
        DBPDataSource dataSource = getDataSource();
        return dataSource == null ? null : dataSource.getContainer();
    }

    public DBPDataSource getDataSource() {
        return folderInput == null ? null : folderInput.getDatabaseObject().getDataSource();
    }

    public DBCSession getSession() throws DBException {
        return null;
    }

}
