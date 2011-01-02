/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.runtime.load;

import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.navigator.DBNNode;

/**
 * Lazy loading service
 * @param <RESULT> result type
 */
public abstract class DatabaseLoadService<RESULT> extends AbstractLoadService<RESULT> {

    private DBPDataSource dataSource;

    protected DatabaseLoadService(DBPDataSource dataSource) {
        this.dataSource = dataSource;
    }

    protected DatabaseLoadService(String serviceName, DBPDataSource dataSource) {
        super(serviceName);
        this.dataSource = dataSource;
    }

    protected DatabaseLoadService(String serviceName, DBNNode node) {
        super(serviceName);
        this.dataSource = node.getObject() == null ? null : node.getObject().getDataSource();
    }

    public Object getFamily() {
        return dataSource;
    }
}