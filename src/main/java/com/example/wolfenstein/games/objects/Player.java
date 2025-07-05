package com.example.wolfenstein.games.objects;

import lombok.Getter;

import java.util.List;
import java.util.logging.Logger;

@Getter
public class Player {
    private final Logger logger = Logger.getLogger(this.getClass().getName());

    // Player position
    private double posX;
    private double posY;

    // Which way the player is looking
    private double dirX;
    private double dirY;

    // Defines the width of camera for perspective for FOV
    private double planeX;
    private double planeY;

    public Player(double startX, double startY) {
        this.posX = startX;
        this.posY = startY;

        // faces left
        this.dirX = -1;
        this.dirY = 0;

        this.planeX = 0;
        this.planeY = 0.66;
    }

    public void moveForward(double speed, Map map, List<Enemy> enemyList) {
        double newX = posX + dirX * speed;
        double newY = posY + dirY * speed;

        if (!map.isWall((int) newX, (int) posY) && !enemyAt((int)newX, (int)posY, enemyList))
            posX = newX;
        if (!map.isWall((int) posX, (int) newY) && !enemyAt((int)posX, (int)newY, enemyList))
            posY = newY;
    }

    public void moveBackward(double speed, Map map, List<Enemy> enemyList) {
        double newX = posX - dirX * speed;
        double newY = posY - dirY * speed;

        if (!map.isWall((int) newX, (int) posY) && !enemyAt((int)newX, (int)posY, enemyList))
            posX = newX;
        if (!map.isWall((int) posX, (int) newY) && !enemyAt((int)posX, (int)newY, enemyList))
            posY = newY;
    }

    private boolean enemyAt(int x, int y, List<Enemy> enemyList) {
        for (Enemy enemy : enemyList)
            if ((int)enemy.getX() == x && (int)enemy.getY() == y)
                return true;
        return false;
    }

    // Rotation of a 2D vector using a rotation matrix
    public void rotateLeft(double rotateSpeed) {
        double oldDirX = dirX;
        double oldPlaneX = planeX;

        dirX = dirX * Math.cos(rotateSpeed) - dirY * Math.sin(rotateSpeed);
        dirY = oldDirX * Math.sin(rotateSpeed) + dirY * Math.cos(rotateSpeed);

        planeX = planeX * Math.cos(rotateSpeed) - planeY * Math.sin(rotateSpeed);
        planeY = oldPlaneX * Math.sin(rotateSpeed) + planeY * Math.cos(rotateSpeed);
    }

    public void rotateRight(double rotateSpeed) {
        double oldDirX = dirX;
        double oldPlaneX = planeX;

        dirX = dirX * Math.cos(-rotateSpeed) - dirY * Math.sin(-rotateSpeed);
        dirY = oldDirX * Math.sin(-rotateSpeed) + dirY * Math.cos(-rotateSpeed);

        planeX = planeX * Math.cos(-rotateSpeed) - planeY * Math.sin(-rotateSpeed);
        planeY = oldPlaneX * Math.sin(-rotateSpeed) + planeY * Math.cos(-rotateSpeed);
    }

    public Bullet shoot() {
        return new Bullet(posX, posY, dirX, dirY, 0.005, 10.0);
    }
}
