package ru.yandex.javacourse.schedule.tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.*;

public class Epic extends Task {
	protected Map<Integer, Task> subtaskIdToSubtask = new LinkedHashMap<>();

	public Epic(int id, String name, String description) {
		super(id, name, description, NEW, TaskType.EPIC);
	}

	public Epic(String name, String description) {
		super(name, description, NEW, TaskType.EPIC);
	}

	public Epic(int id, String name, String description, TaskStatus status) {
		super(id, name, description, status, TaskType.EPIC);
	}

	public void addSubtask(Task subtask) {
		if (this.id == subtask.id) {
			System.out.println("WARN epic should not add itself as subtask");
		} else if (subtaskIdToSubtask.containsKey(id)) {
			System.out.println("WARN should add distinct subtask ids");
		} else {
			subtaskIdToSubtask.put(subtask.getId(), subtask);
		}
	}

	public List<Integer> getSubtaskIds() {
		return new ArrayList<>(subtaskIdToSubtask.keySet());
	}

	public void cleanSubtaskIds() {
		subtaskIdToSubtask.clear();
	}

	public void removeSubtask(int id) {
		subtaskIdToSubtask.remove(id);
	}

	@Override
	public Duration getDuration() {
		return subtaskIdToSubtask.values().stream()
				.map(Task::getDuration)
				.filter(Objects::nonNull)
				.reduce(Duration::plus)
				.orElse(null);
	}

	@Override
	public LocalDateTime getStartTime() {
		return subtaskIdToSubtask.values().stream()
				.filter(task -> task.getStartTime() != null)
				.min(Comparator.comparing(Task::getStartTime))
				.map(Task::getStartTime)
				.orElse(null);
	}

	@Override
	public LocalDateTime getEndTime() {
		return subtaskIdToSubtask.values().stream()
				.filter(task -> task.getEndTime() != null)
				.max(Comparator.comparing(Task::getEndTime))
				.map(Task::getEndTime)
				.orElse(null);
	}

	@Override
	public String toString() {
		return "Epic{" +
				"id=" + id +
				", name='" + name + '\'' +
				", status=" + status +
				", description='" + description + '\'' +
				", subtaskIds=" + subtaskIdToSubtask +
				'}';
	}

	@Override
	public TaskStatus getStatus() {
		if (subtaskIdToSubtask.isEmpty()) {
			return NEW;
		}
		boolean isAnyInProgress = subtaskIdToSubtask.values().stream().anyMatch(subtask -> subtask.getStatus() == IN_PROGRESS);
		if (isAnyInProgress) {
			return IN_PROGRESS;
		}
		boolean isAllDone = subtaskIdToSubtask.values().stream().allMatch(subtask -> subtask.getStatus() == DONE);
		return isAllDone ? DONE : NEW;
	}
}
