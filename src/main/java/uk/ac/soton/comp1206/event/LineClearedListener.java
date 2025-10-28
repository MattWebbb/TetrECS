package uk.ac.soton.comp1206.event;

import java.util.HashSet;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * Listens for the event when a line in GameBoard is cleared
 */
public interface LineClearedListener {

  /**
   * Handle a line cleared event
   * @param blocks the blocks that are part of the line cleared
   */
  public void lineCleared(HashSet<GameBlockCoordinate> blocks);
}
