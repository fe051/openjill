package org.jill.game.entities;


import org.jill.dma.DmaEntry;
import org.jill.openjill.core.api.entities.BackgroundEntity;
import org.jill.openjill.core.api.entities.BackgroundParam;
import org.jill.openjill.core.api.entities.ObjectEntity;
import org.jill.openjill.core.api.manager.TileManager;

/**
 * Background object for catch update/draw/... message
 *
 * @author Emeric MARTINEAU
 */
public abstract class BackgroundEntityImpl implements BackgroundEntity {
    //-- WORK
    /**
     * Draw specially? (blocks)
     */
    private static final int F_MSGDRAW = 16;

    /**
     * Touch routine? (blocks & objs).
     */
    private static final int F_MSGTOUCH = 8;

    /**
     * If true, redraw every cycle (block).
     */
    private static final int F_MSGUPDATE = 32;

    /**
     * Not water (block).
     */
    private static final int F_NOTWATER = 16384;

    //-- DON'T wORK OR NOT IMPLEMENTED
    /**
     * (blocks) -> player can move here.
     */
    private static final int F_PLAYERTHRU = 1;

    /**
     * (blocks).
     */
    private static final int F_STAIR = 2;
    /**
     * Tinyhero thru.
     */
    private static final int F_TINYTHRU = 512;

    /**
     * (blocks).
     */
    private static final int F_VINE = 4;

    /**
     * Always updates object
     */
    // Never used
    //public static final int F_ALWAYS = 1024;
    /**
     * Is water (block).
     */
    private static final int F_WATER = 8192;

    /**
     * Player can move on this block.
     */
    protected boolean playerThru;

    /**
     * Player can stand on.
     */
    protected boolean stair;

    /**
     * Player can climb.
     */
    protected boolean vine;

    /**
     * Block have special draw.
     */
    protected boolean msgDraw;

    /**
     * Block update on each cycle.
     */
    protected boolean msgUpdate;

    /**
     * Block update on each cycle.
     */
    protected boolean msgTouch;

    /**
     * DmaEntry.
     */
    protected DmaEntry dmaEntry;

    /**
     * Cache manager.
     */
    protected TileManager pictureCache;

    /**
     * Pos X in background map.
     */
    protected int x;

    /**
     * Pos Y in background map.
     */
    protected int y;

    /**
     * Initial config.
     */
    protected BackgroundParam backParam;

    /**
     * For internal use only.
     */
    protected BackgroundEntityImpl() {

    }

    /**
     * Init object.
     *
     * @param backParameter background parameter
     */
    @Override
    public void init(final BackgroundParam backParameter) {
        dmaEntry = backParameter.getDmaEntry();

        final int flag = dmaEntry.getFlags();

        playerThru = (flag & F_PLAYERTHRU) != 0;
        stair = (flag & F_STAIR) != 0;
        vine = (flag & F_VINE) != 0;

        msgDraw = (flag & F_MSGDRAW) != 0;
        msgUpdate = (flag & F_MSGUPDATE) != 0;
        msgTouch = (flag & F_MSGTOUCH) != 0;

        pictureCache = backParameter.getPictureCache();

        x = backParameter.getX();
        y = backParameter.getY();

        this.backParam = backParameter;
    }

    /**
     * If player can cross over.
     *
     * @return true/false
     */
    @Override
    public final boolean isPlayerThru() {
        return playerThru;
    }

    /**
     * Msg draw.
     *
     * @return true/false
     */
    @Override
    public final boolean isMsgDraw() {
        return msgDraw;
    }

    /**
     * Msg update.
     *
     * @return true/false
     */
    @Override
    public final boolean isMsgUpdate() {
        return msgUpdate;
    }

    /**
     * @return mapCode
     */
    @Override
    public final int getMapCode() {
        return dmaEntry.getMapCode();
    }

    /**
     * @return tile
     */
    @Override
    public final int getTile() {
        return dmaEntry.getTile();
    }

    /**
     * @return tileset
     */
    @Override
    public final int getTileset() {
        return dmaEntry.getTileset();
    }

    /**
     * Flags.
     *
     * @return flags
     */
    @Override
    public final int getFlags() {
        return dmaEntry.getFlags();
    }

    /**
     * @return name
     */
    @Override
    public final String getName() {
        return dmaEntry.getName();
    }

    /**
     * Return initial config for ReplaceTileBackgroundEntity.
     *
     * @return backparameter
     */
    @Override
    public final BackgroundParam getBackParam() {
        return backParam;
    }

    /**
     * If player can stand here.
     *
     * @return true/false
     */
    @Override
    public final boolean isStair() {
        return stair;
    }

    /**
     * Player can climb here.
     *
     * @return boolean
     */
    @Override
    public final boolean isVine() {
        return vine;
    }

    /**
     * Return X.
     *
     * @return x
     */
    @Override
    public final int getX() {
        return x;
    }

    /**
     * Return Y.
     *
     * @return y
     */
    @Override
    public final int getY() {
        return y;
    }



    /**
     * Player touch this background.
     *
     * @param obj player
     */
    @Override
    public void msgTouch(final ObjectEntity obj) {
        // nothing
    }
}
