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



@WebServlet("/GetGeometry")
public class GetGeometry extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
	private String entitiesWithGeometry =  "SELECT ?p ?g WHERE {?p stko:hasName "+StringPrefix.placeHolder+" . ?p stko:hasGeometry ?g}";
    /**
     * @see HttpServlet#HttpServlet()
     */
    public GetGeometry() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		request.setCharacterEncoding("UTF-8");		
		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		String entityURI = request.getParameter("country");
		if(entityURI.equals("")) return;
		entityURI = "\""+entityURI+"\"";
		getEntityDetailInfo(entityURI,out);
	}

	private void getEntityDetailInfo(String entityURI, PrintWriter out) {

		String thisQueryString = entitiesWithGeometry.replace(StringPrefix.placeHolder, entityURI+"@en");
		
		Query query = QueryFactory.create(StringPrefix.queryPrefix+thisQueryString);  
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
			    
			    boolean bodyPropertyFound = false;
			    while(iterator.hasNext())
			    {  
			    	QuerySolution thisInfoRecord = iterator.next();
			    	
			    	JSONObject thisJsonTriple = new JSONObject();
			    	String propertyName = thisInfoRecord.get("?p").asResource().getLocalName();
			    	
			    	
			    	String propertyURI = thisInfoRecord.get("?p").asResource().getURI();	
			    	thisJsonTriple.put("propertyName",propertyName);
			    	thisJsonTriple.put("propertyURI",propertyURI);
			    	
			    	RDFNode objectRdfNode = thisInfoRecord.get("?g");
			    	String objectName = null;
			    	if(objectRdfNode.isLiteral())
			    	{
			    		objectName = objectRdfNode.asLiteral().getString();
			    		if(objectName.startsWith("http://"))			    			
			    			thisJsonTriple.put("valueType",2);
			    		else {
			    			thisJsonTriple.put("valueType",0);
						}
			    	}
			    	else 
			    	{
			    		objectName = objectRdfNode.asResource().getURI();
			    		if(objectName.startsWith("http://data.linkededucation.org/resource/lak"))
			    			thisJsonTriple.put("valueType",1);
			    		else {
			    			thisJsonTriple.put("valueType",2);
						}
			    		
					}
			    	objectName = objectName.replace("(","[");
			    	objectName = objectName.replace(")","]");
			    	objectName = objectName.replace(",","],[");
			    	objectName = objectName.replace(" ",",");
			    	objectName = objectName.replace("MULTIPOLYGON","");
			    	objectName = "["+objectName+"]";
			    	JSONArray r = new JSONArray(objectName);
			    	thisJsonTriple.put("value", r);
			    	tripleArray.put(thisJsonTriple);
			    	
			    }
			    	
			    parentObject.put("triples", tripleArray);
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
