/*
 * Copyright (c) 2012, Serge Rieder and others. All Rights Reserved.
 */

/*
 * Created on Jul 13, 2004
 */
package org.jkiss.dbeaver.ext.erd.model;

import org.eclipse.swt.graphics.Image;
import org.jkiss.dbeaver.ext.ui.IObjectImageProvider;
import org.jkiss.dbeaver.model.struct.DBSEntityAttribute;
import org.jkiss.dbeaver.ui.DBIcon;

/**
 * Column entry in model Table
 * @author Serge Rieder
 */
public class ERDEntityAttribute extends ERDObject<DBSEntityAttribute>
{
    private boolean inPrimaryKey;
    private boolean inForeignKey;

    public ERDEntityAttribute(DBSEntityAttribute attribute, boolean inPrimaryKey) {
        super(attribute);
        this.inPrimaryKey = inPrimaryKey;
    }

	public String getLabelText()
	{
		return object.getName();
	}

    public Image getLabelImage()
    {
        if (object instanceof IObjectImageProvider) {
            return ((IObjectImageProvider)object).getObjectImage();
        } else {
            return DBIcon.TYPE_UNKNOWN.getImage();
        }
    }

    public boolean isInPrimaryKey() {
        return inPrimaryKey;
    }

    public boolean isInForeignKey() {
        return inForeignKey;
    }

    public void setInForeignKey(boolean inForeignKey) {
        this.inForeignKey = inForeignKey;
    }

    @Override
    public String getName()
    {
        return getObject().getName();
    }
}