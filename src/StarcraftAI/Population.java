package StarcraftAI;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Population
 * Keeps track of a population of genes for use in a genetic algorithm.
 * 
 * @author Casey Sigelmann
 *
 */
public class Population implements Serializable{
	private Gene[] population;
	private final int POPULATION_SIZE = 20;
	private int generation; 
	private int index; 
	
	public Population()
	{
		population = new Gene[POPULATION_SIZE];
	}
	
	//TODO 
	public Gene getNextGene()
	{
		return population[0];
	}
	
	public void initRandomPopulation()
	{
		ArrayList<Integer> valList = new ArrayList<Integer>();	
		for (int z = 0; z < POPULATION_SIZE; z++)
		{
			for (int i = 0; i < 2000; i++)
			{
				valList.add(i);
			}
			population[z].setListValues(valList);	
		}
		
	}
}
