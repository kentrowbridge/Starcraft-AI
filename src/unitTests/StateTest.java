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
	private State TestState;
	private String TestStateString;
	
	
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
		
		TestState = new State(Units, EnemyArmyPosition, EnemyBuildingInfo, EnemyArmyInfo, EnemyArmyCount, Minerals, Gas);
		TestStateString = "{834821160=[0.0, 0.76], }";
	}

	@After
	public void tearDown() throws Exception 
	{
		deleteFile(memoryFileName);
		deleteFile("alpha_value.txt");
		deleteFile("epsilon_value.txt");
	}

	
	///// ====== Tests ====  ////// 
	
	
	// ========= State Hash Code Tests ======== // 
	@Test
	public void hashCodeReturn0Test() 
	{
		State state = new State();
//		System.out.println("hashCodeReturn0Test: " + state.toString());
		assertEquals(state.hashCode(), -2056846209);
	}
	
	@Test
	public void hashCodeInitializedStateTest() 
	{
		State state = new State(Units, EnemyArmyPosition, EnemyBuildingInfo, EnemyArmyInfo, EnemyArmyCount, Minerals, Gas);
//		System.out.println(state.toString());
		assertEquals(state.hashCode(), 834821160);
	}
	
	@Test
	public void testStateHashCodeEquivalence(){
		State state = new State(Units, EnemyArmyPosition, EnemyBuildingInfo, EnemyArmyInfo, EnemyArmyCount, Minerals, Gas);
		State state2 = new State(Units, EnemyArmyPosition, EnemyBuildingInfo, EnemyArmyInfo, EnemyArmyCount, Minerals, Gas);
		assertTrue(state.hashCode() == state2.hashCode());
	}
	
	// =============== State Tests =============== // 
	
	@Test
	public void toStringDebugTest() 
	{
		State state = new State(Units, EnemyArmyPosition, EnemyBuildingInfo, EnemyArmyInfo, EnemyArmyCount, Minerals, Gas);
//		System.out.println(state.toString(true));
		assertEquals(state.toString(true), this.toString());
	}
	
	@Test
	public void testStateEquals(){
		State state = new State(Units, EnemyArmyPosition, EnemyBuildingInfo, EnemyArmyInfo, EnemyArmyCount, Minerals, Gas);
		State state2 = new State(Units, EnemyArmyPosition, EnemyBuildingInfo, EnemyArmyInfo, EnemyArmyCount, Minerals, Gas);
		assertTrue(state.equals(state2));
	}
	
	
	// ========== Memory Tests =============== //
	
	/**
	 * NOTE!!!! : 
	 * Can't have a memory file in existence yet, other wise fails.  
	 */
	@Test
	public void testMemoryInitEmpty() 
	{
		deleteFile(memoryFileName);
		StrategyManager strat1 = new StrategyManager();
		strat1.initMemory();
//		System.out.println(strat.getMemory().toString());	
		assertEquals(strat1.getMemory().toString(), "{}");
	}
	
	@Test
	public void testUpdateMemoryOneState() 
	{
		StrategyManager strat2 = new StrategyManager();
		strat2.initMemory();
		strat2.updateMemory(TestState);
		System.out.println(strat2.toStringMemory());
		assertEquals(strat2.toStringMemory(), "{834821160=[0.0, 0.76], }");
	}
	
	@Test
	public void testUpdateMemoryTwoStates() 
	{
		State TestState2= new State(Units, EnemyArmyPosition, EnemyBuildingInfo, EnemyArmyInfo, 2, Minerals, Gas);
		
		StrategyManager strat = new StrategyManager();
		strat.initMemory();
		strat.updateMemory(TestState);
		strat.updateMemory(TestState2);
		
		System.out.println(strat.toStringMemory());
		assertEquals(strat.toStringMemory(), "{-1970240992=[0.0, 0.76], 834821160=[0.0, 0.5776], }");
	}
	
	// =========== I/O Unit Tests =========== //
	@Test
	public void testMemoryWriteWithEmptyMemory()
	{
		StrategyManager strat4 = new StrategyManager();
		strat4.initMemory();
		strat4.writeMemory();
		assertTrue(new File(memoryFileName).exists());
	}
	
	@Test
	public void testMemoryReadWithEmptyMemory()
	{
		StrategyManager strat5 = new StrategyManager();
		strat5.initMemory();
		strat5.writeMemory();
		Hashtable<Integer, Double[]> temp = strat5.readMemory();
		assertEquals(temp.toString(), "{}");
	}
	
	@Test
	public void testMemoryWriteWithOneStateMemory()
	{
		StrategyManager strat6 = new StrategyManager();
		strat6.initMemory();
		strat6.updateMemory(TestState);
		strat6.writeMemory();
		assertTrue(new File(memoryFileName).exists());
	}
	
	@Test
	public void testMemoryReadWithOneStateMemory()
	{
		StrategyManager strat7 = new StrategyManager();
		strat7.initMemory();
		strat7.updateMemory(TestState);
		strat7.writeMemory();
		
		Hashtable<Integer, Double[]> temp = strat7.readMemory();
		assertEquals(HashTableToString(temp), TestStateString);
	}
	
	
	@Test
	public void testAlphaWrite()
	{
		StrategyManager strat8 = new StrategyManager();
		strat8.writeAlphaValue();
		
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
		StrategyManager strat9 = new StrategyManager();
		strat9.writeAlphaValue();
		Double result = strat9.readAlphaValue();
		
		assertTrue(result.equals(.999));
	}
	
	@Test
	public void testEpsilonWrite()
	{
		StrategyManager strat10 = new StrategyManager();
		strat10.writeEpsilonValue();
		
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
		StrategyManager strat11 = new StrategyManager();
		strat11.writeEpsilonValue();
		Double result = strat11.readEpsilonValue();
		
		assertTrue(result.equals(.9999));
	}
	
	////// ==== Helper methods =====  //////
	@Override
	public String toString() {
		return "State [Units=" + Units.toString() + ", EnemyArmyPosition=" + EnemyArmyPosition.toString() + ", EnemyBuildingInfo="
				+ EnemyBuildingInfo.toString() + ", EnemyArmyInfo=" + EnemyArmyInfo.toString() + ", EnemyArmyCount=" + EnemyArmyCount + "]";
	}
	
	public void deleteFile(String fileName){
		File f = new File(fileName);
    	if(f.exists()){
    		f.delete();
    	}
	}
	
	/**
	 * 
	 * @param table
	 * @return
	 */
	public String HashTableToString(Hashtable<Integer, Double[]> table){
    	StringBuilder sb = new StringBuilder();
    	sb.append('{');
    	for(int key : table.keySet())
    	{
    		sb.append(key);
    		sb.append('=');
    		sb.append('[');
    		Double[] values = table.get(key);
    		for(int i = 0; i<2; i++){
    			sb.append(values[i]);
    			if (i!=1){
    				sb.append(',');
    				sb.append(' ');
    			}
    				
    		}
    		sb.append(']');
    		sb.append(',');
    		sb.append(' ');
    	}
    	sb.append('}');
    	
    	return sb.toString();
	}

}
