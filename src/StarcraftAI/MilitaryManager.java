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
	private int armyCount;
    private Hashtable<UnitType, Double> armyRatio;

	/**
	 * MilitaryManager()
	 * Constructor for the MilitaryManager class.
	 */
	public MilitaryManager(Game game, Player self)
	{
		this.game = game;
		this.self = self;
		
		militaryUnits = new ArrayList<Unit>();
		squads = new HashMap<SquadType, Squad>();
		armyCount = 0;
		armyRatio = new Hashtable<UnitType, Double>();
		
		initSquads();
		
		armyManager = new ArmyManager(squads, self, game);
		battleManager = new BattleManager();
	}
	
	/**
	 * initSquads()
	 * Initialize all Terran squads.
	 */
	public void initSquads()
	{
		for(SquadType type : SquadType.values())
		{
			squads.put(type, new Squad(type));
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
	public void addUnit(Unit unit)
	{
		// put unit in a squad. Default is Offense. 
		if(squads.get(SquadType.Scout).isEmpty())
		{
			//add only the first unit to the scout squad
			squads.get(SquadType.Scout).addUnit(unit);
		}
		else
		{
			//default, add to offense
			squads.get(SquadType.Offense).addUnit(unit);
		}
	}
	
	/**
	 * update()
	 * Examines all the military units and ensures that they 
	 * are in both the squads list and the militaryUnits list.
	 * It also prunes units that no longer exist from both lists.
	 */
	public void update()
	{
		try
		{
			updateArmyRatio();
			updateArmyCount();
		}
		catch(Exception e)
		{
			e.printStackTrace();
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
		switch(command)
		{
			case Attack:
				armyManager.engage(position);
				break;
			case Defend:
				armyManager.defend();
				break;
			case Scout:
				for(Unit u : self.getUnits())
				{
					if(u.getType().equals(UnitType.Terran_SCV))
					{
						ArrayList<Unit> units = new ArrayList<Unit>();
						units.add(u);
						squads.get(SquadType.Scout).setUnits(units);
					}
				}
				
				armyManager.scout();
				break;
		}
	}
    
    /**
     * updateArmyCount()
     * 
     * Update the number of military units the bot controls
     */
    private void updateArmyCount()
    {
    	armyCount = self.completedUnitCount(UnitType.Terran_Marine) + self.completedUnitCount(UnitType.Terran_Medic);
    }
    
    /**
     * updateArmyRatio()
     * 
     * This is a very simple implementation of Army Ratio just for the tournament 
     * Just cares about marines and medics. 
     */
    public void updateArmyRatio()
    {
    	//update marine percentage 
    	double marineCount = self.allUnitCount(UnitType.Terran_Marine);
    	double medicCount = self.allUnitCount(UnitType.Terran_Medic);
    	double total = marineCount + medicCount;
    	
    	armyRatio.put(UnitType.Terran_Marine, marineCount/total);
    	armyRatio.put(UnitType.Terran_Medic, medicCount/total);
    }
    
    public int getArmyCount()
    {
    	return armyCount;
    }
    
    public Double getUnitRatio(UnitType type)
    {
    	return armyRatio.get(type);
    }
}
