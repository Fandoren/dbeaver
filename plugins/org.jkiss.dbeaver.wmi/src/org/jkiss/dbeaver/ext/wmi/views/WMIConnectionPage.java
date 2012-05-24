/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.ext.wmi.views;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.jkiss.dbeaver.ext.wmi.Activator;
import org.jkiss.dbeaver.model.DBPConnectionInfo;
import org.jkiss.dbeaver.ui.UIUtils;
import org.jkiss.dbeaver.ui.dialogs.connection.ConnectionPageAdvanced;
import org.jkiss.utils.CommonUtils;

/**
 * WMIConnectionPage
 */
public class WMIConnectionPage extends ConnectionPageAdvanced
{
    private Text domainText;
    private Text hostText;
    private Combo namespaceCombo;
    private Combo localeCombo;
    private Text usernameText;
    private Text passwordText;
    private Button testButton;

    private static ImageDescriptor logoImage = Activator.getImageDescriptor("icons/wmi_logo.png");

    public WMIConnectionPage()
    {
    }

    @Override
    public void dispose()
    {
        super.dispose();
    }

    @Override
    public void createControl(Composite composite)
    {
        //Composite group = new Composite(composite, SWT.NONE);
        //group.setLayout(new GridLayout(1, true));
        setImageDescriptor(logoImage);

        TabFolder optionsFolder = new TabFolder(composite, SWT.NONE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        optionsFolder.setLayoutData(gd);

        TabItem addrTab = new TabItem(optionsFolder, SWT.NONE);
        addrTab.setText("Settings");
        addrTab.setToolTipText("Connection settings");
        addrTab.setControl(createGeneralTab(optionsFolder));

        final TabItem propsTab = new TabItem(optionsFolder, SWT.NONE);
        propsTab.setText("Advanced");
        propsTab.setToolTipText("WMI context attributes");
        propsTab.setControl(createPropertiesTab(optionsFolder));

        optionsFolder.addSelectionListener(
            new SelectionListener()
            {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    if (e.item == propsTab) {
                        //refreshDriverProperties();
                    }
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e)
                {
                }
            }
        );
        setControl(optionsFolder);
    }

    private Composite createGeneralTab(Composite parent)
    {
        ModifyListener textListener = new ModifyListener()
        {
            @Override
            public void modifyText(ModifyEvent e)
            {
                evaluateURL();
            }
        };

        Composite addrGroup = new Composite(parent, SWT.NONE);
        GridLayout gl = new GridLayout(4, false);
        gl.marginHeight = 10;
        gl.marginWidth = 10;
        addrGroup.setLayout(gl);
        GridData gd = new GridData(GridData.FILL_BOTH);
        addrGroup.setLayoutData(gd);

        Label hostLabel = UIUtils.createControlLabel(addrGroup, "Host");
        hostLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        hostText = new Text(addrGroup, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        hostText.setLayoutData(gd);
        hostText.addModifyListener(textListener);

        Label domainLabel = UIUtils.createControlLabel(addrGroup, "Domain");
        domainLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        domainText = new Text(addrGroup, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        domainText.setLayoutData(gd);
        domainText.addModifyListener(textListener);

        Label namespaceLabel = UIUtils.createControlLabel(addrGroup, "Namespace");
        namespaceLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

        namespaceCombo = new Combo(addrGroup, SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = 3;
        namespaceCombo.setLayoutData(gd);
        namespaceCombo.addModifyListener(textListener);

        Label divLabel = new Label(addrGroup, SWT.SEPARATOR | SWT.SHADOW_OUT | SWT.HORIZONTAL);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = 4;
        divLabel.setLayoutData(gd);

        {
            Label usernameLabel = UIUtils.createControlLabel(addrGroup, "User");
            usernameLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

            usernameText = new Text(addrGroup, SWT.BORDER);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.grabExcessHorizontalSpace = true;
            usernameText.setLayoutData(gd);
            usernameText.addModifyListener(textListener);


            testButton = new Button(addrGroup, SWT.PUSH);
            testButton.setText("Test Connection");
            gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
            gd.horizontalSpan = 2;
            testButton.setLayoutData(gd);
            testButton.addSelectionListener(new SelectionListener()
            {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    site.testConnection();
                }

                @Override
                public void widgetDefaultSelected(SelectionEvent e)
                {
                }
            });
            testButton.setEnabled(false);

            Label passwordLabel = UIUtils.createControlLabel(addrGroup, "Password");
            passwordLabel.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));

            passwordText = new Text(addrGroup, SWT.BORDER | SWT.PASSWORD);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.grabExcessHorizontalSpace = true;
            passwordText.setLayoutData(gd);
            passwordText.addModifyListener(textListener);
        }
        return addrGroup;
    }

    @Override
    public boolean isComplete()
    {
        return hostText != null && namespaceCombo != null &&
            !CommonUtils.isEmpty(hostText.getText()) &&
            !CommonUtils.isEmpty(namespaceCombo.getText());
    }

    @Override
    public void loadSettings()
    {
        // Load values from new connection info
        DBPConnectionInfo connectionInfo = site.getConnectionInfo();
        if (connectionInfo != null) {
            if (connectionInfo.getHostName() == null) {
                connectionInfo.setHostName("localhost");
            }
            if (connectionInfo.getDatabaseName() == null) {
                connectionInfo.setDatabaseName("root");
            }
            if (hostText != null) {
                hostText.setText(CommonUtils.getString(connectionInfo.getHostName()));
            }
            if (domainText != null) {
                domainText.setText(CommonUtils.getString(connectionInfo.getServerName()));
            }
            if (usernameText != null) {
                usernameText.setText(CommonUtils.getString(connectionInfo.getUserName()));
            }
            if (passwordText != null) {
                passwordText.setText(CommonUtils.getString(connectionInfo.getUserPassword()));
            }
            if (namespaceCombo != null) {
                namespaceCombo.setText(CommonUtils.getString(connectionInfo.getDatabaseName()));
            }
        }

        super.loadSettings();
    }

    @Override
    protected void saveSettings(DBPConnectionInfo connectionInfo)
    {
        if (connectionInfo != null) {
            if (hostText != null) {
                connectionInfo.setHostName(hostText.getText());
            }
            if (domainText != null) {
                connectionInfo.setServerName(domainText.getText());
            }
            if (namespaceCombo != null) {
                connectionInfo.setDatabaseName(namespaceCombo.getText());
            }
            if (usernameText != null) {
                connectionInfo.setUserName(usernameText.getText());
            }
            if (passwordText != null) {
                connectionInfo.setUserPassword(passwordText.getText());
            }
            super.saveSettings(connectionInfo);
        }
    }

    private void evaluateURL()
    {
        site.updateButtons();
        if (testButton != null) {
            testButton.setEnabled(this.isComplete());
        }
    }


}
