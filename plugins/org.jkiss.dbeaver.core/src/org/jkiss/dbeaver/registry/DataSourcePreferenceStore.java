/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.registry;

import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.utils.AbstractPreferenceStore;

import java.io.IOException;

/**
 * DataSourcePreferenceStore
 */
public class DataSourcePreferenceStore extends AbstractPreferenceStore
{
    private final DataSourceDescriptor dataSourceDescriptor;

    DataSourcePreferenceStore(DataSourceDescriptor dataSourceDescriptor)
    {
        super(DBeaverCore.getInstance().getGlobalPreferenceStore());
        this.dataSourceDescriptor = dataSourceDescriptor;
    }

    public DataSourceDescriptor getDataSourceDescriptor()
    {
        return dataSourceDescriptor;
    }

    @Override
    public void save()
        throws IOException
    {
        dataSourceDescriptor.getRegistry().flushConfig();
    }

}
