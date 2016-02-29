package StarcraftAI;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import bwapi.*;

/**
 * StateTransition
 * 
 * This is a class is meant to be a stand alone class that provides static methods for 
 * 
 * 
 * @author Kenny Trowbridge
 *
 */
public class StateTransition {
	private static String[] unitsArr = {"Terran Marine","Terran Medic","Terran SCV","Terran Siege Tank Tank Mode",
						 "Terran Vulture","Terran Academy","Terran Barracks","Terran Command Center",
						 "Terran Factory","Terran Machine Shop","Terran Supply Depot"};
	private static List<String> validUnits = new ArrayList<String>(Arrays.asList(unitsArr));
	
	public static State transition(State s, String goal) 
	{
		State newState = new State(s);
		
		Hashtable<String, Integer> units = newState.getUnits();		
		int goalCount = units.get(goal) == null ? 0 : units.get(goal);  
		
		//increment unit count
		if(validUnits.contains(goal)) 
		{
			units.put(goal, goalCount + 1);
			
			//update minerals
			if(newState.getMinerals() == MineralAndGasValue.m150_400) 
			{
				newState.setMinerals(MineralAndGasValue.m0_149);
			} 
			else if(newState.getMinerals() == MineralAndGasValue.m401) 
			{
				newState.setMinerals(MineralAndGasValue.m150_400);
			} 
			//update gas
			if(newState.getGas() == MineralAndGasValue.g25_125)
			{
				newState.setGas(MineralAndGasValue.g0_24);
			}
			else if(newState.getGas() == MineralAndGasValue.g126) 
			{
				newState.setGas(MineralAndGasValue.g25_125);
			}
		}
		else {
			System.out.println("Transition goal not recognized: " + goal);
		}
				
		newState.setUnits(units);
		return newState;
	}
}
