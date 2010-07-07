/*
 * Copyright (c) 2010, Serge Rieder and others. All Rights Reserved.
 */

package org.jkiss.dbeaver.core;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Region;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;

/**
 * This class controls all aspects of the application's execution
 */
public class DBeaverApplication implements IApplication
{

    /* (non-Javadoc)
      * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.IApplicationContext)
      */
    public Object start(IApplicationContext context)
    {
        Display display = PlatformUI.createDisplay();
        try {
            int returnCode = PlatformUI.createAndRunWorkbench(display, new ApplicationWorkbenchAdvisor());
            if (returnCode == PlatformUI.RETURN_RESTART) {
                return IApplication.EXIT_RESTART;
            }
            return IApplication.EXIT_OK;
        } finally {
/*
            try {
                Job.getJobManager().join(null, new NullProgressMonitor());
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
*/
            display.dispose();
        }
    }

    /* (non-Javadoc)
      * @see org.eclipse.equinox.app.IApplication#stop()
      */
    public void stop()
    {
        final IWorkbench workbench = PlatformUI.getWorkbench();
        if (workbench == null)
            return;
        final Display display = workbench.getDisplay();
        display.syncExec(new Runnable()
        {
            public void run()
            {
                if (!display.isDisposed())
                    workbench.close();
            }
        });
    }


    private static void test() {
        Display display = new Display();
        //final Image image2 = new Image(display, "D:\\Devel\\My\\dbeaver\\docs\\branding\\splashscreen-circle.png");
        final Image image = display.getSystemImage(SWT.ICON_WARNING);
        //Shell must be created with style SWT.NO_TRIM
        final Shell shell = new Shell(display, SWT.NO_TRIM | SWT.ON_TOP);
        shell.setBackground(display.getSystemColor(SWT.COLOR_RED));
        //define a region
        Region region = new Region();
        Rectangle pixel = new Rectangle(0, 0, 1, 1);
        for (int y = 0; y < 200; y+=2) {
                for (int x = 0; x < 200; x+=2) {
                    pixel.x = x;
                    pixel.y = y;
                    region.add(pixel);
                }
            }
        //define the shape of the shell using setRegion
        shell.setRegion(region);
        Rectangle size = region.getBounds();
        shell.setSize(size.width, size.height);
        shell.addPaintListener(new PaintListener() {
            public void paintControl(PaintEvent e) {
                Rectangle bounds = image.getBounds();
                Point size = shell.getSize();
                e.gc.drawImage(image, 0, 0, bounds.width, bounds.height, 10, 10, size.x-20, size.y-20);
            }
        });
        shell.addListener(SWT.KeyDown, new Listener() {
            public void handleEvent(Event e)  {
                if (e.character == SWT.ESC) {
                    shell.dispose();
                }
            }
        });
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch())
                display.sleep();
        }
        region.dispose();
        display.dispose();

        System.exit(0);
    }

}
