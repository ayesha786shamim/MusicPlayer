package com.mycompany.my_project;

public class SearchSong {

    private Node root;

    private static class Node {

        Song song;
        Node left, right;

        Node(Song song) {
            this.song = song;
            left = right = null;
        }
    }

    // Insert a song into the binary search tree
    public void insert(Song song) {
        root = insertRec(root, song);
    }

    private Node insertRec(Node root, Song song) {
        if (root == null) {
            root = new Node(song);
            return root;
        }

        // Compare the names of songs for insertion
        int comparisonResult = song.getName().compareTo(root.song.getName());

        // Recursively insert based on the comparison result
        if (comparisonResult < 0) {
            root.left = insertRec(root.left, song);
        } else if (comparisonResult > 0) {
            root.right = insertRec(root.right, song);
        }

        return root;
    }

    // SearchSong for a song by name in the binary search tree
    public Song search(String songName) {
        return searchRec(root, songName);
    }

    private Song searchRec(Node root, String songName) {
        if (root == null || root.song.getName().equals(songName)) {
            return (root != null) ? root.song : null;
        }

        int comparisonResult = songName.compareTo(root.song.getName());

        if (comparisonResult < 0) {
            return searchRec(root.left, songName);
        } else {
            return searchRec(root.right, songName);
        }
    }
}
