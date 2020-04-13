package jujube.algorithm.rectanglepacking.sprite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by tb on 2017/6/20.
 */

public class PackRect {

	List<PngRect> result = new ArrayList<>();

	public List<PngRect> getResult() {
		return result;
	}

	public int getWidth() {
		return rw;
	}

	public int getHeight() {
		return rh;
	}

	int rw;
	int rh;

	List<PngRect> pngs;
	List<Corner> corners = new ArrayList<>();

	List<Rect> as = new ArrayList<>();

	int time = 0;

	int aw;
	int ah;

	int mWidth;
	int mHeight;

	int BArea = Integer.MAX_VALUE;

	public PackRect(List<PngRect> rs) {
		Collections.sort(rs);
		ah = rs.get(0).getHeight();
		aw = 0;
		int area = 0;
		for (PngRect pngRect : rs) {
			area = area + pngRect.getHeight() * pngRect.getWidth();
			if (pngRect.getWidth() > aw) {
				aw = pngRect.getWidth();
			}
		}

		mHeight = rs.get(0).getHeight();
		for (PngRect pngRect : rs) {
			mWidth += pngRect.getWidth();
		}
		putPng(rs);
	}

	public void putPng(List<PngRect> rs) {
		time ++;
		Collections.sort(rs);
		for (PngRect png : rs) {
			putIn(png);
		}


		int Xmax = 0,  Ymax = 0;
		for (PngRect lpng : pngs) {
			if (lpng.getStopP().getX() > Xmax)
				Xmax = lpng.getStopP().getX();
			if (lpng.getStopP().getY() > Ymax)
				Ymax = lpng.getStopP().getY();
		}
		int area = Xmax * Ymax;
		if (area < BArea) {
			BArea = area;
			rw = Xmax;
			rh = Ymax;
			result.clear();
			for (PngRect pr : pngs) {
				PngRect copy = new PngRect(pr.getWidth(), pr.getHeight(), pr.fileName);
				copy.setStartP(pr.getStartP());
				result.add(copy);
			}
		}


		mWidth = mWidth - aw;
		mHeight = mHeight + ah;
		pngs = null;
		corners.clear();
		if (mWidth >= aw)
			putPng(rs);


	}

	private boolean putIn(PngRect png){
		if(pngs == null){
			pngs = new ArrayList<PngRect>();
			pngs.add(png);
			png.setStartP(new Point(0,0));
			if(png.getHeight() < mHeight){
				Rect owner = new Rect(0,png.getStopP().getY(),mWidth,mHeight);
				Corner corner = new Corner(new Point(0,png.getHeight()),owner);
				corners.add(corner);
			}
			if(png.getWidth() < mWidth){
				Rect owner = new Rect(png.getStopP().getX(),0,mWidth,mHeight);
				Corner corner = new Corner(new Point(png.getWidth(),0),owner);
				corners.add(corner);
			}
			return true;
		}else{
			Corner corner;
			int x;
			int y;
			Rect owner;
			for(int i = 0; i < corners.size(); ++i){
				corner = corners.get(i);
				x = corner.getPoint().getX();
				y = corner.getPoint().getY();
				owner = corner.getRect();
				Point startP = corner.getPoint();
				int endX = owner.right;
				int endY = owner.bottom;
                /*
                 * 可以放进该corner，此处存在覆盖问题，需要判断四条边是否有点在已经被使用，如果是，则返回，试下一个corner
                 * v0-----v1
                 * |       |
                 * |   a   |
                 * |       |
                 * v2-----v3
                 * 同时为了防止完全重叠，还需要判断额外一个点,例如图中的a点
                 */
				if(x + png.getWidth() <= mWidth && y + png.getHeight() <= mHeight){
					Point v0 = startP;
					Point v1 = new Point(startP.getX() + png.getWidth(),startP.getY());
					Point v2 = new Point(startP.getX(),startP.getY() + png.getHeight());
					Point v3 = new Point(startP.getX() + png.getWidth(),startP.getY() + png.getHeight());
					Point a = new Point(startP.getX() + png.getWidth()/2,startP.getY() + png.getHeight()/2);
//					if(contains(v0,v1,true) || contains(v1,v3,false) || contains(v2,v3,true) || contains(v0,v2,false) || contains(a)){//有可能正好落在边上
//						continue;
//					}
//                  if(contains(a) ||contains(v0) || contains(v1) || contains(v2) || contains(v3) ){//有可能正好落在边上
//                      continue;
//                  }

					if (contains(v0, v1, a, top) || contains(v1, v3, a, right) || contains(v2, v3, a, bottom) || contains(v0, v2, a, left) || contains(v0, v1, a, center)) {
						continue;
					}
					if(x + png.getWidth() <= endX){
						corners.add(i + 1,new Corner(new Point(x + png.getWidth(),y),
								new Rect(x + png.getWidth(),y,mWidth,mHeight)));//此处owner可能为空
					}

					if(y + png.getHeight() <= endY){
						corners.add(i + 1, new Corner(new Point(x,y + png.getHeight()),
								new Rect(x,y + png.getHeight(),mWidth,mHeight)));
					}
					corners.remove(i);
					sortCorners();
					png.setStartP(corner.getPoint());
					pngs.add(png);
					return true;
				}
			}
		}
		return false;
	}

