package edu.rice.owltorrent.common.util;

public class Exceptions {

  public static class FileAlreadyExistsException extends Exception {}

  public static class FileCouldNotBeCreatedException extends Exception {}

  public static class IllegalByteOffsets extends Exception {

    public IllegalByteOffsets(String message) {
      super(message);
    }
  }
}
