/**
 * CS 351 - Project4 - Disease Simulation Project.
 * Utsav Malla, Aashish Basnet
 */

package diseaseSim;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;

/**
 * Pane is used to display the simulation.
 */


public class UI extends Pane {
    private final GraphicsContext gc;
    private final double WIDTH;
    private final double HEIGHT;
    private final Agent[][] grid;
    /**
     * @param grid Array of agents comprising the simulation
     * @param width Width of the simulation
     * @param height Height of the simulation
     */

    UI(Agent[][] grid, double width, double height){
        this.grid = grid;
        this.WIDTH = width;
        this.HEIGHT = height;
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        Agent.setScreenWidth(WIDTH);
        Agent.setScreenHeight(HEIGHT);
        gc = canvas.getGraphicsContext2D();
        this.getChildren().add(canvas);
    }

    /**
     * Creates the UI canvas and the agents.
     * @param rows number of rows in array.
     * @param cols number of columns in array.
     */

    protected void updateAgents(int rows, int cols) {
        gc.clearRect(0,0, WIDTH, HEIGHT);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] != null) {
                    gc.setFill(grid[i][j].getStateColor());
                    gc.fillOval(grid[i][j].getX(),grid[i][j].getY(),8,8);
                }
            }
        }
    }

    /**
     * @return Width
     */
    protected final double getWIDTH() {
        return WIDTH;
    }

    /**
     * @return Height
     */
    protected final double getHEIGHT() {
        return HEIGHT;
    }
}
