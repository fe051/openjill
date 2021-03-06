package org.jill.game.entities.obj.player;

import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.logging.Logger;

import org.jill.game.entities.obj.bullet.BulletObjectFactory;
import org.jill.openjill.core.api.entities.BackgroundEntity;
import org.jill.openjill.core.api.entities.ObjectParam;
import org.jill.openjill.core.api.jill.JillConst;
import org.jill.openjill.core.api.keyboard.KeyboardLayout;
import org.jill.openjill.core.api.manager.TextManager;
import org.jill.openjill.core.api.message.EnumMessageType;
import org.jill.openjill.core.api.message.statusbar.StatusBarTextMessage;
import org.jill.openjill.core.api.message.statusbar.inventory.EnumInventoryObject;

/**
 * Final player class.
 *
 * @author Emeric MARTINEAU
 */
public final class PlayerManager extends AbstractPlayerManager {

    /**
     * Logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(
            PlayerManager.class.getName());
    /**
     * Size of array for right/middle/leflt.
     */
    private static final int DIRECTION_IMAGE_NUMBER = 3;
    /**
     * To know if message must be display.
     */
    private static boolean messageDisplayHitFloorMessage = true;
    /**
     * Begin player level picture.
     */
    private final Optional<BufferedImage>[] stBegin
            = new Optional[PlayerBeginConst.PICTURE_NUMBER];

    /**
     * Jump player to stand.
     */
    private final Optional<BufferedImage>[][] stJumpingToStandPicture
            = new Optional[DIRECTION_IMAGE_NUMBER][];

    /**
     * Jump players.
     */
    private final Optional<BufferedImage>[][] stJumpingPicture
            = new Optional[PlayerJumpingConst.PICTURE_NUMBER][];

    /**
     * Run left.
     */
    private final Optional<BufferedImage>[] stStandLeftRunning
            = new Optional[PlayerStandConst.PICTURE_RUNNING_NUMBER];

    /**
     * Run right.
     */
    private final Optional<BufferedImage>[] stStandRightRunning
            = new Optional[PlayerStandConst.PICTURE_RUNNING_NUMBER];

    /**
     * Full picture face.
     */
    private final Optional<BufferedImage>[] stStandPicture
            = new Optional[DIRECTION_IMAGE_NUMBER];

    /**
     * Full picture face.
     */
    private final Optional<BufferedImage>[] stClimbPicture
            = new Optional[PlayerClimbConst.PICTURE_NUMBER];

    /**
     * Die 0.
     */
    private final Optional<BufferedImage>[] stDie0Enemy
            = new Optional[PlayerDie0Const.IMAGE_NUMBER];

    /**
     * Die 1.
     */
    private final Optional<BufferedImage>[] stDie1Water
            = new Optional[PlayerDie1Const.IMAGE_NUMBER];

    /**
     * Die 2.
     */
    private final Optional<BufferedImage>[] stDie2Other
            = new Optional[PlayerDie2Const.IMAGE_NUMBER];

    /**
     * Current picture to display.
     */
    private Optional<BufferedImage> currentPlayerPicture = Optional.empty();

    /**
     * Current picture to display.
     */
    private Optional<BufferedImage> stStandJillWaitWithArm;

    /**
     * Current picture to display.
     */
    private Optional<BufferedImage> stStandJillSquat;

    /**
     * Value of current wait animation.
     */
    private int waitAnimationIndex = 0;

