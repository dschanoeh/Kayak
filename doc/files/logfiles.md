Logfile format description
============================

Kayak uses the default socketCAN log file format. This is an ASCII line separated file format with each line representing a CAN frame. The frames have to be written ordered so that the last received frame is represented by the last line in the log file.
Some more description lines (as described below) may be added *only* before the first frame.

The default file extension is '.log'. Because a log file can get huge gzipping of the content is supported. In this case the file extension must be '.log.gz'.

Frame syntax
------------
A frame description consists of three space separated fields of which none is optional.

    time bus_name id#data

### Time
The first field contains the time when the frame was received in seconds. It is either possible to use the system time for the timestamps or to use the start of loging as reference. Fractions of seconds have to be written after a '.' with a fix precision of 6 digits. The time is shown in decimal form and surrounded by '(' ')'.
##### Regex
    \([0-9]+\.[0-9]{6}\)

### Bus name
Name of the bus the frame was received from. The name must be at least one character and at maximum 6 characters long. Spaces or hashes are not allowed. Normally the name of the '/dev/can*' device should be used. E.g.: 'can0'.
##### Regex
    [a-z0-9]{1,6}

#### Id and data
This field is divided by a hash '#' into two subfields. The first one contains the id of the frame in hexadecimal representation and must be exactly three digits long.
After the hash the frame data is inserted. Each byte of the frame is represented by two hexadecimal digits. This field is not padded with zeros because the data length code can be extracted of the length.
##### Regex
    [A-Z0-9]{3}#[A-Z0-9]{2,16}

### Example

    (1267.100152) vcan0 3FA#0011223344556677


Description lines
-----------------
### Description

A description may be used to describe the content of the logfile in a human readable form.

##### Regex
    DESCRIPTION "[:alnum::punct:]+"

### Platform

The platform line is used to define the type of car in which the log file was recorded. Only capitals, numbers and underlines can be used in the platform string.

##### Regex
    PLATFORM [A-Z0-9_]+

### Device alias

With device aliases it is possible to give the CAN busses a human readable name for later assignment. An alias pair consists of a human readable name (which may not contain spaces) and the bus name as used in the frame entries.
    
##### Regex
    DEVICE_ALIAS [A-Za-z0-9]+ [a-z0-9]{1,6}

Full example
------------
Below is a small example file:

    DEVICE_ALIAS Powertrain vcan0
    DEVICE_ALIAS Comfort vcan1
    PLATFORM MY_CAR_42
    DESCRIPTION "Short testdrive at the parking lot"
    (1258822325.798846) vcan0 5D1#002A
    (1258822325.798965) vcan0 271#7100
    (1258822325.809524) vcan0 289#72027000
    (1258822325.820565) vcan0 12E#0811000000
    (1258822325.824598) vcan1 540#1100FF00FF00000F
