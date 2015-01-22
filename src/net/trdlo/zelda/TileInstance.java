/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.trdlo.zelda;


public class TileInstance {
    private Tile tile;
    private int timeOffset;

    public TileInstance(Tile tile, int timeOffset) {
        this.tile = tile;
        this.timeOffset = timeOffset;
    }

    public Tile getTile() {
        return tile;
    }

    public int getTimeOffset() {
        return timeOffset;
    }

    public void setTile(Tile tile) {
        this.tile = tile;
    }

    public void setTimeOffset(int timeOffset) {
        this.timeOffset = timeOffset;
    }
    
}
