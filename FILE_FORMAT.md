Sport Activity File Format
==========================

The file consist of three sections written after each other:
* Header
* Basic data
* Data event list
And is closed with an end-of-file marker.

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

Data event list
---------------
Consists of any number of 'fields' with identifiers between 0x2000 and 0x2fff.

End-of-File marker
------------------
The marker is a 'field' of the identifier = 0xffff and an empty data area (length value = 0).
