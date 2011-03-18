package edu.ntou.cvlab.Point;


public class Point
{
	public int x, y, width;
	private boolean isAssigned;

	public Point()
	{
		isAssigned = false;
	}

	public Point(Point target)
	{
		set(target.x, target.y, target.width);
		isAssigned = target.isAssigned;
	}

	public Point(int i_x, int i_y, int i_width)
	{
		set(i_x, i_y, i_width);
	}
	
	public void set(int i_x, int i_y, int i_width)
	{
		x = i_x;
		y = i_y;
		width = i_width;
		isAssigned = true;
	}

	public int getX()
	{
		return x;
	}

	public int getY()
	{
		return y;
	}

	public Boolean isAssigned()
	{
		return isAssigned;
	}
}