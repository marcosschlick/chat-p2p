package com.chatp2p.utils;

import java.net.DatagramSocket;
import java.net.InetAddress;

public class NetworkUtils {
    public static String getLocalIP() {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
            return socket.getLocalAddress().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }
}