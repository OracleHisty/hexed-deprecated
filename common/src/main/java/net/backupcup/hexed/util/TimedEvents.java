package net.backupcup.hexed.util;

import java.util.concurrent.CopyOnWriteArrayList;

/** Runs actions a number of server ticks in the future. */
public final class TimedEvents {
    private static final CopyOnWriteArrayList<TimedEvent> EVENTS = new CopyOnWriteArrayList<>();

    private TimedEvents() {}

    /**
     * Waits {@code tickDelay} ticks and then runs {@code action}. With {@code repeatable} it keeps running the
     * action every {@code tickDelay} ticks until {@link #cancel} is called.
     */
    public static TimedEvent schedule(boolean repeatable, int tickDelay, Runnable action) {
        TimedEvent event = new TimedEvent(repeatable, tickDelay, action);
        EVENTS.add(event);
        return event;
    }

    public static TimedEvent schedule(int tickDelay, Runnable action) {
        return schedule(false, tickDelay, action);
    }

    public static void cancel(TimedEvent event) {
        EVENTS.remove(event);
    }

    public static void tick() {
        EVENTS.forEach(TimedEvent::tick);
    }

    public static void clear() {
        EVENTS.clear();
    }

    public static final class TimedEvent {
        private final boolean repeatable;
        private final int tickDelay;
        private final Runnable action;
        private int ticks = 1;

        private TimedEvent(boolean repeatable, int tickDelay, Runnable action) {
            this.repeatable = repeatable;
            this.tickDelay = Math.max(1, tickDelay);
            this.action = action;
        }

        private void tick() {
            if (++ticks < tickDelay) return;
            action.run();
            if (repeatable) {
                ticks = 1;
            } else {
                cancel(this);
            }
        }
    }
}
