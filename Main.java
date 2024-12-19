import java.util.ArrayList;
import java.util.List;
import lib.javafx.Task; // Import your Task class
import lib.json.JSONHandler; // Import your JSONHandler class

public class Main {
    public static void main(String[] args) {
        // Create a list of tasks
        List<Task> tasks = new ArrayList<>();
        
        // Set priority as a string: "High", "Medium", "Low"
        Task task1 = new Task("Title", "Description", "Category", "High", "14/12/24", "Open");
        
        tasks.add(task1); // Add the task to the list

        // Save tasks to JSON
        JSONHandler.writeTasks(tasks);
        System.out.println("Tasks saved to JSON file.");

        // Read tasks from JSON
        List<Task> readTasks = JSONHandler.readTasks();
        System.out.println("Tasks read from JSON:");

        // Change variable name in loop to avoid duplication
        for (Task t : readTasks) { 
            // Print task details
            System.out.println(t.getTitle() + ": " + t.getDescription() + ", Priority: " + t.getPriority());
        }
    }
}
