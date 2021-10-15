package edu.rice.owltorrent.storage;

import static java.lang.Math.min;
import static org.junit.Assert.assertEquals;

import edu.rice.owltorrent.common.entity.FileBlock;
import edu.rice.owltorrent.common.entity.FileBlockInfo;
import edu.rice.owltorrent.common.util.Exceptions;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.Test;

/** @author Josh */
public class DiskFileTest {

  /**
   * A basic test that simply writes out the piece number to every piece in a test file. We choose
   * the number of bytes, block size, and piece size to be not be nice numbers and not divide evenly
   * into each other to ensure the implementation can handle that. Finally, the test reads back the
   * file into a byte array and ensures it equals the expected value.
   */
  @Test
  public void writingToDiskWorks()
      throws IOException, Exceptions.IllegalByteOffsets, Exceptions.FileAlreadyExistsException,
          Exceptions.FileCouldNotBeCreatedException {
    String testFileName = "writingToDiskTestFile";
    Path testFilePath = Paths.get(testFileName);
    int numBytes = 193;
    int pieceSize = 41;
    int blockSize = 11;

    // Delete the file if it already exists. Ignore exceptions because we do not care if it does not
    // exist.
    try {
      Files.delete(testFilePath);
    } catch (Exception ignored) {
    }

    // Write to the file so every byte equals its corresponding piece number
    DiskFile testFile = new DiskFile(testFileName, numBytes, pieceSize);
    for (int piece = 0; piece < numBytes / pieceSize + 1; piece++) {
      int startOffset = piece * pieceSize;
      for (int blockOffset = 0;
          startOffset + blockOffset < numBytes && blockOffset < pieceSize;
          blockOffset += blockSize) {
        int currentBlockSize =
            min(blockSize, min(pieceSize - blockOffset, numBytes - blockOffset - startOffset));
        byte[] blockData = new byte[currentBlockSize];
        Arrays.fill(blockData, (byte) piece);
        testFile.writeBlock(new FileBlock(piece, blockOffset, blockData));
      }
    }

    // Read the file back piece by piece and make sure it is as expected
    for (int piece = 0; piece < numBytes / pieceSize + 1; piece++) {
      int pieceLength = min(pieceSize, numBytes - piece * pieceSize);
      byte[] fileBytes = testFile.readBlock(new FileBlockInfo(piece, 0, pieceLength));
      for (int i = 0; i < pieceLength; i++) {
        assertEquals(piece, fileBytes[i]);
      }
    }

    // Close the file
    testFile.finishFile();

    // Delete the file to clean up
    Files.delete(Paths.get(testFileName));
  }
}
