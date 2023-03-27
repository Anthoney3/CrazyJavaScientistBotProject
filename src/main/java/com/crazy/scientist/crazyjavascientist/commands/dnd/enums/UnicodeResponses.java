package com.crazy.scientist.crazyjavascientist.commands.dnd.enums;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum UnicodeResponses {
  ATTENDING("\uD83C\uDF5E"),
  EXCUSED("\uD83D\uDE2D"),
  NO_SHOW_NO_RESPONSE("N/A");

  private String response;

  UnicodeResponses(String response) {
    this.response = response;
  }
}
