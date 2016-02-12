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
	private static final long serialVersionUID = 7808275998841633772L;
	
	public Population(int geneSize)
	{
		population = new Gene[POPULATION_SIZE];
		for(int i = 0; i < population.length; i++)
		{
			population[i] = new Gene(geneSize);
		}
		
	}
	
	/*
	 * getNextGene()
	 * from the gene population, select the next gene that has not been evaluated yet. 
	 */
	public Gene getNextGene()
	{
		for(int i = 0; i < population.length; i++)
		{
			Gene candidateGene = population[i];
			if(candidateGene.getFitness() == -1.0)
			{
				return candidateGene;
			}
		}
		return null;
	}
	
	public void setGene(int index, Gene gene)
	{
		population[index] = gene; 
	}
	
	/*
	 * allGenesAnalyzed()
	 * check if all the genes in a population have been analyzed
	 */
	public boolean allGenesAnalyzed()
	{
		for(int i = 0; i < population.length; i++)
		{
			Gene candidateGene = population[i];
			if(candidateGene.getFitness() == -1.0)
			{
				return false;
			}
		}
		return true;
	}
	
	public Gene getGene(int index)
	{
		return population[index];
	}
	
}
