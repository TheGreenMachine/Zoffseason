package com.team1816.season.states;

import com.google.inject.Singleton;
import com.team1816.season.Constants;
import com.team1816.season.subsystems.*;
import edu.wpi.first.math.geometry.*;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/* class responsible with logging the robot's ACTUAL states - robot position (predicted) and superstructure subsystem actual states */

@Singleton
public class RobotState {

    public final Field2d field = new Field2d();
    public Pose2d field_to_vehicle = Constants.EmptyPose;
    public Pose2d estimated_field_to_vehicle = Constants.EmptyPose;
    public ChassisSpeeds delta_vehicle = new ChassisSpeeds();

    // Superstructure ACTUAL states
    public Point visionPoint = new Point();


    public RobotState() {
        SmartDashboard.putData("Field", field);
        reset();
    }

    /**
     * Resets the field to robot transform (robot's position on the field)
     */
    public synchronized void reset(
        Pose2d initial_field_to_vehicle,
        Rotation2d initial_vehicle_to_turret
    ) {
        reset(initial_field_to_vehicle);
        vehicle_to_turret = initial_vehicle_to_turret;
    }

    public synchronized void reset(Pose2d initial_field_to_vehicle) {
        field_to_vehicle = initial_field_to_vehicle;
    }

    public synchronized void reset() {
        reset(Constants.StartingPose);
    }

    public synchronized Pose2d getLatestFieldToVehicle() {
        // CCW rotation increases degrees
        return field_to_vehicle;
    }

    public Rotation2d getLatestFieldToTurret() {
        return field_to_vehicle.getRotation().plus(vehicle_to_turret);
    }

    public synchronized void outputToSmartDashboard() {
        //shuffleboard periodic updates should be here
        field.setRobotPose(field_to_vehicle);
        field.getObject(Turret.NAME).setPose(getFieldToTurretPos());
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