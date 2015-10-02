package jp.sys_link.labyrinth;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class LabyrinthView extends SurfaceView implements SurfaceHolder.Callback, SensorEventListener {

    private static final float BALL_SCALE = 0.8f;

    private static final float ACCEL_WEIGHT = 3f;

    private static final Paint TEXT_PAINT = new Paint();

    static {
        TEXT_PAINT.setColor(Color.WHITE);
        TEXT_PAINT.setTextSize(40f);
    }

    private Bitmap ballBitmap;

    private Ball ball;
    private Map map;

    private int seed;

    public void setSeed(int seed) {
        this.seed = seed;
    }

    interface Callback {
        public void onGoal();

        public void onHole();
    }

    private Callback callback;

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public LabyrinthView(Context context) {
        super(context);

        getHolder().addCallback(this);

        // ボールのBitmapをロード
        ballBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ball);
    }


    private DrawThread drawThread;

    private class DrawThread extends Thread {
        private boolean isFinished;

        @Override
        public void run() {

            while (!isFinished) {
                Canvas canvas = getHolder().lockCanvas();
                if (canvas != null) {
                    drawLabyrinth(canvas);
                    getHolder().unlockCanvasAndPost(canvas);
                }
            }
        }
    }

    public void startDrawThread() {
        stopDrawThread();

        drawThread = new DrawThread();
        drawThread.start();
    }

    public boolean stopDrawThread() {
        if (drawThread == null) {
            return false;
        }

        drawThread.isFinished = true;
        drawThread = null;

        return true;
    }

    public void drawLabyrinth(Canvas canvas) {
        canvas.drawColor(Color.BLACK);

        int blockSize = ballBitmap.getHeight();
        if (map == null) {
            map = new Map(canvas.getWidth(), canvas.getHeight(), blockSize, callback, seed);
        }

        if (ball == null) {
            ball = new Ball(ballBitmap, map.getStartBlock(), BALL_SCALE);
            ball.setOnMoveListener(map);
        }

        map.drawMap(canvas);

        ball.draw(canvas);

        if (sensorValues != null) {
            canvas.drawText("sensor[0] = " + sensorValues[0], 10, 150, TEXT_PAINT);
            canvas.drawText("sensor[1] = " + sensorValues[1], 10, 200, TEXT_PAINT);
            canvas.drawText("sensor[2] = " + sensorValues[2], 10, 250, TEXT_PAINT);
        }
    }

    public void startSensor() {
        sensorValues = null;

        SensorManager sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    public void stopSensor() {
        SensorManager sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        startDrawThread();

        startSensor();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopDrawThread();

        stopSensor();
    }

    private static final float ALPHA = 0.8f;
    private float[] sensorValues;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (sensorValues == null) {
            sensorValues = new float[3];
            sensorValues[0] = event.values[0];
            sensorValues[1] = event.values[1];
            sensorValues[2] = event.values[2];
            return;
        }

        sensorValues[0] = sensorValues[0] * ALPHA + event.values[0] * (1f - ALPHA);
        sensorValues[1] = sensorValues[1] * ALPHA + event.values[1] * (1f - ALPHA);
        sensorValues[2] = sensorValues[2] * ALPHA + event.values[2] * (1f - ALPHA);

        if (ball != null) {
            ball.move(-sensorValues[0] * ACCEL_WEIGHT, sensorValues[1] * ACCEL_WEIGHT);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
