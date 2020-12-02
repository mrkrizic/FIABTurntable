package hardware.actuators;


import hardware.sensors.MockSensor;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class TurningMockMotor extends MockMotor {

    private MockSensor sensorHoming;
    private long delay;
    private int currentAngle;
    private boolean isTurningForward, isTurningBackward;
    private ScheduledThreadPoolExecutor executor;
    private ScheduledFuture timerTask;
    private ScheduledFuture rotateTask;

    public TurningMockMotor(MockSensor sensorHoming, int speed) {
        super(speed);
        this.sensorHoming = sensorHoming;
        this.delay = speed * 10;     //simulate time for turning, assuming speed >= 100 (1s) and speed <= 500 (5s)
        isTurningForward = false;
        isTurningBackward = false;
        currentAngle = 0;
        sensorHoming.setDetectedInput(false);
        executor = new ScheduledThreadPoolExecutor(2);
    }

    @Override
    public void forward() {
        super.forward();
        isTurningForward = true;
        isTurningBackward = false;
        sensorHoming.setDetectedInput(false);
        rotateTask = executor.scheduleAtFixedRate(() -> {
            currentAngle += 10;
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    @Override
    public void backward() {
        super.backward();
        isTurningForward = false;
        isTurningBackward = true;
        rotateTask = executor.scheduleAtFixedRate(() -> {
                    currentAngle -= 10;
                }, 0, 100, TimeUnit.MILLISECONDS);
        timerTask = executor.schedule(() -> sensorHoming.setDetectedInput(true),
                delay, TimeUnit.MILLISECONDS);
    }

    @Override
    public void stop() {
        super.stop();
        if (timerTask != null) {
            timerTask.cancel(true);
        }
        if (rotateTask != null) {
            rotateTask.cancel(true);
        }
        isTurningForward = false;
        isTurningBackward = false;
    }

    @Override
    public void waitMs(long period) {
        super.waitMs(period);
    }

    @Override
    public int getRotationAngle() {
        return currentAngle;
    }

    @Override
    public void resetTachoCount() {
        super.resetTachoCount();
        currentAngle = 0;
    }
}
