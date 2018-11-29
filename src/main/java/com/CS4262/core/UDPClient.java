package com.CS4262.core;

import com.CS4262.common.Constants;
import com.CS4262.common.FileCollection;
import com.CS4262.common.QueryCollection;
import com.CS4262.store.Queries;

import java.io.IOException;
import java.net.*;
import java.util.*;

//import static spark.Spark.stop;


public class UDPClient {

    private static class InstanceHolder {
        private static UDPClient instance = new UDPClient();
    }

    private static final List<Node> peers = new ArrayList<>();
    private static final List<Queries> full_query_stores = new ArrayList<>();
    private static final List<UUID> previous_uuid = new ArrayList<>();

    private int numOfSendMessages;
    private int numOfReceivedMessages;

    private Node BootStrapServer;
    private static Node currentNode;
    private int SendingPort;
    private int ReceivingPort;

    private UDPClient() {

    }

    public static UDPClient getInstance() {
        return InstanceHolder.instance;
    }
    private static FileCollection files = FileCollection.getInstance();
    private static QueryCollection queries = QueryCollection.getInstance();

    public static Node getCurrentNode() {
        return currentNode;
    }

    public static List<Node> getPeers() {
        return peers;
    }

    public static FileCollection getFiles() {
        return files;
    }

    public int getSendingPort() {
        return SendingPort;
    }

    public void setSendingPort(int sendingPort) {
        SendingPort = sendingPort;
    }

    public int getReceivingPort() {
        return ReceivingPort;
    }

    public void setReceivingPort(int receivingPort) {
        ReceivingPort = receivingPort;
    }

    public int getNumOfSendMessages() {
        return numOfSendMessages;
    }

    public void setNumOfSendMessages(int numOfSendMessages) {
        this.numOfSendMessages = numOfSendMessages;
    }

    public int getNumOfReceivedMessages() {
        return numOfReceivedMessages;
    }

    public void setNumOfReceivedMessages(int numOfReceivedMessages) {
        this.numOfReceivedMessages = numOfReceivedMessages;
    }

    public synchronized boolean register(String msg, String serverIP, int serverPort) throws Exception {

        if (!Objects.isNull(currentNode)) {
            throw new RuntimeException("UDPClient is already registered");
        }

        this.BootStrapServer = new Node(serverIP,serverPort);
        StringTokenizer tokens = new StringTokenizer(msg, " ");
        tokens.nextToken();
        String nodeIP = tokens.nextToken();
        int nodePort = Integer.parseInt(tokens.nextToken());
        String userName = tokens.nextToken();
        currentNode = new Node(nodeIP, nodePort, userName);
        try {
            String result = sendReceiveUDP(msg,BootStrapServer);
//            System.out.println(result);
            StringTokenizer tokenizer = new StringTokenizer(result, " ");
            tokenizer.nextToken();
            String command = tokenizer.nextToken();
            String JOIN_MESSAGE = "JOIN " + nodeIP + " " + nodePort;
            if (Constants.REGOK.equals(command)) {
                int no_of_nodes = Integer.parseInt(tokenizer.nextToken());
                switch (no_of_nodes) {
                    case 0:
                        System.out.println("First Node is Registered.");
                        break;
                    case 1:
                        System.out.println("Second Node is Registered.");
                        String ipAddress = tokenizer.nextToken();
                        int portNumber = Integer.parseInt(tokenizer.nextToken());
                        Node detail = new Node(ipAddress, portNumber, " ");
                        join(detail);
                        send(detail, JOIN_MESSAGE);
                        break;

                    default:
                        System.out.println("Node Registered Successfully.");
                        List<Node> returnedNodes = new ArrayList<>();
                        for (int i = 0; i < no_of_nodes; i++) {
                            String host = tokenizer.nextToken();
                            String hostPort = tokenizer.nextToken();
                            Node node = new Node(host, Integer.parseInt(hostPort));
                            returnedNodes.add(node);
                        }
                        Collections.shuffle(returnedNodes);
                        Node nodeA = returnedNodes.get(0);
                        Node nodeB = returnedNodes.get(1);

                        join(nodeA);
                        send(nodeA, JOIN_MESSAGE);

                        join(nodeB);
                        send(nodeB, JOIN_MESSAGE);
                        break;
                    case 9996:
                        System.out.println("[INFO]:Bootstrap Server is Full");
                        currentNode = null;
                        return false;

                    case 9997:
                        System.out.println("[ERROR]:IP and Port is Already Occupied.");
                        currentNode = null;
                        return false;

                    case 9998:
                        System.out.println("[ERROR]:Unregister First.");
                        currentNode = null;
                        return false;

                    case 9999:
                        System.out.println("[ERROR]:Error in Command");
                        currentNode = null;
                        return false;
                }
                System.out.println("Node = "+nodeIP+":"+nodePort);
                printFiles();
                printPeers();
                return true;
            } else {
                currentNode = null;
                return false;

            }
        } catch (IOException e) {
            e.printStackTrace();
            currentNode = null;
            return false;

        }
    }

