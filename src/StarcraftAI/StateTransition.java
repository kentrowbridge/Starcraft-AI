package StarcraftAI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import bwapi.*;

public class StateTransition {
	private static String[] unitsArr = {"Terran_Marine","Terran_Medic","Terran_SCV","Terran_Siege_Tank_Tank_Mode",
						 "Terran_Vulture","Terran_Academy","Terran_Barracks","Terran_Command_Center",
						 "Terran_Factory","Terran_Machine_Shop","Terran_Supply_Depot"};
	private static List<String> validUnits = new ArrayList<String>(Arrays.asList(unitsArr));
	
	public static State transition(State s, String goal) 
	{
		State newState = new State(s);
		
		Hashtable<String, Integer> units = newState.getUnits();		
		int goalCount = units.get(goal) == null ? 0 : units.get(goal);  
		
		if(validUnits.contains(goal)) {
			units.put(goal, goalCount + 1);
		}
		else {
			System.out.println("Transition goal not recognized: " + goal);
		}
				
		newState.setUnits(units);
		return newState;
	}
}
