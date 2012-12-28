package dima.android.snake;

public class Coordinate {
    private int x;
    private int y;
   
    public int getX(){
    	return x;
    }
    public int getY(){
    	return y;
    }
    public Coordinate(int newX, int newY) {
        x = newX;
        y = newY;
    }
	public boolean equals(Coordinate other) {
        if (x == other.x && y == other.y) {
            return true;
        }
        return false;
    }
}
