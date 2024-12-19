package lib.javafx;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Modality;
import lib.json.JSONHandler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class TaskManager extends Application {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy");

    @Override
    public void start(Stage stage) {
        // Load tasks from JSON file using JSONHandler
        List<Task> tasks = JSONHandler.readTasks();
        ObservableList<Task> taskList = FXCollections.observableArrayList(tasks);

        // Automatically update statuses of tasks based on due dates
        updateTaskStatuses(taskList);

        // Create TableView to display tasks
        TableView<Task> tableView = new TableView<>(taskList);

        // Define columns for the TableView
        TableColumn<Task, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));

        TableColumn<Task, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn<Task, String> categoryCol = new TableColumn<>("Category");
        categoryCol.setCellValueFactory(new PropertyValueFactory<>("category"));

        TableColumn<Task, String> priorityCol = new TableColumn<>("Priority");
        priorityCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPriority()));

        TableColumn<Task, String> dueDateCol = new TableColumn<>("Due Date");
        dueDateCol.setCellValueFactory(new PropertyValueFactory<>("dueDate"));

        TableColumn<Task, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        // Add columns to TableView
        tableView.getColumns().add(titleCol);
        tableView.getColumns().add(descCol);
        tableView.getColumns().add(categoryCol);
        tableView.getColumns().add(priorityCol);
        tableView.getColumns().add(dueDateCol);
        tableView.getColumns().add(statusCol);

        // Create a "+" button to add a new task
        Button addButton = new Button("+");
        addButton.setStyle("-fx-font-size: 20px; -fx-base: #4CAF50; -fx-text-fill: white;");
        addButton.setOnAction(e -> {
            showAddTaskDialog(taskList);
            updateTaskStatuses(taskList); // Recheck statuses after adding
            tableView.refresh();
        });

        // Create an "Edit" button to edit a selected task
        Button editButton = new Button("Edit");
        editButton.setStyle("-fx-font-size: 14px;");
        editButton.setOnAction(e -> {
            Task selectedTask = tableView.getSelectionModel().getSelectedItem();
            if (selectedTask != null) {
                showEditTaskDialog(selectedTask, taskList);
                updateTaskStatuses(taskList); // Recheck statuses after editing
                tableView.refresh();
                JSONHandler.writeTasks(taskList); // Save updated tasks to JSON
            } else {
                showAlert("No Task Selected", "Please select a task to edit.");
            }
        });

        // Define a Delete button column
        TableColumn<Task, Void> deleteCol = new TableColumn<>("Delete");

        deleteCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteButton = new Button("🗑");

            {
                deleteButton.setStyle("-fx-background-color: red; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 3 5 3 5;");
                deleteButton.setOnAction(e -> {
                    Task selectedTask = getTableView().getItems().get(getIndex());
                    showDeleteConfirmation(selectedTask, taskList);
                    tableView.refresh(); // Refresh the table view after deletion
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        // Add the delete column to the TableView
        tableView.getColumns().add(deleteCol);

        // Create a horizontal box for the buttons
        HBox buttonBox = new HBox(10, addButton, editButton);
        buttonBox.setStyle("-fx-padding: 10; -fx-alignment: center;");

        // Create a vertical layout to combine the buttons and the table
        VBox vbox = new VBox(10, buttonBox, tableView);
        vbox.setStyle("-fx-padding: 10;");

        // Create the scene
        Scene scene = new Scene(vbox, 800, 600);

        // Set up the stage (window)
        stage.setTitle("Task Manager");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Updates the status of tasks based on their due dates.
     */
    private void updateTaskStatuses(ObservableList<Task> taskList) {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Matches JSON date format

        for (Task task : taskList) {
            if (!"Completed".equalsIgnoreCase(task.getStatus())) {
                try {
                    LocalDate taskDueDate = LocalDate.parse(task.getDueDate(), formatter);

                    if (taskDueDate.isBefore(today)) {
                        task.setStatus("Delayed");
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Invalid date format for task: " + task.getTitle() + " - " + task.getDueDate());
                }
            }
        }

        JSONHandler.writeTasks(taskList);
    }

    private void showAddTaskDialog(ObservableList<Task> taskList) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Add New Task");

        TextField titleField = new TextField();
        TextField descriptionField = new TextField();
        TextField categoryField = new TextField();
        TextField priorityField = new TextField(); // e.g., High, Low
        TextField dueDateField = new TextField();
        ChoiceBox<String> statusChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList("Open", "In Progress", "Postponed", "Completed", "Delayed"));
        statusChoiceBox.setValue("Open"); // Default status

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        gridPane.add(new Label("Title:"), 0, 0);
        gridPane.add(titleField, 1, 0);
        gridPane.add(new Label("Description:"), 0, 1);
        gridPane.add(descriptionField, 1, 1);
        gridPane.add(new Label("Category:"), 0, 2);
        gridPane.add(categoryField, 1, 2);
        gridPane.add(new Label("Priority (High/Low):"), 0, 3);
        gridPane.add(priorityField, 1, 3);
        gridPane.add(new Label("Due Date (dd/MM/yy):"), 0, 4);
        gridPane.add(dueDateField, 1, 4);
        gridPane.add(new Label("Status:"), 0, 5);
        gridPane.add(statusChoiceBox, 1, 5);

        Button saveButton = new Button("Save Task");
        saveButton.setOnAction(e -> {
            Task newTask = new Task(
                    titleField.getText(),
                    descriptionField.getText(),
                    categoryField.getText(),
                    priorityField.getText(),
                    dueDateField.getText(),
                    statusChoiceBox.getValue()
            );
            taskList.add(newTask);
            JSONHandler.writeTasks(taskList);
            dialog.close();
        });

        gridPane.add(saveButton, 1, 6);

        Scene dialogScene = new Scene(gridPane, 300, 300);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showEditTaskDialog(Task task, ObservableList<Task> taskList) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Task");

        TextField titleField = new TextField(task.getTitle());
        TextField descriptionField = new TextField(task.getDescription());
        TextField categoryField = new TextField(task.getCategory());
        TextField priorityField = new TextField(task.getPriority());
        TextField dueDateField = new TextField(task.getDueDate());
        ChoiceBox<String> statusChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList("Open", "In Progress", "Postponed", "Completed", "Delayed"));
        statusChoiceBox.setValue(task.getStatus());

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        gridPane.add(new Label("Title:"), 0, 0);
        gridPane.add(titleField, 1, 0);
        gridPane.add(new Label("Description:"), 0, 1);
        gridPane.add(descriptionField, 1, 1);
        gridPane.add(new Label("Category:"), 0, 2);
        gridPane.add(categoryField, 1, 2);
        gridPane.add(new Label("Priority (High/Low):"), 0, 3);
        gridPane.add(priorityField, 1, 3);
        gridPane.add(new Label("Due Date (dd/MM/yy):"), 0, 4);
        gridPane.add(dueDateField, 1, 4);
        gridPane.add(new Label("Status:"), 0, 5);
        gridPane.add(statusChoiceBox, 1, 5);

        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> {
            task.setTitle(titleField.getText());
            task.setDescription(descriptionField.getText());
            task.setCategory(categoryField.getText());
            task.setPriority(priorityField.getText());
            task.setDueDate(dueDateField.getText());
            task.setStatus(statusChoiceBox.getValue());
            dialog.close();
        });

        gridPane.add(saveButton, 1, 6);

        Scene dialogScene = new Scene(gridPane, 300, 300);
        dialog.setScene(dialogScene);
        dialog.showAndWait();
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showDeleteConfirmation(Task task, ObservableList<Task> taskList) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete the task \"" + task.getTitle() + "\"?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                taskList.remove(task);
                JSONHandler.writeTasks(taskList);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
