package com.CS4262.core;

import com.CS4262.common.Constants;
import com.CS4262.common.FileCollection;
import com.CS4262.common.QueryCollection;
import com.CS4262.ftp.FTPClient;
import com.CS4262.store.Queries;
import com.CS4262.util.ConsoleTable;

import java.io.IOException;
import java.net.*;
import java.util.*;

//import static spark.Spark.stop;


public class UDPClient {

    private static class InstanceHolder {
        private static UDPClient instance = new UDPClient();
    }

    private static final List<Node> peers = new ArrayList<>();
//    private static final List<Queries> full_query_stores = new ArrayList<>();
    private static final List<UUID> previous_uuid = new ArrayList<>();
    private static Map<UUID, List<Queries>> searchResults = new HashMap<>();
    private static Map<Integer, Queries> fileDownloadOptions;
    private int numOfSendMessages;
    private int numOfReceivedMessages;

    private Node BootStrapServer;
    private static Node currentNode;
    private int SendingPort;
    private int ReceivingPort;

//    private long lastPingTime;
//    private static Map<String, Long> pingTimes = new HashMap<String, Long>();
//    private static final Object lastPingLock = new Object();
//    private static final Object pingTimesLock = new Object();

    private UDPClient() {

    }

    public static UDPClient getInstance() {
        return InstanceHolder.instance;
    }
    private static FileCollection files;
    private static QueryCollection queries = QueryCollection.getInstance();

    public static Node getCurrentNode() { return currentNode;}

    public static List<Node> getPeers() {
        return peers;
    }

    public static FileCollection getFiles() {
        return files;
    }

    public static Map<UUID, List<Queries>> getSearchResults() { return searchResults; }

    public static int getFileDownloadCount(){ return fileDownloadOptions.size(); }

    public int getReceivingPort() {
        return ReceivingPort;
    }

    public void setReceivingPort(int receivingPort) {
        ReceivingPort = receivingPort;
    }

    public int getNumOfSendMessages() {
        return numOfSendMessages;
    }

    public int getNumOfReceivedMessages() {
        return numOfReceivedMessages;
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
        files = FileCollection.getInstance(currentNode.getUsername());
        try {
            String result = sendReceiveUDP(msg,BootStrapServer);
            System.out.println(result);
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
                        Node detail = new Node(ipAddress, portNumber);
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
//                lastPingTime = System.currentTimeMillis();
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
//            lastPingTime = Long.parseLong(null);
//            pingTimes = new HashMap<String, Long>();
        }
        return success;
    }

    public void listening() throws SocketException {
        DatagramSocket rec_socket = new DatagramSocket(ReceivingPort);
        rec_socket.setSoTimeout(1000);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
//                    synchronized (lastPingLock){
//                        if ((System.currentTimeMillis()-lastPingTime) > Constants.PING_INTERVAL){
//                            try {
//                                checkPeersAreAlive();
//                                lastPingTime = System.currentTimeMillis();
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//
//                    synchronized (pingTimesLock){
//                        Map<String, Node> nodeToRemove = new HashMap<String, Node>();
//                        for (String key : pingTimes.keySet()) {
//                            if(System.currentTimeMillis() - pingTimes.get(key)> Constants.PING_TIMEOUT){
//                                System.out.println("ping timeout Key : "+ key + "=" + pingTimes.get(key));
//                                System.out.println("Time Difference : "+ (System.currentTimeMillis() - pingTimes.get(key)) + "=" + Constants.PING_TIMEOUT);
//                                StringTokenizer tokenizer = new StringTokenizer(key, ":");
//                                String nodeIP = tokenizer.nextToken();
//                                int nodePort = Integer.parseInt(tokenizer.nextToken());
//                                Node detail = new Node(nodeIP, nodePort);
//                                nodeToRemove.put(key,detail);
//
//                            }
//                        }
//                        for (String key: nodeToRemove.keySet()) {
//                            peers.remove(nodeToRemove.get(key));
//                            pingTimes.remove(key);
//                        }
//                    }
//
//                    if(peers.size()<Constants.MIN_NEIGHBOURS){
//                        try {
//                            askPeers();
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }

