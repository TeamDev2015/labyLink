package jp.sys_link.labyrinth;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Map implements Ball.OnMoveListener {

    // ブロックサイズの変数宣言
    private int blockSize;
    private final int stageSeed;

    private int horizontalBlockNum;
    private int verticalBlockNum;

    private Block[][] block;

    private final Block[][] targetBlock = new Block[3][3];
    private Block startBlock;

    public Block getStartBlock() {
        return startBlock;
    }

    private LabyrinthView.Callback callback;

    // マップを表示する大きさ(横幅、高さ)とブロックサイズを受け取り、
    // 縦横のブロック数を計算したあと、マップを生成する
    public Map(int w, int h, int bs, LabyrinthView.Callback cb, int seed) {
        blockSize = bs;
        horizontalBlockNum = w / blockSize;
        verticalBlockNum = h / blockSize;
        callback = cb;
        stageSeed = seed;

        // 縦横のブロック数が偶数であれば１を減算して奇数にする
        // 迷路生成アルゴリズムでマップを生成する場合、
        // 奇数個でないと正しくマップを生成できないため
        if (horizontalBlockNum % 2 == 0) {
            horizontalBlockNum--;
        }
        if (verticalBlockNum % 2 == 0) {
            verticalBlockNum--;
        }

        createMap();
    }

    private void createMap() {

        LabyrinthGenerator.MapResult map = LabyrinthGenerator.getMap(stageSeed, horizontalBlockNum, verticalBlockNum);

        block = new Block[verticalBlockNum][horizontalBlockNum];
        for (int y = 0; y < verticalBlockNum; y++) {
            for (int x = 0; x < horizontalBlockNum; x++) {
                int type = map.result[y][x];
                int left = x * blockSize + 1;
                int top = y * blockSize + 1;
                int right = left + blockSize - 2;
                int bottom = top + blockSize - 2;
                block[y][x] = new Block(type, left, top, right, bottom);
            }
        }
        startBlock = block[map.startY][map.startX];
    }

    // 全てのBlockを描画する
    void drawMap(Canvas canvas) {
        for (int y = 0; y < verticalBlockNum; y++) {
            for (int x = 0; x < horizontalBlockNum; x++) {
                block[y][x].draw(canvas);
            }
        }
    }

    @Override
    public boolean canMove(int left, int top, int right, int bottom) {
        // ボールの現在位置から、ボールがあるブロックの縦と横の位置を計算する
        int verticalBlock = top / blockSize;
        int horizontalBlock = left / blockSize;

        // 検索対象のブロックを設定
        seTargetBlock(verticalBlock, horizontalBlock);

        int yLen = targetBlock.length;
        int xLen = targetBlock[0].length;

        for (int y = 0; y < yLen; y++) {
            for (int x = 0; x < xLen; x++) {
                if (targetBlock[y][x] == null) {
                    continue;
                }
                if (targetBlock[y][x].type == Block.TYPE_WALL
                        && targetBlock[y][x].rect.intersects(left, top, right, bottom)) {
                    callback.onGoal();
                    return false;
                } else if (targetBlock[y][x].type == Block.TYPE_GOAL
                        && targetBlock[y][x].rect.contains(left, top, right, bottom)) {
                    callback.onGoal();
                    return true;
                } else if (targetBlock[y][x].type == Block.TYPE_HOLE) {

                    int ballCenterX = left + (right - left) / 2;
                    int ballCenterY = top + (bottom - top) / 2;

                    int distanceX = targetBlock[y][x].rect.centerX() - ballCenterX;
                    int distanceY = targetBlock[y][x].rect.centerY() - ballCenterY;

                    double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));

                    // 穴に落ちる判定
                    if (distance < blockSize / 2) {
                        callback.onHole();
                    }
                }
            }
        }
        return true;
    }

    private void seTargetBlock(int verticalBlock, int horizontalBlock) {
        targetBlock[1][1] = getBlock(verticalBlock, horizontalBlock);

        targetBlock[0][0] = getBlock(verticalBlock - 1, horizontalBlock - 1);
        targetBlock[0][1] = getBlock(verticalBlock - 1, horizontalBlock);
        targetBlock[0][2] = getBlock(verticalBlock - 1, horizontalBlock + 1);

        targetBlock[1][0] = getBlock(verticalBlock, horizontalBlock - 1);
        targetBlock[1][2] = getBlock(verticalBlock, horizontalBlock + 1);

        targetBlock[2][0] = getBlock(verticalBlock + 1, horizontalBlock - 1);
        targetBlock[2][1] = getBlock(verticalBlock + 1, horizontalBlock);
        targetBlock[2][2] = getBlock(verticalBlock + 1, horizontalBlock + 1);
    }

    private Block getBlock(int y, int x) {
        if (y < 0 || x < 0 || y >= verticalBlockNum || x >= horizontalBlockNum) {
            return null;
        }
        return block[y][x];
    }

    static class Block {

        // 床のタイプを定数宣言、０で初期化
        private static final int TYPE_FLOOR = 0;
        // 壁のタイプを定数宣言、１で初期化
        private static final int TYPE_WALL = 1;
        private static final int TYPE_START = 2;
        private static final int TYPE_GOAL = 3;
        private static final int TYPE_HOLE = 4;

        // 床オブジェクトの生成
        private static final Paint PAINT_FLOOR = new Paint();
        // 壁オブジェクトの生成
        private static final Paint PAINT_WALL = new Paint();
        private static final Paint PAINT_START = new Paint();
        private static final Paint PAINT_GOAL = new Paint();
        private static final Paint PAINT_HOLE = new Paint();

        static {
            // 床の描画色の設定(シアン色)
            PAINT_FLOOR.setColor(Color.CYAN);
            // 壁の描画色の設定(ブラック色)
            PAINT_WALL.setColor(Color.BLACK);
            PAINT_START.setColor(Color.GREEN);
            PAINT_GOAL.setColor(Color.RED);
            PAINT_HOLE.setColor(Color.rgb(32, 32, 32));
        }

        private final int type;

        final Rect rect;

        private Block(int type, int left, int top, int right, int bottom) {
            this.type = type;
            rect = new Rect(left, top, right, bottom);
        }

        private Paint getPaint() {
            switch (type) {
                case TYPE_FLOOR:
                    return PAINT_FLOOR;
                case TYPE_START:
                    return PAINT_START;
                case TYPE_GOAL:
                    return PAINT_GOAL;
                case TYPE_WALL:
                    return PAINT_WALL;
                case TYPE_HOLE:
                    return PAINT_HOLE;

            }
            return null;
        }

        private void draw(Canvas canvas) {
            canvas.drawRect(rect, getPaint());

        }
    }
}
