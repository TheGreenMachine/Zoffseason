package com.team1816.season;

import com.google.inject.Singleton;
import com.team1816.lib.hardware.factory.RobotFactory;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

@Singleton
public class Constants {

    private static final RobotFactory factory = Robot.getFactory();

    public static final Pose2d EmptyPose = new Pose2d();
    public static final Rotation2d EmptyRotation = new Rotation2d();
    public static final double kLooperDt = factory.getConstant("kLooperDt", .020);

    // CAN Timeouts
    public static final int kCANTimeoutMs = 10; // use for important on the fly updates
    public static final int kLongCANTimeoutMs = 100; // use for constructors

    // Field characterization
    public static final double kTargetHeight = 104; // inches
    public static final double kTargetRadius = 26.56; // inches
    public static final double kCameraMountingHeight = 29.5; // inches
    public static final double kHeightFromCamToHub =
        kTargetHeight - kCameraMountingHeight; // inches
    public static final double kCameraMountingAngleY = 26; // degrees
    public static final double fieldCenterY = 8.23 / 2.0;
    public static final double fieldCenterX = 16.46 / 2.0;
    public static final Pose2d targetPos = new Pose2d(
        fieldCenterX,
        fieldCenterY,
        EmptyRotation
    );
    public static final Pose2d kDefaultZeroingPose = new Pose2d(
        0.5,
        fieldCenterY,
        EmptyRotation
    );

    //Badlog
    public static final boolean kIsBadlogEnabled =
        factory.getConstant("badLogEnabled") > 0;
    public static final boolean kIsLoggingTeleOp = factory.getConstant("logTeleOp") > 0;
    public static final boolean kIsLoggingAutonomous = factory.getConstant("logAuto") > 0;
}
