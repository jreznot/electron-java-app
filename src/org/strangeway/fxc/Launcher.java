package org.strangeway.fxc;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.strangeway.fxc.server.AppServlet;

/**
 * @author Yuriy Artamonov
 */
public final class Launcher {

    public static void main(String[] args) {
        System.out.println("Server starting...");

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
            System.out.print("Server error:\n" + e.getMessage());
            e.printStackTrace(System.out);
        }

        System.out.println("Server stopped");
    }
}