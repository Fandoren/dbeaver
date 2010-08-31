/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

/*
 * Created on Jul 13, 2004
 */
package org.jkiss.dbeaver.ext.erd.model;

import org.jkiss.dbeaver.model.struct.DBSTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Model object representing a relational database Table
 * Also includes the bounds of the table so that the diagram can be 
 * restored following a save, although ideally this should be
 * in a separate diagram specific model hierarchy
 * @author Serge Rieder
 */
public class ERDTable extends ERDObject<DBSTable>
{

	private List<ERDTableColumn> columns = new ArrayList<ERDTableColumn>();

	private List<ERDAssociation> primaryKeyRelationships = new ArrayList<ERDAssociation>();
	private List<ERDAssociation> foreignKeyRelationships = new ArrayList<ERDAssociation>();

    public ERDTable(DBSTable dbsTable) {
        super(dbsTable);
    }

	public void addColumn(ERDTableColumn column)
	{
		if (columns.contains(column))
		{
			throw new IllegalArgumentException("Column already present");
		}
		columns.add(column);
		firePropertyChange(CHILD, null, column);
	}

	public void addColumn(ERDTableColumn column, int index)
	{
		if (columns.contains(column))
		{
			throw new IllegalArgumentException("Column already present");
		}
		columns.add(index, column);
		firePropertyChange(CHILD, null, column);
	}

	public void removeColumn(ERDTableColumn column)
	{
		columns.remove(column);
		firePropertyChange(CHILD, column, null);
	}

	public void switchColumn(ERDTableColumn column, int index)
	{
		columns.remove(column);
		columns.add(index, column);
		firePropertyChange(REORDER, this, column);
	}

	/**
	 * Sets name without firing off any event notifications
	 * 
	 * @param name
	 *            The name to set.
	 *
	public void setName(String name)
	{
		this.name = name;
	}*/

	/**
	 * Adds relationship where the current object is the foreign key table in a relationship
	 * 
	 * @param table
	 *            the primary key relationship
	 */
	public void addForeignKeyRelationship(ERDAssociation table)
	{
		foreignKeyRelationships.add(table);
		firePropertyChange(OUTPUT, null, table);
	}

	/**
	 * Adds relationship where the current object is the primary key table in a relationship
	 * 
	 * @param table
	 *            the foreign key relationship
	 */
	public void addPrimaryKeyRelationship(ERDAssociation table)
	{
		primaryKeyRelationships.add(table);
		firePropertyChange(INPUT, null, table);
	}

	/**
	 * Removes relationship where the current object is the foreign key table in a relationship
	 * 
	 * @param table
	 *            the primary key relationship
	 */
	public void removeForeignKeyRelationship(ERDAssociation table)
	{
		foreignKeyRelationships.remove(table);
		firePropertyChange(OUTPUT, table, null);
	}

	/**
	 * Removes relationship where the current object is the primary key table in a relationship
	 * 
	 * @param table
	 *            the foreign key relationship
	 */
	public void removePrimaryKeyRelationship(ERDAssociation table)
	{
		primaryKeyRelationships.remove(table);
		firePropertyChange(INPUT, table, null);
	}

	/**
	 * If modified, sets name and fires off event notification
	 * 
	 * @param name
	 *            The name to set.
	 *
	public void modifyName(String name)
	{
		String oldName = this.name;
		if (!name.equals(oldName))
		{
			this.name = name;
			firePropertyChange(NAME, null, name);
		}
	}

	public String getName()
	{
		return name;
	}
    */

	public List getColumns()
	{
		return columns;
	}

	/**
	 * @return Returns the foreignKeyRelationships.
	 */
	public List<ERDAssociation> getForeignKeyRelationships()
	{
		return foreignKeyRelationships;
	}

	/**
	 * @return Returns the primaryKeyRelationships.
	 */
	public List<ERDAssociation> getPrimaryKeyRelationships()
	{
		return primaryKeyRelationships;
	}

	public String toString()
	{
		return object.getName();
	}

	/**
	 * hashcode implementation for use as key in Map
	 */
	public int hashCode()
	{
		return object.hashCode();
	}

	/**
	 * equals implementation for use as key in Map
	 */
	public boolean equals(Object o)
	{
		if (o == null || !(o instanceof ERDTable))
			return false;
        return object.equals(((ERDTable)o).object);
	}
}