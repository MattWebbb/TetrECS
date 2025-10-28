package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GameWindow;
import javafx.util.Pair;

/**
 * Multiplayer scene that extends challenge scene
 */
public class MultiplayerScene extends ChallengeScene{

  private Communicator communicator = gameWindow.getCommunicator();
  private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

  /**
   * Vertical box containing the current leaderboard
   */
  VBox leaderboard = new VBox();

  /**
   * Vertical box containing received and sent messages
   */
  VBox messageBox = new VBox();

  /**
   * Vertical box containing all nodes related to the chat
   */
  VBox chatComponent = new VBox();

  /**
   * Text field where users can input messages
   */
  TextField sendMessageField = new TextField();

  /**
   * List of players currently in the game
   */
  private String[] playerList;

  /**
   * Whether the player is currently in the game
   */
  private boolean inGame = true;

  /**
   * Timer to control how often the leaderboard refreshes
   */
  Timeline timer;

  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public MultiplayerScene(GameWindow gameWindow) {
    super(gameWindow);
  }

  /**
   * Initialise the scene, game and timer
   */
  public void initialise() {
    super.initialise();
    communicator.addListener(this::handleCommunications);
    communicator.send("SCORES");
    timer();
    chatHandler();
  }

  /**
   * Setup game by creating new multiplayer game object and assigning listeners
   */
  public void setupGame(){
    game = new MultiplayerGame(5, 5, communicator);
    game.setNextPieceListener(nextPieceListener);
    game.setLineClearedListener(lineClearedListener);
    game.setGameLoopListener(gameLoopListener);
    game.setGameEndListener(gameEndListener);
  }

  /**
   * Build the multiplayerScene
   */
  public void build(){

    logger.info("Building " + this.getClass().getName());
    super.build();

    ScrollPane messageScroller = new ScrollPane();
    messageScroller.setFitToWidth(true);
    messageScroller.setMinSize(gameWindow.getWidth() * 0.2, gameWindow.getHeight() * 0.25);
    messageScroller.setMaxSize(gameWindow.getWidth() * 0.2, gameWindow.getHeight() * 0.25);
    messageScroller.setStyle("-fx-background: black");
    messageScroller.setContent(messageBox);
    messageBox.getStyleClass().add("multiplayerComponent");

    Label explanationText = new Label("Press T to chat!");
    explanationText.getStyleClass().add("messages");
    messageBox.getChildren().add(explanationText);

    chatComponent.getChildren().addAll(messageScroller);
    chatComponent.setOpacity(0.8);
    chatComponent.setPadding(new Insets(40,0,30,0));

    Label leaderboardLabel = new Label("       Leaderboard");
    leaderboardLabel.getStyleClass().add("multiplayerComponent");
    leaderboardLabel.setTextFill(Color.WHITE);

    leaderboard.setAlignment(Pos.CENTER);

    VBox multiplayerComponents = new VBox(leaderboardLabel, leaderboard, chatComponent);
    multiplayerComponents.setPadding(new Insets(50, 0,0,20));

    super.mainPane.setLeft(multiplayerComponents);
  }

  /**
   * Listen for the escape key
   * Load main menu if pressed
   */
  public void escapePressed() {
    super.escapePressed();
    inGame = false;
    communicator.send("DIE");
  }

  /**
   * Timer that regularly requests the leaderboard from the server
   */
  public void timer(){
    timer = new Timeline(new KeyFrame(Duration.millis(2000), e -> {
      if (inGame) {
        communicator.send("SCORES");
      } else {
        timer.stop();
      }
    }));
    timer.setCycleCount(Timeline.INDEFINITE);
    timer.play();
  }

  /**
   * Listens for responses from the server
   * @param response response
   */
  private void handleCommunications(String response) {
    String[] responseSplit = response.split(" ", 2);
    if (Objects.equals(responseSplit[0], "SCORES")) {
      if (!Objects.equals(responseSplit[1], "")) {
        Platform.runLater(() -> updateLeaderBoard(responseSplit[1]));
      }
    } else if (Objects.equals(responseSplit[0],"SCORE")){
      communicator.send("SCORES");
    } else if (Objects.equals(responseSplit[0], "MSG")){
      Platform.runLater(() -> displayMessage(responseSplit[1]));
    }
  }

