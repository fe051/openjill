/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jill.sha;

import java.awt.Color;

/**
 * Vga Color map.
 *
 * @author emeric_martineau
 */
public class CgaColorMapImpl implements ColorMap {
    @Override
    public Color[] getColorMap() {
        return InternalColorMapImpl.CGA_COLOR_MAP.getColorMap();
    }
}
