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
- **Priorities**: Assign different priority levels to tasks.
- **Categories**: Organize tasks into custom categories.
- **Data Persistence**: Tasks, reminders, categories, and priorities are stored in JSON format and persist across sessions.
- **Task Statistics**: View statistics such as total tasks, completed tasks, and upcoming deadlines.


## Project Structure
```
/taskmanager
│── vscode/                 # VS Code launch configurations
│── bin/                    # Compiled Java files
│── docs/                   # Generated Javadoc documentation
│── lib/                    # External libraries (JAR files)
│── lib/javafx/             # JavaFX source files and source code files
│── lib/json/               #JSONHandler                 
│── medialab                #json files for categories, tasks, priorities, reminders
│── ui                      #ui for app
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
    c:; cd 'c:\Users\Dimitra\Desktop\taskmanager'; & 'C:\Program Files\Java\jdk-23\bin\java.exe' '@C:\Users\Dimitra\AppData\Local\Temp\cp_apjc9596xge6ed0typwm3iffa.argfile' 'lib.javafx.TaskManager'
   ```

3. **Generate Javadoc Documentation**:
   ```sh
   javadoc -d docs -sourcepath lib -classpath "bin;lib/javafx/lib/*;lib/json/*;lib/jackson-annotations-2.18.1.jar;lib/jackson-core-2.18.1.jar;lib/jackson-databind-2.18.1.jar" lib/javafx/ReminderHandler.java
   ```

## Usage
- **Adding Tasks**: Click the "Add Task" button and enter task details.
- **Editing Tasks**: Select a task and click "Edit" to modify it.
- **Deleting Tasks**: Select a task and click "Delete" to remove it.
- **Managing Reminders**: Click "Set Reminder" to add a reminder to a task.
- **Managing Categories/Priorities**: Use the respective management windows to add, rename, or delete categories and priorities.



