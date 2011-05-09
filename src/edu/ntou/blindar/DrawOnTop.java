package edu.ntou.blindar;

import java.util.ArrayList;

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
		Color.rgb(0,0,127),
		Color.rgb(0,0,63),
		Color.rgb(0,0,0),
		Color.rgb(63,0,0),
		Color.rgb(127,0,0),
		Color.rgb(255,0,0)};

		int widthTotal[] = new int[width];
		int heightTotal[] = new int[height];
		
		for(int pos=0;pos<pixels;pos++)
		{

			int offset = CbCrOffset(CbCr[pos], spotColor);
			
			if (pos < width || pos >= pixels-width)
			{
				continue;
			}
			
			if (offset < 100)
			{
				if (offset < 64)
				{
					if (offset < 36)
					{
						detectionMap[pos] = 3;
					}
					else
					{
						detectionMap[pos] = 2;
					}
				}
				else
				{
					detectionMap[pos] = 1;
				}
			}
			else
			{
				detectionMap[pos] = 0;
				continue;
			}
		}
		
		for(int pos=0;pos<pixels;pos++)
		{
			int d = detectionMap[pos]-lastDetectionMap[pos];
			bitmap[pos] = colors[d+3];
			widthTotal[pos%width] += d;
			heightTotal[pos/width] += d;
		}
		
		ArrayList <PeakData> widthPeaks = new ArrayList<PeakData>();
		ArrayList <PeakData> heightPeaks = new ArrayList<PeakData>();
		boolean isIncreasing = false;
		
		for(int i=1;i<width;i++)
		{
			int diff = widthTotal[i] - widthTotal[i-1];
			/*
			if (widthTotal[i] > 0)
			{
				bitmap[i] = Color.rgb(0,(widthTotal[i] << 1),0);
			}
			else
			{
				bitmap[i] = Color.rgb(0,0,-(widthTotal[i] << 1));
			}
			*/
			
			if (diff > 0 && !isIncreasing)
			{
				isIncreasing = true;
			}
			else if (diff < 0 && isIncreasing)
			{
				isIncreasing = false;
				if(widthTotal[i]>80)
				{
					widthPeaks.add(new PeakData(i, widthTotal[i]));
					bitmap[i] = Color.rgb(0,255,0);
				}
			}
			
		}
		
		for(int i=1;i<height;i++)
		{
			int diff = heightTotal[i] - heightTotal[i-1];
			/*
			if (heightTotal[i] > 0)
			{
				bitmap[i*width] = Color.rgb(0,(heightTotal[i] << 1),0);
			}
			else
			{
				bitmap[i*width] = Color.rgb(0,0,-(heightTotal[i] << 1));
			}
			*/
			
			if (diff > 0 && !isIncreasing)
			{
				isIncreasing = true;
			}
			else if (diff < 0 && isIncreasing)
			{
				isIncreasing = false;
				if(heightTotal[i]>80)
				{
					heightPeaks.add(new PeakData(i, heightTotal[i]));
					bitmap[i*width] = Color.rgb(0,255,0);
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
		int cb = a[0]-b[0];
		int cr = a[1]-b[1];
		return cb*cb + cr*cr;
	}

}

