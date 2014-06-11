package org.jill.game.level;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jill.game.gui.menu.ClassicMenu;
import org.jill.game.gui.menu.MenuInterface;
import org.jill.game.level.cfg.LevelConfiguration;
import org.jill.openjill.core.api.message.statusbar.inventory.EnumInventoryObject;
import org.jill.openjill.core.api.message.statusbar.inventory.InventoryItemMessage;
import org.jill.game.screen.ControlArea;
import org.jill.game.screen.InventoryArea;
import org.jill.jn.BackgroundLayer;
import org.jill.openjill.core.api.entities.BackgroundEntity;
import org.jill.openjill.core.api.entities.ObjectEntity;
import org.jill.openjill.core.api.jill.JillConst;
import org.jill.openjill.core.api.keyboard.KeyboardLayout;
import org.jill.openjill.core.api.message.EnumMessageType;
import org.simplegame.InterfaceSimpleGameHandleInterface;

/**
 * This class manage all of execution method of game (run, pause, cheat, key,
 * update background, update object...).
 *
 * @author Emeric MARTINEAU
 */
public abstract class AbstractExecutingStdLevel extends AbstractMenuJillLevel {
    /**
     * Number of pressed key to activate cheat code.
     */
    private static final int CHEAT_CODE_NUMBER = 3;

    /**
     * Turtle swith.
     */
    protected boolean turtleSwitch = false;

    /**
     * Load game.
     */
    protected MenuInterface menuStd;

    /**
     * Control area.
     */
    protected  ControlArea controlArea;

    /**
     * Inventory area.
     */
    protected InventoryArea inventoryArea;

    /**
     * Screen to draw.
     */
    protected BufferedImage drawingScreen;

    /**
     * G2 for drawing screen.
     */
    protected Graphics2D g2DrawingScreen;

    /**
     * Screen rectangle.
     */
    protected Rectangle screenRect;

    /**
     * Object rectangle (only for optimization).
     */
    protected Rectangle objRect = new Rectangle();

    /**
     * Player rectangle (only for optimization).
     */
    protected Rectangle playerRect = new Rectangle();

    /**
     * If need update inventory screen.
     */
    protected boolean updateInventoryScreen = false;

    /**
     * Keyboard object share between object (include player).
     */
    protected KeyboardLayout keyboardLayout;

    /**
     * Object to draw.
     */
    protected List<ObjectEntity> listObjectToDraw = new ArrayList<>();

    /**
     * Cheat count.
     */
    protected int cheatCount;

    /**
     * Cheat count for gem.
     */
    protected int gemCheatCount;

    /**
     * Cheat for high jump.
     */
    protected int highJumpCheatCount;

    /**
     * True if player invicible.
     */
    protected boolean invincibility;

    /**
     * False if pause game.
     */
    protected boolean runGame;

    /**
     * Current list of oject currently on screen.
     */
    private final List<ObjectEntity> listObjectCurrentlyDisplayedOnScreen =
            new ArrayList<>();

    /**
     * Level configuration.
     *
     * @param cfgLevel  configuration of level
     *
     * @throws IOException if error of reading file
     * @throws ClassNotFoundException if not class found
     * @throws IllegalAccessException if error
     * @throws InstantiationException  if error
     */
    public AbstractExecutingStdLevel(final LevelConfiguration cfgLevel)
            throws IOException, ClassNotFoundException,
            IllegalAccessException, InstantiationException {
        super(cfgLevel);

        constructor();
    }

