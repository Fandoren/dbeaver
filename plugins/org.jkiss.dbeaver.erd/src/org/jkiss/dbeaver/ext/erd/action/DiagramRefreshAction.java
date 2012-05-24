/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

/*
 * Created on Jul 23, 2004
 */
package org.jkiss.dbeaver.ext.erd.action;

import org.eclipse.jface.action.Action;
import org.jkiss.dbeaver.ext.erd.editor.ERDEditorPart;
import org.jkiss.dbeaver.ui.DBIcon;

/**
 * Action to toggle the layout between manual and automatic
 *
 * @author Serge Rieder
 */
public class DiagramRefreshAction extends Action
{
	private ERDEditorPart editor;

	public DiagramRefreshAction(ERDEditorPart editor)
	{
		super("Refresh Diagram", DBIcon.REFRESH.getImageDescriptor());
		this.editor = editor;
	}

	@Override
    public void run()
	{
        //editor.get
        editor.refreshDiagram();
	}

}