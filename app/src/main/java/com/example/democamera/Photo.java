package com.example.democamera;

import static android.net.Uri.fromFile;
import static com.example.democamera.MainActivity.TAKEN_PHOTO_KEY;

import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Photo extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String photoFileName = extras.getString(TAKEN_PHOTO_KEY);
            ImageView imageView = findViewById(R.id.imageView);
            File file = new File(photoFileName);
            System.out.println(file.exists());
            Uri uri = fromFile(file);
            imageView.setImageURI(uri);
//            imageView.setImageURI(Uri.parse("/media/external/images/media/32"));
        }

        try {
            Files.walk(getApplication().getDataDir().toPath())
            .forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File file = new File("/storage/emulated/0/Android/data/com.example.democamera/files/Pictures/CameraX-Image");

        ImageView imageView = findViewById(R.id.imageView);
        String uriString = getApplication().getDataDir().toString() + "/Pictures/37";
        imageView.setImageURI(Uri.parse(uriString));
        File dataDir = getApplication().getDataDir();
        System.out.println(dataDir);
    }
}