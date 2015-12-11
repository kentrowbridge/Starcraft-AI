package StarcraftAI;
import java.util.*;
import bwapi.*;
/**
 * MilitaryManager
 * Maintains and manages all military units under the
 * agent's control
 * 
 * @author Kenny Trowbridge
 *
 */

public class MilitaryManager{
	private Game game;
	private Player self;
	
	protected List<Unit> militaryUnits;
	protected Squad[] squads;
	private ArmyManager armyManager;
	private BattleManager battleManager;
	private boolean doScout = false;

	/**
	 * ctor
	 */
	public MilitaryManager(Game game, Player self){
		this.game = game;
		this.self = self;
		
		militaryUnits = new ArrayList<Unit>();
		squads = new Squad[SquadType.values().length];
		
		initSquads();
		
		
		armyManager = new ArmyManager(squads, self);
		battleManager = new BattleManager();
	}
	
	/**
	 * initSquads()
	 * Initialize all terran squads.
	 */
	public void initSquads(){
		for(int i = 0; i < squads.length; i++){
			squads[i] = new Squad(SquadType.values()[i]);
		}
	}
	
	/**
	 * addUnit()
	 * Adds a unit to the militaryUnits list and adds it to a squad.
	 * If the unit already exists, it is not added again to either 
	 * to either list.
	 * 
	 * @param unit - unit to add
	 */
	public void addUnit(Unit unit){
		// put unit in a squad. Default is Offense. 
		for(Squad squad: squads){
			if(squad.getSquadType() == SquadType.Offense){
				squad.addUnit(unit);
			}
		}
	}
	
	/**
	 * update()
	 * Examines all the military units and ensures that they 
	 * are in both the squads list and the militaryUnits list.
	 * It also prunes units that no longer exist from both lists.
	 */
	public void update(){
		if (doScout){
			armyManager.scout();
		}
	}

	/**
	 * command()
	 * Given a command from the StrategyManager this method will
	 * interpret and execute that command.
	 * 
	 * @param command - command from the StrategyManager
	 * @param percentCommit - percentage of units to commit to command
	 * @param position - the position of the commanded
	 */
	public void command(Command command, Double percentCommit, Position position)
	{
		switch(command){
			case Attack:
				armyManager.engage(position);
				break;
			case Defend:
				armyManager.defend();
				break;
			case Scout:
				this.doScout = true; 
				armyManager.getBuildingLocations();
				break;
		}
	}
	
}
