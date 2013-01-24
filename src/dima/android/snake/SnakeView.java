package dima.android.snake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ColorMatrixColorFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class SnakeView extends TileView {
    //private static final String TAG = "SnakeView";
	
	private Mode mMode = Mode.READY;
	
	private Direction mDirection = Direction.NORTH;
	private Direction mNextDirection;
    
    private int 
    	mLevel,
    	mLastLevel = 10, //TODO change
    	mScore,
    	mMoveDelay,
    	mLongPressDelay = 1000, //TODO Need?   
    	
    	mBonus,
    	mBonusLeft,
    	mBonusTime,
    	mBonusTimeLeft,
    	mApplesNumber,
    	mLevelUp,
    	mLevelUpLeft, 
    	mSnakeStartLength;
    
    private long 
    	mLastMove, 
    	mLastTouch;
    
    private TextView mStatusText;
	
    static final float[] NEGATIVE_COLOR_MATRIX = { 
	    -1.0f, 0, 0, 0, 255, //red
	    0, -1.0f, 0, 0, 255, //green
	    0, 0, -1.0f, 0, 255, //blue
	    0, 0, 0, 1.0f, 0 //alpha  
	  };
    
    //sound
    private SoundPool soundPool;
    private HashMap<Sounds, Integer> soundsMap;
    protected MediaPlayer mPlayer;
    private boolean mSounds;
    private boolean mMusic=false;
    
    public boolean getMusic(){
    	return mMusic;
    }
    
    private List<Coordinate> mSnakeTrail = new ArrayList<Coordinate>();
    private List<Coordinate> mAppleList = new ArrayList<Coordinate>();
    private List<Coordinate> mAppleLvList = new ArrayList<Coordinate>();
    private List<Coordinate> mAppleBonusList = new ArrayList<Coordinate>();
    private List<WallCoordinate> mWallList = new ArrayList<WallCoordinate>();
    
    Coordinate mCollisionCoordinate = null;
   
    private static final Random RNG = new Random();

    // handler
    private RefreshHandler mRedrawHandler = new RefreshHandler();

    protected Coordinate ifNeedRotate270(float x, float y) {
    	return (getHeight()>getWidth()? 
    			new Coordinate((int)x,(int)y): 
    			new Coordinate((int)(getHeight() - ++y),(int)x)); 
    }
    
    @SuppressLint("HandlerLeak")
	class RefreshHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            SnakeView.this.update();
            SnakeView.this.invalidate();
        }

        public void sleep(long delayMillis) {
        	this.removeMessages(0);
            sendMessageDelayed(obtainMessage(0), delayMillis);
        }
    };

    //constructor
    public SnakeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        soundPool = new SoundPool(4, AudioManager.STREAM_MUSIC, 100);
        soundsMap = new HashMap<Sounds, Integer>();
        
        for (Sounds sounds : Sounds.values()) {
        	soundsMap.put(sounds, soundPool.load(getContext(),sounds.sound , 1));
        }
   }

    //load settings
    public void loadSettings(){
    	mMoveDelay = (Integer.parseInt(Snake.prefs.getString("speed","400")));
    	mApplesNumber = Integer.parseInt(Snake.prefs.getString("apples","1"));
    	if (mBonus != Integer.parseInt(Snake.prefs.getString("bonus","5"))){
    		mBonus = Integer.parseInt(Snake.prefs.getString("bonus","5"));
        	mBonusLeft=mBonus;
    	}
    	if (mBonusTime != Integer.parseInt(Snake.prefs.getString("bonus_time","20"))){
    		mBonusTime = Integer.parseInt(Snake.prefs.getString("bonus_time","20"));
        	mBonusTimeLeft=mBonusTime;
    	}
    	if (mLevelUp != Integer.parseInt(Snake.prefs.getString("levelup","20"))){
    		mLevelUp = Integer.parseInt(Snake.prefs.getString("levelup","20"));
    		mLevelUpLeft = mLevelUp;
    	}
    	mSounds = Snake.prefs.getBoolean("sounds", true);
    	if (mMusic != Snake.prefs.getBoolean("music", true)){
    		mMusic = Snake.prefs.getBoolean("music", true);
    		if (mMusic) {
    			mPlayer = MediaPlayer.create (getContext(), R.raw.background);
    	        mPlayer.setLooping(true);
    			mPlayer.start();
    		}else{
    			mPlayer.release();
			}
    	}
    	if (Snake.prefs.getBoolean("negative", false)){
    		mPaint.setColorFilter(new ColorMatrixColorFilter(NEGATIVE_COLOR_MATRIX));
    	}else{
    		mPaint.setColorFilter(null);
    	}
    }
    
    public void checkSettings(){
    	//List<Coordinate> mTempAppleList = new ArrayList<Coordinate>();
    	if (mApplesNumber<mAppleList.size()){
    		mAppleList = mAppleList.subList(0, (int)mApplesNumber);
    	} else{
    		while (mApplesNumber>mAppleList.size()){
    			addRandomApple(Tiles.APLLE_IMAGE);
    		}
    	}
    }
    
    private void initSnakeView() {
    	setFocusable(true);
    	mFirst=true;
    	//mWTileCount = TILE_COUNT;
    	//mWTileCount = Snake.prefs.getBoolean("double", false)?2*TILE_COUNT:TILE_COUNT;
    	TileSizeChange();
    	Resources r = this.getContext().getResources();
        resetTiles(9);
        
        for (Tiles tiles : Tiles.values()) {
			loadTile(tiles.ordinal(), r.getDrawable(tiles.tile));
		}
    }
    public void initNewGame() {
    	loadSettings();
    	initSnakeView();
    	mLevel = 1; //mLevel = Integer.parseInt(Snake.prefs.getString("level","1"));
    	mSnakeStartLength=Integer.parseInt(Snake.prefs.getString("length","5"));
        mScore = 0;
        initNewLevel(mLevel);
    }
    
    public void initNewLevel(int level) {
    	initSnakeView();
    	mLevel=level;
        mBonusLeft=mBonus;
        mBonusTimeLeft=mBonusTime;
        mLevelUpLeft=mLevelUp;
    	mSnakeTrail.clear();
        mAppleList.clear();
        mAppleLvList.clear();
        mAppleBonusList.clear();
        mWallList.clear();      

        //initiation 
        mSnakeTrail.add(new Coordinate(0, 1)); //head
        mNextDirection = Direction.EAST;
        
        Random rand = new Random();
        WallCoordinate c;
   
        
        //load level from picture
        BitmapFactory.Options bitOpt=new BitmapFactory.Options();
        bitOpt.inScaled=false;
        TypedArray levels = getResources().obtainTypedArray(R.array.levels);
        Bitmap  mLevelImage = BitmapFactory.decodeResource(getResources(), levels.getResourceId(level-1, R.drawable.level1), bitOpt);
        int height = mLevelImage.getHeight();
        int width = mLevelImage.getWidth();
        int[] pixels = new int[height*width];
        mLevelImage.getPixels(pixels, 0, width, 0, 0, width, height);
        
        for (int i = 0; i < pixels.length; i++) {
			if (pixels[i]==Color.BLACK){
				c = new WallCoordinate(i%width, i/width, rand.nextInt(3)+1);
				if (c.getX()<(mAbsoluteTileCount.getX())&
						c.getY()<(mAbsoluteTileCount.getY())){
    				mWallList.add(c);
				}
			}
		}
        
        for (int i = 0; i < mApplesNumber; i++) {				
        	addRandomApple(Tiles.APLLE_IMAGE);        
        }
        if ((mLevelUp==0) & (mLevel!=mLastLevel)) {
    		addRandomApple(Tiles.APLLE_LV_IMAGE);
    		mLevelUpLeft--;		
    	} 
    }
    
    // ArrayList of wallcoordinates as [x1,y1,x2,y2,x3,y3...]    
    private int[] coordArrayListToArray(List<?> cvec, int dimension) {
        int count = cvec.size();
        int[] rawArray = new int[count * dimension];
        for (int index = 0; index < count; index++) {
            Coordinate c = (Coordinate)cvec.get(index);
            rawArray[dimension * index] = c.getX();
            rawArray[dimension * index + 1] = c.getY();
            if (dimension == 3){
            	WallCoordinate w = (WallCoordinate)cvec.get(index);
            	rawArray[dimension * index + 2] = w.getVersion();
            }
        }
        return rawArray;
    }
    
    //save game state
    public Bundle saveState() {
        Bundle map = new Bundle();
        map.putIntArray("mAppleList", coordArrayListToArray(mAppleList,2));
        map.putIntArray("mAppleLvList", coordArrayListToArray(mAppleLvList,2));
        map.putIntArray("mAppleBonusList", coordArrayListToArray(mAppleBonusList,2));
        map.putIntArray("mWallList", coordArrayListToArray(mWallList,3));
        map.putString("mDirection", mDirection.toString());
        map.putString("mNextDirection", mNextDirection.toString());
        map.putInt("mMoveDelay", Integer.valueOf(mMoveDelay));
        map.putInt("mScore", Integer.valueOf(mScore));
        map.putIntArray("mSnakeTrail", coordArrayListToArray(mSnakeTrail,2));
        
        map.putInt("mLevel", Integer.valueOf(mLevel));
        map.putInt("mApplesNumber", Integer.valueOf(mApplesNumber));
        map.putInt("mBonus", Integer.valueOf(mBonus));
        map.putInt("mBonusTime", Integer.valueOf(mBonusTime));
        map.putInt("mLevelUp", Integer.valueOf(mLevelUp));
        map.putInt("mSnakeStartLength", Integer.valueOf(mSnakeStartLength));
        map.putInt("mBonusLeft", Integer.valueOf(mBonusLeft));
        map.putInt("mBonusTimeLeft", Integer.valueOf(mBonusTimeLeft));
        map.putInt("mLevelUpLeft", Integer.valueOf(mLevelUpLeft));
        
        return map;
    }
       
    //int array to Coordinate     
    private List<Coordinate> coordArrayToArrayList(int[] rawArray) {
        List<Coordinate> coordArrayList = new ArrayList<Coordinate>();
        int coordCount = rawArray.length;
        for (int index = 0; index < coordCount; index += 2) {
            Coordinate c = new Coordinate(rawArray[index], rawArray[index + 1]);
            coordArrayList.add(c);
        }
        return coordArrayList;
    }
    
    //int array to WallCoordinate     
    private List<WallCoordinate> wallCoordArrayToArrayList(int[] rawArray) {
        List<WallCoordinate> wallCoordArrayList = new ArrayList<WallCoordinate>();

        int coordCount = rawArray.length;
        for (int index = 0; index < coordCount; index += 3) {
            WallCoordinate c = new WallCoordinate(rawArray[index], rawArray[index + 1], rawArray[index + 2]);
            wallCoordArrayList.add(c);
        }
        return wallCoordArrayList;
    }
    
    //restore game      
    public void restoreState(Bundle icicle) {
        setMode(Mode.PAUSE); 
        mAppleList = coordArrayToArrayList(icicle.getIntArray("mAppleList"));
        mAppleLvList = coordArrayToArrayList(icicle.getIntArray("mAppleLvList"));
        mAppleBonusList = coordArrayToArrayList(icicle.getIntArray("mAppleBonusList"));
        mWallList = wallCoordArrayToArrayList(icicle.getIntArray("mWallList"));
        mDirection = Direction.valueOf(icicle.getString("mDirection"));
        mNextDirection = Direction.valueOf(icicle.getString("mNextDirection"));
        mMoveDelay = icicle.getInt("mMoveDelay");
        mScore = icicle.getInt("mScore");
        mSnakeTrail = coordArrayToArrayList(icicle.getIntArray("mSnakeTrail"));
        
        mLevel = icicle.getInt("mLevel");
        mApplesNumber = icicle.getInt("mApplesNumber");
        mBonus = icicle.getInt("mBonus");
        mBonusTime = icicle.getInt("mBonusTime");
        mLevelUp = icicle.getInt("mLevelUp");
        mSnakeStartLength = icicle.getInt("mSnakeStartLength");
        mBonusLeft = icicle.getInt("mBonusLeft");
        mBonusTimeLeft = icicle.getInt("mBonusTimeLeft");
        mLevelUpLeft = icicle.getInt("mLevelUpLeft");
    }  

    //toush control    
    @Override
    public boolean onTouchEvent(MotionEvent event){ 
    	switch (event.getAction()){ 
    		case MotionEvent.ACTION_DOWN: 
    	
    			mLastTouch = System.currentTimeMillis();
    			
    			if ((mMode == Mode.PAUSE) && (System.currentTimeMillis() - mLastTouch > mLongPressDelay)){
    				setMode(Mode.PAUSE);
    				return(true);
    			}
    			
    			if (mMode == Mode.READY | mMode == Mode.LOSE) {
    				mLevel = 1;
    				initNewGame();
    				setMode(Mode.RUNNING);
    				update();
    				return (true);
    			}
    			
    			if (mMode == Mode.LEVELUP) {
    				if (mLevel != mLastLevel){
    					mLevel++;
    					initNewLevel(mLevel);
    					setMode(Mode.RUNNING);
    					update();
    					return true;
    				}
    			}

    			if (mMode == Mode.PAUSE) {
    				setMode(Mode.RUNNING);
    				update();
    				return true;
    			}
    			
    			if ((System.currentTimeMillis() - mLastTouch > mLongPressDelay)){
    				setMode(Mode.PAUSE);
    				return true;
    			}
    			
    			Coordinate xy = ifNeedRotate270(event.getX(),event.getY());
    			if ((mDirection == Direction.NORTH)||(mDirection == Direction.SOUTH)){
    				mNextDirection = 
    					(mSnakeTrail.get(0).getX() - (xy.getX() / mTileSize)>=0)?
    						Direction.WEST:Direction.EAST;
    				return true;
    			}    	
    	
    			if ((mDirection == Direction.WEST)||(mDirection == Direction.EAST)){
    				mNextDirection = 
    					(mSnakeTrail.get(0).getY() - (xy.getY() / mTileSize)>=0)?
    						Direction.NORTH:Direction.SOUTH;
    				return true;
    			}    	
    			break;
    		case MotionEvent.ACTION_UP:
    			break;
    			
    		default:
    			if ((mMode == Mode.RUNNING) && (System.currentTimeMillis() - mLastTouch > mLongPressDelay)){
    				setMode(Mode.PAUSE);
    				return true;
    			}
        }
    	return super.onTouchEvent(event);
    }
    
    public void setTextView(TextView newView) {
        mStatusText = newView;
    }
    
    //get Mode
    public Mode getMode() {
    	return this.mMode;
    }
    
    //set Mode
    public void setMode(Mode newMode) {
    	Mode oldMode = mMode;
        mMode = newMode;
        if (newMode == Mode.RUNNING & oldMode != Mode.RUNNING) {
            mStatusText.setVisibility(View.INVISIBLE);
            update();
            return;
        }
        Resources res = getContext().getResources();
        CharSequence str = "";
        if (newMode == Mode.PAUSE) {
        	str = res.getString(R.string.pause) 
        			+ "\n" + res.getString(R.string.score_) + mScore
                    + "\n" + res.getString(R.string.level_) + mLevel
                    + "\n" + res.getString(R.string.touch_to_play);
        }
        if (newMode == Mode.READY) {
            str = res.getText(R.string.touch_to_play);
        }
        if (newMode == Mode.LOSE) {
            str = res.getString(R.string.game_over)
            	  + "\n" + res.getString(R.string.score_)+ mScore
                  + "\n" + res.getString(R.string.touch_to_play);
        }
        if (newMode == Mode.LEVELUP) {
            str = res.getString(R.string.levelup)
            	  + "\n" + res.getString(R.string.next_level_)+ (mLevel+1)
                  + "\n" + res.getString(R.string.touch_to_play);
        }
        mStatusText.setText(str);
        mStatusText.setVisibility(View.VISIBLE);
    }
    
    
    private boolean collision(Coordinate newCoord, List<? extends Coordinate> list){
    	@SuppressWarnings("unchecked")
		List<Coordinate> list2 = (List<Coordinate>) list;
    	for (Coordinate c : list2) {
        	if (c.equals(newCoord)) {
        		mCollisionCoordinate= c;
        		return true;
            }
        }
    	return false;
    }
    
    // add random apple   
    @SuppressWarnings("incomplete-switch")
	private void addRandomApple(Tiles kind) {
        Coordinate newCoord = null;
        boolean found = false;
        label:
        while (!found) {
            // choose new location
            int newX = 1 + RNG.nextInt((mAbsoluteTileCount.getX()) - 2);
            int newY = 1 + RNG.nextInt((mAbsoluteTileCount.getY()) - 2);
            newCoord = new Coordinate(newX, newY);
            
            if (collision(newCoord, mSnakeTrail)){
            	continue label;
            }            
            if (collision(newCoord, mWallList)){
            	continue label;
            }            
            if (collision(newCoord, mAppleList)){
            	continue label;
            }
            if (collision(newCoord, mAppleLvList)){
            	continue label;
            }
            if (collision(newCoord, mAppleBonusList)){
            	continue label;
            }                
            found = true;
        }
        switch (kind) {
		case APLLE_IMAGE:
			mAppleList.add(newCoord);
			break;
		case APLLE_LV_IMAGE:
			mAppleLvList.add(newCoord);
			break;	
		case APLLE_BONUS_IMAGE:
			mAppleBonusList.add(newCoord);
			break;	
		}
    }  
     
    //updating screen     
    public void update() {
        if (mMode == Mode.RUNNING) {
        	checkSettings();
            long now = System.currentTimeMillis();
            if (now - mLastMove > mMoveDelay) {                
            	clearTiles();
                updateWalls();
                updateSnake();
                updateApples();
                updateApplesLv();
                updateApplesBonus();                
                mLastMove = now;
            }
            mRedrawHandler.sleep(mMoveDelay);
        }
    }
    
	//draws  walls    
    private void updateWalls() {    	
        for (WallCoordinate c : mWallList) {
        	setTile(c.getVersion(), c.getX(), c.getY());
        }
    }
    
    //draws  apples    
    private void updateApples() {    	
    	for (Coordinate c : mAppleList) {
            setTile(Tiles.APLLE_IMAGE.ordinal(), c.getX(), c.getY());
        }
    }
    
    //draws  applesLv    
    private void updateApplesLv() {       
    	for (Coordinate c : mAppleLvList) {
            setTile(Tiles.APLLE_LV_IMAGE.ordinal(), c.getX(), c.getY());
        }
    }
    
    //draws  applesLv    
    private void updateApplesBonus() {
    	if (!mAppleBonusList.isEmpty()){
        	mBonusTimeLeft--;
        	if (mBonusTimeLeft==0){
        		mAppleBonusList.clear();
        	}        	
    	}
    	for (Coordinate c : mAppleBonusList) {
    		setTile(Tiles.APLLE_BONUS_IMAGE.ordinal(), c.getX(), c.getY());
    	}
    }

    //draws snake
	private void updateSnake() {
		if (mMode == Mode.RUNNING) {
			boolean growSnake = false;
			
			// move head
			Coordinate head = mSnakeTrail.get(0);
			Coordinate newHead = new Coordinate(1, 1);
			mDirection = mNextDirection;

			newHead = new Coordinate(
					head.getX() + ( (mDirection == Direction.EAST) ?  1 : 
									(mDirection == Direction.WEST) ? -1 : 0),
					head.getY() + (	(mDirection == Direction.SOUTH)?  1 : 
									(mDirection == Direction.NORTH)? -1 : 0));

			// collisions with Wall or Snake, except last trail 
			if (collision(newHead, mWallList) | ((collision(newHead, mSnakeTrail)) 
							& mCollisionCoordinate != mSnakeTrail.get(mSnakeTrail.size() - 1))){
				setMode(Mode.LOSE);
				// play crash
				if (mSounds){
					soundPool.play(soundsMap.get(Sounds.SOUND_CRASH), 1, 1, 1, 0, 1);
				}
				return;
			}

			// find appleLv
			if (collision(newHead, mAppleLvList)) {
				
				// play levelup
				if (mSounds){
    				soundPool.play(soundsMap.get(Sounds.SOUND_LEVELUP), 1, 1, 1, 0, 1);
				}
				mAppleLvList.remove(mCollisionCoordinate);
				setMode(Mode.LEVELUP);
				return;
			}

			// find apple
			if (collision(newHead, mAppleList)) {
				
				// play bite
				if (mSounds){
    				soundPool.play(soundsMap.get(Sounds.SOUND_BITE), 1, 1, 1, 0, 1);
				}
				mAppleList.remove(mCollisionCoordinate);
				addRandomApple(Tiles.APLLE_IMAGE);
				mScore += mLevel;
				mMoveDelay *= 0.96;
				if (mBonusLeft == 0) {
					mAppleBonusList.clear();
					addRandomApple(Tiles.APLLE_BONUS_IMAGE);
					mBonusTimeLeft = mBonusTime;
					mBonusLeft = mBonus;
				} else {
					mBonusLeft--;
				}

				if ((mAppleLvList.isEmpty()) & (mLevelUpLeft >= 0)
						& (mLevel != mLastLevel)) {
					if (mLevelUpLeft == 0) {
						addRandomApple(Tiles.APLLE_LV_IMAGE);
					}
					mLevelUpLeft--;
				}
				growSnake = true;
			} else if (collision(newHead, mAppleBonusList)) {
				
				// play bite
				if (mSounds){
    				soundPool.play(soundsMap.get(Sounds.SOUND_BITE), 1, 1, 1, 0, 1);
				}
				mAppleBonusList.remove(mCollisionCoordinate);
				mScore += (mLevel * mBonusTimeLeft);
				growSnake = true;
			}

			// +head -trail
			if (!growSnake & mSnakeTrail.size() >= mSnakeStartLength) {
				mSnakeTrail.remove(mSnakeTrail.size() - 1);
			}
			mSnakeTrail.add(0, newHead);
			
			// play step
			if (mSounds){
				soundPool.play(soundsMap.get(Sounds.SOUND_STEP), 1, 1, 1, 0, 1);
			}
			
			//draw head or body
			for (Coordinate c : mSnakeTrail) {
				if (c.equals(newHead)){
					setTile(Tiles.HEAD_IMAGE.ordinal(), c.getX(), c.getY());
				}else{
					setTile(Tiles.SNAKE_IMAGE.ordinal(), c.getX(), c.getY());
				}
			}
		}
	}
}
