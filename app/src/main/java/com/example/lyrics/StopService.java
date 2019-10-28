package com.example.lyrics;

import android.app.IntentService;
import android.content.Intent;

import com.shkmishra.lyrically.LyricsService;


public class StopService extends IntentService {
    public StopService() {
        super("StopService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        stopService(new Intent(this, LyricsService.class));
    }

}
