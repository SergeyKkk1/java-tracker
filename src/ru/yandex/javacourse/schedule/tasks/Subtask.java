package ru.yandex.javacourse.schedule.tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
	protected int epicId;

	public Subtask(int id, String name, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
		super(id, name, description, status, TaskType.SUBTASK, duration, startTime);
		setEpicId(epicId, id);
	}

	public Subtask(String name, String description, TaskStatus status, int epicId) {
		super(name, description, status, TaskType.SUBTASK);
		setEpicId(epicId, id);
	}

	private void setEpicId(int epicId, int id) {
		if (epicId != id) {
			this.epicId = epicId;
		} else {
			System.out.println("WARN subtask cannot be attached to itself");
		}
	}

	public int getEpicId() {
		return epicId;
	}

	@Override
	public String toString() {
		return "Subtask{" +
				"id=" + id +
				", epicId=" + epicId +
				", name='" + name + '\'' +
				", status=" + status +
				", description='" + description + '\'' +
				'}';
	}
}
