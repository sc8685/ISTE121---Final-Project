import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class WaterPlantTask extends StackPane {
    
    // Final static
    private static final int TASK_X = 1000;
    private static final int TASK_Y = 500;

    private static final int TASK_RADIUS = 20;
    private static final int INTERACTION_RADIUS = 50;

    private static final String PLANT_IMAGE = "plant.png";
    private static final String WATER_IMAGE = "water.png";

    // Attributes
    private Text taskText;
    private Stage taskWindow;
    private ImageView plant;
    private ImageView waterCan;
    private Circle taskCircle;
    
    // ProgressBar
    private ProgressBar waterProgress;
    private ProgressBar tasksProgressBar;

    // Public
    public WaterPlantTask(ProgressBar tasksProgressBar) {

        // ProgressBar
        this.tasksProgressBar = tasksProgressBar;

        // Task Text
        taskText = new Text("Water the Plant");
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
        taskWindow.setTitle("Water the Plant");

        // StackPane 
        StackPane pane = new StackPane();
        pane.setPadding(new Insets(10, 10, 10, 10));
        pane.setPrefSize(320, 450);

        // Plant
        plant = new ImageView(PLANT_IMAGE);
        plant.setFitWidth(300);
        plant.setFitHeight(300);
        pane.getChildren().add(plant);

        // Water Can
        waterCan = new ImageView(WATER_IMAGE);
        waterCan.setFitWidth(120);
        waterCan.setFitHeight(100);
        pane.getChildren().add(waterCan);

        // Water Progress
        waterProgress = new ProgressBar(0);
        waterProgress.setPrefWidth(300);
        waterProgress.setTranslateY(200);
        pane.getChildren().add(waterProgress);

        makeWaterCanDraggable();

        Scene taskScene = new Scene(pane);
        taskWindow.setScene(taskScene);
    }

    // Water can draggable
    private void makeWaterCanDraggable() {

        // Water can
        waterCan.setOnMousePressed(event -> {
            waterCan.setMouseTransparent(true);
        });

        waterCan.setOnMouseDragged(event -> {

            double offsetX = event.getSceneX() - waterCan.getLayoutX();
            double offsetY = event.getSceneY() - waterCan.getLayoutY();

            waterCan.setTranslateX(offsetX);
            waterCan.setTranslateY(offsetY);

            // Bounds
            Bounds plantBounds = plant.getBoundsInParent();
            Bounds canBounds = waterCan.getBoundsInParent();

            if (plantBounds.intersects(canBounds)) {
                double currentProgress = waterProgress.getProgress();
                waterProgress.setProgress(currentProgress + 0.01);

                if (waterProgress.getProgress() >= 1) {
                    taskText.setText("Task Completed!");
                    Game2DClean.taskCounter++;
                    // Update progress value
                    tasksProgressBar.setProgress((double) Game2DClean.taskCounter / Game2DClean.TARGET_TASKS);
                    taskWindow.close();
                }
            }
        });

        waterCan.setOnMouseReleased(event -> {
            waterCan.setMouseTransparent(false);
            waterCan.setTranslateX(0);
            waterCan.setTranslateY(0);
        });
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
    
