Tracker
=======

Tracker is an Android Wear (WearOS) app for recording sport activities, mostly jogging. Published under the ![MIT](LICENSE.txt) license.

![Main Screen](Screenshots/device-2020-08-16-155128.png)

Features
--------
- No cloud!
- Time measurement (pretty obvious :))
- Current heart rate graphically shown around the face
- History of last heart rate measurements shown as a bar graph
- Average heart rate shown numerically
- GNSS status shown as an icon
  - dark: no GNSS signal
  - magenta: one satellite (no position available)
  - red: two satellites (no position available)
  - yellow: three satellites (no position available)
  - white: three or more satellites, position available

![Files View](Screenshots/device-2020-08-16-171227.png)
- Data stored internally in the watch
- Possible to delete all files via the UI
- Possible to send a selected file via the UI to a raw TCP socket (anything better still in development)
- Details of each recording
  - Date/time of the beginning of the activity
  - Duration of the activity (h:mm:ss)
  - File size in kB
  - Average and maximum heart rate (bpm)
  - Total steps and average steps per minute
  - GNSS-based data: average speed, total ascent and total descent

![Settings View](Screenshots/device-2020-08-16-171256.png)
- Configurable address and port for receiving files
  - Please note, private addresses (e.g. in your home network) will not work
- Toggle recording of: heart rate, steps, GNSS data, air pressure
- Toggle display always-on

Known issues
------------
- UI designed for round watches only
- File upload is as minimalistic as possible at the moment. You need a TCP-socket server available on a public IP in order to get the data. Give me some time to make it better.
- File upload works only with public IP addresses due to the routing limitations of the WearOS proxy. With enough time a companion app could solve it.
- GNSS (GPS, Glonass, Galileo, whatever your watch does support) is, at least on my device, very inaccurate, so the calculated speed, ascent and descent is mostly to make you laugh instead of any real value.
- Air pressure sensor, similarly, gives such a noise, that is it not worth recording its data. Maybe some filter would help - need to analyze it.
- GNSS always-on is still buggy in the implementation, that's why the switch is disabled per default.

To-do-List
----------
- Hide graphical heart rate in case no data received.
- Correct lifecycle management of the service in connection with the "GNSS always-on" function.
- Keep last activity time visible after display off/on cycle.
- Publish a .trk converter to .kml and .xlsx
- Make better file upload
- Offer constant data upload during the activity
- Analyze filters for air pressure sensor
- Nicer "delete all" confirmation dialog
