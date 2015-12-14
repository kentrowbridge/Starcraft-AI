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
	protected HashMap<SquadType, Squad> squads;
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
		squads = new HashMap<SquadType, Squad>();
		
		initSquads();
		
		
		armyManager = new ArmyManager(squads, self, game);
		battleManager = new BattleManager();
	}
	
	/**
	 * initSquads()
	 * Initialize all terran squads.
	 */
	public void initSquads(){
		for(SquadType type : SquadType.values())
		{
			squads.put(type, new Squad(type));
		}
		
//		for(int i = 0; i < squads.length; i++){
//			squads[i] = new Squad(SquadType.values()[i]);
//		}
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
		if(squads.get(SquadType.Scout).isEmpty())
		{//add only the first unit to the scout squad
			squads.get(SquadType.Scout).addUnit(unit);
		}
		else
		{//default, add to offense
			squads.get(SquadType.Offense).addUnit(unit);
		}
		
//		for(Squad squad: squads){
//			if(squad.getSquadType() == SquadType.Scout && squad.isEmpty())
//			{//add only one unit to scout squad
//				squad.addUnit(unit);
//				System.out.println("added unit to scout");
//			}
//			else if (squad.getSquadType() == SquadType.Offense){
//				squad.addUnit(unit);
//			}
//		}
	}
	
	/**
	 * update()
	 * Examines all the military units and ensures that they 
	 * are in both the squads list and the militaryUnits list.
	 * It also prunes units that no longer exist from both lists.
	 */
	public void update(){
		
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
		System.out.println("Military Manager Command: " + command);
		switch(command){
			case Attack:
				armyManager.engage(position);
				break;
			case Defend:
				armyManager.defend();
				break;
			case Scout:
				armyManager.scout();
//				this.doScout = true; 
//				armyManager.getBuildingLocations();
				break;
		}
	}
	
}
