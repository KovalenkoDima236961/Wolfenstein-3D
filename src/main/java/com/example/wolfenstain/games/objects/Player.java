package com.example.wolfenstain.games.objects;

import lombok.Getter;

import static java.lang.Math.cos;
import static java.lang.Math.sin;

@Getter
public class Player {
    private double posX, posY;
    private double dirX, dirY; // Вектор напряму (куди дивиться)
    private double planeX, planeY; // Вектор камери (права перпендикулярна до напрямку)

    public Player(double startX, double startY) {
        this.posX = startX;
        this.posY = startY;

        // Початковий напрямок (вліво)
        this.dirX = -1;
        this.dirY = 0;

        // "Камера" для FOV (field of view = ~66 градусів)
        this.planeX = 0;
        this.planeY = 0.66;
    }

    public void moveForward(double speed, Map map) {
        double newX = posX + dirX * speed;
        double newY = posY + dirY * speed;

        if (!map.isWall((int) newX, (int) newY)) posX = newX;
        if (!map.isWall((int) newX, (int) newY)) posY = newY;
    }

    public void moveBackward(double moveSpeed, Map map) {
        double newX = posX - dirX * moveSpeed;
        double newY = posY - dirY * moveSpeed;

        if (!map.isWall((int)newX, (int)posY)) posX = newX;
        if (!map.isWall((int)posX, (int)newY)) posY = newY;
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
                System.out.println("🔥 Enemy hit at " + mapX + ", " + mapY);
                break;
            }
        }
    }
}
