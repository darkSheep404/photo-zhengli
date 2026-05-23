package com.photozhengli.app;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.RecoverableSecurityException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.util.Collections;

@CapacitorPlugin(name = "MovePhotoPlugin")
public class MovePhotoPlugin extends Plugin {

    private static final String TAG = "MovePhotoPlugin";
    private ActivityResultLauncher<IntentSenderRequest> writeRequestLauncher;
    private PluginCall pendingCall;
    private Uri pendingUri;
    private String pendingAlbumName;

    @Override
    public void load() {
        super.load();
        Activity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            writeRequestLauncher = ((AppCompatActivity) activity).registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(),
                result -> {
                    if (pendingCall == null) return;
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // User granted permission, retry the move
                        try {
                            performMove(pendingUri, pendingAlbumName);
                            pendingCall.resolve();
                        } catch (Exception e) {
                            Log.e(TAG, "Move after permission grant failed", e);
                            pendingCall.reject("Move failed after permission grant: " + e.getMessage());
                        }
                    } else {
                        pendingCall.reject("User denied write permission");
                    }
                    pendingCall = null;
                    pendingUri = null;
                    pendingAlbumName = null;
                }
            );
        }
    }

    @PluginMethod
    public void movePhoto(PluginCall call) {
        String photoUri = call.getString("photoUri");
        String albumName = call.getString("albumName");

        if (photoUri == null || albumName == null) {
            call.reject("photoUri and albumName are required");
            return;
        }

        Log.d(TAG, "movePhoto: uri=" + photoUri + ", album=" + albumName);

        try {
            ContentResolver resolver = getContext().getContentResolver();
            Uri uri = Uri.parse(photoUri);

            // If photoUri is a file path, convert to content URI
            if (!photoUri.startsWith("content://")) {
                uri = getContentUriFromFilePath(resolver, photoUri);
                if (uri == null) {
                    call.reject("Could not find photo in MediaStore");
                    return;
                }
            }

            try {
                performMove(uri, albumName);
                call.resolve();
            } catch (RecoverableSecurityException e) {
                Log.d(TAG, "RecoverableSecurityException, requesting user permission");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    // Android 11+: use createWriteRequest
                    PendingIntent pi = MediaStore.createWriteRequest(resolver, Collections.singletonList(uri));
                    if (writeRequestLauncher != null) {
                        pendingCall = call;
                        pendingUri = uri;
                        pendingAlbumName = albumName;
                        call.setKeepAlive(true);
                        writeRequestLauncher.launch(new IntentSenderRequest.Builder(pi.getIntentSender()).build());
                    } else {
                        call.reject("Cannot request write permission");
                    }
                } else {
                    // Android 10: use RecoverableSecurityException's action
                    IntentSender sender = e.getUserAction().getActionIntent().getIntentSender();
                    if (writeRequestLauncher != null) {
                        pendingCall = call;
                        pendingUri = uri;
                        pendingAlbumName = albumName;
                        call.setKeepAlive(true);
                        writeRequestLauncher.launch(new IntentSenderRequest.Builder(sender).build());
                    } else {
                        call.reject("Cannot request write permission");
                    }
                }
            } catch (SecurityException e) {
                Log.e(TAG, "SecurityException (non-recoverable)", e);
                // Android 11+: try createWriteRequest
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    try {
                        PendingIntent pi = MediaStore.createWriteRequest(resolver, Collections.singletonList(uri));
                        if (writeRequestLauncher != null) {
                            pendingCall = call;
                            pendingUri = uri;
                            pendingAlbumName = albumName;
                            call.setKeepAlive(true);
                            writeRequestLauncher.launch(new IntentSenderRequest.Builder(pi.getIntentSender()).build());
                        } else {
                            call.reject("Cannot request write permission: " + e.getMessage());
                        }
                    } catch (Exception e2) {
                        call.reject("Security exception: " + e.getMessage());
                    }
                } else {
                    call.reject("Security exception: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error moving photo", e);
            call.reject("Error moving photo: " + e.getMessage());
        }
    }

    private void performMove(Uri uri, String albumName) throws SecurityException {
        ContentResolver resolver = getContext().getContentResolver();
        String targetPath = "Pictures/" + albumName;

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.RELATIVE_PATH, targetPath);

        int updated = resolver.update(uri, values, null, null);
        Log.d(TAG, "performMove: updated=" + updated + " rows for path=" + targetPath);
        if (updated <= 0) {
            throw new RuntimeException("Failed to move photo, 0 rows updated");
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
