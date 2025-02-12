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

public class CategoryHandler {
    private static TableView<Task> taskTableRef; 


    private static final String CATEGORIES_FILE = System.getProperty("user.dir") + "/medialab/categories.json";
    private static ObservableList<String> categories = FXCollections.observableArrayList();

    static {
        loadCategories();
    }

    private static void loadCategories() {
        List<String> loadedCategories = new ArrayList<>();
        File file = new File(CATEGORIES_FILE);

        if (file.exists() && file.length() > 0) {
            try {
                String json = new String(Files.readAllBytes(Paths.get(CATEGORIES_FILE)));
                json = json.replace("[", "").replace("]", "").replace("\"", "");
                String[] categoryArray = json.split(",");

                for (String c : categoryArray) {
                    if (!c.trim().isEmpty()) {
                        loadedCategories.add(c.trim());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!loadedCategories.contains("General")) {
            loadedCategories.add(0, "General"); // Ensure "General" category exists
        }

        categories.setAll(loadedCategories);
    }

    public static void saveCategories() {
        String json = "[\"" + String.join("\",\"", categories) + "\"]";

        try (FileWriter writer = new FileWriter(CATEGORIES_FILE)) {
            writer.write(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static ObservableList<String> getCategories() {
        return categories;
    }

    public static void showManageCategoriesDialog(TableView<Task> taskTable) {  
        taskTableRef = taskTable;
        Stage manageDialog = new Stage();
        manageDialog.initModality(Modality.APPLICATION_MODAL);
        manageDialog.setTitle("Manage Categories");

        ListView<String> categoryListView = new ListView<>(FXCollections.observableArrayList(categories));
        categoryListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        Button addButton = new Button("Add");
        Button renameButton = new Button("Rename");
        Button deleteButton = new Button("Delete");
        Button okButton = new Button("OK");

        addButton.setOnAction(e -> {
            String newCategory = showNewCategoryDialog();
            if (newCategory != null && !newCategory.trim().isEmpty() && !categories.contains(newCategory)) {
                categories.add(newCategory);
                saveCategories();
                categoryListView.setItems(FXCollections.observableArrayList(categories));
            }
        });

        renameButton.setOnAction(e -> {
            String selectedCategory = categoryListView.getSelectionModel().getSelectedItem();
            if (selectedCategory != null && !selectedCategory.equals("General")) {
                TextInputDialog renameDialog = new TextInputDialog(selectedCategory);
                renameDialog.setTitle("Rename Category");
                renameDialog.setHeaderText("Enter a new name for the category:");
                renameDialog.setContentText("New Name:");
        
                renameDialog.showAndWait().ifPresent(newName -> {
                    if (!newName.trim().isEmpty() && !categories.contains(newName)) {
                        int index = categories.indexOf(selectedCategory);
                        categories.set(index, newName);
                        saveCategories();
        
                        // Update tasks in the JSON file
                        updateTasksWithRenamedCategory(selectedCategory, newName);
        
                        categoryListView.setItems(FXCollections.observableArrayList(categories));
                        refreshTaskTable(); 
                        System.out.println("Category '" + selectedCategory + "' renamed to '" + newName + "' and tasks updated.");
                    } else {
                        showAlert("Rename Error", "Invalid or duplicate category name.");
                    }
                });
            } else {
                showAlert("Rename Error", "This category cannot be renamed.");
            }
        });
        
        deleteButton.setOnAction(e -> {
            String selectedCategory = categoryListView.getSelectionModel().getSelectedItem();
            
            if (selectedCategory != null && !selectedCategory.equals("General")) {
                // Confirm deletion
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Category");
                confirm.setHeaderText("Are you sure you want to delete the category: " + selectedCategory + "?");
                confirm.setContentText("All tasks under this category will be deleted.");

                confirm.showAndWait().ifPresent(response -> {
                    if (response == ButtonType.OK) {
                        System.out.println("🗑 Deleting category: " + selectedCategory);
                        
                        removeTasksWithCategory(selectedCategory, taskTable);  

                        categories.remove(selectedCategory);
                        saveCategories();

                        categoryListView.setItems(FXCollections.observableArrayList(categories));
                        refreshTaskTable(); 
                        System.out.println("✅ Category and associated tasks successfully deleted.");
                    }
                });

            } else {
                showAlert("Delete Error", "The 'General' category cannot be deleted.");
            }
        });

        okButton.setOnAction(e -> {
            System.out.println("OK button clicked: Closing category manager and refreshing task table.");
            refreshTaskTable();
            manageDialog.close();
        });
        

        HBox buttonBox = new HBox(10, addButton, renameButton, deleteButton, okButton);
        VBox layout = new VBox(10, categoryListView, buttonBox);
        manageDialog.setScene(new Scene(layout, 300, 300));
        manageDialog.showAndWait();
    }

    private static void updateTasksWithRenamedCategory(String oldCategory, String newCategory) {
        System.out.println("Renaming tasks from category '" + oldCategory + "' to '" + newCategory + "'...");
 
        List<Task> taskList = JSONHandler.readTasks();
    
        boolean updated = false;
        for (Task task : taskList) {
            if (task.getCategory().equalsIgnoreCase(oldCategory)) {
                task.setCategory(newCategory);
                updated = true;
            }
        }

        if (updated) {
            JSONHandler.writeTasks(FXCollections.observableArrayList(taskList));
            System.out.println(" Tasks updated with new category name.");
        } else {
            System.out.println("ℹ No tasks found with category '" + oldCategory + "'.");
        }
    }
    

    private static void removeTasksWithCategory(String deletedCategory, TableView<Task> taskTable) { 
        System.out.println("🗑 Removing tasks with category: " + deletedCategory);
        System.out.println(" Tasks before deletion: ");
        taskTable.getItems().forEach(task -> System.out.println("  - " + task.getTitle() + " | Category: " + task.getCategory()));
        List<Task> taskList = JSONHandler.readTasks();
        List<Task> updatedTasks = new ArrayList<>(taskList);
        updatedTasks.removeIf(task -> task.getCategory().equalsIgnoreCase(deletedCategory));
        JSONHandler.writeTasks(FXCollections.observableArrayList(updatedTasks));
        System.out.println("Tasks under category '" + deletedCategory + "' have been deleted.");
    }

    private static void refreshTaskTable() {  
        if (taskTableRef == null) {
            System.out.println("Task table reference is null! UI will not update.");
            return;
        }
    
        System.out.println("Refreshing task table in UI...");
    
        List<Task> latestTasks = JSONHandler.readTasks();
        ObservableList<Task> latestTaskList = FXCollections.observableArrayList(latestTasks);
    
        taskTableRef.setItems(latestTaskList); 
        taskTableRef.refresh();
    
        System.out.println("UI Task table updated.");
    }
    

    private static String showNewCategoryDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Category");
        dialog.setHeaderText("Enter a new category:");
        dialog.setContentText("Category:");
        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
