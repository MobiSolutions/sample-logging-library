package mobi.lab.sample_event_logging_library.service;

/**
 * This file is part of sample-logging-library.
 *
 * sample-logging-library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * sample-logging-library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with sample-logging-library.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TimeZone;

import mobi.lab.sample_event_logging_library.Config;
import mobi.lab.sample_event_logging_library.data.LogEvent;
import mobi.lab.sample_event_logging_library.network.LogEventRequest;
import mobi.lab.sample_event_logging_library.network.Network;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

/**
 * Service for posting logs in background. Should run in a separate process (should be set so in Manifest)<br/>
 * What it does:<br/>
 *
 * It gets an log event and tries to add a location to it (if location service is not available then events will be just sent to server).
 * If no location is available (but location service is available) then it will first try to get a valid location and then upload events to server.
 * <br/><br/>
 * Events are meant to be uploaded in batches: if {@link mobi.lab.sample_event_logging_library.Config#EVENTS_PER_REQUEST_SOFT_LIMIT} event soft
 * limit is hit or time interval exceeds {@link mobi.lab.sample_event_logging_library.Config#MAX_TIME_INTERVAL_BETWEEN_REQUESTS} milliseconds.
 *<br/><br/>
 *
 * TODO #1: location service is not fully functional, we are not getting enough location updates which can cause log events with no location
 * TODO #2: handle no connection -> in case of no connection there is no point to initiate logs sync request
 * TODO #3: handle no location permission situation (dialogs for asking permission)
 * TODO #4 look over the handler logic for time interval -> maybe support JobScheduler as much as possible?
 *
 * To make use of this you have to declare this in the Manifest: {@code <service android:name="LogPostService" android:label="@string/app_name" android:process="mobi.lab.sample_event_logging_library.someOtherProcess" /> }
 * <br/>
 * You'll need {@code <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />} to get location. <br/>
 * You'll also need {@code <uses-permission android:name="android.permission.INTERNET" />} to post logs. <br/>
 *
 * TO
 */
public class LogPostService extends Service implements LocationListener {

    private static final String TAG = LogPostService.class.getSimpleName();
    public static final String EXTRA_EVENT_TYPE = TAG + ".EXTRA_EVENT_TYPE";
    public static final String EXTRA_EVENT_CONTENT = TAG + ".EXTRA_EVENT_CONTENT";

    private ArrayList<LogEvent> logEvents = new ArrayList<>();
    private final Object logEventsSyncLock = new Object();
    private ArrayList<LogEvent> logEventsForRequest;
    private Handler handler;
    private LogEventRequest logEventService;
    private Runnable timedRunnable = new Runnable() {
        @Override
        public void run() {
            if (logEvents.size() > 0) {
                sendLogsToServer();
                if (handler != null) {
                    handler.postDelayed(timedRunnable, Config.MAX_TIME_INTERVAL_BETWEEN_REQUESTS);
                }
            }
        }
    };
    private boolean isLocationServicesEnabled;
    private Location lastKnownLocation;

    public LogPostService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        //TODO: starting from Android 5.0 we could use JobScheduler (currently not in support libraries)
        handler = new Handler();
        handler.postDelayed(timedRunnable, Config.MAX_TIME_INTERVAL_BETWEEN_REQUESTS);
        logEventsForRequest = new ArrayList<>();

