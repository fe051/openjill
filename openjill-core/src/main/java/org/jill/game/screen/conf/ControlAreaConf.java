package org.jill.game.screen.conf;

import java.util.List;
import java.util.Map;

/**
 * Config class to control area.
 *
 * @author Emeric MARINEAU
 */
public final class ControlAreaConf extends AbstractLineTextConf {

    /**
     * Special key.
     */
    private List<TextToDraw> specialKey;

    /**
     * Turtle bullet.
     */
    private TextToDraw turtleBullet;

    /**
     * NoiseBullet.
     */
    private TextToDraw noiseBullet;

    /**
     * Alt key text by inventory.
     */
    private Map<String, String> altKeyText;

    /**
     * Special key.
     *
     * @return special key
     */
    public List<TextToDraw> getSpecialKey() {
        return specialKey;
    }

    /**
     * Special key.
     *
     * @param sk special key
     */
    public void setSpecialKey(final List<TextToDraw> sk) {
        this.specialKey = sk;
    }

    /**
     * Turtle bullet.
     *
     * @return bullet
     */
    public TextToDraw getTurtleBullet() {
        return turtleBullet;
    }

    /**
     * Turtle bullet.
     *
     * @param tb bullet positon
     */
    public void setTurtleBullet(final TextToDraw tb) {
        this.turtleBullet = tb;
    }

    /**
     * Noise bullet.
     *
     * @return bullet
     */
    public TextToDraw getNoiseBullet() {
        return noiseBullet;
    }

    /**
     * Noise bullet.
     *
     * @param nb bullet positon
     */
    public void setNoiseBullet(final TextToDraw nb) {
        this.noiseBullet = nb;
    }

    /**
     * Alt text.
     *
     * @return alt text
     */
    public Map<String, String> getAltKeyText() {
        return altKeyText;
    }

    /**
     * Alt text.
     *
     * @param altText alt text
     */
    public void setAltKeyText(final Map<String, String> altText) {
        this.altKeyText = altText;
    }
}
