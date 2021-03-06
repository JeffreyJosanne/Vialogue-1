package com.comp.iitb.vialogue.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation;
import com.comp.iitb.vialogue.R;
import com.comp.iitb.vialogue.adapters.SlideThumbnailsRecyclerViewAdapter;
import com.comp.iitb.vialogue.library.camera.CameraPreview;
import com.comp.iitb.vialogue.listeners.OnSwipeListener;
import com.comp.iitb.vialogue.models.ParseObjects.models.Resources.Image;
import com.comp.iitb.vialogue.models.ParseObjects.models.Slide;
import com.comp.iitb.vialogue.models.ParseObjects.models.interfaces.BaseResourceClass;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.bitmap;
import static android.R.attr.contextClickable;
import static android.os.Build.VERSION.SDK_INT;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class CameraActivity extends AppCompatActivity {

    // constants
    public static final int REQUEST_CAMERA_PERMISSION = 876;
    public static final int REQUEST_WRITE_EXTERNAL_STORAGE = 877;
    public static final int REQUEST_READ_EXTERNAL_STORAGE = 878;
    public static final int CAMERA_ID = 0;
    public static final String RESULT_KEY = "photos_array_list";
    public static final String IMAGE_PATHS_ARRAY = "image_paths_array";
    public static final String CAPTURE_MULTIPLE_IMAGES_INTENT_KEY = "capture_multiple_images";
    public static final String CAPTURE_SINGLE_IMAGE_INTENT_KEY = "capture_single_image";
    public static final int FOCUS_AREA_SIZE = 100;

    // state variables
    private int mPermissionsRequiredCount;
    private int mPermissionsGrantedCount;

    // variables
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private CapturesRecyclerViewAdapter mCapturesRecyclerViewAdapter;
    private Camera.PictureCallback mPictureCallback;
    private Camera.ShutterCallback mShutterCallback;
    private int mCameraDisplayRotation;
    private ArrayList<String> mImagePaths;
    private boolean mCaptureMultipleImages;
    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback;

    // UI Variables
    private FrameLayout mCameraPreviewFrameLayout;
    private ImageButton mCaptureButton;
    private RecyclerView mCapturesRecyclerView;
    private FrameLayout mFrameOverlay;
    private ImageButton mDoneButton;
    private ImageButton mRetryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null && bundle.getBoolean(CAPTURE_SINGLE_IMAGE_INTENT_KEY)) {
            // single image camera
            mCaptureMultipleImages = false;
        } else {
            mCaptureMultipleImages = true;
        }

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //Remove notification bar
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        getSupportActionBar().hide();

        setContentView(R.layout.activity_camera);

        // initializing state
        mPermissionsRequiredCount = 0;
        mPermissionsGrantedCount = 0;

        // initializing UI Components
        mCameraPreviewFrameLayout = (FrameLayout) findViewById(R.id.camera_preview);
        mCaptureButton = (ImageButton) findViewById(R.id.button_capture);
        mFrameOverlay = (FrameLayout) findViewById(R.id.frame_overlay);
        mDoneButton = (ImageButton) findViewById(R.id.done_button);
        mRetryButton = (ImageButton) findViewById(R.id.retry_button);
        if(mCaptureMultipleImages) {
            mCapturesRecyclerView = (RecyclerView) findViewById(R.id.captures_recycler_view);
        }

        // initializing variables
        mImagePaths = new ArrayList<String>();
        if(mCaptureMultipleImages) {
            mCapturesRecyclerViewAdapter = new CapturesRecyclerViewAdapter(CameraActivity.this);
            mCapturesRecyclerView.setAdapter(mCapturesRecyclerViewAdapter);
            mPictureCallback = new Camera.PictureCallback() {

                @Override
                public void onPictureTaken(final byte[] data, Camera camera) {

                    (new AsyncTask<Void, Void, String>() {
                        @Override
                        public String doInBackground(Void... params) {

                            File pictureFile = BaseResourceClass.makeTempResourceFile(Slide.ResourceType.IMAGE, CameraActivity.this);
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(pictureFile);
                                fos.write(data);
                                fos.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if(fos != null) {
                                    try {
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            // resize image
                            if(!Image.resizeImage(CameraActivity.this, Uri.fromFile(pictureFile))) {
                                Toast.makeText(CameraActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                            }

                            return pictureFile.getAbsolutePath();
                        }

                        @Override
                        public void onPostExecute(String imagePath) {
                            mCapturesRecyclerViewAdapter.add(imagePath);
                        }
                    }).execute();

                    mCamera.startPreview();
                }
            };
        } else {
            mRetryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDoneButton.setVisibility(View.GONE);
                    mRetryButton.setVisibility(View.GONE);
                    mCamera.startPreview();
                    mImagePaths.clear();
                }
            });

            mPictureCallback = new Camera.PictureCallback() {

                @Override
                public void onPictureTaken(final byte[] data, Camera camera) {

                    (new AsyncTask<Void, Void, String>() {

                        @Override
                        public void onPreExecute() {
                            mCamera.stopPreview();
                        }

                        @Override
                        public String doInBackground(Void... params) {

                            File pictureFile = BaseResourceClass.makeTempResourceFile(Slide.ResourceType.IMAGE, CameraActivity.this);
                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(pictureFile);
                                fos.write(data);
                                fos.close();
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                if(fos != null) {
                                    try {
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            return pictureFile.getAbsolutePath();
                        }

                        @Override
                        public void onPostExecute(String imagePath) {
                            mImagePaths.add(imagePath);
                            mRetryButton.setVisibility(View.VISIBLE);
                            mDoneButton.setVisibility(View.VISIBLE);
                        }
                    }).execute();
                }
            };

        }

        mShutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                (new AsyncTask<Void, Void, Void>() {
                    @Override
                    public void onPreExecute() {
                        mFrameOverlay.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public Void doInBackground(Void... params) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    public void onPostExecute(Void result) {
                        mFrameOverlay.setVisibility(View.GONE);
                    }
                }).execute();
            }
        };

        mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                if (success) {
                    System.out.println("auto focus success");
                    // TODO show an indication of focusing
                } else {
                    System.out.println("auto focus failed");
                }
            }
        };

        // add listeners (only if camera started successfully)
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mCamera.takePicture(mShutterCallback, null, mPictureCallback);
                } catch (Exception e) {}
            }
        });

        mDoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent data = new Intent();
                System.out.println("mImagePaths : " + mImagePaths);
                data.putStringArrayListExtra(RESULT_KEY, mImagePaths);
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    public void onPermissionsSatisfied() {
        if(!checkCameraHardware()) {
            Toast.makeText(CameraActivity.this, R.string.noCameraFound, Toast.LENGTH_SHORT).show();
            finish();
        }
        setUpCamera();
        startCamera();
    }

    // Check if this device has a camera
    private boolean checkCameraHardware() {
        if (CameraActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    // check if camera permissions given
    private void checkCameraPermissions() {
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mPermissionsRequiredCount = 3;
            mPermissionsGrantedCount = 0;
            if(!(ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(CameraActivity.this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
            } else {
                mPermissionsGrantedCount += 1;
            }

            if (!(ContextCompat.checkSelfPermission(CameraActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                mPermissionsGrantedCount += 1;
            }

            if (!(ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_EXTERNAL_STORAGE);
            } else {
                mPermissionsGrantedCount += 1;
            }

            if(mPermissionsGrantedCount == mPermissionsRequiredCount) {
                onPermissionsSatisfied();
            }
        }
    }

    private void setUpCamera() {
        try {
            mCamera = Camera.open(CAMERA_ID); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
            e.printStackTrace();
            Toast.makeText(CameraActivity.this, R.string.cameraNotAvailable, Toast.LENGTH_LONG).show();
            finish();
        }

        // set display orientation based on the screen
        // (vertical in our case)
        setCameraDisplayOrientation();

        //set camera to continually auto-focus
        Camera.Parameters params = mCamera.getParameters();
        //*EDIT*//params.setFocusMode("continuous-picture");
        //It is better to use defined constraints as opposed to String
        for(String focusMode: params.getSupportedFocusModes()) {
            if(focusMode.equals(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }
        }

        mCamera.setParameters(params);
    }

    public void startCamera() {
        mCameraPreview = new CameraPreview(CameraActivity.this, CameraActivity.this, mCamera);

        // set camera picture and preview size
        // based on the sizes calculated in the mCameraPreview
        Camera.Parameters params = mCamera.getParameters();
        params.setPreviewSize(mCameraPreview.getPreviewWidth(), mCameraPreview.getPreviewHeight());
        params.setPictureSize(mCameraPreview.getPreviewWidth(), mCameraPreview.getPreviewHeight());
        mCamera.setParameters(params);

        // start camera (finally)
        mCameraPreviewFrameLayout.addView(mCameraPreview);

        // focus on touch
        mCameraPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    focusOnTouch(event);
                }
                return true;
            }
        });
    }

    public void setCameraDisplayOrientation() {
        android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(CAMERA_ID, info);

        int rotation = CameraActivity.this.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        mCameraDisplayRotation = result;
        mCamera.setDisplayOrientation(result);

        //STEP #2: Set the 'rotation' parameter
        Camera.Parameters params = mCamera.getParameters();
        params.setRotation(mCameraDisplayRotation);
        mCamera.setParameters(params);
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        releaseCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        // try to start camera
        checkCameraPermissions();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putStringArrayList(IMAGE_PATHS_ARRAY, mImagePaths);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mImagePaths = savedInstanceState.getStringArrayList(IMAGE_PATHS_ARRAY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionsGrantedCount += 1;
                } else {
                    Toast.makeText(CameraActivity.this, R.string.gimmeCamera, Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case REQUEST_WRITE_EXTERNAL_STORAGE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionsGrantedCount += 1;
                } else {
                    Toast.makeText(CameraActivity.this, R.string.gimmeStorage, Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            case REQUEST_READ_EXTERNAL_STORAGE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mPermissionsGrantedCount += 1;
                } else {
                    Toast.makeText(CameraActivity.this, R.string.gimmeStorage, Toast.LENGTH_LONG).show();
                    finish();
                }
                break;
        }

        if(mPermissionsGrantedCount == mPermissionsRequiredCount) {
            onPermissionsSatisfied();
        }
    }

    public class RotateTransformation extends BitmapTransformation {

        private float rotateRotationAngle = 0f;

        public RotateTransformation(Context context, float rotateRotationAngle) {
            super(context);
            this.rotateRotationAngle = rotateRotationAngle;
        }

        @Override
        protected Bitmap transform(BitmapPool pool, Bitmap toTransform, int outWidth, int outHeight) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotateRotationAngle);
            return Bitmap.createBitmap(toTransform, 0, 0, toTransform.getWidth(), toTransform.getHeight(), matrix, true);
        }

        @Override
        public String getId() {
            return "rotate" + rotateRotationAngle;
        }
    }

    private class CapturesRecyclerViewAdapter extends RecyclerView.Adapter<CapturesRecyclerViewAdapter.CaptureViewHolder> {

        public class CaptureViewHolder extends RecyclerView.ViewHolder{
            public ImageView thumbnail;

            public CaptureViewHolder(View view) {
                super(view);
                thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            }
        }

        private Context mContext;
        private GestureDetector mDetector;
        private OnSwipeListener mOnSwipeListener;

        public CapturesRecyclerViewAdapter(Context context) {
            mContext = context;
        }

        @Override
        public CaptureViewHolder onCreateViewHolder(ViewGroup parentGroup, int viewType) {
            View slideView = LayoutInflater.from(parentGroup.getContext()).inflate(R.layout.camera_thumbnail, parentGroup, false);
            return new CapturesRecyclerViewAdapter.CaptureViewHolder(slideView);
        }

        public void onBindViewHolder(CapturesRecyclerViewAdapter.CaptureViewHolder viewHolder, final int position) {
            Glide
                    .with(mContext)
                    .load(new File(mImagePaths.get(position)))
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .into(viewHolder.thumbnail)
            ;
        }

        public void add(String imagePath) {
            mImagePaths.add(imagePath);
            notifyItemInserted(mImagePaths.size()-1);
            mDoneButton.setVisibility(View.VISIBLE);
        }

        @Override
        public int getItemCount() {
            return mImagePaths.size();
        }
    }

    private void focusOnTouch(MotionEvent event) {
        if (mCamera != null ) {

            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0){
                Rect rect = calculateFocusArea(event.getX(), event.getY());

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                meteringAreas.add(new Camera.Area(rect, 800));
                parameters.setFocusAreas(meteringAreas);

                mCamera.setParameters(parameters);
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }else {
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }
        }
    }

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / mCameraPreview.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / mCameraPreview.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper)+focusAreaSize/2>1000){
            if (touchCoordinateInCameraReper>0){
                result = 1000 - focusAreaSize/2;
            } else {
                result = -1000 + focusAreaSize/2;
            }
        } else{
            result = touchCoordinateInCameraReper - focusAreaSize/2;
        }
        return result;
    }

}
