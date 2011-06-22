/*
 * Copyright (c) 2011, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.model.impl.jdbc.api;

import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.qm.QMUtils;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.Calendar;

/**
 * Managable prepared statement.
 * Stores information about execution in query manager and operated progress monitor.
 */
public class JDBCPreparedStatementImpl extends JDBCStatementImpl implements JDBCPreparedStatement {

    protected final PreparedStatement original;

    public JDBCPreparedStatementImpl(
        JDBCExecutionContext connection,
        PreparedStatement original,
        String query)
    {
        super(connection);
        this.original = original;
        setQueryString(query);
    }

    public PreparedStatement getOriginal()
    {
        return original;
    }

    private void handleStatementBind(int parameterIndex, Object o)
    {
        QMUtils.getDefaultHandler().handleStatementBind(this, parameterIndex, null);
    }

    ////////////////////////////////////////////////////////////////////
    // DBC Statement overrides
    ////////////////////////////////////////////////////////////////////

    public boolean executeStatement()
        throws DBCException
    {
        try {
            return execute();
        }
        catch (SQLException e) {
            throw new DBCException(e);
        }
    }

    ////////////////////////////////////////////////////////////////////
    // Statement overrides
    ////////////////////////////////////////////////////////////////////

    public JDBCResultSet executeQuery()
        throws SQLException
    {
        this.beforeExecute();
        try {
            return new JDBCResultSetImpl(this, getOriginal().executeQuery());
        } catch (Throwable e) {
            throw super.handleExecuteError(e);
        } finally {
            super.afterExecute();
        }
    }

    public int executeUpdate()
        throws SQLException
    {
        this.beforeExecute();
        try {
            return handleExecuteResult(getOriginal().executeUpdate());
        } catch (Throwable e) {
            throw super.handleExecuteError(e);
        } finally {
            super.afterExecute();
        }
    }

    public boolean execute()
        throws SQLException
    {
        this.beforeExecute();
        try {
            return handleExecuteResult(getOriginal().execute());
        } catch (Throwable e) {
            throw super.handleExecuteError(e);
        } finally {
            super.afterExecute();
        }
    }

    public void setNull(int parameterIndex, int sqlType)
        throws SQLException
    {
        getOriginal().setNull(parameterIndex, sqlType);

        handleStatementBind(parameterIndex, null);
    }

