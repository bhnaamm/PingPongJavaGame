package game;

import java.util.Collection;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Player {

	private final int id;
	private final WebSocketSession session;
	private Direction direction;
	private int heigth;
	private Ball ball;

	public WebSocketSession getSession() {
		return session;
	}

	private String name;
	private Location location;
	private int score;
	private int X;
	private int Y;

	public int getX() {
		return X;
	}

	public void setX(int x) {
		X = x;
	}

	public int getY() {
		return Y;
	}

	public void setY(int y) {
		Y = y;
	}

	private int width;

	public Player(int id, WebSocketSession session) {
		this.id = id;

		this.score = 0;
		this.session = session;
		this.location = new Location();
		this.location.y = -Constants.DEFAULT_HEIGHT / 2; // temporary solution
		this.location.x = 5;
		this.width = Constants.DEFAULT_WIDTH;
		this.heigth = Constants.DEFAULT_HEIGHT;
		this.X = this.location.x;
		this.Y = this.location.y;
	}

	public synchronized void update(Collection<Player> players) throws Exception {
		if (this.direction == null)
			return;
		this.location = new Location();
		Location nextLocation = this.location.getNextLocation(this.direction);
		final JsonObjectBuilder json = Json.createObjectBuilder();

		nextLocation.y = (this.direction == Direction.DOWN)
				? Math.min(Constants.GAME_H - Constants.DEFAULT_HEIGHT, this.Y + Constants.STEP_SIZE)
				: (this.direction == Direction.UP) ? Math.max(0, this.Y - Constants.STEP_SIZE) : this.Y;
		this.Y = nextLocation.y;
		
		
		this.ball = PlayerTimer.getBall();
		int tmpX=0;
		
		//due to unimportance of details, Collisions have been Ignored
		if(this.ball.Vx > 0)
		{
			if(this.X < Constants.GAME_W/2)
				tmpX = 503; //512-5-4
			
//			if(this.ball.location.x - tmpX<3 && 
//					(this.Y <= this.ball.location.y 
//					&& this.ball.location.y< (this.Y+Constants.PADDLE_H)))
//				this.ball.Vx = -this.ball.Vx;
			 if (tmpX <= this.ball.location.x + this.ball.width &&
					 tmpX > this.ball.location.x - this.ball.Vx + this.ball.width) {
		            int collisionDiff = this.ball.location.x + this.ball.width - tmpX;
		            int k = collisionDiff/this.ball.Vx;
		            int y = this.ball.Vy*k + (this.ball.location.y - this.ball.Vy);
		            if (y >= this.Y && y + this.ball.heigth <= this.Y + Constants.PADDLE_H) {
		                // collides with right paddle
		                this.ball.location.x = tmpX - this.ball.width;
		                this.ball.location.y = (int) Math.floor(this.ball.location.y - this.ball.Vy + this.ball.Vy*k);
		                this.ball.Vx = -this.ball.Vx;
		            }
		        }
		else
		{
			if(this.X > Constants.GAME_W/2)
				tmpX = 7; //5+2
			if (tmpX + this.width >= this.ball.location.x) {
	            int collisionDiff1 = tmpX + this.width - this.ball.location.x;
	            int k1 = collisionDiff1/-this.ball.Vx;
	            int y1 = this.ball.Vy*k1 + (this.ball.location.y - this.ball.Vy);
	            if (y1 >= this.Y && y1 + this.ball.heigth <= this.Y + this.heigth) {
	                // collides with the left paddle
	                this.ball.location.x = this.X + this.width;
	                this.ball.location.y = (int) Math.floor(this.ball.location.y - this.ball.Vy + this.ball.Vy*k1);
	                this.ball.Vx = -this.ball.Vx;
	            }
	        }
		}
//			
			 if ((this.ball.Vy < 0 && this.ball.location.y < 0) ||
			            (this.ball.Vy > 0 && this.ball.location.y + this.ball.heigth > this.heigth)) {
			        this.ball.Vy = -this.ball.Vy;
			    }
			 
//			if(this.ball.location.x == tmpX + 5 && 
//					(this.Y <= this.ball.location.y 
//					&& this.ball.location.y <= (this.Y+Constants.PADDLE_H)))
//				this.ball.Vx = -this.ball.Vx;
//		}

		
		json
			.add("players", Json.createArrayBuilder()
				.add(Json.createObjectBuilder()
						.add("id", this.id)
						.add("x", this.X)
						.add("y", this.Y)));

		if (ball != null) {

			json
				.add("ball", Json.createArrayBuilder()
					.add(Json.createObjectBuilder()
							.add("x", this.ball.location.x)
							.add("y", this.ball.location.y)
							.add("vx", this.ball.Vx)
							.add("vy", this.ball.Vy)
							.add("width", this.ball.width)
							.add("heigth", this.ball.heigth)));
		}

		json.add("type", "update");
		final String jsonStr = json.build().toString();
		try {
			sendMessage(jsonStr);
		} catch (Exception e) {
			e.printStackTrace();
			}
		}
	}

	private synchronized void giveScore() throws Exception {
		this.score++;
		sendMessage("{'type':'score'}");
	}

	protected void sendMessage(String msg) throws Exception {

		this.session.sendMessage(new TextMessage(msg));
	}

	public synchronized void setDirection(Direction direction) {
		this.direction = direction;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public int getHeigth() {
		return heigth;
	}

	public void setHeigth(int heigth) {
		this.heigth = heigth;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

}
