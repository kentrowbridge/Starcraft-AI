package StarcraftAI;

/**
 * Gene
 * Represents a gene for use in a genetic algorithm for building placement.
 * 
 * @author Casey Sigelmann
 */
public class Gene {
	private int[] gene;
	private double fitness;
	private final int GENE_SIZE = 2000; //estimated 850 TilePositions in Benzene;
	
	/**
	 * ctor
	 */
	public Gene()
	{
		gene = new int[GENE_SIZE];
		for(int i = 0; i < gene.length; i++)
		{
			gene[i] = -1;
		}
		
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
		gene = values;
	}
	
	/**
	 * getValue
	 * 
	 * @return the value of the given allele
	 */
	public int getValue(int idx)
	{
		return gene[idx];
	}
}
