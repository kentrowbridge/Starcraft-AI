package StarcraftAI;
import java.util.*;

import bwapi.*;
import bwta.*;

/**
 *  BuildingManager Class
 * 	Responsible for managing all the buildings under the agent’s control
 *  
 * @author Kenny Trowbridge
 * @author Casey Sigelmann
 * @author Alex Bowns
 *
 */
public class BuildingManager{
	private Game game;
	private Player self;
	
	private final boolean IS_TRAINING = true;
	private final int POPULATION_SIZE = 20;
	private final int GENE_SIZE = 2000;
	
	private BWTA bwta;

	// hash table where key is "map-name" concatenated with the starting base coordinates,
	// value is the population of genes
	private Hashtable<String, Population> populations;
	private Population population;
	private Gene gene;
	
	private ArrayList<Unit> buildingList;
	
	/**
	 * c'tor
	 * @param game - a reference to the game match
	 * @param self - a reference to our player of the game
	 */
	public BuildingManager(Game game, Player self)
	{
		this.game = game;
		this.self = self;
		this.bwta = new BWTA();
		this.populations = new Hashtable<String, Population>();
		this.buildingList = new ArrayList<Unit>();
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
		
		
		return null;
		
//		// values to help determine the search radius of where to build different constructs
//		int maxDist = 8;
//		int changeRate = 2;
//		int stopDist = 40;
//		TilePosition aroundTile = self.getStartLocation();
//
//		// build a refinery at the nearest geyser location to the starting point
//		if(buildingType == UnitType.Terran_Refinery)
//		{
//			List<Unit> geysers = game.getGeysers();
//			Unit closest = null;
//			Position position = BWTA.getStartLocation(self).getPosition();
//			for(Unit geyser : geysers)
//			{
//				if(game.canBuildHere(builder, geyser.getTilePosition(), buildingType, true))
//				{
//					if(closest == null || geyser.getDistance(position) < geyser.getDistance(position))
//					{
//						closest = geyser;
//					}
//				}
//			}
//			return closest.getTilePosition();
//		}
//
//		// search for a an empty tile position that can build a given building type
//		while((maxDist < stopDist))
//		{
//			int minX = aroundTile.getX()-maxDist;
//			int maxX = aroundTile.getX()+maxDist;
//			int minY = aroundTile.getY()-maxDist;
//			int maxY = aroundTile.getY()+maxDist;
//			// loop through the defined area
//			for(int i = minX; i <= maxX; i++)
//			{
//				for(int j = minY; j <= maxY; j++)
//				{
//					if(i < maxX && i > minX && j < maxY && j > minY)
//					{
//						continue;
//					}
//					if(game.canBuildHere(builder, new TilePosition(i,j), buildingType, true))
//					{
//						return new TilePosition(i,j);
//					}
//				}
//			}
//			// we didn't find a valid tile, so increase max distance
//			maxDist+=changeRate;
//		}
//		game.printf("Unable to find suitable build position for "+buildingType.toString());
//		return null;
	}

	/**
	 * update()
	 * This updates the building list in order to prune dead units
	 */
	public void update()
	{
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
	
	/*
	 * productionBuildingCount()
	 * counts the number of team buildings that are either barracks or command centers
	 * 
	 * @return the total count
	 */
	public int productionBuildingCount()
	{
		int count = 0;
		for (Unit building : buildingList)
		{
			if(building.getType() == UnitType.Terran_Barracks 
					|| building.getType() == UnitType.Terran_Command_Center)
			{
				count++;
			}
		}
		
		return count;
	}
	
	/*
	 * mateGenes() 
	 * Mate two genes and return two children
	 * 
	 * @param gene1 - the first parent gene
	 * @param gene2 - the second parent gene
	 * @return children - two child genes of the parents
	 */
	public Gene[] mateGenes(Gene gene1, Gene gene2)
	{
		Gene[] children = new Gene[2];
		
		// split the genes at random index, combine opposite halves
		int idx = (int)(Math.random() * GENE_SIZE); 
		ArrayList<Integer> kid1 = new ArrayList<Integer>();
		ArrayList<Integer> kid2 = new ArrayList<Integer>();
		
		kid1.addAll(gene1.getRange(0, idx));
		kid1.addAll(gene2.getRange(idx, GENE_SIZE));
		kid2.addAll(gene2.getRange(0, idx));
		kid2.addAll(gene1.getRange(idx, GENE_SIZE));
				
		children[0].setListValues(kid1);
		children[1].setListValues(kid2);
		
		// possibly mutate an allele of either gene
		children[0].mutateAllele();
		children[1].mutateAllele();
		
		return children;
	}
	
	
	
	/**
	 * selectGene()
	 * Select the specific gene per map. 
	 * 
	 * @return The gene that we should use for this game
	 */
	private ArrayList selectGene()
	{
		if(IS_TRAINING)
		{
			String key = getMapAndCoords();
			if(populations.containsKey(key))
			{
				population = populations.get(key);
			}
			else
			{
				population = new Population();
				populations.put(key, population);
			}
			
			
		}
		else
		{
			System.out.println("Hardcoded final genes not yet implemented");
		}
		
		//bwta.Region baseRegion = bwta.getRegion(base); 
		
		//ArrayList<TilePosition> tilePositions = new ArrayList<TilePosition>();
		
		
		return null; 
	}

	/**
	 * getMapAndCoords()
	 * Gets the map name and coordinates of our base and concatenates them together.
	 * 
	 * @return String - the map name and coordinates
	 */
	private String getMapAndCoords()
	{
		TilePosition base = self.getStartLocation();
		String coords = "(" + base.getX() + "," + base.getY() + ")";
		return game.mapFileName() + "_" + coords;
	}
}
