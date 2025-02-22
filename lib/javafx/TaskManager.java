package lib.javafx;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.Modality;
import lib.json.JSONHandler;
import javafx.util.Duration;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lib.javafx.CategoryHandler;


public class TaskManager extends Application {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yy");
    private TableView<Task> tableView = new TableView<>();

    @Override
    public void start(Stage primaryStage) {
        showMedialabAssistant(primaryStage); // Open Medialab Assistant First
    }
    private Label totalLabel;
    private Label completedLabel;
    private Label delayedLabel;
    private Label upcomingLabel;

private void showMedialabAssistant(Stage primaryStage) {
    Stage assistantStage = new Stage();

    assistantStage.setTitle("Medialab Assistant");

    // ✅ Create labels for statistics (dynamic updating)
    Label titleLabel = new Label("Task Summary");
    titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

    totalLabel = new Label();
    completedLabel = new Label();
    delayedLabel = new Label();
    upcomingLabel = new Label();

    updateTaskStatistics();

    VBox statsBox = new VBox(10, titleLabel, totalLabel, completedLabel, delayedLabel, upcomingLabel);
    statsBox.setStyle("-fx-padding: 15px; -fx-alignment: center; -fx-background-color: #f0f0f0;");
    statsBox.setMinHeight(180); 

    // ✅ Buttons for actions
    Label welcomeLabel = new Label("Welcome to Medialab Assistant");
    welcomeLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

    Button manageTasksButton = new Button("Manage Tasks");
    manageTasksButton.setMinWidth(250);
    manageTasksButton.setOnAction(e -> {
        showTaskManager(primaryStage);
        updateTaskStatistics(); // ✅ Refresh statistics
    });

    Button manageCategoriesButton = new Button("See Categories");
    manageCategoriesButton.setMinWidth(250);
    manageCategoriesButton.setOnAction(e -> {
        CategoryHandler.showManageCategoriesDialog(tableView);
        updateTaskStatistics();
    });

    Button managePrioritiesButton = new Button("See Priorities");
    managePrioritiesButton.setMinWidth(250);
    managePrioritiesButton.setOnAction(e -> {
        PriorityHandler.showManagePrioritiesDialog(tableView);
        updateTaskStatistics();
    });

    Button seeRemindersButton = new Button("See Reminders");
    seeRemindersButton.setMinWidth(250);
    seeRemindersButton.setOnAction(e -> {
        ReminderHandler.showRemindersDialog();
        updateTaskStatistics();
    });

    VBox buttonBox = new VBox(10, manageTasksButton, manageCategoriesButton, managePrioritiesButton, seeRemindersButton);
    buttonBox.setStyle("-fx-padding: 15px; -fx-alignment: center;");
    buttonBox.setMinHeight(200);

    // ✅ Main layout
    VBox mainLayout = new VBox(20, statsBox, buttonBox);
    mainLayout.setStyle("-fx-padding: 20px; -fx-alignment: center;");

    Scene scene = new Scene(mainLayout, 400, 400);
    assistantStage.setScene(scene);
    assistantStage.setOnShown(e -> {
        List<Task> tasks = JSONHandler.readTasks();
        ObservableList<Task> taskList = FXCollections.observableArrayList(tasks);
        showDelayedTasksPopup(assistantStage, taskList);
    });
    assistantStage.show();
}

    
private void showDelayedTasksPopup(Stage owner, ObservableList<Task> taskList) {
    List<Task> delayedTasks = new ArrayList<>();

    for (Task task : taskList) {
        if ("Delayed".equalsIgnoreCase(task.getStatus())) {
            delayedTasks.add(task);
        }
    }

    if (!delayedTasks.isEmpty()) {
        StringBuilder message = new StringBuilder("The following tasks are delayed:\n\n");

        for (Task task : delayedTasks) {
            message.append("• ").append(task.getTitle()).append(" (Due: ").append(task.getDueDate()).append(")\n");
        }

        showAlert(owner, "Delayed Tasks Alert", message.toString());
    }
}
private void updateTaskStatistics() {
    List<Task> tasks = JSONHandler.readTasks(); // ✅ Reload latest tasks
    long totalTasks = tasks.size();
    long completedTasks = tasks.stream().filter(task -> "Completed".equalsIgnoreCase(task.getStatus())).count();
    long delayedTasks = tasks.stream().filter(task -> "Delayed".equalsIgnoreCase(task.getStatus())).count();
    long upcomingTasks = tasks.stream().filter(task -> isTaskDueWithin7Days(task.getDueDate())).count();

    totalLabel.setText("Total Tasks: " + totalTasks);
    completedLabel.setText("Completed Tasks: " + completedTasks);
    delayedLabel.setText("Delayed Tasks: " + delayedTasks);
    upcomingLabel.setText(" Tasks Due in 7 Days: " + upcomingTasks);
}

