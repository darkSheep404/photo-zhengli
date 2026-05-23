package com.photozhengli.app;

import android.os.Bundle;
import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        registerPlugin(MovePhotoPlugin.class);
        registerPlugin(TrashPhotoPlugin.class);
        registerPlugin(MediaAccessPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
