package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTaskManagerTest {

    @AfterEach
    public void cleanUp() throws IOException {
        Files.deleteIfExists(Paths.get("tasks.csv"));
    }

    @Test
    public void testLoadFromFile() throws IOException {
        File testCsv = File.createTempFile("testCsv", ".csv");
        Path path = testCsv.toPath();
        List<CharSequence> lines = List.of(
                "id,type,name,status,description,epic",
                "1,TASK,Task1,NEW,Description task1,",
                "2,EPIC,Epic2,DONE,Description epic2,",
                "3,SUBTASK,Sub Task2,DONE,Description sub task3,2"
        );
        Files.write(path, lines);

        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(path.toFile());

        List<Task> tasks = fileBackedTaskManager.getTasks();
        List<Epic> epics = fileBackedTaskManager.getEpics();
        List<Subtask> subtasks = fileBackedTaskManager.getSubtasks();
        assertEquals(1, tasks.size());
        assertEquals(1, epics.size());
        assertEquals(1, subtasks.size());
        Task task = tasks.getFirst();
        assertEquals("Description task1", task.getDescription());
        Task epic = epics.getFirst();
        assertEquals("Description epic2", epic.getDescription());
        Task subtask = subtasks.getFirst();
        assertEquals("Description sub task3", subtask.getDescription());
    }

    @Test
    public void testLoadFromEmptyFile() throws IOException {
        Path testCsv = Files.createTempFile("testCsv", ".csv");

        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(testCsv.toFile());

        List<Task> tasks = fileBackedTaskManager.getTasks();
        List<Epic> epics = fileBackedTaskManager.getEpics();
        List<Subtask> subtasks = fileBackedTaskManager.getSubtasks();
        assertEquals(0, tasks.size());
        assertEquals(0, epics.size());
        assertEquals(0, subtasks.size());
    }
}
