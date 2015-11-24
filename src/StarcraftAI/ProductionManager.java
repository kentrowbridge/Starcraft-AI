package StarcraftAI;
import java.util.*;

import bwapi.*;
/**
 * The production manager is responsible for building units that the strategy manager requests. 
 * ProductionManager uses the WorkerManager and the BuildingManager to handle build and research 
 * orders passed to it from the StrategyManager.
 */
public class ProductionManager {
	
	private Game game;
	private ArrayList<List<UnitType>> productionQueue; 
	private ArrayList<UnitType> goal;
	private ArrayList<UnitType> newGoal;
	private ArrayList<List<UnitType>> techDag; 
	private ArrayList<List<UnitType>> paths;  
	private BuildingManager buildingManager;
	private WorkerManager workerManager;
	
	
	public ProductionManager(Game game){
		this.game = game;
		
		this.buildingManager = new BuildingManager();
		this.workerManager = new WorkerManager(game.getNeutralUnits());
		
		//add starting workers to worker list
		for(Unit u : game.self().getUnits())
		{
			if(u.getType() == UnitType.Terran_SCV)
			{
				workerManager.addUnit(u);
			}
		}
	}
	
	/**
	 * addUnit()
	 * This checks the type of the given unit and then calls the appropriate 
	 * addUnit method in either the BuildingManager or WorkerManager.
	 * 
	 * @param unit - the specific unit that we are checking
	 */
	public void addUnit(Unit unit){			
	}
	
	/**
	 * setGoal()
	 * This method sets the newGoal instance variable to the specified parameter.
	 * 
	 * @param newGoal - the new goal instance variable
	 */
	public void setGoal(ArrayList<UnitType> newGoal){
		
	}
	
	/** 
	 * buildBuilding()
	 * This method will retrieve a worker from the Worker Manager,
	 * then using that worker, issue a build command to the Building Manager to construct 
	 * the building type specified.
	 *  
	 * @param unitType
	 */
	public void buildBuilding(UnitType unitType){
		
	}
	
	/**
	 * training()
	 * This method is responsible for issuing an order to train a unit, 
	 * specified by the UnitType parameter, from a given building, provided by the Unit parameter.
	 * 
	 * @param unitType
	 * @param building
	 */
	public void training(UnitType unitType, Unit building){
		
	}
	
	/**
	 * update()
	 * This method is responsible for calling the update methods in both the Building Manager 
	 * and the Worker Manager. It checks if goal and newGoal are the same and if so,
	 * it sets goal to newGoal and updates the productionQueue. 
	 * It then calls processQueue to initiate building construction, unit training, 
	 * and technology research using the productionQueue.
	 * 
	 */
	public void update(){
		workerManager.update();
	}
	
	/**
	 * processQueue()
	 * The process queue method is responsible for sending out build orders based on the 
	 * contents of the priority queue. This method will be called every time update() is run. 
	 * This method will examine the front of the first of the priority queue and use the Worker 
	 * Manager to acquire a builder unit and then send a build order to the Building Manager using 
	 * that unit and the building type taken from the priority queue.
	 * 
	 */
	public void processQueue(){
			
	}
	
	/**
	 * findTechPath()
	 * This method is responsible for taking a desired unit type and constructing a
	 * dependency list based on the tech DAG. We will use Dijkstra’s shortest path algorithm 
	 * in order to implement this. 
	 * 
	 * @param goalUnit
	 * @return a dependency list of what to construct (in what order) for the specific unit
	 */
	public ArrayList<UnitType> findTechPath(UnitType goalUnit){
		return null;
	}
	
	/**
	 * examinePath()
	 * This method is responsible for examining a tech path list and determining if a 
	 * subsequence of the dependency list is already in the priority queue.
	 * If there is a subsequence that is found, that sequence will be removed from the list.
	 * 
	 * @param path
	 * @return path of buildings that have not been constructed yet
	 */
	public ArrayList<UnitType> examinePath(ArrayList<UnitType> path){
		return null; 
	}
	
	
}
