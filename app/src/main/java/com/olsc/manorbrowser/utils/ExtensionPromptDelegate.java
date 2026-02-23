package com.olsc.manorbrowser.utils;

import android.app.AlertDialog;
import android.content.Context;
import org.mozilla.geckoview.AllowOrDeny;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.WebExtension;
import org.mozilla.geckoview.WebExtensionController;
import android.widget.Toast;
import androidx.annotation.NonNull;

public class ExtensionPromptDelegate implements WebExtensionController.PromptDelegate {
    private Context context;

    public ExtensionPromptDelegate(Context context) {
        this.context = context;
    }

    @Override
    public GeckoResult<WebExtension.PermissionPromptResponse> onInstallPromptRequest(
            @NonNull WebExtension extension,
            @NonNull String[] permissions,
            @NonNull String[] origins,
            @NonNull String[] dataCollectionPermissions) {

        if (context instanceof android.app.Activity) {
            ((android.app.Activity) context).runOnUiThread(() -> {
                Toast.makeText(context, context.getString(com.olsc.manorbrowser.R.string.msg_installing_extension), Toast.LENGTH_SHORT).show();
            });
        }
        
        try {
            WebExtension.PermissionPromptResponse response = new WebExtension.PermissionPromptResponse(true, false, false);
            return GeckoResult.fromValue(response);
        } catch (Exception e) {
            e.printStackTrace();
            return GeckoResult.fromValue(null);
        }
    }
}
