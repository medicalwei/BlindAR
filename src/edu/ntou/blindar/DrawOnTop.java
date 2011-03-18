package edu.ntou.blindar;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import edu.ntou.cvlab.Point.Line;
import edu.ntou.cvlab.Point.Point;

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
        	/*
			int bytes = mImageWidth * mImageHeight;
        	int[] resultBuffer = new int[bytes];
        	
			for (int offset = 0; offset < bytes; offset++) {
					int color = mYUVData[offset] & 0xFF;
					resultBuffer[offset] = Color.rgb(color, color, color);
			}
				
			canvas.drawBitmap(resultBuffer, 0, mImageWidth, 0, 0,
					mImageWidth, mImageHeight, false, null);
			*/
			Paint paintRed = new Paint();
			paintRed.setColor(Color.RED);
			paintRed.setStrokeWidth(2);
			
			Paint paintGreen = new Paint();
			paintGreen.setColor(Color.GREEN);
			
			Paint paintBlue = new Paint();
			paintBlue.setColor(Color.BLUE);
			
			LinesAndPoints f = doRoadlineDetection(mYUVData, mImageWidth, mImageHeight);
			
			for(Line line:f.lines)
			{
				canvas.drawLine(line.getFirstPoint().x,
								line.getFirstPoint().y,
								line.getLastPoint().x,
								line.getLastPoint().y,
								paintRed);
			}
			
			for(Point point:f.points)
			{
				canvas.drawPoint(point.x, point.y, paintGreen);
			}
			
			canvas.drawText("Points: " + f.points.size() + ", Lines: " + f.lines.size(), 10, 10, paintBlue);
			
			invalidate();
        }
        
        super.onDraw(canvas);
    }

	public LinesAndPoints doRoadlineDetection(byte[] src, int width, int height) 
	{
		/* State machine routine
		 *
		 * Used to detect the pattern like this:
		 * | = | + | + | = | = | = | - | - | = |
		 */
		
		byte state = 0, condition;
		int lineDetectionBegin = 0;
		
		boolean[][] centerPointArray = new boolean[width][height];
		
		ArrayList<Point> P = new ArrayList<Point>();
		ArrayList<Line> L = new ArrayList<Line>();
		
		/* nextState[condition][state] */
		final byte nextState[][] = 
		{
				{0, 0, 0, 4, 4, 0},
				{1, 1, 3, 3, 5, 0},
				{0, 2, 2, 0, 0, 0}
		};
		
		for (int y = 1; y < height - 1; y++) {
			for (int x = 1; x < width - 1; x++) {
				
				int difference = ((int) (src[y*width + x + 1] & 0xFF) - (int) (src[y*width + x] & 0xFF));
				
				/* condition: 0: -, 1: =, 2: + */
				if (difference > 50) {
					condition = 2;
				}
				else if (difference < -50) {
					condition = 0;
				}
				else {
					condition = 1;
				}
				
				/* Line detection begin on the first + */
				if (condition == 2 && state == 1) {
					lineDetectionBegin = x; 
				}
				
				/* on state 5 */
				else if (state == 5 && ((x - lineDetectionBegin) < (width / 20)))
				{
					Point point = new Point();
					point.x = (x + lineDetectionBegin) / 2; // 取中點
					point.y = y;
					point.width=x-lineDetectionBegin;
					P.add(point);
					centerPointArray[point.x][point.y]=true;
				}
				
				/* ask for next state */
				state = nextState[condition][state];
			}
		}
		
		
		
		
		
		
		
		for(Point point: P)
		{
			Point target = new Point();
			
			if (point.x >= width-2 || point.y >= height-2)
			{
				continue;
			}
			
			

			/* search for forward point */
			if (centerPointArray[point.x][point.y+2])
			{
				target.set(point.x, point.y+2, point.width);
			}
			else if (centerPointArray[point.x+2][point.y])
			{
				target.set(point.x+2, point.y, point.width);
			}
			else if (centerPointArray[point.x+2][point.y+2])
			{
				target.set(point.x+2, point.y+2, point.width);
			}
			else if (centerPointArray[point.x-2][point.y+2])
			{
				target.set(point.x-2, point.y+2, point.width);
			}
			else if (centerPointArray[point.x-2][point.y+1])
			{
				target.set(point.x-2, point.y+1, point.width);
			}
			else if (centerPointArray[point.x+2][point.y+1])
			{
				target.set(point.x+2, point.y+1, point.width);
			}
			else if (centerPointArray[point.x+1][point.y+2])
			{
				target.set(point.x+1, point.y+2, point.width);
			}
			else if (centerPointArray[point.x-1][point.y+2])
			{
				target.set(point.x-1, point.y+2, point.width);
			}
			else
			{
				continue;
			}

			
			Line line = new Line(point, target);
			
			/* seek if there is a line already there */
			
			boolean lineHasFound = false;
			for (Line line2: L) {
				Point lineLastPoint = line2.getLastPoint();
				if (	(lineLastPoint.y < target.y)
						&& line2.isExtension(line, 3))
				{
					line2.setLastPoint(target);
					lineHasFound = true;
					break;
				}
			}
			
			if (lineHasFound)
			{
				continue;
			}
			else
			{
				/* else create a new line */
				L.add(line);
			}
			
		}
		
		
		
		
		
		LinesAndPoints f = new LinesAndPoints();
		f.lines = L;
		f.points = P;
		
		return f;
	}
}

class LinesAndPoints{
	public ArrayList<Line> lines;
	public ArrayList<Point> points;
}

