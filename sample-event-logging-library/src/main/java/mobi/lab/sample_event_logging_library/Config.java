package mobi.lab.sample_event_logging_library;

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

public class Config {
    /**
     * Number of events which will automatically trigger an upload request
     */
    public static final int EVENTS_PER_REQUEST_SOFT_LIMIT = 100;
    /**
     * Time interval between upload requests (15 minutes)
     */
    public static final int MAX_TIME_INTERVAL_BETWEEN_REQUESTS = 15 * 60 * 1000; //minutes
    /**
     * Distance we count the location as detected / open
     */
    public static final float LOCATION_MAX_DISTANCE = 25; // meters
    /**
     * Update interval in milliseconds
     */
    public static final long LOCATION_UPDATES_MIN_TIME = 60l * 1000l; // milliseconds
    /**
     * Location difference in meters to trigger a location update
     */
    public static final float LOCATION_UPDATES_MIN_DISTANCE = 5f; // meters
    /**
     * Location is not accurate
     */
    public static final float LOCATION_IS_NOT_ACCURATE = Math.max(LOCATION_MAX_DISTANCE, 50); // Meters
    /**
     * Location is old
     */
    public static final long LOCATION_IS_OLD = 60l * 1000l; // milliseconds
    /**
     * Nr of tries to get the location, if this is exceeded then we will send the logEvent without a location
     */
    public static final long NR_OF_GET_LOCATION_TRIES = 48;
}