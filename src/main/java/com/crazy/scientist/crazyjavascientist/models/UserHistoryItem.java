package com.crazy.scientist.crazyjavascientist.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserHistoryItem {

    private String userName;
    private String searchPrompt;

}
