package com.CS4262.core;

import com.CS4262.Constants;

import java.io.IOException;
import java.net.*;
import java.util.Properties;
import java.util.logging.Logger;

public class GNode {

    private final Logger LOG = Logger.getLogger(GNode.class.getName());

    private String BS_IPAddress ;
    private int BS_Port;
    private DatagramSocket datagramSocket;

    private String userName;
    private String ipAddress;
    private int port;


    public GNode (String userName) throws IOException {

        this.userName = userName;
        this.ipAddress = Constants.LOCALHOST;
        this.port = getFreePort();

//        this.bsClient = new BSClient();
        datagramSocket = new DatagramSocket();
        Properties bsProperties = new Properties();
        try {
            bsProperties.load(getClass().getClassLoader().getResourceAsStream(
                    Constants.BS_PROPERTIES));

        } catch (IOException e) {
            LOG.info("Could not open " + Constants.BS_PROPERTIES);
            throw new RuntimeException("Could not open " + Constants.BS_PROPERTIES);
        } catch (NullPointerException e) {
            LOG.info("Could not find " + Constants.BS_PROPERTIES);
            throw new RuntimeException("Could not find " + Constants.BS_PROPERTIES);
        }
        this.BS_IPAddress = bsProperties.getProperty("bootstrap.ip");
        this.BS_Port = Integer.parseInt(bsProperties.getProperty("bootstrap.port"));

//        this.messageBroker = new MessageBroker();
//        messageBroker.start();
        LOG.info("Gnode initiated on IP :" + ipAddress + " and Port :" + port);

    }

    public void register() {

        try{
//            this.bsClient.register(this.userName, this.ipAddress, this.port);
            String request = String.format(Constants.REG_FORMAT, ipAddress, port, userName);

            request = String.format(Constants.MSG_FORMAT, request.length() + 5, request);

            DatagramPacket sendingPacket = new DatagramPacket(request.getBytes(), request.length(),
                    InetAddress.getByName(BS_IPAddress), BS_Port);

            datagramSocket.setSoTimeout(Constants.TIMEOUT_REG);

            datagramSocket.send(sendingPacket);

            byte[] buffer = new byte[65536];

            DatagramPacket received = new DatagramPacket(buffer, buffer.length);

            datagramSocket.receive(received);

            LOG.info("Response ==>" +new String(received.getData(), 0, received.getLength()));

        } catch (IOException e) {
            LOG.info("Registering Gnode failed");
            e.printStackTrace();
        }
    }

    private int getFreePort() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            int port = socket.getLocalPort();
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore IOException on close()
            }
            return port;
        } catch (IOException e) {
            LOG.severe("Getting free port failed");
            throw new RuntimeException("Getting free port failed");
        }
    }
}
