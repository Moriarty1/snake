package dima.android.snake;

public enum Sounds {
	SOUND_STEP 		(R.raw.step), 
	SOUND_LEVELUP 	(R.raw.levelup),
    SOUND_BITE 		(R.raw.bite),
    SOUND_CRASH 	(R.raw.damage);
	
	public final int sound;
    private Sounds (int sound) {
    	this.sound=sound;
	}
}
