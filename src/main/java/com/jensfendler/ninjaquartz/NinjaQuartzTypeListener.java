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
package com.jensfendler.ninjaquartz;

import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.jensfendler.ninjaquartz.annotations.QuartzSchedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * @author Jens Fendler
 */
public class NinjaQuartzTypeListener implements TypeListener
{

	static final Logger logger = LoggerFactory.getLogger(NinjaQuartzModule.class);

	/**
	 * The {@link NinjaQuartzSchedulerRegistration} as passed to the constructor.
	 */
	NinjaQuartzSchedulerRegistration scheduleHelper;

	public NinjaQuartzTypeListener(NinjaQuartzSchedulerRegistration scheduleHelper)
	{
		this.scheduleHelper = scheduleHelper;
	}

	/**
	 * @see com.google.inject.spi.TypeListener#hear(com.google.inject.TypeLiteral,
	 * com.google.inject.spi.TypeEncounter)
	 */
	public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter)
	{
		Class<?> clazz = type.getRawType();
		for (Method method : clazz.getMethods())
		{
			QuartzSchedule quartzSchedule = method.getAnnotation(QuartzSchedule.class);
			if (quartzSchedule == null)
			{
				continue;
			}

			logger.debug("Scheduling methods in class {}.", type.getRawType().getName());
			encounter.register(new QuartzScheduleInjectionListener<>(scheduleHelper));
		}
	}

	/**
	 * Listener to receive injection events on a per-instance basis. This will
	 * be used to do the scheduling (which requires a "target" instance to
	 * invoke the scheduled methods on.)
	 *
	 * @param <I>
	 * @author Jens Fendler
	 */
	private static class QuartzScheduleInjectionListener<I> implements InjectionListener<I>
	{

		private final NinjaQuartzSchedulerRegistration scheduleHelper;

		private QuartzScheduleInjectionListener(NinjaQuartzSchedulerRegistration scheduleHelper)
		{
			this.scheduleHelper = scheduleHelper;
		}

		/**
		 * Invoked by Guice after it injects the fields and methods of instance.
		 *
		 * @param injectee The object which has the scheduler annotation
		 * @see com.google.inject.spi.InjectionListener#afterInjection(java.lang.Object)
		 */
		public void afterInjection(final I injectee)
		{
			scheduleHelper.scheduleTarget(injectee);
		}
	}
}
