/**
 * 动态背景视图 - 流星效果。
 */
package com.olsc.manorbrowser.view;
import com.olsc.manorbrowser.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
public class MeteorView extends View {
    private final List<Meteor> meteors = new ArrayList<>();
    private final List<Star> stars = new ArrayList<>();
    private Paint meteorPaint;
    private Paint starPaint;
    private Paint bgPaint;
    private int width, height;
    private Random random = new Random();
    private long lastMeteorTime = 0;
    public MeteorView(Context context) {
        this(context, null);
    }
    public MeteorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public MeteorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init() {
        meteorPaint = new Paint();
        meteorPaint.setStyle(Paint.Style.STROKE);
        meteorPaint.setStrokeCap(Paint.Cap.ROUND);
        meteorPaint.setAntiAlias(true);
        starPaint = new Paint();
        starPaint.setStyle(Paint.Style.FILL);
        starPaint.setColor(Color.WHITE);
        starPaint.setAntiAlias(true);
        bgPaint = new Paint();
        bgPaint.setStyle(Paint.Style.FILL);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        LinearGradient bgGradient = new LinearGradient(
                0, 0, 0, height,
                new int[]{0xFF050510, 0xFF101530},
                null, Shader.TileMode.CLAMP);
        bgPaint.setShader(bgGradient);
        stars.clear();
        int starCount = 80 + random.nextInt(40);
        float density = getResources().getDisplayMetrics().density;
        for (int i = 0; i < starCount; i++) {
            stars.add(new Star(w, h, density));
        }
        meteors.clear();
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(0, 0, width, height, bgPaint);
        for (Star star : stars) {
            star.update();
            starPaint.setAlpha(star.alpha);
            canvas.drawCircle(star.x, star.y, star.radius, starPaint);
        }
        long currentTime = System.currentTimeMillis();
        if (meteors.size() < 3 && (currentTime - lastMeteorTime > 500 + random.nextInt(1500))) {
            float density = getResources().getDisplayMetrics().density;
            meteors.add(new Meteor(width, height, density));
            lastMeteorTime = currentTime;
        }
        Iterator<Meteor> iterator = meteors.iterator();
        while (iterator.hasNext()) {
            Meteor meteor = iterator.next();
            meteor.move();
            if (meteor.isOffScreen(width, height)) {
                iterator.remove();
            } else {
                drawMeteor(canvas, meteor);
            }
        }
        postInvalidateOnAnimation();
    }
    private void drawMeteor(Canvas canvas, Meteor meteor) {
        meteorPaint.setStrokeWidth(meteor.thickness);
        int colorHead = Color.argb(255, 255, 255, 255);
        int colorTail = Color.argb(0, 255, 255, 255);
        LinearGradient gradient = new LinearGradient(
                meteor.x - meteor.lengthX, meteor.y - meteor.lengthY,
                meteor.x, meteor.y,
                colorTail, colorHead, Shader.TileMode.CLAMP);
        meteorPaint.setShader(gradient);
        canvas.drawLine(
                meteor.x - meteor.lengthX,
                meteor.y - meteor.lengthY,
                meteor.x,
                meteor.y,
                meteorPaint);
        meteorPaint.setShader(null);
    }
    private class Star {
        float x, y;
        float radius;
        int alpha;
        int maxAlpha;
        boolean fadingOut;
        int twinkleSpeed;
        Star(int w, int h, float density) {
            x = random.nextFloat() * w;
            y = random.nextFloat() * h;
            radius = (0.5f + random.nextFloat() * 1.5f) * density;
            maxAlpha = 150 + random.nextInt(105);
            alpha = random.nextInt(maxAlpha);
            fadingOut = random.nextBoolean();
            twinkleSpeed = 2 + random.nextInt(3);
        }
        void update() {
            if (fadingOut) {
                alpha -= twinkleSpeed;
                if (alpha <= 50) {
                    alpha = 50;
                    fadingOut = false;
                }
            } else {
                alpha += twinkleSpeed;
                if (alpha >= maxAlpha) {
                    alpha = maxAlpha;
                    fadingOut = true;
                }
            }
        }
    }
    private class Meteor {
        float x, y;
        float lengthX, lengthY;
        float speedX, speedY;
        float thickness;
        Meteor(int w, int h, float density) {
            double angle = Math.toRadians(35 + random.nextInt(20));
            float speed = (15 + random.nextFloat() * 15) * density;
            float length = (100 + random.nextFloat() * 200) * density;
            speedX = (float) (speed * Math.cos(angle));
            speedY = (float) (speed * Math.sin(angle));
            lengthX = (float) (length * Math.cos(angle));
            lengthY = (float) (length * Math.sin(angle));
            thickness = (1 + random.nextFloat() * 2) * density;
            if (random.nextBoolean()) {
                x = random.nextInt(w);
                y = -lengthY - 50;
            } else {
                x = -lengthX - 50;
                y = random.nextInt(h / 2);
            }
        }
        void move() {
            x += speedX;
            y += speedY;
        }
        boolean isOffScreen(int w, int h) {
            return x - lengthX > w || y - lengthY > h;
        }
    }
}