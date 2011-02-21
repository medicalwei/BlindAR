package edu.ntou.blindar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

class DrawOnTop extends View {
	Bitmap mBitmap;
	boolean mBegin;
	byte[] mYUVData;
	int mImageWidth, mImageHeight;

    public DrawOnTop(Context context) {
        super(context);
        
        mBegin = false;
        mBitmap = null;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBegin)
        {        	
        	mBitmap = edgeDetect(mYUVData, mImageWidth, mImageHeight);
        	canvas.drawBitmap(mBitmap, 0, 0, null);
        	
        	
        } // end if statement
        
        super.onDraw(canvas);
        
    }
    
    /* Use Y only, no Cb nor Cr at all. */
	public Bitmap edgeDetect(byte fg[], int width, int height)
	{
		Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
		
		for (int y = 1; y < height-1; y++) {
			
			for (int x = 1; x < width-1; x++) {
				
				int offset = y*width + x;
				
				float O = fg[offset];
				float A = fg[offset-1] - O; // x-1, y
				float B = fg[offset-width] - O; // x, y-1
				
				int t = (A*A+B*B) > 22500?
						Color.argb(255, 255, 255, 255):Color.argb(127, 0, 0, 0);

				result.setPixel(x, y, t);
			}
		}
		
		return result;
	}
	
	/*
	public Bitmap edgeDetectWithSobelAlgorithm(byte fg[], int width, int height)
	{
		Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
		
		int [][] GX= { { 1, 0, -1 }, { 2, 0, -2 }, { 1, 0, -1 } };
		int [][] GY= { { 1, 2, 1 }, { 0, 0, 0 }, { -1, -2, -1 } };
		
		for (int y = 1; y < height-1; y++) {
			
			for (int x = 1; x < width-1; x++) {
				float A=0, B=0;
				for (int f = -1; f <= 1; f++) {
					for (int g = -1; g <= 1; g++) {
						A += fg[(y+f)*width + (x+g)] * GX[f+1][g+1];
						B += fg[(y+f)*width + (x+g)] * GY[f+1][g+1];
					}
				}
				
				int c = (int) Math.sqrt(A*A+B*B);
				int t = Color.argb(127+c/2, c, c, c);

				result.setPixel(x, y, t);
			}
		}
		
		return result;
	}
	*/
}
