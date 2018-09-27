import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import uchicago.src.sim.analysis.BinDataSource;
import uchicago.src.sim.analysis.DataSource;
import uchicago.src.sim.analysis.OpenHistogram;
import uchicago.src.sim.analysis.OpenSequenceGraph;
import uchicago.src.sim.analysis.Sequence;
import uchicago.src.sim.engine.BasicAction;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.ColorMap;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.Value2DDisplay;
import uchicago.src.sim.space.Object2DGrid;
import uchicago.src.sim.util.SimUtilities;

/**
 * Class that implements the simulation model for the rabbits grass
 * simulation.  This is the first class which needs to be setup in
 * order to run Repast simulation. It manages the entire RePast
 * environment and the simulation.
 *
 * @author 
 */


public class RabbitsGrassSimulationModel extends SimModelImpl {	
		//Default Values
		private static final int NUMRABBITS = 5;
		private static final int GRIDSIZEX = 20;
		private static final int GRIDSIZEY = 20;
		private static final int BIRTHTHRESHOLD = 100;
		private static final int GRASSGROWTHRATE = 5;
		private static final int INITIALGRASS = 250;
		
		private int numRabbits = NUMRABBITS;
		private int gridSizeX = GRIDSIZEX;
		private int gridSizeY = GRIDSIZEY;
		private int birthThreshold = BIRTHTHRESHOLD;
		private int grassGrowthRate = GRASSGROWTHRATE;
		private int initialGrass = INITIALGRASS;
		
		private Schedule schedule;
		
		private RabbitsGrassSimulationSpace grassSpace;
		
		private ArrayList<RabbitsGrassSimulationAgent> rabbitList;
		
		private DisplaySurface displaySurf;
		
		private OpenSequenceGraph amountOfTotalPopulationInSpace;
		private OpenHistogram agentSurvivalDistribution;

		class rabbitsInSpace implements DataSource, Sequence {

			public Object execute() {
				return new Double(getSValue());
			}

			public double getSValue() {
				return (double)(grassSpace.getTotalRabbits());
			}
		}
		
		class grassInSpace implements DataSource, Sequence {

			public Object execute() {
				return new Double(getSValue());
			}

			public double getSValue() {
				return (double)(grassSpace.getTotalGrass());
			}
		}

		public static void main(String[] args) {			
			System.out.println("Rabbit skeleton");		
			SimInit init = new SimInit();
			RabbitsGrassSimulationModel model = new RabbitsGrassSimulationModel();
			init.loadModel(model, "", false);
		}
		
		class agentRabbit implements BinDataSource{
	        public double getBinValue(Object o) {
	          RabbitsGrassSimulationAgent rabbit = (RabbitsGrassSimulationAgent)o;
	          return (double)rabbit.getEnergy();
	        }
	    }

		public void setup() {
			System.out.println("Running setup");
			grassSpace = null;
			rabbitList = new ArrayList<RabbitsGrassSimulationAgent>();
			schedule = new Schedule(1);
			
		    if (displaySurf != null){
		        displaySurf.dispose();
		    }
		    displaySurf = null;
		    
		    if (amountOfTotalPopulationInSpace != null){
		        amountOfTotalPopulationInSpace.dispose();
		      }
		    amountOfTotalPopulationInSpace = null;

		    if (agentSurvivalDistribution != null){
		    	agentSurvivalDistribution.dispose();
		      }
		    agentSurvivalDistribution = null;
		    
		    displaySurf = new DisplaySurface(this, "Rabbits Grass Simulation Model Window 1");
		    amountOfTotalPopulationInSpace = new OpenSequenceGraph("Amount Of Total Rabbits and Grass In Space",this);
		    agentSurvivalDistribution = new OpenHistogram("Agent Energy", 8, 0);
		    
		    registerDisplaySurface("Rabbits Grass Simulation Model Window 1", displaySurf);
		    this.registerMediaProducer("Plot", amountOfTotalPopulationInSpace);
		}
		
		public void begin() {			
			buildModel();
			buildSchedule();
			buildDisplay();
			
			displaySurf.display();
			amountOfTotalPopulationInSpace.display();
			agentSurvivalDistribution.display();
		}
		
		public void buildModel() {
			System.out.println("Running BuildModel");
			grassSpace = new RabbitsGrassSimulationSpace(gridSizeX, gridSizeY);
			grassSpace.growGrass(initialGrass);
			for(int i = 0; i < numRabbits; i++){
			    addNewRabbit();
			}
		    for(int i = 0; i < rabbitList.size(); i++){
		        RabbitsGrassSimulationAgent rabbit = rabbitList.get(i);
		        rabbit.report();
		    }
		}

