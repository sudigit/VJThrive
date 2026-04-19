package com.vjti.vjthrive.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;

import java.util.HashMap;
import java.util.Map;

public class CloudinaryHelper {
    private static final String TAG = "CloudinaryHelper";
    private static boolean isInitialized = false;

    public static void init(Context context) {
        if (isInitialized)
            return;

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", CloudinaryConfig.CLOUD_NAME);
        config.put("secure", "true");
        MediaManager.init(context, config);
        isInitialized = true;
    }

    public interface UploadListener {
        void onSuccess(String url);

        void onError(String error);
    }

    public static void uploadFile(Uri fileUri, String folderName, UploadListener listener) {
        Log.d(TAG, "Uploading file to folder " + folderName + ": " + fileUri.toString());

        MediaManager.get().upload(fileUri)
                .unsigned(CloudinaryConfig.UPLOAD_PRESET)
                .option("folder", CloudinaryConfig.FOLDER_NAME + "/" + folderName)
                .option("resource_type", "auto")
                .callback(new UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        Log.d(TAG, "Upload started: " + requestId);
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Can implement progress bar updates here if needed
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        Log.d(TAG, "Upload success: " + url);
                        if (listener != null) {
                            listener.onSuccess(url);
                        }
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Upload error: " + error.getDescription());
                        if (listener != null) {
                            listener.onError(error.getDescription());
                        }
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) {
                        Log.e(TAG, "Upload rescheduled: " + error.getDescription());
                    }
                }).dispatch();
    }
}
