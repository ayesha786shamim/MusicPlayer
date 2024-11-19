package com.mycompany.my_project;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;

public class AudioPlayerGUI1 extends javax.swing.JFrame {

    public AudioPlayer audioPlayer;
    private final Playlist folderPlaylist = new Playlist();
    private boolean isPlaying = false;  // Track whether a song is currently playing
    private final SearchSong searchTree = new SearchSong();

    public AudioPlayerGUI1() {
        super("VelvetBeats");
        System.out.println("AudioPlayerGUI constructor called.");
        initComponents();
        // Initialize audioPlayer
        audioPlayer = new AudioPlayer(folderPlaylist);

        // Set the current song in the AudioPlayer to the first song in folderPlaylist
        Song firstSong = folderPlaylist.getCurrentSong();
        if (firstSong != null) {
            audioPlayer.setCurrentSong(firstSong);
        } else {
            System.out.println("No songs in the folderPlaylist.");
        }

        this.setVisible(true);
        attachButtonListeners();

        // Initial population of lists
        updateSongsLists();
    }

    private void updateFolderSongsList() {
        String folderPath = "C:\\Users\\HP\\OneDrive\\Desktop\\Wav";
        readSongsFromFolder(folderPath, folderSongs);

        // Insert songs into the binary search tree
        for (String songName : folderSongs.getItems()) {
//            // Replace spaces with underscores in the song name
//            songName = songName.replace(" ", "_");

            // Remove file extension from song name
            int dotIndex = songName.lastIndexOf(".");
            if (dotIndex != -1) {
                songName = songName.substring(0, dotIndex);
            }

            Song song = new Song(songName, "C:\\Users\\HP\\OneDrive\\Desktop\\Wav", 0);
            searchTree.insert(song);
        }
    }

