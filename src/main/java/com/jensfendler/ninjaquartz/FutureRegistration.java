package com.jensfendler.ninjaquartz;

import com.jensfendler.ninjaquartz.annotations.QuartzSchedule;

import java.lang.reflect.Method;

/**
 * Holds details to allow a delayed creation of the schedulers
 */
class FutureRegistration
{
	public final Object instance;
	public final Method method;
	public final QuartzSchedule annotation;

	FutureRegistration(Object instance, Method method, QuartzSchedule annotation)
	{
		this.instance = instance;
		this.method = method;
		this.annotation = annotation;
	}
}
