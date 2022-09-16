package net.orbyfied.j8.render;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RenderWorker {

    // if the worker should be running
    final AtomicBoolean active = new AtomicBoolean();
    // the worker thread
    final WorkerThread thread = new WorkerThread();

    // a task queue
    // can be used to schedule tasks
    // on the worker thread from other threads
    final Deque<Runnable> tasks = new ArrayDeque<>();
    // the list of render contexts
    final List<RenderContext> contexts = new ArrayList<>();

    // timings
    float targetFps;
    float targetDt;

    float fps;
    float dt;

    public RenderWorker() {
        // set target fps
        setTargetFps(60.0f);
    }

    private void activate() {
        thread.start();
    }

    public RenderWorker setTargetFps(float targetFps) {
        if (targetFps != -1) {
            this.targetFps = -1;
            this.targetDt = 1f / targetFps;
        } else {
            this.targetFps = -1;
        }

        return this;
    }

    public RenderWorker setActive(boolean b) {
        this.active.set(b);
        if (b)
            activate();
        return this;
    }

    public RenderWorker put(RenderContext context) {
        context.owner = this;
        synchronized (contexts) {
            contexts.add(context);
        }
        return this;
    }

    public RenderWorker remove(RenderContext context) {
        synchronized (contexts) {
            contexts.remove(context);
        }
        return this;
    }

    public RenderWorker schedule(Runnable runnable) {
        synchronized (tasks) {
            tasks.push(runnable);
        }
        return this;
    }

    /* -------------------------- */

    class WorkerThread extends Thread {

        @Override
        public void run() {
            long t1 = 0;
            long t2 = 0;

            // main loop
            while (active.get()) {
                // timings
                long tns = t2 - t1;
                dt = tns / 1_000_000_000f;
                if (dt < targetDt && targetFps != -1) {
                    tsleep((int)(dt * 1000f), 0);
                    fps = Math.min(targetFps, 1f / dt);
                } else {
                    fps = 1f / dt;
                }

                t1 = System.nanoTime();

                // update contexts
                synchronized (contexts) {
                    for (RenderContext context : contexts)
                        context.update(RenderWorker.this, dt);
                }

                // run tasks
                synchronized (tasks) {
                    while (!tasks.isEmpty()) {
                        try {
                            tasks.poll().run();
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                    }
                }

                // timings
                t2 = System.nanoTime();
            }
        }

        void tsleep(long ms, int nanos) {
            try {
                Thread.sleep(ms, nanos);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

}
