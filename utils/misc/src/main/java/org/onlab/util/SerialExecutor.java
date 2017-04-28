/*
 * Copyright 2017-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onlab.util;

import java.util.LinkedList;
import java.util.concurrent.Executor;

/**
 * Executor that executes tasks in serial on a shared thread pool.
 * <p>
 * The serial executor behaves semantically like a single-threaded executor, but multiplexes tasks on a shared thread
 * pool, ensuring blocked threads in the shared thread pool don't block individual serial executors.
 */
public class SerialExecutor implements Executor {
    private final Executor parent;
    private final LinkedList<Runnable> tasks = new LinkedList<>();
    private boolean running;

    public SerialExecutor(Executor parent) {
        this.parent = parent;
    }

    private void run() {
        for (;;) {
            final Runnable task;
            synchronized (tasks) {
                task = tasks.poll();
                if (task == null) {
                    running = false;
                    return;
                }
            }
            task.run();
        }
    }

    @Override
    public void execute(Runnable command) {
        synchronized (tasks) {
            tasks.add(command);
            if (!running) {
                running = true;
                parent.execute(this::run);
            }
        }
    }
}