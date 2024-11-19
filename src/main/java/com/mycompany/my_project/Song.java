package com.mycompany.my_project;

public class Song {
    private final String name;
    private final String filePath;
    public int duration = 0; // in seconds

    public Song(String name, String filePath,int duration) {
        this.name = name;
        this.filePath = filePath;
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getDuration() {
        return duration;
    }
}
