package com.crazy.scientist.crazyjavascientist.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.util.FileSize;
import lombok.ToString;
import lombok.experimental.Delegate;

import java.util.Calendar;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@ToString
public class WriteLimitedDelegatingEncoder implements Encoder<ILoggingEvent> {

    private interface Encode {
        byte[] encode(ILoggingEvent event);
    }

    @Delegate(excludes = Encode.class)
    private final Encoder<ILoggingEvent> encoder;
    private final String appdenderName;
    private final long maxBytesToWrite;
    private final String windowStr;
    private volatile long windowStart;
    private volatile long windowDuration;
    private final AtomicLong numBytesEncoded = new AtomicLong(0);
    private final AtomicBoolean printedTooMuchLogged = new AtomicBoolean(false);

    public WriteLimitedDelegatingEncoder(Encoder<ILoggingEvent> encoder, String writeLimit, String appdenderName){

        this.encoder = encoder;
        this.appdenderName = appdenderName;

        String[] tokens = writeLimit.split("every");
        this.maxBytesToWrite = FileSize.valueOf(tokens[0].trim()).getSize();
        this.windowStr = tokens[1].trim();
        reset();
    }

    @Override
    public byte[] encode(ILoggingEvent event){
        if(writtenTooMuch()){
            if(!printedTooMuchLogged.getAndSet(true)){
                long delta = windowStart + windowDuration - System.currentTimeMillis();
                String msg = String.format("Appender %s has written %s bytes which is more than its configured %s bytes. You must wait another %s minutes for logs to print again!",appdenderName,numBytesEncoded.get(),maxBytesToWrite,delta/1000/60);
                StaticUtil.printMessage(msg);
            }
            return null;
        }

        final byte[] result = encoder.encode(event);
        numBytesEncoded.addAndGet(result.length);
        return result;
    }

    synchronized boolean writtenTooMuch(){
        long now = System.currentTimeMillis();

        if(now - windowStart > windowDuration){
            reset();
        }

        return numBytesEncoded.get() >= maxBytesToWrite;
    }

    private void reset(){
        Calendar c = Calendar.getInstance();

        if("day".equalsIgnoreCase(windowStr)){
            c.set(Calendar.HOUR_OF_DAY,0);
            c.set(Calendar.MINUTE,0);
            c.set(Calendar.SECOND,0);
            c.set(Calendar.MILLISECOND,0);
            windowDuration = 24 * 60 * 60_000;
        }else if("hour".equalsIgnoreCase(windowStr)){
            c.set(Calendar.MINUTE,0);
            c.set(Calendar.SECOND,0);
            c.set(Calendar.MILLISECOND,0);
            windowDuration = 60 * 60_000;
        }else if("minute".equalsIgnoreCase(windowStr)){
            c.set(Calendar.SECOND,0);
            c.set(Calendar.MILLISECOND,0);
            windowDuration = 60_000;
        }else{
            throw new RuntimeException("Missing the portion of this attribute: must be like '3GB every day'");
        }

        windowStart = c.getTimeInMillis();
        numBytesEncoded.set(0);
        printedTooMuchLogged.set(false);
    }

}
