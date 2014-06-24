/**
 * 
 */


var directionsDisplay;
var directionsService = new google.maps.DirectionsService();
var map;
var result;
var geocoder;

function initialize() {
  directionsDisplay = new google.maps.DirectionsRenderer();

  var nantes = new google.maps.LatLng(47.21837, -1.55362);
  var mapOptions = {
    zoom:15,
    center: nantes
  }
  map = new google.maps.Map(document.getElementById("map-canvas"), mapOptions);
  directionsDisplay.setMap(map);
  calcRoute();
}

function calcRoute() {
  var start = document.getElementById("depart").value;
  var end = document.getElementById("arrivee").value;
  if(start != null && start.length > 0) {
      // Tan
      
	    $.ajax({
	    	type: "GET",
	        url: "tan",
	        data: { nom: start},
	    	success: function(data) {
		    	$("body").append(data);
		    },
		    error : function(jqXHR, status, exception) {
		    	alert("error : " + status + " / " + exception);
		    }
	    });
      
      
	    $.ajax({
	    	type: "GET",
	        url: "itineraire",
	        data: { depart: start, arrive: end},
	    	success: function(data) {
	    		var parsedJSON = $.parseJSON(data);
	    		var inject = "";
	    		
	    		inject += "<div> D�part : " + parsedJSON.adresseDepart + " � " + parsedJSON.heureDepart
    					+ "<br/> Arriv&eacute;e : " + parsedJSON.adresseArrivee + " � " + parsedJSON.heureArrivee
    					+ "<br/> Dur&eacute;e : " + parsedJSON.duree + "<table>";
    					
    			var trip = new Array();	
    			
    			/*geocoder.geocode( { 'address': parsedJSON.adresseDepart + ", Nantes"}, function(results, status) {
	    	      if (status == google.maps.GeocoderStatus.OK) {
	    	    	  var latlng = results[0].geometry.location.toString().substr(1, results[0].geometry.location.toString().length - 1);
	    	    	  latlng = latlng.split(", ");
	        		  trip.push(new google.maps.LatLng(latlng[0], latlng[1]));
	    	      } else {
	    	        alert("Geocode was not successful for the following reason: " + status);
	    	      }
	    	    });
    			geocoder.geocode( { 'address': parsedJSON.adresseArrivee + ", Nantes"}, function(results, status) {
	    	      if (status == google.maps.GeocoderStatus.OK) {
	    	    	  var latlng = results[0].geometry.location.toString().substr(1, results[0].geometry.location.toString().length - 1);
	    	    	  latlng = latlng.split(", ");
	        		  trip.push(new google.maps.LatLng(latlng[0], latlng[1]));
	    	      } else {
	    	        alert("Geocode was not successful for the following reason: " + status);
	    	      }
	    	    });*/
    			
	    		$.each(parsedJSON, function(index, element) {
	    			if(typeof element == "object" && index != "itineraire") {
	    				//trip.push(element.libelle);
    					inject += "<tr><td>" + element.heureDepart + " - " + element.heureArrivee + "</td><td>" + element.libelle + "</td><td>" + element.route + "</td></tr>";
	    			}
	    		});
	    		
	    		inject += "</table></div>";
	    		
		    	$("#details_bottom").append(inject);
		    	
		    	inject = "";
		    	$.each(parsedJSON.itineraire, function(index, element) {
		    		trip.push(new google.maps.LatLng(element.lat, element.lng));
		    		inject += "<p>" + index + "</p><br/>";
		    	});
			
		    	$("body").append(data);
		    	var poly = new google.maps.Polyline({
		    		map: map,
		    		path: trip,//chemin du trac�
		    		strokeColor: "#FF0000",//couleur du trac�
		    		strokeOpacity: 1.0,//opacit� du trac�
		    		strokeWeight: 2//grosseur du trac�
		    	});

		    	poly.setMap(map);
		    	
		    	
		    },
		    error : function(jqXHR, status, exception) {
		    	alert("error : " + status + " / " + exception);
		    }
	    });
	  
      
      // Google MAP
      var travelmode
      if($('input[name=travelmode]:checked').val()=="bike"){
    	  travelmode=google.maps.TravelMode.BICYCLING;
      }else if($('input[name=travelmode]:checked').val()=="walk"){
    	  travelmode=google.maps.TravelMode.WALKING;
      }else{
    	  travelmode=google.maps.TravelMode.DRIVING;
      }
      var today = new Date();
      var hour = $("#hour").val();
      var minute = $("#minute").val();
      var day = today.getDate();
      var month = today.getMonth();
      var year = today.getFullYear();
      var date = new Date(year,month,day,hour,minute);
      var request = {
        origin:start,
        destination:end,
        travelMode: travelmode,
        transitOptions: {
           departureTime: new Date(1337675679473)
        },
        unitSystem: google.maps.UnitSystem.METRIC
      };
      directionsService.route(request, function(result, status) {
        if (status == google.maps.DirectionsStatus.OK) {
		  if($('input[name=travelmode]:checked').val()=="bike"){
			$("#details_top > .subtitle_details").text("A V�lo");
		  }else if($('input[name=travelmode]:checked').val()=="walk"){
		 	$("#details_top > .subtitle_details").text("A Pieds");
		  }else{
		 	$("#details_top > .subtitle_details").text("En Voiture");
		  }
          directionsDisplay.setDirections(result);
          var text = "Distance: "+result.routes[0].legs[0].distance.value/1000+"km";
          text += "<br/>Dur�e: "+secondsToTime(result.routes[0].legs[0].duration.value);
          text += "<br/>Co�t: "+Math.round(result.routes[0].legs[0].distance.value/1000*$("#carburant").val()*100)/100+"�";
          $("#details_top > p").text("");
          $("#details_top > p").append(text);
        }
      });
  
  }
}

