package org.jill.game.level;

import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jill.cfg.SaveGameItem;
import org.jill.file.FileAbstractByte;
import org.jill.game.config.JillGameConfig;
import org.jill.game.config.ObjectInstanceFactory;
import org.jill.game.gui.menu.HighScoreMenu;
import org.jill.game.gui.menu.LoadGameMenu;
import org.jill.game.gui.menu.SaveGameMenu;
import org.jill.game.level.cfg.JillLevelConfiguration;
import org.jill.game.level.cfg.LevelConfiguration;
import org.jill.game.level.handler.LoadNewLevelHandler;
import org.jill.game.screen.conf.RectangleConf;
import org.jill.jn.BackgroundLayer;
import org.jill.jn.JnFile;
import org.jill.jn.ObjectItem;
import org.jill.jn.SaveData;
import org.jill.jn.StringItem;
import org.jill.openjill.core.api.entities.ObjectEntity;
import org.jill.openjill.core.api.message.EnumMessageType;
import org.jill.openjill.core.api.message.statusbar.inventory.EnumInventoryObject;
import org.simplegame.InterfaceSimpleGameHandleInterface;
import org.simplegame.SimpleGameConfig;
import org.simplegame.SimpleGameHandler;

/**
 * This class contains all methods to change level, load/restore game.
 *
 * @author Emeric MARTINEAU
 */
