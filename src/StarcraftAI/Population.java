package StarcraftAI;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

/**
 * Population
 * Keeps track of a population of genes for use in a genetic algorithm.
 * 
 * @author Casey Sigelmann
 *
 */
public class Population {
	
	private Gene[] population;
	public static final int POPULATION_SIZE = 20;
	private int generationCount;
	
	public Population(int geneSize, int generationCount)
	{
		population = new Gene[POPULATION_SIZE];
		for(int i = 0; i < population.length; i++)
		{
			population[i] = new Gene(geneSize);
		}
		this.generationCount = generationCount;
	}
	
	/**
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
	
	/**
	 * setGene()
	 * Sets the value of the given index in the population to be the given gene.
	 * 
	 * @param index	the index of the gene to set.
	 * @param gene	the gene to set.
	 */
	public void setGene(int index, Gene gene)
	{
		population[index] = gene; 
	}
	
	/**
	 * allGenesAnalyzed()
	 * Check if all the genes in a population have been analyzed
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
	
	/**
	 * getGene()
	 * gets the gene at the given index
	 * 
	 * @param index	the index of the gene
	 * @return	the gene
	 */
	public Gene getGene(int index)
	{
		return population[index];
	}
	
	/**
	 * printPopulation()
	 * Prints a representation of this population to the console.
	 */
	public void printPopulation()
	{
		System.out.println("***** <Population> *****");
		System.out.println("Generation: " + generationCount);
		for(Gene gene : population)
		{
			System.out.println("====================");
			gene.printGene();
		}
		System.out.println("***** </Population> *****");
	}
	
	public int getGenerationCount()
	{
		return generationCount;
	}
	
	public void setGenerationCount(int generationCount)
	{
		this.generationCount = generationCount;
	}
	
	/**
	 * savePopulationFile()
	 * Saves this population to the given file.
	 * 
	 * @param mapFileName	the name of the file to save to.
	 */
	public void savePopulationFile(String mapFileName)
	{
		String populationFileName = mapFileName;
		PrintWriter pw;
		
		try 
		{
			pw = new PrintWriter(populationFileName);
			pw.println(generationCount);
			for(Gene gene : population)
			{
				gene.reset();
				pw.println("====================");
				pw.println(gene.geneToString());
				pw.println(gene.getWins());
				pw.println(gene.getLosses());
				pw.println(gene.getFitness());
			}
			pw.close();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return; 
		}
	}
}