    /**
     * Construct this object.
     */
    private void constructor() {
        keyboardLayout = new KeyboardLayout();

        controlArea = new ControlArea(pictureCache, statusBar);

        inventoryArea = new InventoryArea(pictureCache, statusBar,
                this.messageDispatcher);

        inventoryArea.setLevel(
                this.levelConfiguration.getLevelNumber());

        messageDispatcher.addHandler(EnumMessageType.INVENTORY_ITEM,
                inventoryArea);
        messageDispatcher.addHandler(EnumMessageType.INVENTORY_LIFE,
                inventoryArea);
        messageDispatcher.addHandler(EnumMessageType.INVENTORY_POINT,
                inventoryArea);

        messageDispatcher.addHandler(EnumMessageType.INVENTORY_ITEM,
                controlArea);

        offsetX = 0;
        offsetY = 0;

        drawingScreen = createGameScreen();

        g2DrawingScreen = drawingScreen.createGraphics();

        messageDispatcher.addHandler(EnumMessageType.INVENTORY_ITEM,
                this);
        messageDispatcher.addHandler(EnumMessageType.INVENTORY_LIFE,
                this);
        messageDispatcher.addHandler(EnumMessageType.INVENTORY_POINT,
                this);

        cheatCount = 0;

        gemCheatCount = 0;

        highJumpCheatCount = 0;

        invincibility = false;

        runGame = true;

        // TODO explain why '* 2' ????
        screenRect = new Rectangle(0, 0,
            this.statusBar.getGameAreaConf().getWidth()
                    + JillConst.X_UPDATE_SCREEN_BORDER * 2,
            this.statusBar.getGameAreaConf().getHeight()
                    + JillConst.Y_UPDATE_SCREEN_BORDER * 2);
    }

    /**
     * Create game screen for draw background and object.
     *
     * @return picture
     */
    protected BufferedImage createGameScreen() {
        return statusBar.createGameScreen();
    }

    /**
     * Draw control.
     */
    protected final void drawControl() {
        statusBar.drawControl(controlArea.drawControl());
    }

    /**
     * Draw inventory & control area.
     */
    protected final void drawInventory() {
        statusBar.drawInventory(inventoryArea.drawInventory());
        statusBar.drawControl(controlArea.drawControl());
        updateInventoryScreen = false;
    }

    @Override
    protected void doRun() {
        if (keyboard.isOtherKey()) {
            switch (keyboard.consumeOtherKey()) {
                case 'p' :
                case 'P' :
                    runGame = !runGame;
                    break;
                case 'n' :
                case 'N' :
                    // TODO noise
                    break;
                case 'q' :
                case 'Q' :
                    menu.setEnable(true);
                    return;
                case 's' :
                case 'S' :
                    saveGame();
                    break;
                case 'r' :
                case 'R' :
                    loadGame();
                    break;
                case 't' :
                case 'T' :
                    controlArea.setTurtleMode(!controlArea.isTurtleMode());
                    drawControl();
                    break;
                case 'x' :
                case 'X' :
                    if (invincibility) {
                        break;
                    }

                    cheatCount++;

                    if (cheatCount == CHEAT_CODE_NUMBER) {
                        messageDispatcher.sendMessage(
                                EnumMessageType.INVENTORY_ITEM,
                                new InventoryItemMessage(
                                    EnumInventoryObject.INVINCIBILITY, true));
                        messageDispatcher.sendMessage(
                                     EnumMessageType.INVENTORY_ITEM,
                                     new InventoryItemMessage(
                                         EnumInventoryObject.RED_KEY, true));

                        cheatCount = 0;

                        invincibility = true;
                    }
                    break;
                case 'g' :
                case 'G' :
                    gemCheatCount++;

                    if (gemCheatCount == CHEAT_CODE_NUMBER) {
                        messageDispatcher.sendMessage(
                            EnumMessageType.INVENTORY_ITEM,
                                     new InventoryItemMessage(
                                         EnumInventoryObject.GEM, true));
                        gemCheatCount = 0;
                    }
                    break;
                case 'h' :
                case 'H' :
                    highJumpCheatCount++;

                    if (highJumpCheatCount == CHEAT_CODE_NUMBER) {
                        messageDispatcher.sendMessage(
                            EnumMessageType.INVENTORY_ITEM,
                                     new InventoryItemMessage(
                                         EnumInventoryObject.HIGH_JUMP, true));
                        highJumpCheatCount = 0;
                    }
                    break;
                default:
            }
        }

        if (keyboard.isUp()) {
            //offsetY += JillConst.BLOCK_SIZE;
            keyboardLayout.setUp(true);
        }

        if (keyboard.isDown()) {
            //offsetY -= JillConst.BLOCK_SIZE;
            keyboardLayout.setDown(true);
        }

        if (keyboard.isRight()) {
            //offsetX -= JillConst.BLOCK_SIZE;
            keyboardLayout.setRight(true);
        }

        if (keyboard.isLeft()) {
            //offsetX += JillConst.BLOCK_SIZE;
            keyboardLayout.setLeft(true);
        }

        if (keyboard.isFire()) {
            keyboardLayout.setFire(true);
        }

        if (keyboard.isJump()) {
            keyboardLayout.setJump(true);
        }

        currentDisplayScreen = statusBar.getStatusBar();

        if (runGame) {
            doRunNext();
        }
    }