    /**
     * Constructor.
     *
     * @param objectParam object parameter
     */
    @Override
    public void init(final ObjectParam objectParam) {
        super.init(objectParam);

        stBegin[PlayerBeginConst.PICTURE_HEAD_DOWN]
                = pictureCache.getImage(PlayerBeginConst.TILESET_INDEX,
                PlayerBeginConst.TILE_HEAD_DOWN_INDEX);
        stBegin[PlayerBeginConst.PICTURE_HEAD_NORMAL]
                = pictureCache.getImage(PlayerBeginConst.TILESET_INDEX,
                PlayerBeginConst.TILE_HEAD_NORMAL_INDEX);
        stBegin[PlayerBeginConst.PICTURE_HEAD_UP]
                = pictureCache.getImage(PlayerBeginConst.TILESET_INDEX,
                PlayerBeginConst.TILE_HEAD_UP_INDEX);

        initJumpingPicture();

        initJumpingToStandPicture();

        for (int index = 0; index < stStandLeftRunning.length; index++) {
            stStandLeftRunning[index] = pictureCache.getImage(
                    PlayerStandConst.TILESET_INDEX, index);
        }

        for (int index = 0; index < stStandLeftRunning.length; index++) {
            stStandRightRunning[index] = pictureCache.getImage(
                    PlayerStandConst.TILESET_INDEX, index
                            + PlayerStandConst.TILE_LEFT_RUNNING_INDEX);
        }

        // Stand picture
        stStandPicture[0] = pictureCache.getImage(
                PlayerStandConst.TILESET_INDEX,
                PlayerStandConst.TILE_RIGHT_INDEX);
        stStandPicture[1] = pictureCache.getImage(
                PlayerStandConst.TILESET_INDEX,
                PlayerStandConst.TILE_MIDDLE_INDEX);
        stStandPicture[2] = pictureCache.getImage(
                PlayerStandConst.TILESET_INDEX,
                PlayerStandConst.TILE_LEFT_INDEX);

        initClimbPicture();

        stStandJillWaitWithArm = pictureCache.getImage(
                PlayerStandConst.TILESET_INDEX,
                PlayerStandConst.TILE_ARM_INDEX);

        initDiePicture();

        this.currentPlayerPicture = this.stStandPicture[1];

        if (getWidth() == 0) {
            // Player created manually
            final Optional<BufferedImage> img
                    = stBegin[PlayerBeginConst.PICTURE_HEAD_NORMAL];

            setWidth(img.get().getWidth());
            setHeight(img.get().getHeight());
        }

        messageDispatcher.sendMessage(EnumMessageType.CHANGE_PLAYER_CHARACTER,
                EnumInventoryObject.JILL);
    }

    /**
     * Init climb picutre.
     */
    private void initClimbPicture() {
        // Climb picture
        int i = 0;
        final int i2 = 3;
        stClimbPicture[i++] = pictureCache.getImage(
                PlayerClimbConst.TILESET_INDEX, PlayerClimbConst.TILE_ONE);
        stClimbPicture[i++] = stClimbPicture[0];
        stClimbPicture[i++] = pictureCache.getImage(
                PlayerClimbConst.TILESET_INDEX, PlayerClimbConst.TILE_TWO);
        stClimbPicture[i++] = pictureCache.getImage(
                PlayerClimbConst.TILESET_INDEX, PlayerClimbConst.TILE_THREE);
        stClimbPicture[i++] = stClimbPicture[i2];
        stClimbPicture[i] = stClimbPicture[2];
    }

    /**
     * Init jumping picture.
     */
    private void initJumpingPicture() {
        final Optional<BufferedImage>[] stJumping
                = new Optional[PlayerJumpingConst.PICTURE_NUMBER];
        final Optional<BufferedImage>[] stJumpingLeft
                = new Optional[PlayerJumpingConst.PICTURE_NUMBER];
        final Optional<BufferedImage>[] stJumpingRight
                = new Optional[PlayerJumpingConst.PICTURE_NUMBER];

        for (int index = 0; index < stJumping.length; index++) {
            stJumping[index] = pictureCache.getImage(
                    PlayerJumpingConst.TILESET_INDEX, index
                            + PlayerJumpingConst.TILE_MIDDLE_INDEX);
        }

        for (int index = 0; index < stJumpingLeft.length; index++) {
            stJumpingLeft[index] = pictureCache.getImage(
                    PlayerJumpingConst.TILESET_INDEX, index
                            + PlayerJumpingConst.TILE_LEFT_INDEX);
        }

        for (int index = 0; index < stJumpingRight.length; index++) {
            stJumpingRight[index] = pictureCache.getImage(
                    PlayerJumpingConst.TILESET_INDEX, index
                            + PlayerJumpingConst.TILE_RIGHT_INDEX);
        }

        stJumpingPicture[0] = stJumpingLeft;
        stJumpingPicture[1] = stJumping;
        stJumpingPicture[2] = stJumpingRight;
    }

