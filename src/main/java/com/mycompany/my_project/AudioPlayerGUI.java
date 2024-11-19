package com.mycompany.my_project;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import javax.swing.ImageIcon;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.util.concurrent.ExecutionException;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;

public class AudioPlayerGUI extends javax.swing.JFrame {

    public AudioPlayer audioPlayer;
    private final Playlist folderPlaylist = new Playlist();
    private boolean isPlaying = false;  // Track whether a song is currently playing
    private final SearchSong searchTree = new SearchSong();

    private javax.swing.JLayeredPane layeredPane;
    private javax.swing.JLabel backgroundLabel;

    public AudioPlayerGUI() {
        super("AudioNaut");
        System.out.println("AudioPlayerGUI constructor called.");
        initComponents();
        // Initialize audioPlayer
        audioPlayer = new AudioPlayer(folderPlaylist);

        //background
        PANEL.setLayout(null);

        layeredPane = new javax.swing.JLayeredPane();
        layeredPane.setBounds(0, 0, PANEL.getWidth(), PANEL.getHeight());

        // Load and scale the background image
        var backgroundImageIcon = new ImageIcon("C:\\Users\\HP\\OneDrive\\Documents\\DSA project\\New Compressed (zipped) Folder\\icons\\b1-mp.jpg");
        Image backgroundImage = backgroundImageIcon.getImage();
        Image scaledBackgroundImage = backgroundImage.getScaledInstance(
                PANEL.getWidth(), PANEL.getHeight(), Image.SCALE_SMOOTH);

        // Set the scaled image as the background
        backgroundLabel = new javax.swing.JLabel(new ImageIcon(scaledBackgroundImage));
        backgroundLabel.setBounds(0, 0, PANEL.getWidth(), PANEL.getHeight());

        layeredPane.add(backgroundLabel, javax.swing.JLayeredPane.DEFAULT_LAYER);
        PANEL.add(layeredPane);

        PANEL.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent evt) {
                layeredPane.setBounds(0, 0, PANEL.getWidth(), PANEL.getHeight());
                Image scaledImage = backgroundImage.getScaledInstance(
                        PANEL.getWidth(), PANEL.getHeight(), Image.SCALE_SMOOTH);
                backgroundLabel.setIcon(new ImageIcon(scaledImage));
                backgroundLabel.setBounds(0, 0, PANEL.getWidth(), PANEL.getHeight());
            }
        });

        // Set the current song in the AudioPlayer to the first song in folderPlaylist
        Song firstSong = folderPlaylist.getCurrentSong();
        if (firstSong != null) {
            audioPlayer.setCurrentSong(firstSong);
            // Stop the audio player immediately after initializing it
            audioPlayer.stop();
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
            // Replace spaces with underscores in the song name
            songName = songName.replace(" ", "_");

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
                String selectedSong = selectedSongWithExtension.replace(" ", " ");

                // Get the file extension
                int dotIndex = selectedSongWithExtension.lastIndexOf(".");

                // Remove file extension from the selected song name for display
                String displaySongName = (dotIndex != -1) ? selectedSongWithExtension.substring(0, dotIndex) : selectedSongWithExtension;

                // Call the method to copy the song to the playlist folder
                boolean success = copySongToPlaylistFolder(selectedSongWithExtension, selectedSong);

                if (success) {
                    updatePlaylistSongsList();
                    showPopUp("Song Added to Playlist!", "C:\\Users\\HP\\OneDrive\\Documents\\DSA project\\New Compressed (zipped) Folder\\icons\\icons8-tick.gif");
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

    //pop up for add to playlist
    private void showPopUp(String message, String imagePath) {
        SwingUtilities.invokeLater(() -> {
            JFrame popUpFrame = new JFrame();
            popUpFrame.setUndecorated(true);
            popUpFrame.setSize(300, 100);
            popUpFrame.setLocationRelativeTo(this);

            JPanel popUpPanel = new JPanel();
            popUpPanel.setBackground(new Color(0, 0, 0, 255)); // Semi-transparent black background
            popUpPanel.setLayout(new BoxLayout(popUpPanel, BoxLayout.Y_AXIS));
            popUpPanel.setBorder(new RoundedBorder(30)); // Rounded borders with radius 30
            popUpFrame.add(popUpPanel);

            JLabel popUpLabel = new JLabel(message);
            popUpLabel.setForeground(Color.WHITE);
            popUpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            popUpPanel.add(popUpLabel);

            // Asynchronously load the image
            new LoadImageWorker(imagePath, popUpPanel).execute();

            Timer timer = new Timer(2500, (ActionEvent evt) -> {
                popUpFrame.dispose();
            });
            timer.setRepeats(false);
            timer.start();

            popUpFrame.setVisible(true);
        });
    }

    class LoadImageWorker extends SwingWorker<ImageIcon, Void> {

        private final String imagePath;
        private final JPanel popUpPanel;

        public LoadImageWorker(String imagePath, JPanel popUpPanel) {
            this.imagePath = imagePath;
            this.popUpPanel = popUpPanel;
        }

        @Override
        protected ImageIcon doInBackground() throws Exception {
            return new ImageIcon(imagePath);
        }

        @Override
        protected void done() {
            try {
                ImageIcon icon = get();
                JLabel iconLabel = new JLabel(icon);
                iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                popUpPanel.add(iconLabel);
                popUpPanel.revalidate();
                popUpPanel.repaint();
            } catch (InterruptedException | ExecutionException e) {
            }
        }
    }

    class RoundedBorder implements Border {

        private final int radius;

        public RoundedBorder(int radius) {
            this.radius = radius;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setColor(Color.WHITE);
            g2d.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2d.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            int borderWidth = radius / 2; // Adjusted to make sure the entire border is visible
            return new Insets(borderWidth, borderWidth, borderWidth, borderWidth);
        }

        @Override
        public boolean isBorderOpaque() {
            return true;
        }
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
                System.out.println("Copied song to My Playlist folder: " + selectedSong);
                return true; // Copy operation succeeded
            } catch (IOException ex) {
                System.err.println("Error copying song: " + ex.getMessage());
            }
        } else {
            // Print an error message if the source file is not found
            System.err.println("Source file not found: " + sourceFile.getAbsolutePath());
            System.err.println("No files found in the folder: " + sourceFolderPath);
        }
        return false; // Copy operation failed
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
            System.out.println("Files found in the folder: " + folderPath);
        } else {
            System.out.println("No files found in the folder: " + folderPath);
        }
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        playlistSongs = new java.awt.List();
        PANEL = new javax.swing.JPanel();
        ViewPlaylist = new javax.swing.JButton();
        SEARCH = new javax.swing.JButton();
        folderSongs = new java.awt.List();
        SEARCHSONG = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        NEXT = new javax.swing.JButton();
        STOP = new javax.swing.JButton();
        PAUSE = new javax.swing.JButton();
        PLAY = new javax.swing.JButton();
        PREVIOUS = new javax.swing.JButton();
        AddToPlaylist = new javax.swing.JButton();
        VOLUMESLIDER = new javax.swing.JSlider();
        Volume = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        PANEL.setBackground(new java.awt.Color(0, 0, 51));

        ViewPlaylist.setBackground(new java.awt.Color(0, 0, 0));
        ViewPlaylist.setForeground(new java.awt.Color(255, 255, 255));
        ViewPlaylist.setIcon(new javax.swing.ImageIcon("C:\\Users\\HP\\OneDrive\\Documents\\DSA project\\New Compressed (zipped) Folder\\icons\\icons8-playlist-30 (2).png")); // NOI18N
        ViewPlaylist.setText("View Playlist");

        SEARCH.setBackground(new java.awt.Color(0, 0, 0));
        SEARCH.setForeground(new java.awt.Color(255, 255, 255));
        SEARCH.setIcon(new javax.swing.ImageIcon("C:\\Users\\HP\\OneDrive\\Documents\\DSA project\\New Compressed (zipped) Folder\\icons\\icons8-search-26 (1).png")); // NOI18N
        SEARCH.setText("SEARCH");

        folderSongs.setBackground(new java.awt.Color(0, 31, 38));
        folderSongs.setFont(new java.awt.Font("Yu Gothic Medium", 0, 14)); // NOI18N
        folderSongs.setForeground(new java.awt.Color(255, 255, 255));

        SEARCHSONG.setBackground(new java.awt.Color(255, 255, 255));
        SEARCHSONG.setText("search");

        jLabel1.setFont(new java.awt.Font("Segoe Print", 1, 18)); // NOI18N
        jLabel1.setIcon(new javax.swing.ImageIcon("C:\\Users\\HP\\OneDrive\\Documents\\DSA project\\New Compressed (zipped) Folder\\icons\\icons8-playlist-29.png")); // NOI18N
        jLabel1.setText("AudioNaut");

        jLabel2.setBackground(new java.awt.Color(0, 0, 0));
        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setIcon(new javax.swing.ImageIcon("C:\\Users\\HP\\OneDrive\\Documents\\DSA project\\New Compressed (zipped) Folder\\icons\\icons8-music-folder-29.png")); // NOI18N
        jLabel2.setText("SONGS LIST");

        NEXT.setBackground(new java.awt.Color(0, 0, 0));
        NEXT.setForeground(new java.awt.Color(255, 255, 255));
        NEXT.setIcon(new javax.swing.ImageIcon("C:\\Users\\HP\\OneDrive\\Documents\\DSA project\\New Compressed (zipped) Folder\\icons\\icons8-next-30 (1).png")); // NOI18N
        NEXT.setText("NEXT");

        STOP.setBackground(new java.awt.Color(0, 0, 0));
        STOP.setForeground(new java.awt.Color(255, 255, 255));
        STOP.setIcon(new javax.swing.ImageIcon("C:\\Users\\HP\\OneDrive\\Documents\\DSA project\\New Compressed (zipped) Folder\\icons\\icons8-stop-20.png")); // NOI18N
        STOP.setText("STOP");

        PAUSE.setBackground(new java.awt.Color(0, 0, 0));
        PAUSE.setForeground(new java.awt.Color(255, 255, 255));
        PAUSE.setIcon(new javax.swing.ImageIcon("C:\\Users\\HP\\OneDrive\\Documents\\DSA project\\New Compressed (zipped) Folder\\icons\\icons8-pause-30.png")); // NOI18N
        PAUSE.setText("PAUSE");

        PLAY.setBackground(new java.awt.Color(0, 0, 0));
        PLAY.setForeground(new java.awt.Color(255, 255, 255));
        PLAY.setIcon(new javax.swing.ImageIcon("C:\\Users\\HP\\OneDrive\\Documents\\DSA project\\New Compressed (zipped) Folder\\icons\\icons8-play-30.png")); // NOI18N
        PLAY.setText("PLAY");

        PREVIOUS.setBackground(new java.awt.Color(0, 0, 0));
        PREVIOUS.setForeground(new java.awt.Color(255, 255, 255));
        PREVIOUS.setIcon(new javax.swing.ImageIcon("C:\\Users\\HP\\OneDrive\\Documents\\DSA project\\New Compressed (zipped) Folder\\icons\\icons8-previous-30.png")); // NOI18N
        PREVIOUS.setText("PREVIOUS");

        AddToPlaylist.setBackground(new java.awt.Color(0, 0, 0));
        AddToPlaylist.setForeground(new java.awt.Color(255, 255, 255));
        AddToPlaylist.setIcon(new javax.swing.ImageIcon("C:\\Users\\HP\\OneDrive\\Documents\\DSA project\\New Compressed (zipped) Folder\\icons\\icons8-add-29.png")); // NOI18N
        AddToPlaylist.setText("Add to PLaylist");

        VOLUMESLIDER.setBackground(new java.awt.Color(153, 153, 153));
        VOLUMESLIDER.setForeground(new java.awt.Color(0, 153, 0));

        Volume.setBackground(new java.awt.Color(0, 0, 0));
        Volume.setForeground(new java.awt.Color(255, 255, 255));
        Volume.setText("VOLUME");

        javax.swing.GroupLayout PANELLayout = new javax.swing.GroupLayout(PANEL);
        PANEL.setLayout(PANELLayout);
        PANELLayout.setHorizontalGroup(
            PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PANELLayout.createSequentialGroup()
                .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PANELLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 195, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(PANELLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 159, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(PANELLayout.createSequentialGroup()
                                .addComponent(SEARCHSONG, javax.swing.GroupLayout.PREFERRED_SIZE, 237, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(SEARCH, javax.swing.GroupLayout.PREFERRED_SIZE, 129, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(folderSongs, javax.swing.GroupLayout.PREFERRED_SIZE, 548, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PANELLayout.createSequentialGroup()
                        .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(PANELLayout.createSequentialGroup()
                                .addComponent(PREVIOUS)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(PLAY)
                                .addGap(18, 18, 18)
                                .addComponent(PAUSE))
                            .addGroup(PANELLayout.createSequentialGroup()
                                .addComponent(AddToPlaylist)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(VOLUMESLIDER, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, PANELLayout.createSequentialGroup()
                                        .addComponent(Volume)
                                        .addGap(89, 89, 89)))))
                        .addGap(37, 37, 37)
                        .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(STOP, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(NEXT, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 95, Short.MAX_VALUE)
                .addComponent(ViewPlaylist, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(91, 91, 91))
        );
        PANELLayout.setVerticalGroup(
            PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(PANELLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(PANELLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(SEARCHSONG, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(SEARCH))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2))
                    .addGroup(PANELLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addComponent(ViewPlaylist)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(folderSongs, javax.swing.GroupLayout.PREFERRED_SIZE, 242, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(26, 26, 26)
                .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(NEXT)
                    .addComponent(PAUSE)
                    .addComponent(PLAY)
                    .addComponent(PREVIOUS))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(AddToPlaylist)
                    .addGroup(PANELLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(STOP)
                        .addGroup(PANELLayout.createSequentialGroup()
                            .addComponent(VOLUMESLIDER, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(Volume))))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(541, 541, 541)
                .addComponent(playlistSongs, javax.swing.GroupLayout.PREFERRED_SIZE, 1, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(PANEL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(PANEL, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(playlistSongs, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> {
            System.out.println("Creating AudioPlayerGUI instance.");
            new AudioPlayerGUI().setVisible(true);
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private java.awt.List playlistSongs;
    // End of variables declaration//GEN-END:variables
}
