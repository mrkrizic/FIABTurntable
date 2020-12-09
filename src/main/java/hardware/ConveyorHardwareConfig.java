package hardware;

import hardware.lego.LegoConveyorHardware;
import hardware.mock.ConveyorMockHardware;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;

public class ConveyorHardwareConfig {
    //In case the operating system is windows, we do not want to use EV3 libraries
    private static final boolean DEBUG = System.getProperty("os.name").toLowerCase().contains("win");

    private final ConveyorHardware conveyorHardware;

    public ConveyorHardwareConfig() {
        if (DEBUG) {
            conveyorHardware = new ConveyorMockHardware(200, 1000);
        } else {
            conveyorHardware = new LegoConveyorHardware(MotorPort.A, SensorPort.S2, SensorPort.S3);
            conveyorHardware.getConveyorMotor().setSpeed(500);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            conveyorHardware.getConveyorMotor().stop();
        }));
    }

    public ConveyorHardware getConveyorHardware() {
        return conveyorHardware;
    }
}
