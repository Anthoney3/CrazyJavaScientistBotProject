package com.crazy.scientist.crazyjavascientist.logging;

import java.time.LocalDateTime;

public class StaticUtil {
    private StaticUtil(){}

    static void printMessage(String message){
        System.out.println(LocalDateTime.now() + " : " + message);
    }
}
