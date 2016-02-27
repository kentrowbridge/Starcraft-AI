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
	
	private static final String memoryFileName = "memory.txt";
	private static final String alphaValueFileName = "alpha_value.txt";
	private static final String epsilonValueFileName = "epsilon_value.txt";
	
	private int GameNumber = 1;
	private double Gamma = .8;
	private double Alpha = .999;
	private double Epsilon = .9999;
	private double Lambda = .95;
	
	// variable for holding the previous state.
    private State PreviousState = null;
	
    private Mirror mirror = new Mirror();
    protected Game game;
    private Player self;    
    
    private int enemyArmyCount;
    private Hashtable<UnitType, Double> enemyArmyRatio;
    private Hashtable<Position, UnitType> enemyBuildingInfo;
    private HashSet<Position> enemyArmyPosition;
    private HashSet<Position> enemyBuildingLocation;
    private HashSet<UnitType> enemyArmyInfo;
    
    private ProductionManager productionManager;
    private MilitaryManager militaryManager;
    
    private boolean isScouting = false;
    private boolean hasExtendedRange = false;
    
    // Memory<stateHashCode, UtilityValue, Eligibility Trace Value>
    private Hashtable<Integer, Double[]> Memory;
    

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
        
        // Init variables for enemy info
        enemyArmyCount = 0;
        enemyArmyRatio = new Hashtable<UnitType, Double>();
