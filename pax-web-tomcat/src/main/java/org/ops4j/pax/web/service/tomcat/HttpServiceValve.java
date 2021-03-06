/*
 * HttpServiceValve.java
 *
 * created at 31.05.2016 by utzig <j.utzig@seeburger.de>
 *
 * Copyright (c) SEEBURGER AG, Germany. All Rights Reserved.
 */
package org.ops4j.pax.web.service.tomcat;


import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.Container;
import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.core.ContainerBase;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.valves.ValveBase;
import org.ops4j.pax.web.service.spi.ServerControllerFactory;
import org.ops4j.pax.web.service.tomcat.internal.TomcatServerControllerFactory;
import org.ops4j.pax.web.service.tomcat.internal.TomcatServerFactory;
import org.ops4j.pax.web.service.tomcat.internal.TomcatServerStateFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;


public class HttpServiceValve extends ValveBase implements Valve
{

    private boolean initialized;
    private ContainerBase host;
    private BundleContext context;


    @Override
    public void setContainer(Container container)
    {
        super.setContainer(container);
        initialize();
    }


    @Override
    public void invoke(Request request, Response response) throws IOException, ServletException
    {
        if (!initialized)
            initialize();
        if (getNext() != null)
            getNext().invoke(request, response);

    }


    private void initialize()
    {
        context = FrameworkUtil.getBundle(getClass()).getBundleContext();
        final Container localContext = getContainer();
        if (localContext != null)
        {
            if (localContext instanceof StandardContext)
            {
                localContext.addPropertyChangeListener(new PropertyChangeListener()
                {

                    @Override
                    public void propertyChange(PropertyChangeEvent evt)
                    {
                        if ("parent".equals(evt.getPropertyName()))
                        {
                            host = (ContainerBase)evt.getNewValue();
                            ServerControllerFactory factory = TomcatServerControllerFactory.newInstance(TomcatServerStateFactory.newInstance(new TomcatServerFactory(host)));
                            context.registerService(ServerControllerFactory.class, factory, null);
                            localContext.removePropertyChangeListener(this);
                        }
                    }
                });
            }
        }

    }

}
