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
package com.jensfendler.ninjaquartz.test;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import ninja.app.controllers.Application;
import ninja.app.modules.TestSchedules;
import ninja.app.modules.TimedCounter;
import ninja.standalone.Standalone;
import ninja.standalone.StandaloneHelper;
import ninja.utils.NinjaMode;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;


import static ninja.app.modules.TestSchedules.NINJA_START;
import static ninja.app.modules.TestSchedules.SCHEDULE_TESTS;
import static ninja.app.modules.TestSchedules.SCHEDULE_TEST_1;
import static ninja.app.modules.TestSchedules.SCHEDULE_TEST_5;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This test runs a ninja server which has some defined schedulers.
 * After a certain amount of time the scheduler calls are inspected to
 * verify if the scheduler did the expected things
 */
public class NinjaQuartzIntegrationTest
{
	private static Standalone<?> standalone;
	private static OkHttpClient client;
	private static okhttp3.Request.Builder requestBuilder;

	private static final ObjectMapper mapper = new ObjectMapper();

	@SuppressWarnings("rawtypes")
	@BeforeAll
	static public void beforeClass() throws Exception
	{
		// start ninja application
		Application.LOG.info("Starting Ninja server...");
		int randomPort = StandaloneHelper.findAvailablePort(8090, 9000);
		Class<? extends Standalone> standaloneClass = StandaloneHelper.resolveStandaloneClass();
		standalone = StandaloneHelper.create(standaloneClass).port(randomPort);
		standalone.ninjaMode(NinjaMode.test).configure().start();

		// prepare http client
		Application.LOG.info("Initializing OKHttpClient...");
		requestBuilder = new Request.Builder();
		HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
		loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
		client = new OkHttpClient.Builder().followRedirects(true).addInterceptor(loggingInterceptor).build();

	}

	@AfterAll
	static public void afterClass()
	{
		// clean application shutdown
		standalone.shutdown();
	}

	@Test
	public void application() throws Exception
	{
		Response response = requestGet("/");
		assertTrue(response.isSuccessful(), "Index page failed with code " + response.code());
	}

	@Test
	public void schedulers() throws Exception
	{
		Application.LOG.info("Pausing 15s for Quartz to run scheduled methods...");
		// give the scheduler some time to run the test methods
		Thread.sleep(15000);
		Application.LOG.info("Done pausing.");

		Response response = requestGet("/schedules");
		assertTrue(response.isSuccessful(), "Schedules page failed with code " + response.code());

		String jsonStr = response.body().string();
		Application.TicksDto replyDto = mapper.readValue(jsonStr, Application.TicksDto.class);

		// Make sure the first scheduler did not run before ninja has been fully started
		long ninjaStartup = replyDto.ticks.get(NINJA_START).timestamps.get(0);
		long firstSchedule = replyDto.ticks.get(SCHEDULE_TEST_1).timestamps.get(0);
		assertTrue(ninjaStartup < firstSchedule, "Scheduler before ninja startup");

		for (String name : SCHEDULE_TESTS)
		{
			TimedCounter.Ticks ticks = replyDto.ticks.get(name);
			// Each scheduler should have executed least twice
			assertNotNull(ticks, name + " did not run");
			assertTrue(ticks.timestamps.size() > 1, name + " did not run twice");
		}

		for (String name : SCHEDULE_TESTS)
		{
			TimedCounter.Ticks ticks = replyDto.ticks.get(name);
			assertDelta(name, ticks, 2);
		}

		// The last scheduler has a startup delay of 5 seconds
		// Since the first scheduler has a delay of 1
		TimedCounter.Ticks scheduler1 = replyDto.ticks.get(TestSchedules.SCHEDULE_TEST_1);
		long delta = replyDto.ticks.get(SCHEDULE_TEST_5).timestamps.get(0) - scheduler1.timestamps.get(0);
		// So there should be roughly a 4 sec delay
		assertEquals(4, TimeUnit.NANOSECONDS.toSeconds(delta), 1, "Startup delay not working");
	}

	/**
	 * Checks if the given ticks are roughly the given number of seconds apart
	 *
	 * @param ticks   Ticks
	 * @param seconds Seconds
	 */
	private void assertDelta(String schedulerName, TimedCounter.Ticks ticks, int seconds)
	{
		long previous = ticks.timestamps.get(0);
		for (int X = 1; X < ticks.timestamps.size(); X++)
		{
			long timestamp = ticks.timestamps.get(X);
			long millisDelta = TimeUnit.NANOSECONDS.toMillis(timestamp - previous);
			// Allow 1sec drift
			assertEquals(0, Math.abs(TimeUnit.SECONDS.toMillis(seconds) - millisDelta), 1000,
					"Time delta too long for " + schedulerName + ", tick " + X);
			previous = timestamp;
		}
	}


	protected Response requestGet(String appPath) throws IOException
	{
		Request request = buildGetRequest(appPath);
		return client.newCall(request).execute();
	}

	protected Request buildGetRequest(String appPath)
	{
		return requestBuilder.get().url(standalone.getBaseUrls().get(0) + appPath).build();
	}
}