                    byte[] buffer = new byte[65536];
                    DatagramPacket incoming = new DatagramPacket(buffer, buffer.length);
                    try {
                        rec_socket.receive(incoming);
                        byte[] data = incoming.getData();
                        String s = new String(data, 0, incoming.getLength());
//                        System.out.println("Received Data : "+s);
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

    public static String process(String msg) throws Exception {
        StringTokenizer tokenizer = new StringTokenizer(msg, " ");
        tokenizer.nextToken();
        String command = tokenizer.nextToken();
//        System.out.println("received msg : "+msg);
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
//                System.out.println("Alive OK : "+NodeIP+":"+NodePort);
                String reply = checkAlive();
                send(node,reply);
            }
            else if (Objects.equals(command, Constants.SEARCH)) {
//                System.out.println("Search found : " + msg);
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
//                System.out.println("[INFO] : Search result Found: "+msg);
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
                    for (String filename: results) {
//                        Node detail = new Node(nodeIP, nodePort);
                        Queries queryStore = new Queries();
                        queryStore.setUuid(uuid);
                        queryStore.setFileOwnerIP(nodeIP);
                        queryStore.setFileOwnerPort(nodePort);
                        queryStore.setFileRequesterIP(requestIP);
                        queryStore.setFileRequesterPort(requestPort);
                        queryStore.setFileName(filename);
                        queryStore.setNoOfHops(hops);
//                        if (!full_query_stores.contains(queryStore)) {
//                            full_query_stores.add(queryStore);
//                        }
                        List<Queries> result = searchResults.get(uuid);
                        if(result != null){
                            result.add(queryStore);
                        }else{
                            result = new ArrayList<Queries>();
                            result.add(queryStore);
                        }
                        searchResults.put(uuid,result);
                    }
//                    printResult(queryStore);
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
                String key = nodeIP+":"+nodePort;
//                System.out.println("Alive ok received : "+key);
//                synchronized (pingTimesLock){
//                    if(pingTimes.get(key) != null){
////                        System.out.println("ping update Key : "+ key + "=" + pingTimes.get(key));
//                        pingTimes.put(key,System.currentTimeMillis());
//                    }
//                }
                if (reply != 0) {
                    peers.remove(detail);
                }
                printPeers();

            }else if (Objects.equals(command, Constants.PING)) {
                System.out.println("ping received");
                String nodeIP = tokenizer.nextToken();
                int nodePort = Integer.parseInt(tokenizer.nextToken());
                Node detail = new Node(nodeIP,nodePort);
                for (Node peer: peers) {
                    if(nodeIP.equals(peer.getNodeIP()) && nodePort == peer.getNodePort()){
                        continue;
                    }else{
                        System.out.println("Pong Send");
                        String replymsg = String.format(Constants.PONG_FORMAT, peer.getNodeIP(), peer.getNodePort());
                        System.out.println(replymsg);
                        send(detail,replymsg);
                        break;
                    }
                }
            }else if (Objects.equals(command, Constants.PONG)) {
                System.out.println("pong received");
                String JOIN_MESSAGE = "JOIN " + currentNode.getNodeIP() + " " + currentNode.getNodePort();
                String nodeIP = tokenizer.nextToken();
                int nodePort = Integer.parseInt(tokenizer.nextToken());
                Node detail = new Node(nodeIP, nodePort);
                join(detail);
                send(detail, JOIN_MESSAGE);
            }
        }
        return null;
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

