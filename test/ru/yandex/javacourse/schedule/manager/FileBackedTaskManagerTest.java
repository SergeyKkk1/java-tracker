package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.yandex.javacourse.schedule.tasks.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileBackedTaskManagerTest extends TaskManagersTest<FileBackedTaskManager> {
    private static final LocalDateTime SUBTASK_START_TIME = LocalDateTime.parse("2025-09-10T21:42:27");
    private static final int SUBTASK_DURATION_MINUTES = 4000;

    @TempDir
    Path tempDir;

    @AfterEach
    public void cleanUp() throws IOException {
        Files.deleteIfExists(Paths.get("tasks.csv"));
    }

    @BeforeEach
    public void init() {
        manager = new FileBackedTaskManager();
    }

    @Test
    public void testLoadFromFile() throws IOException {
        File testCsv = File.createTempFile("testCsv", ".csv");
        Path path = testCsv.toPath();
        List<CharSequence> lines = List.of(
                "id,type,name,status,description,epic,duration,startTime",
                "1,TASK,Task1,NEW,Description task1,,30,2025-11-10T21:42:27",
                "2,EPIC,Epic2,DONE,Description epic2,,60,2025-10-10T21:42:27",
                "3,SUBTASK,Sub Task2,DONE,Description sub task3,2,4000,2025-09-10T21:42:27"
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
        assertEquals(LocalDateTime.parse("2025-11-10T21:42:27"), task.getStartTime());
        assertEquals(30, task.getDuration().toMinutes());

        Task epic = epics.getFirst();
        assertEquals("Description epic2", epic.getDescription());
        assertEquals(SUBTASK_START_TIME, epic.getStartTime());
        assertEquals(SUBTASK_DURATION_MINUTES, epic.getDuration().toMinutes());

        Task subtask = subtasks.getFirst();
        assertEquals("Description sub task3", subtask.getDescription());
        assertEquals(SUBTASK_START_TIME, subtask.getStartTime());
        assertEquals(SUBTASK_DURATION_MINUTES, subtask.getDuration().toMinutes());
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

    @Test
    public void testExceptionWhileAddingTask() throws IOException {
        File testCsv = File.createTempFile("testCsv", ".csv");
        Path path = testCsv.toPath();
        List<CharSequence> lines = List.of(
                "id,type,name,status,description,epic,duration,startTime",
                "1,TASK,Task1,NEW,Description task1,,30,2025-11-10T21:42:27",
                "2,EPIC,Epic2,DONE,Description epic2,,60,2025-10-10T21:42:27",
                "3,SUBTASK,Sub Task2,DONE,Description sub task3,2,4000,2025-09-10T21:42:27"
        );
        Files.write(path, lines);
        FileBackedTaskManager fileBackedTaskManager = FileBackedTaskManager.loadFromFile(path.toFile());
        Paths.get("tasks.csv").toFile().setWritable(false, false);

        assertThrows(ManagerSaveException.class,
                () -> fileBackedTaskManager.addNewTask(new Task("Test 1", "Testing task 1", TaskStatus.NEW,
                        TaskType.TASK, Duration.ofDays(3), LocalDateTime.now().plusDays(10))));
    }

    @Test
    public void testReadingFromFileException() {
        assertThrows(ManagerSaveException.class, () -> FileBackedTaskManager.loadFromFile(tempDir.toFile()));
    }
}
