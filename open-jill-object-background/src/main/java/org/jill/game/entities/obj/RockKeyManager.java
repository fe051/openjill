package org.jill.game.entities.obj;

import java.awt.image.BufferedImage;
import org.jill.game.entities.obj.abs.AbstractParameterObjectEntity;
import org.jill.openjill.core.api.entities.ObjectEntity;
import org.jill.openjill.core.api.entities.ObjectParam;
import org.jill.openjill.core.api.message.EnumMessageType;
import org.jill.openjill.core.api.message.object.ObjectListMessage;
import org.jill.openjill.core.api.message.statusbar.StatusBarTextMessage;
import org.jill.openjill.core.api.message.statusbar.inventory.
        InventoryItemMessage;
import org.jill.openjill.core.api.message.statusbar.inventory.
        InventoryPointMessage;

/**
 * Rockkey for mapdoor.
 *
 * @author Emeric MARTINEAU
 */
public final class RockKeyManager
    extends AbstractParameterObjectEntity {

    /**
     * To know if message must be display.
     */
    private static boolean messageDisplayRockMessage = true;

    /**
     * Message to display in bottom of screen.
     */
    private StatusBarTextMessage msg;

    /**
     * Inventory object to add.
     */
    private InventoryItemMessage inventory;

    /**
     * Point.
     */
    private InventoryPointMessage point;

    /**
     * To remove this object from object list.
     */
    private ObjectListMessage killme;

    /**
     * Picture array.
     */
    private BufferedImage[] images;

    /**
     * Default constructor.
     *
     * @param objectParam object paramter
     */
    @Override
    public void init(final ObjectParam objectParam) {
        super.init(objectParam);

        String textMsg = getConfString("msg");
        int textColor = getConfInteger("msgColor");
        int textTime = getConfInteger("msgTime");

        this.msg = new StatusBarTextMessage(textMsg, textTime, textColor);

        this.inventory = new InventoryItemMessage(
                getConfString("inventory"), true);

        // Remove me from list of object (= kill me)
        this.killme = new ObjectListMessage(this, false);

        // Point
        this.point = new InventoryPointMessage(
                getConfInteger("point"), true);

        loadPicture();
    }

    /**
     * Load picture.
     */
    private void loadPicture() {
        int tileIndex = getConfInteger("tile");
        int tileSetIndex = getConfInteger("tileSet");

        int numberTileSet = getConfInteger("numberTileSet");

        // Load picture for each object. Don't use cache cause some picture
        // change between jill episod.
        this.images
            = new BufferedImage[numberTileSet * 2];

        int indexArray = 0;

        for (int index = 0; index < numberTileSet; index++) {
            this.images[indexArray]
                = this.pictureCache.getImage(tileSetIndex, tileIndex
                    + index);
            this.images[indexArray + 1] = this.images[indexArray];

            indexArray += 2;
        }
    }

    @Override
    public void msgTouch(final ObjectEntity obj) {
        if (obj.isPlayer()) {
            this.messageDispatcher.sendMessage(EnumMessageType.INVENTORY_ITEM,
                this.inventory);
            this.messageDispatcher.sendMessage(EnumMessageType.INVENTORY_POINT,
                this.point);

            if (messageDisplayRockMessage) {
                this.messageDispatcher.sendMessage(
                    EnumMessageType.MESSAGE_STATUS_BAR, this.msg);

                messageDisplayRockMessage = false;
            }

            messageDispatcher.sendMessage(EnumMessageType.OBJECT, this.killme);
        }
    }

    @Override
    public BufferedImage msgDraw() {
        return this.images[this.counter];
    }

    /**
     * Call to update.
     */
    @Override
    public void msgUpdate() {
        this.counter++;

        if (this.counter >= this.images.length) {
            this.counter = 0;
        }
    }
}
