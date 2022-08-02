package com.team1816.season;

import static com.team1816.season.controlboard.ControlUtils.*;

import badlog.lib.BadLog;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.team1816.lib.Infrastructure;
import com.team1816.lib.LibModule;
import com.team1816.lib.auto.AutoModeExecutor;
import com.team1816.lib.auto.actions.LambdaAction;
import com.team1816.lib.auto.modes.AutoModeBase;
import com.team1816.lib.controlboard.IControlBoard;
import com.team1816.lib.hardware.factory.RobotFactory;
import com.team1816.lib.loops.Looper;
import com.team1816.lib.subsystems.Drive;
import com.team1816.lib.subsystems.DrivetrainLogger;
import com.team1816.lib.subsystems.SubsystemManager;
import com.team1816.season.auto.AutoModeSelector;
import com.team1816.season.auto.paths.TrajectorySet;
import com.team1816.season.controlboard.ActionManager;
import com.team1816.season.states.RobotState;
import com.team1816.season.subsystems.Fingers;
import com.team254.lib.util.LatchedBoolean;
import com.team254.lib.util.SwerveDriveSignal;
import edu.wpi.first.wpilibj.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

public class Robot extends TimedRobot {

    private BadLog logger;

    private final Injector injector;

    private final Looper mEnabledLooper = new Looper(this);
    private final Looper mDisabledLooper = new Looper(this);

    private IControlBoard mControlBoard;

    private final SubsystemManager mSubsystemManager;

    //State managers
    private final Infrastructure mInfrastructure;
    private final com.team1816.season.states.RobotState mRobotState;

    // subsystems
    private final Drive mDrive;
    private final Fingers mFingers;

    private final LatchedBoolean mWantsAutoExecution = new LatchedBoolean();
    private final LatchedBoolean mWantsAutoInterrupt = new LatchedBoolean();

    private final AutoModeSelector mAutoModeSelector;
    private final AutoModeExecutor mAutoModeExecutor;
    private TrajectorySet trajectorySet;

    private double loopStart;

    private ActionManager actionManager;

    private boolean faulted = false;

    // private PowerDistributionPanel pdp = new PowerDistributionPanel();

    Robot() {
        super();
        // initialize injector
        injector = Guice.createInjector(new LibModule(), new SeasonModule());
        mDrive = (injector.getInstance(Drive.Factory.class)).getInstance();
        mFingers = (injector.getInstance(Fingers.class));
        mRobotState = injector.getInstance(RobotState.class);
        mSubsystemManager = injector.getInstance(SubsystemManager.class);
        mAutoModeExecutor = injector.getInstance(AutoModeExecutor.class);
        mAutoModeSelector = injector.getInstance(AutoModeSelector.class);
        mInfrastructure = injector.getInstance(Infrastructure.class);
        trajectorySet = injector.getInstance(TrajectorySet.class);
    }

    public static RobotFactory getFactory() {
        return RobotFactory.getInstance();
    }

    private Double getLastLoop() {
        return (Timer.getFPGATimestamp() - loopStart) * 1000;
    }

    @Override
    public void robotInit() {
        try {
            mControlBoard = injector.getInstance(IControlBoard.class);
            DriverStation.silenceJoystickConnectionWarning(true);
            if (Constants.kIsBadlogEnabled) {
                var logFile = new SimpleDateFormat("MMdd_HH-mm").format(new Date());
                var robotName = System.getenv("ROBOT_NAME");
                if (robotName == null) robotName = "default";
                var logFileDir = "/home/lvuser/";
                // if there is a usb drive use it
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
                    String.valueOf(Constants.kPathFollowingMaxVelMeters)
                );
                BadLog.createValue(
                    "Max Acceleration",
                    String.valueOf(Constants.kPathFollowingMaxAccelMeters)
                );

                BadLog.createTopic(
                    "Timings/Looper",
                    "ms",
                    mEnabledLooper::getLastLoop,
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

                DrivetrainLogger.init(mDrive);
                if (RobotBase.isReal()) {
                    BadLog.createTopic(
                        "PDP/Current",
                        "Amps",
                        mInfrastructure.getPdh()::getTotalCurrent
                    );

                    mDrive.CreateBadLogValue("Drivetrain PID", mDrive.pidToString());
                }
            }

            logger.finishInitialization();

            mSubsystemManager.setSubsystems(mDrive, mFingers);

            mSubsystemManager.zeroSensors();

            mSubsystemManager.registerEnabledLoops(mEnabledLooper);
            mSubsystemManager.registerDisabledLoops(mDisabledLooper);

            // Robot starts forwards.
            mRobotState.reset();

            mAutoModeSelector.updateModeCreator();

            //
            actionManager = new ActionManager(
                createAction(() -> mControlBoard.dropFrisbee(), () -> mFingers.incrementFingers())); // () -> boolean
        } catch (Throwable t) {
            faulted = true;
            throw t;
        }
    }

