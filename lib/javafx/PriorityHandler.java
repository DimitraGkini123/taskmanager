package lib.javafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lib.json.JSONHandler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PriorityHandler {
    private static TableView<Task> taskTableRef;  // ✅ Stores reference to task table

    private static final String PRIORITIES_FILE = System.getProperty("user.dir") + "/medialab/priorities.json";
    private static ObservableList<String> priorities = FXCollections.observableArrayList();

    static {
        loadPriorities();
    }

    private static void loadPriorities() {
        List<String> loadedPriorities = new ArrayList<>();
        File file = new File(PRIORITIES_FILE);

        if (file.exists() && file.length() > 0) {
            try {
                String json = new String(Files.readAllBytes(Paths.get(PRIORITIES_FILE)));
                json = json.replace("[", "").replace("]", "").replace("\"", "");
                String[] priorityArray = json.split(",");

                for (String p : priorityArray) {
                    if (!p.trim().isEmpty()) {
                        loadedPriorities.add(p.trim());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!loadedPriorities.contains("Default")) {
            loadedPriorities.add(0, "Default");
        }

        priorities.setAll(loadedPriorities);
    }

    public static void savePriorities() {
        String json = "[\"" + String.join("\",\"", priorities) + "\"]";

        try (FileWriter writer = new FileWriter(PRIORITIES_FILE)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ObservableList<String> getPriorities() {
        return priorities;
    }

    public static void showManagePrioritiesDialog(TableView<Task> taskTable) {
        taskTableRef = taskTable;
        Stage manageDialog = new Stage();
        manageDialog.initModality(Modality.APPLICATION_MODAL);
        manageDialog.setTitle("Manage Priorities");

        ListView<String> priorityListView = new ListView<>(FXCollections.observableArrayList(priorities));
        priorityListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        Button addButton = new Button("Add");
        Button renameButton = new Button("Rename");
        Button deleteButton = new Button("Delete");
        Button okButton = new Button("OK");


        addButton.setOnAction(e -> {
            String newPriority = showNewPriorityDialog();
            if (newPriority != null && !newPriority.trim().isEmpty() && !priorities.contains(newPriority)) {
                priorities.add(newPriority);
                savePriorities();
               priorityListView.setItems(FXCollections.observableArrayList(priorities));
            }
        });


        renameButton.setOnAction(e -> {
            String selectedPriority = priorityListView.getSelectionModel().getSelectedItem();
            if (selectedPriority != null && isRenamable(selectedPriority)) {
                TextInputDialog renameDialog = new TextInputDialog(selectedPriority);
                renameDialog.setTitle("Rename Priority");
                renameDialog.setHeaderText("Enter a new name for the priority:");
                renameDialog.setContentText("New Name:");

                renameDialog.showAndWait().ifPresent(newName -> {
                    if (!newName.trim().isEmpty() && !priorities.contains(newName)) {
                        int index = priorities.indexOf(selectedPriority);
                        priorities.set(index, newName);
                        savePriorities();

                        updateTasksWithRenamedPriority(selectedPriority, newName);
                        priorityListView.setItems(FXCollections.observableArrayList(priorities));
                        refreshTaskTable();
                        System.out.println("Priority '" + selectedPriority + "' renamed to '" + newName + "' and tasks updated.");
                    } else {
                        showAlert("Rename Error", "Invalid or duplicate priority name.");
                    }
                });
            } else {
                showAlert("Rename Error", "This priority cannot be renamed.");
            }
        });

        deleteButton.setOnAction(e -> {
            String selectedPriority = priorityListView.getSelectionModel().getSelectedItem();

            if (selectedPriority != null && isDeletable(selectedPriority)) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Priority");
                confirm.setHeaderText("Are you sure you want to delete: " + selectedPriority + "?");
                confirm.setContentText("All tasks with this priority will become 'Default'.");

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        System.out.println("🗑 Deleting priority: " + selectedPriority);

                        priorityListView.getSelectionModel().clearSelection();
                        updateTasksWithDefaultPriority(selectedPriority);

                        priorities.remove(selectedPriority);
                        savePriorities();

                        priorityListView.setItems(FXCollections.observableArrayList(priorities));
                        refreshTaskTable();
                        System.out.println("✅ Priority and associated tasks successfully updated.");
                    }
                });
            } else {
                showAlert("Delete Error", "This priority cannot be deleted.");
            }
        });

        okButton.setOnAction(e -> {
            System.out.println("OK button clicked: Closing priority manager and refreshing task table.");
            refreshTaskTable();
            manageDialog.close();
        });

        HBox buttonBox = new HBox(10, addButton, renameButton, deleteButton, okButton);
        VBox layout = new VBox(10, priorityListView, buttonBox);
        manageDialog.setScene(new Scene(layout, 300, 300));
        manageDialog.showAndWait();
    }
    private static String showNewPriorityDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Priority");
        dialog.setHeaderText("Enter a new priority level:");
        dialog.setContentText("Priority:");
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private static void updateTasksWithRenamedPriority(String oldPriority, String newPriority) {
        System.out.println("Renaming tasks from priority '" + oldPriority + "' to '" + newPriority + "'...");

        List<Task> taskList = JSONHandler.readTasks();
        boolean updated = false;

        for (Task task : taskList) {
            if (task.getPriority().equalsIgnoreCase(oldPriority)) {
                task.setPriority(newPriority);
                updated = true;
            }
        }

        if (updated) {
            JSONHandler.writeTasks(taskList);
            System.out.println("✅ Tasks updated with new priority name.");
        } else {
            System.out.println("ℹ️ No tasks found with priority '" + oldPriority + "'.");
        }
    }

    private static void updateTasksWithDefaultPriority(String deletedPriority) {
        System.out.println("🔄 Updating tasks: Setting priority '" + deletedPriority + "' to 'Default'");

        List<Task> taskList = JSONHandler.readTasks();
        boolean updated = false;

        for (Task task : taskList) {
            if (task.getPriority().equalsIgnoreCase(deletedPriority)) {
                task.setPriority("Default");
                updated = true;
            }
        }

        if (updated) {
            JSONHandler.writeTasks(taskList);
            System.out.println("✅ Tasks successfully updated to 'Default' priority.");
        } else {
            System.out.println("⚠️ No tasks required priority updates.");
        }
    }

    private static void refreshTaskTable() {
        if (taskTableRef == null) {
            System.out.println("⚠️ Task table reference is null! UI will not update.");
            return;
        }

        System.out.println("🔄 Refreshing task table in UI...");

        List<Task> latestTasks = JSONHandler.readTasks();
        ObservableList<Task> latestTaskList = FXCollections.observableArrayList(latestTasks);

        taskTableRef.setItems(latestTaskList);
        taskTableRef.refresh();

        System.out.println("✅ UI Task table updated.");
    }

    private static boolean isRenamable(String priority) {
        return !priority.equals("Default");
    }

    private static boolean isDeletable(String priority) {
        return !priority.equals("Default");
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