    private boolean isTaskDueWithin7Days(String dueDate) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            LocalDate taskDate = LocalDate.parse(dueDate, formatter);
            LocalDate today = LocalDate.now();
            return !taskDate.isBefore(today) && !taskDate.isAfter(today.plusDays(7));
        } catch (DateTimeParseException e) {
            System.err.println("⚠ Invalid date format for task: " + dueDate);
            return false;
        }
    }
        
        //Timeline reminderChecker = new Timeline(new KeyFrame(Duration.minutes(10), event -> checkReminders()));
        //reminderChecker.setCycleCount(Animation.INDEFINITE);
       // reminderChecker.play();

    private void showTaskManager(Stage stage) {
        // Load tasks from JSON file using JSONHandler
        List<Task> tasks = JSONHandler.readTasks();
        ObservableList<Task> taskList = FXCollections.observableArrayList(tasks);
        tasks.sort(Comparator.comparing(Task::getCategory));
        updateTaskStatuses(taskList);
        tableView.setItems(taskList);



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
        tableView.getColumns().addAll(titleCol, descCol, categoryCol, priorityCol, dueDateCol, statusCol);
        tableView.getSortOrder().add(categoryCol); // Ensure sorting by category

        
    // Search Bar UI
TextField titleSearchField = new TextField();
titleSearchField.setPromptText("Search by Title");

// Load categories and priorities, adding an "All" option
ObservableList<String> categories = FXCollections.observableArrayList("All");
categories.addAll(CategoryHandler.getCategories());

ObservableList<String> priorities = FXCollections.observableArrayList("All");
priorities.addAll(PriorityHandler.getPriorities());

// Create category filter dropdown
ComboBox<String> categoryFilter = new ComboBox<>(categories);
categoryFilter.setValue("All"); // Default selection

// Create priority filter dropdown
ComboBox<String> priorityFilter = new ComboBox<>(priorities);
priorityFilter.setValue("All"); // Default selection

// Search button with filter logic
Button searchButton = new Button("Search");
searchButton.setOnAction(e -> applyFilters(
    taskList,
    titleSearchField.getText(),
    categoryFilter.getValue(),
    priorityFilter.getValue()
));

// Layout for search bar
HBox searchBox = new HBox(10, titleSearchField, categoryFilter, priorityFilter, searchButton);
searchBox.setStyle("-fx-padding: 10; -fx-alignment: center;");

        
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
            
            if (selectedTask == null) {
                showAlert(tableView.getScene().getWindow(), "No Task Selected", "Please select a task to edit.");
                return;
            }
        
            String selectedTaskTitle = selectedTask.getTitle(); // Store title before refreshing
        
            List<Task> latestTasks = JSONHandler.readTasks();
            ObservableList<Task> latestTaskList = FXCollections.observableArrayList(latestTasks);
            
            tableView.setItems(latestTaskList);
 
            Task updatedTask = latestTaskList.stream()
                .filter(task -> task.getTitle().equals(selectedTaskTitle))
                .findFirst()
                .orElse(null);
        
            if (updatedTask != null) {
                tableView.getSelectionModel().select(updatedTask); // Reselect task in the table
                showEditTaskDialog(updatedTask, latestTaskList); // Open edit dialog with latest task data
                updateTaskStatuses(latestTaskList); // Recheck statuses after editing
                tableView.refresh();
                JSONHandler.writeTasks(latestTaskList); // Save updated tasks to JSON
            } else {
                showAlert(tableView.getScene().getWindow(), "Error", "Selected task not found after refreshing.");
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


        Button setReminderButton = new Button("Set Reminder");
        setReminderButton.setOnAction(e -> {
        Task selectedTask = tableView.getSelectionModel().getSelectedItem();
        if (selectedTask == null) {
        showAlert(tableView.getScene().getWindow(), "No Task Selected", "Please select a task to set a reminder.");
        return;
        }
        if (selectedTask.getStatus().equalsIgnoreCase("Completed")) {
        showAlert(tableView.getScene().getWindow(), "Action Denied", "You cannot set reminders for completed tasks.");
        return;
        }
        ReminderHandler.showSetReminderDialog(selectedTask);
        });


        // Create a horizontal box for the buttons
        HBox buttonBox = new HBox(10, addButton, editButton, setReminderButton );
        buttonBox.setStyle("-fx-padding: 10; -fx-alignment: center;");

     // Main Layout (Search Bar + Buttons + Task Table)
     VBox vbox = new VBox(10, searchBox, buttonBox, tableView);
     vbox.setStyle("-fx-padding: 10;");
        // Create the scene
        Scene scene = new Scene(vbox, 800, 600);

        // Set up the stage (window)
        stage.setTitle("Task Manager");
        stage.setScene(scene);
        stage.show();
    }
    private void applyFilters(ObservableList<Task> taskList, String title, String category, String priority) {
        ObservableList<Task> filteredTasks = FXCollections.observableArrayList(
            taskList.stream()
                .filter(task -> (title == null || title.isEmpty() || task.getTitle().toLowerCase().contains(title.toLowerCase())))
                .filter(task -> (category == null || category.equals("All") || task.getCategory().equals(category)))
                .filter(task -> (priority == null || priority.equals("All") || task.getPriority().equals(priority)))
                .toList()
        );
    
        tableView.setItems(filteredTasks);
        tableView.refresh();
    }

    private boolean isValidDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
        try {
            LocalDate parsedDate = LocalDate.parse(date, formatter);
            return !parsedDate.isBefore(LocalDate.now()); // ✅ Ensure date is **not in the past**
        } catch (DateTimeParseException e) {
            return false; // ❌ Invalid format
        }
    }
    

