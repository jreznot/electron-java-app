package org.strangeway.electronvaadin.app;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.server.FontAwesome;
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
        JavaScript js = getPage().getJavaScript();
        js.addFunction("appMenuItemTriggered", arguments -> {
            if (arguments.length() == 1 && arguments.get(0) instanceof JsonString) {
                String menuId = arguments.get(0).asString();
                new Notification(
                        "Menu item clicked " + menuId,
                        Type.HUMANIZED_MESSAGE
                ).show(getPage());
            }
        });
        js.addFunction("appWindowExit", arguments -> {
            // todo show confirmation dialog
            // todo if Yes then call function from Electron UI
        });
    }

    protected void initLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSizeFull();

        VerticalLayout centerLayout = new VerticalLayout();
        centerLayout.setSpacing(true);
        centerLayout.setWidth(400, Unit.PIXELS);
        centerLayout.setHeight(100, Unit.PERCENTAGE);

        // todo remove
        Button showButton = new Button("Show value");
        showButton.addClickListener((Button.ClickListener) event ->
                callElectronUiApi(new String[]{"Table value", ""})
        );

        Label titleLabel = new Label("Active tasks");
        titleLabel.setStyleName(ValoTheme.LABEL_H1);
        centerLayout.addComponent(titleLabel);

        Button addButton = new Button("Add");
        addButton.setIcon(FontAwesome.PLUS);
        addButton.setStyleName(ValoTheme.BUTTON_PRIMARY);

        Button removeButton = new Button("Remove");
        removeButton.setIcon(FontAwesome.TRASH_O);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(addButton);
        buttonsLayout.addComponent(removeButton);

        centerLayout.addComponent(buttonsLayout);
        centerLayout.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_RIGHT);

        Grid grid = new Grid();
        grid.setSizeFull();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.addColumn("Summary");

        centerLayout.addComponent(grid);
        centerLayout.setExpandRatio(grid, 1);

        layout.addComponent(centerLayout);
        layout.setComponentAlignment(centerLayout, Alignment.TOP_CENTER);

        setContent(layout);
    }

    protected void callElectronUiApi(String[] args) {
        getPage().getJavaScript().execute("alert(callElectronUiApi([1,2,3]))");
    }
}