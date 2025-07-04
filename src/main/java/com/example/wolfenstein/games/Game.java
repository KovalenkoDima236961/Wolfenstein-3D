package com.example.wolfenstein.games;

import com.example.wolfenstein.games.objects.Map;
import com.example.wolfenstein.games.objects.Player;
import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Game {

    private final int WIDTH = 1024;
    private final int HEIGHT = 768;

    private Canvas canvas;
    private GraphicsContext gc;

    private Map map;
    private Player player;
    private Renderer renderer;

    public void start(Stage stage) {
        canvas = new Canvas(WIDTH, HEIGHT);
        gc = canvas.getGraphicsContext2D();

        map = new Map();
        player = new Player(3.5, 3.5);
        renderer = new Renderer(WIDTH, HEIGHT);

        Scene scene = new Scene(new StackPane(canvas));
        stage.setTitle("Wolf3D FX");
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
                case SPACE -> player.shoot(map);
            }
        });
    }

    private void startGameLoop() {
        new AnimationTimer() {
            public void handle(long now) {
                gc.clearRect(0, 0, WIDTH, HEIGHT);
                renderer.render(gc, player, map);
            }
        }.start();
    }
}
