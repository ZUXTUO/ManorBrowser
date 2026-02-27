/**
 * 动态背景视图 - 模拟雪天效果
 * 包含静态的雪景景观和动态落下的雪花：
 * 1. 景观分层：天空渐变、远山、山体阴影、近处雪地。使用 Path 绘制复杂曲线。
 * 2. 性能优化：景观部分只在尺寸变化时绘制一次到 landscapeBitmap，onDraw 时直接贴图。
 * 3. 落雪逻辑：雪花带有随机大小、下降速度以及正弦波晃动 (Wobble) 效果，模拟真实飘落感。
 */
package com.olsc.manorbrowser.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SnowView extends View {
    private Paint skyPaint;
    private Paint mountainPaint;
    private Paint mountainShadowPaint;
    private Paint groundPaint;
    private Paint groundShadowPaint;
    private Paint snowFlakePaint;
    private int width, height;
    private float density;
    private final Random random = new Random();
    
    /** 雪花实例列表 */
    private final List<SnowFlake> snowFlakes = new ArrayList<>();
    
    // 景观图形路径
    private Path mountainPath;
    private Path mountainShadowPath;
    private Path groundPath;
    private Path groundShadowPath;
    
    /** 缓存静态景观的位图，避免每一帧都重复计算复杂的 Path */
    private Bitmap landscapeBitmap;

    public SnowView(Context context) {
        this(context, null);
    }

    public SnowView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SnowView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;
        
        skyPaint = new Paint();
        skyPaint.setStyle(Paint.Style.FILL);
        
        mountainPaint = new Paint();
        mountainPaint.setColor(0xFFFFFFFF);
        mountainPaint.setStyle(Paint.Style.FILL);
        mountainPaint.setAntiAlias(true);
        
        mountainShadowPaint = new Paint();
        mountainShadowPaint.setColor(0xFFD6E6F2);
        mountainShadowPaint.setStyle(Paint.Style.FILL);
        mountainShadowPaint.setAntiAlias(true);
        
        groundPaint = new Paint();
        groundPaint.setColor(0xFFFFFFFF);
        groundPaint.setStyle(Paint.Style.FILL);
        groundPaint.setAntiAlias(true);
        
        groundShadowPaint = new Paint();
        groundShadowPaint.setColor(0xFFE8F1F6);
        groundShadowPaint.setStyle(Paint.Style.STROKE);
        groundShadowPaint.setStrokeWidth(2 * density);
        groundShadowPaint.setStrokeCap(Paint.Cap.ROUND);
        groundShadowPaint.setAntiAlias(true);
        
        snowFlakePaint = new Paint();
        snowFlakePaint.setColor(0xE6FFFFFF);
        snowFlakePaint.setStyle(Paint.Style.FILL);
        snowFlakePaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        
        // 天空色彩：淡蓝到淡黄的冬季清晨感
        LinearGradient skyGradient = new LinearGradient(
                0, 0, 0, height,
                new int[]{0xFFE1F5FE, 0xFFF0F9FF, 0xFFFFFDE7},
                new float[]{0f, 0.6f, 1f},
                Shader.TileMode.CLAMP);
        skyPaint.setShader(skyGradient);
        
        generateLandscape();
        generateLandscapeBitmap();
        generateSnowflakes();
    }

    /**
     * 使用三次贝塞尔曲线构建山脉和地面的 Path
     */
    private void generateLandscape() {
        mountainPath = new Path();
        mountainShadowPath = new Path();
        groundPath = new Path();
        groundShadowPath = new Path();
        
        float horizonY = height * 0.7f;
        
        // 绘制远山
        mountainPath.reset();
        mountainPath.moveTo(0, height);
        mountainPath.lineTo(0, horizonY);
        
        // 第一座山峰
        float p1X = width * 0.25f;
        float p1Y = horizonY - 120 * density;
        mountainPath.cubicTo(width * 0.05f, horizonY, p1X - 40*density, p1Y + 60*density, p1X, p1Y);
        mountainPath.cubicTo(p1X + 40*density, p1Y + 60*density, width * 0.4f, horizonY - 20*density, width * 0.5f, horizonY - 40*density);
        createMountainShadow(mountainShadowPath, p1X, p1Y, 120*density);
        
        // 第二座更大的山峰
        float p2X = width * 0.7f;
        float p2Y = horizonY - 200 * density;
        mountainPath.cubicTo(width * 0.55f, horizonY - 40*density, p2X - 60*density, p2Y + 100*density, p2X, p2Y);
        mountainPath.cubicTo(p2X + 60*density, p2Y + 100*density, width * 0.9f, horizonY, width, horizonY);
        createMountainShadow(mountainShadowPath, p2X, p2Y, 200*density);
        
        mountainPath.lineTo(width, height);
        mountainPath.lineTo(0, height);
        mountainPath.close();
        
        // 绘制近处雪地
        float groundStart = horizonY - 20 * density;
        groundPath.moveTo(0, groundStart);
        groundPath.cubicTo(width * 0.3f, groundStart + 40*density, width * 0.6f, groundStart - 10*density, width, groundStart + 30*density);
        groundPath.lineTo(width, height);
        groundPath.lineTo(0, height);
        groundPath.close();
        
        // 近景雪地阴影线条
        groundShadowPath.moveTo(width * 0.2f, height * 0.82f);
        groundShadowPath.cubicTo(width * 0.3f, height * 0.85f, width * 0.4f, height * 0.80f, width * 0.5f, height * 0.82f);
    }

    /**
     * 将 Path 离屏缓存到位图
     */
    private void generateLandscapeBitmap() {
        if (width <= 0 || height <= 0) return;
        if (landscapeBitmap != null && !landscapeBitmap.isRecycled()) {
            landscapeBitmap.recycle();
        }
        landscapeBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(landscapeBitmap);
        canvas.drawRect(0, 0, width, height, skyPaint);
        canvas.drawPath(mountainPath, mountainPaint);
        canvas.drawPath(mountainShadowPath, mountainShadowPaint);
        canvas.drawPath(groundPath, groundPaint);
        canvas.drawPath(groundShadowPath, groundShadowPaint);
    }

    private void createMountainShadow(Path path, float peakX, float peakY, float h) {
        path.moveTo(peakX, peakY);
        float rightBaseX = peakX + h * 0.6f;
        float rightBaseY = peakY + h * 1.0f;
        path.lineTo(rightBaseX, rightBaseY);
        path.cubicTo(
            peakX + h*0.2f, peakY + h*0.8f,
            peakX + h*0.1f, peakY + h*0.4f,
            peakX, peakY
        );
        path.close();
    }

    private void generateSnowflakes() {
        snowFlakes.clear();
        int count = 70;
        for (int i = 0; i < count; i++) {
            snowFlakes.add(new SnowFlake(width, height, density));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 先绘制缓存的背景景观
        if (landscapeBitmap != null) {
            canvas.drawBitmap(landscapeBitmap, 0, 0, null);
        }
        // 再绘制每一帧位置都在变化的雪花
        for (SnowFlake flake : snowFlakes) {
            flake.update();
            canvas.drawCircle(flake.x, flake.y, flake.radius, snowFlakePaint);
        }
        postInvalidateOnAnimation();
    }

    /**
     * 雪花模型
     */
    private class SnowFlake {
        float x, y, radius;
        float speedY, speedX;
        float wobble;

        SnowFlake(int w, int h, float density) {
            reset(w, h, density, true);
        }

        void reset(int w, int h, float density, boolean randomStart) {
            x = random.nextInt(w);
            y = randomStart ? random.nextInt(h) : -20;
            radius = (2f + random.nextFloat() * 2f) * density;
            float speedFactor = 0.5f + random.nextFloat() * 0.5f;
            speedY = (1f + random.nextFloat()) * density * speedFactor;
            speedX = (random.nextFloat() - 0.5f) * 0.4f * density;
            wobble = random.nextFloat() * 100;
        }

        void update() {
            y += speedY;
            // 核心动画：基于 sin 函数实现左右轻微晃动飘落
            x += speedX + (float)Math.sin(y * 0.01f + wobble) * 0.4f * density;
            
            if (y > height + 20) {
                reset(width, height, density, false);
            }
        }
    }
}