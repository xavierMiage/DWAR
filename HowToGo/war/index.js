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
  var startVal =  $("#departSelect option:selected").val(); 
  var start =  $("#depart").val(); 
  var endVal =  $("#arriveeSelect option:selected").val();
  var end =  $("#arrivee").val();
  if(start != null && start.length > 0) {
      // Tan
	  	var hour = $("#hour").val();
	  	var minute = $("#minute").val();
	  	
	  	if(hour.length < 2)
	  		hour = "0" + hour;
	  	
	  	if(minute.length < 2)
	  		minute = "0" + minute;
	  	
	  	var now = new Date();
	  	var an = now.getFullYear();
	  	var mois = ('0'+now.getMonth()+1).slice(-2);
	  	var jour = ('0'+now.getDate()   ).slice(-2);
	    $.ajax({
	    	type: "GET",
	        url: "itineraire",
	        data: { depart: encodeURIComponent(startVal), arrive: encodeURIComponent(endVal), heure: hour + ":" + minute, date: an + "-" + mois + "-" + jour},
	    	success: function(data) {
	    		var parsedJSON = $.parseJSON(data);
	    		var inject = "";
	    		
	    		inject += "<p> Départ : " + parsedJSON.adresseDepart + " à " + parsedJSON.heureDepart
    					+ "<br/> Arriv&eacute;e : " + parsedJSON.adresseArrivee + " à " + parsedJSON.heureArrivee
    					+ "<br/> Dur&eacute;e : " + parsedJSON.duree + "<div id='table_details'><table>";
    					
    			var trip = new Array();	
    			
    			
	    		$.each(parsedJSON, function(index, element) {
	    			if(typeof element == "object" && index != "itineraire") {
    					inject += "<tr><td>" + element.heureDepart + "</br>" + element.heureArrivee + "</td><td>" + element.libelle + "</td><td>" + element.route + "</td></tr>";
	    			}
	    		});
	    		
	    		inject += "</table></div></p>";
	    		
	    		$("#details_bottom p").remove();
	    		$("#table_details").remove();
		    	$("#details_bottom").append(inject);
		        $("#table_details").niceScroll({cursorcolor:"#E87976"});

		    	
		    	inject = "";
		    	$.each(parsedJSON.itineraire, function(index, element) {
		    		trip.push(new google.maps.LatLng(element.lat, element.lng));
		    		inject += "<p>" + index + "</p><br/>";
		    	});
		    	
		    	//json print
		    	/*var poly = new google.maps.Polyline({
		    		map: map,
		    		path: trip,//chemin du tracé
		    		strokeColor: "#FF0000",//couleur du tracé
		    		strokeOpacity: 1.0,//opacité du tracé
		    		strokeWeight: 2//grosseur du tracé
		    	});*/

		    	//poly.setMap(map);
		    	
		    	//test
		    	
		    	//endtest
		    	
		    	
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
			$("#details_top > .subtitle_details").text("A Vélo");
		  }else if($('input[name=travelmode]:checked').val()=="walk"){
		 	$("#details_top > .subtitle_details").text("A Pieds");
		  }else{
		 	$("#details_top > .subtitle_details").text("En Voiture");
		  }
          directionsDisplay.setDirections(result);
          var text = "Distance: "+result.routes[0].legs[0].distance.value/1000+"km";
          text += "<br/>Durée: "+secondsToTime(result.routes[0].legs[0].duration.value);
          text += "<br/>Coût: "+Math.round(result.routes[0].legs[0].distance.value/1000*$("#carburant").val()*100)/100+"€";
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
		

		$("#depart").change(function() {
			$.ajax({
		    	type: "GET",
		        url: "tan",
		        data: { nom: $(this).val()},
		    	success: function(data) {
			    	var parsedJSON = $.parseJSON(data);
			    	var inject = "";
			    	$.each(parsedJSON, function(index, element) {
			    		if(typeof element == "object")
			    			inject += "<option value='" + element.id + "'>" + element.nom + " " + element.ville + "</option>";
			    	});
			    	$("#departSelect").empty();
			    	$("#departSelect").append(inject);
			    },
			    error : function(jqXHR, status, exception) {
			    	alert("error : " + status + " / " + exception);
			    }
		    });
		});
		
		$("#arrivee").change(function() {
			$.ajax({
		    	type: "GET",
		        url: "tan",
		        data: { nom: $(this).val()},
		    	success: function(data) {
			    	var parsedJSON = $.parseJSON(data);
			    	var inject = "";
			    	$.each(parsedJSON, function(index, element) {
			    		if(typeof element == "object")
			    			inject += "<option value='" + element.id + "'>" + element.nom + " " + element.ville + "</option>";
			    	});
			    	$("#arriveeSelect").empty();
			    	$("#arriveeSelect").append(inject);
			    },
			    error : function(jqXHR, status, exception) {
			    	alert("error : " + status + " / " + exception);
			    }
		    });
		});
	
	
	
	
	$("#map-canvas").height($("body").height()-350);
	//google.maps.event.trigger(map,'resize');
	var h_map = $("#map-canvas").height();
	var h_footer = $("#footer").height();
	var h_body = $("body").height();

	$("#title_details").css("top",($("#bloc_details").height()-158/2)/2+"px");
	
	$('.input').pressEnter(function(){
		calcRoute();
		$("#map-canvas").animate({height: "86.8%"});
		$("#bloc_details").fadeIn();
		$("#footer").animate({height: "54px"});
		$("#foot_title").slideDown();;
	});
	
	$('.date_input').pressEnter(function(){
		calcRoute();
		$("#map-canvas").animate({height: "86.8%"});
		$("#bloc_details").fadeIn();
		$("#footer").animate({height: "54px"});
		$("#foot_title").slideDown();;
	});
	
	$("#process").click(function(){
		calcRoute();
		$("#map-canvas").animate({height: h_body-104});
		$("#bloc_details").fadeIn();
		$("#footer").animate({height: "54px"});
		$("#foot_title").slideDown();
	});
	
	$("#foot_title").click(function(){
		$("#map-canvas").animate({height: h_map});
		$("#bloc_details").fadeOut();
		$("#footer").animate({height: h_footer});
		$("#foot_title").slideUp();
	});
     
     $('.radio_img_travelmode').click(function(){
    	 $('.radio_img_travelmode').removeClass('travelmode_selected');
         $(this).addClass('travelmode_selected');
     });
     
     $('#questionmark').click(function(){
    	 $('#backhelp').animate({top: "130px"}, {duration: "slow"});
     });
     
     $('#closehelp').click(function(){
    	 $('#backhelp').animate({top: "-1345px"}, {duration: "slow"});
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
