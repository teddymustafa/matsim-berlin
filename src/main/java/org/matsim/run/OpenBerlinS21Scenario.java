package org.matsim.run;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.Scenario;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.network.Link;
import org.matsim.application.MATSimApplication;
import org.matsim.core.config.Config;
import org.matsim.core.config.ConfigUtils;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.population.routes.NetworkRoute;
import org.matsim.core.population.routes.RouteUtils;
import org.matsim.pt.transitSchedule.api.*;
import org.matsim.simwrapper.SimWrapperConfigGroup;
import org.matsim.vehicles.VehicleType;

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

		SimWrapperConfigGroup sw = ConfigUtils.addOrGetModule(config, SimWrapperConfigGroup.class);
		sw.defaultDashboards = SimWrapperConfigGroup.Mode.disabled;
		config.controller().setLastIteration(0);
		config.controller().setOutputDirectory("output-s21-third");

		return config;
	}

	@Override
	protected void prepareScenario(Scenario scenario) {
		super.prepareScenario(scenario);

		// Determination of Fixed Values
		double firstDep = 4.5*3600; // 04:30 AM
		double lastDep = 25.5 * 3600; // 01:30 AM next day
		int i = 0; // for schedule
		double s21Freespeed = 22.22; // 80 kph
		double stopTime = 10.0; // Stops for 10 s (doors opening)
		int headway = 5 * 60; // Every 5 Minutes

		// Loading the Network Elements
		var network = scenario.getNetwork();

		// NODES -> Commonly shared
		// Creating 2 Nodes for S21
		var perlbruck = network.getFactory().createNode(Id.createNodeId("pt_perlbruck_suburbanRailway"), new Coord(795540.18,5829632.68));
		var gleisdreieck = network.getFactory().createNode(Id.createNodeId("pt_gleisdreieck_suburbanRailway"), new Coord(796845.48,5825610.95));

		// Adding 2 new Nodes for S21
		List<Node> nodes= List.of(perlbruck, gleisdreieck);
		for(Node node : nodes) {
			network.addNode(node);
		}

		// Load new and existing nodes necessary for creation of S21 line
		Node westhafenNode = network.getNodes().get(Id.createNodeId("pt_473821_SuburbanRailway"));
		Node weddingNode = network.getNodes().get(Id.createNodeId("pt_4832_SuburbanRailway"));
		Node perlNode = network.getNodes().get(Id.createNodeId("pt_perlbruck_suburbanRailway"));
		Node hbfNode = network.getNodes().get(Id.createNodeId("pt_359974_SuburbanRailway"));
		Node potsdamerNode = network.getNodes().get(Id.createNodeId("pt_415349_SuburbanRailway"));
		Node gleisdreieckNode = network.getNodes().get(Id.createNodeId("pt_gleisdreieck_suburbanRailway"));
		Node yorckNode = network.getNodes().get(Id.createNodeId("pt_415374_SuburbanRailway"));
		Node juliusNode = network.getNodes().get(Id.createNodeId("pt_175655_SuburbanRailway"));
		Node sudkreuzNode = network.getNodes().get(Id.createNodeId("pt_176775_SuburbanRailway"));

		// In this Order: Links, Links List, Stations List, its Implementation to Network Route,

		// LOOP LINK -> Starting Link
		Link linkS21LoopLink = network.getFactory().createLink(
			Id.createLinkId("linkS21LoopLink_s21_SuburbanRailway"),
			westhafenNode,
			westhafenNode
		);

		Link linkWesthafenWedding = network.getFactory().createLink(
			Id.createLinkId("WesthafenWedding_s21_SuburbanRailway"),
			westhafenNode,
			weddingNode
		);

		Link linkWeddingPerlbruck = network.getFactory().createLink(
			Id.createLinkId("WeddingPerlbruck_s21_SuburbanRailway"),
			weddingNode,
			perlNode
		);

		Link linkPerlbruckHbf = network.getFactory().createLink(
			Id.createLinkId("PerlbruckHbf_s21_SuburbanRailway"),
			perlNode,
			hbfNode
		);

		Link linkHbfPotsdamer = network.getFactory().createLink(
			Id.createLinkId("HbfPotsdamer_s21_SuburbanRailway"),
			hbfNode,
			potsdamerNode
		);

		Link linkPotsdamerGleisdreieck = network.getFactory().createLink(
			Id.createLinkId("PotsdamerGleisdreieck_s21_SuburbanRailway"),
			potsdamerNode,
			gleisdreieckNode
		);

		Link linkGleisdreieckYorck = network.getFactory().createLink(
			Id.createLinkId("GleisdreieckYorck_s21_SuburbanRailway"),
			gleisdreieckNode,
			yorckNode
		);

		Link linkYorckJulius = network.getFactory().createLink(
			Id.createLinkId("YorckJuli_s21_SuburbanRailway"),
			yorckNode,
			juliusNode
		);

		Link linkJuliusSudkreuz = network.getFactory().createLink(
			Id.createLinkId("JuliSudkreuz_s21_SuburbanRailway"),
			juliusNode,
			sudkreuzNode
		);

		// Add Links for S21 Forward Direction
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

		// Setting Length, Capacity, Allowed Mode and conclusively Adding the Links
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

		// ================
		// TRANSIT FACILITY
		// ================
		// Creating Stops Forward Direction
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

		// Implementing Links into the Network Route FORWARD DIRECTION
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

		// ================
		// TRANSIT SCHEDULE
		// ================
		// Transit Route Stops Forward Direction
		// Travel Time is calculated with BeeLine Distance as an assumption
		List<TransitRouteStop> stops = new ArrayList<>();
		stops.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			WesthafenonLoop,
			0.0d,
			stopTime));

		double travelTimeWesthafenToWedding = NetworkUtils
			// Calculation of Travel Time with BeeLine Distance
			// t = s_euclidean/v
			.getEuclideanDistance(
				westhafenNode.getCoord(),
				weddingNode.getCoord())
			/ s21Freespeed + 1;
		// Add Entry to stops
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

		stops.forEach(s -> s.setAwaitDepartureTime(true));

		// Create TransitRoute
		// TransitRoute (name of Route, route, list of stops, mode)
		//
		TransitRoute transitRoute = scenario.getTransitSchedule().getFactory()
			.createTransitRoute(
				Id.create("s21", TransitRoute.class),
				networkRoute,
				stops,
				"pt"
			);

		// Service Frequency
		for (double t = firstDep; t <= lastDep; t += headway) {
			Departure departure = scenario.getTransitSchedule().getFactory().createDeparture(Id.create("pt_s21_" + i, Departure.class), t);
			scenario.getTransitVehicles().addVehicle(
				scenario.getTransitVehicles().getFactory().createVehicle(
					Id.createVehicleId("pt_s21_" + i),
					scenario.getTransitVehicles().getVehicleTypes().get(Id.create("S-Bahn_veh_type", VehicleType.class))));
			departure.setVehicleId(Id.createVehicleId("pt_s21_" + i));
			transitRoute.addDeparture(departure);
			i++;
		}

		// THE SAME THING REVERSED:
		Link linkS21LoopLinkRev = network.getFactory().createLink(
			Id.createLinkId("linkS21LoopLinkRev_s21_SuburbanRailway"),
			sudkreuzNode,
			sudkreuzNode
		);
		Link linkSudkreuzJulius = network.getFactory().createLink(
			Id.createLinkId("SudkreuzJuli_s21_SuburbanRailway"),
			sudkreuzNode,
			juliusNode
		);
		Link linkJuliusYorck = network.getFactory().createLink(
			Id.createLinkId("JuliYorck_s21_SuburbanRailway"),
			juliusNode,
			yorckNode
		);
		Link linkYorckGleisdreieck = network.getFactory().createLink(
			Id.createLinkId("YorckGleisdreieck_s21_SuburbanRailway"),
			yorckNode,
			gleisdreieckNode
		);
		Link linkGleisdreieckPotsdamer = network.getFactory().createLink(
			Id.createLinkId("GleisdreieckPotsdamer_s21_SuburbanRailway"),
			gleisdreieckNode,
			potsdamerNode
		);
		Link linkPotsdamerHbf = network.getFactory().createLink(
			Id.createLinkId("PotsdamerHbf_s21_SuburbanRailway"),
			potsdamerNode,
			hbfNode
		);
		Link linkHbfPerlbruck = network.getFactory().createLink(
			Id.createLinkId("HbfPerlbruck_s21_SuburbanRailway"),
			hbfNode,
			perlNode
		);
		Link linkPerlbruckWedding = network.getFactory().createLink(
			Id.createLinkId("PerlbruckWedding_s21_SuburbanRailway"),
			perlNode,
			weddingNode
		);
		Link linkWeddingWesthafen = network.getFactory().createLink(
			Id.createLinkId("WeddingWesthafen_s21_SuburbanRailway"),
			weddingNode,
			westhafenNode
		);

		// Add Links for S21 (reverse)
		List<Link> linksRev = List.of(
			linkS21LoopLinkRev,
			linkSudkreuzJulius,
			linkJuliusYorck,
			linkYorckGleisdreieck,
			linkGleisdreieckPotsdamer,
			linkPotsdamerHbf,
			linkHbfPerlbruck,
			linkPerlbruckWedding,
			linkWeddingWesthafen
		);
		for(Link link : linksRev) {
			if(link.equals(linkS21LoopLinkRev)){
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

		// STOPS REVERSED
		TransitStopFacility SudkreuzonLoop = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("SudkreuzonLoop", TransitStopFacility.class),
				sudkreuzNode.getCoord(),
				false);
		SudkreuzonLoop.setLinkId(linkS21LoopLinkRev.getId());
		SudkreuzonLoop.setName("S Südkreuz");
		scenario.getTransitSchedule().addStopFacility(SudkreuzonLoop);

		TransitStopFacility toJuliusFromSudkreuz = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("toJuliusFromSudkreuz", TransitStopFacility.class),
				juliusNode.getCoord(),
				false);
		toJuliusFromSudkreuz.setLinkId(linkSudkreuzJulius.getId());
		toJuliusFromSudkreuz.setName("S Julius Leber Brücke");
		scenario.getTransitSchedule().addStopFacility(toJuliusFromSudkreuz);

		TransitStopFacility toYorckFromJulius = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("toYorckFromJulius", TransitStopFacility.class),
				yorckNode.getCoord(),
				false);
		toYorckFromJulius.setLinkId(linkJuliusYorck.getId());
		toYorckFromJulius.setName("S+U Yorckstraße (Großgörschenstraße)");
		scenario.getTransitSchedule().addStopFacility(toYorckFromJulius);

		TransitStopFacility toGleisdreieckFromYorck = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("toGleisdreieckFromYorck", TransitStopFacility.class),
				gleisdreieckNode.getCoord(),
				false);
		toGleisdreieckFromYorck.setLinkId(linkYorckGleisdreieck.getId());
		toGleisdreieckFromYorck.setName("S+U Gleisdreieck");
		scenario.getTransitSchedule().addStopFacility(toGleisdreieckFromYorck);

		TransitStopFacility toPotsdamerFromGleisdreieck = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("toPotsdamerFromGleisdreieck", TransitStopFacility.class),
				potsdamerNode.getCoord(),
				false);
		toPotsdamerFromGleisdreieck.setLinkId(linkGleisdreieckPotsdamer.getId());
		toPotsdamerFromGleisdreieck.setName("S+U Potsdamer Platz");
		scenario.getTransitSchedule().addStopFacility(toPotsdamerFromGleisdreieck);

		TransitStopFacility toHbfFromPotsdamer = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("toHbfFromPotsdamer", TransitStopFacility.class),
				hbfNode.getCoord(),
				false);
		toHbfFromPotsdamer.setLinkId(linkPotsdamerHbf.getId());
		toHbfFromPotsdamer.setName("S+U Hauptbahnhof");
		scenario.getTransitSchedule().addStopFacility(toHbfFromPotsdamer);

		TransitStopFacility toPerlbruckFromHbf = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("toPerlbruckFromHbf", TransitStopFacility.class),
				perlNode.getCoord(),
				false);
		toPerlbruckFromHbf.setLinkId(linkHbfPerlbruck.getId());
		toPerlbruckFromHbf.setName("S Perleberger Brücke");
		scenario.getTransitSchedule().addStopFacility(toPerlbruckFromHbf);

		TransitStopFacility toWeddingFromPerlbruck = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("toWeddingFromPerlbruck", TransitStopFacility.class),
				weddingNode.getCoord(),
				false);
		toWeddingFromPerlbruck.setLinkId(linkPerlbruckWedding.getId());
		toWeddingFromPerlbruck.setName("S+U Wedding");
		scenario.getTransitSchedule().addStopFacility(toWeddingFromPerlbruck);

		TransitStopFacility toWesthafenFromWedding = scenario.getTransitSchedule().getFactory()
			.createTransitStopFacility(
				Id.create("toWesthafenFromWedding", TransitStopFacility.class),
				westhafenNode.getCoord(),
				false);
		toWesthafenFromWedding.setLinkId(linkWeddingWesthafen.getId());
		toWesthafenFromWedding.setName("S+U Westhafen");
		scenario.getTransitSchedule().addStopFacility(toWesthafenFromWedding);


		List<Id<Link>> linkroutesRev = List.of(
			linkSudkreuzJulius.getId(),
			linkJuliusYorck.getId(),
			linkYorckGleisdreieck.getId(),
			linkGleisdreieckPotsdamer.getId(),
			linkPotsdamerHbf.getId(),
			linkHbfPerlbruck.getId(),
			linkPerlbruckWedding.getId()
		);

		// networkRoute REVERSED
		NetworkRoute networkRouteRev = RouteUtils.createLinkNetworkRouteImpl(
			linkS21LoopLinkRev.getId(),
			linkroutesRev,
			linkWeddingWesthafen.getId()
		);

		List<TransitRouteStop> stopsRev = new ArrayList<>();
		stopsRev.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			SudkreuzonLoop,
			0.0d,
			stopTime));

		double travelTimeSudkreuzToJulius = NetworkUtils
			.getEuclideanDistance(
				sudkreuzNode.getCoord(),
				juliusNode.getCoord())
			/ s21Freespeed + 1;
		stopsRev.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toJuliusFromSudkreuz,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimeSudkreuzToJulius,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimeSudkreuzToJulius + stopTime));

		double travelTimeJuliusToYorck = NetworkUtils
			.getEuclideanDistance(
				juliusNode.getCoord(),
				yorckNode.getCoord())
			/ s21Freespeed + 1;
		stopsRev.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toYorckFromJulius,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimeJuliusToYorck,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimeJuliusToYorck + stopTime));

		double travelTimeYorckToGleisdreieck = NetworkUtils
			.getEuclideanDistance(
				yorckNode.getCoord(),
				gleisdreieckNode.getCoord())
			/ s21Freespeed + 1;
		stopsRev.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toGleisdreieckFromYorck,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimeYorckToGleisdreieck,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimeYorckToGleisdreieck + stopTime));

		double travelTimeGleisdreieckToPotsdamer = NetworkUtils
			.getEuclideanDistance(
				gleisdreieckNode.getCoord(),
				potsdamerNode.getCoord())
			/ s21Freespeed + 1;
		stopsRev.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toPotsdamerFromGleisdreieck,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimeGleisdreieckToPotsdamer,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimeGleisdreieckToPotsdamer + stopTime));

		double travelTimePotsdamerToHbf = NetworkUtils
			.getEuclideanDistance(
				potsdamerNode.getCoord(),
				hbfNode.getCoord())
			/ s21Freespeed + 1;
		stopsRev.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toHbfFromPotsdamer,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimePotsdamerToHbf,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimePotsdamerToHbf + stopTime));

		double travelTimeHbfToPerlbruck = NetworkUtils
			.getEuclideanDistance(
				hbfNode.getCoord(),
				perlNode.getCoord())
			/ s21Freespeed + 1;
		stopsRev.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toPerlbruckFromHbf,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimeHbfToPerlbruck,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimeHbfToPerlbruck + stopTime));

		double travelTimePerlbruckToWedding = NetworkUtils
			.getEuclideanDistance(
				perlNode.getCoord(),
				weddingNode.getCoord())
			/ s21Freespeed + 1;
		stopsRev.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toWeddingFromPerlbruck,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimePerlbruckToWedding,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimePerlbruckToWedding + stopTime));

		double travelTimeWeddingToWesthafen = NetworkUtils
			.getEuclideanDistance(
				weddingNode.getCoord(),
				westhafenNode.getCoord())
			/ s21Freespeed + 1;
		stopsRev.add(scenario.getTransitSchedule().getFactory().createTransitRouteStop(
			toWesthafenFromWedding,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimeWeddingToWesthafen,
			stopsRev.getLast().getDepartureOffset().seconds() + travelTimeWeddingToWesthafen + stopTime));

		stops.forEach(s -> s.setAwaitDepartureTime(true));

		// Reversed
		TransitRoute transitRouteRev = scenario.getTransitSchedule().getFactory()
			.createTransitRoute(
				Id.create("s21_rev", TransitRoute.class),
				networkRouteRev,
				stopsRev,
				"pt"
			);

		// ── REVERSE departures (ADD THIS — note: pt_s21_rev_ and transitRouteRev) ──
		for (double t = firstDep; t <= lastDep; t += headway) {
			Departure departure = scenario.getTransitSchedule().getFactory()
				.createDeparture(Id.create("pt_s21_rev_" + i, Departure.class), t);
			scenario.getTransitVehicles().addVehicle(
				scenario.getTransitVehicles().getFactory().createVehicle(
					Id.createVehicleId("pt_s21_rev_" + i),
					scenario.getTransitVehicles().getVehicleTypes().get(Id.create("S-Bahn_veh_type", VehicleType.class))));
			departure.setVehicleId(Id.createVehicleId("pt_s21_rev_" + i));
			transitRouteRev.addDeparture(departure);
			i++;
		}

		// Creating the TransitLine
		TransitLine transitLine = scenario.getTransitSchedule().getFactory().createTransitLine(Id.create("S21", TransitLine.class));
		transitLine.setName("S21");
		transitLine.addRoute(transitRoute);
		transitLine.addRoute(transitRouteRev);
		scenario.getTransitSchedule().addTransitLine(transitLine);
	}
}
