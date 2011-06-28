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
	    
	public int[] handDetect(byte fg[], int width, int height)
	{
		width = width / 2;
		height = height / 2;
		
		int pixels = height * width;
		short CbCr[][] = new short[pixels][2];
		int bitmap[] = new int[pixels];
		int detectionMapIntegralImage[][] = new int[height][width];
		boolean detectionMap[][] = new boolean[height][width];
		
		// Decode YUV to CbCr
		decodeYUVtoCbCr(CbCr, fg, pixels);
		
		// get a skin color integral image
		for(int y=1;y<height-1;y++)
		{
			for(int x=1;x<width-1;x++)
			{
				detectionMapIntegralImage[y][x] = 
					detectionMapIntegralImage[y-1][x] +
					detectionMapIntegralImage[y][x-1] -
					detectionMapIntegralImage[y-1][x-1] +
					(isSkinColor(CbCr[y*width+x])?1:0);
			}
		}
		
		// filtering
		for(int y=3;y<height-3;y++)
		{
			for(int x=3;x<width-3;x++)
			{
				int regionCount = 
					detectionMapIntegralImage[y+3][x+3] -
					detectionMapIntegralImage[y+3][x-3] -
					detectionMapIntegralImage[y-3][x+3] +
					detectionMapIntegralImage[y-3][x-3];
				
				detectionMap[y][x] = (regionCount > 20);
			}
		}		
		

		
		for(int y=3;y<height-3;y++)
		{
			for(int x=3;x<width-3;x++)
			{
				bitmap[y*width+x] = detectionMap[y][x]?Color.RED:Color.BLACK;
			} 
		}
		
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
	
	public static boolean isSkinColor(short a[]) {
		return (a[0] >= 133 && a[0] <= 173 && a[1] >= 77 && a[1] <= 127);
	}

}

