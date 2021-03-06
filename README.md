General idea:

#####"sample-android-app" directory
is the android project where one can find a button, pressing on it will initiate a log event which will be uploaded to the server. In this sample the logs are mocked to be uploaded to [this sample endpoint](http://office.mobi.ee/~karlmartin/api/logs/save/)

#####"sample-event-logging-library" directory
is the library project which will take care of the logs. It will try to add location and time to the log events and then sync the logs with backend.

Log events as such are holding "type", "comment", "timestamp", "location" (if available)

Log events are uploaded to the backend  when the soft limit for events is reached (>100 events are waiting to be uploaded to the server) or if the time interval exceeds 15 minutes. Location updates accuracy is set to < 50m and updates if the location has changed more than 5 meters. Location updates minimal time is set to 60 seconds.
In case of any failures the logs will be put back to the queue.

Everything is configurable from [Config.java](/sample-event-logging-library/src/main/java/mobi/lab/sample_event_logging_library/Config.java) sitting in the "__sample-event-logging-library__" library

__"backend"__ is in a file called api.php. It will return 200 OK with a 20% chance of 400 Bad Request in which case the client will have to retry later.

For library project:
  1. __TODO__: Location service is not fully functional, we are not getting enough location updates which can cause log events with no location. (current fallback for this is setting limit of tries to get location for each log event)
  2. __TODO__: Handle no connection -> in case of no connection there is no point to initiate logs sync request
  3. __TODO__: Handle no location permission situation (dialogs for asking permission)
  4. __TODO__: Look over the handler logic for time interval between requests -> [JobScheduler](/sample-event-logging-library/src/main/java/mobi/lab/sample_event_logging_library/Config.java) can be used from API level 21 (currently not available in support lib)
  5. __TODO__: Service can be improved, on some Android versions service might be killed and STICKY flag will not restart it(currently the service is not made as sticky) -> it has to be made persistent with AlarmManager.