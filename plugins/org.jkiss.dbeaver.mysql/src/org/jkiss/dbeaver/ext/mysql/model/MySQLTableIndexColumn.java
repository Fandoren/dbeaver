/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.mysql.model;

import org.jkiss.dbeaver.model.impl.struct.AbstractTableIndexColumn;
import org.jkiss.dbeaver.model.meta.Property;

/**
 * GenericIndexColumn
 */
public class MySQLTableIndexColumn extends AbstractTableIndexColumn
{
    private MySQLTableIndex index;
    private MySQLTableColumn tableColumn;
    private int ordinalPosition;
    private boolean ascending;
    private boolean nullable;

    public MySQLTableIndexColumn(
        MySQLTableIndex index,
        MySQLTableColumn tableColumn,
        int ordinalPosition,
        boolean ascending,
        boolean nullable)
    {
        this.index = index;
        this.tableColumn = tableColumn;
        this.ordinalPosition = ordinalPosition;
        this.ascending = ascending;
        this.nullable = nullable;
    }

    MySQLTableIndexColumn(MySQLTableIndex toIndex, MySQLTableIndexColumn source)
    {
        this.index = toIndex;
        this.tableColumn = source.tableColumn;
        this.ordinalPosition = source.ordinalPosition;
        this.ascending = source.ascending;
        this.nullable = source.nullable;
    }

    @Override
    public MySQLTableIndex getIndex()
    {
        return index;
    }

    //@Property(name = "Name", viewable = true, order = 1)
    @Override
    public String getName()
    {
        return tableColumn.getName();
    }

    @Override
    @Property(id = "name", name = "Column", viewable = true, order = 1)
    public MySQLTableColumn getTableColumn()
    {
        return tableColumn;
    }

    @Override
    @Property(name = "Position", viewable = false, order = 2)
    public int getOrdinalPosition()
    {
        return ordinalPosition;
    }

    @Override
    @Property(name = "Ascending", viewable = true, order = 3)
    public boolean isAscending()
    {
        return ascending;
    }

    @Property(name = "Nullable", viewable = true, order = 4)
    public boolean isNullable()
    {
        return nullable;
    }

    @Override
    public String getDescription()
    {
        return tableColumn.getDescription();
    }

    @Override
    public MySQLTableIndex getParentObject()
    {
        return index;
    }

    @Override
    public MySQLDataSource getDataSource()
    {
        return index.getDataSource();
    }

}
