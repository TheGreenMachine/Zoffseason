package com.team1816.lib.subsystems.drive;

import static com.team1816.lib.subsystems.drive.Drive.NAME;
import static com.team1816.lib.subsystems.drive.Drive.factory;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.SupplyCurrentLimitConfiguration;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;
import com.ctre.phoenix.sensors.CANCoder;
import com.team1816.lib.hardware.PIDSlotConfiguration;
import com.team1816.lib.hardware.components.motor.IGreenMotor;
import com.team1816.lib.math.DriveConversions;
import com.team1816.lib.math.SwerveKinematics;
import com.team1816.season.Constants;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.Timer;

public class SwerveModule implements ISwerveModule {

    // Components
    private final IGreenMotor driveMotor;
    private final IGreenMotor azimuthMotor; // angle motor (the pivot part of a shopping cart except motorized)
    public final CANCoder canCoder;

    // State
    public double driveDemand;
    public double driveActual;
    public double azimuthDemand;
    public double azimuthActual;
    public double motorTemp; // drive motor temperature

    // Constants
    private final ModuleConfig mModuleConfig;
    private final int AZIMUTH_TICK_MASK;
    private final double allowableError;

    public SwerveModule(
        String subsystemName,
        ModuleConfig moduleConfig,
        CANCoder canCoder
    ) {
        mModuleConfig = moduleConfig;

        System.out.println(
            "Configuring Swerve Module " +
            mModuleConfig.moduleName +
            " on subsystem " +
            subsystemName
        );

        AZIMUTH_TICK_MASK = (int) factory.getConstant(NAME, "azimuthEncPPR", 4096) - 1; // was 0xFFF

        /* Drive Motor Config */
        driveMotor =
            factory.getMotor(
                subsystemName,
                mModuleConfig.driveMotorName,
                factory.getSubsystem(subsystemName).swerveModules.drivePID,
                -1
            );

        /* Azimuth (Angle) Motor Config */
        azimuthMotor =
            factory.getMotor(
                subsystemName,
                mModuleConfig.azimuthMotorName,
                factory.getSubsystem(subsystemName).swerveModules.azimuthPID,
                canCoder == null ? -1 : canCoder.getDeviceID()
            );

        driveMotor.configOpenloopRamp(0.25, Constants.kCANTimeoutMs);
        azimuthMotor.configSupplyCurrentLimit(
            new SupplyCurrentLimitConfiguration(true, 18, 28, 1),
            Constants.kLongCANTimeoutMs
        );

        azimuthMotor.configPeakOutputForward(.4, Constants.kLongCANTimeoutMs);
        azimuthMotor.configPeakOutputReverse(-.4, Constants.kLongCANTimeoutMs);

        azimuthMotor.setNeutralMode(NeutralMode.Brake);

        azimuthMotor.configAllowableClosedloopError(
            0,
            mModuleConfig.azimuthPid.allowableError,
            Constants.kLongCANTimeoutMs
        );

        allowableError = 5; // TODO this is a dummy value for checkSystem

        /* Angle Encoder Config */
        this.canCoder = canCoder;

        System.out.println("  " + this);
    }

    public void setDesiredState(SwerveModuleState desiredState, boolean isOpenLoop) {
        SwerveModuleState desired_state = SwerveKinematics.optimize(
            desiredState,
            getActualState().angle
        );
        driveDemand =
            DriveConversions.metersPerSecondToTicksPer100ms(
                desired_state.speedMetersPerSecond
            );
        if (!isOpenLoop) {
            driveMotor.set(ControlMode.Velocity, driveDemand);
        } else {
            driveMotor.set(ControlMode.PercentOutput, desired_state.speedMetersPerSecond); // lying to it - speedMetersPerSecond passed in is actually percent output (1 to -1)
        }
        azimuthDemand =
            DriveConversions.convertDegreesToTicks(desired_state.angle.getDegrees()) +
            mModuleConfig.azimuthEncoderHomeOffset;
        azimuthMotor.set(ControlMode.Position, azimuthDemand);
    }

