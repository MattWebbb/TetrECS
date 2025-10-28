package uk.ac.soton.comp1206.event;

/**
 * Used to listen for the event that a game ends
 */
public interface GameEndListener {

  /**
   * Handle a game ending
   */
  public void gameEnded();

}
