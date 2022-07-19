package com.team1816.season.auto.paths;

import com.google.inject.Singleton;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.trajectory.Trajectory;
import java.util.List;

@Singleton
public class TrajectorySet {

    // Testing and Tuning
    public static Trajectory TUNE_DRIVETRAIN;
    public static Trajectory LIVING_ROOM;
    public static List<Rotation2d> LIVING_ROOM_HEADINGS;

    //2022
    public static Trajectory TWO_BALL_A;

    public static Trajectory TWO_BALL_B;
    public static Trajectory FOUR_BALL_B2;
    public static Trajectory FOUR_BALL_B3;

    public static Trajectory TWO_BALL_C;
    public static Trajectory FOUR_BALL_C2;
    public static Trajectory FOUR_BALL_C3;

    public static Trajectory FIVE_BALL_A;
    public static Trajectory FIVE_BALL_B;
    public static Trajectory FIVE_BALL_C;
    public static Trajectory FIVE_BALL_D;

    public static Trajectory ONE_BALL_A_B;
    public static Trajectory ONE_BALL_C_BORDER;

    public static List<Rotation2d> TWO_BALL_A_HEADINGS;

    public static List<Rotation2d> TWO_BALL_B_HEADINGS;
    public static List<Rotation2d> FOUR_BALL_B2_HEADINGS;
    public static List<Rotation2d> FOUR_BALL_B3_HEADINGS;

    public static List<Rotation2d> TWO_BALL_C_HEADINGS;
    public static List<Rotation2d> FOUR_BALL_C2_HEADINGS;
    public static List<Rotation2d> FOUR_BALL_C3_HEADINGS;

    public static List<Rotation2d> FIVE_BALL_A_HEADINGS;
    public static List<Rotation2d> FIVE_BALL_B_HEADINGS;
    public static List<Rotation2d> FIVE_BALL_C_HEADINGS;
    public static List<Rotation2d> FIVE_BALL_D_HEADINGS;

    public static List<Rotation2d> ONE_BALL_A_B_HEADINGS;
    public static List<Rotation2d> ONE_BALL_C_BORDER_HEADINGS;

    public TrajectorySet() {
        // Trajectories
        // DRIVE_STRAIGHT = THIS USES OPEN LOOP - NOT TRAJECTORY
        TUNE_DRIVETRAIN = new DriveStraight(120, 40).generateTrajectory();
        LIVING_ROOM = new LivingRoomPath().generateTrajectory();

        // Heading lists
        LIVING_ROOM_HEADINGS = new LivingRoomPath().generateHeadings();
    }
}
