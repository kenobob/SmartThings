/**
 *  Better Weather Station
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
metadata {
    definition (name: "Better Weather Station", namespace: "kenobobbws", author: "kenobob") {
        capability "Configuration"
        capability "Illuminance Measurement"
        capability "Polling"
        capability "Refresh"
        capability "Relative Humidity Measurement"
        capability "Sensor"
        capability "Temperature Measurement"
        capability "Water Sensor"
        
        command "refresh"
    }

    simulator {
        // TODO: define status and reply messages here
    }
    

    preferences {
        input "zipCode", "text", title: "Zip Code", required: false
    }

    tiles {
        valueTile("temperature", "device.temperature") {
            state "default", label:'${currentValue}°',
            backgroundColors:[
                //Put in Color range for Northern United States Temperature Ranges.
                [value: -50, color: "#FFFFFF"],
                [value: -15, color: "#EF8CEF"],
                [value:  0, color: "#AE1EB8"],
                [value: 15, color: "#4B12A0"],
                [value: 31, color: "#153591"],
                [value: 44, color: "#1e9cbb"],
                [value: 59, color: "#90d2a7"],
                [value: 74, color: "#44b621"],
                [value: 84, color: "#f1d801"],
                [value: 95, color: "#d04e00"],
                [value: 96, color: "#bc2323"]
            ]
        }
        
        //Define Humidty settings
        valueTile("humidity", "device.humidity", decoration:"flat") {
            state "default", label: "${currentValue} humidity"
        }
        
        //Define Water settings
        valueTile("water", "device.water", decoration:"flat") {
            state "default", label: "updating..."
            state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
            state "wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
        }
        
        main (["temperature, humidity, water"])
        details(["temperature"])
    }
}

// parse events into attributes
def parse(String description) {
    log.debug("Parsing '${description}'")
    // TODO: handle 'illuminance' attribute
    // TODO: handle 'humidity' attribute
    // TODO: handle 'temperature' attribute
    // TODO: handle 'water' attribute

}

//When the Device is installed, fire off this function. Note, does not fire if upgraded.
def installed() {
    log.trace("Executing 'installed'");
    //TODO: Handle if Scheduler Dies.
    //TODO: Create Update Command to make user configurable
    runPeriodically(90, poll)
    //Run Poll on installation to update the screen right away
    poll()
    log.trace("End Executing 'installed'");
}

//When the Device is uninstalled, fire off this function
def uninstalled() {
    log.trace("Executing 'uninstalled'");
    unschedule()
    log.trace("End Executing 'uninstalled'");
}

// handle commands
def configure() {
    log.debug("Executing 'configure'")
    // TODO: handle 'configure' command
}

def poll() {
    log.trace("Executing 'poll'")
    def weatherConditions
    
    //Grab the appropriate weather based on user's imput
    if(settings.zipCode){
    	log.debug("Using user Provided Zip for WU Service ${settings.zipCode}.")
    	//Send Zip to WU Web Service
    	weatherConditions = getWeatherFeature("conditions", settings.zipCode)
    } else {
    	log.debug("Using system Provided Zip for WU Service.")
    	//Let the hub send it's assumed location.
    	weatherConditions = getWeatherFeature("conditions")
    }
    
    if(weatherConditions && weatherConditions.current_observation){
    	def obs = weatherConditions.current_observation
    	//WU sent information.
        //Temp
        if(getTemperatureScale() == "C") {
            sendEvent(name: "temperature", value: obs.temp_c, unit: "C")
        } else {
            sendEvent(name: "temperature", value: obs.temp_f, unit: "F")
        }
        
        //Precip Notification - WU Sends Huge Decimal places, can't parse to int.
        if(obs.precip_1hr_in.toDouble() > 0){
            log.debug("Currently Wet");
            sendEvent(name: "water", value: "wet")
        } else {
            log.debug("Currently Dry");
            sendEvent(name: "water", value: "dry")
        }
        
        //Humidity
        log.debug("Humidty${obs.relative_humidity}")
        sendEvent(name: "humidity", value: obs.relative_humidity.tokenize('%')[0].toInteger(), unit: "%")
    } else {
    	//Weather Underground did not return any weather information.
    	log.warn("Unable to get current weather conditions from Weather Underground API.")
    }
    
    log.trace("End Executing 'poll'")
}

def refresh() {
    log.trace("Executing 'refresh'")
    //Manually poll.
    poll()
    log.trace("End Executing 'refresh'")
}