package machine.microspin.com.ringDoubler;


import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothService;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothStatus;
import com.github.douglasjunior.bluetoothclassiclibrary.BluetoothWriter;

import java.io.UnsupportedEncodingException;

import machine.microspin.com.ringDoubler.entity.Packet;
import machine.microspin.com.ringDoubler.entity.Pattern;
import machine.microspin.com.ringDoubler.entity.Settings;
import machine.microspin.com.ringDoubler.entity.SettingsCommunicator;
import machine.microspin.com.ringDoubler.fragment.RunModeFragment;
import machine.microspin.com.ringDoubler.fragment.SettingsFragment;

@SuppressWarnings("ALL")
public class DashboardRunMode extends AppCompatActivity implements SettingsCommunicator, BluetoothService.OnBluetoothEventCallback, TabLayout.OnTabSelectedListener {

    public static final String TAG = "MicroSpin";
    private static String deviceAddress;
    public static Boolean isExpectingSettings = false;
    public static Boolean isInIdleMode = false;
    public static Boolean isWaitingForAck = false;
    public TabLayout tabLayout;
    boolean doubleBackToExitPressedOnce = false;

    final private static String SETTINGS_REQ_PAYLOAD = "7E020B0101029900010002007E";

    private BluetoothService mService;
    private BluetoothWriter mWriter;

    private static Boolean isSecondConnectionTry = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //******* Release Change v2
        if(Settings.device != null) {
            setTitle(Settings.device.getName());
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setOnTabSelectedListener(this);

        mService = BluetoothService.getDefaultInstance();
        mWriter = new BluetoothWriter(mService);

        // ===== Request for Settings ======
        mWriter.writeln(SETTINGS_REQ_PAYLOAD.toUpperCase());
        isExpectingSettings = true;
    }

    //================================== BLUETOOTH EVENT CALLBACKS =================================
    @Override
    public void onDataRead(byte[] bytes, int length) {
        final RunModeFragment runFragment = (RunModeFragment) getSupportFragmentManager().getFragments().get(0);
        final SettingsFragment settingsFragment = (SettingsFragment) getSupportFragmentManager().getFragments().get(1);
        if (runFragment instanceof RunModeFragment) {
            try {
                String payload = new String(bytes, "UTF-8").replaceAll("(\\r|\\n)", "");
                if (!payload.isEmpty()) {
                    //=============================================================
                    String packetPayload;
                    switch (payload) {
                        case "XXNORMAL":
                            packetPayload = "7E011601040102010206022EE0070200057E";
                            break;
                        case "XXPAUSE1":
                            packetPayload = "7E01130104010311030102000800020000000200007E";
                            break;
                        case "XXHALT":
                            packetPayload = "7E01130104010312030102004602020002000200007E";
                            break;
                        case "XXIDLE":
                            packetPayload = "7E01000104010100007E";
                            break;
                        case "XXHOMING":
                            packetPayload = "7E011601040102040206020000070200007E";
                            break;
                        case "XXSETTING":
                            packetPayload = "7E012501040204000180280320410ccccd3f66666600283f99999a3f8ccccd423c6666007800207E";
                            break;
                        case "XXACK":
                            packetPayload = "7E010A010402049901000000007E";
                            break;
                        case "XXDOFFRESET":
                            packetPayload = "7E010A010402049401000000007E";
                            break;
                        case "XXLAYERSOVER":
                            packetPayload = "7E01130104010312030102000600020000000200007E";
                            break;
                        default:
                            packetPayload = payload;
                    }

                    if (packetPayload.length() >= 20) { //size of header is 20 . Min Size of packet 20

                        if (Packet.getHeadersScreen(packetPayload).equals(Pattern.Screen.SETTING.name())) {
                            if (Packet.getHeadersSubScreen(packetPayload).equals(Pattern.ScreenSubState.ACK.name())) {
                                if (isWaitingForAck) {
                                    isWaitingForAck = false;
                                    Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_setting_updated, Snackbar.LENGTH_SHORT)
                                            .setAction("Action", null).show();
                                }
                            } else if (Packet.getHeadersSubScreen(packetPayload).equals(Pattern.ScreenSubState.DOFFRESET.name())) {
                                if (runFragment.isdoffReset == false) {
                                    Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_doffSetting_reset, Snackbar.LENGTH_SHORT)
                                            .setAction("Action", null).show();
                                    runFragment.isdoffReset = true;
                                    }

                                runFragment.hideDoffRestartButton();
                            }
                            else if (Settings.processSettingsPacket(packetPayload)) {
                                //if the Settings was requested by Device to Machine
                                if (isExpectingSettings) {
                                    settingsFragment.updateContent();
                                    isExpectingSettings = false;
                                }
                            }
                        } else {
                            //Update Run, Stop & Idle Mode Screen(s)
                            runFragment.updateContent(packetPayload);
                            Boolean canEditSettings = runFragment.canEditSettings();
                            settingsFragment.isEditMode(canEditSettings);
                        }
                    }
                    //=================================================================
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
            if (Settings.device != null) {
                mService.connect(Settings.device);
                /*isSecondConnectionTry = true;*/
            }
            /*if (isSecondConnectionTry) {
                Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_bluetooth_disconnected, Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                isSecondConnectionTry = false;
            }*/
        }
    }


