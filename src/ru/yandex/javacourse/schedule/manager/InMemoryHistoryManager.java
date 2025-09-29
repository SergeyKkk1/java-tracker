package ru.yandex.javacourse.schedule.manager;

import java.util.*;

import ru.yandex.javacourse.schedule.tasks.Task;

/**
 * In memory history manager.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class InMemoryHistoryManager implements HistoryManager {
	private final HashMap<Integer, Node> taskIdToNode = new HashMap<>();
	private Node head;
	private Node tail;

	@Override
	public List<Task> getHistory() {
		return new ArrayList<>(getTasks());
	}

	@Override
	public void addTask(Task task) {
		if (task == null) {
			return;
		}
		if (taskIdToNode.containsKey(task.getId())) {
			Node alreadyInHistoryNode = taskIdToNode.get(task.getId());
			removeNode(alreadyInHistoryNode);
		}
		linkLast(task);
	}

	@Override
	public void remove(int id) {
		Node removingNode = taskIdToNode.remove(id);
		if (removingNode != null) {
			removeNode(removingNode);
		}
	}

	@Override
	public void removeAll() {
		taskIdToNode.clear();
	}

	@Override
	public void removeAll(Set<Integer> ids) {
		ids.forEach(this::remove);
	}

	private void linkLast(Task task) {
		final Node oldLast = tail;
		final Node newLast = new Node(task, oldLast, null);
		tail = newLast;
		if (oldLast == null) {
			head = newLast;
		} else {
			oldLast.setNext(newLast);
		}
		taskIdToNode.put(task.getId(), newLast);
	}

	private List<Task> getTasks() {
		List<Task> tasks = new ArrayList<>();
		Node current = head;
		while (current != null) {
			tasks.add(current.getTask());
			current = current.getNext();
		}
		return tasks;
	}

	private void removeNode(Node node) {
		final Node prev = node.getPrev();
		final Node next = node.getNext();

		if (prev == null) {
			head = next;
		} else {
			prev.setNext(next);
			node.setPrev(null);
		}

		if (next == null) {
			tail = prev;
		} else {
			next.setPrev(prev);
			node.setNext(null);
		}
	}
}
