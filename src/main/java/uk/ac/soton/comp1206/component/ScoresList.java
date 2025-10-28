package uk.ac.soton.comp1206.component;

import java.util.ArrayList;
import java.util.Collections;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A leaderboard to display a list of users and associated scores
 */
public class ScoresList{

  private static final Logger logger = LogManager.getLogger(GameBlock.class);

  /**
   * Vertical box containing rows of player: score
   */
  private VBox scoresList = new VBox();

  /**
   * List containing every row in scoresList
   */
  private ArrayList<HBox> lines = new ArrayList<>();

  /**
   * Generate a vertical box containing rows of player : score
   * Each row is coloured on a gradient from red to yellow
   * @param scores Array containing every player and score that need to be added to scoresList
   * @return scoresList
   */
  public VBox generateList(ArrayList<Pair<SimpleStringProperty, SimpleIntegerProperty>> scores){

    int colorVariable = 0;
    logger.info("Scores list created");

    for (int i = 0; i < scores.size(); i++) {
      for (int j = i + 1; j < scores.size(); j++) {
        if (scores.get(i).getValue().get() < scores.get(j).getValue().get()) {
          Collections.swap(scores, i, j);
        }
      }
    }

    for(Pair<SimpleStringProperty,SimpleIntegerProperty> score : scores){

      // Label containing players name
      Label player = new Label();
      player.getStyleClass().add("scorelist");
      player.textProperty().bind(score.getKey());
      player.setTextAlignment(TextAlignment.CENTER);
      player.setTextFill(Color.rgb(255, ((255 * colorVariable) / scores.size()) + ((68 * (scores.size() - colorVariable)) / scores.size()), 0));

      // Label containing score
      Label playerScore = new Label();
      playerScore.getStyleClass().add("scorelist");
      playerScore.textProperty().bind(score.getValue().asString(": %d"));
      playerScore.setTextAlignment(TextAlignment.CENTER);
      playerScore.setTextFill(Color.rgb(255, ((255 * colorVariable) / scores.size()) + ((68 * (scores.size() - colorVariable)) / scores.size()), 0));

      // Create new line and add it to lines and scoreslist
      HBox line = new HBox(player, playerScore);
      line.setAlignment(Pos.CENTER);
      lines.add(line);
      scoresList.getChildren().add(line);

      colorVariable++;

    }
    return scoresList;
  }

  /**
   * Animate the list of scores by fading in from top to bottom
   */
  public void animateScores(){
    logger.info("Score list animated");
    ArrayList<Transition> transitions = new ArrayList<>();
    for(HBox line : lines){
      // Fade from transparent to opaque
      FadeTransition fade = new FadeTransition(Duration.millis(200), line);
      fade.setFromValue(0);
      fade.setToValue(1);
      transitions.add(fade);
    }
    SequentialTransition transition = new SequentialTransition(transitions.toArray(Animation[]::new));
    transition.play();
  }


}

