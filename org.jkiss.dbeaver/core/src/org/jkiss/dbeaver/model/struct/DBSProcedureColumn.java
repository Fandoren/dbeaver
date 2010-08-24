/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.struct;

/**
 * DBSIndex
 */
public interface DBSProcedureColumn extends DBSColumnDefinition, DBSEntityAttribute
{

    DBSProcedure getProcedure();

    DBSProcedureColumnType getColumnType();

}