    /**
     * Init jumping picture.
     */
    private void initJumpingToStandPicture() {
        final Optional<BufferedImage>[] stJumpingToStandLeft
                = new Optional[PlayerStandConst.PICTURE_NUMBER];
        final Optional<BufferedImage>[] stJumpingToStandRight
                = new Optional[PlayerStandConst.PICTURE_NUMBER];

        stStandJillSquat = pictureCache.getImage(
                PlayerStandConst.TILESET_INDEX,
                PlayerStandConst.TILE_DOWN_INDEX);

        Optional<BufferedImage> temp = pictureCache.getImage(
                PlayerStandConst.TILESET_INDEX,
                PlayerStandConst.TILE_FALL_INDEX);

        final Optional<BufferedImage>[] stJumpingToStand = new Optional[]{temp,
                stStandJillSquat, stStandJillSquat, temp, temp};

        int i = 0;
        stJumpingToStandLeft[i++] = pictureCache.getImage(
                PlayerStandConst.TILESET_INDEX,
                PlayerStandConst.TILE_LEFT_HIT_FLOOR_INDEX0);
        stJumpingToStandLeft[i++] = stJumpingToStandLeft[0];
        stJumpingToStandLeft[i++] = pictureCache.getImage(
                PlayerStandConst.TILESET_INDEX,
                PlayerStandConst.TILE_LEFT_HIT_FLOOR_INDEX2);
        stJumpingToStandLeft[i++] = stJumpingToStandLeft[2];
        stJumpingToStandLeft[i] = pictureCache.getImage(
                PlayerStandConst.TILESET_INDEX,
                PlayerStandConst.TILE_LEFT_HIT_FLOOR_INDEX1);

        i = 0;

        stJumpingToStandRight[i++] = pictureCache.getImage(
                PlayerStandConst.TILESET_INDEX,
                PlayerStandConst.TILE_RIGHT_HIT_FLOOR_INDEX0);
        stJumpingToStandRight[i++] = stJumpingToStandRight[0];
        stJumpingToStandRight[i++] = pictureCache.getImage(
                PlayerStandConst.TILESET_INDEX,
                PlayerStandConst.TILE_RIGHT_HIT_FLOOR_INDEX2);
        stJumpingToStandRight[i++] = stJumpingToStandRight[2];
        stJumpingToStandRight[i] = pictureCache.getImage(
                PlayerStandConst.TILESET_INDEX,
                PlayerStandConst.TILE_RIGHT_HIT_FLOOR_INDEX1);

        stJumpingToStandPicture[0] = stJumpingToStandLeft;
        stJumpingToStandPicture[1] = stJumpingToStand;
        stJumpingToStandPicture[2] = stJumpingToStandRight;

        drawPieceOfLand(stJumpingToStandPicture);
    }

    @Override
    public Optional<BufferedImage> msgDraw() {
        Optional<BufferedImage> currentPicture;

        switch (getState()) {
            case PlayerState.BEGIN:
                currentPicture = msgDrawBegin();
                break;
            case PlayerState.JUMPING:
                currentPicture = msgDrawJumping();
                break;
            case PlayerState.STAND:
                currentPicture = msgDrawStand();
                break;
            case PlayerState.CLIMBING:
                currentPicture = msgDrawClimb();
                break;
            case PlayerState.DIE:
                currentPicture = msgDrawDied();
                break;
            default:
                currentPicture = null;
                LOGGER.severe(
                        String.format("The state %d is unknow for player !",
                                getState()));
        }

        this.currentPlayerPicture = currentPicture;

        return currentPicture;
    }

