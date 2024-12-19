package lib.javafx;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Task {
    private String title;
    private String description;
    private String category;
    private String priority;  // Changed to String to represent High/Low
    private String dueDate;   // Date format "dd/MM/yy"
    private String status;    // Status: "Open", "In Progress", "Postponed", "Completed", "Delayed"

    // Default constructor
    public Task() {
        this.status = "Open";  // Default status is "Open"
    }

    // Constructor with parameters
    public Task(String title, String description, String category, String priority, String dueDate, String status) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.priority = priority;
        this.dueDate = dueDate;
        this.status = (status != null) ? status : "Open";  // Default to "Open"
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    // Method to check and update status if overdue
    public void checkAndUpdateStatus() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy");
        LocalDate today = LocalDate.now();
        LocalDate dueDateObj = LocalDate.parse(dueDate, formatter);

        if (!status.equals("Completed") && dueDateObj.isBefore(today)) {
            status = "Delayed";  // Update status if overdue
        }
    }
}
