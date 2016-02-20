package StarcraftAI;

/**
 * mineralAndGasValue 
 * 
 * This Enum keeps is to keep track of how many minerals
 * and how much gas we have in a given state.
 * The naming convention is as follows:
 * 'char' : m or g for mineral or gas value
 * range : a range of numbers corresponding to the number of minerals or gas
 * 	that this label represents. If no end number, assume just greater or equal to the first number
 * 
 * eg:  m0_149 means minerals in the range of 0-149 minerals. 
 * 		g126 means more than 126 gas. 
 * 
 * @author MaxRobinson
 *
 */
public enum MineralAndGasValue {
	m0_149,
	m150_400,
	m401,
	g0_24,
	g25_125,
	g126,
}
