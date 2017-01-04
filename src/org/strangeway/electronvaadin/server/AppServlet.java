package org.strangeway.electronvaadin.server;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinServlet;
import org.strangeway.electronvaadin.app.MainUI;

import javax.servlet.annotation.WebServlet;

/**
 * @author Yuriy Artamonov
 */
@WebServlet(value = "/*")
@VaadinServletConfiguration(productionMode = false, ui = MainUI.class)
public class AppServlet extends VaadinServlet {
}