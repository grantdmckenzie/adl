package edu.ucsb.geog;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
// import com.hp.hpl.jena.rdf.model.StmtIterator;

public class Combined {

	private static final String NULL = null;
	Connection connection = null;
	Statement s = null;
	CallableStatement cs = null;
	
	/**
	 * @param args
	 * @throws UnsupportedEncodingException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, FileNotFoundException, UnsupportedEncodingException{
		
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
			
			for(int z=0;z<46;z++) {
				int offset = z*100000;
			    String sqlquery="SELECT * FROM tbl_combined_agg ORDER BY feature_id LIMIT 100000 OFFSET "+offset+";";
			    
			    ResultSet results= ex.s.executeQuery(sqlquery);
			    Model model = ModelFactory.createDefaultModel();
			    String adlgaz = "http://adl-gazetteer.geog.ucsb.edu/ONT/ADL#";
			    String rdf = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
			    String muenster = "http://ifgi.uni-muenster.de/simcat/ontology/ftt#";
			    model.setNsPrefix("adlgaz", "http://adl-gazetteer.geog.ucsb.edu/ONT/ADL#");
			    Property hasPrimaryName = model.createProperty(adlgaz+	"hasPrimaryName");
			    Property hasAlternateName = model.createProperty(adlgaz+	"hasAlternateName");
			    Property hasID = model.createProperty(adlgaz+	"hasID");
			    Property hasExtent = model.createProperty(adlgaz+	"hasExtent");
			    Property hasDescription = model.createProperty(adlgaz+	"hasDescription");
			    Property onPlanet = model.createProperty(adlgaz+	"onPlanet");
			    Property hasEntryDate = model.createProperty(adlgaz+	"hasEntryDate");
			    Property hasModifiedDate = model.createProperty(adlgaz+	"hasModifiedDate");
			    Property hasFeatureType = model.createProperty(rdf+ "type");
			    Property hasSchema = model.createProperty(adlgaz+	"hasSchema");
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
			       String placeURI = "http://adl-gazetteer.geog.ucsb.edu/ADL/"+primaryname.replace("\"", "").replaceAll("[^a-zA-Z ]", "").toLowerCase().replace(" ", "_");
			       
			       place = model.createResource(placeURI);
			       place.addProperty(hasPrimaryName , model.createLiteral(primaryname.replace("\"", ""), "en"));
			       for(int i=0;i<langs.length;i++){
			    	   if(!langs[i].trim().equals("eng")) {
			    		   place.addProperty(hasAlternateName , model.createLiteral(names[i].replaceAll(xml10pattern, "").replace("\"", ""), (!langs[i].trim().equals("NULL") ? langs[i].trim() : "")));
			    	   }
			       }
		           place.addProperty(onPlanet , results.getString("planet")); 
		           place.addProperty(hasDescription , ((results.getString("short_description")==null) ? "" : results.getString("short_description").replaceAll(xml10pattern, ""))); 
		           place.addProperty(hasSchema , ((results.getString("scheme_name")==null) ? "" : results.getString("scheme_name")));
		           place.addProperty(hasExtent , results.getString("encoded_geometry").trim()); 
		           place.addProperty(hasEntryDate , model.createTypedLiteral(results.getString("entry_date"), XSDDatatype.XSDdate));
		           place.addProperty(hasModifiedDate , model.createTypedLiteral(results.getString("modification_date"), XSDDatatype.XSDdate));
		           place.addProperty(hasID , results.getString("feature_id"));
		           place.addProperty(hasFeatureType , muenster+results.getString("term").toLowerCase().replace(" ", "_"));
		    	  
				}	
			    // StmtIterator iter = model.listStatements();
			    if (offset > 3200000) {
			    	FileOutputStream out = new FileOutputStream("data/"+offset+".nt");
					model.write(out,"N-TRIPLES");
					System.out.print("Offset: "+offset+"\n");
			    } else {
			       FileOutputStream out = new FileOutputStream("data/"+offset+".xml");
				   //model.write(out,"N-TRIPLES");
			       model.write(out,"RDF/XML-ABBREV");
				   System.out.print("Offset: "+offset+"\n");
			    }
			}
			
	}
			
}