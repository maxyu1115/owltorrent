package edu.rice.owltorrent.network;

import edu.rice.owltorrent.common.entity.Peer;
import edu.rice.owltorrent.common.entity.Torrent;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.List;

public class PeerLocator {
    public List<Peer> locatePeers(Torrent torrent) {
        try {
            CloseableHttpClient httpClient = HttpClientBuilder.create().build();

            HttpGet httpGet = new HttpGet("https://academictorrents.com/announce.php");
            URI uri = new URIBuilder(httpGet.getURI())
                    .addParameter("info_hash", "%69%7F%CD%F6%E9%77%36%AC%93%ED%AE%63%88%45%C7%3E%6C%9B%BD%AE")
                    .addParameter("peer_id", "owltorrentclientpeer")
                    .addParameter("left", "0")
                    .addParameter("downloaded", "0")
                    .addParameter("uploaded", "0")
                    .addParameter("compact", "0")
                    .build();
            ((HttpRequestBase) httpGet).setURI(uri);
            CloseableHttpResponse response = httpClient.execute(httpGet);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            String responseString = out.toString();
            System.out.println(responseString);

            out.close();
            httpClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
