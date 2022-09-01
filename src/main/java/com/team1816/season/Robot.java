package com.team1816.season;

import static com.team1816.lib.subsystems.drive.Drive.kPathFollowingMaxAccelMeters;
import static com.team1816.lib.subsystems.drive.Drive.kPathFollowingMaxVelMeters;
import static com.team1816.season.controlboard.ControlUtils.createHoldAction;

import badlog.lib.BadLog;
import com.team1816.lib.Infrastructure;
import com.team1816.lib.Injector;
import com.team1816.lib.controlboard.ControlBoardBrige;
import com.team1816.lib.controlboard.IControlBoard;
import com.team1816.lib.hardware.factory.RobotFactory;
import com.team1816.lib.loops.Looper;
import com.team1816.lib.subsystems.SubsystemManager;
import com.team1816.lib.subsystems.drive.Drive;
import com.team1816.lib.subsystems.drive.DrivetrainLogger;
import com.team1816.season.auto.AutoModeManager;
import com.team1816.season.controlboard.ActionManager;
import com.team1816.season.states.RobotState;
import com.team1816.season.states.Superstructure;
import edu.wpi.first.wpilibj.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Robot extends TimedRobot {

    private BadLog logger;

    private final Looper enabledLoop;
    private final Looper disabledLoop;

    // controls
    private IControlBoard controlBoard;
    private ActionManager actionManager;

    private final Infrastructure infrastructure;
    private final SubsystemManager subsystemManager;

    //State managers
    private final Superstructure superstructure;
    private final RobotState robotState;

    // subsystems
    private final Drive drive;

    private static RobotFactory factory;

    // autonomous
    private final AutoModeManager autoModeManager;

    // timing
    private double loopStart;
    private boolean faulted = false;

    Robot() {
        super();
        // initialize injector
        Injector.registerModule(new SeasonModule());
        enabledLoop = new Looper(this);
        disabledLoop = new Looper(this);
        drive = (Injector.get(Drive.Factory.class)).getInstance(); //TODO: need to fix this get drive instance should just return the proper one
        superstructure = Injector.get(Superstructure.class);
        infrastructure = Injector.get(Infrastructure.class);
        robotState = Injector.get(RobotState.class);
        subsystemManager = Injector.get(SubsystemManager.class);
        autoModeManager = Injector.get(AutoModeManager.class);
    }

    public static RobotFactory getFactory() {
        if (factory == null) factory = Injector.get(RobotFactory.class);
        return factory;
    }

    private Double getLastLoop() {
        return (Timer.getFPGATimestamp() - loopStart) * 1000;
    }

    @Override
    public void robotInit() {
        try {
            controlBoard = Injector.get(IControlBoard.class);
            DriverStation.silenceJoystickConnectionWarning(true);
            if (Constants.kIsBadlogEnabled) {
                var logFile = new SimpleDateFormat("MMdd_HH-mm").format(new Date());
                var robotName = System.getenv("ROBOT_NAME");
                if (robotName == null) robotName = "default";
                var logFileDir = "/home/lvuser/";
                // if there is a USB drive use it
                if (Files.exists(Path.of("/media/sda1"))) {
                    logFileDir = "/media/sda1/";
                }
                if (RobotBase.isSimulation()) {
                    if (System.getProperty("os.name").toLowerCase().contains("win")) {
                        logFileDir = System.getenv("temp") + "\\";
                    } else {
                        logFileDir = System.getProperty("user.dir") + "/";
                    }
                }
                var filePath = logFileDir + robotName + "_" + logFile + ".bag";
                logger = BadLog.init(filePath);

                BadLog.createValue(
                    "Max Velocity",
                    String.valueOf(kPathFollowingMaxVelMeters)
                );
                BadLog.createValue(
                    "Max Acceleration",
                    String.valueOf(kPathFollowingMaxAccelMeters)
                );

                BadLog.createTopic(
                    "Timings/Looper",
                    "ms",
                    enabledLoop::getLastLoop,
                    "hide",
                    "join:Timings"
                );
                BadLog.createTopic(
                    "Timings/RobotLoop",
                    "ms",
                    this::getLastLoop,
                    "hide",
                    "join:Timings"
                );
                BadLog.createTopic(
                    "Timings/Timestamp",
                    "s",
                    Timer::getFPGATimestamp,
                    "xaxis",
                    "hide"
                );

                DrivetrainLogger.init(drive);
                if (RobotBase.isReal()) {
                    BadLog.createTopic(
                        "PDP/Current",
                        "Amps",
                        infrastructure.getPd()::getTotalCurrent
                    );

                    drive.CreateBadLogValue("Drivetrain PID", drive.pidToString());
                }

                logger.finishInitialization();
            }

            subsystemManager.setSubsystems(drive);
            subsystemManager.registerEnabledLoops(enabledLoop);
            subsystemManager.registerDisabledLoops(disabledLoop);
            subsystemManager.zeroSensors();

            actionManager =
                new ActionManager(
                    createHoldAction(
                        () -> controlBoard.getAsBool("brakeMode"),
                        drive::setBraking
                    ),
                    createHoldAction(
                        () -> controlBoard.getAsBool("slowMode"),
                        drive::setSlowMode
                    )
                );
        } catch (Throwable t) {
            faulted = true;
            throw t;
        }
    }

    @Override
    public void disabledInit() {
        try {
            enabledLoop.stop();

            // Stop any running autos
            autoModeManager.stopAuto();

            if (autoModeManager.getSelectedAuto() == null) {
                autoModeManager.reset();
            }

            superstructure.setStopped();
            subsystemManager.stop();

            robotState.resetAllStates();
            drive.zeroSensors();

            disabledLoop.start();
        } catch (Throwable t) {
            faulted = true;
            throw t;
        }
    }

    @Override
    public void autonomousInit() {
        disabledLoop.stop();

        drive.zeroSensors(autoModeManager.getSelectedAuto().getInitialPose());
        superstructure.setStopped();

        drive.setControlState(Drive.ControlState.TRAJECTORY_FOLLOWING);
        autoModeManager.startAuto();

        enabledLoop.start();
    }

    @Override
    public void teleopInit() {
        try {
            disabledLoop.stop();

            superstructure.setStopped();

            infrastructure.startCompressor();

            enabledLoop.start();
        } catch (Throwable t) {
            faulted = true;
            throw t;
        }
    }

    @Override
    public void testInit() {}

    @Override
    public void robotPeriodic() {
        try {
            // update shuffleboard for subsystem values
            subsystemManager.outputToSmartDashboard();
            // update robot state on field for Field2D widget
            robotState.outputToSmartDashboard();
            // update shuffleboard selected auto mode
            autoModeManager.outputToSmartDashboard();

            if (ControlBoardBrige.getInstance().isDemoMode()) { //todo: should be using injector
                controlBoard.outputToSmartDashboard();
            }
        } catch (Throwable t) {
            faulted = true;
            System.out.println(t.getMessage());
        }
    }

    @Override
    public void disabledPeriodic() {
        loopStart = Timer.getFPGATimestamp();
        try {
            // Periodically check if drivers changed desired auto - if yes, then update the robot's position on the field
            if (autoModeManager.update()) {
                drive.zeroSensors(autoModeManager.getSelectedAuto().getInitialPose());
                robotState.field
                    .getObject("Trajectory")
                    .setTrajectory(
                        autoModeManager.getSelectedAuto().getCurrentTrajectory()
                    );
            }

            // check if demo mode speed multiplier changed
            if (ControlBoardBrige.getInstance().isDemoMode()) { //todo: should be using injector
                controlBoard.update();
            }
        } catch (Throwable t) {
            faulted = true;
            throw t;
        }
    }

    @Override
    public void autonomousPeriodic() {
        loopStart = Timer.getFPGATimestamp();
        robotState.field
            .getObject("Trajectory")
            .setTrajectory(autoModeManager.getSelectedAuto().getCurrentTrajectory());

        if (Constants.kIsLoggingAutonomous) {
            logger.updateTopics();
            logger.log();
        }
    }

    @Override
    public void teleopPeriodic() {
        loopStart = Timer.getFPGATimestamp();

        try {
            manualControl(); // controls drivetrain and turret joystick control mode
        } catch (Throwable t) {
            faulted = true;
            throw t;
        }
        if (Constants.kIsLoggingTeleOp) {
            logger.updateTopics();
            logger.log();
        }
    }

    public void manualControl() {
        // update what's currently being imputed from both driver and operator controllers
        actionManager.update();

        drive.setTeleopInputs(
            -controlBoard.getAsDouble("throttle"),
            -controlBoard.getAsDouble("strafe"),
            controlBoard.getAsDouble("rotation")
        );
    }

    @Override
    public void testPeriodic() {}
}
