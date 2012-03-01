package com.maptweet.test;



import java.util.List;
/*google maps libraries*/
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView; 
import com.google.android.maps.MyLocationOverlay;

/*twitter4j twitter libraries*/
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

/*singpost libraries for OAuth*/
import oauth.signpost.OAuth;
import oauth.signpost.OAuthProvider; 
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

/*android libraries*/
import android.os.AsyncTask;
import android.os.Bundle;
import android.net.Uri;
import android.content.DialogInterface;
import android.location.*;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.app.AlertDialog;




public class MaptweetActivity extends MapActivity {
    /** Called when the activity is first created. */
    
	 /** OAuth requirements**/
	  private final static String CONSUMER_KEY = "70BFDQpKRT92uh5XT53v5g"; 
	  private final static String CONSUMER_SECRET = "tO3xuiGcZDc87H3UfNLWUz1XEkVziWkOvf6njqTf8s";
	  private final static String URL_CALLBACK = "maptweet://twitter";
      private final static String URL_OAUTH_REQUEST_TOKEN = "https://api.twitter.com/oauth/request_token";
      private final static String URL_OAUTH_ACCESS_TOKEN = "http://twitter.com/oauth/access_token";
      private final static String URL_OAUTH_AUTHORIZE = "http://twitter.com/oauth/authorize";
      private OAuthProvider provider = new CommonsHttpOAuthProvider(URL_OAUTH_REQUEST_TOKEN,URL_OAUTH_ACCESS_TOKEN,URL_OAUTH_AUTHORIZE);
	  private CommonsHttpOAuthConsumer consumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY,CONSUMER_SECRET);
      
	  
      /*locationmagr is presently only used to verify location services are enabled*/
	  private LocationManager locationmgr;
	  
	  /*the map view and mylocationoverlay (the current location drop)*/
	  private MapView mapView;
	  private MyLocationOverlay myLocationOverlay;


	  /*map controller to control the map - move to current position and zoom */
	  private MapController mapController;
	  
	  /*Variables for geocoding - geopoint for current position and string to house the city name*/
	  private GeoPoint geoPoint;
	  private Geocoder geocoder;
	  private String addrString;
	

	
  
	
	  
	/*main method kicked off on start*/
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        

        
        
        /*Check if location services are enabled and if not prompt to enable*/
        if (!locationEnabled()) {
                    showAlert();
         }
        
        
        
    }

	
	
	
	
   /*Method checks if  provider (GPS) is enabled and returns results*/
    public boolean locationEnabled()
    {
   	 
    		locationmgr = (LocationManager) getSystemService(LOCATION_SERVICE);
	
    	return locationmgr.isProviderEnabled(LocationManager.GPS_PROVIDER);   	
    }
    
    
    
    

 
   
   
   
   
   
   /*Draw the map and place overlay on current position*/
   public void drawMap(){
	   
	   
	   Button mapbutton = (Button) findViewById(R.id.button2);
	   // Button tweetbutton = (Button) findViewById(R.id.button1);
	    mapbutton.setVisibility(View.INVISIBLE);
	     

		  geocoder = new Geocoder(this);
		  new getMap().execute();
		 
		  
	/*	  
		  myLocationOverlay.runOnFirstFix(new Runnable() {
	    	  
	            public void run() {
	              
	            	
	            	geoPoint = myLocationOverlay.getMyLocation();
	            	mapController.animateTo(geoPoint);
	            	
	                try {
	                   
	                	
	                	double lat2 = geoPoint.getLatitudeE6() / 1E6;
	                    double lng2 = geoPoint.getLongitudeE6() / 1E6;
	                    
	                	
	                	List<Address> addresses =  geocoder.getFromLocation(lat2,lng2, 1);
	                	
	                    if(addresses != null && addresses.size() > 0)
	                    {
	                    	
	                            addrString = addresses.get(0).getLocality(); //returnedAddress.getLocality().toString().substring(1, 100);

	                           
	                    }
	                    else
	                    {
	                    	 errorAlert("Error Locating City!!");
	                    }

	                	
	                	

					
					} catch (Exception e) {
						
						 throw new RuntimeException(e);

						
						
					}
	                

	                
	            }
	        });
	       
		  
		*/  
		  

		  mapView.setVisibility(View.VISIBLE);
		 // tweetbutton.setVisibility(View.VISIBLE);
		  
		  
		  
		  
	   
	   
   }
   
   

   
   /*Method called by clicking the map button */
   public void mapIt(View button1)
   {
	   
      /*Initialize map*/
       mapView = (MapView) findViewById(R.id.mapview);
       myLocationOverlay = new MyLocationOverlay(this, mapView);
       mapView.getOverlays().add(myLocationOverlay);
       myLocationOverlay.enableMyLocation();
       mapController = mapView.getController();
       mapController.setZoom(16);


       drawMap();
		 

      
	   
   }
   
   
   
   
   
   /*called by tweet button - tweets current position in background threads*/
   public void tweet(View button1)
   {
	   
	   try{

		   new requestTweet().execute();

		   
		   
	   }catch(Exception e){
		   

		   throw new RuntimeException(e);

		   
	   }
	    
	   

   }
  
   

   
   
   
   

    
    
    
