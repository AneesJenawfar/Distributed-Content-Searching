package com.CS4262.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class FileCollection {
    private List<String> files = new ArrayList<String>();

    private static FileCollection instance;


    private FileCollection() {

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


        // do shuffling to avoid getting same file again
        Collections.shuffle(list);

        Random rand = new Random();
        int num = rand.nextInt(2) + 3;
        for (int i = 0; i < num; i++) {
            files.add(list.get(i).replace(" ", "-"));
        }
        this.files = files;
    }

    public static FileCollection getInstance() {
        if (instance == null) {
            synchronized (FileCollection.class) {
                if (instance == null) {
                    instance = new FileCollection();
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
}
