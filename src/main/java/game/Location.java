package game;

import java.util.Random;

import game.Direction;


public class Location {
	public int x;
	public int y;
	private static final Random random = new Random();
	
	public Location(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Location() {
		x = random.nextInt(Constants.GAME_W);
		y = random.nextInt(Constants.GAME_H);
	}
	public Location getNextLocation (Direction direction) {
		switch(direction) {
		case UP:
			return new Location(this.x, this.y - Constants.STEP_SIZE);
		case DOWN:
			return new Location(this.x,this.y + Constants.STEP_SIZE);
		case NONE:
		default:
			return this;
		}
	}
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;
		
		Location location = (Location) o;
		if(this.x != location.x) return false;
		if(this.y!= location.y) return false;
		
		return true;
	}
	
	@Override
	public int hashCode() {
		int result =this.x;
		result = 31* result + this.y;
		return result;
	}
}
