/*
 * Copyright (C) 2010-2012 Serge Rieder
 * serge@jkiss.org
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
package org.jkiss.dbeaver.model.impl.jdbc.data;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.core.CoreMessages;
import org.jkiss.dbeaver.core.DBeaverUI;
import org.jkiss.dbeaver.ext.IContentEditorPart;
import org.jkiss.dbeaver.model.data.*;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.model.exec.DBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCExecutionContext;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCPreparedStatement;
import org.jkiss.dbeaver.model.exec.jdbc.JDBCResultSet;
import org.jkiss.dbeaver.model.impl.BytesContentStorage;
import org.jkiss.dbeaver.model.impl.ExternalContentStorage;
import org.jkiss.dbeaver.model.impl.StringContentStorage;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.runtime.DBRRunnableWithProgress;
import org.jkiss.dbeaver.model.struct.DBSTypedObject;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.controls.imageview.ImageViewer;
import org.jkiss.dbeaver.ui.editors.binary.BinaryContent;
import org.jkiss.dbeaver.ui.editors.binary.HexEditControl;
import org.jkiss.dbeaver.ui.editors.content.ContentEditor;
import org.jkiss.dbeaver.ui.editors.content.parts.ContentBinaryEditorPart;
import org.jkiss.dbeaver.ui.editors.content.parts.ContentImageEditorPart;
import org.jkiss.dbeaver.ui.editors.content.parts.ContentTextEditorPart;
import org.jkiss.dbeaver.ui.editors.content.parts.ContentXMLEditorPart;
import org.jkiss.dbeaver.ui.properties.PropertySourceAbstract;
import org.jkiss.dbeaver.utils.ContentUtils;
import org.jkiss.dbeaver.utils.MimeTypes;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.List;

/**
 * JDBC Content value handler.
 * Handle LOBs, LONGs and BINARY types.
 *
 * @author Serge Rider
 */
public class JDBCContentValueHandler extends JDBCAbstractValueHandler {

    static final Log log = LogFactory.getLog(JDBCContentValueHandler.class);

    public static final JDBCContentValueHandler INSTANCE = new JDBCContentValueHandler();

    public static final String PROP_CATEGORY_CONTENT = "LOB";
    private static final int MAX_STRING_LENGTH = 0xfffff;

    @Override
    protected DBDContent fetchColumnValue(
        DBCExecutionContext context,
        JDBCResultSet resultSet,
        DBSTypedObject type,
        int index)
        throws DBCException, SQLException
    {
        Object value = resultSet.getObject(index);
        if (value == null && !resultSet.wasNull()) {
            // This may happen in some bad drivers like ODBC bridge
            switch (type.getTypeID()) {
                case java.sql.Types.CHAR:
                case java.sql.Types.VARCHAR:
                case java.sql.Types.NVARCHAR:
                case java.sql.Types.LONGVARCHAR:
                case java.sql.Types.LONGNVARCHAR:
                case java.sql.Types.CLOB:
                case java.sql.Types.NCLOB:
                    value = resultSet.getString(index);
                    break;
                case java.sql.Types.BINARY:
                case java.sql.Types.VARBINARY:
                case java.sql.Types.LONGVARBINARY:
                case java.sql.Types.BLOB:
                    value = resultSet.getBytes(index);
                    break;
                case java.sql.Types.SQLXML:
                    value = resultSet.getSQLXML(index);
                    break;
                default:
                    value = resultSet.getObject(index);
                    break;
            }
        }
        return getValueFromObject(context, type, value, false);
    }

    @Override
    protected void bindParameter(
        JDBCExecutionContext context,
        JDBCPreparedStatement statement,
        DBSTypedObject paramType,
        int paramIndex,
        Object value)
        throws DBCException, SQLException
    {
        if (value instanceof JDBCContentAbstract) {
            ((JDBCContentAbstract)value).bindParameter(context, statement, paramType, paramIndex);
        } else {
            throw new DBCException(CoreMessages.model_jdbc_unsupported_value_type_ + value);
        }
    }

    @Override
    public int getFeatures()
    {
        return FEATURE_VIEWER | FEATURE_EDITOR | FEATURE_INLINE_EDITOR | FEATURE_SHOW_ICON;
    }

    @Override
    public Class getValueObjectType()
    {
        return DBDContent.class;
    }