    @Override
    public void disabledInit() {
        try {
            mEnabledLooper.stop();

            // Reset all auto mode states.
            if (mAutoModeExecutor != null) {
                mAutoModeExecutor.stop();
            }
            mAutoModeSelector.updateModeCreator();

            mDisabledLooper.start();

            mDrive.stop();
            mDrive.setBrakeMode(false);
        } catch (Throwable t) {
            faulted = true;
            throw t;
        }
    }

    @Override
    public void autonomousInit() {
        try {
            mDisabledLooper.stop();

            // Robot starts at first waypoint (Pose2D) of current auto path chosen
            mRobotState.reset();

            mDrive.zeroSensors();

            mDrive.setControlState(Drive.DriveControlState.TRAJECTORY_FOLLOWING);
            mAutoModeExecutor.start();

            mEnabledLooper.start();
        } catch (Throwable t) {
            throw t;
        }
    }

    @Override
    public void teleopInit() {
        try {
            mDisabledLooper.stop();

            if (mAutoModeExecutor != null) {
                mAutoModeExecutor.stop();
            }

            mDrive.stop();

            mEnabledLooper.start();

            mInfrastructure.startCompressor();
        } catch (Throwable t) {
            faulted = true;
            throw t;
        }
    }

    @Override
    public void testInit() {
        try {
            double initTime = System.currentTimeMillis();

            mEnabledLooper.stop();
            mDisabledLooper.start();
            mDrive.zeroSensors();

            if (mSubsystemManager.checkSubsystems()) {
                System.out.println("ALL SYSTEMS PASSED");
            } else {
                System.err.println("CHECK ABOVE OUTPUT SOME SYSTEMS FAILED!!!");
            }
        } catch (Throwable t) {
            faulted = true;
            throw t;
        }
    }

    @Override
    public void robotPeriodic() {
        try {
            // update shuffleboard for subsystem values
            mSubsystemManager.outputToSmartDashboard();
            // update robot state on field for Field2D widget
            mRobotState.outputToSmartDashboard();
            mAutoModeSelector.outputToSmartDashboard();
        } catch (Throwable t) {
            faulted = true;
            System.out.println(t.getMessage());
        }
    }

    @Override
    public void disabledPeriodic() {
        loopStart = Timer.getFPGATimestamp();
        try {
            if (RobotController.getUserButton()) {
                mDrive.zeroSensors(Constants.ZeroPose);
            }

            // Periodically check if drivers changed desired auto - if yes, then update the actual auto mode
            mAutoModeSelector.updateModeCreator();

            Optional<AutoModeBase> autoMode = mAutoModeSelector.getAutoMode();
            if (
                autoMode.isPresent() && autoMode.get() != mAutoModeExecutor.getAutoMode()
            ) {
                var auto = autoMode.get();
                System.out.println("Set auto mode to: " + auto.getClass().toString());
                mRobotState.field.getObject("Trajectory");
                mAutoModeExecutor.setAutoMode(auto);
                Constants.StartingPose = auto.getTrajectory().getInitialPose();
            }
        } catch (Throwable t) {
            faulted = true;
            throw t;
        }
    }

    @Override
    public void autonomousPeriodic() {
        loopStart = Timer.getFPGATimestamp();
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

        // If brake button is held, disable drivetrain joystick controls
        if (mControlBoard.getBrakeMode()) {
            mDrive.setOpenLoop(SwerveDriveSignal.BRAKE);
        } else {
            mDrive.setTeleopInputs(
                mControlBoard.getThrottle(),
                mControlBoard.getStrafe(),
                mControlBoard.getTurn(),
                mControlBoard.getSlowMode(),
                false
            );
        }
    }

    @Override
    public void testPeriodic() {}
}
