/**
 * Android 工具测试类。
 */
package com.olsc.manorbrowser;
import android.content.Context;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
/**
 * 仪表化测试，将在 Android 设备上执行。
 *
 * @see <a href="http://d.android.com/tools/testing">测试文档</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // 被测应用程序的 Context。
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("com.olsc.manorbrowser", appContext.getPackageName());
    }
}