    private void attachButtonListeners() {

        PLAY.addActionListener((ActionEvent e) -> {
            System.out.println("PLAY ");
            if (isPlaying) {
                audioPlayer.stop();  // Stop the currently playing song
            }

            // Ensure that there is a current song before playing
            Song currentSong = folderPlaylist.getCurrentSong();
            if (currentSong != null) {
                audioPlayer.play();
                isPlaying = true;
            } else {
                System.out.println("No song selected.");
            }
        });

        PAUSE.addActionListener((ActionEvent e) -> {
            System.out.println("PAUSE ");
            audioPlayer.pause();
        });

        NEXT.addActionListener((ActionEvent e) -> {
            System.out.println("NEXT SONG ");
            audioPlayer.playNextSong();
        });

        PREVIOUS.addActionListener((ActionEvent e) -> {
            System.out.println("PREVIOUS SONG ");
            audioPlayer.playPreviousSong();
        });

        STOP.addActionListener((ActionEvent e) -> {
            System.out.println("STOP ");
            audioPlayer.stop();
        });

        VOLUMESLIDER.addChangeListener((ChangeEvent e) -> {
            int volume = VOLUMESLIDER.getValue();
            audioPlayer.setVolume(volume);
        });

        folderSongs.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedSong = folderSongs.getSelectedItem();
                if (!isPlaying) {
                    // Use the Playlist class to get the Song by name
                    Song selectedSongObject = folderPlaylist.getSongByName(selectedSong);

                    // Set the selected song in the AudioPlayer
                    audioPlayer.setCurrentSong(selectedSongObject);
                }
            }
        });

        playlistSongs.addItemListener((ItemEvent e) -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String selectedSong = playlistSongs.getSelectedItem();
                if (!isPlaying) {
                    // Use the Playlist class to get the Song by name
                    Song selectedSongObject = folderPlaylist.getSongByName(selectedSong);

                    // Set the selected song in the AudioPlayer
                    audioPlayer.setCurrentSong(selectedSongObject);
                }
            }
        });

        AddToPlaylist.addActionListener((ActionEvent e) -> {
            String selectedSongWithExtension = folderSongs.getSelectedItem();

            if (selectedSongWithExtension != null) {
                // Replace spaces with underscores in the selected song name
                String selectedSong = selectedSongWithExtension.replace(" ", "-");

                // Get the file extension
                int dotIndex = selectedSongWithExtension.lastIndexOf(".");

                // Remove file extension from the selected song name for display
                String displaySongName = (dotIndex != -1) ? selectedSongWithExtension.substring(0, dotIndex) : selectedSongWithExtension;

                // Call the method to copy the song to the playlist folder
                boolean success = copySongToPlaylistFolder(selectedSongWithExtension, selectedSong);

                if (success) {
                    updatePlaylistSongsList();
                } else {
                    // Display an error message if the copy operation fails
                    JOptionPane.showMessageDialog(this, "The song is already in the playlist", "Oops", JOptionPane.ERROR_MESSAGE);
                }

            } else {
                System.out.println("No song selected.");
            }
        });

        ViewPlaylist.addActionListener((ActionEvent e) -> {
            // Clear the existing items in the playlistSongs list
            playlistSongs.removeAll();

            // Read songs from the playlist and populate playlistSongs list
            updatePlaylistSongsList();
        });

        SEARCH.addActionListener((ActionEvent e) -> {

            String searchSongName = SEARCHSONG.getText();
            if (!searchSongName.isEmpty()) {
                // Search for the song in the binary search tree
                Song foundSong = searchTree.search(searchSongName);

                if (foundSong != null) {
                    System.out.println("Found Song: " + foundSong.getName());

                    // Highlight or scroll down to the found song in the list
                    highlightOrScrollToSong(foundSong.getName());
                } else {
                    // Handle case when the song is not found
                    System.out.println("Song not found: " + searchSongName);
                    JOptionPane.showMessageDialog(this, "Song not found: " + searchSongName, "Song Not Found", JOptionPane.INFORMATION_MESSAGE);
                }
            } else {
                System.out.println("Enter a song name to search.");
            }
        });

    }

    private void highlightOrScrollToSong(String songName) {
        for (int i = 0; i < folderSongs.getItemCount(); i++) {
            if (folderSongs.getItem(i).equalsIgnoreCase(songName)) {

                folderSongs.select(i);

                // Scroll down to the found song
                Container parent = folderSongs.getParent();
                if (parent instanceof JScrollPane jScrollPane) {
                    JScrollBar verticalScrollBar = jScrollPane.getVerticalScrollBar();
                    verticalScrollBar.setValue(i * verticalScrollBar.getUnitIncrement());
                } else {
                    // If the parent is not a JScrollPane, try to find it in the hierarchy
                    JScrollPane scrollPane = findScrollPaneInHierarchy(folderSongs);
                    if (scrollPane != null) {
                        JScrollBar verticalScrollBar = scrollPane.getVerticalScrollBar();
                        verticalScrollBar.setValue(i * verticalScrollBar.getUnitIncrement());
                    }
                }
                break;
            }
        }
    }

    private JScrollPane findScrollPaneInHierarchy(Component component) {
        Container parent = component.getParent();
        while (parent != null) {
            if (parent instanceof JScrollPane jScrollPane) {
                return jScrollPane;
            }
            parent = parent.getParent();
        }
        return null;
    }

    private void updateSongsLists() {
        updateFolderSongsList();

        updatePlaylistSongsList();
    }

    private void updatePlaylistSongsList() {
        String playlistPath = "C:\\Users\\HP\\OneDrive\\Desktop\\My Playlist";

        // Declare and initialize an array to store existing songs
        String[] existingSongs = new String[playlistSongs.getItemCount()];

        // Populate existingSongs with current items in the playlistSongs list
        for (int i = 0; i < existingSongs.length; i++) {
            existingSongs[i] = playlistSongs.getItem(i);
        }

        // Read songs from the playlist and add to playlistSongs list
        readSongsFromFolder(playlistPath, playlistSongs);

        // Remove file extension from playlist songs for display
        for (int i = 0; i < playlistSongs.getItemCount(); i++) {
            String songName = playlistSongs.getItem(i);
            int dotIndex = songName.lastIndexOf(".");
            if (dotIndex != -1) {
                songName = songName.substring(0, dotIndex);
            }

            // Check for duplicates
            if (contains(existingSongs, songName)) {
                // Remove duplicate entry
                playlistSongs.remove(songName);
            }

            playlistSongs.replaceItem(songName, i);
        }
    }

