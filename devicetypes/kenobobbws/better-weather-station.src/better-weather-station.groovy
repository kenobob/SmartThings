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
//Beaufort Scale
def windRanges = [
    //0 - Calm
    [value: 0, color: "#FFFFFF"],
    //1 - Light air
    //[value: 1, color: "#CCFFFF"],
    //2 - Light breeze
    [value: 4, color: "#1E9CBB"],
    //3 - Gentle breeze
    //[value: 8, color: "#99FF99"],
    //4 - Moderate breeze
    [value: 13, color: "#44B621"],
    //5 - Fresh breeze
    //[value: 19, color: "#99FF00"],
    //6 - Strong breeze
    //[value: 25, color: "#CCFF00"],
    //7 - High wind, moderate gale, near gale
    [value: 32, color: "#F1D801"],
    //8 - Gale, fresh gale
    //[value: 39, color: "#FFCC00"],
    //9 - Strong/severe gale
    [value: 47, color: "#FF9900"],
    //10 - Storm, Whole Gale
    [value: 55, color: "#FF6600"],
    //11 - Violent storm
    [value: 64, color: "#FF3300"],
    //12 -Hurricane Force
    [value: 73, color: "#BC2323"]
]

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

def uvRangs = [
    [value: 0, color: "#FFFFFF"],
    [value: 1, color:  "#289500"],
    [value: 3, color:  "#f7e400"],
    [value: 6, color:  "#f85900"],
    [value: 8, color:  "#d8001d"],
    [value: 11, color:  "#6b49c8"],    
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
        
        //Expose custom attributes to Smart Apps
        attribute "hightemperature","number"
        attribute "lowtemperature","number"
        attribute "uv","number"
        attribute "wind","number"
        attribute "windgust","number"
    }

    simulator {
        // TODO: define status and reply messages here
    }
    

    preferences {
        input "refreshMinutes", "number", title: "Auto Refresh Minutes", required: false, defaultValue: 30
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
        
        //Define Feels Like Temp
        valueTile("feelsliketemperature", "device.feelsliketemperature") {
            state "default", label:'Feels Like ${currentValue}°',
            backgroundColors: tempRanges
        }
        
        
        //Define UV Index
        valueTile("uv", "device.uv") {
            state "default", label:'UV Index ${currentValue}',
            backgroundColors: uvRangs
        }
        
        //Define Location
        valueTile("location", "device.location", decoration: "flat", width: 3, height: 1) {
            state "default", label:'${currentValue}'
        }
        
        //Define Observed Time
        valueTile("observedtime", "device.observedtime", decoration: "flat", width: 3, height: 1) {
            state "default", label:'${currentValue}'
        }
        
        //Define refresh button
        standardTile("refresh", "device.refresh", decoration: "flat") {
            state "default", label: "", action: "refresh", icon:"st.secondary.refresh"
        }
        
        standardTile("weatherIcon", "device.weatherIcon", decoration: "flat") {
            state "00", icon:"https://smartthings-twc-icons.s3.amazonaws.com/00.png", label: ""
            state "01", icon:"https://smartthings-twc-icons.s3.amazonaws.com/01.png", label: ""
            state "02", icon:"https://smartthings-twc-icons.s3.amazonaws.com/02.png", label: ""
            state "03", icon:"https://smartthings-twc-icons.s3.amazonaws.com/03.png", label: ""
            state "04", icon:"https://smartthings-twc-icons.s3.amazonaws.com/04.png", label: ""
            state "05", icon:"https://smartthings-twc-icons.s3.amazonaws.com/05.png", label: ""
            state "06", icon:"https://smartthings-twc-icons.s3.amazonaws.com/06.png", label: ""
            state "07", icon:"https://smartthings-twc-icons.s3.amazonaws.com/07.png", label: ""
            state "08", icon:"https://smartthings-twc-icons.s3.amazonaws.com/08.png", label: ""
            state "09", icon:"https://smartthings-twc-icons.s3.amazonaws.com/09.png", label: ""
            state "10", icon:"https://smartthings-twc-icons.s3.amazonaws.com/10.png", label: ""
            state "11", icon:"https://smartthings-twc-icons.s3.amazonaws.com/11.png", label: ""
            state "12", icon:"https://smartthings-twc-icons.s3.amazonaws.com/12.png", label: ""
            state "13", icon:"https://smartthings-twc-icons.s3.amazonaws.com/13.png", label: ""
            state "14", icon:"https://smartthings-twc-icons.s3.amazonaws.com/14.png", label: ""
            state "15", icon:"https://smartthings-twc-icons.s3.amazonaws.com/15.png", label: ""
            state "16", icon:"https://smartthings-twc-icons.s3.amazonaws.com/16.png", label: ""
            state "17", icon:"https://smartthings-twc-icons.s3.amazonaws.com/17.png", label: ""
            state "18", icon:"https://smartthings-twc-icons.s3.amazonaws.com/18.png", label: ""
            state "19", icon:"https://smartthings-twc-icons.s3.amazonaws.com/19.png", label: ""
            state "20", icon:"https://smartthings-twc-icons.s3.amazonaws.com/20.png", label: ""
            state "21", icon:"https://smartthings-twc-icons.s3.amazonaws.com/21.png", label: ""
            state "22", icon:"https://smartthings-twc-icons.s3.amazonaws.com/22.png", label: ""
            state "23", icon:"https://smartthings-twc-icons.s3.amazonaws.com/23.png", label: ""
            state "24", icon:"https://smartthings-twc-icons.s3.amazonaws.com/24.png", label: ""
            state "25", icon:"https://smartthings-twc-icons.s3.amazonaws.com/25.png", label: ""
            state "26", icon:"https://smartthings-twc-icons.s3.amazonaws.com/26.png", label: ""
            state "27", icon:"https://smartthings-twc-icons.s3.amazonaws.com/27.png", label: ""
            state "28", icon:"https://smartthings-twc-icons.s3.amazonaws.com/28.png", label: ""
            state "29", icon:"https://smartthings-twc-icons.s3.amazonaws.com/29.png", label: ""
            state "30", icon:"https://smartthings-twc-icons.s3.amazonaws.com/30.png", label: ""
            state "31", icon:"https://smartthings-twc-icons.s3.amazonaws.com/31.png", label: ""
            state "32", icon:"https://smartthings-twc-icons.s3.amazonaws.com/32.png", label: ""
            state "33", icon:"https://smartthings-twc-icons.s3.amazonaws.com/33.png", label: ""
            state "34", icon:"https://smartthings-twc-icons.s3.amazonaws.com/34.png", label: ""
            state "35", icon:"https://smartthings-twc-icons.s3.amazonaws.com/35.png", label: ""
            state "36", icon:"https://smartthings-twc-icons.s3.amazonaws.com/36.png", label: ""
            state "37", icon:"https://smartthings-twc-icons.s3.amazonaws.com/37.png", label: ""
            state "38", icon:"https://smartthings-twc-icons.s3.amazonaws.com/38.png", label: ""
            state "39", icon:"https://smartthings-twc-icons.s3.amazonaws.com/39.png", label: ""
            state "40", icon:"https://smartthings-twc-icons.s3.amazonaws.com/40.png", label: ""
            state "41", icon:"https://smartthings-twc-icons.s3.amazonaws.com/41.png", label: ""
            state "42", icon:"https://smartthings-twc-icons.s3.amazonaws.com/42.png", label: ""
            state "43", icon:"https://smartthings-twc-icons.s3.amazonaws.com/43.png", label: ""
            state "44", icon:"https://smartthings-twc-icons.s3.amazonaws.com/44.png", label: ""
            state "45", icon:"https://smartthings-twc-icons.s3.amazonaws.com/45.png", label: ""
            state "46", icon:"https://smartthings-twc-icons.s3.amazonaws.com/46.png", label: ""
            state "47", icon:"https://smartthings-twc-icons.s3.amazonaws.com/47.png", label: ""
state "na", icon:"https://smartthings-twc-icons.s3.amazonaws.com/na.png", label: ""
        }
        
        valueTile("windgust", "device.windgust") {
            state "default", label:'Gusts ${currentValue}',
            backgroundColors: windRanges
        }
        valueTile("wind", "device.wind") {
            state "default", label:'Wind ${currentValue}',
            backgroundColors: windRanges
        }
        
        main "temperature"
        details(
            ["temperature", "feelsliketemperature", "weatherIcon", "hightemperature", "humidity", "wind", "lowtemperature", "uv", "windgust", "water", "refresh", "observedtime" , "location"])
    }
}

