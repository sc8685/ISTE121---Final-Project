import javafx.application.*;
import javafx.event.*;
import javafx.scene.*;
import javafx.scene.image.*;
import javafx.scene.control.*;
import javafx.scene.control.Alert.*;
import javafx.scene.text.*;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.*;
import javafx.geometry.*;
import javafx.animation.*;
import java.io.*;
import java.net.Socket;
import java.nio.channels.ClosedSelectorException;
import java.nio.file.Paths;
import java.util.*;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

/**
 * Group Name: Kumpiri
 * Members: Toni Katanušić, Stipe Čulina
 * Among Us
 */

public class Game2DClean extends Application {
   // Window attributes
   private Stage stage;
   private Scene scene;
   private StackPane root;

   private static String[] args;

   private final static String CREWMATE_RUNNER = "character0_left.png"; // file with icon for a racer

   // multiple maps
   private final static String BACKGROUND_IMAGE1 = "Map_AmongUs_1.png";
   private final static String BACKGROUND_IMAGE2 = "Map_AmongUs_2.png";
   private final static String BACKGROUND_IMAGE3 = "Map_AmongUs_3.png";

   // game settings
   public static int tasksNeededToWin = 4;
   public static double INTERACTION_RADIUS = 100;
   public static double speed = 7;
   public static String mapSelection = "1";

   // counters
   public static int taskCounter = 0;
   public static int monstersKilled = 0;

   //progress bar targets
   public static double TARGET_TASKS=4;
   public static double targetMonsters=4;

   // character index
   private int selectedCharacter = 0;

   // list of monsters
   public List<Monster> monsters = new ArrayList<>();

   // crewmates
   CrewmateRacer masterCrewmate = null;
   ArrayList<CrewmateRacer> robotCrewmates = new ArrayList<>();

   // movable background
   MovableBackground movableBackground = null;

   // Animation timer
   AnimationTimer timer = null;
   int counter = 0;
   boolean moveUP = false, moveDown = false, moveRight = false, moveLeft = false;

   // background detection/collision
   Image backgroundCollision = null;

   //server attributes
   private Socket clientSocket;
   private PrintWriter out;
   private BufferedReader in;

   //music
   private MediaPlayer mediaPlayer;

   // main program
   public static void main(String[] _args) {
      args = _args;
      launch(args);
      
   }

   // start() method, called via launch
   public void start(Stage _stage) {
      // stage seteup
      stage = _stage;
      stage.setTitle("Among Us");
      stage.setOnCloseRequest(
            new EventHandler<WindowEvent>() {
               public void handle(WindowEvent evt) {
                  System.exit(0);
               }
            });

      // root pane
      root = new StackPane();

      menuScene();
      stage.show();

   }


   //connect to server
   private void connect(){
     
      try {
         clientSocket=new Socket("localhost",12345);
         out=new PrintWriter(clientSocket.getOutputStream(), true);
         in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

      } catch (IOException e) {
         // TODO Auto-generated catch block
         e.printStackTrace();
      }
   }

   //background music method
   private void backgroundMusic(){
      String musicFile="/song.wav";
      Media media=new Media(getClass().getResource(musicFile).toString());
      mediaPlayer=new MediaPlayer(media);
      mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
      mediaPlayer.play();
      mediaPlayer.setOnError(() -> {
         System.out.println("Error playing media: " + mediaPlayer.getError());
     });
   }

   //method to create progressbars
   public HBox createProgressBar(String labelText, ProgressBar progressBar){
      Label label=new Label(labelText);
      label.setStyle("-fx-font-size: 18px; -fx-font-weight:bold; -fx-text-fill: darkmagenta;");
      progressBar.setPrefWidth(200);
      progressBar.setMinWidth(200);
      progressBar.setStyle("-fx-accent: darkmagenta;");
      HBox hBox=new HBox(10);
      hBox.setPadding(new Insets(10,0,0,10));
      hBox.getChildren().addAll(label,progressBar);
      return hBox;
   }

   //task and kill progressbars
   public static ProgressBar taskProgressBar=new ProgressBar(0);
   public static ProgressBar monsterProgressBar=new ProgressBar(0);



