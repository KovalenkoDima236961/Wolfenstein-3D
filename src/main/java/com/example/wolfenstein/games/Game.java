package com.example.wolfenstein.games;

import com.example.wolfenstein.games.objects.Bullet;
import com.example.wolfenstein.games.objects.Map;
import com.example.wolfenstein.games.objects.Player;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 768;

    private Canvas canvas;
    private GraphicsContext gc;

    private Map map;
    private Player player;
    private Renderer renderer;
    private final List<Bullet> bullets;

    public Game() {
        bullets = new ArrayList<>();
    }

    public void start(Stage stage) {
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();

        map = new Map();
        player = new Player(3.5, 3.5);
        renderer = new Renderer(WIDTH, HEIGHT); // game render

        Scene scene = new Scene(new StackPane(canvas));
        stage.setTitle("Wolf 2.5D");
        stage.setScene(scene);
        stage.show();

        setupInput(scene);
        startGameLoop();
    }

    private void setupInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case W -> player.moveForward(0.1, map);
                case S -> player.moveBackward(0.1, map);
                case A -> player.rotateLeft(0.1);
                case D -> player.rotateRight(0.1);
                case SPACE -> bullets.add(player.shoot());
                default -> throw new IllegalStateException("Unexpected value: " + e.getCode());
            }
        });
    }

    private void updateBullets() {
        List<Bullet> toRemove = new ArrayList<>();
        for (Bullet bullet : bullets) {
            // Move the bullet forward in its direction
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

            if (map.isEnemy(gridX, gridY)) {
                map.removeEnemy(gridX, gridY);
                toRemove.add(bullet);
            }
        }
        bullets.removeAll(toRemove);
    }

    private void startGameLoop() {
        new AnimationTimer() {
            public void handle(long now) {
                gc.clearRect(0, 0, WIDTH, HEIGHT);
                updateBullets();
                renderer.render(gc, player, map, bullets);
            }
        }.start();
    }
}
