package edu.ntou.hw1;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;


class DrawOnTop extends View {
	boolean mBegin;
	byte[] mYUVData;
	int[] mIntBuffer;
	boolean[] mBooleanBuffer;
	int mImageWidth, mImageHeight;
	int pixels;
	double contrastRatio;
	int histogramMax, histogramMin;
	
	int status = 1;

	
	
	
    public DrawOnTop(Context context) {
        super(context);
        mBegin = false;
    }

    
    
    
    
    @Override
    protected void onDraw(Canvas canvas) {
        if (mBegin)
        {
        	if(status>=1)
        	{
        		convertFromByteToInt();
        		contrastLift();
        		
        		if(status==2)
        		{
        			convertFromIntToBoolean();
            		findObjects();
        		}
        		else if(status==3)
        		{
        			convertFromIntToBoolean();
        			edgeDetection();
        			convertFromBooleanToColor();
        		}
        		else
        		{
        			convertFromIntToColor();
        		}

    			Bitmap bitmap = convertFromColorToBitmap();

        		canvas.drawBitmap(bitmap, 0, 0, null);
        	}
        } // end if statement
        
        super.onDraw(canvas);
        
    }
    
    
    
    
    
    
    private void findObjects() {
    	int[] objects = new int[pixels/4];
    	mIntBuffer = new int[pixels];
    	int objectCount = 0;
    	
    	int[] colorList = {	
    			Color.BLUE,
    			Color.CYAN,
    			Color.GREEN,
    			Color.MAGENTA,
    			Color.RED,
    			Color.YELLOW
    			};
    	
    	// phase 1: marking
    	for (int y=mImageWidth; y<pixels-mImageWidth; y+=mImageWidth)
    	{
    		for (int x=1; x<mImageWidth-1; x++)
    		{
    			int p = x+y;
    			if(mBooleanBuffer[p])
    			{
    				int left = mIntBuffer[p-1];
    				int top = mIntBuffer[p-mImageWidth];
    				
    				while(objects[left] > 0)
    				{
    					left = objects[left];
    				}
    				
    				while(objects[top] > 0)
    				{
    					top = objects[top];
    				}
    				
    				    				
    				if (left > 0)
    				{
    					if (top > 0)
    					{
    						if (left < top)
    						{
    							objects[top] = left;
    							mIntBuffer[p] = left;
    						}
    						if (left > top)
    						{
    							objects[left] = top;
    							mIntBuffer[p] = top;
    						}
    						else
    						{
    							mIntBuffer[p] = left;
    						}
    					}
    					else
    					{
    						mIntBuffer[p] = left;
    					}
    				}
    				else if (top > 0)
    				{
    					mIntBuffer[p] = top;
    				}
    				else
    				{
    					objectCount += 1;
    					mIntBuffer[p] = objectCount;
    				}
    			}
    		}
    	}
    	
    	// phase 2: shorting
    	
    	for (int p = 0; p<pixels; p++)
    	{
    		while (objects[mIntBuffer[p]] > 0)
    		{
    			mIntBuffer[p] = objects[mIntBuffer[p]];
    		}

    		int f = mIntBuffer[p];
    		if (f > 0)
    		{
    			mIntBuffer[p] = colorList[f%colorList.length];
    		}
    		else
    		{
    			mIntBuffer[p] = Color.BLACK;
    		}
    	}
	}





	/* Use Y only, no Cb nor Cr at all. */
	private void convertFromIntToBoolean() {        
		mBooleanBuffer = new boolean[pixels];
        
		for (int offset = 0; offset < pixels; offset++) {
			mBooleanBuffer[offset] = mIntBuffer[offset] < 127;
		}
	}
	
	
	
	
	private Bitmap convertFromColorToBitmap() {
		Bitmap result = Bitmap.createBitmap(mImageWidth, mImageHeight, Bitmap.Config.RGB_565);
		result.setPixels(mIntBuffer,0,mImageWidth,0,0,mImageWidth,mImageHeight);
		return result;
	}
	
	
	
	private void convertFromIntToColor() {
		for (int offset = 0; offset < pixels; offset++) {
			int color = mIntBuffer[offset];
			mIntBuffer[offset] = Color.rgb(color, color, color);
		}
	}
	
	
	
	private void convertFromBooleanToColor() {
		for (int offset = 0; offset < pixels; offset++) {
			if (mBooleanBuffer[offset])
			{
				mIntBuffer[offset] = Color.WHITE;
			}
			else
			{
				mIntBuffer[offset] = Color.BLACK;
			}
		}
	}
	
	
	
	

	private void convertFromByteToInt() {
        pixels = mImageWidth * mImageHeight;
        
		mIntBuffer = new int[pixels];
        
		for (int offset = 0; offset < pixels; offset++) {
			mIntBuffer[offset] = mYUVData[offset] & 0xFF;
		}
	}
	
	
	private void edgeDetection()
	{
		boolean[] edge = new boolean[pixels];

    	for (int y=mImageWidth; y<pixels-mImageWidth; y+=mImageWidth)
    	{
    		for (int x=1; x<mImageWidth-1; x++)
    		{
    			int p = x+y;
    			edge[p] = mBooleanBuffer[p-1] != mBooleanBuffer[p] ||
    			mBooleanBuffer[p-mImageWidth] != mBooleanBuffer[p];
    		}
    	}
    	
    	mBooleanBuffer = edge;
	}
	
	

	private void contrastLift()
	{
		int[] histogram = new int[256];

		for (int offset = 0; offset < pixels; offset++) {
				int color = mIntBuffer[offset];
				histogram[color]++;
				color = (int) ((color-histogramMin)*contrastRatio);
				if(color > 255) color = 255;
				else if(color < 0) color = 0;
				mIntBuffer[offset] = color;
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
		
		contrastRatio = 255.0 / (histogramMax - histogramMin);
	}
}
