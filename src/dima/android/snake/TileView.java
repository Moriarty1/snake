
package dima.android.snake;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class TileView extends View {
	private static final Coordinate NOT_LONG_SIZE = new Coordinate(18,24);
	private static final Coordinate LONG_SIZE = new Coordinate(18,30);
	
	protected static int mTileSize;
	protected static Coordinate mAbsoluteTileCount;
	private static Coordinate mTileCount;
	private static Coordinate mOffset;

    private Bitmap[] mTileArray;
    private static int[][] mTileGrid;
    protected static Paint mPaint = new Paint();
    public static boolean mFirst=true;
    
    //constructor  
    public TileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setClickable(true);
    }

    public int min(int x, int y){
    	return x>y?y:x;
    }
    public int max(int x, int y){
    	return x<y?y:x;
    }
    
    public void resetTiles(int tilecount) {
    	mTileArray = new Bitmap[tilecount];
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    	boolean rotation=false;
    	if ( ( (oldh>oldw) & (h<w) ) | ( (oldh<oldw) & (h>w) ) ){
    		rotation=true;
    	}
    	TileSizeChange(w, h, rotation);      
    }
    
    public void TileSizeChange(){
    	TileSizeChange(getWidth(), getHeight(), false);
    }
    
    public void TileSizeChange(int w, int h, boolean rotation){
    	if (mFirst){
    		//long screen or not
    		if ((double)min(w, h)/max(w, h)>0.65) {
    			mAbsoluteTileCount = new Coordinate(NOT_LONG_SIZE);
    		}else{
    			mAbsoluteTileCount = new Coordinate(LONG_SIZE);
    		}
    		mTileCount = new Coordinate(mAbsoluteTileCount);
    		if (h<w){
    			mTileCount.reverse();
    		}
			mFirst=false;
    	}else{ 
    		if(rotation){
    			//change X Y
    			mTileCount.reverse();
    		}
    	}
    	
    	mTileSize = min((int)Math.floor((double)w / mTileCount.getX()),
    			(int)Math.floor((double)h / mTileCount.getY()));
    	//Y>X
    	mTileGrid = new int[mAbsoluteTileCount.getY()][mAbsoluteTileCount.getY()];
    	mOffset = new Coordinate((w - (mTileSize * mTileCount.getX())) / 2, 
    							(h - (mTileSize * mTileCount.getY())) / 2);
    }
    
    public void loadTile(int key, Drawable tile) {
        Bitmap bitmap = Bitmap.createBitmap(mTileSize, mTileSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        tile.setBounds(0, 0, mTileSize, mTileSize);
        tile.draw(canvas);        
        mTileArray[key] = bitmap;
    }
    
    //clear or fill the background
    public void clearTiles() {
    	for (int x = 0; x < (mAbsoluteTileCount.getX()); x++) {
            for (int y = 0; y < (mAbsoluteTileCount.getY()); y++) {
                setTile(0, x, y);
            }
        }
    }
    
    protected Coordinate ifNeedRotate90(int x, int y) {
    	return (getHeight()>getWidth()? 
    			new Coordinate(x,y): 
    			new Coordinate(y,mTileCount.getY() - ++x)); 
    }
    
    protected Coordinate ifNeedRotate270(int x, int y) {
    	return (getHeight()>getWidth()? 
    			new Coordinate(x,y): 
    			new Coordinate(mTileCount.getX() - ++y,x)); 
    }
    
    public void setTile(int tileindex, int x, int y) {
    	Coordinate xy = ifNeedRotate90(x,y);
    	mTileGrid[xy.getX()][xy.getY()] = tileindex;
	}

    @Override
    public void onDraw(Canvas canvas) {
    	super.onDraw(canvas);
        for (int x = 0; x < (mTileCount.getX()); x += 1) {
            for (int y = 0; y < (mTileCount.getY()); y += 1) {
            	if (mTileArray != null){
                    	canvas.drawBitmap(mTileArray[mTileGrid[x][y]], 
                    		mOffset.getX() + x * mTileSize, 
                    		mOffset.getY() + y * mTileSize,mPaint);
                }  
            }
        }
    }
}
