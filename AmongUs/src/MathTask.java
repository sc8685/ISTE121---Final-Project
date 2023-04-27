import java.util.ArrayList;
import java.util.Random;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class MathTask extends StackPane{
    
    // Final static
    private final static int TASK_RADIUS = 20;
    private final static int INTERACTION_RADIUS = 50;

    private final static int TASK_X = 850;
    private final static int TASK_Y = 2600;

    // Attributes
    private Text taskText;
    private Stage taskWindow;
    private VBox vbox;
    private Circle taskCircle;
    private int currentIndex;
    private boolean completed;

    // ArrayLists
    private ArrayList<String> problems;
    private ArrayList<String> solutions;

    // ProgressBar
    private ProgressBar tasksProgressBar;

    // Public
    public MathTask(int numProblems, ProgressBar tasksProgressBar) {

        // ProgressBar
        this.tasksProgressBar = tasksProgressBar;

        // Text
        taskText = new Text("Discrete Mathematics");
        taskText.setFont(new Font(22));
        taskText.setFill(Color.BLACK);
        taskText.setVisible(true);

        // Circle 
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

        createTaskWindow(numProblems);
    }

    // Task window
    private void createTaskWindow(int numProblems) {

        // Window
        taskWindow = new Stage();
        taskWindow.initModality(Modality.APPLICATION_MODAL);
        taskWindow.setTitle("Solve these problems:");

        // VBox
        vbox = new VBox(10);
        vbox.setPadding(new Insets(10, 10, 10, 10));
        vbox.setPrefHeight(115);
        vbox.setPrefWidth(400);

        // ArrayLists
        problems = new ArrayList<>();
        solutions = new ArrayList<>();

        // Random
        Random random = new Random();
        
        for (int i = 0; i < numProblems; i++) {
            
            int num1 = random.nextInt(21);
            int num2 = random.nextInt(21);

            String problem = num1 + " + " + num2 + " = ";
            int solution = num1 + num2;

            if (random.nextBoolean()) {
                problem = num1 + " - " + num2 + " = ";
                solution = num1 - num2;
            }
            problems.add(problem);
            solutions.add(String.valueOf(solution));
        }

        currentIndex = 0;
        completed = false;

        // Label
        Label problemLabel = new Label(problems.get(currentIndex));
        problemLabel.setFont(new Font(20));

        // TextField
        TextField solutionField = new TextField();
        solutionField.setFont(new Font(20));

        // Button
        Button submitButton = new Button("Submit");
        submitButton.setFont(new Font(20));

        submitButton.setOnAction(event -> {
            String solution = solutionField.getText().trim();

            if (solution.equals(solutions.get(currentIndex))) {
                currentIndex++;

                if (currentIndex == numProblems) {
                    completed = true;
                    taskText.setText("Task Completed!");
                    Game2DClean.taskCounter++;
                    // Update progress value
                    tasksProgressBar.setProgress((double) Game2DClean.taskCounter / Game2DClean.TARGET_TASKS);
                    taskWindow.close();
                }
                else {
                    problemLabel.setText(problems.get(currentIndex));
                    solutionField.clear();
                }
            }
            else {
                solutionField.setText("");
                solutionField.setStyle("-fx-border-color: red;");
            }
        });

        // HBox
        HBox inputBox = new HBox(10);
        inputBox.getChildren().addAll(solutionField, submitButton);
        inputBox.setAlignment(Pos.CENTER);

        // Get to VBox
        vbox.getChildren().addAll(problemLabel, inputBox);

        // Scene
        Scene taskScene = new Scene(vbox);
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
    public void tryToSolveProblem(int backgroundX, int backgroundY, Scene scene) {

        if (!completed) {
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
}
