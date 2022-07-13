package com.team1816.season.controlboard;

import com.google.inject.Inject;
import com.team1816.lib.controlboard.IControlBoard;
import com.team1816.lib.controlboard.IDriveControlBoard;
import com.team1816.lib.controlboard.IOperatorControlBoard;

public class ControlBoard implements IControlBoard {

    private final IDriveControlBoard mDriveControlBoard;
    private final IOperatorControlBoard mButtonControlBoard;

    @Inject
    private ControlBoard(
        IDriveControlBoard driveControlBoard,
        IOperatorControlBoard buttonControlBoard
    ) {
        mDriveControlBoard = driveControlBoard;
        mButtonControlBoard = buttonControlBoard;
    }

    @Override
    public void reset() {}

    // Drive Control Board
    @Override
    public double getThrottle() {
        return mDriveControlBoard.getThrottle();
    }

    @Override
    public double getTurn() {
        return mDriveControlBoard.getTurn();
    }

    @Override
    public double getStrafe() {
        return -mDriveControlBoard.getStrafe();
    }

    @Override
    public boolean getBrakeMode() {
        return mDriveControlBoard.getBrakeMode();
    }

    @Override
    public boolean getSlowMode() {
        return mDriveControlBoard.getSlowMode();
    }

    @Override
    public boolean getZeroPose() {
        return mDriveControlBoard.getZeroPose();
    }

    @Override
    public boolean getQuickTurnMode() {
        return false;
    }

    // Button Control Board
    @Override
    public void setRumble(boolean on) {
        mButtonControlBoard.setRumble(on);
    }

    @Override
    public double getDPad() {
        return mDriveControlBoard.getDPad();
    }

    @Override
    public boolean getFieldRelative() {
        return mDriveControlBoard.getFieldRelative();
    }
}
