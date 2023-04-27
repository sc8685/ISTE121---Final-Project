import java.util.ArrayList;
import java.util.List;

import javax.swing.text.rtf.RTFEditorKit;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SwipeTask extends Pane {
    
    // Final static
    private static final int TASK_X = 3200;
    private static final int TASK_Y = 1800;

    private static final int TASK_RADIUS = 20;
    private static final int INTERACTION_RADIUS = 50;

    private final static String TRASH_IMAGE = "trash.png";
    
    // Attributes
    private Text taskText;
    private Circle taskCircle;
    private Stage taskWindow;

    // List
    private List <ImageView> trashItems;

    // ProgressBar 
    private ProgressBar tasksProgressBar;

    // Public
    public SwipeTask(ProgressBar tasksProgressBar) {

        // ProgressBar 
        this.tasksProgressBar = tasksProgressBar;

        // Task Text
        taskText = new Text("Throw out the Trash");
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

        // Window
        taskWindow = new Stage();
        taskWindow.initModality(Modality.APPLICATION_MODAL);
        taskWindow.setTitle("Clean the Trash");

        // GridPane
        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10, 10, 10, 10));

        // ArrayList
        trashItems = new ArrayList<>();

        int numberOfTrashItems = 6;
        int numberOfRows = 2;

        for (int i = 0; i < numberOfTrashItems; i++) {
            
            // ImageView
            ImageView trash = new ImageView(TRASH_IMAGE);
            trash.setFitWidth(100);
            trash.setFitHeight(120);
            trashItems.add(trash);

            int rowIndex = i % numberOfRows;
            int columnIndex = i / numberOfRows;

            // Add to GridPane
            gridPane.add(trash, columnIndex, rowIndex);

            // Dragged
            trash.setOnMouseDragged(event -> {
                trash.setOpacity(0.5);
                trash.setTranslateX(event.getScreenX() - trash.getLayoutX());
                trash.setTranslateY(event.getScreenY() - trash.getLayoutY());
            });

            // Released
            trash.setOnMouseReleased(event -> {

                if (Math.abs(trash.getTranslateX()) > trash.getFitWidth() * 0.8
                    || Math.abs(trash.getTranslateY()) > trash.getFitHeight() * 0.8) {

                        // Remove
                        gridPane.getChildren().remove(trash);
                    }
                    else {
                        trash.setOpacity(1);
                        trash.setTranslateX(0);
                        trash.setTranslateY(0);
                    }
                    if (gridPane.getChildren().isEmpty()) {
                        taskText.setText("Task Completed!");
                        Game2DClean.taskCounter++;
                        // Update progress value
                        tasksProgressBar.setProgress((double) Game2DClean.taskCounter / Game2DClean.TARGET_TASKS);
                        taskWindow.close();
                    }
            });
        }

        // Scene 
        Scene taskScene = new Scene(gridPane);
        taskWindow.setScene(taskScene);
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
