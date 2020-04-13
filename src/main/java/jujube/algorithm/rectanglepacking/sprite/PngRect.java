package jujube.algorithm.rectanglepacking.sprite;

/**
 * Created by tb on 2017/6/20.
 */

public class PngRect implements Comparable<PngRect>{


	public String fileName;

	int mWidth;
	int mHeight;
	Point mStartP;
	Point mStopP;

	public PngRect(int mWidth, int mHeight, String fileName) {
		this.mWidth = mWidth;
		this.mHeight = mHeight;
		this.fileName = fileName;
		mStartP = new Point(0, 0);
		mStopP = new Point(mWidth, mHeight);
	}

	public Point getStartP() {
		return mStartP;
	}

	public Point getStopP() {
		return mStopP;
	}

	public void setStartP(Point mStartP) {
		this.mStartP = mStartP;
		this.mStopP = new Point(mStartP.getX() + mWidth, mStartP.getY() + mHeight);
	}

	public void setStopP(Point mStopP) {
		this.mStopP = mStopP;
	}

	public int getWidth() {
		return mWidth;
	}

	public int getHeight() {
		return mHeight;
	}


	@Override
	public int compareTo(PngRect o) {
		return -(this.mHeight - o.getHeight());
	}
}
