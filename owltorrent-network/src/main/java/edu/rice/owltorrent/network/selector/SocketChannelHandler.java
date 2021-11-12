package edu.rice.owltorrent.network.selector;

import edu.rice.owltorrent.network.SelectorHandler;
import edu.rice.owltorrent.network.peerconnector.SocketChannelConnector;
import edu.rice.owltorrent.network.task.ParseMsgTask;
import edu.rice.owltorrent.network.task.SendMsgTask;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ExecutorService;

/**
 * The wrapper class for SocketChannelConnectors. Handles all messages except handshake from peers.
 * @author Max Yu
 */
@RequiredArgsConstructor
public class SocketChannelHandler implements SelectorHandler {
    private final SocketChannelConnector socketChannelConnector;
    private final ExecutorService executorService;

    @Override
    public boolean read() {
        executorService.submit(new ParseMsgTask(socketChannelConnector));
        return true;
    }

    @Override
    public boolean write() {
        if (socketChannelConnector.hasOutgoingMsg()) {
            executorService.submit(new SendMsgTask(socketChannelConnector));
            return true;
        }
        return false;
    }
}
