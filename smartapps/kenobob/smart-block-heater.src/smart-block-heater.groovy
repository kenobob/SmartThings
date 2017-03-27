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
        input (name: "onTemperature", type: "number", title: "Minutes?", required: true)
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
    //checkCreateScheduler()
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
		if(!state.lastActiveScheduleDate == getJustDate(new Date())){
			//The low tempurature is going to be cold enough we want to turn on switch.
			log.info("Forecast Low is going to be below threshold")
			checkCreateScheduler()
			
			//Save last scheduled date for later comparisons.
			state.lastActiveScheduleDate = getJustDate(new Date())
		} else {
			log.info("Already Scheduled, no need to re-schedule.")
		}
    } else {
		
		if(state.lastActiveScheduleDate == new Date().toLocalDate()){
			clearTodyasSchedules()
		}
			
        log.info("Nice and warm, no worries.")
    }
    
    
    log.trace("End lowForecastedTemperatureChanges")
}

private def getJustDate(date){
    def cal = Calendar.getInstance(date)
	
	//Not Set time from date, have to do it manually
    cal.set(Calendar.DATE, date.getDate())
    cal.set(Calendar.MONTH, date.getMonth())
    cal.set(Calendar.YEAR, date.getYear())
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
	
	//Turn back to a date
    def dateWithoutTime = cal.getTime()
    
    return dateWithoutTime
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
    log.trace("End checkCreateScheduler")
}

def notifyUserToPlugIn(){
    log.trace("Executing notifyUserToPlugIn")
    if(sendPushMessage != null && sendPushMessage){
        sendPush("Plug in your block heater.")
		log.debug("Push Notification: 'Plug in your block heater.'")
    }
    log.trace("End notifyUserToPlugIn")
}



private def clearAllSchedules(){
	// remove all scheduled executions for this SmartApp install
	unschedule()
}

private def clearTodyasSchedules(){
	// unschedule the notification
	unschedule(notifyUserToPlugIn)
}