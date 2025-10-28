package uk.ac.soton.comp1206.scene;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.ScoresList;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * Scene to display scores to the player
 * Loaded after a game has ended
 */
public class ScoresScene extends BaseScene{
  private static final Logger logger = LogManager.getLogger(MenuScene.class);
  private  Multimedia audioPlayer = new Multimedia();
  private Communicator communicator = gameWindow.getCommunicator();

  /**
   * Arraylist of local scores
   */
  public ArrayList<Pair<SimpleStringProperty, SimpleIntegerProperty >> localScores = new ArrayList<>();

  /**
   * Arraylist of online scores
   */
  public  ArrayList<Pair<SimpleStringProperty, SimpleIntegerProperty>> onlineScores = new ArrayList<>();

  /**
   * Arraylist of multiplayer scores
   */
  public  ArrayList<Pair<SimpleStringProperty, SimpleIntegerProperty>> gameScores = new ArrayList<>();

  /**
   * Players score from the previous round
   */
  private Pair<SimpleStringProperty, SimpleIntegerProperty> playerScore;

  /**
   * Initialise the scene
   */
  public void initialise() {
    gameWindow.getScene().setOnKeyPressed(event -> {
      if(event.getCode() == KeyCode.ESCAPE) {
        logger.info("Escape Pressed");
        gameWindow.startMenu();
      }
    });
  }

  /**
   * Create a new scores scene
   * @param gameWindow the Game Window this will be displayed in
   * @param score The players score
   */
  public ScoresScene(GameWindow gameWindow, int score) {
    super(gameWindow);
    playerScore = new Pair<>(new SimpleStringProperty(""), new SimpleIntegerProperty(score));
  }

  /**
   * Builds a score scene with multiplayer scores
   * @param gameWindow The game window
   * @param gameScores multiplayer scores
   * @param score The players score
   */
  public ScoresScene(GameWindow gameWindow, int score, ArrayList<Pair<SimpleStringProperty, SimpleIntegerProperty>> gameScores) {
    super(gameWindow);
    playerScore = new Pair<>(new SimpleStringProperty(""), new SimpleIntegerProperty(score));
    this.gameScores = gameScores;
  }

  /**
   * Build the menu layout
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    communicator.addListener(response -> {
      String[] responseSplit = response.split(" ");
      if(Objects.equals(responseSplit[0], "HISCORES")) {
        if (responseSplit.length > 1) {
          loadOnlineScores(responseSplit[1]);
        }
      }
    });

    logger.info("Creating Instruction Scene");
    communicator.send("HISCORES");
    localScores = getLocalScores();

    // Wait until scores have been received
    try{
      while(onlineScores.size() != 10){
        TimeUnit.MILLISECONDS.sleep(1);
      }
    } catch (InterruptedException ignored){}

    //Check if player has achieved new highscore
    if(!gameScores.isEmpty()) {
      continueBuild();
    } else if (localScores.isEmpty() || localScores.get(localScores.size() - 1).getValue().get() < playerScore.getValue().get() || onlineScores.isEmpty() || onlineScores.get(onlineScores.size() - 1).getValue().get() < playerScore.getValue().get()) {
      registerNewHighScore();
    } else {
      continueBuild();
    }
  }

  /**
   * Build scene to allow player to enter their name
   * Called if a player has achieved a new highscore
   */
  public void registerNewHighScore(){

    var getNamePane = new StackPane();
    getNamePane.setMaxWidth(gameWindow.getWidth());
    getNamePane.setMaxHeight(gameWindow.getHeight());
    getNamePane.getStyleClass().add("menu-background");
    root.getChildren().add(getNamePane);

    var title = new Image(MenuScene.class.getResource("/images/TetrECS.png").toExternalForm());
    ImageView imageView = new ImageView(title);
    imageView.setPreserveRatio(true);
    imageView.setFitHeight(120);

    Label highScoreText = new Label("You Got A Highscore!");
    highScoreText.getStyleClass().add("title");
    highScoreText.setAlignment(Pos.CENTER);

    TextField textBox = new TextField("Enter name");
    textBox.setMaxWidth(gameWindow.getWidth() * 0.5);
    textBox.setAlignment(Pos.CENTER);

    Button submitButton = new Button("Submit");
    submitButton.getStyleClass().add("menuItem");
    submitButton.setAlignment(Pos.CENTER);

    VBox verticalPane = new VBox(20, imageView, highScoreText, textBox, submitButton);
    verticalPane.setAlignment(Pos.CENTER);
    getNamePane.getChildren().add(verticalPane);

    // Submit button
    submitButton.setOnAction(event -> {
      playerScore.getKey().set(textBox.getText());

      localScores.add(playerScore);
      localScores = sortScores(localScores);

      // If new local score
      if(localScores.size() > 9) {
        localScores = new ArrayList<>(localScores.subList(0, 10));
      }
      saveLocalScores(localScores);
      continueBuild();

      // If new online score
      if (onlineScores.get(onlineScores.size() - 1).getValue().get() < playerScore.getValue().get()) {
        communicator.send("HISCORE " + textBox.getText() + ":" + playerScore.getValue().get());
      }
    });
  }

