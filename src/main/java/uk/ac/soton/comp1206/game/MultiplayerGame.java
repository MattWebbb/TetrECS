package uk.ac.soton.comp1206.game;

import java.util.Objects;
import javafx.application.Platform;
import uk.ac.soton.comp1206.network.Communicator;

/**
 * Game with added multiplayer functionality
 */
public class MultiplayerGame extends Game{

  private Communicator communicator;

  /**
   * The ID of the next piece to be generated
   */
  private int pieceID = 0;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   * @param cols number of columns
   * @param rows number of rows
   * @param communicator The server event listener from multiplayerScene
   */
  public MultiplayerGame(int cols, int rows, Communicator communicator) {
    super(cols, rows);
    this.communicator = communicator;
    this.communicator.addListener(this::handleCommunications);
    this.spawnPiece();
  }

  /**
   * Handles messages received from the server
   * @param response message
   */
  private void handleCommunications(String response) {
    String[] responseSplit = response.split(" ", 2);
    if (Objects.equals(responseSplit[0], "PIECE")) {
      Platform.runLater(() -> pieceID = Integer.parseInt(responseSplit[1]));
    }
  }


  /**
   * Generate a new game piece based on an ID received from the server from the server
   * Also handles sending server the number of lives
   * @return The generated game piece
   */
  protected GamePiece spawnPiece(){

    communicator.send("PIECE");
    GamePiece piece = GamePiece.createPiece(pieceID);
    communicator.send("LIVES " + lives.get());
    // If number of lives is 0, exit lobby.
    if(lives.get() < 0){
      communicator.send("DIE");
    }
    return piece;
  }

  /**
   * Send sever current score
   * @param lines The number of lines cleared
   * @param blocks The number of blocks cleared
   */
  protected void score(int lines, int blocks){
    super.score(lines,blocks);
    communicator.send("SCORE " + score.get());
  }
}
