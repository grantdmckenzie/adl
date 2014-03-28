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

$(function () { 
	
	getFeatureClasses(1);
	$('#jstree_demo_div').on("changed.jstree", function (e, data) {
	  console.log(data.selected);
	});
	$('#uxSearchExtent').on("click", function() {
		$(this).toggleClass("on");
	});
	
	String.prototype.capitalize = function() {
	    return this.charAt(0).toUpperCase() + this.slice(1);
	}
});

function onSearch() {
	var params = {};
	params.q = encodeURIComponent($('#uxSearchTerm').val());
	
	$.ajax({
      type: 'GET',
	  'dataType': "json",
	  'data': params,
	  'url': 'SearchMain',
	  'success': function(data){
		  	displayResults(data.c);
	  },
	  'error': function(response) {
	  	console.error(response);
	  	alert("error");
	  }
	});
}

function displayResults(data) {
	var header = "<h2>Search Results</h2>There are "+data.length+" entities matching your search criteria...";
	$('#resultsHeader').html(header);
	var content = "<table><tr><td class=\"resultEntity\" style=\"text-decoration:none;font-weight:bold;\">Entity</td><td class=\"resultEntity\" style=\"text-decoration:none;font-weight:bold;\">Feature Type</td></tr>";
	for(var i in data) {
		content += "<tr><td style=\"cursor:pointer;text-decoration: underline;\" class=\"resultEntity\" onclick=\"getEntityDetails('"+data[i].uri+"')\">"+data[i].name + "</td><td class=\"resultEntity\">" + data[i].ft.replace(/_/gi,' ').capitalize() + "</td></tr>";
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
	var header = "<h2>Selected Entity</h2>There are "+data.length+" relations assigned to this entity";
	$('#entityHeader').html(header);
	var content = "<table><tr><td class=\"resultEntity\" style=\"text-decoration:none;font-weight:bold;\">Entity</td><td class=\"resultEntity\" style=\"text-decoration:none;font-weight:bold;\">Feature Type</td></tr>";
	for(var i in data) {
		content += "<tr><td class=\"resultEntity\">"+data[i].rel + "</td><td class=\"resultEntity\">" + data[i].val + "</td></tr>";
		if(data[i].rel == "hasExtent") {
			mapData(data[i].val);
		}
	}
	$('#entity').html(content);

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
				jstreedata.push({"id" : data[i].e, "parent" : data[i].p, "text" : data[i].e.replace(/_/gi,' ').capitalize() });
			} else {
				features[data[i].p] = {};
				features[data[i].p][data[i].e] = {};
				jstreedata.push({"id" : data[i].p, "parent" : "#", "text" : data[i].p.replace(/_/gi,' ').capitalize() });
			}
		}
	} else if (tier == 2){
		for(var i in data) {
			for(var j in features) {
				for(var h in features[j]) {
					if(h == data[i].p) {
						features[j][h][data[i].e] = {};
						//jstreedata.push({"id" : data[i].e, "parent" : data[i].p, "text" : data[i].e });
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