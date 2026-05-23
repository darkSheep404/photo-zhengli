package com.photozhengli.app;

import android.app.Activity;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "MovePhotoPlugin")
public class MovePhotoPlugin extends Plugin {

    @PluginMethod
    public void movePhoto(PluginCall call) {
        String photoUri = call.getString("photoUri");
        String albumName = call.getString("albumName");

        if (photoUri == null || albumName == null) {
            call.reject("photoUri and albumName are required");
            return;
        }

        try {
            ContentResolver resolver = getContext().getContentResolver();
            Uri uri = Uri.parse(photoUri);

            // If photoUri is a file path (Android identifier), convert to content URI
            if (!photoUri.startsWith("content://")) {
                uri = getContentUriFromFilePath(resolver, photoUri);
                if (uri == null) {
                    call.reject("Could not find photo in MediaStore");
                    return;
                }
            }

            String targetPath = "Pictures/" + albumName;

            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.RELATIVE_PATH, targetPath);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // Android 11+: may need write permission via createWriteRequest
                try {
                    resolver.update(uri, values, null, null);
                    call.resolve();
                } catch (SecurityException e) {
                    if (e instanceof RecoverableSecurityException) {
                        RecoverableSecurityException rse = (RecoverableSecurityException) e;
                        IntentSender sender = rse.getUserAction().getActionIntent().getIntentSender();
                        // For simplicity, try direct update first; if it fails,
                        // we need user interaction which requires Activity result handling
                        call.reject("Write permission required. Please grant access.", "PERMISSION_REQUIRED");
                    } else {
                        call.reject("Security exception: " + e.getMessage());
                    }
                }
            } else {
                // Android 10: direct update via Scoped Storage
                int updated = resolver.update(uri, values, null, null);
                if (updated > 0) {
                    call.resolve();
                } else {
                    call.reject("Failed to move photo");
                }
            }
        } catch (Exception e) {
            call.reject("Error moving photo: " + e.getMessage());
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
                return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));
            }
        }
        return null;
    }
}