    /**
     * Run.
     */
    protected void doRunNext() {
        boolean turtleMode = controlArea.isTurtleMode();

        if ((turtleMode && turtleSwitch) || !turtleMode) {
            // Execute if not in turtle mode or turtle mode and execute cycle

            if (keyboardLayout.isFire()) {
                doPlayerFire();
            }

            // Update background
            updateBackground();

            // Draw object list
            updateObject();

            // Move player
            movePlayer();

            if (updateInventoryScreen) {
                drawInventory();
            }

            // Copy background screen on
            g2DrawingScreen.drawImage(background, offsetX, offsetY, null);

            // Draw object
            drawObject();

            // Draw background
            statusBar.drawGameScreen(drawingScreen);
        }

        turtleSwitch = !turtleSwitch;
    }

    /**
     * Draw object if displayed.
     */
    private void updateObject() {
        int lOffsetX = Math.abs(offsetX);
        int lOffsetY = Math.abs(offsetY);

        screenRect.setLocation(lOffsetX - JillConst.X_UPDATE_SCREEN_BORDER,
                lOffsetY - JillConst.Y_UPDATE_SCREEN_BORDER);

        // Set player bounds
        playerRect.setBounds(player.getX(), player.getY(), player.getWidth(),
                player.getHeight());

        // Grap list of object on screen
        for (ObjectEntity obj : listObject) {
            objRect.setBounds(obj.getX(), obj.getY(), obj.getWidth(),
                    obj.getHeight());

            if (screenRect.intersects(objRect)) {
                listObjectCurrentlyDisplayedOnScreen.add(obj);
            }
        }

        listObjectCurrentlyDisplayedOnScreen.add(player);

        int zaphold;

        // Update and object touch
        for (ObjectEntity obj1 : listObjectCurrentlyDisplayedOnScreen) {
            objRect.setBounds(obj1.getX(), obj1.getY(), obj1.getWidth(),
                    obj1.getHeight());

            // Decreate touch player flag
            zaphold = obj1.getZapHold();

            if (zaphold > 0) {
                obj1.setZapHold(zaphold - 1);
            }

            obj1.msgUpdate();

            listObjectToDraw.add(obj1);

            for (ObjectEntity obj2 : listObjectCurrentlyDisplayedOnScreen) {
                playerRect.setBounds(obj2.getX(), obj2.getY(),
                    obj2.getWidth(), obj2.getHeight());

                // Check object collision.
                // Skip if same object
                // Skip if obj1 is player because, msgKeyboard call after and
                // if don't skip when object collision state of player update
                // twice
                if (obj1 != obj2 && obj1 != player
                        && playerRect.intersects(objRect)) {
                    obj1.msgKeyboard(obj2, keyboardLayout);
                    obj1.msgTouch(obj2);
                }
            }
        }

        // Reset last object. Check if need redraw inventory for backcolor.
        this.updateInventoryScreen = this.inventoryArea.isNeedRedraw()
                || this.updateInventoryScreen;

        listObjectCurrentlyDisplayedOnScreen.clear();

        // Call manually to update
        player.msgKeyboard(player, keyboardLayout);

        // Remove object from list
        for (ObjectEntity obj : listObjectToRemove) {
            listObject.remove(obj);
        }

        listObjectToRemove.clear();

        // Add object from list
        for (ObjectEntity obj : listObjectToAdd) {
            listObject.add(obj);
        }

        listObjectToAdd.clear();
    }

