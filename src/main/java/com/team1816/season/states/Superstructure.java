package com.team1816.season.states;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.team1816.lib.subsystems.drive.Drive;

/* class responsible for organizing subsystem behaviors into runnable actions - manages the robot's DESIRED states */

@Singleton
public class Superstructure {

    private static Drive drive;

    @Inject
    public Superstructure(Drive.Factory df) {
        drive = df.getInstance();
    }

    public void setStopped() {
        System.out.println("stopping/starting superstructure");
    }
}
