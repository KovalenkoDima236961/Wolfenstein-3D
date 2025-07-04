package com.example.wolfenstain;

import com.example.wolfenstain.games.Game;
import com.example.wolfenstain.games.Renderer;
import com.example.wolfenstain.games.objects.Map;
import com.example.wolfenstain.games.objects.Player;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        Game game = new Game();
        game.start(stage);
    }

    public static void main(String[] args) {
        launch();
    }
}