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
            
metadata {
    definition (name: "Virtual Tunable Bulb", namespace: "kenobobbws", author: "kenobob") {
        capability "Switch"
        capability "Refresh"
        capability "Switch Level"
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
		standardTile("refresh", "device.switch", inactiveLabel: false, decoration: "flat") {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}
        valueTile("lValue", "device.level", inactiveLabel: true, height:1, width:1, decoration: "flat") {
            state "levelValue", label:'${currentValue}%', unit:"", backgroundColor: "#53a7c0"
        }

		main(["switch"])
		details(["switch", "refresh","lValue"])
	}
}

def parse(String description) {
}

def on() {
	sendEvent(name: "switch", value: "on")
    log.info "Dimmer On"
}

def off() {
	sendEvent(name: "switch", value: "off")
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