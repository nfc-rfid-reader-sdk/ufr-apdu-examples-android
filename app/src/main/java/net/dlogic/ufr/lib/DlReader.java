package net.dlogic.ufr.lib;

/**
 * Created by dlogic on 12.5.2015.
 *
 * 20.07.2017. class DlReader v2.8
 *             - Added support for APDU commands:
 *             - New method:
 *               public synchronized void setISO14443_4_Mode() throws DlReaderException,
 *                                                                    InterruptedException
 *               Select ISO14443-4A tag and "open channel for the APDU commands" to the ISO14443-4A
 *               tag supporting APDU protocol.
 *             - New method:
 *               public synchronized void s_block_deselect() throws DlReaderException,
 *                                                                  InterruptedException
 *               Deselect ISO14443-4A tag. This is a mandatory call after finished one or more APDU
 *               transactions with a previously selected ISO14443-4A tag. Otherwise reader will be
 *               blocked for further work and you will be forced to call readerReset().
 *             - New method:
 *               public synchronized byte[] APDUPlainTransceive(byte[] c_apdu)
 *                                                    throws DlReaderException, InterruptedException
 *               Function that send c_apdu bytes to the to the ISO14443-4A tag supporting APDU
 *               protocol and, after successfully executed APDU command by the tag selected returns
 *               R-APDU. Count of the R-APDU bytes varies depending on the issued APDU command.
 *               Minimum number of the R-APDU bytes is 2 and in that case contains SW1 and SW2.
 * 23.06.2017. class DlReader v2.7
 *             - New method:
 *               public synchronized void blockWrite(byte[] data, byte block_address,
 *                        byte auth_mode, byte[] key) throws DlReaderException, InterruptedException
 *               Write data (16 bytes) in to the blocks designated by the block_address parameter.
 *               On Mifare Classic compatible cards You have to skip direct write in to sector
 *               trailers. Otherwise You get an exception with code = 10
 *               (FORBIDEN_DIRECT_WRITE_IN_SECTOR_TRAILER = 0x0A).
 * 11.07.2016. class DlReader v2.6
 *             - Fixed unchecked java.lang.NullPointerException (by throwing
 *               new DlReaderException("UFR_COMMUNICATION_TERMINATED") )
 *               in net.dlogic.ufr.lib.DlReader$ComProtocol.portRead from
 *               com.ftdi.j2xx.ProcessInCtrl.readBulkInData() at
 *               com.ftdi.j2xx.FT_Device.read().
 * 28.06.2016. class DlReader v2.5
 *             - Improvements in closeAoAEndpoints() when uFR device is in sleep mode on closing.
 *               In D-Logic usb_2_usb power bridge interface is implemented USB2USB_PING command
 *               for getting response when only bridge device is connected and when uFR is in sleep.
 *               Applicable on D-Logic usb_2_usb power bridge interface from firmware version 6.
 * 17.06.2016. class DlReader v2.4
 *             - Improvements in killAoAEndpoints().
 *             - New method usb2usbResetUfr() which serve as a patch for LG G3 and similar Android
 *               devices connected to D-Logic usb_2_usb power bridge interface.
 *             - Patch for LG G3 and similar Android devices implemented in open() method.
 *             - The problem with LG G3 and similar Android devices is bad power status detecting
 *               algorithm in their API. When usb_2_usb power bridge interface switch from AoA to
 *               OTG mode, Android device does not detect there is no power supply attached until
 *               OTG ID pin goes logic high for a while. This will reset and restore true Android
 *               power status.
 *             - For most devices having Android version greater than 4.4 (KitKat), the only way
 *               when attachedAndroid Open Accessory device is plugged out from the USB port is to
 *               terminate Application by calling.
 *               android.os.Process.killProcess(android.os.Process.myPid()).
 * 25.05.2016. class DlReader v2.3
 *             - Improved algorithm for detecting connection/disconnection of the uFR devices.
 *             - For most devices not supporting USB Host mode (without OTG) only way when attached
 *               Android Open Accessory device is plugged out from the USB port is to terminate
 *               Application by calling android.os.Process.killProcess(android.os.Process.myPid()).
 * 19.05.2016. class DlReader v2.2
 *             - Added support for D-Logic FT312D-RS485 interface
 * 18.05.2016. class DlReader v2.1
 *             - Fixed algorithm for detecting connection/disconnection of the uFR devices, based
 *               only on chaging status now. We don't use UsbManager intents any more because they
 *               have proven to be unreliable on some Android devices.
 * 28.04.2016. class DlReader v1.9
 *             - BugFix:
 *               in commonBlockRead() fixed wrong checkXOR comparison due to implicit integer
 *               promotion during calculation.
 * 22.04.2016. class DlReader v1.8
 *             - Added support for DLogic AoA interfaces based on FTDI Vinculum II 
 *               and AoA uFR interfaces based on FT312D chips.
 *             - AoA support implemented without any public methods.
 * 04.12.2015. class DlReader v1.7
 *             - Added method getNumOfDlDevices()
 * 03.12.2015. class DlReader v1.6
 *             - in ComProtocol.portWrite() from this version we use an nonblocking write()
 *               overloaded method from the FTDI class and we have implemented retry count
 *               (Consts.MAX_COMMUNICATION_BREAK_RETRIES times) on communication break. If
 *               number of communication break retries exceeded, raised DlReaderException have
 *               Consts.DL_READER_IS_NOT_CONNECTED err_code and usb device is closed. This algorithm
 *               solve usb otg device disconnect detection problem on the Runbo X5 and probably some
 *               other Android smart phones with usb otg port.
 * 01.12.2015. class DlReader v1.5
 *             - New method:
 *                   public synchronized boolean readerStillConnected()
 *                       returns true if device still connected to the system (false otherwise).
 *             - New Status Consts in DlReader.DlReaderException (defined in DlReader.Consts):
 *                   public static final int DL_READER_COMMUNICATION_BREAK = 0x50;
 *                       raised when there is communication problem (mostly hardware problems).
 *                   public static final int DL_READER_IS_NOT_CONNECTED = 0x104;
 *                       raised when the device is disconnected from the Android system (if device is
 *                       still attached to the system check cable, connectors and/or device).
 * 15.06.2015. class DlReader v1.3
 *             - Implementation of the new functionality (Sleep mode of the attached nFR device).
 *             - New methods:   public synchronized void enterSleepMode() throws DlReaderException, InterruptedException
 *                              public synchronized void leaveSleepMode() throws DlReaderException, InterruptedException
 * 12.05.2015. class DlReader v1.0
 *             - First public release.
 */

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.ParcelFileDescriptor;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class DlReader {
    private static final String DL_READER_PACKAGE_NAME = "net.dlogic.ufr.lib";
    private static final String ACTION_USB_PERMISSION = DL_READER_PACKAGE_NAME + ".USB_PERMISSION";
    private static final int MAX_BYTES = 256;
    private static final int CHUNK_BYTES = 64;
    private static DlReader mDlReader = null;
    private static D2xxManager ftD2xx = null;
    private static Context mContext = null;
    boolean isCharging;

    private static int mAccessoriyFilterItems = 0;
    private static ArrayList<String> mModelStrings;
    private static ArrayList<String> mManufacturerStrings;
    private static ArrayList<String> mVersionStrings;

    private static ArrayList<String> mDeviceDescriptionStrings;

    private static ParcelFileDescriptor mAoAFileDescriptor = null;
    private static FileInputStream mAoAInStream = null;
    private static FileOutputStream mAoAOutStream = null;

    private AoAReadThread mAoAReadThread = null;
    private static byte[] AoAReadBuffer = new byte[MAX_BYTES]; // circular buffer
    private static int totalBytes;
    private static int writeIndex;
    private static int readIndex;

    private static boolean mSupportUsbHost = false;
    private static boolean mSupportAoA = false;

    private static boolean mPermissionRequestPending = false;
    private static PendingIntent mPendingIntent = null;
    private static IntentFilter mPermissionFilter = null;
    private UsbManager mUsbManager = null;
    private static FT_Device ft_device = null;
    private static UsbAccessory mAccessory = null;
    private static int open_index = -1;
    private static int retry_cnt = 0;
    //----------------------------------------------------------------------------------------------
    /*/ Broadcast Receivers:
    private BroadcastReceiver mAoAPlugIntents = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

//            Toast.makeText(context, action, Toast.LENGTH_SHORT).show();

            if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                if (mAccessory != null) {
                    killAoAEndpoints();
                    mAccessory = null;
                    open_index = -1;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(action)) {
                mAccessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
            }
        }
    };*/

    private BroadcastReceiver mPowerConnectionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            synchronized(this) {
                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = context.registerReceiver(null, ifilter);

                //            int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                //            isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING) || (status == BatteryManager.BATTERY_STATUS_FULL);

                //            int chargePlug = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                //            boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
                //            boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                isCharging = (status == BatteryManager.BATTERY_PLUGGED_USB);

                if (open_index > -1) {
                    if (isCharging && (ft_device != null) && (ft_device.isOpen())) {
                        ft_device.close();
                        ft_device = null;
                        open_index = -1;
                    }
                    if (!isCharging && (mAccessory != null)) {
                        //if (!mSupportUsbHost)
                        if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                            //System.runFinalizersOnExit(true);
                            //System.exit(0);
                            android.os.Process.killProcess(android.os.Process.myPid()); // For most of these type of devices this is only solution.
                        } else {
                            killAoAEndpoints();
                            mAccessory = null;
                            open_index = -1;
                        }
                    }
                }
            }
        }
    };

    private final BroadcastReceiver mAoADevicePermissions = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