    public void setBoolean(int parameterIndex, boolean x)
        throws SQLException
    {
        getOriginal().setBoolean(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setByte(int parameterIndex, byte x)
        throws SQLException
    {
        getOriginal().setByte(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setShort(int parameterIndex, short x)
        throws SQLException
    {
        getOriginal().setShort(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setInt(int parameterIndex, int x)
        throws SQLException
    {
        getOriginal().setInt(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setLong(int parameterIndex, long x)
        throws SQLException
    {
        getOriginal().setLong(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setFloat(int parameterIndex, float x)
        throws SQLException
    {
        getOriginal().setFloat(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setDouble(int parameterIndex, double x)
        throws SQLException
    {
        getOriginal().setDouble(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setBigDecimal(int parameterIndex, BigDecimal x)
        throws SQLException
    {
        getOriginal().setBigDecimal(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setString(int parameterIndex, String x)
        throws SQLException
    {
        getOriginal().setString(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setBytes(int parameterIndex, byte[] x)
        throws SQLException
    {
        getOriginal().setBytes(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setDate(int parameterIndex, Date x)
        throws SQLException
    {
        getOriginal().setDate(parameterIndex, x);
        
        handleStatementBind(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x)
        throws SQLException
    {
        getOriginal().setTime(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setTimestamp(
        int parameterIndex, Timestamp x)
        throws SQLException
    {
        getOriginal().setTimestamp(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, int length)
        throws SQLException
    {
        getOriginal().setAsciiStream(parameterIndex, x, length);

        handleStatementBind(parameterIndex, x);
    }

    @Deprecated
    public void setUnicodeStream(int parameterIndex, InputStream x, int length)
        throws SQLException
    {
        getOriginal().setUnicodeStream(parameterIndex, x, length);

        handleStatementBind(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, int length)
        throws SQLException
    {
        getOriginal().setBinaryStream(parameterIndex, x, length);

        handleStatementBind(parameterIndex, x);
    }

    public void clearParameters()
        throws SQLException
    {
        getOriginal().clearParameters();
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType)
        throws SQLException
    {
        getOriginal().setObject(parameterIndex, x, targetSqlType);

        handleStatementBind(parameterIndex, x);
    }

    public void setObject(int parameterIndex, Object x)
        throws SQLException
    {
        getOriginal().setObject(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void addBatch()
        throws SQLException
    {
        getOriginal().addBatch();
    }

    public void setCharacterStream(int parameterIndex, Reader reader, int length)
        throws SQLException
    {
        getOriginal().setCharacterStream(parameterIndex, reader, length);

        handleStatementBind(parameterIndex, reader);
    }

    public void setRef(int parameterIndex, Ref x)
        throws SQLException
    {
        getOriginal().setRef(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setBlob(int parameterIndex, Blob x)
        throws SQLException
    {
        getOriginal().setBlob(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setClob(int parameterIndex, Clob x)
        throws SQLException
    {
        getOriginal().setClob(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setArray(int parameterIndex, Array x)
        throws SQLException
    {
        getOriginal().setArray(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public ResultSetMetaData getMetaData()
        throws SQLException
    {
        return getOriginal().getMetaData();
    }

    public void setDate(int parameterIndex, Date x, Calendar cal)
        throws SQLException
    {
        getOriginal().setDate(parameterIndex, x, cal);

        handleStatementBind(parameterIndex, x);
    }

    public void setTime(int parameterIndex, Time x, Calendar cal)
        throws SQLException
    {
        getOriginal().setTime(parameterIndex, x, cal);

        handleStatementBind(parameterIndex, x);
    }

    public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal)
        throws SQLException
    {
        getOriginal().setTimestamp(parameterIndex, x, cal);

        handleStatementBind(parameterIndex, x);
    }

    public void setNull(int parameterIndex, int sqlType, String typeName)
        throws SQLException
    {
        getOriginal().setNull(parameterIndex, sqlType, typeName);

        handleStatementBind(parameterIndex, null);
    }

    public void setURL(int parameterIndex, URL x)
        throws SQLException
    {
        getOriginal().setURL(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public ParameterMetaData getParameterMetaData()
        throws SQLException
    {
        return getOriginal().getParameterMetaData();
    }

    public void setRowId(int parameterIndex, RowId x)
        throws SQLException
    {
        getOriginal().setRowId(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setNString(int parameterIndex, String x)
        throws SQLException
    {
        getOriginal().setNString(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setNCharacterStream(int parameterIndex, Reader x, long length)
        throws SQLException
    {
        getOriginal().setNCharacterStream(parameterIndex, x, length);

        handleStatementBind(parameterIndex, x);
    }

    public void setNClob(int parameterIndex, NClob x)
        throws SQLException
    {
        getOriginal().setNClob(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setClob(int parameterIndex, Reader x, long length)
        throws SQLException
    {
        getOriginal().setClob(parameterIndex, x, length);

        handleStatementBind(parameterIndex, x);
    }

    public void setBlob(int parameterIndex, InputStream x, long length)
        throws SQLException
    {
        getOriginal().setBlob(parameterIndex, x, length);

        handleStatementBind(parameterIndex, x);
    }

    public void setNClob(int parameterIndex, Reader x, long length)
        throws SQLException
    {
        getOriginal().setNClob(parameterIndex, x, length);

        handleStatementBind(parameterIndex, x);
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject)
        throws SQLException
    {
        getOriginal().setSQLXML(parameterIndex, xmlObject);

        handleStatementBind(parameterIndex, xmlObject);
    }

    public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)
        throws SQLException
    {
        getOriginal().setObject(parameterIndex, x, targetSqlType, scaleOrLength);

        handleStatementBind(parameterIndex, x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length)
        throws SQLException
    {
        getOriginal().setAsciiStream(parameterIndex, x, length);

        handleStatementBind(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length)
        throws SQLException
    {
        getOriginal().setBinaryStream(parameterIndex, x, length);

        handleStatementBind(parameterIndex, x);
    }

    public void setCharacterStream(int parameterIndex, Reader x, long length)
        throws SQLException
    {
        getOriginal().setCharacterStream(parameterIndex, x, length);

        handleStatementBind(parameterIndex, x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x)
        throws SQLException
    {
        getOriginal().setAsciiStream(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x)
        throws SQLException
    {
        getOriginal().setBinaryStream(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setCharacterStream(int parameterIndex, Reader x)
        throws SQLException
    {
        getOriginal().setCharacterStream(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setNCharacterStream(int parameterIndex, Reader x)
        throws SQLException
    {
        getOriginal().setNCharacterStream(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setClob(int parameterIndex, Reader x)
        throws SQLException
    {
        getOriginal().setClob(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setBlob(int parameterIndex, InputStream x)
        throws SQLException
    {
        getOriginal().setBlob(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

    public void setNClob(int parameterIndex, Reader x)
        throws SQLException
    {
        getOriginal().setNClob(parameterIndex, x);

        handleStatementBind(parameterIndex, x);
    }

}
