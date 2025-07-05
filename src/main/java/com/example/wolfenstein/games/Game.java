package com.example.wolfenstein.games;

import com.example.wolfenstein.games.objects.*;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

import static com.example.wolfenstein.games.objects.EnemyState.ATTACKING;
import static com.example.wolfenstein.games.objects.EnemyState.DEAD;

public class Game {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;

    private Canvas canvas;
    private GraphicsContext gc;

    private Map map;
    private Player player;
    private Renderer renderer;
    private final List<Bullet> bullets;
    private final List<Enemy> enemies;

    public Game() {
        bullets = new ArrayList<>();
        enemies = new ArrayList<>();
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
            switch (e.getCode()) {
                case W -> player.moveForward(0.1, map, enemies);
                case S -> player.moveBackward(0.1, map, enemies);
                case A -> player.rotateLeft(0.1);
                case D -> player.rotateRight(0.1);
                case SPACE -> bullets.add(player.shoot());
                default -> throw new IllegalStateException("Unexpected value: " + e.getCode());
            }
        });
    }

    private void updateBullets() {
        List<Bullet> toRemove = new ArrayList<>();
        List<Enemy> enemiesToRemove = new ArrayList<>();
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
                    enemiesToRemove.add(enemy);
                    toRemove.add(bullet);
                    break;
                }
            }
        }
        bullets.removeAll(toRemove);
        enemies.removeAll(enemiesToRemove);
    }

    private void updateEnemies() {
        for (Enemy enemy : enemies) {
            if (enemy.getState() == DEAD) continue;

            double dx = player.getPosX() - enemy.getX();
            double dy = player.getPosY() - enemy.getY();
            double dist = Math.sqrt(dx * dx + dy * dy);

            if (enemyCanSeePlayer(enemy, player, map)) {
                if(dist < enemy.getAttackRange()) {
                    enemy.setState(ATTACKING);
                    // TODO: Damage player
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
                }
            } else {
                enemy.setState(EnemyState.IDLE);
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

    private void startGameLoop() {
        new AnimationTimer() {
            public void handle(long now) {
                gc.clearRect(0, 0, WIDTH, HEIGHT);
                updateEnemies();
                updateBullets();
                renderer.render(gc, player, map, bullets,enemies);
            }
        }.start();
    }
}