    @Override
    public void setState(final int state) {
//        // Don't change state if same
//        if (state == this.state && subState != 0) {
//            return;
//        }
        switch (state) {
            case PlayerState.STAND:
                setSubState(0);
                break;
            case PlayerState.BEGIN:
                setSubState(PlayerState.BEGIN_SUB_STATE);
                setStateCount(X_SPEED_MIDDLE);
                break;
            case PlayerState.JUMPING:
                setSubState(X_SPEED_MIDDLE);
                break;
            default:
                break;
        }

        // State Die managed in msgKill()
        super.setState(state);
    }

    @Override
    public void msgUpdate(final KeyboardLayout keyboardLayout) {
        move(keyboardLayout);

        switch (getState()) {
            case PlayerState.BEGIN:
                msgUpdateBegin();
                break;
            case PlayerState.JUMPING:
                msgUpdateJumping();
                break;
            case PlayerState.STAND:
                msgUpdateStand();
                break;
            case PlayerState.CLIMBING:
                msgUpdateClimb();
                break;
            case PlayerState.DIE:
                msgUpdateDied();
                break;
            default:
                LOGGER.severe(
                        String.format("The state %d is unknow for player !",
                                getState()));
        }
    }

    /**
     * Manage update message at begin state.
     */
    private void msgUpdateBegin() {
        this.stateCount++;

        if (this.stateCount
                >= PlayerBeginConst.PICTURE_HEAD_DOWN_STATECOUNT) {
            setState(PlayerState.STAND);
        }
    }

    /**
     * Manage update message at begin state.
     */
    private void msgUpdateJumping() {
        this.stateCount++;
        this.subState++;
    }

    /**
     * Display climb picture.
     */
    private void msgUpdateClimb() {
        this.stateCount++;
    }

    /**
     * Player stand.
     */
    private void msgUpdateStand() {
        this.stateCount++;
        // Down
        if (this.stateCount
                > PlayerStandConst.HIT_FLOOR_ANIMATION_STATECOUNT) {
            msgUpdateStandHitFloorAnimation();
        } else if (this.xSpeed == X_SPEED_MIDDLE
                && this.ySpeed == Y_SPEED_MIDDLE) {
            msgUpdateStandWait();
        }
    }

    /**
     * Player no move.
     */
    private void msgUpdateStandWait() {
        if (this.stateCount
                == PlayerStandConst.STATECOUNT_WAIT_MSG) {
            msgUpdateStandWatDisplayMessage();
        } else if (this.stateCount
                >= PlayerStandConst.STATECOUNT_WAIT_END) {
            // Reinit
            setStateCount(0);
        }
    }

    /**
     * Display message and set index of wait animation.
     */
    private void msgUpdateStandWatDisplayMessage() {
        // Generate wait animation index
        final int max = PlayerWaitConst.WAIT_MESSAGES.length;
        // 0 value is reserved when reload game
        this.waitAnimationIndex = (int) (Math.random() * max) + 1;

        this.messageDispatcher.sendMessage(
                EnumMessageType.MESSAGE_STATUS_BAR,
                PlayerWaitConst.WAIT_MESSAGES[this.waitAnimationIndex - 1]);
    }

    /**
     * Stand hit floor animation.
     */
    private void msgUpdateStandHitFloorAnimation() {
        // Hit floor animation
        if (this.counter == 0) {
            // End hit animation
            if (messageDisplayHitFloorMessage) {
                messageDisplayHitFloorMessage = false;
                final StatusBarTextMessage msg =
                        new StatusBarTextMessage(
                                "Hey,  your shoes are untied.", 10,
                                TextManager.COLOR_GREEN);

                this.messageDispatcher.sendMessage(
                        EnumMessageType.MESSAGE_STATUS_BAR, msg);
            }

            setStateCount(1);
        }

        this.counter--;
    }

