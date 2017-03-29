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

    unsubscribe()
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
		def todaysDate = getJustDate(new Date())
		if(state.lastActiveScheduleDate != todaysDate && getJustDate(state.onTimeRunOnceDate) != todaysDate){
			//The low tempurature is going to be cold enough we want to turn on switch.
			log.info("Forecast Low is going to be below threshold")
			checkCreateScheduler()
			
			//Save last scheduled date for later comparisons.
			state.lastActiveScheduleDate = getJustDate(new Date())
		} else {
			log.info("Already Scheduled, no need to re-schedule.")
		}
    } else {
		//I scheduled something for today, but I don't need to any more, the low changed
		if(state.lastActiveScheduleDate == new Date().toLocalDate()){
			clearTodyasSchedules()
		}
			
        log.info("Nice and warm, no worries.")
    }
    
    
    log.trace("End lowForecastedTemperatureChanges")
}

private def createDailyScheduler(){
    log.trace("Executing createDailyScheduler")
	
	def onTime = CalculateOnTime()
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
    
    log.debug("Set Notification time for ${beforeBedNotificationTime}")
    //Create Reminder to Plug in Car.
//    def tempbeforeBedNotificaitonDate = new Date()
//    tempbeforeBedNotificaitonDate.set( hourOfDay: 12, minute: 0, second: 0)

    runOnce(beforeBedNotificationTime, notifyUserToPlugIn)
	
	//create scheduler to turn on block hearter(s)
	def onTime = CalculateOnTime()
	
    log.debug("Set On time for ${onTime}")
	runOnce(onTime,checkThenTurnOnSwitch)
	
	state.onTimeRunOnceDate = onTime;
    log.trace("End checkCreateScheduler")
}

//Has to be public because the scheduler is calling it
def notifyUserToPlugIn(){
    log.trace("Executing notifyUserToPlugIn")
    if(sendPushMessage != null && sendPushMessage){
        //sendPush("Plug in your block heater.")
		log.debug("Push Notification: 'Plug in your block heater.'")
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

private def CalculateOnTime(){
    log.trace("Executing CalculateOnTime")
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
	if(currentTimeCal.get(Calendar.HOUR_OF_DAY) < beforeBedNOtificationCal.get(Calendar.HOUR_OF_DAY) && currentTimeCal.get(Calendar.HOUR_OF_DAY) > carStartTimeCal.get(Calendar.HOUR_OF_DAY))
	{
		isCarStartTomorrow = true
	}
	
	log.debug("CalculateOnTime - Car Start Time: ${convertISODateStringToDate(carStartTime)}")
	//Make sure date month and year are correct
	carOnTimeCal.set(Calendar.DATE, currentTimeCal.get(Calendar.DATE))
	carOnTimeCal.set(Calendar.YEAR, currentTimeCal.get(Calendar.YEAR))
	carOnTimeCal.set(Calendar.MONTH, currentTimeCal.get(Calendar.MONTH))
	
	//Set hour and minute
	carOnTimeCal.set(Calendar.HOUR_OF_DAY, carStartTimeCal.get(Calendar.HOUR_OF_DAY))	
	carOnTimeCal.set(Calendar.MINUTE, carStartTimeCal.get(Calendar.MINUTE)-minutes)
	
	//Correct any date offset needed
	if(isCarStartTomorrow && carOnTimeCal.get(Calendar.DATE) <= currentTimeCal.get(Calendar.DATE)){
		log.debug("Move Date to tomorrow")
		carOnTimeCal.set(Calendar.DATE, currentTimeCal.get(Calendar.DATE)+1)
	}
		
	if(carOnTimeCal.get(Calendar.DATE) <	currentTimeCal.get(Calendar.DATE)){
		//if(carStarTimeCal
		log.error("UH HO! We are in the past!")
		
		//TODO Fix this edge case
	}
	
		
	
	//Turn back to a date
    def rtvDate = carOnTimeCal.getTime()
	log.debug("CalculateOnTime - Blockheater On Time: ${rtvDate}")
    
	log.info("Start Time: ${rtvDate}")
	
    log.trace("End CalculateOnTime")
    return rtvDate
	
}


private def convertISODateStringToDate(String date){
	try{
		return Date.parse( "yyyy-MM-dd'T'HH:mm:ss.SSS", date )
	}catch(Exception e){
		log.error(e)
		return null
	}
}

//Convert from string to date.
private def convertDateToCalendar(String date){
	Calendar rtv = null
	
	convertISODateStringToDate(date)
	
	if(d){
		rtv = convertDateToCalendar(d) 
	}
	
	return rtv
}

private def convertDateToCalendar(Date date){
	def cal = Calendar.getInstance()
	
	//Not Set time from date, have to do it manually
    cal.set(Calendar.DATE, date.getDate())
    cal.set(Calendar.MONTH, date.getMonth())
    cal.set(Calendar.YEAR, date.getYear())
    cal.set(Calendar.HOUR_OF_DAY, date.getHours())
    cal.set(Calendar.MINUTE, date.getMinutes())
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    
    return cal
}

private def getJustDate(date){
	
	//Quick null check
	if(date == null){
		return null
	}
	
    def cal = convertDateToCalendar(date)
	
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
    log.trace("End clearTodyasSchedules")
}