package StarcraftAI;

import java.util.Hashtable;

import bwapi.*;

public class StateTransition {
	public static State transition(State s, UnitType goal) 
	{
		State newState = new State(s);
		
		Hashtable<String, Integer> units = newState.getUnits();
		String goalString = goal.c_str();
		int goalCount = units.get(goalString) == null ? 0 : units.get(goalString);  
		
		switch(goalString) 
		{
		//units
		case "Terran_Marine":
		case "Terran_Medic":
		case "Terran_SCV":
		case "Terran_Siege_Tank_Tank_Mode":
		case "Terran_Vulture":			
		//buildings
		case "Terran_Academy":
		case "Terran_Barracks":
		case "Terran_Command_Center":
		case "Terran_Factory":
		case "Terran_Machine_Shop":
		case "Terran_Supply_Depot":
		
			units.put(goalString, goalCount + 1);
			break;			
		default:
			System.out.println("Goal not available");
			break;
		}
		
		newState.setUnits(units);
		return newState;
	}
}
