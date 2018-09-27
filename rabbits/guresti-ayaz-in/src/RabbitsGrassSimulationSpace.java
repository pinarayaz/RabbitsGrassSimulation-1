
import uchicago.src.sim.space.Object2DGrid;

/**
 * Class that implements the simulation space of the rabbits grass simulation.
 * @author 
 */

public class RabbitsGrassSimulationSpace {
private Object2DGrid grassSpace;
private Object2DGrid rabbitSpace;

	public RabbitsGrassSimulationSpace(int xSize, int ySize) {
		grassSpace = new Object2DGrid(xSize, ySize);
		rabbitSpace = new Object2DGrid(xSize, ySize);
		
		for(int i = 0; i < xSize; i++) {
			for(int j = 0; j < ySize; j++) {
				grassSpace.putObjectAt(i,j,new Integer(0));
			}
		}
	}
	
	public void growGrass(int grass) {
		for(int i = 0; i < grass; i++) {
			int x = (int)(Math.random()*(grassSpace.getSizeX()));
			int y = (int)(Math.random()*(grassSpace.getSizeY()));
			
			int I = getGrassAt(x, y);
			grassSpace.putObjectAt(x, y, new Integer(I+1));
		}
	}
	
	public int getGrassAt(int x, int y) {
		int i;
		if(grassSpace.getObjectAt(x,y)!= null){
			i = ((Integer)grassSpace.getObjectAt(x,y)).intValue();
		}
		else{
		    i = 0;
		}
		return i;
	}
	
	public RabbitsGrassSimulationAgent getRabbitAt(int x, int y) {
		return (RabbitsGrassSimulationAgent) rabbitSpace.getObjectAt(x, y);
	}
	
	public Object2DGrid getCurrentGrassSpace(){
	    return grassSpace;
	}
	
	public Object2DGrid getCurrentRabbitSpace(){
	    return rabbitSpace;
	}
	

	public boolean isCellOccupied(int x, int y){
	    return (rabbitSpace.getObjectAt(x, y)!=null);
	}
	
	public boolean addRabbit(RabbitsGrassSimulationAgent rabbit){
	    boolean retVal = false;
		int count = 0;
		int countLimit = 10 * rabbitSpace.getSizeX() * rabbitSpace.getSizeY();

		while((retVal==false) && (count < countLimit)){
		    int x = (int)(Math.random()*(rabbitSpace.getSizeX()));
		    int y = (int)(Math.random()*(rabbitSpace.getSizeY()));
		    if(isCellOccupied(x,y) == false){
		        rabbitSpace.putObjectAt(x,y,rabbit);
		        rabbit.setXY(x,y);
		        rabbit.setRabbitsGrassSimulationSpace(this);
		        retVal = true;
		    }
		    count++;
		    System.out.println(rabbit.getID() + " is born at " + rabbit.getX() + ", " + rabbit.getY() + ".");
		}

		return retVal;
	}
	
	public void removeRabbitAt(int x, int y){
	    rabbitSpace.putObjectAt(x, y, null);
	}
	
	public int eatGrassAt(int x, int y){
	    int grass = getGrassAt(x, y);
	    grassSpace.putObjectAt(x, y, new Integer(0));
	    return grass;
	}
	
	public boolean moveRabbitAt(int x, int y, int newX, int newY){
	    boolean retVal = false;
	    if(!isCellOccupied(newX, newY)){
	        RabbitsGrassSimulationAgent rabbit = (RabbitsGrassSimulationAgent)rabbitSpace.getObjectAt(x, y);
		    removeRabbitAt(x,y);
		    rabbit.setXY(newX, newY);
		    rabbitSpace.putObjectAt(newX, newY, rabbit);
		    retVal = true;
		    System.out.println(rabbit.getID() + " moved " + " to " + newX + ", " + newY + ".");
		}
		return retVal;
	}
	
}
