package edu.ntou.blindar;

import java.io.IOException;

import edu.ntou.blindar.DrawOnTop;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

class Preview extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder mHolder;
    Camera mCamera;
    DrawOnTop mDrawOnTop;
    boolean mFinished;

    Preview(Context context, DrawOnTop drawOnTop) {
        super(context);
        
        mDrawOnTop = drawOnTop;
        mFinished = false;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
        
        try {
           mCamera.setPreviewDisplay(holder);
           
           // Preview callback used whenever new viewfinder frame is available
           mCamera.setPreviewCallback(new PreviewCallback() {
        	  public void onPreviewFrame(byte[] data, Camera camera)
        	  {
        		  if ( (mDrawOnTop == null) || mFinished )
        			  return;
        		  
        		  if (mDrawOnTop.mBegin == false)
        		  {
        			  // Initialize the draw-on-top companion
        			  Camera.Parameters params = camera.getParameters();
        			  mDrawOnTop.mImageWidth = params.getPreviewSize().width;
        			  mDrawOnTop.mImageHeight = params.getPreviewSize().height;
        			  mDrawOnTop.mYUVData = new byte[data.length];
        			  mDrawOnTop.mBegin = true;
        		  }
        		  
        		  // Pass YUV data to draw-on-top companion
        		  // System.arraycopy(data, 0, mDrawOnTop.mYUVData, 0, data.length);
        		  mDrawOnTop.mYUVData=data;
    			  mDrawOnTop.invalidate();
        	  }
           });
        } 
        catch (IOException exception) {
            mCamera.release();
            mCamera = null;
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // Surface will be destroyed when we return, so stop the preview.
        // Because the CameraDevice object is not a shared resource, it's very
        // important to release it when the activity is paused.
    	mFinished = true;
    	mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // Now that the size is known, set up the camera parameters and begin
        // the preview.
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(320, 240);
        parameters.setPreviewFrameRate(15);
        parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(parameters);
        mCamera.startPreview();
    }

}