    /**
     * Message update for die.
     */
    private void msgUpdateDied() {
        switch (this.subState) {
            case PlayerState.DIE_SUB_STATE_ENNEMY:
                msgUpdateDiedEnnemy();
                break;
            case PlayerState.DIE_SUB_STATE_WATER_BACK:
                msgUpdateDiedWater();
                break;
            case PlayerState.DIE_SUB_STATE_OTHER_BACK:
                msgUpdateDiedOther();
                break;
            default:
        }
    }

    /**
     * Message update for die 0.
     */
    private void msgUpdateDiedEnnemy() {
        if (this.stateCount >= PlayerDie0Const.STATECOUNT_MAX_TO_RESTART_GAME) {
            this.messageDispatcher.sendMessage(
                    EnumMessageType.DIE_RESTART_LEVEL, null);
        } else {
            this.stateCount++;
        }
    }

    /**
     * Message update for die .
     */
    private void msgUpdateDiedWater() {
        if (this.stateCount >= PlayerDie1Const.STATECOUNT_MAX_TO_RESTART_GAME) {
            this.messageDispatcher.sendMessage(
                    EnumMessageType.DIE_RESTART_LEVEL, null);
        } else {
            this.stateCount++;
        }
    }

    /**
     * Message update for die 2.
     */
    private void msgUpdateDiedOther() {
        if (this.ySpeed > PlayerDie2Const.YD_MAX_TO_CHANGE) {
            if (this.stateCount
                    > PlayerDie2Const.STATECOUNT_MAX_TO_RESTART_GAME) {
                this.messageDispatcher.sendMessage(
                        EnumMessageType.DIE_RESTART_LEVEL, null);
            }

            // Realign player on background
            if (this.stateCount == 0) {
                final Optional<BufferedImage> currentPicture = getDieOtherPicture();

                // Align on block under kill background.
                this.y = (((this.y / JillConst.getBlockSize()))
                        * JillConst.getBlockSize()) + JillConst.getBlockSize()
                        + currentPicture.get().getHeight();

                // If player out of screen
                if ((this.y + currentPicture.get().getHeight())
                        > JillConst.getMaxHeight()) {
                    this.y = JillConst.getMaxHeight()
                            - currentPicture.get().getHeight();
                }
            }

            this.stateCount++;
        } else {
            this.y += this.ySpeed;
            this.ySpeed += 2;

//            this.currentPicture = this.stDie2[PlayerDie2Const.FIRST_PICTURE];
        }
    }

    @Override
    protected void killPlayer(final int typeOfDeath,
            final Optional<BackgroundEntity> senderBack) {
        setState(PlayerState.DIE);
        setSubState(typeOfDeath);
        setStateCount(0);

        switch (typeOfDeath) {
            case PlayerState.DIE_SUB_STATE_OTHER_BACK:
                setySpeed(PlayerDie2Const.START_YD);

                // Align player on bottom of background
                this.y = (senderBack.get().getY() + 1) * JillConst.getBlockSize()
                        - this.stDie2Other[
                        PlayerDie2Const.FIRST_PICTURE].get().getHeight();
                break;
            case PlayerState.DIE_SUB_STATE_WATER_BACK:
                setySpeed(PlayerDie1Const.START_YD);

                // Align player on bottom of background
                this.y = senderBack.get().getY() * JillConst.getBlockSize()
                        - this.stDie1Water[
                        PlayerDie1Const.FIRST_PICTURE].get().getHeight();
                break;
            case PlayerState.DIE_SUB_STATE_ENNEMY:
                setySpeed(PlayerDie0Const.START_YD);
            default:
                // ennemy death
        }

        BulletObjectFactory.explode(this,
                PlayerDie0Const.NB_COLORED_BULLET,
                this.messageDispatcher);
    }

    @Override
    public int getWidth() {
        if (!this.currentPlayerPicture.isPresent()) {
            this.currentPlayerPicture = msgDraw();
        }

        return this.currentPlayerPicture.get().getWidth();
    }

    @Override
    public int getHeight() {
        if (!this.currentPlayerPicture.isPresent()) {
            this.currentPlayerPicture = msgDraw();
        }

        return this.currentPlayerPicture.get().getHeight();
    }