private void updateTaskStatuses(ObservableList<Task> taskList) {
    LocalDate today = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Matches JSON date format

    for (Task task : taskList) {
        if (!"Completed".equalsIgnoreCase(task.getStatus())) {
            try {
                LocalDate taskDueDate = LocalDate.parse(task.getDueDate(), formatter);

                if ("Delayed".equalsIgnoreCase(task.getStatus()) && taskDueDate.isAfter(today)) {
                    task.setStatus("Open");
                }
                // If the due date is in the past, mark it as delayed
                else if (taskDueDate.isBefore(today)) {
                    task.setStatus("Delayed");
                }
            } catch (DateTimeParseException e) {
                System.err.println("Invalid date format for task: " + task.getTitle() + " - " + task.getDueDate());
            }
        }
    }

    JSONHandler.writeTasks(taskList);
}


private ObservableList<String> categories = FXCollections.observableArrayList("General");
private ComboBox<String> createCategorySelectionBox() {
    ComboBox<String> categoryComboBox = new ComboBox<>(CategoryHandler.getCategories());
    categoryComboBox.setValue("General"); // Default category
    return categoryComboBox;
}
private ComboBox<String> createPrioritySelectionBox() {
    ComboBox<String> priorityComboBox = new ComboBox<>(PriorityHandler.getPriorities());
    priorityComboBox.setValue("Default"); // Default category
    return priorityComboBox;
}


