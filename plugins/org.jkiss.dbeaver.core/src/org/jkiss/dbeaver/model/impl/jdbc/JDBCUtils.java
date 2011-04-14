/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.impl.jdbc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCConnector;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.runtime.DBRBlockingObject;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSConstraintCascade;
import org.jkiss.dbeaver.model.struct.DBSDataKind;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.utils.ContentUtils;

import java.sql.*;

/**
 * JDBCUtils
 */
public class JDBCUtils {
    static final Log log = LogFactory.getLog(JDBCUtils.class);

    public static String safeGetString(ResultSet dbResult, String columnName)
    {
        try {
            return dbResult.getString(columnName);
        } catch (SQLException e) {
            debugColumnRead(columnName, e);
            return null;
        }
    }

    public static String safeGetStringTrimmed(ResultSet dbResult, String columnName)
    {
        try {
            final String value = dbResult.getString(columnName);
            if (value != null && !value.isEmpty()) {
                return value.trim();
            } else {
                return value;
            }
        } catch (SQLException e) {
            debugColumnRead(columnName, e);
            return null;
        }
    }

    public static String safeGetString(ResultSet dbResult, int columnIndex)
    {
        try {
            return dbResult.getString(columnIndex);
        } catch (SQLException e) {
            debugColumnRead(columnIndex, e);
            return null;
        }
    }

    public static String safeGetStringTrimmed(ResultSet dbResult, int columnIndex)
    {
        try {
            final String value = dbResult.getString(columnIndex);
            if (value != null && !value.isEmpty()) {
                return value.trim();
            } else {
                return value;
            }
        } catch (SQLException e) {
            debugColumnRead(columnIndex, e);
            return null;
        }
    }

    public static int safeGetInt(ResultSet dbResult, String columnName)
    {
        try {
            return dbResult.getInt(columnName);
        } catch (SQLException e) {
            debugColumnRead(columnName, e);
            return 0;
        }
    }

    public static long safeGetLong(ResultSet dbResult, String columnName)
    {
        try {
            return dbResult.getLong(columnName);
        } catch (SQLException e) {
            debugColumnRead(columnName, e);
            return 0;
        }
    }

    public static double safeGetDouble(ResultSet dbResult, String columnName)
    {
        try {
            return dbResult.getDouble(columnName);
        } catch (SQLException e) {
            debugColumnRead(columnName, e);
            return 0.0;
        }
    }

    public static boolean safeGetBoolean(ResultSet dbResult, String columnName)
    {
        try {
            return dbResult.getBoolean(columnName);
        } catch (SQLException e) {
            debugColumnRead(columnName, e);
            return false;
        }
    }

    public static byte[] safeGetBytes(ResultSet dbResult, String columnName)
    {
        try {
            return dbResult.getBytes(columnName);
        } catch (SQLException e) {
            debugColumnRead(columnName, e);
            return null;
        }
    }

    public static Timestamp safeGetTimestamp(ResultSet dbResult, String columnName)
    {
        try {
            return dbResult.getTimestamp(columnName);
        } catch (SQLException e) {
            debugColumnRead(columnName, e);
            return null;
        }
    }

    public static Timestamp safeGetTimestamp(ResultSet dbResult, int columnIndex)
    {
        try {
            return dbResult.getTimestamp(columnIndex);
        } catch (SQLException e) {
            debugColumnRead(columnIndex, e);
            return null;
        }
    }

    public static int getDataTypeByName(int valueType, String typeName)
    {
        if (valueType == java.sql.Types.OTHER) {
            if ("BLOB".equalsIgnoreCase(typeName)) {
                return java.sql.Types.BLOB;
            } else if ("CLOB".equalsIgnoreCase(typeName)) {
                return java.sql.Types.CLOB;
            } else if ("NCLOB".equalsIgnoreCase(typeName)) {
                return java.sql.Types.NCLOB;
            }
        }
        return valueType;
    }

