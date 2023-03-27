package com.crazy.scientist.crazyjavascientist.exceptions;

public class DNDException extends Exception {

  public DNDException(String message, Throwable cause) {
    super(message, cause);
  }
  public DNDException(String message){
    super(message);
  }
}
