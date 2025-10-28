package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * Listens for the event when a new piece is created
 */
public interface NextPieceListener {

  /**
   * Handle a new piece being created
   * @param currentGamePiece the current game piece
   * @param nextGamePiece the next game piece
   */

  void nextPiece(GamePiece currentGamePiece, GamePiece nextGamePiece);

}
