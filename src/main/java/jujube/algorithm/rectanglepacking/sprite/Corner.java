package jujube.algorithm.rectanglepacking.sprite;

/**
 * Created by tb on 2017/6/20.
 */

public class Corner implements Comparable<Corner>{

	Point p;
	Rect r;

	public Corner(Point p, Rect r) {
		this.p = p;
		this.r = r;
	}

	public Point getPoint() {
		return p;
	}

	public Rect getRect() {
		return r;
	}

	@Override
	public int compareTo(Corner o) {

		return (this.p.getX() - o.getPoint().getX());
	}
}
