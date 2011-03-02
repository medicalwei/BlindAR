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
	int mThreshold=20, mDotted, mCThreshold=20, mCDotted;

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
        	canvas.drawText("T: " + mThreshold + ", D: " + mDotted + ", CT: "+ mCThreshold + " , CD: " + mCDotted, 10, 10, paint);
        } // end if statement
        
        super.onDraw(canvas);
        
    }
    
    /* Use Y only, no Cb nor Cr at all. */
    
	public Bitmap edgeDetect(byte fg[], int width, int height)
	{
		Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
		mDotted = mCDotted = 0;
		int[] resultBuffer = new int[height*width];
		int[] N = new int[height*width];
		
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
				
				N[offset] = A*A+B*B;
			}
		}

		
		for (int y = 2; y < height-2; y++) {
			for (int x = 2; x < width-2; x++) {
				int offset = y*width + x;
				int Q = N[offset];
				
				if (Q>mThreshold)
				{
					if (Q>mCThreshold &&
						Q>N[offset-1] && Q>N[offset+1] &&
						Q>N[offset-width] && Q>N[offset+width] &&
						
						Q>N[offset-width-1] && Q>N[offset-width+1] &&
						Q>N[offset+width-1] && Q>N[offset+width+1]
					){
						mCDotted += 1;
						resultBuffer[offset] = Color.RED;
					}
					else
					{
						resultBuffer[offset] = Color.WHITE;
						mDotted += 1;
					}
				}
			}
		}
		/* Adjust threshold by looking at the number of dots */
		mThreshold += (mDotted-5000) / 100;
		mCThreshold += (mCDotted-30) / 5;
		
		if(mThreshold<5) mThreshold = 5;
		else if(mThreshold>65535) mThreshold = 65535;
		
		if(mCThreshold<5) mCThreshold = 5;
		else if(mCThreshold>65535) mCThreshold = 65535;
		
		result.setPixels(resultBuffer,0,width,0,0,width,height);
		
		return result;
	}

	
		
}
