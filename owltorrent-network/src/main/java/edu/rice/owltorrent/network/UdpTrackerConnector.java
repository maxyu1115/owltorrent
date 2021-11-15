package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import edu.rice.owltorrent.common.entity.TorrentContext;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

/**
 * UDP tracker connector to fetch peers.
 *
 * @author bhaveshshah
 */
@Log4j2(topic = "network")
public class UdpTrackerConnector {

  // Connect parameters
  public static final long PROTOCOL_ID = 0x41727101980L;
  public static final int CONNECT_ACTION = 0;
  public static final int ANNOUNCE_ACTION = 1;

  // Announce parameters
  public static final int DEFAULT_IP_ADDRESS = 0;
  public static final int DEFAULT_NUM_WANT = -1;

  /**
   * Retrieve peers from UDP tracker
   *
   * @param torrentContext Torrent Context object
   * @return list of peers
   */
  public List<Peer> locateWithUDPTracker(
      @NonNull TorrentContext torrentContext,
      long downloaded,
      long left,
      long uploaded,
      Event event,
      String announceURL)
      throws IOException {
    Torrent torrent = torrentContext.getTorrent();
    List<Peer> peers = new ArrayList<>();
    Random random = new Random();

    // Create socket on any available port.
    DatagramSocket datagramSocket = new DatagramSocket();

    // Configure Inet address to connect to.
    var tracker = URI.create(announceURL);
    InetSocketAddress trackerAddress = new InetSocketAddress(tracker.getHost(), tracker.getPort());
    log.info("Tracker address" + trackerAddress);

    // Set up connection with tracker.
    datagramSocket.connect(trackerAddress);

    // Create connectRequestPacket
    ByteBuffer connectRequestData = ByteBuffer.allocate(16);
    connectRequestData.putLong(PROTOCOL_ID);
    connectRequestData.putInt(CONNECT_ACTION);
    int transaction_id = random.nextInt();
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
    //    log.info("Connect Response" + Arrays.toString(connectResponse));

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
    announceRequestData.putInt(ANNOUNCE_ACTION);
    announceRequestData.putInt(random.nextInt()); // transaction id
    announceRequestData.put(torrent.getInfoHash().getBytes());
    announceRequestData.put(torrentContext.getOurPeerId().getBytes());
    announceRequestData.putLong(downloaded);
    announceRequestData.putLong(left);
    announceRequestData.putLong(uploaded);
    announceRequestData.putInt(event.getEventCode());
    // log.debug("Address: {}", InetAddress.getLocalHost());
    // announceRequestData.put(InetAddress.getLocalHost().getAddress());
    announceRequestData.putInt(DEFAULT_IP_ADDRESS);
    announceRequestData.putInt(random.nextInt());
    announceRequestData.putInt(DEFAULT_NUM_WANT);
    announceRequestData.putShort(torrentContext.getListenerPort());
    DatagramPacket announceRequestPacket =
        new DatagramPacket(
            announceRequestData.array(), announceRequestData.capacity(), trackerAddress);

    // Create announceResponsePacket
    byte[] announceResponseData = new byte[65508];
    DatagramPacket announceResponsePacket =
        new DatagramPacket(announceResponseData, announceResponseData.length);
    log.debug("Communicating with Socket");

    // Communicate with socket to get announce response
    byte[] announceResponse =
        communicateWithSocket(datagramSocket, announceRequestPacket, announceResponsePacket);
    if (announceResponse == null) {
      log.info("No response from socket");
      return peers; // Return empty addresses
    }

    //    log.info("Announce Response" + Arrays.toString(announceResponse));

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
    log.info("trying to connect to socket...");

    // Try connecting up to 8 times
    for (int i = 0; i < 8; i++) {
      datagramSocket.setSoTimeout((int) (15000 * Math.pow(2, i))); // Timeout is 15 * 2 ^ n seconds.
      log.info(i + "th attempt trying to connect to tracker.");

      try {
        datagramSocket.send(requestPacket);
        log.info("successfully sent");
        datagramSocket.receive(responsePacket);
        break;
      } catch (SocketTimeoutException s) {
        if (i == 7) { // Couldn't connect after 8 attempts (max # of allowed attempts per protocol).
          throw new IOException();
        }
      }
    }

    return responsePacket.getData();
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
    for (int i = 0; i <= addresses.length - 6; i += 6) {
      // Create Inet Socket Address
      byte[] ipAsBytes = Arrays.copyOfRange(addresses, i, i + 4);
      InetAddress inetAddress = InetAddress.getByAddress(ipAsBytes);
      int peerPort = ((0xFF & (int) addresses[i + 4]) << 8) | (0xFF & (int) addresses[i + 5]);
      InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, peerPort);

      if (inetSocketAddress.toString().equals("/0.0.0.0:0")) {
        break;
      }
      // System.out.println(inetSocketAddress.toString());

      // Create peer
      Peer peer = new Peer(inetSocketAddress, torrent);
      peers.add(peer);
    }
    log.debug("Found peers: {}", peers);

    return peers;
  }
}
