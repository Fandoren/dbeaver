/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.navigator;

import org.eclipse.core.runtime.IAdaptable;
import org.jkiss.dbeaver.ext.IDataSourceContainerProvider;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.registry.DataSourceDescriptor;
import org.jkiss.dbeaver.registry.tree.DBXTreeNode;
import org.jkiss.dbeaver.ui.ICommandIds;
import org.jkiss.dbeaver.ui.actions.datasource.DataSourceConnectHandler;

/**
 * DBNDataSource
 */
public class DBNDataSource extends DBNTreeNode implements IAdaptable, IDataSourceContainerProvider
{
    private DataSourceDescriptor dataSource;
    private DBXTreeNode treeRoot;

    public DBNDataSource(DBNRoot parentNode, DataSourceDescriptor dataSource)
    {
        super(parentNode);
        this.dataSource = dataSource;
        this.treeRoot = dataSource.getDriver().getProviderDescriptor().getTreeDescriptor();
        this.getModel().addNode(this, true);
    }

    protected void dispose()
    {
        this.getModel().removeNode(this, true);
/*
        if (this.dataSource.isConnected()) {
            try {
                this.dataSource.disconnect(this);
            }
            catch (DBException ex) {
                log.error("Error disconnecting datasource", ex);
            }
        }
*/
        this.dataSource = null;
        super.dispose();
    }

    public DataSourceDescriptor getObject()
    {
        return dataSource;
    }

    public Object getValueObject()
    {
        return dataSource == null ? null : dataSource.getDataSource();
    }

    public String getNodeName()
    {
        return dataSource == null ? "" : dataSource.getName();
    }

    public String getNodeDescription()
    {
        return dataSource.getDescription();
    }

    public String getDefaultCommandId()
    {
        return ICommandIds.CMD_OBJECT_OPEN;
    }

    public boolean isLazyNode()
    {
        return false;
    }

    public boolean isManagable()
    {
        return true;
    }

    public DBXTreeNode getMeta()
    {
        return treeRoot;
    }

    @Override
    protected void reloadObject(DBRProgressMonitor monitor, DBSObject object) {
        dataSource = (DataSourceDescriptor) object;
    }

    public boolean initializeNode()
    {
        if (!dataSource.isConnected()) {
            DataSourceConnectHandler.execute(dataSource);
            //dataSource.connect(monitor);
        }
        return dataSource.isConnected();
    }

    public Object getAdapter(Class adapter) {
        if (adapter == DBNDataSource.class) {
            return this;
        } else if (DBSDataSourceContainer.class.isAssignableFrom(adapter)) {
            return dataSource;
        }
        return null;
    }

    public DBSDataSourceContainer getDataSourceContainer()
    {
        return dataSource;
    }

}
