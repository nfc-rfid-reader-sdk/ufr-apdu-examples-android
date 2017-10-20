Digital Logic uFR Android Library

uFR Android library allows developers of the Android applications to communicate with all uFR readers directly or through specialized D-Logic interfaces such as usb_2_usb, usb_2_usb_dock etc.

Depends on FTDI Java D2xx lib (d2xx.jar) library. You can download this library and place it in prj_folder/app/libs/ from:
http://www.ftdichip.com/Android.htm

Implementation:
---------------
The library is given in the form of source code. The library is in the Java package "net.dlogic.ufr" and need to be imported to your project with a directive:
import net.dlogic.ufr.DlReader;

If you use Android Studio, library need to be in a path:
prj_folder/app/src/main/java/net/dlogic/ufr/DlReader.java

Class DlReader is created by the singleton design pattern and its instance you get by calling:
DlReaderNew.getInstance(Context context, int AccessoryFilterXmlId, int DevDescFilterXmlId);

where for the context you can use "this" keyword in your Activity, while AccessoryFilterXmlId and DevDescFilterXmlId you can get with:
int AccessoryFilterXmlId = R.xml.accessory_filter;
int DevDescFilterXmlId = R.xml.dev_desc_filter;
from your Activity also. 

In a path:
prj_folder/app/src/main/res/xml/
you should place next files:
accessory_filter.xml
dev_desc_filter.xml
device_filter.xml

which content should be:
accessory_filter.xml
----------------------------------------------------------------------------------------
<?xml version="1.0" encoding="UTF-8"?>
<resources>
    <usb-accessory manufacturer="FTDI" model="Android Accessory FT312D" version="1.0" />
    <usb-accessory manufacturer="D-Logic" model="FT312D-RS485" version="1.0" />
    <usb-accessory manufacturer="FTDI" model="FTDIUARTDemo" version="1.0" />
    <usb-accessory manufacturer="DLogic" model="usb_2_usb" version="1.0" />
    <usb-accessory manufacturer="DLogic" model="usb_2_usb_dock" version="1.0" />
</resources>
----------------------------------------------------------------------------------------

device_filter.xml
----------------------------------------------------------------------------------------
<?xml version="1.0" encoding="utf-8"?>

<resources>
	<usb-device vendor-id="1027" product-id="24577" /> <!-- FT232RL -->
</resources>
----------------------------------------------------------------------------------------

dev_desc_filter.xml
----------------------------------------------------------------------------------------
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <usb-device description="nFR USB CLASSIC" />
    <usb-device description="nFR USB PRO" />
    <usb-device description="uFR CLASSIC" />
    <usb-device description="uFR ADVANCE" />
    <usb-device description="uFR PRO" />
    <usb-device description="uFR XR CLASSIC" />
    <usb-device description="uFR XRC CLASSIC" />
    <usb-device description="uFR XRC  CLASSIC" />
</resources>
----------------------------------------------------------------------------------------

Mandatory part of the AndroidManifest.xml file should be:
	<uses-feature android:name="android.hardware.usb.host" />
	<uses-feature android:name="android.hardware.usb.accessory" />

enclosed in <manifest> tag and:

	<intent-filter>
        	<action android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" />
        </intent-filter>
       	<meta-data android:name="android.hardware.usb.action.USB_DEVICE_ATTACHED" android:resource="@xml/device_filter" />

        <intent-filter>
                <action android:name="android.hardware.usb.action.USB_ACCESSORY_ATTACHED" />
        </intent-filter>
        <meta-data android:name="android.hardware.usb.action.USB_ACCESSORY_ATTACHED" android:resource="@xml/accessory_filter" 

enclosed in <activity> tag.

Once you have an instance of a DlReader class acquired with DlReaderNew.getInstance() it is necessary to open the attached device by calling the method:
DlReader.open();

Device is closing by calling the method:
DlReader.close();

Other methods that are used to communicate with the connected uFR reader are:
int getReaderType()
byte[] getCardIdEx(CardParams c_params); // class CardParams Data je u daljem tekstu
byte[] blockRead(byte block_address, byte auth_mode, byte[] key);
void readerUiSignal(byte lightSignalMode, byte beepSignalMode);
void enterSleepMode();
void leaveSleepMode();

    public static class CardParams {
        private byte sak;
        private byte uid_size;

        public CardParams() {
            sak = 0;
            uid_size = 0;
        }

        public void setSak(byte p_sak) {
            sak = p_sak;
        }

        public byte getSak() {
            return sak;
        }

        public void setUidSize(byte p_uid_size) {
            uid_size = p_uid_size;
        }

        public byte getUidSize() {
            return uid_size;
        }
    }
