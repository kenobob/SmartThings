/**
 *  Smart Block Heater
 *
 *  Copyright 2016 kenobob
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
    //Time or Mode, not sure yet.
    section("When does your quiet hours start?") {
        input "timeOfDay", "time", title: "Time?", required: true
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
    // TODO: subscribe to attributes, devices, locations, etc.
    createSubscriptions()
    log.trace("End Initialize")
}


private def createSubscriptions()
{
    log.trace("Executing Create Subscriptions")
    subscribe(bwsTemperatureMeasurement, "lowtemperature", temperatureChanges)
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
def temperatureChanges(evt){
    
    log.trace("Executing Create Subscriptions")
    log.debug "The Low Changed To: ${evt.numericValue}"
}