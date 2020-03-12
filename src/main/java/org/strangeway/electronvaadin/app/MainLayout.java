package org.strangeway.electronvaadin.app;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Shortcuts;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.shared.ui.Transport;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Yuriy Artamonov
 * @author Erik Lumme
 */
@CssImport("./styles/electron.css")
@CssImport(value = "./styles/menu-bar.css", themeFor = "vaadin-menu-bar")
@CssImport(value = "./styles/button.css", themeFor = "vaadin-button")
@CssImport(value = "./styles/grid.css", themeFor = "vaadin-grid")
@JsModule("./src/electron-bridge.js")
@Push(transport = Transport.WEBSOCKET)
@Route("")
public class MainLayout extends VerticalLayout {

    private Grid<Task> tasksGrid;

    private Dialog confirmDialog;

    @Override
    public void onAttach(AttachEvent attachEvent) {
        if (attachEvent.isInitialAttach()) {
            initLayout();

            // Expose the @ClientCallable methods through the global window object
            getElement().executeJs("window.vaadinApi = this.$server");
        }
    }

    private void initLayout() {
        addClassName("window-root");
        setSpacing(false);
        setPadding(false);
        setSizeFull();

        initMenu();

        var windowContent = new VerticalLayout();
        windowContent.setSizeFull();
        windowContent.setSpacing(false);
        windowContent.setPadding(false);
        windowContent.addClassName("window-content");

        var centerLayout = new VerticalLayout();
        centerLayout.addClassName("window-inner");
        centerLayout.setWidthFull();
        centerLayout.setHeightFull();

        var titleLabel = new H1("Active tasks");
        centerLayout.add(titleLabel);

        var tasks = new ArrayList<Task>();
        var dataProvider = new ListDataProvider<>(tasks);

        var addButton = new Button("Add", VaadinIcon.PLUS.create());
        addButton.focus();
        addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addButton.addClickListener(event -> {
            var task = new Task(false, "New task");
            tasks.add(task);

            tasksGrid.getEditor().save();
            dataProvider.refreshAll();

            tasksGrid.select(task);
        });

        var removeButton = new Button("Remove", VaadinIcon.TRASH.create());
        removeButton.setEnabled(false);
        removeButton.addClickListener(event -> {
            var selectedItems = tasksGrid.getSelectedItems();
            tasks.removeAll(selectedItems);
            dataProvider.refreshAll();

            var iterator = dataProvider.getItems().iterator();
            if (iterator.hasNext()) {
                tasksGrid.select(iterator.next());
            }
        });

        var buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.add(addButton);
        buttonsLayout.add(removeButton);

        centerLayout.add(buttonsLayout);
        centerLayout.setAlignSelf(Alignment.END, buttonsLayout);

        tasksGrid = new Grid<>();
        tasksGrid.setDataProvider(dataProvider);
        tasksGrid.setSizeFull();
        tasksGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        tasksGrid.addSelectionListener(event -> {
            var enableRemove = !event.getAllSelectedItems().isEmpty();
            removeButton.setEnabled(enableRemove);
        });

        var binder = new Binder<>(Task.class);
        var editor = tasksGrid.getEditor();
        editor.setBinder(binder);
        editor.setBuffered(true);

        var checkbox = new Checkbox();
        binder.forField(checkbox).bind("done");

        var textField = new TextField();
        textField.setWidthFull();
        binder.forField(textField).asRequired().bind("summary");

        tasksGrid.addItemDoubleClickListener(e -> {
            if (tasksGrid.getEditor().isOpen()) {
                var item = tasksGrid.getEditor().getItem();
                tasksGrid.getEditor().save();
                if (e.getItem().equals(item)) {
                    return;
                }
            }
            tasksGrid.getEditor().editItem(e.getItem());
            checkbox.focus();
        });
        Shortcuts.addShortcutListener(tasksGrid,
                () -> tasksGrid.getEditor().save(), Key.ENTER).listenOn(tasksGrid);
        Shortcuts.addShortcutListener(tasksGrid,
                () -> tasksGrid.getEditor().cancel(), Key.ESCAPE).listenOn(tasksGrid);

        tasksGrid.addColumn(task -> task.isDone() ? "Yes" : "No")
                .setHeader("Done")
                .setWidth("80px")
                .setFlexGrow(0)
                .setClassNameGenerator(item -> item.isDone() ? "task-done" : "task-todo")
                .setEditorComponent(checkbox);

        tasksGrid.addColumn(Task::getSummary)
                .setHeader("Summary")
                .setEditorComponent(textField);

        centerLayout.add(tasksGrid);
        centerLayout.setFlexGrow(1, tasksGrid);

        windowContent.add(centerLayout);

        addAndExpand(windowContent);
    }

