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
	private Player self; 
	
	private ArrayList<List<UnitType>> productionQueue; 
	private ArrayList<UnitType> goals;
	private ArrayList<UnitType> newGoal;
	private ArrayList<List<UnitType>> techDag; 
	private ArrayList<List<UnitType>> paths;  
	private BuildingManager buildingManager;
	private WorkerManager workerManager;
	
	private Hashtable<UnitType, UnitType> buildingsForUnits = new Hashtable<UnitType, UnitType>();
	
	public ProductionManager(Game game, Player self){
		this.game = game;
		this.self = self;
		
		this.buildingManager = new BuildingManager(game, self);
		
		//add starting workers to worker list
		for(Unit u : game.self().getUnits())
		{
			if(u.getType() == UnitType.Terran_SCV)
			{
				workerManager.addUnit(u);
			}
		}
		
		initBuildingsForUnits();
	}

	/**
	 * addUnit()
	 * This checks the type of the given unit and then calls the appropriate 
	 * addUnit method in either the BuildingManager or WorkerManager.
	 * 
	 * @param unit - the specific unit that we are checking
	 */
	public void addUnit(Unit unit)
	{		if (unit == null)
			return;
		
		if(unit.getType().isBuilding())
		{
			buildingManager.addUnit(unit);
		}
		else if(unit.getType() == UnitType.Terran_SCV)
		{
			workerManager.addUnit(unit);
		}
	}
	
	/**
	 * setGoal()
	 * This method sets the newGoal instance variable to the specified parameter.
	 * 
	 * @param newGoal - the new goal instance variable
	 */
	public void setGoal(ArrayList<UnitType> newGoal)
	{
		this.newGoal = newGoal;
	}
	
	/** 
	 * buildBuilding()
	 * This method will retrieve a worker from the Worker Manager,
	 * then using that worker, issue a build command to the Building Manager to construct 
	 * the building type specified.
	 *  
	 * @param unitType - type of building ot build
	 */
	public void buildBuilding(UnitType unitType)
	{
		if(unitType.isBuilding())
		{
			Unit builder = workerManager.getWorker();
			
			//make sure the builder is not null
			if(builder != null)
			{
				buildingManager.build(unitType, builder);
			}
		}
	}
	
	/**
	 * training()
	 * This method is responsible for issuing an order to train a unit, 
	 * specified by the UnitType parameter, from a given building, provided by the Unit parameter.
	 * 
	 * @param unitType - unit type to train
	 * @param building - building to train from
	 */
	public void training(UnitType unitType, Unit building)
	{
		if(unitType == null || building == null)
			return;
		
		building.train(unitType);
	}
	
	/**
	 * update()
	 * This method is responsible for calling the update methods in both the Building Manager 
	 * and the Worker Manager. It checks if goal and newGoal are not the same and if so,
	 * it sets goal to newGoal and updates the productionQueue. 
	 * It then calls processQueue to initiate building construction, unit training, 
	 * and technology research using the productionQueue.
	 * 
	 */
	public void update()
	{
		buildingManager.update();
		workerManager.update();
		
		//if goal and new goal are the same, 
		if(!Arrays.deepEquals(goals.toArray(), newGoal.toArray()))
		{
			goals = newGoal;
			
			//find paths for all of the goals
			//update production queue
			for(UnitType u : goals)
			{
				//create paths for goals
				List<UnitType> path = new ArrayList<UnitType>();
				
				//only add end goal for now
				//THIS WILL BE CHANGED LATER ON IN IMPLEMENTATION
				path.add(u);
				
				//add path to production q
				productionQueue.add(path);
			}
		}
		
		processQueue();
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
	public void processQueue()
	{
		for(List<UnitType> buildPath : productionQueue)
		{
			//temporarily: build path is always going to be the next thing that needs to be built
			// dependency conflicts are handled by strategy manager FOR NOW
			UnitType item = buildPath.get(0);
			
			if(item != null)
			{
				if(item.isBuilding())
				{
					buildBuilding(item);
				}
				else
				{
					//find building type that builds the item
					UnitType buildingType = buildingsForUnits.get(item);
					//retrieve one of those buildings
					Unit building = buildingManager.getBuilding(buildingType);
					
					if(building != null)
					{
						training(item, building);
					}
				}
			}
		}
	}
	
	/**
	 * findTechPath()
	 * This method is responsible for taking a desired unit type and constructing a
	 * dependency list based on the tech DAG. We will use Dijkstra’s shortest path algorithm 
	 * in order to implement this. 
	 * 
	 * @param goalUnit - end point of the path
	 * @return a dependency list of what to construct (in what order) for the specific unit
	 */
	public ArrayList<UnitType> findTechPath(UnitType goalUnit)
	{
		return null;
	}
	
	/**
	 * examinePath()
	 * This method is responsible for examining a tech path list and determining if a 
	 * subsequence of the dependency list is already in the priority queue.
	 * If there is a subsequence that is found, that sequence will be removed from the list.
	 * 
	 * @param path - path to examine
	 * @return path of buildings that have not been constructed yet
	 */
	public ArrayList<UnitType> examinePath(ArrayList<UnitType> path)
	{
		return null; 
	}
	
	/**
	 * initBuildingsForUnits()
	 * initializes the hashtable that determines what buildings builds a unit
	 */
	private void initBuildingsForUnits() 
	{
		//barracks
		buildingsForUnits.put(UnitType.Terran_Marine, UnitType.Terran_Barracks);
		buildingsForUnits.put(UnitType.Terran_Medic, UnitType.Terran_Barracks);
		buildingsForUnits.put(UnitType.Terran_Firebat, UnitType.Terran_Barracks);
		buildingsForUnits.put(UnitType.Terran_Ghost, UnitType.Terran_Barracks);
		
		//factory
		buildingsForUnits.put(UnitType.Terran_Vulture, UnitType.Terran_Factory);
		buildingsForUnits.put(UnitType.Terran_Siege_Tank_Tank_Mode, UnitType.Terran_Factory);
		buildingsForUnits.put(UnitType.Terran_Goliath, UnitType.Terran_Factory);
		
		//starport
		buildingsForUnits.put(UnitType.Terran_Wraith, UnitType.Terran_Starport);
		buildingsForUnits.put(UnitType.Terran_Dropship, UnitType.Terran_Starport);
		buildingsForUnits.put(UnitType.Terran_Valkyrie, UnitType.Terran_Starport);
		buildingsForUnits.put(UnitType.Terran_Science_Vessel, UnitType.Terran_Starport);
		buildingsForUnits.put(UnitType.Terran_Battlecruiser, UnitType.Terran_Starport);
		
		//command center
		buildingsForUnits.put(UnitType.Terran_SCV, UnitType.Terran_Command_Center);
	}
}
