package main

import org.quartz.*
import java.util.*

// The `DisallowConcurrentExecution` constraint is based upon an instance definition
// (JobDetail), not on instances of the EndOfDayFetcher class.
// http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-03.html
@DisallowConcurrentExecution
class EndOfDayFetcher : Job {
    override fun execute(context: JobExecutionContext?) {
        if (context != null) {
            // TODO: log job key.
            // TODO: should a log message contain logger ID to understand context better?
            context.jobDetail.key
        }
        // The only type of exception (including RuntimeExceptions) that you are allowed
        // to throw from the execute method is the JobExecutionException.
        // Because of this, you should generally wrap the entire contents
        // of the execute method with a ‘try-catch’ block. You should also spend
        // some time looking at the documentation for the JobExecutionException,
        // as your job can use it to provide the scheduler various directives
        // as to how you want the exception to be handled.
        try {
            fetchIntradayData()
            fetchSummary()
        } catch (e: Exception) {

        }
    }
}

// http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-06.html
fun setupEndOfDayFetcher() {

    // TODO: don't fetch on market holidays, or stop fetching if the data is the same
    // TODO: as for the previous weekday.

    //    Field Name	 	Allowed Values	 	Allowed Special Characters
    //    Seconds           0-59                , - * /
    //    Minutes           0-59                , - * /
    //    Hours             0-23                , - * /
    //    Day-of-month      1-31                , - * ? / L W
    //    Month             0-11 or JAN-DEC     , - * /
    //    Day-of-Week       1-7 or SUN-SAT      , - * ? / L #
    //    Year (Optional)   empty, 1970-2199    , - * /

    // Examples:
    // Every 20 seconds: "0/20 * * * * ?"
    // Every other minute, starting at 15 seconds past the minute: "15 0/2 * * * ?"
    // Every other minute, between 8am and 5pm: "0 0/2 8-17 * * ?"
    // At 10am on the 1st and 15th days of the month: "0 0 10am 1,15 * ?"
    // Every 30 seconds on weekdays: "0,30 * * ? * MON-FRI"
    // Every 30 seconds on weekends: "0,30 * * ? * SAT,SUN"

    // See CronExpression class docs for a full description.

    val endOfDayFetcher: JobDetail = JobBuilder.newJob(EndOfDayFetcher::class.java)
            .withIdentity("endOfDayFetcher")
            .build()

    // Yahoo's intraday data starts at market open and ends at market close sharp,
    // without going into the extended hours territory, so we can safely fetch
    // soon after the market close.

    // 10 minutes after 4pm ET on weekdays.
    val endOfDaySchedule = CronScheduleBuilder.cronSchedule("0 10 4pm ? * MON-FRI")
            .inTimeZone(TimeZone.getTimeZone("America/New_York"))

    val endOfDayTrigger: CronTrigger = TriggerBuilder.newTrigger()
            .withIdentity("endOfDayTrigger")
            .withSchedule(endOfDaySchedule)
            .build()

    appScheduler.scheduleJob(endOfDayFetcher, endOfDayTrigger)
}