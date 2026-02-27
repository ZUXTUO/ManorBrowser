/**
 * 动态背景视图 - 模拟雨天效果
 * 综合了三种视觉元素：
 * 1. 远景雨线 (RainLine)：快速下落的半透明细线，营造整体降雨氛围。
 * 2. 玻璃雨滴 (GlassDrop)：模拟贴在屏幕（玻璃）上的水珠，会间歇性下滑并带有高光效果。
 * 3. 底部烟雾 (MistShape)：在屏幕下方缓慢移动的大圆，模拟雨雾蒙蒙的效果。
 */
package com.olsc.manorbrowser.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class RainView extends View {
    private Paint bgPaint;
    private Paint rainPaint;
    private Paint glassDropPaint;
    private Paint mistPaint;
    private int width, height;
    private float density;
    private final Random random = new Random();
    
    /** 远景雨线列表 */
    private final List<RainLine> rainLines = new ArrayList<>();
    /** 玻璃上的水珠列表 */
    private final List<GlassDrop> glassDrops = new ArrayList<>();
    /** 地面/底部薄雾列表 */
    private final List<MistShape> mists = new ArrayList<>();

    public RainView(Context context) {
        this(context, null);
    }

    public RainView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RainView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        density = getResources().getDisplayMetrics().density;
        
        // 背景渐变画笔
        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
        
        // 雨线画笔
        rainPaint = new Paint();
        rainPaint.setColor(0x88AACCFF);
        rainPaint.setStyle(Paint.Style.STROKE);
        rainPaint.setStrokeCap(Paint.Cap.BUTT);
        rainPaint.setAntiAlias(true);
        
        // 玻璃水滴画笔
        glassDropPaint = new Paint();
        glassDropPaint.setStyle(Paint.Style.FILL);
        glassDropPaint.setColor(0xCCFFFFFF);
        glassDropPaint.setAntiAlias(true);
        
        // 雾气画笔
        mistPaint = new Paint();
        mistPaint.setStyle(Paint.Style.FILL);
        mistPaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        
        // 设置深蓝色调的阴雨天渐变背景
        LinearGradient bgGradient = new LinearGradient(
                0, 0, 0, height,
                new int[]{0xFF1A2A3A, 0xFF2C3E50, 0xFF4B6584},
                new float[]{0f, 0.6f, 1f},
                Shader.TileMode.CLAMP);
        bgPaint.setShader(bgGradient);
        
        // 初始化雨线、水珠、云雾
        rainLines.clear();
        for (int i = 0; i < 150; i++) {
            rainLines.add(new RainLine(w, h, density));
        }
        glassDrops.clear();
        for (int i = 0; i < 30; i++) {
            glassDrops.add(new GlassDrop(w, h, density));
        }
        mists.clear();
        for (int i=0; i<3; i++) {
            mists.add(new MistShape(w, h, density));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 1. 绘制背景
        canvas.drawRect(0, 0, width, height, bgPaint);
        
        // 2. 绘制雾气
        for (MistShape mist : mists) {
            mist.update();
            mistPaint.setColor(0x33B0BEC5); // 淡灰色雾
            mistPaint.setAlpha(mist.alpha);
            canvas.drawCircle(mist.x, mist.y, mist.radius, mistPaint);
        }
        
        // 3. 绘制雨线
        rainPaint.setStrokeWidth(1 * density);
        for (RainLine line : rainLines) {
            line.update();
            rainPaint.setAlpha(line.alpha);
            canvas.drawLine(line.x, line.y, line.x, line.y + line.length, rainPaint);
        }
        
        // 4. 绘制玻璃水珠
        for (GlassDrop drop : glassDrops) {
            drop.update();
            glassDropPaint.setAlpha(drop.alpha);
            if (drop.speed > 0.1) {
                // 滑动状态：绘制稍微拉长的水珠
                canvas.drawCircle(drop.x, drop.y - drop.size * 1.5f, drop.size * 0.5f, glassDropPaint);
                canvas.drawCircle(drop.x, drop.y, drop.size, glassDropPaint);
            } else {
                // 静止状态：圆形水珠
                canvas.drawCircle(drop.x, drop.y, drop.size, glassDropPaint);
            }
            // 绘制白色高光点
            glassDropPaint.setAlpha(200);
            canvas.drawCircle(drop.x - drop.size*0.3f, drop.y - drop.size*0.3f, drop.size*0.25f, glassDropPaint);
        }
        
        // 继续下一帧动画
        postInvalidateOnAnimation();
    }

    /**
     * 雨线模型
     */
    private class RainLine {
        float x, y, length, speed;
        int alpha;

        RainLine(int w, int h, float density) {
            reset(w, h, density, true);
        }

        void reset(int w, int h, float density, boolean randomStart) {
            x = random.nextInt(w);
            y = randomStart ? random.nextInt(h) : -random.nextInt(200);
            length = (20 + random.nextInt(20)) * density;
            speed = (15 + random.nextFloat() * 10) * density;
            alpha = 50 + random.nextInt(100);
        }

        void update() {
            y += speed;
            if (y > height) {
                reset(width, height, density, false);
            }
        }
    }

    /**
     * 玻璃上的水珠模型
     */
    private class GlassDrop {
        float x, y, size, speed;
        int alpha;
        int state; // 0: 静止, 1: 滑动中

        GlassDrop(int w, int h, float density) {
            reset(w, h, density, true);
        }

        void reset(int w, int h, float density, boolean randomStart) {
            x = random.nextInt(w);
            y = randomStart ? random.nextInt(h) : -random.nextInt(100);
            size = (2 + random.nextFloat() * 3) * density;
            alpha = 150 + random.nextInt(80);
            state = 0;
            speed = 0;
        }

        void update() {
            if (state == 0) {
                // 极小概率进入滑动状态
                if (random.nextInt(1000) < 10) {
                    state = 1;
                    speed = (0.5f + random.nextFloat()) * density;
                }
            } else {
                y += speed;
                if (random.nextFloat() < 0.1f) speed += 0.1f;
                // 滑动过程中有几率停下来
                if (random.nextInt(1000) < 5) {
                    state = 0;
                    speed = 0;
                }
            }
            
            if (y > height + 50) {
                reset(width, height, density, false);
            }
        }
    }

    /**
     * 雾气/云团模型
     */
    private class MistShape {
        float x, y, radius, speed;
        int alpha;

        MistShape(int w, int h, float density) {
            reset(w, h, density);
        }

        void reset(int w, int h, float density) {
             x = random.nextInt(w);
             y = h * 0.8f + random.nextInt((int)(h * 0.4f)); // 集中在底部绘制
             radius = (100 + random.nextInt(300)) * density;
             speed = (0.2f + random.nextFloat() * 0.3f) * density * (random.nextBoolean() ? 1 : -1);
             alpha = 20 + random.nextInt(20);
        }

        void update() {
            x += speed;
            if (x - radius > width) x = -radius;
            if (x + radius < 0) x = width + radius;
        }
    }
}