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
	public static final int POPULATION_SIZE = 20;
	private int generation; 
	private int index; 
	
	public Population()
	{
		population = new Gene[POPULATION_SIZE];
		for(int i = 0; i < population.length; i++)
		{
			population[i] = new Gene();
		}
		
	}
	
	//TODO comment
	public Gene getNextGene()
	{
		for(int i = index; i < population.length; i++)
		{
			Gene candidateGene = population[i];
			if(candidateGene.getFitness() == -1.0)
			{
				return candidateGene;
			}
		}
		return null;
	}
}