private ComboBox<String> createCategoryComboBox() {
    ComboBox<String> categoryComboBox = new ComboBox<>(CategoryHandler.getCategories());
    categoryComboBox.setValue("General");

    Button manageCategoriesButton = new Button("Manage Categories");
    manageCategoriesButton.setOnAction(e -> {
        CategoryHandler.showManageCategoriesDialog(tableView);
        categoryComboBox.setItems(FXCollections.observableArrayList(CategoryHandler.getCategories()));
    });

    return categoryComboBox;
}


private void showAddTaskDialog(ObservableList<Task> taskList) {
    Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.setTitle("Add New Task");

    TextField titleField = new TextField();
    TextField descriptionField = new TextField();
    ComboBox<String> categoryComboBox = createCategorySelectionBox();
    TextField dueDateField = new TextField();
    ComboBox<String> priorityComboBox = createPrioritySelectionBox();
    ChoiceBox<String> statusChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList("Open", "In Progress", "Postponed", "Completed"));
    statusChoiceBox.setValue("Open");

    GridPane gridPane = new GridPane();
    gridPane.setVgap(10);
    gridPane.setHgap(10);
    gridPane.add(new Label("Title:"), 0, 0);
    gridPane.add(titleField, 1, 0);
    gridPane.add(new Label("Description:"), 0, 1);
    gridPane.add(descriptionField, 1, 1);
    gridPane.add(new Label("Category:"), 0, 2);
    gridPane.add(categoryComboBox, 1, 2);        
    gridPane.add(new Label("Priority:"), 0, 3);
    gridPane.add(priorityComboBox, 1, 3);
    gridPane.add(new Label("Due Date (dd/MM/yyyy):"), 0, 4);
    gridPane.add(dueDateField, 1, 4);
    gridPane.add(new Label("Status:"), 0, 5);
    gridPane.add(statusChoiceBox, 1, 5);

    Button saveButton = new Button("Save Task");
    saveButton.setOnAction(e -> {
        String dueDate = dueDateField.getText();
        String Title = titleField.getText().trim();

        if (Title.isEmpty()) {
            showAlert(dialog, "Invalid Input", "Task title cannot be empty.");
            return;
        }
    
    
        if (!isValidDate(dueDate)) {
            showAlert(dialog, "Invalid Date", "Please enter a valid due date in the format dd/MM/yyyy.");
            return; 
        }
        boolean titleExists = taskList.stream().anyMatch(task -> task.getTitle().equalsIgnoreCase(Title));
        if (titleExists) {
            showAlert(dialog, "Duplicate Task Title", "A task with this title already exists. Please choose a different title.");
            return;
        }
        Task newTask = new Task(
            titleField.getText(),
            descriptionField.getText(),
            categoryComboBox.getValue(),
            priorityComboBox.getValue(),
            dueDateField.getText(),
            statusChoiceBox.getValue()
        );
    
        taskList.add(newTask);
        taskList.sort(Comparator.comparing(Task::getCategory));
        JSONHandler.writeTasks(taskList);

        updateTaskStatistics();
    
        ObservableList<Task> updatedTaskList = FXCollections.observableArrayList(JSONHandler.readTasks());
        tableView.setItems(updatedTaskList);
        tableView.refresh();
        dialog.close();
    });
    

    Button closeButton = new Button("close");
    closeButton.setOnAction(e -> {
        tableView.refresh(); 
        dialog.close();
    });

    HBox buttonBox = new HBox(10, saveButton, closeButton);
    gridPane.add(buttonBox, 1, 6);

    dialog.setScene(new Scene(gridPane, 400, 400));
    dialog.showAndWait();
}

    private void showEditTaskDialog(Task task, ObservableList<Task> taskList) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Edit Task");
        String originalTitle = task.getTitle();

        TextField titleField = new TextField(task.getTitle());
        TextField descriptionField = new TextField(task.getDescription());
        ComboBox<String> categoryComboBox = createCategoryComboBox();
        categoryComboBox.setValue(task.getCategory());
        TextField dueDateField = new TextField(task.getDueDate());
        ComboBox<String> priorityComboBox = createPrioritySelectionBox();
        priorityComboBox.setValue(task.getPriority());
        ChoiceBox<String> statusChoiceBox = new ChoiceBox<>(FXCollections.observableArrayList("Open", "In Progress", "Postponed", "Completed"));
        statusChoiceBox.setValue(task.getStatus());

        GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);
        gridPane.add(new Label("Title:"), 0, 0);
        gridPane.add(titleField, 1, 0);
        gridPane.add(new Label("Description:"), 0, 1);
        gridPane.add(descriptionField, 1, 1);
        gridPane.add(new Label("Category:"), 0, 2);
        gridPane.add(categoryComboBox, 1, 2);
        gridPane.add(new Label("Priority:"), 0, 3);
        gridPane.add(priorityComboBox, 1, 3 );
        gridPane.add(new Label("Due Date (dd/MM/yyyy):"), 0, 4);
        gridPane.add(dueDateField, 1, 4);
        gridPane.add(new Label("Status:"), 0, 5);
        gridPane.add(statusChoiceBox, 1, 5);

        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> {
            String oldTitle = task.getTitle();
            String oldDueDate = task.getDueDate(); 
            String newDueDate = dueDateField.getText();
            String newTitle = titleField.getText().trim();
    

            if (newTitle.isEmpty()) {
                showAlert(dialog, "Invalid Input", "Task title cannot be empty.");
                return;
            }

            if (!isValidDate(newDueDate)) {
                showAlert(dialog, "Invalid Date", "Please enter a valid due date in the format dd/MM/yyyy.");
                return; 
            }
        
            boolean titleExists = taskList.stream()
            .anyMatch(existingTask -> !existingTask.getTitle().equals(oldTitle) &&
                                  existingTask.getTitle().equalsIgnoreCase(newTitle));

            if (titleExists) {
                showAlert(dialog, "Duplicate Task Title", "A task with this title already exists. Please choose a different title.");
                return;
             }

            if (isCustomReminderAfterDueDate(task.getTitle(), newDueDate)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Reminder Warning");
                alert.setHeaderText("Reminder Exists After New Due Date");
                alert.setContentText("A reminder is set after the new due date. Please update your reminders.");
        
                ButtonType updateReminders = new ButtonType("Update Reminders");
                alert.getButtonTypes().setAll(updateReminders);

                DialogPane dialogPane = alert.getDialogPane();
                dialogPane.setMinWidth(400);  // Set minimum width
                dialogPane.setMinHeight(200); // Set minimum height
                dialogPane.setPrefSize(450, 250); // Set preferred size
        
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == updateReminders) {
                    ReminderHandler.showRemindersDialog();
                    return; // Stop saving to let the user update the reminders first
                }
            }
            boolean titleChanged = !originalTitle.equals(newTitle);
            

            task.setTitle(newTitle);
            task.setDescription(descriptionField.getText());
            task.setCategory(categoryComboBox.getValue());
            task.setPriority(priorityComboBox.getValue());
            task.setDueDate(dueDateField.getText());
            task.setStatus(statusChoiceBox.getValue());
        
            if (task.getStatus().equalsIgnoreCase("Completed")) {
                ReminderHandler.deleteRemindersForTask(task.getTitle());
            } else {
                if (!oldDueDate.equals(newDueDate)) {
                    ReminderHandler.updateReminderForTask(originalTitle, newDueDate); // ✅ Use original title
                }
                if (titleChanged) {
                    ReminderHandler.updateTaskTitleInReminders(originalTitle, newTitle); // ✅ Update title in reminders
                }
            }
        
            taskList.sort(Comparator.comparing(Task::getCategory));
            JSONHandler.writeTasks(taskList);
        
            updateTaskStatistics(); // ✅ Refresh task statistics after save
        
            tableView.refresh();
            dialog.close();
        });
        
        gridPane.add(saveButton, 1, 6);
        dialog.setScene(new Scene(gridPane, 400, 400));
        dialog.showAndWait();
    }        

    private boolean isCustomReminderAfterDueDate(String taskTitle, String newDueDate) {
        List<Reminder> reminders = JSONHandler.readReminders();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
        try {
            LocalDate newDueDateParsed = LocalDate.parse(newDueDate, formatter);
    
            for (Reminder reminder : reminders) {
                if (reminder.getTaskTitle().equals(taskTitle) && "Custom date".equalsIgnoreCase(reminder.getReminderType())) {
                    LocalDate reminderDate = LocalDate.parse(reminder.getDate(), formatter);
                    if (reminderDate.isAfter(newDueDateParsed)) {
                        return true; // ✅ Found a "Custom date" reminder after the new due date
                    }
                }
            }
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format: " + newDueDate);
        }
        return false;
    }
    
    

    private void showDeleteConfirmation(Task task, ObservableList<Task> taskList) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Task");
        alert.setHeaderText(null);
        alert.setContentText("Are you sure you want to delete the task \"" + task.getTitle() + "\"?");
    
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                System.out.println("🗑 Attempting to delete task: " + task.getTitle());
    
                // 🔹 Retrieve latest task list from JSON (ensures we get the correct title)
                List<Task> latestTasks = JSONHandler.readTasks();
                
                // 🔹 Find task in JSON by checking for matching title
                Optional<Task> matchingTask = latestTasks.stream()
                    .filter(t -> t.getTitle().equals(task.getTitle()))
                    .findFirst();
    
                if (matchingTask.isEmpty()) {
                    System.out.println("⚠ Task not found in JSON, cannot delete!");
                    showAlert(tableView.getScene().getWindow(), "Error", "Task not found in records.");
                    return;
                }
    
                String actualTaskTitle = matchingTask.get().getTitle(); // Use the correct title
                System.out.println("📌 Task title to delete: " + actualTaskTitle);
    
                // ✅ Delete reminders linked to this task
                ReminderHandler.deleteRemindersForTask(actualTaskTitle);
    
                // ✅ Remove from both in-memory list & JSON list
                boolean removedFromMemory = taskList.removeIf(t -> t.getTitle().equals(actualTaskTitle));
                boolean removedFromJSON = latestTasks.removeIf(t -> t.getTitle().equals(actualTaskTitle));
    
                System.out.println("🔹 Removal status (Memory): " + removedFromMemory);
                System.out.println("🔹 Removal status (JSON): " + removedFromJSON);
    
                if (!removedFromJSON) {
                    System.out.println("Task could not be removed from JSON. Check logic.");
                    return;
                }
    
    
                JSONHandler.writeTasks(latestTasks);
                List<Task> debugTasks = JSONHandler.readTasks();
                System.out.println("✅ JSON File After Writing: " + debugTasks);
    
                ObservableList<Task> updatedTaskList = FXCollections.observableArrayList(debugTasks);
                tableView.setItems(updatedTaskList);
                tableView.refresh();
    
                System.out.println("✅ Task successfully deleted and reminders removed.");
            }
        });
    }
    
    

private void showAlert(Window owner, String title, String message) {
    Alert alert = new Alert(Alert.AlertType.WARNING);
    alert.initOwner(owner); // ✅ Ensures the popup is attached to the main window
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

@Override
public void stop() {
    System.out.println(" Saving data before exit...");

    JSONHandler.writeTasks(tableView.getItems());

    List<Reminder> reminders = JSONHandler.readReminders();
    JSONHandler.writeReminders(reminders);

    CategoryHandler.saveCategories();

    PriorityHandler.savePriorities(); 

    System.out.println("All data (tasks, reminders, categories, priorities) successfully saved before exit.");
}


    

    public static void main(String[] args) {
        launch(args);
    }
}
