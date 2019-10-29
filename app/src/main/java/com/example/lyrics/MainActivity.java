package com.example.lyrics;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    CardView cardView1, cardView2, cardView3, cardView4;

    // permissions dialog stuff
    AlertDialog alertDialog;
    View dialogView;
    Button overlayButton;
    Button storageButton;
    ImageView overlayCheck;
    ImageView storageCheck;

    boolean storageFolderCreated;

    private static final int STORAGE_PERMISSION_REQUEST = 112;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setContentView(R.layout.activity_main);
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkForPermissions();

        File file = new File(getExternalFilesDir(null), "Lyrics");
        if (file.exists()) {
            storageFolderCreated = true;
        } else {
            storageFolderCreated = file.mkdir();
        }

        cardView1 = findViewById(R.id.card1);  // download lyrics
        cardView2 = findViewById(R.id.card2);  // watch tutorial
        cardView3 = findViewById(R.id.card3);  // view source code
        cardView4 = findViewById(R.id.card4);  // view other apps

        cardView1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (storageFolderCreated) {
                    // progress dialog shown while we get a list of songs on the device
                    ProgressDialog progressDialog = new ProgressDialog(MainActivity.this, R.style.DownloadDialog);
                    progressDialog.setMessage(getResources().getString(R.string.please_wait));
                    progressDialog.setCancelable(false);
                    progressDialog.show();

                    final ArrayList<Song> songArrayList = new ArrayList<>();
                    // get the list of songs present on the device
                    String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
                    String[] projection = {
                            MediaStore.Audio.Media._ID,
                            MediaStore.Audio.Media.ARTIST,
                            MediaStore.Audio.Media.TITLE,
                            MediaStore.Audio.Media.DURATION
                    };
                    Cursor cursor = getContentResolver().query(
                            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                            projection,
                            selection,
                            null,
                            null);
                    while (cursor.moveToNext()) {
                        String artist = cursor.getString(1);
                        String title = cursor.getString(2);
                        long songID = Long.parseLong(cursor.getString(0));
                        long duration = Long.parseLong(cursor.getString(3));
                        if ((duration / 1000) > 40) {
                            songArrayList.add(new Song(title, artist, songID));
                        }

                    }
                    cursor.close();
                    progressDialog.dismiss();

                    // yes-no dialog before we start downloading the lyrics
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.DownloadDialog);
                    builder.setMessage(getResources().getString(R.string.download_prompt, songArrayList.size()))
                        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(getApplicationContext(), DownloadService.class);
                                intent.putExtra("songs", songArrayList);
                                startService(intent);
                            }
                        })
                        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create().show();
                }
            }
        });

        cardView2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.youtube.com/watch?v=g0XidfCGZHU"));
                startActivity(intent);
            }
        });

        cardView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://github.com/shkcodes/lyrically"));
                startActivity(intent);
            }
        });

        cardView4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.shkmishra.instadict"));
                startActivity(intent);
            }
        });
    }

    @SuppressLint("NewApi")
    private void checkForPermissions() {

        final String[] storagePermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};


        boolean isStoragePermission = (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
        boolean isOverlayPermission = Settings.canDrawOverlays(this);

        // if either of the permissions haven't been granted, we show the permissions dialog
        if (!isStoragePermission || !isOverlayPermission) {
            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            dialogBuilder.setCancelable(false);
            dialogView = inflater.inflate(R.layout.permissions_dialog, null);
            dialogBuilder.setView(dialogView);

            overlayButton = dialogView.findViewById(R.id.button_overlay);
            storageButton = dialogView.findViewById(R.id.button_storage);

            overlayCheck = dialogView.findViewById(R.id.overlay_check);
            storageCheck = dialogView.findViewById(R.id.storage_check);

            // storage permission already granted; disable the button
            if (isStoragePermission) {
                storageButton.setClickable(false);
                storageCheck.setVisibility(View.VISIBLE);
                storageButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorPrimary));
            }

            // draw over other apps permission already granted; disable the button
            if (isOverlayPermission) {
                overlayButton.setClickable(false);
                overlayCheck.setVisibility(View.VISIBLE);
                overlayButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorPrimary));
            }

            // request the respective permissions on button click
            overlayButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivityForResult(intent, 23);
                }
            });
            storageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(MainActivity.this, storagePermissions, STORAGE_PERMISSION_REQUEST);
                }
            });


            alertDialog = dialogBuilder.create();
            alertDialog.show();

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_mainactivity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuSettings:
                startActivity(new Intent(this, PreferenceActivity.class));
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    @SuppressLint("NewApi")
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // disable the storage permission button
                    storageButton.setClickable(false);
                    storageCheck.setVisibility(View.VISIBLE);
                    storageButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorPrimary));

                    // if both the permissions have been granted, dismiss the permissions dialog
                    if (Settings.canDrawOverlays(this) && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
                        alertDialog.dismiss();

                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 23) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && Settings.canDrawOverlays(this)) {

                // disable the overlay permission button
                overlayButton.setClickable(false);
                overlayCheck.setVisibility(View.VISIBLE);
                overlayButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.colorPrimary));

                // if both the permissions have been granted, dismiss the permissions dialog
                if (Settings.canDrawOverlays(this) && (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED))
                    alertDialog.dismiss();

            }
        }


    }
}
