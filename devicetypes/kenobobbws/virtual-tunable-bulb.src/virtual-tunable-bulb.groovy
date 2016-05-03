/**
 *  Virtual Tunable Bulb
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


def lightColorRange = [

    [value: 2699, color: "#ff9329"],
	[value: 4600, color: "#92d6f3"],
	[value:	6500, color: "#0000ff"]	

]
		 
metadata {
    definition (name: "Virtual Tunable Bulb", namespace: "kenobobbws", author: "kenobob") {
        capability "Switch"
        capability "Refresh"
        capability "Switch Level"
		capability "Actuator"
        capability "Color Temperature"
        attribute "colorName","string"
    }

	// simulator metadata
	simulator {
	}

	// UI tile definitions
	tiles(scale:2) {
		multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: true){
            tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
                attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
                attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.light.on", backgroundColor:"#79b821", nextState:"turningOff"
                attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.light.off", backgroundColor:"#ffffff", nextState:"turningOn"
            }
			tileAttribute ("device.level", key: "SLIDER_CONTROL") {
                attributeState "level", action:"switch level.setLevel"
            }
		}
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("lValue", "device.level", inactiveLabel: true, width: 2, height: 2, decoration: "flat") {
            state "levelValue", label:'${currentValue}%', unit:"", backgroundColor: "#53a7c0"
        }
		controlTile("colorTempSliderControl", "device.colorTemperature", "slider", width: 4, height: 2, inactiveLabel: false, range:"(2700..6500)") {
            state "colorTemperature", action:"color temperature.setColorTemperature"
        }
        valueTile("colorTemp", "device.colorTemperature", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "colorTemperature", label: '${currentValue} K',
            backgroundColors: lightColorRange
        }
		valueTile("colorName", "device.colorName", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
            state "default", label:'${currentValue}',
            backgroundColors: lightColorRange
        }

		main(["switch"])
		details(["switch", "colorTempSliderControl", "colorTemp", "colorName", "refresh","lValue"])
	}
}

def parse(String description) {
}

def on() {
	sendEvent(name: "switch", value: "on")
 	sendEvent(name: "level", value: 100)
    log.info "Dimmer On"
}

def off() {
	sendEvent(name: "switch", value: "off")
 	sendEvent(name: "level", value: 0)
    log.info "Dimmer Off"
}

def setLevel(val){
    log.info "setLevel $val"
    
    if (val < 0) val = 0
    else if( val > 100) val = 100
    
    if(val == 0) off() else {
 	on()
 	sendEvent(name: "level", value: val)
    }
}

def refresh() {
    log.info "refresh"
}

//range 2700k - 6500k
def setColorTemperature(value) {
    log.debug ("Color Temp: ${value}")
	
	//Fix bug in SM
	if( 0 <= value && value <= 100)
	{
		log.trace("Adjust values to smartthings bug")
		//Let's do some basic math.
		def difference = 3800;
		def percentatage = value/100;
		value = (difference * percentatage) + 2700;
        //convert back to int
        value = value.toInteger()
        log.trace("Bug Adjustment: ${value}")
	}
    
	//Ensure not out of range occurs
	if (value < 2700){ 
    	log.trace("Value out of range, to low. ${value}");
    	value = 2700
    }
    else if( value > 6500) {
    	log.trace("Value out of range, to high. ${value}");
    	value = 6500
    }
	
    log.debug ("Adjusted Color Temp: ${value}")
    
    setGenericName(value)
	sendEvent(name: "colorTemperature", value: value)
}

//Naming based on the wiki article here: http://en.wikipedia.org/wiki/Color_temperature
def setGenericName(value){
    if (value != null) {
        def genericName = "White"
        if (value < 3300) {
            genericName = "Soft White"
        } else if (value < 4150) {
            genericName = "Moonlight"
        } else if (value <= 5000) {
            genericName = "Cool White"
        } else if (value >= 5000) {
            genericName = "Daylight"
        }
        sendEvent(name: "colorName", value: genericName)
    }
}