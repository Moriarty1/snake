package dima.android.snake;

public class WallCoordinate extends Coordinate {
	private int version;
	public int getVersion(){
		return version;
	}
	public WallCoordinate(int newX, int newY, int newVersion) {
    	super(newX, newY);
    	version = newVersion;
    	
    }
}
