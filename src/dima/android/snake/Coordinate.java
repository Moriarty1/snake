package dima.android.snake;

public class Coordinate {
    private int x;
    private int y;
    public Coordinate(int newX, int newY) {
        x = newX;
        y = newY;
    }
    public Coordinate(Coordinate coordinate) {
        x = coordinate.x;
        y = coordinate.y;
    }
    
    public int getX(){
    	return x;
    }
    public int getY(){
    	return y;
    }
    public void reverse(){
    	x ^= y;
		y ^= x;
		x ^= y;
    }
//    public void setX(int x){
//    	this.x=x;
//    }
//    public void setY(int y){
//    	this.y=y ;
//    }
//    public void setXY(int x, int y){
//    	this.x=x ;
//    	this.y=y ;
//    }
    
	public boolean equals(Coordinate other) {
        if (x == other.x && y == other.y) {
            return true;
        }
        return false;
    }
} 