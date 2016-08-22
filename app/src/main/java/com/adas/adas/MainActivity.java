package com.adas.adas;

import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;


public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.video:
                checkItem(item);
                return true;
            case R.id.image:
                checkItem(item);
                return true;
            case R.id.vehicle_detection:
                checkItem(item);
                return true;
            case R.id.lane_detection:
                checkItem(item);
                return true;
            case R.id.people_detection:
                checkItem(item);
                return true;
            case R.id.color_conversion:
                checkItem(item);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void checkItem(MenuItem item){
        if (item.isChecked() == false)
            item.setChecked(true);
        else if(item.isChecked() == true)
            item.setChecked(false);
    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this){
        @Override
            public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("OpenCV", "OpenCV Loaded successfully");
                    mOpenCVCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };

    private JavaCameraView mOpenCVCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*if (!OpenCVLoader.initDebug()) {
            Log.i(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        } else {
            Log.i(this.getClass().getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }*/


        mOpenCVCameraView = (JavaCameraView) findViewById(R.id.MainActivityCameraView);
        mOpenCVCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCVCameraView.setCvCameraViewListener(this);
    }

    public void onResume() {
            super.onResume();
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);

    }

    public void onDestory(){
        super.onDestroy();
        if(mOpenCVCameraView != null){
            mOpenCVCameraView.disableView();
        }
    }


    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
    }
}