   // start the game scene
   public void initializeScene(String playerName) {


      connect();

      
      //crewmate
      masterCrewmate = new CrewmateRacer(true,selectedCharacter,playerName);
      
      // create background
      movableBackground = new MovableBackground();

      // add background
      this.root.getChildren().add(movableBackground);
      // add to the root
      this.root.getChildren().add(masterCrewmate);
      this.root.getChildren().addAll(robotCrewmates);

      //adding progressbars
      HBox taskContainer=createProgressBar("Tasks Completed: ", taskProgressBar);
      HBox monsterContainer=createProgressBar("Monsters Killed:    ", monsterProgressBar);

      //wrapping progressbars in a vbox
      VBox container=new VBox(10);
      container.setPadding(new Insets(10,0,0,10));
      container.getChildren().addAll(taskContainer,monsterContainer);
      this.root.getChildren().add(container);

      //music
      if(mediaPlayer==null){
      backgroundMusic();
      }
      mediaPlayer.play();

      // display the window
      scene = new Scene(root, 800, 500);

      // scene.getStylesheets().addAll(this.getClass().getResource("style.css").toExternalForm());
      stage.setScene(scene);
      stage.show();

      // KEYBOARD CONTROL
      scene.setOnKeyPressed(
            new EventHandler<KeyEvent>() {
               @Override
               public void handle(KeyEvent event) {
                  switch (event.getCode()) {
                     case UP:
                        moveUP = true;
                        break;
                     case DOWN:
                        moveDown = true;
                        break;
                     case LEFT:
                        moveLeft = true;
                        break;
                     case RIGHT:
                        moveRight = true;
                        break;
                     //case for tasks   
                     case ENTER:
                     movableBackground.countTask.tryToIncrementCounter(movableBackground.racerPosX,
                     movableBackground.racerPosY, scene);

                     movableBackground.swipeTask.tryToIncrementCounter(movableBackground.racerPosX,
                     movableBackground.racerPosY, scene);

                     movableBackground.waterPlantTask.tryToIncrementCounter(movableBackground.racerPosX,
                     movableBackground.racerPosY, scene);

                     movableBackground.mathTask.tryToSolveProblem(movableBackground.racerPosX,
                     movableBackground.racerPosY, scene);

                     taskProgressBar.setProgress((double) taskCounter/ TARGET_TASKS);

                     //case for killing   
                     case SPACE:
                     Monster closesMonster=getClosestMonster(movableBackground.racerPosX,
                     movableBackground.racerPosY, scene);

                     if(closesMonster!=null){
                        closesMonster.killMonster();
                        monstersKilled++;
                        monsterProgressBar.setProgress((double)monstersKilled/targetMonsters);
                        
                     }
                     break;
                  }

               }
            });

      scene.setOnKeyReleased(
            new EventHandler<KeyEvent>() {
               @Override
               public void handle(KeyEvent event) {
                  switch (event.getCode()) {
                     case UP:
                        moveUP = false;
                        break;
                     case DOWN:
                        moveDown = false;
                        break;
                     case LEFT:
                        moveLeft = false;
                        break;
                     case RIGHT:
                        moveRight = false;
                        break;
                  }

               }
            });

      //calling  background collision
      if (mapSelection.equals("1")) {
         backgroundCollision = new Image(BACKGROUND_IMAGE1);
      }
      if (mapSelection.equals("2")) {
         backgroundCollision = new Image(BACKGROUND_IMAGE2);
      }
      if (mapSelection.equals("3")) {
         backgroundCollision = new Image(BACKGROUND_IMAGE3);
      }
      timer = new AnimationTimer() {
         @Override
         public void handle(long now) {
            masterCrewmate.update();

            movableBackground.update();
         }
      };
      timer.start();
   }

   // background collision
   private Color getPixel(int x, int y){
      if(x >= 0 && y >= 0 && x < backgroundCollision.getWidth() && y < backgroundCollision.getHeight()){
         return backgroundCollision.getPixelReader().getColor(x, y);
      }else{
         return Color.BLACK;
      }
   }

   //return black
   private boolean colorBlack(Color color){
      return color.equals(Color.BLACK);
   }

