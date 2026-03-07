package com.olsc.manorbrowser.utils;

import android.graphics.Bitmap;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Reader;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.EnumMap;
import java.util.Map;

/**
 * 二维码识别辅助类
 */
public class QrCodeHelper {

    /**
     * 识别 Bitmap 中的二维码
     * @param bitmap 要识别的图片
     * @return 识别出的文本内容，若未找到二维码则返回 null
     */
    public static String scanQrCode(Bitmap bitmap) {
        if (bitmap == null) return null;

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        Reader reader = new MultiFormatReader();
        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        try {
            Result result = reader.decode(binaryBitmap, hints);
            return result.getText();
        } catch (NotFoundException | ChecksumException | FormatException e) {
            // 未找到二维码
            return null;
        }
    }
}
