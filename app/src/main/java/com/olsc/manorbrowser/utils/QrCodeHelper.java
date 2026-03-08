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
     * 识别 Bitmap 中的多个二维码
     * @param bitmap 要识别的图片
     * @return 识别出的文本内容列表，若未找到则返回空数组
     */
    public static String[] scanMultiQrCodes(Bitmap bitmap) {
        if (bitmap == null) return new String[0];

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        LuminanceSource source = new RGBLuminanceSource(width, height, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
        hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
        hints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);

        try {
            com.google.zxing.multi.qrcode.QRCodeMultiReader reader = new com.google.zxing.multi.qrcode.QRCodeMultiReader();
            Result[] results = reader.decodeMultiple(binaryBitmap, hints);
            String[] resultTexts = new String[results.length];
            for (int i = 0; i < results.length; i++) {
                resultTexts[i] = results[i].getText();
            }
            return resultTexts;
        } catch (NotFoundException e) {
            // 如果多重读取失败，尝试单读取（有些时候单读取能找到，多重读取却找不到）
            try {
                MultiFormatReader reader = new MultiFormatReader();
                Result result = reader.decode(binaryBitmap, hints);
                return new String[]{result.getText()};
            } catch (Exception e1) {
                return new String[0];
            }
        } catch (Exception e) {
            return new String[0];
        }
    }

    /**
     * 兼容旧版本的单识别方法
     */
    public static String scanQrCode(Bitmap bitmap) {
        String[] results = scanMultiQrCodes(bitmap);
        return results.length > 0 ? results[0] : null;
    }
}
