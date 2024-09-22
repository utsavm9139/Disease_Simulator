/**
 * CS 351 - Project4 - Disease Simulation Project.
 * Utsav Malla, Aashish Basnet
 */

package diseaseSim;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

/**
 * Pane is used to make the HistoryPane
 */

public class HistoryPane extends Pane {
    private static final Label historyLbl = new Label("History:");
    private static final VBox listBox = new VBox(10, historyLbl);
    private static int day = 0;


    public HistoryPane() {
        getChildren().add(listBox);
        setMinWidth(280);
    }


    protected static void addToHistory(int agentNum, String message){
        Label historyLine = new Label("Agent " + agentNum + " became "
                + message + " on Day " + day);
        listBox.getChildren().add(1,historyLine);
        if(listBox.getChildren().size() > 6) listBox.getChildren().remove(6);
    }

    protected static void reset(){
        listBox.getChildren().remove(1,listBox.getChildren().size());
        day = 0;
    }

    /**
     * Increments the current day elapsed in the simulation by 1
     */

    protected static void incDay() {
        HistoryPane.day++;
    }
}


