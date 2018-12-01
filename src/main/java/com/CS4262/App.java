package com.CS4262;

import com.CS4262.core.UDPClient;
import com.CS4262.core.Node;
import com.CS4262.common.Constants;
import com.CS4262.common.FileCollection;
import com.CS4262.store.Queries;
//
//import spark.QueryParamsMap;
//import spark.Request;
//import spark.Response;
//import spark.Route;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.*;

//import static com.CS4262.util.ResponseUtil.json;
//import static spark.Spark.get;
//import static spark.Spark.port;
//import static spark.Spark.ipAddress;
//import static spark.Spark.staticFileLocation;

public class App {

    private static UDPClient instance = UDPClient.getInstance();

    public static void main( String[] args ) {
//        App app = new App();
        try {
            String uniqueID = UUID.randomUUID().toString();
            String userName  = "node" + uniqueID;

            try (final DatagramSocket socket = new DatagramSocket()){
                socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
                String nodeIP = socket.getLocalAddress().getHostAddress();
                System.out.println("Obtaining Host address and Port...");
                int nodePort = getFreePort();
                System.out.println("Node = "+nodeIP+":"+nodePort);
                instance.setReceivingPort(nodePort);
                instance.listening();
                String msg = "REG " + nodeIP + " " + instance.getReceivingPort() + " " + userName;

                Properties bsProperties = new Properties();
                bsProperties.load(App.class.getClassLoader().getResourceAsStream(
                        Constants.BS_PROPERTIES));
                String serverIP = bsProperties.getProperty("bootstrap.ip");
                int serverPort = Integer.parseInt(bsProperties.getProperty("bootstrap.port"));
                System.out.println("Bootstrap Sever = "+serverIP+":"+serverPort+"\n");
                System.out.println("Registering the Node...");
                instance.register(msg, serverIP, serverPort);

            } catch (IOException e) {
                throw new RuntimeException("Could not open " + Constants.BS_PROPERTIES);
            } catch (NullPointerException e) {
                throw new RuntimeException("Could not find " + Constants.BS_PROPERTIES);
            }catch (Exception e){
                throw new RuntimeException("Could not find host address");
            }
            Scanner scanner = new Scanner(System.in);
            Node currentNode = UDPClient.getCurrentNode();

            while(true){
                System.out.println("\n("+currentNode.getNodeIP()+":"+currentNode.getNodePort()+") Choose what do you want to do below : ");
                System.out.println("1-Do a search  2-Print neighbors(peers)  3-Print available files  \n4-Message Counts  5-Exit the network");
                System.out.println("\nPlease enter the option : ");

                String commandOption = scanner.nextLine();

                if (commandOption.equals("1")){
                    System.out.println("\nEnter your search query below : ");
                    String searchQuery = scanner.nextLine();

                    if (searchQuery != null && !searchQuery.equals("")){
                        UUID uuid = null;
                        try {
                            uuid = UDPClient.find(searchQuery);
                            Thread.sleep(Constants.SEARCH_TIMEOUT);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Map<UUID, List<Queries>> searchResult = UDPClient.getSearchResults();
                        if(searchResult.get(uuid) != null){
                            if (searchResult.get(uuid).size() != 0){
                                instance.printSearchResults(uuid,searchResult);

                                while(true){
                                    try{
                                        System.out.println("\nPlease choose the file you need to download : ");
                                        String fileOption = scanner.nextLine();

                                        int option = Integer.parseInt(fileOption);
                                        int resultCount = UDPClient.getFileDownloadCount();
                                        if (option > resultCount){
                                            System.out.println("Please give an option within the search results...");
                                            continue;
                                        }
                                        if(option == 0){
                                            break;
                                        }
                                        instance.getFile(option);
                                        break;

                                    } catch (NumberFormatException e){
                                        System.out.println("Enter a valid integer indicating " +
                                                "the file option shown above in the results...");
                                    }
                                }
                            }
                        }else{
                            System.out.println("Sorry. No files are found!!!");
                        }

                    } else {
                        System.out.println("Please give a valid search query!!!");
                    }
                } else if (commandOption.equals("2")){
                    UDPClient.printPeers();

                }  else if (commandOption.equals("3")){
                    UDPClient.printFiles();

                }else if (commandOption.equals("4")){
                    System.out.println("\nSend Messages :"+ (UDPClient.getNumOfSendMessages()));
                    System.out.println("Received Messages :"+ (UDPClient.getNumOfReceivedMessages()));
                    System.out.println("Total Messages :"+ (UDPClient.getNumOfSendMessages()+UDPClient.getNumOfReceivedMessages()));
                }else if (commandOption.equals("5")){
                    try {
                        instance.unregister();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.exit(0);
                    break;
                } else {
                    System.out.println("Please enter a valid option...");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static int getFreePort() {
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
            throw new RuntimeException("Getting free port failed");
        }
    }

//    public App() throws SocketException {
//        Random rand = new Random();
//        int nodePort = rand.nextInt(10000) + 10000;
//        port(nodePort);
//        instance.setReceivingPort(nodePort+2000);
//        System.out.println("listening port " + instance.getReceivingPort());
//        try (final DatagramSocket socket = new DatagramSocket()){
//            socket.connect(InetAddress.getByName("8.8.8.8"), 10002);
//            String ip = socket.getLocalAddress().getHostAddress();
//            ipAddress(ip);
//            System.out.println("Web Interface : http://" + ip + ":" +nodePort);
//        } catch (Exception e){
//            throw new RuntimeException("Could not find host address");
//        }
//        instance.listening();
//
//        staticFileLocation("/public");
//        // registering call
//        get("/register", (req, res) -> {
//            QueryParamsMap map = req.queryMap();
//            try {
//                String serverIP = map.get("serverIP").value();
//                int serverPort = Integer.parseInt(map.get("serverPort").value());
//                String nodeIP = map.get("nodeIP").value();
//                String userName = map.get("userName").value();
//                String msg = "REG " + nodeIP + " " + instance.getReceivingPort() + " " + userName;
//                return instance.register(msg, serverIP, serverPort);
//            }
//            catch (Exception e){
//                return "Error: " + e.getMessage();
//            }
//        },json());
//
//        // getting files
//        get("/files", new Route() {
//            @Override
//            public Object handle(Request request, spark.Response response) throws Exception {
//                FileCollection files = FileCollection.getInstance();
//                UDPClient.printFiles();
//                return  files.getFiles();
//            }
//        },json());
//
//        // getting peers
//        get("/peers", new Route() {
//            @Override
//            public Object handle(Request request, spark.Response response) throws Exception {
//                UDPClient.printPeers();
//                return  UDPClient.getPeers();
//            }
//        },json());
//
//        // getting results
//        get("/results", new Route() {
//            @Override
//            public Object handle(Request request, spark.Response response) throws Exception {
//                UDPClient.printResults();
//                return  true;            }
//        },json());
//
//
//        // un registering
//        get("/unregister", new Route() {
//            @Override
//            public Object handle(Request request, Response response) throws Exception {
//                return instance.unregister();
//            }
//        },json());
//
//        // checking peer status
//        get("/check", new Route() {
//            @Override
//            public Object handle(Request request, Response response) throws Exception {
//                UDPClient.checkPeersAreAlive();
//                return  true;
//            }
//        },json());
//
//        // killing
//        get("/kill", new Route() {
//            @Override
//            public Object handle(Request request, Response response) throws Exception {
//                return instance.kill();
//            }
//        },json());
//
//
//        // find files
//        get("/findFiles", new Route() {
//            @Override
//            public Object handle(Request request, Response response) throws Exception {
//                QueryParamsMap map = request.queryMap();
//                try{
//                    String query = map.get("query").value();
//                    UDPClient.find(query);
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//                return Constants.SEROK;
//            }
//        });
//
//
//        // runQueries
//        get("/runQueries", new Route() {
//            @Override
//            public Object handle(Request request, Response response) throws Exception {
//                try{
//                    UDPClient.searchQueries();
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//                return Constants.SEROK;
//            }
//        });
//
//
//        // runQueries
//        get("/printStats", new Route() {
//            @Override
//            public Object handle(Request request, Response response) throws Exception {
//                try{
//                    UDPClient.printStats();
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//                return Constants.SEROK;
//            }
//        });
//    }

}
