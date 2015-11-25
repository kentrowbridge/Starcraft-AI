package StarcraftAI;
import java.util.ArrayList;

import bwapi.*;

/**
 * Squad Class
 * Represents a group of units with the same assigned duty.
 * 
 * @author Casey Sigelmann
 *
 */
public class Squad {

	/**
	 * A list containing all the units that are members of this Squad.
	 */
	private ArrayList<Unit> squad;
	
	/**
	 * The type of this squad, representing its assigned duty.
	 */
	private SquadType squadType;
	
	/**
	 * Squad()
	 * Constructor for the Squad class.
	 */
	public Squad()
	{
		squad = new ArrayList<Unit>();
	}
	
	/**
	 * getSquadType
	 * @return squadType
	 */
	public SquadType getSquadType() {
		return squadType;
	}

	/**
	 * setSquadType
	 * @param squadType = the type of squad it is.
	 */
	public void setSquadType(SquadType squadType) {
		this.squadType = squadType;
	}

	/**
	 * isInCombat()
	 * Determines if this squad should be classified as 'in combat'.
	 * 
	 * @return - true if this squad is in combat, false otherwise.
	 */
	public boolean isInCombat()
	{
		// requires work! 
		return false;
	}
	
	/**
	 * isEmpty()
	 * Determines if this squad is empty.
	 * 
	 * @return true if the squad is empty, false otherwise.
	 */
	public boolean isEmpty()
	{
		return squad.isEmpty();
	}
	
	/**
	 * move()
	 * Moves all units in this squad to the given position.
	 * 
	 * @param position - the position to move to.
	 */
	public void move(Position position)
	{
		for(Unit unit: squad){
			unit.move(position);
		}
	}
	
	/**
	 * attackMove()
	 * Moves all units in this squad to the given position and attacks
	 * along the way.
	 * @param position
	 */
	public void attackMove(Position position)
	{
		for(Unit unit: squad){
			unit.attack(position);
		}
	}
	
	/**
	 * getUnits()
	 * Returns the units that are in this squad.
	 * 
	 * @return - a list of all units in this squad.
	 */
	public ArrayList<Unit> getUnits()
	{
		return squad;
	}
	
	/**
	 * setUnits()
	 * Sets the units in this squad to be the given units.
	 * 
	 * @param units - the units this squad should contain.
	 */
	public void setUnits(ArrayList<Unit> units)
	{
		squad = units;
	}
	
	/**
	 * addUnit()
	 * Adds the given unit to this squad.
	 * 
	 * @param unit - the unit to add.
	 */
	public void addUnit(Unit unit)
	{
		squad.add(unit);
	}
	
	/**
	 * removeUnit()
	 * Removes the given unit from this squad.
	 * 
	 * @param unit - the unit to remove.
	 */
	public void removeUnit(Unit unit)
	{
		squad.remove(unit);
	}
}
