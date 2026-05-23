package com.photozhengli.app;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.getcapacitor.annotation.Permission;
import com.getcapacitor.annotation.PermissionCallback;
import com.getcapacitor.PermissionState;

import androidx.core.content.ContextCompat;
import android.content.pm.PackageManager;

import java.util.HashSet;
import java.util.Set;

@CapacitorPlugin(
    name = "MediaAccessPlugin",
    permissions = {
        @Permission(
            strings = { Manifest.permission.READ_EXTERNAL_STORAGE },
            alias = "storage"
        ),
        @Permission(
            strings = { Manifest.permission.READ_MEDIA_IMAGES },
            alias = "mediaImages"
        )
    }
)
public class MediaAccessPlugin extends Plugin {

    private static final String TAG = "MediaAccessPlugin";

    @PluginMethod
    public void checkPermissions(PluginCall call) {
        boolean granted = hasReadPermission();
        Log.d(TAG, "checkPermissions: granted=" + granted + ", SDK=" + Build.VERSION.SDK_INT);
        JSObject result = new JSObject();
        result.put("granted", granted);
        call.resolve(result);
    }

    @PluginMethod
    public void requestPermissions(PluginCall call) {
        if (hasReadPermission()) {
            Log.d(TAG, "requestPermissions: already granted");
            JSObject result = new JSObject();
            result.put("granted", true);
            call.resolve(result);
            return;
        }

        // Use Capacitor's built-in permission request
        String alias = Build.VERSION.SDK_INT >= 33 ? "mediaImages" : "storage";
        Log.d(TAG, "requestPermissions: requesting alias=" + alias);
        requestPermissionForAlias(alias, call, "permissionResultCallback");
    }

    @PermissionCallback
    private void permissionResultCallback(PluginCall call) {
        boolean granted = hasReadPermission();
        Log.d(TAG, "permissionResultCallback: granted=" + granted);
        JSObject result = new JSObject();
        result.put("granted", granted);
        call.resolve(result);
    }

    @PluginMethod
    public void getPhotos(PluginCall call) {
        if (!hasReadPermission()) {
            call.reject("Storage permission not granted. Call requestPermissions() first.", "PERMISSION_DENIED");
            return;
        }

        int quantity = call.getInt("quantity", 50);
        int offset = call.getInt("offset", 0);
        boolean ascending = call.getBoolean("ascending", false);
        String albumId = call.getString("albumId", null);

        try {
            JSArray photos = queryPhotos(quantity, offset, ascending, albumId);
            JSObject result = new JSObject();
            result.put("photos", photos);
            call.resolve(result);
        } catch (Exception e) {
            Log.e(TAG, "Error getting photos", e);
            call.reject("Error getting photos: " + e.getMessage());
        }
    }

    @PluginMethod
    public void getAlbums(PluginCall call) {
        if (!hasReadPermission()) {
            call.reject("Storage permission not granted. Call requestPermissions() first.", "PERMISSION_DENIED");
            return;
        }

        try {
            JSArray albums = queryAlbums();
            JSObject result = new JSObject();
            result.put("albums", albums);
            call.resolve(result);
        } catch (Exception e) {
            Log.e(TAG, "Error getting albums", e);
            call.reject("Error getting albums: " + e.getMessage());
        }
    }

