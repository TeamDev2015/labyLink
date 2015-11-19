package jp.sys_link.labyrinth;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class Map implements Ball.OnMoveListener {

    // �u���b�N�T�C�Y�̕ϐ��錾
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

    // �}�b�v��\������傫��(�����A����)�ƃu���b�N�T�C�Y���󂯎��A
    // �c���̃u���b�N�����v�Z�������ƁA�}�b�v�𐶐�����
    public Map(int w, int h, int bs, LabyrinthView.Callback cb, int seed) {
        blockSize = bs;
        horizontalBlockNum = w / blockSize;
        verticalBlockNum = h / blockSize;
        callback = cb;
        stageSeed = seed;

        // �c���̃u���b�N���������ł���΂P�����Z���Ċ�ɂ���
        // ���H�����A���S���Y���Ń}�b�v�𐶐�����ꍇ�A
        // ��łȂ��Ɛ������}�b�v�𐶐��ł��Ȃ�����
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

    // �S�Ă�Block��`�悷��
    void drawMap(Canvas canvas) {
        for (int y = 0; y < verticalBlockNum; y++) {
            for (int x = 0; x < horizontalBlockNum; x++) {
                block[y][x].draw(canvas);
            }
        }
    }

    @Override
    public boolean canMove(int left, int top, int right, int bottom) {
        // �{�[���̌��݈ʒu����A�{�[��������u���b�N�̏c�Ɖ��̈ʒu���v�Z����
        int verticalBlock = top / blockSize;
        int horizontalBlock = left / blockSize;

        // �����Ώۂ̃u���b�N��ݒ�
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

                    // ���ɗ����锻��
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

        // ���̃^�C�v��萔�錾�A�O�ŏ�����
        private static final int TYPE_FLOOR = 0;
        // �ǂ̃^�C�v��萔�錾�A�P�ŏ�����
        private static final int TYPE_WALL = 1;
        private static final int TYPE_START = 2;
        private static final int TYPE_GOAL = 3;
        private static final int TYPE_HOLE = 4;

        // ���I�u�W�F�N�g�̐���
        private static final Paint PAINT_FLOOR = new Paint();
        // �ǃI�u�W�F�N�g�̐���
        private static final Paint PAINT_WALL = new Paint();
        private static final Paint PAINT_START = new Paint();
        private static final Paint PAINT_GOAL = new Paint();
        private static final Paint PAINT_HOLE = new Paint();

        static {
            // ���̕`��F�̐ݒ�(�V�A���F)
            PAINT_FLOOR.setColor(Color.CYAN);
            // �ǂ̕`��F�̐ݒ�(�u���b�N�F)
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
