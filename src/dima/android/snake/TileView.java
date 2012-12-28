
package dima.android.snake;

import android.content.Context;
import android.content.res.Configuration;
//import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

public class TileView extends View {
	protected static final int TILE_COUNT = 12;
	protected static int mTileSize;
	private static int mXTileCount;
	private static int mYTileCount;
	protected static int mWTileCount = TILE_COUNT;
	protected static int mHTileCount;
    private static int mXOffset;
    private static int mYOffset;
    private Bitmap[] mTileArray;
    private int[][] mTileGrid;
    private final Paint mPaint = new Paint();
    public static boolean mFirst=true;
    
    //constructor  
    public TileView(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setClickable(true);
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
    		if (h>w){
    			mXTileCount = mWTileCount;
    			mTileSize = (int) Math.floor((double)w / mXTileCount);    	
    			mYTileCount = (int) Math.floor((double)h / mTileSize);
    			mHTileCount = mYTileCount;
    		}else{
    			mYTileCount = mWTileCount;    	
    			mTileSize = (int) Math.floor((double)h / mYTileCount);    	
    			mXTileCount = (int) Math.floor((double)w / mTileSize);
    			mHTileCount = mXTileCount;
    		}
			mFirst=false;
    	}else{ 
    		if(rotation){
    			//change X Y
    			mXTileCount ^= mYTileCount;
    			mYTileCount ^= mXTileCount;
    			mXTileCount ^= mYTileCount;
    		}
    		mTileSize = (int) (	Math.floor((double)w / mXTileCount)<
    							Math.floor((double)h / mYTileCount)?
    			Math.floor((double)w / mXTileCount):
    			Math.floor((double)h / mYTileCount));
    	}
    	mTileGrid = new int[mHTileCount][mHTileCount];
    	mXOffset = ((w - (mTileSize * mXTileCount)) / 2);
		mYOffset = ((h - (mTileSize * mYTileCount)) / 2);
		//clearTiles();
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
    	for (int x = 0; x < (mWTileCount); x++) {
            for (int y = 0; y < (mHTileCount); y++) {
                setTile((mTileArray.length-1), x, y);
            }
        }
    }
    
    protected Coordinate ifNeedRotate90(int x, int y) {
    	return (getHeight()>getWidth()? 
    			new Coordinate(x,y): 
    			new Coordinate(y,mYTileCount - ++x)); 
    }
    
    protected Coordinate ifNeedRotate270(int x, int y) {
    	return (getHeight()>getWidth()? 
    			new Coordinate(x,y): 
    			new Coordinate(mXTileCount - ++y,x)); 
    }
    
    public void setTile(int tileindex, int x, int y) {
    	Coordinate xy = ifNeedRotate90(x,y);
    	mTileGrid[xy.getX()][xy.getY()] = tileindex;
	}

    @Override
    public void onDraw(Canvas canvas) {
    	super.onDraw(canvas);
        for (int x = 0; x < (mXTileCount); x += 1) {
            for (int y = 0; y < (mYTileCount); y += 1) {
            	if (mTileGrid[x][y] > 0 ) {
                    	canvas.drawBitmap(mTileArray[mTileGrid[x][y]], 
                    		mXOffset + x * mTileSize, mYOffset + y * mTileSize,mPaint);
                }  
            }
        }
    }
}
