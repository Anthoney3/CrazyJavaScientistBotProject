package com.crazy.scientist.crazyjavascientist.logging;

import ch.qos.logback.core.PropertyDefinerBase;

public class ShortHostName extends PropertyDefinerBase {

    @Override
    public String getPropertyValue(){
        String logbackHostname = getContext().getProperty("HOSTNAME");

        if(logbackHostname == null){
            StaticUtil.printMessage("couldn't determine hostname for logback");
            return "hostname_NA";
        }else {
            StaticUtil.printMessage("logback set the HOSTNAME. using the first token of " + logbackHostname);
            return logbackHostname.split("\\.")[0];
        }
    }
}
