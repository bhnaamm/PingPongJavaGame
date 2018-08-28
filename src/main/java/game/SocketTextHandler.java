package game;

import java.util.Iterator;
import java.util.List;

import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import org.springframework.stereotype.Component;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import game.Location;
import game.Player;

@Component
public class SocketTextHandler extends TextWebSocketHandler {

	List<WebSocketSession> sessions = new CopyOnWriteArrayList<WebSocketSession>();

	private static final AtomicInteger playerIds = new AtomicInteger(0);
	private static final Random random = new Random();
	private JsonObjectBuilder json = Json.createObjectBuilder();
	private JsonArrayBuilder jsonArray = Json.createArrayBuilder();
	private int id;
	private Player player;

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

		Player[] players = PlayerTimer.getPlayers().toArray(new Player[PlayerTimer.getPlayers().size()]);

		String payloadmessage = message.getPayload();
		String payload;
		String[] postfixes;
		if(payloadmessage.contains(", type:")) {
			postfixes = payloadmessage.split(", type:");
			payload = postfixes[1];
		}else {
			payload = payloadmessage;
		}
		

		String jsonStr = "";
		
		switch (payload) {

		case "play":
			this.player = (PlayerTimer.getPlayers().size() == 0) ? new Player(this.id, session, 1)
					: (PlayerTimer.getPlayers().size() == 1) ? new Player(this.id, session, 2) : null;
			
			
			if (this.player != null)
				PlayerTimer.addPlayer(this.player);

			if (PlayerTimer.getPlayers().size() == 2) {
				this.ball = new Ball(session);
				PlayerTimer.addBall(this.ball);
			}
			for (Iterator<Player> iterator = PlayerTimer.getPlayers().iterator(); iterator.hasNext();) {

				Player player = iterator.next();
				if (player != null)
					jsonArray.add(Json.createObjectBuilder()
							.add("id", player.getId()).add("x", player.location.x).add("y", player.location.y));

			}
			if (this.ball != null) {
				json.add("ball",
						Json.createArrayBuilder()
								.add(Json.createObjectBuilder().add("x", ball.location.x).add("y", ball.location.y)
										.add("vx", ball.Vx).add("vy", ball.Vy).add("width", ball.width)
										.add("heigth", ball.heigth)));
			}
			json.add("players", jsonArray);
			json.add("type", "join");
			jsonStr = json.build().toString();
			PlayerTimer.broadcast(jsonStr);
			break;
		case "playupdate1":
			this.player = players[0];
			this.player.update();
			jsonArray.add(Json.createObjectBuilder()
					.add("id", this.player.getId()).add("x", player.location.x).add("y", player.location.y));
			json.add("type", "update");
			json.add("players", jsonArray);
			jsonStr = json.build().toString();
			PlayerTimer.broadcast(jsonStr);
			break;
		case "playupdate2":
			this.player = players[1];
			this.player.update();
			jsonArray.add(Json.createObjectBuilder()
					.add("id", this.player.getId()).add("x", player.location.x).add("y", player.location.y));
			json.add("type", "update");
			json.add("players", jsonArray);
			jsonStr = json.build().toString();
			PlayerTimer.broadcast(jsonStr);
			break;
		case "ballupdate":
			this.ball = PlayerTimer.getBall();
			this.ball.Update();
				jsonArray
							.add(Json.createObjectBuilder().add("x", ball.location.x).add("y", ball.location.y)
									.add("vx", ball.Vx).add("vy", ball.Vy).add("width", ball.width)
									.add("heigth", ball.heigth));
			json.add("ball", jsonArray);
			json.add("type", "ballupdate");
			jsonStr = json.build().toString();
			PlayerTimer.broadcast(jsonStr);
			break;
		case "up":
			this.player.setDirection(Direction.UP);
			break;
		case "down":
			this.player.setDirection(Direction.DOWN);
			break;
		}

	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {

//		PlayerTimer.broadcast(String.format("{'type':'join','data':[%s]}",sb.toString()));
		sessions.add(sessions.size(), session);
	}

//	@Override
//	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
//		PlayerTimer.broadcast(String.)
//	}
}
