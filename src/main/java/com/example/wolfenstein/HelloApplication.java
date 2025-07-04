package com.example.wolfenstein;

import com.example.wolfenstein.games.Game;
import javafx.application.Application;
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