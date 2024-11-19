package com.mycompany.my_project;

import java.io.File;

public class My_Project {

    public static void main(String[] args) {
        AudioPlayerGUI1 audioPlayerGUI = new AudioPlayerGUI1();
        System.out.println("AudioPlayerGUI instance created");

        if (audioPlayerGUI.audioPlayer == null) {
            audioPlayerGUI.audioPlayer = new AudioPlayer(new Playlist()); // Create a Playlist if needed
        }

        // Read songs from a file 
        readSongsFromFile(audioPlayerGUI);

//        audioPlayerGUI.audioPlayer.play();
    }

    public static void readSongsFromFile(AudioPlayerGUI1 audioPlayerGUI) {
        // Folder's Path
        String folderPath = "C:\\Users\\HP\\OneDrive\\Desktop\\Wav";

        File folder = new File(folderPath);

        System.out.println("Path: " + folder.getAbsolutePath());
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Folder does not exist : ");
            return;
        }
        File[] files = folder.listFiles();
        if (files != null && files.length > 0) {
            int songCount = 0;
            for (File file : files) {
                if (file.isFile()) {
                    String songName = file.getName();
                    audioPlayerGUI.audioPlayer.playlist.addSong(new Song(songName, file.getAbsolutePath(), 0));
                    songCount++;
                }
            }
            if (songCount == 0) {
                System.out.println("No songs available in the folder.");
            } else {
                System.out.println("Loaded " + songCount + " songs into the playlist.");
                // Update the current song index in the AudioPlayer to the first song in the playlist
                audioPlayerGUI.audioPlayer.setCurrentSong(audioPlayerGUI.audioPlayer.playlist.getCurrentSong());
            }
        } else {
            System.out.println("No files found in the folder.");
        }
    }

}
