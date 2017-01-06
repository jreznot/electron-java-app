package org.strangeway.electronvaadin.app;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Theme;
import com.vaadin.data.Container;
import com.vaadin.data.util.converter.StringToBooleanConverter;
import com.vaadin.server.FontAwesome;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonString;

/**
 * @author Yuriy Artamonov
 */
@PreserveOnRefresh
@Theme(ValoTheme.THEME_NAME)
public class MainUI extends UI {

    protected Grid tasksGrid;

    @Override
    protected void init(VaadinRequest request) {
        initLayout();
        initElectronApi();
    }

    protected void initLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSizeFull();

        VerticalLayout centerLayout = new VerticalLayout();
        centerLayout.setSpacing(true);
        centerLayout.setWidth(400, Unit.PIXELS);
        centerLayout.setHeight(100, Unit.PERCENTAGE);

        Label titleLabel = new Label("Active tasks");
        titleLabel.setStyleName(ValoTheme.LABEL_H1);
        centerLayout.addComponent(titleLabel);

        Button addButton = new Button("Add", FontAwesome.PLUS);
        addButton.focus();
        addButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        addButton.addClickListener(event -> {
            Object itemId = tasksGrid.addRow(false, "New task");
            tasksGrid.select(itemId);
        });

        Button removeButton = new Button("Remove", FontAwesome.TRASH_O);
        removeButton.setEnabled(false);
        removeButton.addClickListener(event -> {
            Object selectedItemId = tasksGrid.getSelectedRow();
            if (selectedItemId != null) {
                Container.Indexed ds = tasksGrid.getContainerDataSource();
                ds.removeItem(selectedItemId);
                removeButton.setEnabled(false);
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(addButton);
        buttonsLayout.addComponent(removeButton);

        centerLayout.addComponent(buttonsLayout);
        centerLayout.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_RIGHT);

        tasksGrid = new Grid();
        tasksGrid.setSizeFull();
        tasksGrid.setEditorEnabled(true);
        tasksGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        tasksGrid.addColumn("Done", Boolean.class);
        tasksGrid.getColumn("Done").setWidth(80);
        tasksGrid.getColumn("Done").setConverter(new StringToBooleanConverter("Yes", "No"));

        tasksGrid.addColumn("Summary");

        tasksGrid.addSelectionListener(event -> {
            boolean enableRemove = !event.getSelected().isEmpty();
            removeButton.setEnabled(enableRemove);
        });

        centerLayout.addComponent(tasksGrid);
        centerLayout.setExpandRatio(tasksGrid, 1);

        layout.addComponent(centerLayout);
        layout.setComponentAlignment(centerLayout, Alignment.TOP_CENTER);

        setContent(layout);
    }

    protected void initElectronApi() {
        JavaScript js = getPage().getJavaScript();
        js.addFunction("appMenuItemTriggered", arguments -> {
            if (arguments.length() == 1 && arguments.get(0) instanceof JsonString) {
                String menuId = arguments.get(0).asString();
                if ("Help".equals(menuId)) {
                    onMenuHelp();
                } else if ("Exit".equals(menuId)) {
                    onWindowExit();
                }
            }
        });
        js.addFunction("appWindowExit", arguments -> onWindowExit());
    }

    protected void callElectronUiApi(String[] args) {
        JsonArray paramsArray = Json.createArray();
        int i = 0;
        for (String arg : args) {
            paramsArray.set(i, Json.create(arg));
            i++;
        }
        getPage().getJavaScript().execute("callElectronUiApi(" + paramsArray.toJson() + ")");
    }

    protected void onMenuHelp() {

    }

    protected void onWindowExit() {
        if (!getUI().getWindows().isEmpty()) {
            // it seems that confirmation window is already shown
            return;
        }

        Window confirmationWindow = new Window();
        confirmationWindow.setResizable(false);
        confirmationWindow.setModal(true);
        confirmationWindow.setCaption("Exit confirmation");
        confirmationWindow.setSizeUndefined();

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setMargin(true);
        layout.setWidthUndefined();

        Label confirmationText = new Label("Are you sure?");
        confirmationText.setSizeUndefined();
        layout.addComponent(confirmationText);

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        Button yesBtn = new Button("Yes", FontAwesome.SIGN_OUT);
        yesBtn.focus();
        yesBtn.addClickListener(event -> {
            confirmationWindow.close();
            callElectronUiApi(new String[]{"exit"});
        });
        buttonsLayout.addComponent(yesBtn);

        Button noBtn = new Button("No", FontAwesome.CLOSE);
        noBtn.addClickListener(event -> confirmationWindow.close());
        buttonsLayout.addComponent(noBtn);

        layout.addComponent(buttonsLayout);

        confirmationWindow.setContent(layout);

        getUI().addWindow(confirmationWindow);
    }
}