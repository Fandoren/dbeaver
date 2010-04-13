package org.jkiss.dbeaver.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.ui.PlatformUI;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.utils.DBeaverUtils;

public class DisconnectAction extends DataSourceAction
{
    @Override
    protected void updateAction(IAction action) {
        DBSDataSourceContainer dataSourceContainer = getDataSourceContainer(false);
        action.setEnabled(dataSourceContainer != null && dataSourceContainer.isConnected());
    }

    public void run(IAction action)
    {
        DBSDataSourceContainer dataSourceContainer = getDataSourceContainer(false);
        if (dataSourceContainer != null && dataSourceContainer.isConnected()) {
            try {
                dataSourceContainer.disconnect(this);
            }
            catch (DBException ex) {
                DBeaverUtils.showErrorDialog(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
                    "Disconnect", "Can't disconnect from '" + dataSourceContainer.getName() + "'", ex);
            }
        }
    }

}