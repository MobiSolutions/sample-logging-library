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

import android.content.Context;
import android.content.Intent;

import mobi.lab.sample_event_logging_library.service.LogPostService;

/**
 * Simple Log implementation that sends logs to web backend.<br/>
 * Doesn't need init().
 */
public class LogImplNetworkOptimized extends Log {

	@Override
	protected void message(Context context, int type, String content) {
		Intent intent = new Intent(context, LogPostService.class);
		intent.putExtra(LogPostService.EXTRA_EVENT_TYPE, type);
        intent.putExtra(LogPostService.EXTRA_EVENT_CONTENT, content);
		context.startService(intent);
	}

}