    private boolean hasReadPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_MEDIA_IMAGES)
                == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
        }
    }

    private JSArray queryPhotos(int quantity, int offset, boolean ascending, String albumId) {
        JSArray photos = new JSArray();
        ContentResolver resolver = getContext().getContentResolver();

        String[] projection = {
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.DATE_MODIFIED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.WIDTH,
            MediaStore.Images.Media.HEIGHT,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        };

        Uri collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);

        // Use Bundle-based query for pagination (API 26+, our minSdk is 29)
        Bundle queryArgs = new Bundle();

        // Sort
        queryArgs.putStringArray(ContentResolver.QUERY_ARG_SORT_COLUMNS,
            new String[]{ MediaStore.Images.Media.DATE_ADDED });
        queryArgs.putInt(ContentResolver.QUERY_ARG_SORT_DIRECTION,
            ascending ? ContentResolver.QUERY_SORT_DIRECTION_ASCENDING
                      : ContentResolver.QUERY_SORT_DIRECTION_DESCENDING);

        // Pagination
        queryArgs.putInt(ContentResolver.QUERY_ARG_LIMIT, quantity);
        queryArgs.putInt(ContentResolver.QUERY_ARG_OFFSET, offset);

        // Album filter
        if (albumId != null && !albumId.isEmpty()) {
            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION,
                MediaStore.Images.Media.BUCKET_ID + "=?");
            queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS,
                new String[]{ albumId });
        }

        Log.d(TAG, "queryPhotos: quantity=" + quantity + ", offset=" + offset +
            ", ascending=" + ascending + ", albumId=" + albumId);

        Cursor cursor = null;
        try {
            cursor = resolver.query(collection, projection, queryArgs, null);

            if (cursor == null) {
                Log.e(TAG, "queryPhotos: cursor is null!");
                return photos;
            }

            Log.d(TAG, "queryPhotos: cursor count=" + cursor.getCount());

            int idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
            int nameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            int dateAddedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
            int dateModifiedCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
            int sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
            int widthCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH);
            int heightCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT);
            int bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
            int bucketNameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

            while (cursor.moveToNext()) {
                long id = cursor.getLong(idCol);
                Uri contentUri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                JSObject photo = new JSObject();
                photo.put("id", String.valueOf(id));
                photo.put("contentUri", contentUri.toString());
                photo.put("filename", cursor.getString(nameCol));
                photo.put("dateAdded", cursor.getLong(dateAddedCol) * 1000);
                photo.put("dateModified", cursor.getLong(dateModifiedCol) * 1000);
                photo.put("size", cursor.getLong(sizeCol));
                photo.put("width", cursor.getInt(widthCol));
                photo.put("height", cursor.getInt(heightCol));
                photo.put("albumId", cursor.getString(bucketIdCol) != null ? cursor.getString(bucketIdCol) : "");
                photo.put("albumName", cursor.getString(bucketNameCol) != null ? cursor.getString(bucketNameCol) : "");

                photos.put(photo);
            }
        } catch (Exception e) {
            Log.e(TAG, "queryPhotos exception: " + e.getMessage(), e);
            // Fallback: try without pagination args
            try {
                Log.d(TAG, "queryPhotos: trying fallback without Bundle pagination...");
                String selection = null;
                String[] selectionArgs = null;
                if (albumId != null && !albumId.isEmpty()) {
                    selection = MediaStore.Images.Media.BUCKET_ID + "=?";
                    selectionArgs = new String[]{ albumId };
                }
                String sortOrder = MediaStore.Images.Media.DATE_ADDED +
                    (ascending ? " ASC" : " DESC");

                Cursor fallbackCursor = resolver.query(collection, projection,
                    selection, selectionArgs, sortOrder);

                if (fallbackCursor != null) {
                    Log.d(TAG, "queryPhotos fallback: cursor count=" + fallbackCursor.getCount());
                    int count = 0;
                    int skipped = 0;

                    int idCol2 = fallbackCursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
                    int nameCol2 = fallbackCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                    int dateAddedCol2 = fallbackCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
                    int dateModifiedCol2 = fallbackCursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_MODIFIED);
                    int sizeCol2 = fallbackCursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE);
                    int widthCol2 = fallbackCursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH);
                    int heightCol2 = fallbackCursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT);
                    int bucketIdCol2 = fallbackCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
                    int bucketNameCol2 = fallbackCursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

                    while (fallbackCursor.moveToNext() && count < quantity) {
                        if (skipped < offset) {
                            skipped++;
                            continue;
                        }
                        long id = fallbackCursor.getLong(idCol2);
                        Uri contentUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);

                        JSObject photo = new JSObject();
                        photo.put("id", String.valueOf(id));
                        photo.put("contentUri", contentUri.toString());
                        photo.put("filename", fallbackCursor.getString(nameCol2));
                        photo.put("dateAdded", fallbackCursor.getLong(dateAddedCol2) * 1000);
                        photo.put("dateModified", fallbackCursor.getLong(dateModifiedCol2) * 1000);
                        photo.put("size", fallbackCursor.getLong(sizeCol2));
                        photo.put("width", fallbackCursor.getInt(widthCol2));
                        photo.put("height", fallbackCursor.getInt(heightCol2));
                        photo.put("albumId", fallbackCursor.getString(bucketIdCol2) != null ? fallbackCursor.getString(bucketIdCol2) : "");
                        photo.put("albumName", fallbackCursor.getString(bucketNameCol2) != null ? fallbackCursor.getString(bucketNameCol2) : "");

                        photos.put(photo);
                        count++;
                    }
                    fallbackCursor.close();
                }
            } catch (Exception e2) {
                Log.e(TAG, "queryPhotos fallback also failed: " + e2.getMessage(), e2);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        Log.d(TAG, "queryPhotos result: " + photos.length() + " photos");
        return photos;
    }

    private JSArray queryAlbums() {
        JSArray albums = new JSArray();
        ContentResolver resolver = getContext().getContentResolver();

        String[] projection = {
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media._ID
        };

        Uri collection;
        if (Build.VERSION.SDK_INT >= 29) {
            collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
        } else {
            collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        String sortOrder = MediaStore.Images.Media.DATE_ADDED + " DESC";

        // Track unique bucket IDs
        Set<String> seenBucketIds = new HashSet<>();

        try (Cursor cursor = resolver.query(collection, projection, null, null, sortOrder)) {
            if (cursor != null) {
                int bucketIdCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_ID);
                int bucketNameCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
                int idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);

                // First pass: collect album info with counts
                java.util.Map<String, JSObject> albumMap = new java.util.LinkedHashMap<>();
                java.util.Map<String, Integer> countMap = new java.util.LinkedHashMap<>();
                java.util.Map<String, Long> coverMap = new java.util.LinkedHashMap<>();

                while (cursor.moveToNext()) {
                    String bucketId = cursor.getString(bucketIdCol);
                    if (bucketId == null) continue;

                    String bucketName = cursor.getString(bucketNameCol);
                    if (bucketName == null) bucketName = "未知相册";

                    if (!albumMap.containsKey(bucketId)) {
                        JSObject album = new JSObject();
                        album.put("id", bucketId);
                        album.put("name", bucketName);
                        albumMap.put(bucketId, album);
                        // First photo in this album is the cover (sorted by date desc)
                        coverMap.put(bucketId, cursor.getLong(idCol));
                    }

                    countMap.put(bucketId, countMap.getOrDefault(bucketId, 0) + 1);
                }

                for (java.util.Map.Entry<String, JSObject> entry : albumMap.entrySet()) {
                    String bucketId = entry.getKey();
                    JSObject album = entry.getValue();
                    album.put("count", countMap.getOrDefault(bucketId, 0));

                    Long coverId = coverMap.get(bucketId);
                    if (coverId != null) {
                        Uri coverUri = ContentUris.withAppendedId(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, coverId);
                        album.put("coverUri", coverUri.toString());
                    } else {
                        album.put("coverUri", "");
                    }

                    albums.put(album);
                }
            }
        }

        Log.d(TAG, "Found " + albums.length() + " albums");
        return albums;
    }
}