/*Alert window with options - yes starts settings intent*/
public void showAlert() {


	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setMessage("Location Services Required! Enable?")
	.setCancelable(false)
	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {

			Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
			startActivity(intent);
			// MaptweetActivity.this.finish();



		}
	})
	.setNegativeButton("No", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {
			//  dialog.cancel();
			MaptweetActivity.this.finish();
		}
	});
	AlertDialog alert = builder.create();
	alert.show();



}




/*Presents an alert with the passed string and kills app*/
public void errorAlert(String string)
{
	
	new AlertDialog.Builder(this).setTitle("ERROR").setMessage(string).setNeutralButton("Close",  new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int id) {

			MaptweetActivity.this.finish();

		}

		}).show();

}
    
    

/*Intent called when coming back from twitter - get the data then create background task to get access info and tweet*/
@Override
public void onNewIntent(Intent intent){
	super.onNewIntent(intent);
	Uri uri = intent.getData();
	
	if(uri != null)
	{
		
		String uriString = uri.toString();
		if(uriString.startsWith(URL_CALLBACK))
		{
			 new accessTweet().execute(intent);
			
			
		}
		
		
	}
	
	
}






/*Background task to retrieve request token then kick off the intent to log in*/
private class requestTweet extends AsyncTask<Void, Void, Void> {
	
	
	 private String authURL;
	
	
    protected Void doInBackground(Void... args) {
		  

    	try {
    	   authURL = provider.retrieveRequestToken(consumer, URL_CALLBACK);

    	}catch(Exception e){
    		throw new RuntimeException(e);
    		
    	}
    	
  
       return null;
    }

    protected void onPostExecute(Void result) {
   
		   startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authURL)));

    }
}





/*background task retrieve access token then tweet*/
private class accessTweet extends AsyncTask<Intent, Void, Void> {
	
	

	
	
   protected Void doInBackground(Intent... intent) {

	      
	   Uri uri = intent[0].getData();

		   try{
				
				String verifier = uri.getQueryParameter(OAuth.OAUTH_VERIFIER);
				provider.retrieveAccessToken(consumer, verifier);
				

				 try{
					  
					  Configuration conf = new ConfigurationBuilder().setOAuthConsumerKey(consumer.getConsumerKey()).setOAuthConsumerKey(consumer.getConsumerSecret()).build();
					  AccessToken accessToken = new AccessToken(consumer.getToken(),consumer.getTokenSecret());
					  Twitter twitter = new TwitterFactory(conf).getInstance(accessToken);
					  
					  
					  
					  /*if the addrstring has been set, tweet it!*/
					  if(addrString != "")
					  {
					
						  Log.w("myApp", addrString);
					      twitter.updateStatus(addrString + "   " +  System.currentTimeMillis());

					  }else{
						  errorAlert("Error Tweeting Location!!!");
						  
					  }
					  

					  
				  }catch(Exception e) {
					  
					  
					  throw new RuntimeException(e);
					  
					  
					  
				  }
				
	
				
				
			}catch(Exception e){
				 throw new RuntimeException(e);

			}
		   
		   

      return null;
   }

   protected void onPostExecute(Void result) {	   
	     
	   /*after tweeting, remove tweet button*/
	   findViewById(R.id.button1).setVisibility(View.INVISIBLE);
	 
   }
}





/*Background task to get geolocation*/
private class getMap extends AsyncTask<Void, Void, Void> {	
	

	
	
    protected Void doInBackground(Void... args) {
		  
    	//System.out.println("in site builder +++++++++++++++++++++++++++++++++++");
       // 
    	
    	 myLocationOverlay.runOnFirstFix(new Runnable() {
	    	  
	            public void run() {
	              
	            //	System.out.println("in site builder +++++++++++++++++++++++++++++++++++");
	            	geoPoint = myLocationOverlay.getMyLocation();
	            	mapController.animateTo(geoPoint);
	            	
	                try {
	                   
	                	
	                	double lat2 = geoPoint.getLatitudeE6() / 1E6;
	                    double lng2 = geoPoint.getLongitudeE6() / 1E6;
	                    
	                	
	                	List<Address> addresses =  geocoder.getFromLocation(lat2,lng2, 1);
	                	
	                    if(addresses != null && addresses.size() > 0)
	                    {
	                    	
	                            addrString = addresses.get(0).getLocality(); //returnedAddress.getLocality().toString().substring(1, 100);

	                           
	                    }
	                    else
	                    {
	                    	 errorAlert("Error Locating City!!");
	                    }

	                	
	                	

					
					} catch (Exception e) {
						
						 throw new RuntimeException(e);

						
						
					}
	                

	                
	            }
	        });
	       
    	
  
       return null;
    }

    protected void onPostExecute(Void result) {
   
    	findViewById(R.id.button1).setVisibility(View.VISIBLE);

    }
}


    
    /*required for mapActivity*/
	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
}

