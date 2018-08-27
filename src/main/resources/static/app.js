var Game = {};

Game.fps = 30;
Game.socket = null;
Game.nextFrame = null;
Game.interval = null;
Game.direction = 'none';


Game.initialize = function() {

	canvas = document.getElementById("game");

	this.context = canvas.getContext("2d");

	this.context.fillStyle = "white";
	
	window.addEventListener('keydown', function(e) {
		var code = e.keyCode;
		switch (code) {
		case 38:
			Game.setDirection(1, 'up');
			break;
		case 87:
			Game.setDirection(2, 'up');
			break;
		case 40:
			Game.setDirection(1, 'down');
			break;
		case 83:
			Game.setDirection(2, 'down');
			break;
		}
	}, false);
	Game.connect();
}

Game.setDirection = function(player, direction) {
	if (player == 1)
		Game.player1.direction = direction;
	if (player == 2)
		Game.player2.direction = direction;
	Game.socket.send(direction);
}

Game.startGameLoop = function() {

	if (window.webkitRequestAnimationFrame) {
		Game.nextFrame = function() {
			webkitRequestAnimationFrame(Game.run);
		};
	} else if (window.mozRequestAnimationFrame) {
		Game.nextFrame = function() {
			mozRequestAnimationFrame(Game.run);
		};
	} else {
		Game.interval = setInterval(Game.run, 1000 / Game.fps);
	}

	if (Game.nextFrame != null)
		Game.nextFrame();
};

Game.stopGameLoop = function() {
	Game.nextFrame = null;
	if (Game.interval != null) {
		clearInterval(Game.interval);
	}
};

Game.draw = function() {
	
	this.context.clearRect(0, 0, 512, 256);
	this.context.fillRect(256, 0, 2, 256);
	if (this.player1){
		this.player1.draw(this.context);
		Game.socket.send(JSON.stringify(this.player1)+", type= play");
	}
	if (this.player2){
		this.player2.draw(this.context);
		Game.socket.send(JSON.stringify(this.player2)+", type= play");
	}
	if(typeof this.ball != 'undefined') {
		this.ball.draw(this.context);
		Game.socket.send(JSON.stringify(this.ball));
	}
	
	
};

Game.addPlayer = function(type, id) {

	switch (type) {
	case 1:
		this.player1 = new Player(id, 5, 114);
		
		Game.socket.send(JSON.stringify(this.player1)+', type: playupdate1');
		break;

	case 2:
		this.player2 = new Player(id, 512 - 7, 114);
		Game.socket.send(JSON.stringify(this.player2)+', type: playupdate2');
		break;
	default:
		break;
	}

}
Game.play = function(){
	// $("#play").prop("disabled",true);
	this.ball = new Ball();
	Game.draw();
	Game.socket.send("play");
}
Game.updateBall = function(ballData) {
	if(this.paused) return;
// this.ball = ballData[0];
	if(typeof ballData == 'undefined' || typeof this.ball == 'undefined') return;
	this.ball.x = ballData[0].x;
	this.ball.y = ballData[0].y;
	this.ball.vx = ballData[0].vx;
	this.ball.vy = ballData[0].vy;
	this.ball.width = ballData[0].width;
	this.ball.heigth = ballData[0].heigth;
	Game.socket.send(JSON.stringify(this.ball)+', type: ballupdate');
	Game.draw();
}

Game.updatePlayer = function(id, playerBody) {
	if (typeof this.player1 == "undefined"
			|| typeof this.player2 == "undefined") {
		return;
	}
	switch (id) {
	case 0:
		this.player1.y = playerBody.y;
		Game.socket.send(JSON.stringify(this.player1)+', type: playupdate1');
		Game.draw();
		break;
	case 1:
		this.player2.y = playerBody.y;
		Game.socket.send(JSON.stringify(this.player2)+', type: playupdate2');
		Game.draw();
		break;
	}
	
	
};

Game.run = (function() {
	var delayTicks = 1000 / Game.fps;
	var nextGameTick = (new Date).getTime();
	return function() {
		while ((new Date).getTime() > nextGameTick) {
			nextGameTick += delayTicks;
		}
		Game.draw();
		if (Game.nextFrame != null) {
			Game.nextFrame();
		}
	};
})();

Game.connect = (function() {
	Game.socket = new SockJS("/game");
	// Game.socket = new WebSocket("ws://localhost:8080/user");
	Game.socket.onopen = function() {
		Game.startGameLoop();
		setInterval(function() {
			Game.socket.send('ping');// keeps it alive
		}, 5000);
	};
	// Game.draw();
	Game.socket.onclose = function() {
		Game.stopGameLoop();
	};

	Game.socket.onmessage = function(message) {
		var packet = eval('(' + message.data.toString() + ')');

		Game.updateBall(packet.ball);
		switch (packet.type) {
		case 'update':
			var body = {};
			for (var i = 0; i < packet.players.length; i++) {
				body.x = packet.players[i].x;
				body.y = packet.players[i].y;
				Game.updatePlayer(packet.players[i].id, body);
			}
			break;
		case 'join':
			if (typeof Game.player1 == "undefined")
				Game.addPlayer(1, packet.players[0].id);
			if (typeof Game.player1 != "undefined"
					&& typeof Game.player2 == "undefined")
				Game.player2 = packet.players[0];
			Game.addPlayer(2, packet.players[0].id);
			break;
		case 'ballUpdate':
			Game.updateBall(packet.ball);
			break;
		default:
		}
	};
});

function Ball() {
	this.x = 10;
	this.y = 10;
	this.vx = 0;
	this.vy = 0;
	this.width = 4;
	this.height = 4;
};

Ball.prototype.draw = function(p) {
	p.fillRect(this.x, this.y, this.width, this.height);
};

function Player(id, x, y) {
	this.id = id;
	this.x = x;
	this.y = y;
	this.width = 4;
	this.height = 28;
	this.direction = 'NONE';
}

Player.prototype.draw = function(p) {
	p.fillRect(this.x, this.y, this.width, this.height);
};

$(function() {
	$("form").on('submit', function(e) {
		e.preventDefault();
	});
	$("#play").click(function() {
		Game.play();
	});
	$("#connect").click(function() {
		Game.initialize();
	});
});