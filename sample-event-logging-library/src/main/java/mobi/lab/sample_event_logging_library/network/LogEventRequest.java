package mobi.lab.sample_event_logging_library.network;

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

import java.util.ArrayList;

import mobi.lab.sample_event_logging_library.data.LogEvent;
import retrofit.Call;
import retrofit.http.Body;
import retrofit.http.POST;

public interface LogEventRequest {

    @POST("/~karlmartin/api/logs/save/")
    Call<Void> saveLogEvents(@Body ArrayList<LogEvent> logEvent);

}