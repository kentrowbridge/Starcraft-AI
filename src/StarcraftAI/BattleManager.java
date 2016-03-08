package StarcraftAI;
import java.util.*;

import bwapi.*;

/**
 * BattleManager
 * Manages military units that are currently in a combat situation.
 * 
 * @author Max Robinson
 * @author Alex Bowns
 *
 */
public class BattleManager{
	private Game game; 
	private Player self;
	
	//A constant to determine if a unit is far away from a position
	private static final double FARAWAY = 10.0;
	
	private Squad[] squads;
	
	/**
	 * BattleManager()
	 * Constructor for the battle Manager class. 
	 */
	public BattleManager(){}
	
	public BattleManager(Squad[] squads)
	{
		this.squads = squads;
	}
	
	/**
	 * update()
	 * updates needed information, and calls the correct methods
	 * to take any actions, should they be needed. 
	 */
	public void update()
	{
		//look at each squad that is in battle
		ArrayList<Squad> squadsInBattle = checkSquadIsEngaged();
		
		//manage the battle for every squad in combat
		for (Squad squad : squadsInBattle)
		{
			manageBattle(squad);
		}
		
	}
	
	/**
	 * checkSquadIsEngaged()
	 * 
	 * @return a list of all the squads that are currently 
	 * 		engaged in battle
	 */
	public ArrayList<Squad> checkSquadIsEngaged()
	{
		//this list will be returned, holding all of the squads that are in battle
		ArrayList<Squad> squadsInBattle = null; 
		
		//add all squads in battle to the list
		for (Squad squad : squads)
		{
			if (squad.isInCombat())
			{
				squadsInBattle.add(squad);
			}
		}
			
		return squadsInBattle;
	}
	
	/**
	 * manageBattle()
	 * Determines how a squad should act when they are in combat. 
	 * 
	 * @param squad
	 */
	public void manageBattle(Squad squad)
	{
		ArrayList<Unit> allSquadUnits = squad.getUnits();
		Position attackedUnitPosition = null; 
		
		//record the location of one of the units from the squad that is in battle 
		for (Unit unit : allSquadUnits)
		{
			if (unit.isUnderAttack())
			{
				attackedUnitPosition = unit.getPosition();
				break;
			}
		}
		
		//for all units of a squad: if it is far away from the unit under attack,
		//send it to the position of the unit under attack.
		for (Unit unit : allSquadUnits)
		{
			Position unitPosition = unit.getPosition();
			
			//the unit is too far away from the attacked unit, send it to the battle
			if (unitPosition.getApproxDistance(attackedUnitPosition) > FARAWAY)
			{
				unit.attack(attackedUnitPosition);
			}
		}
	}
}
