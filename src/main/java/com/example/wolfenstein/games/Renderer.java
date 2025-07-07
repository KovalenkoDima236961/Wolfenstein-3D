package com.example.wolfenstein.games;

import com.example.wolfenstein.games.objects.*;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Renderer {
    private final int screenWidth;
    private final int screenHeight;

    private static final java.util.Map<Integer, Image> wallTextures = new HashMap<>();

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
            } else if (map.isKey(mapX, mapY)) {
                gc.setFill(Color.GOLD);
                gc.fillOval(x, drawStart, x, drawEnd);
            } else if (map.isHealth(mapX, mapY)) {
                gc.setFill(Color.GREEN);
                gc.fillOval(x, drawStart, x, drawEnd);
            } else if (map.isAmmo(mapX, mapY)) {
                gc.setFill(Color.DEEPSKYBLUE);
                gc.fillOval(x, drawStart, x, drawEnd);
            } else if (map.isWall(mapX, mapY)) {
                int wallType = map.getTile(mapX, mapY);
                GameObject wallObj = GameObject.fromValue(wallType);
                if (wallObj != null) {
                    Image wallImage = wallTextures.computeIfAbsent(
                            wallType,
                            id -> new Image(Objects.requireNonNull(
                                    Renderer.class.getResourceAsStream(wallObj.getImagePathForWalls())
                            ))
                    );
                    int texWidth = (int) wallImage.getWidth();
                    double wallX;
                    if (side == 0) {
                        wallX = player.getPosY() + perpWallDist * rayDirY;
                    } else {
                        wallX = player.getPosX() + perpWallDist * rayDirX;
                    }
                    wallX -= Math.floor(wallX);

                    int texX = (int)(wallX * texWidth);
                    if ((side == 0 && rayDirX > 0) || (side == 1 && rayDirY < 0)) {
                        texX = texWidth - texX - 1;
                    }

                    gc.drawImage(
                            wallImage,
                            texX, 0, 1, wallImage.getHeight(),  // source rect (1px wide)
                            x, drawStart, 1, drawEnd - drawStart // dest rect (stretch to screen)
                    );

                    if (side == 1) {
                        gc.setGlobalAlpha(0.4);
                        gc.setFill(Color.BLACK);
                        gc.fillRect(x, drawStart, 1, drawEnd - drawStart);
                        gc.setGlobalAlpha(1.0);
                    }
                }
            } else {
                gc.setStroke(side == 0 ? Color.RED : Color.DARKRED);
                gc.strokeLine(x, drawStart, x, drawEnd);
            }
        }

        enemies.forEach(enemy -> renderEnemy(gc, player, enemy.getX(), enemy.getY(), zBuffer));
        bullets.forEach(bullet -> renderBullet(gc, bullet, player, zBuffer));
        enemyBullets.forEach(bullet -> renderEnemyBullet(gc, bullet, player, zBuffer));
    }

    public void renderWeapon(GraphicsContext gc, Player player) {
        String weaponImagePath = "/com/example/wolfenstein/images/weapons/Jagpistol.png";
        try {
            Image weaponImage = new Image(Objects.requireNonNull(Renderer.class.getResourceAsStream(weaponImagePath)));
            double weaponWidth = screenWidth * 0.30;
            double weaponHeight = screenHeight * 0.30;
            double x = (screenWidth - weaponWidth) / 2.0;

            int hudHeight = 90;
            double y = screenHeight - hudHeight - weaponHeight + 20;

            gc.drawImage(weaponImage, x, y, weaponWidth, weaponHeight);
        } catch (Exception e) {
            // fallback
            gc.setFill(Color.BLACK);
            int hudHeight = 90;
            double w = screenWidth * 0.20;
            double h = screenHeight * 0.15;
            gc.fillRect((screenWidth - w) / 2.0, screenHeight - hudHeight - h, w, h);
        }
    }


    public void renderHUD(GraphicsContext gc, Player player) {
        int hudHeight = 90;
        int sectionCount = 7; // Level, Score, Lives, Face, Health, Ammo, Weapon
        int sectionWidth = screenWidth / sectionCount;
        int y = screenHeight - hudHeight;

        // Draw blue HUD background
        gc.setFill(Color.rgb(11, 39, 132));
        gc.fillRect(0, y, screenWidth, hudHeight);

        // Draw box borders
        gc.setStroke(Color.rgb(190, 208, 255));
        gc.setLineWidth(3);
        for (int i = 0; i < sectionCount; i++) {
            gc.strokeRect(i * sectionWidth, y, sectionWidth, hudHeight);
        }

        gc.setLineWidth(1);

        // Set text font
        gc.setFont(javafx.scene.text.Font.font("Consolas", 32));
        gc.setFill(Color.WHITE);

        // For each box, compute centerX and centerY (middle of box)
        double baseY = y;
        double boxCenterY = baseY + hudHeight / 2.2;

        // 1. Level
        drawCenteredText(gc, "LEVEL", sectionWidth * 0.5, baseY + 32);
        drawCenteredText(gc, String.valueOf(player.getLevel()), sectionWidth * 0.5, boxCenterY + 18);

        // 2. Score
        drawCenteredText(gc, "SCORE", sectionWidth * 1.5, baseY + 32);
        drawCenteredText(gc, String.format("%06d", player.getScore()), sectionWidth * 1.5, boxCenterY + 18);

        // 3. Lives
        drawCenteredText(gc, "LIVES", sectionWidth * 2.5, baseY + 32);
        drawCenteredText(gc, String.valueOf(player.getLives()), sectionWidth * 2.5, boxCenterY + 18);

        // 4. Face (center image in box)
        try {
            Image faceImage = new Image(Objects.requireNonNull(Renderer.class.getResourceAsStream("/com/example/wolfenstein/images/hud/character.jpeg")));
            double faceSize = hudHeight - 18;
            double faceX = sectionWidth * 3 + (sectionWidth - faceSize) / 2;
            double faceY = baseY + (hudHeight - faceSize) / 2;
            gc.drawImage(faceImage, faceX, faceY, faceSize, faceSize);
        } catch (Exception e) {
            // fallback: draw a brown box
            gc.setFill(Color.BROWN);
            gc.fillRect(sectionWidth * 3 + 8, baseY + 8, sectionWidth - 16, hudHeight - 16);
        }

        // 5. Health
        gc.setFill(Color.WHITE);
        drawCenteredText(gc, "HEALTH", sectionWidth * 4.5, baseY + 32);
        drawCenteredText(gc, String.format("%3d%%", (int)(player.getHealth())), sectionWidth * 4.5, boxCenterY + 18);

        // 6. Ammo
        drawCenteredText(gc, "AMMO", sectionWidth * 5.5, baseY + 32);
        drawCenteredText(gc, String.valueOf(player.getAmmo()), sectionWidth * 5.5, boxCenterY + 18);

        // 7. Weapon (center image in last box)
        try {
            Image weaponImage = new Image(Objects.requireNonNull(Renderer.class.getResourceAsStream("/com/example/wolfenstein/images/hud/weapon/Pistol.png")));
            double weaponSize = hudHeight - 24; // a bit smaller for padding
            double weaponX = sectionWidth * 6 + (sectionWidth - weaponSize) / 2;
            double weaponY = baseY + (hudHeight - weaponSize) / 2;
            gc.drawImage(weaponImage, weaponX, weaponY, weaponSize, weaponSize);
        } catch (Exception e) {
            // fallback: draw a black gun shape
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(6);
            double gunCenterX = sectionWidth * 6 + sectionWidth / 2.0;
            double gunY = baseY + hudHeight / 2.0;
            gc.strokeLine(gunCenterX - 24, gunY - 5, gunCenterX + 28, gunY - 5);
            gc.strokeLine(gunCenterX + 8, gunY - 17, gunCenterX + 8, gunY + 15);
            gc.strokeLine(gunCenterX - 6, gunY + 10, gunCenterX + 22, gunY + 16);
            gc.setLineWidth(1);
        }
    }

    private void drawCenteredText(GraphicsContext gc, String text, double centerX, double centerY) {
        Font font = gc.getFont();
        Text tempText = new Text(text);
        tempText.setFont(font);
        double textWidth = tempText.getLayoutBounds().getWidth();
        double textHeight = tempText.getLayoutBounds().getHeight();
        gc.fillText(text, centerX - textWidth / 2, centerY + textHeight / 4);
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