function secondsToTime(secs)
{
    var hours = Math.floor(secs / (60 * 60));
   
    var divisor_for_minutes = secs % (60 * 60);
    var minutes = Math.floor(divisor_for_minutes / 60);
 
    var divisor_for_seconds = divisor_for_minutes % 60;
    var seconds = Math.ceil(divisor_for_seconds);
   
    return hours+"h"+minutes;
}

$.fn.pressEnter = function(fn) {  

    return this.each(function() {  
        $(this).bind('enterPress', fn);
        $(this).keyup(function(e){
            if(e.keyCode == 13)
            {
              $(this).trigger("enterPress");
            }
        })
    });  
 };

google.maps.event.addDomListener(window, 'load', initialize);
  
	$(document).ready(function() {
		
		//Autocompletion
	var availableTags = [
		"Michelet",
		"Facult�s",
		"Commerce"
	];
	
	$(".input").autocomplete({
		source: availableTags
	});
	
	$("#map-canvas").height($(window).height()-350);
	//google.maps.event.trigger(map,'resize');
	
	$('.input').pressEnter(function(){
		calcRoute();
		$("#map-canvas").animate({height: "75%"});
		$("#bloc_details").fadeIn();
	});
	
	$('.date_input').pressEnter(function(){
		calcRoute();
		$("#map-canvas").animate({height: "75%"});
		$("#bloc_details").fadeIn();
	});
	
	$("#process").click(function(){
		calcRoute();
		$("#map-canvas").animate({height: "75%"});
		$("#bloc_details").fadeIn();
	});
     
     $('.radio_img_travelmode').click(function(){
    	 $('.radio_img_travelmode').removeClass('travelmode_selected');
         $(this).addClass('travelmode_selected');
     });
     
     var slide= false;
     $("#title_details").click(function(){
    	 if(slide == true){
    		 $("#bloc_details").animate({width:"47px"});
    		 slide = false;
    	 }else{
    		 $("#bloc_details").animate({width:"300px"});
    		 slide = true;
    	 }
     });
	
});




  
	
