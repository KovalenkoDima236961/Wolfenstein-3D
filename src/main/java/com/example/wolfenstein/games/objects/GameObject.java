package com.example.wolfenstein.games.objects;

public enum GameObject {
    WALL_1(1, "1.gif"),
    WALL_2(2, "2.gif"),
    WALL_3(3, "3.gif"),
    WALL_4(4, "4.gif"),
    WALL_5(5, "5.gif"),
    WALL_6(6, "6.gif"),
    WALL_7(7, "7.gif"),
    WALL_8(8, "8.gif"),
    WALL_9(9, "9.gif"),
    WALL_10(10, "10.gif"),
    WALL_11(11, "11.gif"),
    WALL_12(12, "12.gif"),
    WALL_13(13, "13.gif"),
    WALL_14(14, "14.gif"),
    WALL_15(15, "15.gif"),
    WALL_16(16, "16.gif");

    private int value;
    private String imageName;

    GameObject(int val, String imageName) {
        this.value = val;
        this.imageName = imageName;
    }

    public String getImagePathForWalls() {
        return "/com/example/wolfenstein/images/walls/" + imageName;
    }

    public static GameObject fromValue(int value) {
        return switch (value) {
            // Special tiles
            case 1   -> WALL_1;
            case 2   -> WALL_2;
            case 3   -> WALL_3;
            case 4   -> WALL_4;
            case 5   -> WALL_5;
            case 6   -> WALL_6;
            case 7   -> WALL_7;
            case 8   -> WALL_8;
            case 9   -> WALL_9;
            case 10   -> WALL_10;
            case 11  -> WALL_11;
            case 12   -> WALL_12;
            case 13   -> WALL_13;
            case 14   -> WALL_14;
            case 15   -> WALL_15;
            case 16   -> WALL_16;

            // Default fallback
            default -> null;
        };
    }
}