    public static void sendUDP(String msg, Node node) throws IOException {

        synchronized (UDPClient.getInstance()) {
            msg = String.format("%04d", (msg.length() + 5)) + " " + msg;
//            System.out.println("From sendUDP : "+msg);
            DatagramSocket sock = new DatagramSocket();
            InetAddress node_address = InetAddress.getByName(node.getNodeIP());
            byte[] buffer = msg.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, node_address, node.getNodePort());
            sock.send(packet);
            sock.close();
        }

    }

    public static UUID find(String query) throws Exception {
        System.out.println("\nSearching started... : " + query);
        UUID randomUUID = UUID.randomUUID();
        sendUDP("SER " + randomUUID + " " + currentNode.getNodeIP() + " " + currentNode.getNodePort() + " " + query + " " + 0,currentNode);
        getInstance().numOfSendMessages++;
        return randomUUID;
    }

    public static synchronized String search(String query, Node detail, int hops, UUID randomUUID) throws Exception {
        String reply = "";
        List<String> results_current = getFiles().searchFile(query);
        if (results_current.size() > 0) {
            StringBuilder result = new StringBuilder("");
            for (int j = 0; j < results_current.size(); j++) {
                result.append(" ").append(results_current.get(j));
            }
//            System.out.println("Result :"+result);
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

    public void getFile(int fileOption) {
        try {
            Queries fileDetail = fileDownloadOptions.get(fileOption);
            System.out.println("The file you requested is " + fileDetail.getFileName());
            FTPClient ftpClient = new FTPClient(fileDetail.getFileOwnerIP(), fileDetail.getFileOwnerPort()+Constants.FTP_PORT_OFFSET,
                    fileDetail.getFileName());

            System.out.println("Waiting for file download...");
            Thread.sleep(Constants.FILE_DOWNLOAD_TIMEOUT);
        } catch (Exception e) {
            System.out.println("Could not reach the Node!");
//            e.printStackTrace();
        }
    }

    public void printSearchResults(UUID uuid, Map<UUID, List<Queries>> searchResults){

        System.out.println("\nFile search results : ");

        ArrayList<String> headers = new ArrayList<String>();
        headers.add("Option No");
        headers.add("FileName");
        headers.add("Source");
        headers.add("Hop count");

        ArrayList<ArrayList<String>> content = new ArrayList<ArrayList<String>>();

        int fileIndex = 1;

        fileDownloadOptions = new HashMap<Integer, Queries>();
        List<Queries> results = (List<Queries>) searchResults.get(uuid);
        if (results == null){
            System.out.println("Sorry. No files are found!!!");
            return;
        }
        for (Queries query : results){
            fileDownloadOptions.put(fileIndex, query);

            ArrayList<String> row1 = new ArrayList<String>();
            row1.add("" + fileIndex);
            row1.add(query.getFileName().replace("-", " "));
            row1.add(query.getFileOwnerIP() + ":" + query.getFileOwnerPort());
            row1.add("" + query.getNoOfHops());

            content.add(row1);

            fileIndex++;


        }
        ConsoleTable ct = new ConsoleTable(headers,content);
        ct.printTable();
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

    private static synchronized String checkAlive() throws Exception {
        String  reply = Constants.AVEOK + " " + 0 + " " + currentNode.getNodeIP() + " " + currentNode.getNodePort();
//        reply = String.format("%04d", (reply.length() + 5)) + " " + reply;
        return reply;
    }

//    public static void checkPeersAreAlive() throws Exception {
//        if (!Objects.isNull(currentNode)) {
//            for (Node peer : peers) {
//                send(peer, Constants.ALIVE + " "+currentNode.getNodeIP() + " " + currentNode.getNodePort());
//                synchronized (pingTimesLock){
//                    pingTimes.put(peer.getNodeIP()+":"+peer.getNodePort(),System.currentTimeMillis());
//                }
//            }
//        }
//    }

    public static void askPeers() throws Exception {
        for (Node peer : peers) {
            String msg = String.format(Constants.PING_FORMAT, currentNode.getNodeIP(), currentNode.getNodePort());
            send(peer, msg);
        }
    }

    private static void printResult(Queries queryStore){
        System.out.println("[File Name] : " + queryStore.getFileName());
        System.out.println("[Owner Details] : " + queryStore.getFileOwnerIP() + " : " + queryStore.getFileOwnerPort());
        System.out.println("[Hops] : " + queryStore.getNoOfHops());
        System.out.println("[Time Query arrived] : "+ System.currentTimeMillis());
    }

    public static void searchQueries() throws Exception {
        for(String query: queries.getQueries()){
            find(query);
        }
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
            System.out.println("File No " + id + ":" + files.getFiles().get(i).replace(" ", "-"));
        }
    }

    public static void printStats() throws Exception{
        System.out.println("No of Send Messages " + getInstance().numOfSendMessages);
        System.out.println("No of Received Messages " + getInstance().numOfReceivedMessages);
    }


//    public synchronized boolean kill() throws Exception {
//        // make join request for peers....
//        askPeersToJoin();
//        stop();
//        return true;
//    }
}