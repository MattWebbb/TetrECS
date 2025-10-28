package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.Glow;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;

    private final double width;
    private final double height;


    /**
     * Whether a circle should be drawn on this block
     */
    private boolean isCircle;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }

        // Add lighting
        var gc = getGraphicsContext2D();
        Lighting lighting = new Lighting();
        lighting.setLight(new Light.Distant());
        lighting.setSurfaceScale(1);

        Glow glow = new Glow();
        glow.setLevel(1);

        gc.applyEffect(glow);
        gc.applyEffect(lighting);

        // Draw the circle if needed
        if(this.isCircle){
            drawCircle();
        }
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        gc.setFill(new Color(1,1,1,0.2));
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Draw a circle onto the block
     */
    public void drawCircle(){

        var gc = getGraphicsContext2D();
        gc.setFill(new Color(0,0,0,0.6));
        gc.fillOval(width/2 - 10, height/2 - 10, 20, 20);

    }

    /**
     * Change the appearance of the block when
     * Called when the user hovers their mouse over the block
     */
    public void hover(){

        paint();
        var gc = getGraphicsContext2D();
        gc.setFill(new Color(1,1,1,0.3));
        gc.fillRect(0,0, width, height);
    }

    /**
     * Fade the contents of a block to fully transparent
     * Called when a line is cleared
     */
    public void fadeOut(){

        var gc = getGraphicsContext2D();
        final boolean[] flash = {true};
        final float[] transparency = {1};

        gc.setStroke(Color.BLACK);
        gc.strokeRect(0,0,width,height);

        var timer = new AnimationTimer(){
            @Override
            public void handle(long l) {

                gc.setFill(new Color(1,1,1,1- transparency[0]));
                gc.fillRect(0,0, width, height);


                if(flash[0]) {
                    //Flash to white
                    transparency[0] -= 0.3;
                    if (transparency[0] <= 0.5) {
                        flash[0] = false;
                    }
                } else {
                    //Fade to transparent
                    transparency[0] += 0.1;
                    if(transparency[0] >= 0.9){
                        stop();
                        paint();
                    }
                }
            }
        };

        timer.start();

    }


    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

    /**
     * Whether the block should contain a circle
     * @param circle circle
     */
    public void setCircle(boolean circle){
        this.isCircle = circle;
    }
}
