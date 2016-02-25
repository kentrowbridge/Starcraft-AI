package unitTests;

import static org.junit.Assert.*;

import java.util.Hashtable;

import org.junit.Test;

import StarcraftAI.State;
import StarcraftAI.StateTransition;
import bwapi.*;

public class StateTransitionTest {

	@Test
	public void simpleUnitTest() {
		State s = new State();
		String goal = "Terran_Marine";
		
		State newS = StateTransition.transition(s, goal);
		
		Integer oldMarines = s.getUnits().get(goal) == null ? 0 : s.getUnits().get(goal);
		Integer newMarines = newS.getUnits().get(goal) == null ? 0 : newS.getUnits().get(goal);
		assertTrue(oldMarines + 1 == newMarines);
	}
	
	@Test
	public void simpleBuildingTest() {
		State s = new State();
		String goal = "Terran_Barracks";
		
		State newS = StateTransition.transition(s, goal);
		
		Integer oldGoalUnits = s.getUnits().get(goal) == null ? 0 : s.getUnits().get(goal);
		Integer newGoalUnits = newS.getUnits().get(goal) == null ? 0 : newS.getUnits().get(goal);
		assertTrue(oldGoalUnits + 1 == newGoalUnits);
	}
	
	@Test
	public void multipleTransitionsTest() {
		State s = new State();
		State newS = new State(s);
		String[] goals = {"Terran_Barracks","Terran_Marine","Terran_Academy",
						  "Terran_Medic","Terran_Factory","Terran_Marine"};
		
		
		for(String g : goals) {
			newS = StateTransition.transition(newS, g);
		}
		
		for(String g : goals) {			
			Integer oldGoalUnits = s.getUnits().get(g) == null ? 0 : s.getUnits().get(g);
			Integer newGoalUnits = newS.getUnits().get(g) == null ? 0 : newS.getUnits().get(g);
			System.out.println(g + ": " + oldGoalUnits + " --> " + newGoalUnits);
			assertTrue(oldGoalUnits < newGoalUnits);
		}
	}
	
	@Test
	public void invalidGoalTest() {
		State s = new State();
		String goal = "Marine";
		
		State newS = StateTransition.transition(s, goal);
		
		Integer oldGoalUnits = s.getUnits().get(goal) == null ? 0 : s.getUnits().get(goal);
		Integer newGoalUnits = newS.getUnits().get(goal) == null ? 0 : newS.getUnits().get(goal);
		assertFalse(oldGoalUnits + 1 == newGoalUnits);
	}
}
