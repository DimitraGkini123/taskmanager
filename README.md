# Task Manager

## Description
This is a JavaFX-based Task Management application that allows users to create, manage, and track tasks with categories, priorities, and reminders. The application stores its data using JSON files and supports various functionalities, including:

- Creating, editing, and deleting tasks
- Managing task categories and priorities
- Setting and modifying reminders
- Viewing task statistics
- Saving and loading data persistently

## Features
- **Task Management**: Add, edit, delete, and categorize tasks.
- **Reminders**: Set custom reminders for tasks.
- **Priority System**: Assign different priority levels to tasks.
- **Categories**: Organize tasks into custom categories.
- **Data Persistence**: Tasks, reminders, categories, and priorities are stored in JSON format and persist across sessions.
- **Task Statistics**: View statistics such as total tasks, completed tasks, and upcoming deadlines.

## Project Structure
```
/taskmanager
│── bin/                    # Compiled Java files
│── docs/                   # Generated Javadoc documentation
│── json/                   # JSON files storing application data
│── lib/                    # External libraries (JAR files)
│── lib/javafx/             # JavaFX source files
│── lib/json/               # JSON handling source files
│── src/                    # Source code directory
│── README.md               # Project documentation
│── settings.json           # VS Code project settings
│── launch.json             # VS Code launch configurations
```

## Installation and Setup
### Prerequisites
- Java 17 or later
- JavaFX SDK
- VS Code (optional, for development)

### Build and Run
1. **Compile the Project**:
   ```sh
   javac -d bin -classpath "lib/javafx/lib/*;lib/*" src/**/*.java
   ```

2. **Run the Application**:
   ```sh
   java --module-path lib/javafx/lib --add-modules javafx.controls,javafx.fxml -cp bin lib.javafx.TaskManager
   ```

3. **Generate Javadoc Documentation**:
   ```sh
   javadoc -d docs -sourcepath lib -subpackages javafx:json -classpath "bin;lib/javafx/lib/*;lib/*"
   ```

## Usage
- **Adding Tasks**: Click the "Add Task" button and enter task details.
- **Editing Tasks**: Select a task and click "Edit" to modify it.
- **Deleting Tasks**: Select a task and click "Delete" to remove it.
- **Managing Reminders**: Click "Set Reminder" to add a reminder to a task.
- **Managing Categories/Priorities**: Use the respective management windows to add, rename, or delete categories and priorities.

## Troubleshooting
### Common Issues
1. **JavaFX Runtime Error**:
   - Ensure JavaFX SDK is correctly referenced in the classpath.
   - Use the correct `--module-path` and `--add-modules` options when running the application.

2. **Javadoc Generation Issues**:
   - Ensure the classpath includes all required JAR files (Jackson, JavaFX).
   - Use the correct `-sourcepath` and `-subpackages` parameters.

3. **Data Not Saving**:
   - Ensure JSON files have write permissions.
   - Check for JSON formatting errors if the application crashes while saving.

## License
This project is licensed under the MIT License.

## Author
Developed by Dimitra.

