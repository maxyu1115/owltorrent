package edu.rice.owltorrent.network;

import com.dampcake.bencode.BencodeInputStream;
import com.google.common.io.ByteStreams;
import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

public class PeerLocator {

    public static final String infoHash = "%32%31%0b%4d%b8%4d%e5%c0%23%bf%cb%9f%40%64%8d%8c%9e%7c%a1%6e"; // TODO: retrieve from torrent
    public static final String peerID = "owltorrentclientpeer";
    public static final String port = "6991";
    public static final String left = "0";
    public static final String downloaded = "0";
    public static final String uploaded = "0";
    public static final String compact = "1";

    public List<Peer> locatePeers(Torrent torrent) {
        try {
            StringBuilder builder = new StringBuilder("https://torrent.ubuntu.com/announce?");
            builder.append("info_hash=" + infoHash + "&");
            builder.append("peer_id=" + peerID + "&");
            builder.append("port=" + port + "&");
            builder.append("left=" + left + "&");
            builder.append("downloaded=" + downloaded + "&");
            builder.append("uploaded=" + uploaded + "&");
            builder.append("compact=" + compact);

            URL url = new URL(builder.toString());
            URLConnection connection = url.openConnection();

            InputStream is = connection.getInputStream();
            byte[] bytes = ByteStreams.toByteArray(is);

            // Decode response using base64 decoder
//            byte[] decodedBytes = Base64.getMimeDecoder().decode(bytes);
//            System.out.println(Arrays.toString(decodedBytes));
//            int len=0;
//            for (int i=0; i<decodedBytes.length; i++) {
//                len++;
//                System.out.println(decodedBytes[i]);
//            }
//            String decodedString = new String(decodedBytes);
//            System.out.println("decoded string: " + decodedString);


            // Bencode response first
            ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
            BencodeInputStream bencodeInputStream = new BencodeInputStream(inputStream);
            var dict = bencodeInputStream.readDictionary();
            System.out.println(dict.keySet());

            // Decode peers field using base64 decoder
            String peers = (String) dict.get("peers");
            byte[] decodedBytes = Base64.getMimeDecoder().decode(peers.getBytes());
            System.out.println(peers);
            System.out.println(Arrays.toString(decodedBytes));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
