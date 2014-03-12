var map = L.map('map').setView([20, -10], 3);
	map.addLayer(new L.StamenTileLayer("watercolor"));
		/* L.tileLayer('http://{s}.tile.cloudmade.com/d1abd7c11778439c95f45e03924ab211/997/256/{z}/{x}/{y}.png', {
			maxZoom: 18,
		}).addTo(map); */
		
var country = null;	
		
function onSearch() {
	var params = {};
	params.country = encodeURIComponent($('#searchBox').val());
	$.ajax({
      type: 'GET',
	  'dataType': "json",
	  'data': params,
	  'url': 'GetGeometry',
	  'success': function(data){
		  	var x = data;
		  	mapData(data);
	  },
	  'error': function(response) {
	  	console.error(response);
	  	alert("error");
	  }
	});
}

function mapData(data){
	if (map.hasLayer(country))
		map.removeLayer(country);
	var mp = null;
	/*$.each(data.triples, function(key,val) {
		var wkt = new Wkt.Wkt();
		var w = wkt.read(val.value); */
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
				    var d = new L.GeoJSON(mp).addTo(map);
		// country = new L.Polygon(ll).addTo(map).bindPopup("I am a polygon.");
		map.fitBounds(d.getBounds());
	
}