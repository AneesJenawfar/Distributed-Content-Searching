package com.CS4262;

import com.CS4262.core.GNode;

import java.util.UUID;

public class App {

    public static void main( String[] args )
    {
        for (int i = 0; i < 5 ; i++) {
            String uniqueID = UUID.randomUUID().toString();

            try {
                GNode node = new GNode("node" + uniqueID);
                node.register();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }
}
