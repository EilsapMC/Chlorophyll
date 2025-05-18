package me.mrhua269.chlorophyll.utils;

import ca.spottedleaf.moonrise.libs.ca.spottedleaf.concurrentutil.collection.MultiThreadedQueue;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class EntityTaskScheduler {
    private final MultiThreadedQueue<Runnable> tasks = new MultiThreadedQueue<>();

    public void destroy() {
        Runnable task;
        while ((task = tasks.pollOrBlockAdds()) != null) {
            task.run();
        }
    }

    public boolean schedule(Runnable task) {
        return tasks.offer(task);
    }

    public boolean isDestroyed() {
        return this.tasks.isAddBlocked();
    }

    public void runTasks() {
        Runnable task;
        while ((task = this.tasks.poll()) != null) {
            task.run();
        }
    }
}
