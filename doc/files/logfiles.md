Logfile format description
============================

Kayak uses the default socketCAN log file format. This is an ASCII line separated file format with each line representing a CAN frame. The frames have to be written ordered so that the last received frame is represented by the last line in the log file.
Some more description lines (as described below) may be added *only* before the first frame.

The default file extension is '.log'. Because a log file can get huge gzipping of the content is supported by Kayak. In this case the file extension must be '.log.gz'.

Frame syntax
------------
A frame description consists of three space separated fields of which none is optional.

    time bus_name id#data

### Time
The first field contains the time when the frame was received in seconds. It is either possible to use the system time for the timestamps or to use the start of loging as reference. Fractions of seconds have to be written after a '.' with a fix precision of 6 digits. The time is shown in decimal form and surrounded by '(' ')'.
##### Regex
    \([0-9]+\.[0-9]{6}\)

### Bus name
Name of the bus the frame was received from. The name must be at least one character and at maximum 16 characters long. Spaces or hashes are not allowed. Normally the name of the '/dev/can*' device should be used. E.g.: 'can0'.
##### Regex
    [a-zA-Z0-9]{1,16}

#### Id and data
This field is divided into two subfields by a hash '#'. The first one contains the id of the frame in hexadecimal representation and must be exactly three digits (in case of standard frame format) or eight digits (in case of extended frame format) long.
After the hash the frame data is inserted. Each byte of the frame is represented by two hexadecimal digits. This field is not padded with zeros because the data length code can be extracted of the length.
If the frame is a retransmission frame the data field is replaced by an 'r' or 'R'. All hex values may include lower and upper case letters.
##### Regex
    [A-Za-z0-9]{3,8}#[A-Fa-f0-9rR]+

### Examples

    (1267.100152) vcan0 3FA#0011223344556677
    (1267.235422) vcan0 3fb#r
    (1270.452665) vcan0 003fba35#0fbea3


Header
------
A log file starts with a header that may include multiple of the following fields. It is assumed that header ends with the first frame in the file. Every header field after the first frame is ignored.

### Description

A description may be used to describe the content of the logfile in a human readable form. This element may only once occur in the header.

##### Regex
    DESCRIPTION "[^"]+"

### Platform

The platform line is used to define the type of car in which the log file was recorded. Only capitals, numbers and underlines can be used in the platform string. As for the description this field is per file and may only be used once.

##### Regex
    PLATFORM [A-Z0-9_]+

### Device alias

With device aliases it is possible to give the CAN busses a human readable name for later assignment. An alias pair consists of a human readable name (which may not contain spaces) and the bus name as used in the frame entries. Multiple aliases for the same bus are not allowed. Also multiple busses may not share the same alias.
    
##### Regex
    DEVICE_ALIAS [A-Za-z0-9]+ [a-z0-9]{1,16}

Events
------
An event may be used anywhere in the file after the first frame. It is used to mark positions in the log file and add a description. A timestamp with the same syntax as for a frame may be included. If no timestamp is present it is assumed that the event occured at the time of the previous frame. Optionally it is also possible to provide a bus name on which the event occured.

##### Regex
    EVENT \([0-9]+\.[0-9]{6}\) [a-z0-9]{1,16} "[:alnum::punct:]+"

##### Examples
    EVENT (2.542456) can0 "Engine running"
    EVENT "Engine off"

Full example
------------
Below is a small example file:

    DEVICE_ALIAS Powertrain vcan0
    DEVICE_ALIAS Comfort vcan1
    PLATFORM MY_CAR_42
    DESCRIPTION "Short testdrive at the parking lot"
    (1258822325.798846) vcan0 5D1#002A
    (1258822325.798965) vcan0 271#R
    (1258822325.809524) vcan0 289#72.02.70.00
    (1258822325.820565) vcan0 0043F12E#0811000000
    (1258822325.824598) vcan1 540#1100FF00FF00000F
