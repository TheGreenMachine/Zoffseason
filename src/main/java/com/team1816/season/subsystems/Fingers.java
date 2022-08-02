package com.team1816.season.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.team1816.lib.hardware.components.motor.IGreenMotor;
import com.team1816.lib.subsystems.Subsystem;

import javax.inject.Singleton;

@Singleton
public class Fingers extends Subsystem {
    private static final String NAME = "fingers";

    private IGreenMotor fingA;
    private IGreenMotor fingB;

    private STATE desired;
    private STATE actual;

    private int thenarWebspace = 0;
    private int fingCount = 2;

    //d means desired, a means actual
    private double dPositionA = 0;
    private double dPositionB = 0;
    private double aPositionA = 0;
    private double aPositionB = 0;

    public boolean outputsChanged = false;


    public Fingers(){
        super(NAME);
        fingA = factory.getMotor(NAME, "fingA");
        fingB = factory.getMotor(NAME, "fingB");
        desired = STATE.IDLE;
        actual = STATE.IDLE;
        thenarWebspace = (int)factory.getConstant(NAME, "wideness");
        fingCount = (int)factory.getConstant(NAME, "fings");
    }

    public void setFingersPosition(double position) {
        dPositionA = position;
        dPositionB = position;
        fingA.set(ControlMode.Position, dPositionA);
        fingB.set(ControlMode.Position, dPositionB);
    }

    public void incrementFingers(){
        setFingersPosition(aPositionA + thenarWebspace);
    }

    public void setDesiredState(STATE s){
        desired = s;
    }

    @Override
    public void readFromHardware() {
        aPositionA = fingA.getSelectedSensorPosition(0);
        aPositionB = fingB.getSelectedSensorPosition(0);
        outputsChanged = (outputsChanged && Math.abs(fingA.getSelectedSensorVelocity(0))>0 && dPositionA-aPositionA <= .1*thenarWebspace);
    }

    @Override
    public void writeToHardware() {
        switch(desired) {
            case IDLE: break;
            case DROPPING: incrementFingers();
            break;
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
        IDLE,
        DROPPING,
    }
}
