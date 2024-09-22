/**
 * CS 351 - Project4 - Disease Simulation Project.
 * Utsav Malla, Aashish Basnet
 */

package diseaseSim;

import javafx.application.Platform;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import static java.lang.Math.random;

/**
 * This represents all the active agents. Each agent runs on its own thread
 * which can be used to move the agent, process potential state changes and
 * notify neighboring agents of potential exposure.
 */

public class Agent implements Runnable {
    private double x;
    private double y;
    private double direction;
    private final BlockingQueue<Message> inbox;
    private State state;
    private ArrayList<Agent> neighbors;
    private ArrayList<Agent> neighborsUpdate;
    private int agentNum;
    private int daysSick = 0;
    private static boolean paused = false;
    private static boolean moveOn = true;
    private static double probToSpread;
    private static double probToGetSick = .75;
    private static double probToDie;
    private static double daysOfIncubation;
    private static double daysOfSickness;
    private static double screenWidth = 200;
    private static double screenHeight = 200;
    private static double dayLength;
    private static long simSpeed = 100;
    private static final double MOVE_RADIUS = 2;


    /**
     * Possible agent states during simulation.
     */
    private enum State {
        VULNERABLE,
        INFECTED,
        ASYMPTOMATIC,
        SICK,
        IMMUNE,
        DEAD
    }


    private enum Message {
        HAVE_GERMS,
        BECOME_SICK,
        BECOME_ASYMPTOMATIC,
        BECOME_IMMUNE,
        BECOME_DEAD,
    }


    public Agent(boolean infected, double x, double y, int agentNum) {
        inbox = new ArrayBlockingQueue<>(50);
        this.x = x;
        this.y = y;
        this.agentNum = agentNum;
        direction = Math.random()*2*Math.PI;
        neighbors = new ArrayList<>();
        neighborsUpdate = new ArrayList<>();
        if(infected) this.state = State.INFECTED;
        else this.state = State.VULNERABLE;
    }


    @Override
    public void run() {
        int frameCount = 0;
        while(true) {
            try {
                Thread.sleep(simSpeed);
            } catch (InterruptedException e) {
                System.out.println("e");
            }

            if (!paused) {
                if(state != State.DEAD && moveOn) moveRandDir();
                frameCount++;
                if (frameCount == 5) {
                    if (state == State.SICK || state == State.ASYMPTOMATIC) {
                        for (Agent agent : neighbors) {
                            if (agent != null) {
                                if (random() < probToSpread) {
                                    agent.sendMessage(Message.HAVE_GERMS);
                                }
                            }
                        }
                        daysSick++;
                        if (daysSick >= daysOfSickness) {
                            if(state == State.SICK) {
                                if (random() < probToDie) inbox.add(Message.BECOME_DEAD);
                                else inbox.add(Message.BECOME_IMMUNE);
                                StatsGraph.decSick();
                            }
                            if(state == State.ASYMPTOMATIC) {
                                inbox.add(Message.BECOME_IMMUNE);
                                StatsGraph.decAsym();
                            }
                            daysSick = 0;
                        }
                    }
                    if(state == State.INFECTED){
                        daysSick++;
                        if(daysSick >= daysOfIncubation){
                            if(random() < probToGetSick) {
                                inbox.add(Message.BECOME_SICK);
                            } else {
                                inbox.add(Message.BECOME_ASYMPTOMATIC);
                            }
                            daysSick = 0;
                        }
                    }

                    if (!inbox.isEmpty()) {
                        processInbox();
                    }
                    frameCount = 0;
                }
            }
            setNeighbors();
        }
    }


    private void setNeighbors(){
        neighbors.clear();
        neighbors.addAll(neighborsUpdate);
    }


    protected void updateNeighbors(ArrayList<Agent> agents){
        neighborsUpdate.clear();
        neighborsUpdate.addAll(agents);
    }


