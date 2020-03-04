package org.strangeway.electronvaadin;

import java.net.MalformedURLException;
import java.net.URL;

import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.startup.ServletContextListeners;
import org.eclipse.jetty.annotations.AnnotationConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.Configuration;
import org.eclipse.jetty.webapp.JettyWebXmlConfiguration;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Yuriy Artamonov
 * @author Erik Lumme
 */
public class Launcher {

    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) throws Exception {
        log.info("Server starting...");

        Server server = new Server(8080);

        final WebAppContext context = new WebAppContext();
        context.setBaseResource(findWebRoot());
        context.addServlet(VaadinServlet.class, "/*");
        context.setContextPath("/");
        context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                ".*vaadin/.*\\.jar|.*/classes/.*");
        context.setConfigurationDiscovered(true);
        context.getServletContext().setExtendedListenerTypes(true);
        context.addEventListener(new ServletContextListeners());
        server.setHandler(context);

        SessionHandler sessions = new SessionHandler();
        context.setSessionHandler(sessions);

        final Configuration.ClassList classList = Configuration.ClassList.setServerDefault(server);
        classList.addBefore(JettyWebXmlConfiguration.class.getName(), AnnotationConfiguration.class.getName());
        WebSocketServerContainerInitializer.initialize(context); // fixes IllegalStateException: Unable to configure jsr356 at that stage. ServerContainer is null

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            log.error("Server error:\n", e);
        }

        log.info("Server stopped");
    }

    private static Resource findWebRoot() throws MalformedURLException {
        // don't look up directory as a resource, it's unreliable: https://github.com/eclipse/jetty.project/issues/4173#issuecomment-539769734
        // instead we'll look up the /webapp/ROOT and retrieve the parent folder from that.
        final URL f = Launcher.class.getResource("/webapp/ROOT");
        if (f == null) {
            throw new IllegalStateException("Invalid state: the resource /webapp/ROOT doesn't exist, has webapp been packaged in as a resource?");
        }
        final String url = f.toString();
        if (!url.endsWith("/ROOT")) {
            throw new RuntimeException("Parameter url: invalid value " + url + ": doesn't end with /ROOT");
        }
        System.err.println("/webapp/ROOT is " + f);

        // Resolve file to directory
        URL webRoot = new URL(url.substring(0, url.length() - 5));
        System.err.println("WebRoot is " + webRoot);
        return Resource.newResource(webRoot);
    }
}