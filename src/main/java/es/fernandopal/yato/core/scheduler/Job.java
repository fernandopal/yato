package es.fernandopal.yato.core.scheduler;

import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public abstract class Job extends TimerTask {
    private final long delay;
    private final long period;
    private final TimeUnit unit;

    protected Job(long delay, long period, TimeUnit unit) {
        this.delay = delay;
        this.period = period;
        this.unit = unit;
    }

    long getDelay() {
        return delay;
    }

    long getPeriod() {
        return period;
    }

    TimeUnit getUnit() {
        return unit;
    }

    protected void handleTask(Task... tasks) {
        for(Task task: tasks) {
            try {
                task.run();
            } catch(Exception ex) {
            	ex.printStackTrace();
            }
        }
    }

}