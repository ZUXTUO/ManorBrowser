/**
 * 动态背景容器类
 * 作为一个管理容器 (FrameLayout)，负责根据用户的选择切换不同的背景特效。
 * 支持多种视觉特效：流星、下雨、飘雪、极光、樱花等。
 */
package com.olsc.manorbrowser.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class DynamicBackgroundView extends FrameLayout {
    /** 特效模式枚举 */
    public enum EffectMode {
        /** 流星特效 (默认) */
        METEOR, 
        /** 雨滴特效 */
        RAIN, 
        /** 飘雪特效 */
        SNOW, 
        /** 极光渐变特效 */
        AURORA, 
        /** 樱花飘落特效 */
        SAKURA, 
        /** 纯色（无动效） */
        SOLID,
        /** 自定义图片 */
        IMAGE
    }

    private EffectMode currentMode = EffectMode.METEOR;
    private String customImagePath;

    public void setCustomImagePath(String path) {
        this.customImagePath = path;
    }

    public DynamicBackgroundView(Context context) {
        this(context, null);
    }

    public DynamicBackgroundView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DynamicBackgroundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setMode(currentMode);
    }

    /**
     * 设置并更新特效模式
     * 清空子视图并根据模式实例化对应的特效 View
     */
    public void setMode(EffectMode mode) {
        this.currentMode = mode;
        removeAllViews();
        Context context = getContext();
        
        switch (mode) {
            case RAIN:
                addView(new RainView(context));
                break;
            case SNOW:
                addView(new SnowView(context));
                break;
            case AURORA:
                addView(new AuroraView(context));
                break;
            case SAKURA:
                addView(new SakuraView(context));
                break;
            case SOLID:
                android.view.View solidView = new android.view.View(context);
                solidView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                addView(solidView);
                break;
            case IMAGE:
                android.widget.ImageView imageView = new android.widget.ImageView(context);
                imageView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
                imageView.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                if (customImagePath != null) {
                    imageView.setImageURI(android.net.Uri.fromFile(new java.io.File(customImagePath)));
                } else {
                     // 尝试从默认位置加载
                     java.io.File defaultFile = new java.io.File(context.getFilesDir(), "custom_bg.jpg");
                     if (defaultFile.exists()) {
                         imageView.setImageURI(android.net.Uri.fromFile(defaultFile));
                     }
                }
                addView(imageView);
                break;
            case METEOR:
            default:
                addView(new MeteorView(context));
                break;
        }
    }
}
