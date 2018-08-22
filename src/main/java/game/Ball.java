package game;

import java.io.StringReader;
import java.util.Collection;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class Ball {
//	private final int id;
	public int width;
	public int heigth;
	public Location location;
	public int Vx;
	public int Vy;
	private final WebSocketSession session;
	
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
		Vy = (int) Math.floor(Math.random() * 12 - 2);
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