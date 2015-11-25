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
        
        if( unit.getType().isWorker() ){
        	productionManager.addUnit(unit);
        }
        else if(unit.getType().isBuilding()){
        	productionManager.addUnit(unit);
        }
        else if(!unit.getType().isNeutral()){
        	// Military Unit
        	
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
        
        productionManager = new ProductionManager(game, self);
        militaryManager = new MilitaryManager(game, self);
        
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
    	try{
    		executeStrategy();
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}

    	productionManager.update();
    }
    
    /**
     * executeStrategy:
     * Develops and executes the strategy that the AI will play with. 
     */
    private void executeStrategy(){
    	
    	ArrayList<UnitType> productionGoal = new ArrayList<UnitType>();
		
    	// If we are almost supply capped build a supply depot.
    	if(self.supplyTotal() - self.supplyUsed() <= 3){
    		productionGoal.add(UnitType.Terran_Supply_Depot);
    	}
    	
    	//if there's enough minerals, and not currently training an SCV, train an SCV
    	else if (self.minerals() >= 50 && self.allUnitCount(UnitType.Terran_SCV) < 20) {
            productionGoal.add(UnitType.Terran_SCV);
    	}
    	
    	// else if we don't have a barracks build a barracks. 
        else if(self.minerals() >= 150 && self.allUnitCount(UnitType.Terran_Barracks) < 2){
        	productionGoal.add(UnitType.Terran_Barracks);        	
        }
        
        // else build marines
        else if(self.minerals() >= 100){
        	productionGoal.add(UnitType.Terran_Marine);
        }
        
        //set goal for the prodution manager
    	productionManager.setGoal(productionGoal);
		
    }
    
    /**
     * updateEnemyArmyPos:
     * updates the enemy Army Position based on information that is
     * known in the game. 
     */
    private void updateEnemyArmyPos(){
    	
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