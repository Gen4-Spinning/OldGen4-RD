package machine.microspin.com.ringDoubler.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.text.TextUtils;
import android.widget.LinearLayout;

import machine.microspin.com.ringDoubler.R;
import machine.microspin.com.ringDoubler.entity.DoubleInputFilter;
import machine.microspin.com.ringDoubler.entity.IntegerInputFilter;
import machine.microspin.com.ringDoubler.entity.SettingsCommunicator;
import machine.microspin.com.ringDoubler.entity.Settings;

/**
 * Fragment to handle Settings (Editable and non Editable)
 */

public class SettingsFragment extends Fragment implements View.OnClickListener {
    private SettingsCommunicator mCallback;
    public EditText setting1, setting2, setting3, setting4, setting5, setting6, setting7,setting8,setting9;
    public Button saveBtn,factorystngsBtn;
    private LinearLayout leftSide,rightSide,draft;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.frament_settings, container, false);

        setting1 = (EditText) rootView.findViewById(R.id.setting1);
        setting2 = (EditText) rootView.findViewById(R.id.setting2);
        setting3 = (EditText) rootView.findViewById(R.id.setting3);
        setting4 = (EditText) rootView.findViewById(R.id.setting4);
        setting5 = (EditText) rootView.findViewById(R.id.setting5);
        setting6 = (EditText) rootView.findViewById(R.id.setting6);
        setting7 = (EditText) rootView.findViewById(R.id.setting7);
        setting8 = (EditText) rootView.findViewById(R.id.setting8);
        setting9 = (EditText) rootView.findViewById(R.id.setting9);

        saveBtn = (Button) rootView.findViewById(R.id.saveBtn);
        saveBtn.setOnClickListener(this);
        factorystngsBtn = (Button) rootView.findViewById(R.id.factorystngs);
        factorystngsBtn.setOnClickListener(this);

        leftSide = (LinearLayout) rootView.findViewById(R.id.leftSideOnLayout);
        rightSide = (LinearLayout) rootView.findViewById(R.id.rightSideOnLayout);
        draft = (LinearLayout) rootView.findViewById(R.id.draftLayout);

        leftSide.setVisibility(View.GONE);
        rightSide.setVisibility(View.GONE);
        draft.setVisibility(View.GONE);

        setStatusInputFields(false);
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


        @Override
    public void onClick(View v) {
        if (v.getId() == R.id.saveBtn) {
            if (TextUtils.isEmpty(setting1.getText().toString()))
            {
                setting1.setText("0");
            }
            if (TextUtils.isEmpty(setting2.getText().toString()))
            {
                setting2.setText("0");
            }
            if (TextUtils.isEmpty(setting3.getText().toString()))
            {
                setting3.setText("0");
            }
            if (TextUtils.isEmpty(setting4.getText().toString()))
            {
                setting4.setText("0");
            }
            if (TextUtils.isEmpty(setting5.getText().toString()))
            {
                setting5.setText("0");
            }
            if (TextUtils.isEmpty(setting6.getText().toString()))
            {
                setting6.setText("0");
            }
            if (TextUtils.isEmpty(setting7.getText().toString()))
            {
                setting7.setText("0");
            }
            if (TextUtils.isEmpty(setting8.getText().toString()))
            {
                setting8.setText("2");
            }
            if (TextUtils.isEmpty(setting9.getText().toString()))
            {
                setting9.setText("2");
            }
            String validateMessage = isValidSettings();
            if (validateMessage == null) {
                String payload = Settings.updateNewSetting(
                        setting1.getText().toString(),
                        setting2.getText().toString(),
                        Integer.toString(2), // Draft is always 2, but unused
                        setting4.getText().toString(),
                        setting5.getText().toString(),
                        setting6.getText().toString(),
                        setting7.getText().toString(),
                        Integer.toString(1),
                        Integer.toString(1)
                        );
                mCallback.onSettingsUpdate(payload.toUpperCase());
            } else {
                mCallback.raiseMessage(validateMessage);
            }

        }
        if (v.getId() == R.id.factorystngs){
            setting1.setText(Settings.getDefaultYarnCountString());
            setting2.setText(Settings.getDefaultSpindleSpeedString());
            setting3.setText(Settings.getDefaultTensionDraftString());
            setting4.setText(Settings.getDefaultTwistPerInchString());
            setting5.setText(Settings.getDefaultBindWindRatioString());
            setting6.setText(Settings.getDefaultChaseLengthString());
            setting7.setText(Settings.getDefaultPreferredPackageSizeString());
            setting8.setText(Settings.getDefaultrightSideOnString());
            setting9.setText(Settings.getDefaultleftSideOnString());
        }
    }

    private String isValidSettings() {
        IntegerInputFilter set1 = new IntegerInputFilter(getString(R.string.label_setting_1), 20, 66); //yarncount in Ne limits are 16 and 40, in Nm limits are 26 and 66
        int yarncount = Integer.parseInt(setting1.getText().toString());

        int minspindleSpeed = 4000;
        int maxSpindleSpeed = 8000;
        //No draft for Ring Doubler
        //int minDraft = 15;
        //int maxDraft = 40;
        int minTPI = 7;
        int maxTPI = 25;
        /*if (yarncount < 30) {
            minspindleSpeed = 4000;
            maxSpindleSpeed = 7500;
            minDraft = 20;
            maxDraft = 30;
            minTPI = 10;
            maxTPI = 26;
        }else if (yarncount < 40){
            minspindleSpeed = 6500;
            maxSpindleSpeed = 10000;
            minDraft = 30;
            maxDraft = 35;
            minTPI = 19;
            maxTPI = 30;
        } else{
            minspindleSpeed = 7500;
            maxSpindleSpeed = 10000;
            minDraft = 30;
            maxDraft = 33;
            minTPI = 24;
            maxTPI = 36;
        }*/

        IntegerInputFilter set2 = new IntegerInputFilter(getString(R.string.label_setting_2), minspindleSpeed, maxSpindleSpeed); //spindle speed
        //IntegerInputFilter set3 = new IntegerInputFilter(getString(R.string.label_setting_3), minDraft, maxDraft); //Draft
        IntegerInputFilter set4 = new IntegerInputFilter(getString(R.string.label_setting_4), minTPI, maxTPI); //TPI
        DoubleInputFilter set5 = new DoubleInputFilter(getString(R.string.label_setting_5), 1.8, 2.5); //bindWindRatio
        DoubleInputFilter set6 = new DoubleInputFilter(getString(R.string.label_setting_6), 53.34, 60.96); //chaselength
        IntegerInputFilter set7 = new IntegerInputFilter(getString(R.string.label_setting_7), 70, 130); //packagesize
        //IntegerInputFilter set8 = new IntegerInputFilter(getString(R.string.label_setting_8), 0, 1); //right side on
        //IntegerInputFilter set9 = new IntegerInputFilter(getString(R.string.label_setting_9), 0, 1); //left side on

        if (set1.filter(setting1) != null) {
            return set1.filter(setting1);
        }
        if (set2.filter(setting2) != null) {
            return set2.filter(setting2);
        }
        /*if (set3.filter(setting3) != null) {
            return set3.filter(setting3);
        }*/
        if (set4.filter(setting4) != null) {
            return set4.filter(setting4);
        }
        if (set5.filter(setting5) != null) {
            return set5.filter(setting5);
        }
        if (set6.filter(setting6) != null) {
            return set6.filter(setting6);
        }
        if (set7.filter(setting7) != null) {
            return set7.filter(setting7);
        }
        //No left side on Right Side On
        /*
        if (set8.filter(setting8) != null) {
            return set8.filter(setting8);
        }
        if (set9.filter(setting9) != null) {
            return set9.filter(setting9);
        }*/

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
        setting1.setText(Settings.getYarnCountString());
        setting2.setText(Settings.getSpindleSpeedString());
        setting3.setText(Settings.getTensionDraftString());
        setting4.setText(Settings.getTwistPerInchString());
        setting5.setText(Settings.getBindWindRatioString());
        setting6.setText(Settings.getChaseLengthString());
        setting7.setText(Settings.getPreferredPackageSizeString());
        setting8.setText(Settings.getrightSideOnString());
        setting9.setText(Settings.getleftSideOnString());

    }

    public void setStatusInputFields(Boolean bol) {
        setting1.setEnabled(bol);
        setting2.setEnabled(bol);
        setting3.setEnabled(bol);
        setting4.setEnabled(bol);
        setting5.setEnabled(bol);
        setting6.setEnabled(bol);
        setting7.setEnabled(bol);
        setting8.setEnabled(bol);
        setting9.setEnabled(bol);
    }


}

