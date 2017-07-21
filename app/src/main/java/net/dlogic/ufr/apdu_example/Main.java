package net.dlogic.ufr.apdu_example;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import net.dlogic.ufr.lib.DlReader;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by d-logic on 15.5.2015..
 */

public class Main extends Activity {
    static Context context;
    static DlReader device;
    static Button btnReaderType;
    static Button btnUiSignal;
    static Button btnSelect;
    static Button btnDeselect;
    static Button btnApduTransceive;
    static EditText ebDeviceType;
    static EditText ebCAPDU;
    static EditText ebRAPDU;
    static Spinner spnLightMode;
    static Spinner spnBeepMode;
    static Spinner spnCAPDUPicker;
    static int lightMode = 0;
    static int beepMode = 0;
    static IncomingHandler handler = new IncomingHandler();
    static Resources res;
    static ConcurrentLinkedQueue<Task> mCommandQueue = new ConcurrentLinkedQueue<Task>();
    ReaderThread mReaderThread;

    static final String[] mC_APDUs = new String[]{
            "00A4040007D276000085010100", // <item>NFC NDEF tag app select</item>
            "00A4000C02E103",             // <item>NFC CC select</item>
            "00A4000C020001",             // <item>NFC NDEF file select</item>
            "00A4000C02E101",             // <item>ST M24SRxx sys file select</item>
            "00B0000002"                  // <item>Read Binary</item> !!! offset = 0, read 2 bytes.
    };

    @Override
    protected void onResume() {
        super.onResume();
        mCommandQueue.add(new Task(Consts.TASK_CONNECT));
    }