public abstract class AbstractChangeLevel extends
        AbstractExecutingStdPlayerLevel {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(
            AbstractChangeLevel.class.getName());

    /**
     * Level number to know if restart level.
     */
    private static final int RESTART_LEVEL_NUMBER = -1;

    /**
     * Map data.
     */
    protected Optional<FileAbstractByte> mapLevel;

    /**
     * Next level to load.
     */
    protected boolean isRestoreLevel = false;

    /**
     * File name of new level.
     */
    protected Optional<String> newLevelFileName;

    /**
     * Level number of new level.
     */
    protected int newLevelNumber;

    /**
     * Menu fo hiscore.
     */
    protected HighScoreMenu menuHighScore;

    /**
     * Save game.
     */
    protected SaveGameMenu menuSaveGame;

    /**
     * Load game.
     */
    protected LoadGameMenu menuLoadGame;

    /**
     * levelMessageWait cycle
     */
    protected int levelMessageWait;

    /**
     * Level configuration.
     *
     * @param cfgLevel configuration of level
     * @throws IOException                  if error reading file
     * @throws ReflectiveOperationException if can't create class
     */
    public AbstractChangeLevel(final LevelConfiguration cfgLevel)
            throws IOException, ReflectiveOperationException {
        super(cfgLevel);

        constructor();
    }

    /**
     * Change level.
     *
     * @param levelFileName filename of level
     * @param levelNumber   number of level
     */
    private void changeLevel(final Optional<String> levelFileName,
            final int levelNumber) {
        this.newLevelFileName = levelFileName;
        this.newLevelNumber = levelNumber;

        switch (levelNumber) {
            case SaveData.MAP_LEVEL:
                this.levelMessageBox.setLevel(0);
                this.levelMessageBox.setEnable(true);
                break;
            case RESTART_LEVEL_NUMBER:
                this.levelMessageBox.setCanchange(true);
                break;
            default:
                this.levelMessageBox.setLevel(levelNumber);
                this.levelMessageBox.setEnable(true);
                break;
        }

        JillGameConfig jillCfg =
                (JillGameConfig) SimpleGameConfig.getInstance();

        this.levelMessageWait =
                jillCfg.getLevelMessageTimeout() / jillCfg.getGameTimerDelay();
    }

    /**
     * Return current number of gem.
     *
     * @return number of gem
     */
    private int getCurrentGemCount() {
        int gemCount = 0;

        for (EnumInventoryObject inv : this.inventoryArea.getObjects()) {
            if (inv == EnumInventoryObject.GEM) {
                gemCount++;
            }
        }

        return gemCount;
    }

    /**
     * Create and load new level.
     */
    private void createAndLoadNewLevel() {
        // Stop game
        this.runGame = false;
        this.levelMessageBox.setCanchange(false);

        try {
            // If in map level, store map level to object
            switch (this.newLevelNumber) {
                case RESTART_LEVEL_NUMBER:
                    loadRestartLevel();
                    break;
                case SaveData.MAP_LEVEL:
                    loadMapFromLevel();
                    break;
                default:
                    loadNewLevelFromMap();
                    break;
            }
        } catch (final ReflectiveOperationException e) {
            LOGGER.severe("Error when loading game.");
            e.printStackTrace();
        }
    }

    /**
     * Restart level.
     */
    private void loadRestartLevel() throws ReflectiveOperationException {
        try {
            final FileAbstractByte map = this.mapLevel.get();
            map.seek(0);

            // Reload map to get some infrmation
            final JnFile mapFile = ObjectInstanceFactory.getNewJn();
            mapFile.load(map);

            // Load level
            LevelConfiguration cfgNewLevel = new JillLevelConfiguration(
                    this.levelConfiguration.getShaFileName(),
                    this.newLevelFileName,
                    this.levelConfiguration.getVclFileName(),
                    this.levelConfiguration.getCfgFileName(),
                    this.levelConfiguration.getCfgSavePrefixe(),
                    this.levelConfiguration.getStartScreen(),
                    this.inventoryArea.getLevel(), this.mapLevel, Optional.empty(),
                    mapFile.getSaveData().getScore(), 0);

            // Create next level
            final InterfaceSimpleGameHandleInterface newLevel
                    = new LoadNewLevelHandler(cfgNewLevel);

            SimpleGameHandler.setNewHandler(newLevel);
        } catch (IOException | ClassNotFoundException |
                IllegalAccessException | InstantiationException ex) {
            LOGGER.log(Level.SEVERE,
                    "Error when switch to new level", ex);
        }
    }

    /**
     * Reload map from level.
     */
    private void loadMapFromLevel() throws ReflectiveOperationException {
        try {
            final FileAbstractByte map = this.mapLevel.get();
            map.seek(0);

            LevelConfiguration cfgNewLevel = new JillLevelConfiguration(
                    this.levelConfiguration.getShaFileName(),
                    Optional.empty(),
                    this.levelConfiguration.getVclFileName(),
                    this.levelConfiguration.getCfgFileName(),
                    this.levelConfiguration.getCfgSavePrefixe(),
                    this.levelConfiguration.getStartScreen(),
                    this.inventoryArea.getLevel(), Optional.empty(), this.mapLevel,
                    this.inventoryArea.getScore(),
                    getCurrentGemCount());

            // Retore map level
            final InterfaceSimpleGameHandleInterface newLevel
                    = new LoadNewLevelHandler(cfgNewLevel);

            SimpleGameHandler.setNewHandler(newLevel);
        } catch (IOException | ClassNotFoundException |
                IllegalAccessException | InstantiationException ex) {
            LOGGER.log(Level.SEVERE,
                    "Error when return in map level", ex);

        }
    }

    /**
     * Load new level from map.
     */
    private void loadNewLevelFromMap() throws ReflectiveOperationException {
        try {
            // store new level number in map
            this.inventoryArea.setLevel(this.newLevelNumber);

            // Create virtual file
            final FileAbstractByte jnData = putCurrentLevelInFileMemory();
            //jnData.seek(0);

            LevelConfiguration cfgNewLevel = new JillLevelConfiguration(
                    this.levelConfiguration.getShaFileName(),
                    this.newLevelFileName,
                    this.levelConfiguration.getVclFileName(),
                    this.levelConfiguration.getCfgFileName(),
                    this.levelConfiguration.getCfgSavePrefixe(),
                    this.levelConfiguration.getStartScreen(),
                    this.newLevelNumber, Optional.of(jnData), Optional.empty(),
                    this.inventoryArea.getScore(),
                    getCurrentGemCount());

            // Create next level
            final InterfaceSimpleGameHandleInterface newLevel
                    = new LoadNewLevelHandler(cfgNewLevel);

            SimpleGameHandler.setNewHandler(newLevel);
        } catch (IOException | ClassNotFoundException |
                IllegalAccessException | InstantiationException ex) {
            LOGGER.log(Level.SEVERE,
                    "Error when switch to new level", ex);
        }
    }

    /**
     * Put current level in abstract file.
     *
     * @return file data
     */
    private FileAbstractByte putCurrentLevelInFileMemory() {
        // Create virtual file
        final FileAbstractByte fab = ObjectInstanceFactory.getNewFileByte();
        fab.load(calculateFileSize());

        try {
            writeBackgroundInFile(fab);

            writeObjectInFile(fab);

            writeSaveDataInFile(fab);

            writeStringObjectInFile(fab);
        } catch (final EOFException ex) {
            throw new RuntimeException(ex);
        }

        return fab;
    }

    @Override
    protected void loadGame() {
        this.menu = this.menuLoadGame;
        this.menu.setEnable(true);
    }

    @Override
    protected void saveGame() {
        this.menu = this.menuSaveGame;
        this.menu.setEnable(true);
    }

    /**
     * Calculate file size.
     *
     * @return size of file
     */
    private int calculateFileSize() {
        int fileSize = BackgroundLayer.SIZE_IN_FILE;

        // Size of object
        int objectSize = 2; // 2 -> two byte to store number of object

        // Running object
        for (ObjectItem obj : this.listObject) {
            objectSize += obj.getSizeInFile();
        }

        // Background object
        for (ObjectItem obj : this.listObjectDrawOnBackground) {
            objectSize += obj.getSizeInFile();
        }

        // Object always on screen
        for (ObjectItem obj : listObjectAlwaysOnScreen) {
            objectSize += obj.getSizeInFile();
        }

        fileSize += objectSize;

        fileSize += SaveData.SIZE_IN_FILE;

        return fileSize;
    }

    /**
     * Write background in virtual file.
     *
     * @param fab file
     * @throws EOFException if virtual file is to small
     */
    private void writeBackgroundInFile(final FileAbstractByte fab)
            throws EOFException {
        // Save background. Background store by row
        for (int indexX = 0; indexX < BackgroundLayer.MAP_WIDTH; indexX++) {
            for (int indexY = 0; indexY < BackgroundLayer.MAP_HEIGHT;
                 indexY++) {
                fab.write16bitLE(
                        this.backgroundObject[indexX][indexY].getMapCode());
            }
        }
    }

    /**
     * Write object in virtual file.
     *
     * @param fab file
     * @throws EOFException if virtual file is to small
     */
    private void writeObjectInFile(final FileAbstractByte fab)
            throws EOFException {
        // Calculate object number
        fab.write16bitLE(this.listObject.size()
                + this.listObjectDrawOnBackground.size()
                + this.listObjectAlwaysOnScreen.size());

        // Running object
        for (ObjectItem obj : this.listObject) {
            obj.writeToFile(fab);
        }

        // Background object
        for (ObjectItem obj : this.listObjectDrawOnBackground) {
            obj.writeToFile(fab);
        }

        // Object always on screen
        for (ObjectItem obj : this.listObjectAlwaysOnScreen) {
            obj.writeToFile(fab);
        }
    }

    /**
     * Write save data in virtual file.
     *
     * @param fab file
     * @throws EOFException if virtual file is to small
     */
    private void writeSaveDataInFile(final FileAbstractByte fab)
            throws EOFException {
        fab.write16bitLE(this.inventoryArea.getLevel());
        fab.write16bitLE(this.inventoryArea.getLife());

        List<EnumInventoryObject> listInventory
                = this.inventoryArea.getObjects();

        fab.write16bitLE(listInventory.size());

        for (EnumInventoryObject eip : listInventory) {
            fab.write16bitLE(eip.getIndex());
        }

        fab.skipBytes(
                (SaveData.MAX_INVENTORY_ENTRY - listInventory.size()) * 2);

        fab.write32bitLE(this.inventoryArea.getScore());

        fab.skipBytes(SaveData.SAVE_HOLE);
    }

    /**
     * Write string of object in virtual file.
     *
     * @param fab file
     * @throws EOFException if virtual file is to small
     */
    private void writeStringObjectInFile(final FileAbstractByte fab)
            throws EOFException {
        // Running object
        writeListObjectString(this.listObject, fab);

        // Background object
        writeListObjectString(this.listObjectDrawOnBackground, fab);

        // Object always on screen
        writeListObjectString(this.listObjectAlwaysOnScreen, fab);
    }

    /**
     * Write string of object.
     *
     * @param listObject list of object
     * @param fab        file to save
     * @throws EOFException if virtual file is to small
     */
    private void writeListObjectString(final List<ObjectEntity> listObject,
            final FileAbstractByte fab) throws EOFException {
        String data;
        int len;
        int indexChar;
        StringItem stringItem;

        // Running object
        for (ObjectEntity obj : listObject) {
            if (obj.getStringStackEntry().isPresent()) {
                stringItem = obj.getStringStackEntry().get();

                data = stringItem.getValue();
                len = data.length();

                fab.write16bitLE(len);

                for (indexChar = 0; indexChar < len; indexChar++) {
                    fab.writeByte(data.charAt(indexChar));
                }

                fab.writeByte(0);
            }
        }
    }

    @Override
    public void recieveMessage(final EnumMessageType type, final Object msg) {
        super.recieveMessage(type, msg);

        ObjectEntity oe;

        switch (type) {
            case CHECK_POINT_CHANGING_LEVEL:
                oe = (ObjectEntity) msg;
                changeLevel(Optional.of(oe.getStringStackEntry().get().getValue()),
                        oe.getCounter());
                break;
            case CHECK_POINT_CHANGING_LEVEL_PREVIOUS:
                changeLevel(Optional.empty(), SaveData.MAP_LEVEL);
                break;
            case DIE_RESTART_LEVEL:
                changeLevel(Optional.of(getCurrentJnFileName()), RESTART_LEVEL_NUMBER);
                break;
            default:
        }
    }

    @Override
    public void run() {
        this.levelMessageWait--;

        if (this.levelMessageBox.isCanchange()
                || (this.levelMessageBox.isEnable()
                && this.levelMessageWait <= 0)) {
            // Do it here to let all data delete
            createAndLoadNewLevel();
        } else if (this.isRestoreLevel) {
            // Do it here to let all data delete
            try {
                loadGameInSaveFile();
            } catch (final ReflectiveOperationException e) {
                LOGGER.severe("Error when load game in save file.");
                e.printStackTrace();
            }
        } else if (this.menu == this.menuHighScore
                && !this.menuHighScore.isEditorMode()) {
            // No name enter, validate
            doMenuValidate();
        } else {
            super.run();
        }
    }

    @Override
    protected void doMenuValidate() {
        if (this.menu == this.menuSaveGame
                && this.menuSaveGame.isEditorMode()) {
            saveGameInFile();

            if (!this.menu.getPreviousMenu().isPresent()) {
                this.menu = this.menuStd;
            }

            this.menuSaveGame.setEditorMode(false);
            this.menuSaveGame.setEnable(false);
        } else if (this.menu == this.menuSaveGame
                && !this.menuSaveGame.isEditorMode()) {
            // Switch menu to enable keyboard
            this.menuSaveGame.setEditorMode(true);
        } else if (this.menu == this.menuLoadGame) {
            // Load saved game
            if (!this.menu.getPreviousMenu().isPresent()) {
                this.menu = this.menuStd;
            }

            this.menuLoadGame.setEnable(false);

            isRestoreLevel = true;
        } else if (this.menu == this.menuHighScore) {
            saveHighScoreMenu();
        } else {
            displayHighScoreMenu();
        }
    }

    /**
     * Display save game menu.
     */
    private void saveGameInFile() {
        final String saveName
                = this.menuSaveGame.getNameSave().trim();
        final int numSave
                = this.menuSaveGame.getNumberSave();

        final SaveGameItem currentSaveGame
                = this.cfgFile.addNewSaveGame(saveName, numSave);

        // Create virtual file
        FileAbstractByte fab = putCurrentLevelInFileMemory();

        try {
            final String filePath
                    = ((JillGameConfig) SimpleGameConfig.getInstance()).
                    getFilePath();

            // Save level
            fab.saveToFile(
                    new File(filePath, currentSaveGame.getSaveGameFile()));

            // Save map
            if (this.levelConfiguration.getLevelMapData().isPresent()) {
                this.levelConfiguration.getLevelMapData().get().saveToFile(
                        new File(filePath, currentSaveGame.getSaveMapFile()));
            } else {
                // Map file is current file
                fab.saveToFile(
                        new File(filePath, currentSaveGame.getSaveMapFile()));
            }

            // Save new save entry
            this.cfgFile.save();
        } catch (final FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Can't save file ! File not found ????",
                    ex);
        } catch (final IOException ex) {
            LOGGER.log(Level.SEVERE, "Can't save file !", ex);
        }
    }

    /**
     * Load game saved in file.
     */
    private void loadGameInSaveFile() throws ReflectiveOperationException {
        final int numSave
                = this.menuLoadGame.getNumberSave();

        final SaveGameItem currentSaveGame
                = this.cfgFile.getSaveGame().get(numSave);

        final String filePath
                = ((JillGameConfig) SimpleGameConfig.getInstance()).
                getFilePath();
        try {
            final File saveFileMap = new File(filePath,
                    currentSaveGame.getSaveMapFile());

            final File saveFileGame = new File(filePath,
                    currentSaveGame.getSaveGameFile());

            if (saveFileMap.exists() && saveFileGame.exists()) {
                // Load map
                final FileAbstractByte mapData =
                        ObjectInstanceFactory.getNewFileByte();
                mapData.load(saveFileMap);

                final LevelConfiguration cfgNewLevel
                        = new JillLevelConfiguration(
                        this.levelConfiguration.getShaFileName(),
                        Optional.of(currentSaveGame.getSaveGameFile()),
                        this.levelConfiguration.getVclFileName(),
                        this.levelConfiguration.getCfgFileName(),
                        this.levelConfiguration.getCfgSavePrefixe(),
                        this.levelConfiguration.getStartScreen(), Optional.of(mapData),
                        Optional.empty());

                // Create next level
                final InterfaceSimpleGameHandleInterface newLevel
                        = new LoadNewLevelHandler(cfgNewLevel);

                SimpleGameHandler.setNewHandler(newLevel);
            }

            this.isRestoreLevel = false;
        } catch (FileNotFoundException ex) {
            LOGGER.log(Level.SEVERE, "Can't load file ! File not found !", ex);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "Can't load file !", ex);
        } catch (ClassNotFoundException |
                IllegalAccessException | InstantiationException ex) {
            LOGGER.log(Level.SEVERE, "Error when load saved level", ex);
        }
    }

    /**
     * Save highscore.
     */
    private void saveHighScoreMenu() {
        final String highScoreName
                = this.menuHighScore.getNameHighScore().trim();

        // Save
        if (this.menuHighScore.isEditorMode() && !highScoreName.isEmpty()) {
            cfgFile.addNewHighScore(this.menuHighScore.getNameHighScore(),
                    this.inventoryArea.getScore());

            try {
                // Save
                this.cfgFile.save();

            } catch (IOException ex) {
                LOGGER.log(Level.SEVERE, "Can't save highscore !", ex);
            }
        }

        this.menu = null;

        if (getStartScreenClass().isPresent()) {
            changeScreenManager(getStartScreenClass().get());
        }
    }

    /**
     * Display highscore menu.
     */
    private void displayHighScoreMenu() {
        // Clear key to not send Enter key of exit menu
        this.keyboardLayout.clear();

        // Exit validate. Display high score menu
        this.menuHighScore.setModeEdit(this.inventoryArea.getScore());

        this.menu = this.menuHighScore;
        this.menu.setEnable(true);
    }

    /**
     * To know if current level is map or not.
     *
     * @return boolean
     */
    protected boolean isCurrentLevelMap() {
        return this.inventoryArea.getLevel() == SaveData.MAP_LEVEL
                || !this.mapLevel.isPresent();
    }

    /**
     * Construct object.
     */
    private void constructor() {
        this.messageDispatcher.addHandler(
                EnumMessageType.CHECK_POINT_CHANGING_LEVEL, this);
        this.messageDispatcher.addHandler(
                EnumMessageType.CHECK_POINT_CHANGING_LEVEL_PREVIOUS, this);
        this.messageDispatcher.addHandler(
                EnumMessageType.DIE_RESTART_LEVEL, this);

        BufferedImage highScore = this.statusBar.createControlArea();

        final RectangleConf controlAreaConf =
                this.statusBar.getControlAreaConf();

        this.menuHighScore = new HighScoreMenu(highScore, this.pictureCache,
                this.cfgFile.getHighScore(), controlAreaConf.getX(),
                controlAreaConf.getY(), Optional.ofNullable(this.menu));

        highScore = this.statusBar.createControlArea();

        this.menuSaveGame = new SaveGameMenu(highScore, pictureCache,
                this.cfgFile.getSaveGame(), controlAreaConf.getX(),
                controlAreaConf.getY());

        highScore = this.statusBar.createControlArea();

        this.menuLoadGame = new LoadGameMenu(highScore, pictureCache,
                this.cfgFile.getSaveGame(), controlAreaConf.getX(),
                controlAreaConf.getY());
    }

    /**
     * Override escape handle.
     *
     * @see org.jill.game.level.AbstractMenuJillLevel
     */
    @Override
    protected void doEscape() {
        if (this.menu == this.menuLoadGame || this.menu == this.menuSaveGame) {
            this.menu.setEnable(false);
            this.menu = this.menuStd;
        } else {
            super.doEscape();
        }
    }
}
