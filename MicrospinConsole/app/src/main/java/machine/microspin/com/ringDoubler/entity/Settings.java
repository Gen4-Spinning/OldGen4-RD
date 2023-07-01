package machine.microspin.com.ringDoubler.entity;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

/**
 * Settings Repo for storing setting vars throughout the application lifetime
 */

public class Settings {

    public static BluetoothDevice device;
    //===== Settings Parameters =======
    private static int spindleSpeed;
    private static int tensionDraft;
    private static int twistPerInch;
    private static int yarncount;
    private static float bindWindRatio;
    private static float chaseLength;
    private static int preferredPackageSize;
    private static int rightSideOn;
    private static int leftSideOn;
    //Default settings for factory settings
    private static int defaultspindleSpeed = 7000;
    private static int defaulttensionDraft = 5;
    private static int defaulttwistPerInch = 12;
    private static int defaultyarncount= 14; // 24 in Ne = 24 * 0.6 in Nm = 14.4 = 14
    private static float defaultbindWindRatio = 2.0f;
    private static float defaultchaseLength = 55.0f;
    private static int defaultpreferredPackageSize = 110;
    private static int defaultrightSideOn = 1;
    private static int defaultleftSideOn = 1;


    private static String machineId = "01"; //default
    //==== END: Settings Parameters ====

    final private static String ATTR_COUNT = "01";
    final private static String ATTR_MACHINE_TYPE = Utility.ReverseLookUp(Pattern.machineTypeMap, Pattern.MachineType.RING_DOUBLER.name());
    final private static String ATTR_MSG_TYPE_BACKGROUND = Utility.ReverseLookUp(Pattern.messageTypeMap, Pattern.MessageType.BACKGROUND_DATA.name());
    final private static String ATTR_SCREEN_SUB_STATE_NONE = "00";
    final private static String ATTR_TYPE_RINGFRAME_PATTERN = "80";
    final private static String ATTR_SCREEN_SETTING = Utility.ReverseLookUp(Pattern.screenMap, Pattern.Screen.SETTING.name());
    final private static String ATTR_PACKET_LENGTH = "37"; // HexCode full packet length
    final private static int ATTR_LENGTH = 28; //settings TLV length


    public static String getMachineId (){
        return machineId;
    }
    public static Boolean processSettingsPacket(String payload) {
        if (payload.length() < 4) {
            return false;
        }

        String SOF = payload.substring(0, 2);
        int payloadLength = payload.length();
        String EOF = payload.substring(payloadLength - 2, payloadLength);
        if (!SOF.equals(Packet.START_IDENTIFIER) || !EOF.equals(Packet.END_IDENTIFIER)) {
            return false;
        }

        String sender = payload.substring(2, 4);
        if (!sender.equals(Packet.SENDER_MACHINE)) {
            return false;
        }

        machineId = payload.substring(6, 8);

        // Mapping Setting Parameters.
        int yarncountNe = Utility.convertHexToInt(payload.substring(22, 26));
        yarncount =  Math.round(yarncountNe/0.3f) ;

        spindleSpeed = Utility.convertHexToInt(payload.substring(26, 30));
        tensionDraft = Utility.convertHexToInt(payload.substring(30, 34));
        twistPerInch = Utility.convertHexToInt(payload.substring(34, 38));
        bindWindRatio = Utility.convertHexToFloat(payload.substring(38, 46));
        chaseLength = Utility.convertHexToFloat(payload.substring(46,54));
        preferredPackageSize = Utility.convertHexToInt(payload.substring(54, 58));
        rightSideOn = Utility.convertHexToInt(payload.substring(58, 62));
        leftSideOn = Utility.convertHexToInt(payload.substring(62, 66));

        return true;
    }

