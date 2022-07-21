package com.team1816.season.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.team1816.lib.hardware.components.motor.IGreenMotor;
import com.team1816.lib.subsystems.Subsystem;

public class Elevator extends Subsystem {

    public static final String NAME = "elevator";
    private IGreenMotor spinnyMotor;

    private boolean outputsChanged;
    private double desiredPosition;
    private double actualPosition;
    private STATE desiredState;
    private STATE actualState;
    private final double incrementAmt;

    public Elevator() {
        super(NAME);
        spinnyMotor = factory.getMotor(NAME, "spinnyMotor");
        outputsChanged = false;
        incrementAmt = factory.getConstant(NAME, "incrementAmt", 8000);
    }

    public void dropFrisbee(){
        desiredPosition += incrementAmt;
        desiredState = STATE.MOVING;
        outputsChanged = true;
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean checkSystem() {
        return false;
    }

    public void readFromHardware(){
        actualPosition = spinnyMotor.getSelectedSensorPosition(0);
        if(actualState != desiredState){
            if(actualPosition == desiredPosition){
                actualState = STATE.STOP;
            }
            else
                actualState = STATE.MOVING;
        }

    }

    public void writeToHardware(){
        if(outputsChanged){
            outputsChanged = false;
            spinnyMotor.set(ControlMode.Position, desiredPosition);
        }
    }

    public enum STATE {
        STOP,
        MOVING,
    }
}
