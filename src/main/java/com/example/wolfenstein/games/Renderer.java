package com.example.wolfenstein.games;

import com.example.wolfenstein.games.objects.Bullet;
import com.example.wolfenstein.games.objects.Enemy;
import com.example.wolfenstein.games.objects.Map;
import com.example.wolfenstein.games.objects.Player;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class Renderer {
    private final int screenWidth;
    private final int screenHeight;

    public Renderer(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void render(GraphicsContext gc, Player player, Map map, List<Bullet> bullets, List<Bullet> enemyBullets,List<Enemy> enemies) {
        // Draw sky
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, screenWidth, (double) screenHeight / 2);

        // Draw Floor
        gc.setFill(Color.DARKGRAY);
        gc.fillRect(0, (double) screenHeight / 2, screenWidth, (double) screenHeight / 2);

        double[] zBuffer = new double[screenWidth];

        // Simulate what we see if we looked straight ahead, column by column
        for (int x = 0; x < screenWidth; x++) {

            // Calculate ray direction
            double cameraX = 2 * x / (double) screenWidth - 1;
            double rayDirX = player.getDirX() + player.getPlaneX() * cameraX;
            double rayDirY = player.getDirY() + player.getPlaneY() * cameraX;

            int mapX = (int) player.getPosX();
            int mapY = (int) player.getPosY();

            // track the distance to the next X or Y gridline from the player's current position
            double sideDistX;
            double sideDistY;

            // tell how far to go in X or Y to cross gridline
            double deltaDistX = (rayDirX == 0) ? 1e30 : Math.abs(1 / rayDirX);
            double deltaDistY = (rayDirY == 0) ? 1e30 : Math.abs(1 / rayDirY);

            double perpWallDist;

            // tell which way to go in the grid
            int stepX;
            int stepY;

            boolean hit = false;
            int side = 0;

            if (rayDirX < 0) {
                stepX = -1;
                sideDistX = (player.getPosX() - mapX) * deltaDistX;
            } else {
                stepX = 1;
                sideDistX = (mapX + 1.0 - player.getPosX()) * deltaDistX;
            }

            if (rayDirY < 0) {
                stepY = -1;
                sideDistY = (player.getPosY() - mapY) * deltaDistY;
            } else {
                stepY = 1;
                sideDistY = (mapY + 1.0 - player.getPosY()) * deltaDistY;
            }

            // Step to the next nearest gridline (X or Y), whichever is close
            while (!hit) {
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX;
                    mapX += stepX;
                    side = 0;
                } else {
                    sideDistY += deltaDistY;
                    mapY += stepY;
                    side = 1;
                }

                if (map.isWall(mapX, mapY)) hit = true;
            }

            // Calculates true straight-line distance to the wall, correcting for the angle
            if (side == 0) {
                assert rayDirX > 0;
                perpWallDist = (mapX - player.getPosX() + (double) (1 - stepX) / 2) / rayDirX;
            } else {
                assert rayDirY > 0;
                perpWallDist = (mapY - player.getPosY() + (double) (1 - stepY) / 2) / rayDirY;
            }

            zBuffer[x] = perpWallDist;

            // Draw the wall slice
            int lineHeight = (int) (screenHeight / perpWallDist);
            int drawStart = -lineHeight / 2 + screenHeight / 2;
            if (drawStart < 0) drawStart = 0;
            int drawEnd = lineHeight / 2 + screenHeight / 2;
            if (drawEnd >= screenHeight) drawEnd = screenHeight - 1;

            if (map.isDoor(mapX, mapY)) {
                gc.setStroke(Color.SADDLEBROWN);
                gc.strokeLine(x, drawStart, x, drawEnd);
            } else if (map.isLockedDoor(mapX, mapY)) {
                gc.setStroke(Color.PURPLE);
                gc.strokeLine(x, drawStart, x, drawEnd);
            } else if (map.isExit(mapX, mapY)) {
                gc.setStroke(Color.GOLD);
                gc.strokeLine(x, drawStart, x, drawEnd);
            } else {
                gc.setStroke(side == 0 ? Color.RED : Color.DARKRED);
                gc.strokeLine(x, drawStart, x, drawEnd);
            }
        }

        enemies.forEach(enemy -> renderEnemy(gc, player, enemy.getX(), enemy.getY(), zBuffer));
        bullets.forEach(bullet -> renderBullet(gc, bullet, player, zBuffer));
        enemyBullets.forEach(bullet -> renderEnemyBullet(gc, bullet, player, zBuffer));
    }

    private void renderBullet(GraphicsContext gc, Bullet bullet, Player player, double[] zBuffer) {
        double dx = bullet.getX() - player.getPosX();
        double dy = bullet.getY() - player.getPosY();

        // Камерні координати
        double invDet = 1.0 /  (player.getPlaneX() * player.getDirY() - player.getDirX() * player.getPlaneY());
        double transformX = invDet * (player.getDirY() * dx - player.getDirX() * dy);
        double transformY = invDet * (-player.getPlaneY() * dx + player.getPlaneX() * dy);

        if (transformY <= 0) return;

        int bulletScreenX = (int) ((screenWidth / 2.0) * (1 + transformX / transformY));

        int bulletSize = (int) Math.max(3, Math.abs(screenHeight / (transformY * 16)));

        int bulletScreenY = screenHeight / 2  - bulletSize / 2;

        if (bulletScreenX >= 0 && bulletScreenX < screenWidth && transformY < zBuffer[bulletScreenX]) {
            gc.setFill(Color.BLUE);
            gc.fillOval(bulletScreenX - bulletSize / 2.0, bulletScreenY, bulletSize, bulletSize);
        }
    }

    private void renderEnemyBullet(GraphicsContext gc, Bullet bullet, Player player, double[] zBuffer) {
        double dx = bullet.getX() - player.getPosX();
        double dy = bullet.getY() - player.getPosY();

        // Камерні координати
        double invDet = 1.0 /  (player.getPlaneX() * player.getDirY() - player.getDirX() * player.getPlaneY());
        double transformX = invDet * (player.getDirY() * dx - player.getDirX() * dy);
        double transformY = invDet * (-player.getPlaneY() * dx + player.getPlaneX() * dy);

        if (transformY <= 0) return;

        int bulletScreenX = (int) ((screenWidth / 2.0) * (1 + transformX / transformY));

        int bulletSize = (int) Math.max(3, Math.abs(screenHeight / (transformY * 16)));

        int bulletScreenY = screenHeight / 2  - bulletSize / 2;

        if (bulletScreenX >= 0 && bulletScreenX < screenWidth && transformY < zBuffer[bulletScreenX]) {
            gc.setFill(Color.RED);
            gc.fillOval(bulletScreenX - bulletSize / 2.0, bulletScreenY, bulletSize, bulletSize);
        }
    }

    private void renderEnemy(GraphicsContext gc, Player player, double enemyX, double enemyY, double[] zBuffer) {
        // Вектор від гравця до ворога
        double dx = enemyX - player.getPosX();
        double dy = enemyY - player.getPosY();

        // Камерні координати
        double invDet = 1.0 /  (player.getPlaneX() * player.getDirY() - player.getDirX() * player.getPlaneY());
        double transformX = invDet * (player.getDirY() * dx - player.getDirX() * dy);
        double transformY = invDet * (-player.getPlaneY() * dx + player.getPlaneX() * dy);

        if (transformY <= 0) return;

        int spriteScreenX = (int) (((double) screenWidth / 2) * (1 + transformX / transformY));
        int spriteHeight = Math.abs((int) (screenHeight / transformY));
        int drawStartY = -spriteHeight / 2 + screenHeight / 2;
        int drawEndY = spriteHeight / 2 + screenHeight / 2;

        int spriteWidth = spriteHeight;
        int drawStartX = -spriteWidth / 2 + spriteScreenX;
        int drawEndX = spriteWidth / 2 + spriteScreenX;

        for (int stripe = drawStartX; stripe < drawEndX; stripe++) {
            if (stripe < 0 || stripe >= screenWidth) continue;
            if (transformY < zBuffer[stripe]) { // Check if the enemy is visible at this column
                gc.setFill(Color.LIMEGREEN);
                gc.fillRect(stripe, drawStartY, 1, drawEndY - drawStartY);
            }
        }
    }
}