        @Override
    public void onDeviceName(String s) {

    }

    @Override
    public void onToast(String message) {
        Snackbar.make(getWindow().getDecorView().getRootView(), message, Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
    }

    @Override
    public void onDataWrite(byte[] bytes) {

    }

    //============================== FRAGMENT IMPLEMENTED FUNCTIONS ================================
    @Override
    public void onSettingsUpdate(String payload) {
        mWriter.writeln(payload.toUpperCase());
        isWaitingForAck = true;
        Snackbar.make(getWindow().getDecorView().getRootView(), R.string.msg_setting_updating, Snackbar.LENGTH_INDEFINITE)
                .setAction("Action", null).show();
    }

    @Override
    public void onViewChangeClick(View view) {
        switch (view.getId()) {
            case R.id.diagnoseBtn:
                startActivity(new Intent(DashboardRunMode.this, Diagnose.class));
                break;
            case R.id.restartBtn:
                mWriter.writeln(Pattern.RF_MACHINE_RESTART.toUpperCase());
                break;
        }
    }

    @Override
    public void updateIdleModeStatus(Boolean bol) {
        isInIdleMode = bol;
    }

    @Override
    public void raiseMessage(String Message) {
        if (Message.equals(getString(R.string.msg_halt_restart))) {
            Snackbar.make(getWindow().getDecorView().getRootView(), Message, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.action_restart, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent i = getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                        }
                    }).show();
        } else {
            Snackbar.make(getWindow().getDecorView().getRootView(), Message, Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
        }
    }

    @Override
    public void updateRTF(String type) {

    }

    //==================================== CUSTOM FUNCTIONS ========================================


    //===================================== ACTIVITY EVENTS ========================================
    @Override
    protected void onResume() {
        super.onResume();
        mService.setOnEventCallback(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mService.disconnect();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAndRemoveTask();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    //======================================= MENU EVENTS ==========================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dashboard, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_quit) {
            mService.disconnect();
            mService = null;
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //==================================== FRAGMENT SELECTION ======================================

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new RunModeFragment();
                case 1:
                    return new SettingsFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        // Get Tab Title(s)
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getResources().getString(R.string.label_status);
                case 1:
                    return getResources().getString(R.string.label_settings);
            }
            return null;
        }
    }

    //========================  TAB Events ===============================
    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (isInIdleMode) {
            switch (tab.getPosition()) {
                case 1:
                    mWriter.writeln(Pattern.DISABLE_MACHINE_START_SETTINGS.toUpperCase());
                    break;
                case 0:
                    mWriter.writeln(Pattern.ENABLE_MACHINE_START.toUpperCase());
            }
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

}
