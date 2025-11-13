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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private static final String TASK_COLUMN_NAMES = "id,type,name,status,description,epic,duration,startTime";

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
    public Integer addNewTask(Task task) {
        Integer newTaskId = super.addNewTask(task);
        save(task);
        return newTaskId;
    }

    @Override
    public Integer addNewEpic(Epic epic) {
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
        String durationInMinutes = task.getDuration() == null ? "" : String.valueOf(task.getDuration().toMinutes());
        if (task instanceof Subtask subtask) {
            return String.format("%s,%s,%s,%s,%s,%s,%s,%s", subtask.getId(), subtask.getType(), subtask.getName(),
                    subtask.getStatus(), subtask.getDescription(), subtask.getEpicId(), durationInMinutes, subtask.getStartTime());
        } else {
            return String.format("%s,%s,%s,%s,%s,,%s,%s", task.getId(), task.getType(), task.getName(),
                    task.getStatus(), task.getDescription(), durationInMinutes, task.getStartTime() == null ? "" : task.getStartTime().toString());
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

        if (taskProperties.length == 8 && TaskType.SUBTASK.equals(taskType)) {
            int epicId = Integer.parseInt(taskProperties[5]);
            Duration duration = Duration.ofMinutes(Integer.parseInt(taskProperties[6]));
            LocalDateTime startTime = LocalDateTime.parse(taskProperties[7]);
            return new Subtask(id, name, description, status, epicId, duration, startTime);
        } else if (taskType == TaskType.EPIC) {
            return new Epic(id, name, description, status);
        } else {
            Duration duration = Duration.ofMinutes(Integer.parseInt(taskProperties[6]));
            LocalDateTime startTime = LocalDateTime.parse(taskProperties[7]);
            return new Task(id, name, description, status, taskType, duration, startTime);
        }
    }
}
