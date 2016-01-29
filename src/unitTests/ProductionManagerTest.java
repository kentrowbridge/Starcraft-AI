package unitTests;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Hashtable;

import org.junit.Test;
import StarcraftAI.ProductionManager;
import bwapi.*;
import bwapi.UnitType;

public class ProductionManagerTest {

	@Test
	public void testExaminePath() {
		fail("Not yet implemented");
	}

	@Test
	public void testInitTechPaths() {
		Hashtable<UnitType, ArrayList<UnitType>> results = ProductionManager.initTechPaths();
		assertEquals(true, true);
	}

	@Test
	public void testBuildDependecies() {
		fail("Not yet implemented");
	}

}
