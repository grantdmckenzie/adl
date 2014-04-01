package edu.ucsb.geog;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

import org.apache.naming.factory.ResourceFactory;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Bag;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
// import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class Combined {

	private static final String NULL = null;
	Connection connection = null;
	Statement s = null;
	CallableStatement cs = null;
	
	/**
	 * @param args
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 * @throws NoSuchAlgorithmException 
	 */
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws ClassNotFoundException, SQLException, FileNotFoundException, UnsupportedEncodingException, NoSuchAlgorithmException{
		
			Combined ex = new Combined();
			Class.forName("org.postgresql.Driver");
			ex.connection = DriverManager.getConnection(StringPrefix.dbstring, StringPrefix.dbname, StringPrefix.dbpass);
			ex.s = ex.connection.createStatement();
			
			String xml10pattern = "[^"
                    + "\u0009\r\n"
                    + "\u0020-\uD7FF"
                    + "\uE000-\uFFFD"
                    + "\ud800\udc00-\udbff\udfff"
                    + "]";
			String adlgaz = "http://adl-gazetteer.geog.ucsb.edu/ONT/ADL#";
		    String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
		    String geo = "http://www.opengis.net/ont/geosparql#";
		    String wGS84String = "<http://www.opengis.net/def/crs/OGC/1.3/CRS84>";
		    String wKTLiteralType ="http://www.opengis.net/ont/sf#wktLiteral";
		    
			for(int z=0;z<46;z++) {
				int offset = z*100000;
			    String sqlquery="SELECT * FROM vw_combined_related ORDER BY feature_id LIMIT 100000 OFFSET "+offset+";";
			    
			    ResultSet results= ex.s.executeQuery(sqlquery);
			    Model model = ModelFactory.createDefaultModel();
			    
			    model.setNsPrefix("adlgaz", "http://adl-gazetteer.geog.ucsb.edu/ONT/ADL#");
			    Property hasPrimaryName = model.createProperty(adlgaz+	"hasPrimaryName");
			    Property hasAlternateName = model.createProperty(adlgaz+	"hasAlternateName");
			    Property hasID = model.createProperty(adlgaz+	"hasID");
			    Property hasGeometry = model.createProperty(geo+	"hasGeometry");
			    Property hasDescription = model.createProperty(adlgaz+	"hasDescription");
			    Property onPlanet = model.createProperty(adlgaz+	"onPlanet");
			    Property hasEntryDate = model.createProperty(adlgaz+	"hasEntryDate");
			    Property hasModifiedDate = model.createProperty(adlgaz+	"hasModifiedDate");
			    Property hasFeatureType = model.createProperty(rdf+ "type");
			    Property hasSchema = model.createProperty(adlgaz+	"hasSchema");
			    Property relatedItem = model.createProperty(adlgaz+	"relatedItem");
			    Property relatedFeature = model.createProperty(adlgaz+	"relatedFeature");
			    Property asWKT = model.createProperty(geo+	"asWKT");
			    Property hasWKT = model.createProperty(adlgaz+ "hasWKT");
			    Resource place = null;
			    String primaryname = null;
			    while (results.next()) {
			    	
			       boolean match = false;
			       String[] names = results.getString("name").replace("{", "").replace("}","").split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
			       String[] langs = results.getString("lang").replace("{", "").replace("}","").split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
			       
			       for(int i=0;i<langs.length;i++){
			    	   if(langs[i].trim().equals("eng")) {
			    		   primaryname = names[i].replace("\"", "");
			    		   match = true;
			    	   }
			       }
			       
			       if (!match) {
			    	   primaryname = names[0];
			       }
			       
			       Resource geoGeometry = model.createResource(geo+"Geometry");
			       String geomURI = "http://adl-gazetteer.geog.ucsb.edu/ADL/"+UUID.randomUUID().toString();
			       Resource extentGeometry = model.createResource(geomURI);
			       extentGeometry.addProperty(RDF.type, geoGeometry);
			       String encodedgeom = results.getString("encoded_geometry").trim();
			       // Check to see if this is a POLYGON or a POINT
			       String polygonLiteralString = wGS84String + encodedgeom;
			       if (encodedgeom.substring(0,7).equals("POLYGON"))
			    	   polygonLiteralString = wGS84String + encodedgeom.substring(0,encodedgeom.lastIndexOf(",")) + "))";	// remove the extra comma after the last coordinate
			
			       Literal literal = model.createTypedLiteral(polygonLiteralString,wKTLiteralType);
			       extentGeometry.addProperty(asWKT, literal);
			       
			       String placeURI = "http://adl-gazetteer.geog.ucsb.edu/ADL/"+primaryname.replace("\"", "").replaceAll("[^a-zA-Z ]", "").toLowerCase().replace(" ", "_");
			       
			       place = model.createResource(placeURI);
			       place.addProperty(hasPrimaryName , model.createLiteral(primaryname.replace("\"", ""), "en"));
			       for(int i=0;i<langs.length;i++){
			    	   if(!langs[i].trim().equals("eng")) {
			    		   place.addProperty(hasAlternateName , model.createLiteral(names[i].replaceAll(xml10pattern, "").replace("\"", ""), (!langs[i].trim().equals("NULL") ? langs[i].trim() : "")));
			    	   }
			       }
			       if (results.getString("related_features") != null) {
			    	   if(results.getString("related_ids").indexOf(",") != -1) {
				    	   String[] related_features = results.getString("related_features").replace("{", "").replace("}","").split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
				    	   String[] related_ids = results.getString("related_ids").replace("{", "").replace("}","").split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
				    	   
				    	   for(int i=0;i<related_ids.length;i++){
				    		   if(!related_ids[i].equals("NULL")) {
				    			   place.addProperty(relatedFeature, "http://adl-gazetteer.geog.ucsb.edu/ADL/"+related_features[i]); 
				    		   } else {
				    			   place.addProperty(relatedItem, "http://adl-gazetteer.geog.ucsb.edu/ADL/"+related_features[i]);
				    		   }
				    	   }
			    	   } else {
			    		   String related_features = results.getString("related_features").replace("{", "").replace("}","");
			    		   String related_ids = results.getString("related_ids").replace("{", "").replace("}","");
			    		   if(!related_ids.equals("NULL")) {
			    			   place.addProperty(relatedFeature, "http://adl-gazetteer.geog.ucsb.edu/ADL/"+related_features); 
			    		   } else {
			    			   place.addProperty(relatedItem, "http://adl-gazetteer.geog.ucsb.edu/ADL/"+related_features);
			    			   
			    		   }
			    	   }
			       }       
		           place.addProperty(onPlanet , results.getString("planet")); 
		           place.addProperty(hasDescription , ((results.getString("short_description")==null) ? "" : results.getString("short_description").replaceAll(xml10pattern, ""))); 
		           place.addProperty(hasSchema , ((results.getString("scheme_name")==null) ? "" : results.getString("scheme_name")));
		           place.addProperty(hasEntryDate , model.createTypedLiteral(results.getString("entry_date"), XSDDatatype.XSDdate));
		           place.addProperty(hasModifiedDate , model.createTypedLiteral(results.getString("modification_date"), XSDDatatype.XSDdate));
		           place.addProperty(hasID , results.getString("feature_id"));
		           Resource featureType = model.createResource(adlgaz+results.getString("term").toLowerCase().replace(" ", "_"));
	               place.addProperty(hasFeatureType , featureType);
	               place.addProperty(hasGeometry, extentGeometry);
	               place.addProperty(hasWKT, encodedgeom);
		           
				}	
		
		       FileOutputStream out = new FileOutputStream("data/"+offset+".nt");
		       model.write(out,"N-TRIPLES");
			   System.out.print("Offset: "+offset+"\n");
			}
			
	}
			
}