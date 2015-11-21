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
public class WorkerManager extends ProductionManager{
	
	private List<UnitType> workerList = new ArrayList<UnitType>();
	
	public WorkerManager(){ }
	
	/**
	 * update()
	 * This method maintains the list of worker units by pruning
	 * units that no longer exist
	 */
	public void update(){ }
	
	/**
	 * getWorker()
	 * Finds a worker unit
	 * 
	 * @return - a worker unit
	 */
	public Unit getWorker()
	{
		return null;
	}
	
	/**
	 * addUnit
	 * Adds a unit to the workerList
	 * 
	 * @param unit - unit to be added
	 */
	public void addUnit(Unit unit){ }
}
