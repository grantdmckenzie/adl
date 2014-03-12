package edu.ucsb.geog;

public class StringPrefix 
{
	public static final String queryPrefix = "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "+
											 "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> "+
											 "prefix xsd: <http://www.w3.org/2001/XMLSchema#> "+
											 "prefix foaf: <http://xmlns.com/foaf/0.1/> "+
											 "prefix dcterms: <http://purl.org/dc/terms/> "+
											 "prefix dc: <http://purl.org/dc/elements/1.1/> "+
											 "prefix ical: <http://www.w3.org/2002/12/cal/ical#> "+
											 "prefix swrc: <http://swrc.ontoware.org/ontology#> "+
											 "prefix bibo: <http://purl.org/ontology/bibo/> "+
											 "prefix swc: <http://data.semanticweb.org/ns/swc/ontology#> "+
											 "prefix led: <http://data.linkededucation.org/ns/linked-education.rdf#> "+
											 "prefix geo: <http://www.w3.org/2003/01/geo/wgs84_pos#>"+
											 "prefix stko: <http://adl-gazetteer.geog.ucsb.edu/ONT/ADL#>";


	
	public static final String serviceEndpoint = "http://adl-gazetteer.geog.ucsb.edu:8081/parliament/sparql"; 
	
	public static final String placeHolder = "VariablePlaceholder";
	
	public static final String dbstring = "jdbc:postgresql://adl-gazetteer.geog.ucsb.edu:5432/gazrepu";
	
	public static final String dbname = "endpoint";
	
	public static final String dbpass = "GAZ..adl";

}