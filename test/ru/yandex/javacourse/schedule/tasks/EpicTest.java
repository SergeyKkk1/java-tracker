package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.*;

public class EpicTest {

    public static final LocalDateTime DEFAULT_DATE_TIME = LocalDateTime.of(2025, 10, 10, 12, 30);

    @Test
    public void testEqualityById() {
        Epic e0 = new Epic(1, "Test 1", "Testing task 1");
        Epic e1 = new Epic(1, "Test 2", "Testing task 2");
        assertEquals(e0, e1, "task and subentities should be compared by id");
    }

    @Test
    public void testSubtaskUniqueIds() {
        Epic epic = new Epic(0, "Epic 1", "Testing epic 1");
        epic.addSubtask(new Subtask(1, "name1", "desc1", NEW, 0, Duration.ofMinutes(20), LocalDateTime.now()));
        epic.addSubtask(new Subtask(2, "name2", "desc2", NEW, 0, Duration.ofMinutes(20), LocalDateTime.now()));
        assertEquals(2, epic.subtaskIdToSubtask.size(), "should add distinct subtask ids");
        epic.addSubtask(new Subtask(1, "name1", "desc1", NEW, 0, Duration.ofMinutes(20), LocalDateTime.now()));
        assertEquals(2, epic.subtaskIdToSubtask.size(), "should not add same subtask id twice");
    }

    @Test
    public void testNotSelfAttaching() {
        Epic epic = new Epic(0, "Epic 1", "Testing epic 1");
        epic.addSubtask(new Epic(0, "Epic 1", "Testing epic 1"));
        assertEquals(0, epic.subtaskIdToSubtask.size(), "epic should not add itself as subtask");
    }

    @ParameterizedTest
    @MethodSource("subtaskStatuses")
    public void testEpicStatusCalculation(TaskStatus firstSubtaskStatus, TaskStatus secondSubtaskStatus, TaskStatus expectedEpicStatus) {
        Epic epic = new Epic(0, "Epic 1", "Testing epic 1");
        epic.addSubtask(new Subtask(1, "name1", "desc1", firstSubtaskStatus, 0, Duration.ofMinutes(20), LocalDateTime.now()));
        epic.addSubtask(new Subtask(2, "name2", "desc2", secondSubtaskStatus, 0, Duration.ofMinutes(20), LocalDateTime.now()));

        assertEquals(expectedEpicStatus, epic.getStatus(), "epic status calculated incorrectly");
    }

    @Test
    public void testEpicStartDateCalculation() {
        Epic epic = new Epic(0, "Epic 1", "Testing epic 1");
        epic.addSubtask(new Subtask(1, "name1", "desc1", NEW, 0, Duration.ofMinutes(20), DEFAULT_DATE_TIME.minusDays(4)));
        epic.addSubtask(new Subtask(2, "name2", "desc2", NEW, 0, Duration.ofMinutes(20), DEFAULT_DATE_TIME.plusDays(20)));

        assertEquals(DEFAULT_DATE_TIME.minusDays(4), epic.getStartTime(), "epic stating time should be equal to the earliest subtask start date");
    }

    private static Stream<Arguments> subtaskStatuses() {
        return Stream.of(
                Arguments.of(NEW, NEW, NEW),
                Arguments.of(IN_PROGRESS, NEW, IN_PROGRESS),
                Arguments.of(IN_PROGRESS, DONE, IN_PROGRESS),
                Arguments.of(DONE, DONE, DONE)
        );
    }
}
