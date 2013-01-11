
package dima.android.snake;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class Snake extends Activity {
    public static SharedPreferences prefs;
	private SnakeView mSnakeView;
    private static String ICICLE_KEY = "snake-view";
    Bundle map = null;
    
    // onPreferenceChange
    SharedPreferences.OnSharedPreferenceChangeListener prefListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs,String key) {
        	//mSnakeView.initNewGame();
        	//set screen orientation
            setRequestedOrientation(Integer.parseInt(prefs.getString(
            		"orientation",String.valueOf(ActivityInfo.SCREEN_ORIENTATION_SENSOR))));
        	mSnakeView.loadSettings();
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //set screen orientation
        setRequestedOrientation(Integer.parseInt(prefs.getString(
        		"orientation",String.valueOf(ActivityInfo.SCREEN_ORIENTATION_SENSOR))));
        setContentView(R.layout.snake_layout);
        mSnakeView = (SnakeView) findViewById(R.id.snake);
        mSnakeView.setTextView((TextView) findViewById(R.id.text));
        if (savedInstanceState == null) {
            // just launched 
            mSnakeView.setMode(Mode.READY);
        } else {
            // restored
            map = savedInstanceState.getBundle(ICICLE_KEY);
            if (map != null) {
                mSnakeView.restoreState(map);
            } else {
                mSnakeView.setMode(Mode.PAUSE);
            }
        }    
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;		
    }
    
    @Override
    public boolean onMenuOpened(int featureId, Menu menu){
    	if (mSnakeView.getMode()==Mode.RUNNING){
			mSnakeView.setMode(Mode.PAUSE);
    	}
    	if (mSnakeView.getMusic()){
    		mSnakeView.mPlayer.pause();
    	}
    	return true;
    }
    
    @Override
    public void onOptionsMenuClosed(Menu menu){
    	if (mSnakeView.getMode()==Mode.PAUSE){
			mSnakeView.setMode(Mode.RUNNING);
    	}
    	if (mSnakeView.getMusic()){
    		mSnakeView.mPlayer.start();
    	}
    }
    
    //menu select item
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case R.id.menu_new_game:
			mSnakeView.initNewGame();
			break;
		case R.id.menu_level1:
			mSnakeView.initNewLevel(1);
			onOptionsMenuClosed(null);
			break;
		case R.id.menu_level2:
			mSnakeView.initNewLevel(2);
			onOptionsMenuClosed(null);
			break;
		case R.id.menu_level3:
			mSnakeView.initNewLevel(3);
			onOptionsMenuClosed(null);
			break;
		case R.id.menu_level4:
			mSnakeView.initNewLevel(4);
			onOptionsMenuClosed(null);
			break;
		case R.id.menu_level5:
			mSnakeView.initNewLevel(5);
			onOptionsMenuClosed(null);
			break;	
		case R.id.menu_settings:
			Intent settings = new Intent(Snake.this, SettingsActivity.class);
	        startActivity(settings);
			break;
		}
        return true;
    } 

    @Override
    protected void onPause() {
        super.onPause();
        mSnakeView.setMode(Mode.PAUSE);
        if (mSnakeView.getMusic()){
    		mSnakeView.mPlayer.pause();
    	}
    }

    @Override 
    public void onResume(){
        super.onResume(); 
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(prefListener);
        if (mSnakeView.getMusic()){
    		mSnakeView.mPlayer.start();
    	}
    }
    
    //save game
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBundle(ICICLE_KEY, mSnakeView.saveState());
    }
}
