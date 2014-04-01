package edu.ucsb.geog;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONArray;
import org.json.JSONObject;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;	
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;



@WebServlet("/SearchMain")
public class SearchMain extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
			//"SELECT ?p ?g WHERE {?p stko:hasName "+StringPrefix.placeHolder+" . ?p stko:hasGeometry ?g}";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SearchMain() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		request.setCharacterEncoding("UTF-8");		
		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		String searchTerm = "";
        String searchFeatures = "";
        String searchGeometry = "";
        if (request.getParameterMap().containsKey("q")) {
        	searchTerm = request.getParameter("q");
        }
        if (request.getParameterMap().containsKey("ft")) {
        	searchFeatures = request.getParameter("ft");
        }
        if (request.getParameterMap().containsKey("g")) {
        	searchGeometry = request.getParameter("g");
        }
        //if (searchTerm.length() > 2) {
        	getEntityDetailInfo(searchTerm, searchFeatures, searchGeometry, out);
        //} else {
        	//out.print("Please supply more than 2 characters");
       // }
	}

	private void getEntityDetailInfo(String searchTerm, String searchFeatures, String searchGeometry, PrintWriter out) {

		String q = "";
		if (searchFeatures.length() < 1 && searchGeometry.length() < 1) {
			q = "SELECT DISTINCT ?a (str(?b) AS ?name) ?c WHERE {?a stko:hasPrimaryName ?b . ?a stko:hasAlternateName ?d . ?a rdf:type ?c . FILTER( regex(str(?b), \""+searchTerm+"\") || regex(str(?d), \""+searchTerm+"\") ) }"; 
		} else if (searchFeatures.length() < 1) {
			q = "SELECT DISTINCT ?a (str(?b) AS ?name) ?c WHERE { ?a rdf:type ?c . ?a stko:hasAlternateName ?d . ?a stko:hasPrimaryName ?b . ?a geo:hasGeometry ?bGeom . ?bGeom geo:asWKT ?bWKT . FILTER (geof:sfWithin(?bWKT, \""+searchGeometry+"\"^^sf:wktLiteral)) . FILTER( regex(str(?b), \""+searchTerm+"\") || regex(str(?d), \""+searchTerm+"\") )}";
		} else if (searchGeometry.length() > 1){
			q = "SELECT DISTINCT ?a (str(?b) AS ?name) ?c WHERE { ?a rdf:type ?c . ";
			if(searchFeatures.indexOf(",") != -1) {
				String[] features = searchFeatures.split(",");
				for(int i=0;i<features.length;i++) {
					q += "{ ?a rdf:type <http://adl-gazetteer.geog.ucsb.edu/ONT/ADL#"+features[i]+"> }";
					if (i < features.length - 1) {
						q += " UNION ";
					}
				}
			} else {
				q += "?a rdf:type <http://adl-gazetteer.geog.ucsb.edu/ONT/ADL#"+searchFeatures+">";
			}
			q += " . ";
			q += "?a stko:hasAlternateName ?d . ?a stko:hasPrimaryName ?b . ?a geo:hasGeometry ?bGeom . ?bGeom geo:asWKT ?bWKT . ";
			q += "FILTER (geof:sfWithin(?bWKT, \""+searchGeometry+"\"^^sf:wktLiteral)) . ";
			q += "FILTER( regex(str(?b), \""+searchTerm+"\") || regex(str(?d), \""+searchTerm+"\") )}";
		} else {
			q = "SELECT DISTINCT ?a (str(?b) AS ?name) ?c WHERE { ?a rdf:type ?c . ";
			if(searchFeatures.indexOf(",") != -1) {
				String[] features = searchFeatures.split(",");
				for(int i=0;i<features.length;i++) {
					q += "{ ?a rdf:type <http://adl-gazetteer.geog.ucsb.edu/ONT/ADL#"+features[i]+"> }";
					if (i < features.length - 1) {
						q += " UNION ";
					}
				}
			} else {
				q += "?a rdf:type <http://adl-gazetteer.geog.ucsb.edu/ONT/ADL#"+searchFeatures+">";
			}
			q += " . ";
			q += "?a stko:hasAlternateName ?d . ?a stko:hasPrimaryName ?b . ";
			q += "FILTER( regex(str(?b), \""+searchTerm+"\") || regex(str(?d), \""+searchTerm+"\") )}";
		}
		
		Query query = QueryFactory.create(StringPrefix.queryPrefix+q);
		QueryExecution qe = QueryExecutionFactory.sparqlService(StringPrefix.serviceEndpoint,query);
		
		try 
		{
			ResultSet rs = qe.execSelect();
			if ( rs.hasNext() ) 
			{
			    JSONObject parentObject = new JSONObject();
			    JSONArray tripleArray = new JSONArray();
			    	
			    List<QuerySolution> resultList = ResultSetFormatter.toList(rs);
			    Iterator<QuerySolution> iterator = resultList.iterator();
			    
			    while(iterator.hasNext())
			    {  
			    	QuerySolution thisInfoRecord = iterator.next();
			    	
			    	JSONObject thisJsonTriple = new JSONObject();
			    	String displayName = thisInfoRecord.get("?name").asLiteral().getString();
			    	String uri = thisInfoRecord.get("?a").asResource().getURI();
			    	String featureType = thisInfoRecord.get("?c").toString();
			    	String[] featureParts = featureType.split("#");
			    	
			    	thisJsonTriple.put("name",displayName);
			    	thisJsonTriple.put("uri",uri);
			    	thisJsonTriple.put("ft",featureParts[1]);
			    	tripleArray.put(thisJsonTriple);
			    }
			    	
			    parentObject.put("c", tripleArray);
			    out.print(parentObject.toString());
			}
		} 
		catch(Exception e) { 
			System.out.println(e.getMessage());
		}
		finally {
			qe.close();
		}	
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

}
