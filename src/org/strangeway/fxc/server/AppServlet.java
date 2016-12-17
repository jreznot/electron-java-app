package org.strangeway.fxc.server;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;
import org.strangeway.fxc.app.MainUI;

import javax.servlet.annotation.WebServlet;

/**
 * @author Yuriy Artamonov
 */
@WebServlet(value = "/*")
@VaadinServletConfiguration(productionMode = false, ui = MainUI.class)
public final class AppServlet extends VaadinServlet {
}