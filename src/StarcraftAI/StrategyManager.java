package StarcraftAI;
import java.util.*;

import bwapi.*;
import bwta.BWTA;
import bwta.BaseLocation;

public class StrategyManager extends DefaultBWListener {

    private Mirror mirror = new Mirror();
    protected Game game;
    private Player self;
    
    private int armyCount;
    private int scvCount;
    private Hashtable<UnitType, Double> armyRatio;
    private Hashtable<UnitType, Integer> buildingInfo;
    
    private int enemyArmyCount;
    private Hashtable<UnitType, Double> enemyArmyRatio;
    private Hashtable<UnitType, Integer> enemyBuildingInfo;
    private HashSet<Position> enemyArmyPosition;
    private HashSet<Position> enemyBuildingLocation;
    
    private ProductionManager productionManager;
    private MilitaryManager militaryManager;
    
    private boolean isScouting = false;

    /**
     * run()
     * Called when running our bot so that it may connect to a game
     * using the BWMirror api. 
     */
    public void run() {
        mirror.getModule().setEventListener(this);
        mirror.startGame();
    }
    
    /**
     * onUnitCreate:
     * Called by the game framework when a unit is done being created.  
     * Units will then be processed based on what the unit type is. 
     */
    @Override
    public void onUnitCreate(Unit unit) {
//        System.out.println("New unit " + unit.getType());
//        System.out.println(productionManager);
    	
        if( unit.getType().isWorker() ){
        	productionManager.addUnit(unit);
        	scvCount++;
        }
        else if(unit.getType().isBuilding()){
        	productionManager.addUnit(unit);
        }
        else if(!unit.getType().isNeutral()){
        	// Military Unit
        	militaryManager.addUnit(unit);
        	armyCount++;
        }
        	
    }
    
    /**
     * onStart:
     * Effectively the constructor for the class. 
     * It is called when a game first starts and is used to initialize 
     * information needed by the AI. 
     */
    @Override
    public void onStart() {
        game = mirror.getGame();
        self = game.self();
        
        // init production manager and military manager
        productionManager = new ProductionManager(game, self);
        militaryManager = new MilitaryManager(game, self);
        
        // Init variable our unit info
        armyCount = 0;
        scvCount = 0;
        armyRatio = new Hashtable<UnitType, Double>();
        buildingInfo = new Hashtable<UnitType, Integer>();
        
        // Init variables for enemy info
        enemyArmyCount = 0;
        enemyArmyRatio = new Hashtable<UnitType, Double>();
        enemyBuildingInfo = new Hashtable<UnitType, Integer>();
        enemyArmyPosition = new HashSet<Position>();
        enemyBuildingLocation = new HashSet<Position>();
        
        //Use BWTA to analyze map
        //This may take a few minutes if the map is processed first time!
        System.out.println("Analyzing map...");
        BWTA.readMap();
        BWTA.analyze();
        System.out.println("Map data ready");
    }
    
    /**
     * onFrame:
     * Gets called every time the frame changes in the game.  
     * This is called from the game BWAPI framework. 
     */
    @Override
    public void onFrame() {
        game.setTextSize(10);
        game.drawTextScreen(10, 10, "Playing as " + self.getName() + " - " + self.getRace());
        try{
        	this.update();
        }
        catch(Exception e){
        	e.printStackTrace();
        }
    }
    
    /**
     * update:
     * runs the necessary methods to update the AI's information as well as
     * execute the strategy of the AI.    
     */

