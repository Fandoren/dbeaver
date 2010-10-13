/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.exec;

/**
 * DBC warning
 */
public interface DBCWarning {

    public int getErrorCode();

    public String getMessage();

}
