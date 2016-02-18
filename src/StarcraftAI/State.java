package StarcraftAI;

import java.lang.reflect.Field;
import java.util.*;
import bwapi.*;

public class State {
	private ArrayList<Unit> Units;
	private ArrayList<ArmyPosition> EnemyArmyPosition;
	private HashSet<UnitType> EnemyBuildingInfo;
	private HashSet<UnitType> EnemyArmyInfo;
	private int EnemyArmyCount;

	/**
	 * State
	 * Default Ctor 
	 */
	public State()
	{
		Units = new ArrayList<Unit>();
		EnemyArmyPosition = new ArrayList<ArmyPosition>();
		EnemyBuildingInfo = new HashSet<UnitType>();
		EnemyArmyInfo = new HashSet<UnitType>();
		EnemyArmyCount = 0;		
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
	public State(ArrayList<Unit> units, ArrayList<ArmyPosition> enemyArmyPosition, HashSet<UnitType> enemyBuildingInfo, 
			HashSet<UnitType> enemyArmyInfo, int enemyArmyCount)
	{
		Units = new ArrayList<Unit>(units);
		EnemyArmyPosition = new ArrayList<ArmyPosition>(enemyArmyPosition);
		EnemyBuildingInfo = new HashSet<UnitType>(enemyBuildingInfo);
		EnemyArmyInfo = new HashSet<UnitType>(enemyArmyInfo);
		EnemyArmyCount = enemyArmyCount;
	}
	
	/**
	 * State
	 * Copy Ctor
	 * 
	 * @param state - the state to copy
	 */
	public State(State state)
	{
		Units = new ArrayList<Unit>(state.getUnits());
		EnemyArmyPosition = new ArrayList<ArmyPosition>(state.getEnemyArmyPosition());
		EnemyBuildingInfo = new HashSet<UnitType>(state.getEnemyBuildingInfo());
		EnemyArmyInfo = new HashSet<UnitType>(state.getEnemyArmyInfo());
		EnemyArmyCount = state.getEnemyArmyCount();
	}
	
	
	/**
	 * hashCode()
	 */
	@Override
	public int hashCode() 
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + EnemyArmyCount;
		result = prime * result + ((EnemyArmyInfo == null) ? 0 : EnemyArmyInfo.hashCode());
		result = prime * result + ((EnemyArmyPosition == null) ? 0 : EnemyArmyPosition.hashCode());
		result = prime * result + ((EnemyBuildingInfo == null) ? 0 : EnemyBuildingInfo.hashCode());
		result = prime * result + ((Units == null) ? 0 : Units.hashCode());
		return result;
	}

	/**
	 * equals
	 * 
	 */
	@Override
	public boolean equals(Object obj) 
	{
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
	public ArrayList<Unit> getUnits() {
		return Units;
	}

	public void setUnits(ArrayList<Unit> units) {
		Units = units;
	}

	public ArrayList<ArmyPosition> getEnemyArmyPosition() {
		return EnemyArmyPosition;
	}

	public void setEnemyArmyPosition(ArrayList<ArmyPosition> enemyArmyPosition) {
		EnemyArmyPosition = enemyArmyPosition;
	}

	public HashSet<UnitType> getEnemyBuildingInfo() {
		return EnemyBuildingInfo;
	}

	public void setEnemyBuildingInfo(HashSet<UnitType> enemyBuildingInfo) {
		EnemyBuildingInfo = enemyBuildingInfo;
	}

	public HashSet<UnitType> getEnemyArmyInfo() {
		return EnemyArmyInfo;
	}

	public void setEnemyArmyInfo(HashSet<UnitType> enemyArmyInfo) {
		EnemyArmyInfo = enemyArmyInfo;
	}

	public int getEnemyArmyCount() {
		return EnemyArmyCount;
	}

	public void setEnemyArmyCount(int enemyArmyCount) {
		EnemyArmyCount = enemyArmyCount;
	}
}
