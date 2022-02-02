package com.example.democamera;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
import static android.provider.MediaStore.MediaColumns.DISPLAY_NAME;
import static android.provider.MediaStore.MediaColumns.MIME_TYPE;
import static android.provider.MediaStore.MediaColumns.RELATIVE_PATH;
import static androidx.camera.core.CameraSelector.DEFAULT_BACK_CAMERA;
import static androidx.camera.lifecycle.ProcessCameraProvider.getInstance;
import static com.example.democamera.R.id.fillEnd;
import static com.example.democamera.R.id.image_capture_button;
import static com.example.democamera.R.id.position;
import static com.example.democamera.R.id.video_capture_button;
import static com.example.democamera.R.id.viewFinder;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCapture.OnImageSavedCallback;
import androidx.camera.core.ImageCapture.OutputFileOptions;
import androidx.camera.core.ImageCapture.OutputFileOptions.Builder;
import androidx.camera.core.ImageCapture.OutputFileResults;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.video.Recorder;
import androidx.camera.video.Recording;
import androidx.camera.video.VideoCapture;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

/**
 * Ich bin blöd.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAKEN_PHOTO_KEY = "takenPhoto";

    //  private ActivityMainBinding viewBinding;
    private ImageCapture imageCapture;
    private VideoCapture<Recorder> videoCapture;
    private Recording recording;
    private ExecutorService cameraExecutor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{CAMERA, RECORD_AUDIO, WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE},
                    10
            );
        }

        // Set up the listeners for take photo and video capture buttons
        findViewById(image_capture_button).setOnClickListener(this);
        //viewBinding.videoCaptureButton.setOnClickListener(this);

        cameraExecutor = newSingleThreadExecutor();
    }

    private boolean allPermissionsGranted() {
        return ContextCompat.checkSelfPermission(
                this,
                CAMERA
        ) == PERMISSION_GRANTED;
    }

    private void startCamera() {
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case image_capture_button:
                //action
                takePhoto();
                break;

            case video_capture_button:
                //action
                captureVideo();
                break;
        }
    }

    private void takePhoto() {
        ContentValues contentValues = new ContentValues();
        String fileName = "CameraX_" + LocalDateTime.now();
        contentValues.put(DISPLAY_NAME, fileName);
        contentValues.put(MIME_TYPE, "image/jpeg");
        contentValues.put(RELATIVE_PATH, "Pictures/CameraX-Image");
//        File mediaStorageDir = getExternalFilesDir(Environment.getExternalStorageDirectory());

        try {
            Files.walk(Paths.get(Environment.getExternalStorageDirectory().toURI())).forEach(System.out::println);
        } catch (IOException e) {
            e.printStackTrace();
        }


        OutputFileOptions options =
                new Builder(getContentResolver(), EXTERNAL_CONTENT_URI, contentValues)
                .build();

        File file = new File(fileName);

        imageCapture.takePicture(options, cameraExecutor,
                new OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(OutputFileResults outputFileResults) {
                        // insert your code here.
                        try {
                            Files.copy(Paths.get(outputFileResults.getSavedUri().toString()), Paths.get(getApplicationContext().getDataDir().toURI()));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        File file1 = new File(Objects.requireNonNull(outputFileResults.getSavedUri()).toString());
                        Intent photoIntent = new Intent(MainActivity.this, Photo.class);
                        photoIntent.putExtra(TAKEN_PHOTO_KEY, file1.toString());
                        startActivity(photoIntent);
                        Log.d("ajodsfoajdsfojhasojfojhfooasdijofsda", outputFileResults.getSavedUri().getPath());
                    }

                    @Override
                    public void onError(ImageCaptureException error) {
                        // insert your code here.
                        Log.d("dfsdf", "dfsd2");
                    }
                }
        );

    }

    private void captureVideo() {
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();
        Preview preview = new Preview.Builder()
                .build();

        preview.setSurfaceProvider(((PreviewView) findViewById(viewFinder)).getSurfaceProvider());
        imageCapture =
                new ImageCapture.Builder()
                        .setTargetRotation(findViewById(viewFinder).getDisplay().getRotation())
                        .build();
        Camera camera = cameraProvider.bindToLifecycle(this, DEFAULT_BACK_CAMERA, imageCapture, preview);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cameraExecutor.shutdown();
    }

}