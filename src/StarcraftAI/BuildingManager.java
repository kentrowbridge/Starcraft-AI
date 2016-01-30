package StarcraftAI;
import java.util.*;

import bwapi.*;
import bwta.BWTA;

/**
 *  BuildingManager Class
 * 	Responsible for managing all the buildings under the agent’s control
 *  
 * @author Kenny Trowbridge
 * @author Casey Sigelmann
 *
 */
public class BuildingManager{
	private Game game;
	private Player self;

	private ArrayList<Unit> buildingList = new ArrayList<Unit>();
	private Hashtable<UnitType, Integer> buildingMaxHealth = new Hashtable<UnitType, Integer>();

	/**
	 * c'tor
	 * @param game - a reference to the game match
	 * @param self - a reference to our player of the game
	 */
	public BuildingManager(Game game, Player self)
	{
		this.game = game;
		this.self = self;
		this.buildingList = new ArrayList<Unit>();
		initBuildingHealthHash();
	}

	/**
	 * addUnit
	 * Adds a given unit the list of buildings if it does not already exist.
	 * 
	 * @param unit - unit to add to the list
	 */
	public void addUnit(Unit unit)
	{
		buildingList.add(unit);
	}

	/**
	 * build
	 * Builds a unit of the given type with the builder unit
	 * 
	 * @param buildingType - unit type to build
	 * @param builder - unit used to build
	 */
	public void build(UnitType buildingType, Unit builder)
	{ 
		TilePosition placement = getPlacement(buildingType, builder);
		if(placement != null)
		{
			builder.build(placement, buildingType);
		}
	}

	/**
	 * getPlacement()
	 * Finds the best location to place a given type of building
	 * 
	 * @param buildingType - type of building to be placed
	 * @return TilePosition - returns the location to place the building
	 */
	private TilePosition getPlacement(UnitType buildingType, Unit builder)
	{
		// values to help determine the search radius of where to build different constructs
		int maxDist = 8;
		int changeRate = 2;
		int stopDist = 40;
		TilePosition aroundTile = self.getStartLocation();

		// build a refinery at the nearest geyser location to the starting point
		if(buildingType == UnitType.Terran_Refinery)
		{
			List<Unit> geysers = game.getGeysers();
			Unit closest = null;
			Position position = BWTA.getStartLocation(self).getPosition();
			for(Unit geyser : geysers)
			{
				if(game.canBuildHere(builder, geyser.getTilePosition(), buildingType, true))
				{
					if(closest == null || geyser.getDistance(position) < geyser.getDistance(position))
					{
						closest = geyser;
					}
				}
			}
			return closest.getTilePosition();
		}

		// search for a an empty tile position that can build a given building type
		while((maxDist < stopDist))
		{
			int minX = aroundTile.getX()-maxDist;
			int maxX = aroundTile.getX()+maxDist;
			int minY = aroundTile.getY()-maxDist;
			int maxY = aroundTile.getY()+maxDist;
			// loop through the defined area
			for(int i = minX; i <= maxX; i++)
			{
				for(int j = minY; j <= maxY; j++)
				{
					if(i < maxX && i > minX && j < maxY && j > minY)
					{
						continue;
					}
					if(game.canBuildHere(builder, new TilePosition(i,j), buildingType, true))
					{
						return new TilePosition(i,j);
					}
				}
			}
			// we didn't find a valid tile, so increase max distance
			maxDist+=changeRate;
		}
		game.printf("Unable to find suitable build position for "+buildingType.toString());
		return null;
	}

	/**
	 * update()
	 * This updates the building list in order to prune dead units
	 */
	public void update()
	{
		//examine buildings and remove dead units
		ArrayList<Unit> buildingsToRemove = new ArrayList<Unit>();
		for(Unit building : buildingList)
		{
			if (!building.exists())
			{
				buildingsToRemove.add(building);
			}
		}
		for(Unit building : buildingsToRemove)
		{
			buildingList.remove(building);
		}
	}

	/**
	 * checkBuildings()
	 * Checks the buildings list and returns a list of buildings that are 
	 * damaged ( < %50 health) or incomplete
	 * 
	 * @return - list of damaged or incomplete buildings
	 */
	public List<Unit> checkBuildings()
	{
		List<Unit> returnList = new ArrayList<Unit>();
		
		//search all buildings for incomplete and damaged buildings
		for(Unit b : buildingList)
		{
			//check if the building is incomplete and not being constructed
			if(!b.isCompleted() && b.getBuildUnit() == null)
			{
				returnList.add(b);
				continue;
			}
			
			//check if buildings health is too low
			float healthPercentage = b.getHitPoints() / (float) (buildingMaxHealth.get(b.getType()));
			if(healthPercentage <= 0.5)
			{
				returnList.add(b);
			}
		}
		
		//Display which buildings need repairs
		int margin = 20;
		for (Unit b : returnList)
		{
			game.drawTextScreen(10, margin, b.getPosition().toString());
			margin += 10;
		}	
		
		return returnList;
	}

	/**
	 * getBuilding()
	 * Finds a building of the given building type
	 * 
	 * @param building - type of building to find
	 * @param checkTraining - true if only a building that is not training should be returned
	 * @return - building
	 */
	public Unit getBuilding(UnitType buildingType, boolean checkTraining)
	{
		for(Unit building : buildingList)
		{
			if(building.getType() == buildingType)
			{
				if((!checkTraining) || (!building.isTraining()))
				{
					return building;
				}
			}
		}
		return null;
	}
	
	/**
	 * productionBuildingCount()
	 * 
	 * @return the number of unit producing buildings
	 */
	public int productionBuildingCount()
	{
		int count = 0;
		for (Unit building : buildingList)
		{
			if(building.getType().canProduce())
			{
				count++;
			}
		}
		
		return count;
	}
	
	/**
	 * initBuildingHealthHash()
	 * 
	 * Inits the max health for building hash using values found at
	 * http://starcraft.wikia.com/wiki/
	 */
	private void initBuildingHealthHash()
	{		
		this.buildingMaxHealth.put(UnitType.Terran_Academy, 600);
		this.buildingMaxHealth.put(UnitType.Terran_Armory, 750);
		this.buildingMaxHealth.put(UnitType.Terran_Barracks, 1000);
		this.buildingMaxHealth.put(UnitType.Terran_Bunker, 350);
		this.buildingMaxHealth.put(UnitType.Terran_Command_Center, 1500);
		this.buildingMaxHealth.put(UnitType.Terran_Comsat_Station, 750);
		this.buildingMaxHealth.put(UnitType.Terran_Control_Tower, 750);
		this.buildingMaxHealth.put(UnitType.Terran_Covert_Ops, 750);
		this.buildingMaxHealth.put(UnitType.Terran_Engineering_Bay, 850);
		this.buildingMaxHealth.put(UnitType.Terran_Factory, 1250);
		this.buildingMaxHealth.put(UnitType.Terran_Machine_Shop, 750);
		this.buildingMaxHealth.put(UnitType.Terran_Missile_Turret, 200);
		this.buildingMaxHealth.put(UnitType.Terran_Nuclear_Silo, 600);
		this.buildingMaxHealth.put(UnitType.Terran_Physics_Lab, 600);
		this.buildingMaxHealth.put(UnitType.Terran_Refinery, 750);
		this.buildingMaxHealth.put(UnitType.Terran_Science_Facility, 850);
		this.buildingMaxHealth.put(UnitType.Terran_Starport, 1300);
		this.buildingMaxHealth.put(UnitType.Terran_Supply_Depot, 500);
	}
}
