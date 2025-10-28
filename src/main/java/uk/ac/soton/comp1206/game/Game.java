package uk.ac.soton.comp1206.game;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import java.util.Random;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.GameEndListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.scene.Multimedia;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    Multimedia audioPlayer;

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    /**
     * The timer used to track how long a player has to place a piece
     */
    public Timeline timer;

    /**
     * The current game piece
     */
    protected GamePiece currentPiece;

    /**
     * The next game piece
     */
    protected GamePiece nextPiece;


    // Bindable properties used to bind the game state to the user interface

    /**
     * Property representing the score
     */
    public SimpleIntegerProperty score = new SimpleIntegerProperty(0);

    /**
     * Property representing the level
     */
    public SimpleIntegerProperty level = new SimpleIntegerProperty(0);

    /**
     * Property representing the lives
     */
    public SimpleIntegerProperty lives = new SimpleIntegerProperty(3);

    /**
     * Property representing the multiplier
     */
    public SimpleIntegerProperty multiplier = new SimpleIntegerProperty(1);

    /**
     * Property representing the highscore
     */
    public SimpleIntegerProperty highscore = new SimpleIntegerProperty(0);

    /**
     * The next piece listener
     */
    public NextPieceListener nextPieceListener;

    /**
     * The line cleared listener
     */
    public LineClearedListener lineClearedListener;

    /**
     * The game loop listener
     */
    public GameLoopListener gameLoopListener;

    /**
     * The game end listener
     */
    public GameEndListener gameEndListener;


    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        audioPlayer = new Multimedia();
        initialiseGame();
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        this.currentPiece = this.spawnPiece();
        this.nextPiece = this.spawnPiece();
        highscore();
        nextPieceListener.nextPiece(this.currentPiece, this.nextPiece);
        gameLoopListener.gameLoop();
        timer();
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public void blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        // If the piece can be played
        if (grid.canPlayPiece(this.currentPiece,x,y)){
            audioPlayer.playAudio("place.wav");
            grid.playPiece(this.currentPiece,x,y);
            this.nextPiece();
            this.afterPiece();
            nextPieceListener.nextPiece(this.currentPiece, this.nextPiece);
            timer();
        } else {
            audioPlayer.playAudio("fail.wav");
        }
    }

    /**
     * Generate a random game piece
     * @return the generated game piece
     */
    protected GamePiece spawnPiece(){
        Random random = new Random();
        return GamePiece.createPiece(random.nextInt(15));
    }

    /**
     * Assign new pieces to the current and next piece variables
     */
    public void nextPiece(){
        this.currentPiece = this.nextPiece;
        this.nextPiece = this.spawnPiece();
    }

    /**
     * Rotate the current piece 90 degrees right
     */
    public void rotateCurrentPiece(){
        currentPiece.rotate();
        nextPieceListener.nextPiece(this.currentPiece, this.nextPiece);
        audioPlayer.playAudio("rotate.wav");

    }

    /**
     * Swap the current piece with the next piece
     */
    public void swapCurrentPiece(){
        GamePiece temp = this.currentPiece;
        this.currentPiece = this.nextPiece;
        this.nextPiece = temp;
        nextPieceListener.nextPiece(this.currentPiece, this.nextPiece);

        audioPlayer.playAudio("rotate.wav");
    }

    /**
     * Starts a new timer and resets the game loop
     * This method is called every game loop until the player quits or runs out of lives
     */
    public void timer(){
        if (timer != null){timer.stop();}
        gameLoopListener.gameLoop();
        timer = new Timeline(new KeyFrame(Duration.millis(calculateDelay()), e -> {
            lives.set(lives.getValue()-1);
            nextPiece();
            nextPieceListener.nextPiece(currentPiece, nextPiece);
            multiplier.set(1);

            audioPlayer.playAudio("lifelose.wav");

            if(lives.getValue() < 0){
                gameEndListener.gameEnded();
            } else {
                timer();
            }
        }));
        timer.play();
    }

    /**
     * Performs tasks that need to be completed after a piece has been placed
     * Determines if any rows have been cleared
     * Updates players score, level and multiplier
     */
    public void afterPiece(){
        int linesToClear = 0;
        HashSet<GameBlockCoordinate> blockSet = new HashSet<>();

        //Check if any lines have been cleared
        for(int i = 0; i < getCols(); i++){
            if(grid.get(0, i) != 0 && grid.get(1, i) != 0 && grid.get(2, i) != 0 && grid.get(3, i) != 0 && grid.get(4, i) != 0){
                linesToClear += 1;
                blockSet.add(new GameBlockCoordinate(0, i));
                blockSet.add(new GameBlockCoordinate(1, i));
                blockSet.add(new GameBlockCoordinate(2, i));
                blockSet.add(new GameBlockCoordinate(3, i));
                blockSet.add(new GameBlockCoordinate(4, i));
            }
            if(grid.get(i,0) != 0 && grid.get(i,1) != 0 && grid.get(i,2) != 0 && grid.get(i,3) != 0 && grid.get(i,4) != 0) {
                linesToClear += 1;
                blockSet.add(new GameBlockCoordinate(i,0));
                blockSet.add(new GameBlockCoordinate(i,1));
                blockSet.add(new GameBlockCoordinate(i,2));
                blockSet.add(new GameBlockCoordinate(i,3));
                blockSet.add(new GameBlockCoordinate(i,4));
            }
        }

        // Update score, multiplier and level
        score(linesToClear, blockSet.size());
        multiplier(linesToClear);
        level();
        highscore();

        // Clear any completed lines from the board
        lineClearedListener.lineCleared(blockSet);
        for(GameBlockCoordinate block : blockSet){
            grid.set(block.getX(), block.getY(), 0);
        }
    }

    /**
     * Update the score based on how many lines and blocks are cleared
     * @param lines The number of lines cleared
     * @param blocks The number of blocks cleared
     */
    protected void score(int lines, int blocks){
        score.set(score.get() + (multiplier.getValue() * lines * blocks * 10));
    }

    /**
     * Updates the multiplier
     * @param lines The number of lines cleared
     */
    public void multiplier(int lines){
        if (lines > 0){
            multiplier.set(multiplier.get() + 1);
        } else {
            multiplier.set(1);
        }
    }

    /**
     * Loads the current high score from an external file
     * And updates it if the player is beating it
     */
    public void highscore(){
        File scoreFile = new File("Scores.txt");
        // Load current highscore from file
        try {
            scoreFile.createNewFile();
            List<String> scores = Files.readAllLines(scoreFile.toPath());
            if (!scores.isEmpty()){
                highscore.set(Integer.parseInt(scores.get(0).split(":")[1]));
            }
        } catch (Exception e){
            throw new RuntimeException();
        }
        // Update it if necessary
        if (score.getValue() > highscore.getValue()){
            highscore.set(score.getValue());
        }
    }

    /**
     * Update the players level based on their score
     */
    public void level(){
        int lastLevel = level.get();
        int currentLevel = (int) Math.floor(score.divide(1000).get());
        // If the level increases play audio
        if(lastLevel != currentLevel){
            audioPlayer.playAudio("level.wav");
        }
        level.set(currentLevel);
    }

    /**
     * Calculate the amount of time the player has to place a piece
     * @return The amount of time in milliseconds
     */
    public int calculateDelay(){
        return Math.max(12500 - (500 * level.get()), 2500);
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get the current level
     * @return the current level
     */
    public SimpleIntegerProperty getLevel() {
        return level;
    }

    /**
     * Get the current score
     * @return the current score
     */
    public SimpleIntegerProperty getScore() {
        return score;
    }

    /**
     * Get the current multiplier
     * @return the current multiplier
     */
    public SimpleIntegerProperty getMultiplier() {
        return multiplier;
    }

    /**
     * Get the current number of lives
     * @return the current number of lives
     */
    public SimpleIntegerProperty getLives() {
        return lives;
    }

    /**
     * Get the high score
     * @return the high score
     */
    public SimpleIntegerProperty getHighScore(){
        return highscore;
    }

    /**
     * Sets the next piece listener
     * @param nextPieceListener nextPieceListener from challengeScene
     */
    public void setNextPieceListener(NextPieceListener nextPieceListener) {
        this.nextPieceListener = nextPieceListener;
    }

    /**
     * Sets the line cleared listener
     * @param lineClearedListener lineClearedListener from challengeScene
     */
    public void setLineClearedListener(LineClearedListener lineClearedListener) {
        this.lineClearedListener = lineClearedListener;
    }

    /**
     * Sets the game loop listener
     * @param gameLoopListener gameLoopListener from challengeScene
     */
    public void setGameLoopListener(GameLoopListener gameLoopListener){
        this.gameLoopListener = gameLoopListener;
    }

    /**
     * Sets the game end listener
     * @param gameEndListener gameEndListener from challengeScene
     */
    public void setGameEndListener(GameEndListener gameEndListener){
        this.gameEndListener = gameEndListener;
    }

}
