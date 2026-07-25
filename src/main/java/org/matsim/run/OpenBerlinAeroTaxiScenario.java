package org.matsim.run;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.TransportMode;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
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

	private static final Map<String, Coord> VERTIPORTS = Map.of(
		"FlughafenBER", new Coord(806543.5, 5811370.5),
		"Spandau", new Coord(793982.2, 5825971.3),
		"MediaSpree", new Coord(799180.3, 5828177.5),
		"BerlinHbf", new Coord(796906.3, 5826705.7),
		"PotsdamerPlatz", new Coord(796368.1, 5828408.9),
		"EuropaCenter", new Coord(784793.5, 5828898.5),
		"AlexanderPlatz", new Coord(801876.3, 5826452.1)
	);
	private void addVertiportLayer(Network network) {
		NetworkFactory factory = network.getFactory();

		for (String name : VERTIPORTS.keySet()){
			Coord coord = VERTIPORTS.get(name);
			Node node = factory.createNode(Id.createNodeId("vp_" + name), coord);
			network.addNode(node);
		}

		for(String from : VERTIPORTS.keySet()){
			for (String to : VERTIPORTS.keySet()){
				if(from.equals(to)) continue;

				Node fromNode = network.getNodes().get(Id.createNodeId("vp_" + from));
				Node toNode = network.getNodes().get(Id.createNodeId("vp_" + to));

				Link link = factory.createLink(
					Id.createLinkId("avtaxi_" + from + "_" + to),
					fromNode, toNode
				);

				link.setFreespeed(60.0);
				link.setAllowedModes(Set.of("avtaxi"));
				network.addLink(link);
			}
		}
	}
	private void addMode(){}

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
		addVertiportLayer(scenario.getNetwork());
	}

	@Override
	protected void prepareControler(Controler controler){
		super.prepareControler(controler);
	}
}
