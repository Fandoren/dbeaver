/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.oracle.model;

import net.sf.jkiss.utils.CommonUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.oracle.OracleConstants;
import org.jkiss.dbeaver.ext.oracle.OracleUtils;
import org.jkiss.dbeaver.model.impl.jdbc.JDBCUtils;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCColumnKeyType;
import org.jkiss.dbeaver.model.impl.jdbc.struct.JDBCTableColumn;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.struct.DBSDataType;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.model.struct.DBSTableColumn;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * OracleTableColumn
 */
public class OracleTableColumn extends JDBCTableColumn<OracleTableBase> implements DBSTableColumn
{
    static final Log log = LogFactory.getLog(OracleTableColumn.class);

    private static Pattern enumPattern = Pattern.compile("'([^']*)'");

    public static enum KeyType implements JDBCColumnKeyType {
        PRI,
        UNI,
        MUL;

        public boolean isInUniqueKey()
        {
            return this == PRI || this == UNI;
        }

        public boolean isInReferenceKey()
        {
            return this == MUL;
        }
    }

    private String comment;
    private String defaultValue;
    private long charLength;
    private boolean autoIncrement;
    private KeyType keyType;

    private List<String> enumValues;

    public OracleTableColumn(OracleTableBase table)
    {
        super(table, false);
    }

    public OracleTableColumn(
        OracleTableBase table,
        ResultSet dbResult)
        throws DBException
    {
        super(table, true);
        loadInfo(dbResult);
    }

    private void loadInfo(ResultSet dbResult)
        throws DBException
    {
        setName(JDBCUtils.safeGetString(dbResult, OracleConstants.COL_COLUMN_NAME));
        setOrdinalPosition(JDBCUtils.safeGetInt(dbResult, OracleConstants.COL_ORDINAL_POSITION));
        String typeName = JDBCUtils.safeGetString(dbResult, OracleConstants.COL_DATA_TYPE);
        String keyTypeName = JDBCUtils.safeGetString(dbResult, OracleConstants.COL_COLUMN_KEY);
        if (!CommonUtils.isEmpty(keyTypeName)) {
            try {
                keyType = KeyType.valueOf(keyTypeName);
            } catch (IllegalArgumentException e) {
                log.debug(e);
            }
        }
        setTypeName(typeName);
        setValueType(OracleUtils.typeNameToValueType(typeName));
        DBSDataType dataType = getDataSource().getInfo().getSupportedDataType(typeName.toUpperCase());
        this.charLength = JDBCUtils.safeGetLong(dbResult, OracleConstants.COL_CHARACTER_MAXIMUM_LENGTH);
        if (this.charLength <= 0) {
            if (dataType != null) {
                setMaxLength(dataType.getPrecision());
            }
        } else {
            setMaxLength(this.charLength);
        }
        this.comment = JDBCUtils.safeGetString(dbResult, OracleConstants.COL_COLUMN_COMMENT);
        setNotNull(!"YES".equals(JDBCUtils.safeGetString(dbResult, OracleConstants.COL_IS_NULLABLE)));
        setScale(JDBCUtils.safeGetInt(dbResult, OracleConstants.COL_NUMERIC_SCALE));
        setPrecision(JDBCUtils.safeGetInt(dbResult, OracleConstants.COL_NUMERIC_PRECISION));
        this.defaultValue = JDBCUtils.safeGetString(dbResult, OracleConstants.COL_COLUMN_DEFAULT);

        String extra = JDBCUtils.safeGetString(dbResult, OracleConstants.COL_COLUMN_EXTRA);
        this.autoIncrement = extra != null && extra.contains(OracleConstants.EXTRA_AUTO_INCREMENT);

        String typeDesc = JDBCUtils.safeGetString(dbResult, OracleConstants.COL_COLUMN_TYPE);
        if (!CommonUtils.isEmpty(typeDesc) &&
            (typeName.equalsIgnoreCase(OracleConstants.TYPE_NAME_ENUM) || typeName.equalsIgnoreCase(OracleConstants.TYPE_NAME_SET)))
        {
            enumValues = new ArrayList<String>();
            Matcher enumMatcher = enumPattern.matcher(typeDesc);
            while (enumMatcher.find()) {
                String enumStr = enumMatcher.group(1);
                enumValues.add(enumStr);
            }
        }
    }

    public DBSObject getParentObject()
    {
        return getTable();
    }

    public OracleDataSource getDataSource()
    {
        return getTable().getDataSource();
    }

    public long getCharLength()
    {
        return charLength;
    }

    @Property(name = "Data Type", viewable = true, editable = true, updatable = true, order = 20, listProvider = ColumnTypeNameListProvider.class)
    @Override
    public String getTypeName()
    {
        return super.getTypeName();
    }

    @Property(name = "Length", viewable = true, editable = true, updatable = true, order = 40)
    @Override
    public long getMaxLength()
    {
        return super.getMaxLength();
    }

    @Property(name = "Not Null", viewable = true, editable = true, updatable = true, order = 50)
    @Override
    public boolean isNotNull()
    {
        return super.isNotNull();
    }

    @Property(name = "Auto Increment", viewable = true, editable = true, updatable = true, order = 51)
    public boolean isAutoIncrement()
    {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement)
    {
        this.autoIncrement = autoIncrement;
    }

    @Property(name = "Default", viewable = true, editable = true, updatable = true, order = 70)
    public String getDefaultValue()
    {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        this.defaultValue = defaultValue;
    }

    @Property(name = "Key", viewable = true, order = 80)
    public KeyType getKeyType()
    {
        return keyType;
    }

    public List<String> getEnumValues()
    {
        return enumValues;
    }

    @Property(name = "Comment", viewable = true, editable = true, updatable = true, order = 100)
    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

}
