package com.crazy.scientist.crazyjavascientist.utils;

import com.crazy.scientist.crazyjavascientist.utils.models.PhoneNumbers;
import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;
import io.github.cdimascio.dotenv.Dotenv;
import com.twilio.rest.api.v2010.account.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;

@Slf4j
@Component
public class SendSMSMessage {

    private final static Dotenv smsConfig = Dotenv.configure().load();
    private final String msgToSend = "This is a Friendly Reminder to update your timesheet for the month!";
    private final String cteReminderMsg = "This is a Friendly Reminder to ensure CTE Env is Shutdown Using the CLI Tool!";

    @Scheduled(cron = "0 0 8 1 * *")
    public void sendTextWhenReadyForW2WSubmission(){
        Twilio.init(smsConfig.get("TWILIO_SID"), smsConfig.get("TWILIO_AUTH_ID"));
        Message w2wAnthonyMessage = Message.creator(new PhoneNumber(PhoneNumbers.ANTHONY.getPhoneNumber()),new PhoneNumber(PhoneNumbers.TWILIO.getPhoneNumber()),msgToSend).setStatusCallback(URI.create("https://209.127.178.46/api/message-status")).create();
        log.info("Message with body: \"{}\" sent to {} at {} Successfully",w2wAnthonyMessage.getBody(), w2wAnthonyMessage.getFrom(),w2wAnthonyMessage.getDateCreated());
        Message w2wZachMessage = Message.creator(new PhoneNumber(PhoneNumbers.ZACH.getPhoneNumber()),new PhoneNumber(PhoneNumbers.TWILIO.getPhoneNumber()),msgToSend).create();
        log.info("Message with body: \"{}\" sent to {} at {} Successfully",w2wZachMessage.getBody(), w2wZachMessage.getFrom(),w2wZachMessage.getDateCreated());

    }


    @Scheduled(cron = "0 0 8 * * *")
    public void reminderForCTE(){
        Twilio.init(smsConfig.get("TWILIO_SID"), smsConfig.get("TWILIO_AUTH_ID"));
        Message w2wAnthonyMessage = Message.creator(new PhoneNumber(PhoneNumbers.ANTHONY.getPhoneNumber()),new PhoneNumber(PhoneNumbers.TWILIO.getPhoneNumber()),cteReminderMsg).create();
        log.info("Message with body: \"{}\" sent to {} at {} Successfully",w2wAnthonyMessage.getBody(), w2wAnthonyMessage.getFrom(),w2wAnthonyMessage.getDateCreated());

    }


}
