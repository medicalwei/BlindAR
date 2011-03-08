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
	boolean mDrawEdges=true;
	boolean areaDetected=false;

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
        	// canvas.drawColor(Color.argb(127,0,0,0));
        	canvas.drawBitmap(edgeDetect(mYUVData, mImageWidth, mImageHeight), 0, 0, null);
        	canvas.drawText("T: " + mThreshold + ", D: " + mDotted, 10, 10, paint);
        } // end if statement
        
        super.onDraw(canvas);
        
    }
    
    /* Use Y only, no Cb nor Cr at all. */
    
	public Bitmap edgeDetect(byte fg[], int width, int height)
	{
		Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		if(!mDrawEdges) return result;
		mDotted = 0;
		int[] resultBuffer = new int[height*width];
		short[] N = new short[height*width];
		// int[] bitmapRGB = new int[height*width];
		
		decodeYUV(resultBuffer, fg, width, height);
		
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
				
				short O = (short) (fg[offset] & 0xFF);
				
				/*
				 * 
				 * 
				short A = (short) ((fg[offset-1] & 0xFF) - O); // x-1, y
				short B = (short) ((fg[offset-width] & 0xFF) - O); // x, y-1
				
				N[offset] = (short) (A+B);
				*/
				
				N[offset] = (short) ((fg[offset-1] & 0xFF) - O);

			}
		}

		for (int y = 2; y < height-2; y++) {
			for (int x = 2; x < width-2; x++) {
				int offset = y*width + x;
				if (Math.abs(N[offset])>mThreshold)
				{
					if (N[offset]>0)
					{
						resultBuffer[offset] = Color.RED;	
					}
					else if (N[offset]<0)
					{
						resultBuffer[offset] = Color.BLUE;
					}
					mDotted += 1;
				}
				else
				{
					if(areaDetected)
					{
						resultBuffer[offset] = Color.GREEN;
					}
				}
			}
		}
		
		/* Adjust threshold by looking at the number of dots */
		mThreshold += (mDotted-5000) / 500;
		
		if(mThreshold<5) mThreshold = 5;
		else if(mThreshold>255) mThreshold = 255;
		
		result.setPixels(resultBuffer,0,width,0,0,width,height);
		
		return result;
	}

	public static void decodeYUV(int[] out, byte[] fg, int width, int height) {
		final int sz = width * height;
		int i, j;
		int Y, Cr = 0, Cb = 0;
		for(j = 0; j < height; j++) {
			int pixPtr = j * width;
			final int jDiv2 = j >> 1;
			for(i = 0; i < width; i++) {
				Y = fg[pixPtr]; if(Y < 0) Y += 255;
				if((i & 0x1) != 1) {
					final int cOff = sz + jDiv2 * width + (i >> 1) * 2;
					Cb = fg[cOff];
					if(Cb < 0) Cb += 127; else Cb -= 128;
					Cr = fg[cOff + 1];
					if(Cr < 0) Cr += 127; else Cr -= 128;
				}
				int R = Y + Cr + (Cr >> 2) + (Cr >> 3) + (Cr >> 5);
				if(R < 0) R = 0; else if(R > 255) R = 255;
				int G = Y - (Cb >> 2) + (Cb >> 4) + (Cb >> 5) - (Cr >> 1) + (Cr >> 3) + (Cr >> 4) + (Cr >> 5);
				if(G < 0) G = 0; else if(G > 255) G = 255;
				int B = Y + Cb + (Cb >> 1) + (Cb >> 2) + (Cb >> 6);
				if(B < 0) B = 0; else if(B > 255) B = 255;
				out[pixPtr++] = 0xff000000 + (B << 16) + (G << 8) + R;
			}
		}
	}
	
	public static int colorOffset(int a, int b) {
		int R = Math.abs((a & 0x000000ff) - (b & 0x000000ff));
		int G = Math.abs(((a & 0x0000ff00) >> 8) - (b & (0x0000ff00) >> 8));
		int B = Math.abs(((a & 0x00ff0000) >> 16) - ((b & 0x00ff0000) >> 16));
		return R+G+B;
	}
}

