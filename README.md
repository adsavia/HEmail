#########################################################
# HEmail - Simple Email Transport Notification Device
#########################################################

This is a basic system for providing notifications from the Hubitat Elevation to 
specific recipients via email. It can also be used for unlimited text notifications IF your
Phone provider offers an email to text email address. Note: most US providers DO..

#########################################################
### US Cell Providers "Email to Txt" Email Addresses
#########################################################
###### AT&T: {cell number}@txt.att.net (SMS), {cell number}@mms.att.net (MMS)
###### T-Mobile: {cell number}@tmomail.net (SMS & MMS)
###### Verizon: {cell number}@vtext.com (SMS), {cell number}@vzwpix.com (MMS)
###### Sprint: {cell number}@messaging.sprintpcs.com (SMS), {cell number}@pm.sprint.com (MMS)
###### XFinity Mobile: {cell number}@vtext.com (SMS), {cell number}@mypixmessages.com (MMS)
###### Virgin Mobile: {cell number}@vmobl.com (SMS), {cell number}@vmpix.com (MMS)
###### Tracfone: {cell number}@mmst5.tracfone.com (MMS)
###### Metro PCS: {cell number}@mymetropcs.com (SMS & MMS)
###### Boost Mobile: {cell number}@sms.myboostmobile.com (SMS), {cell number}@myboostmobile.com (MMS)
###### Cricket: {cell number}@sms.cricketwireless.net (SMS), {cell number}@mms.cricketwireless.net (MMS)
###### Republic Wireless: {cell number}@text.republicwireless.com (SMS)
###### Google Fi (Project Fi): {cell number}@msg.fi.google.com (SMS & MMS)
###### U.S. Cellular: {cell number}@email.uscc.net (SMS), {cell number}@mms.uscc.net (MMS)
###### Ting: {cell number}@message.ting.com
###### Consumer Cellular: {cell number}@mailmymobile.net
###### C-Spire: {cell number}@cspire1.com
###### Page Plus: {cell number}@vtext.com
#########################################################
#### The system works by installing by a custom device that 
#### communicates with an smtp server via telnet..
#########################################################

##### Notes:
1) THIS WILL NOT WORK WITH GMAIL or other providers that require SSL/TLS!!!!!
2) One hard coded device per set of contact(s).
3) For the "Email User" make sure to include the domain if your mail server requires it (most do).
   ex. "myuser@mydomain.com" - if not then it's okay to use just user login "myuser".
4) If the "Email User" and "From User" do not match - outgoing email may get flagged by spam filtering.

##### Requirements:

Hubitat HE hub with the custom HEmail device added to the "Drivers Code" section.

#########################################################

- From github view raw format for the https://github.com/adsavia/HEmail/raw/master/HEmail.groovy
- Cut and paste it into a new device in the drivers code section on your HE. Save
- Add new device, select "HEmail" device. Label something like "HEmail-MyEmail" or "HEmail-MyPhone", save.
- Fill in the required preferences
  - "Email Server"		- SMTP Server with plain authentication (SSL/TLS will NOT work)
  - "Email Domain"		- mydomain.com
  - "Email User"		- myuser@mydomain.com
  - "Email Password"	- myuserpassword 
  - "Port #" 			- OPTIONAL, default is 25, if that doesn't work try 587, again no SSL/TLS capability unfortunately.
  - "From" 				- myuser@yourdomain.com
  - "To" 				- somebody@tosendthisto.com
  - "Subject" 			- some subject like "HE Notification Alert".
- Save preferences.
- To test enter a test message in "Device Notification" and click on the 
Device Notification header. If everything is working you should recieve an 
email with your test message and parameters you defined.
- Use device for any notifications you want!!!!!
- 20/01/07 !!!! EXTRA functionality added - IF you supply a JSON Style string with "Subject" and "Body" then those will be used
instead of the defined ones in preferences. This allows for a more dynamic email capability. 
EXAMPLE: {"Subject":"Subject 1","Body":"This is the body of the email, can use HE variables here (and in the subject line) too.."}

