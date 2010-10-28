BCM server
==========

The BCM server provides a network interface to the Socket CAN broadcast manager of the host. It can be controlled over a single TCP socket and supports transmission and reception of CAN frames. The used protocol is ASCII based and data transmissions have the following structure:

### Data structure ###
    < interface data_type ival_s ival_us can_id can_dlc [data]* >

### Data types ###
The field data_type must be present in every transmitted piece of data. This makes it easy to separate relevant from irellevant data. If the receiver does not understand a specific command or data transmission he can simply ignore it.
Two types of data are transmitted:

* Commands have a data_type with a single uppercase letter (e.g. A, U, D, ...)
* Normal data transfer has a data_type with a sigle lowercase letter, which denotes the type of data (e.g. f, e, s, ...)

### Commands for transmission ###
There are a few commands that control the transmission of CAN frames. Most of them are intervall based and the Socket CAN broadcast manager guarantees that the frames are sent cyclic with the given intervalls. To be able to control these transmission jobs they are automatically removed when the BCM server socket is closed.

##### Add a new frame for transmission #####
This command adds a new frame to the BCM queue. An intervall can be configured to have the frame sent cyclic.

Examples:
Send the CAN frame 123#1122334455667788 every second on vcan1
    < vcan1 A 1 0 123 8 11 22 33 44 55 66 77 88 >

Send the CAN frame 123#1122334455667788 every 10 usecs on vcan1
    < vcan1 A 0 10 123 8 11 22 33 44 55 66 77 88 >

Send the CAN frame 123#42424242 every 20 msecs on vcan1
    < vcan1 A 0 20000 123 4 42 42 42 42 >

##### Update a frame #####
This command updates a frame transmission job that was created via the 'A' command with new content. The transmission timers are not touched

Examle:
Update the CAN frame 123#42424242 with 123#112233 - no change of timers
    < vcan1 U 0 0 123 3 11 22 33 >

##### Delete a send job #####
A send job can be removed with the 'D' command.

Example:
Delete the cyclic send job from above
    < vcan1 D 0 0 123 0 >

##### Send a single frame #####
This command is used to send a single CAN frame only once.

Example:
Send a single CAN frame without cyclic transmission
    < can0 S 0 0 123 0 >

### Commands for reception ###
The commands for reception are 'R'eceive setup, 'F'ilter ID Setup and 'X' for delete.

##### Content filtering #####
This command is used to configure the broadcast manager for reception of frames with a given CAN ID. Frames are only sent when they match the pattern that is provided.

Examples: 
Receive CAN ID 0x123 from vcan1 and check for changes in the first byte
    < vcan1 R 0 0 123 1 FF >

Receive CAN ID 0x123 from vcan1 and check for changes in given mask
    < vcan1 R 0 0 123 8 FF 00 F8 00 00 00 00 00 >

As above but throttle receive update rate down to 1.5 seconds
    < vcan1 R 1 500000 123 8 FF 00 F8 00 00 00 00 00 >

##### Filter for CAN ID #####
Adds a filter for a CAN ID. The frames are sent regardless of their content.

Example:
Filter for CAN ID 0x123 from vcan1 without content filtering
    < vcan1 F 0 0 123 0 >

##### Delete a filter #####
This deletes all 'R' or 'F' filters for a specific CAN ID.

Example:
Delete receive filter ('R' or 'F') for CAN ID 0x123
    < vcan1 X 0 0 123 0 >


### Frame transmission ###
CAN messages received by the given filters are send in the format:
    < interface f can_id can_dlc [data]* >

Example:
when receiving a CAN message from vcan1 with CAN ID 0x123 , data length 4 and data 0x11, 0x22, 0x33 and 0x44
    < vcan1 f 123 4 11 22 33 44 >

Extensions
----------

If the BCM server is supposed to be a fundament for Kayak it needs to provide a few more features:

### General ###

* It shall be possible to subscribe to all frames that are on the bus. This is necessary if Kayak has no information about the messages on the bus and we simply want to display all frames that drop in. This was implemented, for example, in the cansniffer ( http://svn.berlios.de/wsvn/socketcan/trunk/can-utils/cansniffer.c ) were the BCM subscribes to all 
2048 (7FFh) 11-Bit identifiers via a for-loop. This is pragmatic solution, but will take longer for 29-Bit identifiers. 
Alternatively Kayak may use the RAW socket for simple dump functionality, showing the whole CAN bus traffic and the BCM functionality for closer inspection of messages and signals of interest.

* Error frames shall be reported in line with the normal frames. It is essential that Kayak is informed about error frames
* The BCM server must provide service discovery, bus configuration and bus statistics. These features are described in the following sections

### Service discovery ###
Because configuration shall be as easy as possible and the virtual CAN bus and the Kayak instance are not necessarily on the same machine a machanism for service discovery is necessary.

The server sends a UDP broadcast beacon to port 42000 on the subnet where the server port was bound. The interval for these discovery beacons shall not be longer than three seconds. Because the BCM server handles all communication (even for multiple busses) over a single TCP connection the broadcast must provide information about all busses that are accessible through the BCM server.

##### Content #####

Required:
* Name of the device that provides access to the busses. On linux machines this could be the hostname
* Name of the busses (in case of socketCAN and embedded this should be the same as the device name)
* URL with port and IP address. If the server is listening on multiple sockets all of them should be included in the beacon
* Device type the service is running on

Optional:
* Description of the service in a human readable form

##### Device types ######

* SocketCAN - general socketCAN service on a linux machine
* embedded - embedded linux with access to a bus over socketCAN
* adapter - e.g. microcontroller driven CAN to ethernet adapter

##### Structure #####

For simple parsing and a human readable schema XML is used to structure the information in a CAN beacon.

##### Example #####

    <CANBeacon name="HeartOfGold" type="SocketCAN" description="A human readable description"/>
        <URL>can://127.0.0.1:28600</URL>
        <Bus name="vcan0"/>
        <Bus name="vcan1"/>
    </CANBeacon>

### Error frame transmission ###
Error frames are sent similar to normal frames only distinguished by the data_type 'e'. An error frame always has the length of 8 data bytes. Because of this only the fields can_id and data are necessary (see socketcan/can/error.h for further information):
    < interface e can_id data >

### Configuration ###

##### Configure the bittiming #####
The protocol enables the client to change the bittiming of a given bus as provided by set link. Automatic bitrate configuration by the kernel is not supported because it is not guaranteed that the corresponding option was enabled during compile time (e.g. in Ubuntu 10.10 it isn't). This way it it also easyer to implement the function in a microcontroller based adapter.
    < can0 B bitrate sample_point tq prop_seg phase_seg1 phase_seg2 sjw brp >

##### Set the controlmode #####
The control mode controls if the bus is set to listen only, if sent packages are looped back and if the controller is configured to take three samples. The following command provides access to these settings. Each field must be set to '0' or '1' to disable or enable the setting.

    < can0 C listen_only loopback three_samples >

##### Enable or disable statistic transmission #####
THis command requests the transmission of statistic information in line with the normal information. An intervall must be set to specify how often the transmission should occur. A flag of '1' enables the transmission and a flag of '0' disables it.

    < vcan0 E flag ival_ms >



### Statistics ###


