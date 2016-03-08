package StarcraftAI;
import java.io.*;
import java.util.*;

import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class StrategyManager extends DefaultBWListener {

//	private static final int INIT_UT_VALUE = 0;
	private static final double INIT_ET_VALUE = 1;
	private static final int UTIL_INDEX = 0;
	private static final int ET_INDEX = 1;
	private static final int ARMY_COUNT_WINDOW_SIZE = 200;
	
	private static final String memoryFileName = "memory.txt";
	private static final String alphaValueFileName = "alpha_value.txt";
	private static final String epsilonValueFileName = "epsilon_value.txt";
	
	private int GameNumber = 1;
	private double Gamma = .8;
	private double Alpha = .999;
	private double Epsilon = .999;
	private double Lambda = .95;
	
	// variable for holding the previous state.
    private State PreviousState = null;
	
    private Mirror mirror = new Mirror();
    protected Game game;
    private Player self;    
    
    private int enemyArmyCount;
    private Hashtable<UnitType, Double> enemyArmyRatio;

    private Hashtable<Position, UnitType> enemyBuildingInfo;
    private ArmyPosition enemyArmyPosition;
    private int[] armyCountWindow = new int[ARMY_COUNT_WINDOW_SIZE];
    private HashSet<Position> enemyBuildingLocation;
    private HashSet<UnitType> enemyArmyInfo;
    
    private Hashtable<Position, ArmyPosition> regionCategories;
    
    private ProductionManager productionManager;
    private MilitaryManager militaryManager;
    
    private boolean isScouting = false;
    private boolean hasExtendedRange = false;
    
    // Memory<stateHashCode, UtilityValue, Eligibility Trace Value>
    private Hashtable<Integer, Double[]> Memory;
    
    private UnitType[] VALID_GOALS;
    
    private UnitType CURRENT_GOAL; 
    
    // VARIABLE FOR DE BUGGING 
    private long UpdateTime;
    private long FrameTime;

    /**
     * run()
     * 
     * Called when running our bot so that it may connect to a game
     * using the BWMirror api. 
     */
    public void run() 
    {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }
    
    /**
     * onUnitCreate()
     * 
     * Called by the game framework when a unit is done being created.  
     * Units will then be processed based on what the unit type is. 
     */
    @Override
    public void onUnitCreate(Unit unit) 
    {
//        System.out.println("New unit " + unit.getType());
//        System.out.println(productionManager);
    	
    	//assign unit to appropriate manager
        if(unit.getType().isWorker())
        {
        	if (militaryManager.hasScout())
        	{
        		productionManager.addUnit(unit);	
        	}
        	else
        	{
        		militaryManager.addUnit(unit);
        	}
        }
        else if(unit.getType().isBuilding())
        {
        	productionManager.addUnit(unit);
        }
        else if(!unit.getType().isNeutral())
        {
        	// Military Unit
        	militaryManager.addUnit(unit);
        }
        	
    }

	/**
     * onStart()
     * 
     * Effectively the constructor for the class. 
     * It is called when a game first starts and is used to initialize 
     * information needed by the AI. 
     */
    @Override
    public void onStart() 
    {
        game = mirror.getGame();
        self = game.self();
        
        // init production manager and military manager
        productionManager = new ProductionManager(game, self);
        militaryManager = new MilitaryManager(game, self);
        
        // init Possible goals
        initGoals();
        CURRENT_GOAL = null;
        
        // Init variables for enemy info
        enemyArmyCount = 0;
        enemyArmyRatio = new Hashtable<UnitType, Double>();
//        enemyBuildingInfo = new Hashtable<UnitType, Integer>();
        enemyBuildingInfo = new Hashtable<Position, UnitType>();
        enemyArmyPosition = null;
        enemyBuildingLocation = new HashSet<Position>();
        enemyArmyInfo = new HashSet<UnitType>();
        
        isScouting = false;
        hasExtendedRange = false;
        
        initMemory();
        readAlphaValue();
        readEpsilonValue();
        
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
        
        initRegionCategories();
        
//        for(int i = 0; i<VALID_GOALS.length; i++)
//        {
//        	System.out.println(VALID_GOALS[i].c_str());
//        }
        
        
        // Timing
        UpdateTime = 0L;
        FrameTime = 0L;
        
        // Set Game Speed
        game.setLocalSpeed(15);
        
    }
    
    /**
     * onEnd 
     */
    @Override
    public void onEnd(boolean isWinner)
    {
    	// create end State
    	State endState = new State();
    	endState.setHasWon(isWinner);
    	endState.setHasWon(!isWinner);
    	
    	// update Memory with the win State
    	updateMemory(endState);
    	
    	// Update alpha and epsilon
    	Alpha*= .999;
    	Epsilon *= .999;
    	
    	// save Memory
    	writeMemory();
    	// save alpha and epsilon
    	writeAlphaValue();
    	writeEpsilonValue();
    }
    
    /**
     * onFrame()
     * 
     * Gets called every time the frame changes in the game.  
     * This is called from the game BWAPI framework. 
     */
    @Override
    public void onFrame() 
    {
    	displayGameInfo();
    	
        try
        {
        	long startTime = System.currentTimeMillis();
        	//update game info for this and subsequent classes
        	update();
        	long endTime = System.currentTimeMillis();
        	FrameTime = startTime - endTime;
        }
        catch(Exception e)
        {
        	//catches any errors we may get, Brood War does nothing to let us know
        	e.printStackTrace();
        }
    }
    
    /**
     * update()
     * 
     * Runs the necessary methods to update the AI's information as well as
     * execute the strategy of the AI.    
     */
    private void update()
    {
		//update game information
		updateEnemyBuildingLocations();
		updateEnemyArmyPos();
		
		// only update every 200 frames
    	if(game.getFrameCount() % 200 == 0)
    	{
    		long startTime = System.currentTimeMillis();
    		State currentState = compressState();
    		updateMemory(currentState);
    		long endTime = System.currentTimeMillis();
    		UpdateTime = endTime - startTime;
    		
    		//give orders to lower tier classes
    		executeStrategy();
    	}
    	
//    	executeStrategy();
		
		//update lower tier classes with new information from game
    	productionManager.update();
    	militaryManager.update();
    }
    
    /**
     * executeStrategy()
     * 
     * Develops and executes the strategy that the AI will play with. 
     */
    private void executeStrategy()
    {
    	//KT - not sure how i feel about this, I would prefer to move basically all of
    	//	this logic into the production manager but im not sure how, or how well it 
    	//	would work. Since we plan on using AI for some of the building thresholds
    	//	we may not even need this since it will learn the best supply level.
    	int productionBuildings = productionManager.getProdBuildingCount();
    	int armyCount = militaryManager.getArmyCount();
    	
    	ArrayList<UnitType> productionGoal = new ArrayList<UnitType>();
    	
    	
        /////////////////////////////////////////////////////////
        ///                                                   ///
        ///                                                   ///
        ///                TD LEARNING STRAT                  ///
        ///                                                   ///
        ///                                                   ///
        /////////////////////////////////////////////////////////
    	
    	UnitType action = null; 
    	
    	// Get current State
    	State currentState = compressState();
    	
    	// Random role
    	double role = Math.random();
    	// if this is less then epsilon, make a random move
    	if(role < Epsilon)
    	{
    		int index = (int)Math.round(Math.random()*10);
//    		productionGoal.add(VALID_GOALS[index]);
    		action = VALID_GOALS[index];
    	}
    	else{
    		double maxUtil = -Integer.MAX_VALUE;
    		
    		// for each of the valid moves, find the one that gives us the greates Util
    		for(int i = 0; i < VALID_GOALS.length; i++)
    		{
    			// get the state we'd be in if we made this move
    			State expandedState = StateTransition.transition(currentState, VALID_GOALS[i].c_str());
    			
    			// get the hash code for the expanded state
    			int expandedStateKey = expandedState.hashCode();
    			
    			// if that state exists in our memory then we will compare the value to our max Util
    			// else we can't judge
    			if(Memory.get(expandedStateKey) != null)
    			{
    				// get the values for that state
    				Double[] utilAndET = Memory.get(expandedStateKey);
    				
    				// if the util is greater than the current Util it's the best we've seen so far. 
    				if(utilAndET[UTIL_INDEX] > maxUtil){
    					maxUtil = utilAndET[UTIL_INDEX];
    					action = VALID_GOALS[i];
    				}
    			}
    		}
    	}
    	
    	// if we get through the above, and action is still null
    	// select a random action to do. 
    	if(action == null){
    		int index = (int)Math.round(Math.random()*10);
    		action = VALID_GOALS[index];
    	}
    	
    	productionGoal.add(action);
    	productionManager.setGoal(productionGoal);
    	CURRENT_GOAL = action;
    	
    	
    	
    	
    	// ** Simplified workable build queue!
//    	productionGoal.add(UnitType.Terran_Marine);
//    	productionGoal.add(UnitType.Terran_Medic);
//    	productionGoal.add(UnitType.Terran_Siege_Tank_Tank_Mode);
//    	
//    	if(self.allUnitCount(UnitType.Terran_Refinery) < 1)
//    		productionGoal.add(UnitType.Terran_Refinery);
//    	
//    	if(self.allUnitCount(UnitType.Terran_SCV) < 28)
//    		productionGoal.add(UnitType.Terran_SCV);
//    	
//    	if(self.allUnitCount(UnitType.Terran_Barracks) < 2)
//    		productionGoal.add(UnitType.Terran_Barracks);
//    	
//    	if((self.supplyTotal() - self.supplyUsed() <= 6 
//    		|| self.supplyTotal() - self.supplyUsed() <= productionBuildings*3 + 1)
//    		&& self.incompleteUnitCount(UnitType.Terran_Supply_Depot) < 1){
//    		productionGoal.add(UnitType.Terran_Supply_Depot);
//    	}
    	
		
    	//grab the current resource count
//    	int minerals = self.minerals();
//    	int gas = self.gas();
//    	
//    	// If we are almost supply capped build a supply depot.
//    	// Should supply cap - supplyused < = # of production buildings * 2
//    	if((self.supplyTotal() - self.supplyUsed() <= 6 
//    			|| self.supplyTotal() - self.supplyUsed() <= productionBuildings*3 + 1)  
//    			&& self.incompleteUnitCount(UnitType.Terran_Supply_Depot) < 1
//    			&& minerals >= 100)
//    	{
////    		System.out.println("BUILD SUPPLY DEPOT!");
//			productionGoal.add(UnitType.Terran_Supply_Depot);
//			minerals -= 100;
//    	}
//    	
//    	// Upgrade Marine attack range. 
//    	if(minerals >= 150 && gas >= 150 
//    			&& self.completedUnitCount(UnitType.Terran_Academy)>=1 
//    			&& !hasExtendedRange)
//    	{
//    		
//    		for(Unit u : self.getUnits())
//    		{
//    			if(u.getType().equals(UnitType.Terran_Academy))
//    			{
//    				u.upgrade(UpgradeType.U_238_Shells);
//    				hasExtendedRange = true;
//    			}
//    		}
//    		
//    		minerals -= 150;
//    		gas -= 150;
//    	}
//    	
//    	// build Academy
//    	if(minerals >= 150 && self.allUnitCount(UnitType.Terran_Barracks)>1 
//    			&& self.allUnitCount(UnitType.Terran_Academy) < 1)
//    	{
//    		productionGoal.add(UnitType.Terran_Academy);
//    		minerals -= 150;
//    	}
//    	
//    	// build refinery 
//    	if(minerals >= 100 && self.supplyTotal() > 12 && self.allUnitCount(UnitType.Terran_Refinery) < 1
//    			&& self.allUnitCount(UnitType.Terran_Barracks) >= 1)
//    	{
////        	System.out.println("BUILD Refinery!!!");
//    		productionGoal.add(UnitType.Terran_Refinery);
//    		minerals -= 100;
//    	}
//    	
//    	// else if we don't have a barracks build a barracks. 
//        if(minerals >= 150 && self.allUnitCount(UnitType.Terran_Barracks) < 3)
//        {
////        	System.out.println("BUILD BARRACKS!!!");
//        	productionGoal.add(UnitType.Terran_Barracks);
//        	minerals -= 150;
//        }
//        
//        // else build marines
//        if(minerals >= 100 && self.allUnitCount(UnitType.Terran_Barracks)>0)
//        {
////        	System.out.println("BUILD Marine!!!");
//        	// If we have an academy and marines make up less than 75 percent of our army then build a marine
//        	if(self.completedUnitCount(UnitType.Terran_Academy) >= 1 
//        			&& militaryManager.getUnitRatio(UnitType.Terran_Marine) <= .75 )
//        	{
//	        	productionGoal.add(UnitType.Terran_Marine);
//	        	minerals -= 100;
//        	}
//        	// else if there is no academy, just build the marine
//        	else if(self.completedUnitCount(UnitType.Terran_Academy) < 1)
//        	{
//        		productionGoal.add(UnitType.Terran_Marine);
//	        	minerals -= 100;
//        	}
//        	//else don't build a marine. We have too many. 
//        }
//        
//        // else build Medics 
//        if(minerals >= 50 && gas >= 25 && self.allUnitCount(UnitType.Terran_Barracks)>0 
//        		&& self.completedUnitCount(UnitType.Terran_Academy) >= 1)
//        {
////        	System.out.println("BUILD Marine!!!");
//        	if(militaryManager.getUnitRatio(UnitType.Terran_Medic) != null 
//        			&& militaryManager.getUnitRatio(UnitType.Terran_Medic) <= .25){
//	        	productionGoal.add(UnitType.Terran_Medic);
//	        	minerals -= 50;
//	        	gas -= 25;
//        	}
//        }
//    	
//    	//if there's enough minerals, and not currently training an SCV, and we don't infringe on building a supply depot
//        //train an SCV
//    	if (minerals >= 50 && self.allUnitCount(UnitType.Terran_SCV) < 28)
//    	{
//    		//check the amount of supply available
//    		if(self.supplyTotal() - self.supplyUsed() != 6 
//    				|| (self.supplyTotal() - self.supplyUsed() == 6 
//    				&& self.incompleteUnitCount(UnitType.Terran_Supply_Depot) >= 1))
//    		{
//    			productionGoal.add(UnitType.Terran_SCV);
//                minerals -= 50;
//    		}
//    	}
//    	
//    	//Contingency to build more barracks over time. 
//    	if (minerals >= 150*3) 
//    	{
//    		productionGoal.add(UnitType.Terran_Barracks);
//        	minerals -= 150;
//    	}
//    	
//        //set goal for the production manager
//    	productionManager.setGoal(productionGoal);
    	
    	// Debugging 
//    	drawGoals(productionGoal);
		
    	//Attack if we have enough units
    	if(armyCount >= 20 && armyCount >= (productionBuildings-1)*3)
    	{
    		//pick a building to attack and order an attack
    		for(Position pos : enemyBuildingLocation)
    		{
    			militaryManager.command(Command.Attack, 1.0, pos);
    			break;
    		}
    	}
    	
    	//make sure we are scouting  	
    	if(!isScouting)
    	{
    		militaryManager.command(Command.Scout, 1.0, null);
    		isScouting = true;
    	}
    }
    
    /**
     * updateEnemyArmyPos:
     * updates the enemy Army Position based on information that is
     * known in the game. 
     */
    private void updateEnemyArmyPos()
    {
    	//army position update
//    	if(!game.enemy().getUnits().isEmpty())
//    	{
		ArmyPosition armyPos = null;
		int buildingCount = 0;
		int unitCount = 0;
    	for(Unit u : game.enemy().getUnits())
    	{
    		//ignore buildings
    		if(u.getType().isBuilding())
    		{
    			buildingCount++;
    			continue;
    		}
    		//ignore workers and supply units (overlords)
    		else if(u.getType().isWorker() || u.getType().supplyProvided() > 0)
    		{
    			continue;
    		}   		
    		unitCount++;
    		//find out which region unit is in
    		ArmyPosition uPos = regionCategories.get(BWTA.getRegion(u.getPosition()).getCenter());
    		if(armyPos == null)
    		{
    			armyPos = uPos;
    		}
    		else if(uPos == ArmyPosition.OurBase)// ourbase is top priority
    		{
    			armyPos = uPos;
    		}
    		else if(uPos == ArmyPosition.Neutral && armyPos != ArmyPosition.OurBase)// neutral is second priority
    		{
    			armyPos = uPos;
    		}
    	}
    	//only update if some non-buildings were found
    	if(buildingCount != game.enemy().getUnits().size())
    	{
    		enemyArmyPosition = armyPos;
    	}	    	
    	
    	//update army count
		int frameIdx = game.getFrameCount() % ARMY_COUNT_WINDOW_SIZE;
		armyCountWindow[frameIdx] = unitCount; // place army count for this frame
		int[] armyCountCopy = Arrays.copyOf(armyCountWindow, armyCountWindow.length);//copy to save values
		Arrays.sort(armyCountCopy);
		//get max seen in last window
		enemyArmyCount = armyCountCopy[ARMY_COUNT_WINDOW_SIZE - 1];
//    	}
    }
    
    
    /**
     * updateEnemyBuildingLocations
     * 
     * This method updates the memory of the AI using Eligibility trace
     * for TD-Learning.
     */
    private void updateEnemyBuildingLocations()
    {
    	//Add any buildings we see to list.
    	for(Unit u: game.enemy().getUnits())
    	{
    		//if this unit is a building add it to the hash
    		if(u.getType().isBuilding())
    		{
    			//check if we have it's position in memory and add it if we don't
    			if(!enemyBuildingLocation.contains(u.getPosition()))
    			{
    				enemyBuildingLocation.add(u.getPosition());
    				// add to enemy Building Info
        			enemyBuildingInfo.put(u.getPosition(), u.getType());
    			}
    			
				//track which regions have enemy buildings
				bwta.Region uRegion = BWTA.getRegion(u.getPosition());
				if(regionCategories.get(uRegion.getCenter()) != ArmyPosition.EnemyBase)
				{
					regionCategories.put(uRegion.getCenter(), ArmyPosition.EnemyBase);
				}
    		}
    	}
    	
    	ArrayList<Position> toRemove = new ArrayList<Position>();
    	
    	//loop over the visible enemy units that we remember
    	for(Position p : enemyBuildingLocation)
    	{
    		TilePosition tileCorrespondingToP = new TilePosition(p.getX()/32, p.getY()/32);
    		
    		//if visible
    		if(game.isVisible(tileCorrespondingToP))
    		{
    			//loop over the visible enemy buildings and find out if at least
    			// one of them is still at the remembered position
    			boolean buildingStillThere = false;
    			for(Unit u: game.enemy().getUnits())
    			{
    				if(u.getType().isBuilding() && u.getPosition().equals(p) && u.exists())
    				{
    					buildingStillThere = true;
    					break;
    				}
    			}
    			
    			if(!buildingStillThere)
    			{
    				toRemove.add(p);
    				break;
    			}
    		}
    	}
    	
    	//remove
    	for(Position p : toRemove)
    	{
    		enemyBuildingLocation.remove(p);
    		enemyBuildingInfo.remove(p);
    	}
    }
    
    /**
     * convertPositionToTilePosition:
     * Takes a position and turns it into a tilePosition object
     * 
     * 
     * @param pos a position object, pixel precise. 
     * @return A tilePosition object corresponding to a given position
     */
    public static TilePosition convertPositionToTilePosition(Position pos)
    {
    	TilePosition tileCorrespondingToP = new TilePosition(pos.getX()/32, pos.getY()/32);
    	return tileCorrespondingToP;
    }
    
    /**
     * convertTilePositionToPosition:
     * Takes a tile position and turns it into a position object
     * 
     * @param tilePosition a tile position object
     * @return the pixel position or the Position object corresponding to 
     * 		a given tile position
     */
    public static Position convertTilePositionToPosition(TilePosition tilePosition)
    {
    	Position position = new Position(tilePosition.getX()*32, tilePosition.getY()*32);
    	return position;
    }
    
    /**
     * displayGameInfo()
     * 
     * Debugging method that displays the units order near the unit itself and also
     * displays a green line to its destination, if it has one.
     */
    private void displayGameInfo()
    {
        //Unit destination lines and orders
    	for(Unit myUnit : self.getUnits())
    	{
    		//display units order
//	    	game.drawTextMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), myUnit.getOrder().toString());
	    	
	    	int x = myUnit.getOrderTargetPosition().getX() == 0 ? myUnit.getPosition().getX() : myUnit.getOrderTargetPosition().getX();
	    	int y = myUnit.getOrderTargetPosition().getY() == 0 ? myUnit.getPosition().getY() : myUnit.getOrderTargetPosition().getY();
	    	//draw line to unit destination
	    	
	    	game.drawLineMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), x, 
	    			y, bwapi.Color.Green);
	    	if(!myUnit.getType().isBuilding()){
	    		game.drawTextMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), "AP: " + myUnit.getOrderTargetPosition().toString());
	    	}
    	}
    	
    	for(Position p : enemyBuildingLocation)
    	{
    		game.drawCircleMap(p.getX(), p.getY(), 20, Color.Green);
    	}
    	
    	// ATTACK POSITION DEBUG CODE
        game.setTextSize(1);