    public synchronized boolean unregister() throws Exception {
        String LEAVE_MESSAGE = "LEAVE " + currentNode.getNodeIP() + " " + currentNode.getNodePort();
        System.out.println("\nAsk peers to join...");
        askPeersToJoin();
        // send to peers that the node is leaving and sharing it details.
        System.out.println("Broadcast leaving message to peers...");
        for (Node peer : peers) {
            send(peer, LEAVE_MESSAGE);
        }
        // send the message to the server...
        String message = String.format("UNREG %s %d %s", currentNode.getNodeIP(), currentNode.getNodePort(), currentNode.getUsername());
//        System.out.println(message);
        String result = sendReceiveUDP(message, BootStrapServer);
        StringTokenizer tokenizer = new StringTokenizer(result, " ");
        String total_size = tokenizer.nextToken();
        String reply = tokenizer.nextToken();
        // if UNROK was obtained from the server then the unregister was
        // successful
        boolean success = Constants.UNROK.equals(reply);
        if (success) {
            System.out.println("Left successfully!");
            currentNode = null;
        }
        return success;
    }

    private synchronized void askPeersToJoin() throws Exception {
        // send the peer list and make other peers join with each other
        // in order to avoid the failure in P2P connection
        final int peerSize = peers.size();
        for (int i = 0; i < peerSize; i++) {
            Node on = peers.get(i);
            for (int j = 0; j < peerSize; j++) {
                Node detail = peers.get(j);
                if (i != j) {
                    String JOIN_MESSAGE = "JOIN " + detail.getNodeIP() + " " + detail.getNodePort();
                    send(on, JOIN_MESSAGE);
                }
            }
        }
    }

    public static void send(final Node node, final String msg) throws Exception {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendReceiveUDP(msg, node);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static String sendReceiveUDP(String msg, Node node) throws Exception {
        msg = String.format("%04d", (msg.length() + 5)) + " " + msg;
        DatagramSocket sock = null;
        String s;
        try {
            sock = new DatagramSocket();
            InetAddress node_address = null;
            try {
                node_address = InetAddress.getByName(node.getNodeIP());
            } catch (UnknownHostException e) {
                System.out.println("Unable to find the Name for your IP " + node.getNodeIP());
            }
            byte[] buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, node_address, node.getNodePort());
            try {
                sock.send(packet);
            } catch (IOException e) {
                System.out.println("Sending " + packet + " is in failure");
            }
            buffer = new byte[65536];
            DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
            try {
                sock.receive(incoming);
            } catch (IOException e) {
                System.out.println("Receiving " + incoming + " is in failure");
            }
            byte[] data = incoming.getData();
            s = new String(data, 0, incoming.getLength());
            sock.close();
        } catch (SocketException e) {
            System.out.println("Port " + getInstance().SendingPort + " is already taken");
            s=null;
        }
        return s;
    }

    public static synchronized int join(Node peerNode) {
        int reply = 9999;
        if (!peers.contains(peerNode)) {
            peers.add(peerNode);
            reply = 0;
        }
        return reply;
    }

    public static synchronized int leave(Node peerNode) {
        int reply = 9999;
        if (peers.contains(peerNode)) {
            peers.remove(peerNode);
            reply = 0;
        }
        return reply;
    }

