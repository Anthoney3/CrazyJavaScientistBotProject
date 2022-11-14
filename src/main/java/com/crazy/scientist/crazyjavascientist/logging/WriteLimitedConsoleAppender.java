package com.crazy.scientist.crazyjavascientist.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.Encoder;
import lombok.Getter;
import lombok.Setter;


public class WriteLimitedConsoleAppender extends ConsoleAppender<ILoggingEvent> {

    @Getter @Setter
    private String writeLimit;

    @Override
    public void setEncoder(Encoder<ILoggingEvent> encoder){

        if(writeLimit == null || writeLimit.trim().isEmpty()){
            super.setEncoder(encoder);
        }else{
            WriteLimitedDelegatingEncoder delegatingEncoder =
                    new WriteLimitedDelegatingEncoder(encoder, writeLimit.trim(), getName());

            super.setEncoder(delegatingEncoder);
            String msg = String.format("Appender %s will use %s",getName(),delegatingEncoder);
            StaticUtil.printMessage(msg);
        }
    }

}
