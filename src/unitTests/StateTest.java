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
	
	private Hashtable<String, Integer> Units;
	private ArrayList<ArmyPosition> EnemyArmyPosition;
	private HashSet<String> EnemyBuildingInfo;
	private HashSet<String> EnemyArmyInfo;
	private int EnemyArmyCount;
	private MineralAndGasValue Minerals;
	private MineralAndGasValue Gas; 
	
	
	
	///// ===== Set up, Tear Down ==== //////
	
	@Before
	public void setUp() throws Exception 
	{
		
		Units = new Hashtable<String, Integer>();
		EnemyArmyPosition = new ArrayList<ArmyPosition>();
		EnemyBuildingInfo = new HashSet<String>();
		EnemyArmyInfo = new HashSet<String>();
		EnemyArmyCount = 0;		
		
		EnemyArmyPosition.add(ArmyPosition.OurBase);
		EnemyBuildingInfo.add("Terran SCV");
		EnemyArmyInfo.add("Zerg Zergling");
		EnemyArmyCount = 10;
		Minerals = MineralAndGasValue.m0_149;
		Gas = MineralAndGasValue.g0_24;
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
		System.out.println("hashCodeReturn0Test: " + state.toString());
		assertEquals(state.hashCode(), -436006662);
	}
	
	@Test
	public void hashCodeReturnEquivalenceTest() 
	{
		State state = new State(Units, EnemyArmyPosition, EnemyBuildingInfo, EnemyArmyInfo, EnemyArmyCount, Minerals, Gas );
		System.out.println(state.toString());
		assertEquals(state.hashCode(), 861800028);
	}
	
	@Test
	public void toStringDebugTest() 
	{
		State state = new State(Units, EnemyArmyPosition, EnemyBuildingInfo, EnemyArmyInfo, EnemyArmyCount, Minerals, Gas);
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
