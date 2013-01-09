package dima.android.snake;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


public class SnakeView extends TileView {
    //private static final String TAG = "SnakeView";
    
    //mode of application
    
    private int mMode = READY;
    public static final int PAUSE = 0;
    public static final int READY = 1;
    public static final int RUNNING = 2;
    public static final int LOSE = 3;
    public static final int LEVELUP = 4;

    //current direction     
    private int mDirection = NORTH;
    private int mNextDirection = NORTH;
    private static final int NORTH = 1;
    private static final int SOUTH = 2;
    private static final int EAST = 3;
    private static final int WEST = 4;
    
    // tiles      
    private static final int WALL_IMAGE_1 = 1; // do not change number
    private static final int WALL_IMAGE_2 = 2; // do not change number
    private static final int WALL_IMAGE_3 = 3; // do not change number   
    private static final int HEAD_IMAGE = 4;
    private static final int SNAKE_IMAGE = 5;
    private static final int APLLE_IMAGE = 6;
    private static final int APLLE_LV_IMAGE = 7;
    private static final int APLLE_BONUS_IMAGE = 8;
    private static final int BACKGROUND_IMAGE = 9; // last mast be background 

    private long mLevel;
    private long mLastLevel = 5;
    private long mScore = 0;
    private long mMoveDelay;
    private long mLongPressDelay = 1000;  
    private long mLastMove;
    private long mLastTouch;
    private long mBonus;
    private long mBonusLeft=0;
    private long mBonusTime;
    private long mBonusTimeLeft=0;
    private long mApplesNumber;
    private long mLevelUp;
    private long mLevelUpLeft=0;
    private long mSnakeStartLength;
    private TextView mStatusText;
    
    //sound
    AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
    private SoundPool soundPool;
    private SparseIntArray soundsMap;
    private static final int SOUND_STEP=1;
    private static final int SOUND_LEVELUP=2;
    private static final int SOUND_BITE=3;
    private static final int SOUND_CRASH=4;
    public MediaPlayer mPlayer;
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
    
