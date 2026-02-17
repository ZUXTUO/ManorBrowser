/**
 * 动态背景视图 - 流云效果。
 */
package com.olsc.manorbrowser.view;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
public class SkyFlowView extends View {
    private Paint bgPaint;
    private Paint wavePaint;
    private Paint starPaint;
    private float time = 0;
    private final Random random = new Random();
    private final List<Star> stars = new ArrayList<>();
    private final List<OpticalRipple> ripples = new ArrayList<>();
    private final int COLOR_SKY_START = 0xFF00B4DB;
    private final int COLOR_SKY_END = 0xFF0083B0;
    private final int COLOR_WAVE = 0x22FFFFFF;
    public SkyFlowView(Context context) {
        this(context, null);
    }
    public SkyFlowView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    private void init() {
        bgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        wavePaint.setStyle(Paint.Style.FILL);
        starPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        starPaint.setStyle(Paint.Style.FILL);
        starPaint.setColor(Color.WHITE);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        LinearGradient gradient = new LinearGradient(0, 0, w, h,
                new int[]{COLOR_SKY_START, COLOR_SKY_END},
                null, Shader.TileMode.CLAMP);
        bgPaint.setShader(gradient);
        stars.clear();
        for (int i = 0; i < 8; i++) {
            stars.add(new Star(w, h));
        }
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, getWidth(), getHeight(), bgPaint);
        time += 0.008f;
        drawFlowingWaves(canvas);
        for (Star star : stars) {
            star.update();
            starPaint.setAlpha(star.alpha);
            canvas.drawCircle(star.x, star.y, star.size, starPaint);
        }
        drawOpticalRipples(canvas);
        postInvalidateOnAnimation();
    }
    private void drawFlowingWaves(Canvas canvas) {
        int w = getWidth();
        int h = getHeight();
        for (int i = 0; i < 2; i++) {
            float phase = time * (0.3f + i * 0.1f);
            Path path = new Path();
            path.moveTo(0, h);
            for (int x = 0; x <= w; x += 15) {
                float y = (float) (h * 0.5f + Math.sin(x * 0.008f + phase) * (12 * getResources().getDisplayMetrics().density));
                path.lineTo(x, y);
            }
            path.lineTo(w, h);
            path.close();
            wavePaint.setColor(COLOR_WAVE);
            canvas.drawPath(path, wavePaint);
        }
    }
    private void drawOpticalRipples(Canvas canvas) {
        Iterator<OpticalRipple> it = ripples.iterator();
        while (it.hasNext()) {
            OpticalRipple r = it.next();
            if (r.update()) {
                for (int i = 0; i < 3; i++) {
                    float ringProgress = r.progress - (i * 0.15f);
                    if (ringProgress <= 0 || ringProgress > 1.0f) continue;
                    Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
                    p.setStyle(Paint.Style.STROKE);
                    p.setStrokeWidth(5 * (1.1f - ringProgress) * getResources().getDisplayMetrics().density);
                    p.setColor(Color.WHITE);
                    p.setAlpha((int) (100 * (1.0f - ringProgress)));
                    canvas.drawCircle(r.x, r.y, r.maxRadius * ringProgress, p);
                    if (i == 0) {
                        Paint shine = new Paint(Paint.ANTI_ALIAS_FLAG);
                        shine.setStyle(Paint.Style.STROKE);
                        shine.setStrokeWidth(2 * getResources().getDisplayMetrics().density);
                        shine.setColor(Color.WHITE);
                        shine.setAlpha((int) (180 * (1.0f - ringProgress)));
                        canvas.drawArc(r.x - r.maxRadius*ringProgress, r.y - r.maxRadius*ringProgress,
                                r.x + r.maxRadius*ringProgress, r.y + r.maxRadius*ringProgress,
                                -45, 90, false, shine);
                    }
                }
            } else {
                it.remove();
            }
        }
    }
    private float lastRippleX = -1, lastRippleY = -1;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            ripples.add(new OpticalRipple(x, y, getWidth()));
            lastRippleX = x;
            lastRippleY = y;
            return true;
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            float dx = x - lastRippleX;
            float dy = y - lastRippleY;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            if (dist > 30 * getResources().getDisplayMetrics().density) {
                ripples.add(new OpticalRipple(x, y, getWidth()));
                lastRippleX = x;
                lastRippleY = y;
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
    private class Star {
        float x, y, size;
        int alpha, baseAlpha;
        float twinkleOffset;
        Star(int w, int h) {
            x = random.nextFloat() * w;
            y = random.nextFloat() * h;
            size = (1.2f + random.nextFloat() * 1.5f) * getResources().getDisplayMetrics().density;
            baseAlpha = 80 + random.nextInt(120);
            twinkleOffset = random.nextFloat() * 100;
        }
        void update() {
            float t = (float) (System.currentTimeMillis() * 0.0015);
            alpha = (int) (baseAlpha * (0.6f + 0.4f * Math.sin(t + twinkleOffset)));
        }
    }
    private class OpticalRipple {
        float x, y, maxRadius, progress;
        OpticalRipple(float x, float y, int screenWidth) {
            this.x = x;
            this.y = y;
            this.maxRadius = screenWidth * 0.6f;
            this.progress = 0;
        }
        boolean update() {
            progress += 0.008f;
            return progress <= 1.3f;
        }
    }
}