   //checking is crewmate touches black
   private boolean crewmateMovement(int x, int y){
      int crewmateWidth=100;
      int crewmateHeight=80;

      //positions to check collision
      int midX=x+ crewmateWidth/2;
      int midY=y+crewmateHeight/2;
      int leftMidY=y+crewmateHeight/4;
      int rightMidY=y+3*crewmateHeight/4;

      Color topLeft=getPixel(x,y);
      Color topRight=getPixel(x+crewmateWidth,y );
      Color bottomLeft=getPixel(x,y+ crewmateHeight);
      Color bottomRight=getPixel(x+crewmateWidth, y+crewmateHeight);
      Color midLeft=getPixel(x, leftMidY);
      Color midRight=getPixel(x+crewmateWidth,rightMidY);
      Color topMid=getPixel(midX, y);
      Color bottomMid=getPixel(midX,y+crewmateHeight);

      return !(colorBlack(topLeft) || colorBlack(topRight) || colorBlack(bottomLeft) || colorBlack(bottomRight) ||
           colorBlack(midLeft) || colorBlack(midRight) || colorBlack(topMid) || colorBlack(bottomMid));
   }


   // settings getters and setters
   public int getTasksNeededToWin() {
      return tasksNeededToWin;
   }

   public void setTasksNeededToWin(int tasksNeededtoWin) {
      this.tasksNeededToWin = tasksNeededtoWin;
   }

   public double getINTERACTION_RADIUS() {
      return INTERACTION_RADIUS;
   }

   public void setINTERACTION_RADIUS(double iNTERACTION_RADIUS) {
      this.INTERACTION_RADIUS = iNTERACTION_RADIUS;
   }

   public double getSpeed() {
      return speed;
   }

   public void setSpeed(double playerspeed) {
      this.speed = playerspeed;
   }

   public String getMapSelection() {
      return mapSelection;
   }

   public void setMapSelection(String mapselection) {
      this.mapSelection = mapselection;
   }

   // game settings
   private void settingsWindow() {

      Stage settingsStage = new Stage();
      settingsStage.initModality(Modality.APPLICATION_MODAL);
      settingsStage.setTitle("Game Settings");

      VBox settingsLayout = new VBox(10);
      settingsLayout.setPadding(new Insets(20, 20, 20, 20));

      // speed slider
      Label playerSpeedLabel = new Label("Player Speed (1-12):");
      playerSpeedLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
      Slider playerSlider = new Slider(1, 12, 7);
      playerSlider.setShowTickMarks(true);
      playerSlider.setShowTickLabels(true);
      playerSlider.setMajorTickUnit(1);
      playerSlider.setBlockIncrement(1);

      // tasks needed to win slider
      Label tasksNeededLabel = new Label("Tasks Needed to Win (1-4):");
      tasksNeededLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
      Slider taskSlider = new Slider(1, 4, 4);
      taskSlider.setShowTickMarks(true);
      taskSlider.setShowTickLabels(true);
      taskSlider.setMajorTickUnit(1);
      taskSlider.setBlockIncrement(1);

      // kill distance slider
      Label killDistanceLabel = new Label("Kill Distance (50-300):");
      killDistanceLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
      Slider killSlider = new Slider(50, 300, 100);
      killSlider.setShowTickMarks(true);
      killSlider.setShowTickLabels(true);
      killSlider.setMajorTickUnit(50);
      killSlider.setBlockIncrement(50);

      // map selection
      Label mapLabel = new Label("Map Selection (1, 2 or 3)");
      mapLabel.setFont(Font.font(null, FontWeight.BOLD, 14));
      ComboBox<String> mapComboBox = new ComboBox<>();
      mapComboBox.getItems().addAll("1", "2", "3");
      mapComboBox.setValue("1");

      // adding sliders and labels
      settingsLayout.getChildren().addAll(playerSpeedLabel, playerSlider, tasksNeededLabel, taskSlider,
            killDistanceLabel, killSlider, mapLabel, mapComboBox);

      // save btn
      Button saveButton = new Button("Save");
      saveButton.setOnAction(e -> {
         setSpeed(playerSlider.getValue());
         setTasksNeededToWin((int) taskSlider.getValue());
         setINTERACTION_RADIUS(killSlider.getValue());
         setMapSelection(mapComboBox.getValue());

         settingsStage.close();

      });

      settingsLayout.getChildren().add(saveButton);
      Scene settingsScene = new Scene(settingsLayout,350,400);
      settingsStage.setScene(settingsScene);

      settingsStage.showAndWait();
   }

