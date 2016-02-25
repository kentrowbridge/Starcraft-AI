package unitTests;

import static org.junit.Assert.*;

import org.junit.Test;

import StarcraftAI.State;
import StarcraftAI.StateTransition;
import bwapi.*;

public class StateTransitionTest {

	@Test
	public void test1() {
		State s = new State();
		
		State newS = StateTransition.transition(s, UnitType.Terran_Marine);
		
		Integer oldMarines = s.getUnits().get(UnitType.Terran_Marine.c_str());
		Integer newMarines = newS.getUnits().get(UnitType.Terran_Marine.c_str());
		assertTrue(oldMarines + 1 == newMarines);
	}

}
