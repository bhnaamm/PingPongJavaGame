package game;






import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;



import org.springframework.web.socket.WebSocketHandler;

import org.springframework.web.socket.config.annotation.EnableWebSocket;

import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.PerConnectionWebSocketHandler;


@Configuration
@EnableAutoConfiguration
@EnableWebSocket
public class Application extends SpringBootServletInitializer implements WebSocketConfigurer {
	
	
	@Bean
	public WebSocketHandler socketTextHandler() {
		return new PerConnectionWebSocketHandler(SocketTextHandler.class);
	}

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(socketTextHandler(), "/game").setAllowedOrigins("*").withSockJS();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	
}


