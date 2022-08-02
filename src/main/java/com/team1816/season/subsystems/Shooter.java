package com.team1816.season.subsystems;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.google.inject.Singleton;
import com.team1816.lib.hardware.PIDSlotConfiguration;
import com.team1816.lib.hardware.components.motor.IGreenMotor;
import com.team1816.lib.subsystems.PidProvider;
import com.team1816.lib.subsystems.Subsystem;
import com.team1816.lib.util.EnhancedMotorChecker;
import com.team1816.season.Constants;
import edu.wpi.first.util.sendable.SendableBuilder;


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

    public static final double NEAR_VELOCITY = (int) factory.getConstant(NAME, "nearVel");

    public static final double MID_VELOCITY = (int) factory.getConstant(NAME, "midVel");

    public static final double TARMAC_TAPE_VEL = (int) factory.getConstant(NAME,"tarmacTapeVel");

    public static final double LAUNCHPAD_VEL = (int) factory.getConstant(NAME,"maxVel");

    public static final double MAX_VELOCITY = (int) factory.getConstant(NAME, "maxVel");

    public static final double COAST_VELOCITY = (int) factory.getConstant(NAME,"coastVel");

    public final int VELOCITY_THRESHOLD;


    public Shooter(double desiredOutput) {
        super(NAME);
        this.desiredOutput = desiredOutput;
        shooterMotor = factory.getMotor(NAME, "shooterMotor");
        shooterMotor.setNeutralMode(NeutralMode.Coast);
        shooterMotor.configClosedloopRamp(0.5, Constants.kCANTimeoutMs);
        shooterMotor.setSensorPhase(false);
        configCurrentLimits(40/* amps */);

        pidConfig = factory.getPidSlotConfig(NAME);
        VELOCITY_THRESHOLD = pidConfig.allowableError.intValue();
    }

    private void configCurrentLimits(int currentLimitAmps) {
        shooterMotor.configSupplyCurrentLimit(
            new SupplyCurrentLimitConfiguration(true, currentLimitAmps,0,0),
            Constants.kCANTimeoutMs
        );
    }

    public double getActualOutput() {
        return actualOutput;
    }
    public double getDesiredOutput() {
        return desiredOutput;
    }

    @Override
    public PIDSlotConfiguration getPidConfig() {return pidConfig; }

    public double getactualOutput() {return actualOutput; }

    public double getTargetOutput() {return desiredOutput; }

    public double getError() {return Math.abs(actualOutput - desiredOutput); }

    public void setOutput(double output) {
        desiredOutput = output;
        shooterMotor.set(ControlMode.Velocity,desiredOutput);
    }

    public void setDesiredState(STATE state) {
        desiredState = state;
        outputsChanged = true;
        }

    public boolean isOutputNearTarget() {
        if (!isImplemented()) {
            return true;
        }
        return (
            Math.abs(desiredOutput - actualOutput) < VELOCITY_THRESHOLD &&
                desiredState != STATE.COASTING
            );
    }



    @Override
    public void readFromHardware() {
         actualOutput = shooterMotor.getSelectedSensorVelocity(0);

         robotState.shooterMPS = convertShooterTicksToMetersPerSecond(actualOutput);

         if (desiredState != robotState.shooterSTATE) {
             if (actualOutput < VELOCITY_THRESHOLD) {
                 robotState.shooterSTATE = STATE.STOP;
             } else if (isOutputNearTarget()) {
                 robotState.shooterSTATE = STATE.REVVING;
             } else {
                 robotState.shooterSTATE = STATE.COASTING;
             }
         }
    }

    @Override
    public void writeToHardware() {
        if (outputsChanged) {
            outputsChanged = false;
            switch (desiredState) {
                // add case statements
                case STOP:
                    setOutput(0);
                    break;
                case REVVING:
                    break;
                case COASTING:
                    setOutput(COAST_VELOCITY);
                    break;
            }
        }
    }

    public double convertShooterTicksToMetersPerSecond(double ticks) {
        return 0.00019527 * ticks; //verify conversion?
    }

    public double convertShooterMetersToTicksPerSecond(double metersPerSecond) {
        return (metersPerSecond + 0.53) / 0.0248;
    }

   @Override
   public void initSendable(SendableBuilder builder) {}

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
        COASTING,
        REVVING,
        CAMERA,
    }
}


