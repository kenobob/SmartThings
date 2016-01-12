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
//Predifind temperature ranges.
def tempRanges = [
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
            
metadata {
    definition (name: "Better Weather Station", namespace: "kenobobbws", author: "kenobob") {
        capability "Configuration"
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
            backgroundColors: tempRanges
        }
        
        //Define Humidty settings
        valueTile("humidity", "device.humidity", decoration:"flat") {
            state "default", label: '${currentValue}% humidity'
        }
        
        //Define Water settings
        valueTile("water", "device.water", decoration:"flat") {
            state "default", label: "updating..."
            state "dry", icon:"st.alarm.water.dry", backgroundColor:"#ffffff"
            state "wet", icon:"st.alarm.water.wet", backgroundColor:"#53a7c0"
        }
        
        //Define High Temp
        valueTile("hightemperature", "device.hightemperature") {
            state "default", label:'High ${currentValue}°',
            backgroundColors: tempRanges
        }  
        
        //Define High Temp
        valueTile("lowtemperature", "device.lowtemperature") {
            state "default", label:'Low ${currentValue}°',
            backgroundColors: tempRanges
        }
        
        //Define refresh button
        standardTile("refresh", "device.refresh", decoration: "flat") {
            state "default", label: "", action: "refresh", icon:"st.secondary.refresh"
        }
        
        main "temperature"
        details(["temperature", "humidity", "water", "hightemperature", "lowtemperature", "refresh"])
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
    runPeriodically(1800, poll)
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
private def getWeatherInfo(){
    def weather = [
        Conditions: null,
        Forecast: null
    ]
    
    //Grab the appropriate weather based on user's imput
    if(settings.zipCode){
    	log.debug("Using user Provided Zip for WU Service ${settings.zipCode}.")
    	//Send Zip to WU Web Service
    	weather.Conditions = getWeatherFeature("conditions", settings.zipCode)
        weather.Forecast = getWeatherFeature("forecast", settings.zipCode)
    } else {
    	log.debug("Using system Provided Zip for WU Service.")
    	//Let the hub send it's assumed location.
    	weather.Conditions = getWeatherFeature("conditions")
        weather.Forecast = getWeatherFeature("forecast")
    }
    
    return weather
}

private def setWeatherConditions(weatherConditions){
    if(weatherConditions && weatherConditions.current_observation){
    	def obs = weatherConditions.current_observation
    	//WU sent information.
        //Temp
        if(getTemperatureScale() == "C") {
            log.debug("Temp ${obs.temp_c}")
            sendEvent(name: "temperature", value: obs.temp_c, unit: "C")
        } else {
            log.debug("Temp ${obs.temp_f}")
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
        log.debug("Humidty ${obs.relative_humidity.tokenize('%')[0].toInteger()}%")
        sendEvent(name: "humidity", value: obs.relative_humidity.tokenize('%')[0].toInteger(), unit: "%")
    } else {
    	//Weather Underground did not return any weather information.
    	log.warn("Unable to get current weather conditions from Weather Underground API.")
    }
}

private def setWeatherForecast(weatherForecast){

    if(weatherForecast && weatherForecast.forecast && weatherForecast.forecast.simpleforecast && weatherForecast.forecast.simpleforecast.forecastday){
        //Grab the first day of the forecast
        def forecastDay = weatherForecast.forecast.simpleforecast.forecastday[0]
        //WU sent information.
        //Temp High
        if(forecastDay.high){
            if(getTemperatureScale() == "C") {
                log.debug("High Temp ${forecastDay.high.celsius}")
                sendEvent(name: "hightemperature", value: forecastDay.high.celsius, unit: "C")
            } else {
                log.debug("High Temp ${forecastDay.high.fahrenheit}")
                sendEvent(name: "hightemperature", value: forecastDay.high.fahrenheit, unit: "F")
            }
        }
        //Temp Low
        if(forecastDay.low){
            if(getTemperatureScale() == "C") {
                log.debug("Low Temp ${forecastDay.low.celsius}")
                sendEvent(name: "lowtemperature", value: forecastDay.low.celsius, unit: "C")
            } else {
                log.debug("Low Temp ${forecastDay.low.fahrenheit}")
                sendEvent(name: "lowtemperature", value: forecastDay.low.fahrenheit, unit: "F")
            }
        } 
    } else {
    	//Weather Underground did not return any weather information.
    	log.warn("Unable to get current weather conditions from Weather Underground API.")
    }
}

def poll() {
    log.trace("Executing 'poll'")
    
    def weather = getWeatherInfo()
    
    setWeatherConditions(weather.Conditions)
    setWeatherForecast(weather.Forecast)
    
    log.trace("End Executing 'poll'")
}

def refresh() {
    log.trace("Executing 'refresh'")
    //Manually poll.
    poll()
    log.trace("End Executing 'refresh'")
}