   // menu
   private void menuScene() {

      
      Pane menuRoot = new Pane();

      ImageView menuBackground = new ImageView(new Image("background.png"));
      menuRoot.getChildren().add(menuBackground);


      

      // game settings btn
      Button settingsButton = new Button("Game Settings");
      settingsButton.setPrefWidth(160);
      settingsButton.setPrefHeight(40);
      settingsButton.setStyle(
            "-fx-font-size: 20px; -fx-background-color: #3F51B5; -fx-text-fill: white; -fx-border-radius: 5px;");
      settingsButton.setLayoutX(320);
      settingsButton.setLayoutY(50);

      // hover effect
      settingsButton.setOnMouseEntered(e -> settingsButton.setStyle(
            "-fx-font-size: 20px; -fx-background-color: #5C6BC0; -fx-text-fill: white; -fx-border-radius: 5px;"));

      settingsButton.setOnMouseExited(e -> settingsButton.setStyle(
            "-fx-font-size: 20px; -fx-background-color: #3F51B5; -fx-text-fill: white; -fx-border-radius: 5px;"));

      // btn listener
      settingsButton.setOnAction(e -> settingsWindow());

      menuRoot.getChildren().add(settingsButton);

      // name entry
      TextField playerNameInput = new TextField();
      playerNameInput.setPromptText("Enter your name");
      playerNameInput.setLayoutX(308);
      playerNameInput.setLayoutY(365);
      menuRoot.getChildren().add(playerNameInput);

      // box for characters
      Rectangle characterBox = new Rectangle(120, 110, 560, 250);
      characterBox.setFill(Color.TURQUOISE);
      characterBox.setOpacity(0.9);
      characterBox.setArcWidth(30);
      characterBox.setArcHeight(30);
      menuRoot.getChildren().add(characterBox);

      // character selection
      int numberOfCharacters = 8;
      int charactersPerRow = 4;
      int imageWidth = 100;
      int imageHeight = 80;
      ArrayList<ImageView> characterImages = new ArrayList<>();

      for (int i = 0; i < numberOfCharacters; i++) {
         final int characterIndex = i;

         // display character image
         ImageView characterImage = new ImageView(new Image("character" + characterIndex + "_right.png"));
         characterImage.setFitHeight(imageHeight);
         characterImage.setFitWidth(imageWidth);
         characterImage.setLayoutX(150 + (i % charactersPerRow) * (imageWidth + 40));
         characterImage.setLayoutY(145 + (i / charactersPerRow) * (imageHeight + 20));
         characterImage.setOpacity(0.5);

         // add hover effect
         characterImage.setOnMouseEntered(e -> characterImage.setOpacity(1.0));
         characterImage.setOnMouseExited(e -> {
            if (selectedCharacter != characterIndex) {
               characterImage.setOpacity(0.5);
            }
         });

         // selected character
         characterImage.setOnMouseClicked(e -> {
            selectedCharacter = characterIndex;
            for (int j = 0; j < characterImages.size(); j++) {
               if (j != characterIndex) {
                  characterImages.get(j).setOpacity(0.5);
               }
            }
            characterImage.setOpacity(1.0);
         });
         menuRoot.getChildren().add(characterImage);
         characterImages.add(characterImage);

      }

      // play button
      Button playButton = new Button("Play");
      playButton.setPrefWidth(120);
      playButton.setPrefHeight(40);
      playButton.setStyle(
            "-fx-font-size: 20px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 5px;");
      playButton.setLayoutX(340);
      playButton.setLayoutY(400);

      // hover effect
      playButton.setOnMouseEntered(e -> playButton.setStyle(
            "-fx-font-size: 20px; -fx-background-color: #66BB6A; -fx-text-fill: white; -fx-border-radius: 5px;"));
      playButton.setOnMouseExited(e -> playButton.setStyle(
            "-fx-font-size: 20px; -fx-background-color: #4CAF50; -fx-text-fill: white; -fx-border-radius: 5px;"));

      // click listener for play btn
      playButton.setOnAction(e -> {
         if (selectedCharacter >= 0) {
            String playerName = playerNameInput.getText().trim();
            if (playerName.isEmpty()) {
               playerName = "Player";
            }

            initializeScene(playerName);
            stage.setScene(scene);
         }
      });

      menuRoot.getChildren().add(playButton);

      //music
      if(mediaPlayer==null){
         backgroundMusic();
         }
         mediaPlayer.play();

      Scene menuScene = new Scene(menuRoot, 800, 500);
      stage.setScene(menuScene);
   }

