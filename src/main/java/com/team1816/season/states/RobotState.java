package com.team1816.season.states;

import com.google.inject.Singleton;
import com.team1816.season.Constants;
import edu.wpi.first.math.geometry.*;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/* class responsible with logging the robot's ACTUAL states - robot position (predicted) and superstructure subsystem actual states */

@Singleton
public class RobotState {

    public final Field2d field = new Field2d();
    public Pose2d fieldToVehicle = Constants.EmptyPose;
    public Pose2d estimatedFieldToVehicle = Constants.EmptyPose;
    public ChassisSpeeds deltaVehicle = new ChassisSpeeds();

    // Superstructure ACTUAL states

    public boolean overheating = false;

    public RobotState() {
        SmartDashboard.putData("Field", field);
        resetPosition();
    }

    /**
     * Resets the field to robot transform (robot's position on the field)
     */
    public synchronized void resetPosition(
        Pose2d initial_field_to_vehicle,
        Rotation2d initial_vehicle_to_turret
    ) {
        resetPosition(initial_field_to_vehicle);
    }

    public synchronized void resetPosition(Pose2d initial_field_to_vehicle) {
        fieldToVehicle = initial_field_to_vehicle;
    }

    public synchronized void resetPosition() {
        resetPosition(Constants.kDefaultZeroingPose);
    }

    public synchronized void resetAllStates() {
        deltaVehicle = new ChassisSpeeds();
        overheating = false;
    }

    public synchronized Pose2d getLatestFieldToVehicle() {
        // CCW rotation increases degrees
        return fieldToVehicle;
    }

    public synchronized void outputToSmartDashboard() {
        //shuffleboard periodic updates should be here
        field.setRobotPose(fieldToVehicle);
    }

    // Camera state
    public class Point {

        public double cX;
        public double cY;

        public Point() {
            cX = 0;
            cY = 0;
        }
    }
}
