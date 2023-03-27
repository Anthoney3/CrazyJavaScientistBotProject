package com.crazy.scientist.crazyjavascientist;

import com.crazy.scientist.crazyjavascientist.commands.dnd.dnd_entities.DNDPlayersEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.asm.Opcodes;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@EnableScheduling
//@EnableJpaRepositories
@SpringBootApplication
@Slf4j
public class CrazyJavaScientistApplication implements Opcodes {


  public static void main(String[] args) throws Exception {
    SpringApplication.run(CrazyJavaScientistApplication.class, args);

    if (!new File("./logs/cjs.log").exists()) {
      log.info("Log Directory Not Found...Attempting to create new log directory");
      log.info("{}", (new File("./logs").mkdir()) ? "New Log Directory Created Successfully" : "Log Directory Creation Failed");
    }
  }
}
