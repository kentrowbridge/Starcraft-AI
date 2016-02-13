package StarcraftAI;
import java.util.*;
import java.util.concurrent.TimeUnit;

import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class StrategyManager extends DefaultBWListener {

    private Mirror mirror = new Mirror();
    protected Game game;
    private Player self;    
    
    private int enemyArmyCount;
    private Hashtable<UnitType, Double> enemyArmyRatio;
    private Hashtable<UnitType, Integer> enemyBuildingInfo;
    private HashSet<Position> enemyArmyPosition;
    private HashSet<Position> enemyBuildingLocation;
    private long startTime;
    private long endTime;
    
    private ProductionManager productionManager;
    private MilitaryManager militaryManager;
    
    private boolean isScouting = false;
    
    private boolean hasExtendedRange = false;

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
    
    @Override
    public void onEnd(boolean isWinner)
    {
    	endTime = System.nanoTime();
    	long elapsedTime = endTime - startTime; 
    	TimeUnit.NANOSECONDS.toSeconds(elapsedTime);
    	
    	productionManager.onEnd(isWinner, elapsedTime);
    	
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
    	//Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
    	
        game = mirror.getGame();
        self = game.self();
        
        game.setLocalSpeed(10);
        
        //start a clock for the game time
        startTime = System.nanoTime();
        
        // init production manager and military manager
        try{
        	productionManager = new ProductionManager(game, self);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        militaryManager = new MilitaryManager(game, self);
        // Init variables for enemy info
        enemyArmyCount = 0;
        enemyArmyRatio = new Hashtable<UnitType, Double>();
        enemyBuildingInfo = new Hashtable<UnitType, Integer>();
        enemyArmyPosition = new HashSet<Position>();
        enemyBuildingLocation = new HashSet<Position>();
        
        isScouting = false;
        hasExtendedRange = false;
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
    	//displayGameInfo();
        
        try
        {
        	//update game info for this and subsequent classes
        	update();
        }
        catch(Exception e)
        {
        	//catches any errors we may get, Brood War does nothing to let us know
        	e.printStackTrace();
        	System.exit(0);
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
		
		//give orders to lower tier classes
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
    	
    	//if we can't find the enemy continue scouting
    	if(enemyBuildingLocation.isEmpty())
    	{
    		isScouting = false;
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
     * TODO
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
    			}
    		}
    	}
    	
    	ArrayList<Position> toRemove = new ArrayList<Position>();
    	
    	//loop over the visible enemy units that we remember
    	if(enemyBuildingLocation == null) System.out.println("Uh Oh!");
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
     * displayUnitOrders()
     * 
     * Debugging method that disiplays the units order near the unit itself and also
     * displays a green line to its destination, if it has one.
     */
    private void displayGameInfo()
    {
    	//Unit destination lines and orders
    	for(Unit myUnit : self.getUnits())
    	{
    		//display units order
	    	game.drawTextMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), myUnit.getOrder().toString());
	    	
	    	int x = myUnit.getOrderTargetPosition().getX() == 0 ? myUnit.getPosition().getX() : myUnit.getOrderTargetPosition().getX();
	    	int y = myUnit.getOrderTargetPosition().getY() == 0 ? myUnit.getPosition().getY() : myUnit.getOrderTargetPosition().getY();
	    	//draw line to unit destination
	    	game.drawLineMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), x, 
	    			y, bwapi.Color.Green);
    	}
    	
    	//Race identifier
        game.setTextSize(10);
        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());
    }

    public static void main(String[] args) 
    {
        new StrategyManager().run();
    }
}