package com.example.lyrics;

import android.app.IntentService;
import android.content.Intent;

import com.example.lyrics.LyricsService;


public class StopService extends IntentService {
    public StopService() {
        super("StopService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        stopService(new Intent(this, LyricsService.class));
    }

}
