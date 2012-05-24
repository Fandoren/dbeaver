/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.runtime.sql;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbenchPartSite;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.DBeaverConstants;
import org.jkiss.dbeaver.model.DBConstants;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPDataTypeProvider;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.data.DBDValueController;
import org.jkiss.dbeaver.model.data.DBDValueEditor;
import org.jkiss.dbeaver.model.data.DBDValueHandler;
import org.jkiss.dbeaver.model.struct.DBSColumnBase;
import org.jkiss.dbeaver.model.struct.DBSDataKind;
import org.jkiss.dbeaver.model.struct.DBSDataType;
import org.jkiss.dbeaver.registry.DataSourceProviderRegistry;
import org.jkiss.dbeaver.registry.DataTypeProviderDescriptor;
import org.jkiss.dbeaver.ui.DBIcon;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.utils.CommonUtils;

import java.util.*;
import java.util.List;

/**
 * Parameter binding
 */
public class SQLQueryParameterBindDialog extends StatusDialog {

    private IWorkbenchPartSite ownerSite;
    private DBPDataSource dataSource;
    private List<SQLStatementParameter> parameters;
    private List<DBSDataType> validDataTypes = new ArrayList<DBSDataType>();
    private TableEditor tableEditor;
    private Table paramTable;

    private static Map<String, Object> savedParamValues = new HashMap<String, Object>();

    protected SQLQueryParameterBindDialog(IWorkbenchPartSite ownerSite, DBPDataSource dataSource, List<SQLStatementParameter> parameters)
    {
        super(ownerSite.getShell());
        this.ownerSite = ownerSite;
        this.dataSource = dataSource;
        this.parameters = parameters;

        if (dataSource instanceof DBPDataTypeProvider) {
            for (DBSDataType dataType : ((DBPDataTypeProvider)dataSource).getDataTypes()) {
                if (dataType.getDataKind() == DBSDataKind.UNKNOWN) {
                    continue;
                }
                final DataTypeProviderDescriptor dataTypeProvider = DataSourceProviderRegistry.getDefault().getDataTypeProvider(dataSource, dataType.getName(), dataType.getValueType());
                if (dataTypeProvider != null) {
                    final DBDValueHandler handler = dataTypeProvider.getInstance().getHandler(dataSource.getContainer(), dataType.getName(), dataType.getValueType());
                    if (handler != null && (handler.getFeatures() & DBDValueHandler.FEATURE_INLINE_EDITOR) != 0) {
                        validDataTypes.add(dataType);
                    }
                }
            }
        }
        Collections.sort(validDataTypes, new Comparator<DBSDataType>() {
            @Override
            public int compare(DBSDataType o1, DBSDataType o2)
            {
                return o1.getName().compareTo(o2.getName());
            }
        });

        for (SQLStatementParameter param : this.parameters) {
            final DBSDataType dataType = DBUtils.findBestDataType(validDataTypes, DBConstants.DEFAULT_DATATYPE_NAMES);
            if (dataType != null) {
                param.setParamType(dataType);
                param.resolve();
            }
            Object value = savedParamValues.get(param.getName());
            if (value != null) {
                param.setValue(value);
            }
        }
    }

    @Override
    protected boolean isResizable()
    {
        return true;
    }


    @Override
    protected Control createDialogArea(Composite parent)
    {
        getShell().setText("Bind parameter(s)");
        final Composite composite = (Composite)super.createDialogArea(parent);

        paramTable = new Table(composite, SWT.SINGLE | SWT.FULL_SELECTION | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        final GridData gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = 400;
        gd.heightHint = 200;
        paramTable.setLayoutData(gd);
        paramTable.setHeaderVisible(true);
        paramTable.setLinesVisible(true);

        tableEditor = new TableEditor(paramTable);
        tableEditor.verticalAlignment = SWT.TOP;
        tableEditor.horizontalAlignment = SWT.RIGHT;
        tableEditor.grabHorizontal = true;
        tableEditor.grabVertical = true;

        final TableColumn indexColumn = UIUtils.createTableColumn(paramTable, SWT.LEFT, "#");
        indexColumn.setWidth(30);
        final TableColumn nameColumn = UIUtils.createTableColumn(paramTable, SWT.LEFT, "Name");
        nameColumn.setWidth(100);
        final TableColumn typeColumn = UIUtils.createTableColumn(paramTable, SWT.LEFT, "Type");
        typeColumn.setWidth(70);
        final TableColumn valueColumn = UIUtils.createTableColumn(paramTable, SWT.LEFT, "Value");
        valueColumn.setWidth(200);

        for (SQLStatementParameter param : parameters) {
            TableItem item = new TableItem(paramTable, SWT.NONE);
            item.setData(param);
            item.setImage(DBIcon.TREE_ATTRIBUTE.getImage());
            item.setText(0, String.valueOf(param.getIndex() + 1));
            item.setText(1, param.getTitle());
            item.setText(2, CommonUtils.toString(param.getTypeName()));
            item.setText(3, param.getValueHandler() == null ? "" : param.getValueHandler().getValueDisplayString(param, param.getValue()));
        }

        paramTable.addMouseListener(new ParametersMouseListener());

        return composite;
    }

    private void disposeOldEditor()
    {
        Control oldEditor = tableEditor.getEditor();
        if (oldEditor != null) oldEditor.dispose();
    }

    private class ParametersMouseListener implements MouseListener {

        @Override
        public void mouseDoubleClick(MouseEvent e)
        {
            //handleColumnClick(e, true);
        }

        @Override
        public void mouseDown(MouseEvent e)
        {
        }

        @Override
        public void mouseUp(MouseEvent e)
        {
            handleColumnClick(e);
        }

        private void handleColumnClick(MouseEvent e) {
            // Clean up any previous editor control
            disposeOldEditor();

            TableItem item = paramTable.getItem(new Point(e.x, e.y));
            if (item == null) {
                return;
            }
            int columnIndex = UIUtils.getColumnAtPos(item, e.x, e.y);
            if (columnIndex <= 1) {
                return;
            }
            if (columnIndex == 2) {
                showTypeSelector(item);
            } else if (columnIndex == 3) {
                showEditor(item);
            }
        }

        private void showTypeSelector(final TableItem item)
        {
            final SQLStatementParameter param = (SQLStatementParameter)item.getData();
            final CCombo typeSelector = new CCombo(paramTable, SWT.BORDER | SWT.DROP_DOWN | SWT.READ_ONLY);
            typeSelector.setListVisible(true);
            typeSelector.setVisibleItemCount(15);
            int selectionIndex = 0;
            for (DBSDataType dataType : validDataTypes) {
                typeSelector.add(dataType.getName());
                if (param.getParamType() == dataType) {
                    selectionIndex = typeSelector.getItemCount() - 1;
                }
            }
            typeSelector.select(selectionIndex);
            typeSelector.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    final DBSDataType paramType = validDataTypes.get(typeSelector.getSelectionIndex());
                    if (param.getParamType() == null || param.getParamType().getDataKind() != paramType.getDataKind()) {
                        param.setValue(null);
                    }
                    param.setParamType(paramType);
                    item.setText(2, paramType.getName());
                    item.setText(3, param.getValueHandler() == null ? "" : param.getValueHandler().getValueDisplayString(param, param.getValue()));
                    param.resolve();
                }
            });

