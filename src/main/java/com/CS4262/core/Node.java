package com.CS4262.core;

import com.CS4262.common.Constants;
//import com.google.gson.Gson;

import java.io.IOException;
import java.net.*;
import java.util.Properties;
import java.util.logging.Logger;

public class Node {

//    private final Logger LOG = Logger.getLogger(Node.class.getName());

//    private String BS_IPAddress ;
//    private int BS_Port;
//    private DatagramSocket datagramSocket;

    private String nodeIP;
    private int nodePort;
    private String username;

//    public Node (String username) throws Exception {
//
//        try (final DatagramSocket socket = new DatagramSocket()){
//            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
//            this.nodeIP = socket.getLocalAddress().getHostAddress();
//
//        } catch (Exception e){
//            throw new RuntimeException("Could not find host address");
//        }
//
//        this.username = username;
//        this.nodePort = getFreePort();
//
//    }


    public Node(String nodeIP, int nodePort) {
        this.nodeIP = nodeIP;
        this.nodePort = nodePort;
    }

    public Node(String nodeIP, int nodePort, String username) {
        this.nodeIP = nodeIP;
        this.nodePort = nodePort;
        this.username = username;
    }

    public String getNodeIP() {
        return nodeIP;
    }

    public void setNodeIP(String nodeIP) {
        this.nodeIP = nodeIP;
    }

    public int getNodePort() {
        return nodePort;
    }

    public void setNodePort(int nodePort) {
        this.nodePort = nodePort;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node detail = (Node) o;

        if (nodePort != detail.nodePort) return false;
        return nodeIP.equals(detail.nodeIP);

    }

    @Override
    public int hashCode() {
        int result = nodeIP.hashCode();
        result = 31 * result + nodePort;
        return result;
    }

//    @Override
//    public String toString() {
//        return new Gson().toJson(this);
//    }

//    private int getFreePort() {
//        try (ServerSocket socket = new ServerSocket(0)) {
//            socket.setReuseAddress(true);
//            int port = socket.getLocalPort();
//            try {
//                socket.close();
//            } catch (IOException e) {
//                // Ignore IOException on close()
//            }
//            return port;
//        } catch (IOException e) {
//            throw new RuntimeException("Getting free port failed");
//        }
//    }

//    public Node(String userName) throws IOException {
//
//        this.userName = userName;
//        this.ipAddress = Constants.LOCALHOST;
//        this.port = getFreePort();
//
////        this.bsClient = new BSClient();
//        datagramSocket = new DatagramSocket();
//        Properties bsProperties = new Properties();
//        try {
//            bsProperties.load(getClass().getClassLoader().getResourceAsStream(
//                    Constants.BS_PROPERTIES));
//
//        } catch (IOException e) {
//            LOG.info("Could not open " + Constants.BS_PROPERTIES);
//            throw new RuntimeException("Could not open " + Constants.BS_PROPERTIES);
//        } catch (NullPointerException e) {
//            LOG.info("Could not find " + Constants.BS_PROPERTIES);
//            throw new RuntimeException("Could not find " + Constants.BS_PROPERTIES);
//        }
//        this.BS_IPAddress = bsProperties.getProperty("bootstrap.ip");
//        this.BS_Port = Integer.parseInt(bsProperties.getProperty("bootstrap.port"));

////        this.messageBroker = new MessageBroker();
////        messageBroker.start();
////        LOG.info("Gnode initiated on IP :" + ipAddress + " and Port :" + port);
//
//    }
//
//    public void register() {
//
//        try{
////            this.bsClient.register(this.userName, this.ipAddress, this.port);
//            String request = String.format(Constants.REG_FORMAT, ipAddress, port, userName);
//
//            request = String.format(Constants.MSG_FORMAT, request.length() + 5, request);
//            LOG.info("Request ==>" +request);
//
//            DatagramPacket sendingPacket = new DatagramPacket(request.getBytes(), request.length(),
//                    InetAddress.getByName(BS_IPAddress), BS_Port);
//
//            datagramSocket.setSoTimeout(Constants.TIMEOUT_REG);
//
//            datagramSocket.send(sendingPacket);
//
//            byte[] buffer = new byte[65536];
//
//            DatagramPacket received = new DatagramPacket(buffer, buffer.length);
//
//            datagramSocket.receive(received);
//
//            LOG.info("Response ==>" +new String(received.getData(), 0, received.getLength()));
//
//        } catch (IOException e) {
//            LOG.info("Registering Gnode failed");
//            e.printStackTrace();
//        }
//    }
//

}
