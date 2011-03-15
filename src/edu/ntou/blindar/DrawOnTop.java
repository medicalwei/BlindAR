package edu.ntou.blindar;

import java.util.ArrayList;
import edu.ntou.cvlab.Point.Line;
import edu.ntou.cvlab.Point.Point;

import android.content.Context;
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
		
		Point center = new Point();
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
					center.x = (x + lineDetectionBegin) / 2; // 取中點
					center.y = y;
					center.width=x-lineDetectionBegin;
					
					/* 抓直線 */
					if (L.isEmpty()) {// 沒有線的情況下
						if (P.isEmpty()) {// 沒有線 也沒有點
							P.add(new Point(center));
						} else {
							Point test;// 沒有線 有點
							int Long, small = -1, S2 = 0;
							for (int temp = 0; temp < P.size(); temp++) {
								
								test = (Point) P.get(temp);
								if(center.width!=test.width){
									continue;
								}
								Long = (center.x - test.x)
										* (center.x - test.x)
										+ (center.y - test.y)
										* (center.y - test.y);
								if (Long < small || small == -1) {
									small = Long;
									S2 = temp;
								}
							}
							if (small < 20) {
								test = (Point) P.get(S2);
								Line newLine = new Line(test, center);
								L.add(new Line(newLine));
								P.remove(S2);
							} else {
								P.add(new Point(center));
							}
						}
					} else {// 有線
						Line tempLine;
						int temp;
						for (temp = 0; temp < L.size(); temp++) {
							tempLine = (Line) L.get(temp);
							if (tempLine.getDistance(center.x, center.y) < 5) {// 再和線距離小於1的情況下
								tempLine.setLastPoint(center);
								break;
							}
						}
						if (temp >= L.size()) {// 和現有的線太遠
							if (P.isEmpty()) {
								P.add(new Point(center));
							} else {
								Point test;// 沒有線 有點
								int Long, small = -1, S2 = 0;
								for (temp = 0; temp < P.size(); temp++) {
									test = (Point) P.get(temp);
									Long = (center.x - test.x)
											* (center.x - test.x)
											+ (center.y - test.y)
											* (center.y - test.y);
									if (Long < small || small == -1) {
										small = Long;
										S2 = temp;
									}
								}
								if (small < 30) {
									test = (Point) P.get(S2);
									Line newLine = new Line(test, center);
									L.add(new Line(newLine));
									P.remove(S2);
								} else {
									P.add(new Point(center));
								}
							}
						}
					}
				}
				
				/* ask for next state */
				state = nextState[condition][state];
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
