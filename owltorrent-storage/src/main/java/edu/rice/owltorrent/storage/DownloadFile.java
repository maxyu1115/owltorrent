package edu.rice.owltorrent.storage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Josh Represents the local disk version of a file that a client is downloading from the
 *     BitTorrent network.
 */
public class DownloadFile {

  public static class FileAlreadyExistsException extends Exception {}

  public static class FileCouldNotBeCreatedException extends Exception {}

  public static class IllegalByteOffsets extends Exception {}

  private long pieceSize;
  private long numBytes;
  private RandomAccessFile file;

  /**
   * Creates a new DownloadFile object that abstracts the process of writing data to disk.
   *
   * @param filePath The name of the target file we will write the data we receive to
   * @param numBytes The exact number of bytes in the file
   * @param pieceSize The piece size in bytes, see the BitTorrent specification
   *     https://www.bittorrent.org/beps/bep_0003.html
   * @throws FileAlreadyExistsException Thrown if the file we are trying to write to already exists
   * @throws FileCouldNotBeCreatedException Thrown if the file we plan to write to could not be
   *     created
   */
  public DownloadFile(String filePath, long numBytes, long pieceSize)
      throws FileAlreadyExistsException, FileCouldNotBeCreatedException, IllegalByteOffsets {
    if (numBytes < 0 || pieceSize < 0) {
      throw new IllegalByteOffsets();
    }

    this.pieceSize = pieceSize;
    this.numBytes = numBytes;

    if (fileExists(filePath)) {
      throw new FileAlreadyExistsException();
    }

    // Note that technically this file could have been created between the last line and here, but
    // this
    // is unlikely enough (and would have a small enough effect even if it did happen) that we
    // ignore it.
    try {
      this.file = new RandomAccessFile(filePath, "rw");
    } catch (FileNotFoundException e) {
      throw new FileCouldNotBeCreatedException();
    }

    // See https://stackoverflow.com/questions/245251/create-file-with-given-size-in-java
    try {
      file.setLength(numBytes);
    } catch (IOException e) {
      throw new FileCouldNotBeCreatedException();
    }
  }

  /**
   * Writes a block of bytes to the file. Currently, this method is synchronized so that there is
   * not competition over the file object. If this becomes a bottleneck we can have a pool of file
   * objects that are shared, or buffer writes in a clever way that prevents the disk head from
   * having to seek back and forth over the file (likely the network download speed will be the
   * actual bottleneck, especially with an SSD).
   *
   * @param pieceNum The piece number the block of data is from
   * @param offset The offset in bytes of the block within the piece
   * @param blockData The bytes to write to the file
   * @throws IllegalByteOffsets Thrown if the arguments entail writing bytes to an illegal position.
   * @throws IOException If writing to the file failed
   */
  public synchronized void writeBlock(long pieceNum, long offset, byte[] blockData)
      throws IllegalByteOffsets, IOException {
    long startOffset = pieceNum * pieceSize + offset;
    long endOffset = startOffset + blockData.length;
    boolean writingPastEndOfFile = endOffset > numBytes;
    boolean writingPastEndOfPiece = offset + blockData.length > pieceSize;
    boolean writingBeforeStartOfFile = startOffset < 0;
    if (writingPastEndOfFile || writingPastEndOfPiece || writingBeforeStartOfFile) {
      throw new IllegalByteOffsets();
    }

    file.seek(startOffset);
    file.write(blockData);
  }

  /**
   * Lets the object know that we are done writing to the file, and any file descriptors can be
   * closed.
   *
   * @throws IOException If closing the file fails
   */
  public void finishFile() throws IOException {
    file.close();
  }

  /** Returns whether the input file currently exists */
  private boolean fileExists(String filePath) {
    return new File(filePath).isFile();
  }
}
