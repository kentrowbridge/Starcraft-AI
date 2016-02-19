package unitTests;

import static org.junit.Assert.*;

import java.io.File;
import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import bwapi.*;
import StarcraftAI.*;

public class StateTest {
	private static final String memoryFileName = "memory.txt";
	private static final String alphaValueFileName = "alpha_value.txt";
	private static final String epsilonValueFileName = "epsilon_value.txt";
	
	private ArrayList<Unit> Units;
	private ArrayList<ArmyPosition> EnemyArmyPosition;
	private HashSet<UnitType> EnemyBuildingInfo;
	private HashSet<UnitType> EnemyArmyInfo;
	private int EnemyArmyCount;
	
	
	
	///// ===== Set up, Tear Down ==== //////
	
	@Before
	public void setUp() throws Exception 
	{
		
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
	public void tearDown() throws Exception 
	{
	}

	
	///// ====== Tests ====  ////// 
	
	@Test
	public void hashCodeReturn0Test() 
	{
		State state = new State();
		System.out.println(state.toString());
		assertEquals(state.hashCode(), 28630113);
	}
	
	@Test
	public void hashCodeReturnEquivalenceTest() 
	{
		State state = new State(Units, EnemyArmyPosition, EnemyBuildingInfo, EnemyArmyInfo, EnemyArmyCount);
		System.out.println(state.toString());
		assertEquals(state.hashCode(), 861800028);
	}
	
	@Test
	public void toStringDebugTest() 
	{
		State state = new State(Units, EnemyArmyPosition, EnemyBuildingInfo, EnemyArmyInfo, EnemyArmyCount);
		System.out.println(state.toString(true));
		assertEquals(state.toString(true), this.toString());
	}
	
	@Test
	public void testMemoryInitEmpty() 
	{
		StrategyManager strat = new StrategyManager();
		strat.initMemory();
		System.out.println(strat.getMemory().toString());	
		assertEquals(strat.getMemory().toString(), "{}");
	}

	@Test
	public void testMemoryWriteWithEmptyMemory()
	{
		StrategyManager strat = new StrategyManager();
		strat.initMemory();
		strat.writeMemory();
		assertTrue(new File(memoryFileName).exists());
	}
	
	@Test
	public void testMemoryReadWithEmptyMemory()
	{
		StrategyManager strat = new StrategyManager();
		strat.initMemory();
		Hashtable<Integer, Double[]> temp = strat.readMemory();
		assertEquals(temp.toString(), "{}");
	}
	
	@Test
	public void testAlphaWrite()
	{
		StrategyManager strat = new StrategyManager();
		strat.writeAlphaValue();
		
		Double value = 0.0;
		try{
			File f = new File(alphaValueFileName);
			Scanner sc = new Scanner(f);
			value = sc.nextDouble();
			sc.close();
		}
		catch(Exception ex){
			
		}
		assertTrue(value.equals(.999));
	}
	
	@Test
	public void testAlphaRead()
	{
		StrategyManager strat = new StrategyManager();
		strat.writeAlphaValue();
		Double result = strat.readAlphaValue();
		
		assertTrue(result.equals(.999));
	}
	
	@Test
	public void testEpsilonWrite()
	{
		StrategyManager strat = new StrategyManager();
		strat.writeEpsilonValue();
		
		Double value = 0.0;
		try{
			File f = new File(epsilonValueFileName);
			Scanner sc = new Scanner(f);
			value = sc.nextDouble();
			sc.close();
		}
		catch(Exception ex){
			
		}
		assertTrue(value.equals(.9999));
	}
	
	@Test
	public void testEpsilonRead()
	{
		StrategyManager strat = new StrategyManager();
		strat.writeEpsilonValue();
		Double result = strat.readEpsilonValue();
		
		assertTrue(result.equals(.9999));
	}
	
	////// ==== Helper methods =====  //////
	@Override
	public String toString() {
		return "State [Units=" + Units.toString() + ", EnemyArmyPosition=" + EnemyArmyPosition.toString() + ", EnemyBuildingInfo="
				+ EnemyBuildingInfo.toString() + ", EnemyArmyInfo=" + EnemyArmyInfo.toString() + ", EnemyArmyCount=" + EnemyArmyCount + "]";
	}

}