		public void buildSchedule() {			
			System.out.println("Running BuildSchedule");
			
		    class RabbitsGrassSimulationStep extends BasicAction {
		        public void execute() {
		          SimUtilities.shuffle(rabbitList);
		          for(int i =0; i < rabbitList.size(); i++){
		            RabbitsGrassSimulationAgent rabbit = rabbitList.get(i);
		            rabbit.step();
		          }
		          
		          for(int i = 0; i < rabbitList.size(); i++){
		        	  RabbitsGrassSimulationAgent rabbit = rabbitList.get(i);
			            if(rabbit.getEnergy() >= 100) {
			            	rabbit.giveBirth();
			            	addNewRabbit();
			            }
			      }
		          
		          reapDeadRabbits();
		          
		          grassSpace.growGrass(GRASSGROWTHRATE);
		          
		          displaySurf.updateDisplay();
		        }
		    }    

		    schedule.scheduleActionBeginning(0, new RabbitsGrassSimulationStep());
		      
		    class RabbitsGrassSimulationCountLiving extends BasicAction {
		        public void execute(){
		            countLivingRabbits();
		        }
		    }   

		    schedule.scheduleActionAtInterval(10, new RabbitsGrassSimulationCountLiving());
		
		    class RabbitsGrassUpdateRabbitInSpace extends BasicAction {
		        public void execute(){
		          amountOfTotalPopulationInSpace.step();
		        }
		    }

		    schedule.scheduleActionAtInterval(10, new RabbitsGrassUpdateRabbitInSpace());
		
		    class RabbitsGrassSimulationUpdateAgentEnergy extends BasicAction {
		        public void execute(){
		          agentSurvivalDistribution.step();
		        }
		      }

		      schedule.scheduleActionAtInterval(10, new RabbitsGrassSimulationUpdateAgentEnergy());
		    
		}    
		
		public void buildDisplay() {			
			System.out.println("Running BuildDisplay");
			
			ColorMap map = new ColorMap();

		    for(int i = 1; i<16; i++){
		      map.mapColor(i, new Color( 0, (int)(i * 8 + 127), 0));
		    }
		    map.mapColor(0, Color.BLACK);

		    Value2DDisplay displayGrass =
		        new Value2DDisplay(grassSpace.getCurrentGrassSpace(), map);
		    
		    Object2DDisplay displayAgents = new Object2DDisplay(grassSpace.getCurrentGrassSpace());
		    displayAgents.setObjectList(rabbitList);

		    displaySurf.addDisplayableProbeable(displayGrass, "Grass");
		    displaySurf.addDisplayableProbeable(displayAgents, "Rabbits");

		    amountOfTotalPopulationInSpace.addSequence("Rabbits In Space", new rabbitsInSpace());
		    amountOfTotalPopulationInSpace.addSequence("Grass In Space", new grassInSpace());
		    agentSurvivalDistribution.createHistogramItem("Rabbit energy",rabbitList,new agentRabbit());
		}
		
		public void addNewRabbit(){
			RabbitsGrassSimulationAgent rabbit = new RabbitsGrassSimulationAgent();
		    rabbitList.add(rabbit);
		    grassSpace.addRabbit(rabbit);
		}
		
		private int countLivingRabbits(){
		    int livingAgents = 0;
		    for(int i = 0; i < rabbitList.size(); i++){
		        RabbitsGrassSimulationAgent rabbit = rabbitList.get(i);
			    if(rabbit.getEnergy() > 0) livingAgents++;
			}
			System.out.println("Number of living agents is: " + livingAgents);

			return livingAgents;
		}
		
		private int reapDeadRabbits(){
		    int count = 0;
		    for(int i = (rabbitList.size() - 1); i >= 0 ; i--){
		    	RabbitsGrassSimulationAgent rabbit = rabbitList.get(i);
			    if(rabbit.getEnergy() <= 0){
			        grassSpace.removeRabbitAt(rabbit.getX(), rabbit.getY());
			        rabbitList.remove(i);
			        System.out.println(rabbit.getID() + " died at " + rabbit.getX() + ", " + rabbit.getY() + ".");
			        count++;
			    }
			}
			return count;
		}
		
		public String[] getInitParam() {			
			String[] initParams = {"NumRabbits", "GridSizeX", "GridSizeY", "BirthThreshold", "GrassGrowthRate", "InitialGrass"};
			return initParams;					
		}

		public String getName() {			
			return "Rabbits Grass Simulation Model";
		}

		public Schedule getSchedule() {			
			return schedule;			
		}
		
		public int getNumRabbits() {			
			return numRabbits;			
		}
		
		public void setNumRabbits(int numRabbits) {			
			this.numRabbits = numRabbits;			
		}
		
		public int getGridSizeX() {			
			return gridSizeX;			
		}
		
		public void setGridSizeX(int gridSizeX) {			
			this.gridSizeX = gridSizeX;			
		}
		
		public int getGridSizeY() {			
			return gridSizeY;			
		}
		
		public void setGridSizeY(int gridSizeY) {			
			this.gridSizeY = gridSizeY;			
		}
		
		public int getBirthThreshold() {			
			return birthThreshold;			
		}
		
		public void setBirthThreshold(int birthThreshold) {			
			this.birthThreshold = birthThreshold;			
		}
		
		public int getGrassGrowthRate() {			
			return grassGrowthRate;			
		}
		
		public void setGrassGrowthRate(int grassGrowthRate) {			
			this.grassGrowthRate = grassGrowthRate;			
		}
		
		public int getInitialGrass() {			
			return initialGrass;			
		}
		
		public void setInitialGrass(int initialGrass) {			
			this.initialGrass = initialGrass;			
		}
		
}