        OkHttpClient client = new OkHttpClient();
        client.interceptors().add(new Interceptor() {
            @Override
            public com.squareup.okhttp.Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Log.d(TAG, String.format("\n\nrequest:%s\nheaders:%s\n" +
                                "url:%s",
                        request.body().toString(), request.headers(), request.httpUrl().toString()));
                return chain.proceed(request);
            }
        });

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Network.BASE_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        logEventService = retrofit.create(LogEventRequest.class);

        if (checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkCallingOrSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO: ask for permission if not given
            Log.d(TAG, "location permission not granted");
            isLocationServicesEnabled = false;
            return;
        }
        isLocationServicesEnabled = true;
        final LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        Log.d(TAG, "requesting user location");
        locationManager.requestLocationUpdates(Config.LOCATION_UPDATES_MIN_TIME, Config.LOCATION_UPDATES_MIN_DISTANCE, criteria, this, null);
    }

    @Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind");
		throw new UnsupportedOperationException("onBind not yet implemented");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// intent may be null if the service is being restarted after its process has gone away,
		// and it had previously returned anything except START_STICKY_COMPATIBILITY.
		if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey(EXTRA_EVENT_TYPE)) {
			Log.d(TAG, " onStartCommand");
            int eventType = intent.getExtras().getInt(EXTRA_EVENT_TYPE);
            String eventContent = intent.getExtras().getString(EXTRA_EVENT_CONTENT);
            LogEvent event = new LogEvent(eventType, eventContent);
            if (isLocationGoodEnough(lastKnownLocation)) {
                event.setLocation(lastKnownLocation);
            }
            handleLogEvent(event);
		}
		return super.onStartCommand(intent, flags, startId);
	}

    private void handleLogEvent(LogEvent logEvent) {
        if (logEvent.getLocation() == null && isLocationServicesEnabled && !isLocationGoodEnough(lastKnownLocation)) {
            //let's wait until we get a good enough location
            logEvents.add(logEvent);
            return;
        }

        if (logEvents.size() + 1 >= Config.EVENTS_PER_REQUEST_SOFT_LIMIT) {
            sendLogsToServer();
        } else {
            logEvents.add(logEvent);
        }
    }

    private void sendLogsToServer() {
        synchronized (logEventsSyncLock) {
            //check for running requests
            if (logEventsForRequest.size() == 0) {
                //if location services are enabled then we will only send events with location and wait for the location
                if (isLocationServicesEnabled) {
                    for (Iterator<LogEvent> iterator = logEvents.iterator(); iterator.hasNext();) {
                        LogEvent logEvent = iterator.next();
                        if (logEvent.getLocation() != null || logEvent.getNrOfTriesForLocation() >= Config.NR_OF_GET_LOCATION_TRIES) {
                            Log.d(TAG, "sendLogsToServer -> queueing log: " + logEvent.toString());
                            logEventsForRequest.add(logEvent);
                            iterator.remove();
                        } else {
                            logEvent.increaseNrOfTriesForLocation();
                        }
                    }
                } else {
                    //if location services are disabled then we will send even the events without the location
                    Log.d(TAG, "sendLogsToServer -> location services are disabled, will sync all(" + logEvents.size() + ") logEvents");
                    logEventsForRequest.addAll(logEvents);
                    logEvents.clear();
                }

                if (logEventsForRequest.size() > 0) {
                    logEventService.saveLogEvents(logEventsForRequest).enqueue(new Callback<Void>() {
                        @Override
                        public void onResponse(Response<Void> response) {
                            synchronized (logEventsSyncLock) {
                                if (response.code() >= 200 && response.code() < 300) {
                                    Log.d(TAG, logEventsForRequest.size() + " events synced to server [" + logEvents.size() + " events waiting for sync]");
                                    logEventsForRequest.clear();
                                    stopServiceIfEventsHandled();
                                } else {
                                    logEvents.addAll(logEventsForRequest);
                                    logEventsForRequest.clear();
                                    //something went wrong, let's try again later
                                    stopServiceIfEventsHandled();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Throwable t) {
                            synchronized (logEventsSyncLock) {
                                logEvents.addAll(logEventsForRequest);
                                logEventsForRequest.clear();
                                //something went wrong, probably network error -> we will try again later
                                stopServiceIfEventsHandled();
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "no logEvents to sync -> " + logEvents.size() + " events waiting for location");
                    stopServiceIfEventsHandled();
                }
            } else {
                stopServiceIfEventsHandled();
            }
        }
    }

    private void stopServiceIfEventsHandled() {
        if (logEvents.size() == 0) {
            Log.d(TAG, "stopService()");
            stopSelf();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (!isLocationGoodEnough(location)) {
            Log.d(TAG, "onLocationChanged: Skipping " + location + ", provider: " + location.getProvider() + ",  not good enough");
            return;
        }
        Log.d(TAG, "onLocationChanged: updating to " + location + ", provider: " + location.getProvider());
        lastKnownLocation = location;
        synchronized (logEventsSyncLock) {
            for (LogEvent logEvent : logEvents) {
                if (logEvent.getLocation() == null) {
                    Log.d(TAG, "updating location of logEvent: " + logEvent.toString());
                    logEvent.setLocation(lastKnownLocation);
                }
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    private boolean isLocationGoodEnough(final Location location) {
        if (location == null) {
            return false;
        }
        if (location.hasAccuracy() && location.getAccuracy() >= Config.LOCATION_IS_NOT_ACCURATE) {
            Log.d(TAG, "isLocationGoodEnough: false, location accuracy " + location.getAccuracy() + "m >= " + Config.LOCATION_IS_NOT_ACCURATE + "m");
            return false;
        }

        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        final long locationTime = location.getTime();
        final long timeDifference = cal.getTime().getTime() - locationTime;
        if (timeDifference >= Config.LOCATION_IS_OLD) {
            Log.d(TAG, "isLocationGoodEnough: false, location age >= " + Config.LOCATION_IS_OLD + " ms");
            return false;
        }
        return true;
    }

}
