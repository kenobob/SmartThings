/**
 *  Smart Block Heater
 *
 *  Copyright 2017 kenobob
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Smart Block Heater",
    namespace: "kenobob",
    author: "kenobob",
    description: "This will be a smart block heater",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("On for this amount of time") {
        input (name: "minutes", type: "number", title: "Minutes?", required: true)
    }
    section("Temperature You want to turn on at.") {
        input (name: "onTemperature", type: "number", title: "Temp?", required: true)
    }
    //Time or Mode, not sure yet.
    section("When does your quiet hours start?") {
        input "beforeBedNotificationTime", "time", title: "Time?", required: true
    }
    section("When do you need your car to start?") {
        input "carStartTime", "time", title: "Time?", required: true
    }
    section("Which Switch is your block heater plugged into?") {
        input "switches", "capability.switch", multiple: true, required: true
    }
    section("Temperature Sensor"){
        input "bwsTemperatureMeasurement", "capability.temperatureMeasurement", multiple: false, required: true
    }
    section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phoneNumber", "phone", title: "Send a Text Message?", required: false
        }
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    // remove all scheduled executions for this SmartApp install
    unschedule()
    // unsubscribe all listeners.
    unsubscribe()
    // re-initialize the smartapp with new options
    initialize()
}

def initialize() {
    log.trace("Executing Initialize")
    createSubscriptions()
	
    //Schedule back up the daily check
    createDailyScheduler()
	
    //TODO Remove after testing
    checkCreateScheduler()
	
    log.trace("End Initialize")
}


private def createSubscriptions()
{
    log.trace("Executing Create Subscriptions")
    
    //Subscribe to BWS low tempurature change.
    subscribe(bwsTemperatureMeasurement, "lowtemperature", lowForecastedTemperatureChanges)
    
    //	subscribe(motionSensors, "motion.active", motionActiveHandler)
    //	subscribe(motionSensors, "motion.inactive", motionInactiveHandler)
    //	subscribe(switches, "switch.off", switchOffHandler)
    //	subscribe(location, modeChangeHandler)
    //
    //	if (state.modeStartTime == null) {
    //		state.modeStartTime = 0
    //	}
    log.trace("End Create Subscriptions")
}


// TODO: implement event handlers
def lowForecastedTemperatureChanges(evt){
    
    log.trace("Executing lowForecastedTemperatureChanges")
    log.debug ("The Low Changed To: ${evt.numericValue}")
    
    if(evt.numericValue <= onTemperature){
        def todaysDate = convertDatetoISODateString(getJustDate(new Date()))
        if(state.lastActiveScheduleDate != todaysDate && state.onTimeRunOnceDate != todaysDate){
            //The low tempurature is going to be cold enough we want to turn on switch.
            log.info("state.lastActiveScheduleDate: ${state.lastActiveScheduleDate}, todays date: ${todaysDate}, state.onTimeRunOnceDate: ${state.onTimeRunOnceDate}")
            log.info("Forecast Low is going to be below threshold")
            checkCreateScheduler()
			
            //Save last scheduled date for later comparisons.
            state.lastActiveScheduleDate = convertDatetoISODateString(getJustDate(new Date()))
        } else {
            log.info("Already Scheduled, no need to re-schedule.")
        }
    } else {
        //I scheduled something for today, but I don't need to any more, the low changed
        if(state.lastActiveScheduleDate == convertDatetoISODateString(getJustDate(new Date())))//new Date().toLocalDate())
        {
            clearTodyasSchedules()
        }
			
        log.info("Nice and warm, no worries.")
    }
    
    
    log.trace("End lowForecastedTemperatureChanges")
}

private def createDailyScheduler(){
    log.trace("Executing createDailyScheduler")
	
    def onTime = CalculateReOccuringOnTime()
	
    log.debug("Set Daily On Time: ${onTime}")
	
    //Only the Time is used for the date object
    schedule(onTime, justInCaseCheck)
	
    log.trace("End createDailyScheduler")
}

private def checkCreateScheduler(){
    log.trace("Executing checkCreateScheduler")
	
    //I'm out of scheduled events somehow, clear them out!
    if(!canSchedule()){
        log.debug("Scheduler Full, clear them out and let's start over")
        clearAllSchedules()
    }
	
    
    def onTime = CalculateOnTime2()
    
    log.debug("Block Heater On Time: ${onTime}")
    
    createNotificationScheduler()
	
    //    def isTypeOfDate = (onTime instanceof Date)
    //    log.info("onTIme is Type of Date? - ${isTypeOfDate}")
    //    java.lang.String onTimeString = convertDatetoISODateString(onTime)
    //    log.debug("OnTime String ${onTimeString}")
    //    if(onTimeString && onTimeString instanceof String){
    //create scheduler to turn on block hearter(s)
    runOnce(onTime, checkThenTurnOnSwitch)
    //    } else {
    //        log.error("On Time String is Maybe NOt INstace of String")
    //        log.error("On Time String is ${onTimeString}")
    //        runOnce(onTime, checkThenTurnOnSwitch)
    //    }
	
    state.onTimeRunOnceDate =  convertDatetoISODateString(getJustDate(onTime))
    log.trace("End checkCreateScheduler")
}

def createNotificationScheduler(){
    log.trace("Executing createNotificationScheduler")
    if(!isQuietHours())
    {
        log.debug("Set Notification time for ${beforeBedNotificationTime}")
        //Create Reminder to Plug in Car.
        runOnce(beforeBedNotificationTime, notifyUserToPlugIn)
    } else {
        log.info("SSSHHHH No Notification, it's in the quiet times.")
    }
    log.trace("end createNotificationScheduler")
}

//Has to be public because the scheduler is calling it
def notifyUserToPlugIn(){
    log.trace("Executing notifyUserToPlugIn")
    log.info("Push Notification Selection ${sendPushMessage}")
    if(sendPushMessage != null && sendPushMessage == "Yes"){
        //sendPush("Plug in your block heater.")
        log.debug("Push Notification: 'Plug in your block heater.'")
    }
    if(phoneNumber){
        sendSms(phoneNumber, "Plug in your block heater.")
    }
    log.trace("End notifyUserToPlugIn")
}

def checkThenTurnOnSwitch(){
    log.trace("Executing checkThenTurnOnSwitch")
    def currentTemp = getCurrentTemp()
	
    log.info("Current Temp: ${currentTemp}, On Temp: ${onTemperature}")
	
    //Last Minute Check of tempatrue before turning on
    if(currentTemp <= onTemperature){
        log.info("Turning the Outlets on")
        switches.on()
        //Not sure I'll need state as I probably won't be turning them off in this app... maybe?
        //state.outlets = "on"
    } else{
        log.debug("It's too warm right now, don't need to turn on")
    }
	
    state.onTimeRunOnceDate = null
	
    log.trace("End checkThenTurnOnSwitch")
}

def justInCaseCheck(){
    log.trace("Executing justInCaseCheck")
    //Just in case the estimated low is totally differnt than the real temp at start time, lets check.
    checkThenTurnOnSwitch()
    log.trace("End justInCaseCheck")
}

private def CalculateReOccuringOnTime(){
    log.trace("Executing CalculateReOccuringOnTime")
	
    //Convert the start time to Calendar
    def carStartTimeCal = convertDateToCalendar(convertISODateStringToDate(carStartTime))
    carStartTimeCal.set(Calendar.MINUTE, carStartTimeCal.get(Calendar.MINUTE)-minutes)
	
    //Turn back to a date
    def rtvDate = carStartTimeCal.getTime()
    log.trace("End CalculateReOccuringOnTime")
    return rtvDate
}

private def isQuietHours(){
    
    //Convert Everything to Calendars
    def startCalendar = convertDateToCalendar(beforeBedNotificationTime)
    def currentCalendar = convertDateToCalendar(new Date())
    def endCalendar = convertDateToCalendar(carStartTime)
    
    //Convert to minutes for easy comparison
    def startMinutes = startCalendar.get(Calendar.MINUTE) + (startCalendar.get(Calendar.HOUR_OF_DAY) * 60);
    def endMinutes = endCalendar.get(Calendar.MINUTE) + (endCalendar.get(Calendar.HOUR_OF_DAY) * 60);
    def currentMinutes = currentCalendar.get(Calendar.MINUTE) + (currentCalendar.get(Calendar.HOUR_OF_DAY) * 60);
    log.debug("Minutes: start: ${startMinutes} end: ${endMinutes} current: ${currentMinutes}")
    
    if(startMinutes > endMinutes)
    {
        log.info("Quiet Hours Span A Day Into the future")
        //Assuming seperate days
        if(startMinutes < currentMinutes || currentMinutes < endMinutes){
            //Assuming we crossed one day into the future
            return true
        } else{
            return false
        }
    } else {
        log.info("Quiet Hours Are on the Same Day")
        //Assume the same day
        if(startMinutes < currentMinutes && currentMinutes < startMinutes){
            return true
        } else {
            return false
        }
    }
}

private def CalculateOnTime2(){
    log.trace("Executing CalculateOnTime2")
    //TODO Do SOMETHING
    /* Some thoughts
     * - If start time is before Noon
     * - If wakeup time is after noon
     *    - If so following assumptions apply
     * - If even triggers before midnight, but after car start time, assume tomrrow
     * - If even triggers after midnight, but before car start time, assume today
     */
	
    //Grab the current time
    def currentTimeCal = convertDateToCalendar(new Date())
	
    //Convert the start time to Calendar
    def carStartTimeCal = convertDateToCalendar(convertISODateStringToDate(carStartTime))
	
    //Convert the notification time to Calendar
    def beforeBedNOtificationCal = convertDateToCalendar(convertISODateStringToDate(beforeBedNotificationTime))
	
    def carOnTimeCal = convertDateToCalendar(new Date())
	
    //Ensure the days are correct
    def isCarStartTomorrow = false
    //Logic For Tomorrow ... the On Time needs to be less than the current time
    //aka it's already past start time....
    if(//currentTimeCal.get(Calendar.HOUR_OF_DAY) < beforeBedNOtificationCal.get(Calendar.HOUR_OF_DAY) && 
        currentTimeCal.get(Calendar.HOUR_OF_DAY) > carStartTimeCal.get(Calendar.HOUR_OF_DAY))
    {
        isCarStartTomorrow = true
    }
    
    
	
    log.debug("CalculateOnTime2 - Settings Car Start Time: ${convertISODateStringToDate(carStartTime)}")
    //Make sure date month and year are correct
    carOnTimeCal.set(Calendar.DATE, currentTimeCal.get(Calendar.DATE))
    carOnTimeCal.set(Calendar.YEAR, currentTimeCal.get(Calendar.YEAR))
    carOnTimeCal.set(Calendar.MONTH, currentTimeCal.get(Calendar.MONTH))
    
    //Calculate the Date to make sure it's up to-date: TODO Remove
    def throwMeAway = carOnTimeCal.getTime()
	
    //Set hour and minute
    carOnTimeCal.set(Calendar.HOUR_OF_DAY, carStartTimeCal.get(Calendar.HOUR_OF_DAY))	
    carOnTimeCal.set(Calendar.MINUTE, carStartTimeCal.get(Calendar.MINUTE)-minutes)
	
    //Correct any date offset needed
    if(isCarStartTomorrow && carOnTimeCal.get(Calendar.DATE) <= currentTimeCal.get(Calendar.DATE)){
        log.debug("Move Date to tomorrow")
        carOnTimeCal.set(Calendar.DATE, currentTimeCal.get(Calendar.DATE)+1)
    }
	
    //Check for scheduling in the past problems.
    //if(carOnTimeCal.get(Calendar.DATE) < currentTimeCal.get(Calendar.DATE)){
    //	if(carStartTimeCal.get(Calendar.HOUR_OF_DAY) > carOnTimeCal.get(Calendar.HOUR_OF_DAY)) {
    //		log.info("We are somehow late!")
    //	} else {
    //		log.error("UH HO! We are in the past!")
    //	}
    //SmartThings Build in function
    //def isBetweenTime = timeOfDayIsBetween(carOnTimeCal.getTime(), convertISODateStringToDate(carStartTime), new Date(), location.timeZone)
    //TODO Fix this edge case
    //}
	
    //Turn back to a date
    def rtvDate = carOnTimeCal.getTime()
    log.debug("CalculateOnTime2 - Blockheater On Time: ${rtvDate}")
    
    //log.info("Start Time: ${rtvDate}")
	
    log.trace("End CalculateOnTime2")
    return rtvDate
	
}

