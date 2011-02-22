package edu.ntou.blindar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

class DrawOnTop extends View {
	boolean mBegin;
	byte[] mYUVData;
	int mImageWidth, mImageHeight;

    public DrawOnTop(Context context) {
        super(context);
        
        mBegin = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBegin)
        {
        	canvas.drawColor(Color.argb(127,0,0,0));
        	canvas.drawBitmap(edgeDetect(mYUVData, mImageWidth, mImageHeight), 0, 0, null);
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
				
				if(A*A+B*B>10000)
				{
					result.setPixel(x, y, Color.WHITE);
				}
			}
		}
		
		return result;
	}
	
	
    /*
	public Bitmap edgeDetect(byte fg[], int width, int height)
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
				
				if(A*A+B*B>22500)
				{
					result.setPixel(x, y, Color.WHITE);
				}
			}
		}
		
		return result;
	}
	*/
	
}
