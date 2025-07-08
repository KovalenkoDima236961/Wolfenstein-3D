package com.example.wolfenstein.games.objects;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Enemy {
    private double x;
    private double y;

    private double dirX;
    private double dirY;

    private double speed;

    private EnemyState state;

    private double health;
    private double damage;

    private double shootCooldown = 0.0;
    private double shootInterval = 2.0;
    private double attackRange;
    private double chasingRange;


    public Enemy(double x, double y) {
        this.x = x;
        this.y = y;
        this.speed = 0.025;
        this.state = EnemyState.PATROL;
        this.health = 20.0;
        this.damage = 0.1;
        this.attackRange = 6.0;
        this.chasingRange = 10.0;

        this.dirX = 0;
        this.dirY = 1;
    }

    public boolean isDead() {
        return health <= 0;
    }
}
