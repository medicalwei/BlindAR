package edu.ntou.blindar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

class DrawOnTop extends View {
	boolean mBegin;
	byte[] mYUVData;
	int mImageWidth, mImageHeight;
	int mThreshold=20, mDotted;

    public DrawOnTop(Context context) {
        super(context);
        mBegin = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBegin)
        {
        	Paint paint=new Paint();
        	paint.setARGB(255, 127, 127, 127);
        	canvas.drawColor(Color.argb(127,0,0,0));
        	canvas.drawBitmap(edgeDetect(mYUVData, mImageWidth, mImageHeight), 0, 0, null);
        	canvas.drawText("Threshold: " + mThreshold + ", Dotted: " + mDotted, 10, 10, paint);
        } // end if statement
        
        super.onDraw(canvas);
        
    }
    
    /* Use Y only, no Cb nor Cr at all. */
    
	public Bitmap edgeDetect(byte fg[], int width, int height)
	{
		Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);

		mDotted = 0;
		
		for (int y = 1; y < height-1; y++) {
			
			for (int x = 1; x < width-1; x++) {
				
				int offset = y*width + x;

				/*  Unsigned byte
				 *  Reference:
				 *  
				 *  public static int unsignedByteToInt(byte b) {
				 *  	return (int) b & 0xFF;
    			 *  }
				 */
				
				int O = (int) fg[offset] & 0xFF;
				int A = Math.abs(((int) fg[offset-1] & 0xFF) - O); // x-1, y
				int B = Math.abs(((int) fg[offset-width] & 0xFF) - O); // x, y-1
				
				if(A+B>mThreshold)
				{
					result.setPixel(x, y, Color.WHITE);
					mDotted += 1;
				}
			}
		}

		/* Adjust threshold by looking at the number of dots */
		    if(mDotted>10000) mThreshold += 5;
		else if(mDotted>7500) mThreshold += 3;
		else if(mDotted>5000) mThreshold += 1;
		else if(mDotted<1000) mThreshold -= 5;
		else if(mDotted<2000) mThreshold -= 3;
		else if(mDotted<4000) mThreshold -= 1;
		
		if(mThreshold<5) mThreshold = 5;
		else if(mThreshold>255) mThreshold = 255;
		
		return result;
	}

	
		
}
