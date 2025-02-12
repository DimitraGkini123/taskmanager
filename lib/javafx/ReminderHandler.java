package lib.javafx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lib.json.JSONHandler;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReminderHandler {


   public static void showSetReminderDialog(Task task) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Set Reminder for " + task.getTitle());
    
        // User selects reminder type
        ChoiceBox<String> reminderTypeBox = new ChoiceBox<>();
        reminderTypeBox.getItems().addAll(
                "One day before deadline",
                "One week before deadline",
                "One month before deadline",
                "Custom date"
        );
        reminderTypeBox.setValue("One day before deadline");
    
        // Custom input fields for date and time
        TextField dateField = new TextField("dd/MM/yyyy");
        TextField timeField = new TextField("HH:mm");
        dateField.setDisable(true); // Disabled by default unless "Custom date" is selected
        timeField.setDisable(false);
    
        reminderTypeBox.setOnAction(e -> {
            if (reminderTypeBox.getValue().equals("Custom date")) {
                dateField.setDisable(false);
            } else {
                dateField.setDisable(true);
            }
        });
    
        TextField messageField = new TextField();
    
        Button saveButton = new Button("Save Reminder");
        saveButton.setOnAction(e -> {
            try {
                // Determine reminder date based on selection
                String reminderDate;
                DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                LocalDate dueDate = LocalDate.parse(task.getDueDate(), dateFormatter);
    
                switch (reminderTypeBox.getValue()) {
                    case "One day before deadline":
                        reminderDate = dueDate.minusDays(1).format(dateFormatter);
                        break;
                    case "One week before deadline":
                        reminderDate = dueDate.minusWeeks(1).format(dateFormatter);
                        break;
                    case "One month before deadline":
                        reminderDate = dueDate.minusMonths(1).format(dateFormatter);
                        break;
                    case "Custom date":
                        reminderDate = dateField.getText();
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid reminder type selected");
                }
    
                // ✅ Validate time format
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
                String reminderTime = LocalTime.parse(timeField.getText(), timeFormatter).format(timeFormatter);
    
                // ✅ Create new reminder object
                Reminder newReminder = new Reminder(task.getTitle(), reminderDate, reminderTime, messageField.getText(), reminderTypeBox.getValue());
    
                // ✅ Read existing reminders and update them
                List<Reminder> reminders = JSONHandler.readReminders();
                if (!reminders.contains(newReminder)) {
                    reminders.add(newReminder);
                    JSONHandler.writeReminders(reminders);
                    System.out.println("Reminder set for task: " + task.getTitle() + " on " + reminderDate + " at " + reminderTime);
                } else {
                    System.out.println("Duplicate reminder ignored: " + newReminder);
                }
    
                dialog.close();
            } catch (Exception ex) {
                showAlert("Invalid Input", "Please enter a valid date and time.");
            }
        });
    
        // Layout for the UI
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("Reminder Type:"), 0, 0);
        grid.add(reminderTypeBox, 1, 0);
        grid.add(new Label("Custom Date (dd/MM/yyyy):"), 0, 1);
        grid.add(dateField, 1, 1);
        grid.add(new Label("Time (HH:mm):"), 0, 2);
        grid.add(timeField, 1, 2);
        grid.add(new Label("Message:"), 0, 3);
        grid.add(messageField, 1, 3);
        grid.add(saveButton, 1, 4);
    
        dialog.setScene(new Scene(grid, 350, 250));
        dialog.showAndWait();
    }
    


    public static void showRemindersDialog() {
        Stage reminderStage = new Stage();
        reminderStage.initModality(Modality.APPLICATION_MODAL);
        reminderStage.setTitle("Manage Reminders");
    
        TableView<Reminder> reminderTable = new TableView<>();
        ObservableList<Reminder> reminders = FXCollections.observableArrayList(JSONHandler.readReminders());
    
        TableColumn<Reminder, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("date"));
    
        TableColumn<Reminder, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
    
        TableColumn<Reminder, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(new PropertyValueFactory<>("message"));
    
        TableColumn<Reminder, String> taskCol = new TableColumn<>("Task Title");
        taskCol.setCellValueFactory(new PropertyValueFactory<>("taskTitle"));
    
        reminderTable.getColumns().addAll(dateCol, timeCol, messageCol, taskCol);
        reminderTable.setItems(reminders);
    
        Button modifyButton = new Button("Modify");
        modifyButton.setOnAction(e -> {
            Reminder selectedReminder = reminderTable.getSelectionModel().getSelectedItem();
            if (selectedReminder != null) {
                showModifyReminderDialog(selectedReminder, reminders, reminderTable);
            } else {
                showAlert("No Reminder Selected", "Please select a reminder to modify.");
            }
        });
    
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(e -> {
            Reminder selectedReminder = reminderTable.getSelectionModel().getSelectedItem();
        
            if (selectedReminder != null) {
                System.out.println("🗑 Selected Reminder to delete: " + selectedReminder);
        
                boolean removed = reminders.remove(selectedReminder);
                System.out.println("Removal status: " + removed); // ✅ Check if removal was successful
        
                List<Reminder> updatedReminders = reminders.stream().collect(Collectors.toList());
                System.out.println("Updated Reminders List Before Writing: " + updatedReminders);
        
                JSONHandler.writeReminders(updatedReminders);
        
                // ✅ Reload the reminders from JSON to check if deletion actually happened
                List<Reminder> reloadedReminders = JSONHandler.readReminders();
                System.out.println("📂 Reminders After Writing to JSON: " + reloadedReminders);
        
                reminderTable.refresh(); // ✅ Refresh UI to reflect changes
        
                if (!reloadedReminders.contains(selectedReminder)) {
                    System.out.println("✅ Reminder successfully removed from JSON!");
                } else {
                    System.out.println("⚠️ Reminder still exists in JSON! Check writeReminders() method.");
                }
            } else {
                showAlert("No Reminder Selected", "Please select a reminder to delete.");
            }
        });
        

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> reminderStage.close());
    
        HBox buttonBox = new HBox(10, modifyButton, deleteButton, closeButton);
        buttonBox.setStyle("-fx-padding: 10; -fx-alignment: center;");
    
        VBox layout = new VBox(10, reminderTable, buttonBox);
        layout.setStyle("-fx-padding: 10;");
    
        reminderStage.setScene(new Scene(layout, 500, 400));
        reminderStage.showAndWait();
    }
    
    private static void showModifyReminderDialog(Reminder reminder, ObservableList<Reminder> reminders, TableView<Reminder> table) {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Modify Reminder");
    
        TextField dateField = new TextField(reminder.getDate());
        TextField timeField = new TextField(reminder.getTime());
        TextField messageField = new TextField(reminder.getMessage());
    
        Button saveButton = new Button("Save Changes");
        saveButton.setOnAction(e -> {
            reminder.setDate(dateField.getText());
            reminder.setTime(timeField.getText());
            reminder.setMessage(messageField.getText());
    
            JSONHandler.writeReminders(new ArrayList<>(reminders));
            table.refresh();
            dialog.close();
        });
    
        GridPane grid = new GridPane();
        grid.setVgap(10);
        grid.setHgap(10);
        grid.add(new Label("Date:"), 0, 0);
        grid.add(dateField, 1, 0);
        grid.add(new Label("Time:"), 0, 1);
        grid.add(timeField, 1, 1);
        grid.add(new Label("Message:"), 0, 2);
        grid.add(messageField, 1, 2);
        grid.add(saveButton, 1, 3);
    
        dialog.setScene(new Scene(grid, 300, 200));
        dialog.showAndWait();
    }
    
    private static void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public static void deleteRemindersForTask(String taskTitle) {
        System.out.println("🛑 Deleting all reminders for task: " + taskTitle);

        List<Reminder> reminders = JSONHandler.readReminders();
        List<Reminder> updatedReminders = reminders.stream()
                .filter(r -> !r.getTaskTitle().equalsIgnoreCase(taskTitle))  // Keep only reminders NOT related to this task
                .toList();

        JSONHandler.writeReminders(updatedReminders);

        System.out.println("✅ Deleted all reminders for completed task: " + taskTitle);
    }

    public static void updateReminderForTask(String taskTitle, String newDueDate) {
        List<Reminder> reminders = JSONHandler.readReminders(); // Load existing reminders
    
        for (Reminder reminder : reminders) {
            if (reminder.getTaskTitle().equals(taskTitle)) {
                // Skip update if it's a custom date (we assume custom reminders have a flag)
                if (reminder.isCustomDate()) {
                    System.out.println("🔹 Skipping reminder update for task: " + taskTitle + " (Custom Reminder)");
                    continue;
                }
    
                // Update reminder date based on user preference
                String newReminderDate;
                switch (reminder.getReminderType()) {
                    case "One day before deadline":
                        newReminderDate = subtractDays(newDueDate, 1);
                        break;
                    case "One week before deadline":
                        newReminderDate = subtractDays(newDueDate, 7);
                        break;
                    case "One month before deadline":
                        newReminderDate = subtractDays(newDueDate, 30);
                        break;
                    case "Custom date":
                        // If it's a custom date, we **don't change** the reminder date
                        newReminderDate = reminder.getDate();
                        break;
                    default:
                        System.err.println(" Unexpected reminder type: " + reminder.getReminderType());
                        newReminderDate = reminder.getDate(); // Fallback to existing date
                        break;
                }
    
                reminder.setDate(newReminderDate); // ✅ Update the reminder date
                System.out.println("✅ Reminder updated for task: " + taskTitle + " New Reminder Date: " + newReminderDate);
            }
        }
    
        JSONHandler.writeReminders(reminders); // Save updated reminders
    }
    private static String subtractDays(String dateStr, int days) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
        try {
            // Convert String to LocalDate for calculation
            LocalDate date = LocalDate.parse(dateStr, dateFormatter);
            // Subtract the required days
            LocalDate newDate = date.minusDays(days);
            // Convert back to String and return
            return newDate.format(dateFormatter);
        } catch (DateTimeParseException e) {
            System.err.println("⚠️ Invalid date format: " + dateStr);
            return dateStr; // Return original if parsing fails
        }
    }
    
    
    
}
