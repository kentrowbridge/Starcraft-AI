package StarcraftAI;
import java.util.*;
import bwapi.*;

/**
 * This class tracks and manages all of the worker units 
 * the agent owns.
 * 
 * @author Kenny Trowbridge
 *
 */
public class WorkerManager{
	
	private List<Unit> neutralUnits = new ArrayList<Unit>();
	private List<Unit> workerList = new ArrayList<Unit>();
	
	public WorkerManager(List<Unit> neutralUnits)
	{
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
		for(Unit u : workerList)
		{			
			if(u.isIdle() && u.isCompleted())
			{
				//assign a task
				u.gather(findClosestMineral(u.getPosition()));
			}
			
			//save dead units for deletion	
			if(!u.exists())
			{
				workersToRemove.add(u);
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
//			if(u.isConstructing() && u.isMoving() && (u.getBuildUnit() == null))
			if(u.getOrder().equals(Order.PlaceBuilding))
			{
				return null;
			}		
			
			//find a free worker
			if(!u.isConstructing() && u.isInterruptible() && u.isCompleted())
			{//save a valid worker
				availableWorker = u;
			}
		}
		
		return availableWorker;
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
			return null;
		//init closest to first in list
		Unit closest = null;
		
		//find closest mineral
		for(Unit neutral : this.neutralUnits)
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
}

