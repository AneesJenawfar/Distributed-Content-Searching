package com.CS4262.common;

import java.util.ArrayList;
import java.util.List;

public class QueryCollection {
    private static QueryCollection instance;
    private List<String> queries;
    public static QueryCollection getInstance() {
        if (instance == null) {
            synchronized (FileCollection.class) {
                if (instance == null) {
                    instance = new QueryCollection();
                }
            }
        }
        return instance;
    }
    private QueryCollection(){
        List<String> list = new ArrayList<String>();
        list.add("Twilight");
        list.add("Jack");
        list.add("American Idol");
        list.add("Happy Feet");
        list.add("Twilight saga");
        list.add("Happy Feet");
        list.add("Happy Feet");
        list.add("Feet");
        list.add("Happy Feet");
        list.add("Twilight");
        list.add("Windows");
        list.add("Happy Feet");
        list.add("Mission Impossible");
        list.add("Twilight");
        list.add("Windows 8");
        list.add("The");
        list.add("Happy");
        list.add("Windows 8");
        list.add("Happy Feet");
        list.add("Super Mario");
        list.add("Jack and Jill");
        list.add("Happy Feet");
        list.add("Impossible");
        list.add("Happy Feet");
        list.add("Turn Up The Music");
        list.add("Adventures of Tintin");
        list.add("Twilight saga");
        list.add("Happy Feet");
        list.add("Super Mario");
        list.add("American Pickers");
        list.add("Microsoft Office 2010");
        list.add("Twilight");
        list.add("Modern Family");
        list.add("Jack and Jill");
        list.add("Jill");
        list.add("Glee");
        list.add("The Vampire Diarie");
        list.add("King Arthur");
        list.add("Jack and Jill");
        list.add("King Arthur");
        list.add("Windows XP");
        list.add("Harry Potter");
        list.add("Feet");
        list.add("Kung Fu Panda");
        list.add("Lady Gaga");
        list.add("Gaga");
        list.add("Happy Feet");
        list.add("Twilight ");
        list.add("Hacking");
        list.add("King");
        this.queries = list;
    }

    public List<String> getQueries(){
        return this.queries;
    }
}
