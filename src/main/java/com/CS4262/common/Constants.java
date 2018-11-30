package com.CS4262.common;

public class Constants {

    public static final String BS_PROPERTIES = "Bootstrap.properties";
    public static final String LOCALHOST = "127.0.0.1";

    public static final String MSG_FORMAT = "%04d %s";

    public static final String REG_FORMAT = "REG %s %s %s";
    public static final String UNREG_FORMAT = "UNREG %s %s %s";

    public static final String REGOK = "REGOK";
    public static final String UNROK = "UNROK";
    public static final String JOIN = "JOIN";
    public static final String JOINOK = "JOINOK";
    public static final String LEAVE = "LEAVE";
    public static final String LEAVEOK = "LEAVEOK";
    public static final String ALIVE = "AVE";
    public static final String AVEOK = "AVEOK";
    public static final String SEARCH = "SER";
    public static final String SEROK = "SEROK";
    public static final String PING_FORMAT = "PING %s %s";
    public static final String PING = "PING";
    public static final String PONG_FORMAT = "PONG %s %s";
    public static final String PONG = "PONG";

    public static final int TIMEOUT_REG = 10000;
    public static final int SEARCH_TIMEOUT = 3000;
    public static final int FILE_DOWNLOAD_TIMEOUT = 2000;
    public static final int FTP_PORT_OFFSET = 100;

    public static final int PING_TIMEOUT = 3000;
    public static final int PING_INTERVAL = 8000;

    public static final int MIN_NEIGHBOURS = 2;
    public static final int MAX_NEIGHBOURS = 4;
}
