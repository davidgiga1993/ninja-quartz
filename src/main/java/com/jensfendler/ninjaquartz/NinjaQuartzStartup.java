package com.jensfendler.ninjaquartz;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import ninja.lifecycle.Start;

@Singleton
public class NinjaQuartzStartup
{
	@Inject
	private NinjaQuartzSchedulerRegistration schedulerRegistration;

	/**
	 * As one of the last steps of the ninja startup the actual schedulers are registered.
	 * Before this call the annotated methods just have been collected.
	 */
	@Start(order = 90)
	public void buildSchedulers()
	{
		schedulerRegistration.buildSchedulers();
	}
}
