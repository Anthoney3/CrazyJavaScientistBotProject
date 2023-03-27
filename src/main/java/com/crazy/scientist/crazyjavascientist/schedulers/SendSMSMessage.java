package com.crazy.scientist.crazyjavascientist.schedulers;

import com.crazy.scientist.crazyjavascientist.security.EncryptorAESGCM;
import com.crazy.scientist.crazyjavascientist.utils.models.PhoneNumbers;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Calendar;

import static com.crazy.scientist.crazyjavascientist.config.AppConfig.auth_info;

@Slf4j
@Component
public class SendSMSMessage {


    private final EncryptorAESGCM encryptorAESGCM;
    private final ApplicationContext context;

    public SendSMSMessage(EncryptorAESGCM encryptorAESGCM, ApplicationContext context) {
        this.encryptorAESGCM = encryptorAESGCM;
        this.context = context;
    }


    private final String msgToSend = "This is a Friendly Reminder to update your timesheet for the month!";

    @Scheduled(cron = "${sms.w2w.reminder.cron.job}")
    public void sendTextWhenReadyForW2WSubmission() throws Exception {

        String aes_info = (String) context.getBean("aesAuthInfo");
        Calendar calendar = Calendar.getInstance();

        if (calendar.get(Calendar.DAY_OF_MONTH) <= 7 && calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            Twilio.init(encryptorAESGCM.decrypt(auth_info.get("TWILIO_SID"), aes_info), encryptorAESGCM.decrypt(auth_info.get("TWILIO_AUTH_ID"), aes_info));
            Message w2wAnthonyMessage = Message.creator(new PhoneNumber(PhoneNumbers.ANTHONY.getPhoneNumber()), new PhoneNumber(PhoneNumbers.TWILIO.getPhoneNumber()), msgToSend).setStatusCallback(URI.create("http://209.127.178.46/api/message-status")).create();
            log.info("Message with body: \"{}\" sent to {} at {} Successfully", w2wAnthonyMessage.getBody(), w2wAnthonyMessage.getTo(), w2wAnthonyMessage.getDateCreated());
            Message w2wZachMessage = Message.creator(new PhoneNumber(PhoneNumbers.ZACH.getPhoneNumber()), new PhoneNumber(PhoneNumbers.TWILIO.getPhoneNumber()), msgToSend).setStatusCallback(URI.create("http://209.127.178.46/api/message-status")).create();
            log.info("Message with body: \"{}\" sent to {} at {} Successfully", w2wZachMessage.getBody(), w2wZachMessage.getTo(), w2wZachMessage.getDateCreated());
        }
    }
}
