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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jensfendler.ninjaquartz.annotations.QuartzSchedule;

import org.quartz.JobExecutionContext;

import ninja.app.controllers.Application;
import ninja.lifecycle.Start;
import ninja.utils.NinjaProperties;

@Singleton
public class TestSchedules
{
	public static final String SCHEDULE_TEST_1 = "scheduleTest1";
	public static final String SCHEDULE_TEST_2 = "scheduleTest2";
	public static final String SCHEDULE_TEST_3 = "scheduleTest3";
	public static final String SCHEDULE_TEST_4 = "scheduleTest4";
	public static final String SCHEDULE_TEST_5 = "scheduleTest5";
	public static final String[] SCHEDULE_TESTS = new String[]{SCHEDULE_TEST_1, SCHEDULE_TEST_2, SCHEDULE_TEST_3, SCHEDULE_TEST_4, SCHEDULE_TEST_5};

	public static final String NINJA_START = "start";

	@Inject
	protected TimedCounter counter;

	@Start(order = 90)
	public void start()
	{
		counter.increment(NINJA_START);
		// Simulate a slow startup
		try
		{
			Thread.sleep(2000);
		}
		catch (InterruptedException e)
		{
			// Ignore
		}
	}

	/**
	 * Run every 2 seconds 1 sec delay, no arguments
	 */
	@QuartzSchedule(cronSchedule = "0/2 * * * * ?", schedulerDelay = 1, jobDescription = "Test Schedule 1", jobName = "test1")
	public void schedule1()
	{
		TimedCounter.Ticks ticks = counter.increment(SCHEDULE_TEST_1);
		Application.LOG.info("testSchedule1() updated value to {}", ticks);
	}

	/**
	 * Run every 2 seconds, 2 sec delay, JobExecutionContext argument
	 */
	@QuartzSchedule(cronSchedule = "0/2 * * * * ?", schedulerDelay = 2, jobDescription = "Test Schedule 2", jobName = "test2")
	public void schedule2(JobExecutionContext context)
	{
		TimedCounter.Ticks ticks = counter.increment(SCHEDULE_TEST_2);
		Application.LOG.info("testSchedule2() updated value to {}. Description={}", ticks,
				context.getJobDetail().getDescription());
	}

	/**
	 * Run every 5 seconds (configured in application.conf), JobExecutionContext argument
	 */
	@QuartzSchedule(cronSchedule = "schedule.testSchedule3", schedulerDelay = 3, jobDescription = "Test Schedule 3", jobName = "test3")
	public void schedule3(JobExecutionContext context)
	{
		TimedCounter.Ticks ticks = counter.increment(SCHEDULE_TEST_3);
		Application.LOG.info("testSchedule3() updated value to {}. Description={}", ticks,
				context.getJobDetail().getDescription());
	}

	/**
	 * Runs every 2 sec after 4 sec delay
	 */
	@QuartzSchedule(cronSchedule = "0/2 * * * * ?", schedulerDelay = 4, jobDescription = "Test Schedule 4", jobName = "test4")
	public void schedule4(NinjaProperties ninjaProperties)
	{
		TimedCounter.Ticks ticks = counter.increment(SCHEDULE_TEST_4);
		Application.LOG.info("testSchedule4() updated value to {}. Available ninjaProperties: {}", ticks,
				ninjaProperties.getAllCurrentNinjaProperties().size());
	}

	/**
	 * Runs every 2 sec after 5 sec delay
	 */
	@QuartzSchedule(cronSchedule = "0/2 * * * * ?", schedulerDelay = 5, jobDescription = "Test Schedule 5", jobName = "test5")
	public void schedule5(NinjaProperties ninjaProperties, JobExecutionContext context)
	{
		TimedCounter.Ticks ticks = counter.increment(SCHEDULE_TEST_5);
		Application.LOG.info(
				"testSchedule5() updated value to {}. Context: {}, Available ninjaProperties: {}", ticks,
				context.hashCode(), ninjaProperties.getAllCurrentNinjaProperties().size());
	}
}
