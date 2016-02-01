package StarcraftAI;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Gene
 * Represents a gene for use in a genetic algorithm for building placement.
 * 
 * @author Casey Sigelmann
 * @author Alex Bowns
 */
public class Gene {
	//private int[] gene
	private ArrayList<Integer> gene;
	private double fitness;
	private final int GENE_SIZE = 2000; //estimated 850 TilePositions in Benzene;
	
	/**
	 * ctor
	 */
	public Gene()
	{
		//gene = new int[GENE_SIZE];
		gene = new ArrayList<Integer>();
//		for(int i = 0; i < gene.length; i++)
//		{
//			gene[i] = -1;
//		}
		
		fitness = 0;
	}
	
	/**
	 * setValues
	 * Sets this gene to the given values
	 * 
	 * @param values
	 */
	public void setValues(int[] values)
	{
		for (int i = 0; i < values.length; i++)
		{
			gene.add(values[i]);
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
			//TODO not sure what the range of our alleles is...so i'm guessing
			int newVal = (int)(Math.random() * 1000);
			gene.set(allele, newVal);
		}
	}
}