    /**
     * Init die picture.
     */
    private void initDiePicture() {
        // Just for checkstyle, sorry
        int i = 0;
        stDie2Other[i++] = pictureCache.getImage(PlayerDie2Const.TILESET_INDEX,
                PlayerDie2Const.TILE0);
        stDie2Other[i++] = pictureCache.getImage(PlayerDie2Const.TILESET_INDEX,
                PlayerDie2Const.TILE1);
        stDie2Other[i++] = pictureCache.getImage(PlayerDie2Const.TILESET_INDEX,
                PlayerDie2Const.TILE2);
        stDie2Other[i] = pictureCache.getImage(PlayerDie2Const.TILESET_INDEX,
                PlayerDie2Const.TILE3);

        for (int index = 0; index < PlayerDie0Const.IMAGE_NUMBER; index++) {
            this.stDie0Enemy[index] = pictureCache.getImage(
                    PlayerDie0Const.TILESET_INDEX,
                    PlayerDie0Const.TILE_INDEX + index);
        }

        for (int index = 0; index < PlayerDie1Const.IMAGE_NUMBER; index++) {
            this.stDie1Water[index] = pictureCache.getImage(
                    PlayerDie1Const.TILESET_INDEX,
                    PlayerDie1Const.TILE_INDEX + index);
        }
    }

    /**
     * Manage draw at begin state.
     *
     * @return picture to draw
     */
    private Optional<BufferedImage> msgDrawBegin() {
        final int indexPicture;
        BufferedImage currentPicture;

        // Get good picture
        if (this.stateCount < PlayerBeginConst.PICTURE_HEAD_UP_STATECOUNT) {
            indexPicture = PlayerBeginConst.PICTURE_HEAD_UP;
        } else if (this.stateCount
                < PlayerBeginConst.PICTURE_HEAD_NORMAL_STATECOUNT) {
            indexPicture = PlayerBeginConst.PICTURE_HEAD_NORMAL;
        } else if (this.stateCount
                < PlayerBeginConst.PICTURE_HEAD_DOWN_STATECOUNT) {
            indexPicture = PlayerBeginConst.PICTURE_HEAD_DOWN;
        } else {
            indexPicture = DIRECTION_IMAGE_NUMBER;
        }

        if (indexPicture < DIRECTION_IMAGE_NUMBER) {
            // Get image
            final BufferedImage baseImage = this.stBegin[indexPicture].get();

            currentPicture = new BufferedImage(baseImage.getWidth(),
                    baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);

            final int reduction = baseImage.getHeight() - this.stateCount;

            // Draw picutre
            drawFromImage(currentPicture, baseImage, 0, reduction);

            return Optional.of(currentPicture);
        }

        return Optional.empty();
    }

    /**
     * Manage update message at begin state.
     *
     * @return picture to draw
     */
    private Optional<BufferedImage> msgDrawJumping() {
        Optional<BufferedImage> currentPicture;

        if (getSubState() >= PlayerStandConst.SUBSTATE_VALUE_TO_FALL) {
            // Animation picture done
            if (getySpeed() > Y_SPEED_MIDDLE) {
                // Down
                currentPicture =
                        this.stJumpingToStandPicture[this.xSpeed + 1][0];
            } else {
                currentPicture =
                        stJumpingPicture[this.xSpeed + 1][2];
            }
        } else {
            currentPicture =
                    this.stJumpingPicture[this.xSpeed + 1][this.subState];
        }

        return currentPicture;
    }

