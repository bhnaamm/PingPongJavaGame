package game;

import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.springframework.format.Parser;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonParser;

import game.Location;
import game.Player;

@Component
public class SocketTextHandler extends TextWebSocketHandler {

	List<WebSocketSession> sessions = new CopyOnWriteArrayList<WebSocketSession>();

	private static final AtomicInteger playerIds = new AtomicInteger(0);
	private static final Random random = new Random();
	final JsonObjectBuilder json = Json.createObjectBuilder();
	private int id;
	private Player player;
	private Player player2;

	private Ball ball;

	public static Location randLocation() {
		int x = random.nextInt(Constants.GAME_W);
		int y = random.nextInt(Constants.GAME_H);
		return new Location(x, y);
	}

	public SocketTextHandler() {
		this.id = playerIds.getAndIncrement();
	}

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		
		
		String payload = message.getPayload();
		 
		
//		JsonReader jsonReader = Json.createReader(new StringReader(payload[0]+"}"));
//		JsonObject msgJson = jsonReader.readObject();
//		jsonReader.close();
		if ("up".equals(payload))
			this.player.setDirection(Direction.UP);
		if ("down".equals(payload))
			this.player.setDirection(Direction.DOWN);
		
//		switch (payload[1]) {
//		case "player1":
//		case "player2":
//		case "dir":
//			String direction = msgJson.getString("direction");
//			if ("up".equals(direction))
//				this.player.setDirection(Direction.UP);
//			if ("down".equals(direction))
//				this.player.setDirection(Direction.DOWN);
//			break;
//		case "ball":
//				this.ball.setLocation(payload[0]);
//				break;
//		}
	
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {

		
		this.player = new Player(this.id, session);
		//this.player2 = new Player(this.id, session);
		this.ball = new Ball(session);
		PlayerTimer.addBall(this.ball);
		PlayerTimer.addPlayer(this.player);
		//PlayerTimer.addPlayer(this.player2);
//		

		for (Iterator<Player> iterator = PlayerTimer.getPlayers().iterator(); iterator.hasNext();) {

			Player player = iterator.next();

			json.add("players", Json.createArrayBuilder().add(Json.createObjectBuilder().add("id", player.getId())
					.add("x", player.getX()).add("y", player.getY())));

		}

		ball = PlayerTimer.getBall();
		if (ball != null) {
			json.add("ball",
					Json.createArrayBuilder()
							.add(Json.createObjectBuilder().add("x", ball.location.x).add("y", ball.location.y)
									.add("vx", ball.Vx).add("vy", ball.Vy).add("width", ball.width)
									.add("heigth", ball.heigth)));
		}

		json.add("type", "join");
		final String jsonStr = json.build().toString();
		PlayerTimer.broadcast(jsonStr);
		// PlayerTimer.broadcast(String.format("{'type':'join','data':[%s]}",sb.toString()));
		sessions.add(sessions.size(), session);
	}

//	@Override
//	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//		PlayerTimer.broadcast(String.)
//	}
}
