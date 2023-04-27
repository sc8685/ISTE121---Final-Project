import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.animation.FadeTransition;
public class Monster extends Pane{

    //attributes
    private double INTERACTION_RADIUS=Game2DClean.INTERACTION_RADIUS;
    private ImageView monsterView;
    private Text killText;
    private boolean isDead=false;
    private int monsterX;
    private int monsterY;
    
    public Monster(String monsterImage, int monsterX, int monsterY){
        this.monsterX=monsterX;
        this.monsterY=monsterY;

        monsterView=new ImageView(new Image(monsterImage));
        monsterView.setFitWidth(200);
        monsterView.setFitHeight(160);
        this.getChildren().add(monsterView);

        killText=new Text("Kill");
        killText.setFont(new Font(20));
        killText.setFill(Color.RED);
        killText.setTranslateX(100);

        killText.setVisible(false);
        this.getChildren().add(killText);

        killText.setOnMouseClicked(e->killMonster());
    }


    //kill method
    public void killMonster(){
        if(!isDead){
            isDead=true;
            killText.setVisible(false);

            //change img
            Image deadMonsterImage=new Image("deadMonster.png");
            monsterView.setImage(deadMonsterImage);
            monsterView.setFitWidth(160);
            monsterView.setFitHeight(200);

            //fade away effect
            FadeTransition fadeOut=new FadeTransition(javafx.util.Duration.millis(1000),monsterView);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.play();
        }
    }

    //update Position method
    public void update(int backgrounX,int backgroundY, Scene scene){
        setTranslateX(monsterX+backgrounX);
        setTranslateY(monsterY+backgroundY);

        if(isDead){
            return;
        }
        int centerX= (int) (scene.getWidth()/2);
        int centerY=(int) (scene.getHeight()/2);
        int monsterScreenX=monsterX+backgrounX;
        int monsterScreenY=monsterY+backgroundY;
        double distance=Math.sqrt(Math.pow(centerX-monsterScreenX, 2)+Math.pow(centerY-monsterScreenY, 2));
        killText.setVisible(distance<=INTERACTION_RADIUS);
    }

    //getters
    public int getMonsterX(){
        return monsterX;
    }
    public int getMonsterY(){
        return monsterY;
    }
    public boolean isDead(){
        return isDead;
    }
}