package edu.rice.owltorrent.network.socketconnector;

import edu.rice.owltorrent.common.adapters.TaskExecutor;
import edu.rice.owltorrent.common.adapters.TaskExecutor.LongRunningTask;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.TwentyByteId;
import edu.rice.owltorrent.network.MessageHandler;
import edu.rice.owltorrent.network.MessageReader;
import edu.rice.owltorrent.network.PeerConnector;
import edu.rice.owltorrent.network.PeerMessage;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

/**
 * Socket implementation of PeerConnector class.
 *
 * @author Lorraine Lyu, Max Yu
 */
@Log4j2(topic = "network")
class SocketConnector extends PeerConnector {
  private final Socket peerSocket;
  private final TaskExecutor taskExecutor;

  private boolean initiated = false;

  private DataOutputStream out;
  private ReadableByteChannel in;
  @Getter private final Queue<PeerMessage> outQueue = new ConcurrentLinkedQueue<>();

  private final LongRunningTask listenForInput =
      new LongRunningTask() {
        @Override
        public void run() {
          while (true) {
            try {
              handleMessage(in);
            } catch (InterruptedException e) {
              log.info(e);

              try {
                close();
              } catch (Exception exception) {
                log.error("Could not close: ", exception);
              }
              return;
            }
          }
        }
      };

  private Future<Void> listenerThread;

  private SocketConnector(
      TwentyByteId ourPeerId,
      Peer peer,
      MessageHandler handler,
      TaskExecutor taskExecutor,
      Socket peerSocket) {
    super(ourPeerId, peer, handler);
    this.taskExecutor = taskExecutor;
    this.peerSocket = peerSocket;
  }

  public static SocketConnector makeInitialConnection(
      TwentyByteId ourPeerId, Peer peer, MessageHandler handler, TaskExecutor taskExecutor)
      throws IOException {
    return new SocketConnector(
        ourPeerId,
        peer,
        handler,
        taskExecutor,
        new Socket(peer.getAddress().getAddress(), peer.getAddress().getPort()));
  }

  static SocketConnector makeRespondingConnection(
      TwentyByteId ourPeerId,
      Peer peer,
      MessageHandler handler,
      TaskExecutor taskExecutor,
      Socket peerSocket) {
    return new SocketConnector(ourPeerId, peer, handler, taskExecutor, peerSocket);
  }

  @Override
  public void initiateConnection() throws IOException {
    if (initiated) {
      return;
    }
    this.in = Channels.newChannel(peerSocket.getInputStream());
    this.out = new DataOutputStream(peerSocket.getOutputStream());

    this.out.write(PeerMessage.constructHandShakeMessage(this.peer.getTorrent(), ourPeerId));

    // read and confirm handshake from peer
    if (!MessageReader.handShake(in, peer)) {
      throw new IOException(
          String.format("Invalid handshake from peer id=%s", this.peer.getPeerID()));
    }
    // listen for input with busy waiting
    listenerThread = taskExecutor.submitLongRunningTask(listenForInput);
    initiated = true;
  }

  @Override
  public void respondToConnection() throws IOException {
    if (initiated) {
      return;
    }
    this.in = Channels.newChannel(peerSocket.getInputStream());
    this.out = new DataOutputStream(peerSocket.getOutputStream());

    this.out.write(PeerMessage.constructHandShakeMessage(this.peer.getTorrent(), ourPeerId));
    // listen for input with busy waiting
    listenerThread = taskExecutor.submitLongRunningTask(listenForInput);
    initiated = true;
  }

  @Override
  public void sendMessage(PeerMessage message) {
    log.info("Writing: {}", message);
    outQueue.add(message);
    taskExecutor.submitTask(new SocketWriteMsgTask(this));
  }

  void writeMessage(PeerMessage message) throws IOException {
    log.info("Attempting to send msg: " + message);
    out.write(message.toBytes());
    log.info("Sent msg: " + message);
  }

  @Override
  public void close() throws Exception {
    if (listenerThread != null) {
      listenerThread.cancel(true);
    }
    peerSocket.close();
  }
}