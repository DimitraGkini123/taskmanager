package lib.javafx;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true) 
public class Reminder {
    private String taskTitle;
    private String date; // Stored as String in "dd/MM/yyyy" format
    private String message;
    private String reminderType; // Stores: "One day before", "One week before", "One month before", "Custom"

    // Default constructor for Jackson
    public Reminder() {}

    public Reminder(String taskTitle, String date,  String message, String reminderType) {
        this.taskTitle = taskTitle;
        this.date = date;
        this.message = message;
        this.reminderType = reminderType;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReminderType() {
        return reminderType;
    }

    public void setReminderType(String reminderType) {
        this.reminderType = reminderType;
    }

    public boolean isCustomDate() {
        return "Custom".equalsIgnoreCase(reminderType);
    }

    @Override
    public String toString() {
        return "Reminder{" +
                "taskTitle='" + taskTitle + '\'' +
                ", date='" + date + '\'' +
                ", message='" + message + '\'' +
                ", reminderType='" + reminderType + '\'' +
                '}';
    }
}