    public static DBSDataKind getDataKind(DBSTypedObject type)
    {
        switch (type.getValueType()) {
            case java.sql.Types.BOOLEAN:
                return DBSDataKind.BOOLEAN;
            case java.sql.Types.CHAR:
            case java.sql.Types.VARCHAR:
            case java.sql.Types.NVARCHAR:
            case java.sql.Types.LONGVARCHAR:
            case java.sql.Types.LONGNVARCHAR:
                return DBSDataKind.STRING;
            case java.sql.Types.BIGINT:
            case java.sql.Types.DECIMAL:
            case java.sql.Types.DOUBLE:
            case java.sql.Types.FLOAT:
            case java.sql.Types.INTEGER:
            case java.sql.Types.NUMERIC:
            case java.sql.Types.REAL:
            case java.sql.Types.SMALLINT:
                return DBSDataKind.NUMERIC;
            case java.sql.Types.BIT:
            case java.sql.Types.TINYINT:
                if (type.getTypeName().toLowerCase().contains("bool")) {
                    // Declared as numeric but actually it's a boolean
                    return DBSDataKind.BOOLEAN;
                } else {
                    return DBSDataKind.NUMERIC;
                }
            case java.sql.Types.DATE:
            case java.sql.Types.TIME:
            case java.sql.Types.TIMESTAMP:
                return DBSDataKind.DATETIME;
            case java.sql.Types.BINARY:
            case java.sql.Types.VARBINARY:
            case java.sql.Types.LONGVARBINARY:
                return DBSDataKind.BINARY;
            case java.sql.Types.BLOB:
            case java.sql.Types.CLOB:
            case java.sql.Types.NCLOB:
                return DBSDataKind.LOB;
            case java.sql.Types.STRUCT:
                return DBSDataKind.STRUCT;
            case java.sql.Types.ARRAY:
                return DBSDataKind.ARRAY;
            case java.sql.Types.ROWID:
                // threat ROWID as string
                return DBSDataKind.STRING;
        }
        return DBSDataKind.UNKNOWN;
    }

    public static DBIcon getDataIcon(DBSTypedObject type)
    {
        switch (JDBCUtils.getDataKind(type)) {
            case BOOLEAN:
                return DBIcon.TYPE_BOOLEAN;
            case STRING:
                return DBIcon.TYPE_STRING;
            case NUMERIC:
                if (type.getValueType() == java.sql.Types.BIT) {
                    return DBIcon.TYPE_BOOLEAN;
                } else {
                    return DBIcon.TYPE_NUMBER;
                }
            case DATETIME:
                return DBIcon.TYPE_DATETIME;
            case BINARY:
                return DBIcon.TYPE_BINARY;
            case LOB:
                return DBIcon.TYPE_LOB;
            case ARRAY:
                return DBIcon.TYPE_ARRAY;
            case STRUCT:
                return DBIcon.TYPE_STRUCT;
            default:
                return DBIcon.TYPE_UNKNOWN;
        }
    }

    public static Object getParameter(ResultSet dbResult, int columnIndex, int columnType)
        throws DBCException
    {
        try {
            switch (columnType) {
                case java.sql.Types.BOOLEAN:
                case java.sql.Types.BIT:
                    try {
                        return dbResult.getByte(columnIndex);
                    } catch (SQLException e) {
                        // Try to get as int
                        return dbResult.getInt(columnIndex);
                    }
                case java.sql.Types.CHAR:
                case java.sql.Types.VARCHAR:
                case java.sql.Types.LONGVARCHAR:
                    return dbResult.getString(columnIndex);
                case java.sql.Types.BIGINT:
                case java.sql.Types.DECIMAL:
                    return dbResult.getBigDecimal(columnIndex);
                case java.sql.Types.DOUBLE:
                    return dbResult.getDouble(columnIndex);
                case java.sql.Types.FLOAT:
                    return dbResult.getFloat(columnIndex);
                case java.sql.Types.INTEGER:
                    return dbResult.getInt(columnIndex);
                case java.sql.Types.NUMERIC:
                case java.sql.Types.REAL:
                    return dbResult.getBigDecimal(columnIndex);
                case java.sql.Types.SMALLINT:
                    return dbResult.getShort(columnIndex);
                case java.sql.Types.TINYINT:
                    return dbResult.getByte(columnIndex);
                case java.sql.Types.DATE:
                    return dbResult.getDate(columnIndex);
                case java.sql.Types.TIME:
                    return dbResult.getTime(columnIndex);
                case java.sql.Types.TIMESTAMP:
                    return dbResult.getTimestamp(columnIndex);
                case java.sql.Types.BLOB:
                    return dbResult.getBlob(columnIndex);
                case java.sql.Types.CLOB:
                    return dbResult.getClob(columnIndex);
                case java.sql.Types.VARBINARY:
                case java.sql.Types.LONGVARBINARY:
                    // Try to use BLOB wrapper
                    return dbResult.getBlob(columnIndex);
                case java.sql.Types.STRUCT:
                    return dbResult.getObject(columnIndex);
                case java.sql.Types.ARRAY:
                    return dbResult.getArray(columnIndex);
                default:
                    return new JDBCUnknownType(
                        columnType,
                        dbResult.getObject(columnIndex));
            }
        } catch (SQLException ex) {
            //throw new DBCException(ex);
            // get by parameter type failed - try to use generic getter
        }
        try {
            return dbResult.getObject(columnIndex);
        } catch (SQLException ex) {
            throw new DBCException(ex);
        }
    }

