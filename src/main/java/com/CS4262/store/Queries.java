package com.CS4262.store;

//import com.google.gson.Gson;

import java.util.List;
import java.util.UUID;

public class Queries {

    private UUID uuid;
    private String fileOwnerIP;
    private int fileOwnerPort;
    private String fileRequesterIP;
    private int fileRequesterPort;
    private String fileName;
    private int noOfHops;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getFileOwnerIP() {
        return fileOwnerIP;
    }

    public void setFileOwnerIP(String fileOwnerIP) {
        this.fileOwnerIP = fileOwnerIP;
    }

    public int getFileOwnerPort() {
        return fileOwnerPort;
    }

    public void setFileOwnerPort(int fileOwnerPort) {
        this.fileOwnerPort = fileOwnerPort;
    }

    public String getFileRequesterIP() {
        return fileRequesterIP;
    }

    public void setFileRequesterIP(String fileRequesterIP) {
        this.fileRequesterIP = fileRequesterIP;
    }

    public int getFileRequesterPort() {
        return fileRequesterPort;
    }

    public void setFileRequesterPort(int fileRequesterPort) {
        this.fileRequesterPort = fileRequesterPort;
    }

    public String getFileName() { return fileName;}

    public void setFileName(String filesName) { this.fileName = filesName; }

    public int getNoOfHops() {
        return noOfHops;
    }

    public void setNoOfHops(int noOfHops) {
        this.noOfHops = noOfHops;
    }

//    @Override
//    public String toString() {
//        return new Gson().toJson(this);
//    }
}
