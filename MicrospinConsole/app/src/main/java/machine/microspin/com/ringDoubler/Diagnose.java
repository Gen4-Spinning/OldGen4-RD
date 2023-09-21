package machine.microspin.com.ringDoubler;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.text.TextUtils;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothWriter;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import machine.microspin.com.ringDoubler.entity.IntegerInputFilter;
import machine.microspin.com.ringDoubler.entity.Packet;
import machine.microspin.com.ringDoubler.entity.Pattern;
import machine.microspin.com.ringDoubler.entity.Settings;
import machine.microspin.com.ringDoubler.entity.TLV;
import machine.microspin.com.ringDoubler.entity.Utility;

public class Diagnose extends AppCompatActivity implements View.OnClickListener, BluetoothService.OnBluetoothEventCallback,AdapterView.OnItemSelectedListener,View.OnFocusChangeListener {

    // ITEMS SHOWN ON NORMAL MENU
    private Spinner testType;
    private Spinner motorCode;
    private EditText runTime;
    private EditText signalValue;
    private EditTextCustom targetRPMPercent;
    private TextView motorCodeLive;
    private TextView maxRpmText;
    private TextView targetRPMOut;

    private TextView signalValueLive;
    private TextView actualRPMLive;
    private TextView testTypeLive;
	private TextView targetTextLive;
    private TextView targetLabelLive;

    private Spinner directionTypespinner;
    private EditText distanceMenu;
    private Button runDiagnose;
    private LinearLayout menuLayout;
    private LinearLayout liveLayout;
    private LinearLayout normalMenu;
    private LinearLayout liftMenu;
    private LinearLayout maxMotorRpmLayout;

    private LinearLayout livenormal;
    private LinearLayout livelift;
    private TextView liveliftDone;
    private ProgressBar liveliftprogress;


    private static Boolean isDiagnoseRunning = false;

    //harsha added
    private Snackbar snackbarComplete ;
    private int iCurrentSelection = 0;
    private int maxRPM = 0;
    private int actualRPM = 0;
    private int targetRpmCalc = 0;
    private int targetSignalVoltage = 0;

    private static boolean firstInit = false;
    private static boolean isSnackbarOn = false;

    //=================== STATIC Codes ========================
    final private static String SPINNER_TEST_TYPE = "TEST_TYPE";
    final private static String SPINNER_MOTOR_CODE = "MOTOR_TYPE";
    final private static String SPINNER_DIRECTION_CODE = "DIRECTIONS";

    final private static String LAYOUT_MENU = "MENU";
    final private static String LAYOUT_LIVE = "LIVE";

    final private static String NORMAL_MENU = "NORMAL";
    final private static String LIFT_MENU = "LIFT" ;
    private boolean LiftMode = false;

