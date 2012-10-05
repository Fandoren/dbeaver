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
package org.jkiss.dbeaver.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * PrefPageEmpty
 */
public class PrefPageEmpty extends PreferencePage implements IWorkbenchPreferencePage
{
    public static final String PAGE_ID = "org.jkiss.dbeaver.ui.preferences.PrefPageEmpty";

    public PrefPageEmpty()
    {
        super();
    }

    @Override
    protected Control createContents(Composite parent)
    {
        super.noDefaultAndApplyButton();
        return parent;
    }

    @Override
    public void init(IWorkbench workbench)
    {
    }
}
