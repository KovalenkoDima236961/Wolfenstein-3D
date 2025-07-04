package com.example.wolfenstain.games.objects;

public class Map {
    private final int [][] mapData;

    public Map() {
        this.mapData = new int[][] {
                {1, 1, 1, 1, 1, 1, 1, 1},
                {1, 0, 0, 0, 0, 0, 0, 1},
                {1, 0, 1, 0, 1, 0, 0, 1},
                {1, 0, 0, 2, 0, 0, 0, 1},
                {1, 1, 1, 1, 1, 1, 1, 1}
        };
    }

    public int getTile(int x, int y) {
        if (x < 0 || y < 0 || y >= mapData.length || x >= mapData[0].length) {
            return 1;
        }
        return mapData[y][x];
    }

    public boolean isWall(int x, int y) {
        return getTile(x, y) == 1;
    }

    public int getWidth() {
        return mapData[0].length;
    }

    public int getHeight() {
        return mapData.length;
    }

    public boolean isEnemy(int x, int y) {
        return getTile(x, y) == 2;
    }

    public void removeEnemy(int x, int y) {
        if (getTile(x, y) == 2) {
            mapData[y][x] = 0;
        }
    }
}
