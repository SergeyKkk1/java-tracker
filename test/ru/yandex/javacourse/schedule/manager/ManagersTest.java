package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ManagersTest {

    @Test
    public void testManagersNotNull() {
        assertNotNull(Managers.getDefault(), "default manager should not be null");
        assertNotNull(Managers.getDefaultHistory(), "default history managers should not be null");
        assertNotNull(Managers.getFileBackedTaskManager(), "file backed task manager should not be null");
    }

}