    /**
     * Update background.
     */
    private void updateBackground() {
        final int startX = Math.abs(offsetX) / JillConst.BLOCK_SIZE;
        final int startY = Math.abs(offsetY) / JillConst.BLOCK_SIZE;
        final int endX = Math.min(startX + screenWidthBlock,
                BackgroundLayer.MAP_WIDTH);
        final int endY = Math.min(startY + screenHeightBlock,
                BackgroundLayer.MAP_HEIGHT);

        BackgroundEntity back;

        BufferedImage tilePicture;

        for (int indexBackX = startX; indexBackX < endX; indexBackX++) {
            for (int indexBackY = startY; indexBackY < endY; indexBackY++) {
                back = backgroundObject[indexBackX][indexBackY];

                if (back.isMsgUpdate()) {
                    back.msgUpdate();
                    tilePicture = back.getPicture();

                    g2Background.drawImage(tilePicture,
                            indexBackX * JillConst.BLOCK_SIZE,
                            indexBackY * JillConst.BLOCK_SIZE, null);
                }
            }
        }
    }

    /**
     * Draw objet always on screen.
     */
    private void drawObject() {
        for (ObjectEntity currentObject : listObjectAlwaysOnScreen) {
            g2DrawingScreen.drawImage(currentObject.msgDraw(),
                    currentObject.getX(), currentObject.getY(), null);
        }

        for (ObjectEntity currentObject : listObjectToDraw) {
            g2DrawingScreen.drawImage(currentObject.msgDraw(),
                    currentObject.getX() + offsetX,
                    currentObject.getY() + offsetY, null);
        }

        // NOTE : Disable in 0.0.28 cause, new collision method do this
        //g2DrawingScreen.drawImage(player.msgDraw(), player.getX() + offsetX,
        //        player.getY() + offsetY, null);

        listObjectToDraw.clear();
    }

    @Override
    protected void initMenu() {
        this.menuStd = new ClassicMenu("exit_menu.json", pictureCache);
        this.menu = menuStd;
    }

    @Override
    protected void menuEntryValidate(final int value) {
        if (value == 0) {
            doMenuValidate();
        } else {
            menu.setEnable(false);
        }
    }

    @Override
    protected void doEscape() {
        menu.setEnable(true);
        keyboard.unescape();
    }

    /**
     * Return start screen class.
     *
     * @return class
     */
    protected Class<?
            extends InterfaceSimpleGameHandleInterface> getStartScreenClass() {
        return this.levelConfiguration.getStartScreen();
    }

    /**
     * Move player.
     */
    protected abstract void movePlayer();

    @Override
    public void recieveMessage(final EnumMessageType type, final Object msg) {
        super.recieveMessage(type, msg);

//        if (type == EnumMessageType.INVENTORY) {
//            // Catch call to know if need update inventory screen
//            // Use boolean to ensure that all add/remove item are passed
//            updateInventoryScreen = true;
//        }
    }

    /**
     * Save current game.
     */
    protected abstract void saveGame();

    /**
     * load new game.
     */
    protected abstract void loadGame();

    /**
     * Call when menu is validate.
     */
    protected void doMenuValidate() {
        changeScreenManager(getStartScreenClass());
    }

    /**
     * Player fire.
     */
    protected abstract void doPlayerFire();
}