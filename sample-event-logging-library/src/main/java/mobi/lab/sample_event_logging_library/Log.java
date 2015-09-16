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

/**
 * Light-weight class for logging events with capability for different implementations<br/>
 * Allows to:<br/>
 * 1) Log an {@link mobi.lab.sample_event_logging_library.data.LogEvent} event.<br/>
 */
public abstract class Log {


	private static final String DEFAULT_TAG = "sample-logging-library";

	private String tag = DEFAULT_TAG;
	private static Class logImplementation = LogImplNetworkOptimized.class;

	/**
	 * Get log instance
	 *
	 * @param obj
	 * @return A new instance of the log implementation
	 */
	public static Log getInstance(final Object obj) {
		if (obj == null) {
			throw new RuntimeException("Object given to getInstance() was null");
		}
		return getInstance(obj.getClass());
	}

	/**
	 * Get log instance
	 *
	 * @param clazz
	 *            Name of this class used as the log tag
	 * @return A new instance of the log implementation
	 */
	public static Log getInstance(final Class clazz) {
		if (clazz == null) {
			throw new RuntimeException("Class given to getInstance() was null");
		}
		return getInstance(clazz.getName());
	}

	/**
	 * Get log instance
	 *
	 * @param tag
	 *            Log tag to use
	 * @return A new instance of the log implementation
	 */
	public static Log getInstance(final String tag) {
		try {
			final Log log = (Log) logImplementation.newInstance();
			log.setTag(tag);
			return log;
		} catch (InstantiationException e) {
			throw new RuntimeException(e.getClass() + " " + e.getMessage());
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e.getClass() + " " + e.getMessage());
		}
	}

	/**
	 * Set a custom log implementation.
	 *
	 * @param clazz
	 *            Implementation class to use
	 */
	public static void setImplementation(final Class clazz) {
		if (clazz == null) {
			logImplementation = LogImplNetworkOptimized.class;
			return;
		}
		logImplementation = clazz;
	}

	protected Log() {
	}


	protected Log(final String tag) {
		setTag(tag);
	}

	protected final void setTag(final String tag) {
		this.tag = tag;
	}

	/**
	 * Log a simple message
	 *
	 * @param type
	 *            Log event type
	 * @param content
	 *            Log event content
	 */
	public final void m(Context context, final int type, final String content) {

		synchronized (logImplementation) {
			message(context, type, content == null ? "null" : content);
		}
	}

	/**
	 * Override for scrolls to info level. This method is called by holding lock to object set with <code>setImplementation(Class)</code>
	 *
	 * @param type
	 *            Type of log event
	 * @param content
	 *            Content of log event
	 */
	abstract protected void message(Context context, final int type, final String content);

}
