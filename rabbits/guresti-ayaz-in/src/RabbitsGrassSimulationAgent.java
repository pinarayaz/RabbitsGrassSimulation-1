
import java.awt.Color;
import java.util.Map;

import uchicago.src.sim.gui.Drawable;
import uchicago.src.sim.gui.SimGraphics;
import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation agent for the rabbits grass simulation.

 * @author
 */

public class RabbitsGrassSimulationAgent implements Drawable {
	
	private static final int MININITIALENERGY = 10;
	private static final int MAXINITIALENERGY = 90;
	private static final int STEPENERGYLOSS = 5;
	private static final int BIRTHENERGYLOSS = 40;
	private static final int GRASSENERGY = 10;
	
	private int x;
	private int y;
	private int energy;
	private static final int[][] validMoves = {{0,1}, {0,-1}, {1,0}, {-1,0}};
	private static int IDNumber = 0;
	private int ID;
	
	private RabbitsGrassSimulationModel grassModel;
	private RabbitsGrassSimulationSpace grassSpace;
	private RabbitsGrassSimulationSpace rabbitSpace;
	
	public RabbitsGrassSimulationAgent(){
		x = -1;
		y = -1;
		energy = (int)((Math.random() * (MAXINITIALENERGY - MININITIALENERGY)) + MININITIALENERGY);
	    IDNumber++;
	    ID = IDNumber;
	}
	
	public void setXY(int newX, int newY){
		x = newX;
	    y = newY;
	}
	
	public int[] setMove() {
		int i = (int)Math.floor(Math.random() * 4);
		return validMoves[i];
	}
	
	public void setRabbitsGrassSimulationSpace(RabbitsGrassSimulationSpace grassSpace){
	    this.grassSpace = grassSpace;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}
	
	public int getEnergy() {
		return energy;
	}
	

	public String getID(){
	    return "R-" + ID;
	}

	public void report(){
	    System.out.println(getID() + " at " + x + ", " + y + " has " + getEnergy() + " energy.");
	}	
	
	public void step(){
		report();
	    
	    Object2DGrid grid = grassSpace.getCurrentRabbitSpace();
	    
	    if(move()) {
	    	energy -= STEPENERGYLOSS;
		    report();
	    }
	    
	}
	
	public boolean move() {
		boolean retVal = false;
		if(energy > STEPENERGYLOSS) {
			int[] move = setMove();
		    int newX = x + move[0];
		    int newY = y + move[1];
		    
		    Object2DGrid grid = grassSpace.getCurrentRabbitSpace();
		    
		    while(newX < 0 || newX >= grid.getSizeX() || newY < 0 || newY >= grid.getSizeY()) {
			    move = setMove();
			    newX = x + move[0];
			    newY = y + move[1];
		    }
		    
		    int oldX = x;
		    int oldY = y; 
		    
		    if(tryMove(newX, newY)){
		    	retVal = true;
		    	System.out.println("R-" + ID + " lost " + STEPENERGYLOSS + ".");
		    	int gainedEnergy = GRASSENERGY * grassSpace.eatGrassAt(newX, newY);
		    	energy += gainedEnergy;
		    	System.out.println("R-" + ID + " gained " + gainedEnergy + ".");
		    }
		    else{
		    	grassSpace.moveRabbitAt(x, y, oldX, oldY);
		        System.out.println("R-" + ID + " collided with " 
		        		+ grassSpace.getRabbitAt(newX, newY).getID() + " at " +
		        		newX + ", " + newY +  "and returned to previous location.");
		    }
		}
		else energy = 0;
	    return retVal;
	}
	
	public boolean tryMove(int newX, int newY){
	    return grassSpace.moveRabbitAt(x, y, newX, newY);
	}
	
	public void giveBirth() {
		energy -= BIRTHENERGYLOSS;
    	System.out.println("R-" + ID + " lost " + BIRTHENERGYLOSS + ".");
    	report();
	}

	public void draw(SimGraphics graphic) {
		if(!grassSpace.isCellOccupied(this.getX(), this.getY())) {
			if(grassSpace.getGrassAt(this.getX(), this.getY()) == 0) {
				graphic.drawFastRoundRect(Color.BLACK);
			}
			else {
				graphic.drawFastRoundRect(Color.GREEN);
			}
		}
		else {
			graphic.drawFastRoundRect(Color.WHITE);
		}
		
	}

}
