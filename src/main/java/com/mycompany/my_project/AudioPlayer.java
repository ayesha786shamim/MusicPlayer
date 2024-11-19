package com.mycompany.my_project;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

public class AudioPlayer {

    private static final String VOLUME_KEY = "volume";
    private static final String PAUSE_POSITION_KEY = "pausePosition";

    Playlist playlist;
    private Song currentSong;
    private boolean isPlaying;
    private int volume;
    private long pausePosition;
    private Clip clip;

    public AudioPlayer(Playlist playlist) {
        this.playlist = playlist;
        this.currentSong = null;
        this.isPlaying = false;

        // Load volume and pause position from preferences
        Preferences prefs = Preferences.userNodeForPackage(AudioPlayer.class);
        this.volume = prefs.getInt(VOLUME_KEY, 50);
        this.pausePosition = prefs.getLong(PAUSE_POSITION_KEY, 0);

        // Initialize the Clip object
        try {
            clip = AudioSystem.getClip();
        } catch (LineUnavailableException e) {
            System.err.println("Failed to get Clip. AudioSystem.getClip() returned null.");
        }
    }

    public void play() {
        if (currentSong != null && !isPlaying) {
            try {
                File audioFile = new File(currentSong.getFilePath());

                if (!audioFile.exists()) {
                    System.err.println("Audio file not found: " + audioFile.getAbsolutePath());
                    return;
                }

                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFile);

                // Always open a new Clip for each playback
                clip.close();
                clip.open(audioInputStream);

                // Set volume every time before starting
                setVolume(volume);

                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP || event.getType() == LineEvent.Type.CLOSE) {
                        isPlaying = false;
                        // Save pause position and volume to preferences when playback stops
                        saveStateToPreferences();
                    }
                });

                if (pausePosition > 0) {
                    // If there is a pause position, set it before starting
                    clip.setMicrosecondPosition(pausePosition);
                    pausePosition = 0;  // Reset pause position to 0 after using it
                }

                clip.start();
                isPlaying = true;
                System.out.println("Playing: " + currentSong.getName());
            } catch (LineUnavailableException | UnsupportedAudioFileException | IOException e) {
                System.err.println("Error during playback: " + e.getMessage());
                e.printStackTrace();  // Log the exception for debugging
                isPlaying = false;
            }
        } else if (isPlaying) {
            System.out.println("Already playing");
        } else {
            // If there is no current song, try playing the next song
            playNextSong();
        }
    }

    public void pause() {
        if (isPlaying) {
            clip.stop();
            pausePosition = clip.getMicrosecondPosition();  // Save the current position
            isPlaying = false;
            // Save pause position and volume to preferences when playback is paused
            saveStateToPreferences();
        }
    }

    public void stop() {
        if (isPlaying) {
            clip.stop();
            clip.close();
            isPlaying = false;
            // Reset pause position to 0 when playback stops
            pausePosition = 0;
            // Save pause position and volume to preferences when playback stops
            saveStateToPreferences();
        }
    }

    public void setVolume(int volume) {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float gain = volumeControl.getMinimum() + (volumeControl.getMaximum() - volumeControl.getMinimum()) * volume / 100.0f;
            volumeControl.setValue(gain);
            this.volume = volume;
            // Save volume to preferences when volume is changed
            saveStateToPreferences();
        } else {
            System.err.println("Volume control not supported");
        }
    }

    private void saveStateToPreferences() {
        Preferences prefs = Preferences.userNodeForPackage(AudioPlayer.class);
        prefs.putInt(VOLUME_KEY, volume);
        prefs.putLong(PAUSE_POSITION_KEY, pausePosition);
    }

    public int getVolume() {
        return volume;
    }

    public void playNextSong() {
        Song nextSong = playlist.getNextSong();
        if (nextSong != null) {
            stop();
            setCurrentSong(nextSong);
            // Reset pause position to 0 when playing the next song
            pausePosition = 0;
            play();
        } else {
            System.out.println("No next song available.");
        }
    }

    public void playPreviousSong() {
        Song previousSong = playlist.getPreviousSong();
        if (previousSong != null) {
            stop();
            setCurrentSong(previousSong);
            // Reset pause position to 0 when playing the previous song
            pausePosition = 0;
            play();
        } else {
            System.out.println("No previous song available.");
        }
    }

    public void setCurrentSong(Song song) {
        if (song != null) {
            currentSong = song;
            System.out.println("Current song set to: " + currentSong.getName());
        } else {
            System.out.println("Cannot set current song. Song is null.");
        }
    }
}
