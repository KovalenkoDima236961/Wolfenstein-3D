package com.example.wolfenstein.games.objects;

public enum EnemyState {
    IDLE(1),
    CHASING(2),
    ATTACKING(3);

    private final int value;
    EnemyState(int i) {
        value = i;
    }

    public static EnemyState fromValue(int value) {
        return values()[value];
    }
}
