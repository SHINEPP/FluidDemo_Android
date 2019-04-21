package com.demo.fluidparticle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.annotation.WorkerThread;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.demo.fluidparticle.fluidmodel.FluidEngine;
import com.demo.fluidparticle.fluidmodel.FluidParticle;

import java.util.Timer;
import java.util.TimerTask;

public class ParticlesSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private FluidEngine fluidEngine = new FluidEngine();
    private Paint paintClear;
    private Paint paintParticle;

    private final Object object = new Object();
    private Thread thread;
    private boolean runFlag;
    private boolean drawOk;

    private Timer timer = new Timer();

    public ParticlesSurfaceView(Context context) {
        super(context);

        init();
    }

    public ParticlesSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public ParticlesSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawOk = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        drawOk = false;
    }

    public void start() {
        runFlag = true;
        synchronized (object) {
            if (thread != null) {
                return;
            }

            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (runFlag) {
                        updateFrame();

                        try {
                            Thread.sleep(16);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    synchronized (object) {
                        thread = null;
                    }
                }
            });

            thread.start();
        }
    }

    public void stop() {
        runFlag = false;
    }

    @WorkerThread
    private void updateFrame() {
        if (!drawOk) {
            return;
        }

        if (getVisibility() != SurfaceView.VISIBLE) {
            return;
        }

        SurfaceHolder surfaceHolder = getHolder();
        if (surfaceHolder == null || surfaceHolder.isCreating()) {
            return;
        }

        Canvas canvas = surfaceHolder.lockCanvas();
        if (canvas == null) {
            return;
        }

        fluidEngine.simulate();

        canvas.drawPaint(paintClear);

        for (FluidParticle particle : fluidEngine.getParticles()) {
            canvas.drawCircle(particle.x * getWidth(), particle.y * getHeight(), 8, paintParticle);
        }

        surfaceHolder.unlockCanvasAndPost(canvas);
    }

    private void init() {
        SurfaceHolder surfaceHolder = getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setFormat(PixelFormat.RGBA_8888);

        paintClear = new Paint();
        paintClear.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

        paintParticle = new Paint();
        paintParticle.setARGB(255, 0, 0, 255);
        paintParticle.setAntiAlias(true);
        paintParticle.setStyle(Paint.Style.FILL);

        TimerTask task = new TimerTask() {
            private boolean flag;
            @Override
            public void run() {
                flag = !flag;
                fluidEngine.setgX(flag ? 0.00002f : -0.00002f);
            }
        };

        timer.schedule(task, 0, 5000);
    }
}