   // get closest monster method
   private Monster getClosestMonster(int playerX, int playerY, Scene scene) {
      Monster closesMonster = null;
      double minDistance = Double.MAX_VALUE;

      int centerX = (int) (scene.getWidth() / 2);
      int centerY = (int) (scene.getHeight() / 2);

      for (Monster monster : monsters) {
         int monsterScreenX = monster.getMonsterX() + playerX;
         int monsterScreenY = monster.getMonsterY() + playerY;
         double distance = Math.sqrt(Math.pow(centerX - monsterScreenX, 2) + Math.pow(centerY - monsterScreenY, 2));

         if (distance <= getINTERACTION_RADIUS() && distance < minDistance) {
            minDistance = distance;
            closesMonster = monster;
         }

      }
      return closesMonster;

   }

   // inner cremate class
   class CrewmateRacer extends Pane {
      private int racerPosX = 0;
      private int racerPosY = 0;
      private ImageView aPicView = null;
      private boolean isMaster = true;
      private Image leftImage;
      private Image rightImage;
      private Label nameLabel;

      public CrewmateRacer(boolean isMaster, int characterIndex, String name) {

         this.isMaster = isMaster;
         nameLabel = new Label(name);
         nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
         nameLabel.setTextFill(Color.BLACK);

         aPicView = new ImageView();
         if (isMaster) {

            leftImage = new Image("character" + characterIndex + "_left.png");
            rightImage = new Image("character" + characterIndex + "_right.png");
            aPicView.setImage(rightImage);
            racerPosX = 400;// (int)(root.getWidth()/2);
            racerPosY = 250;// (int)(root.getHeight()/2);

            //send player name and index to server
            out.println("NEW: "+characterIndex+": "+name);

         } else {
            aPicView = new ImageView(CREWMATE_RUNNER);
         }

         aPicView.setFitHeight(80);
         aPicView.setFitWidth(100);
         this.getChildren().add(aPicView);

         // update nameLabel pos
         nameLabel.setTranslateX(racerPosX + aPicView.getFitWidth() / 2 - nameLabel.getWidth() / 2 + 20);
         nameLabel.setTranslateY(racerPosY - nameLabel.getHeight());
         this.getChildren().add(nameLabel);
      }

      // set facing direction
      public void setFacingDirection(boolean faceRight) {
         if (isMaster) {
            if (faceRight) {
               aPicView.setImage(rightImage);

            } else {
               aPicView.setImage(leftImage);
            }
         }
      }

      public void update() {

         double speed = 0;

         if (isMaster) {// MASTER CONTROL

            // get pixel
            Color color = backgroundCollision.getPixelReader().getColor(racerPosX, racerPosY);
            // System.out.println(color.getRed()+" "+color.getGreen()+" "+color.getBlue());

            // get distance
            int targetX = 0;
            int targetY = 0;
            double dist = Math.sqrt(Math.pow(racerPosX - targetX, 2) + Math.pow(racerPosY - targetY, 2));

         } else {// ALL THE OTHERS
            racerPosX += Math.random() * speed;
            racerPosY += (Math.random() - 0.2) * speed;
         }

         aPicView.setTranslateX(racerPosX);
         aPicView.setTranslateY(racerPosY);

         if (racerPosX > root.getWidth())
            racerPosX = 0;
         if (racerPosY > root.getHeight())
            racerPosY = 0;
         if (racerPosX < 0)
            racerPosX = 0;
         if (racerPosY < 0)
            racerPosY = 0;

      }
   }

   

   // background
   class MovableBackground extends Pane {
      private int racerPosX = 0;
      private int racerPosY = 0;
      private ImageView aPicView = null;
      private Label winLabel;
      private TranslateTransition hoppingTransition;
      private CountTask countTask;
      private SwipeTask swipeTask;
      private WaterPlantTask waterPlantTask;
      private MathTask mathTask;
      

