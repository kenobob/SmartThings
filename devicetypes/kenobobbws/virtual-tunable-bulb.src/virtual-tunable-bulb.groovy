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

    [value: 2700, color: "#ff9329"],
    [value: 3650, color: "#fbf9cd"],    
    [value: 4600, color: "#92d6f3"],
    [value: 6500, color: "#0000ff"]	

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
    
    preferences {
        section("Remember"){
            paragraph("Remember where you left off. So when you turn it back on, it's right there. Similar to how most smart bulbs work.")
            input "doesRememberState", "bool", title: "Remeber Previous State", required: true, defaultValue: true
        }
        section("Default Values"){
            paragraph "This May not Function as expected when the Remember Feature is enabled."
            input "defaultOnDimmer", "number", title: "Default On Dimmer", required: true, defaultValue: 100        
        }
        section("On Off Dimmer"){
            input "doesSendDimmerWithSwitch", "bool", title: "Send Dimmer Level with On/Off commands?", required: true, defaultValue: true
        }
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
    log.info("Turning Switch On");
    sendEvent(name: "switch", value: "on")
    
    if(settings.doesSendDimmerWithSwitch){
        //See if we are suppose to remember state and we have a value
        if(settings.doesRememberState && state.dimmer){
            log.info("Dimmer Remembers State of ${state.dimmer}")
            sendEvent(name: "level", value: state.dimmer)
        } else {    
            log.debug("Setting to default brightness of ${settings.defaultOnDimmer}")
            sendEvent(name: "level", value: settings.defaultOnDimmer)
        }
    }
    
    state.isOn = true
    log.info "Switch On"
}

def off() {
    log.info("Turning Switch Off");
    sendEvent(name: "switch", value: "off")
    
    if(settings.doesSendDimmerWithSwitch){
        sendEvent(name: "level", value: 0)
    }
    
    state.isOn = false
    log.info "Switch Off"
}

def setLevel(val){
    log.info "setLevel $val"
    
    if (val < 0){
        log.debug("Dimmer is Less Than 0: ${val}");
        val = 0
    }
    else if( val > 100){ 
        log.debug("Dimmer is greater than 100: ${val}");
        val = 100
    }
    
    if(val == 0) { 
        off()
    } else {
        if(!state.isOn){
            log.info("Switch is off, but brightness is being changed, turn it on.")
            on()
        }
 	sendEvent(name: "level", value: val)
    }
    
    //Something changed, remember level.
    if(settings.doesRememberState && val && val > 0){
        log.debug("Saved Dimmer Level: ${val}")
        state.dimmer = val;
    }
}

def refresh() {
    log.info "refresh"
    state.dimmer=0
    state.isOn=false
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