package mobi.lab.sample_event_logging_library.data;

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

import android.location.Location;

/**
 * Data class for the log event
 */
public class LogEvent {

    public static final int TYPE_FOO = 0;
    public static final int TYPE_BAR = 1;

    private int type;
    private String content;
	private Location location;

    /**
	 * Holds the number of retries to get the location, if this exceeds {@link mobi.lab.sample_event_logging_library.Config#NR_OF_GET_LOCATION_TRIES} then
	 * event will be synced to server
	 */
	private int nrOfTriesForLocation;

    private long timestamp;

	public LogEvent(int type, String content) {
		this.type = type;
		this.content = content;
		this.location = null;
        this.timestamp = System.currentTimeMillis();
        this.nrOfTriesForLocation = 0;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Location getLocation() {
		return location;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

    public int getNrOfTriesForLocation() {
        return nrOfTriesForLocation;
    }

    public void increaseNrOfTriesForLocation() {
        this.nrOfTriesForLocation++;
    }

    @Override
    public String toString() {
        return "LogEvent{" +
                "type=" + type +
                ", content='" + content + '\'' +
                ", location=" + location +
                ", timestamp=" + timestamp +
                ", nrOfTriesForLocation=" + nrOfTriesForLocation +
                '}';
    }

}
