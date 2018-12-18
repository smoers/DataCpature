package com.aerospace.sabena.datacpature;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private TextRecognizer textRecognizer;
    private TextView tv_result;
    private CameraSource cameraSource;
    private SurfaceView surfaceView;
    private final int PERMISSION_REQUEST_CAMERA = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView = (SurfaceView) findViewById(R.id.surface_camera);
        tv_result = (TextView) findViewById(R.id.tv_result);
        startCameraSource();
    }

    private void startCameraSource() {

        textRecognizer = new TextRecognizer.Builder(this).build();
        if(!textRecognizer.isOperational()){

        }
        cameraSource = new CameraSource.Builder(getApplicationContext(), textRecognizer)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(640,512)
                .setRequestedFps(2.0f)
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback(){

            @SuppressLint("MissingPermission")
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if(isCameraPermissionGranted()){
                        cameraSource.start(holder);
                    } else {
                        requestForPermission();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<TextBlock> detections) {
                final SparseArray<TextBlock> items = detections.getDetectedItems();
                if(items.size() <= 0){
                    return;
                } else {
                    tv_result.post(new Runnable() {
                        @Override
                        public void run() {
                            StringBuilder stringBuilder = new StringBuilder();
                            for (int i=0; i<items.size();i++) {
                                stringBuilder.append(items.valueAt(i).getValue());
                                stringBuilder.append('\n');
                            }
                            tv_result.setText(stringBuilder.toString());
                        }
                    });
                }
            }
        });

    }


    private boolean isCameraPermissionGranted(){
        return checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestForPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode != PERMISSION_REQUEST_CAMERA) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
            if(isCameraPermissionGranted()){
                try {
                    cameraSource.start(surfaceView.getHolder());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(this, "Camera permission denied !", Toast.LENGTH_LONG);
                finish();
            }
        }
    }
}
