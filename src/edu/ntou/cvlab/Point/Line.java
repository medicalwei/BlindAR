package edu.ntou.cvlab.Point;

public class Line {
	/*
	 * y=ax+b a=�ײv
	 */
	private Point firstPoint;
	private Point lastPoint;

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
	}

	public void setLastPoint(Point X) {
		lastPoint = new Point(X);
	}

	public double getSlope() {
		double m;// slope
		m = ((double) (lastPoint.y - firstPoint.y))
				/ ((double) (lastPoint.x - firstPoint.x));
		return m;
	}

	public boolean isExtension(Point target, double threshold) {
		
		double a = getSlope();
		double b1 = firstPoint.y - a*firstPoint.x;
		double b2 = target.y - a*target.x;
		
		return (Math.abs(b2 - b1) <= threshold);
	}
	
	public double getB() {
		double b;
		double m = this.getSlope();
		b = firstPoint.y - (m * firstPoint.x);
		return b;
	}

	public Point getFirstPoint() {
		return firstPoint;
	}

	public Point getLastPoint() {
		return lastPoint;
	}

	public double getDistance(int x, int y) {
		return Math.abs(this.getSlope() * x - y + this.getB())
				/ (double) (Math.pow(Math.pow(this.getSlope(), 2) + 1, 0.5));
	}
	
	public double getLength() {
		double x = lastPoint.x - firstPoint.x;
		double y = lastPoint.y - firstPoint.y;
		return Math.sqrt(x*x + y*y);
	}
}