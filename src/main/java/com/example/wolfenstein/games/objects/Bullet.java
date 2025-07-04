package com.example.wolfenstein.games.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Bullet {
    private double x;
    private double y;

    private double dirX;
    private double dirY;

    private double speed;
    private double maxDistance;
}
