package edu.rice.owltorrent.network.messages;

import static org.junit.Assert.assertEquals;

import edu.rice.owltorrent.network.PeerMessage;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Contains helper methods for testing the PeerMessage implementation.
 *
 * @author Josh
 */
class GenericMessageTestHelper {

  /**
   * This is a property based test helper method that converts the given message to binary and back
   * and asserts that the final message equals the initial message.
   */
  static void binaryAndBackWorks(PeerMessage message) throws IOException {
    byte[] messageBytes = message.toBytes();
    PeerMessage message_from_bytes = PeerMessage.parse(ByteBuffer.wrap(messageBytes));
    assertEquals(message, message_from_bytes);
  }
}
