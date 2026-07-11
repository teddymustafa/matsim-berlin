package org.matsim.run;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.network.Link;
import org.matsim.application.MATSimApplication;
import org.matsim.core.config.Config;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.pt.transitSchedule.api.*;
import org.matsim.vehicles.MatsimVehicleWriter;
import org.matsim.vehicles.VehicleType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OpenBerlinS21Scenario extends OpenBerlinScenario {


	public static void main(String[] args) {
		MATSimApplication.execute(OpenBerlinS21Scenario.class, args);
	}
	@Override
	protected Config prepareConfig(Config config){
		super.prepareConfig(config);

		config.controller().setLastIteration(0);
		config.controller().setOutputDirectory("output-s21-first");

		return config;
	}

	@Override
	protected void prepareScenario(Scenario scenario) {
		super.prepareScenario(scenario);

		double s21Freespeed = 22.22;
		// Add S21 as S-Bahn_veh_type
		var vehicleType = scenario.getTransitVehicles().getVehicleTypes().get(Id.create("S-Bahn_veh_type", VehicleType.class));

		// Add Network Elements
		var network = scenario.getNetwork();

		// Nodes

		// Ideal ware Westhafen>wedding>perlebergerbruecke> Hbf> Potsdamer Platz > Gleisdreieck> yorckstrasse grossgorchen> Julius Leber Brücke> südkreuz
		//TODO: new node for perleberger Bruecke and gleisdreieck

		// Creating 2 Nodes for S21
		var perlbruck = network.getFactory().createNode(Id.createNodeId("pt_perlbruck_suburbanRailway"), new Coord(795540.18,5829632.68));
		var gleisdreieck = network.getFactory().createNode(Id.createNodeId("pt_gleisdreieck_suburbanRailway"), new Coord(796845.48,5825610.95));

		// Adding 2 Nodes for S21
		List<Node> nodes= List.of(perlbruck, gleisdreieck);
		for(Node node : nodes) {
			network.addNode(node);
		}

		// Load nodes necessary for creating new links for S21
		Node westhafenNode = network.getNodes().get(Id.createNodeId("pt_473821_SuburbanRailway"));
		Node weddingNode = network.getNodes().get(Id.createNodeId("pt_4832_SuburbanRailway"));
		Node perlNode = network.getNodes().get(Id.createNodeId("pt_perlbruck_suburbanRailway"));
		Node hbfNode = network.getNodes().get(Id.createNodeId("pt_359974_SuburbanRailway"));
		Node potsdamerNode = network.getNodes().get(Id.createNodeId("pt_415349_SuburbanRailway"));
		Node gleisdreieckNode = network.getNodes().get(Id.createNodeId("pt_gleisdreieck_suburbanRailway"));
		Node yorckNode = network.getNodes().get(Id.createNodeId("pt_415374_SuburbanRailway"));
		Node juliusNode = network.getNodes().get(Id.createNodeId("pt_175655_SuburbanRailway"));
		Node sudkreuzNode = network.getNodes().get(Id.createNodeId("pt_176775_SuburbanRailway"));

		// Creating new links for S21

		// LOOP LINK
		Link linkS21LoopLink = network.getFactory().createLink(
			Id.createLinkId("linkS21LoopLink"),
			westhafenNode,
			westhafenNode
		);

		Link linkWesthafenWedding = network.getFactory().createLink(
			Id.createLinkId("WesthafenWedding"),
			westhafenNode,
			weddingNode
		);

		Link linkWeddingPerlbruck = network.getFactory().createLink(
			Id.createLinkId("WeddingPerlbruck"),
			weddingNode,
			perlNode
		);

		Link linkPerlbruckHbf = network.getFactory().createLink(
			Id.createLinkId("PerlbruckHbf"),
			perlNode,
			hbfNode
		);

		Link linkHbfPotsdamer = network.getFactory().createLink(
			Id.createLinkId("HbfPotsdamer"),
			hbfNode,
			potsdamerNode
		);

		Link linkPotsdamerGleisdreieck = network.getFactory().createLink(
			Id.createLinkId("PotsdamerGleisdreieck"),
			potsdamerNode,
			gleisdreieckNode
		);

		Link linkGleisdreieckYorck = network.getFactory().createLink(
			Id.createLinkId("GleisdreieckYorck"),
			gleisdreieckNode,
			yorckNode
		);

		Link linkYorckJulius = network.getFactory().createLink(
			Id.createLinkId("YorckJuli"),
			yorckNode,
			juliusNode
		);

		Link linkJuliusSudkreuz = network.getFactory().createLink(
			Id.createLinkId("JuliSudkreuz"),
			juliusNode,
			sudkreuzNode
		);

		// ASSUMPTION: use BeeLine distance for unknown, unbuilt stations
		linkS21LoopLink.setLength(10);

		// Add Links for S21
		List<Link> links = List.of(
			linkS21LoopLink,
			linkWesthafenWedding,
			linkWeddingPerlbruck,
			linkPerlbruckHbf,
			linkHbfPotsdamer,
			linkPotsdamerGleisdreieck,
			linkGleisdreieckYorck,
			linkYorckJulius,
			linkJuliusSudkreuz
		);
		for(Link link : links) {
			if(link.equals(linkS21LoopLink)){
				link.setLength(10);
			}
			else{
				link.setLength(NetworkUtils.getEuclideanDistance(
					link.getFromNode().getCoord(),link.getToNode().getCoord()
				));
			}
			link.setCapacity(100000.0);
			link.setFreespeed(s21Freespeed);
			link.setAllowedModes(Set.of("pt"));
			network.addLink(link);
		}

		// Creating Stops
		// CREATING LOOP STOP
		TransitStopFacility WesthafenonLoop = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("WesthafenonLoop", TransitStopFacility.class),
				westhafenNode.getCoord(),
				false);
		WesthafenonLoop.setLinkId(linkS21LoopLink.getId());
		WesthafenonLoop.setName("S+U Westhafen");
		scenario.getTransitSchedule().addStopFacility(WesthafenonLoop);

		TransitStopFacility toWeddingFromWesthafen = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
			Id.create("toWeddingFromWesthafen", TransitStopFacility.class),
			weddingNode.getCoord(),
			false);
		toWeddingFromWesthafen.setLinkId(linkWesthafenWedding.getId());
		toWeddingFromWesthafen.setName("S+U Wedding");
		scenario.getTransitSchedule().addStopFacility(toWeddingFromWesthafen);

		TransitStopFacility toPerlbruckFromWedding = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("toPerlbruckFromWedding", TransitStopFacility.class),
				perlNode.getCoord(),
				false);
		toPerlbruckFromWedding.setLinkId(linkWeddingPerlbruck.getId());
		toPerlbruckFromWedding.setName("S Perleberger Brücke");
		scenario.getTransitSchedule().addStopFacility(toPerlbruckFromWedding);

		TransitStopFacility toHbfFromPerlbruck = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("toHbfFromPerlbruck", TransitStopFacility.class),
				hbfNode.getCoord(),
				false);
		toHbfFromPerlbruck.setLinkId(linkPerlbruckHbf.getId());
		toHbfFromPerlbruck.setName("S+U Hauptbahnhof");
		scenario.getTransitSchedule().addStopFacility(toHbfFromPerlbruck);

		TransitStopFacility toPotsdamerFromHbf = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("toPotsdamerFromHbf", TransitStopFacility.class),
				potsdamerNode.getCoord(),
				false);
		toPotsdamerFromHbf.setLinkId(linkHbfPotsdamer.getId());
		toPotsdamerFromHbf.setName("S+U Potsdamer Platz");
		scenario.getTransitSchedule().addStopFacility(toPotsdamerFromHbf);

		TransitStopFacility toGleisdreieckFromPotsdamer = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("toGleisdreieckFromPotsdamer", TransitStopFacility.class),
				gleisdreieckNode.getCoord(),
				false);
		toGleisdreieckFromPotsdamer.setLinkId(linkPotsdamerGleisdreieck.getId());
		toGleisdreieckFromPotsdamer.setName("S+U Gleisdreieck");
		scenario.getTransitSchedule().addStopFacility(toGleisdreieckFromPotsdamer);

		TransitStopFacility toYorckFromGleisdreieck = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("toYorckFromGleisdreieck", TransitStopFacility.class),
				yorckNode.getCoord(),
				false);
		toYorckFromGleisdreieck.setLinkId(linkGleisdreieckYorck.getId());
		toYorckFromGleisdreieck.setName("S+U Yorckstraße (Großgörschenstraße)");
		scenario.getTransitSchedule().addStopFacility(toYorckFromGleisdreieck);

		TransitStopFacility toJuliusFromYorck = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("toJuliusFromYorck", TransitStopFacility.class),
				juliusNode.getCoord(),
				false);
		toJuliusFromYorck.setLinkId(linkYorckJulius.getId());
		toJuliusFromYorck.setName("S Julius Leber Brücke");
		scenario.getTransitSchedule().addStopFacility(toJuliusFromYorck);

		TransitStopFacility toSudkreuzFromJulius = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("toSudkreuzFromJulius", TransitStopFacility.class),
				sudkreuzNode.getCoord(),
				false);
		toSudkreuzFromJulius.setLinkId(linkJuliusSudkreuz.getId());
		toSudkreuzFromJulius.setName("S Südkreuz");
		scenario.getTransitSchedule().addStopFacility(toSudkreuzFromJulius);

		//List to Create NetworkRoute
		List<Id<Link>> linkroutes = List.of(
			linkWesthafenWedding.getId(),
			linkWeddingPerlbruck.getId(),
			linkPerlbruckHbf.getId(),
			linkHbfPotsdamer.getId(),
			linkPotsdamerGleisdreieck.getId(),
			linkGleisdreieckYorck.getId(),
			linkYorckJulius.getId()
		);

		// Creating networkRoute
		NetworkRoute networkRoute = RouteUtils.createLinkNetworkRouteImpl(
			linkS21LoopLink.getId(),
			linkroutes,
			linkJuliusSudkreuz.getId()
		);

		double stopTime = 10.0;

		List<TransitRouteStop> stops = new ArrayList<>();
		stops.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			WesthafenonLoop,
			0.0d,
			stopTime));

		double travelTimeWesthafenToWedding = NetworkUtils
			.getEuclideanDistance(
				westhafenNode.getCoord(),
				weddingNode.getCoord())
			/ s21Freespeed + 1;
		stops.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toWeddingFromWesthafen,
			stops.getLast().getDepartureOffset().seconds() + travelTimeWesthafenToWedding,
			stops.getLast().getDepartureOffset().seconds() + travelTimeWesthafenToWedding + stopTime));

		double travelTimeWeddingToPerlbruck = NetworkUtils
			.getEuclideanDistance(weddingNode.getCoord(), perlNode.getCoord()) / s21Freespeed + 1;
		stops.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toPerlbruckFromWedding,
			stops.getLast().getDepartureOffset().seconds() + travelTimeWeddingToPerlbruck,
			stops.getLast().getDepartureOffset().seconds() + travelTimeWeddingToPerlbruck + stopTime));

		double travelTimePerlbruckToHbf = NetworkUtils
			.getEuclideanDistance(
				perlNode.getCoord(),
				hbfNode.getCoord())
			/ s21Freespeed + 1;
		stops.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toHbfFromPerlbruck,
			stops.getLast().getDepartureOffset().seconds() + travelTimePerlbruckToHbf,
			stops.getLast().getDepartureOffset().seconds() + travelTimePerlbruckToHbf + stopTime));

		double travelTimeHbfToPotsdamer = NetworkUtils
			.getEuclideanDistance(
				hbfNode.getCoord(),
				potsdamerNode.getCoord())
			/ s21Freespeed + 1;
		stops.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toPotsdamerFromHbf,
			stops.getLast().getDepartureOffset().seconds() + travelTimeHbfToPotsdamer,
			stops.getLast().getDepartureOffset().seconds() + travelTimeHbfToPotsdamer + stopTime));

		double travelTimePotsdamerToGleisdreieck = NetworkUtils
			.getEuclideanDistance(
				potsdamerNode.getCoord(),
				gleisdreieckNode.getCoord())
			/ s21Freespeed + 1;
		stops.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toGleisdreieckFromPotsdamer,
			stops.getLast().getDepartureOffset().seconds() + travelTimePotsdamerToGleisdreieck,
			stops.getLast().getDepartureOffset().seconds() + travelTimePotsdamerToGleisdreieck + stopTime));

		double travelTimeGleisdreieckToYorck = NetworkUtils
			.getEuclideanDistance(
				gleisdreieckNode.getCoord(),
				yorckNode.getCoord())
			/ s21Freespeed + 1;
		stops.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toYorckFromGleisdreieck,
			stops.getLast().getDepartureOffset().seconds() + travelTimeGleisdreieckToYorck,
			stops.getLast().getDepartureOffset().seconds() + travelTimeGleisdreieckToYorck + stopTime));

		double travelTimeYorckToJulius = NetworkUtils
			.getEuclideanDistance(
				yorckNode.getCoord(),
				juliusNode.getCoord())
			/ s21Freespeed + 1;
		stops.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toJuliusFromYorck,
			stops.getLast().getDepartureOffset().seconds() + travelTimeYorckToJulius,
			stops.getLast().getDepartureOffset().seconds() + travelTimeYorckToJulius + stopTime));

		double travelTimeJuliusToSudkreuz = NetworkUtils
			.getEuclideanDistance(
				juliusNode.getCoord(),
				sudkreuzNode.getCoord())
			/ s21Freespeed + 1;
		stops.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toSudkreuzFromJulius,
			stops.getLast().getDepartureOffset().seconds() + travelTimeJuliusToSudkreuz,
			stops.getLast().getDepartureOffset().seconds() + travelTimeJuliusToSudkreuz + stopTime));

		//Create TransitLine and TransitRoute
		TransitRoute transitRoute = scenario.getTransitSchedule().getFactory()
			.createTransitRoute(
				Id.create("s21", TransitRoute.class),
				networkRoute,
				stops,
				""
			);
		transitRoute.setTransportMode("pt");

		int headway = 5 * 60;
		for (int i = 0; i < 30*60*60 / headway; i++) {
			Departure departure = scenario.getTransitSchedule().getFactory().createDeparture(Id.create("pt_s21_" + i, Departure.class), i * headway);
			scenario.getTransitVehicles().addVehicle(
				scenario.getTransitVehicles().getFactory().createVehicle(
					Id.createVehicleId("pt_s21_" + i),
					scenario.getTransitVehicles().getVehicleTypes().get(Id.create("S-Bahn_veh_type", VehicleType.class))));
			departure.setVehicleId(Id.createVehicleId("pt_s21_" + i));
			transitRoute.addDeparture(departure);
		}
		// Creating the TransitLine
		TransitLine transitLine = scenario.getTransitSchedule().getFactory().createTransitLine(Id.create("S21", TransitLine.class));
		transitLine.setName("S21");
		transitLine.addRoute(transitRoute);
		scenario.getTransitSchedule().addTransitLine(transitLine);

		var root = Path.of("input/v6.4-s21/");
		new NetworkWriter(network).write(root.resolve("network-with-pt.xml.gz").toString());
		new TransitScheduleWriter(scenario.getTransitSchedule()).writeFile(root.resolve("transit-Schedule.xml.gz").toString());
		new MatsimVehicleWriter(scenario.getTransitVehicles()).writeFile(root.resolve("transit-Vehicle.xml.gz").toString());
	}
}