private def convertISODateStringToDate(String date){
    try{
        return Date.parse( "yyyy-MM-dd'T'HH:mm:ss.SSSX", date )
    }catch(def e){
        log.error(e)
        return null
    }
}

private def convertDatetoISODateString(Date date){
    try{
        log.debug("Convert Date to String: ${date}")
        def formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
        
        //formatter.setTimeZone(location.timeZone)
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
                
        def rtv = formatter.format(date)
                
        log.debug("Convert Date to String Converted: ${rtv}")
        
        return rtv
    }catch(def e){
        log.error(e)
    }
}

//Convert from string to date.
private def convertDateToCalendar(String date){
    Calendar rtv = null
	
    def d = convertISODateStringToDate(date)
	
    if(d != null){
        rtv = convertDateToCalendar(d) 
    } else {
        log.error("convertDateToCalendar string is null: ${date}")
    }

    return rtv
}

private def convertDateToCalendar(Date date){
    def cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    cal.setTime(date)
	
    //Now Set time from date, have to do it manually - Assume date coming in has been converted to UTC
    //    cal.set(Calendar.DATE, date.getDate())
    //    cal.set(Calendar.MONTH, date.getMonth())
    //    cal.set(Calendar.YEAR, date.getYear())
    //    cal.set(Calendar.HOUR_OF_DAY, date.getHours())
    //    cal.set(Calendar.MINUTE, date.getMinutes())
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    
    return cal
}

private def getJustDate(date){
	
    //Quick null check
    if(date == null){
        return null
    }
	
    
    def cal = null
    if (date instanceof Date) {
        //get unix time
        cal = convertDateToCalendar(date)
    } else if(date instanceof Calendar){
        cal = date
    }
	
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
	
    //Turn back to a date
    def dateWithoutTime = cal.getTime()
    
    return dateWithoutTime
}

private def getCurrentTemp(){
    return bwsTemperatureMeasurement.latestValue("temperature")
}

private def clearAllSchedules(){
    log.trace("Executing clearAllSchedules")
    // remove all scheduled executions for this SmartApp install
    unschedule()
	
    //Schedule back up the daily check
    createDailyScheduler()
	
    log.trace("End clearAllSchedules")
}

private def clearTodyasSchedules(){
    log.trace("Executing clearTodyasSchedules")
    // unschedule the notification
    unschedule(notifyUserToPlugIn)
    unschedule(checkThenTurnOnSwitch)
    
    //Reset State Elements
    state.lastActiveScheduleDate = null 
    state.onTimeRunOnceDate = null
    
    log.trace("End clearTodyasSchedules")
}