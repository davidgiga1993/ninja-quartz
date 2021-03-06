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
package com.jensfendler.ninjaquartz.annotations;

import com.jensfendler.ninjaquartz.NinjaQuartzSchedulerRegistration;

import org.quartz.CronExpression;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Jens Fendler
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QuartzSchedule
{
	String DEFAULT_JOB_NAME = "_noJobName";

	String DEFAULT_JOB_GROUP = "_noJobGroup";

	String DEFAULT_JOB_DESCRIPTION = "_noJobDescription";

	boolean DEFAULT_JOB_RECOVERY = true;

	boolean DEFAULT_JOB_DURABILITY = false;

	String DEFAULT_TRIGGER_NAME = "_noTriggerName";

	String DEFAULT_TRIGGER_GROUP = "_noTriggerGroup";

	int MISFIRE_POLICY_DO_NOTHING = 1;

	int MISFIRE_POLICY_FIRE_AND_PROCEED = 2;

	int MISFIRE_POLICY_IGNORE = 3;

	int DEFAULT_MISFIRE_POLICY = MISFIRE_POLICY_DO_NOTHING;

	int DEFAULT_SCHEDULER_DELAY = -1;

	String DEFAULT_TRIGGER_START_AT = "_noTriggerStartAt";

	String DEFAULT_TRIGGER_END_AT = "_noTriggerEndAt";

	boolean DEFAULT_ALLOW_PARALLEL_INVOCATIONS = false;

	boolean DEFAULT_PERSISTENT = false;

	boolean DEFAULT_REMOVE_ON_ERROR = false;

	boolean DEFAULT_FORCE_KEEP = false;

	/**
	 * The group name of the trigger to use for the scheduled method.
	 *
	 * @return the name of the trigger group
	 */
	String triggerGroup() default DEFAULT_TRIGGER_GROUP;

	/**
	 * The name of the trigger to use for the scheduled method.
	 *
	 * @return the name of the trigger
	 */
	String triggerName() default DEFAULT_TRIGGER_NAME;

	/**
	 * A datetime string in the format
	 * {@link NinjaQuartzSchedulerRegistration#TRIGGER_DATETIME_FORMAT} indicating when
	 * the trigger should end.
	 *
	 * @return the datetime string to end the trigger
	 * @see TriggerBuilder#endAt(java.util.Date)
	 */
	String triggerEndAt() default DEFAULT_TRIGGER_END_AT;

	/**
	 * This string defines the UNIX Cron-like execution
	 * schedule to set for an annotated method.
	 *
	 * @return the cron expression defining the schedule, <em>or</em> the key
	 * name of a Ninja property (from application.conf) that contains a
	 * Cron Schedule string.
	 * @see CronExpression
	 */
	String cronSchedule();

	/**
	 * The name of the {@link Job} to run for the scheduled method.
	 *
	 * @return the name of the job
	 */
	String jobName() default DEFAULT_JOB_NAME;

	/**
	 * The group name of the {@link Job} to run for the scheduled method.
	 *
	 * @return the group name of the job
	 */
	String jobGroup() default DEFAULT_JOB_GROUP;

	/**
	 * The (optional) description of the {@link Job} to run for the scheduled
	 * method.
	 *
	 * @return the description of the job
	 */
	String jobDescription() default DEFAULT_JOB_DESCRIPTION;

	/**
	 * The recovery strategy for the {@link Job}.
	 *
	 * @return the recovery property of the job.
	 * @see JobBuilder#requestRecovery(boolean)
	 */
	boolean jobRecovery() default DEFAULT_JOB_RECOVERY;

	/**
	 * The durability strategy for the {@link Job}.
	 *
	 * @return the durability property of the job.
	 * @see JobBuilder#storeDurably(boolean)
	 */
	boolean jobDurability() default DEFAULT_JOB_DURABILITY;

	/**
	 * A datetime string in the format
	 * {@link NinjaQuartzSchedulerRegistration#TRIGGER_DATETIME_FORMAT} indicating when
	 * the trigger should start.
	 *
	 * @return the datetime string to start the trigger at
	 * @see TriggerBuilder#startAt(java.util.Date)
	 */
	String triggerStartAt() default DEFAULT_TRIGGER_START_AT;

	/**
	 * The priority for the {@link Trigger} to use for the scheduling.
	 *
	 * @return the priority level of the trigger
	 * @see TriggerBuilder#withPriority(int)
	 */
	int triggerPriority() default Trigger.DEFAULT_PRIORITY;

	/**
	 * The initial delay (in seconds) before the scheduler should start
	 * running after initialisation. By default the Scheduler will start without
	 * delay.
	 * This will overwrite {@link #triggerStartAt()}.
	 *
	 * @return the initial delay (seconds) of the scheduler
	 */
	int schedulerDelay() default DEFAULT_SCHEDULER_DELAY;

	/**
	 * The misfire strategy to use if the {@link CronTrigger} misfires.
	 *
	 * @return the misfire policy to use for the cron trigger
	 * @see CronScheduleBuilder#withMisfireHandlingInstructionDoNothing()
	 * @see CronScheduleBuilder#withMisfireHandlingInstructionFireAndProceed()
	 * @see CronScheduleBuilder#withMisfireHandlingInstructionIgnoreMisfires()
	 */
	int cronScheduleMisfirePolicy() default DEFAULT_MISFIRE_POLICY;

	/**
	 * NinjaQuartz tries to prevent multiple (running in parallel) invocations
	 * of the same scheduled method in different worker threads. If such
	 * parallel invocations are not a problem for you (or you need this), set
	 * this property to true. In most cases, allowing this is probably a bad
	 * idea.
	 *
	 * @return the allowConcurrent property of the job
	 */
	boolean allowConcurrent() default DEFAULT_ALLOW_PARALLEL_INVOCATIONS;

	/**
	 * Support for stateful jobs, whose {@link JobDataMap} is kept between
	 * incovations. Defaults to false.
	 *
	 * @return the persistent property of the job
	 */
	boolean persistent() default DEFAULT_PERSISTENT;

	/**
	 * If this argument is set to <code>true</code>, the job will be immediately
	 * removed from the scheduler after a {@link InvocationTargetException} is
	 * thrown by the scheduled method.
	 * <p>
	 * If set to <code>false</code> (the default), the job will remain
	 * scheduled, and thrown exceptions will be logged briefly at WARN level.
	 *
	 * @return the removeOnError property of the job
	 */
	boolean removeOnError() default DEFAULT_REMOVE_ON_ERROR;

	/**
	 * If this argument is set to <code>true</code>, this job will remain in the
	 * scheduler despite <em>any</em> exception that might be thrown during
	 * attempts to execute the scheduled method.
	 * <p>
	 * If it is set to <code>false</code> (the default), all (see
	 * {@link #removeOnError()} for an exception to this rule) exceptions thrown
	 * will result in the scheduled method being removed from the scheduler.
	 *
	 * @return the forceKeep property of the job
	 */
	boolean forceKeep() default DEFAULT_FORCE_KEEP;

}
