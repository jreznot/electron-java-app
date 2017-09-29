package org.strangeway.electronvaadin.app;

import com.vaadin.annotations.PreserveOnRefresh;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.icons.VaadinIcons;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.shared.ui.ui.Transport;
import com.vaadin.ui.*;
import com.vaadin.ui.renderers.TextRenderer;
import com.vaadin.ui.themes.ValoTheme;
import elemental.json.Json;
import elemental.json.JsonArray;
import elemental.json.JsonString;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Yuriy Artamonov
 */
@PreserveOnRefresh
@Theme(ValoTheme.THEME_NAME)
@Push(transport = Transport.WEBSOCKET)
public class MainUI extends UI {

    private Grid<Task> tasksGrid;

    @Override
    protected void init(VaadinRequest request) {
        initLayout();
        initElectronApi();
    }

    private void initLayout() {
        VerticalLayout layout = new VerticalLayout();
        layout.setMargin(false);
        layout.setSpacing(false);
        layout.setSizeFull();

        initMenu(layout);

        VerticalLayout windowContent = new VerticalLayout();
        windowContent.setSizeFull();
        windowContent.setMargin(false);
        windowContent.setSpacing(false);
        windowContent.addStyleName("window-content");

        VerticalLayout centerLayout = new VerticalLayout();
        centerLayout.setMargin(true);
        centerLayout.setSpacing(true);
        centerLayout.setWidth(500, Unit.PIXELS);
        centerLayout.setHeight(100, Unit.PERCENTAGE);

        Label titleLabel = new Label("Active tasks");
        titleLabel.setStyleName(ValoTheme.LABEL_H1);
        centerLayout.addComponent(titleLabel);

        ArrayList<Task> tasks = new ArrayList<>();
        ListDataProvider<Task> dataProvider =
                new ListDataProvider<>(tasks);

        Button addButton = new Button("Add", VaadinIcons.PLUS);
        addButton.focus();
        addButton.setStyleName(ValoTheme.BUTTON_PRIMARY);
        addButton.addClickListener(event -> {
            Task task = new Task(false, "New task");
            tasks.add(task);
            dataProvider.refreshAll();

            tasksGrid.select(task);
        });

        Button removeButton = new Button("Remove", VaadinIcons.TRASH);
        removeButton.setEnabled(false);
        removeButton.addClickListener(event -> {
            Set<Task> selectedItems = tasksGrid.getSelectedItems();
            tasks.removeAll(selectedItems);
            dataProvider.refreshAll();

            Iterator<Task> iterator = dataProvider.getItems().iterator();
            if (iterator.hasNext()) {
                tasksGrid.select(iterator.next());
            }
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.addComponent(addButton);
        buttonsLayout.addComponent(removeButton);

        centerLayout.addComponent(buttonsLayout);
        centerLayout.setComponentAlignment(buttonsLayout, Alignment.MIDDLE_RIGHT);

        tasksGrid = new Grid<>(Task.class);
        tasksGrid.setDataProvider(dataProvider);
        tasksGrid.setSizeFull();
        tasksGrid.getEditor().setEnabled(true);
        tasksGrid.setSelectionMode(Grid.SelectionMode.SINGLE);

        @SuppressWarnings("unchecked")
        Grid.Column<Task, Boolean> doneColumn = (Grid.Column<Task, Boolean>) tasksGrid.getColumn("done");
        doneColumn.setEditorComponent(new CheckBox());

        doneColumn.setCaption("Done")
                .setWidth(80)
                .setRenderer(isDone -> {
                    if (isDone) {
                        return "Yes";
                    }
                    return "No";
                }, new TextRenderer());

        tasksGrid.getColumn("summary")
                .setCaption("Summary")
                .setEditorComponent(new TextField());

        tasksGrid.addSelectionListener(event -> {
            boolean enableRemove = !event.getAllSelectedItems().isEmpty();
            removeButton.setEnabled(enableRemove);
        });

        centerLayout.addComponent(tasksGrid);
        centerLayout.setExpandRatio(tasksGrid, 1);

        windowContent.addComponent(centerLayout);
        windowContent.setComponentAlignment(centerLayout, Alignment.TOP_CENTER);

        layout.addComponentsAndExpand(windowContent);

        setContent(layout);
    }

    private void initMenu(VerticalLayout layout) {
        HorizontalLayout menuLayout = new HorizontalLayout();
        menuLayout.setSpacing(false);
        menuLayout.addStyleName("window-header");
        menuLayout.setWidth("100%");

        MenuBar titleLabel = new MenuBar();
        titleLabel.addStyleName("window-title");
        titleLabel.addStyleName(ValoTheme.MENUBAR_BORDERLESS);

        MenuBar.MenuItem mainMenuItem = titleLabel.addItem("Tasks", VaadinIcons.TASKS, null);
        mainMenuItem.setStyleName("title-menu-item");
        mainMenuItem.addItem("About", selectedItem -> onMenuAbout());
        mainMenuItem.addSeparator();
        mainMenuItem.addItem("Exit", selectedItem -> onWindowExit());

        menuLayout.addComponentsAndExpand(titleLabel);
        menuLayout.setComponentAlignment(titleLabel, Alignment.MIDDLE_LEFT);

        Button minimizeBtn = new Button(VaadinIcons.MINUS);
        minimizeBtn.addClickListener(event -> callElectronUiApi(new String[]{"minimize"}));
        minimizeBtn.addStyleName("window-control");
        minimizeBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        minimizeBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        minimizeBtn.setDescription("Minimize");

        Button maximizeBtn = new Button(VaadinIcons.PLUS);
        maximizeBtn.addClickListener(event -> callElectronUiApi(new String[]{"maximize"}));
        maximizeBtn.addStyleName("window-control");
        maximizeBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        maximizeBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        maximizeBtn.setDescription("Maximize");

        Button closeBtn = new Button(VaadinIcons.CLOSE);
        closeBtn.addClickListener(event -> onWindowExit());
        closeBtn.addStyleName("window-control");
        closeBtn.addStyleName("window-close");
        closeBtn.addStyleName(ValoTheme.BUTTON_BORDERLESS);
        closeBtn.addStyleName(ValoTheme.BUTTON_ICON_ONLY);
        closeBtn.setDescription("Close");

        menuLayout.addComponents(minimizeBtn, maximizeBtn, closeBtn);
        menuLayout.setComponentAlignment(minimizeBtn, Alignment.MIDDLE_RIGHT);
        menuLayout.setComponentAlignment(maximizeBtn, Alignment.MIDDLE_RIGHT);
        menuLayout.setComponentAlignment(closeBtn, Alignment.MIDDLE_RIGHT);

        layout.addComponent(menuLayout);
    }

    private void initElectronApi() {
        JavaScript js = getPage().getJavaScript();
        js.addFunction("appMenuItemTriggered", arguments -> {
            if (arguments.length() == 1 && arguments.get(0) instanceof JsonString) {
                String menuId = arguments.get(0).asString();
                if ("About".equals(menuId)) {
                    onMenuAbout();
                } else if ("Exit".equals(menuId)) {
                    onWindowExit();
                }
            }
        });
        js.addFunction("appWindowExit", arguments -> onWindowExit());

        Page.Styles styles = getPage().getStyles();
        try {
            InputStream resource = MainUI.class.getResourceAsStream(
                    "/org/strangeway/electronvaadin/resources/electron.css");
            styles.add(IOUtils.toString(resource, StandardCharsets.UTF_8));
        } catch (IOException ignored) {
        }
    }

    private void callElectronUiApi(String[] args) {
        JsonArray paramsArray = Json.createArray();
        int i = 0;
        for (String arg : args) {
            paramsArray.set(i, Json.create(arg));
            i++;
        }
        getPage().getJavaScript().execute("callElectronUiApi(" + paramsArray.toJson() + ")");
    }

    private void onMenuAbout() {
        Window helpWindow = new Window();
        helpWindow.setCaption("About");
        helpWindow.setModal(true);
        helpWindow.setResizable(false);

        helpWindow.setSizeUndefined();

        VerticalLayout content = new VerticalLayout();
        content.setSizeUndefined();
        content.setMargin(true);
        content.setSpacing(true);

        Label aboutLabel = new Label("Electron+Vaadin Demo\nAuthor: Yuriy Artamonov");
        aboutLabel.setContentMode(ContentMode.PREFORMATTED);
        aboutLabel.setSizeUndefined();

        content.addComponent(aboutLabel);

        Button okBtn = new Button("Ok", VaadinIcons.CHECK);
        okBtn.focus();
        okBtn.addClickListener(event -> helpWindow.close());

        content.addComponent(okBtn);
        content.setComponentAlignment(okBtn, Alignment.MIDDLE_CENTER);

        helpWindow.setContent(content);

        getUI().addWindow(helpWindow);
    }

    private void onWindowExit() {
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

        Button yesBtn = new Button("Yes", VaadinIcons.SIGN_OUT);
        yesBtn.focus();
        yesBtn.addClickListener(event -> {
            confirmationWindow.close();
            callElectronUiApi(new String[]{"exit"});
        });
        buttonsLayout.addComponent(yesBtn);

        Button noBtn = new Button("No", VaadinIcons.CLOSE);
        noBtn.addClickListener(event -> confirmationWindow.close());
        buttonsLayout.addComponent(noBtn);

        layout.addComponent(buttonsLayout);

        confirmationWindow.setContent(layout);

        getUI().addWindow(confirmationWindow);
    }

    public static class Task {
        private boolean done;
        private String summary;

        public Task(boolean done, String summary) {
            this.done = done;
            this.summary = summary;
        }

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public boolean isDone() {
            return done;
        }

        public void setDone(boolean done) {
            this.done = done;
        }
    }
}