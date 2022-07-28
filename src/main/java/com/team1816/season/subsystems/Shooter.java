package com.team1816.season.subsystems;

import com.team1816.lib.hardware.PIDSlotConfiguration;
import com.team1816.lib.hardware.components.motor.IGreenMotor;
import com.team1816.lib.subsystems.PidProvider;
import com.team1816.lib.subsystems.Subsystem;

public class Shooter extends Subsystem implements PidProvider {

    public static final String NAME = "shooter";

    private final IGreenMotor shooterMotor;

    // State
    private double desiredOutput;
    private double actualOutput;
    private boolean outputsChanged;
    private STATE desiredState = STATE.STOP;

    // Constants
    private final PIDSlotConfiguration pidConfig;

    private final double NEAR_VELOCITY = (int) factory.getConstant(NAME, "nearVel");

    private final double MID_VELOCITY = (int) factory.getConstant(NAME, "midVel");

    private final double TARMAC_TAPE_VEL = (int) factory.getConstant(NAME,"tarmacTapeVel");

    private final double LAUNCHPAD_VEL = (int) factory.getConstant(NAME,"maxVel");

    private final double MAX_VELOCITY = (int) factory.getConstant(NAME, "maxVel");

    private final double COAST_VELOCITY = (int) factory.getConstant(NAME,"coastVel");

    public final int VELOCITY_THRESHOLD;


    public Shooter(String name) {
        super(name);
        pidConfig = factory.getPidSlotConfig(NAME);
        VELOCITY_THRESHOLD = pidConfig.allowableError.intValue();

        // Components
        this.shooterMotor = factory.getMotor(NAME, "shooterMotor");

        // Constants
        ONE = factory.getConstant(NAME, "one", -1);
        TWO = factory.getConstant(NAME, "two", -1);
        THREE = factory.getConstant(NAME, "three", -1);
        FOUR = factory.getConstant(NAME, "four", -1);
    }

    public void setDesiredState(STATE state) {
        if (this.desiredState != state) {
            this.desiredState = state;
            outputsChanged = true;
        }
    }

    public double getActualOutput() {
        return actualOutput;
    }
    public double getDesiredOutput() {
        return desiredOutput;
    }

    @Override
    public void readFromHardware() {
//        add stuff
    }

    @Override
    public void writeToHardware() {
        if (outputsChanged) {
            outputsChanged = false;
            switch (desiredState) {
                // add case statements
                case STOP:
                    break;
                case ONE:
                    break;
                case TWO:
                    break;
                case THREE:
                    break;
                case FOUR:
                    break;
            }
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean checkSystem() {
        return false;
    }

    @Override
    public PIDSlotConfiguration getPIDConfig() {
        return null;
    }

    public enum STATE {
        STOP,
        ONE,
        TWO,
        THREE,
        FOUR,
    }
}