// Helper method to check if an array contains a specific value
    private boolean contains(String[] array, String value) {
        for (String item : array) {
            if (item != null && item.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean copySongToPlaylistFolder(String selectedSongWithExtension, String selectedSong) {
        String sourceFolderPath = "C:\\Users\\HP\\OneDrive\\Desktop\\Wav";
        String destinationFolderPath = "C:\\Users\\HP\\OneDrive\\Desktop\\My Playlist";

        // Hardcoded file extension
        String fileExtension = ".wav";

        // Construct the correct source file path
        String sourceFilePath = sourceFolderPath + File.separator + selectedSongWithExtension;

        // Check if the file extension is missing and add it
        if (!selectedSongWithExtension.endsWith(fileExtension)) {
            sourceFilePath += fileExtension;
        }

        // Print the source file path for debugging
        System.out.println("Source file path: " + sourceFilePath);

        // Construct the destination file path
        String destinationFilePath = destinationFolderPath + File.separator + selectedSong + fileExtension;

        // Check if the source file exists before copying
        File sourceFile = new File(sourceFilePath);
        if (sourceFile.exists()) {
            try {
                // Check if the destination file already exists in the playlist
                File destinationFile = new File(destinationFilePath);
                if (destinationFile.exists()) {
                    // If the file already exists, return false (copy operation failed)
                    return false;
                }

                // Copy the file to the destination folder
                Files.copy(sourceFile.toPath(), destinationFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Copied song to Playlist: " + selectedSong);
                return true; // Copy operation succeeded
            } catch (IOException ex) {
                ex.printStackTrace();
                System.err.println("Error copying song: " + ex.getMessage());
            }
        } else {
            // Print an error message if the source file is not found
            System.err.println("Source file not found: " + sourceFile.getAbsolutePath());
        }
        return false;
    }

    private void readSongsFromFolder(String folderPath, java.awt.List targetList) {
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Folder does not exist: " + folderPath);
            return;
        }

        File[] files = folder.listFiles();
        if (files != null && files.length > 0) {
            // Sort the files (songs) based on their names
            Arrays.sort(files);

            for (File file : files) {
                if (file.isFile()) {
                    // Add the song name without extension to the specified list (targetList)
                    String songName = file.getName();
                    int dotIndex = songName.lastIndexOf(".");
                    if (dotIndex != -1) {
                        songName = songName.substring(0, dotIndex);
                    }
                    targetList.add(songName);
                }
            }
        } else {
            System.out.println("No files found in the folder: " + folderPath);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        PANEL = new javax.swing.JPanel();
        PLAY = new javax.swing.JButton();
        PAUSE = new javax.swing.JButton();
        NEXT = new javax.swing.JButton();
        PREVIOUS = new javax.swing.JButton();
        STOP = new javax.swing.JButton();
        VOLUMESLIDER = new javax.swing.JSlider();
        Volume = new javax.swing.JLabel();
        folderSongs = new java.awt.List();
        playlistSongs = new java.awt.List();
        AddToPlaylist = new javax.swing.JButton();
        ViewPlaylist = new javax.swing.JButton();
        SEARCHSONG = new javax.swing.JTextField();
        SEARCH = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        PANEL.setBackground(new java.awt.Color(51, 51, 255));

        PLAY.setBackground(new java.awt.Color(255, 255, 0));
        PLAY.setForeground(new java.awt.Color(0, 0, 102));
        PLAY.setText("PLAY");

        PAUSE.setBackground(new java.awt.Color(255, 255, 0));
        PAUSE.setForeground(new java.awt.Color(0, 0, 102));
        PAUSE.setText("PAUSE");

        NEXT.setBackground(new java.awt.Color(255, 255, 0));
        NEXT.setForeground(new java.awt.Color(0, 0, 102));
        NEXT.setText("NEXT");

        PREVIOUS.setBackground(new java.awt.Color(255, 255, 0));
        PREVIOUS.setForeground(new java.awt.Color(0, 0, 102));
        PREVIOUS.setText("PREVIOUS");

        STOP.setBackground(new java.awt.Color(255, 255, 0));
        STOP.setForeground(new java.awt.Color(0, 0, 102));
        STOP.setText("STOP");

        VOLUMESLIDER.setBackground(new java.awt.Color(255, 255, 0));
        VOLUMESLIDER.setForeground(new java.awt.Color(255, 255, 0));

        Volume.setBackground(new java.awt.Color(255, 255, 0));
        Volume.setForeground(new java.awt.Color(255, 255, 0));
        Volume.setText("VOLUME");

        folderSongs.setBackground(new java.awt.Color(51, 51, 255));
        folderSongs.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        folderSongs.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        folderSongs.setForeground(new java.awt.Color(255, 255, 0));

        playlistSongs.setBackground(new java.awt.Color(51, 51, 255));
        playlistSongs.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        playlistSongs.setFont(new java.awt.Font("Times New Roman", 1, 14)); // NOI18N
        playlistSongs.setForeground(new java.awt.Color(255, 255, 0));

        AddToPlaylist.setBackground(new java.awt.Color(255, 255, 0));
        AddToPlaylist.setForeground(new java.awt.Color(0, 0, 102));
        AddToPlaylist.setText("Add to Playlist");

        ViewPlaylist.setBackground(new java.awt.Color(255, 255, 0));
        ViewPlaylist.setForeground(new java.awt.Color(0, 0, 102));
        ViewPlaylist.setText("View Playlist");

        SEARCHSONG.setBackground(new java.awt.Color(255, 255, 0));

        SEARCH.setBackground(new java.awt.Color(255, 255, 0));
        SEARCH.setForeground(new java.awt.Color(0, 0, 102));
        SEARCH.setText("SEARCH");

        javax.swing.GroupLayout PANELLayout = new javax.swing.GroupLayout(PANEL);
        PANEL.setLayout(PANELLayout);
        PANELLayout.setHorizontalGroup(
            PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PANELLayout.createSequentialGroup()
                .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(PANELLayout.createSequentialGroup()
                        .addComponent(PREVIOUS)
                        .addGap(24, 24, 24)
                        .addComponent(PLAY)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(PAUSE)
                        .addGap(18, 18, 18)
                        .addComponent(NEXT))
                    .addComponent(folderSongs, javax.swing.GroupLayout.PREFERRED_SIZE, 365, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PANELLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(playlistSongs, javax.swing.GroupLayout.PREFERRED_SIZE, 378, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PANELLayout.createSequentialGroup()
                        .addComponent(STOP)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(ViewPlaylist)
                            .addComponent(VOLUMESLIDER, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(14, 14, 14))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PANELLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(Volume)
                .addGap(94, 94, 94))
            .addGroup(PANELLayout.createSequentialGroup()
                .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PANELLayout.createSequentialGroup()
                        .addGap(137, 137, 137)
                        .addComponent(AddToPlaylist))
                    .addGroup(PANELLayout.createSequentialGroup()
                        .addGap(178, 178, 178)
                        .addComponent(SEARCHSONG, javax.swing.GroupLayout.PREFERRED_SIZE, 220, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(SEARCH)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        PANELLayout.setVerticalGroup(
            PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PANELLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(SEARCHSONG, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SEARCH))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(folderSongs, javax.swing.GroupLayout.DEFAULT_SIZE, 245, Short.MAX_VALUE)
                    .addComponent(playlistSongs, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(AddToPlaylist, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(ViewPlaylist, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(PLAY)
                    .addComponent(PAUSE)
                    .addComponent(NEXT)
                    .addComponent(PREVIOUS)
                    .addComponent(STOP)
                    .addComponent(VOLUMESLIDER, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(5, 5, 5)
                .addComponent(Volume))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PANEL, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(PANEL, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            System.out.println("Creating AudioPlayerGUI instance.");
            new AudioPlayerGUI1().setVisible(true);
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton AddToPlaylist;
    private javax.swing.JButton NEXT;
    private javax.swing.JPanel PANEL;
    private javax.swing.JButton PAUSE;
    private javax.swing.JButton PLAY;
    private javax.swing.JButton PREVIOUS;
    private javax.swing.JButton SEARCH;
    private javax.swing.JTextField SEARCHSONG;
    private javax.swing.JButton STOP;
    private javax.swing.JSlider VOLUMESLIDER;
    private javax.swing.JButton ViewPlaylist;
    private javax.swing.JLabel Volume;
    private java.awt.List folderSongs;
    private java.awt.List playlistSongs;
    // End of variables declaration//GEN-END:variables

}