    private void update(){
    	for(Unit myUnit : self.getUnits()){
	    	game.drawTextMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), myUnit.getOrder().toString());
	    	int x = myUnit.getOrderTargetPosition().getX() == 0 ? myUnit.getPosition().getX() : myUnit.getOrderTargetPosition().getX();
	    	int y = myUnit.getOrderTargetPosition().getY() == 0 ? myUnit.getPosition().getY() : myUnit.getOrderTargetPosition().getY();
	    	game.drawLineMap(myUnit.getPosition().getX(), myUnit.getPosition().getY(), x, 
	    			y, bwapi.Color.Green);
    	}
    	try{
    		updateEnemyBuildingLocations();
    		updateArmyRatio();
    		executeStrategy();
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}

    	productionManager.update();
    	militaryManager.update();
    }
    
    /**
     * executeStrategy:
     * Develops and executes the strategy that the AI will play with. 
     */
    private void executeStrategy(){
    	int productionBuildings = 0;
    	for(Unit u : self.getUnits()){
    		if(u.getType().equals(UnitType.Terran_Command_Center) || u.getType().equals(UnitType.Terran_Barracks)){
    			productionBuildings ++;
    		}
    	}
    	
    	ArrayList<UnitType> productionGoal = new ArrayList<UnitType>();
		
    	int minerals = self.minerals();
    	int gas = self.gas();
    	
    	// If we are almost supply capped build a supply depot.
    	// Should supply cap - supplyused < = # of production buildings * 2
    	if((self.supplyTotal() - self.supplyUsed() <= 6 || self.supplyTotal() - self.supplyUsed() <= productionBuildings*3 + 1)  
    			&& self.incompleteUnitCount(UnitType.Terran_Supply_Depot) < 1 && minerals >= 100){
//    		System.out.println("BUILD SUPPLY DEPOT!");
			productionGoal.add(UnitType.Terran_Supply_Depot);
			minerals -= 100;
    	}
    	
    	// build refinery 
        if(minerals >= 100 && self.supplyTotal() > 12 && self.allUnitCount(UnitType.Terran_Refinery) < 1){
//        	System.out.println("BUILD Refinery!!!");
        	productionGoal.add(UnitType.Terran_Refinery);
        	minerals -= 100;
        }
    	
    	// else if we don't have a barracks build a barracks. 
        if(minerals >= 150 && self.allUnitCount(UnitType.Terran_Barracks) < 3){
//        	System.out.println("BUILD BARRACKS!!!");
        	productionGoal.add(UnitType.Terran_Barracks);
        	minerals -= 150;
        }
        
        // build Academy
        if(minerals >= 150 && self.allUnitCount(UnitType.Terran_Barracks)>0 && self.allUnitCount(UnitType.Terran_Academy) < 1){
//        	System.out.println("BUILD Marine!!!");
        	productionGoal.add(UnitType.Terran_Academy);
        	minerals -= 150;
        }
        
        // else build marines
        if(minerals >= 100 && self.allUnitCount(UnitType.Terran_Barracks)>0){
//        	System.out.println("BUILD Marine!!!");
        	// If we have an academy and marines make up less than 75 percent of our army then build a marine
        	if(self.completedUnitCount(UnitType.Terran_Academy) >= 1 && armyRatio.get(UnitType.Terran_Marine) <= .75 ){
	        	productionGoal.add(UnitType.Terran_Marine);
	        	minerals -= 100;
        	}
        	// else if there is no academy, just build the marine
        	else if(self.completedUnitCount(UnitType.Terran_Academy) < 1){
        		productionGoal.add(UnitType.Terran_Marine);
	        	minerals -= 100;
        	}
        	//else don't build a marine. We have too many. 
        }
        
        // else build Medics 
        if(minerals >= 50 && gas >= 25 && self.allUnitCount(UnitType.Terran_Barracks)>0 && self.completedUnitCount(UnitType.Terran_Academy) >= 1){
//        	System.out.println("BUILD Marine!!!");
        	if(armyRatio.get(UnitType.Terran_Medic) != null && armyRatio.get(UnitType.Terran_Medic) <= .25){
	        	productionGoal.add(UnitType.Terran_Medic);
	        	minerals -= 50;
	        	gas -= 25;
        	}
        }
    	
    	//if there's enough minerals, and not currently training an SCV, and we don't infringe on building a supply depot
        //train an SCV
    	if (minerals >= 50 && self.allUnitCount(UnitType.Terran_SCV) < 28){
    		if(self.supplyTotal() - self.supplyUsed() != 6 ||
    			(self.supplyTotal() - self.supplyUsed() == 6 && self.incompleteUnitCount(UnitType.Terran_Supply_Depot)>=1)){
    			productionGoal.add(UnitType.Terran_SCV);
                minerals -= 50;
    		}
//    		else if(self.supplyTotal() - self.supplyUsed() != productionBuildings*3 + 1 ||
//        			(self.supplyTotal() - self.supplyUsed() != productionBuildings*3 + 1 && self.incompleteUnitCount(UnitType.Terran_Supply_Depot)>=1)){
//    			productionGoal.add(UnitType.Terran_SCV);
//                minerals -= 50;
//        	}
//    		System.out.println("BUILD SCV");
//            productionGoal.add(UnitType.Terran_SCV);
//            minerals -= 50;
    	}
    	
    	//Contingincy to build more barracks over time. 
    	if (minerals >= 150*3) {
    		productionGoal.add(UnitType.Terran_Barracks);
        	minerals -= 150;
    	}
    	
        //set goal for the prodution manager
    	productionManager.setGoal(productionGoal);
		
    	//Attack if we have enough units
    	if(armyCount >= 20)
    	{
    		for(Position pos : enemyBuildingLocation)
    		{
    			Position closePos = pos;
    			militaryManager.command(Command.Attack, 1.0, closePos);
    			break;
    		}
    	}
    	
    	// see if we should be scouting;
//    	if(armyCount > 1 && !isScouting){
//    		militaryManager.command(Command.Scout, 1.0, null);
//    		isScouting = true;
//    	}
    	
    	// scout if we we haven't seen enemy building and supply is over 30
    	if(armyCount > 1 && enemyBuildingLocation.isEmpty() && self.supplyUsed() >= 60){
    		militaryManager.command(Command.Scout, 1.0, null);
    		isScouting = true;
    	}
    	
    	if(!isScouting){
    		militaryManager.command(Command.Scout, 1.0, null);
    		isScouting = true;
    	}
    }
    
    /**
     * updateEnemyArmyPos:
     * updates the enemy Army Position based on information that is
     * known in the game. 
     */
    private void updateEnemyArmyPos(){
    	
    }
    
    
    /**
     * updateEnemyBuildingLocations
     * 
     * 
     */
    private void updateEnemyBuildingLocations(){
    	//Add any buildings we see to list.
    	for(Unit u: game.enemy().getUnits()){
    		//if this unit is a building add it to the hash
    		if(u.getType().isBuilding()){
    			//check if we have it's position in memory and add it if we don't
    			if(!enemyBuildingLocation.contains(u.getPosition())){
    				enemyBuildingLocation.add(u.getPosition());
    			}
    		}
    	}
    	
    	//loop over the visible enemy units that we remember
    	for(Position p : enemyBuildingLocation){
    		TilePosition tileCorrespondingToP = new TilePosition(p.getX()/32, p.getY()/32);
    		
    		//if visible
    		if(game.isVisible(tileCorrespondingToP)){
    			//loop over the visible enemy buildings and find out if at least
    			// one of them is still at the remembered position
    			boolean buildingStillThere = false;
    			for(Unit u: game.enemy().getUnits()){
    				if(u.getType().isBuilding() && u.getPosition() == p){
    					buildingStillThere = true;
    					break;
    				}
    			}
    			if(!buildingStillThere){
    				enemyBuildingLocation.remove(p);
    				break;
    			}
    		}
    	}
    }
    
    /**
     * updateArmyRatio()
     * 
     * This is a very simple implementation of Army Ratio just for the tournament 
     * Just cares about marines and medics. 
     */
    public void updateArmyRatio(){
    	//update marine percentage 
    	double marineCount = self.allUnitCount(UnitType.Terran_Marine);
    	double medicCount = self.allUnitCount(UnitType.Terran_Medic);
    	double total = marineCount + medicCount;
    	
    	armyRatio.put(UnitType.Terran_Marine, marineCount/total);
    	armyRatio.put(UnitType.Terran_Medic, medicCount/total);
    }
    
    /**
     * convertPositionToTilePosition:
     * Takes a position and turns it into a tilePosition object
     * 
     * 
     * @param pos a position object, pixel precise. 
     * @return A tilePosition object corresponding to a given position
     */
    private TilePosition convertPositionToTilePosition(Position pos){
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
    private Position convertTilePositionToPosition(TilePosition tilePosition){
    	Position position = new Position(tilePosition.getX()*32, tilePosition.getY()*32);
    	return position;
    }

    public static void main(String[] args) {
        new StrategyManager().run();
    }
}