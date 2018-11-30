package com.CS4262.ftp;

import java.net.Socket;

public class FTPClient {

    public FTPClient(String IpAddress, int port, String fileName) throws Exception {

        long start = System.currentTimeMillis();
        Socket serverSock = new Socket(IpAddress, port);

        System.out.println("Connecting...");
        Thread t = new Thread(new ReceivingData(serverSock, fileName));
        t.start();
        long stop = System.currentTimeMillis();
    }
}
