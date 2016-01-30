package StarcraftAI;

/**
 * Population
 * Keeps track of a population of genes for use in a genetic algorithm.
 * 
 * @author Casey Sigelmann
 *
 */
public class Population {
	private Gene[] population;
	private final int POPULATION_SIZE = 20;
	
	public Population()
	{
		population = new Gene[POPULATION_SIZE];
	}
}
