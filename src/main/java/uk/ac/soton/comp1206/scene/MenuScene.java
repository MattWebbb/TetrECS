package uk.ac.soton.comp1206.scene;

import javafx.animation.Animation;
import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {}

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        //Create title
        var title = new Image(MenuScene.class.getResource("/images/TetrECS.png").toExternalForm());
        ImageView imageView = new ImageView(title);
        imageView.setPreserveRatio(true);
        imageView.setFitHeight(150);
        mainPane.setCenter(imageView);

        // Animate title
        RotateTransition animate = new RotateTransition();
        animate.setNode(imageView);
        animate.setFromAngle(-7);
        animate.setToAngle(7);
        animate.setDuration(Duration.seconds(2));
        animate.setAutoReverse(true);
        animate.setCycleCount(Animation.INDEFINITE);
        animate.play();

        //Create Buttons
        var singlePlayerButton = new Button("Singleplayer");
        singlePlayerButton.getStyleClass().add("menuItem");
        singlePlayerButton.setOnAction(this::startGame);

        var multiPlayerButton = new Button("Multiplayer");
        multiPlayerButton.getStyleClass().add("menuItem");
        multiPlayerButton.setOnAction(this::multiplayerScreen);

        var instructionButton = new Button("How To Play");
        instructionButton.getStyleClass().add("menuItem");
        instructionButton.setOnAction(this::instructionScreen);

        var exitButton = new Button("Exit");
        exitButton.getStyleClass().add("menuItem");
        exitButton.setOnAction(this::quit);

        var musicLabel = new Label("Music: ");
        musicLabel.getStyleClass().add("heading");
        musicLabel.setAlignment(Pos.CENTER);

        // Create sliders
        var musicSlider = new Slider();
        musicSlider.setMaxWidth(gameWindow.getWidth() * 0.2);
        musicSlider.minProperty().set(0);
        musicSlider.maxProperty().set(1);
        musicSlider.setValue(Multimedia.musicVolume);
        musicSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            Multimedia.musicVolume = (float) musicSlider.getValue();
            Multimedia.changeMusicVolume();
        });

        var musicBox = new VBox(5, musicLabel, musicSlider);
        musicBox.setAlignment(Pos.CENTER);

        var sfxLabel = new Label("SFX: ");
        sfxLabel.getStyleClass().add("heading");
        sfxLabel.setAlignment(Pos.CENTER);

        var sfxSlider = new Slider();
        sfxSlider.setMaxWidth(gameWindow.getWidth() * 0.2);
        sfxSlider.minProperty().set(0);
        sfxSlider.maxProperty().set(1);
        sfxSlider.setValue(Multimedia.audioVolume);
        sfxSlider.valueProperty().addListener((observableValue, oldValue, newValue) -> {
            Multimedia.audioVolume = (float) sfxSlider.getValue();
        });

        var sfxBox = new VBox(5, sfxLabel, sfxSlider);
        sfxBox.setAlignment(Pos.CENTER);

        var controlPane = new BorderPane();
        controlPane.setPadding(new Insets(0,20,0,20));
        controlPane.setLeft(musicBox);
        controlPane.setRight(sfxBox);

        var menuButtons = new VBox(singlePlayerButton, multiPlayerButton, instructionButton, exitButton, controlPane);
        menuButtons.setAlignment(Pos.CENTER);
        menuButtons.setPadding(new Insets(0,0,40,0));
        mainPane.setBottom(menuButtons);

        // Play menu music
        new Multimedia().playMusic("menu.mp3");
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }

    /**
     * Handle when the multiplayer button is pressed
     * @param event event
     */
    private void multiplayerScreen(ActionEvent event){
        gameWindow.startMultiplayer();
    }

    /**
     * Handle when the how to play button is pressed
     * @param event event
     */
    private void instructionScreen(ActionEvent event){
        gameWindow.startInstructions();
    }

    /**
     * Handle when the quit button is pressed
     * @param event event
     */
    private void quit(ActionEvent event){
        System.exit(0);
    }


}
