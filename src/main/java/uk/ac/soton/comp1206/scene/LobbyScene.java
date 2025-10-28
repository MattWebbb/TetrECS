package uk.ac.soton.comp1206.scene;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * Lobby scene with options to create and join games, as well as changing nicknames and sending messages to chat
 */
public class LobbyScene extends BaseScene{
  private static final Logger logger = LogManager.getLogger(LobbyScene.class);
  private Communicator communicator = gameWindow.getCommunicator();
  private Multimedia multimedia = new Multimedia();


  /**
   * Timer to control how often list of games refreshes
   */
  private Timeline timer;

  /**
   * Vertical box containing all available games
   */
  private VBox gamesBox = new VBox();

  /**
   * Vertical box containing all chat messages
   */
  private VBox messageBox = new VBox();

  /**
   * Flow pane containing all users in a given game
   */
  private FlowPane usersList = new FlowPane();

  /**
   * Whether the user is in a game
   */
  private boolean inGame = false;

  /**
   * Initialise the scene
   */
  public void initialise() {
    communicator.addListener(this::handleCommunications);
    communicator.send("LIST");
    escapeListener();
  }

  /**
   * Create a new menu scene
   * @param gameWindow the Game Window this will be displayed in
   */
  public LobbyScene(GameWindow gameWindow) {
    super(gameWindow);
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());
    logger.info("Creating Instruction Scene");
  }

  /**
   * Build the menu layout
   */
  public void build() {

    logger.info("Building " + this.getClass().getName());

    escapeListener();
    inGame = false;
    timer();

    var lobbyPane = new StackPane();
    lobbyPane.setMaxWidth(gameWindow.getWidth());
    lobbyPane.setMaxHeight(gameWindow.getHeight());
    lobbyPane.getStyleClass().add("menu-background");
    root.getChildren().add(lobbyPane);

    var title = new Image(MenuScene.class.getResource("/images/TetrECS.png").toExternalForm());
    ImageView imageView = new ImageView(title);
    imageView.setPreserveRatio(true);
    imageView.setFitHeight(120);

    // General user interface
    Label availableGamesText = new Label("Available Games:");
    availableGamesText.getStyleClass().add("title");
    availableGamesText.setAlignment(Pos.CENTER);

    VBox gameInfoBox = new VBox(10, imageView, availableGamesText, gamesBox);
    gameInfoBox.setPadding(new Insets(30,0,0,0));
    gameInfoBox.setAlignment(Pos.TOP_CENTER);
    lobbyPane.getChildren().add(gameInfoBox);

    Button createNewGameButton = new Button("Create New Game");
    gameInfoBox.getChildren().add(createNewGameButton);
    createNewGameButton.getStyleClass().add("channelItem");
    createNewGameButton.setAlignment(Pos.CENTER);

    TextField textBox = new TextField();
    textBox.setStyle("-fx-font-family: Orbitron; -fx-font-size: 14px");
    final boolean[] createNewGameButtonActivated = {false};

    // Create new game button
    createNewGameButton.setOnAction(event -> {
      if (!createNewGameButtonActivated[0]) {

        createNewGameButtonActivated[0] = true;
        textBox.textProperty().set("");
        gameInfoBox.getChildren().add(textBox);
        textBox.setMaxWidth(gameWindow.getWidth() * 0.2);
        textBox.setAlignment(Pos.CENTER);

        this.scene.setOnKeyPressed((e) -> {

          if (Objects.requireNonNull(e.getCode()) == KeyCode.ENTER) {
            gameInfoBox.getChildren().remove(textBox);
            communicator.send("CREATE " + textBox.getText());
            loadGameInformation(textBox.getText(), true);
          } else if(Objects.requireNonNull(e.getCode()) == KeyCode.ESCAPE) {
            inGame = true;
            logger.info("Escape Pressed");
            gameWindow.startMenu();
          }
        });
      } else {
        createNewGameButtonActivated[0] = false;
        gameInfoBox.getChildren().remove(textBox);
        communicator.send("LIST");
      }

    });
  }

  /**
   * Load the information for a particular game including players and chat messages
   * @param gameName The name of the game
   * @param isHost Whether the player is the host
   */
  public void loadGameInformation(String gameName, boolean isHost){

    logger.info("Loading game information for: " + gameName);
    multimedia.playAudio("message.wav");
    root.getChildren().clear();
    messageBox.getChildren().clear();
    inGame = true;

    // Load general game information
    var gamePane = new StackPane();
    gamePane.setMaxWidth(gameWindow.getWidth());
    gamePane.setMaxHeight(gameWindow.getHeight());
    gamePane.getStyleClass().add("menu-background");
    root.getChildren().add(gamePane);

    Label gameTitle = new Label(gameName);
    gameTitle.getStyleClass().add("bigtitle");
    gameTitle.setAlignment(Pos.CENTER);

    Button leaveGameButton = new Button("Leave Game");
    leaveGameButton.getStyleClass().add("channelItem");
    leaveGameButton.setAlignment(Pos.CENTER);
    leaveGameButton.setOnAction(event -> {
      communicator.send("PART");
      communicator.send("LIST");
      root.getChildren().clear();
      build();
    });

    Button startGameButton = new Button("Start Game");
    startGameButton.getStyleClass().add("channelItem");
    startGameButton.setAlignment(Pos.CENTER);
    startGameButton.setOnAction(event -> {
      communicator.send("START");
    });

    // If the user is not the host disable the start game button
    if(!isHost){
      startGameButton.setDisable(true);
    }

    var titlePane = new GridPane();
    titlePane.add(leaveGameButton, 0, 0);
    titlePane.add(gameTitle, 1, 0);
    titlePane.add(startGameButton, 2, 0);
    titlePane.getColumnConstraints().add(new ColumnConstraints(gameWindow.getWidth() * 0.25));
    titlePane.getColumnConstraints().add(new ColumnConstraints(gameWindow.getWidth() * 0.5));
    titlePane.getColumnConstraints().add(new ColumnConstraints(gameWindow.getWidth() * 0.25));
    titlePane.setHalignment(gameTitle, HPos.CENTER);
    titlePane.setHalignment(leaveGameButton, HPos.CENTER);
    titlePane.setHalignment(startGameButton, HPos.CENTER);

    // Chat box
    ScrollPane messageScroller = new ScrollPane();
    messageScroller.setFitToWidth(true);
    messageScroller.setMinSize(gameWindow.getWidth() * 0.75, gameWindow.getHeight() * 0.5);
    messageScroller.setMaxSize(gameWindow.getWidth() * 0.75, gameWindow.getHeight() * 0.5);
    messageScroller.setStyle("-fx-background: black");
    messageScroller.setContent(messageBox);

    TextField sendMessageField = new TextField();
    sendMessageField.setMaxWidth(gameWindow.getWidth() * 0.75);
    sendMessageField.setStyle("-fx-font-family: Orbitron; -fx-font-size: 14px; -fx-background-color: black");
    sendMessageField.setAlignment(Pos.CENTER);

    Label explanationText = new Label("Welcome to the lobby! Use /nick NEWNAME to change your nickname.");
    explanationText.getStyleClass().add("messages");
    messageBox.getChildren().add(explanationText);

    VBox chatComponent = new VBox(messageScroller, sendMessageField);
    chatComponent.setOpacity(0.8);
    chatComponent.setPadding(new Insets(30,0,0,0));

    chatComponent.setAlignment(Pos.CENTER);

    // Handle sending chat messages
    this.scene.setOnKeyPressed((e) -> {
          if (Objects.requireNonNull(e.getCode()) == KeyCode.ENTER) {
            if(sendMessageField.getText().startsWith("/nick ")){
              String nickname = sendMessageField.getText().substring(6);
              communicator.send("NICK " + nickname);
            } else if (!sendMessageField.getText().isEmpty()){
              multimedia.playAudio("message.wav");
              communicator.send("MSG " + sendMessageField.getText());
            }
            sendMessageField.clear();
          }
        });

    var gameInformation = new VBox(10, titlePane, usersList, chatComponent);
    gamePane.getChildren().add(gameInformation);
  }

  /**
   * Timer to load a list of games at regular intervals
   */
  public void timer(){
    timer = new Timeline(new KeyFrame(Duration.millis(2000), e -> {
      if (!inGame) {
        communicator.send("LIST");
      } else {
        timer.stop();
      }
    }));
    timer.setCycleCount(Timeline.INDEFINITE);
    timer.play();
  }

  /**
   * Listen for the escape key
   * Load main menu if pressed
   */
  public void escapeListener(){
    gameWindow.getScene().setOnKeyPressed(event -> {
      if (Objects.requireNonNull(event.getCode()) == KeyCode.ESCAPE) {
        inGame = true;
        logger.info("Escape Pressed");
        gameWindow.startMenu();
      }
    });
  }

  /**
   * Listen for responses from the server
   * @param response response
   */
  private void handleCommunications(String response){
    String[] responseSplit = response.split(" ", 2);
    if(Objects.equals(responseSplit[0], "CHANNELS")){
      if(!Objects.equals(responseSplit[1], "")) {
        Platform.runLater(() -> displayChannels(responseSplit[1]));
      } else {
        Platform.runLater(() -> gamesBox.getChildren().clear());
      }
    } else if (Objects.equals(responseSplit[0],"USERS")) {
      Platform.runLater(() -> displayUsers(responseSplit[1]));
    } else if (Objects.equals(responseSplit[0],"MSG")){
      Platform.runLater(() -> displayMessage(responseSplit[1]));
    } else if (Objects.equals(responseSplit[0], "ERROR")){
      Platform.runLater(() -> displayError(responseSplit[1]));
    } else if (Objects.equals(responseSplit[0], "START")){
      Platform.runLater(gameWindow::startMultiplayerGame);
    }
  }

  /**
   * Handle errors received by the server
   * @param errorMessage error message
   */
  public void displayError(String errorMessage){
    logger.info("Error occurred: " + errorMessage);
    build();
    Alert error = new Alert(AlertType.ERROR);
    error.setContentText(errorMessage);
    error.showAndWait();
  }

  /**
   * Load a list of available games based on the servers response
   * @param games List of available games
   */
  public void displayChannels(String games){
    String[] gameList = games.split("\n");
    gamesBox.getChildren().clear();
    gamesBox.setAlignment(Pos.CENTER);

    // Loop through all games and add them to a vertical box
    for(String game : gameList) {
      Button gameButton = new Button("Game " + (new ArrayList<>(List.of(gameList)).indexOf(game) + 1) + ": " + game);
      gameButton.getStyleClass().add("channelItem");
      gameButton.setAlignment(Pos.CENTER);
      gameButton.setOnAction(event -> {
        communicator.send("JOIN " + game);
        loadGameInformation(game, false);
      });
      gamesBox.getChildren().add(gameButton);
    }
  }

  /**
   * Display all users connected to the game
   * @param users users
   */
  public void displayUsers(String users){
    String[] userList = users.split("\n");

    usersList.getChildren().clear();
    Label usersText = new Label("Players: ");
    usersText.getStyleClass().add("heading");
    usersText.setAlignment(Pos.CENTER);
    usersList.getChildren().add(usersText);
    usersList.setAlignment(Pos.CENTER);
    usersList.setPadding(new Insets(30,0,0,0));

    // Loop through all users and add them to a flowPane
    for(String user : userList){
      Label userText = new Label(user);
      userText.getStyleClass().add("heading");
      Label comma = new Label(", ");
      comma.getStyleClass().add("heading");
      usersList.getChildren().add(userText);
      usersList.getChildren().add(comma);
    }
    usersList.getChildren().get(usersList.getChildren().size() - 1).setVisible(false);
  }

  /**
   * Display the received message in messageBox
   * @param message received message
   */
  public void displayMessage(String message){
    logger.info("Chat message displayed");
    Label messageText = new Label(message.split(":")[0] + ": " + message.split(":")[1]);
    messageText.getStyleClass().add("messages");
    messageBox.getChildren().add(messageText);
  }
}
