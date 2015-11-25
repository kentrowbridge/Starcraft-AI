package StarcraftAI;
import java.util.List;

import bwapi.Position;
import bwapi.TilePosition;
import bwta.BWTA;
import bwta.BaseLocation;

/**
 * ArmyManager Class
 * 	This class is responsible for handling all direct orders to
 * 	military units when they are not in combat situations.
 * 
 * @author Casey Sigelmann
 *
 */
public class ArmyManager{
	
	private Squad[] squads;
	
	/**
	 * ctor
	 * default constructor
	 * 
	 */
	public ArmyManager(){
		
	}
	
	/**
	 * ctor
	 * 
	 */
	public ArmyManager(Squad[] squads){
		this.squads = squads;
	}
	
	/**
	 * setSquads
	 * given a list of squads, set those squads as the squads 
	 * for this class to act on. 
	 * 
	 * @param squads - a list of squads 
	 */
	public void setSquads(Squad[] squads){
		this.squads = squads;
	}
	
	/**
	 * defend()
	 * Positions the defend squad in a defensive position around our base.
	 */
	public void defend()
	{
		
	}
	
	/**
	 * engage()
	 * Makes the offensive squad attack the given location.
	 * 
	 * @param position - the location to attack
	 */
	public void engage(Position position)
	{
		for(Squad squad : squads){
			if(squad.getSquadType() == SquadType.Offense){
				squad.attackMove(position);
				break;
			}
		}
		
	}
	
	/**
	 * scout()
	 * Moves units in the scout squad to unexplored locations.
	 */
	public void scout()
	{
		for(Squad squad : squads){
			if(squad.getSquadType() == SquadType.Offense){
				List<BaseLocation> baseLocations = BWTA.getBaseLocations();
				for(BaseLocation base : baseLocations){
		    		TilePosition baseToP = new TilePosition(base.getX()/32, base.getY()/32);
    				squad.move(base.getPosition());
    				/*// For queueing moves...
    				 * squad.addDestination(base.getPosition());
    				 */
		    	}
			}
		}
		/*// For queueing moves...
		 * squad.addDestination(base.getPosition());
		 */
	}
	
}
