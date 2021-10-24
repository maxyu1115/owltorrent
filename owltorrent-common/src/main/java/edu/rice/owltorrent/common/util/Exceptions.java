package edu.rice.owltorrent.common.util;

import lombok.Getter;

public class Exceptions {

  public static class FileAlreadyExistsException extends Exception {}

  public static class FileCouldNotBeCreatedException extends Exception {}

  public static class IllegalByteOffsets extends Exception {

    public IllegalByteOffsets(String message) {
      super(message);
    }
  }

  public static class ParsingTorrentFileFailedException extends Exception {}

  @Getter
  public static class BTException extends RuntimeException {
    public BTException(String message) {
      super(message);
    }
  }
}
