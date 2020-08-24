Sport Activity File Format
==========================

The file consist of three sections written after each other:
* Header
* Basic data
* Data event list
And is closed with an end-of-file marker.

Data types
----------
All integers (16, 32 and 64 bit) are stored as big-endians.
All floats (32 and 64 bit) are stored in IEEE 754 format. Their bit-represenation is stored as big-endians.

Field data format
-----------------
A 'field' is a data structure used in the file.
Offset | Size | Value | Meaning
-------|------|-------|--------
0 | 1 Byte | '#' | Start-of-the-Field marker.
1 | 2 Bytes | integer | Field identifier.
3 | 4 Bytes | integer | Length of the data.
7 | equal to "Length of the data" | | The contained data.

Header
------
Offset | Size | Value | Meaning
-------|------|-------|--------
0 | 20 Bytes | "//MILTSCHEK/TRACKER/" | File type identifier.
20 | 2 Bytes | integer = 2 | Version of the file.

Basic data
----------
Consists of any number of 'fields' with identifiers between 0x1000 and 0x1fff.

Id | Description | Length (bytes) | Data type | Units
---|-------------|----------------|-----------|------
0x1001 | Real-time-clock timestamp of the beginning of the sport activity. | 8 | long integer | milliseconds since Jan, 1st 1970
0x1002 | Real-time-clock timestamp of the end of the sport activity. | 8 | long integer | milliseconds since Jan, 1st 1979
0x1003 | Ticks timestamp of the beginning of the sport activity (relevant for comparison of sensor events' timestamps). | 8 | long integer | nanoseconds abstract
0x1004 | Ticks timestamp of the end of the sport activity (relevant for comparison of sensor events' timestamps). | 8 | long integer | nanoseconds abstract
0x1011 | Average heart rate during the sport activity. | 4 | float | beats per minute
0x1012 | Maximum heart rate during the sport activity. | 4 | integer | beats per minute
0x1013 | Total number of steps during the sport activity. | 4 | integer | number
0x1014 | Average steps rate during the sport activity. | 4 | float | steps per minute
0x1015 | Total ascent during the sport activity (using GNSS data). | 4 | float | meters
0x1016 | Total descent during the sport activity (using GNSS data). | 4 | float | meters
0x1017 | Average speed during the sport activity (using GNSS data). | 4 | float | meters per second

Data event list
---------------
Consists of any number of 'fields' with identifiers between 0x2000 and 0x2fff. The data area of each field usually contain multiple values stored after each other with no additional separators.

Id | Description | Length (bytes) | Data type | Value | Units
---|-------------|----------------|-----------|-------|------
0x2011 | Heart rate sensor event. | 8 | long integer | Timestamp of the event (comparable to 0x1003 and 0x1004). | nanoseconds abstract
||| 4 | integer | Measured heart rate. | beats per minute
||| 4 | integer | Sensor accuracy. | See: [Sensor accuracy](#sensor-accuracy).
0x2021 | Steps counter event. | 8 | long integer | Timestamp of the event (comparable to 0x1003 and 0x1004). | nanoseconds abstract
||| 4 | integer | Steps counter state (always total amount of steps since sensor reset). | number
||| 4 | integer | Sensor accuracy. | See: [Sensor accuracy](#sensor-accuracy).
0x2031 | Air pressure sensor event. | 8 | long integer | Timestamp of the event (comparable to 0x1003 and 0x1004). | nanoseconds abstract
||| 4 | float | Measured air pressure. | millibars
||| 4 | integer | Sensor accuracy. | See: [Sensor accuracy](#sensor-accuracy).
0x2041 | Geo (GNSS) sensor event. | 8 | long integer | Timestamp of the event (comparable to 0x1003 and 0x1004). | nanoseconds abstract
||| 8 | long integer | Timestamp of the fix (ticks, comparable to 0x1003 and 0x1004). | nanoseconds abstract
||| 8 | long integer | Real-time-clock timestamp of the fix. | milliseconds since Jan, 1st 1970
||| 8 | double | Latitude. | degrees
||| 8 | double | Longitude. | degrees
||| 4 | float | Lateral accuracy. | meters
||| 8 | double | Altitude. | meters
||| 4 | float | Bearing. | degrees
||| 4 | float | Speed. | meters per second
||| 4 | integer | Sensor accuracy. Not used for GNSS. | See: [Sensor accuracy](#sensor-accuracy).

End-of-File marker
------------------
The marker is a 'field' of the identifier = 0xffff and an empty data area (length value = 0).

Id | Description | Length (bytes) | Data type | Units
---|-------------|----------------|-----------|------
0xffff | End-of-file marker. | 0 | n/a | n/a

#### Sensor accuracy
---------------
Some sensors do report the measurement accuracy. The summary values (e.g. average heart rate) are calculated on events of at least low accuracy.

Value | Meaning
------|--------
-1 | Measurement not to be trusted as the sensor did not have a contact with what it was supposed to measure.
0 | Measurement is unreliable (e.g. uncalibrated).
1 | Low accuracy (e.g. calibration needed).
2 | Medium accuracy (e.g. calibration could improve the readings).
3 | High accuracy (maximum available for the sensor).
