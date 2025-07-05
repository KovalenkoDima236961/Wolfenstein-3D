package com.example.wolfenstein.games.objects;

public enum EnemyState {
    IDLE(1),
    PATROL(2),
    CHASING(3),
    ATTACKING(4),
    DEAD(5);

    private final int value;
    EnemyState(int i) {
        value = i;
    }

    public static EnemyState fromValue(int value) {
        return values()[value];
    }
}
