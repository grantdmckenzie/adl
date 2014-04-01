var map = L.map('map').setView([20, -10], 3);
	//map.addLayer(new L.StamenTileLayer("watercolor"));

L.tileLayer('http://{s}.tile.cloudmade.com/dd98a06a415d402f9fdcbc577bc2e30b/96931/256/{z}/{x}/{y}.png?token=410c6ba8f3d1448cb9a3582ebe266355', {
	maxZoom: 19,
}).addTo(map);

var country = null;	
var _tier = 1;
var features = {};
var jstreedata = [];
var marker = null;
var selectedjstree = [];

$(function () { 
	
	getFeatureClasses(1);
	$('#jstree_demo_div').on("changed.jstree", function (e, data) {
	  // console.log(data.selected);
	  selectedjstree = data.selected;
	});
	$('#uxSearchExtent').on("click", function() {
		$(this).toggleClass("on");
	});
	
	String.prototype.capitalize = function() {
	    return this.charAt(0).toUpperCase() + this.slice(1);
	};
	
});

function onSearch() {
	$('#wrapperResults').fadeIn();
	$('#search_submit').html("<img src=\"img/loading_h.GIF\"/>");
	var params = {};
	if($('#uxSearchExtent').hasClass('on')) {
		var b = map.getBounds();
		params.g = "POLYGON(("+b.getSouth() + " "+b.getWest() + ", " + b.getSouth() + " "+b.getEast() + ", " + b.getNorth() + " " + b.getEast() + ", " + b.getNorth() + " " + b.getWest()  + ", " + b.getSouth() + " "+b.getWest()+"))";
	}
	if(selectedjstree.length > 0) {
		params.ft = selectedjstree.join();
	}
	params.q = encodeURIComponent($('#uxSearchTerm').val());
	

	$.ajax({
      type: 'GET',
	  'dataType': "json",
	  'data': params,
	  'url': 'SearchMain',
	  'success': function(data){
		  	displayResults(data.c);
		  	$('#search_submit').html("SEARCH");
	  },
	  'error': function(response) {
	  	console.error(response);
	  	// alert("Sorry, no entities found matching the specified criteria");
	  	var header = "<h2>Search Results</h2>Sorry, no entities found matching the specified criteria.";
		$('#resultsHeader').html(header);
		$('#results').html("");
		$('#search_submit').html("SEARCH");
	  }
	});
}

function displayResults(data) {
	var header = "<h2>Search Results</h2>There are "+data.length+" entities matching your search criteria...";
	$('#resultsHeader').html(header);
	var prev = "";
	var content = "<table><tr><td class=\"resultEntity\" style=\"text-decoration:none;font-weight:bold;\">Entity</td><td class=\"resultEntity\" style=\"text-decoration:none;font-weight:bold;\">Feature Type</td></tr>";
	for(var i in data) {
		if (data[i].uri != prev) {
			content += "<tr><td style=\"cursor:pointer;text-decoration: underline;\" class=\"resultEntity\" onclick=\"getEntityDetails('"+data[i].uri+"')\">"+data[i].name + "</td><td class=\"resultEntity\">" + data[i].ft.replace(/_/gi,' ').capitalize() + "</td></tr>";
			prev = data[i].uri;
		}
	}
	$('#results').html(content);
}

function getEntityDetails(uri) {
	$.ajax({
	      type: 'GET',
		  'dataType': "json",
		  'url': 'GetDetails?uri='+uri,
		  'success': function(data){
			  displayEntityInfo(data.c);
		  },
		  'error': function(response) {
		  	console.error(response);
		  	alert("error");
		  }
		});	
}
function displayEntityInfo(data) {
	var primaryName = "";
	var content = "<table><tr><td class=\"resultEntity\" style=\"text-decoration:none;font-weight:bold;\">Entity</td><td class=\"resultEntity\" style=\"text-decoration:none;font-weight:bold;\">Feature Type</td></tr>";
	for(var i in data) {
		
		if(data[i].rel == "hasExtent") {
			mapData(data[i].val);
		}
		if(data[i].rel == "hasPrimaryName") {
			primaryName = data[i].val.replace("@en","");
			content += "<tr><td class=\"resultEntity\">"+data[i].rel + "</td><td class=\"resultEntity\">" + primaryName + "</td></tr>"
		} else if (data[i].rel == "relatedFeature") {
			content += "<tr><td class=\"resultEntity\">"+data[i].rel + "</td><td style=\"cursor:pointer;text-decoration: underline;\" class=\"resultEntity\" onclick=\"getEntityDetails('"+data[i].val+"')\">" + data[i].val.replace("http://adl-gazetteer.geog.ucsb.edu/ADL/","").replace(/_/g," ").capitalize() + "</td></tr>";
		} else if (data[i].rel == "type") {
			content += "<tr><td class=\"resultEntity\">"+data[i].rel + "</td><td class=\"resultEntity\">" + data[i].val.replace("http://adl-gazetteer.geog.ucsb.edu/ONT/ADL#","").replace("_"," ").capitalize() + "</td></tr>";
		} else if (data[i].rel == "hasModifiedDate" || data[i].rel == "hasEntryDate") {
			content += "<tr><td class=\"resultEntity\">"+data[i].rel + "</td><td class=\"resultEntity\">" + data[i].val.substring(0,10) + "</td></tr>";
		} else if (data[i].rel == "relatedItem") {
			content += "<tr><td class=\"resultEntity\">"+data[i].rel + "</td><td class=\"resultEntity\">" + data[i].val.replace("http://adl-gazetteer.geog.ucsb.edu/ADL/","").replace(/_/g," ") + "</td></tr>";
		} else if (data[i].rel == "hasDescription") {
			if (data[i].val.length > 0)
				content += "<tr><td class=\"resultEntity\">"+data[i].rel + "</td><td class=\"resultEntity\">" + data[i].val.replace("http://adl-gazetteer.geog.ucsb.edu/ADL/","").replace(/_/g," ") + "</td></tr>";
		} else {
			content += "<tr><td class=\"resultEntity\">"+data[i].rel + "</td><td class=\"resultEntity\">" + data[i].val + "</td></tr>";
		}
	}
	$('#entity').html(content);
	var header = "<h2>"+primaryName+"</h2>There are "+data.length+" relations assigned to this entity";
	$('#entityHeader').html(header);
	// $('#wrapperResults').slideUp();
}

