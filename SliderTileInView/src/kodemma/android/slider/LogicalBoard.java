package kodemma.android.slider;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import android.graphics.Point;
import android.util.Log;

enum Direction {
	UP, DOWN, RIGHT, LEFT, NONE;
	boolean virtical() { return this == UP || this == DOWN; }
	boolean horizontal() { return this == RIGHT || this == LEFT; }
}
class LogicalTile {
	int serial;	// start with zero
	Point lp;	// logical position
	LogicalTile (int s, Point p) { serial = s; lp = p; }
	@Override public String toString() {
		return "serial=" + serial + ", lp.x=" + lp.x + ", lp.y=" + lp.y;
	}
}
public class LogicalBoard {
	private static final String TAG = LogicalBoard.class.getSimpleName();
	private static final float DISTANCE_FACTOR	= 2.0F;
	private static final float SHUFFLE_FACTOR	= 2.0F;
	int rows;
	int cols;
	LogicalTile[][] tiles;
	LogicalTile hole;
	List<Point> footprints = new ArrayList<Point>();
	int distance = 0;
	private Random random = new Random();
	
	LogicalBoard(int r, int c) {
		rows = r; cols = c;
		tiles =  new LogicalTile[rows][cols];
		initializeTiles(tiles);
	}
	boolean initialized(int r, int c) {
		return false;
	}
	private void initializeTiles(LogicalTile[][] ts) {
		footprints.clear();
		int holeSerial = random.nextInt(rows*cols);
		for (int i=0, serial=0; i<rows; i++) {
			for (int j=0; j<cols; j++) {
				LogicalTile tile = new LogicalTile(serial, new Point(j, i));
				if (serial == holeSerial) {
					hole = tile;
					footprints.add(hole.lp);
				}
				ts[i][j] = tile;
				serial++;
			}
		}
	}
	private Point initialPosition(int serial) { return new Point(serial%cols, serial/cols); }
	private int distance(LogicalTile tile) {
		Point ip = initialPosition(tile.serial);
		return Math.abs(tile.lp.x - ip.x) + Math.abs(tile.lp.y - ip.y);
	}
	private LogicalTile slideAtRandom(LogicalTile previous) {
		LogicalTile[] nominees = new LogicalTile[4];
		int counter = 0;
		Point h = hole.lp;
		// 移動させるタイルの候補を選定
		if (h.y > 0) {		// upper tile
			nominees[counter] = tiles[h.y-1][h.x];
			if (nominees[counter] != previous) counter++;
		}
		if (h.x < cols-1) {	// right tile
			nominees[counter] = tiles[h.y][h.x+1];
			if (nominees[counter] != previous) counter++;
		}
		if (h.y < rows-1) {	// lower tile
			nominees[counter] = tiles[h.y+1][h.x];
			if (nominees[counter] != previous) counter++;
		}
		if (h.x > 0) {		// left tile
			nominees[counter] = tiles[h.y][h.x-1];
			if (nominees[counter] != previous) counter++;
		}
		// 移動させるタイルを決定
		LogicalTile target = nominees[random.nextInt(counter)];
		slide(target);	// タイルをスライドする
		return target;
	}
	LogicalTile slide(LogicalTile target) {
		distance -= distance(target);	// 現状の離散度を減算
		Point h = hole.lp;
		// タイルを移動する
		Point t = target.lp;
		LogicalTile tmp = tiles[t.y][t.x];
		tiles[t.y][t.x] = tiles[h.y][h.x];
		tiles[h.y][h.x] = tmp;
		// 論理位置を付け替える
		target.lp = hole.lp;
		hole.lp = t;
		footprints.add(t);				// 棋譜に追加
		distance += distance(target);	// 新しい離散度を加算
Log.d(TAG, "- distance=" + distance);
		return target;
	}
	int shuffle() {
		int total = (int)(rows * cols * DISTANCE_FACTOR);
		int maxSlide = (int)(total * SHUFFLE_FACTOR);
maxSlide = 2;
		return shuffle(total, maxSlide);
	}
	private int shuffle(int totalDistance, int maxSlide) {
//		Log.d(TAG, "totalDistance=" + totalDistance + ", maxSlide=" + maxSlide);
		if (distance != 0) initializeTiles(tiles);
		LogicalTile previous = null;
		int counter = 0;
		for (; counter<maxSlide; counter++) {
			previous = slideAtRandom(previous);
			if (distance >= totalDistance) break;
			//print(); // for debug
		}
		return counter;
	}
	// 最後に移動したタイルを取得
	LogicalTile getUndoTile() {
		int size = footprints.size();
		if (size < 2) return null;
		Point p = footprints.get(size - 2);
		return tiles[p.y][p.x];
	}
	// 最後に移動したタイルをアンドゥする際の方向を取得する
	Direction getUndoDirection() {
		Point t = getUndoTile().lp;
		if (t == null) return Direction.NONE;
		return getDirection(hole.lp, t);
	}
	// タイルから穴への方向を取得する。 
	Direction getDirection(Point holePos, Point tilePos) {
		return (holePos.x == tilePos.x)?
				(holePos.y < tilePos.y)? Direction.UP : Direction.DOWN :
				(holePos.x < tilePos.x)? Direction.LEFT : Direction.RIGHT;
	}
	Direction getDirection(LogicalTile tile) {
		Point h = hole.lp; Point t = tile.lp;
		if (h.x != t.x && h.y != t.y) return Direction.NONE;
		return getDirection(h, t);
	}
	List<LogicalTile> getMovables(LogicalTile tile) {
		Direction d = getDirection(tile);
		if (d == Direction.NONE) return null; 
		List<LogicalTile> movables = new ArrayList<LogicalTile>();
		int s; Point h = hole.lp; Point t = tile.lp;
		if (d.virtical()) {
			s = (d == Direction.UP)? -1 : 1;
			for (int i=t.y; i!=h.y; i+=s) movables.add(0, tiles[i][h.x]);
		} else {
			s = (d == Direction.LEFT)? -1 : 1;
			for (int i=t.x; i!=h.x; i+=s) movables.add(0, tiles[h.y][i]);
		}
		return movables;
	}
	void print() {
		Log.d(TAG, "- footprints=" + footprints.size() + ", distance=" + distance);
		for (int i=0; i<rows; i++) {
			StringBuffer buffer = new StringBuffer();
			for (int j=0; j<cols; j++) {
				LogicalTile tile = tiles[i][j];
				if (tile == hole)
					buffer.append("XX");
				else
					buffer.append(String.format("%02d", tile.serial));
				if (j < cols - 1) buffer.append("-");
			}
			Log.d(TAG, buffer.toString());
		}
	}
}