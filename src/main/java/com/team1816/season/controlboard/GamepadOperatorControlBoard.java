package com.team1816.season.controlboard;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.team1816.lib.controlboard.Controller;
import com.team1816.lib.controlboard.IOperatorControlBoard;
import com.team1816.season.Constants;

/*
    operator controller (xbox, logitech, or keyboard) -
    what method names (ie setRumble, getShoot) correspond to what button / trigger / joystick values
 */
@Singleton
public class GamepadOperatorControlBoard implements IOperatorControlBoard {

    private final Controller mController;

    @Inject
    private GamepadOperatorControlBoard(Controller.Factory controller) {
        mController = controller.getControllerInstance(Constants.kOperatorGamepadPort);
        reset();
    }

    @Override
    public void setRumble(boolean on) {
        mController.setRumble(on);
    }

    @Override
    public void reset() {}
}