// parse events into attributes
def parse(String description) {
    log.trace("Executing 'parse'")
    try{
        log.debug("Parsing '${description}'")
        // TODO: handle 'humidity' attribute
        // TODO: handle 'temperature' attribute
        // TODO: handle 'water' attribute
    }catch(all){
        log.error(all)
    }
    log.trace("End Executing 'parse'")

}

//When the Device is installed, fire off this function. Note, does not fire if upgraded.
def installed() {
    log.trace("Executing 'installed'")
    try{
        schedulePolling()
    }catch(all){
        log.error(all)
    }
    //Run Poll on installation to update the screen right away
    poll()
    log.trace("End Executing 'installed'")
}

//When the Device is uninstalled, fire off this function
def uninstalled() {
    log.trace("Executing 'uninstalled'")
    try{
        unschedule()
    }catch(all){
        log.error(all)
    }
    log.trace("End Executing 'uninstalled'")
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
    	weather.Conditions = getTwcConditions(settings.zipCode)
        weather.Forecast = getTwcForecast(settings.zipCode)
		weather.Location = getTwcLocation(settings.zipCode)
    } else {
    	log.debug("Using system Provided Zip for WU Service.")
    	//Let the hub send it's assumed location.
    	weather.Conditions = getTwcConditions()
        weather.Forecast = getTwcForecast()
		weather.Location = getTwcLocation()
    }
    
    return weather
}

