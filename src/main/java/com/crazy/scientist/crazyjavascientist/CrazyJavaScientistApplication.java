package com.crazy.scientist.crazyjavascientist;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import com.crazy.scientist.crazyjavascientist.config.DiscordBotConfigJDAStyle;
import com.crazy.scientist.crazyjavascientist.satisfactory.enums.Ingots;
import com.crazy.scientist.crazyjavascientist.satisfactory.enums.Resources;
import net.dv8tion.jda.api.entities.Webhook;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class CrazyJavaScientistApplication {



    public static void main(String[] args) {

        System.out.println(Ingots.IRON_INGOT.getValue() + " " + Ingots.IRON_INGOT.getResource1());



        SpringApplication.run(CrazyJavaScientistApplication.class, args);
      /*  try {
            new OAuthToken().getOsuOAuthToken();
        }catch (Exception e){

            e.printStackTrace();
        }*/



        try {
            new DiscordBotConfigJDAStyle();
        }catch (Exception e){
            e.printStackTrace();
        }




    }




}
