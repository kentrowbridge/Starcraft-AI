package StarcraftAI;
import java.util.*;

import bwapi.*;
/**
 * The production manager is responsible for building units that the strategy manager requests. 
 * ProductionManager uses the WorkerManager and the BuildingManager to handle build and research 
 * orders passed to it from the StrategyManager.
 * 
 * @author Kenny Trowbridge
 * @author Alex Bowns
 * @author Max Robinson
 */
public class ProductionManager {
	
	private Game game;
	private Player self; 
	
	private Hashtable<UnitType, ArrayList<UnitType>> techPaths; 
	
	private ArrayList<List<UnitType>> productionQueue = new ArrayList<List<UnitType>>(); 
	private ArrayList<UnitType> goals = new ArrayList<UnitType>();
	private ArrayList<UnitType> newGoal = new ArrayList<UnitType>();
	private ArrayList<List<UnitType>> techDag; 
	private ArrayList<List<UnitType>> paths;  
	private BuildingManager buildingManager;
	private WorkerManager workerManager;
	
	private static Hashtable<UnitType, UnitType> buildingsForUnits = new Hashtable<UnitType, UnitType>();
	
	/**
	 * Ctor
	 * Sets up the needed instance variables for the class and sets up the game and player objects needed 
	 * to reference the game.
	 * 
	 * @param game
	 * @param self
	 */
	public ProductionManager(Game game, Player self){
		this.game = game;
		this.self = self;
		
		this.techPaths = new Hashtable<UnitType, ArrayList<UnitType>>();
		
		this.buildingManager = new BuildingManager(game, self);
		this.workerManager = new WorkerManager(self, game.getNeutralUnits());
		
		this.productionQueue = new ArrayList<List<UnitType>>();
		this.goals = new ArrayList<UnitType>();
		this.newGoal = new ArrayList<UnitType>();
		this.techDag = new ArrayList<List<UnitType>>();
		this.paths = new ArrayList<List<UnitType>>();
		
		//add starting workers to worker list
		for(Unit u : game.self().getUnits())
		{
			if(u.getType() == UnitType.Terran_SCV)
			{
				workerManager.addUnit(u);
			}
		}
		
		initBuildingsForUnits();
		
		techPaths = ProductionManager.initTechPaths();
		for(UnitType key : techPaths.keySet()){
			System.out.println(techPaths.get(key).toString());
		}
		
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
			
			// make sure the builder is not null
			if(builder != null && game.canMake(builder, unitType))
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
		{
			return;
		}
		
		if(!building.isTraining())
		{
			building.train(unitType);
		}
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
		try
		{
			buildingManager.update();
			workerManager.update();
			
			//if goal and new goal are the same, 
			if(!Arrays.deepEquals(goals.toArray(), newGoal.toArray()))
			{
				goals = newGoal;
				
				productionQueue.clear();
				
				//find paths for all of the goals
				//update production queue
				for(UnitType u : goals)
				{
					//create paths for goals
//					List<UnitType> path = new ArrayList<UnitType>();
					ArrayList<UnitType> path = findTechPath(u);
					
					//only add end goal for now
					//THIS WILL BE CHANGED LATER ON IN IMPLEMENTATION
//					path.add(u);
					
					path = examinePath(path);
					System.out.println("Path changed to: " + path.toString());
					
					//add path to production q
					productionQueue.add(path);
				}
				
				productionQueue = reduceCrossover(productionQueue);
				printProcutionQueue();
			}
			
			processQueue();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Temporary method until we move the building restrictions logic into this class
	 * @return
	 */
	public int getProdBuildingCount()
	{
		return buildingManager.productionBuildingCount();
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

					Unit building = buildingManager.getBuilding(buildingType, true);

					if(building != null)
					{
						training(item, building);
					}
				}
			}
			
			// build the thing in the front of the queue, not pop it off it is not the last thing
			if( buildPath.size() > 1)
			{
				buildPath.remove(0);
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
		ArrayList<UnitType> path = new ArrayList<UnitType>(techPaths.get(goalUnit));
		return path;
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
		//remove units from path that we already have. 
		ArrayList<UnitType> toRemove = new ArrayList<UnitType>();
		for(UnitType ut : path)
		{
			if(buildingManager.getBuilding(ut, false) != null)
			{
				// If it is the last element, don't remove it. 
				if(path.lastIndexOf(ut) == path.size()-1){
					break;
				}
				toRemove.add(ut);
			}
			else
			{
				break;
			}
		}
		
		for(UnitType ut : toRemove)
		{
			path.remove(ut);
		}
		
		return path; 
	}
	
	/**
	 * initTechPaths()
	 * creates a Hashtable of all of the tech paths needed to get to a given unit.  
	 * This runs once at the instantiation of the class and never again. 
	 * 
	 */
	public static Hashtable<UnitType, ArrayList<UnitType>> initTechPaths()
	{
		Hashtable<UnitType, ArrayList<UnitType>> techPaths = new Hashtable<UnitType, ArrayList<UnitType>>();
		// command center
		ArrayList<UnitType> cc = new ArrayList<UnitType>();
		cc.add(UnitType.Terran_Command_Center);
		techPaths.put(UnitType.Terran_Command_Center, cc);
		
		// Supply Depot
		ArrayList<UnitType> supply = new ArrayList<UnitType>();
		supply.add(UnitType.Terran_Supply_Depot);
		techPaths.put(UnitType.Terran_Supply_Depot, supply);
		
		// Refinery
		ArrayList<UnitType> refinery = new ArrayList<UnitType>();
		refinery.add(UnitType.Terran_Refinery);
		techPaths.put(UnitType.Terran_Refinery, refinery);
		
		/* Branch ONE */
		// Engineering bay
		ArrayList<UnitType> ebay = new ArrayList<UnitType>(cc);
		ebay.add(UnitType.Terran_Engineering_Bay);
		techPaths.put(UnitType.Terran_Engineering_Bay, ebay);
		
		// Missile turret 
		ArrayList<UnitType> turret = new ArrayList<UnitType>(ebay);
		turret.add(UnitType.Terran_Missile_Turret);
		techPaths.put(UnitType.Terran_Missile_Turret, turret);
		
		/* Branch TWO */
		// Barracks
		ArrayList<UnitType> racks = new ArrayList<UnitType>(cc);
		racks.add(UnitType.Terran_Barracks);
		techPaths.put(UnitType.Terran_Barracks, racks);
		
		// bunker
		ArrayList<UnitType> bunker = new ArrayList<UnitType>(racks);
		bunker.add(UnitType.Terran_Bunker);
		techPaths.put(UnitType.Terran_Bunker, bunker);
		
		// academy
		ArrayList<UnitType> academy = new ArrayList<UnitType>(racks);
		academy.add(UnitType.Terran_Academy);
		techPaths.put(UnitType.Terran_Academy, academy);
		
		// Factory
		ArrayList<UnitType> factory = new ArrayList<UnitType>(racks);
		factory.add(UnitType.Terran_Factory);
		techPaths.put(UnitType.Terran_Factory, factory);
		
		// Armory 
		ArrayList<UnitType> armory = new ArrayList<UnitType>(factory);
		armory.add(UnitType.Terran_Armory);		
		techPaths.put(UnitType.Terran_Armory, armory);
		
		// Starport	
		ArrayList<UnitType> starport = new ArrayList<UnitType>(factory);
		starport.add(UnitType.Terran_Starport);		
		techPaths.put(UnitType.Terran_Starport, starport);
		
		// Science facility
		ArrayList<UnitType> science = new ArrayList<UnitType>(starport);
		science.add(UnitType.Terran_Science_Facility);		
		techPaths.put(UnitType.Terran_Starport, starport);
		
		/* Add-ons */ 
		
		// ComSat --> Dependent, academy
		ArrayList<UnitType> comSat = new ArrayList<UnitType>(academy);
		comSat.add(UnitType.Terran_Comsat_Station);		
		techPaths.put(UnitType.Terran_Comsat_Station, comSat);
		
		// Machine Shop --> Dependent, factory
		ArrayList<UnitType> machineShop = new ArrayList<UnitType>(factory);
		machineShop.add(UnitType.Terran_Machine_Shop);		
		techPaths.put(UnitType.Terran_Machine_Shop, machineShop);
		
		// Control Tower --> Dependent, starport
		ArrayList<UnitType> tower = new ArrayList<UnitType>(starport);
		tower.add(UnitType.Terran_Control_Tower);		
		techPaths.put(UnitType.Terran_Control_Tower, tower);
		
		// Physics Lab --> Dependent, Science Facility
		ArrayList<UnitType> physics = new ArrayList<UnitType>(science);
		physics.add(UnitType.Terran_Physics_Lab);		
		techPaths.put(UnitType.Terran_Physics_Lab, physics);
		
		// Covert Ops --> Dependent, Science Facility
		ArrayList<UnitType> ops = new ArrayList<UnitType>(science);
		ops.add(UnitType.Terran_Covert_Ops);		
		techPaths.put(UnitType.Terran_Covert_Ops, ops);
		
		// nuclear Silo --> Dependent, Covert Ops
		ArrayList<UnitType> nuke = new ArrayList<UnitType>(ops);
		nuke.add(UnitType.Terran_Nuclear_Silo);		
		techPaths.put(UnitType.Terran_Nuclear_Silo, nuke);
		
		/* Non-Building Units */
		
		// For each army unit
		for(UnitType key : buildingsForUnits.keySet())
		{
//			ArrayList<UnitType> temp = new ArrayList<UnitType>(techPaths.get(buildingsForUnits.get(key)));
//			temp.add(key);
			ArrayList<UnitType> temp = buildDependecies(key, techPaths.get(buildingsForUnits.get(key)));
			techPaths.put(key, temp);
		}
		
		ArrayList<UnitType> medic = new ArrayList<UnitType>(academy);
		medic.add(UnitType.Terran_Medic);		
		techPaths.put(UnitType.Terran_Medic, medic);
		
		ArrayList<UnitType> tank = new ArrayList<UnitType>(machineShop);
		tank.add(UnitType.Terran_Siege_Tank_Tank_Mode);	
		techPaths.put(UnitType.Terran_Siege_Tank_Tank_Mode, tank);
		
		
		return techPaths;
	}
	
	/**
	 * buildDependecies
	 * given a list of prerequisite unit types, this adds the new unit to the dependent list 
	 * in a new list after the preReqs. 
	 *  
	 * @param ut - The Unit Type to be added to the list
	 * @param preReqs - The list of Prerequisite unitTypes that are needed to build ut. 
	 * @return - a new list with the new Unit Type added to the prerequisites. 
	 */
	public static ArrayList<UnitType> buildDependecies(UnitType ut, ArrayList<UnitType> preReqs)
	{
		ArrayList<UnitType> temp;
		
		// if we have prereqs
		if(preReqs != null)
		{
			temp = new ArrayList<UnitType>(preReqs);
		}
		// if not, new arrayList.
		else
		{
			temp = new ArrayList<UnitType>();
		}
		
		temp.add(ut);
		
		return temp;
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
	
	/**
	 * 
	 * @param ProdQueue
	 * @return
	 */
	public ArrayList<List<UnitType>> reduceCrossover(ArrayList<List<UnitType>> ProdQueue)
	{
		ArrayList<List<UnitType>> queue = new ArrayList<List<UnitType>>(ProdQueue);
		
//		int maxLength = 0;
//		int numLists = queue.size();
//		
//		// find the max length of the queue lists
//		for(List<UnitType> list : queue)
//		{
//			if(list.size() > maxLength)
//			{
//				maxLength = list.size();
//			}
//		}
		
		// compare starting at index 0 to max length -1 the items that are in each queue.
		// itterate along the lists
//		for(int idx = 0; idx < maxLength-1; idx++)
//		{
//			UnitType[] values = getValueAtIndex(idx, queue);
//			
//			// compare each value to each other value. 
//			// find the ones that are the same.
//			for(int list_idx = 0; list_idx < values.length; list_idx++)
//			{
//				if(values[list_idx] == null)
//				{
//					continue;
//				}
//				
//				// idx with same values as in list i . 
//				ArrayList<Integer> idxOfSameValues = new ArrayList<Integer>();
//				for(int nextList = list_idx + 1; nextList < values.length; nextList++)
//				{
//					if(values[list_idx] == values[nextList])
//					{
//						idxOfSameValues.add(nextList);
//					}
//				}
//				
//				// we should now have a list of integers
//				// this list has indecies that coorespond to lists with the same values as list i
//				// ie [[1,2,3,4], [7,2,5,6], [9,2,8]]  => the list would have [1,2] 
//				
//				// if we have any overlap
//				if(!idxOfSameValues.isEmpty())
//				{
//					// find shortest list of those list
//					int shortest = 1000;
//					
//					
//				}
//			}
//		}
		
		ArrayList<UnitType> seen = new ArrayList<UnitType>();
		for(int i = 0; i<queue.size(); i++)
		{
			for(int j = 0; j<queue.get(i).size(); j++)
			{
				UnitType ut = queue.get(i).get(j); 
				if(!seen.contains(ut))
				{
					seen.add(ut);
				}
				else{
					// already been seen, remove from this list.
					// unless it is the last element.
					if(j != queue.get(i).size()-1 ){
						queue.get(i).remove(j);
						
						// Decrement the index to relook at the same j spot. 
						j--;
					}
				}
			}
		}
		
		return queue;
	}
	
	public UnitType[] getValueAtIndex(int idx, ArrayList<List<UnitType>> queue)
	{
		UnitType[] values = new UnitType[queue.size()];
		for(int list_idx = 0; list_idx < queue.size(); list_idx++)
		{
			// if the index is "off the end" of one of the queue arrays, that entry is null
			if(idx > queue.get(list_idx).size()-1)
			{
				values[list_idx] = null; 
			}
			else{
				values[list_idx] = queue.get(list_idx).get(idx);
			}
		}
		return values;
	}
	
	/**
	 * getTechPaths()
	 * Getter method for Tech paths. 
	 * @return Hashtable of tech paths. 1 path for each unit type. 
	 */
	public Hashtable<UnitType, ArrayList<UnitType>> getTechPaths()
	{
		return techPaths;
	}
	
	/**
	 * printProductionQueue
	 * prints the production queue as it currently is. 
	 */
	public void printProcutionQueue()
	{
		if(productionQueue.size() > 0)
		{
			System.out.println("Production QUEUE");
			for(List<UnitType> list : productionQueue)
			{
				System.out.println(list.toString());
			}
		}
	}
}
