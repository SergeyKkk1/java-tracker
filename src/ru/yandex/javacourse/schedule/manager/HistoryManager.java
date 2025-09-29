package ru.yandex.javacourse.schedule.manager;

import java.util.List;
import java.util.Set;

import ru.yandex.javacourse.schedule.tasks.Task;

/**
 * History manager.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public interface HistoryManager {
	List<Task> getHistory();

	void addTask(Task task);

	void remove(int id);

	void removeAll();

	void removeAll(Set<Integer> ids);
}
