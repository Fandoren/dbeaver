/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.actions.navigator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPObject;
import org.jkiss.dbeaver.model.DBPTool;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.struct.DBSWrapper;
import org.jkiss.dbeaver.registry.DataSourceToolDescriptor;
import org.jkiss.dbeaver.ui.UIUtils;

public class NavigatorActionExecuteTool implements IActionDelegate
{
    private IWorkbenchWindow window;
    private DataSourceToolDescriptor tool;
    private ISelection selection;

    public NavigatorActionExecuteTool(IWorkbenchWindow window, DataSourceToolDescriptor tool)
    {
        this.window = window;
        this.tool = tool;
    }

    @Override
    public void run(IAction action)
    {
        if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
            Object element = ((IStructuredSelection) selection).getFirstElement();
            if (element instanceof DBSWrapper) {
                executeTool(((DBSWrapper) element).getObject());
            } else if (element instanceof DBPObject) {
                executeTool((DBPObject) element);
            }
        }
    }

    private void executeTool(DBPObject object)
    {
        try {
            DBPTool toolInstance = tool.createTool();
            toolInstance.execute(window, DBUtils.getPublicObject(object));
        } catch (DBException e) {
            UIUtils.showErrorDialog(window.getShell(), "Tool error", "Error executing tool '" + tool.getLabel() + "'", e);
        }
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection)
    {
        this.selection = selection;
    }

}