  /**
   * Updates the leaderboard based on the scores received from the server
   * @param scores scores received from the server
   */
  public void updateLeaderBoard(String scores){
    playerList = scores.split("\n");
    leaderboard.getChildren().clear();

    for (int i = 0; i < playerList.length; i++) {
      for (int j = i + 1; j < playerList.length; j++) {
        if (Integer.parseInt(playerList[i].split(":")[1]) < Integer.parseInt(playerList[j].split(":")[1])) {
          String temp = playerList[i];
          playerList[i] = playerList[j];
          playerList[j] = temp;
        }
      }
    }

    for(String player : playerList){
      String[] playerSplit = player.split(":");
      Label playerLabel = new Label(playerSplit[0] + ": " + playerSplit[1]);
      playerLabel.getStyleClass().add("multiplayerComponent");
      if(Objects.equals(playerSplit[2], "3")){
        playerLabel.setTextFill(Color.GREEN);
      } else if (Objects.equals(playerSplit[2], "2")) {
        playerLabel.setTextFill(Color.YELLOW);
      } else if (Objects.equals(playerSplit[2], "1")) {
        playerLabel.setTextFill(Color.ORANGE);
      } else if (Objects.equals(playerSplit[2], "0")) {
        playerLabel.setTextFill(Color.RED);
      } else {
        playerLabel.setTextFill(Color.BLACK);
      }
      leaderboard.getChildren().add(playerLabel);
    }
  }

  /**
   * Display a received chat message in messageBox
   * @param message message
   */
  public void displayMessage(String message){
    logger.info("Message displayed: " + message);
    Label messageText = new Label(message);
    messageText.getStyleClass().add(message.split(":")[0] + ": " + message.split(":")[1]);
    messageText.setTextFill(Color.WHITE);
    messageBox.getChildren().add(messageText);
  }

  /**
   * Handles updating keybindings when in chat mode to make sure the game is not affected
   */
  public void chatHandler(){

    AtomicBoolean typing = new AtomicBoolean(false);

    // If the user is typing
    gameWindow.getScene().setOnKeyPressed((keyEvent) -> {
      if(keyEvent.getCode() == KeyCode.T) {

        typing.set(true);
        chatComponent.getChildren().remove(sendMessageField);
        sendMessageField = new TextField();
        sendMessageField.setMaxWidth(gameWindow.getWidth() * 0.2);
        sendMessageField.setStyle("-fx-font-family: Orbitron; -fx-font-size: 14px; -fx-background-color: black");
        sendMessageField.setAlignment(Pos.CENTER);
        chatComponent.getChildren().add(sendMessageField);

        // Only check for the enter key
        sendMessageField.setOnKeyPressed((key) -> {
          if (Objects.requireNonNull(key.getCode()) == KeyCode.ENTER) {
            if (!sendMessageField.getText().isEmpty()) {
              communicator.send("MSG " + sendMessageField.getText());
            }
            chatComponent.getChildren().remove(sendMessageField);
          }
        });

        // Else check for input like normal
      }else if(keyEvent.getCode() == KeyCode.ENTER){
        if (typing.get()){
          typing.set(false);
        } else {
          super.handleKeyPress(keyEvent);
        }
      } else{super.handleKeyPress(keyEvent);}
    });
  }

  /**
   * Handles the end of the game
   */
  @Override
  protected void endGame() {
    logger.info("Game ending");
    ArrayList<Pair<SimpleStringProperty, SimpleIntegerProperty>> simplePlayerList = new ArrayList<>();
    for(String player : playerList){
      simplePlayerList.add(new Pair<>(new SimpleStringProperty(player.split(":")[0]), new SimpleIntegerProperty(Integer.parseInt(player.split(":")[1]))));
    }
    gameWindow.startScores(game.getScore().getValue(),simplePlayerList);
    escapePressed();
  }
}
