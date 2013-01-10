package dima.android.snake;

public interface Const {
	
	//mode of application
    int PAUSE = 0;
    int READY = 1;
    int RUNNING = 2;
    int LOSE = 3;
    int LEVELUP = 4;
	
	// direction
	int NORTH = 1;
    int SOUTH = 2;
    int EAST = 3;
    int WEST = 4;
    
    // tiles      
    int WALL_IMAGE_1 = 1; // do not change number
    int WALL_IMAGE_2 = 2; // do not change number
    int WALL_IMAGE_3 = 3; // do not change number   
    int HEAD_IMAGE = 4;
    int SNAKE_IMAGE = 5;
    int APLLE_IMAGE = 6;
    int APLLE_LV_IMAGE = 7;
    int APLLE_BONUS_IMAGE = 8;
    int BACKGROUND_IMAGE = 9; // last mast be background 
    
    //sounds
    int SOUND_STEP=1;
    int SOUND_LEVELUP=2;
    int SOUND_BITE=3;
    int SOUND_CRASH=4;
}