function getFeatureClasses(tier) {
	$.ajax({
	      type: 'GET',
		  'dataType': "json",
		  'url': 'GetFeatureClasses?tier='+tier,
		  'success': function(data){
			  processFeatures(data.c, tier);
			  tier++;
			  if (tier < 5)
				  getFeatureClasses(tier);
		  },
		  'error': function(response) {
		  	console.error(response);
		  	alert("error");
		  }
		});	
}

function processFeatures(data, tier) {
	if (tier == 1) {
		for(var i in data) {
			if(features.hasOwnProperty(data[i].p)) {
				features[data[i].p][data[i].e] = {};
				//jstreedata.push({"id" : data[i].e, "parent" : data[i].p, "text" : data[i].e.replace(/_/gi,' ').capitalize() });
			} else {
				features[data[i].p] = {};
				features[data[i].p][data[i].e] = {};
				//jstreedata.push({"id" : data[i].p, "parent" : "#", "text" : data[i].p.replace(/_/gi,' ').capitalize() });
			}
		}
	} else if (tier == 2){
		for(var i in data) {
			for(var j in features) {
				for(var h in features[j]) {
					if(h == data[i].p) {
						features[j][h][data[i].e] = {};
						// jstreedata.push({"id" : data[i].e, "parent" : data[i].p, "text" : data[i].e });
					}
				}
			}
		}
	} else if (tier == 3){
		for(var i in data) {
			for(var j in features) {
				for(var h in features[j]) {
					for(var g in features[j][h]) {
						if(g == data[i].p) {
							features[j][h][g][data[i].e] = {};
						}
					}
				}
			}
		}
	} else if (tier == 4){
		for(var i in data) {
			for(var j in features) {
				for(var h in features[j]) {
					for(var g in features[j][h]) {
						for(var k in features[j][h][g]) {
							if(k == data[i].p) {
								features[j][h][g][k][data[i].e] = {};
							}
						}
					}
				}
			}
		}
		for(var a in features) {
			jstreedata.push({"id" : a, "parent" : "#", "text" : a.replace(/_/gi,' ').capitalize() });
			for(var b in features[a]) {
				jstreedata.push({"id" : b, "parent" : a, "text" : b.replace(/_/gi,' ').capitalize() });
				for(var c in features[a][b]) {
					jstreedata.push({"id" : c, "parent" : b, "text" : c.replace(/_/gi,' ').capitalize() });
					for(var d in features[a][b][c]) {
						jstreedata.push({"id" : d, "parent" : c, "text" : d.replace(/_/gi,' ').capitalize() });
					}
				}
			}
			
		}
		$('#jstree_demo_div').jstree({ 'core' : {
		    'data' : jstreedata
		} 
		});
	}

}

function mapData(data){
	if (map.hasLayer(marker))
		map.removeLayer(marker);
	if (data.substring(0,5) == "POINT") {
		data = data.replace("POINT (","");
		data = data.replace(")","");
		var ll = data.split(" ");
		marker = L.marker([ll[1],ll[0]]).addTo(map);
		map.setView([ll[1],ll[0]], 9);
		// map.fitBounds(marker.getBounds());
	}
	if (map.hasLayer(country))
		map.removeLayer(country);
	/* var mp = null;
	$.each(data.triples, function(key,val) {
		var wkt = new Wkt.Wkt();
		var w = wkt.read(val.value); 
		mp = {"type": "Feature",
				  "geometry": {
				    "type": "MultiPolygon",
				    "coordinates": data.triples[0].value,
				    "properties": {
				        "name": "MultiPolygon",
				        "style": {
				            color: "black",
				            opacity: 1,
				            fillColor: "white",
				            fillOpacity: 1,
				        }
				      }
				    }
		};
	// });
				    var d = new L.GeoJSON(mp).addTo(map); */
		// country = new L.Polygon(ll).addTo(map).bindPopup("I am a polygon.");
		
	
}