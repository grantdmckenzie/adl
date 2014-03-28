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
import com.hp.hpl.jena.rdf.model.RDFNode;



@WebServlet("/GetFeatureClasses")
public class GetFeatureClasses extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
			//"SELECT ?p ?g WHERE {?p stko:hasName "+StringPrefix.placeHolder+" . ?p stko:hasGeometry ?g}";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetFeatureClasses() {
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
		
		String tier = request.getParameter("tier");
		getEntityDetailInfo(tier, out);
	}

	private void getEntityDetailInfo(String tier, PrintWriter out) {

		//select distinct ?d WHERE {?a rdfs:subClassOf ?b . ?b rdfs:subClassOf ?c . ?c rdfs:subClassOf ?d }
		Query query = null;
		if(tier.equals("1")) {
			query = QueryFactory.create(StringPrefix.queryPrefix+"select distinct ?c ?d WHERE {?x rdfs:subClassOf ?a . ?a rdfs:subClassOf ?b . ?b rdfs:subClassOf ?c . ?c rdfs:subClassOf ?d}"); 
		} else if(tier.equals("2")) {
			query = QueryFactory.create(StringPrefix.queryPrefix+"select distinct ?c ?d WHERE {?a rdfs:subClassOf ?b . ?b rdfs:subClassOf ?c . ?c rdfs:subClassOf ?d}"); 
		} else if(tier.equals("3")) {
			query = QueryFactory.create(StringPrefix.queryPrefix+"select distinct ?c ?d WHERE {?b rdfs:subClassOf ?c . ?c rdfs:subClassOf ?d}"); 
		} else if(tier.equals("4")) {
			query = QueryFactory.create(StringPrefix.queryPrefix+"select distinct ?c ?d WHERE {?c rdfs:subClassOf ?d}"); 
		}
		
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
			    	String propertyName = thisInfoRecord.get("?c").asResource().getLocalName();
			    	String parentName = thisInfoRecord.get("?d").asResource().getLocalName();
			    	
			    	String propertyURI = thisInfoRecord.get("?c").asResource().getURI();
			    	String parentURI = thisInfoRecord.get("?d").asResource().getURI();
			    	
			    	thisJsonTriple.put("e",propertyName);
			    	thisJsonTriple.put("uri",propertyURI);
			    	thisJsonTriple.put("p",parentName);
			    	thisJsonTriple.put("puri",parentURI);
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
