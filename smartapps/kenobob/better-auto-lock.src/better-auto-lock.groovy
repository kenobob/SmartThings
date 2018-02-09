/**
 *  Better Auto Lock
 *
 *  Copyright 2018 kenobob
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
    name: "Better Auto Lock",
    namespace: "kenobob",
    author: "kenobob",
    description: "Description to fill in later. ",
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section() {
        input (name: "minutesUnlocked", type: "number", title: "Number of Minutes the Lock must be unlocked before auto Locking", required: true)
        input (name: "minutesDoorClosed", type: "number", title: "Number of Minutes the Door must be closed Before auto Locking", required: true)
    }
    //Time or Mode, not sure yet.
    section() {
        input "contactSensor", "capability.contactSensor", title: "The Door Sensor on the Door with the Lock", multiple: false, required: true
        input "doorLock", "capability.lock", title: "The Lock to Control", multiple: false, required: true
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

def initialize() {
    logtrace("Executing 'initialize'")
    
    // remove all scheduled executions for this SmartApp install
    unschedule()
    // unsubscribe all listeners.
    unsubscribe()
    
    //Reset variables
    //state.doorIsUnlocked = null
    state.schedulerActive = null
    state.contectOpenRawDate = now()
    state.doorUnlockedRawDate = now()
    
    //Subscribe to Sensor Changes
    log.debug("Subscribing Contact Sensor")
    subscribe(contactSensor, "contact", contactChangeEventHandler)
    
    //log.debug("Subscribing Door Lock")
    subscribe(doorLock, "lock", lockChangeEventHandler)
	
	
    logtrace("End Executing 'initialize'")
}

def lockChangeEventHandler(evt)
{
    logtrace("Executing 'lockChangeEventHandler'")
    
    // did the value of this event change from its previous state?
    if(evt.isStateChange()){
        
        //If the Door is Unlocked, lets do something
        if(!isDoorLocked()){
            
            state.doorUnlockedRawDate = now()
            
            if(state.schedulerActive != true){
                //Kick Off Door Lock Scheduler
                //Reminder to close
                def doorLockCheckData =  [
                    minutesUnlocked: null,
                    minutesDoorClosed: null
                ]
            
                reminderEventData.minutesUnlocked = minutesUnlocked
                reminderEventData.minutesDoorClosed = minutesDoorClosed
            
                //Set check for every 5 minutes
                runEvery5Minutes(checkToLockTheDoor, [data: doorLockCheckData])
                
                state.schedulerActive = true
            }
            
        } else {
            //The Door is locked, lets kill schedulers
            state.schedulerActive = false
            //Kill scheduler
            unscheduleCheckToLockTheDoor();
        }
        
    }
    
    
    logtrace("End Executing 'lockChangeEventHandler'")
}

def checkToLockTheDoor(data){
    
    def elapsedContactOpenTime = now() - state.contectOpenRawDate.time
    def elapsedUnlockedTime = now() - state.doorUnlockedRawDate.time    
    
    log.debug("Elapsed Contact Sensor Open Time: ${elapsedContactOpenTime}")
    log.debug("Elapsed time since door unlcoked: ${elapsedUnlockedTime}")
    
    if(!isContactSensorOpen() 
        && !isDoorLocked()
        && data.minutesUnlocked <= elapsedUnlockedTime
        && data.minutesDoorClosed <= elapsedContactOpenTime){
        //The Contact Sensor is Closed, the door is unlocked, and the 2 time requirements are met
        //Lets Lock the Door!!
        doorLock.lock()
        
        //Lets let the lock event fire to kill this job, to make sure the door actually locks.
    }
    
}

private def unscheduleCheckToLockTheDoor(){
    logtrace("Executing 'unscheduleCheck'")
    //Cancel Scheduler
    unschedule(checkToLockTheDoor)
    
    //Check to see if still can Schedule more events
    if(!canSchedule()){
        // remove all scheduled executions for this SmartApp
        unschedule()
    }
    
    //Reset Variables
    state.schedulerActive = null
    
    logtrace("End Executing 'unscheduleCheck'")
}

def contactChangeEventHandler(evt)
{
    logtrace("Executing 'contactChangeEventHandler'")
    state.contectOpenRawDate = now()

    logtrace("End Executing 'contactChangeEventHandler'")
}


private def isContactSensorOpen(){
    log.info("Contact Sensor is Currently ${contactSensor.latestValue("contact")}")
    if(contactSensor.latestValue("contact")=="open"){
        return true
    } else {
        return false
    }
}

private def isDoorLocked(){
    log.info("Door Lock is Currently ${doorLock.latestValue("lock")}")
    if(doorLock.latestValue("lock")=="locked"){
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