            tableEditor.minimumHeight = typeSelector.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
            tableEditor.setEditor(typeSelector, item, 2);
        }

        private void showEditor(final TableItem item) {
            SQLStatementParameter param = (SQLStatementParameter)item.getData();
            if (!param.isResolved()) {
                return;
            }
            final DBDValueHandler valueHandler = param.getValueHandler();
            Composite placeholder = new Composite(paramTable, SWT.NONE);
            placeholder.setLayout(new FillLayout());
            //placeholder.setLayout(new FillLayout(SWT.HORIZONTAL));
            ParameterValueController valueController = new ParameterValueController(param, placeholder, item);
            try {
                if (valueHandler.editValue(valueController)) {
                    tableEditor.minimumHeight = placeholder.computeSize(SWT.DEFAULT, SWT.DEFAULT).y;
                    tableEditor.setEditor(placeholder, item, 3);
                } else {
                    // No editor was created so just drop placeholder
                    placeholder.dispose();
                }
            } catch (DBException e) {
                UIUtils.showErrorDialog(getShell(), "Can't open editor", null, e);
                placeholder.dispose();
            }
        }
    }

    private class ParameterValueController implements DBDValueController {

        private final SQLStatementParameter parameter;
        private final Composite placeholder;
        private final TableItem item;

        private ParameterValueController(SQLStatementParameter parameter, Composite placeholder, TableItem item)
        {
            this.parameter = parameter;
            this.placeholder = placeholder;
            this.item = item;
        }

        @Override
        public DBPDataSource getDataSource()
        {
            return dataSource;
        }

        @Override
        public DBSColumnBase getColumnMetaData()
        {
            return parameter;
        }

        @Override
        public Object getValue()
        {
            return parameter.getValue();
        }

        @Override
        public void updateValue(Object value)
        {
            parameter.setValue(value);
            item.setText(3, getValueHandler().getValueDisplayString(parameter, value));
            String paramName = parameter.getName().trim();
            boolean isNumber = true;
            try {
                Integer.parseInt(paramName);
            } catch (NumberFormatException e) {
                isNumber = false;
            }
            if (!isNumber && !paramName.equals("?")) {
                savedParamValues.put(paramName, value);
            }
            //parameter.getIndex()

            updateStatus(Status.OK_STATUS);
        }

        @Override
        public DBDValueHandler getValueHandler()
        {
            return parameter.getValueHandler();
        }

        @Override
        public boolean isInlineEdit()
        {
            return true;
        }

        @Override
        public boolean isReadOnly()
        {
            return false;
        }

        @Override
        public IWorkbenchPartSite getValueSite()
        {
            return ownerSite;
        }

        @Override
        public Composite getInlinePlaceholder()
        {
            return placeholder;
        }

        @Override
        public void closeInlineEditor()
        {
            disposeOldEditor();
        }

        @Override
        public void nextInlineEditor(boolean next)
        {
        }

        @Override
        public void registerEditor(DBDValueEditor editor)
        {
        }

        @Override
        public void unregisterEditor(DBDValueEditor editor)
        {
        }

        @Override
        public void showMessage(String message, boolean error)
        {
            updateStatus(new Status(error ? Status.ERROR : Status.INFO, DBeaverConstants.PLUGIN_ID, message));
        }
    }

}
