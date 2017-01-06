package org.strangeway.electronvaadin.app;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.themes.ValoTheme;
import elemental.json.JsonString;

/**
 * @author Yuriy Artamonov
 */
@PreserveOnRefresh
@Theme(ValoTheme.THEME_NAME)
public class MainUI extends UI {
    @Override
    protected void init(VaadinRequest request) {
        initLayout();

        initElectronEndpoint();
    }

    protected void initElectronEndpoint() {
        getPage().getJavaScript().addFunction("menuItemTriggered", (JavaScriptFunction) arguments -> {
            if (arguments.length() == 1 && arguments.get(0) instanceof JsonString) {
                String menuId = arguments.get(0).asString();
                new Notification(
                        "Menu item clicked " + menuId,
                        Type.HUMANIZED_MESSAGE
                ).show(getPage());
            }
        });
    }

    protected void initLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSizeFull();

        VerticalLayout centerLayout = new VerticalLayout();
        centerLayout.setWidth(300, Unit.PIXELS);
        centerLayout.setHeightUndefined();

        Button showButton = new Button("Show value");
        centerLayout.addComponent(showButton);
        centerLayout.setComponentAlignment(showButton, Alignment.MIDDLE_RIGHT);

        Table table = new Table("The Brightest Stars");
        table.setSelectable(true);
        table.setWidth(100, Unit.PERCENTAGE);
        table.setHeightUndefined();

        table.addContainerProperty("Name", String.class, null);
        table.addContainerProperty("Mag", Float.class, null);

        table.addItem(new Object[]{"Canopus", -0.72f}, 2);
        table.addItem(new Object[]{"Arcturus", -0.04f}, 3);
        table.addItem(new Object[]{"Alpha Centauri", -0.01f}, 4);

        table.setPageLength(table.size());
        table.setValue(2);
        table.setNullSelectionAllowed(false);

        centerLayout.addComponent(table);

        layout.addComponent(centerLayout);
        layout.setComponentAlignment(centerLayout, Alignment.MIDDLE_CENTER);

        showButton.addClickListener((Button.ClickListener) event ->
                callElectronUiApi(new String[]{"Table value", String.valueOf(table.getValue())})
        );

        setContent(layout);
    }

    protected void callElectronUiApi(String[] args) {
        getPage().getJavaScript().execute("alert(callElectronUiApi([1,2,3]))");
    }
}