    /**
     * Player stand.
     *
     * @return picture to draw
     */
    private Optional<BufferedImage> msgDrawStand() {
        Optional<BufferedImage> currentPicture;

        // Down
        if (this.stateCount
                > PlayerStandConst.HIT_FLOOR_ANIMATION_STATECOUNT) {
            currentPicture = msgDrawStandHitFloorAnimation();
        } else {
            if (this.xSpeed == X_SPEED_MIDDLE) {
                if (this.ySpeed < Y_SPEED_MIDDLE) {
                    // Head up
                    currentPicture
                            = stBegin[PlayerBeginConst.PICTURE_HEAD_UP];
                } else if (this.ySpeed >= PlayerStandConst.Y_SPEED_SQUAT_DOWN) {
                    // Jill squat
                    currentPicture = stStandJillSquat;
                } else if (this.ySpeed > Y_SPEED_MIDDLE) {
                    // Head down
                    currentPicture
                            = stBegin[PlayerBeginConst.PICTURE_HEAD_DOWN];
                } else {
                    currentPicture = msgDrawStandWait();
                }
            } else {
                if (this.xSpeed < X_SPEED_MIDDLE) {
                    // Player running.
                    currentPicture
                            = this.stStandLeftRunning[this.subState];
                } else {
                    currentPicture
                            = this.stStandRightRunning[this.subState];
                }
            }
        }

        return currentPicture;
    }

    /**
     * Stand hit floor animation.
     *
     * @return picture to draw
     */
    private Optional<BufferedImage> msgDrawStandHitFloorAnimation() {
        Optional<BufferedImage> currentPicture;

        // Hit floor animation
        int indexPicture = this.stateCount
                - PlayerStandConst.HIT_FLOOR_ANIMATION_STATECOUNT - 1;

        if (indexPicture
                >= this.stJumpingToStandPicture[
                this.xSpeed + 1].length) {
            indexPicture = 0;
        }

        currentPicture =
                this.stJumpingToStandPicture[
                        this.xSpeed + 1][indexPicture];

        return currentPicture;
    }

    /**
     * Player no move.
     *
     * @return picture to draw
     */
    private Optional<BufferedImage> msgDrawStandWait() {
        Optional<BufferedImage> currentPicture;

        if (this.info1 != X_SPEED_MIDDLE
                && this.stateCount
                >= PlayerStandConst.STATECOUNT_LEFT_RIGHT_TO_FACE
                && this.stateCount
                < PlayerStandConst.STATECOUNT_WAIT_ARM) {
            currentPicture = this.stStandPicture[1];
        } else if (this.stateCount
                >= PlayerStandConst.STATECOUNT_WAIT_ARM
                && this.stateCount
                < PlayerStandConst.STATECOUNT_WAIT_MSG) {
            currentPicture = this.stStandJillWaitWithArm;
        } else if (this.stateCount
                == PlayerStandConst.STATECOUNT_WAIT_MSG) {
            currentPicture = this.stStandJillWaitWithArm;
        } else if (this.stateCount
                >= PlayerStandConst.STATECOUNT_WAIT_MSG
                && this.stateCount
                < PlayerStandConst.STATECOUNT_WAIT_ANIMATION) {
            currentPicture = this.stStandJillWaitWithArm;
        } else if (this.stateCount
                >= PlayerStandConst.STATECOUNT_WAIT_ANIMATION
                && this.stateCount
                < PlayerStandConst.STATECOUNT_WAIT_END) {
            currentPicture = msgDrawStandWaitDisplayAnimation();
        } else {
            currentPicture = this.stStandPicture[this.info1 + 1];
        }

        return currentPicture;
    }

    /**
     * Display wait animation.
     *
     * @return picture to draw
     */
    private Optional<BufferedImage> msgDrawStandWaitDisplayAnimation() {
        Optional<BufferedImage> currentPicture;

        switch (this.waitAnimationIndex - 1) {
            case PlayerWaitConst.HAVE_YOU_SEEN_JILL_ANYWHERE:
                int reste = (int) Math.IEEEremainder(this.stateCount
                                - PlayerStandConst.STATECOUNT_WAIT_ANIMATION,
                        PlayerWaitConst.HAVE_YOU_SEEN_JILL_ANYWHERE_DIV);
                switch (reste) {
                    case 0:
                    case 1:
                        currentPicture
                                = stBegin[PlayerBeginConst.PICTURE_HEAD_UP];
                        break;
                    case 2:
                    case 3:
                        currentPicture
                                = stBegin[PlayerBeginConst.PICTURE_HEAD_NORMAL];
                        break;
                    default:
                        currentPicture
                                = stBegin[PlayerBeginConst.PICTURE_HEAD_DOWN];
                }
                break;
            case PlayerWaitConst.LOOK_AN_AIREPLANE:
                currentPicture
                        = stBegin[PlayerBeginConst.PICTURE_HEAD_UP];
                break;
            case PlayerWaitConst.HEY_YOUR_SHOES_ARE_UNTIED:
                currentPicture
                        = stBegin[PlayerBeginConst.PICTURE_HEAD_DOWN];
                break;
            case PlayerWaitConst.ARE_YOU_JUST_GONNA_SIT_THERE:
                currentPicture = stStandJillWaitWithArm;
                break;
            default:
                // From restore game
                currentPicture = this.stStandPicture[1];
        }

        return currentPicture;
    }

