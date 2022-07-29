package com.team1816.season.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.team1816.lib.hardware.components.motor.IGreenMotor;
import com.team1816.lib.subsystems.Subsystem;

public class Hood extends Subsystem {

    private static final String NAME = "hood";

    // Components
    private final IGreenMotor hoodMotor;

    // State
    private double desiredPosition;
    private double actualPosition;
    private boolean outputsChanged;
    private STATE desiredState = STATE.STOP;

    // Constants
    private final double ONE;
    private final double TWO;
    private final double THREE;
    private final double FOUR;


    public Hood() {
        super(NAME);

        // Components
        this.hoodMotor = factory.getMotor(NAME, "hoodMotor");

        // Constants
        ONE = factory.getConstant(NAME, "one", -1);
        TWO = factory.getConstant(NAME, "two", -1);
        THREE = factory.getConstant(NAME, "three", -1);
        FOUR = factory.getConstant(NAME, "four", -1);
    }

    public void setDesiredPosition(double position){
        desiredState = STATE.POSITION;
        desiredPosition = position;
        outputsChanged = true;
    }

    public void setDesiredState(STATE state) {
        if (this.desiredState != state) {
            this.desiredState = state;
            outputsChanged = true;
        }
    }

    public double getActualOutput() {
        return actualPosition;
    }
    public double getDesiredOutput() {
        return desiredPosition;
    }

    @Override
    public void readFromHardware() {
        actualPosition = hoodMotor.getSelectedSensorPosition(0);
        actualPosition = desiredPosition;
    }

    @Override
    public void writeToHardware() {
        if (outputsChanged) {
            outputsChanged = false;
            switch (desiredState) {
                // add case statements
                case STOP:
                    hoodMotor.set(ControlMode.PercentOutput, 0);
                    break;
                case POSITION:
                    hoodMotor.set(ControlMode.Position, desiredPosition);
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
        POSITION,
    }
}
