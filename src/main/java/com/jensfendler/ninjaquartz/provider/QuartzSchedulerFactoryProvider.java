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
package com.jensfendler.ninjaquartz.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.jensfendler.ninjaquartz.NinjaQuartzModule;

import org.quartz.SchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

import ninja.utils.NinjaProperties;

/**
 * @author Jens Fendler
 */
@Singleton
public class QuartzSchedulerFactoryProvider implements Provider<SchedulerFactory>
{
	protected static final Logger logger = LoggerFactory.getLogger(NinjaQuartzModule.class);

	private final NinjaProperties ninjaProperties;

	private static SchedulerFactory schedulerFactory;

	@Inject
	public QuartzSchedulerFactoryProvider(NinjaProperties ninjaProperties)
	{
		logger.info("Initialising {}.", getClass().getName());
		this.ninjaProperties = ninjaProperties;
	}

	/**
	 * @see com.google.inject.Provider#get()
	 */
	public SchedulerFactory get()
	{
		logger.debug("{} called to get SchedulerFactory.", getClass().getName());
		if (schedulerFactory == null)
		{
			loadSchedulerFactory();
		}
		return schedulerFactory;
	}

	/**
	 * Instantiate the configured {@link SchedulerFactory} class.
	 */
	private void loadSchedulerFactory()
	{
		String sfClassName = ninjaProperties.getWithDefault("quartz.schedulerFactory",
				"org.quartz.impl.StdSchedulerFactory");
		logger.info("Using Quartz SchedulerFactory from {}.", sfClassName);

		try
		{
			Class<?> sfClass = Class.forName(sfClassName);
			schedulerFactory = (SchedulerFactory) sfClass.getConstructor().newInstance();
		}
		catch (ClassNotFoundException e)
		{
			throw new RuntimeException("Quartz SchedulerFactory class '" + sfClassName + "' not found.", e);
		}
		catch (InstantiationException | NoSuchMethodException | InvocationTargetException e)
		{
			throw new RuntimeException("Cannot instantiate Quartz SchedulerFactory class '" + sfClassName + "'.", e);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException("Illegal access exception while trying to instantiate Quartz SchedulerFactory class '" + sfClassName + "'.", e);
		}
	}

}
