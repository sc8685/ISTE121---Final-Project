import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class CountTask extends Pane {
    
    // Static finals
    private static final int TASK_X = 3200;
    private static final int TASK_Y = 2300;

    private static final int TASK_RADIUS = 20;
    private static final int INTERACTION_RADIUS = 50;

    private static final int RECT_SIZE = 50;

    // Attributes
    private Text taskText;
    private Circle taskCircle;
    private Stage taskWindow;
    private GridPane gridPane;
    private int currentNumber;

    // ProgressBar 
    private ProgressBar tasksProgressBar;

    // Public 
    public CountTask(ProgressBar tasksProgressBar) {

        // ProgressBar 
        this.tasksProgressBar = tasksProgressBar;
        
        // Task Text
        taskText = new Text("Counter");
        taskText.setFont(new Font(22));
        taskText.setFill(Color.BLACK);
        taskText.setVisible(true);

        // Task Circle
        taskCircle = new Circle(TASK_RADIUS, Color.TRANSPARENT);
        taskCircle.setStroke(Color.RED);
        taskCircle.setStrokeWidth(2);
        taskCircle.setStrokeType(StrokeType.OUTSIDE);
        taskCircle.setFill(new Color(1, 0, 0, 0.1));
    
        // StackPane
        StackPane taskContainer = new StackPane();
        taskContainer.getChildren().addAll(taskCircle, taskText);
        StackPane.setAlignment(taskText, Pos.CENTER); 

        this.getChildren().add(taskContainer);

        createTaskWindow();
    }

    // Task window
    private void createTaskWindow() {

        // Task Window
        taskWindow = new Stage();
        taskWindow.initModality(Modality.APPLICATION_MODAL);
        taskWindow.setTitle("Count to 10");

        // GridPane
        gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10, 10, 10, 10));
    
        // List for numbers
        List <Integer> numbers = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        Collections.shuffle(numbers);

        // For entered numbers
        int index = 0;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 5; j++) {
                int number = numbers.get(index++);

                // Rectanlge
                Rectangle rectangle = new Rectangle(RECT_SIZE, RECT_SIZE, Color.LIGHTGRAY);
                rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.LIGHTGREEN));
                rectangle.setOnMouseExited(event -> rectangle.setFill(Color.LIGHTGRAY));

                // Text
                Text text = new Text(Integer.toString(number));
                text.setFont(new Font(20));

                // StackPane
                StackPane stackPane = new StackPane(rectangle, text);
                stackPane.setOnMouseClicked(event -> handleRectangleClick(number, rectangle));

                // Add to GridPane
                gridPane.add(stackPane, j, i);
            }
        }

        // Scene
        Scene taskScene = new Scene(gridPane);
        taskWindow.setScene(taskScene);
        currentNumber = 1;
    }

    // Rectangle click
    private void handleRectangleClick(int number, Rectangle rectangle) {

        if (number == currentNumber) {
            rectangle.setFill(Color.GREEN);

            if (currentNumber < 10) {
                currentNumber++;
            }
            else {
                taskText.setText("Task Completed!");
                Game2DClean.taskCounter++;
                // Update progress value
                tasksProgressBar.setProgress((double) Game2DClean.taskCounter / Game2DClean.TARGET_TASKS);
                taskWindow.close();
            }
        }
    }

    // Update 
    public void update(int backgroundX, int backgroundY, Scene scene) {

        // Set the task postion relative to the background
        setTranslateX(TASK_X + backgroundX);
        setTranslateY(TASK_Y + backgroundY);

        // Task visibility based on the character's distance from task
        int centerX = (int) (scene.getWidth() / 2);
        int centerY = (int) (scene.getHeight() / 2);

        int taskScreenX = TASK_X + backgroundX;
        int taskScreenY = TASK_Y + backgroundY;

        double distance = Math.sqrt(Math.pow(centerX - taskScreenX, 2)) + Math.sqrt(Math.pow(centerY - taskScreenY, 2));
        taskText.setVisible(distance <= INTERACTION_RADIUS);
    }

    // Increment Counter
    public void tryToIncrementCounter(int backgroundX, int backgroundY, Scene scene) {

        int centerX = (int) (scene.getWidth() / 2);
        int centerY = (int) (scene.getHeight() / 2);

        int taskScreenX = TASK_X + backgroundX;
        int taskScreenY = TASK_Y + backgroundY;

        double distance = Math.sqrt(Math.pow(centerX - taskScreenX, 2)) + Math.sqrt(Math.pow(centerY - taskScreenY, 2));
    
        if (distance <= INTERACTION_RADIUS) {
            taskWindow.show();
        }
    }

}
