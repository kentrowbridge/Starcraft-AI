package StarcraftAI;

import java.lang.reflect.Field;
import java.util.*;
import bwapi.*;

public class State {
	private Hashtable<String, Integer> Units;
	private ArrayList<ArmyPosition> EnemyArmyPosition;
	private HashSet<String> EnemyBuildingInfo;
	private HashSet<String> EnemyArmyInfo;
	private int EnemyArmyCount;
	private MineralAndGasValue Minerals;
	private MineralAndGasValue Gas;

	/**
	 * State
	 * Default Ctor 
	 */
	public State()
	{
		Units = new Hashtable<String, Integer>();
		EnemyArmyPosition = new ArrayList<ArmyPosition>();
		EnemyBuildingInfo = new HashSet<String>();
		EnemyArmyInfo = new HashSet<String>();
		EnemyArmyCount = 0;
		Minerals = MineralAndGasValue.m0_149;
		Gas = MineralAndGasValue.g0_24;
	}
	
	/**
	 * State
	 * Ctor
	 * A state will be used for TD-Learning. A state will represent what the state of the game looks like
	 * at a given point in time. States are each have a given amount of information. 
	 * 
	 * @param units
	 * @param enemyArmyPosition
	 * @param enemyBuildingInfo
	 * @param enemyArmyInfo
	 * @param enemyArmyCount
	 */
	public State(Hashtable<String, Integer> units, ArrayList<ArmyPosition> enemyArmyPosition, HashSet<String> enemyBuildingInfo, 
			HashSet<String> enemyArmyInfo, int enemyArmyCount, MineralAndGasValue minerals, MineralAndGasValue gas)
	{
		Units = new Hashtable<String, Integer>(units);
		EnemyArmyPosition = new ArrayList<ArmyPosition>(enemyArmyPosition);
		EnemyBuildingInfo = new HashSet<String>(enemyBuildingInfo);
		EnemyArmyInfo = new HashSet<String>(enemyArmyInfo);
		EnemyArmyCount = enemyArmyCount;
		Minerals = minerals;
		Gas = gas;
	}
	
	/**
	 * State
	 * Copy Ctor
	 * 
	 * @param state - the state to copy
	 */
	public State(State state)
	{
		Units = new Hashtable<String, Integer>(state.getUnits());
		EnemyArmyPosition = new ArrayList<ArmyPosition>(state.getEnemyArmyPosition());
		EnemyBuildingInfo = new HashSet<String>(state.getEnemyBuildingInfo());
		EnemyArmyInfo = new HashSet<String>(state.getEnemyArmyInfo());
		EnemyArmyCount = state.getEnemyArmyCount();
		Minerals = state.getMinerals();
		Gas = state.getGas();
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + EnemyArmyCount;
		result = prime * result + ((EnemyArmyInfo == null) ? 0 : EnemyArmyInfo.hashCode());
		result = prime * result + ((EnemyArmyPosition == null) ? 0 : EnemyArmyPosition.hashCode());
		result = prime * result + ((EnemyBuildingInfo == null) ? 0 : EnemyBuildingInfo.hashCode());
		result = prime * result + ((Gas == null) ? 0 : Gas.hashCode());
		result = prime * result + ((Minerals == null) ? 0 : Minerals.hashCode());
		result = prime * result + ((Units == null) ? 0 : Units.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		State other = (State) obj;
		if (EnemyArmyCount != other.EnemyArmyCount)
			return false;
		if (EnemyArmyInfo == null) {
			if (other.EnemyArmyInfo != null)
				return false;
		} else if (!EnemyArmyInfo.equals(other.EnemyArmyInfo))
			return false;
		if (EnemyArmyPosition == null) {
			if (other.EnemyArmyPosition != null)
				return false;
		} else if (!EnemyArmyPosition.equals(other.EnemyArmyPosition))
			return false;
		if (EnemyBuildingInfo == null) {
			if (other.EnemyBuildingInfo != null)
				return false;
		} else if (!EnemyBuildingInfo.equals(other.EnemyBuildingInfo))
			return false;
		if (Gas != other.Gas)
			return false;
		if (Minerals != other.Minerals)
			return false;
		if (Units == null) {
			if (other.Units != null)
				return false;
		} else if (!Units.equals(other.Units))
			return false;
		return true;
	}
	
	
	/**
	 * toString()
	 * return the HashCode corresponding to a state.
	 */
	@Override
	public String toString()
	{
		return "" + this.hashCode();
	}
	
	/**
	 * toString()
	 * Allows for more information to be printed about a given state. Based on the instance variables 
	 * All instance variables that aren't null will be returned in a string value. 
	 * 
	 * @param debug - if true, return the debug info. otherwise use toString()
	 * @return
	 */
	public String toString(boolean debug) 
	{
		if(!debug)
		{
			return this.toString();
		}
		return "State [Units=" + Units.toString() + ", EnemyArmyPosition=" + EnemyArmyPosition.toString() + ", EnemyBuildingInfo="
				+ EnemyBuildingInfo.toString() + ", EnemyArmyInfo=" + EnemyArmyInfo.toString() 
				+ ", EnemyArmyCount=" + EnemyArmyCount + "]";
	}

	
	//// ==== Getters and Setters ==== /////
	public Hashtable<String, Integer> getUnits() {
		return Units;
	}

	public void setUnits(Hashtable<String, Integer> units) {
		Units = units;
	}

	public ArrayList<ArmyPosition> getEnemyArmyPosition() {
		return EnemyArmyPosition;
	}

	public void setEnemyArmyPosition(ArrayList<ArmyPosition> enemyArmyPosition) {
		EnemyArmyPosition = enemyArmyPosition;
	}

	public HashSet<String> getEnemyBuildingInfo() {
		return EnemyBuildingInfo;
	}

	public void setEnemyBuildingInfo(HashSet<String> enemyBuildingInfo) {
		EnemyBuildingInfo = enemyBuildingInfo;
	}

	public HashSet<String> getEnemyArmyInfo() {
		return EnemyArmyInfo;
	}

	public void setEnemyArmyInfo(HashSet<String> enemyArmyInfo) {
		EnemyArmyInfo = enemyArmyInfo;
	}

	public int getEnemyArmyCount() {
		return EnemyArmyCount;
	}

	public void setEnemyArmyCount(int enemyArmyCount) {
		EnemyArmyCount = enemyArmyCount;
	}

	public MineralAndGasValue getMinerals() {
		return Minerals;
	}

	public void setMinerals(MineralAndGasValue minerals) {
		Minerals = minerals;
	}

	public MineralAndGasValue getGas() {
		return Gas;
	}

	public void setGas(MineralAndGasValue gas) {
		Gas = gas;
	}
}
