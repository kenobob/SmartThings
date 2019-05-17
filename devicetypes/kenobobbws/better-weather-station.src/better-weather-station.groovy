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
            state "default", label: "No Weather Condition"
            state "chanceflurries", icon:"st.custom.wu1.chanceflurries", label: ""
            state "chancerain", icon:"st.custom.wu1.chancerain", label: ""
            state "chancesleet", icon:"st.custom.wu1.chancesleet", label: ""
            state "chancesnow", icon:"st.custom.wu1.chancesnow", label: ""
            state "chancetstorms", icon:"st.custom.wu1.chancetstorms", label: ""
            state "clear", icon:"st.custom.wu1.clear", label: ""
            state "cloudy", icon:"st.custom.wu1.cloudy", label: ""
            state "flurries", icon:"st.custom.wu1.flurries", label: ""
            state "fog", icon:"st.custom.wu1.fog", label: ""
            state "hazy", icon:"st.custom.wu1.hazy", label: ""
            state "mostlycloudy", icon:"st.custom.wu1.mostlycloudy", label: ""
            state "mostlysunny", icon:"st.custom.wu1.mostlysunny", label: ""
            state "partlycloudy", icon:"st.custom.wu1.partlycloudy", label: ""
            state "partlysunny", icon:"st.custom.wu1.partlysunny", label: ""
            state "rain", icon:"st.custom.wu1.rain", label: ""
            state "sleet", icon:"st.custom.wu1.sleet", label: ""
            state "snow", icon:"st.custom.wu1.snow", label: ""
            state "sunny", icon:"st.custom.wu1.sunny", label: ""
            state "tstorms", icon:"st.custom.wu1.tstorms", label: ""
            state "cloudy", icon:"st.custom.wu1.cloudy", label: ""
            state "partlycloudy", icon:"st.custom.wu1.partlycloudy", label: ""
            state "nt_chanceflurries", icon:"st.custom.wu1.nt_chanceflurries", label: ""
            state "nt_chancerain", icon:"st.custom.wu1.nt_chancerain", label: ""
            state "nt_chancesleet", icon:"st.custom.wu1.nt_chancesleet", label: ""
            state "nt_chancesnow", icon:"st.custom.wu1.nt_chancesnow", label: ""
            state "nt_chancetstorms", icon:"st.custom.wu1.nt_chancetstorms", label: ""
            state "nt_clear", icon:"st.custom.wu1.nt_clear", label: ""
            state "nt_cloudy", icon:"st.custom.wu1.nt_cloudy", label: ""
            state "nt_flurries", icon:"st.custom.wu1.nt_flurries", label: ""
            state "nt_fog", icon:"st.custom.wu1.nt_fog", label: ""
            state "nt_hazy", icon:"st.custom.wu1.nt_hazy", label: ""
            state "nt_mostlycloudy", icon:"st.custom.wu1.nt_mostlycloudy", label: ""
            state "nt_mostlysunny", icon:"st.custom.wu1.nt_mostlysunny", label: ""
            state "nt_partlycloudy", icon:"st.custom.wu1.nt_partlycloudy", label: ""
            state "nt_partlysunny", icon:"st.custom.wu1.nt_partlysunny", label: ""
            state "nt_sleet", icon:"st.custom.wu1.nt_sleet", label: ""
            state "nt_rain", icon:"st.custom.wu1.nt_rain", label: ""
            state "nt_sleet", icon:"st.custom.wu1.nt_sleet", label: ""
            state "nt_snow", icon:"st.custom.wu1.nt_snow", label: ""
            state "nt_sunny", icon:"st.custom.wu1.nt_sunny", label: ""
            state "nt_tstorms", icon:"st.custom.wu1.nt_tstorms", label: ""
            state "nt_cloudy", icon:"st.custom.wu1.nt_cloudy", label: ""
            state "nt_partlycloudy", icon:"st.custom.wu1.nt_partlycloudy", label: ""
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
        weather.Forecast = getWeatherFeature("forecast", settings.zipCode)
		weather.Location = getTwcLocation(settings.zipCode)
    } else {
    	log.debug("Using system Provided Zip for WU Service.")
    	//Let the hub send it's assumed location.
    	weather.Conditions = getTwcConditions()
        weather.Forecast = getWeatherFeature("forecast")
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
        def weatherIcon = obs.icon_url.split("/")[-1].split("\\.")[0]
        log.debug("Weather Icon ${weatherIcon}")
        sendEvent(name: "weatherIcon", value: weatherIcon, displayed: false)
        
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

    if(weatherForecast && weatherForecast.forecast && weatherForecast.forecast.simpleforecast && weatherForecast.forecast.simpleforecast.forecastday){
        //Grab the first day of the forecast
        def forecastDay = weatherForecast.forecast.simpleforecast.forecastday[0]
        //WU sent information.
        //Temp High
        if(forecastDay.high){
            if(location.temperatureScale == "C") {
                log.debug("High Temp ${forecastDay.high.celsius}")
                sendEvent(name: "hightemperature", value: forecastDay.high.celsius, unit: "C")
            } else {
                log.debug("High Temp ${forecastDay.high.fahrenheit}")
                sendEvent(name: "hightemperature", value: forecastDay.high.fahrenheit, unit: "F")
            }
        }
        //Temp Low
        if(forecastDay.low){
            if(location.temperatureScale == "C") {
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