package StarcraftAI;
import java.util.*;
import bwapi.*;
import bwta.BWTA;

/**
 * This class tracks and manages all of the worker units 
 * the agent owns.
 * 
 * @author Kenny Trowbridge
 *
 */
public class WorkerManager{
	
	private Player self = null;
	private List<Unit> neutralUnits = new ArrayList<Unit>();
	private List<Unit> workerList = new ArrayList<Unit>();
	
	/**
	 * constructor
	 * @param self  Player object for bot
	 * @param neutralUnits  List of neutral units in the game (Only used to task workers to gather)
	 */
	public WorkerManager(Player self, List<Unit> neutralUnits)
	{
		this.self = self;
		this.neutralUnits = neutralUnits;
	}
	
	/**
	 * update()
	 * This method maintains the list of worker units by pruning
	 * units that no longer exist. Also assigns idle units tasks
	 */
	public void update()
	{ 
		List<Unit> workersToRemove = new ArrayList<Unit>();
		
		boolean gatheringGas = false;
		
		for(Unit worker : workerList)
		{			
			if(worker.isIdle() && worker.isCompleted())
			{
				//assign a task
				// protect against not finding any closest minerals. 
				// -- ie. don't pass null to u.gather(); that is a bad thing. 
				Unit closestMineral = findClosestMineral(BWTA.getStartLocation(self).getPosition());
				if(closestMineral != null)
				{
					worker.gather(closestMineral);
				}
			}
			
			//save dead units for deletion	
			if(!worker.exists())
			{
				workersToRemove.add(worker);
			}
			
			if(worker.isGatheringGas())
			{
				gatheringGas = true;
			}
		}
		
		if(!gatheringGas && self.completedUnitCount(UnitType.Terran_Refinery)>=1)
		{
			Unit worker = getWorker();
			if(worker != null)
			{
				worker.gather(findClosestRefinery(BWTA.getStartLocation(self).getPosition()));
			}
		}
		
		//remove dead workers
		for(Unit u : workersToRemove)
		{
			workerList.remove(u);
		}
	}

	/**
	 * getWorker()
	 * Finds an available worker unit
	 * 
	 * @return - a worker unit
	 */
	public Unit getWorker()
	{
		Unit availableWorker = null;
		for(Unit worker : workerList)
		{			
			//make sure no workers are on there way to build at the same time
			if(worker.getOrder().equals(Order.PlaceBuilding))
			{
				return null;
			}		
			
			Order order = worker.getOrder();
			//find a free worker
			if(!worker.isConstructing() && worker.isInterruptible() 
					&& worker.isCompleted() && order != Order.Repair)
			{
				availableWorker = worker;
			}
		}
		
		return availableWorker;
	}
	
	/**
	 * getSCVCount()
	 * 
	 * @return the number of SCVs controlled by the player
	 */
	public int getSCVCount()
	{
		return workerList.size();
	}
	
	/**
	 * addUnit
	 * Adds a unit to the workerList
	 * 
	 * @param unit - unit to be added
	 */
	public void addUnit(Unit unit)
	{ 
		//add only worker units
		if (unit != null && unit.getType() == UnitType.Terran_SCV)
		{
			workerList.add(unit);
		}
	}

	/**
	 * findClosestMineral()
	 * Finds the closest mineral to the given position
	 * 
	 * @param pos - position of the unit
	 */
	private Unit findClosestMineral(Position pos) 
	{
		if(pos == null)
		{
			return null;
		}
		//init closest to first in list
		Unit closest = null;
		
		//find closest mineral
		for(Unit neutral : neutralUnits)
		{
			//only check mineral fields
			if(neutral.getType() == UnitType.Resource_Mineral_Field)
			{
				if(closest == null || neutral.getDistance(pos) < closest.getDistance(pos))
				{
					closest = neutral;
				}
			}
		}
		
		return closest;
	}
	
	/**
	 * findClosestRefiner()
	 * Finds the closest refinery to the given position
	 * 
	 * @param pos
	 * @return
	 */
	private Unit findClosestRefinery(Position pos){
		if(pos == null)
		{
			return null;
		}
		//init closest to first in list
		Unit closest = null;
		
		//find closest mineral
		for(Unit unit : self.getUnits())
		{
			//only check mineral fields
			if(unit.getType() == UnitType.Terran_Refinery)
			{
				if(closest == null || unit.getDistance(pos) < closest.getDistance(pos))
				{
					closest = unit;
				}
			}
		}
		
		return closest;
	}
}

