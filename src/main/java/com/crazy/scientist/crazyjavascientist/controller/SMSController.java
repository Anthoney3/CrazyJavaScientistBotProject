package com.crazy.scientist.crazyjavascientist.controller;

import com.twilio.http.Request;
import com.twilio.rest.api.v2010.account.Message;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api")
public class SMSController {

    @PostMapping(value = "/message-status", consumes = "application/x-www-form-urlencoded")
    public void getSMSMessageStatus(@RequestBody String body){

        log.info("MSG Status Received: {}",body);

    }

    @GetMapping(value = "/test")
    public String testEndpoint(){

        return "This is a Test Message to see if the controller is working as intended";
    }


}
