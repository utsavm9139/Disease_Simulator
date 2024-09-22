/**
 * CS 351 - Project4 - Disease Simulation Project.
 * Utsav Malla, Aashish Basnet
 */

package diseaseSim;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class StatsGraph extends Pane {
    private static double agentNum;
    private static double vulNum;
    private static double deadNum;
    private static double sickNum;
    private static double immNum;
    private static double asymNum;
    private static final double HEIGHT = 200;
    private static final double WIDTH = 30;
    private static final Canvas canvas = new Canvas(250, 200);
    private static final GraphicsContext gc = canvas.getGraphicsContext2D();

    public StatsGraph() {
        setMinHeight(200);
        setMinWidth(280);
        getChildren().add(canvas);
    }

    private static void drawVulBar() {
        gc.setFill(Color.BLUE);
        gc.clearRect(20,0,WIDTH, HEIGHT);
        double h = (vulNum/agentNum)*HEIGHT;
        gc.fillRect(20,200 - h, WIDTH, h);
    }

    private static void drawSickBar() {
        gc.setFill(Color.RED);
        gc.clearRect(60,0, WIDTH, HEIGHT);
        double h = (sickNum/agentNum)*HEIGHT;
        gc.fillRect(60,200 - h, WIDTH, h);
    }

    private static void drawAsymBar() {
        gc.setFill(Color.YELLOWGREEN);
        gc.clearRect(100,0, WIDTH, HEIGHT);
        double h = (asymNum/agentNum)*HEIGHT;
        gc.fillRect(100,200 - h, WIDTH, h);
    }


    private static void drawDeadBar() {
        gc.setFill(Color.BLACK);
        gc.clearRect(140,0, WIDTH, HEIGHT);
        double h = (deadNum/agentNum)*HEIGHT;
        gc.fillRect(140,200 - h, WIDTH, h);
    }

    private static void drawImmBar() {
        gc.setFill(Color.GREEN);
        gc.clearRect(180,0, WIDTH, HEIGHT);
        double h = (immNum/agentNum)*HEIGHT;
        gc.fillRect(180,200 - h, WIDTH, h);
    }

    protected static synchronized void setAgentNum(int agentNum){
        StatsGraph.agentNum = agentNum;
        vulNum = agentNum;
        gc.setFill(Color.BLACK);
        gc.setFont(new Font(9));
        gc.fillText((int)agentNum + "-",0,6);
        gc.fillText((int)3*agentNum/4 + "-",0,HEIGHT/4);
        gc.fillText((int)agentNum/2 + "-",0,HEIGHT/2);
        gc.fillText((int)agentNum/4 + "-",0,3*HEIGHT/4);
        gc.fillText("-" + (int)agentNum,212,6);
        gc.fillText("-" + (int)3*agentNum/4,212,HEIGHT/4);
        gc.fillText("-" + (int)agentNum/2,212,HEIGHT/2);
        gc.fillText("-" + (int)agentNum/4,212,3*HEIGHT/4);
        drawVulBar();
    }

    protected static synchronized void incDead(){
        deadNum++;
        drawDeadBar();
    }

    protected static synchronized void incSick(){
        sickNum++;
        drawSickBar();
        vulNum--;
        drawVulBar();
    }


    protected static synchronized void decSick() {
        sickNum--;
        drawSickBar();
    }


    protected static synchronized void incImm(){
        immNum++;
        drawImmBar();
    }

    protected static synchronized void incAsym(){
        asymNum++;
        drawAsymBar();
        vulNum--;
        drawVulBar();
    }


    protected static synchronized void decAsym(){
        asymNum--;
        drawAsymBar();
    }


    protected static synchronized void reset(){
        vulNum = agentNum;
        deadNum = 0;
        sickNum = 0;
        immNum = 0;
        asymNum = 0;
        gc.clearRect(20,0,192,200);
        drawVulBar();
    }
}
