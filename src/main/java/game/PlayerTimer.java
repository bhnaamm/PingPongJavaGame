package game;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;



import java.util.Timer;
import java.util.TimerTask;

public class PlayerTimer {

	private static final Log log = LogFactory.getLog(PlayerTimer.class);
	private static Timer gameTimer = null;
	private static final long DELAY = 100;
	private static JsonObjectBuilder json = Json.createObjectBuilder();
	private static JsonArrayBuilder jsonArray = Json.createArrayBuilder();
	
	private static final ConcurrentHashMap<Integer, Player> players = new ConcurrentHashMap<Integer, Player>();
	private static Ball ball;

	public static synchronized void addPlayer(Player player) {
		if(players.size()==0)
			startTimer();
//		if(players.size()==2) return;
		if(player != null)
		players.put(Integer.valueOf(player.getId()), player);
	}
	
	public static synchronized void addBall(Ball bal) {

		ball = bal;
	}
	
	public static Collection<Player> getPlayers(){
		return Collections.unmodifiableCollection(players.values());
	}
	
	public static Ball getBall(){
		return ball;
	}
	
	public static void tick() throws Exception {
		
		
		
		
		ball = PlayerTimer.getBall();
		
		if(ball != null) {
  			ball.Update();
			json
			  .add("ball", Json.createArrayBuilder()
			    .add(Json.createObjectBuilder()
    				.add("x", ball.location.x)
    				.add("y", ball.location.y)
    				.add("vx", ball.Vx)
    				.add("vy", ball.Vy)
    				.add("width", ball.width)
    				.add("heigth", ball.heigth)));
			}

		
		for (Iterator<Player> iterator = PlayerTimer.getPlayers().iterator(); iterator
				.hasNext();) {
			Player player = iterator.next();
			player.update();
			 jsonArray
			    .add(Json.createObjectBuilder()
			      .add("id", player.getId())
			      .add("x", player.location.x)
			      .add("y", player.location.y));

//			}
		}
		json.add("players",jsonArray);
		json.add("type", "update");
		final String jsonStr = json.build().toString();

		broadcast(jsonStr);
//		broadcast(String.format("{'type': 'update', 'data' : [%s]}", sb.toString()));
	}
	
	public static void broadcast(String message) throws Exception {
		Collection<Player> players = new CopyOnWriteArrayList<>(PlayerTimer.getPlayers());
		for (Player player : players) {
			try {
				player.sendMessage(message);
			}
			catch (Throwable ex) {
				
			}
		}
//		Ball ball = PlayerTimer.getBall();
//		ball.sendMessage(message);
		
	}
	
	public static void startTimer() {
		gameTimer = new Timer(PlayerTimer.class.getSimpleName() + " Timer");
		gameTimer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {
				try {
					tick();
				}
				catch (Throwable ex) {
					log.error("Error: ", ex);
				}
				
			}
		
		}, DELAY, DELAY);}
		
		
}
