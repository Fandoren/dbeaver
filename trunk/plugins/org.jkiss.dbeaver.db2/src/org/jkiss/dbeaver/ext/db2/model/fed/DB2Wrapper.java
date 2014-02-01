/*
 * Copyright (C) 2013      Denis Forveille titou10.titou10@gmail.com
 * Copyright (C) 2010-2014 Serge Rieder serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.ext.db2.model.fed;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.db2.model.DB2DataSource;
import org.jkiss.dbeaver.ext.db2.model.DB2GlobalObject;
import org.jkiss.dbeaver.model.DBPRefreshableObject;
import org.jkiss.dbeaver.model.impl.DBSObjectCache;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.cache.JDBCObjectSimpleCache;
import org.jkiss.dbeaver.model.meta.Association;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.utils.CommonUtils;

import java.sql.ResultSet;
import java.util.Collection;

/**
 * DB2 Federated Wrapper
 * 
 * @author Denis Forveille
 */
public class DB2Wrapper extends DB2GlobalObject implements DBPRefreshableObject {

    private static final String C_OP = "SELECT * FROM SYSCAT.WRAPOPTIONS WHERE WRAPNAME = ? ORDER BY OPTION WITH UR";

    private final DBSObjectCache<DB2Wrapper, DB2WrapperOption> optionsCache;

    private String name;
    private DB2WrapperType type;
    private Integer version;
    private String library;
    private String remarks;

    // -----------------
    // Constructors
    // -----------------

    public DB2Wrapper(DB2DataSource db2DataSource, ResultSet dbResult)
    {
        super(db2DataSource, true);

        this.name = JDBCUtils.safeGetString(dbResult, "WRAPNAME");
        this.type = CommonUtils.valueOf(DB2WrapperType.class, JDBCUtils.safeGetString(dbResult, "WRAPTYPE"));
        this.version = JDBCUtils.safeGetInteger(dbResult, "WRAPVERSION");
        this.library = JDBCUtils.safeGetString(dbResult, "LIBRARY");
        this.remarks = JDBCUtils.safeGetString(dbResult, "REMARKS");

        optionsCache = new JDBCObjectSimpleCache<DB2Wrapper, DB2WrapperOption>(DB2WrapperOption.class, C_OP, name);
    }

    @Override
    public boolean refreshObject(DBRProgressMonitor monitor) throws DBException
    {
        optionsCache.clearCache();
        return true;
    }

    // -----------------
    // Associations
    // -----------------

    @Association
    public Collection<DB2WrapperOption> getOptions(DBRProgressMonitor monitor) throws DBException
    {
        return optionsCache.getObjects(monitor, this);
    }

    // -----------------
    // Properties
    // -----------------

    @Override
    @Property(viewable = true, order = 1)
    public String getName()
    {
        return name;
    }

    @Property(viewable = true, order = 2)
    public DB2WrapperType getType()
    {
        return type;
    }

    @Property(viewable = true, order = 3)
    public Integer getVersion()
    {
        return version;
    }

    @Property(viewable = true, order = 4)
    public String getLibrary()
    {
        return library;
    }

    @Property(viewable = true, order = 5)
    public String getRemarks()
    {
        return remarks;
    }

}
