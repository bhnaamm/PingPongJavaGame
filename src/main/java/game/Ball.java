package game;

import java.io.StringReader;
import java.util.ArrayList;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Ball {

	public int width;
	public int heigth;
	public Location location;
	public int Vx;
	public int Vy;
	private final WebSocketSession session;
	private Player player;
	public synchronized void Update() throws Exception {
		
		
//		nextLocation.y = this.Y;
	final JsonObjectBuilder json = Json.createObjectBuilder();

	location.x += Vx;
	location.y += Vy;
	if (location.x > Constants.GAME_W || location.x + width < 0) {
		Vx = -Vx;
	} else if (location.y > Constants.GAME_H || location.y + heigth < 0) {
		Vy = -Vy;
	}
	
	
	int tmpX = 0;
	
	Player[] players = PlayerTimer.getPlayers().toArray(new Player[PlayerTimer.getPlayers().size()]);
	//
	if(players.length<2) return;
	if (this.Vx > 0 ) {
		this.player = players[0];
			tmpX = this.player.getX();

		if (tmpX <= this.location.x + this.width
				&& tmpX > this.location.x - this.Vx + this.width) {
			int collisionDiff = this.location.x + this.width - tmpX;
			int k = collisionDiff / this.Vx;
			int y = this.Vy * k + (this.location.y - this.Vy);
			if (y >= this.player.getY() && y + this.heigth <= this.player.getY() + Constants.PADDLE_H) {
				// collides with right paddle
				this.location.x = tmpX - this.width;
				this.location.y = (int) Math.floor(this.location.y - this.Vy + this.Vy * k);
				this.Vx = -this.Vx;
			}
		}
	} else if(this.Vx <0){
		
		this.player = players[1];
		tmpX = this.player.getX();
		if (tmpX + this.player.getWidth() >= this.location.x) {
			int collisionDiff1 = tmpX + this.player.getHeigth() - this.location.x;
			int k1 = collisionDiff1 / -this.Vx;
			int y1 = this.Vy * k1 + (this.location.y - this.Vy);
			if (y1 >= this.player.getY() && y1 + this.heigth <= this.player.getY() + this.player.getHeigth()) {
				// collides with the left paddle
				this.location.x = this.player.getX() + this.player.getWidth();
				this.location.y = (int) Math.floor(this.location.y - this.Vy + this.Vy * k1);
				this.Vx = -this.Vx;
			}
		}
	}

	
	
		json
		  .add("ball", Json.createArrayBuilder()
		    .add(Json.createObjectBuilder()
				.add("x", this.location.x)
				.add("y", this.location.y)
				.add("vx", this.Vx)
				.add("vy", this.Vy)
				.add("width", this.width)
				.add("heigth", this.heigth)));
		json.add("type", "ballUpdate");
		final String jsonStr = json.build().toString();
		try {
			sendMessage(jsonStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	public Ball(WebSocketSession session) {
		
		this.session = session;
		Vy = (int) Math.floor(Math.random() * 5 - 2);
		Vx = 7 - Math.abs(Vy);
		width = Constants.Ball_SIZE;
		heigth = Constants.Ball_SIZE;
		location = new Location();
		
	}
	
	public synchronized void setLocation(String payload) {
		JsonReader jsonReader = Json.createReader(new StringReader(payload+"}"));
		JsonObject msgJson = jsonReader.readObject();
		jsonReader.close();
		JsonObject tmpJsn = msgJson.getJsonObject("location");
		int ballX = tmpJsn.getInt("x");
		int ballY = tmpJsn.getInt("y");
		this.location.x = ballX;
		this.location.y = ballY;
	}
	protected void sendMessage(String msg) throws Exception {

		if(this.session.isOpen())
			this.session.sendMessage(new TextMessage(msg));
	}


	public WebSocketSession getSession() {
		return session;
	}
	
	
}