    Coordinate collisionCoordinate = null;
   
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
        soundsMap = new SparseIntArray();
        soundsMap.put(SOUND_STEP, soundPool.load(getContext(),R.raw.step , 1));
        soundsMap.put(SOUND_LEVELUP, soundPool.load(getContext(),R.raw.levelup , 1));
        soundsMap.put(SOUND_BITE, soundPool.load(getContext(),R.raw.bite , 1));
        soundsMap.put(SOUND_CRASH, soundPool.load(getContext(),R.raw.damage , 1));
   }

    //load settings
    public void loadSettings(){
    	mMoveDelay = (11-Integer.parseInt(Snake.prefs.getString("speed","6")))*100;
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
    }
    
    public void checkSettings(){
    	//List<Coordinate> mTempAppleList = new ArrayList<Coordinate>();
    	if (mApplesNumber<mAppleList.size()){
    		mAppleList = mAppleList.subList(0, (int)mApplesNumber);
    	} else{
    		while (mApplesNumber>mAppleList.size()){
    			addRandomApple(APLLE_IMAGE);
    		}
    	}
    }
    
    private void initSnakeView() {
    	setFocusable(true);
    	mFirst=true;
    	mWTileCount = Snake.prefs.getBoolean("double", false)?2*TILE_COUNT:TILE_COUNT;
    	TileSizeChange();
    	Resources r = this.getContext().getResources();
        resetTiles(10);
        loadTile(WALL_IMAGE_1, r.getDrawable(R.drawable.snake_wall_1));
        loadTile(WALL_IMAGE_2, r.getDrawable(R.drawable.snake_wall_2));
        loadTile(WALL_IMAGE_3, r.getDrawable(R.drawable.snake_wall_3));
        loadTile(HEAD_IMAGE, r.getDrawable(R.drawable.snake_head));
        loadTile(SNAKE_IMAGE, r.getDrawable(R.drawable.snake_body));
        loadTile(APLLE_IMAGE, r.getDrawable(R.drawable.snake_apple));
        loadTile(APLLE_LV_IMAGE, r.getDrawable(R.drawable.snake_apple_lv));
        loadTile(APLLE_BONUS_IMAGE, r.getDrawable(R.drawable.snake_apple_bonus));
        loadTile(BACKGROUND_IMAGE, r.getDrawable(R.drawable.snake_background));    	
    }
    public void initNewGame() {
    	loadSettings();
    	initSnakeView();
    	mLevel = 1; //mLevel = Integer.parseInt(Snake.prefs.getString("level","1"));
    	mSnakeStartLength=Integer.parseInt(Snake.prefs.getString("length","5"));
        mScore = 0;
        initNewLevel(mLevel);
    }
    
    public void initNewLevel(long level) {
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
        mNextDirection = EAST;
        
        Random rand = new Random();
        WallCoordinate c;
        for (int x = 0; x < (mWTileCount); x++) {
        	c = new WallCoordinate(x, 0, rand.nextInt(3)+1);
			mWallList.add(c);
            c = new WallCoordinate(x, (mHTileCount) - 1, rand.nextInt(3)+1);
			mWallList.add(c);
        } 	
        for (int y = 1; y < (mHTileCount) - 1; y++) {
        	c = new WallCoordinate(0, y, rand.nextInt(3)+1);
			mWallList.add(c);
            c = new WallCoordinate((mWTileCount) - 1, y, rand.nextInt(3)+1);
			mWallList.add(c);
        }
        
        //level selection
        XmlPullParser wallParser;        
        switch ((int)level) {
		case 2:
			wallParser = getResources().getXml(R.xml.walls2);
			break;			
		case 3:
			wallParser = getResources().getXml(R.xml.walls3);
			break;		
		case 4:
			wallParser = getResources().getXml(R.xml.walls4);
			break;			
		case 5:
			wallParser = getResources().getXml(R.xml.walls5);
			break;
		default:
			wallParser = getResources().getXml(R.xml.walls1);
		}        
        
        //adding walls
        try {
			while (wallParser.getEventType()!= XmlPullParser.END_DOCUMENT) {
			    if (wallParser.getEventType() == XmlPullParser.START_TAG
			    		&& wallParser.getName().equals("horizontal")) {
			    	wallParser.next();
			    	while (wallParser.getName().equals("wall")) {
			    		if (wallParser.getEventType()== XmlPullParser.END_TAG) {
			    			wallParser.next();
			    			continue;
			    		}
			    		String length = wallParser.getAttributeValue(null, "length");
			    		String startX = wallParser.getAttributeValue(null, "startX");
			    		String startY = wallParser.getAttributeValue(null, "startY");
			    		for (int i = 0; i < Integer.parseInt(length); i++) {
			    			c = new WallCoordinate(Integer.parseInt(startX)+i,
			    					Integer.parseInt(startY), rand.nextInt(3)+1);
			    			if ((c.getX()<(mWTileCount))&(c.getY()<(mHTileCount))){
			    				mWallList.add(c);
			    			}
			    		}
			    	wallParser.next();
			        }
			    	
			    } else{
			    	if (wallParser.getEventType() == XmlPullParser.START_TAG
				    		&& wallParser.getName().equals("vertical")) {
				    	wallParser.next();
				    	while (wallParser.getName().equals("wall")) {
				    		if (wallParser.getEventType()== XmlPullParser.END_TAG) {
				    			wallParser.next();
				    			continue;
				    		}
				    		String length = wallParser.getAttributeValue(null, "length");
				    		String startX = wallParser.getAttributeValue(null, "startX");
				    		String startY = wallParser.getAttributeValue(null, "startY");
				    		for (int i = 0; i < Integer.parseInt(length); i++) {
				    			c = new WallCoordinate(Integer.parseInt(startX),
				    					Integer.parseInt(startY)+i, rand.nextInt(3)+1);
				    			if (c.getX()<(mWTileCount)&c.getY()<(mHTileCount)){
				    				mWallList.add(c);
				    			}
				    		}
				    	wallParser.next();
				        }
				    	
				    } else{			    	 
				    	wallParser.next();
				    }
			    }	
			}
		} catch (XmlPullParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}        
        for (int i = 0; i < mApplesNumber; i++) {				
        	addRandomApple(APLLE_IMAGE);        
        }
        if ((mLevelUp==0) & (mLevel!=mLastLevel)) {
    		addRandomApple(APLLE_LV_IMAGE);
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
        map.putInt("mDirection", Integer.valueOf(mDirection));
        map.putInt("mNextDirection", Integer.valueOf(mNextDirection));
        map.putLong("mMoveDelay", Long.valueOf(mMoveDelay));
        map.putLong("mScore", Long.valueOf(mScore));
        map.putIntArray("mSnakeTrail", coordArrayListToArray(mSnakeTrail,2));
        
        map.putLong("mLevel", Long.valueOf(mLevel));
        map.putLong("mApplesNumber", Long.valueOf(mApplesNumber));
        map.putLong("mBonus", Long.valueOf(mBonus));
        map.putLong("mBonusTime", Long.valueOf(mBonusTime));
        map.putLong("mLevelUp", Long.valueOf(mLevelUp));
        map.putLong("mSnakeStartLength", Long.valueOf(mSnakeStartLength));
        map.putLong("mBonusLeft", Long.valueOf(mBonusLeft));
        map.putLong("mBonusTimeLeft", Long.valueOf(mBonusTimeLeft));
        map.putLong("mLevelUpLeft", Long.valueOf(mLevelUpLeft));
        
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
        setMode(PAUSE); 
        mAppleList = coordArrayToArrayList(icicle.getIntArray("mAppleList"));
        mAppleLvList = coordArrayToArrayList(icicle.getIntArray("mAppleLvList"));
        mAppleBonusList = coordArrayToArrayList(icicle.getIntArray("mAppleBonusList"));
        mWallList = wallCoordArrayToArrayList(icicle.getIntArray("mWallList"));
        mDirection = icicle.getInt("mDirection");
        mNextDirection = icicle.getInt("mNextDirection");
        mMoveDelay = icicle.getLong("mMoveDelay");
        mScore = icicle.getLong("mScore");
        mSnakeTrail = coordArrayToArrayList(icicle.getIntArray("mSnakeTrail"));
        
        mLevel = icicle.getLong("mLevel");
        mApplesNumber = icicle.getLong("mApplesNumber");
        mBonus = icicle.getLong("mBonus");
        mBonusTime = icicle.getLong("mBonusTime");
        mLevelUp = icicle.getLong("mLevelUp");
        mSnakeStartLength = icicle.getLong("mSnakeStartLength");
        mBonusLeft = icicle.getLong("mBonusLeft");
        mBonusTimeLeft = icicle.getLong("mBonusTimeLeft");
        mLevelUpLeft = icicle.getLong("mLevelUpLeft");
    }  

    //toush control    
    @Override
    public boolean onTouchEvent(MotionEvent event){ 
    	switch (event.getAction()){ 
    		case MotionEvent.ACTION_DOWN: 
    	
    			mLastTouch = System.currentTimeMillis();
    			
    			if ((mMode == PAUSE) && (System.currentTimeMillis() - mLastTouch > mLongPressDelay)){
    				setMode(PAUSE);
    				return(true);
    			}
    			
    			if (mMode == READY | mMode == LOSE) {
    				mLevel = 1;
    				initNewGame();
    				setMode(RUNNING);
    				update();
    				return (true);
    			}
    			
    			if (mMode == LEVELUP) {
    				if (mLevel != mLastLevel){
    					mLevel++;
    					initNewLevel(mLevel);
    					setMode(RUNNING);
    					update();
    					return true;
    				}
    			}

    			if (mMode == PAUSE) {
    				setMode(RUNNING);
    				update();
    				return true;
    			}
    			
    			if ((System.currentTimeMillis() - mLastTouch > mLongPressDelay)){
    				setMode(PAUSE);
    				return true;
    			}
    			
    			Coordinate xy = ifNeedRotate270(event.getX(),event.getY());
    			if ((mDirection == NORTH)||(mDirection == SOUTH)){
    				mNextDirection = 
    					(mSnakeTrail.get(0).getX() - (xy.getX() / mTileSize)>=0)?
    						WEST:EAST;
    				return true;
    			}    	
    	
    			if ((mDirection == WEST)||(mDirection == EAST)){
    				mNextDirection = 
    					(mSnakeTrail.get(0).getY() - (xy.getY() / mTileSize)>=0)?
    						NORTH:SOUTH;
    				return true;
    			}    	
    			break;
    		case MotionEvent.ACTION_UP:
    			break;
    			
    		default:
    			if ((mMode == RUNNING) && (System.currentTimeMillis() - mLastTouch > mLongPressDelay)){
    				setMode(PAUSE);
    				return true;
    			}
        }
    	return super.onTouchEvent(event);
    }
    
    public void setTextView(TextView newView) {
        mStatusText = newView;
    }
    
    //get Mode
    public int getMode() {
    	return this.mMode;
    }
    
    //set Mode
    public void setMode(int newMode) {
        int oldMode = mMode;
        mMode = newMode;
        if (newMode == RUNNING & oldMode != RUNNING) {
            mStatusText.setVisibility(View.INVISIBLE);
            update();
            return;
        }
        Resources res = getContext().getResources();
        CharSequence str = "";
        if (newMode == PAUSE) {
        	str = res.getString(R.string.pause) 
        			+ "\n" + res.getString(R.string.score_) + mScore
                    + "\n" + res.getString(R.string.level_) + mLevel
                    + "\n" + res.getString(R.string.touch_to_play);
        }
        if (newMode == READY) {
            str = res.getText(R.string.touch_to_play);
        }
        if (newMode == LOSE) {
            str = res.getString(R.string.game_over)
            	  + "\n" + res.getString(R.string.score_)+ mScore
                  + "\n" + res.getString(R.string.touch_to_play);
        }
        if (newMode == LEVELUP) {
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
        		this.collisionCoordinate= c;
        		return true;
            }
        }
    	return false;
    }
    
    // add random apple   
    private void addRandomApple(int kind) {
        Coordinate newCoord = null;
        boolean found = false;
        label:
        while (!found) {
            // choose new location
            int newX = 1 + RNG.nextInt((mWTileCount) - 2);
            int newY = 1 + RNG.nextInt((mHTileCount) - 2);
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
        if (mMode == RUNNING) {
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
            setTile(APLLE_IMAGE, c.getX(), c.getY());
        }
    }
    
    //draws  applesLv    
    private void updateApplesLv() {       
    	for (Coordinate c : mAppleLvList) {
            setTile(APLLE_LV_IMAGE, c.getX(), c.getY());
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
    		setTile(APLLE_BONUS_IMAGE, c.getX(), c.getY());
    	}
    }

    //draws snake
	private void updateSnake() {
		if (mMode == RUNNING) {
			boolean growSnake = false;
			
			// move head
			Coordinate head = mSnakeTrail.get(0);
			Coordinate newHead = new Coordinate(1, 1);
			mDirection = mNextDirection;

			newHead = new Coordinate(
					head.getX() + ( (mDirection == EAST) ?	1 : 
									(mDirection == WEST) ? -1 : 0),
					head.getY() + (	(mDirection == SOUTH)?	1 : 
									(mDirection == NORTH)? -1 : 0));

			// collisions with Wall or Snake, except last trail 
			if (collision(newHead, mWallList) | ((collision(newHead, mSnakeTrail)) 
							& collisionCoordinate != mSnakeTrail.get(mSnakeTrail.size() - 1))){
				setMode(LOSE);
				// play crash
				if (mSounds){
					soundPool.play(soundsMap.get(SOUND_CRASH), 1, 1, 1, 0, 1);
				}
				return;
			}

			// find appleLv
			if (collision(newHead, mAppleLvList)) {
				
				// play levelup
				if (mSounds){
    				soundPool.play(soundsMap.get(SOUND_LEVELUP), 1, 1, 1, 0, 1);
				}
				mAppleLvList.remove(collisionCoordinate);
				setMode(LEVELUP);
				return;
			}

			// find apple
			if (collision(newHead, mAppleList)) {
				
				// play bite
				if (mSounds){
    				soundPool.play(soundsMap.get(SOUND_BITE), 1, 1, 1, 0, 1);
				}
				mAppleList.remove(collisionCoordinate);
				addRandomApple(APLLE_IMAGE);
				mScore += mLevel;
				mMoveDelay -= 10;
				if (mBonusLeft == 0) {
					mAppleBonusList.clear();
					addRandomApple(APLLE_BONUS_IMAGE);
					mBonusTimeLeft = mBonusTime;
					mBonusLeft = mBonus;
				} else {
					mBonusLeft--;
				}

				if ((mAppleLvList.isEmpty()) & (mLevelUpLeft >= 0)
						& (mLevel != mLastLevel)) {
					if (mLevelUpLeft == 0) {
						addRandomApple(APLLE_LV_IMAGE);
					}
					mLevelUpLeft--;
				}
				growSnake = true;
			} else if (collision(newHead, mAppleBonusList)) {
				
				// play bite
				if (mSounds){
    				soundPool.play(soundsMap.get(SOUND_BITE), 1, 1, 1, 0, 1);
				}
				mAppleBonusList.remove(collisionCoordinate);
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
				soundPool.play(soundsMap.get(SOUND_STEP), 1, 1, 1, 0, 1);
			}
			
			//draw head or body
			for (Coordinate c : mSnakeTrail) {
				setTile(c.equals(newHead)?HEAD_IMAGE:SNAKE_IMAGE, c.getX(), c.getY());
			}
		}
	}
}
