package com.example.wolfenstein.games.objects;

import lombok.Getter;

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

    public void moveForward(double speed, Map map) {
        double newX = posX + dirX * speed;
        double newY = posY + dirY * speed;

        if (!map.isWall((int) newX, (int) newY) || !map.isEnemy((int) newX, (int) newY)) {
            posX = newX;
            posY = newY;
        }
    }

    public void moveBackward(double speed, Map map) {
        double newX = posX - dirX * speed;
        double newY = posY - dirY * speed;

        if (!map.isWall((int) newX, (int) newY) || !map.isEnemy((int) newX, (int) newY)) {
            posX = newX;
            posY = newY;
        }
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

    public void shoot(Map map) {
        double rayX = posX;
        double rayY = posY;

        double stepSize = 0.05;
        double maxDistance = 10.0;

        for (double distance = 0; distance < maxDistance; distance += stepSize) {
            rayX += dirX * stepSize;
            rayY += dirY * stepSize;

            int mapX = (int) rayX;
            int mapY = (int) rayY;

            if (map.isWall(mapX, mapY)) {
                break;
            }

            if (map.isEnemy(mapX, mapY)) {
                map.removeEnemy(mapX, mapY);
                logger.info("🔥 Enemy hit at " + mapX + ", " + mapY);
                break;
            }
        }
    }
}
