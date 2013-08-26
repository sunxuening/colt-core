package codeOrchestra.colt.core.ui

import codeOrchestra.colt.core.ColtException
import codeOrchestra.colt.core.ColtProjectManager
import codeOrchestra.colt.core.errorhandling.ErrorHandler
import codeOrchestra.colt.core.license.CodeOrchestraLicenseManager
import codeOrchestra.colt.core.license.ExpirationHelper
import codeOrchestra.colt.core.license.LicenseListener
import codeOrchestra.colt.core.model.Project
import codeOrchestra.colt.core.model.listener.ProjectListener
import codeOrchestra.colt.core.ui.dialog.ColtDialogs
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyCodeCombination
import javafx.scene.input.KeyCombination
import javafx.stage.FileChooser

import static codeOrchestra.colt.core.RecentProjects.getRecentProjectsPaths

class ColtMenuBar extends MenuBar {

    private Menu recentProjectsSubMenu
    ArrayList<MenuItem> popupMenuItems = new ArrayList<>()

    ColtMenuBar() {
        Menu fileMenu = new Menu("File")

        MenuItem openProjectMenuItem = new MenuItem("Open Project")
        openProjectMenuItem.accelerator = new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN)
        openProjectMenuItem.onAction = { t ->
            FileChooser fileChooser = new FileChooser()
            fileChooser.extensionFilters.add(new FileChooser.ExtensionFilter("COLT", "*.colt"))
            File file = fileChooser.showOpenDialog(scene.window)
            if (file != null) {
                try {
                    ColtProjectManager.instance.load(file.getPath())
                } catch (ColtException e) {
                    ErrorHandler.handle(e, "Can't load the project")
                }
            }
        } as EventHandler<ActionEvent>

        MenuItem saveProjectMenuItem = new MenuItem("Save Project")
        saveProjectMenuItem.accelerator = new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN)
        saveProjectMenuItem.onAction = { t ->
            println("save project")
            try {
                ColtProjectManager.getInstance().save()
            } catch (ColtException e) {
                ErrorHandler.handle(e, "Can't save the project")
            }
        } as EventHandler<ActionEvent>
        saveProjectMenuItem.disable = true
        ColtProjectManager.instance.addProjectListener(new ProjectListener() {
            @Override
            public void onProjectLoaded(Project project) {
                saveProjectMenuItem.disable = false
            }

            @Override
            public void onProjectUnloaded(Project project) {
                saveProjectMenuItem.disable = true
            }
        })

        MenuItem newProjectMenuItem = new MenuItem("New Project")
        newProjectMenuItem.onAction = { t ->
            String projectName = ColtDialogs.showCreateProjectDialog(scene.window)

            if (projectName != null) {
                FileChooser fileChooser = new FileChooser()
                fileChooser.initialFileName = projectName
                fileChooser.extensionFilters.add(new FileChooser.ExtensionFilter("COLT", "*.colt"))
                File file = fileChooser.showSaveDialog(scene.window)
                if (file != null) {
                    try {
                        // TODO: a handler must be defined by the user (AS, JS, etc)
                        ColtProjectManager.instance.create("AS", projectName, file)
                    } catch (ColtException e) {
                        ErrorHandler.handle(e, "Can't create a new project")
                    }
                }
            }
        } as EventHandler<ActionEvent>

        MenuItem exitMenuItem = new MenuItem("Exit")
        exitMenuItem.onAction = { t ->
            System.exit(0)
        } as EventHandler<ActionEvent>

        recentProjectsSubMenu = new Menu("Open Recent")
        refreshRecentProjectsMenu()
        ColtProjectManager.instance.addProjectListener(new ProjectListener() {
            @Override
            public void onProjectLoaded(Project project) {
                refreshRecentProjectsMenu()
            }

            @Override
            public void onProjectUnloaded(Project project) {
            }
        })

        popupMenuItems.addAll(newProjectMenuItem, openProjectMenuItem, saveProjectMenuItem)

        fileMenu.items.addAll(newProjectMenuItem, new SeparatorMenuItem(), openProjectMenuItem, recentProjectsSubMenu, saveProjectMenuItem, new SeparatorMenuItem(), exitMenuItem)

        Menu helpMenu = new Menu("Help")
        final MenuItem enterSerialItem = new MenuItem("Enter Serial Number")
        enterSerialItem.onAction = { t ->
            ExpirationHelper.getExpirationStrategy().showSerialNumberDialog()
        } as EventHandler<ActionEvent>
        enterSerialItem.disable = ExpirationHelper.expirationStrategy.trialOnly || !CodeOrchestraLicenseManager.noSerialNumberPresent()
        CodeOrchestraLicenseManager.addListener({
            enterSerialItem.disable = false
        } as LicenseListener)
        helpMenu.items.add(enterSerialItem)

        menus.add(fileMenu)
        menus.add(helpMenu)
        setUseSystemMenuBar(true)
    }

    private void refreshRecentProjectsMenu() {
        recentProjectsSubMenu.items.removeAll()

        List<String> recentProjectsPaths = recentProjectsPaths
        if (recentProjectsPaths.empty) {
            recentProjectsSubMenu.disable = true
            return
        }

        recentProjectsSubMenu.disable = false

        for (String recentProjectsPath : recentProjectsPaths) {
            MenuItem openRecentProjectItem = new MenuItem(recentProjectsPath)

            final File projectFile = new File(recentProjectsPath)
            if (!projectFile.exists() || projectFile.isDirectory()) {
                continue
            }

            openRecentProjectItem.onAction = { actionEvent ->
                try {
                    ColtProjectManager.instance.load(projectFile.path)
                } catch (ColtException e) {
                    ErrorHandler.handle(e, "Can't load a project " + recentProjectsPath)
                }
            } as EventHandler<ActionEvent>

            recentProjectsSubMenu.items.add(openRecentProjectItem)
        }
    }
}