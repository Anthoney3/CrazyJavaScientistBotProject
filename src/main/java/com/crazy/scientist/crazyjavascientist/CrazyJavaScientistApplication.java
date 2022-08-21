package com.crazy.scientist.crazyjavascientist;

import com.crazy.scientist.crazyjavascientist.config.DiscordBotConfigJDAStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CrazyJavaScientistApplication {



    public static void main(String[] args) {
        SpringApplication.run(CrazyJavaScientistApplication.class, args);

        try {
             new DiscordBotConfigJDAStyle();
        }catch (Exception e){
            e.printStackTrace();
        }

    }

}
