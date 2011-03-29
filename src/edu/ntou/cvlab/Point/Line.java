package edu.ntou.cvlab.Point;

public class Line {
	/*
	 * y=ax+b a=�ײv
	 */
	private Point firstPoint;
	private Point lastPoint;
	
	private boolean hasCachedABC = false;
	private double a;
	private double b;
	private double c;
	private double absqrt;
	
	private boolean hasCachedLength = false;
	private double length;
	
	private boolean hasCachedCenter = false;
	private Point center;
	
	private boolean hasCachedSlope = false;
	private double slope;

	public Line() {
		firstPoint.x = -1;
		lastPoint.x = -1;
		firstPoint.y = -1;
		lastPoint.y = -1;
	}

	public Line(Line X) {
		this.firstPoint = new Point(X.getFirstPoint());
		this.lastPoint = new Point(X.getLastPoint());
	}

	public Line(Point F, Point L) {
		firstPoint = new Point(F);
		lastPoint = new Point(L);
	}

	public void setFirstPoint(Point X) {
		firstPoint = new Point(X);
		this.clearCache();
	}

	public void setLastPoint(Point X) {
		lastPoint = new Point(X);
		this.clearCache();
	}

	public Point getFirstPoint() {
		return firstPoint;
	}

	public Point getLastPoint() {
		return lastPoint;
	}
	
	private void clearCache() {
		hasCachedABC = hasCachedLength = hasCachedCenter = hasCachedSlope = false;
	}
	
	public double getSlope() {
		if(!hasCachedSlope)
		{
			slope = ((double) (lastPoint.y - firstPoint.y))	/ ((double) (lastPoint.x - firstPoint.x));
			hasCachedSlope = true;
		}
		return slope;
	}

	public double getDistance(Point p) {
		// ax+by+c = 0:
		//  (y1 – y2)x + (x2 – x1)y + (x1y2 – x2y1) = 0
		// Math.abs(ax+by+c) / Math.sqrt(a*a+b*b)
		if(!hasCachedABC)
		{
			a = firstPoint.y - lastPoint.y;
			b = lastPoint.x - firstPoint.x;
			c = firstPoint.x * lastPoint.y - lastPoint.x * firstPoint.y;
			absqrt = Math.sqrt(a * a + b * b);
			hasCachedABC = true;
		}
		
		return Math.abs(a * p.x + b * p.y + c) / absqrt;
	}
	
	public Point getCenter()
	{
		if(!hasCachedCenter)
		{
			center = new Point((lastPoint.x+firstPoint.x) / 2, (lastPoint.y+firstPoint.y) / 2, 0);
			hasCachedCenter = true;
		}
		
		return new Point(center);
	}
	
	public double getLength() {
		if(!hasCachedLength)
		{
			double x = lastPoint.x - firstPoint.x;
			double y = lastPoint.y - firstPoint.y;
			length = Math.sqrt(x*x + y*y);
			hasCachedLength = true;
		}
		
		return length;
	}
}