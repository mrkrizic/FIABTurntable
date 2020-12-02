package hardware;

import hardware.lego.LegoConveyorHardware;
import hardware.lego.LegoTurningHardware;
import hardware.mock.ConveyorMockHardware;
import hardware.mock.TurningMockHardware;
import lejos.hardware.port.MotorPort;
import lejos.hardware.port.SensorPort;

public class TurningHardwareConfig {
    //In case the operating system is windows, we do not want to use EV3 libraries
    private static final boolean DEBUG = System.getProperty("os.name").toLowerCase().contains("win");

    private final TurningHardware turningHardware;;

    public TurningHardwareConfig() {
        if (DEBUG) {
            turningHardware = new TurningMockHardware(300);
        } else {
            turningHardware = new LegoTurningHardware(MotorPort.D, SensorPort.S4);
            turningHardware.getTurningMotor().setSpeed(300);
        }
        Runtime.getRuntime().addShutdownHook(new Thread(() -> turningHardware.getTurningMotor().stop()));
    }

    public TurningHardware getTurningHardware() {
        return turningHardware;
    }
}
