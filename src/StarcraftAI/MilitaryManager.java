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
public class MilitaryManager extends StrategyManager {
	List<Unit> militaryUnits = new ArrayList<Unit>();
	//Squad[] squads = new Squad[SquadType.values().length];
	List<Squad> squads = new ArrayList<Squad>();
	ArmyManager armyManager = new ArmyManager();
	BattleManager battleManager = new BattleManager();
	
	/**
	 * ctor
	 */
	public MilitaryManager(){ }
	
	/**
	 * addUnit()
	 * Adds a unit to the militaryUnits list and adds it to a squad.
	 * If the unit already exists, it is not added again to either 
	 * to either list.
	 * 
	 * @param unit - unit to add
	 */
	public void addUnit(Unit unit){ }
	
	/**
	 * update()
	 * Examines all the military units and ensures that they 
	 * are in both the squads list and the militaryUnits list.
	 * It also prunes units that no longer exist from both lists.
	 */
	public void update(){ }

	/**
	 * command()
	 * Given a command from the StrategyManager this method will
	 * interpret and execute that command.
	 * 
	 * @param command - command from the StrategyManager
	 * @param percentCommit - percentage of units to commit to command
	 */
	public void command(Command command, Double percentCommit){ }
	
}