//            Toast.makeText(context, action, Toast.LENGTH_LONG).show();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized(this) {
                    UsbAccessory accessory = intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if(intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        mAccessory = accessory;
                        if (!openAoAEndpoints(mAccessory)) {
                            mAccessory = null;
                        } else {
                            open_index = 0;
                        }
                    }
                    mPermissionRequestPending = false;
                }
            }
        }
    };
    //----------------------------------------------------------------------------------------------

    // constructor:
    private DlReader(Context context, int AccessoryFilterXmlId, int DevDescFilterXmlId) throws DlReaderException {
        Context appContext;

        if(context == null) {
            throw new DlReaderException("DlReader failed: Can not find parentContext!");
        } else {
            updateContext(context);
            appContext = mContext.getApplicationContext();
            mUsbManager = (UsbManager)appContext.getSystemService(Context.USB_SERVICE);
            if(mUsbManager == null) {
                throw new DlReaderException("DlReader failed: USB not supported on this Android device");
            } else {

                mSupportUsbHost = mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_HOST);
                mSupportAoA = mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_USB_ACCESSORY);

                if (!mSupportUsbHost && !mSupportAoA) {
                    throw new DlReaderException("DlReader failed: USB not supported on this Android device");
                }

                if (mSupportUsbHost) {
                    try {
                        ftD2xx = D2xxManager.getInstance(context);
                    } catch (D2xxManager.D2xxException ex) {
                        throw new DlReaderException("Can't open usb host driver manager.");
                    }
                }

//                IntentFilter filter = new IntentFilter();
//                filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
//                filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
                //appContext.registerReceiver(this.mAoAPlugIntents, filter);

                IntentFilter filter = new IntentFilter();
                filter.addAction(Intent.ACTION_POWER_CONNECTED);
                filter.addAction(Intent.ACTION_POWER_DISCONNECTED);
                appContext.registerReceiver(this.mPowerConnectionReceiver, filter);

                XmlPullParser xpp = mContext.getResources().getXml(AccessoryFilterXmlId);
                mModelStrings = new ArrayList<String>();
                mManufacturerStrings = new ArrayList<String>();
                mVersionStrings = new ArrayList<String>();
                try {
                    while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                        if (xpp.getEventType() == XmlPullParser.START_TAG) {
                            if (xpp.getName().equals("usb-accessory")) {
                                ++mAccessoriyFilterItems;
                                mModelStrings.add("mModel=" + xpp.getAttributeValue(null, "model"));
                                mManufacturerStrings.add("mManufacturer=" + xpp.getAttributeValue(null, "manufacturer"));
                                mVersionStrings.add("mVersion=" + xpp.getAttributeValue(null, "version"));
                            }
                        }
                        xpp.next();
                    }
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                xpp = mContext.getResources().getXml(DevDescFilterXmlId);
                mDeviceDescriptionStrings = new ArrayList<String>();
                try {
                    while (xpp.getEventType() != XmlPullParser.END_DOCUMENT) {
                        if (xpp.getEventType() == XmlPullParser.START_TAG) {
                            if (xpp.getName().equals("usb-device")) {
                                mDeviceDescriptionStrings.add(xpp.getAttributeValue(null, "description"));
                            }
                        }
                        xpp.next();
                    }
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                Intent batteryStatus = context.registerReceiver(null, ifilter);
//                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//                isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING) || (status == BatteryManager.BATTERY_STATUS_FULL);
                int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
                isCharging = (status == BatteryManager.BATTERY_PLUGGED_USB);
            }
        }
    }

    public static synchronized DlReader getInstance(Context context, int AccessoryFilterXmlId, int DevDescFilterXmlId) throws DlReaderException {

        if (mDlReader == null) {
            mDlReader = new DlReader(context, AccessoryFilterXmlId, DevDescFilterXmlId);
        }

        return mDlReader;
    }

    private boolean updateContext(Context context) {

        if(context == null) {
            return false;
        } else {
            if(mContext != context) {
                mContext = context;
                mPendingIntent = PendingIntent.getBroadcast(mContext.getApplicationContext(), 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_UPDATE_CURRENT);
                mPermissionFilter = new IntentFilter(ACTION_USB_PERMISSION);
                mContext.getApplicationContext().registerReceiver(mAoADevicePermissions, mPermissionFilter);
            }
            return true;
        }
    }

    private boolean isAnyAccessoryAttached() {
        UsbAccessory[] accessories = mUsbManager.getAccessoryList();

        if (accessories == null) {
            mAccessory = null;
            return false;
        }
        return true;
    }

    private boolean openAoAEndpoints(UsbAccessory accessory) {

        mAoAFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mAoAFileDescriptor == null || mAoAReadThread != null) {
            return false;
        }

        FileDescriptor fd = mAoAFileDescriptor.getFileDescriptor();

        if (fd != null) {
            mAoAInStream = new FileInputStream(fd);
            mAoAOutStream = new FileOutputStream(fd);
        } else {
            return false;
        }

        mAoAReadThread = new AoAReadThread(mAoAInStream);
        mAoAReadThread.start();
        return true;
    }

    private void killAoAEndpoints() {

        if (mAoAReadThread != null) {
            mAoAReadThread.stopRequest();
        }
        try {
            if(mAoAFileDescriptor != null) {
                mAoAFileDescriptor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(mAoAInStream != null) {
                mAoAInStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if(mAoAOutStream != null) {
                mAoAOutStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (mAoAReadThread != null) {
                //mAoAReadThread.setPriority(Thread.MIN_PRIORITY);
                mAoAReadThread.interrupt();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAoAFileDescriptor = null;
        mAoAInStream = null;
        mAoAOutStream = null;
        mAoAReadThread = null;
    }

    private void closeAoAEndpoints() {

        try {
            if (mAoAReadThread != null) {
                mAoAReadThread.stopRequest();
                try {
                    usb2usbPing(); //getReaderType();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                mAoAReadThread.join();
            }
            if(mAoAInStream != null) {
                mAoAInStream.close();
            }
            if(mAoAOutStream != null) {
                mAoAOutStream.close();
            }
            if(mAoAFileDescriptor != null) {
                mAoAFileDescriptor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mAoAInStream = null;
        mAoAOutStream = null;
        mAoAFileDescriptor = null;
        mAoAReadThread = null;
    }

    private boolean openAccessory() {
        boolean rt = false;
        UsbAccessory[] accessories = mUsbManager.getAccessoryList();

        if (accessories == null) {
            mAccessory = null;
            return false;
        }

        for (UsbAccessory accessory: accessories) {
            // Check if there is an appropriate accessory in the filter list:
            for (int j = 0; j < mAccessoriyFilterItems; j++) {
                if (accessory.toString().contains(mModelStrings.get(j))
                        && accessory.toString().contains(mManufacturerStrings.get(j))
                        && accessory.toString().contains(mVersionStrings.get(j))) {

                    rt = true;
                    break;
                }
            }
            if (rt) {
                mAccessory = accessory;
                break;
            }
        }

        if (mAccessory != null) {
            if (mUsbManager.hasPermission(mAccessory)) {
                if (!openAoAEndpoints(mAccessory)) {
                    mAccessory = null;
                    return false;
                }
                return true;
            } else {
                synchronized (mAoADevicePermissions) {
                    if (!mPermissionRequestPending) {
                        mPermissionRequestPending = true;
                        mUsbManager.requestPermission(mAccessory, mPendingIntent);
                    }
                }
            }
        }

        return rt;
    }

    private boolean tryOpenFtDevice() throws DlReaderException {
        int dev_cnt;
        D2xxManager.FtDeviceInfoListNode dev_infolist;
        dev_cnt = ftD2xx.createDeviceInfoList(mContext);

        for (int outer_cnt = 0; outer_cnt < dev_cnt; outer_cnt++) {

            dev_infolist = ftD2xx.getDeviceInfoListDetail(outer_cnt);
            if (dev_infolist.description == null) {
                continue;
            }
            for (int inner_cnt = 0; inner_cnt < mDeviceDescriptionStrings.size(); inner_cnt++) {

                if (dev_infolist.description.equals(mDeviceDescriptionStrings.get(inner_cnt))) {
                    ft_device = ftD2xx.openByIndex(mContext, outer_cnt);

                    if ((ft_device != null) && ft_device.isOpen()) {

                        try {
                            if (!ft_device.setLatencyTimer(ComParams.LATENCY_TIMER)) {
                                throw new LocalException();
                            }
                            if (!ft_device.setBitMode((byte) 0, ComParams.BIT_MODE)) {
                                throw new LocalException();
                            }
                            if (!ft_device.setBaudRate(ComParams.BAUD_RATE)) {
                                throw new LocalException();
                            }
                            if (!ft_device.setDataCharacteristics(ComParams.DATA_BITS, ComParams.STOP_BITS, ComParams.PARITY)) {
                                throw new LocalException();
                            }
                            if (!ft_device.setFlowControl(ComParams.FLOW_CONTROL, (byte) 0, (byte) 0)) {
                                throw new LocalException();
                            }
                            if (!ft_device.resetDevice()) {
                                throw new LocalException();
                            }
                        } catch (LocalException ex) {
                            ft_device.close();
                            throw new DlReaderException("Device closed due to a device setting failure.");
                        }

                        open_index = outer_cnt;
                        return true;
                    } else {
                         throw new DlReaderException("Can't open device.",Consts.DL_READER_DEVICE_COULD_NOT_BE_OPENED);
                    }
                }
            }
        }
        return false;
    }

    public synchronized void open() throws DlReaderException {

        if (open_index > -1)
             throw new DlReaderException("Device opened already.", Consts.DL_READER_DEVICE_ALREADY_OPEN);

        if (mSupportUsbHost) {
            // patch for LG G3 and similar Android devices:
            IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = mContext.registerReceiver(null, ifilter);
            int bat_status = batteryStatus.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);

            if (bat_status > 0) {
                if (tryOpenFtDevice()) {
                    try {
                        usb2usbOtgOff();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    ft_device.close();
                    android.os.Process.killProcess(android.os.Process.myPid()); // For most of these type of devices this is only solution.
                }
            }
        }

        if (mSupportAoA && isCharging) {
            if (openAccessory()) {
                open_index = 0;
                if (mAccessory.toString().contains("mModel=Android Accessory FT312D")) {
                    setAoAConfig(115200);
                } else if (mAccessory.toString().contains("mModel=FT312D-RS485")) {
                    setAoAConfig(250000);
                }
                return;
            }
        }

        if (mSupportUsbHost && !isCharging) {
            if (tryOpenFtDevice())
                return;
        }
        throw new DlReaderException("There is no D-Logic devices attached.",Consts.DL_READER_NO_DEVICE_ATTACHED);
//        if (!openAccessory()) {
//            throw new DlReaderException("There is no D-Logic devices attached.");
//        } else {
//            open_index = 0;
//            if (mAccessory.toString().contains("mModel=Android Accessory FT312D")) {
//                setAoAConfig();
//            }
//        }
    }

    public synchronized void readerReset() throws DlReaderException, InterruptedException {

        if (open_index < 0) {
            throw new DlReaderException("Device not opened.");
        }

        try {
            if (!ft_device.setRts()) {
                throw new LocalException();
            }
            Thread.sleep(100L);
            if (!ft_device.clrRts()) {
                throw new LocalException();
            }
        } catch(LocalException ex){
            ft_device.close();
            throw new DlReaderException("Can't reset device.");
        }
        Thread.sleep(1100L); // ReaderReset with bootloader
        // TODO: wait for BOOTLOADER character !
        // or 1100 ms
    }

    public synchronized int getReaderType() throws DlReaderException, InterruptedException {
        byte[] buffer = new byte[] {Consts.CMD_HEADER, Consts.GET_READER_TYPE, Consts.CMD_TRAILER, 0, (byte)0xAA, (byte)0xCC, 0};
        byte bytes_to_read;

        if (open_index < 0) {
            throw new DlReaderException("Device not opened.");
        }

        bytes_to_read = ComProtocol.initialHandshaking(buffer);
        buffer = ComProtocol.portRead(bytes_to_read);
        if (!ComProtocol.testChecksum(buffer, bytes_to_read))
            throw new DlReaderException("UFR_COMMUNICATION_ERROR");

        return (buffer[0] & 0xFF) | (buffer[1] & 0xFF) << 8 | (buffer[2] & 0xFF) << 16 | (buffer[3] & 0xFF) << 24;
    }

    public synchronized void readerUiSignal(byte lightSignalMode, byte beepSignalMode) throws DlReaderException, InterruptedException {
        byte[] buffer = new byte[] {Consts.CMD_HEADER, Consts.USER_INTERFACE_SIGNAL, Consts.CMD_TRAILER, 0, lightSignalMode, beepSignalMode, 0};

        if (open_index < 0) {
            throw new DlReaderException("Device not opened.");
        }
        ComProtocol.initialHandshaking(buffer);
    }

    public synchronized byte[] getCardIdEx(CardParams c_params) throws DlReaderException, InterruptedException {
        byte[] buffer = new byte[] {Consts.CMD_HEADER, Consts.GET_CARD_ID_EX, Consts.CMD_TRAILER, 0, (byte)0xAA, (byte)0xCC, 0 };
        byte[] tmp_buff;
        byte[] result;
        byte bytes_to_read;
        byte sak, uid_size;

        if (open_index < 0) {
            throw new DlReaderException("Device not opened.");
        }

        bytes_to_read = ComProtocol.initialHandshaking(buffer);
        sak = buffer[Consts.VAL0_INDEX];
        uid_size = buffer[Consts.VAL1_INDEX];

        tmp_buff = ComProtocol.portRead(bytes_to_read);

        if (!ComProtocol.testChecksum(tmp_buff, bytes_to_read))
            throw new DlReaderException("UFR_COMMUNICATION_ERROR");
        if (uid_size > 10)
            throw new DlReaderException("UFR_BUFFER_OVERFLOW");

        c_params.setSak(sak);
        c_params.setUidSize(uid_size);
        result = java.util.Arrays.copyOf(tmp_buff, uid_size);
        return result;
    }

    public synchronized void setISO14443_4_Mode() throws DlReaderException, InterruptedException {
        byte[] buffer = new byte[] { Consts.CMD_HEADER, Consts.SET_ISO14433_4_MODE, Consts.CMD_TRAILER, 0, (byte)0xAA, (byte)0xCC, 0 };

        if (open_index < 0) {
            throw new DlReaderException("Device not opened.");
        }
        ComProtocol.initialHandshaking(buffer);
    }

    public synchronized void s_block_deselect() throws DlReaderException, InterruptedException {
        byte[] buffer = new byte[] { Consts.CMD_HEADER, Consts.S_BLOCK_DESELECT, Consts.CMD_TRAILER, 0, 55, 55, 0 };

        if (open_index < 0) {
            throw new DlReaderException("Device not opened.");
        }
        ComProtocol.initialHandshaking(buffer);
    }

    public synchronized byte[] APDUPlainTransceive(byte[] c_apdu) throws DlReaderException, InterruptedException {
        byte[] cmd_intro = new byte[] { Consts.CMD_HEADER, Consts.APDU_TRANSCEIVE, Consts.CMD_TRAILER, 11, (byte)0xAA, (byte)0xCC, 0 };
        byte[] r_apdu;
        byte bytes_to_read;
        byte[] checksum = new byte[] {0};

        if (c_apdu.length > Consts.BUFFER_SIZE - 1) {
            throw new DlReaderException("UFR_BUFFER_OVERFLOW");
        }
        cmd_intro[Consts.EXT_SIZE_INDEX] = (byte)(c_apdu.length + 1);
        checksum[0] = (byte)(ComProtocol.getChecksumFragment((byte)0, c_apdu, (byte)c_apdu.length) + Consts.CHECKSUM_CONST);

        bytes_to_read = ComProtocol.initialHandshaking(cmd_intro);
        ComProtocol.portWrite(c_apdu, c_apdu.length);
        ComProtocol.portWrite(checksum, 1);
        ComProtocol.getAndTestResponse(cmd_intro, Consts.APDU_TRANSCEIVE);
        bytes_to_read = cmd_intro[Consts.RESPONSE_EXT_LENGTH_INDEX];

        r_apdu = ComProtocol.portRead(bytes_to_read - 1);
        checksum = ComProtocol.portRead(1);

        if ((0xFF & checksum[0]) != (0xFF & (ComProtocol.getChecksumFragment((byte)0, r_apdu, (byte)r_apdu.length) + Consts.CHECKSUM_CONST)))
            throw new DlReaderException("UFR_COMMUNICATION_ERROR");

        return r_apdu;
    }

    public synchronized byte[] blockRead(byte block_address, byte auth_mode, byte[] key) throws DlReaderException, InterruptedException {
        byte[] cmd_intro = new byte[] { Consts.CMD_HEADER, Consts.BLOCK_READ, Consts.CMD_TRAILER, 11, (byte)0xAA, (byte)0xCC, 0 };
        byte[] cmd_ext = new byte[11];

        if (open_index < 0) {
            throw new DlReaderException("Device not opened.");
        }

        cmd_intro[4] = auth_mode;
        cmd_ext[0] = block_address;

        if (!ComProtocol.testAuthMode(auth_mode))
            throw new DlReaderException("UFR_PARAMETERS_ERROR");

        java.lang.System.arraycopy(key, 0, cmd_ext, Consts.CMD_EXT_PROVIDED_KEY_INDEX, 6);
        return ComProtocol.commonBlockRead(cmd_intro, cmd_ext, (byte)17);
    }

    public synchronized void blockWrite(byte[] data, byte block_address, byte auth_mode, byte[] key) throws DlReaderException, InterruptedException {
        byte[] cmd_intro = new byte[] { Consts.CMD_HEADER, Consts.BLOCK_WRITE, Consts.CMD_TRAILER, 27, (byte)0xAA, (byte)0xCC, 0 };
        byte[] cmd_ext = new byte[10];

        if (open_index < 0) {
            throw new DlReaderException("Device not opened.");
        }

        if (!ComProtocol.testAuthMode(auth_mode))
            throw new DlReaderException("UFR_PARAMETERS_ERROR");

        cmd_intro[4] = auth_mode;
        cmd_ext[0] = block_address;
        java.lang.System.arraycopy(key, 0, cmd_ext, Consts.CMD_EXT_PROVIDED_KEY_INDEX, 6);

        ComProtocol.commonBlockWrite(data, cmd_intro, cmd_ext);
    }

    public synchronized void enterSleepMode() throws DlReaderException, InterruptedException {
        byte[] buffer = new byte[] {Consts.CMD_HEADER, Consts.ENTER_SLEEP_MODE, Consts.CMD_TRAILER, 0, (byte)0xAA, (byte)0xCC, 0};

        ComProtocol.initialHandshaking(buffer);
    }

    public synchronized void leaveSleepMode() throws DlReaderException, InterruptedException {
        byte[] buffer = new byte[] {Consts.CMD_HEADER, Consts.LEAVE_SLEEP_MODE, Consts.CMD_TRAILER, 0, (byte)0xAA, (byte)0xCC, 0};
        byte[] dummy = new byte[] {0};

        // First write one dummy byte to a port:
        ComProtocol.portWrite(dummy, 1);
        Thread.sleep(100L);
        ComProtocol.initialHandshaking(buffer);
    }

    public synchronized void usb2usbResetUfr() throws DlReaderException, InterruptedException {
        byte[] buffer = new byte[] {Consts.CMD_HEADER, Consts.USB2USB_FT232_RST, Consts.CMD_TRAILER, 0, (byte)0xAA, (byte)0xCC, 0};

        ComProtocol.initialHandshaking(buffer);
    }

    public synchronized void usb2usbOtgOff() throws DlReaderException, InterruptedException {
        byte[] buffer = new byte[] {Consts.CMD_HEADER, Consts.USB2USB_OTG_OFF, Consts.CMD_TRAILER, 0, (byte)0xAA, (byte)0xCC, 0};

        ComProtocol.initialHandshaking(buffer);
    }

    public synchronized void usb2usbPing() throws DlReaderException, InterruptedException {
        byte[] buffer = new byte[] {Consts.CMD_HEADER, Consts.USB2USB_PING, Consts.CMD_TRAILER, 0, (byte)0xAA, (byte)0xCC, 0};

        ComProtocol.initialHandshaking(buffer);
    }

    public synchronized boolean readerStillConnected() {

        if (open_index < 0)
            return false;

        if (ft_device != null) {
            return ft_device.isOpen();
        } else if (mAccessory != null) {
            return true;
        } else
            return false;
    }

    public synchronized void close() throws DlReaderException {

        if (open_index < 0)
            return;

        if (ft_device != null) {
            ft_device.close();
            ft_device = null;
        }
        if (mAccessory != null) {
            closeAoAEndpoints();
            mAccessory = null;
        }
        open_index = -1;
    }

    private static class ComProtocol {

        public static synchronized void erasePort() {

            if (ft_device != null) {
                ft_device.purge((byte) (D2xxManager.FT_PURGE_RX | D2xxManager.FT_PURGE_TX));
            } else if  (mAccessory != null) {
                try {
                    //mAoAOutStream.flush();
                    //mAoAInStream.reset();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public static synchronized void portWrite(byte[] buffer, int buffer_size) throws DlReaderException {
            if (ft_device != null) {
                if (ft_device.write(buffer, buffer_size, false) != buffer_size) {
                    if (ft_device.isOpen()) {
                        if (++retry_cnt >= Consts.MAX_COMMUNICATION_BREAK_RETRIES) {
                            retry_cnt = 0;
                            open_index = -1;
                            ft_device.close();
                            throw new DlReaderException("UFR DEVICE IS NOT CONNECTED", Consts.DL_READER_IS_NOT_CONNECTED);
                        } else {
                            throw new DlReaderException("UFR COMMUNICATION BREAK", Consts.DL_READER_COMMUNICATION_BREAK);
                        }
                    } else {
                        retry_cnt = 0;
                        open_index = -1;
                        throw new DlReaderException("UFR DEVICE IS NOT CONNECTED", Consts.DL_READER_IS_NOT_CONNECTED);
                    }
                }
                else {
                    retry_cnt = 0;
                }
            } else if (mAccessory != null) {
                try {
                    mAoAOutStream.write(buffer, 0, buffer_size);
                    mAoAOutStream.flush();
                } catch (IOException e) {
                    throw new DlReaderException("AOA_COMMUNICATION_BREAK", DlErrorCodes.AOA_WRITE_ERROR);
                }
            }
        }

        public static synchronized byte[] portRead(int buffer_size) throws DlReaderException
        {
            long time_start, time_end;
            byte[] buffer = new byte[buffer_size];
            java.util.Arrays.fill(buffer, (byte) 0);

            if (ft_device != null) {
                try {
                    if (ft_device.read(buffer, buffer_size, ComParams.READ_TIMEOUT) != buffer_size) {
                        throw new DlReaderException("UFR_COMMUNICATION_BREAK");
                    }
                }
                catch (NullPointerException e) {
                    throw new DlReaderException("UFR_COMMUNICATION_TERMINATED");
                }
            } else if (mAccessory != null) {
                try {
                    time_start = System.nanoTime() / 1000000;
                    while (AoAAvailable() < buffer_size) {
                        time_end = System.nanoTime() / 1000000;
                        if ((time_end - time_start) > ComParams.READ_TIMEOUT) {
                            throw new DlReaderException("UFR_COMMUNICATION_BREAK (Timeout)");
                        }
                    }
                    AoARead(buffer, buffer_size);
                } catch (IOException e) {
                    throw new DlReaderException("UFR_COMMUNICATION_BREAK");
                }
            }

            return buffer;
        }

        public static byte getChecksum_local(byte[] buffer, byte length)
        { // Ukoliko se ne bude koristila, spojiti sa CalcChecksum()
            short i;
            byte sum = buffer[0];

            for (i = 1; i < (length - 1); i++)
            {
                sum ^= buffer[i];
            }
            return (byte)(sum + Consts.CHECKSUM_CONST);
        }


        public static byte getChecksumFragment(byte previous_checksum, byte[] buffer, byte length)
        { // !without +7 at the end
            short i;

            for (i = 0; i < length; i++)
            {
                previous_checksum ^= buffer[i];
            }
            return previous_checksum;
        }

        public static void calcChecksum(byte[] buffer, byte length)
        {

            buffer[length - 1] = getChecksum_local(buffer, length);
        }

        public static boolean testChecksum(byte[] buffer, byte length)
        {
            short i;
            byte sum = buffer[0];

            for (i = 1; i < (length - 1); i++)
            {
                sum ^= buffer[i];
            }
            sum += Consts.CHECKSUM_CONST;
            return sum == buffer[length - 1];
        }

        public static boolean testAuthMode(byte auth_mode) {

            return (auth_mode == Consts.MIFARE_AUTHENT1A) || (auth_mode == Consts.MIFARE_AUTHENT1B);
        }

        public static byte initialHandshaking(byte[] data) throws InterruptedException, DlReaderException
        {
            byte command = data[1];
            byte[] rcv_data;

            erasePort();
            Thread.sleep(10L);
            calcChecksum(data, Consts.INTRO_SIZE);
            portWrite(data, Consts.INTRO_SIZE);
            rcv_data = portRead(Consts.INTRO_SIZE);
            if (!testChecksum(rcv_data, Consts.INTRO_SIZE))
                throw new DlReaderException("UFR_COMMUNICATION_ERROR");
            if ((rcv_data[0] == Consts.ERR_HEADER) && (rcv_data[2] == Consts.ERR_TRAILER))
                throw new DlReaderException("Reader error code: " + rcv_data[1], rcv_data[1] & 0xFF);

            if ((rcv_data[1] != command)
                    || (((rcv_data[0] != Consts.RESPONSE_HEADER) || (rcv_data[2] != Consts.RESPONSE_TRAILER))
                    && ((rcv_data[0] != Consts.ACK_HEADER) || (rcv_data[2] != Consts.ACK_TRAILER))))
                throw new DlReaderException("UFR_COMMUNICATION_ERROR");

            java.lang.System.arraycopy(rcv_data, 0, data, 0, 7);
            return data[3];
        }

        public static void getAndTestResponse(byte[] cmd_intro, byte command) throws DlReaderException
        {
            java.lang.System.arraycopy(portRead(Consts.INTRO_SIZE), 0, cmd_intro, 0, Consts.INTRO_SIZE);

            if (!testChecksum(cmd_intro, Consts.INTRO_SIZE))
                throw new DlReaderException("UFR_COMMUNICATION_ERROR");
            if ((cmd_intro[0] == Consts.ERR_HEADER) || (cmd_intro[2] == Consts.ERR_TRAILER))
                throw new DlReaderException("Reader error code: " + cmd_intro[1], cmd_intro[1] & 0xFF);
            if ((cmd_intro[0] != Consts.RESPONSE_HEADER) || (cmd_intro[2] != Consts.RESPONSE_TRAILER)
                    || (cmd_intro[1] != command))
                throw new DlReaderException("UFR_COMMUNICATION_ERROR");
        }

        public static byte[] commonBlockRead(byte[] cmd_intro, byte[] cmd_ext, byte response_ext_length) throws InterruptedException, DlReaderException
        {
            byte command = cmd_intro[Consts.INTRO_CMD_INDEX];
            byte cmd_ext_length = cmd_intro[Consts.CMD_EXT_LENGTH_INDEX];
            byte bytes_to_read;
            byte[] checksum;
            byte[] data;

            bytes_to_read = initialHandshaking(cmd_intro);

            calcChecksum(cmd_ext, cmd_ext_length);
            portWrite(cmd_ext, cmd_ext_length);

            getAndTestResponse(cmd_intro, command);

            if (cmd_intro[Consts.RESPONSE_EXT_LENGTH_INDEX] != response_ext_length)
                throw new DlReaderException("UFR_COMMUNICATION_ERROR");

            data = portRead(response_ext_length - 1);
            checksum = portRead(1);
            if ((0xFF & checksum[0]) != (0xFF & (getChecksumFragment((byte)0, data, (byte)(response_ext_length - 1)) + Consts.CHECKSUM_CONST)))
                throw new DlReaderException("UFR_COMMUNICATION_ERROR");

            return data;
        }

        public static void commonBlockWrite(byte[] data, byte[] cmd_intro, byte[] cmd_ext) throws InterruptedException, DlReaderException
        {
            byte command = cmd_intro[Consts.INTRO_CMD_INDEX];
            byte cmd_ext_length = cmd_intro[Consts.CMD_EXT_LENGTH_INDEX];
            byte[] checksum = new byte[] {0};

            initialHandshaking(cmd_intro);
            checksum[0] = getChecksumFragment((byte)0, cmd_ext, (byte) (cmd_ext_length - (data.length + 1)));
            checksum[0] = (byte)(getChecksumFragment(checksum[0], data, (byte) data.length) + Consts.CHECKSUM_CONST);

            portWrite(cmd_ext, (cmd_ext_length - (data.length + 1)));
            portWrite(data, data.length);
            portWrite(checksum, 1);
            getAndTestResponse(cmd_intro, command);
        }
    }

    private void setAoAConfig(int rs232_baud_rate) throws DlReaderException {
        byte[] configdata = new byte[8];

        /*prepare the baud rate buffer*/
        configdata[0] = (byte)rs232_baud_rate;
        configdata[1] = (byte)(rs232_baud_rate >> 8);
        configdata[2] = (byte)(rs232_baud_rate >> 16);
        configdata[3] = (byte)(rs232_baud_rate >> 24);

        /*data bits*/
        configdata[4] = ComParams.DATA_BITS;
        /*stop bits*/
        configdata[5] = 1;
        /*parity*/
        configdata[6] = 0;
        /*flow control*/
        configdata[7] = 0;

        /*send the UART configuration packet*/
        try {
            ComProtocol.portWrite(configdata, (int) 8);
        } catch (DlReaderException e) {
            if (e.getErrCode() == DlErrorCodes.AOA_WRITE_ERROR) {
                closeAoAEndpoints();
                open_index = -1;
                mAccessory = null;
            }
            throw e;
        }
    }

    private static class ComParams {
        public static final byte LATENCY_TIMER = 2;
        public static final int BAUD_RATE = 1000000;
        public static final long READ_TIMEOUT = 2500;
        public static final long WRITE_TIMEOUT = 1000;
        public static final byte BIT_MODE = D2xxManager.FT_BITMODE_RESET;
        public static final byte DATA_BITS = D2xxManager.FT_DATA_BITS_8;
        public static final short FLOW_CONTROL = D2xxManager.FT_FLOW_NONE;
        public static final byte PARITY = D2xxManager.FT_PARITY_NONE;
        public static final byte STOP_BITS = D2xxManager.FT_STOP_BITS_1;
    }

    public static class Consts {

        // Lengths and ranges:
        public static final int BUFFER_LOCAL_SIZE = 256;
        public static final int BUFFER_SIZE = 192;
        public static final byte INTRO_SIZE = 7;
        public static final int EXT_SIZE_INDEX = 3;
        public static final int PARAM0_INDEX = 4;
        public static final int PARAM1_INDEX = 5;
        public static final int VAL0_INDEX = 4;
        public static final int VAL1_INDEX = 5;
        public static final int INTRO_CMD_INDEX = 1;
        public static final int CMD_EXT_LENGTH_INDEX = 3;
        public static final int CMD_PARAM0_INDEX = 4;
        public static final int CMD_EXT_PROVIDED_KEY_INDEX = 4;
        public static final int RESPONSE_EXT_LENGTH_INDEX = 3;
        public static final int RESPONSE_VAL0_INDEX = 4;

        // Protocol consts:
        public static final byte CMD_HEADER = 0x55;
        public static final byte CMD_TRAILER = (byte)0xAA;
        public static final byte ACK_HEADER = (byte)0xAC;
        public static final byte ACK_TRAILER = (byte)0xCA;
        public static final byte RESPONSE_HEADER = (byte)0xDE;
        public static final byte RESPONSE_TRAILER = (byte)0xED;
        public static final byte ERR_HEADER = (byte)0xEC;
        public static final byte ERR_TRAILER = (byte)0xCE;
        public static final byte CHECKSUM_CONST = (byte)7;

        // Auth consts:
        public static final byte RKA_AUTH1A = 0x00;     // reder keys addressing mode, authentication using key A
        public static final byte RKA_AUTH1B = 0x01;     // reder keys addressing mode, authentication using key B
        public static final byte AKM1_AUTH1A = 0x20;    // auto keys, searching mode 1, authentication using key A
        public static final byte AKM1_AUTH1B = 0x21;    // auto keys, searching mode 1, authentication using key B
        public static final byte AKM2_AUTH1A = 0x40;    // auto keys, searching mode 2, authentication using key A
        public static final byte AKM2_AUTH1B = 0x41;    // auto keys, searching mode 2, authentication using key B
        public static final byte PK_AUTH1A = 0x60;      // provided keys, authentication using key A
        public static final byte PK_AUTH1B = 0x61;      // provided keys, authentication using key B

        public static final byte MIFARE_AUTHENT1A = 0x60;
        public static final byte MIFARE_AUTHENT1B = 0x61;

        // uFR Commands:
        public static final byte GET_READER_TYPE = 0x10;
        public static final byte GET_READER_SERIAL = 0x11;
        public static final byte GET_HARDWARE_VERSION = 0x2A;
        public static final byte GET_FIRMWARE_VERSION = 0x29;
        public static final byte GET_CARD_ID = 0x13;
        public static final byte GET_CARD_ID_EX = 0x2C;
        public static final byte BLOCK_READ = 0x16;
        public static final byte BLOCK_WRITE = 0x17;
        public static final byte SOFT_RESTART = 0x30;
        public static final byte USER_INTERFACE_SIGNAL = 0x26;
        public static final byte ENTER_SLEEP_MODE = 0x46;
        public static final byte LEAVE_SLEEP_MODE = 0x47;

        public static final byte S_BLOCK_DESELECT = (byte)0x92;
        public static final byte SET_ISO14433_4_MODE = (byte)0x93;
        public static final byte APDU_TRANSCEIVE = (byte)0x94;

        public static final byte USB2USB_PING = (byte)0xF0;
        public static final byte USB2USB_FT232_RST = (byte)0xF1;
        public static final byte USB2USB_OTG_OFF = (byte)0xF2;

        // uFR Status:
        public static final int DL_READER_COMMUNICATION_BREAK = 0x50;
        public static final int DL_READER_IS_NOT_CONNECTED = 0x104;
        public static final int DL_READER_GENERAL_EXCEPTION = 1000;
		
		public static final int DL_READER_NO_DEVICE_ATTACHED = 1001;
        public static final int DL_READER_DEVICE_COULD_NOT_BE_OPENED = 1002;
        public static final int DL_READER_DEVICE_ALREADY_OPEN = 1003;

        public static final int MAX_COMMUNICATION_BREAK_RETRIES = 2;
    }

    public class DlErrorCodes {
        private static final int AOA_WRITE_ERROR = 0x8001;
    }

    public static class DlReaderException extends IOException {
        private static final long serialVersionUID = 1L;
        public int err_code;

        public DlReaderException() {
        }

        public DlReaderException(String ftStatusMsg) {
            super(ftStatusMsg);
            err_code = Consts.DL_READER_GENERAL_EXCEPTION;
        }

        public DlReaderException(String ftStatusMsg, int p_err_code) {
            super(ftStatusMsg);
            err_code = p_err_code;
        }

        public int getErrCode() {

            return err_code;
        }
    }

    private static class LocalException extends IOException {
        private static final long serialVersionUID = 1L;

        public LocalException() {
        }

        public LocalException(String ftStatusMsg) {
            super(ftStatusMsg);
        }
    }

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

    // AoA available:
    static private int AoAAvailable() {

        return totalBytes;
    }

    // AoA read:
    // Returns bytes actually read:
    static private int AoARead(byte[] buffer, int byteCount)
    {

		// Should be at least one byte to read:
        if((byteCount < 1) || (totalBytes == 0)){
            return 0;
        }

		/* Check for max limit*/
        if(byteCount > totalBytes) {
            byteCount = totalBytes;
        }

		// Copy to the user buffer:
        for(int i = 0; i < byteCount; i++)
        {
            buffer[i] = AoAReadBuffer[readIndex];
            readIndex++;
			/*shouldnt read more than what is there in the buffer,
			 * 	so no need to check the overflow
			 */
            readIndex %= MAX_BYTES;
        }

        // Update the number of bytes available:
        totalBytes -= byteCount;

        return byteCount;
    }

    // AoA usb input data handler:
    private class AoAReadThread extends Thread {

        FileInputStream instream;
        boolean stop_thread = false;

        private int readcount;

        private byte [] usbdata = new byte[CHUNK_BYTES];

        AoAReadThread(FileInputStream stream ) {

            instream = stream;

            this.setPriority(Thread.MAX_PRIORITY);
        }

        public synchronized void stopRequest() {

            stop_thread = true;
        }

        public void run() {

            while(!stop_thread) {
                while (totalBytes > (MAX_BYTES - CHUNK_BYTES)) {
                    try {
                        Thread.sleep(32);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    if (instream != null) {
                        readcount = instream.read(usbdata);

                        if (readcount > 0) {

                            // MAX_BYTES should be power(2, n), where n is a element of N
                            for(int i = 0; i < readcount; i++) {

                                AoAReadBuffer[writeIndex] = usbdata[i];
                                ++writeIndex;
                                writeIndex %= MAX_BYTES;
                            }

                            if (writeIndex >= readIndex) {
                                totalBytes = writeIndex - readIndex;
                            } else {
                                totalBytes = (MAX_BYTES - readIndex) + writeIndex;
                            }
                        }
                    }
                } catch (IOException e) {
                    stop_thread = true;
                    if (instream != null) {
                        try {
                            instream.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    e.printStackTrace();
                }
            }
        }
    }
}