    protected static void setProbToSpread(double probToSpread) {
        Agent.probToSpread = probToSpread;
    }


    protected static void setProbToGetSick(double probToGetSick) {
        Agent.probToGetSick = probToGetSick;
    }


    protected static void setProbToDie(double probToDie) {
        Agent.probToDie = probToDie;
    }


    protected static void setDaysOfIncubation(double daysOfIncubation) {
        Agent.daysOfIncubation = daysOfIncubation;
    }


    protected static void setDaysOfSickness(double daysOfSickness) {
        Agent.daysOfSickness = daysOfSickness;
    }


    protected static void setScreenWidth(double screenWidth) {
        Agent.screenWidth = screenWidth;
    }


    protected static void setScreenHeight(double screenHeight) {
        Agent.screenHeight = screenHeight;
    }


    protected static void setSimSpeed(long simSpeed) {
        Agent.simSpeed = simSpeed;
        Agent.dayLength = 5000000*simSpeed;
    }


    protected static void setPaused(boolean pauseOn) {
        Agent.paused = pauseOn;
    }

    protected static void setMovement(boolean moveOn) {
        Agent.moveOn = moveOn;
    }


    protected void relocate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    private void moveRandDir(){
        direction += (Math.random()-.5)*.25*Math.PI;
        double moveDistance = MOVE_RADIUS *(Math.random()*.5)+.5;
        x += Math.cos(direction)*moveDistance;
        y += Math.sin(direction)*moveDistance;
        if(x > screenWidth-5) direction = Math.PI;
        if(y > screenHeight-5) direction = 1.5*Math.PI;
        if(x < 0) direction = 0;
        if(y < 0) direction = .5*Math.PI;
    }


    protected void resetState(boolean infected) {
        this.state = infected ? State.INFECTED : State.VULNERABLE;
    }


    protected void clearMessages() {
        this.inbox.clear();
    }


    public void sendMessage(Message message) {
        inbox.add(message);
    }


    private void processInbox(){
        for (int i = 0; i < inbox.size(); i++) {
            Message message = inbox.poll();
            boolean stateChange = true;
            if (message != null) {
                switch (message) {
                    case HAVE_GERMS:
                        if (state == State.VULNERABLE)
                            state = State.INFECTED;
                        else stateChange = false;
                        break;
                    case BECOME_DEAD:
                        state = State.DEAD;
                        StatsGraph.incDead();
                        break;
                    case BECOME_IMMUNE:
                        state = State.IMMUNE;
                        StatsGraph.incImm();
                        break;
                    case BECOME_ASYMPTOMATIC:
                        state = State.ASYMPTOMATIC;
                        StatsGraph.incAsym();
                        break;
                    case BECOME_SICK:
                        state = State.SICK;
                        StatsGraph.incSick();
                        break;
                    default: stateChange = false;
                }
                if(stateChange) Platform.runLater(() -> HistoryPane.addToHistory(agentNum,this.toString()));
            }
        }
    }


    public String toString(){
        return switch (state) {
            case DEAD -> "dead";
            case VULNERABLE -> "vulnerable";
            case INFECTED -> "infected";
            case ASYMPTOMATIC -> "asymptomatic";
            case SICK -> "sick";
            case IMMUNE -> "immune";
        };
    }

    protected Color getStateColor(){
        return switch (state) {
            case VULNERABLE -> Color.BLUE;
            case INFECTED -> Color.SALMON;
            case SICK -> Color.RED;
            case DEAD -> Color.BLACK;
            case IMMUNE -> Color.GREEN;
            case ASYMPTOMATIC -> Color.YELLOWGREEN;
        };
    }


    public static boolean isPaused() {
        return paused;
    }

    public synchronized double getX() {
        return x;
    }

    public synchronized double getY() {
        return y;
    }

    protected static synchronized double getDayLength(){
        return dayLength;
    }

    protected static synchronized long getSimSpeed() {
        return simSpeed;
    }
}

