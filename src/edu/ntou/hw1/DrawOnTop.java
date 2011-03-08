package edu.ntou.hw1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;


class DrawOnTop extends View {
	boolean mBegin;
	byte[] mYUVData;
	int mImageWidth, mImageHeight;
	boolean mDrawEdges=true;
	
	short histogramMax, histogramMin;
	double ratio;
	
    public DrawOnTop(Context context) {
        super(context);
        mBegin = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBegin)
        {
        	if(mDrawEdges)
        	{
        		canvas.drawBitmap(edgeDetect(mYUVData, mImageWidth, mImageHeight), 0, 0, null);
        	}
        } // end if statement
        
        super.onDraw(canvas);
        
    }
    
    /* Use Y only, no Cb nor Cr at all. */
    
	public Bitmap edgeDetect(byte fg[], int width, int height)
	{
		Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
		int[] resultBuffer = new int[height*width];
		int[] histogram = new int[256];

		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				int offset = y*width + x;
				int color = fg[offset] & 0xFF;
				histogram[color]++;
				color = (int) ((color-histogramMin)*ratio);
				if(color > 255) color = 255;
				else if(color < 0) color = 0;
				resultBuffer[offset] = Color.rgb(color, color, color);
			}
		}
		
		for (histogramMax = 255; histogramMax>=0; histogramMax--)
		{
			if(histogram[histogramMax]>0)
			{
				break;
			}
		}
		
		for (histogramMin = 0; histogramMin<=255; histogramMin++)
		{
			if(histogram[histogramMin]>0)
			{
				break;
			}
		}
		
		ratio = 255.0 / (histogramMax - histogramMin);
		
		result.setPixels(resultBuffer,0,width,0,0,width,height);
		
		return result;
	}
}
