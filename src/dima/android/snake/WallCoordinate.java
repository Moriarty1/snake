package dima.android.snake;

public class WallCoordinate extends Coordinate {
	private int version;
	public WallCoordinate(int newX, int newY, int newVersion) {
    	super(newX, newY);
    	version = newVersion;
    }
	public int getVersion(){
		return version;
	}
//	public void setVersion(int version){
//    	this.version=version;
//    }
} 