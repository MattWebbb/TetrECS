package uk.ac.soton.comp1206.component;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

/**
 * A visual component to represent an individual piece
 */
public class PieceBoard extends GameBoard {

  private static final Logger logger = LogManager.getLogger(GameBlock.class);

  /**
   * Create a new PieceBoard, based off a given grid, with a visual width and height.
   * @param grid linked grid
   * @param width the visual width
   * @param height the visual height
   */
  public PieceBoard(Grid grid, double width, double height) {
    super(grid, width, height);
  }


  /**
   * Draws a given piece on the board
   * @param gamePiece piece to draw on the board
   */
  public void DisplayPiece(GamePiece gamePiece){
    logger.info("Piece displayed:" + gamePiece);
    int[][] blocks = gamePiece.getBlocks();
    for(int i = 0; i < blocks.length; i++){
      for (int j = 0; j < blocks[0].length; j++){
        grid.set(i, j, blocks[i][j]);
      }
    }
  }


}
