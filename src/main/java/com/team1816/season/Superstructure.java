package com.team1816.season;

import com.team1816.season.subsystems.Folder;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Superstructure {

    @Inject
    private static Folder folder;

    private boolean revving;
    private boolean firing;

    public Superstructure() {
        revving = false;
        firing = false;
    }

    public void setStopped() {
        folder.setDesiredState(Folder.STATE.STOP);
        shooter.setDesiredState(shooter.STATE.STOP);

        revving = false;
        firing = false;
        System.out.println("stopping/starting superstructure");
    }

    public void setFiring(boolean firing) {
        this.firing = firing;
        System.out.println("struct - fire " + firing);
        if (firing) {
            folder.setDesiredState(Folder.STATE.RUNNING);
            shooter.setDesiredState(shooter.STATE.FIRING);
        }
        else {
            folder.setDesiredState(Folder.STATE.STOP);
            shooter.setDesiredState(shooter.STATE.STOP);
        }
    }
}
