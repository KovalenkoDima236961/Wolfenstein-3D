package com.example.wolfenstein.games.objects;

public class Map {
    private final int [][] mapData;

    public Map() {
        // // 0 = empty, 1 = wall, 2 = enemy, 3 = closed door, 4 = exit, 5 = locked door
        this.mapData = new int[][]{
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1},
                {1,0,0,0,0,3,0,1,0,0,0,0,2,0,0,1},
                {1,0,1,1,1,0,0,1,0,1,1,1,1,1,0,1},
                {1,0,0,0,1,0,2,0,0,0,0,0,0,1,0,1},
                {1,1,1,0,1,0,1,1,1,1,0,1,0,1,0,1},
                {1,0,0,0,0,0,0,0,1,0,0,1,0,1,0,1},
                {1,0,1,1,1,1,1,0,1,0,1,1,0,1,0,1},
                {1,0,1,2,0,0,1,0,1,0,1,0,0,1,0,1},
                {1,0,1,1,1,0,1,0,1,0,1,0,1,1,0,1},
                {1,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1},
                {1,1,1,0,1,1,1,1,1,1,1,0,1,0,1,1},
                {1,0,0,0,0,0,0,0,1,0,0,0,1,0,0,1},
                {1,0,1,1,1,0,1,0,1,1,1,0,1,1,0,1},
                {1,0,1,0,0,0,1,0,0,0,1,0,2,0,0,1},
                {1,2,1,0,1,1,1,1,1,0,1,1,1,1,4,1}, // exit at (14,14)
                {1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
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

    public boolean isDoor(int x, int y) {
        return getTile(x, y) == 3;
    }

    public boolean isLockedDoor(int x, int y) {
        return getTile(x, y) == 5;
    }

    public boolean isExit(int x, int y) {
        return getTile(x, y) == 4;
    }

    public void openDoor(int x, int y) {
        if (isDoor(x, y)) mapData[y][x] = 0;
    }

    public void unlockDoor(int x, int y) {
        if (isLockedDoor(x, y)) mapData[y][x] = 3;
    }
}
