package com.crazy.scientist.crazyjavascientist;

import com.crazy.scientist.crazyjavascientist.config.DiscordBotConfigJDAStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.security.auth.login.LoginException;
import java.io.IOException;

@SpringBootApplication
public class CrazyJavaScientistApplication {




    public static void main(String[] args) throws LoginException, IOException {


        SpringApplication.run(CrazyJavaScientistApplication.class,args).getBean(DiscordBotConfigJDAStyle.class).init();


    }



}
