package com.aerospace.sabena.datacpature;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private SurfaceHolder surfaceHolder;
    private static final String TAG = "SNAE";

    private TextView tv_result;
    private TextView tx_coord;
    private SurfaceView surfaceView;
    private final int PERMISSION_REQUEST_CAMERA = 100;

    //Defini le Detector
    private TextRecognizer textRecognizer;

    //Defini la camera
    private CameraSource cameraSource;

    Detector.Processor<TextBlock> textBlockProcessor = new Detector.Processor<TextBlock>() {
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
                        StringBuilder coord = new StringBuilder();
                        for (int i=0; i<items.size();i++) {
                            stringBuilder.append(items.valueAt(i).getValue());
                            stringBuilder.append('\n');
                            Point[] points = items.get(i).getCornerPoints();
                            if(points != null) {
                                for (int ii = 0; ii < points.length; ii++) {
                                    Point point = points[ii];
                                    coord.append(point.toString());
                                }
                                coord.append("\n");
                            }
                        }
                        tv_result.setText(stringBuilder.toString());
                        tx_coord.setText(coord.toString());
                    }
                });
            }
        }
    };


    //Defini le callback pour le holder
    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            surfaceHolder = holder;
            Log.d(TAG, "SurfaceCreated");
        }

        @SuppressLint("MissingPermission")
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG,"SurfaceChanged");
            try {
                cameraSource.start(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            surfaceHolder = null;
        }
    };

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_result = (TextView) findViewById(R.id.tv_result);
        tx_coord = (TextView) findViewById(R.id.tx_coord);

        textRecognizer = new TextRecognizer.Builder(this).build();
        textRecognizer.setProcessor(textBlockProcessor);

        cameraSource = new CameraSource.Builder(this, textRecognizer)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1280,1024)
                .setRequestedFps(2.0f)
                .build();
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.camera_layout);
        Log.d(TAG,"CustomSurfaceView");
        CustomSurfaceView customSurfaceView = new CustomSurfaceView(this);
        layout.addView(customSurfaceView,0);
        customSurfaceView.getHolder().addCallback(surfaceCallback);
    }

    static class CustomSurfaceView extends SurfaceView{

        private Paint textPaint;

        public CustomSurfaceView(Context context) {
            super(context);
            this.setWillNotDraw(false);

            textPaint = new Paint();
            textPaint.setARGB( 255, 0, 128, 0 );
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.save();
            int width = canvas.getWidth();
            int height = canvas.getHeight();
            Log.d(TAG, Integer.toString(width)+" - "+Integer.toString(height));
            textPaint.setTextSize(30);
            canvas.drawText("Moers",100, 100,textPaint);
            canvas.restore();
        }
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
                    cameraSource.start(surfaceHolder);
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
