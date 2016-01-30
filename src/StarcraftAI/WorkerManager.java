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
		
		for(Unit u : workerList)
		{			
			if(u.isIdle() && u.isCompleted())
			{
				//assign a task
				// protect against not finding any closest minerals. 
				// -- ie. don't pass null to u.gather(); that is a bad thing. 
				Unit closestMineral = findClosestMineral(BWTA.getStartLocation(self).getPosition());
				if(closestMineral != null)
				{
					u.gather(closestMineral);
				}
			}
			
			//save dead units for deletion	
			if(!u.exists())
			{
				workersToRemove.add(u);
			}
			
			if(u.isGatheringGas())
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
	 * Finds a worker unit
	 * 
	 * @return - a worker unit
	 */
	public Unit getWorker()
	{
		Unit availableWorker = null;
		for(Unit u : workerList)
		{			
			//make sure no workers are on there way to build at the same time
			if(u.getOrder().equals(Order.PlaceBuilding))
			{
				return null;
			}		
			
			//find a free worker
			if(!u.isConstructing() && u.isInterruptible() 
					&& u.isCompleted() && !u.isRepairing())
			{
				availableWorker = u;
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
	 * Finds the closest mineral to the given unit
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

