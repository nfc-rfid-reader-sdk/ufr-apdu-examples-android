# Example of APDU communication with Android device and uFR Series readers

Software example written for Android Studio. Shows usage of uRF Series reader's commands and sending APDUs to ISO 14443-4 card. 

## Getting Started

Download project, open source in Android Studio, compile and run.
ufr-lib-android class is mandatory for this project.

### Prerequisites

uFR series reader, Android Studio V1.5 or higher, Android device with OTG support.
NOTE: if you have Android device without OTG support, you can still use it but only if you involve our APB - Android Power Bridge, uniquely designed device. Check D-logic site for details.  

### Installing

No installation needed. 

## Background explanation

uFR Series devices communication with Android is performed through physical USB port and established via our serial protocol. Android device must be capable of performing OTG functionality, e.g. wher Android would be a host while reader is a slave device.
Class ufr-lib-android is mandatory for this project, it adds functions and calls to device firmware features and commands.
Please refer to documents related to serial protocol in /ufr-doc/ project for more details.  

## Usage

Example provides basic funcionality to establish communication channel with card, send and receive APDU commands.
 

## License

This project is licensed under the ..... License - see the [LICENSE.md](LICENSE.md) file for details

## Acknowledgments

* Purpose of this software demo is to provide additional info about usage of uFR Series specific features.
* It is specific to mentioned hardware ONLY and some other hardware might have different approach, please bear in mind that.  

 
