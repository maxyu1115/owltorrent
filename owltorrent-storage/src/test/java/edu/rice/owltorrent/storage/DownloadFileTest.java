package edu.rice.owltorrent.storage;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.junit.Test;

public class DownloadFileTest {

  /**
   * A basic test that simply writes out the piece number to every piece in a test file. We choose
   * the number of bytes, block size, and piece size to be not be nice numbers and not divide evenly
   * into each other to ensure the implementation can handle that.
   */
  @Test
  public void writingToDiskWorks()
      throws IOException, DownloadFile.IllegalByteOffsets, DownloadFile.FileAlreadyExistsException,
          DownloadFile.FileCouldNotBeCreatedException {
    String testFileName = "writingToDiskTestFile";
    Path testFilePath = Paths.get(testFileName);
    int numBytes = 193;
    int pieceSize = 41;
    int blockSize = 11;

    // Delete the file if it already exists
    try {
      Files.delete(testFilePath);
    } catch (Exception ignored) {
    }

    // Write to the file so every byte equals its corresponding piece number
    DownloadFile fileToWriteTo = new DownloadFile(testFileName, numBytes, pieceSize);
    for (int piece = 0; piece < numBytes / pieceSize + 1; piece++) {
      int startOffset = piece * pieceSize;
      for (int blockOffset = 0;
          startOffset + blockOffset < numBytes && blockOffset < pieceSize;
          blockOffset += blockSize) {
        int currentBlockSize =
            Math.min(
                blockSize, Math.min(pieceSize - blockOffset, numBytes - blockOffset - startOffset));
        byte[] blockData = new byte[currentBlockSize];
        Arrays.fill(blockData, (byte) piece);
        fileToWriteTo.writeBlock(piece, blockOffset, blockData);
      }
    }

    // Read the file back and make sure it is as expected
    byte[] fileContent = Files.readAllBytes(testFilePath);
    for (int i = 0; i < fileContent.length; i++) {
      assertEquals(i / pieceSize, fileContent[i]);
    }

    // Delete the file to clean up
    try {
      Files.delete(Paths.get(testFileName));
    } catch (Exception ignored) {
    }
  }
}
