package com.crazy.scientist.crazyjavascientist.utils.models;

import org.springframework.stereotype.Component;

public enum PhoneNumbers {
    ZACH("+17275659753"),
    ANTHONY("+17273178884"),
    TWILIO("+13148047651");

    private String  phoneNumber;

    PhoneNumbers(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }
}
