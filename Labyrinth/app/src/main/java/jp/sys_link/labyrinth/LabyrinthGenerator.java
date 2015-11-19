package jp.sys_link.labyrinth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LabyrinthGenerator {

    public static final int FLOOR = 0;

    public static final int WALL = 1;

    public static final int START = 2;

    public static final int GOAL = 3;

    public static final int HOLE = 4;

    public static final int POLL = -1;

    public enum Direction {
        TOP,
        LEFT,
        RIGHT,
        BOTTOM,
    }

    public static class MapResult {
        final int[][] result;
        final int startY;
        final int startX;

        MapResult(int[][] result, int startY, int startX) {
            this.result = result;
            this.startY = startY;
            this.startX = startX;
        }
    }

    public static MapResult getMap(int seed, int horizontalBlockNum, int verticalBlockNum) {

        int[][] result = new int[verticalBlockNum][horizontalBlockNum];

        // 配列の初期化
        for (int y = 0; y < verticalBlockNum; y++) {

            for (int x = 0; x < horizontalBlockNum; x++) {

                if (y == 0 || y == verticalBlockNum - 1) {
                    // １行目と最終行は壁に設定
                    result[y][x] = WALL;

                } else if (x == 0 || x == horizontalBlockNum - 1) {
                    // １列目と最終列は壁に設定
                    result[y][x] = WALL;

                } else if (x > 1 && x % 2 == 0 && y > 1 && y % 2 == 0) {

                    // ２つ毎に壁
                    result[y][x] = POLL;
                } else {
                    // その他は床に設定
                    result[y][x] = FLOOR;
                }
            }
        }

        // 迷路を生成
        result = generateLabyrinth(seed, result);

        int startY = -1;
        int startX = -1;

        // スタート地点を下端の右端から最初の床に設定
        for (int y = verticalBlockNum - 1; y >= 0; y--) {
            for (int x = horizontalBlockNum - 1; x >= 0; x--) {
                if (result[y][x] == FLOOR) {
                    startY = y;
                    startX = x;
                    result[startY][startX] = START;
                    break;
                }
            }
            if (startY != -1 && startX != -1) {
                break;
            }
        }

        // スタートからの距離をすべてのブロックについて計算する
        int[][] steps = new int[verticalBlockNum][horizontalBlockNum];
        calcStep(result, startY, startX, steps, 0);

        // もっとも長い距離のブロックをゴールに設定する
        int maxScore = 0;
        int maxScoreY = 0;
        int maxScoreX = 0;
        for (int y = 0; y < verticalBlockNum; y++) {
            for (int x = 0; x < horizontalBlockNum; x++) {
                if (steps[y][x] > maxScore) {
                    maxScore = steps[y][x];
                    maxScoreY = y;
                    maxScoreX = x;
                }
            }
        }

        // ゴールの設定
        result[maxScoreY][maxScoreX] = GOAL;

        return new MapResult(result, startY, startX);

    }

    private static int[][] calcStep(int[][] map, int y, int x, int[][] steps, int score) {
        score++;

        if (y < 0 || x < 0 || y >= map.length || x >= map[0].length) {
            return steps;
        }

        if (map[y][x] == WALL) {
            steps[y][x] = -1;
            return steps;
        }

        if (steps[y][x] == 0 || steps[y][x] > score) {
            steps[y][x] = score;

            calcStep(map, y, x + 1, steps, score);
            calcStep(map, y + 1, x, steps, score);
            calcStep(map, y, x - 1, steps, score);
            calcStep(map, y - 1, x, steps, score);
        }
        return steps;
    }

    private static int[][] generateLabyrinth(int seed, int[][] map) {
        Random rand = new Random(seed);

        int horizontal = map[0].length;
        int vertical = map.length;

        for (int y = 0; y < vertical; y++) {
            for (int x = 0; x < horizontal; x++) {
                if (map[y][x] == POLL) {

                    // 壁を作る方向
                    List<Direction> directionList = null;

                    // 閉鎖路を防ぐため、２段目以降は上向きに壁を設定しない
                    if (y == 1) {
                        directionList = new ArrayList<Direction>(
                                Arrays.asList(Direction.TOP, Direction.LEFT, Direction.RIGHT, Direction.BOTTOM));
                    } else {
                        directionList = new ArrayList<Direction>(
                                Arrays.asList(Direction.LEFT, Direction.RIGHT, Direction.BOTTOM));
                    }

                    do {
                        Direction direction = directionList.get(rand.nextInt(directionList.size()));
                        if (setDirection(y, x, direction, map)) {
                            break;
                        } else {
                            directionList.remove(direction);
                        }
                    } while (directionList.size() > 0);
                }
            }
        }

        // 設定する穴の個数
        int holeCount = seed + 1;
        if (holeCount > (vertical + horizontal)) {
            holeCount = vertical + horizontal;
        }

        setHoles(holeCount, rand, vertical, horizontal, map);

        return map;
    }

    private static void setHoles(int holeCount, Random rand, int vertical, int horizontal, int[][] map) {

        do {
            // 外壁は穴にしない
            int y = rand.nextInt(vertical - 2) + 1;
            int x = rand.nextInt(horizontal - 2) + 1;

            if (map[y][x] == WALL) {
                map[y][x] = HOLE;
            }

        } while (rand.nextInt(holeCount) != 0);
    }

    private static boolean setDirection(int y, int x, Direction direction, int[][] map) {
        map[y][x] = WALL;

        switch (direction) {
            case LEFT:
                x -= 1;
                break;
            case RIGHT:
                x += 1;
                break;
            case BOTTOM:
                y -= 1;
                break;
            case TOP:
                y += 1;
                break;
        }

        if (x < 0 || y < 0 || x >= map[0].length || y >= map.length) {
            return false;
        }

        if (map[y][x] == WALL) {
            return false;
        }

        map[y][x] = WALL;

        return true;
    }
}
