package StarcraftAI;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Gene
 * Represents a gene for use in a genetic algorithm for building placement.
 * 
 * @author Casey Sigelmann
 * @author Alex Bowns
 */
public class Gene {

	private ArrayList<Integer> gene;
	private ArrayList<Integer> geneClone;
	private double fitness;
	private int size;
	public static final int NUM_GAMES_FOR_FITNESS_EVAL = 10;
	private int wins;
	private int losses; 
	private ArrayList<Long> timeList;


	/**
	 * ctor
	 */
	public Gene(int geneSize)
	{
		gene = new ArrayList<Integer>();
		geneClone = new ArrayList<Integer>();
		size = geneSize;
		fitness = -1.0;
		wins = 0;
		losses = 0;

		//initialize gene with random values
		for (int i = 0; i < size; i++)
		{
			int geneVal = (int) (Math.random()*Integer.MAX_VALUE);
			gene.add(geneVal);
		}
		geneClone.addAll(gene);
		timeList = new ArrayList<Long>(); 
	}

	public int getSize()
	{
		return size;
	}


	/**
	 * setListValues()
	 * Set the gene and clone values to new values.
	 */
	public void setListValues(ArrayList<Integer> values)
	{
		gene = new ArrayList<Integer>(values);
		geneClone = new ArrayList<Integer>(values); 
	}

	/**
	 * getHighestIdx()
	 * Find and return the index in the gene array list with the highest value.
	 */
	public int getHighestIdx()
	{
		int tempIdx = -1;
		int tempVal = -1; 
		for (int i = 0; i < gene.size(); i++)
		{
			if (gene.get(i) > tempVal)
			{
				tempVal = gene.get(i);
				tempIdx = i; 
			}
		}


		return tempIdx;
	}

	/**
	 * deactivateIndex()
	 * Set the given index to be -1 in case this index was occupied (so we don't re pick it). 
	 * 
	 * @param index	the index of this gene to deactivate.
	 */
	public void deactivateIndex(int index)
	{
		gene.set(index, -1);
	}

	/**
	 * reactivateIndex()
	 * Resets the given index to its original value.
	 * 
	 * @param index	the index of this gene to reset.
	 */
	public void reactivateIndex(int index)
	{
		gene.set(index, geneClone.get(index));
	}

	/**
	 * getValue
	 * 
	 * @return the value of the given allele.
	 */
	public int getValue(int idx)
	{
		return gene.get(idx);
	}

	/**
	 * getRange()
	 * 
	 * @param start	index at start of range
	 * @param end	index at end of range
	 * @return the subList of the gene
	 */
	public List<Integer> getRange(int start, int end)
	{
		return gene.subList(start, end);
	}

	/**
	 * mutateAllele()
	 * Sometimes mutate a single, random, allele of a gene. The probability of a mutation is 20%. 
	 */
	public void mutateAllele()
	{
		int rand = (int)(Math.random() * 5);
		// pick a random allele of the gene to mutate
		if (rand == 1)
		{
			int allele = (int)(Math.random() * size);
			int newVal = (int)(Math.random() * Integer.MAX_VALUE);
			gene.set(allele, newVal);
			geneClone.set(allele, newVal); 
		}
	}

	/**
	 * getFitness()
	 * @return the fitness value
	 */
	public double getFitness()
	{
		return fitness;
	}

	/**
	 * updateFitness()
	 * Updates the fitness for this gene.
	 * 
	 * @param hasWon		true if we have won the last game.
	 * @param elapsedTime	how long the game took in seconds.
	 */
	public void updateFitness(boolean hasWon, long elapsedTime)
	{
		timeList.add(elapsedTime);

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
			long avgTime = (long) 0.0;
			for (long time : timeList)
			{
				avgTime += time;
			}
			avgTime /= timeList.size();

			fitness = ((wins*1.0) / (wins + losses))*0.8 + (avgTime / 500.0)*0.2; 
		}	
	}


	/**
	 * printGene
	 * Prints the values in the gene and geneClone
	 */
	public void printGene()
	{
		System.out.println("Gene: " + gene.toString());
		System.out.println("GeneClone: " + geneClone.toString());
		System.out.println("Games: " + (wins+losses));
		System.out.println("Wins: " + wins);
		System.out.println("Fitness: " + fitness);
	}

	/**
	 * toString()
	 */
	public String geneToString()
	{
		return gene.toString();
	}

	/**
	 * getGames()
	 */
	public int getLosses()
	{
		return losses;
	}

	/**
	 * getWins()
	 */
	public int getWins()
	{
		return wins;
	}

	public void setWins(int wins)
	{
		this.wins = wins;
	}
	public void setLosses(int losses)
	{
		this.losses = losses;
	}
	public void setFitness(float fitness)
	{
		this.fitness = fitness;
	}


	/**
	 * reset
	 * Resets the gene to original values
	 */
	public void reset()
	{
		for (int i = 0; i < gene.size(); i++)
		{
			reactivateIndex(i);	
		}
	}
}

