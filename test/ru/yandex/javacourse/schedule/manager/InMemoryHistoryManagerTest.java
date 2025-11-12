package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;
import ru.yandex.javacourse.schedule.tasks.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class InMemoryHistoryManagerTest extends TaskManagersTest<InMemoryTaskManager> {

    HistoryManager historyManager;

    @BeforeEach
    public void initHistoryManager() {
        manager = new InMemoryTaskManager();
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    public void testHistoricVersionsByPointer() {
        Task task = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, TaskType.TASK, Duration.ofMinutes(20), LocalDateTime.now());
        assertTrue(historyManager.getHistory().isEmpty(), "History should be empty");
        historyManager.addTask(task);
        assertEquals(task.getStatus(), historyManager.getHistory().getFirst().getStatus(), "historic task should be stored");
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.addTask(task);
        assertEquals(TaskStatus.IN_PROGRESS, historyManager.getHistory().getFirst().getStatus(), "historic task should be updated");
        assertEquals(1, historyManager.getHistory().size(), "only last historical event should have been added");
    }

    @Test
    public void testHistoricVersions() {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW, TaskType.TASK, Duration.ofMinutes(20), LocalDateTime.now());
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.addTask(task);
        assertEquals(1, historyManager.getHistory().size(), "historic task should be replaced with last one");
    }

    @Test
    public void testHistoricOrder() {
        List<Task> tasks = prepareTasks();

        tasks.forEach(task -> historyManager.addTask(task));

        List<Integer> historicalTaskIds = historyManager.getHistory().stream().map(Task::getId).toList();
        assertEquals(List.of(2, 1, 3), historicalTaskIds);
    }

    @Test
    public void testTaskRemoval() {
        List<Task> tasks = prepareTasks();

        tasks.forEach(task -> historyManager.addTask(task));
        historyManager.remove(1);
        historyManager.remove(3);

        List<Integer> historicalTaskIds = historyManager.getHistory().stream().map(Task::getId).toList();
        assertEquals(List.of(2), historicalTaskIds);
    }

    @Test
    @DisplayName("Почистить историю")
    void testRemoveAll(){
        Epic epic1 = new Epic(1, "epic1", "description1");
        Epic epic2 = new Epic(2, "epic2", "description2");
        historyManager.addTask(epic1);
        historyManager.addTask(epic2);
        assertEquals(2, historyManager.getHistory().size());
        historyManager.removeAll();
        assertEquals(0, historyManager.getHistory().size());
    }

    private List<Task> prepareTasks() {
        return List.of(
                new Task(1, "T1", "D1", TaskStatus.NEW, TaskType.TASK, Duration.ofMinutes(20), LocalDateTime.now().minusDays(1)),
                new Task(2, "T1", "D1", TaskStatus.DONE, TaskType.TASK, Duration.ofMinutes(20), LocalDateTime.now().minusDays(2)),
                new Task(1, "T1", "D1", TaskStatus.IN_PROGRESS, TaskType.TASK, Duration.ofMinutes(20), LocalDateTime.now().minusDays(3)),
                new Task(3, "T3", "D3", TaskStatus.NEW, TaskType.TASK, Duration.ofMinutes(20), LocalDateTime.now().minusDays(4))
        );
    }
}
