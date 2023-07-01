package machine.microspin.com.ringDoubler.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import machine.microspin.com.ringDoubler.R;
import machine.microspin.com.ringDoubler.entity.Packet;
import machine.microspin.com.ringDoubler.entity.Pattern;
import machine.microspin.com.ringDoubler.entity.SettingsCommunicator;
import machine.microspin.com.ringDoubler.entity.TLV;
import machine.microspin.com.ringDoubler.entity.Utility;

/**
 * Fragment 1 - RUN MODE STATUS
 * Shows the Status details sent by the connected ble device.
 */

public class RunModeFragment extends Fragment implements Pattern, View.OnClickListener {

    public TextView statusText, attr1Value, attr2Value, attr3Value, reasonText, reasonTypeText, errorText;
    public TextView valueValue;
    public LinearLayout stopLayout, runLayout, idleLayout, statusBox, errorBox;
    public LinearLayout attr1Box, attr2Box, attr3Box, valueBox;
    public Button restartBtn,diagnoseBtn;
    private TextView doffOverLabel;
    private Boolean canEdit;
    private SettingsCommunicator mCallback;
    private Boolean haltMessageIsOpen = false;
    public boolean isdoffReset = false; // set in dashboard run mode, and used in diagnose

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_run_mode, container, false);

        //=>Run Screen Layout
        statusText = (TextView) rootView.findViewById(R.id.statusText);
        attr1Value = (TextView) rootView.findViewById(R.id.attr1Value);
        attr2Value = (TextView) rootView.findViewById(R.id.attr2Value);
        //=>Stop Screen Layout
        reasonText = (TextView) rootView.findViewById(R.id.reasonText);
        reasonTypeText = (TextView) rootView.findViewById(R.id.reasonTypeLabel);
        errorText = (TextView) rootView.findViewById(R.id.motorErrorCode);
        valueValue = (TextView) rootView.findViewById(R.id.valueValue);
        //=>Idle Screen Layout
        diagnoseBtn = (Button) rootView.findViewById(R.id.diagnoseBtn);
        restartBtn = (Button) rootView.findViewById(R.id.restartBtn);
        doffOverLabel = (TextView) rootView.findViewById(R.id.doffOverLabel);
        //=>Layout Reference
        stopLayout = (LinearLayout) rootView.findViewById(R.id.stopLayout);
        runLayout = (LinearLayout) rootView.findViewById(R.id.runLayout);
        idleLayout = (LinearLayout) rootView.findViewById(R.id.idleLayout);
        statusBox = (LinearLayout) rootView.findViewById(R.id.statusBox);
        errorBox = (LinearLayout) rootView.findViewById(R.id.errorBox);
        //=>Attribute Boxes
        attr1Box = (LinearLayout) rootView.findViewById(R.id.attr1Box);
        valueBox = (LinearLayout) rootView.findViewById(R.id.valueBox);

        diagnoseBtn.setOnClickListener(this);
        restartBtn.setOnClickListener(this);

        stopLayout.setVisibility(View.INVISIBLE);
        runLayout.setVisibility(View.INVISIBLE);
        idleLayout.setVisibility(View.INVISIBLE);

        return rootView;
    }

    public void updateContent(final String payload) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                Packet packet = new Packet(Packet.INCOMING_PACKET);
                //int attrCount = packet.getAttributeCount()
                if (packet.processIncomePayload(payload)) {
                    statusBox.setVisibility(View.VISIBLE);
                    canEdit = false;

                    TLV[] attr = packet.getAttributes();
                    if (packet.getNextScreen().equals(Screen.RUN.name())) {
                        toggleVisibility(Screen.RUN.name());

                        attr1Value.setText(attr[0].getValue()); // SpindleSpeed
                        attr2Value.setText(attr[1].getValue()); // Doff Percent
                        haltMessageIsOpen = false;
                    }
                    if (packet.getNextScreen().equals(Screen.STOP.name())) {
                        errorBox.setVisibility(View.INVISIBLE);
                        valueBox.setVisibility(View.INVISIBLE);
                        toggleVisibility(Screen.STOP.name());
                        //=>Display Stop Reason & Data, if present
                        if (!attr[0].getType().isEmpty() || !attr[0].getType().equals("")) {
                            if (attr[0].getType().equals(StopMessageType.REASON.name())) {
                                String attr0 = Utility.formatValueByPadding(attr[0].getValue(), 2);
                                reasonText.setText(Utility.formatString(Pattern.stopReasonValueMap.get(attr0)));
                                if (attr[1].getType().equals(StopMessageType.MOTOR_ERROR_CODE.name())) {
                                    reasonTypeText.setText(getString(R.string.label_stop_motor));
                                    errorBox.setVisibility(View.VISIBLE);
                                    String attr1 = Utility.formatValueByPadding(attr[1].getValue(), 2);
                                    errorText.setText(Utility.formatString(Pattern.motorErrorCodeMap.get(attr1)));
                                }else {
                                    reasonTypeText.setText(getString(R.string.label_stop_reason));
                                }

                                if (packet.getScreenSubState().equals(ScreenSubState.HALT.name())) {
                                    if (!haltMessageIsOpen) {
                                        mCallback.raiseMessage(getString(R.string.msg_halt_restart));
                                        haltMessageIsOpen = true;
                                    }
                                } else {
                                    haltMessageIsOpen = false;
                                }
                            }

                            } else {
                                reasonText.setVisibility(View.INVISIBLE);
                                errorBox.setVisibility(View.INVISIBLE);
                            }
                        }
                    if (packet.getNextScreen().equals(Screen.IDLE.name())) {
                        canEdit = true;
                        mCallback.updateIdleModeStatus(true);
                        toggleVisibility(Screen.IDLE.name());
                        haltMessageIsOpen = false;
                    }else{
                        mCallback.updateIdleModeStatus(false);
                    }
                    statusText.setText(Utility.formatString(packet.getScreenSubState()));
                }
            }
        });
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

    public void toggleVisibility(String screenName) {

        if (screenName.equals(Screen.STOP.name())) {
            runLayout.setVisibility(View.INVISIBLE);
            idleLayout.setVisibility(View.INVISIBLE);
            stopLayout.setVisibility(View.VISIBLE);
        } else if (screenName.equals(Screen.RUN.name())) {
            stopLayout.setVisibility(View.INVISIBLE);
            idleLayout.setVisibility(View.INVISIBLE);
            runLayout.setVisibility(View.VISIBLE);
        } else if (screenName.equals(Screen.IDLE.name())) {
            stopLayout.setVisibility(View.INVISIBLE);
            idleLayout.setVisibility(View.VISIBLE);
            runLayout.setVisibility(View.INVISIBLE);
        }
    }

    public Boolean canEditSettings() {
        return canEdit;
    }

    public void hideDoffRestartButton(){
        restartBtn.setVisibility(View.GONE);
        doffOverLabel.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View view) {
        mCallback.onViewChangeClick(view);
    }
}
