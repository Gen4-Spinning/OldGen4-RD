package machine.microspin.com.ringDoubler.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextUtils;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import machine.microspin.com.ringDoubler.R;
import machine.microspin.com.ringDoubler.entity.DoubleInputFilter;
import machine.microspin.com.ringDoubler.entity.IntegerInputFilter;
import machine.microspin.com.ringDoubler.entity.Pattern;
import machine.microspin.com.ringDoubler.entity.SettingsCommunicator;
import machine.microspin.com.ringDoubler.entity.Settings;
import machine.microspin.com.ringDoubler.entity.Utility;

/**
 * Fragment to handle Settings (Editable and non Editable)
 */

public class SettingsFragment extends Fragment implements View.OnClickListener,AdapterView.OnItemSelectedListener {
    private SettingsCommunicator mCallback;
    public EditText inputYarnNe, outputYarnDia, tpi, packageHeight, diaBuildFactor, windingClosenessFactor, windingOffsetCoils;
    public Button saveBtn,factorystngsBtn;
    private Spinner spindleRPMSpinner;
    private boolean firstInit = false;

    public String spindleRPM_Setting;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frament_settings, container, false);

        inputYarnNe = (EditText) rootView.findViewById(R.id.setting1);
        outputYarnDia = (EditText) rootView.findViewById(R.id.setting2);
        tpi = (EditText) rootView.findViewById(R.id.setting4);
        packageHeight = (EditText) rootView.findViewById(R.id.setting5);
        diaBuildFactor = (EditText) rootView.findViewById(R.id.setting6);
        windingClosenessFactor = (EditText) rootView.findViewById(R.id.setting7);
        windingOffsetCoils = (EditText) rootView.findViewById(R.id.setting8);

        spindleRPMSpinner = (Spinner) rootView.findViewById(R.id.spindleSpinner);
        String RPMS [] = {"6000","8000","9000","10000"};
        ArrayAdapter<String> spinnerArrayAdapter =  new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, RPMS);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spindleRPMSpinner.setAdapter(spinnerArrayAdapter);
        spindleRPMSpinner.setOnItemSelectedListener(this);
        spindleRPM_Setting = "6000";

        saveBtn = (Button) rootView.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(this);
        factorystngsBtn = (Button) rootView.findViewById(R.id.factorystngs);
        factorystngsBtn.setOnClickListener(this);

        setStatusInputFields(true);
        return rootView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (SettingsCommunicator) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement SettingsCommunicator");
        }
    }

    @Override
    public void onDetach() {
        mCallback = null;
        super.onDetach();
    }
    //Spinner TestType Events
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        if (firstInit == false) {
            firstInit = true;
            return;
        } else {
            if (parent.getId() == R.id.spindleSpinner) {
                spindleRPM_Setting = Utility.formatStringCode(spindleRPMSpinner.getSelectedItem().toString());
                Log.d("Settings", spindleRPM_Setting);
            }

        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveBtn) {
            if (TextUtils.isEmpty(inputYarnNe.getText().toString()))
            {
                inputYarnNe.setText("0");
            }
            if (TextUtils.isEmpty(outputYarnDia.getText().toString()))
            {
                outputYarnDia.setText("0");
            }
            if (TextUtils.isEmpty(tpi.getText().toString()))
            {
                tpi.setText("0");
            }
            if (TextUtils.isEmpty(packageHeight.getText().toString()))
            {
                packageHeight.setText("0");
            }
            if (TextUtils.isEmpty(diaBuildFactor.getText().toString()))
            {
                diaBuildFactor.setText("0");
            }
            if (TextUtils.isEmpty(windingClosenessFactor.getText().toString()))
            {
                windingClosenessFactor.setText("0");
            }
            if (TextUtils.isEmpty(windingOffsetCoils.getText().toString()))
            {
                windingOffsetCoils.setText("0");
            }
            String validateMessage = isValidSettings();
            if (validateMessage == null) {
                String s1 = inputYarnNe.getText().toString();
                String payload = Settings.updateNewSetting(
                        inputYarnNe.getText().toString(),
                        outputYarnDia.getText().toString(),
                        spindleRPM_Setting,
                        tpi.getText().toString(),
                        packageHeight.getText().toString(),
                        diaBuildFactor.getText().toString(),
                        windingClosenessFactor.getText().toString(),
                        windingOffsetCoils.getText().toString()
                        );
                mCallback.onSettingsUpdate(payload.toUpperCase());
            } else {
                mCallback.raiseMessage(validateMessage);
            }

        }
        if (v.getId() == R.id.factorystngs){
            inputYarnNe.setText(Settings.getDefaultYarnCountString());
            outputYarnDia.setText(Settings.getDefaultOutputYarnDiaString());
            spindleRPMSpinner.setSelection(1);//here we set the default spindle RPM
            spindleRPM_Setting = "8000"; // put into the string also.
            tpi.setText(Settings.getDefaultTwistPerInchString());
            packageHeight.setText(Settings.geDefaultPackageHeightString());
            diaBuildFactor.setText(Settings.defaultDiaBuildFactorString());
            windingClosenessFactor.setText(Settings.getDefaultWindingClosenessFactorString());
            windingOffsetCoils.setText(Settings.getDefaultWindingOffsetCoils());
        }
    }

    private String isValidSettings() {
        IntegerInputFilter set1 = new IntegerInputFilter(getString(R.string.label_setting_1), 10, 86);
        int yarncount = Integer.parseInt(inputYarnNe.getText().toString());
        DoubleInputFilter set2 = new DoubleInputFilter(getString(R.string.label_setting_2), 0.05, 1.5);
        IntegerInputFilter set3 = new IntegerInputFilter(getString(R.string.label_setting_3), 6000, 10000);
        IntegerInputFilter set4 = new IntegerInputFilter(getString(R.string.label_setting_4), 10, 30); //TPI
        IntegerInputFilter set5 = new IntegerInputFilter(getString(R.string.label_setting_5), 50, 250);
        DoubleInputFilter set6 = new DoubleInputFilter(getString(R.string.label_setting_6), 0.05, 2.5);
        IntegerInputFilter set7 = new IntegerInputFilter(getString(R.string.label_setting_7), 50, 200);
        DoubleInputFilter set8 = new DoubleInputFilter(getString(R.string.label_setting_8), 1, 5);

        if (set1.filter(inputYarnNe) != null) {
            return set1.filter(inputYarnNe);
        }
        if (set2.filter(outputYarnDia) != null) {
            return set2.filter(outputYarnDia);
        }
        /*if (set3.filter(spindleRPM) != null) {
            return set3.filter(spindleRPM);
        }*/ // DONT NEED FOR SPINNER
        if (set4.filter(tpi) != null) {
            return set4.filter(tpi);
        }
        if (set5.filter(packageHeight) != null) {
            return set5.filter(packageHeight);
        }
        if (set6.filter(diaBuildFactor) != null) {
            return set6.filter(diaBuildFactor);
        }
        if (set7.filter(windingClosenessFactor) != null) {
            return set7.filter(windingClosenessFactor);
        }
        if (set8.filter(windingOffsetCoils) != null) {
            return set8.filter(windingOffsetCoils);
        }

        return null;
    }

    public void isEditMode(Boolean isEdit) {
        if (isEdit) {
            //Make settings editable
            saveBtn.setVisibility(View.VISIBLE);
            factorystngsBtn.setVisibility(View.VISIBLE);
            setStatusInputFields(true);
        } else {
            //Make settings non editable.
            saveBtn.setVisibility(View.INVISIBLE);
            factorystngsBtn.setVisibility(View.INVISIBLE);
            setStatusInputFields(false);
        }
    }

    public void updateContent() {
        inputYarnNe.setText(Settings.getYarnCountString());
        outputYarnDia.setText(Settings.GetOutputYarnDiaString());
        String spindleRPM = Settings.getSpindleSpeedString();
        if (spindleRPM.equals("6000")){
            spindleRPMSpinner.setSelection(0);
        }else if (spindleRPM.equals("8000")){
            spindleRPMSpinner.setSelection(1);
        }else if (spindleRPM.equals("9000")){
            spindleRPMSpinner.setSelection(2);
        }else if (spindleRPM.equals("10000")){
            spindleRPMSpinner.setSelection(3);
        }else{
            spindleRPMSpinner.setSelection(0);
        }
        //spindleRPM.setText(Settings.GetOutputYarnDiaString());
        //TODO updating the spindleRPM spinner from the settings we get
        tpi.setText(Settings.getTwistPerInchString());
        packageHeight.setText(Settings.getPackageHeightString());
        diaBuildFactor.setText(Settings.getDiaBuildFactorString());
        windingClosenessFactor.setText(Settings.getWindingClosenessFactorString());
        windingOffsetCoils.setText(Settings.getWindingOffsetCoilsString());

    }

    public void setStatusInputFields(Boolean bol) {
        inputYarnNe.setEnabled(bol);
        outputYarnDia.setEnabled(bol);
        spindleRPMSpinner.setEnabled(bol);
        tpi.setEnabled(bol);
        packageHeight.setEnabled(bol);
        diaBuildFactor.setEnabled(bol);
        windingClosenessFactor.setEnabled(bol);
        windingOffsetCoils.setEnabled(bol);
    }

    public List<String> getValueListForSpindleRPMSpinner(){
        List<String> list = new ArrayList<>();
        list.add("6000");
        list.add("8000");
        list.add("9000");
        list.add("10000");
        return list;
    }


}

