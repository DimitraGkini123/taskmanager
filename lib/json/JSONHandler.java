package lib.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lib.javafx.Reminder;
import lib.javafx.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class JSONHandler {

    private static final String FILE_PATH = "medialab/tasks.json"; 
    private static final String REMINDERS_FILE = "medialab/reminders.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<Task> readTasks() {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) return new ArrayList<>();
            return objectMapper.readValue(file, new TypeReference<List<Task>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public static void writeTasks(List<Task> tasks) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), tasks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Reminder> readReminders() {
        File file = new File(REMINDERS_FILE);
    
        try {
            if (!file.exists() || file.length() == 0) {
                System.out.println("⚠️ Reminders file is empty or missing, returning an empty list.");
                return new ArrayList<>();
            }
    
            List<Reminder> reminders = objectMapper.readValue(file, new TypeReference<List<Reminder>>() {});
            System.out.println("Loaded Reminders from JSON: " + reminders.size());
            return reminders;
    
        } catch (IOException e) {
            System.err.println("Error reading reminders: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public static void writeReminders(List<Reminder> reminders) {
        try {
            File file = new File(REMINDERS_FILE);
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, reminders);
    
            System.out.println("✅ Successfully saved reminders to JSON! Total reminders: " + reminders.size());
            List<Reminder> reloadedReminders = readReminders();
            System.out.println(" Final Reminders in JSON after writing: " + reloadedReminders);
        } catch (IOException e) {
            System.err.println("Error writing reminders: " + e.getMessage());
        }
    }
    
    
    
}
