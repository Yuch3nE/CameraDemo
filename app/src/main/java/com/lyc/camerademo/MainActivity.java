package com.lyc.camerademo;

import android.app.Activity;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.Window;
import android.widget.RelativeLayout;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity{

    private SurfaceView mSurfaceView;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    public void init(){
        mSurfaceView = (SurfaceView)findViewById(R.id.camerasurfaceView);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mCamera = Camera.open();
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int w, int h) {
                Log.e("Error" ,"w"+w+"h"+h);
                float mh = (float)w * (16f/9);
                System.out.println("width"+w+"height"+mh);
                Camera.Parameters parameters = mCamera.getParameters();
//                parameters.setPreviewSize(w, h);
//                List<Camera.Size> vSizeList = parameters.getSupportedPictureSizes();
//                //for (int num = 0; num < vSizeList.size(); num++) {
//                Camera.Size vSize = vSizeList.get(0);
//                parameters.setPreviewSize(vSize.width, vSize.height);
//                // }
                /*获取摄像头支持的PictureSize列表*/
                List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
                for(Camera.Size size:pictureSizeList){
                    System.out.println(size.width + ":" + size.height);
                }
                System.out.println(pictureSizeList);
                /*从列表中选取合适的分辨率*/
                Camera.Size picSize = getProperSize(pictureSizeList, ((float) w) / mh);
                if(null != picSize)
                {
                    parameters.setPictureSize(picSize.width, picSize.height);
                }
                else
                {
                    picSize = parameters.getPictureSize();
                }
                /*获取摄像头支持的PreviewSize列表*/
                List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
                for(Camera.Size size:previewSizeList){
                    System.out.println(size.width + ":" + size.height);
                }
                System.out.println(previewSizeList);
                Camera.Size preSize = getProperSize(previewSizeList, ((float) w) / mh);
                if(null != preSize)
                {
                    System.out.println("TestCameraActivityTag" + preSize.width + "," + preSize.height);
                    parameters.setPreviewSize(preSize.width, preSize.height);
                }else{
                    System.out.println("NULL");
                }

                /*根据选出的PictureSize重新设置SurfaceView大小*/
                float width = picSize.width;
                float height = picSize.height;
                System.out.println("Surface width" + width + "height" + height);

                float bili = width/height;

                float surface_width = w;
                float surface_height = w * bili;

                System.out.println("new"+surface_width + "h:" + surface_height + "  "+ bili);
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mSurfaceView.getLayoutParams();
                lp.width = (int)surface_width;
                lp.height = (int)surface_height;
                mSurfaceView.setLayoutParams(lp);
                //mSurfaceView.setLayoutParams(new RelativeLayout.LayoutParams((int) (h * (width / height)), h));

                parameters.setJpegQuality(100); // 设置照片质量
                if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE))
                {
                    parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                }
                mCamera.cancelAutoFocus();//只有加上了这一句，才会自动对焦。
                //mCamera.setDisplayOrientation(0);
                mCamera.setDisplayOrientation(getPreviewDegree(MainActivity.this));
                mCamera.setParameters(parameters);
                try {
                    //设置显示
                    mCamera.setPreviewDisplay(surfaceHolder);
                } catch (IOException exception) {
                    mCamera.release();
                    mCamera = null;
                }
                //开始预览
                mCamera.startPreview();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
                mCamera.release();
                mCamera = null;
            }
        });
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void initCamera(){
        int NumOfCamera = Camera.getNumberOfCameras();

    }

    // 提供一个静态方法，用于根据手机方向获得相机预览画面旋转的角度
    public static int getPreviewDegree(Activity activity) {
        // 获得手机的方向
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该选择的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }


    public static Camera.Size getProperSize(List<Camera.Size> sizeList, float displayRatio)
    {
        //先对传进来的size列表进行排序
        Collections.sort(sizeList, new SizeComparator());

        Camera.Size result = null;
        for(Camera.Size size: sizeList)
        {
            float curRatio =  ((float)size.height) / size.width;
            if(curRatio - displayRatio == 0)
            {
                result = size;
            }
        }
        if(null == result)
        {
            for(Camera.Size size: sizeList)
            {
                float curRatio =  ((float)size.width) / size.height;
                if(curRatio == 3f/4)
                {
                    result = size;
                }
            }
        }
        return result;
    }

    static class SizeComparator implements Comparator<Camera.Size>
    {

        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            // TODO Auto-generated method stub
            Camera.Size size1 = lhs;
            Camera.Size size2 = rhs;
            if(size1.width < size2.width
                    || size1.width == size2.width && size1.height < size2.height)
            {
                return -1;
            }
            else if(!(size1.width == size2.width && size1.height == size2.height))
            {
                return 1;
            }
            return 0;
        }

    }
}