    private void initMenu() {
        var menuLayout = new HorizontalLayout();
        menuLayout.setSpacing(false);
        menuLayout.addClassName("window-header");
        menuLayout.setWidth("100%");

        var titleLabel = new MenuBar();
        titleLabel.addClassName("window-title");
        titleLabel.addThemeVariants(MenuBarVariant.LUMO_TERTIARY);

        var mainMenuItem = addMenuItem(titleLabel, "Tasks", VaadinIcon.TASKS);
        mainMenuItem.getSubMenu().addItem("About", selectedItem -> onMenuAbout());

        if (!VaadinService.getCurrent().getDeploymentConfiguration().isProductionMode()) {
            mainMenuItem.getSubMenu().addItem("Developer tools", selectedItem -> callElectronUiApi("devtools"));
        }

        mainMenuItem.getSubMenu().add(new Hr());
        mainMenuItem.getSubMenu().addItem("Exit", selectedItem -> onWindowExit());

        menuLayout.addAndExpand(titleLabel);

        var minimizeBtn = new Button(VaadinIcon.MINUS.create());
        minimizeBtn.addClickListener(event -> callElectronUiApi("minimize"));
        minimizeBtn.addClassName("window-control");
        minimizeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        minimizeBtn.getElement().setProperty("title", "Minimize");

        var maximizeBtn = new Button(VaadinIcon.PLUS.create());
        maximizeBtn.addClickListener(event -> callElectronUiApi("maximize"));
        maximizeBtn.addClassName("window-control");
        maximizeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        maximizeBtn.getElement().setProperty("title", "Maximize");

        var closeBtn = new Button(VaadinIcon.CLOSE.create());
        closeBtn.addClickListener(event -> onWindowExit());
        closeBtn.addClassName("window-control");
        closeBtn.addClassName("window-close");
        closeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ICON);
        closeBtn.getElement().setProperty("title", "Close");

        menuLayout.add(minimizeBtn, maximizeBtn, closeBtn);

        add(menuLayout);
    }

    private MenuItem addMenuItem(MenuBar menuBar, String caption, VaadinIcon icon) {
        var menuItem = menuBar.addItem(caption);
        menuItem.addComponentAsFirst(icon.create());
        return menuItem;
    }

    @ClientCallable
    private void appMenuItemTriggered(String menuId) {
        switch (menuId) {
            case "About":
                onMenuAbout();
                break;
            case "Exit":
                onWindowExit();
                break;
        }
    }

    @ClientCallable
    private void appWindowExit() {
        onWindowExit();
    }

    private void callElectronUiApi(Serializable... args) {
        UI.getCurrent().getPage().executeJs("callElectronUiApi($0)", args);
    }

    private void onMenuAbout() {
        var helpWindow = new Dialog();
        helpWindow.setSizeUndefined();

        var content = new VerticalLayout();
        content.setSizeUndefined();
        content.setPadding(false);

        content.add(new H2("About"));

        var aboutLabel = new Html("<span>Electron+Vaadin Demo<br>Authors: Yuriy Artamonov, Erik Lumme</span>");
        content.add(aboutLabel);

        var okBtn = new Button("Ok", VaadinIcon.CHECK.create());
        okBtn.focus();
        okBtn.addClickListener(event -> helpWindow.close());

        content.add(okBtn);
        content.setAlignSelf(Alignment.CENTER, okBtn);

        helpWindow.add(content);
        helpWindow.open();
    }

    private void onWindowExit() {
        if (confirmDialog != null && confirmDialog.isOpened()) {
            return;
        }

        if (confirmDialog == null) {
            confirmDialog = new Dialog();

            var layout = new VerticalLayout(new H2("Exit confirmation"));
            layout.setPadding(false);

            var confirmationText = new Span("Are you sure?");
            confirmationText.setSizeUndefined();
            layout.add(confirmationText);

            var buttonsLayout = new HorizontalLayout();
            buttonsLayout.setSpacing(true);

            var yesBtn = new Button("Yes", VaadinIcon.SIGN_OUT.create());
            yesBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            yesBtn.addClickListener(event -> {
                confirmDialog.close();
                callElectronUiApi("exit");
            });
            buttonsLayout.add(yesBtn);

            var noBtn = new Button("No", VaadinIcon.CLOSE.create());
            noBtn.addClickListener(event -> confirmDialog.close());
            buttonsLayout.add(noBtn);

            layout.add(buttonsLayout);

            confirmDialog.add(layout);
            confirmDialog.addAttachListener(e -> yesBtn.focus());
        }

        confirmDialog.open();
    }

    @SuppressWarnings("unused")
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