	private void sortCorners() {
		Collections.sort(corners);
	}

	private boolean contains(Point v1, Point v2, Point vC, int mode) {
		boolean b = false;
		for (PngRect pngRect : pngs) {
			Rect rect = new Rect(pngRect.getStartP().getX(), pngRect.getStartP().getY(),
					pngRect.getStartP().getX() + pngRect.getWidth(), pngRect.getStartP().getY() + pngRect.getHeight());
			if (!b) {
				b = contains(v1, v2, vC, rect, mode);
			}
			if (b)
				return true;
		}
		return b;
	}

	static int left = 0;
	static int top = 1;
	static int right = 2;
	static int bottom = 3;
	static int center = 4;



	private boolean contains(Point v0, Point v1, Point vC, Rect rect, int mode) {
		int y0 = v0.getY();
		int y1 = v1.getY();
		int x0 = v0.getX();
		int x1 = v1.getX();

		boolean b = false;

		switch (mode) {
			case 1:
				if (y0 >= rect.bottom) {
					b =  false;
					break;
				}

				if (y0 >= rect.top && y0 <= rect.bottom) {
					if (x1 <= rect.left || x0 >= rect.right) {
						b =  false;
					}else {
						b =  true;
					}
				}else {
					b =  false;
				}
				break;
			case 3:
				if (y0 <= rect.top) {
					b =  false;
					break;
				}
				if (y0 >= rect.top && y0 <= rect.bottom) {
					if (x1 <= rect.left || x0 >= rect.right) {
						b = false;
					}else {
						b = true;
					}
				}else {
					b = false;
				}
				break;
			case 0:
				if (x0 >= rect.right) {
					b =  false;
					break;
				}
				if (x0 >= rect.left && x0 <= rect.right) {
					if (y1 <= rect.top || y0 >= rect.bottom) {
						b = false;
					}else {
						b = true;
					}
				}else {
					b = false;
				}
				break;
			case 2:
				if (x0 <= rect.left) {
					b =  false;
					break;
				}
				if (x0 >= rect.left && x0 <= rect.right) {
					if (y1 <= rect.top || y0 >= rect.bottom) {
						b = false;
					}else {
						b = true;
					}
				}else {
					b = false;
				}
				break;
			case 4:
				if (vC.getX() == ((rect.left + rect.right) / 2) && vC.getY() == ((rect.top + rect.bottom) / 2)) {
					b = true;
				}else {
					b = false;
				}
				break;
		}
		return b;

	}
}
