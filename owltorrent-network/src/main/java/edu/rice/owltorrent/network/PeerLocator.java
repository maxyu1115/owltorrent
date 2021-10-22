package edu.rice.owltorrent.network;

import com.dampcake.bencode.BencodeInputStream;
import com.google.common.io.ByteStreams;
import edu.rice.owltorrent.common.entity.Torrent;
import lombok.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Tracker connector to fetch peers.
 *
 * @author bhaveshshah
 */
public class PeerLocator {

  public static final String infoHash =
      "%32%31%0b%4d%b8%4d%e5%c0%23%bf%cb%9f%40%64%8d%8c%9e%7c%a1%6e"; // TODO: retrieve from torrent
  public static final String peerID = "owltorrentclientpeer";
  public static final String port = "6991";
  public static final String left = "0";
  public static final String downloaded = "0";
  public static final String uploaded = "0";
  public static final String compact = "1";

  /**
   * Determine which protocol to use and then retrieve peers accordingly
   *
   * @param torrent Torrent object
   * @return list of peer addresses
   */
  public List<InetSocketAddress> locatePeers(@NonNull Torrent torrent) {
    String announceUrl = torrent.getAnnounceURL();
    String protocol = announceUrl.split(":")[0];

    try {
      if (protocol.equals("http")) {
        return locateWithHTTPTracker(torrent);
      } else if (protocol.equals("udp")) {
        return locateWithUDPTracker(torrent);
      } else {
        System.out.println("Unsupported protocol");
        return null;
      }
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Retrieve peers from HTTP tracker
   *
   * @param torrent Torrent object
   * @return list of peer addresses
   */
  public List<InetSocketAddress> locateWithHTTPTracker(@NonNull Torrent torrent) throws IOException {
    List<InetSocketAddress> addresses = new ArrayList<>();

    String baseURL = torrent.getAnnounceURL();
    String request =
        baseURL
            + "info_hash="
            + infoHash
            + "&peer_id="
            + peerID
            + "&port="
            + port
            + "&left="
            + left
            + "&downloaded="
            + downloaded
            + "&uploaded="
            + uploaded
            + "&compact="
            + compact;

    // Set up connection with tracker
    URL url = new URL(request);
    URLConnection connection = url.openConnection();

    // Read bytes from tracker
    InputStream is = connection.getInputStream();
    byte[] bytes = ByteStreams.toByteArray(is);

    // Bencode tracker response
    ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
    BencodeInputStream bencodeInputStream = new BencodeInputStream(inputStream);
    var dict = bencodeInputStream.readDictionary();

    // Extract peers from bencoded dictionary.
    String peers = (String) dict.get("peers");
    byte[] peersAsBytes = peers.getBytes();
    addresses = parsePeers(peersAsBytes);

    return addresses;
  }

  /**
   * Retrieve peers from UDP tracker
   *
   * @param torrent Torrent object
   * @return list of peer addresses
   */
  public List<InetSocketAddress> locateWithUDPTracker(@NonNull Torrent torrent) throws IOException, UnknownHostException {
    List<InetSocketAddress> addresses = new ArrayList<>();

    // Create socket on any available port.
    DatagramSocket datagramSocket = new DatagramSocket();

    // Set up Inet URL to connect to.
    var tracker = URI.create(torrent.getAnnounceURL());
    InetSocketAddress baseURL = new InetSocketAddress(tracker.getHost(), tracker.getPort());
    System.out.println(baseURL.toString());

    // Set up connection with tracker.
    datagramSocket.connect(baseURL);

    // Create connectRequestPacket
    ByteBuffer connectRequestData = ByteBuffer.allocate(16);
    long protocol_id = 0x41727101980L;
    int action = 1;
    int transaction_id = 12345;
    connectRequestData.putLong(protocol_id);
    connectRequestData.putInt(action);
    connectRequestData.putInt(transaction_id);
    DatagramPacket connectRequestPacket =
        new DatagramPacket(connectRequestData.array(), connectRequestData.capacity(), baseURL);

    // Create connectResponsePacket
    byte[] connectResponseData = new byte[65508];
    DatagramPacket connectResponsePacket =
        new DatagramPacket(connectResponseData, connectResponseData.length);

    // Communicate with socket to get connect response
    byte[] connectResponse = communicateWithSocket(datagramSocket, connectRequestPacket, connectResponsePacket);
    if (connectResponse == null) {
      return addresses; // Return empty addresses
    }

    // Parse connect response
    int response_transaction_id = ByteBuffer.wrap(Arrays.copyOfRange(connectResponse, 4, 8)).getInt();
    long connection_id = ByteBuffer.wrap(Arrays.copyOfRange(connectResponse, 8, 16)).getLong();
    if (response_transaction_id != transaction_id) { // Req and resp transaction id's not match.
      return addresses; // Return empty addresses
    }

    // Create announceRequestPacket
    ByteBuffer announceRequestData = ByteBuffer.allocate(98);
    int event = 0;
    int ip_address = 0;
    int key = 0;
    int num_want = -1;
    announceRequestData.putLong(connection_id);
    announceRequestData.putInt(action);
    announceRequestData.putInt(transaction_id);
    announceRequestData.put(torrent.getInfoHash().getBytes());
    announceRequestData.put(peerID.getBytes());
    announceRequestData.putLong(Long.parseLong(downloaded));
    announceRequestData.putLong(Long.parseLong(left));
    announceRequestData.putLong(Long.parseLong(uploaded));
    announceRequestData.putInt(event);
    announceRequestData.putInt(ip_address);
    announceRequestData.putInt(key);
    announceRequestData.putInt(num_want);
    DatagramPacket announceRequestPacket =
            new DatagramPacket(connectRequestData.array(), connectRequestData.capacity(), baseURL);

    // Create announceResponsePacket
    byte[] announceResponseData = new byte[65508];
    DatagramPacket announceResponsePacket =
            new DatagramPacket(announceResponseData, announceResponseData.length);

    // Communicate with socket to get announce response
    byte[] announceResponse = communicateWithSocket(datagramSocket, announceRequestPacket, announceResponsePacket);
    if (announceResponse == null) {
      return addresses; // Return empty addresses
    }

    // Parse peers from announce response
    byte[] peersAsBytes = Arrays.copyOfRange(announceResponse, 20, announceResponse.length);
    addresses = parsePeers(peersAsBytes);

    return addresses;
  }

  /**
   * Communicate with tracker socket
   *
   * @param datagramSocket socket we are talking with
   * @param requestPacket request input parameters
   * @param responsePacket response packet structure
   * @return socket response
   */
  public byte[] communicateWithSocket(@NonNull DatagramSocket datagramSocket, @NonNull DatagramPacket requestPacket, @NonNull DatagramPacket responsePacket) throws IOException {
    System.out.println("trying to connect to socket...");

    // Try connecting up to 8 times
    for (int i = 0; i < 8; i++) {
      datagramSocket.setSoTimeout(
              (int) (15000 * Math.pow(2, i))); // Timeout is 15 * 2 ^ n seconds.
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
   * Parse peers from byte array
   *
   * @param peersAsBytes Peers byte array
   * @return list of IP addresses
   */
  public List<InetSocketAddress> parsePeers(@NonNull byte[] peersAsBytes) throws UnknownHostException {
    List<InetSocketAddress> addresses = new ArrayList<>();

    // Iterate peers and store IP + port for each peer
    for (int i = 0; i < peersAsBytes.length - 6; i += 6) {
      byte[] ipAsBytes = Arrays.copyOfRange(peersAsBytes, i, i + 4);
      InetAddress inetAddress = InetAddress.getByAddress(ipAsBytes);

      int peerPort = ((peersAsBytes[i + 5] & 0xFF) << 8) | (peersAsBytes[i + 6] & 0xFF);

      InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, peerPort);

      if (inetSocketAddress.toString().equals("/0.0.0.0:0")) {
        break;
      }
      System.out.println(inetSocketAddress.toString());
      addresses.add(inetSocketAddress);
    }

    return addresses;
  }
}
