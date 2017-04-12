
/**
 *  Humidity Contact Sensor Notification
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
    name: "Humidity Contact Sensor Notification",
    namespace: "kenobob",
    author: "kenobob",
    description: "An app to monitor humidity sensors, and send notifications (on delay) for contact sensors",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
    section() {
        input (name: "minutes", type: "number", title: "Minutes of Delay between opening contact and notification", required: true)
    }
    section("") {
        input (name: "notificationHumidity", type: "number", title: "This Humidity or Higher to be notified", required: true)
    }
    //Time or Mode, not sure yet.
    section() {
        input "contactSensor", "capability.contactSensor", title: "Contact Sensor to get Notified About", multiple: false, required: true
        input "humiditySensor", "capability.relativeHumidityMeasurement", title: "Get humidity readings from this sensor", multiple: false, required: true
    }
    section( "Notifications" ) {
        input("recipients", "contact", title: "Send notifications to") {
            input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], required: false
            input "phoneNumber", "phone", title: "Send a Text Message?", required: false
        }
        input("notificationText", type: "text", title: "What woul dyou like your notification to say?", required: true)
    }
}

def installed()
{
    logtrace("Executing 'installed'")
    log.debug("Installed with settings: ${settings}")
    initialize()
    logtrace("End Executing 'installed'")
}

def updated()
{
    logtrace("Executing 'updated'")
    log.debug("Updated with settings: ${settings}")
    initialize()
    logtrace("End Executing 'updated'")
}

def initialize()
{
    logtrace("Executing 'initialize'")
    
    // remove all scheduled executions for this SmartApp install
    unschedule()
    // unsubscribe all listeners.
    unsubscribe()
    
    //Subscribe to Sensor Changes
    log.debug("Subscribing Contact Sensor")
    subscribe(contactSensor, "contact", contactChangeEventHandler)
    
    //TODO Subscribe to Humidity for when contact is left open
	
	
    logtrace("End Executing 'initialize'")
}
def contactChangeEventHandler(evt)
{
    logtrace("Executing 'contactChangeEventHandler'")
    // did the value of this event change from its previous state?
    log.debug "The value of this event is different from its previous value: ${evt.isStateChange()}"
    
    if(evt.isStateChange() && notificationHumidity <= getHumidity()){
        if(isContactSensorOpen()){
            //Set Scheduler
            //evt.getDisplayName() the user-friendly name of the source of this event.
            def eventData =  [
                sendPushMessage: null,
                phoneNumber: null,
                notificationText: null
            ]
            
            eventData.sendPushMessage = sendPushMessage
            eventData.notificationText = notificationText
            //eventData.phoneNumber = phoneNumber
            
            log.debug("Contact Open - High Humidity: Schedule Notification for ${minutes*60} seconds")
            
            runIn(minutes*60, notifyUser, [overwrite: true, data: eventData])
            
            //TODO: Create Reminder to close
            def reminderEventData =  [
                sendPushMessage: null,
                phoneNumber: null,
                notificationText: null
            ]
            
            reminderEventData.phoneNumber = phoneNumber
            reminderEventData.sendPushMessage = sendPushMessage
            reminderEventData.notificationText = notificationText
        } else {
            log.debug("Contact Closed: Un-schedule any active notifications")
            //Cancel Scheduler
            unschedule(notifyUser)
        }
    } else {
        log.debug("State: ${evt.isStateChange()} Humidity: ${getHumidity()}. We can ignore")
    }
    
	
    logtrace("End Executing 'contactChangeEventHandler'")
}

def notifyUser(data){
    logtrace("Executing 'notifyUser'")
    log.debug("Notificaiton Data: ${data}")
    
    if(data.sendPushMessage == "Yes" || data.phoneNumber){
        log.debug("Notifications Turned on")
        def options = null
        
        if(data.sendPushMessage == "Yes" && data.phoneNumber) {
            options = [method: "both", phone: data.phoneNumber]
        } else if(data.sendPushMessage == "Yes"){
            options = [method: "push"]
        } else {
            options = [method: "phone", phone: data.phoneNumber]
        }
        log.debug("Options for Notification: ${options}")
        
        sendNotification(data.notificationText, options)
    } else {
        log.error("No notification settings selected")
    }
    
    logtrace("End Executing 'notifyUser'")
}

def getHumidity(){
    humiditySensor.latestValue("humidity")
}

def isContactSensorOpen(){
    log.info("Contact Sensor is Currently ${contactSensor.latestValue("contact")}")
    if(contactSensor.latestValue("contact")=="open"){
        return true
    } else {
        return false
    }
}

private def logtrace(message){
    if(true){
        log.trace(message)
    }
}
