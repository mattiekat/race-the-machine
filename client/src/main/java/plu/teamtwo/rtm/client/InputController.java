package plu.teamtwo.rtm.client;

import plu.teamtwo.rtm.core.util.*;
import plu.teamtwo.rtm.core.util.Point;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

public class InputController {

    private static InputController instance = null;
    public static InputController getInstance() { return instance; }

    public static void init(GraphicsDevice screen) {
        instance = new InputController(screen);
    }

    public enum Key {
        LEFT(KeyEvent.VK_LEFT),
        RIGHT(KeyEvent.VK_RIGHT),
        SPACE(KeyEvent.VK_SPACE);

        public final int keycode;
        Key(int keycode) {
            this.keycode = keycode;
        }
    }

    protected final boolean[] pressedKeys = new boolean[Key.values().length];
    protected final Robot robot;

    protected boolean gameRunning = false;

    protected Point startButtonPosition;

    protected int score = 0;
    protected long lastScoreUpdate = -1;

    private InputController(GraphicsDevice screen) {
        try {
            robot = new Robot(screen);
        } catch(Exception ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    public void setPressed(Key key, boolean pressed) {
        pressedKeys[key.ordinal()] = pressed;
    }

    public void updateInputs() {
        for(Key key : Key.values()) {
            if(pressedKeys[key.ordinal()])
                robot.keyPress(key.keycode);
            else
                robot.keyRelease(key.keycode);
        }
    }

    public void resetInputs() {
        for(Key key : Key.values())
            pressedKeys[key.ordinal()] = false;
    }

    public void setStartButtonPosition(Point position) {
        this.startButtonPosition = position;
    }

    public void startGame() {
        robot.mouseMove(startButtonPosition.x.intValue(), startButtonPosition.y.intValue());
        robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        gameRunning = true;
        lastScoreUpdate = System.currentTimeMillis();
    }

    public boolean isGameRunning() {
        return gameRunning;
    }

    public int getScore() {
        return score;
    }

    public void updateScore(int score) {
        if(!gameRunning) return;
        long ct = System.currentTimeMillis();
        if(score < 0) {
            if((ct - lastScoreUpdate) > 8000) {
                gameRunning = false;
                resetInputs();
                updateInputs();
            }
        } else {
            this.score = score;
            lastScoreUpdate = ct;
        }
    }


}
