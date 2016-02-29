package unitTests;

import static org.junit.Assert.*;

import java.util.Hashtable;

import org.junit.Test;

import StarcraftAI.MineralAndGasValue;
import StarcraftAI.State;
import StarcraftAI.StateTransition;
import bwapi.*;

public class StateTransitionTest {

	@Test
	public void simpleUnitTest() {
		State s = new State();
		String goal = "Terran Marine";
		
		State newS = StateTransition.transition(s, goal);
		
		Integer oldMarines = s.getUnits().get(goal) == null ? 0 : s.getUnits().get(goal);
		Integer newMarines = newS.getUnits().get(goal) == null ? 0 : newS.getUnits().get(goal);
		assertTrue("Unit count updated success.", oldMarines + 1 == newMarines);
	}
	
	@Test
	public void simpleResourceTest() {
		State s = new State();
		s.setMinerals(MineralAndGasValue.m150_400);
		s.setGas(MineralAndGasValue.g25_125);
		String goal = "Terran Barracks";
		
		State newS = StateTransition.transition(s, goal);
		
		Integer oldGoalUnits = s.getUnits().get(goal) == null ? 0 : s.getUnits().get(goal);
		Integer newGoalUnits = newS.getUnits().get(goal) == null ? 0 : newS.getUnits().get(goal);
		assertTrue("Unit count updated success.",oldGoalUnits + 1 == newGoalUnits);
		assertTrue("Mineral range update success.", newS.getMinerals() == MineralAndGasValue.m0_149);
		assertTrue("Gas range update success.", newS.getGas() == MineralAndGasValue.g0_24);
	}
	
	@Test
	public void multipleTransitionsTest() {
		State s = new State();
		State newS = new State(s);
		String[] goals = {"Terran Barracks","Terran Marine","Terran Academy",
						  "Terran Medic","Terran Factory","Terran Marine"};
		
		
		for(String g : goals) {
			newS = StateTransition.transition(newS, g);
		}
		
		for(String g : goals) {			
			Integer oldGoalUnits = s.getUnits().get(g) == null ? 0 : s.getUnits().get(g);
			Integer newGoalUnits = newS.getUnits().get(g) == null ? 0 : newS.getUnits().get(g);
			System.out.println(g + ": " + oldGoalUnits + " --> " + newGoalUnits);
			assertTrue("Unit count updated success.",oldGoalUnits < newGoalUnits);
		}
	}
	
	@Test
	public void invalidGoalTest() {
		State s = new State();
		String goal = "Marine";
		
		State newS = StateTransition.transition(s, goal);
		
		Integer oldGoalUnits = s.getUnits().get(goal) == null ? 0 : s.getUnits().get(goal);
		Integer newGoalUnits = newS.getUnits().get(goal) == null ? 0 : newS.getUnits().get(goal);
		assertFalse("Unit count updated success.", oldGoalUnits + 1 == newGoalUnits);
	}
}