    @Override
    protected void onPause() {
        mCommandQueue.add(new Task(Consts.TASK_DISCONNECT));
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        context = this;
        try {
            device = DlReader.getInstance(context, R.xml.accessory_filter, R.xml.dev_desc_filter);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mReaderThread = new ReaderThread();
        mReaderThread.start();

        // Get arrays from resources:
        res = getResources();

        // Get references to UI widgets:
        ebDeviceType = (EditText) findViewById(R.id.ebDeviceType);
        ebDeviceType.setInputType(0);
        ebCAPDU = (EditText) findViewById(R.id.ebCAPDU);
        ebRAPDU = (EditText) findViewById(R.id.ebRAPDU);

        btnReaderType = (Button) findViewById(R.id.btnDeviceType);
        btnUiSignal = (Button) findViewById(R.id.btnUiSignal);
        btnSelect = (Button) findViewById(R.id.btnSelect);
        btnDeselect = (Button) findViewById(R.id.btnDeselect);
        btnApduTransceive = (Button) findViewById(R.id.btnApduTransceive);

        spnLightMode = (Spinner) findViewById(R.id.spnLightMode);
        ArrayAdapter<CharSequence> spnLightAdapter = ArrayAdapter.createFromResource(context,
                R.array.light_signal_modes,
                R.layout.dl_spinner_textview);
        spnLightAdapter.setDropDownViewResource(R.layout.dl_spinner_textview);
        spnLightMode.setAdapter(spnLightAdapter);
        spnLightMode.setSelection(0);
        spnLightMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                lightMode = pos;
            }

            public void onNothingSelected(AdapterView<?> parent) { }
        });

        spnBeepMode = (Spinner) findViewById(R.id.spnBeepMode);
        ArrayAdapter<CharSequence> spnBeepAdapter = ArrayAdapter.createFromResource(context,
                R.array.beep_signal_modes,
                R.layout.dl_spinner_textview);
        spnBeepAdapter.setDropDownViewResource(R.layout.dl_spinner_textview);
        spnBeepMode.setAdapter(spnBeepAdapter);
        spnBeepMode.setSelection(0);
        spnBeepMode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                beepMode = pos;
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spnCAPDUPicker =  (Spinner) findViewById(R.id.spnCAPDUPicker);
        ArrayAdapter<CharSequence> spnCAPDUPickerAdapter = ArrayAdapter.createFromResource(context,
                R.array.capdu_picker,
                R.layout.dl_spinner_textview);
        spnCAPDUPickerAdapter.setDropDownViewResource(R.layout.dl_spinner_textview);
        spnCAPDUPicker.setAdapter(spnCAPDUPickerAdapter);
        spnCAPDUPicker.setSelection(0);
        spnCAPDUPicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                ebCAPDU.setText(mC_APDUs[pos]);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnReaderType.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (device.readerStillConnected()) {
                    try {
                        mCommandQueue.add(new Task(Consts.TASK_GET_READER_TYPE));
                    } catch (Exception e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Device not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnUiSignal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (device.readerStillConnected()) {
                    mCommandQueue.add(new Task(Consts.TASK_EMIT_UI_SIGNAL, (byte)lightMode, (byte)beepMode));
                } else {
                    Toast.makeText(context, "Device not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (device.readerStillConnected()) {
                    try {
                        mCommandQueue.add(new Task(Consts.TASK_SELECT_ISO14443_4A));
                    } catch (Exception e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Device not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnDeselect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (device.readerStillConnected()) {
                    try {
                        mCommandQueue.add(new Task(Consts.TASK_DESELECT_ISO14443_4A));
                    } catch (Exception e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Device not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnApduTransceive.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (device.readerStillConnected()) {
                    try {
                        mCommandQueue.add(new Task(Consts.TASK_APDU_PLAIN, (byte)0, (byte)0, getCAPDU()));
                    } catch (Exception e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Device not connected.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private static byte[] getCAPDU() throws Exception {
        String dataHexStr = ebCAPDU.getText().toString();

        if ((dataHexStr.length() % 2) != 0) {
            throw new Exception("Wrong CAPDU format");
        }
        if (!dataHexStr.matches("[0-9A-Fa-f]+")) {
            throw new Exception("Wrong CAPDU format");
        }

        byte[] ret_val = new byte[dataHexStr.length() / 2];

        for (int i = 0; i < dataHexStr.length(); i += 2) {
            ret_val[i / 2] = (byte) ((Character.digit(dataHexStr.charAt(i), 16) << 4)
                    + Character.digit(dataHexStr.charAt(i+1), 16));
        }
        if (ret_val.length < 4) {
            throw new Exception("Wrong CAPDU format");
        } else if ((ret_val.length > 5) && (ret_val[4] != (byte)(ret_val.length - 5)) && (ret_val[4] != (byte)(ret_val.length - 6))) {
            throw new Exception("Wrong CAPDU format");
        }
        return ret_val;
    }

    static class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Consts.RESPONSE_CONNECTED:
                    //Toast.makeText(context, "Device successfully connected.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_READER_TYPE:
                    ebDeviceType.setText(Integer.toHexString(msg.arg1));
                    Toast.makeText(context, "Device type obtained successfully.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_APDU_PLAIN:
                    ebRAPDU.setText(Tools.byteArr2Str((byte[]) msg.obj));
                    Toast.makeText(context, "APDU successfully transceived.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_SUCESS:
                    Toast.makeText(context, "Operation completed successfully.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_DISCONNECTED:
                    ebDeviceType.setText("");
                    //ebCAPDU.setText("");
                    ebRAPDU.setText("");

                    //Toast.makeText(context, "Device successfully disconnected.", Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_ERROR:
                    Toast.makeText(context, (String)msg.obj, Toast.LENGTH_SHORT).show();
                    break;

                case Consts.RESPONSE_ERROR_QUIETLY:
                    break;

                default:
                    Toast.makeText(context, "Unknown response.", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    private class Consts {
        public static final int TASK_CONNECT = 1;
        public static final int TASK_GET_READER_TYPE = 2;

        public static final int TASK_DISCONNECT = 5;
        public static final int TASK_EMIT_UI_SIGNAL = 6;

        public static final int TASK_SELECT_ISO14443_4A = 10;
        public static final int TASK_DESELECT_ISO14443_4A = 11;
        public static final int TASK_APDU_PLAIN = 12;

        public static final int RESPONSE_CONNECTED = 100;
        public static final int RESPONSE_READER_TYPE = 101;

        public static final int RESPONSE_DISCONNECTED = 104;
        public static final int RESPONSE_SUCESS = 105;

        public static final int RESPONSE_APDU_PLAIN = 110;

        public static final int RESPONSE_ERROR = 400;
        public static final int RESPONSE_ERROR_QUIETLY = 401;

    }

    class ReaderThread extends Thread {
        private byte[]data;
        private boolean stop_thread = false;
        private boolean connected = false;
        private boolean iso14443_4a_selected = false;
        DlReader.CardParams c_params = new DlReader.CardParams();

        public synchronized void stopRequest() {

            stop_thread = true;
        }

        @Override
        public void run() {
            Task local_task;

            while (!stop_thread) {
                local_task = mCommandQueue.poll();
                if (local_task != null) {

                    switch (local_task.taskCode) {
                        case Consts.TASK_CONNECT:
                            Task peek_task = mCommandQueue.peek();
                            boolean next_task_is_not_disconnect = peek_task == null;
                            if (!next_task_is_not_disconnect) {
                                next_task_is_not_disconnect = peek_task.taskCode != Consts.TASK_DISCONNECT;
                            }
                            while (!device.readerStillConnected() && next_task_is_not_disconnect) {
                                try {
                                    device.open();
                                    connected = true;
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_CONNECTED));
                                } catch (Exception e) {
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR_QUIETLY, e.getMessage()));
                                    try {
                                        Thread.sleep(500);
                                    } catch (InterruptedException ie) {
                                        ie.printStackTrace();
                                    }
                                }
                            }
                            break;

                        case Consts.TASK_DISCONNECT:
                            try {
                                device.close();
                                connected = false;
                                handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_DISCONNECTED));
                            } catch (Exception e) {
                                handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                            }
                            break;

                        case Consts.TASK_GET_READER_TYPE:
                            if (connected) {
                                try {
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_READER_TYPE, device.getReaderType(), 0));
                                } catch (Exception e) {
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                                }
                            }
                            break;

                        case Consts.TASK_SELECT_ISO14443_4A:
                            try {
                                if (!iso14443_4a_selected)
                                    device.setISO14443_4_Mode();
                                else {
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, "ISO14443-4A tag selected already"));
                                    break;
                                }
                                iso14443_4a_selected = true;
                                handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_SUCESS));
                            } catch (Exception e) {
                                handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                            }
                            break;

                        case Consts.TASK_DESELECT_ISO14443_4A:
                            try {
                                if (iso14443_4a_selected) {
                                    iso14443_4a_selected = false;
                                    device.s_block_deselect();
                                } else {
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, "No ISO14443-4A tag has been selected"));
                                    break;
                                }
                                handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_SUCESS));
                            } catch (Exception e) {
                                handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                            }
                            break;

                        case Consts.TASK_APDU_PLAIN:
                            if (connected) {
                                if (iso14443_4a_selected) {
                                    try {
                                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_APDU_PLAIN, device.APDUPlainTransceive(local_task.byte_arr_param1)));
                                    } catch (Exception e) {
                                        handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                                    }
                                } else
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, "No ISO14443-4A tag has been selected"));
                            }
                            break;

                        case Consts.TASK_EMIT_UI_SIGNAL:
                            if (connected) {
                                try {
                                    device.readerUiSignal(local_task.byte_param1/*lightMode*/, local_task.byte_param2/*beepMode*/);
                                } catch (Exception e) {
                                    handler.sendMessage(handler.obtainMessage(Consts.RESPONSE_ERROR, e.getMessage()));
                                }
                            }
                            break;

                        default:
                            break;
                    }
                }
            }
        }
    }

    class Task {
        int taskCode;
        byte byte_param1, byte_param2;
        byte[] byte_arr_param1;

        public Task(int code) {
            taskCode = code;
        }
        public Task(int code, byte p1, byte p2) {
            taskCode = code;
            byte_param1 = p1;
            byte_param2 = p2;
        }
        public Task(int code, byte p1, byte p2, byte[] pa1) {
            taskCode = code;
            byte_param1 = p1;
            byte_param2 = p2;
            byte_arr_param1 = pa1;
        }
    }

    static class Tools {

        public static boolean isNumeric(String s){
            if(TextUtils.isEmpty(s)){
                return false;
            }
            Pattern p = Pattern.compile("[-+]?[0-9]*");
            Matcher m = p.matcher(s);
            return m.matches();
        }

        public static String byteArr2Str(byte[] byteArray) {
            StringBuilder sBuilder = new StringBuilder(byteArray.length * 2);
            for(byte b: byteArray)
                sBuilder.append(String.format("%02X ", b & 0xff));
            return sBuilder.toString();
        }
    }
}