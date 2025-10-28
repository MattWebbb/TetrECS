package uk.ac.soton.comp1206.scene;

import java.util.Objects;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handle all audio and music played in the game and user interface
 */
public class Multimedia {

  private static final Logger logger = LogManager.getLogger(Multimedia.class);

  /**
   * Sound effects player
   */
  private static MediaPlayer audioPlayer;

  /**
   * Music player
   */
  private static MediaPlayer musicPlayer;

  /**
   * Sound effects volume between 0-1
   */
  public static double audioVolume = 0.5;

  /**
   * Sound effects volume between 0-1
   */
  public static double musicVolume = 0.5;


  /**
   * Play a music track indefinitely
   * @param file The file to play
   */
  public void playMusic(String file){
    logger.info("Playing" + file);
    // Stop any currently playing music
    if(musicPlayer != null){
      musicPlayer.stop();
    }
    var music = Multimedia.class.getResource("/music/" + file).toExternalForm();
    musicPlayer = new MediaPlayer(new Media(music));
    musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
    musicPlayer.setVolume(musicVolume);
    musicPlayer.play();
  }

  /**
   * Play a sound effect
   * @param file the sound effect to play
   */
  public void playAudio(String file){
    logger.info("Playing" + file);
    // Stop any currently playing sound effect
    if(audioPlayer != null){
      audioPlayer.stop();
    }
    var audio = Objects.requireNonNull(Multimedia.class.getResource("/sounds/" + file)).toExternalForm();
    audioPlayer = new MediaPlayer(new Media(audio));
    audioPlayer.setVolume(audioVolume);
    audioPlayer.play();
  }

  /**
   * Update the volume of currently playing music
   */
  public static void changeMusicVolume(){
    musicPlayer.setVolume(musicVolume);
  }

}