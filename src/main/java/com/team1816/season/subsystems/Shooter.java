package com.team1816.season.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.team1816.lib.hardware.components.motor.IGreenMotor;
import com.team1816.lib.subsystems.Subsystem;

public class Shooter extends Subsystem {
    private static final String NAME = "shooter";

    private IGreenMotor shooterMotor;

    private STATE desired;
    private STATE actual;

    //d means desired, a means actual
    private double dVelocity = 0;
    private double aVelocity = 0;

    public boolean outputsChanged = false;

    public Shooter(){
        super(NAME);
        shooterMotor = factory.getMotor(NAME, "shooterMotor");
        desired = STATE.IDLE;
        actual = STATE.IDLE;
    }

    public void setDesiredState(STATE s){
        desired = s;
    }

    public void setVelocity(int v){ dVelocity = v; }

    public boolean isVelocityNearTarget(){
        return (dVelocity-aVelocity <= 50);
    }

    public void shoot(){
        setDesiredState(STATE.SHOOTING);
        setVelocity(50000);
        shooterMotor.set(ControlMode.Velocity, dVelocity);
    }
    public void shoot(int motorSpeed){
        setDesiredState(STATE.SHOOTING);
        setVelocity(motorSpeed);
        shooterMotor.set(ControlMode.Velocity, dVelocity);
    }

    public void updateState(){
        if(aVelocity == 0)
            setActualState(STATE.IDLE);
        else if(!(isVelocityNearTarget()))
            setActualState(STATE.REVVING);
        else if(isVelocityNearTarget()){
            setActualState(STATE.SHOOTING);
        }
    }


    @Override
    public void readFromHardware() {
    aVelocity = shooterMotor.getSelectedSensorVelocity(0);
    outputsChanged = (outputsChanged && Math.abs(shooterMotor.getSelectedSensorVelocity(0))>0);
    updateState();
    }

    @Override
    public void writeToHardware() {
        switch(desired){
            case IDLE: break;
            case REVVING: shoot();
        }
    }

    @Override
    public void stop() {

    }

    @Override
    public boolean checkSystem() {
        return false;
    }

    public void setActualState(STATE s){
        actual = s;
    }

    public enum STATE {
        IDLE,
        SHOOTING,
        REVVING,
    }

}
