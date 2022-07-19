package com.team1816.season;

import com.google.inject.AbstractModule;
import com.team1816.lib.SeasonFactory;
import com.team1816.lib.controlboard.IControlBoard;
import com.team1816.lib.controlboard.IDriveControlBoard;
import com.team1816.lib.controlboard.IOperatorControlBoard;
import com.team1816.lib.math.SwerveKinematics;
import com.team1816.lib.subsystems.Drive;
import com.team1816.lib.subsystems.SwerveDrive;
import com.team1816.lib.subsystems.TankDrive;
import com.team1816.season.auto.actions.*;
import com.team1816.season.controlboard.ControlBoard;
import com.team1816.season.controlboard.GamepadDriveControlBoard;
import com.team1816.season.controlboard.GamepadOperatorControlBoard;

public class SeasonModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Drive.Factory.class).to(SeasonFactory.class);
        bind(IControlBoard.class).to(ControlBoard.class);
        bind(IDriveControlBoard.class).to(GamepadDriveControlBoard.class);
        bind(IOperatorControlBoard.class).to(GamepadOperatorControlBoard.class);
        requestStaticInjection(SwerveKinematics.class);
        requestStaticInjection(Drive.class);
        requestStaticInjection(TankDrive.class);
        requestStaticInjection(SwerveDrive.class);
    }
}
