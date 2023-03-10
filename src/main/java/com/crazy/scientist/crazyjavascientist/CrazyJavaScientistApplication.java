package com.crazy.scientist.crazyjavascientist;

import com.crazy.scientist.crazyjavascientist.config.DiscordBotConfigJDAStyle;
import lombok.extern.slf4j.Slf4j;
import org.springframework.asm.Opcodes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
//@EnableJpaRepositories
@SpringBootApplication
@Slf4j
public class CrazyJavaScientistApplication implements Opcodes {

  public static void main(String[] args) throws Exception {
    SpringApplication
            .run(CrazyJavaScientistApplication.class, args)
            .getBean(DiscordBotConfigJDAStyle.class)
            .init();

  }
}