private def setWeatherLocaiton(weatherLocationInfo){
	
	if(weatherLocationInfo){
        //location
        log.debug("Location ${weatherLocationInfo.displayName}, ${weatherLocationInfo.adminDistrictCode} ${weatherLocationInfo.postalCode}")
        sendEvent(name: "location", value: "${weatherLocationInfo.displayName}, ${weatherLocationInfo.adminDistrictCode} ${weatherLocationInfo.postalCode}")
	}
}

private def setWeatherConditions(weatherConditions){
    if(weatherConditions){
    	def obs = weatherConditions
    	//TWC sent information.
        if(location.temperatureScale == "C") {
            //Temp
            log.debug("Temp ${obs.temperature}C")
            sendEvent(name: "temperature", value: obs.temperature, unit: "C")
            //Temp - Feels like
            log.debug("Feels Like ${obs.temperatureFeelsLike}C")
            sendEvent(name: "feelsliketemperature", value: obs.temperatureFeelsLike, unit: "C")
            //Wind
            log.debug("Wind speeds ${obs.windSpeed} KPH")
            sendEvent(name: "wind", value: obs.windSpeed, unit: "kph")
            log.debug("Wind Gusts ${obs.windGust} KPH")
            sendEvent(name: "windgust", value: obs.windGust, unit: "kph")
        } else {
            //Temp
            log.debug("Temp ${obs.temperature}F")
            sendEvent(name: "temperature", value: obs.temperature, unit: "F")
            //Temp - Feels like
            log.debug("Feels Like ${obs.temperatureFeelsLike}F")
            sendEvent(name: "feelsliketemperature", value: obs.temperatureFeelsLike, unit: "F")
            //Wind            
            log.debug("Wind speeds ${obs.windSpeed} MPH")
            sendEvent(name: "wind", value: obs.windSpeed, unit: "mph")
            log.debug("Wind Gusts ${obs.windGust} MPH")   
            sendEvent(name: "windgust", value: obs.windGust, unit: "mph")
        }
        
        //Precip Notification - TWC Sends Huge Decimal places, can't parse to int.
        if(obs.precip1Hour.toDouble() > 0){
            log.debug("Currently Wet");
            sendEvent(name: "water", value: "wet")
        } else {
            log.debug("Currently Dry");
            sendEvent(name: "water", value: "dry")
        }
        
        //Humidity
        log.debug("Humidty ${obs.relativeHumidity.tokenize('%')[0].toInteger()}%")
        sendEvent(name: "humidity", value: obs.relativeHumidity.tokenize('%')[0].toInteger(), unit: "%")
        
        
        //Weather Icon
        log.debug("Weather Icon ${obs.iconCode}")
        sendEvent(name: "weatherIcon", value: obs.iconCode as String, displayed: false)
        
        //Observation Time
        log.debug(obs.validTimeLocal)
        sendEvent(name: "observedtime", value: obs.validTimeLocal)
        
        //UV Index
        log.debug("UV Index ${obs.uvIndex}")
        sendEvent(name: "uv", value: obs.uvIndex)
        
        
    } else {
    	//Weather Channel did not return any weather information.
    	log.warn("Unable to get current weather conditions from Weather Channel API.")
    }
}