    private BluetoothService mService;
    private BluetoothWriter mWriter;
    private static final String TAG = "Diagnose";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnose);

        //******* Release Change v2
        if(Settings.device != null) {
            setTitle(Settings.device.getName());
        }

        testType = (Spinner) findViewById(R.id.testType);
        testType.setOnItemSelectedListener(this);

        motorCode = (Spinner) findViewById(R.id.motorValue);
        motorCode.setOnItemSelectedListener(this);

        runTime = (EditText) findViewById(R.id.testRunTime);
        signalValue = (EditText) findViewById(R.id.signalValue);
        targetRPMPercent = (EditTextCustom) findViewById(R.id.targetRpmPercent);
        targetRPMPercent.setOnFocusChangeListener(this);

        maxRpmText = (TextView) findViewById(R.id.maxRpmVal);
        targetRPMOut =  (TextView) findViewById(R.id.targetRPMout);

        //********************************************//

        //for the other lift menu
        directionTypespinner = (Spinner) findViewById(R.id.directionSpinner);
        distanceMenu = (EditText) findViewById(R.id.distanceEnter);

        //************************************************//
       // live menu normal
        testTypeLive = (TextView) findViewById(R.id.typeOfTestLive);
        motorCodeLive = (TextView) findViewById(R.id.motorCodeLive);
        //runTimeLive = (TextView) findViewById(R.id.testTimeLive);
        targetTextLive = (TextView) findViewById(R.id.targetText);
        targetLabelLive = (TextView) findViewById(R.id.targetlabel);
        signalValueLive = (TextView) findViewById(R.id.signalVoltgaeLive);
        actualRPMLive = (TextView) findViewById(R.id.actualRPMLive);

        //*****************************************************//
        //live lift
        liveliftDone = (TextView) findViewById(R.id.liftDone);
        liveliftprogress = (ProgressBar) findViewById(R.id.liveliftprogressBar);


        //*************************************************//
        // other things
        runDiagnose = (Button) findViewById(R.id.runDiagnose);
        runDiagnose.setOnClickListener(this);

        //main menu and two types of options
        menuLayout = (LinearLayout) findViewById(R.id.diagnoseMenu);
        normalMenu = (LinearLayout) findViewById((R.id.NormalSelect));
        liftMenu = (LinearLayout) findViewById((R.id.LiftSelect));
        maxMotorRpmLayout = (LinearLayout) findViewById((R.id.maxMotorLayout));
        //main live menu andtwo types of options
        liveLayout = (LinearLayout) findViewById(R.id.diagnoseLive);
        livenormal = (LinearLayout) findViewById(R.id.normalLive);
        livelift = (LinearLayout) findViewById(R.id.liftLive);



        List<String> motorValueList = getValueListForSpinner(SPINNER_MOTOR_CODE);
        motorCode.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, motorValueList));
        List<String> testTypeList = getValueListForSpinner(SPINNER_TEST_TYPE);
        testType.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, testTypeList));
        List<String> directionTypeList = getValueListForSpinner(SPINNER_DIRECTION_CODE);
        directionTypespinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, directionTypeList));


        mService = BluetoothService.getDefaultInstance();
        mWriter = new BluetoothWriter(mService);

        setDefaultValue();
        toggleViewOn(LAYOUT_MENU);
        toggleViewOn(NORMAL_MENU);

        maxMotorRpmLayout.setVisibility(View.VISIBLE);

        mWriter.writeln(Pattern.DISABLE_MACHINE_START_DIAGNOSE.toUpperCase());

    }

    //====================================== OTHER EVENTS ==========================================
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.runDiagnose:
                runDiagnose();
                break;
        }
    }


    //Spinner TestType Events
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        if (firstInit == false) {
            firstInit = true;
            return;
        }
        else {
            if (parent.getId() == R.id.testType){
                if (pos == 0) {
                    signalValue.setEnabled(true);
                    targetRPMPercent.setEnabled(false);
                    targetRPMPercent.setText("0");
                    targetRPMOut.setText("0");
                } else {
                    signalValue.setEnabled(false);
                    signalValue.setText("0");
                    targetRPMPercent.setEnabled(true);
                }
            }

            if (parent.getId() == R.id.motorValue){
                String motorSelectedType = Utility.formatStringCode(motorCode.getSelectedItem().toString());
                if ((motorSelectedType.equals(Pattern.MotorTypes.LIFT_LEFT.toString())) || (motorSelectedType.equals(Pattern.MotorTypes.LIFT_RIGHT.toString()))) {
                    LiftMode = true;
                    toggleViewOn(LIFT_MENU);
                    testType.setEnabled(false);
                    maxMotorRpmLayout.setVisibility(View.GONE);
                } else {
                    LiftMode = false;
                    toggleViewOn(NORMAL_MENU);
                    testType.setEnabled(true);
                    maxMotorRpmLayout.setVisibility(View.VISIBLE);
                    maxRPM = GetMaxRPM(motorSelectedType);
                    maxRpmText.setText(Utility.formatString(Integer.toString(maxRPM)));
                }
            }
        }
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    private int GetMaxRPM(String motorSelected)
    {   int maxRpm1 = 0;
        /*if (motorSelected.equals(Pattern.MotorTypes.SPINDLE.toString())){
            maxRpm1 = 3000;
        }
        else {
            maxRpm1 = 1500;
        }*/
        maxRpm1 = 1500;
        return maxRpm1;
    }

    private void UpdateEnabledTestType(){
        if (LiftMode == false) {
            String testTypeSelectedType = Utility.formatStringCode((testType.getSelectedItem().toString()));
            if (testTypeSelectedType.equals(Pattern.DiagnosticTestTypes.OPEN_LOOP.toString())) {
                signalValue.setEnabled(true);
                targetRPMPercent.setEnabled(false);
            }
            if (testTypeSelectedType.equals(Pattern.DiagnosticTestTypes.CLOSED_LOOP.toString())) {
                signalValue.setEnabled(false);
                targetRPMPercent.setEnabled(true);
            }
        }
    }

    //==================================== CUSTOM FUNCTIONS ========================================
    private void runDiagnose() {
        Packet diagnosePacket = new Packet(Packet.OUTGOING_PACKET);
        TLV[] attributes = new TLV[5];  //Specified in the requirements
        String attrType;
        String attrValue;
        String attrLength;

        //Kind of Test and motor type are taken for both types of tests, normal or lift type.
        //****** Handling=> testType Attribute
        attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.KIND_OF_TEST.name());
        attrLength = Pattern.ATTR_LENGTH_02;
        String testTypeSelected = Utility.formatStringCode(testType.getSelectedItem().toString());
        attrValue = Pattern.diagnoseTestTypesMap.get(testTypeSelected);
        TLV testType = new TLV(attrType, attrLength, attrValue);
        attributes[0] = testType;

        //****** Handling=> MotorCode Attribute
        attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.MOTOR_ID.name());
        attrLength = Pattern.ATTR_LENGTH_02;
        String motorCodeSelected = Utility.formatStringCode(motorCode.getSelectedItem().toString());
        attrValue = Pattern.motorMap.get(motorCodeSelected);
        TLV motorCode = new TLV(attrType, attrLength, attrValue);
        attributes[1] = motorCode;

        if (LiftMode == false) {
            //****** Handling=> Signal Voltage Attribute
            attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.SIGNAL_VOLTAGE.name());
            attrLength = Pattern.ATTR_LENGTH_02;
            if (TextUtils.isEmpty(signalValue.getText().toString()))
            {
                signalValue.setText("0");
            }
            targetSignalVoltage = Integer.parseInt(signalValue.getText().toString());
            Integer i1 = Integer.parseInt(signalValue.getText().toString());
            attrValue = Utility.convertIntToHexString(i1);
            TLV signalValue = new TLV(attrType, attrLength, attrValue);
            attributes[2] = signalValue;

            //****** Handling=> Target RPM Attribute
            attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.TARGET_RPM.name());
            attrLength = Pattern.ATTR_LENGTH_02;
            if (TextUtils.isEmpty(targetRPMOut.getText().toString()))
            {
                targetRPMPercent.setText("0");
                targetRPMOut.setText("0");
            }
            Integer i2 = Integer.parseInt(targetRPMOut.getText().toString()); // TO CHANGE TO targetRPM
            attrValue = Utility.convertIntToHexString(i2);
            TLV targetRPM = new TLV(attrType, attrLength, attrValue);
            attributes[3] = targetRPM;
            //Runt time attribute is taken from the app here
            //******* Handling=> RunTime Attribute
            attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.TEST_TIME.name());
            attrLength = Pattern.ATTR_LENGTH_02;
            if (TextUtils.isEmpty(runTime.getText().toString()))
            {
                runTime.setText("0");
            }
            Integer i3 = Integer.parseInt(runTime.getText().toString());
            attrValue = Utility.convertIntToHexString(i3);
            TLV runTime = new TLV(attrType, attrLength, attrValue);
            attributes[4] = runTime;

        }
        // if liftMode = =1
        else {
            //****** Handling=> LiftMode direction Attribute
            attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.LIFT_DIRECTION.name());
            attrLength = Pattern.ATTR_LENGTH_02;
            String directionSelected = Utility.formatStringCode(directionTypespinner.getSelectedItem().toString());
            attrValue = Pattern.liftDirectionTypesMap.get(directionSelected);
            TLV liftDirValue = new TLV(attrType, attrLength, attrValue);
            attributes[2] = liftDirValue;

            //****** Handling=> LiftMode distance Attribute
            attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.LIFT_DISTANCE.name());
            attrLength = Pattern.ATTR_LENGTH_02;
            if (TextUtils.isEmpty(distanceMenu.getText().toString()))
            {
                distanceMenu.setText("0");
            }
            Integer i2 = Integer.parseInt(distanceMenu.getText().toString());
            attrValue = Utility.convertIntToHexString(i2);
            TLV liftDistVal = new TLV(attrType, attrLength, attrValue);
            attributes[3] = liftDistVal;

            //******* Handling=> RunTime Attribute with some default values
            attrType = Pattern.diagnoseAttrTypesMap.get(Pattern.DiagnosticAttrType.TEST_TIME.name());
            attrLength = Pattern.ATTR_LENGTH_02;
            Integer i3 = 0; //default value
            attrValue = Utility.convertIntToHexString(i3);
            TLV runTime = new TLV(attrType, attrLength, attrValue);
            attributes[4] = runTime;
        }

        String screen = Utility.ReverseLookUp(Pattern.screenMap, Pattern.Screen.DIAGNOSTICS.name());
        String machineId = Settings.getMachineId();
        String machineType = Utility.ReverseLookUp(Pattern.machineTypeMap, Pattern.MachineType.RING_FRAME.name());
        String messageType = Utility.ReverseLookUp(Pattern.messageTypeMap, Pattern.MessageType.BACKGROUND_DATA.name());
        String screenSubState = Pattern.COMMON_NONE_PARAM;

        //****CHANGE FOR RELEASE V3 -- harsha

        //****CHANGE FOR RELEASE V2
        //Data Validation for Test RPM & Signal Voltage
        String validateMessage = isValidData();
        if (validateMessage == null) {
            String payload = diagnosePacket.makePacket(screen,
                    machineId,
                    machineType,
                    messageType,
                    screenSubState,
                    attributes);

            mWriter.writeln(payload.toUpperCase());
        } else {
                Snackbar.make(getWindow().getDecorView().getRootView(), validateMessage, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            UpdateEnabledTestType();
        }
    }

    private List<String> getValueListForSpinner(final String entity) {
        List<String> list = new ArrayList<>();
        int length;
        switch (entity) {
            case "MOTOR_TYPE":
                length = Pattern.MotorTypes.values().length;
                while (length > 0) {
                    String value = Pattern.MotorTypes.values()[length - 1].name();
                    list.add(Utility.formatString(value));
                    length--;
                }
                break;
            case "TEST_TYPE":
                length = Pattern.DiagnosticTestTypes.values().length;
                while (length > 0) {
                    String value = Pattern.DiagnosticTestTypes.values()[length - 1].name();
                    list.add(Utility.formatString(value));
                    length--;
                }
                break;
            case "DIRECTIONS":
                length = Pattern.Lift_DirectionTypes.values().length;
                while (length > 0) {
                    String value = Pattern.Lift_DirectionTypes.values()[length - 1].name();
                    list.add(Utility.formatString(value));
                    length--;
                }
                break;
        }
        return list;
    }

    private void DiagnosisMenuModeOn(final Boolean bol) { // what is this
        runOnUiThread(new Runnable() {
            public void run() {
                testType.setEnabled(bol);
                motorCode.setEnabled(bol);
                runTime.setEnabled(bol);
                signalValue.setEnabled(bol);
                targetRPMPercent.setEnabled(bol);
                if (bol) {
                    setDefaultValue();
                }
            }
        });

    }

    private void toggleViewOn(final String layout) {
        runOnUiThread(new Runnable() {
            public void run() {
                switch (layout) {
                    case LAYOUT_LIVE:
                        menuLayout.setVisibility(View.INVISIBLE);
                        liveLayout.setVisibility(View.VISIBLE);
                        if (LiftMode == true) {
                            livenormal.setVisibility(View.INVISIBLE);
                            livelift.setVisibility(View.VISIBLE);
                            liveliftDone.setVisibility(View.INVISIBLE);
                            testTypeLive.setEnabled(false);
                        }
                        else {
                            livenormal.setVisibility(View.VISIBLE);
                            livelift.setVisibility(View.INVISIBLE);
                            testTypeLive.setEnabled(true);
                        }
                        break;
                    case LAYOUT_MENU:
                        menuLayout.setVisibility(View.VISIBLE);
                        liveLayout.setVisibility(View.INVISIBLE);
                        break;
                    case NORMAL_MENU:
                        normalMenu.setVisibility((View.VISIBLE));
                        liftMenu.setVisibility((View.INVISIBLE));
                        break;
                    case LIFT_MENU:
                        normalMenu.setVisibility(View.INVISIBLE);
                        liftMenu.setVisibility((View.VISIBLE));
                        break;
                }

            }
        });

    }

    private void setDefaultValue() {
        runOnUiThread(new Runnable() {
            public void run() {

                LiftMode = false;
                distanceMenu.setText("0");

                runTime.setText("0");
                signalValue.setText("0");
                targetRPMPercent.setText("0");
                targetRPMPercent.setEnabled(false); // on start the default is open loop
                targetRPMOut.setText("0");

                //-----------------------//
                // set the correct max Rpm in the menu screen.
                String motorSelectedType = Utility.formatStringCode((motorCode.getSelectedItem().toString()));
                //put the logic here only for what the maxRpm should be
                maxRPM = GetMaxRPM(motorSelectedType);
                maxRpmText.setText(Utility.formatString(Integer.toString(maxRPM)));
                //----------------------//
                testTypeLive.setText("0");
                motorCodeLive.setText("0");
                //runTimeLive.setText("0");
                signalValueLive.setText("0");
                actualRPMLive.setText("0");
            }
        });

    }

    private void updateLiveData(final TLV[] attributes) {
        runOnUiThread(new Runnable() {
            public void run() {
                String testType = Utility.formatValueByPadding(attributes[0].getValue(), 1);
                testType = Utility.ReverseLookUp(Pattern.diagnoseTestTypesMap, testType);
                String motorCode = Utility.formatValueByPadding(attributes[1].getValue(), 1);
                motorCode = Utility.ReverseLookUp(Pattern.motorMap, motorCode);

                testTypeLive.setText(Utility.formatString(testType));
                motorCodeLive.setText(Utility.formatString(motorCode));

                if (LiftMode == false) {
                    String signalVoltage = attributes[2].getValue();
                    String targetRPM = attributes[3].getValue();

                    if (testType.equals(Pattern.DiagnosticTestTypes.OPEN_LOOP.toString())){
                        targetLabelLive.setText("Target Signal Voltage %");
                        targetTextLive.setText(Integer.toString(targetSignalVoltage));
                    }
                    if (testType.equals(Pattern.DiagnosticTestTypes.CLOSED_LOOP.toString())){
                        targetLabelLive.setText("Target RPM ");
                        targetTextLive.setText(Integer.toString(targetRpmCalc));
                    }

                    signalValueLive.setText(signalVoltage);
                    actualRPMLive.setText(targetRPM);
                }
            }
        });

    }

    private String isValidData() {

        if (LiftMode == false) {
            IntegerInputFilter set1 = new IntegerInputFilter(getString(R.string.label_diagnose_ip_signal), 10, 90);
            IntegerInputFilter set2 = new IntegerInputFilter(getString(R.string.label_diagnose_target_rpm), 10, 90);
            IntegerInputFilter set3 = new IntegerInputFilter(getString(R.string.label_diagnose_run_time), 30, 300);

            String testTypeSelected = Utility.formatStringCode((testType.getSelectedItem().toString()));
            //only check the box you want to use
            if (testTypeSelected.equals(Pattern.DiagnosticTestTypes.OPEN_LOOP.toString())) {
                if (set1.filter(signalValue) != null) {
                    return set1.filter(signalValue);
                }
            }

            if (testTypeSelected.equals(Pattern.DiagnosticTestTypes.CLOSED_LOOP.toString())) {
                if (set2.filter(targetRPMPercent) != null) {
                    return set2.filter(targetRPMPercent);
                }
            }

            if (set3.filter(runTime) != null) {
                return set3.filter(runTime);
            }
        }else{
            IntegerInputFilter set1 = new IntegerInputFilter(getString(R.string.label_diagnose_distance), 5, 150);

            if (set1.filter(distanceMenu) != null) {
                return set1.filter(distanceMenu);
            }
        }

        return null;
    }

    //================================== BLUETOOTH EVENT CALLBACKS =================================
    @Override
    public void onDataRead(byte[] bytes, int i) {

        if (isDiagnoseRunning) {
            try {
                final String payload = new String(bytes, "UTF-8").replaceAll("(\\r|\\n)", "");
                if (!payload.isEmpty()) {
                    String packPayload;
                    switch (payload) {
                        case "XXDLIVE":
                            packPayload = "7E01120101020500040102000202020032030204B0030204B07E";
                            break;
                        case "XXDLIVE2":
                            packPayload = "7E01120101020500040102000202020032030205DC030204B07E";
                            break;
                        case "XXDSUCCESS":
                            packPayload = "7E010B010102059800000000007E";
                            break;
                        case "XXDFAIL":
                            packPayload = "7E010B010102059700000000007E";
                            break;
                        case "XXDINCOMPLETE":
                            packPayload = "7E010B010102059600000000007E";
                            break;
                        case "XXDLIFTDOFF":
                            packPayload = "7E010B010102059500000000007E";
                            break;
                        default:
                            packPayload = payload;
                    }


                    //packPayload = payload;
                    Log.d(TAG,packPayload);
                    if (packPayload.length() >= 20) { //size of header is 20 . Min Size of packet 20
                        if (Packet.getHeadersScreen(packPayload).equals(Pattern.Screen.DIAGNOSTICS.name())) {
                            if (Packet.getHeadersSubScreen(packPayload).equals(Pattern.ScreenSubState.DIAG_SUCCESS.name())) {

                                isDiagnoseRunning = false;
                                snackbarComplete = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_diagnose_complete_success, Snackbar.LENGTH_INDEFINITE);
                                // change snackbar text color
                                View snackbarView = snackbarComplete.getView();
                                int snackbarTextId = android.support.design.R.id.snackbar_text;
                                TextView textView = (TextView)snackbarView.findViewById(snackbarTextId);
                                textView.setTextColor(getResources().getColor(R.color.colorPrimary));

                                snackbarComplete.setAction("Action", null).show();
                                //snackbarVersion = snackBar_diagOK; //to keep track of which snackbar
                                isSnackbarOn = true;

                                if (LiftMode == true)
                                {
                                    liveliftDone.setVisibility(View.VISIBLE);
                                    liveliftprogress.setVisibility(View.INVISIBLE);
                                }
                            }
                            else if (Packet.getHeadersSubScreen(packPayload).equals(Pattern.ScreenSubState.DIAG_FAIL.name())) {
                                isDiagnoseRunning = false;
                                snackbarComplete = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_diagnose_complete_fail, Snackbar.LENGTH_INDEFINITE);

                                // change snackbar text color
                                View snackbarView = snackbarComplete.getView();
                                int snackbarTextId = android.support.design.R.id.snackbar_text;
                                TextView textView = (TextView)snackbarView.findViewById(snackbarTextId);
                                textView.setTextColor(getResources().getColor(R.color.colorAccent));

                                snackbarComplete.setAction("Action", null).show();
                                isSnackbarOn = true;
                            }
                            else if (
                                Packet.getHeadersSubScreen(packPayload).equals(Pattern.ScreenSubState.DIAG_INCOMPLETE.name())) {
                                isDiagnoseRunning = false;
                                snackbarComplete = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_diagnose_Incomplete, Snackbar.LENGTH_INDEFINITE);
                                // change snackbar text color
                                View snackbarView = snackbarComplete.getView();
                                int snackbarTextId = android.support.design.R.id.snackbar_text;
                                TextView textView = (TextView)snackbarView.findViewById(snackbarTextId);
                                textView.setTextColor(getResources().getColor(R.color.darkOrange));

                                snackbarComplete.setAction("Action", null).show();
                               
                            }
                            else if (
                                    Packet.getHeadersSubScreen(packPayload).equals(Pattern.ScreenSubState.DIAG_LIFT_DOFF_ERR.name())) {
                                isDiagnoseRunning = false;
                                Snackbar snackbarComplete = Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_diagnose_lift_doff_err, Snackbar.LENGTH_INDEFINITE);
                                // change snackbar text color
                                View snackbarView = snackbarComplete.getView();
                                int snackbarTextId = android.support.design.R.id.snackbar_text;
                                TextView textView = (TextView)snackbarView.findViewById(snackbarTextId);
                                textView.setTextColor(getResources().getColor(R.color.darkOrange));

                                snackbarComplete.setAction("Action", null).show();

                                runDiagnose.setVisibility(View.INVISIBLE);
                            } else {
                                toggleViewOn(LAYOUT_LIVE);
                                Packet packet = new Packet(Packet.INCOMING_PACKET);

                                if (packet.processIncomePayload(packPayload)) {
                                       TLV[] attr = packet.getAttributes();
                                       updateLiveData(attr);
                                    }
                                }
                            }
                        }
                    }

            } catch (UnsupportedEncodingException | NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    @Override
    public void onStatusChange(BluetoothStatus bluetoothStatus) {
        if (bluetoothStatus == BluetoothStatus.NONE) {
            Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_bluetooth_disconnected, Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public void onDeviceName(String s) {

    }

    @Override
    public void onToast(String s) {

    }

    @Override
    public void onDataWrite(byte[] bytes) {
        try {
            String payload = new String(bytes, "UTF-8").replaceAll("(\\r|\\n)", "");
            if (!payload.equals(Pattern.DISABLE_MACHINE_START_DIAGNOSE)) {
                Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_diagnose_running, Snackbar.LENGTH_INDEFINITE)
                        .setAction("Action", null).show();

                //Here check what the lift mode is, and set the correct screen for the Live setup.
                DiagnosisMenuModeOn(false);

                // set the diagnose  button Off
                runDiagnose.setVisibility(View.VISIBLE);
                isDiagnoseRunning = true;
                }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


    //===================================== ACTIVITY EVENTS ========================================
    @Override
    protected void onResume() {
        super.onResume();
        mService.setOnEventCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //mService.disconnect();
    }

    @Override
    public void onBackPressed() {
        //Back navigation to Menu Screen.

            // go back to idle menu on one press
            mWriter.writeln(Pattern.ENABLE_MACHINE_START.toUpperCase());
            //super.onBackPressed();

        //finish this activity
        finish();
    }




    @Override
    public void onFocusChange(View view, boolean b) {
        if (!b) {
            if (TextUtils.isEmpty(targetRPMPercent.getText().toString())) {
                targetRPMOut.setText(Integer.toString(0));
                targetRPMPercent.setText(Integer.toString(0));
            } else {
                int currentTargetPercent = Integer.parseInt(targetRPMPercent.getText().toString());
                targetRpmCalc = (currentTargetPercent * maxRPM) / 100;
                targetRPMOut.setText(Integer.toString(targetRpmCalc));
            }
        }
    }


}
