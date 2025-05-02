package com.bot.slack.cab;

import com.bot.slack.cab.model.BotReq;
import com.bot.slack.cab.model.ServiceDeployment;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
