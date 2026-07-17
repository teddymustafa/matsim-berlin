package org.matsim.run;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.network.Link;
import org.matsim.application.MATSimApplication;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.controler.Controler;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.core.scenario.ScenarioUtils;
import org.matsim.pt.transitSchedule.api.*;

import java.util.*;

public class OpenBerlinAeroTaxiScenario extends OpenBerlinScenario {


	public static void main(String[] args) {
		MATSimApplication.execute(OpenBerlinAeroTaxiScenario.class, args);
	}
	@Override
	protected Config prepareConfig(Config config){
		super.prepareConfig(config);
		config.controller().setLastIteration(500);
		config.controller().setOutputDirectory("output-aerotaxi-500");
		return config;
	}

	@Override
	protected void prepareScenario(Scenario scenario) {
		super.prepareScenario(scenario);
	}

	@Override
	protected void prepareControler(Controler controler){
		super.prepareControler(controler);
	}
}
