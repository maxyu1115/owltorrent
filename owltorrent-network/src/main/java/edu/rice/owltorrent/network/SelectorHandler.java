package edu.rice.owltorrent.network;

/**
 * Selector Handler Interface that handles selector events
 * @author Max Yu
 */
public interface SelectorHandler {
    /**
     *
     * @return whether the read was successful, and the SelectionKey should be dropped.
     */
    boolean read();
    /**
     *
     * @return whether the write was successful, and the SelectionKey should be dropped.
     */
    boolean write();
}
