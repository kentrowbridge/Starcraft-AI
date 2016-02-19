package StarcraftAI;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
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
	private int generationCount;
	private static final long serialVersionUID = 7808275998841633772L;
	
	public Population(int geneSize, int generationCount)
	{
		population = new Gene[POPULATION_SIZE];
		for(int i = 0; i < population.length; i++)
		{
			population[i] = new Gene(geneSize);
		}
		this.generationCount = generationCount;
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
