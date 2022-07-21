package com.team1816.season.subsystems;

import com.team1816.lib.hardware.components.motor.IGreenMotor;
import com.team1816.lib.subsystems.Subsystem;

public class Folder extends Subsystem {

    private static final String NAME = "folder";

    // Components
    private final IGreenMotor folderMotor;

    // State
    private double desiredOutput;
    private double actualOutput;
    private boolean outputsChanged;
    private STATE desiredState = STATE.STOP;

    // Constants
    private final double ONE;
    private final double TWO;
    private final double THREE;
    private final double FOUR;


    public Folder() {
        super(NAME);

        // Components
        this.folderMotor = factory.getMotor(NAME, "folderMotor");

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

    public enum STATE {
        STOP,
        ONE,
        TWO,
        THREE,
        FOUR,
    }
}