    public SwerveModuleState getActualState() {
        driveActual =
            DriveConversions.ticksToMeters(driveMotor.getSelectedSensorVelocity(0)) * 10;

        azimuthActual =
            DriveConversions.convertTicksToDegrees(
                azimuthMotor.getSelectedSensorPosition(0) -
                mModuleConfig.azimuthEncoderHomeOffset
            );

        Rotation2d angleActual = Rotation2d.fromDegrees(azimuthActual);
        motorTemp = driveMotor.getTemperature(); // Celsius
        return new SwerveModuleState(driveActual, angleActual);
    }

    @Override
    public double getMotorTemp() {
        return motorTemp;
    }

    @Override
    public String getModuleName() {
        return mModuleConfig.moduleName;
    }

    @Override
    public double getDesiredAzimuth() {
        return azimuthDemand;
    }

    @Override
    public double getActualAzimuth() {
        return azimuthActual;
    }

    @Override
    public double getAzimuthError() {
        return azimuthMotor.getClosedLoopError(0);
    }

    @Override
    public double getDesiredDrive() {
        return driveDemand;
    }

    @Override
    public double getActualDrive() {
        return driveActual;
    }

    @Override
    public double getDriveError() {
        return driveMotor.getClosedLoopError(0);
    }

    public void zeroAzimuthSensor() {
        if (azimuthMotor instanceof TalonSRX && canCoder == null) {
            var sensors = ((TalonSRX) azimuthMotor).getSensorCollection();
            sensors.setQuadraturePosition(
                sensors.getPulseWidthPosition() % AZIMUTH_TICK_MASK,
                Constants.kLongCANTimeoutMs
            );
        }
    }

    public boolean checkSystem() {
        boolean checkDrive = true;
        double actualmaxVelTicks100ms = factory.getConstant(NAME, "maxVelTicks100ms"); // if this isn't calculated right this test will fail
        driveMotor.set(ControlMode.PercentOutput, 0.2);
        Timer.delay(1);
        if (
            Math.abs(
                driveMotor.getSelectedSensorVelocity(0) - 0.2 * actualmaxVelTicks100ms
            ) >
            actualmaxVelTicks100ms /
            50
        ) {
            checkDrive = false;
        }
        driveMotor.set(ControlMode.PercentOutput, -0.2);
        Timer.delay(1);
        if (
            Math.abs(
                driveMotor.getSelectedSensorVelocity(0) + 0.2 * actualmaxVelTicks100ms
            ) >
            actualmaxVelTicks100ms /
            50
        ) {
            checkDrive = false;
        }

        boolean checkAzimuth = true;
        double setPoint = mModuleConfig.azimuthEncoderHomeOffset;
        Timer.delay(1);
        for (int i = 0; i < 4; i++) {
            azimuthMotor.set(ControlMode.Position, setPoint);
            Timer.delay(1);
            if (
                Math.abs(azimuthMotor.getSelectedSensorPosition(0) - setPoint) >
                allowableError
            ) {
                checkAzimuth = false;
                break;
            }
            setPoint += DriveConversions.convertRadiansToTicks(Math.PI / 2);
        }

        return checkDrive && checkAzimuth;
    }

    @Override
    public String toString() {
        return (
            "SwerveModule{ " +
            mModuleConfig.driveMotorName +
            " id: " +
            driveMotor.getDeviceID() +
            "  " +
            mModuleConfig.azimuthMotorName +
            " id: " +
            azimuthMotor.getDeviceID() +
            " offset: " +
            mModuleConfig.azimuthEncoderHomeOffset
        );
    }

    public static class ModuleConfig {

        public ModuleConfig() {}

        public String moduleName = "Name";
        public String driveMotorName = "";
        public String azimuthMotorName = "";

        public PIDSlotConfiguration azimuthPid;
        public PIDSlotConfiguration drivePid;

        // constants defined for each swerve module
        public double azimuthEncoderHomeOffset;
        public static final int kAzimuthPPR = (int) factory.getConstant(
            "drive",
            "azimuthEncPPR",
            4096
        );
    }
}