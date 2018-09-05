package org.jill.openjill.core.api.entities;

import org.jill.jn.ObjectItem;
import org.jill.openjill.core.api.keyboard.KeyboardLayout;

import java.awt.image.BufferedImage;
import java.util.Optional;

/**
 * Object entity interface.
 *
 * @author Emeric MARTINEAU
 */
public interface ObjectEntity {
    /**
     * Value to left of speed x.
     */
    int X_SPEED_LEFT = -1;

    /**
     * Value to middle of speed x.
     */
    int X_SPEED_MIDDLE = 0;

    /**
     * Value to right of speed x.
     */
    int X_SPEED_RIGHT = 1;

    /**
     * Value to down of speed y.
     */
    int Y_SPEED_DOWN = 1;

    /**
     * Value to middle of speed y.
     */
    int Y_SPEED_MIDDLE = 0;

    /**
     * Value to up of speed y.
     */
    int Y_SPEED_UP = -1;

    /**
     * Default constructor.
     *
     * @param objectParam object parameter
     */
    void init(ObjectParam objectParam);

    /**
     * if always on screen.
     *
     * @return alwaysOnScreen
     */
    boolean isAlwaysOnScreen();

    /**
     * To know if object is checkpoint.
     *
     * @return if is check point
     */
    boolean isCheckPoint();

    /**
     * If object is killable.
     *
     * @return true/false
     */
    boolean isKillableObject();

    /**
     * If player object.
     *
     * @return true/false
     */
    boolean isPlayer();

    /**
     * If player can fire (with standard method).
     *
     * @return tre/false
     */
    boolean canFire();

    /**
     * Return graphic to draw.
     *
     * @return picture
     */
    Optional<BufferedImage> msgDraw(ObjectItem object);

    /**
     * Call when player touch object. (e.g. for lift).
     *
     * @param obj            object
     * @param keyboardLayout keyboard object
     */
    void msgTouch(ObjectItem obj, KeyboardLayout keyboardLayout);

    /**
     * When weapon kill object or ennemy kill player.
     *
     * @param sender      sender
     * @param nbLife      number life to decrease
     * @param typeOfDeath tyep of death (for player)
     */
    void msgKill(ObjectItem sender, int nbLife, int typeOfDeath);

    /**
     * When background kill object.
     *
     * @param sender      sender
     * @param nbLife      number life to decrease
     * @param typeOfDeath tyep of death (for player)
     */
    void msgKill(BackgroundEntity sender, int nbLife, int typeOfDeath);

    /**
     * Call to update.
     *
     * @param keyboardLayout keyboard object
     */
    void msgUpdate(KeyboardLayout keyboardLayout, ObjectItem object);

    /**
     * Remove object if out of visible screen.
     *
     * @return true -> remove it
     */
    boolean isRemoveOutOfVisibleScreen();

    /**
     * Return default graphic to draw.
     *
     * @return picture
     */
    Optional<BufferedImage> defaultPicture();
}
