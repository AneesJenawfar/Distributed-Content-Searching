package com.CS4262.common;

import com.CS4262.ftp.SendingData;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

public class FileCollection {
    private List<String> files = new ArrayList<String>();

    private static FileCollection instance;

    private final Logger LOG = Logger.getLogger(SendingData.class.getName());

    private String userName;

    private FileCollection(String userName) {

        List<String> list = new ArrayList<>();
        List<String> files = new ArrayList<String>();

        list.add("Adventures of Tintin");
        list.add("Jack and Jill");
        list.add("Glee");
        list.add("The Vampire Diarie");
        list.add("King Arthur");
        list.add("Windows XP");
        list.add("Harry Potter");
        list.add("Kung Fu Panda");
        list.add("Lady Gaga");
        list.add("Twilight");
        list.add("Windows 8");
        list.add("Mission Impossible");
        list.add("Turn Up The Music");
        list.add("Super Mario");
        list.add("American Pickers");
        list.add("Microsoft Office 2010");
        list.add("Happy Feet ");
        list.add("Modern Family");
        list.add("American Idol");
        list.add("Hacking for Dummies");

        this.userName= userName;

        // do shuffling to avoid getting same file again
        Collections.shuffle(list);

        Random rand = new Random();
        int num = rand.nextInt(2) + 3;
        for (int i = 0; i < num; i++) {
            files.add(list.get(i).replace(" ", "-"));
            createFile(list.get(i).replace(" ", "-"));
        }
        this.files = files;
    }

    public static FileCollection getInstance(String userName) {
        if (instance == null) {
            synchronized (FileCollection.class) {
                if (instance == null) {
                    instance = new FileCollection(userName);
                }
            }
        }
        return instance;
    }

    public List<String> searchFile(String query) {
        List<String> list = new ArrayList<String>();

        if (query != null && !query.trim().equals("")) {
            query = query.toLowerCase();
            for (String file : files) {
                if (file.toLowerCase().contains(query)) {
                    list.add(file.replaceAll(" ", "-"));
                }
            }
        }
        return list;
    }


    public List<String> getFiles() {
        return this.files;
    }

    @Override
    public String toString() {
        return files.toString();
    }

    public void createFile(String fileName) {
        try {
            String fileSeparator = System.getProperty("file.separator");
            String absoluteFilePath = "." + fileSeparator + this.userName + fileSeparator + fileName;
            File file = new File(absoluteFilePath);
            file.getParentFile().mkdir();
            if (file.createNewFile()) {
//                LOG.info(absoluteFilePath + " File Created");
            } else LOG.info("File " + absoluteFilePath + " already exists");

            RandomAccessFile r = new RandomAccessFile(file, "rw");
            r.setLength(1024 * 1024 * 10);
//            return file;
        } catch (IOException e){
            e.printStackTrace();
        }
    }
}