    @Override
    public DBDContent getValueFromObject(DBCExecutionContext context, DBSTypedObject type, Object object, boolean copy) throws DBCException
    {
        if (object == null) {
            // Create wrapper using column type
            switch (type.getTypeID()) {
                case java.sql.Types.CHAR:
                case java.sql.Types.VARCHAR:
                case java.sql.Types.NVARCHAR:
                case java.sql.Types.LONGVARCHAR:
                case java.sql.Types.LONGNVARCHAR:
                    return new JDBCContentChars(context.getDataSource(), null);
                case java.sql.Types.CLOB:
                case java.sql.Types.NCLOB:
                    return new JDBCContentCLOB(context.getDataSource(), null);
                case java.sql.Types.BINARY:
                case java.sql.Types.VARBINARY:
                case java.sql.Types.LONGVARBINARY:
                    return new JDBCContentBytes(context.getDataSource(), null);
                case java.sql.Types.BLOB:
                    return new JDBCContentBLOB(context.getDataSource(), null);
                case java.sql.Types.SQLXML:
                    return new JDBCContentXML(context.getDataSource(), null);
                default:
                    log.error(CoreMessages.model_jdbc_unsupported_column_type_ + type.getTypeName());
                    return new JDBCContentBytes(context.getDataSource(), null);
            }
        } else if (object instanceof byte[]) {
            return new JDBCContentBytes(context.getDataSource(), (byte[]) object);
        } else if (object instanceof String) {
            return new JDBCContentChars(context.getDataSource(), (String) object);
        } else if (object instanceof Blob) {
            return new JDBCContentBLOB(context.getDataSource(), (Blob) object);
        } else if (object instanceof Clob) {
            return new JDBCContentCLOB(context.getDataSource(), (Clob) object);
        } else if (object instanceof SQLXML) {
            return new JDBCContentXML(context.getDataSource(), (SQLXML) object);
        } else if (object instanceof DBDContent && object instanceof DBDValueCloneable) {
            return (DBDContent) ((DBDValueCloneable)object).cloneValue(context.getProgressMonitor());
        } else {
            throw new DBCException(CoreMessages.model_jdbc_unsupported_value_type_ + object.getClass().getName());
        }
    }

    @Override
    public String getValueDisplayString(DBSTypedObject column, Object value, DBDDisplayFormat format)
    {
        if (value instanceof DBDContent) {
            String result = value.toString();
            if (result == null) {
                return super.getValueDisplayString(column, null, format);
            } else {
                return result;
            }
        }
        return super.getValueDisplayString(column, value, format);
    }

    @Override
    public void fillContextMenu(IMenuManager menuManager, final DBDValueController controller)
        throws DBCException
    {
        if (controller.getValue() instanceof DBDContent && !((DBDContent)controller.getValue()).isNull()) {
            menuManager.add(new Action(CoreMessages.model_jdbc_save_to_file_, DBIcon.SAVE.getImageDescriptor()) {
                @Override
                public void run() {
                    saveToFile(controller);
                }
            });
        }
        menuManager.add(new Action(CoreMessages.model_jdbc_load_from_file_, DBIcon.LOAD.getImageDescriptor()) {
            @Override
            public void run() {
                loadFromFile(controller);
            }
        });
    }

    @Override
    public void fillProperties(PropertySourceAbstract propertySource, DBDValueController controller)
    {
        super.fillProperties(propertySource, controller);
        try {
            Object value = controller.getValue();
            if (value instanceof DBDContent) {
                propertySource.addProperty(
                    PROP_CATEGORY_CONTENT,
                    "content_type", //$NON-NLS-1$
                    CoreMessages.model_jdbc_content_type,
                    ((DBDContent)value).getContentType());
                final long contentLength = ((DBDContent) value).getContentLength();
                if (contentLength >= 0) {
                    propertySource.addProperty(
                        PROP_CATEGORY_CONTENT,
                        "content_length", //$NON-NLS-1$
                        CoreMessages.model_jdbc_content_length,
                        contentLength);
                }
            }
        }
        catch (Exception e) {
            log.warn("Could not extract LOB value information", e); //$NON-NLS-1$
        }
    }

