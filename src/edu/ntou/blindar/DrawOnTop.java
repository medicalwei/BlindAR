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
	
	byte lastDetectionMap[];
	short skinColor[] = {142, 120};

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
        	
        	int hand[] = handDetect(mYUVData, mImageWidth, mImageHeight);
        	
        	Bitmap result = Bitmap.createBitmap(
        			mImageWidth/2, mImageHeight/2, Bitmap.Config.RGB_565);
        	
        	result.setPixels(
        			hand,0,mImageWidth/2,0,0,mImageWidth/2,mImageHeight/2);
        	
        	canvas.drawBitmap(result, 0, 0, null);
        } // end if statement
        
        super.onDraw(canvas);
    }
   
	
	class PeakData {
		int position;
		int value;
		
		PeakData(int i_position, int i_value)
		{
			position = i_position;
			value = i_value;
		}
	}
	
    /* Use Y only, no Cb nor Cr at all. */
    
	public int[] handDetect(byte fg[], int width, int height)
	{
		width = width / 2;
		height = height / 2;
		
		int pixels = height * width;
		short CbCr[][] = new short[pixels][2];
		int bitmap[] = new int[pixels];
		byte detectionMap[] = new byte[pixels];
		
		if (lastDetectionMap == null)
		{
			lastDetectionMap = new byte[pixels];
		}
		
		decodeYUVtoCbCr(CbCr, fg, pixels);
		
		int centerPos = (width + pixels) / 2;
		
		short spotColor[] = CbCr[centerPos];
		
		
		if (CbCrOffset(spotColor, skinColor) > 100)
		{
			lastDetectionMap = detectionMap;
			return bitmap; /* blank */
		}
		
		skinColor[0] = spotColor[0];
		skinColor[1] = spotColor[1];
		
		
		int colors[]=
		{Color.rgb(0,0,255),
		Color.rgb(0,0,0),
		Color.rgb(255,0,0)};
		
		for(int pos=0;pos<pixels;pos++)
		{

			int offset = CbCrOffset(CbCr[pos], spotColor);
			
			if (pos < width || pos >= pixels-width)
			{
				continue;
			}
			
			if (offset < 100)
			{
				detectionMap[pos] = 1;
			}
			else
			{
				detectionMap[pos] = 0;
				continue;
			}
		}
		
		int heightTotal[] = new int[height];
		int heightThreshold = 0;
		
		for(int pos=0;pos<pixels;pos++)
		{
			int d = detectionMap[pos]-lastDetectionMap[pos];
			bitmap[pos] = colors[d+1];
			heightTotal[pos/width] += d;
			heightThreshold += d;
		}
		
		heightThreshold = (int) ((heightThreshold / height) + 20);
		
		for(int i=1;i<height;i++)
		{
			int h = heightTotal[i] >> 2;
			int a = i*width;
			
			if (h > 0)
			{
				if (h >= (heightThreshold >> 2))
				{
					for (int f=0; f<=h; f++)
					{
						bitmap[a + f] = Color.GREEN;
					}
				}
				else
				{
					for (int f=0; f<=h; f++)
					{
						bitmap[a + f] = Color.WHITE;
					}
				}
			}
		}
		
		
		
		lastDetectionMap = detectionMap;
		
		return bitmap;
	}

	public static void decodeYUVtoCbCr(short[][] out, byte[] fg, int sz) {
		final int uvStart = sz << 2;
		int uvPoint = uvStart;
		
		for(int i = 0; i < sz; i++) {
			out[i][0] = (short) (fg[uvPoint] & 0xFF);
			out[i][1] = (short) (fg[uvPoint+1] & 0xFF);
			uvPoint += 2;
		}
	}
	
	public static int CbCrOffset(short a[], short b[]) {
		int cr = a[0]-b[0];
		int cb = a[1]-b[1];
		return cb*cb + cr*cr;
	}

}

