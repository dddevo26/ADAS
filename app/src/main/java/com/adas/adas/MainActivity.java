package com.adas.adas;

import android.content.Context;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.io.FileOutputStream;
import java.io.InputStream;

// This includes the necessary libraries from OpenCV 3.1
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

// This includes the necessary libraries to read and write files into internal memory.
import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {

    // Member variables
    private static final String     TAG = "ADAS";
    private static final int        VIEW_MODE_LINES = 1;
    private double				    angle = 0;
    private Mat					    lines;
    private float                   mRelativeFaceSize   = 0.2f;
    private Mat                     mRgba;
    private Mat                     mGray;
    private Mat                     mIntermediateMat;
    private File                    mCascadeFile;
    private Rect                    mRoi;
    private CascadeClassifier       mJavaDetector;
    private int                     mAbsoluteFaceSize   = 0;
    private static final int	    VIEW_MODE_OBJECT = 2;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    // reads the xml file for vehicle detection and loads
                    // the file into the cascade classier called mJavaDetector
                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.cars3);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mCascadeFile = new File(cascadeDir, "cars3.xml");
                        FileOutputStream os = new FileOutputStream(mCascadeFile);

                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();

                        mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        mJavaDetector.load(mCascadeFile.getAbsolutePath());

                        if (mJavaDetector.empty()) {
                            Log.e(TAG, "Failed to load cars3.xml");
                            mJavaDetector = null;
                        }else{
                            Log.e(TAG, "Load of cars3.xml success");
                        }

                        cascadeDir.delete();
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCVCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    private JavaCameraView mOpenCVCameraView;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mOpenCVCameraView = (JavaCameraView) findViewById(R.id.MainActivityCameraView);
        mOpenCVCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCVCameraView.setCvCameraViewListener(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_1_0, this, mLoaderCallback);

    }

    public void onDestory() {
        super.onDestroy();
        if (mOpenCVCameraView != null) {
            mOpenCVCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGray = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGray.release();
        mIntermediateMat.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        final int viewMode = 2;
        int height = mGray.rows();
        int width = mGray.cols();

        mRoi = new Rect(0, height/2, width, (height/2));
        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();

        Mat roiRgb = new Mat(mRgba, mRoi);
        Mat roiGray = new Mat(mGray, mRoi);

        MatOfRect faces = new MatOfRect();

        mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);

        lines = new Mat();

        switch (viewMode) {

            case VIEW_MODE_LINES: // Uses Hough Lines
                // input frame has gray scale format
                Imgproc.cvtColor(roiRgb, roiGray, Imgproc.COLOR_RGB2GRAY, 4);
                Imgproc.equalizeHist(roiGray, roiGray);
                Imgproc.blur(roiGray, roiGray, new Size(3, 3));
                Imgproc.Canny(roiGray, roiGray, 50, 200);


                //element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, size, point);
                //Imgproc.morphologyEx(mGray, mGray, Imgproc.MORPH_OPEN , element);

                Imgproc.HoughLinesP(roiGray, lines, 1, Math.PI / 180, 25, 50, 20);

                for (int x = 0; x < lines.cols(); x++) {
                    double[] vec = lines.get(0, x);
                    double x1 = vec[0],
                            y1 = vec[1] + (height / 2),
                            x2 = vec[2],
                            y2 = vec[3] + (height / 2);
                    Point start = new Point(x1, y1);
                    Point end = new Point(x2, y2);

                    double xDiff = start.x - end.x;
                    double yDiff = start.y - end.y;
                    angle = Math.toDegrees(Math.atan2(yDiff, xDiff));
                    if (angle < 0.0) angle += 180;
                    if (angle >= 10 && angle <= 170) {
                        Imgproc.line(mRgba, start, end, new Scalar(255, 0, 0), 2);
                    }
                }
                break;

            case VIEW_MODE_OBJECT:
            {
                if (mJavaDetector != null)
                    mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, new Size(mAbsoluteFaceSize, mAbsoluteFaceSize), new Size());

                Rect[] facesArray = faces.toArray();
                for (int i = 0; i < facesArray.length; i++)
                    Imgproc.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), new Scalar(255,0,0,255), 3);
            }
            break;
        }

        return mRgba;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.adas.adas/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.adas.adas/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
}
