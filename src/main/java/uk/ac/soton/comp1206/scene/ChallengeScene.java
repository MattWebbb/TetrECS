package uk.ac.soton.comp1206.scene;

import java.util.HashSet;
import javafx.animation.FillTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.GameEndListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * The associated game
     */
    protected Game game;
    private int targetXCor = 2;
    private int targetYCor = 2;

    /**
     * The current game board
     */
    protected GameBoard board;

    /**
     * Piece board displaying the next piece to be placed
     */
    protected PieceBoard currentPiece;

    /**
     * Piece board displaying the next next piece to be placed
     */
    protected PieceBoard nextPiece;

    /**
     * Game block that is currently under the mouse
     */
    protected GameBlock hoverBlock;

    /**
     * Rectange displaying how long the player has to place a piece
     */
    protected Rectangle timer;

    /**
     * Main game pane
     */
    protected BorderPane mainPane;

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
        game.setNextPieceListener(nextPieceListener);
        game.setLineClearedListener(lineClearedListener);
        game.setGameLoopListener(gameLoopListener);
        game.setGameEndListener(gameEndListener);
    }

    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        keyboardInputHandler();
        game.start();
    }

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Challenge Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2, gameWindow.getWidth() / 2);
        mainPane.setCenter(board);

        //Handle block on gameboard grid being clicked
        board.setOnBlockClick(this::blockClicked);
        board.setOnMouseClicked(this::rightClickGameBoard);

        // Top pane

        Label challengeModeText = new Label("Challenge Mode");
        challengeModeText.getStyleClass().add("title");
        challengeModeText.setAlignment(Pos.CENTER);

        Label levelTextLabel = new Label("Level");
        levelTextLabel.getStyleClass().add("heading");

        Label levelText = new Label();
        levelText.getStyleClass().add("lives");
        levelText.textProperty().bind(game.getLevel().asString());

        var levelBox = new VBox(levelTextLabel, levelText);
        levelBox.setPadding(new Insets(10,0,0,0));
        levelBox.setAlignment(Pos.CENTER);

        Label livesTextLabel = new Label("Lives");
        livesTextLabel.getStyleClass().add("heading");

        Label livesText = new Label();
        livesText.getStyleClass().add("lives");
        livesText.textProperty().bind(game.getLives().asString());

        var livesBox = new VBox(livesTextLabel, livesText);
        livesBox.setPadding(new Insets(10,0,0,0));
        livesBox.setAlignment(Pos.CENTER);

        var topPane = new GridPane();
        topPane.add(levelBox, 0, 0);
        topPane.add(challengeModeText, 1, 0);
        topPane.add(livesBox, 2, 0);
        topPane.getColumnConstraints().add(new ColumnConstraints(gameWindow.getWidth() * 0.1));
        topPane.getColumnConstraints().add(new ColumnConstraints(gameWindow.getWidth() * 0.8));
        topPane.getColumnConstraints().add(new ColumnConstraints(gameWindow.getWidth() * 0.1));
        topPane.setHalignment(challengeModeText, HPos.CENTER);
        mainPane.setTop(topPane);

        // Left pane

        Label scoreTextLabel = new Label("Score");
        scoreTextLabel.getStyleClass().add("heading");

        Label scoreLabel = new Label();
        scoreLabel.getStyleClass().add("hiscore");
        scoreLabel.textProperty().bind(game.getScore().asString());

        Label highscoreTextLabel = new Label("Highscore");
        highscoreTextLabel.setPadding(new Insets(20,0,0,0));
        highscoreTextLabel.getStyleClass().add("heading");

        Label highscoreLabel = new Label();
        highscoreLabel.getStyleClass().add("level");
        highscoreLabel.textProperty().bind(game.getHighScore().asString());

        Label multiplierTextLabel = new Label("Multiplier");
        multiplierTextLabel.setPadding(new Insets(20,0,0,0));
        multiplierTextLabel.getStyleClass().add("heading");

        Label multiplierLabel = new Label();
        multiplierLabel.getStyleClass().add("multiplier");
        multiplierLabel.textProperty().bind(game.getMultiplier().asString("%dx"));

        var leftPane = new VBox(scoreTextLabel,scoreLabel, highscoreTextLabel, highscoreLabel, multiplierTextLabel, multiplierLabel);
        leftPane.setPadding(new Insets(30,20,30,40));
        leftPane.setAlignment(Pos.CENTER);
        mainPane.setLeft(leftPane);

        // Right Pane

        Label incomingLabel = new Label("Incoming");
        incomingLabel.setPadding(new Insets(10,0,0,0));
        incomingLabel.getStyleClass().add("heading");

        currentPiece = new PieceBoard(new Grid(3, 3), gameWindow.getWidth() / 6,gameWindow.getWidth() / 6);
        currentPiece.getBlock(1,1).setCircle(true);
        currentPiece.setPadding(new Insets(10,0,0,0));
        currentPiece.setOnMouseClicked(this::leftClickCurrentPieceBoard);

        nextPiece = new PieceBoard(new Grid(3, 3), gameWindow.getWidth() / 7,gameWindow.getWidth() / 7);
        nextPiece.setPadding(new Insets(20,0,0,0));
        nextPiece.setOnMouseClicked(this::leftClickNextPieceBoard);

        var rightPane = new VBox(incomingLabel, currentPiece, nextPiece);
        rightPane.setPadding(new Insets(20,32,20,8));
        rightPane.setAlignment(Pos.CENTER);
        mainPane.setRight(rightPane);

        // Bottom Pane

        timer = new Rectangle(gameWindow.getWidth() * 0.99, 25);
        timer.setFill(Paint.valueOf("Red"));
        var timerBox = new VBox(timer);
        timerBox.setPadding(new Insets(gameWindow.getWidth() * 0.005));
        timerBox.setAlignment(Pos.BOTTOM_LEFT);
        mainPane.setBottom(timerBox);

        new Multimedia().playMusic("game.wav");
    }

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    private void blockClicked(GameBlock gameBlock) {
        game.blockClicked(gameBlock);
    }

    /**
     * Handle when the board is right-clicked
     * @param e MouseEvent
     */
    private void rightClickGameBoard(MouseEvent e){
        if (e.getButton() == MouseButton.SECONDARY){
            game.rotateCurrentPiece();
        }
    }

    /**
     * Handle when the current piece board is left-clicked
     * @param e MouseEvent
     */
    private void leftClickCurrentPieceBoard(MouseEvent e){
        if (e.getButton() == MouseButton.PRIMARY){
            game.rotateCurrentPiece();
        }
    }

    /**
     * Handle when the next piece board is left-clicked
     * @param e MouseEvent
     */
    private  void leftClickNextPieceBoard(MouseEvent e){
        if (e.getButton() == MouseButton.PRIMARY){
            game.swapCurrentPiece();
        }
    }

    /**
     * Handles the users mouse position and keyboard input
     */
    protected void keyboardInputHandler(){

        GameBlock[][] blocks = board.getBlocks();

        for(GameBlock[] row : blocks){
            for(GameBlock block : row){
                block.setOnMouseMoved(mouseEvent -> {
                    targetXCor = block.getX();
                    targetYCor = block.getY();
                    hoverHandler(board.getBlock(targetXCor, targetYCor));
                });
                block.setOnMouseExited(mouseEvent -> {
                    board.getBlock(targetXCor, targetYCor).paint();
                });
            }
        }
        gameWindow.getScene().setOnKeyPressed(this::handleKeyPress);
    }

    /**
     * Handles a keypress
     * @param event keypress
     */
    protected void handleKeyPress(KeyEvent event){

        if(event.getCode() == KeyCode.ESCAPE) {
            logger.info("Escape Pressed");
            gameWindow.startMenu();
            escapePressed();
        }

        if (event.getCode() == KeyCode.W || event.getCode() == KeyCode.UP){
            if(targetYCor != 0){
                targetYCor -= 1;
                hoverHandler(board.getBlock(targetXCor, targetYCor));
            }
        }
        if (event.getCode() == KeyCode.S || event.getCode() == KeyCode.DOWN){
            if(targetYCor != 4){
                targetYCor += 1;
                hoverHandler(board.getBlock(targetXCor, targetYCor));
            }
        }
        if (event.getCode() == KeyCode.A || event.getCode() == KeyCode.LEFT){
            if(targetXCor != 0){
                targetXCor -= 1;
                hoverHandler(board.getBlock(targetXCor, targetYCor));
            }
        }
        if (event.getCode() == KeyCode.D || event.getCode() == KeyCode.RIGHT){
            if(targetXCor != 4){
                targetXCor += 1;
                hoverHandler(board.getBlock(targetXCor, targetYCor));
            }
        }
        if (event.getCode() == KeyCode.X || event.getCode() == KeyCode.ENTER){
            game.blockClicked(board.getBlock(targetXCor, targetYCor));
        }
        if (event.getCode() == KeyCode.Q || event.getCode() == KeyCode.Z || event.getCode() == KeyCode.OPEN_BRACKET){
            game.rotateCurrentPiece();
            game.rotateCurrentPiece();
            game.rotateCurrentPiece();
        }
        if (event.getCode() == KeyCode.E || event.getCode() == KeyCode.C || event.getCode() == KeyCode.CLOSE_BRACKET){
            game.rotateCurrentPiece();
        }
        if (event.getCode() == KeyCode.SPACE || event.getCode() == KeyCode.R){
            game.swapCurrentPiece();
        }
    }

    /**
     * Animates the timer
     */
    private void timerAnimation(){

        // Animate size
        logger.info("running timer Animation");
        timer.setScaleX(1);
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(game.calculateDelay()), new KeyValue(timer.scaleXProperty(), 0)));
        timeline.playFromStart();

        // Animate color
        timer.setFill(Color.GREEN);
        FillTransition colorChange = new FillTransition();
        colorChange.setDuration(Duration.millis((game.calculateDelay())));
        colorChange.setShape(timer);
        colorChange.setToValue(Color.RED);
        colorChange.play();
    }

    /**
     * Ends the game
     */
    protected void endGame(){
        gameWindow.startScores(game.getScore().getValue());
        escapePressed();
    }

    /**
     * Resets the game state and timer
     */
    public void escapePressed(){
        game.score.set(0);
        game.multiplier.set(1);
        game.lives.set(3);
        game.level.set(0);
        if(game.timer != null){game.timer.stop();}
    }

    /**
     * Handles painting a block which is hovered over
     * @param block block
     */
    public void hoverHandler(GameBlock block){
        if (this.hoverBlock != null) {
            this.hoverBlock.paint();
        }
        this.hoverBlock = block;
        this.hoverBlock.hover();
    }

    /**
     * Listens for the next game piece
     */
    NextPieceListener nextPieceListener = new NextPieceListener() {
        @Override
        public void nextPiece(GamePiece currentGamePiece, GamePiece nextGamePiece) {
            nextPiece.DisplayPiece(nextGamePiece);
            currentPiece.DisplayPiece(currentGamePiece);
        }
    };

    /**
     * Listens for when a line is cleared
     */
    LineClearedListener lineClearedListener = new LineClearedListener(){

        @Override
        public void lineCleared(HashSet<GameBlockCoordinate> blockSet) {

            for(GameBlockCoordinate blockCoordinate : blockSet){
                board.getBlock(blockCoordinate.getX(), blockCoordinate.getY()).fadeOut();
            }
        }
    };

    /**
     * Listens for a new game loop
     */
    GameLoopListener gameLoopListener = this::timerAnimation;

    /**
     * Listens for when the game ends
     */
    GameEndListener gameEndListener = this::endGame;
}
