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
	
	public int type;


	public WebSocketSession getSession() {
		return session;
	}

	private String name;
	public Location location;
	private int score;
	


	private int width;

	public Player(int id, WebSocketSession session,int type) {
		this.id = id;
		this.type = type;
		this.score = 0;
		this.session = session;
		this.location = new Location();
		this.location.y = Constants.GAME_H / 2; // temporary solution
		this.location.x = (type==2)? 5:503;
		this.width = Constants.DEFAULT_WIDTH;
		this.heigth = Constants.DEFAULT_HEIGHT;

	}

	public synchronized void update() throws Exception {
		if (this.direction == null)
			return;

		Location nextLocation = this.location.getNextLocation(this.direction);
		final JsonObjectBuilder json = Json.createObjectBuilder();

		nextLocation.y = (this.direction == Direction.DOWN)
				? Math.min(Constants.GAME_H - Constants.DEFAULT_HEIGHT, this.location.y + Constants.STEP_SIZE)
				: (this.direction == Direction.UP) ? Math.max(0, this.location.y - Constants.STEP_SIZE) : this.location.y;
		this.location.y = nextLocation.y;
		
		json.add("players", Json.createArrayBuilder()
				.add(Json.createObjectBuilder().add("id", this.id).add("x", this.location.x).add("y", this.location.y)));

		json.add("type", "update");
		final String jsonStr = json.build().toString();
		try {
			sendMessage(jsonStr);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private synchronized void giveScore() throws Exception {
		this.score++;
		sendMessage("{'type':'score'}");
	}

	protected void sendMessage(String msg) throws Exception {
		if(session.isOpen())
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