private def setWeatherForecast(weatherForecast){

    if(weatherForecast){
        //Grab the first day of the forecast
        def todayHigh = null
		if(weatherForecast.temperatureMax){
			todayHigh = weatherForecast.temperatureMax[0]
		}
		def todayLow = null
		if(weatherForecast.temperatureMin){
			todayLow = weatherForecast.temperatureMin[0]
		}
        //WU sent information.
        //Temp High
        if(todayHigh){
            if(location.temperatureScale == "C") {
                log.debug("High Temp ${todayHigh}")
                sendEvent(name: "hightemperature", value: todayHigh, unit: "C")
            } else {
                log.debug("High Temp ${todayHigh}")
                sendEvent(name: "hightemperature", value: todayHigh, unit: "F")
            }
        }
        //Temp Low
        if(todayLow){
            if(location.temperatureScale == "C") {
                log.debug("Low Temp ${todayLow}")
                sendEvent(name: "lowtemperature", value: todayLow, unit: "C")
            } else {
                log.debug("Low Temp ${todayLow}")
                sendEvent(name: "lowtemperature", value: todayLow, unit: "F")
            }
        } 
    } else {
    	//Weather Underground did not return any weather information.
    	log.warn("Unable to get current weather conditions from Weather Channel API.")
    }
}

def poll() {
    log.trace("Executing 'poll'")
    try{
        def weather = getWeatherInfo()
    
        setWeatherConditions(weather.Conditions)
		setWeatherLocaiton(weather.Location)
        setWeatherForecast(weather.Forecast)
    }catch(all){
        log.error(all)
    }
    log.trace("End Executing 'poll'")
}

def refresh() {
    log.trace("Executing 'refresh'")
    //Manually poll.
    try{
        reschedulePolling()
    }catch(all){
        log.error(all)
    }
    poll()
    log.trace("End Executing 'refresh'")
}

//TODO, clean this up
private def reschedulePolling(){
    log.trace("Executing 'reschedulePolling'")
    //This is a long running function
    unschedule()
    schedulePolling()
    log.trace("End Executing 'reschedulePolling'")
}

private def schedulePolling(){
    log.trace("Executing 'schedulePolling'")
    def scheduledMinues = 30
    if(settings.refreshMinutes){
        scheduledMinues = settings.refreshMinutes
    }
    
    log.debug("Scheduled seconds : ${scheduledMinues}")
    //Set Polling
    schedule("0 0/${scheduledMinues} * 1/1 * ?", poll)
    log.trace("End Executing 'schedulePolling'")
}