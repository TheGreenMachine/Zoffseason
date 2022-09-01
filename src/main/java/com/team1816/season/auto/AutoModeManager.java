package com.team1816.season.auto;

import com.team1816.lib.auto.modes.AutoMode;
import com.team1816.lib.auto.modes.DoNothingMode;
import com.team1816.season.auto.modes.*;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import javax.inject.Singleton;

@Singleton
public class AutoModeManager {

    // Auto mode selection
    private final SendableChooser<DesiredAuto> autoModeChooser;
    private DesiredAuto desiredAuto;

    // Auto mode execution
    private AutoMode autoMode;
    private static Thread autoModeThread;

    public AutoModeManager() {
        // Sendable chooser represents the dropdown menu in shuffleboard to pick our desired auto mode
        autoModeChooser = new SendableChooser<>();
        // Populate dropdown menu with all possible auto modes (represented as DesiredMode enums)
        SmartDashboard.putData("Auto mode", autoModeChooser);

        for (DesiredAuto desiredAuto : DesiredAuto.values()) {
            autoModeChooser.addOption(desiredAuto.name(), desiredAuto);
        }
        autoModeChooser.setDefaultOption(
            DesiredAuto.DRIVE_STRAIGHT.name(),
            DesiredAuto.DRIVE_STRAIGHT
        );

        // Initialize auto mode and its respective thread
        reset();
    }

    public void reset() {
        autoMode = new DriveStraightMode();
        autoModeThread = new Thread(autoMode::run);
        desiredAuto = DesiredAuto.DRIVE_STRAIGHT;
    }

    public boolean update() {
        DesiredAuto selectedAuto = autoModeChooser.getSelected();
        boolean autoChanged = desiredAuto != selectedAuto;

        // if auto has been changed, update selected auto mode + thread
        if (autoChanged) {
            System.out.println(
                "Auto changed from: " + desiredAuto + ", to: " + selectedAuto.name()
            );

            autoMode = generateAutoMode(selectedAuto);
            autoModeThread = new Thread(autoMode::run);
        }
        desiredAuto = selectedAuto;

        return autoChanged;
    }

    public void outputToSmartDashboard() {
        if (desiredAuto != null) {
            SmartDashboard.putString("AutoModeSelected", desiredAuto.name());
        }
    }

    public AutoMode getSelectedAuto() {
        return autoMode;
    }

    // Auto Mode Executor
    public void startAuto() {
        autoModeThread.start();
    }

    public void stopAuto() {
        if (autoMode != null) {
            autoMode.stop();
            autoModeThread = new Thread(autoMode::run);
        }
    }

    enum DesiredAuto {
        // 2020
        DO_NOTHING,
        TUNE_DRIVETRAIN,
        LIVING_ROOM,
        DRIVE_STRAIGHT,
    }

    private AutoMode generateAutoMode(DesiredAuto mode) {
        switch (mode) {
            case DO_NOTHING:
                return new DoNothingMode();
            case TUNE_DRIVETRAIN:
                return new TuneDrivetrainMode();
            case LIVING_ROOM:
                return (new LivingRoomMode());
            default:
                System.out.println("Defaulting to drive straight mode");
                return new DriveStraightMode();
        }
    }
}
