package dima.android.snake;

public enum Tiles {
    
	BACKGROUND_IMAGE (R.drawable.snake_background), // first must be background
	WALL_IMAGE_1 (R.drawable.snake_wall_1), // do not change order (1)
    WALL_IMAGE_2 (R.drawable.snake_wall_2), // do not change order (2)
    WALL_IMAGE_3 (R.drawable.snake_wall_3),  // do not change order (3) 
    HEAD_IMAGE (R.drawable.snake_head),
    SNAKE_IMAGE (R.drawable.snake_body),
    APLLE_IMAGE (R.drawable.snake_apple),
    APLLE_LV_IMAGE (R.drawable.snake_apple_lv),
    APLLE_BONUS_IMAGE (R.drawable.snake_apple_bonus);
    
    
	public final int tile;
    private Tiles (int tile) {
    	this.tile=tile;
	}
    	
    }

