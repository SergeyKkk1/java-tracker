package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String TASK_COLUMN_NAMES = "id,type,name,status,description,epic";

    private final Path taskStorageCsv = createTaskStorage(Paths.get("tasks.csv"));

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager();
        List<String> readTasksList;
        try {
            readTasksList = Files.readAllLines(file.toPath());
            if (!readTasksList.isEmpty()) {
                readTasksList.removeFirst();
            }
        } catch (IOException e) {
            throw new ManagerSaveException(e.getMessage());
        }
        for (String readTask : readTasksList) {
            Task task = manager.fromString(readTask);
            if (task instanceof Epic epic) {
                manager.addNewEpic(epic);
            } else if (task instanceof Subtask subtask) {
                manager.addNewSubtask(subtask);
            } else {
                manager.addNewTask(task);
            }
        }
        return manager;
    }

    @Override
    public List<Task> getTasks() {
        return findTasksInStorage(task -> TaskType.TASK.equals(task.getType()));
    }

    @Override
    public List<Subtask> getSubtasks() {
        return findTasksInStorage(task -> TaskType.SUBTASK.equals(task.getType())).stream()
                .map(Subtask.class::cast)
                .toList();
    }

    @Override
    public List<Epic> getEpics() {
        return findTasksInStorage(task -> TaskType.EPIC.equals(task.getType())).stream()
                .map(Epic.class::cast)
                .toList();
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        List<Subtask> epicSubtasks = super.getEpicSubtasks(epicId);
        save();
        return epicSubtasks;
    }

    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        Subtask subtask = super.getSubtask(id);
        save();
        return subtask;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }

    @Override
    public int addNewTask(Task task) {
        int newTaskId = super.addNewTask(task);
        save(task);
        return newTaskId;
    }

    @Override
    public int addNewEpic(Epic epic) {
        int newEpicId = super.addNewEpic(epic);
        save(epic);
        return newEpicId;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        Integer subtaskId = super.addNewSubtask(subtask);
        save(subtask);
        return subtaskId;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public List<Task> getHistory() {
        return super.getHistory();
    }

    private Path createTaskStorage(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createFile(path);
            } else {
                cleanUpStorage(path);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Exception while file creation");
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            writer.write(TASK_COLUMN_NAMES);
            writer.newLine();
        } catch (IOException e) {
            throw new ManagerSaveException("Exception while file creation");
        }
        return path;
    }

    private void cleanUpStorage(Path path) {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(path)) {
            // creating default bufferedWriter cleans up the file
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<Task> findTasksInStorage(Predicate<Task> predicate) {
        List<String> tasks;
        try {
            tasks = Files.readAllLines(taskStorageCsv);
        } catch (IOException e) {
            throw new ManagerSaveException("Exception while file reading");
        }
        if (!tasks.isEmpty()) {
            tasks.removeFirst();
        }
        return tasks.stream()
                .map(this::fromString)
                .filter(predicate)
                .toList();
    }

    private void save(Task task) {
        try (BufferedWriter bufferedWriter = Files.newBufferedWriter(taskStorageCsv, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            bufferedWriter.write(toString(task));
            bufferedWriter.newLine();
        } catch (IOException e) {
            throw new ManagerSaveException("Exception while file writing");
        }
    }

    private void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(taskStorageCsv, StandardCharsets.UTF_8)) {
            writer.write(TASK_COLUMN_NAMES);
            writer.newLine();
            Stream.of(tasks.values(), subtasks.values(), epics.values())
                    .flatMap(Collection::stream)
                    .map(this::toString)
                    .forEach(task -> {
                        try {
                            writer.write(task);
                            writer.newLine();
                        } catch (IOException e) {
                            throw new ManagerSaveException("Exception while writing task to file");
                        }
                    });
        } catch (IOException e) {
            throw new ManagerSaveException("Exception while writing to csv file");
        }
    }

    private String toString(Task task) {
        if (task instanceof Subtask subtask) {
            return String.format("%s,%s,%s,%s,%s,%s", subtask.getId(), subtask.getType(), subtask.getName(),
                    subtask.getStatus(), subtask.getDescription(), subtask.getEpicId());
        } else {
            return String.format("%s,%s,%s,%s,%s,", task.getId(), task.getType(), task.getName(),
                    task.getStatus(), task.getDescription());
        }
    }

    private Task fromString(String intialString) {
        String string = intialString.replaceAll("[\\n\\r]", "");
        String[] taskProperties = string.split(",");
        int id = Integer.parseInt(taskProperties[0]);
        TaskType taskType = TaskType.valueOf(taskProperties[1]);
        String name = taskProperties[2];
        TaskStatus status = TaskStatus.valueOf(taskProperties[3]);
        String description = taskProperties[4];

        if (taskProperties.length == 6 && TaskType.SUBTASK.equals(taskType)) {
            int epicId = Integer.parseInt(taskProperties[5]);
            return new Subtask(id, name, description, status, epicId);
        } else {
            return taskType == TaskType.EPIC ? new Epic(id, name, description, status) : new Task(id, name, description, status, taskType);
        }
    }
}
