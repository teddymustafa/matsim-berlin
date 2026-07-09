package org.matsim.run;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.application.MATSimApplication;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;
import org.matsim.vehicles.VehicleType;

public class OpenBerlinS21Scenario extends OpenBerlinScenario {

	private static final String LINE_NAME = "S21";


	public static void main(String[] args) {
		MATSimApplication.execute(OpenBerlinS21Scenario.class, args);
	}
	@Override
	protected Config prepareConfig(Config config){
		super.prepareConfig(config);

		config.controller().setLastIteration(100);
		config.controller().setOutputDirectory("output-s21-100");

		return config;
	}

	@Override
	protected void prepareScenario(Scenario scenario) {
		super.prepareScenario(scenario);

		// Add S21 as S-Bahn_veh_type
		var vehicleType = scenario.getTransitVehicles().getVehicleTypes().get(Id.create("S-Bahn_veh_type", VehicleType.class));

		// Add Network Elements
		var network = NetworkUtils.readNetwork();

		// Nodes

		// Ideal ware Westhafen>wedding>perlebergerbruecke> Hbf> Potsdamer Platz > Gleisdreieck> yorckstrasse grossgorchen> Julius Leber Brücke> südkreuz
		//TODO: new node for perleberger Bruecke
		var fromNode = network.getFactory().createNode(Id.createNodeId("from"), new Coord(0,0));
		var toNode = network.getFactory().createNode(Id.createNodeId("to"), new Coord(100,100));

		// Add Nodes
		network.addNode(fromNode);


		// Links
		// links for wedding-perlbruck ; perlbruck-hbf ; hbf-potsdamerplatz ; potsdamerplas-Gleisdreieck ; gleisdreieck-yorckgros ; yorckgross-julileber; julileber-suedkreuy
		var link = network.getFactory().createLink(Id.createLinkId("link-id"), fromNode, toNode);
		var link = network.getFactory().createLink(Id.createLinkId("link-id"), fromNode, toNode);
		var link = network.getFactory().createLink(Id.createLinkId("link-id"), fromNode, toNode);
		var link = network.getFactory().createLink(Id.createLinkId("link-id"), fromNode, toNode);
		var link = network.getFactory().createLink(Id.createLinkId("link-id"), fromNode, toNode);
		var link = network.getFactory().createLink(Id.createLinkId("link-id"), fromNode, toNode);
		var link = network.getFactory().createLink(Id.createLinkId("link-id"), fromNode, toNode);
		link.setFreespeed();

		// Add Links, many of them
		network.addLink(link);
	}
}
