package com.example.wolfenstein.games;

import com.example.wolfenstein.games.objects.Map;
import com.example.wolfenstein.games.objects.Player;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Renderer {
    private final int screenWidth;
    private final int screenHeight;

    public Renderer(int screenWidth, int screenHeight) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
    }

    public void render(GraphicsContext gc, Player player, Map map) {
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(0, 0, screenWidth, (double) screenHeight / 2);

        gc.setFill(Color.DARKGRAY);
        gc.fillRect(0, (double) screenHeight / 2, screenWidth, (double) screenHeight / 2);

        for (int x = 0; x < screenWidth; x++) {
            // Обчислення напряму променя
            double cameraX = 2 * x / (double) screenWidth - 1; //  нормалізоване значення від -1 (ліво) до +1 (право)

            // фактичний напрям кожного променя, з урахуванням напрямку гравця та FOV.
            double rayDirX = player.getDirX() + player.getPlaneX() * cameraX;
            double rayDirY = player.getDirY() + player.getPlaneY() * cameraX;

            int mapX = (int) player.getPosX();
            int mapY = (int) player.getPosY();

            //  відстань, яку промінь повинен пройти, щоб перетнути наступну сітку по X або Y.
            double sideDistX;
            double sideDistY;
            double deltaDistX = (rayDirX == 0) ? 1e30 : Math.abs(1 / rayDirX);
            double deltaDistY = (rayDirY == 0) ? 1e30 : Math.abs(1 / rayDirY);
            double perpWallDist;

            int stepX;
            int stepY;
            boolean hit = false;
            int side = 0;

            // Обчислення початкової відстані і напрямку кроку
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

            // Тут обчислюється реальна відстань до стіни, яка не залежить від кута (перпендикулярна). Це потрібно, щоб уникнути ефекту "риб'ячого ока".
            if (side == 0) {
                perpWallDist = (mapX - player.getPosX() + (double) (1 - stepX) / 2) / rayDirX;
            } else {
                perpWallDist = (mapY - player.getPosY() + (double) (1 - stepY) / 2) / rayDirY;
            }

            // Чим ближче стіна — тим вищою виглядає.
            int lineHeight = (int) (screenHeight / perpWallDist);
            // Ми відцентровуємо стіну на екрані і обмежуємо межі.
            int drawStart = -lineHeight / 2 + screenHeight / 2;
            if (drawStart < 0) drawStart = 0;
            int drawEnd = lineHeight / 2 + screenHeight / 2;
            if (drawEnd >= screenHeight) drawEnd = screenHeight - 1;

            gc.setStroke(side == 0 ? Color.RED : Color.DARKRED);
            gc.strokeLine(x, drawStart, x, drawEnd);
        }
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                if (map.getTile(x, y) == 2) {
                    renderEnemy(gc, player, x + 0.5, y + 0.5); // Центр клітинки
                }
            }
        }
    }

    private void renderEnemy(GraphicsContext gc, Player player, double enemyX, double enemyY) {
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
        int drawStartX = -spriteWidth / 2  + spriteScreenX;
        int drawEndX = spriteWidth / 2 + spriteScreenX;

        gc.setFill(Color.LIMEGREEN);
        gc.fillRect(drawStartX, drawStartY, drawEndX - drawStartX, drawEndY - drawStartY);
    }
}
