/**
*   
*   File: HEmail.groovy
*   Platform: Hubitat
*   Modification History:
*       Date    Time		Who		What
*       v0.0.1	2019-04-15	13:38	Eric H	Initial creation of Telnet Email
*       v0.5	2019-04-17	15:21	Eric H	Beta release, full functionality.
*       v0.99	2019-04-19	15:21	Eric H	Created seqSend method to tighten up code a bit.
*											, set email user name to required.
*
*  Copyright 2018 Eric Huebl
*
*  This software is free software: you can redistribute it and/or modify
*  it under the terms of the GNU General Public License as published by
*  the Free Software Foundation, either version 3 of the License, or
*  (at your option) any later version.
*
*  This software is distributed in the hope that it will be useful,
*  but WITHOUT ANY WARRANTY; without even the implied warranty of
*  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*  GNU General Public License for more details.
*
*  You should have received a copy of the GNU General Public License
*  along with this program.  If not, see <https://www.gnu.org/licenses/>.
*
*
*/
def version() {"v0.99"}

preferences {
	input("EmailServer", "text", title: "Email Server:", description: "Enter location of email server", required: true)
	input("EmailDomain", "text", title: "Email Domain:", description: "Enter domain (ex. domain.com)", required: true)
	input("EmailUser", "text", title: "Email User:", description: "Enter email username", required: true)
	input("EmailPwd", "text", title: "Email Password:", description: "Enter email password", required: true)
	input("EmailPort", "integer", title: "Port #:", description: "Enter port number, default 25", defaultValue: 25)
	input("From", "text", title: "From:", description: "", required: true)
	input("To", "text", title: "To:", description: "", required: true)
	input("Subject", "text", title: "Subject:", description: "")
	input("debugMode", "bool", title: "Enable logging", required: true, defaultValue: true)
}

metadata {
    definition (name: "HEmail", namespace: "adsavia", author: "Eric Huebl") {
        capability "Notification"
        capability "Actuator"
		capability "Telnet"
		//attribute "Telnet", ""
    }
}

def installed() {
    initialize()
}

def updated() {
    initialize()   
}

def initialize() {
    state.version = version()
	state.lastMsg = ""
	state.LastCode = 0
	state.EmailBody = ""
}

def deviceNotification(message) {

	state.EmailBody = "${message}"
	state.LastCode = 0
	logDebug("Connecting to ${EmailServer}:${EmailPort}")
	
	//telnetConnect([terminalType: 'VT100',termChars:[13,10]],EmailServer, EmailPort.toInteger(), null, null)
	//telnetConnect([termChars:[10]],EmailServer, EmailPort.toInteger(), null, null)
	//telnetConnect([termChars:[13]],EmailServer, EmailPort.toInteger(), null, null)
	//telnetConnect([termChars:[13,10]],EmailServer, EmailPort.toInteger(), null, null)
	telnetClose()
	telnetConnect(EmailServer, EmailPort.toInteger(), null, null)
}

def sendMsg(String msg) {
	logDebug("Sending ${msg}")
	return sendHubCommand(new hubitat.device.HubAction("${msg}", hubitat.device.Protocol.TELNET))
}

def parse(String msg) {
	
	logDebug("parse ${msg}")

	if (seqSend(220, msg, ["ehlo ${EmailDomain}"],"Connected to email server!",false)) {
		sendEvent([name: "telnet", value: "Connected."])
	}

	def auth = "\u0000${EmailUser}\u0000${EmailPwd}"
	String encoded = auth.bytes.encodeBase64().toString()
	seqSend(250, msg, ["AUTH PLAIN ${encoded}"],"Domain Configured!",false)

	def emlSubject = (Subject != null ? "${Subject}" : "")
	def sndMsgs =[
			"MAIL FROM: ${From}"
			, "RCPT TO: ${To}"
			, "DATA"
			, "From: ${From}"
			, "To: ${To}"
			, "Subject: ${emlSubject}"
			, "${state.EmailBody}"
			, "."
			, ""
	]
	if (seqSend(235, msg, sndMsgs,"Authentication Successful!",true)) {
		logDebug("Email message sent!")
		sendEvent([name: "telnet", value: "Disconnected, email sent."])
		telnetClose()
	}
	
	if ( msg != state.lastMsg){
		logDebug("setting state.lastMsg to ${msg}")
		state.lastMsg = "${msg}"
	}
	//sendEvent([name: "telnet", value: "${msg}"])
}

def telnetStatus(status) {
	logDebug("telnetStatus: ${status}")
	if (status == "receive error: Stream is closed" || status == "send error: Broken pipe (Write failed)") {
		logDebug("Stream is closed")
		try {
        	sendEvent([name: "telnet", value: "Disconnected. ${status}"])
		} catch(e) {
			logDebug(e)
		}
		telnetClose()
    }
}

boolean seqSend(int currCode, msg, msgs, dbgMsg, closeTelnet) {
	def seqSent = false
	if (currCode != state.LastCode) {
		if (msg.startsWith("${currCode}")) {
			state.LastCode = currCode
			logDebug("${dbgMsg}")
			msgs.each {
				sendMsg("${it}")
			}
			seqSent = true
			if (closeTelnet){
				telnetClose()			
			}
		}
	}
	return seqSent
}



def logDebug(txt) {
    try {
    	if (settings.debugMode) { log.debug("${txt}") }
    } catch(ex) {
    	log.error("logDebug unable to output requested data!")
    }
}
