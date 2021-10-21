package edu.rice.owltorrent.network;

import com.dampcake.bencode.BencodeInputStream;
import com.google.common.io.ByteStreams;
import edu.rice.owltorrent.common.entity.Torrent;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.NonNull;

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

    if (protocol.equals("http")) {
      return locateWithHTTPTracker(torrent);
    } else if (protocol.equals("udp")) {
      return locateWithUDPTracker(torrent);
    } else {
      System.out.println("Unsupported protocol");
      return null;
    }
  }

  /**
   * Retrieve peers from HTTP tracker
   *
   * @param torrent Torrent object
   * @return list of peer addresses
   */
  public List<InetSocketAddress> locateWithHTTPTracker(@NonNull Torrent torrent) {
    List<InetSocketAddress> addresses = new ArrayList<>();

    try {
      // Build request
      String baseURL = "https://torrent.ubuntu.com/announce?"; // TODO: retrieve from torrent
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

      // Iterate peers and store IP + port for each peer
      for (int i = 0; i < peersAsBytes.length - 6; i += 6) {
        byte[] ipAsBytes = Arrays.copyOfRange(peersAsBytes, i, i + 4);
        InetAddress inetAddress = InetAddress.getByAddress(ipAsBytes);

        int peerPort = ((peersAsBytes[i + 5] & 0xFF) << 8) | (peersAsBytes[i + 6] & 0xFF);

        InetSocketAddress inetSocketAddress = new InetSocketAddress(inetAddress, peerPort);
        addresses.add(inetSocketAddress);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return addresses;
  }

  /**
   * Retrieve peers from UDP tracker
   *
   * @param torrent Torrent object
   * @return list of peer addresses
   */
  public List<InetSocketAddress> locateWithUDPTracker(@NonNull Torrent torrent) {
    List<InetSocketAddress> addresses = new ArrayList<>();

    try {
      // Create socket on any available port.
      DatagramSocket datagramSocket = new DatagramSocket();

      // Set up Inet URL to connect to.
      var tracker = URI.create("udp://tracker.openbittorrent.com:80/announce");
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
      byte[] connectResponseData = new byte[16];
      DatagramPacket connectResponsePacket =
          new DatagramPacket(connectResponseData, connectResponseData.length);

      System.out.println("trying to connect to socket...");

      // Try connecting up to 8 times
      for (int i = 0; i < 8; i++) {
        datagramSocket.setSoTimeout(
            (int) (15000 * Math.pow(2, i))); // Timeout is 15 * 2 ^ n seconds.
        System.out.println(i + "th attempt trying to connect to tracker.");

        try {
          datagramSocket.send(connectRequestPacket);
          System.out.println("successfully sent");
          datagramSocket.receive(connectResponsePacket);
          break;
        } catch (SocketTimeoutException s) {
          if (i == 7) { // Couldn't connect after 8 attempts.
            return null;
          }
          continue;
        }
      }

      System.out.println("got back response from socket!");

      byte[] connectResponse = connectResponsePacket.getData();
      System.out.println(connectResponse);

      // TODO: announce connection

    } catch (Exception e) {
      e.printStackTrace();
    }

    return addresses;
  }
}
