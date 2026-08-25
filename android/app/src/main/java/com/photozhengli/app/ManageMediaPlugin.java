package com.photozhengli.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "ManageMediaPlugin")
public class ManageMediaPlugin extends Plugin {

    private ActivityResultLauncher<Intent> permissionRequestLauncher;
    private PluginCall pendingCall;

    @Override
    public void load() {
        super.load();
        Activity activity = getActivity();
        if (activity instanceof AppCompatActivity) {
            permissionRequestLauncher = ((AppCompatActivity) activity).registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (pendingCall == null) return;
                    pendingCall.resolve(createStatus());
                    pendingCall = null;
                }
            );
        }
    }

    @PluginMethod
    public void getStatus(PluginCall call) {
        call.resolve(createStatus());
    }

    @PluginMethod
    public void requestPermission(PluginCall call) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            call.resolve(createStatus());
            return;
        }

        if (MediaStore.canManageMedia(getContext())) {
            call.resolve(createStatus());
            return;
        }

        if (permissionRequestLauncher == null) {
            call.reject("Cannot open media management settings");
            return;
        }

        pendingCall = call;
        call.setKeepAlive(true);
        permissionRequestLauncher.launch(new Intent(Settings.ACTION_REQUEST_MANAGE_MEDIA));
    }

    private JSObject createStatus() {
        JSObject status = new JSObject();
        boolean supported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S;
        status.put("supported", supported);
        status.put("granted", supported && MediaStore.canManageMedia(getContext()));
        return status;
    }
}