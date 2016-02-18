package unitTests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.HashSet;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import bwapi.*;
import StarcraftAI.*;

public class StateTest {
	private ArrayList<Unit> Units;
	private ArrayList<ArmyPosition> EnemyArmyPosition;
	private HashSet<UnitType> EnemyBuildingInfo;
	private HashSet<UnitType> EnemyArmyInfo;
	private int EnemyArmyCount;
	
	
	///// ===== Set up, Tear Down ==== //////
	
	@Before
	public void setUp() throws Exception {
		
		Units = new ArrayList<Unit>();
		EnemyArmyPosition = new ArrayList<ArmyPosition>();
		EnemyBuildingInfo = new HashSet<UnitType>();
		EnemyArmyInfo = new HashSet<UnitType>();
		EnemyArmyCount = 0;		
		
		EnemyArmyPosition.add(ArmyPosition.OurBase);
		EnemyBuildingInfo.add(UnitType.Terran_SCV);
		EnemyArmyInfo.add(UnitType.Zerg_Zergling);
		EnemyArmyCount = 10;
	}

	@After
	public void tearDown() throws Exception {
	}

	
	///// ====== Tests ====  ////// 
	
	@Test
	public void hashCodeReturn0Test() {
		State state = new State();
		System.out.println(state.toString());
		assertEquals(state.hashCode(), 28630113);
	}
	
	@Test
	public void hashCodeReturnEquivalenceTest() {
		State state = new State(Units, EnemyArmyPosition, EnemyBuildingInfo, EnemyArmyInfo, EnemyArmyCount);
		System.out.println(state.toString());
		assertEquals(state.hashCode(), 861800028);
	}
	
	@Test
	public void toStringDebugTest() {
		State state = new State(Units, EnemyArmyPosition, EnemyBuildingInfo, EnemyArmyInfo, EnemyArmyCount);
		System.out.println(state.toString(true));
		assertEquals(state.toString(true), this.toString());
	}
	
	@Test
	public void testMemoryInitEmpty() {
		
		StrategyManager strat = new StrategyManager();
		strat.initMemory();
		System.out.println(strat.getMemory().toString());
		
		assertEquals(strat.getMemory().toString(), "{}");
	}

	
	
	////// ==== Helper methods =====  //////
	@Override
	public String toString() {
		return "State [Units=" + Units.toString() + ", EnemyArmyPosition=" + EnemyArmyPosition.toString() + ", EnemyBuildingInfo="
				+ EnemyBuildingInfo.toString() + ", EnemyArmyInfo=" + EnemyArmyInfo.toString() + ", EnemyArmyCount=" + EnemyArmyCount + "]";
	}

}
