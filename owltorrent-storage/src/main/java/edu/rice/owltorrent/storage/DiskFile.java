package edu.rice.owltorrent.storage;

import edu.rice.owltorrent.common.entity.FileBlock;
import edu.rice.owltorrent.common.entity.FileBlockInfo;
import edu.rice.owltorrent.common.util.Exceptions;
import edu.rice.owltorrent.common.util.SHA1Encryptor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author Josh Represents the local disk version of a file that a client is downloading from the
 *     BitTorrent network.
 */
public class DiskFile {

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
   * @throws Exceptions.FileAlreadyExistsException Thrown if the file we are trying to write to
   *     already exists
   * @throws Exceptions.FileCouldNotBeCreatedException Thrown if the file we plan to write to could
   *     not be created
   */
  public DiskFile(String filePath, long numBytes, long pieceSize)
      throws Exceptions.FileAlreadyExistsException, Exceptions.FileCouldNotBeCreatedException,
          Exceptions.IllegalByteOffsets {
    if (numBytes < 0 || pieceSize < 0) {
      throw new Exceptions.IllegalByteOffsets(
          "Number of bytes (" + numBytes + ") or piece size (" + pieceSize + ") is less than 0.");
    }

    this.pieceSize = pieceSize;
    this.numBytes = numBytes;

    if (fileExists(filePath)) {
      throw new Exceptions.FileAlreadyExistsException();
    }

    // Note that technically this file could have been created between the last line and here, but
    // this
    // is unlikely enough (and would have a small enough effect even if it did happen) that we
    // ignore it.
    try {
      this.file = new RandomAccessFile(filePath, "rw");
    } catch (FileNotFoundException e) {
      throw new Exceptions.FileCouldNotBeCreatedException();
    }

    // See https://stackoverflow.com/questions/245251/create-file-with-given-size-in-java
    try {
      file.setLength(numBytes);
    } catch (IOException e) {
      throw new Exceptions.FileCouldNotBeCreatedException();
    }
  }

  /**
   * Writes a block of bytes to the file. Currently, this method is synchronized so that there is
   * not competition over the file object. If this becomes a bottleneck we can have a pool of file
   * objects that are shared, or buffer writes in a clever way that prevents the disk head from
   * having to seek back and forth over the file (likely the network download speed will be the
   * actual bottleneck, especially with an SSD).
   *
   * @param block The block to write to the file
   * @throws Exceptions.IllegalByteOffsets Thrown if the arguments entail writing bytes to an
   *     illegal position.
   * @throws IOException Thrown if writing to the file failed
   */
  public synchronized void writeBlock(FileBlock block)
      throws Exceptions.IllegalByteOffsets, IOException {
    int pieceNum = block.getPieceIndex();
    int offsetWithinPiece = block.getOffsetWithinPiece();
    byte[] data = block.getData();
    long fileOffset = calculateFileOffset(pieceNum, offsetWithinPiece);
    verifyOffset(fileOffset, offsetWithinPiece, data.length);
    file.seek(fileOffset);
    file.write(data);
  }

  /**
   * Read a block of bytes from the file. Currently, this method is synchronized so that there is
   * not competition over the file object; see the writeBlock method for what to do if this becomes
   * a bottleneck.
   *
   * @param blockInfo The block information describing the block to retrieve
   * @throws Exceptions.IllegalByteOffsets Thrown if the arguments entail reading bytes from an
   *     illegal position.
   * @throws IOException Thrown if reading from the file failed
   */
  public synchronized byte[] readBlock(FileBlockInfo blockInfo)
      throws Exceptions.IllegalByteOffsets, IOException {
    int pieceNum = blockInfo.getPieceIndex();
    int offsetWithinPiece = blockInfo.getOffsetWithinPiece();
    int blockLength = blockInfo.getLength();
    long fileOffset = calculateFileOffset(pieceNum, offsetWithinPiece);
    verifyOffset(fileOffset, offsetWithinPiece, blockLength);
    byte[] block = new byte[blockLength];
    file.seek(fileOffset);
    file.read(block);
    return block;
  }

  /**
   * Calculates an overall offset within the file given the piece number and the offset within the
   * piece
   */
  private long calculateFileOffset(long pieceNum, long offsetWithinPiece) {
    return pieceNum * pieceSize + offsetWithinPiece;
  }

  /** Throws an error if the given offset or blockLength are invalid */
  private void verifyOffset(long fileOffset, long pieceOffset, int blockLength)
      throws Exceptions.IllegalByteOffsets {
    boolean writingPastEndOfFile = fileOffset + blockLength > numBytes;
    boolean writingPastEndOfPiece = pieceOffset + blockLength > pieceSize;
    boolean writingBeforeStartOfFile = fileOffset < 0;

    if (writingPastEndOfFile) {
      throw new Exceptions.IllegalByteOffsets(
          "File offset "
              + fileOffset
              + " and block length "
              + blockLength
              + " is past the end of the file.");
    }
    if (writingPastEndOfPiece) {
      throw new Exceptions.IllegalByteOffsets(
          "Piece offset "
              + pieceOffset
              + " and block length "
              + blockLength
              + " is past the end of the piece.");
    }
    if (writingBeforeStartOfFile) {
      throw new Exceptions.IllegalByteOffsets(
          "File offset  " + fileOffset + "is before the start of the file.");
    }
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

  /**
   * Checks whether a piece on disk has a SHA1 hash equal to the passed in hash, returns true if
   * this is the case and false otherwise. Note that this will return false either if incorrect
   * (possible malicious or corrupted) data was written to this piece, or just if the piece has not
   * been entirely written to yet (the external user needs to manager and track which pieces are
   * completed, and call this method when they are).
   *
   * @param pieceNum The piece number to check
   * @param expectedHash The expected hash of the piece
   * @return True if the piece hash on disk is correct
   * @throws Exceptions.IllegalByteOffsets If the passed in piece is invalid
   * @throws IOException If seeking or reading from disk fails
   */
  public synchronized boolean pieceHashCorrect(long pieceNum, byte[] expectedHash)
      throws Exceptions.IllegalByteOffsets, IOException {
    long pieceByteStart = pieceNum * pieceSize;
    if (pieceByteStart < 0 || pieceByteStart >= numBytes) {
      throw new Exceptions.IllegalByteOffsets("Piece num is less than 0 or too large.");
    }

    int pieceLength = (int) Math.min(pieceSize, numBytes - pieceByteStart);
    byte[] actualBytes = new byte[pieceLength];
    file.seek(pieceByteStart);
    file.read(actualBytes);

    return SHA1Encryptor.isSHA1HashEqual(actualBytes, expectedHash);
  }

  /** Returns whether the input file currently exists */
  private boolean fileExists(String filePath) {
    return new File(filePath).isFile();
  }
}
