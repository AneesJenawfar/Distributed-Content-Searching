package com.CS4262.ftp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPServer implements Runnable{

    private ServerSocket serverSocket;
    private Socket clientsocket;

    private String userName;

    public FTPServer(int port, String userName) throws Exception {
        // create socket
        serverSocket = new ServerSocket(port);
        this.userName = userName;
    }

    public int getPort(){
        return serverSocket.getLocalPort();
    }

    @Override
    public void run() {
        while (true) {

            try {
                clientsocket = serverSocket.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }
            Thread t = new Thread(new SendingData(clientsocket, userName));
            t.start();
        }
    }
}