  /**
   * Build scores window
   */
  public void continueBuild(){

    ScoresList localScoreComponent = new ScoresList();
    ScoresList onlineScoreComponent = new ScoresList();

    root.getChildren().clear();
    var scoresPane = new StackPane();
    scoresPane.setMaxWidth(gameWindow.getWidth());
    scoresPane.setMaxHeight(gameWindow.getHeight());
    scoresPane.getStyleClass().add("menu-background");
    root.getChildren().add(scoresPane);

    var title = new Image(MenuScene.class.getResource("/images/TetrECS.png").toExternalForm());
    ImageView imageView = new ImageView(title);
    imageView.setPreserveRatio(true);
    imageView.setFitHeight(120);

    Label gameOverText = new Label("Game Over");
    gameOverText.getStyleClass().add("bigtitle");
    gameOverText.setAlignment(Pos.CENTER);

    Label highscoresText = new Label("Highscores");
    highscoresText.getStyleClass().add("title");
    highscoresText.setAlignment(Pos.CENTER);

    // Score lists

    Label localScoresText = new Label("Local Scores");
    localScoresText.getStyleClass().add("scorelist");
    localScoresText.setAlignment(Pos.CENTER);
    localScoresText.setTextFill(Color.WHITE);

    if(!gameScores.isEmpty()){
      localScoresText.setText("Game Scores");
      localScores = gameScores;
    }

    VBox localScoresList = new VBox(localScoresText, localScoreComponent.generateList(localScores));
    localScoresList.setAlignment(Pos.TOP_CENTER);
    localScoresList.setPadding(new Insets(20, 160, 0, 0));

    Label onlineScoresText = new Label("Online Scores");
    onlineScoresText.getStyleClass().add("scorelist");
    onlineScoresText.setAlignment(Pos.CENTER);
    onlineScoresText.setTextFill(Color.WHITE);

    VBox onlineScoresList = new VBox(onlineScoresText, onlineScoreComponent.generateList(onlineScores));
    onlineScoresList.setAlignment(Pos.TOP_CENTER);
    onlineScoresList.setPadding(new Insets(20, 0, 0, 0));

    GridPane scoresListPane = new GridPane();
    scoresListPane.add(localScoresList, 0, 0);
    scoresListPane.add(onlineScoresList, 1, 0);
    scoresListPane.setAlignment(Pos.CENTER);

    VBox scoresInformation = new VBox(imageView, gameOverText, highscoresText, scoresListPane);
    scoresInformation.setAlignment(Pos.CENTER);
    scoresPane.getChildren().add(scoresInformation);

    localScoreComponent.animateScores();
    onlineScoreComponent.animateScores();
  }


  /**
   * Load local scores from a file
   * @return list of local scores
   */
  public ArrayList<Pair<SimpleStringProperty, SimpleIntegerProperty>> getLocalScores(){
    logger.info("Loading local scores");
    ArrayList<Pair<SimpleStringProperty, SimpleIntegerProperty>> localScores = new ArrayList<>();
    try {
     var scoreFile = new File("Scores.txt");
     scoreFile.createNewFile();
     List<String> scores = Files.readAllLines(scoreFile.toPath());
     // Loop through all scores and add them to an arraylist
     for(String score : scores){
       String[] splitScores = score.split(":");
       localScores.add(new Pair<>(new SimpleStringProperty(splitScores[0]), new SimpleIntegerProperty(Integer.parseInt(splitScores[1]))));
     }
     return localScores;
    } catch (IOException e){
      throw new RuntimeException();
    }
  }

  /**
   * Save local scores in a file
   * @param scores local scores
   */
  public void saveLocalScores(ArrayList<Pair<SimpleStringProperty, SimpleIntegerProperty>> scores){
    logger.info("Saving local scores");
    try{
      File localScores = new File("Scores.txt");
      localScores.delete();
      localScores = new File("Scores.txt");
      localScores.createNewFile();
      Writer writer = new FileWriter(localScores);
      // If scores exist save them to the file
      for(Pair<SimpleStringProperty,SimpleIntegerProperty> score : scores){
        writer.write(score.getKey().get() + ":" + score.getValue().get() + System.lineSeparator());
      }
      // Else save a default list of scores
      if(scores.isEmpty()){
        for(int i = 0; i < 10 - scores.size(); i++){
          writer.write("Test:0" + System.lineSeparator());
        }
      }
      writer.close();
    } catch (Exception e) {
      throw new RuntimeException();
    }
  }

  /**
   * Convert a string of scores received from the server into the correct form
   * @param scores scores
   */
  public void loadOnlineScores(String scores){
    logger.info("Loading online scores");
    String[] scoreLines = scores.split("\n");

    for(String line : scoreLines){
      onlineScores.add(new Pair<>(new SimpleStringProperty(line.split(":")[0]), new SimpleIntegerProperty(Integer.parseInt(line.split(":")[1]))));
    }
  }

  /**
   * Sort a list of scores
   * @param scores scores
   * @return scores
   */
  public ArrayList<Pair<SimpleStringProperty, SimpleIntegerProperty>> sortScores(ArrayList<Pair<SimpleStringProperty, SimpleIntegerProperty>> scores){
    for (int i = 0; i < scores.size(); i++) {
      for (int j = i + 1; j < scores.size(); j++) {
        if (scores.get(i).getValue().get() < scores.get(j).getValue().get()) {
          Collections.swap(scores, i, j);
        }
      }
    }
    return scores;
  }

}