    /**
     * Display climb picture.
     *
     * @return picture to draw
     */
    private Optional<BufferedImage> msgDrawClimb() {
        // subState update in AbstractPlayerManager.moveStdPlayerUpClimb()
        return stClimbPicture[subState];
    }

    /**
     * Message draw for die.
     *
     * @return picture to draw
     */
    private Optional<BufferedImage> msgDrawDied() {
        Optional<BufferedImage> currentPicture;

        switch (this.subState) {
            case PlayerState.DIE_SUB_STATE_ENNEMY:
                currentPicture = msgDrawDiedEnnemy();
                break;
            case PlayerState.DIE_SUB_STATE_WATER_BACK:
                currentPicture = msgDrawDiedWater();
                break;
            case PlayerState.DIE_SUB_STATE_OTHER_BACK:
                currentPicture = msgDrawDiedOther();
                break;
            default:
                currentPicture = Optional.empty();
        }

        return currentPicture;
    }

    /**
     * Message draw for die 0.
     *
     * @return picture to draw
     */
    private Optional<BufferedImage> msgDrawDiedEnnemy() {
        final int indexPicture = this.stateCount
                / PlayerDie0Const.STATECOUNT_STEP_TO_CHANGE_PICTURE;
        return this.stDie0Enemy[indexPicture];
    }

    /**
     * Message draw for die 1.
     *
     * @return picture to draw
     */
    private Optional<BufferedImage> msgDrawDiedWater() {
        Optional<BufferedImage> currentPicture;

        if (this.stateCount == 0) {
            currentPicture = this.stDie1Water[
                    PlayerDie1Const.FIRST_PICTURE];
        } else {
            int indexPicture = this.stateCount
                    / PlayerDie1Const.STATECOUNT_STEP_TO_CHANGE_PICTURE;

            if (indexPicture < this.stDie1Water.length) {
                currentPicture = this.stDie1Water[indexPicture];
            } else {
                currentPicture = Optional.empty();
            }
        }

        return currentPicture;
    }

    /**
     * Message update for die 2.
     *
     * @return picture to draw
     */
    private Optional<BufferedImage> msgDrawDiedOther() {
        Optional<BufferedImage> currentPicture;

        if (this.ySpeed > PlayerDie2Const.YD_MAX_TO_CHANGE) {
            currentPicture = getDieOtherPicture();
        } else {
            this.y += this.ySpeed;
            this.ySpeed += 2;

            currentPicture = this.stDie2Other[PlayerDie2Const.FIRST_PICTURE];
        }

        return currentPicture;
    }

    /**
     * Return pictue of die other. Call to update object and draw object.
     *
     * @return picture
     */
    private Optional<BufferedImage> getDieOtherPicture() {
        Optional<BufferedImage> currentPicture;

        if (this.stateCount
                < PlayerDie2Const.STATECOUNT_MAX_TO_FIRST_ANIMATION) {
            currentPicture = this.stDie2Other[(this.stateCount % 2) + 1];
        } else {
            currentPicture
                    = this.stDie2Other[PlayerDie2Const.LAST_PICTURE];
        }

        return currentPicture;
    }
}
