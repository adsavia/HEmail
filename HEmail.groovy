/**
*   
*   File: HEmail.groovy
*   Platform: Hubitat
*   Modification History:
*       Date    Time		Who		What
*       v0.0.1	2019-04-15	13:38	Eric H	Initial creation of Telnet Email
*       v0.5	2019-04-17	15:21	Eric H	Beta release, full functionality.
*       v0.99	2019-04-19	10:30	Eric H	Created seqSend method to tighten up code a bit.
*											, set email user name to required.
*       v0.99.1	2019-04-19	11:00	Eric H	Modified telnet attribute to only show connect/disconnect
*       v0.99.2	2019-04-19	15:13	Eric H	Fixed history log, removed extra blank sends, added delay to sendMsg.
*       v0.99.3	2020-01-07	16:55	Eric H	Added some blank sends between subject and body and end. Also removed extra telnet close from after seqsend call
*       v0.99.4	2020-01-07	17:21	Eric H	Added ability to set subject and body using json in text.
*       v0.99.5	2020-01-07	17:44	Eric H	Moved call to logDebug for telnetstatus. Was showing redundant msg.
*       v0.99.6	2020-01-08	07:31	Eric H	Minor tweaks in order to resolve telnet close msgs. Changed call to seqsend with false since passing "quit". 
*                                           Adjusted seqsend - added error handling around telnet close.
*       v0.99.7	2020-01-08	07:55	Eric H	Additional Minor tweaks Removed extra code from telnetStatus function changed to straight debug and event call.
*											
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
def version() {"v0.99.7"}

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
	
	telnetClose()
	telnetConnect(EmailServer, EmailPort.toInteger(), null, null)
}

def sendMsg(String msg, Integer millsec) {
	logDebug("Sending ${msg}")
	
	def hubCmd = sendHubCommand(new hubitat.device.HubAction("${msg}", hubitat.device.Protocol.TELNET))

	pauseExecution(millsec)
	
	return hubCmd
}

def parse(String msg) {
	
	logDebug("parse ${msg}")

	if (seqSend(220, msg, ["ehlo ${EmailDomain}"],"Connected to email server!",false)) {
		sendEvent([name: "telnet", value: "Connected."])
	}

	def auth = "\u0000${EmailUser}\u0000${EmailPwd}"
	String encoded = auth.bytes.encodeBase64().toString()
	seqSend(250, msg, ["AUTH PLAIN ${encoded}"],"Domain Configured!",false)
    
    
    def msgData = "${state.EmailBody}"
	def emlBody = ""
	def emlSubject = ""
	
	if(msgData.substring(0,1) == "{") {
		// Parse out message for subject/text
		def slurper = new groovy.json.JsonSlurper()
		def result = slurper.parseText(msgData)
		emlBody = result.Body
		emlSubject = (result.Subject != null ? result.Subject : "")
	} else {
		emlBody = msgData
		emlSubject = (Subject != null ? "${Subject}" : "")
	}
   
    
	def sndMsgs =[
			"MAIL FROM: ${From}"
			, "RCPT TO: ${To}"
			, "DATA"
			, "From: ${From}"
			, "To: ${To}"
			, "Subject: ${emlSubject}"
            , ""
			, "${emlBody}"
            , ""
			, "."
			, "quit"
	]
	if (seqSend(235, msg, sndMsgs,"Authentication Successful!",false)) {
		logDebug("Email message sent!")
		sendEvent([name: "telnet", value: "Disconnected, email sent."])
		//telnetClose()
	}
	
	if ( msg != state.lastMsg){
		logDebug("setting state.lastMsg to ${msg}")
		state.lastMsg = "${msg}"
	}
	//sendEvent([name: "telnet", value: "${msg}"])
}

def telnetStatus(status) {
    logDebug("telnetStatus: ${status}")
    sendEvent([name: "telnet", value: "${status}"])
}

boolean seqSend(int currCode, msg, msgs, dbgMsg, closeTelnet) {
	def seqSent = false
	if (currCode != state.LastCode) {
		if (msg.startsWith("${currCode}")) {
			state.LastCode = currCode
			logDebug("${dbgMsg}")
			msgs.each {
				sendMsg("${it}",250)
			}
			seqSent = true
			if (closeTelnet){
                try {
                    telnetClose()
                } catch(e) {
                    logDebug("Connection already closed, No need to close connection again.")
                }
                
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
