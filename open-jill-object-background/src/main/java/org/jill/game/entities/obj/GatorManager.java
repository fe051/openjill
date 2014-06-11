package org.jill.game.entities.obj;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import org.jill.openjill.core.api.entities.ObjectEntity;
import org.jill.game.entities.obj.abs.AbstractHitPlayerObjectEntity;
import org.jill.openjill.core.api.entities.ObjectParam;
import org.jill.openjill.core.api.message.EnumMessageType;
import org.jill.openjill.core.api.message.object.ObjectListMessage;
import org.jill.openjill.core.api.message.statusbar.inventory.
        InventoryPointMessage;
import org.jill.openjill.core.api.entities.BackgroundEntity;
import org.jill.openjill.core.api.jill.JillConst;

/**
 * Firebird.
 *
 * @author Emeric MARTINEAU
 */
public final class GatorManager extends AbstractHitPlayerObjectEntity {

    /**
     * Picture array.
     */
    private BufferedImage[] leftImages;

    /**
     * Picture array.
     */
    private BufferedImage[] rightImages;

    /**
     * Max X pos left.
     */
    private int maxXLeft;

    /**
     * Max X pos right.
     */
    private int maxXRight;

    /**
     * Kill message.
     */
    private ObjectListMessage killme;

    /**
     * Point message.
     */
    private InventoryPointMessage pointMsg;

    /**
     * Default constructor.
     *
     * @param objectParam object paramter
     */
    @Override
    public void init(final ObjectParam objectParam) {
        super.init(objectParam);

        setKillabgeObject(true);

        final int point = getConfInteger("point");

        // Init list of picture
        final int tileSetIndex = getConfInteger("tileSet");

        final int numberTileSet = getConfInteger("numberTileSet");
        final int rightTileHead = getConfInteger("rightTileHead");
        final int rightTileTail = getConfInteger("rightTileTail");
        final int leftTileHead = getConfInteger("leftTileHead");
        final int leftTileTail = getConfInteger("leftTileTail");

        // Alloc array of picture
        this.leftImages = new BufferedImage[numberTileSet];
        this.rightImages = new BufferedImage[numberTileSet];

        // Init Right
        initPicture(this.rightImages, numberTileSet, tileSetIndex,
                rightTileTail, rightTileHead);

        // Init Left
        initPicture(this.leftImages, numberTileSet, tileSetIndex,
                leftTileHead, leftTileTail);

        // Search block
        final BackgroundEntity[][] backMap =
                objectParam.getBackgroundObject();

        final int blockX = this.x / JillConst.BLOCK_SIZE;
        final int blockYunder = (this.y / JillConst.BLOCK_SIZE) + 1;
        final int blockY = (this.y / JillConst.BLOCK_SIZE);

        int startX = 0;
        int stopX = backMap.length - 1;

        // Search on right
        for (int indexX = blockX; indexX < backMap.length; indexX++) {
            if (backMap[indexX][blockYunder].isPlayerThru()
                    && !backMap[indexX][blockYunder].isStair()) {
                // Search if block under gator
                stopX = indexX;
                break;
            } else if (!backMap[indexX][blockY].isPlayerThru()) {
                // Search if block on same Y
                stopX = indexX;
                break;
            }
        }

        // Search on right
        for (int indexX = blockX; indexX > -1; indexX--) {
            if (backMap[indexX][blockYunder].isPlayerThru()
                    && !backMap[indexX][blockYunder].isStair()) {
                startX = indexX + 1;
                break;
            } else if (!backMap[indexX][blockY].isPlayerThru()) {
                // Search if block on same Y
                startX = indexX + 1;
                break;
            }
        }

        this.maxXLeft = startX * JillConst.BLOCK_SIZE;
        this.maxXRight = (stopX * JillConst.BLOCK_SIZE) - this.width;

        this.killme = new ObjectListMessage(this, false);
        this.pointMsg = new InventoryPointMessage(point, true);
    }

    /**
     * Init picutre.
     *
     * @param images image array
     * @param numberTileSet number of tile
     * @param tileSetIndex tile set index
     * @param tileHead tile for head
     * @param tileTail tile for tail
     */
    private void initPicture(final BufferedImage[] images,
        final int numberTileSet, final int tileSetIndex,
        final int tileHead, final int tileTail) {
        // Current picture for head
        BufferedImage headPicture;
        // Current picture for tail
        BufferedImage tailPicture;
        // Joined picture head+tail
        BufferedImage joinPicture;

        // Object to draw
        Graphics2D g2;

        for (int index = 0; index < numberTileSet; index++) {
            headPicture = this.pictureCache.getImage(tileSetIndex,
                tileHead + index);
            tailPicture = this.pictureCache.getImage(tileSetIndex,
                tileTail + index);

            joinPicture = new BufferedImage(this.width, this.height,
                BufferedImage.TYPE_INT_ARGB);

            g2 = joinPicture.createGraphics();

            g2.drawImage(headPicture, 0, 0, null);
            g2.drawImage(tailPicture, headPicture.getWidth(), 0, null);

            g2.dispose();

            images[index] = joinPicture;
        }
    }

    @Override
    public void msgUpdate() {
        if ((this.x > this.maxXLeft && this.xSpeed < 0)
            || (this.x < this.maxXRight && this.xSpeed > 0)) {
            this.x += this.xSpeed;

            this.counter++;

            if (this.counter == this.rightImages.length) {
                this.counter = 0;
            }
        } else {
            this.xSpeed *= -1;
            this.counter = 0;
        }
    }

    @Override
    public BufferedImage msgDraw() {
        BufferedImage[] currentPictureArray;

        if (this.xSpeed > 0) {
            // Right
            currentPictureArray = this.rightImages;
        } else {
            // Left
            currentPictureArray = this.leftImages;
        }

        return currentPictureArray[this.counter];
    }

    @Override
    public void msgTouch(final ObjectEntity obj) {
        if (obj.isPlayer()) {
            hitPlayer(obj);
        }
    }

    @Override
    public void msgKill(final ObjectEntity sender,
        final int nbLife, final int typeOfDeath) {
        this.messageDispatcher.sendMessage(EnumMessageType.INVENTORY_POINT,
            this.pointMsg);
        this.messageDispatcher.sendMessage(EnumMessageType.OBJECT,
            this.killme);
    }
}