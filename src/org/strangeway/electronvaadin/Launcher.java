package org.strangeway.electronvaadin;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.strangeway.electronvaadin.server.AppServlet;

/**
 * @author Yuriy Artamonov
 */
public class Launcher {

    private static final Logger log = LoggerFactory.getLogger(Launcher.class);

    public static void main(String[] args) {
        log.info("Server starting...");

        Server embeddedServer = new Server(8080);

        ServletContextHandler contextHandler = new ServletContextHandler(null, "/", true, false);
        embeddedServer.setHandler(contextHandler);

        SessionHandler sessions = new SessionHandler();
        contextHandler.setSessionHandler(sessions);

        ServletHolder servletHolder = new ServletHolder(AppServlet.class);
        contextHandler.addServlet(servletHolder, "/*");

        try {
            embeddedServer.start();
            embeddedServer.join();
        } catch (Exception e) {
            log.error("Server error:\n", e);
        }

        log.info("Server stopped");
    }
}