    public static String updateNewSetting(String s1, String s2, String s3, String s4, String s5, String s6, String s7,String s8,String s9) {
        // Update new values in Repo.

        yarncount = Integer.parseInt(s1);//we ll get a Nm , but we need to pass an Ne
        int output_yarnCount = Math.round(yarncount * 0.3f); //for ring doubler, we want to set the coutn in Nm, not ne. Ne = nm*0.6,
        spindleSpeed = Integer.parseInt(s2);//and then we re doubling so the yarn thickness will double, so divide by 2. this is so that the builder works ok.
        tensionDraft = Integer.parseInt(s3);
        twistPerInch = Integer.parseInt(s4);
        bindWindRatio = Float.parseFloat(s5);
        chaseLength = Float.parseFloat(s6);
        preferredPackageSize = Integer.parseInt(s7);
        rightSideOn = Integer.parseInt(s8);
        leftSideOn = Integer.parseInt(s9);

        // Construct payload String
        StringBuilder payload = new StringBuilder();

        //Delimiters
        String SOF = Packet.START_IDENTIFIER;
        String EOF = Packet.END_IDENTIFIER;

        String sender = Packet.SENDER_HMI;

        //Getting Packet length

        //Construct Attribute payload String
        StringBuilder attrPayload = new StringBuilder();

        attrPayload.append(ATTR_TYPE_RINGFRAME_PATTERN).
                append(String.format("%02d", ATTR_LENGTH));

        String attr = Utility.convertIntToHexString(output_yarnCount);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertIntToHexString(spindleSpeed);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertIntToHexString(tensionDraft);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertIntToHexString(twistPerInch);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertFloatToHex(bindWindRatio);
        attrPayload.append(Utility.formatValueByPadding(attr,4));

        attr = Utility.convertFloatToHex(chaseLength);
        attrPayload.append(Utility.formatValueByPadding(attr,4));

        attr = Utility.convertIntToHexString(preferredPackageSize);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertIntToHexString(rightSideOn);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertIntToHexString(leftSideOn);
        attrPayload.append(Utility.formatValueByPadding(attr,2));


        //Construct payload string
        payload.append(SOF).
                append(sender).
                append(ATTR_PACKET_LENGTH).
                append(machineId).
                append(ATTR_MACHINE_TYPE).
                append(ATTR_MSG_TYPE_BACKGROUND).
                append(ATTR_SCREEN_SETTING).
                append(ATTR_SCREEN_SUB_STATE_NONE).
                append(ATTR_COUNT).
                append(attrPayload.toString()).
                append(EOF);

        return payload.toString();

    }

    //==========GETTERS============
    public static int getSpindleSpeed() {
        return spindleSpeed;
    }

    public static String getSpindleSpeedString() {
        return String.format("%d", spindleSpeed);
    }

    public static float getTensionDraft() {
        return tensionDraft;
    }

    public static String getTensionDraftString() {
        return  String.format("%d", tensionDraft);
    }

    public static int getYarnCount() {
        return yarncount;
    }
    public static String getYarnCountString() {
        return String.format("%d", yarncount);
    }

    public static String getTwistPerInchString() {
        return  String.format("%d", twistPerInch);
    }

    public static String getBindWindRatioString() {
        String s =  String.format("%f", bindWindRatio);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }


    public static String getChaseLengthString() {
        String s = String.format("%f", chaseLength);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }


    public static String getPreferredPackageSizeString() {
        return String.format("%d", preferredPackageSize);
    }

    public static String getrightSideOnString() {
        return String.format("%d", rightSideOn);
    }

    public static String getleftSideOnString() {
        return String.format("%d", leftSideOn);
    }



    public static String getDefaultSpindleSpeedString() {
        return String.format("%d", defaultspindleSpeed);
    }

    public static String getDefaultTensionDraftString() {
        return String.format("%d", defaulttensionDraft);    }

    public static String getDefaultYarnCountString() {
        return String.format("%d", defaultyarncount);
    }

    public static String getDefaultTwistPerInchString() {
        return String.format("%d", defaulttwistPerInch);   }

    public static String getDefaultBindWindRatioString() {
        String s =  String.format("%f", defaultbindWindRatio);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }


    public static String getDefaultChaseLengthString() {
        String s = String.format("%f", defaultchaseLength);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }


    public static String getDefaultPreferredPackageSizeString() {
        return String.format("%d", defaultpreferredPackageSize);
    }
    public static String getDefaultrightSideOnString() {
        return String.format("%d", defaultrightSideOn);
    }

    public static String getDefaultleftSideOnString() {
        return String.format("%d", defaultleftSideOn);
    }

}