//        enemyBuildingInfo = new Hashtable<UnitType, Integer>();
        enemyBuildingInfo = new Hashtable<Position, UnitType>();
        enemyArmyPosition = new HashSet<Position>();
        enemyBuildingLocation = new HashSet<Position>();
        enemyArmyInfo = new HashSet<UnitType>();
        
        isScouting = false;
        hasExtendedRange = false;
        
        initMemory();
        
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
    }
    
    @Override
    public void onEnd(boolean isWinner)
    {
    	writeMemory();
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
        	//update game info for this and subsequent classes
        	update();
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
		
		// only update every 200 frames
    	if(game.getFrameCount() % 200 == 0)
    	{
    		State currentState = compressState();
    		updateMemory(currentState);
    		//give orders to lower tier classes
//    		executeStrategy();
    	}
    	
    	executeStrategy();
		
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
    	int minerals = self.minerals();
    	int gas = self.gas();
    	
    	// If we are almost supply capped build a supply depot.
    	// Should supply cap - supplyused < = # of production buildings * 2
    	if((self.supplyTotal() - self.supplyUsed() <= 6 
    			|| self.supplyTotal() - self.supplyUsed() <= productionBuildings*3 + 1)  
    			&& self.incompleteUnitCount(UnitType.Terran_Supply_Depot) < 1
    			&& minerals >= 100)
    	{
//    		System.out.println("BUILD SUPPLY DEPOT!");
			productionGoal.add(UnitType.Terran_Supply_Depot);
			minerals -= 100;
    	}
    	
    	// Upgrade Marine attack range. 
    	if(minerals >= 150 && gas >= 150 
    			&& self.completedUnitCount(UnitType.Terran_Academy)>=1 
    			&& !hasExtendedRange)
    	{
    		
    		for(Unit u : self.getUnits())
    		{
    			if(u.getType().equals(UnitType.Terran_Academy))
    			{
    				u.upgrade(UpgradeType.U_238_Shells);
    				hasExtendedRange = true;
    			}
    		}
    		
    		minerals -= 150;
    		gas -= 150;
    	}
    	
    	// build Academy
    	if(minerals >= 150 && self.allUnitCount(UnitType.Terran_Barracks)>1 
    			&& self.allUnitCount(UnitType.Terran_Academy) < 1)
    	{
    		productionGoal.add(UnitType.Terran_Academy);
    		minerals -= 150;
    	}
    	
    	// build refinery 
    	if(minerals >= 100 && self.supplyTotal() > 12 && self.allUnitCount(UnitType.Terran_Refinery) < 1
    			&& self.allUnitCount(UnitType.Terran_Barracks) >= 1)
    	{
//        	System.out.println("BUILD Refinery!!!");
    		productionGoal.add(UnitType.Terran_Refinery);
    		minerals -= 100;
    	}
    	
    	// else if we don't have a barracks build a barracks. 
        if(minerals >= 150 && self.allUnitCount(UnitType.Terran_Barracks) < 3)
        {
//        	System.out.println("BUILD BARRACKS!!!");
        	productionGoal.add(UnitType.Terran_Barracks);
        	minerals -= 150;
        }
        
        // else build marines
        if(minerals >= 100 && self.allUnitCount(UnitType.Terran_Barracks)>0)
        {
//        	System.out.println("BUILD Marine!!!");
        	// If we have an academy and marines make up less than 75 percent of our army then build a marine
        	if(self.completedUnitCount(UnitType.Terran_Academy) >= 1 
        			&& militaryManager.getUnitRatio(UnitType.Terran_Marine) <= .75 )
        	{
	        	productionGoal.add(UnitType.Terran_Marine);
	        	minerals -= 100;
        	}
        	// else if there is no academy, just build the marine
        	else if(self.completedUnitCount(UnitType.Terran_Academy) < 1)
        	{
        		productionGoal.add(UnitType.Terran_Marine);
	        	minerals -= 100;
        	}
        	//else don't build a marine. We have too many. 
        }
        
        // else build Medics 
        if(minerals >= 50 && gas >= 25 && self.allUnitCount(UnitType.Terran_Barracks)>0 
        		&& self.completedUnitCount(UnitType.Terran_Academy) >= 1)
        {
//        	System.out.println("BUILD Marine!!!");
        	if(militaryManager.getUnitRatio(UnitType.Terran_Medic) != null 
        			&& militaryManager.getUnitRatio(UnitType.Terran_Medic) <= .25){
	        	productionGoal.add(UnitType.Terran_Medic);
	        	minerals -= 50;
	        	gas -= 25;
        	}
        }
    	
    	//if there's enough minerals, and not currently training an SCV, and we don't infringe on building a supply depot
        //train an SCV
    	if (minerals >= 50 && self.allUnitCount(UnitType.Terran_SCV) < 28)
    	{
    		//check the amount of supply available
    		if(self.supplyTotal() - self.supplyUsed() != 6 
    				|| (self.supplyTotal() - self.supplyUsed() == 6 
    				&& self.incompleteUnitCount(UnitType.Terran_Supply_Depot) >= 1))
    		{
    			productionGoal.add(UnitType.Terran_SCV);
                minerals -= 50;
    		}
//    		else if(self.supplyTotal() - self.supplyUsed() != productionBuildings*3 + 1 
//    				|| (self.supplyTotal() - self.supplyUsed() != productionBuildings*3 + 1 
//    				&& self.incompleteUnitCount(UnitType.Terran_Supply_Depot)>=1))
//    		{
//    			productionGoal.add(UnitType.Terran_SCV);
//                minerals -= 50;
//        	}
//    		System.out.println("BUILD SCV");
//          productionGoal.add(UnitType.Terran_SCV);
//          minerals -= 50;
    	}
    	
    	//Contingency to build more barracks over time. 
    	if (minerals >= 150*3) 
    	{
    		productionGoal.add(UnitType.Terran_Barracks);
        	minerals -= 150;
    	}
    	
        //set goal for the production manager
    	productionManager.setGoal(productionGoal);
		
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
    				break;//TODO check if this is necessary
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
    private TilePosition convertPositionToTilePosition(Position pos)
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
    private Position convertTilePositionToPosition(TilePosition tilePosition)
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
	    	
	    	game.drawTextMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), "Attack Position: " + myUnit.getOrderTargetPosition().toString());
    	}
    	
    	for(Position p : enemyBuildingLocation)
    	{
    		game.drawCircleMap(p.getX(), p.getY(), 20, Color.Green);
    	}
    	
    	
    	//Race identifier
        game.setTextSize(1);
//        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());
        game.drawTextScreen(10,10, "Army Count: " + militaryManager.getArmyCount());
        
        for(Position pos : enemyBuildingLocation)
		{
        	game.drawTextScreen(10, 20, "Attack Position: " + pos.toString());
			break;
		}
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
    public int getReward(State state){
    	return 0;
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
    	catch(Exception ex)
    	{
    		
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