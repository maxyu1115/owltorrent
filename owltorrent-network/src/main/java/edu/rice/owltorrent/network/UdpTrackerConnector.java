package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;

/**
 * UDP tracker connector to fetch peers.
 *
 * @author bhaveshshah
 */
public class UdpTrackerConnector implements PeerLocator {

  // Connect parameters
  public static final long protocol_id = 0x41727101980L;
  public static final int action = 1;
  public static final int transaction_id = 12345;

  // Announce parameters
  public static final String peerID = "owltorrentclientpeer";
  public static final long left = 0;
  public static final long downloaded = 0;
  public static final long uploaded = 0;
  public static final int event = 0;
  public static final int ip_address = 0;
  public static final int key = 0;
  public static final int num_want = -1;

  /**
   * Determine which protocol to use and then retrieve peers accordingly
   *
   * @param torrent Torrent object
   * @return list of peers
   */
  public List<Peer> locatePeers(@NonNull Torrent torrent) {
    try {
      List<Peer> peers = locateWithUDPTracker(torrent);
      return peers;
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Retrieve peers from UDP tracker
   *
   * @param torrent Torrent object
   * @return list of peers
   */
  public List<Peer> locateWithUDPTracker(@NonNull Torrent torrent)
      throws IOException, UnknownHostException {
    List<Peer> peers = new ArrayList<>();

    // Create socket on any available port.
    DatagramSocket datagramSocket = new DatagramSocket();

    // Configure Inet address to connect to.
    var tracker = URI.create(torrent.getAnnounceURL());
    InetSocketAddress trackerAddress = new InetSocketAddress(tracker.getHost(), tracker.getPort());
    System.out.println(trackerAddress.toString());

    // Set up connection with tracker.
    datagramSocket.connect(trackerAddress);

    // Create connectRequestPacket
    ByteBuffer connectRequestData = ByteBuffer.allocate(16);
    connectRequestData.putLong(protocol_id);
    connectRequestData.putInt(action);
    connectRequestData.putInt(transaction_id);
    DatagramPacket connectRequestPacket =
        new DatagramPacket(
            connectRequestData.array(), connectRequestData.capacity(), trackerAddress);

    // Create connectResponsePacket
    byte[] connectResponseData = new byte[65508];
    DatagramPacket connectResponsePacket =
        new DatagramPacket(connectResponseData, connectResponseData.length);

    // Communicate with socket to get connect response
    byte[] connectResponse =
        communicateWithSocket(datagramSocket, connectRequestPacket, connectResponsePacket);
    if (connectResponse == null) {
      return peers; // Return empty peers
    }

    // Parse connect response
    int response_transaction_id =
        ByteBuffer.wrap(Arrays.copyOfRange(connectResponse, 4, 8)).getInt();
    long connection_id = ByteBuffer.wrap(Arrays.copyOfRange(connectResponse, 8, 16)).getLong();
    if (response_transaction_id != transaction_id) { // Req and resp transaction id's not match.
      return peers; // Return empty peers
    }

    // Create announceRequestPacket
    ByteBuffer announceRequestData = ByteBuffer.allocate(98);
    announceRequestData.putLong(connection_id);
    announceRequestData.putInt(action);
    announceRequestData.putInt(transaction_id);
    announceRequestData.put(torrent.getInfoHash().getBytes());
    announceRequestData.put(peerID.getBytes());
    announceRequestData.putLong(downloaded);
    announceRequestData.putLong(left);
    announceRequestData.putLong(uploaded);
    announceRequestData.putInt(event);
    announceRequestData.putInt(ip_address);
    announceRequestData.putInt(key);
    announceRequestData.putInt(num_want);
    DatagramPacket announceRequestPacket =
        new DatagramPacket(
            connectRequestData.array(), connectRequestData.capacity(), trackerAddress);

    // Create announceResponsePacket
    byte[] announceResponseData = new byte[65508];
    DatagramPacket announceResponsePacket =
        new DatagramPacket(announceResponseData, announceResponseData.length);

    // Communicate with socket to get announce response
    byte[] announceResponse =
        communicateWithSocket(datagramSocket, announceRequestPacket, announceResponsePacket);
    if (announceResponse == null) {
      return peers; // Return empty addresses
    }

    // Parse peers from announce response
    byte[] addresses = Arrays.copyOfRange(announceResponse, 20, announceResponse.length);

    // Create peers
    peers = createPeers(addresses, torrent);
    return peers;
  }

  /**
   * Communicate with tracker socket
   *
   * @param datagramSocket socket we are talking with
   * @param requestPacket request input parameters
   * @param responsePacket response packet structure
   * @return socket response
   */
  public byte[] communicateWithSocket(
      @NonNull DatagramSocket datagramSocket,
      @NonNull DatagramPacket requestPacket,
      @NonNull DatagramPacket responsePacket)
      throws IOException {
    System.out.println("trying to connect to socket...");

    // Try connecting up to 8 times
    for (int i = 0; i < 8; i++) {
      datagramSocket.setSoTimeout((int) (15000 * Math.pow(2, i))); // Timeout is 15 * 2 ^ n seconds.
      System.out.println(i + "th attempt trying to connect to tracker.");

      try {
        datagramSocket.send(requestPacket);
        System.out.println("successfully sent");
        datagramSocket.receive(responsePacket);
        break;
      } catch (SocketTimeoutException s) {
        if (i == 7) { // Couldn't connect after 8 attempts.
          return null;
        }
        continue;
      }
    }

    byte[] response = responsePacket.getData();
    return response;
  }

  /**
   * Create peers from address array
   *
   * @param addresses Peer addresses byte array
   * @return list of IP addresses
   */
  public List<Peer> createPeers(@NonNull byte[] addresses, @NonNull Torrent torrent)
      throws UnknownHostException {
    List<Peer> peers = new ArrayList<>();

    // Iterate peers and store IP + port for each peer
    for (int i = 0; i < addresses.length - 6; i += 6) {
      // Create Inet Socket Address
      byte[] ipAsBytes = Arrays.copyOfRange(addresses, i, i + 4);
      InetAddress inetAddress = InetAddress.getByAddress(ipAsBytes);
      int peerPort = ((addresses[i + 5] & 0xFF) << 8) | (addresses[i + 6] & 0xFF);
      InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, peerPort);

      if (inetSocketAddress.toString().equals("/0.0.0.0:0")) {
        break;
      }
      System.out.println(inetSocketAddress.toString());

      // Create peer
      Peer peer = new Peer(inetSocketAddress, torrent);
      peers.add(peer);
    }

    return peers;
  }
}
