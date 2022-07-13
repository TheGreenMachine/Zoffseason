package com.team1816.lib.controlboard;

public interface IDriveControlBoard {
    double getThrottle();

    double getTurn();

    double getStrafe();

    boolean getBrakeMode();

    boolean getSlowMode();

    boolean getZeroPose();

    boolean getQuickTurnMode();

    double getDPad();

    boolean getFieldRelative();
}
