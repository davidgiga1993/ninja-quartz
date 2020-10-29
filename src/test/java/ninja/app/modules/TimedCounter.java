/*
 * Copyright 2016 Fendler Consulting cc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ninja.app.modules;

import com.google.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class TimedCounter
{
	private final Map<String, Ticks> valueMap = new HashMap<>();

	/**
	 * Update (or initialize as 0) a value in the map and return the new value.
	 *
	 * @param key Key
	 * @return Count
	 */
	public Ticks increment(String key)
	{
		// Get time before synchronization to keep timestamp accurate
		long timestamp = System.nanoTime();
		synchronized (valueMap)
		{
			return valueMap.compute(key, (s, ticks) -> ticks == null ? new Ticks().inc(timestamp) : ticks.inc(timestamp));
		}
	}

	/**
	 * Return a value from the map.
	 *
	 * @param key Key
	 * @return Count value
	 */
	public Ticks getValue(String key)
	{
		synchronized (valueMap)
		{
			return valueMap.get(key);
		}
	}

	public Map<String, Ticks> getAll()
	{
		return valueMap;
	}

	public static class Ticks
	{
		public List<Long> timestamps = new ArrayList<>();

		public Ticks inc(long timestamp)
		{
			timestamps.add(timestamp);
			return this;
		}

		@Override
		public String toString()
		{
			if (timestamps.size() == 0)
			{
				return "Ticks (empty)";
			}
			return "Ticks (" + timestamps.size() + ", latest: " + timestamps.get(timestamps.size() - 1) + ")";
		}
	}
}
