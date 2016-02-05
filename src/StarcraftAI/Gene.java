package StarcraftAI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Gene
 * Represents a gene for use in a genetic algorithm for building placement.
 * 
 * @author Casey Sigelmann
 * @author Alex Bowns
 */
public class Gene implements Serializable{
	//private int[] gene
	private ArrayList<Integer> gene;
	private double fitness;
	public static final int GENE_SIZE = 2000; //estimated 850 TilePositions in Benzene;
	public static final int NUM_GAMES_FOR_FITNESS_EVAL = 10;
	private int wins;
	private int losses; 
	
	
	/**
	 * ctor
	 */
	public Gene()
	{
		gene = new ArrayList<Integer>();
		fitness = -1.0;
		wins = 0;
		losses = 0;
		
		//initialize gene with random values
		for (int i = 0; i < GENE_SIZE; i++)
		{
			gene.add(i);
		}
	}
	

	
	public void setListValues(ArrayList<Integer> values)
	{
		gene = values;
	}
	
	/**
	 * getValue
	 * 
	 * @return the value of the given allele
	 */
	public int getValue(int idx)
	{
		return gene.get(idx);
	}
	
	/**
	 * getRange()
	 * 
	 * @param - start and end - both are indices
	 * @return the subList of the gene
	 */
	public ArrayList<Integer> getRange(int start, int end)
	{
		return (ArrayList<Integer>) gene.subList(start, end);
	}
	
	/*
	 * mutateAllele()
	 * Sometimes mutate a single, random, allele of a gene. The probability of a mutation is 5%. 
	 */
	public void mutateAllele()
	{
		int rand = (int)(Math.random() * 20);
		// pick a random allele of the gene to mutate
		if (rand == 1)
		{
			int allele = (int)(Math.random() * GENE_SIZE);
			int newVal = (int)(Math.random() * Integer.MAX_VALUE);
			gene.set(allele, newVal);
		}
	}
	
	public double getFitness()
	{
		return fitness;
	}
	
	//TODO waiting for Max's StrategyManager winEvent method to be implemented 
	public void updateFitness(boolean hasWon, double time)
	{
		if(hasWon)
		{
			wins++;
		}
		else
		{
			losses++;
		}
		if(wins + losses >= NUM_GAMES_FOR_FITNESS_EVAL)
		{
			//TODO update fitness using time and win/loss percentage
		}
		
	}
}

