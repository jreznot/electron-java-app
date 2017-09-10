package org.strangeway.electronvaadin.server;

import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.*;
import org.jsoup.nodes.Element;
import org.strangeway.electronvaadin.app.MainUI;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;

/**
 * @author Yuriy Artamonov
 */
@WebServlet(value = "/*", asyncSupported = true)
@VaadinServletConfiguration(productionMode = false, ui = MainUI.class)
public class AppServlet extends VaadinServlet implements BootstrapListener {
    @Override
    protected void servletInitialized() throws ServletException {
        super.servletInitialized();
        getService().addSessionInitListener(event ->
                event.getSession().addBootstrapListener(this)
        );
    }

    @Override
    public void modifyBootstrapFragment(BootstrapFragmentResponse response) {
    }

    @Override
    public void modifyBootstrapPage(BootstrapPageResponse response) {
        // Obtain electron UI API end point
        Element head = response.getDocument().getElementsByTag("head").get(0);
        Element script = response.getDocument().createElement("script");
        script.attr("type", "text/javascript");
        script.text("let {remote} = require('electron');\n" +
                    "window.callElectronUiApi = remote.getGlobal(\"callElectronUiApi\");");
        head.appendChild(script);
    }
}