    public void listening() throws SocketException {
        DatagramSocket rec_socket = new DatagramSocket(ReceivingPort);
        rec_socket.setSoTimeout(1000);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    byte[] buffer = new byte[65536];
                    DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                    try {
                        rec_socket.receive(incoming);
                        byte[] data = incoming.getData();
                        String s = new String(data, 0, incoming.getLength());
                        String reply = UDPClient.process(s);
                        if (reply != null) {
                            synchronized (getInstance().getPeers()) {
                                sendUDP(reply, new Node(incoming.getAddress().toString().substring(1), incoming.getPort()));
                            }
                        }
                    } catch (SocketTimeoutException e) {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    public static void find(String query) throws Exception {
        System.out.println("\nStarted time for searching " + query + " in millisec " + System.currentTimeMillis() );
        UUID randomUUID = UUID.randomUUID();
        sendUDP("SER " + randomUUID + " " + currentNode.getNodeIP() + " " + currentNode.getNodePort() + " " + query + " " + 0,currentNode);
        getInstance().numOfSendMessages++;
    }

    public static void sendUDP(String msg, Node node) throws IOException {

        synchronized (UDPClient.getInstance()) {
            msg = String.format("%04d", (msg.length() + 5)) + " " + msg;
            System.out.println("From sendUDP : "+msg);
            DatagramSocket sock = new DatagramSocket();
            InetAddress node_address = InetAddress.getByName(node.getNodeIP());
            byte[] buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, node_address, node.getNodePort());
            sock.send(packet);
            sock.close();
        }

    }

    public static String process(String msg) throws Exception {
        StringTokenizer tokenizer = new StringTokenizer(msg, " ");
        tokenizer.nextToken();
        String command = tokenizer.nextToken();
        System.out.println("received msg : "+msg);
        synchronized (peers) {
            if (Objects.equals(command, Constants.JOIN)) {
                String NodeIP = tokenizer.nextToken();
                int NodePort = Integer.parseInt(tokenizer.nextToken());
                Node node = new Node(NodeIP, NodePort);
                int reply = join(node);
                if (reply == 0) {
//                    System.out.println("Successfully Joined");
//                    printPeers();
                } else if (reply == 9999) {
                    System.out.println("\n"+currentNode.getNodeIP()+":"+currentNode.getNodePort()+" already joined with "+
                            NodeIP+":"+NodePort);
                }
                return Constants.JOINOK + " " + reply;
            } else if (Objects.equals(command, Constants.LEAVE)) {
                String NodeIP = tokenizer.nextToken();
                int NodePort = Integer.parseInt(tokenizer.nextToken());
                Node node = new Node(NodeIP, NodePort);
                int reply = leave(node);
                if (reply == 0) {
//                    System.out.println("Successfully Leave");
                } else if (reply == 9999) {
                    System.out.println("Leave with error");
                }
                return Constants.LEAVEOK + " " + reply;
            } else if (Objects.equals(command, Constants.ALIVE)) {
                String NodeIP = tokenizer.nextToken();
                int NodePort = Integer.parseInt(tokenizer.nextToken());
                Node node = new Node(NodeIP, NodePort);
                if (Objects.equals(currentNode.getNodeIP(), node.getNodeIP()) & currentNode.getNodePort() == node.getNodePort()) {
                    String reply = checkAlive();
                    return reply;
                } else {
                    return null;
                }
            }
            else if (Objects.equals(command, Constants.SEARCH)) {
                System.out.println("Search found : " + msg);
                String reply = "";
                UUID uuid = UUID.fromString(tokenizer.nextToken());
                getInstance().numOfReceivedMessages++;
                if (previous_uuid.contains(uuid)){

                }else{
                    previous_uuid.add(uuid);
                    String NodeIP = tokenizer.nextToken();
                    int NodePort = Integer.parseInt(tokenizer.nextToken());
                    int count = (tokenizer.countTokens());

                    String query = tokenizer.nextToken();
                    count--;
                    while (count - 1 > 0) {
                        String query_single = tokenizer.nextToken();
                        query += "-" + query_single;
                        count--;
                    }

                    int hops = Integer.parseInt(tokenizer.nextToken());
                    Node node = new Node(NodeIP, NodePort);
                    reply = search(query, node, hops,uuid);

                }

                return reply;
            }else if (Objects.equals(command, Constants.SEROK)) {
                getInstance().numOfReceivedMessages++;
                System.out.println("[INFO] : Search result Found: "+msg);
                UUID uuid = UUID.fromString(tokenizer.nextToken());
                int noOfFiles = Integer.parseInt(tokenizer.nextToken());
                String nodeIP = tokenizer.nextToken();
                int nodePort = Integer.parseInt(tokenizer.nextToken());
                int hops = Integer.parseInt(tokenizer.nextToken());
                int count = noOfFiles;
                List<String> results = new ArrayList<>();
                while (count > 0) {
                    String file = tokenizer.nextToken();
                    results.add(file);
                    count--;
                }
                String requestIP = tokenizer.nextToken();
                int requestPort = Integer.parseInt(tokenizer.nextToken());
                if(requestIP.equals(currentNode.getNodeIP()) && requestPort == currentNode.getNodePort()){
//                    if(requestPort == currentNode.getNodePort()){
                    Node detail = new Node(nodeIP, nodePort);
                    Queries queryStore = new Queries();
                    queryStore.setUuid(uuid);
                    queryStore.setFileOwnerIP(nodeIP);
                    queryStore.setFileOwnerPort(nodePort);
                    queryStore.setFileRequesterIP(requestIP);
                    queryStore.setFileRequesterPort(requestPort);
                    queryStore.setFiles(results);
                    queryStore.setNoOfHops(hops);
                    if (!full_query_stores.contains(queryStore)) {
                        full_query_stores.add(queryStore);
                    }
                    printResult(queryStore);
//                    }else{
//                        for(Node peer: peers){
//                            sendUDP(msg,peer);
//                            getInstance().numOfSendMessages++;
//                        }
//                    }
                }else{
                    for(Node peer: peers){
                        sendUDP(msg,peer);
                        getInstance().numOfSendMessages++;
                    }
                }
            } else if (Objects.equals(command, Constants.AVEOK)) {
                int reply = Integer.parseInt(tokenizer.nextToken());
                String nodeIP = tokenizer.nextToken();
                int nodePort = Integer.parseInt(tokenizer.nextToken());
                Node detail = new Node(nodeIP, nodePort);
                if (reply != 0) {
                    peers.remove(detail);
                }
                printPeers();
            }
        }
        return null;
    }

    public static void checkPeersAreAlive() throws Exception {
        if (!Objects.isNull(currentNode)) {
            for (Node peer : peers) {
                send(peer, "AVE " + peer.getNodeIP() + " " + peer.getNodePort());
            }
        }
    }

    private static synchronized String checkAlive() throws Exception {
        String  reply = Constants.AVEOK + " " + 0 + " " + currentNode.getNodeIP() + " " + currentNode.getNodePort();
        reply = String.format("%04d", (reply.length() + 5)) + " " + reply;
        return reply;
    }

//    public synchronized boolean kill() throws Exception {
//        // make join request for peers....
//        askPeersToJoin();
//        stop();
//        return true;
//    }

    public static synchronized String search(String query, Node detail, int hops, UUID randomUUID) throws Exception {
        String reply = "";
        List<String> results_current = getFiles().searchFile(query);
        if (results_current.size() > 0) {
            StringBuilder result = new StringBuilder("");
            for (int j = 0; j < results_current.size(); j++) {
                result.append(" ").append(results_current.get(j));
            }
            System.out.println("Result :"+result);
            reply = "SEROK " + randomUUID + " " + results_current.size() + " " + currentNode.getNodeIP() + " " + currentNode.getNodePort() + " " + hops + result +
                    " " + detail.getNodeIP() + " " + detail.getNodePort();
            sendUDP(reply,detail);
            getInstance().numOfSendMessages++;
            return  reply;
        } else {
            hops += 1;
            for (Node peer : peers) {
                if (!peer.equals(detail)) {
                    sendUDP("SER " + randomUUID + " " + detail.getNodeIP() + " " + detail.getNodePort() + " " + query + " " + hops
                            ,peer);
                    getInstance().numOfSendMessages++;
                }
            }
            return null;
        }
    }

    public static void printResults() {
        if (full_query_stores.size() == 0) {
            return;
        }
        System.out.println("Available Search Results : ");
        System.out.println("[INFO] : Displaying Results");
        for (int i = 0; i < full_query_stores.size(); i++) {
            int id = i + 1;
            Queries queryStore = full_query_stores.get(i);
            System.out.println("[QueryStore ID] : " + id);
            printResult(queryStore);
        }
    }

    private static void printResult(Queries queryStore){
        System.out.println("[Files] : " + queryStore.getFiles());
        System.out.println("[Owner Details] : " + queryStore.getFileOwnerIP() + " : " + queryStore.getFileOwnerPort());
        System.out.println("[Hops] : " + queryStore.getNoOfHops());
        System.out.println("[Time Query arrived] : "+ System.currentTimeMillis());
    }


    public static void searchQueries() throws Exception {
        for(String query: queries.getQueries()){
            find(query);
        }
    }

    public static QueryCollection getQueries() {
        return queries;
    }

    public static void setQueries(QueryCollection queries) {
        UDPClient.queries = queries;
    }

    public String getResults() {
        StringBuilder full_res = new StringBuilder();
        for (Queries r : full_query_stores) {
            String str = r.toString();
            full_res.append("[ ").append(str).append(" ]");
        }
        return full_res.toString();
    }

    public static void printPeers() {
        if (peers.size() == 0) {
            return;
        }
        System.out.println("\nAvailable Peers : ");
        for (int i = 0; i < peers.size(); i++) {
            int id = i + 1;
            System.out.println("Peer No " + id + " : " + peers.get(i).getNodeIP() + ":" + peers.get(i).getNodePort());
        }
    }

    public static void printFiles() {
        if (files.getFiles().size() == 0) {
            return;
        }
        System.out.println("\nAvailable Files : ");
        for (int i = 0; i < files.getFiles().size(); i++) {
            int id = i + 1;
            System.out.println("File No " + id + ":" + files.getFiles().get(i));
        }
    }

    public static void printStats() throws Exception{
        System.out.println("No of Send Messages " + getInstance().numOfSendMessages);
        System.out.println("No of Received Messages " + getInstance().numOfReceivedMessages);
    }

}