      public MovableBackground() {
         if (mapSelection.equals("1")) {
            aPicView = new ImageView(BACKGROUND_IMAGE1);
         }
         if (mapSelection.equals("2")) {
            aPicView = new ImageView(BACKGROUND_IMAGE2);
         }
         if (mapSelection.equals("3")) {
            aPicView = new ImageView(BACKGROUND_IMAGE3);
         }
         this.getChildren().add(aPicView);

         racerPosX = -1000;
         racerPosY = -750;

         // hopping effect
         hoppingTransition = new TranslateTransition(javafx.util.Duration.millis(150), masterCrewmate);
         hoppingTransition.setCycleCount(2);
         hoppingTransition.setAutoReverse(true);
         hoppingTransition.setByY(-15);

         // adding monsters
         monsters = new ArrayList<>();
         monsters.add(new Monster("monster2.png", 520, 950));
         monsters.add(new Monster("monster2.png", 1300, 1900));
         monsters.add(new Monster("monster.png", 2250, 520));
         monsters.add(new Monster("monster2.png", 2250, 2420));

         for (Monster monster : monsters) {
            this.getChildren().add(monster);
         }

         // adding win label
         winLabel = new Label();
         winLabel.setVisible(false);

         //adding tasks
         countTask=new CountTask(taskProgressBar);
         this.getChildren().add(countTask);

         swipeTask=new SwipeTask(taskProgressBar);
         this.getChildren().add(swipeTask);

         waterPlantTask=new WaterPlantTask(taskProgressBar);
         this.getChildren().add(waterPlantTask);

         mathTask=new MathTask(3, taskProgressBar);
         this.getChildren().add(mathTask);

      }

      // win notification
      private void winNotification() {
         if (!winLabel.isVisible()) {
            winLabel.setVisible(true);

            Stage winStage = new Stage();
            winStage.initModality(Modality.APPLICATION_MODAL);
            winStage.setTitle("Congratulations");

            Label winMessageLabel = new Label("WIN");
            winMessageLabel.setFont(Font.font("Arial", FontWeight.BOLD, 80));
            winMessageLabel.setStyle(
                  "-fx-text-fill: gold; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

            StackPane winPane = new StackPane(winMessageLabel);
            Scene winScene = new Scene(winPane, 350, 200);
            winStage.setScene(winScene);
            winStage.show();

            // close after 5 sec
            new Thread(() -> {
               try {
                  Thread.sleep(5000);
                  Platform.runLater(() -> {
                     Stage stage = (Stage) getScene().getWindow();
                     stage.close();
                     winStage.close();
                  });

               } catch (InterruptedException e) {
                  // TODO Auto-generated catch block
                  e.printStackTrace();
               }

            }).start();

         }
      }

      public void update() {

         int newPosX = racerPosX;
         int newPosY = racerPosY;
         speed = getSpeed();
         tasksNeededToWin = getTasksNeededToWin();

         //checking when win is displayed
         if (tasksNeededToWin == 1) {
            if (taskCounter == 1 && monstersKilled == 4) {
               speed = 0;
               hoppingTransition.stop();
               winNotification();
            }
         }
         if (tasksNeededToWin == 2) {
            if (taskCounter == 2 && monstersKilled == 4) {
               speed = 0;
               hoppingTransition.stop();
               winNotification();
            }
         }
         if (tasksNeededToWin == 3) {
            if (taskCounter == 3 && monstersKilled == 4) {
               speed = 0;
               hoppingTransition.stop();
               winNotification();
            }
         }
         if (tasksNeededToWin == 4) {
            if (taskCounter == 4 && monstersKilled == 4) {
               speed = 0;
               hoppingTransition.stop();
               winNotification();
            }
         }

         if (moveDown)
            newPosY -= speed;
         if (moveUP)
            newPosY += speed;
         if (moveLeft)
            newPosX += speed;
         if (moveRight)
            newPosX -= speed;

         aPicView.setTranslateX(racerPosX);
         aPicView.setTranslateY(racerPosY);

         // facing direction
         if (moveRight || moveLeft || moveUP || moveDown) {
            masterCrewmate.setFacingDirection(moveRight);
            if (hoppingTransition.getStatus() != Animation.Status.RUNNING) {
               hoppingTransition.play();
            }
         }

         //background collision
         //calculate character position on the background
         int characterX=masterCrewmate.racerPosX-newPosX;
         int characterY=masterCrewmate.racerPosY-newPosY;

         //check if the pos is in map boundaries
         if(crewmateMovement(characterX, characterY)){
            racerPosX=newPosX;
            racerPosY=newPosY;
         }else{
            if(crewmateMovement(characterX, racerPosY)){
               racerPosY=newPosY;
            }
            if(crewmateMovement(racerPosX, characterY)){
               racerPosX=newPosX;
            }
         }

         // updating monster position
         for (Monster monster : monsters) {
            monster.update(racerPosX, racerPosY, scene);
         }

         //updating the tasks location
         countTask.update(racerPosX, racerPosY, scene);
         swipeTask.update(racerPosX, racerPosY, scene);
         waterPlantTask.update(racerPosX, racerPosY, scene);
         mathTask.update(racerPosX, racerPosY, scene);

        




      }
   }

} // end class Races