    public static void startConnectionBlock(
        DBRProgressMonitor monitor,
        JDBCConnector connector,
        String taskName)
    {
        monitor.startBlock(makeBlockingObject(connector.getConnection()), taskName);
    }

    public static void endConnectionBlock(
        DBRProgressMonitor monitor)
    {
        monitor.endBlock();
    }

    private static DBRBlockingObject makeBlockingObject(final Connection connection)
    {
        return new DBRBlockingObject() {
            public void cancelBlock()
                throws DBException
            {
                try {
                    connection.close();
                } catch (SQLException e) {
                    throw new DBCException("Can't close connection", e);
                }
            }
        };
    }

    public static String normalizeIdentifier(String value)
    {
        return value == null ? null : value.trim();
    }

    public static void dumpResultSet(ResultSet dbResult)
    {
        try {
            ResultSetMetaData md = dbResult.getMetaData();
            int count = md.getColumnCount();
            dumpResultSetMetaData(dbResult);
            while (dbResult.next()) {
                for (int i = 1; i <= count; i++) {
                    String colValue = dbResult.getString(i);
                    System.out.print(colValue + "\t");
                }
                System.out.println();
            }
            System.out.println();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void dumpResultSetMetaData(ResultSet dbResult)
    {
        try {
            ResultSetMetaData md = dbResult.getMetaData();
            int count = md.getColumnCount();
            for (int i = 1; i <= count; i++) {
                System.out.print(md.getColumnName(i) + " [" + md.getColumnTypeName(i)+ "]\t");
            }
            System.out.println();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isConnectionAlive(Connection connection)
    {
        try {
            if (connection.isClosed()) {
                return false;
            }
            connection.getMetaData().getTables(null, null, "UN_EXIST_DBEAVER_TBL_NAME1978", null);
            return true;
        } catch (SQLException e) {
            log.debug(e);
            return false;
        }
    }

    public static void scrollResultSet(ResultSet dbResult, long offset)
        throws SQLException
    {
        // Scroll to first row
        boolean scrolled = false;
        try {
            scrolled = dbResult.absolute((int) offset);
        } catch (SQLException e) {
            // Seems to be not supported
            log.debug(e.getMessage());
        } catch (AbstractMethodError e) {
            // Seems to be legacy JDBC
            log.debug(e.getMessage());
        } catch (UnsupportedOperationException e) {
            // Seems to be legacy JDBC
            log.debug(e.getMessage());
        }
        if (!scrolled) {
            // Just fetch first 'firstRow' rows
            for (long i = 1; i <= offset; i++) {
                try {
                    dbResult.next();
                } catch (SQLException e) {
                    throw new SQLException("Could not scroll result set to " + offset + " row", e);
                }
            }
        }
    }

    public static void reportWarnings(JDBCExecutionContext context, SQLWarning rootWarning)
    {
        for (SQLWarning warning = rootWarning; warning != null; warning = warning.getNextWarning()) {
            log.warn(
                "SQL Warning (DataSource: " + context.getDataSource().getContainer().getName() +
                    "; Code: " + warning.getErrorCode() +
                    "; State: " + warning.getSQLState() + "): " +
                    warning.getLocalizedMessage());
        }
    }

    public static String limitQueryLength(String query, int maxLength)
    {
        return query == null || query.length() <= maxLength ? query : query.substring(0, maxLength);
    }

/*
    public static boolean isDriverODBC(DBCExecutionContext context)
    {
        return context.getDataSource().getContainer().getDriver().getDriverClassName().contains("Odbc");
    }
*/

    public static DBSConstraintCascade getCascadeFromNum(int num)
    {
        switch (num) {
            case DatabaseMetaData.importedKeyNoAction: return DBSConstraintCascade.NO_ACTION;
            case DatabaseMetaData.importedKeyCascade: return DBSConstraintCascade.CASCADE;
            case DatabaseMetaData.importedKeySetNull: return DBSConstraintCascade.SET_NULL;
            case DatabaseMetaData.importedKeySetDefault: return DBSConstraintCascade.SET_DEFAULT;
            case DatabaseMetaData.importedKeyRestrict: return DBSConstraintCascade.RESTRICT;
            default: return DBSConstraintCascade.UNKNOWN;
        }
    }

    private static void debugColumnRead(String columnName, SQLException error)
    {
        log.debug("Can't get column '" + columnName + "': " + error.getMessage());
    }

    private static void debugColumnRead(int columnIndex, SQLException error)
    {
        log.debug("Can't get column #" + columnIndex + ": " + error.getMessage());
    }

}