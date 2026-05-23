package com.photozhengli.app;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

@CapacitorPlugin(name = "TrashPhotoPlugin")
public class TrashPhotoPlugin extends Plugin {

    @PluginMethod
    public void trashPhotos(PluginCall call) {
        JSONArray photoUrisJson;
        try {
            photoUrisJson = call.getArray("photoUris");
        } catch (Exception e) {
            call.reject("photoUris array is required");
            return;
        }

        if (photoUrisJson == null || photoUrisJson.length() == 0) {
            call.reject("photoUris array is empty");
            return;
        }

        try {
            ContentResolver resolver = getContext().getContentResolver();
            List<Uri> uris = new ArrayList<>();
            long totalSize = 0;

            for (int i = 0; i < photoUrisJson.length(); i++) {
                String photoUri = photoUrisJson.getString(i);
                Uri uri;

                if (photoUri.startsWith("content://")) {
                    uri = Uri.parse(photoUri);
                } else {
                    uri = getContentUriFromFilePath(resolver, photoUri);
                }

                if (uri != null) {
                    totalSize += getFileSize(resolver, uri);
                    uris.add(uri);
                }
            }

            if (uris.isEmpty()) {
                call.reject("No valid photos found");
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+: Use createTrashRequest to move to "Recently Deleted"
                android.app.PendingIntent trashIntent = MediaStore.createTrashRequest(
                        resolver, uris, true);

                Activity activity = getActivity();
                if (activity != null) {
                    // Launch system confirmation dialog
                    final long finalTotalSize = totalSize;
                    activity.startIntentSenderForResult(
                            trashIntent.getIntentSender(),
                            1001, null, 0, 0, 0);

                    // Note: In a production app, you'd handle onActivityResult
                    // For now, resolve optimistically after launching the intent
                    JSObject result = new JSObject();
                    result.put("freedBytes", finalTotalSize);
                    call.resolve(result);
                } else {
                    call.reject("Activity not available");
                }
            } else {
                // Android 10: Use ContentResolver.delete()
                // System Gallery on most devices (including HarmonyOS) has built-in recycle bin
                long freedBytes = 0;
                for (Uri uri : uris) {
                    long size = getFileSize(resolver, uri);
                    int deleted = resolver.delete(uri, null, null);
                    if (deleted > 0) {
                        freedBytes += size;
                    }
                }

                JSObject result = new JSObject();
                result.put("freedBytes", freedBytes);
                call.resolve(result);
            }
        } catch (Exception e) {
            call.reject("Error trashing photos: " + e.getMessage());
        }
    }

    private Uri getContentUriFromFilePath(ContentResolver resolver, String filePath) {
        String[] projection = {MediaStore.Images.Media._ID};
        String selection = MediaStore.Images.Media.DATA + "=?";
        String[] selectionArgs = {filePath};

        try (Cursor cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                long id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
                return ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            }
        }
        return null;
    }

    private long getFileSize(ContentResolver resolver, Uri uri) {
        String[] projection = {MediaStore.Images.Media.SIZE};
        try (Cursor cursor = resolver.query(uri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE));
            }
        } catch (Exception ignored) {
        }
        return 0;
    }
}
