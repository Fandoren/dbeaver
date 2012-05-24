/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.oracle.model;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableIndex;
import org.jkiss.dbeaver.model.meta.Association;
import org.jkiss.dbeaver.model.meta.LazyProperty;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSIndexType;
import org.jkiss.dbeaver.model.struct.DBSObjectLazy;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * OracleTableIndex
 */
public class OracleTableIndex extends JDBCTableIndex<OracleTablePhysical> implements DBSObjectLazy
{
    private Object tablespace;
    private boolean nonUnique;
    private List<OracleTableIndexColumn> columns;

    public OracleTableIndex(
        OracleTablePhysical table,
        String indexName,
        ResultSet dbResult)
    {
        super(table, indexName, null, true);
        String indexTypeName = JDBCUtils.safeGetString(dbResult, "INDEX_TYPE");
        this.nonUnique = JDBCUtils.safeGetInt(dbResult, "UNIQUENESS") != 0;
        if (OracleConstants.INDEX_TYPE_NORMAL.getId().equals(indexTypeName)) {
            indexType = OracleConstants.INDEX_TYPE_NORMAL;
        } else if (OracleConstants.INDEX_TYPE_BITMAP.getId().equals(indexTypeName)) {
            indexType = OracleConstants.INDEX_TYPE_BITMAP;
        } else if (OracleConstants.INDEX_TYPE_FUNCTION_BASED_NORMAL.getId().equals(indexTypeName)) {
            indexType = OracleConstants.INDEX_TYPE_FUNCTION_BASED_NORMAL;
        } else if (OracleConstants.INDEX_TYPE_FUNCTION_BASED_BITMAP.getId().equals(indexTypeName)) {
            indexType = OracleConstants.INDEX_TYPE_FUNCTION_BASED_BITMAP;
        } else if (OracleConstants.INDEX_TYPE_DOMAIN.getId().equals(indexTypeName)) {
            indexType = OracleConstants.INDEX_TYPE_DOMAIN;
        } else {
            indexType = DBSIndexType.OTHER;
        }
        this.tablespace = JDBCUtils.safeGetString(dbResult, "TABLESPACE_NAME");
    }

    public OracleTableIndex(OracleTablePhysical parent, String name, boolean unique, DBSIndexType indexType)
    {
        super(parent, name, indexType, false);
        this.nonUnique = !unique;

    }

    @Override
    public OracleDataSource getDataSource()
    {
        return getTable().getDataSource();
    }

    @Override
    @Property(name = "Unique", viewable = true, order = 5)
    public boolean isUnique()
    {
        return !nonUnique;
    }

    @Override
    public Object getLazyReference(Object propertyId)
    {
        return tablespace;
    }

    @Property(name = "Tablespace", viewable = true, order = 10)
    @LazyProperty(cacheValidator = OracleTablespace.TablespaceReferenceValidator.class)
    public Object getTablespace(DBRProgressMonitor monitor) throws DBException
    {
        return OracleTablespace.resolveTablespaceReference(monitor, this, null);
    }

    @Override
    public String getDescription()
    {
        return null;
    }

    @Override
    public List<OracleTableIndexColumn> getColumns(DBRProgressMonitor monitor)
    {
        return columns;
    }

    @Association
    public OracleTableIndexColumn getColumn(String columnName)
    {
        return DBUtils.findObject(columns, columnName);
    }

    void setColumns(List<OracleTableIndexColumn> columns)
    {
        this.columns = columns;
    }

    public void addColumn(OracleTableIndexColumn column)
    {
        if (columns == null) {
            columns = new ArrayList<OracleTableIndexColumn>();
        }
        columns.add(column);
    }

    @Override
    public String getFullQualifiedName()
    {
        return DBUtils.getFullQualifiedName(getDataSource(),
            getTable().getContainer(),
            this);
    }

    @Override
    public String toString()
    {
        return getFullQualifiedName();
    }
}
