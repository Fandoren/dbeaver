/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ui.dialogs;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.jkiss.dbeaver.ui.UIUtils;

/**
 * Dialog with enabled help
 */
public abstract class HelpEnabledDialog extends TrayDialog {

    protected final String helpContextID;

    protected HelpEnabledDialog(Shell shell, String helpContextID)
    {
        super(shell);
        this.helpContextID = helpContextID;
    }

    @Override
    protected Control createContents(Composite parent)
    {
        final Control contents = super.createContents(parent);
        UIUtils.setHelp(contents, helpContextID);
        return contents;
    }

    @Override
    protected boolean isResizable() {
    	return true;
    }

//    protected Button createHelpButton(Composite parent)
//    {
//        final Button button = createButton(parent, IDialogConstants.HELP_ID, IDialogConstants.HELP_LABEL, false);
//        button.setImage(
//            PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_LCL_LINKTO_HELP)
//        );
//        return button;
//    }

}
