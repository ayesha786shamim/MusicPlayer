package com.mycompany.my_project;

import java.util.LinkedList;

public class Playlist {

    private final LinkedList<Song> songs;
    private int currentSongIndex;

    public Playlist() {
        songs = new LinkedList<>();
        currentSongIndex = -1;
    }

    public void addSong(Song song) {
        songs.add(song);
        if (currentSongIndex == -1) {
            currentSongIndex = 0;
        }
    }

    public Song getCurrentSong() {
        if (!songs.isEmpty() && currentSongIndex >= 0 && currentSongIndex < songs.size()) {
            return songs.get(currentSongIndex);
        }
        return null;
    }

    public Song getNextSong() {
        if (!songs.isEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % songs.size();
            System.out.println("Advancing to next song. New index: " + currentSongIndex);
            return songs.get(currentSongIndex);
        } else {
            System.out.println("No songs available in the playlist.");
            return null;
        }
    }

    public Song getPreviousSong() {
        if (!songs.isEmpty()) {
            currentSongIndex = (currentSongIndex - 1 + songs.size()) % songs.size();
            return songs.get(currentSongIndex);
        } else {
            System.out.println("No songs available in the playlist.");
            return null;
        }
    }

    public boolean isEmpty() {
        return songs.isEmpty();
    }

    public Song getSongByName(String selectedSong) {
        for (Song song : songs) {
            if (song.getName().equals(selectedSong)) {
                return song;
            }
        }
        // Return null if no matching song is found
        return null;
    }
    public boolean containsSong(String songName) {
        for (Song song : songs) {
            if (song.getName().equals(songName)) {
                return true;
            }
        }
        return false;
    }
}
