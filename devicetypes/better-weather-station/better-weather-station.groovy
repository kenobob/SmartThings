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
	}
}

// parse events into attributes
def parse(String description) {
	log.debug "Parsing '${description}'"
	// TODO: handle 'illuminance' attribute
	// TODO: handle 'humidity' attribute
	// TODO: handle 'temperature' attribute
	// TODO: handle 'water' attribute

}

def installed() {
	runPeriodically(3600, poll)
}

def uninstalled() {
	unschedule()
}

// handle commands
def configure() {
	log.debug "Executing 'configure'"
	// TODO: handle 'configure' command
}

def poll() {
	log.debug "Executing 'poll'"
    def weatherConditions
    
    //Grab the appropriate weather based on user's imput
    if(settings.zipCode){
    	//Send Zip to WU Web Service
    	weatherConditions = getWeatherFeature("conditions", settings.zipCode)
    } else {
    	//Let the hub send it's assumed location.
    	weatherConditions = getWeatherFeature("conditions")
    }
    
    if(weatherConditions){
    	//WU sent information.
        if(getTemperatureScale() == "C") {
			send(name: "temperature", value: Math.round(obs.temp_c), unit: "C")
		} else {
			send(name: "temperature", value: Math.round(obs.temp_f), unit: "F")
		}
    } else {
    	//Weather Underground did not return any weather inforamtion.
    	log.warn "Unable to get current weather conditions from Weather Underground API."
    }
    
	log.debug "End Executing 'poll'"
}

def refresh() {
	log.debug "Executing 'refresh'"
    //Manually re-trigger the polling event.
    poll()
	log.debug "End Executing 'refresh'"
}