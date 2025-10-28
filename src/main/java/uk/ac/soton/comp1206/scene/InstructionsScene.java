package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * Instructions scene used for showing how the game is played
 */
public class InstructionsScene extends BaseScene{

  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * Create a new menu scene
   * @param gameWindow the Game Window this will be displayed in
   */
  public InstructionsScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Instruction Scene");
  }

  /**
   * Initialise the scene
   */
  public void initialise() {
    gameWindow.getScene().setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ESCAPE) {
        logger.info("Escape Pressed");
        gameWindow.startMenu();
      }
    });
  }

  /**
   * Build the menu layout
   */
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("menu-background");
    root.getChildren().add(menuPane);

    var instructionsTitle = new Label("How To Play");
    instructionsTitle.getStyleClass().add("title");

    var instructionsImage = new Image(MenuScene.class.getResource("/images/Instructions.png").toExternalForm());
    var imageView = new ImageView(instructionsImage);
    imageView.setPreserveRatio(true);
    imageView.setFitHeight(gameWindow.getHeight()/1.4);

    var piecesTitle = new Label("Game Pieces");
    piecesTitle.getStyleClass().add("heading");

    var pieces = new FlowPane();
    pieces.setHgap(4);
    for(int pieceNumber = 0; pieceNumber < 15; pieceNumber++){
      PieceBoard pieceBoard = new PieceBoard(new Grid(3,3), gameWindow.getHeight()/12.5, gameWindow.getHeight()/12.5);
      pieceBoard.DisplayPiece(GamePiece.createPiece(pieceNumber));
      pieces.getChildren().add(pieceBoard);
    }

    var mainPane = new VBox(10, instructionsTitle, imageView, piecesTitle, pieces);
    mainPane.setPadding(new Insets(10));
    mainPane.setAlignment(Pos.CENTER);
    menuPane.getChildren().add(mainPane);
  }
}