//        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());
        game.drawTextScreen(10,10, "Army Count: " + militaryManager.getArmyCount());
        
        for(Position pos : enemyBuildingLocation)
		{
        	game.drawTextScreen(10, 20, "Attack Position: " + pos.toString());
			break;
		}

    	//Display region control information (OurBase, Neutral, EnemyBase)
    	for(Position r : regionCategories.keySet())
        {
    		Color c;
    		ArmyPosition category = regionCategories.get(r); 
    		if(category == ArmyPosition.OurBase)
    			c = Color.Green;
    		else if(category == ArmyPosition.EnemyBase)
    			c = Color.Red;
    		else
    			c = Color.White;
        	game.drawCircleMap(r.getX(), r.getY(), 25, c, true);
        }
    	
    	
    	// Draw goals: 
    	game.drawTextScreen(10, 30, "Production Goals: " );
    	if( CURRENT_GOAL == null)
    	{
    		game.drawTextScreen(10, 0*10 + 40, "No GOAL");
    	}
    	else{
    		game.drawTextScreen(10, 0*10 + 40, CURRENT_GOAL.c_str());
    	}    	
    	
    	//display enemy army count
    	game.drawTextScreen(500, 280, "Enemy Army Count: " + enemyArmyCount);
    	
    	// Display Frame time 
    	game.drawTextScreen(500, 290, "Frame Time: " + FrameTime + " ms");
    	// Display update time
    	game.drawTextScreen(500, 300, "Update Time: " + UpdateTime + " ms");
    }
    
    /**
     * drawGoals
     * 
     * Draws each of the goals onto the screen.
     * This is separate from Display Game info because it requires the goas to be passed in. 
     * 
     * @param goals - a list of UnitTypes that symbolize the goals of the StratManager at that point in time. 
     */
    private void drawGoals(ArrayList<UnitType> goals){
    	
    	game.drawTextScreen(10, 30, "Production Goals: " );
    	for(int i = 0; i<goals.size(); i++)
    	{
    		game.drawTextScreen(10, i*10 + 40, goals.get(i).c_str());
    	}
    }
    
    /**
     * 
     */
    private void initRegionCategories() 
    {
    	regionCategories = new Hashtable<Position, ArmyPosition>();
    	bwta.Region home = BWTA.getRegion(self.getStartLocation());
    	if(home != null)
    	{
    		regionCategories.put(home.getCenter(), ArmyPosition.OurBase);
    	}
    	for(bwta.Region r : BWTA.getRegions()) 
    	{
    		if(!regionCategories.containsKey(r.getCenter()))
    		{
    			regionCategories.put(r.getCenter(), ArmyPosition.Neutral);
    		}
    	}
    }
    
    /**
     * InitGoals
     * This initializes the subset of UnitTypes that our AI is allowed to use. 
     * These UnitTypes allow use to restrict the possible actions of the AI to building only these UnitTypes 
     * 	Both buildings and Army and SCV units. 
     * 
     */
    private void initGoals()
    {
    	UnitType[] goals = {UnitType.Terran_Marine, UnitType.Terran_Medic, UnitType.Terran_SCV, UnitType.Terran_Siege_Tank_Tank_Mode,
      			 UnitType.Terran_Vulture, UnitType.Terran_Academy, UnitType.Terran_Barracks, UnitType.Terran_Command_Center,
       			 UnitType.Terran_Factory, UnitType.Terran_Machine_Shop, UnitType.Terran_Supply_Depot};
    	VALID_GOALS = goals;
    }
    
    /////////////////////////////////////////////////////////
    ///                                                   ///
    ///                                                   ///
    ///                TD LEARNING CODE                   ///
    ///                                                   ///
    ///                                                   ///
    /////////////////////////////////////////////////////////
    
    
    /**
     * initMemory()
     * 
     * Initializes the memory of the AI. 
     * If a memory file exists, read from that. 
     * otherwise, init with a new memory.
     */
    public void initMemory(){
   		File f = new File(memoryFileName);
    	if(f.exists()){
    		Memory = readMemory();
    	}
    	else{
    		Memory = new Hashtable<Integer, Double[]>();
    	}
    }
    
    /**
     * updateMemory
     * @param currentState
     */
    public void updateMemory(State currentState){
    	int currHashCode = currentState.hashCode();
    	
    	// step 1, check if current state exists, 
    	// if not, place the current state in Memory
    	if(!Memory.containsKey(currHashCode)){
    		Double[] initValues = {0.0, INIT_ET_VALUE};
    		Memory.put(currHashCode, initValues );
    	}
    	
    	// step 2, get current state UtilValue and ET Value
    	Double[] currUtilAndET = Memory.get(currHashCode);
    	
    	// step 3, Calculate delta and update ET since last time
    	double delta = 0;
    	// if no previous state
    	if(PreviousState == null){
    		PreviousState = currentState;
    		// if no previous state then the delta value is 0;
    		delta = 0;
    		// no prior Et value to update if no previous state. 
    	}
    	else{
    		delta = getReward(PreviousState) + Gamma * currUtilAndET[UTIL_INDEX] - Memory.get(PreviousState.hashCode())[UTIL_INDEX];
    		// update the previous State's ET to 1
    		// currUtil is a reference to the one in memory. 
    		currUtilAndET[ET_INDEX] = INIT_ET_VALUE;
    	}
    	
    	// step 4, Iterate through all states and adjust their Util and ET values accordingly.
    	for(Integer key : Memory.keySet() )
    	{
    		// get states Util and ET values
    		Double[] utilAndET = Memory.get(key);
    		
    		Double stateUtil = utilAndET[UTIL_INDEX];
    		Double stateET = utilAndET[ET_INDEX];
    		
    		// update the Util Value of all states
            // equation: where s' is my current state
            // U(s) = U(s) + alpha * eligibility Trace * delta
    		utilAndET[UTIL_INDEX] = stateUtil + stateET * Alpha * delta;
    		
    		// Update ET Value
    		// Et(s) = Et(s) * Lambda * gamma
    		utilAndET[ET_INDEX] = stateET * Lambda * Gamma;
    	}
    	
    	// Last Thing, set Previous state to current State'
    	PreviousState = currentState;
    }
    
    /**
     * 
     * @return compressed current State
     */
    public State compressState(){
    	
    	// our Units <String, Integer> 
    	Hashtable<String, Integer> units = new Hashtable<String, Integer>();
    	for(Unit u : self.getUnits()){
    		String uTypeString = u.getType().c_str();
    		if(units.get(uTypeString) == null){
    			units.put(uTypeString, 1);
    		}
    		else{
    			units.put(uTypeString, units.get(uTypeString) + 1);
    		}
    	}
    	
    	// Enemy Army Position <ArmyPosition>
    	ArrayList<ArmyPosition> enemyArmyPosition = new ArrayList<ArmyPosition>();
    	
    	// Enemy Building Info <String>
    	HashSet<String> enemyBuildingInfoString = new HashSet<String>();
    	for(Position pos : enemyBuildingInfo.keySet()){
    		enemyBuildingInfoString.add(enemyBuildingInfo.get(pos).c_str());
    	}
    	
    	// EnemyArmy Info <UnitType>
    	HashSet<String> enemyArmyInfoString = new HashSet<String>();
    	for(UnitType ut : enemyArmyInfo){
    		enemyArmyInfoString.add(ut.c_str());
    	}
    	
    	// Enemy Army Count -  int
    	int EnemyArmyCount = enemyArmyCount;
    	
    	// Mineral Count - MineralAndGasValue
    	MineralAndGasValue mineralValue = MineralAndGasValue.m0_149;
    	int minerals = self.minerals();
    	if(minerals < 150){
    		mineralValue = MineralAndGasValue.m0_149;
    	}
    	else if(minerals <= 400){
    		mineralValue = MineralAndGasValue.m150_400;
    	}
    	else{
    		mineralValue = MineralAndGasValue.m401;
    	}
    		
    	
    	// Gas count - MineralAndGasValue
    	MineralAndGasValue gasValue = MineralAndGasValue.m0_149;
    	int gas = self.gas();
    	if(gas < 25){
    		gasValue = MineralAndGasValue.g0_24;
    	}
    	else if(gas < 126){
    		gasValue = MineralAndGasValue.g25_125;
    	}
    	else{
    		gasValue = MineralAndGasValue.g126;
    	}
    	
    	
    	return new State(units, enemyArmyPosition, enemyBuildingInfoString, 
    			enemyArmyInfoString, enemyArmyCount, mineralValue, gasValue);
    }
    
    /**
     * getReward
     * @param state - the state to get the reward for. 
     * @return 
     */
    public double getReward(State state){
    	if(state.isHasLost()){
    		return -10;
    	}
    	else if(state.isHasWon()){
    		return 10;
    	}
    	else{
    		return -0.01;
    	}
    	
    }
    
    /**
     * readMemory()
     * Reads out to a Hashtable
     * 
     * @return
     */
    public Hashtable<Integer, Double[]> readMemory(){
    	
    	Hashtable<Integer, Double[]> temp = new Hashtable<Integer, Double[]>(); 
    	
    	try
    	{
    		FileInputStream fis = new FileInputStream(memoryFileName);
    		ObjectInputStream ois = new ObjectInputStream(fis);
    		
    		Object obj = ois.readObject();
    		if(obj instanceof Hashtable<?, ?>)
    		{
    			temp = (Hashtable<Integer, Double[]>)obj;
    		}
    		ois.close();
    	}
    	catch(IOException ex)
    	{
    		System.out.println("IOException: " + ex.getMessage());
    	}
    	catch(ClassNotFoundException ex){
    		System.out.println("ClassNotFoundException: " + ex.getMessage());
    	}
    	return temp;
    }
    
    /**
     * writeMemory()
     * 
     * Write Memory to File.
     */
    public void writeMemory(){
    	try
    	{
    		FileOutputStream fos = new FileOutputStream(memoryFileName);
    		ObjectOutputStream oos = new ObjectOutputStream(fos);
    		
    		oos.writeObject(Memory);
    		oos.close();
    	}
    	catch(IOException ex)
    	{
    		System.out.println(ex.getMessage());
    	}
    }
    
    /**
     * readAlphaValue()
     * @return
     */
    public double readAlphaValue()
    {
    	Double result = 0.0;
    	try
    	{
    		File f = new File(alphaValueFileName);
			Scanner sc = new Scanner(f);
			result = sc.nextDouble();
			sc.close();
    	}
    	catch(FileNotFoundException ex)
    	{
    		return .999;
    	}
    	catch(Exception ex)
    	{
    		System.out.println(ex.getMessage());
    	}
    	return result;
    }
    
    /**
     * writeAlphaValue
     */
    public void writeAlphaValue()
    {
    	try
    	{
    		PrintWriter pw = new PrintWriter(alphaValueFileName);
    		pw.print(Alpha);
    		
    		pw.close();
    	}
    	catch(IOException ex)
    	{
    		System.out.println(ex.getMessage());
    	}
    }
    
    /**
     * readEpsilonValue()
     * @return
     */
    public double readEpsilonValue()
    {
    	Double result = 0.0;
    	try
    	{
    		File f = new File(epsilonValueFileName);
			Scanner sc = new Scanner(f);
			result = sc.nextDouble();
			sc.close();
    	}
    	catch(FileNotFoundException ex)
    	{
    		return .9999;
    	}
    	catch(Exception ex)
    	{
    		System.out.println(ex.getMessage());
    	}
    	return result;
    }
    
    /**
     * writeEpsilonValue()
     */
    public void writeEpsilonValue()
    {
    	try
    	{
    		PrintWriter pw = new PrintWriter(epsilonValueFileName);
    		pw.print(Epsilon);
    		
    		pw.close();
    	}
    	catch(IOException ex)
    	{
    		System.out.println(ex.getMessage());
    	}
    }
    
    /**
     * getMemory()
     * @return memory
     */
    public Hashtable<Integer, Double[]> getMemory()
    {
    	return Memory;
    }
    
    public String toStringMemory()
    {
    	StringBuilder sb = new StringBuilder();
    	sb.append('{');
    	for(int key : Memory.keySet())
    	{
    		sb.append(key);
    		sb.append('=');
    		sb.append('[');
    		Double[] values = Memory.get(key);
    		for(int i = 0; i<2; i++){
    			sb.append(values[i]);
    			if (i!=1){
    				sb.append(',');
    				sb.append(' ');
    			}
    				
    		}
    		sb.append(']');
    		sb.append(',');
    		sb.append(' ');
    	}
    	sb.append('}');
    	
    	return sb.toString();
    }
    
    
    /**
     * Main()
     * Runs the program
     * @param args
     */
    public static void main(String[] args) 
    {
        new StrategyManager().run();
    }
}