    @Override
    public DBDValueEditor createEditor(final DBDValueController controller)
        throws DBException
    {
        switch (controller.getEditType()) {
            case INLINE:
            {
                // Open inline/panel editor
                if (controller.getValue() instanceof DBDContentCached) {
                    final boolean isText = ContentUtils.isTextContent(((DBDContent)controller.getValue()));
                    final String encoding = ContentUtils.getDefaultBinaryFileEncoding(controller.getDataSource());
                    // String editor
                    return new ValueEditor<StyledText>(controller) {
                        @Override
                        public void refreshValue()
                        {
                            DBDContentCached newValue = (DBDContentCached) valueController.getValue();
                            Object cachedValue = newValue.getCachedValue();
                            String stringValue;
                            if (cachedValue == null) {
                                stringValue = "";  //$NON-NLS-1$
                            } else if (cachedValue instanceof byte[]) {
                                stringValue = ContentUtils.convertToString((byte[])cachedValue, controller.getDataSource());
                            } else {
                                stringValue = cachedValue.toString();
                            }
                            control.setText(stringValue);
                            control.selectAll();
                        }
                        @Override
                        protected StyledText createControl(Composite editPlaceholder)
                        {
                            final StyledText editor = new StyledText(editPlaceholder, SWT.BORDER);
                            editor.setEditable(!valueController.isReadOnly());
                            long maxLength = valueController.getValueType().getMaxLength();
                            if (maxLength <= 0) {
                                maxLength = MAX_STRING_LENGTH;
                            } else {
                                maxLength = Math.min(maxLength, MAX_STRING_LENGTH);
                            }
                            editor.setTextLimit((int) maxLength);
                            return editor;
                        }
                        @Override
                        public Object extractValue(DBRProgressMonitor monitor)
                        {
                            String newValue = control.getText();
                            if (isText) {
                                return new JDBCContentChars(valueController.getDataSource(), newValue);
                            } else {
                                return new JDBCContentBytes(
                                    valueController.getDataSource(),
                                    ContentUtils.convertToBytes(newValue, controller.getDataSource()));
                            }
                        }
                    };
                } else {
                    return null;
                }
            }
            case EDITOR:
            {
                // Open LOB editor
                Object value = controller.getValue();
                if (value instanceof DBDContent) {
                    DBDContent content = (DBDContent)value;
                    boolean isText = ContentUtils.isTextContent(content);
                    List<IContentEditorPart> parts = new ArrayList<IContentEditorPart>();
                    if (isText) {
                        parts.add(new ContentTextEditorPart());
                        if (isXML(content)) {
                            parts.add(new ContentXMLEditorPart());
                        }
                    } else {
                        parts.add(new ContentBinaryEditorPart());
                        parts.add(new ContentTextEditorPart());
                        parts.add(new ContentImageEditorPart());
                    }
                    return ContentEditor.openEditor(
                        controller,
                        parts.toArray(new IContentEditorPart[parts.size()]) );
                } else {
                    controller.showMessage(CoreMessages.model_jdbc_unsupported_content_value_type_, true);
                    return null;
                }
            }
            case PANEL:
            {
                return new ValueEditorEx<Control>(controller) {
                    @Override
                    public void showValueEditor()
                    {
                    }

                    @Override
                    public void closeValueEditor()
                    {
                    }

                    @Override
                    public void refreshValue()
                    {
                        DBeaverUI.runInUI(valueController.getValueSite().getWorkbenchWindow(), new DBRRunnableWithProgress() {
                            @Override
                            public void run(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                            {
                                try {
                                    DBDContent content = (DBDContent) valueController.getValue();
                                    DBDContentStorage data = content.getContents(monitor);
                                    if (control instanceof StyledText) {
                                        StyledText text = (StyledText) control;
                                        StringWriter buffer = new StringWriter();
                                        if (data != null) {
                                            ContentUtils.copyStreams(data.getContentReader(), -1, buffer, monitor);
                                        }
                                        text.setText(buffer.toString());
                                    } else if (control instanceof HexEditControl) {
                                        HexEditControl hexEditControl = (HexEditControl) control;
                                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                                        if (data != null) {
                                            ContentUtils.copyStreams(data.getContentStream(), -1, buffer, monitor);
                                        }
                                        hexEditControl.setContent(buffer.toByteArray());
                                    } else if (control instanceof ImageViewer) {
                                        ImageViewer imageViewControl = (ImageViewer)control;
                                        imageViewControl.loadImage(data.getContentStream());
                                    }
                                } catch (Exception e) {
                                    log.error(e);
                                    valueController.showMessage(e.getMessage(), true);
                                }
                            }
                        });
                    }

                    @Override
                    public Object extractValue(DBRProgressMonitor monitor) throws DBException
                    {
                        DBDContent content = (DBDContent) valueController.getValue();
                        try {
                            if (control instanceof StyledText) {
                                StyledText styledText = (StyledText) control;
                                content.updateContents(
                                    monitor,
                                    new StringContentStorage(styledText.getText()));
                            } else if (control instanceof HexEditControl) {
                                HexEditControl hexEditControl = (HexEditControl)control;
                                BinaryContent binaryContent = hexEditControl.getContent();
                                ByteBuffer buffer = ByteBuffer.allocate((int) binaryContent.length());
                                try {
                                    binaryContent.get(buffer, 0);
                                } catch (IOException e) {
                                    log.error(e);
                                }
                                content.updateContents(
                                    monitor,
                                    new BytesContentStorage(buffer.array(), ContentUtils.getDefaultFileEncoding()));
                            }
                        } catch (Exception e) {
                            log.error(e);
                        }
                        return content;
                    }

                    @Override
                    protected Control createControl(Composite editPlaceholder)
                    {
                        DBDContent content = (DBDContent) valueController.getValue();
                        if (ContentUtils.isTextContent(content)) {
                            StyledText text = new StyledText(editPlaceholder, SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
                            text.setEditable(!valueController.isReadOnly());
                            return text;
                        } else {
                            ImageDetector imageDetector = new ImageDetector(content);
                            if (!content.isNull()) {
                                DBeaverUI.runInUI(valueController.getValueSite().getWorkbenchWindow(), imageDetector);
                            }

                            if (imageDetector.isImage()) {
                                return new ImageViewer(editPlaceholder, SWT.BORDER);
                            } else {
                                return new HexEditControl(editPlaceholder, SWT.BORDER);
                            }
                        }
                    }
                };
            }
            default:
                return null;
        }
    }

    private static boolean isXML(DBDContent content)
    {
        return MimeTypes.TEXT_XML.equalsIgnoreCase(content.getContentType());
    }

    private void loadFromFile(final DBDValueController controller)
    {
        if (!(controller.getValue() instanceof DBDContent)) {
            log.error(CoreMessages.model_jdbc_bad_content_value_ + controller.getValue());
            return;
        }

        Shell shell = UIUtils.getShell(controller.getValueSite());
        final File openFile = ContentUtils.openFile(shell);
        if (openFile == null) {
            return;
        }
        final DBDContent value = (DBDContent)controller.getValue();
        try {
            DBeaverUI.runInProgressService(new DBRRunnableWithProgress() {
                @Override
                public void run(DBRProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException
                {
                    try {
                        DBDContentStorage storage;
                        if (ContentUtils.isTextContent(value)) {
                            storage = new ExternalContentStorage(openFile, ContentUtils.DEFAULT_FILE_CHARSET_NAME);
                        } else {
                            storage = new ExternalContentStorage(openFile);
                        }
                        value.updateContents(monitor, storage);
                        controller.updateValue(value);
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        }
        catch (InvocationTargetException e) {
            UIUtils.showErrorDialog(
                shell,
                CoreMessages.model_jdbc_could_not_load_content,
                CoreMessages.model_jdbc_could_not_load_content_from_file + openFile.getAbsolutePath() + "'", //$NON-NLS-2$
                e.getTargetException());
        }
        catch (InterruptedException e) {
            // do nothing
        }
    }

    private void saveToFile(DBDValueController controller)
    {
        if (!(controller.getValue() instanceof DBDContent)) {
            log.error(CoreMessages.model_jdbc_bad_content_value_ + controller.getValue());
            return;
        }

        Shell shell = UIUtils.getShell(controller.getValueSite());
        final File saveFile = ContentUtils.selectFileForSave(shell);
        if (saveFile == null) {
            return;
        }
        final DBDContent value = (DBDContent)controller.getValue();
        try {
            DBeaverUI.runInProgressService(new DBRRunnableWithProgress() {
                @Override
                public void run(DBRProgressMonitor monitor)
                    throws InvocationTargetException, InterruptedException
                {
                    try {
                        DBDContentStorage storage = value.getContents(monitor);
                        if (ContentUtils.isTextContent(value)) {
                            ContentUtils.saveContentToFile(
                                storage.getContentReader(),
                                saveFile,
                                ContentUtils.DEFAULT_FILE_CHARSET_NAME,
                                monitor
                            );
                        } else {
                            ContentUtils.saveContentToFile(
                                storage.getContentStream(),
                                saveFile,
                                monitor
                            );
                        }
                    } catch (Exception e) {
                        throw new InvocationTargetException(e);
                    }
                }
            });
        }
        catch (InvocationTargetException e) {
            UIUtils.showErrorDialog(
                shell,
                CoreMessages.model_jdbc_could_not_save_content,
                CoreMessages.model_jdbc_could_not_save_content_to_file_ + saveFile.getAbsolutePath() + "'", //$NON-NLS-2$
                e.getTargetException());
        }
        catch (InterruptedException e) {
            // do nothing
        }
    }

    private static class ImageDetector implements DBRRunnableWithProgress {
        private final DBDContent content;
        private boolean isImage;

        private ImageDetector(DBDContent content)
        {
            this.content = content;
        }

        public boolean isImage()
        {
            return isImage;
        }

        @Override
        public void run(DBRProgressMonitor monitor) throws InvocationTargetException, InterruptedException
        {
            if (!content.isNull()) {
                try {
                    new ImageData(
                        content.getContents(monitor).getContentStream());
                    isImage = true;
                }
                catch (Exception e) {
                    // this is not an image
                }
            }
        }
    }

}
