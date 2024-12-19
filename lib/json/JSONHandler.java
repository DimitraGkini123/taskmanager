package lib.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import lib.javafx.Task;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class JSONHandler {

    private static final String FILE_PATH = "medialab/tasks.json"; // Path to the JSON file

    public static List<Task> readTasks() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(new File(FILE_PATH), objectMapper.getTypeFactory().constructCollectionType(List.class, Task.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeTasks(List<Task> tasks) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(FILE_PATH), tasks);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
