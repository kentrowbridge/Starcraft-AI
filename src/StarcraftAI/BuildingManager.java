package StarcraftAI;
import java.awt.List;
import java.util.ArrayList;

import bwapi.*;

/**
 * BuildingManager Class
 * 	Responsible for managing all the buildings under the agent’s control
 *  
 * @author Kenny Trowbridge
 *
 */
public class BuildingManager extends ProductionManager{

	private ArrayList<Unit> buildingList;
	
	public BuildingManager(){ }
	
	/**
	 * addUnit
	 * Adds a given unit the list of buildings if it does not already exist.
	 * 
	 * @param unit - unit to add to the list
	 */
	public void addUnit(Unit unit){ }
	
	/**
	 * build
	 * Builds a unit of the given type with the builder unit
	 * 
	 * @param unit - unit type to build
	 * @param builder - unit used to build
	 */
	public void build(UnitType unit, Unit builder){ }
	
	/**
	 * getPlacement()
	 * Finds the best location to place a given type of building
	 * 
	 * @param building - type of building to be placed
	 * @return TilePosition - returns the location to place the building
	 */
	private TilePosition getPlacement(UnitType building)
	{
		return null;
	}
	
	/**
	 * update()
	 * This updates the building list in order to prune dead units
	 */
	public void update(){ }
	
	/**
	 * checkBuildings()
	 * Checks the buildings list and returns a list of buildings that are 
	 * damaged or incomplete
	 * 
	 * @return - list of damaged or incomplete buildings
	 */
	public List checkBuildings()
	{
		return null;
	}
	
	/**
	 * getBuilding()
	 * Finds a building of the given building type
	 * 
	 * @param building - type of building to find
	 * @return - building
	 */
	public Unit getBuilding(UnitType building)
	{
		return null;
	}
}
