package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Socket implementation of PeerConnector class.
 *
 * @author Lorraine Lyu, Max Yu
 */
public class SocketConnector extends PeerConnector {
  private final Socket peerSocket;
  private DataOutputStream out;
  private DataInputStream in;

  public SocketConnector(Peer peer) throws IOException {
    super(peer);
    peerSocket = new Socket(peer.getAddress().getAddress(), peer.getAddress().getPort());
  }

  @Override
  public void connect() throws IOException {
    peerSocket.connect(peer.getAddress());
    this.in = new DataInputStream(peerSocket.getInputStream());
    this.out = new DataOutputStream(peerSocket.getOutputStream());
    // TODO: handshake
  }

  @Override
  public void writeMessage(PeerMessage message) throws IOException {
    out.write(message.toBytes());
  }
}
