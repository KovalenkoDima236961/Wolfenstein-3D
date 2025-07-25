package com.example.wolfenstein.games;

import com.example.wolfenstein.games.objects.*;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Game {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;
    private static final double DELTA_TIME = 1.0 / 60.0;

    private Canvas canvas;
    private GraphicsContext gc;

    private Renderer renderer;

    private Map map;

    private Player player;
    private final List<Bullet> bullets;

    private final List<Enemy> enemies;
    private final List<Bullet> enemyBullets;

    private boolean gameOver = false;

    private double lastUpdateTime = 0;
    private double currentDeltaTime = 1.0 / 60.0;

    private final Set<KeyCode> pressedKeys = new HashSet<>();
    private boolean spacePressedLastFrame = false;

    public Game() {
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
        enemyBullets = new ArrayList<>();
    }

    public void start(Stage stage) {
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();

        map = new Map();
        player = new Player(1.5, 1.5);
        renderer = new Renderer(WIDTH, HEIGHT); // game render
        initEnemy();

        Scene scene = new Scene(new StackPane(canvas));
        stage.setTitle("Wolf 2.5D");
        stage.setScene(scene);
        stage.show();

        setupInput(scene);
        startGameLoop();
    }

    private void initEnemy() {
        for (int x = 0; x < map.getHeight(); x++) {
            for (int y = 0; y < map.getWidth(); y++) {
                if (map.isEnemy(x, y)) {
                    enemies.add(new Enemy(x, y));
                    map.removeEnemy(x, y);
                }
            }
        }
    }


    private void setupInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            if (gameOver) return;
            pressedKeys.add(e.getCode());

            if (e.getCode() == KeyCode.E) {
                tryOpenDoor();
            }
        });

        scene.setOnKeyReleased(e -> pressedKeys.remove(e.getCode()));
    }

    private void handleInput() {
        if (pressedKeys.contains(KeyCode.W)) {
            player.moveForward(2.5 * currentDeltaTime, map, enemies);
            checkCollectibles();
            if (map.isExit((int) player.getPosX(), (int) player.getPosY())) {
                System.out.println("Level Complete! Proceed to next level..");
            }
        }
        if (pressedKeys.contains(KeyCode.S)) {
            player.moveBackward(2.5 * currentDeltaTime, map, enemies);
            checkCollectibles();
            if (map.isExit((int) player.getPosX(), (int) player.getPosY())) {
                System.out.println("Level Complete! Proceed to next level..");
            }
        }
        if (pressedKeys.contains(KeyCode.A)) {
            player.rotateLeft(2.0 * currentDeltaTime);
        }
        if (pressedKeys.contains(KeyCode.D)) {
            player.rotateRight(2.0 * currentDeltaTime);
        }

        // SPACE: Fire one shot per keypress, not autofire
        if (pressedKeys.contains(KeyCode.SPACE)) {
            if (!spacePressedLastFrame) {
                var bullet = player.shoot();
                if (bullet != null) {
                    bullets.add(bullet);
                }
                spacePressedLastFrame = true;
            }
        } else {
            spacePressedLastFrame = false;
        }
    }

    private void checkCollectibles() {
        int px = (int) player.getPosX();
        int py = (int) player.getPosY();

        if (map.isKey(px, py)) {
            player.setKeys(player.getKeys() + 1);
            map.collectItem(px, py);
            System.out.println("Picked up a key! Keys: " + player.getKeys());
        }

        if (map.isHealth(px, py)) {
            player.setHealth(Math.min(1.0, player.getHealth() + 0.5));
            map.collectItem(px, py);
            System.out.println("Picked up a health pack! Health: " + player.getHealth());
        }

        if (map.isAmmo(px, py)) {
            player.setAmmo(player.getAmmo() + 5);
            map.collectItem(px, py);
            System.out.println("Picked up a ammo pack! Ammo: " + player.getAmmo());
        }
    }

    private void tryOpenDoor() {
        int facingX = (int)(player.getPosX() + player.getDirX());
        int facingY = (int)(player.getPosY() + player.getDirY());
        if (map.isDoor(facingX, facingY)) {
            map.openDoor(facingX, facingY);
        } else if (map.isLockedDoor(facingX, facingY)) {
            if (player.getKeys() > 0) {
                map.unlockDoor(facingX, facingY);
                player.setKeys(player.getKeys() - 1);
                System.out.println("Unlocked a door! Remaining keys: " + player.getKeys());
            } else {
                System.out.println("Need a key to open this door!");
            }
        }
    }

    private void updateBullets() {
        List<Bullet> toRemove = new ArrayList<>();
        for (Bullet bullet : bullets) {
            bullet.setX(bullet.getX() + bullet.getDirX() * bullet.getSpeed());
            bullet.setY(bullet.getY() + bullet.getDirY() * bullet.getSpeed());

            int gridX = (int) bullet.getX();
            int gridY = (int) bullet.getY();

            // Check distance from player
            double dx = bullet.getX() - player.getPosX();
            double dy = bullet.getY() - player.getPosY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            // Remove bullet if it hit a wall or traveled too far
            if (map.isWall(gridX, gridY) || distance > bullet.getMaxDistance()) {
                toRemove.add(bullet);
                continue;
            }

            // Check collision with any enemy
            for (Enemy enemy : enemies) {
                if ((int)enemy.getX() == gridX && (int)enemy.getY() == gridY) {
                    enemy.setHealth(enemy.getHealth() - player.getDamage());
                    toRemove.add(bullet);
                    break;
                }
            }
        }
        bullets.removeAll(toRemove);
        enemies.removeIf(Enemy::isDead);
    }

    private void updateEnemyBullets() {
        List<Bullet> toRemove = new ArrayList<>();
        for (Bullet bullet : enemyBullets) {
            bullet.setX(bullet.getX() + bullet.getDirX() * bullet.getSpeed());
            bullet.setY(bullet.getY() + bullet.getDirY() * bullet.getSpeed());

            int gridX = (int) bullet.getX();
            int gridY = (int) bullet.getY();

            double dx = bullet.getX() - player.getPosX();
            double dy = bullet.getY() - player.getPosY();
            double distance = Math.sqrt(dx * dx + dy * dy);

            if (map.isWall(gridX, gridY) || distance > bullet.getMaxDistance()) {
                toRemove.add(bullet);
                continue;
            }

            double hitDist = 0.25;
            if (Math.abs(player.getPosX() - bullet.getX()) < hitDist && Math.abs(player.getPosY() - bullet.getY()) < hitDist) {
                player.takeDamage(0.3);
                toRemove.add(bullet);
                if (player.isDead()) {
                    onGameOver();
                }
            }
        }
        enemyBullets.removeAll(toRemove);
    }

    private void updateEnemies() {
        for (Enemy enemy : enemies) {
            if (enemy.isDead()) continue;

            double dx = player.getPosX() - enemy.getX();
            double dy = player.getPosY() - enemy.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (enemyCanSeePlayer(enemy, player, map)) {
                if (dist < enemy.getAttackRange()) {
                    enemy.setState(EnemyState.ATTACKING);

                    if (enemy.getShootCooldown() > 0)
                        enemy.setShootCooldown(enemy.getShootCooldown() - DELTA_TIME);

                    if (enemy.getShootCooldown() <= 0) {
                        double dirX = dx / dist;
                        double dirY = dy / dist;
                        enemyBullets.add(new Bullet(
                                enemy.getX(), enemy.getY(),
                                dirX, dirY,
                                0.035,
                                8.0
                        ));
                        enemy.setShootCooldown(enemy.getShootInterval());
                    }
                } else if (dist < enemy.getChasingRange()) {
                    enemy.setState(EnemyState.CHASING);

                    double step = enemy.getSpeed();
                    double moveX = dx / dist * step;
                    double moveY = dy / dist * step;

                    double newX = enemy.getX() + moveX;
                    if (!map.isWall((int) newX, (int) enemy.getY()) && !enemyAt((int) newX, (int) enemy.getY(), enemy))
                        enemy.setX(newX);

                    double newY = enemy.getY() + moveY;
                    if (!map.isWall((int) enemy.getX(), (int) newY) && !enemyAt((int) enemy.getX(), (int) newY, enemy))
                        enemy.setY(newY);
                } else {
                    enemy.setState(EnemyState.PATROL);
                }
            } else {
                enemy.setState(EnemyState.PATROL);
            }

            if (enemy.getState() == EnemyState.PATROL) {
                double patrolStep = enemy.getSpeed() * 0.75;
                double nextX = enemy.getX() + enemy.getDirX() * patrolStep;
                double nextY = enemy.getY() + enemy.getDirY() * patrolStep;

                if (!map.isWall((int) nextX, (int) nextY) && !enemyAt((int) nextX, (int) nextY, enemy)) {
                    enemy.setX(nextX);
                    enemy.setY(nextY);
                } else {
                    enemy.setDirX(-enemy.getDirX());
                    enemy.setDirY(-enemy.getDirY());
                }
            }
        }
    }



    private boolean enemyCanSeePlayer(Enemy enemy, Player player, Map map) {
        double x0 = enemy.getX();
        double y0 = enemy.getY();

        double x1 = player.getPosX();
        double y1 = player.getPosY();

        double dx = x1 - x0;
        double dy = y1 - y0;

        double dist = Math.sqrt(dx * dx + dy * dy);

        int steps = (int) (dist * 10);
        for (int i = 1; i < steps; i++) {
            double tx = x0 + dx * i / steps;
            double ty = y0 + dy * i / steps;
            if (map.isWall((int) tx, (int) ty)) return false;
        }
        return true;
    }

    private boolean enemyAt(int x, int y, Enemy self) {
        for (Enemy other : enemies)
            if (other != self && (int) other.getX() == x && (int) other.getY() == y)
                return true;
        return false;
    }

    private void onGameOver() {
        gameOver = true;
        System.out.println("GAME OVER");

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Game Over");
            alert.setHeaderText("You died!");
            alert.setContentText("Better luck next time!");
            alert.showAndWait();
        });
    }

    private void startGameLoop() {
        new AnimationTimer() {
            public void handle(long now) {
                if (gameOver) return;
                if (lastUpdateTime == 0) lastUpdateTime = now;

                double deltaTime = (now - lastUpdateTime) / 1e9;

                // Clamp deltaTime to max 0.1 (100 ms) for smoothness
                if (deltaTime > 0.1) deltaTime = 0.1;

                currentDeltaTime = deltaTime;
                lastUpdateTime = now;

                handleInput();

                gc.clearRect(0, 0, WIDTH, HEIGHT);
                updateEnemies();
                updateBullets();
                updateEnemyBullets();
                renderer.render(gc, player, map, bullets, enemyBullets, enemies);
                renderer.renderWeapon(gc, player);
                renderer.renderHUD(gc, player);
            }
        }.start();
    }
}
