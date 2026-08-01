package org.matsim.run;


import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;
import org.matsim.application.MATSimApplication;
import org.matsim.core.config.Config;
import org.matsim.core.config.groups.RoutingConfigGroup;
import org.matsim.core.config.groups.ScoringConfigGroup;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.vehicles.VehicleType;
import org.matsim.vehicles.VehicleUtils;

import java.util.*;

public class OpenBerlinAeroTaxiScenario extends OpenBerlinScenario{

	public static void main(String[] args) {
		MATSimApplication.run(OpenBerlinAeroTaxiScenario.class, args);
	}

	@Override
	protected Config prepareConfig(Config config) {
		config = super.prepareConfig(config);
		config.controller().setLastIteration(0);
		config.controller().setOutputDirectory("outputAerotaxiFirstRun");

//		// --- 1pct sample ---
//		config.plans().setInputFile(
//			"https://svn.vsp.tu-berlin.de/repos/public-svn/matsim/scenarios/countries/de/berlin/berlin-v6.0/input/berlin-v6.0-1pct.plans.xml.gz");
//		config.qsim().setFlowCapFactor(0.01);
//		config.qsim().setStorageCapFactor(Math.pow(0.01, 0.75)); // ≈ 0.032, standard scaling
//		config.counts().setCountsScaleFactor(100.0);

		// ROUTING
		Set<String> networkModes = new HashSet<>(config.routing().getNetworkModes());
		networkModes.add("aeroTaxi");
		config.routing().setNetworkModes(networkModes);

		//SCORING
		ScoringConfigGroup.ModeParams params = new ScoringConfigGroup.ModeParams("aeroTaxi");
		params.setConstant(-2.0); // whats this?
		params.setMarginalUtilityOfTraveling(0.0); // whats this
		params.setMonetaryDistanceRate(-0.003); //whats this
		params.setDailyMonetaryConstant(-10.0); //whats this
		config.scoring().addModeParams(params);

		//mode choice
		config.subtourModeChoice().setModes(
			new String[]{"car", "pt", "bike", "walk", "ride", "aeroTaxi"}
		);

		return config;
	}

	@Override
	protected void prepareScenario(Scenario scenario) {
		super.prepareScenario(scenario);

		Network network = scenario.getNetwork();
		NetworkFactory nf = network.getFactory();

		Map<String, Coord> vertiports = Map.of(
			"BER",            new Coord(802_211., 5_806_616.),
			"Hauptbahnhof",   new Coord(795_303., 5_824_951.),
			"Alexanderplatz", new Coord(798_233., 5_824_446.),
			"ZooCityWest",    new Coord(792_780., 5_823_072.),
			"Adlershof",      new Coord(804_795., 5_814_446.)
		);

		Map<String, Node> nodes = new HashMap<>();
		vertiports.forEach((name, coord) -> {
			Node n = nf.createNode(Id.createNodeId("vertiport_" + name), coord);
			network.addNode(n);
			nodes.put(name, n);
		});

		List<String> names = new ArrayList<>(nodes.keySet());
		for (int i = 0; i < names.size(); i++) {
			for (int j = i + 1; j < names.size(); j++) {
				addFlightLink(network, nf, nodes.get(names.get(i)), nodes.get(names.get(j)));
				addFlightLink(network, nf, nodes.get(names.get(j)), nodes.get(names.get(i)));
			}
		}

		// vehicle type, in case vehiclesSource requires one per network mode
		VehicleType vt = VehicleUtils.createVehicleType(Id.create("aeroTaxi", VehicleType.class));
		vt.setMaximumVelocity(33.33);
		vt.setNetworkMode("aeroTaxi");
		scenario.getVehicles().addVehicleType(vt);
	}

	private void addFlightLink(Network network, NetworkFactory nf, Node from, Node to) {
		Link l = nf.createLink(Id.createLinkId("air_" + from.getId() + "_" + to.getId()), from, to);
		l.setLength(CoordUtils.calcEuclideanDistance(from.getCoord(), to.getCoord()));
		l.setFreespeed(33.33);
		l.setCapacity(99999.0);
		l.setNumberOfLanes(1);
		l.setAllowedModes(Set.of("aeroTaxi"));
		network.addLink(l);
	}
}
