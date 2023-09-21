package machine.microspin.com.ringDoubler.entity;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

/**
 * Settings Repo for storing setting vars throughout the application lifetime\
 * ilocos code
 */

public class Settings {

    public static BluetoothDevice device;
    //===== Settings Parameters =======
    private static int inputYarnCountInNe;
    private static int spindleSpeed;
    private static float outputYarnDia;
    private static int twistPerInch;
    private static int packageHeight;
    private static float diaBuildFactor;
    private static int windingClosenessFactor;
    private static float windingOffsetCoils;
    //Default settings for factory settings
    private static int defaultInputYarnCountInNe = 30;
    private static int defaultspindleSpeed = 8000;
    private static float defaultOutputYarnDia = 0.31f;
    private static int defaultTPI = 20;
    private static int defaultPackageHeight = 200;
    private static float defaultDiaBuildFactor = 0.6f;
    private static int defaultWindingClosenessFactor = 108;
    private static float defaultWindingOffsetCoils = 1.5f;

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
        inputYarnCountInNe = Utility.convertHexToInt(payload.substring(22, 26));
        outputYarnDia = Utility.convertHexToFloat(payload.substring(26, 34));
        spindleSpeed = Utility.convertHexToInt(payload.substring(34, 38));
        twistPerInch = Utility.convertHexToInt(payload.substring(38, 42));
        packageHeight = Utility.convertHexToInt(payload.substring(42, 46));
        diaBuildFactor = Utility.convertHexToFloat(payload.substring(46,54));
        windingClosenessFactor = Utility.convertHexToInt(payload.substring(54, 58));
        windingOffsetCoils = Utility.convertHexToFloat(payload.substring(58, 66));
        Log.d("Settings","recieved!" + inputYarnCountInNe +"," + outputYarnDia +"," + spindleSpeed + "," + twistPerInch
            + "," + packageHeight +"," + diaBuildFactor + "," + windingClosenessFactor + "," + windingOffsetCoils);
        return true;
    }

    public static String updateNewSetting(String s1, String s2, String s3, String s4, String s5, String s6, String s7,String s8) {
        // Update new values in Repo.

        inputYarnCountInNe = Integer.parseInt(s1);
        outputYarnDia = Float.parseFloat(s2);
        spindleSpeed = Integer.parseInt(s3);
        twistPerInch = Integer.parseInt(s4);
        packageHeight = Integer.parseInt(s5);
        diaBuildFactor = Float.parseFloat(s6);
        windingClosenessFactor = Integer.parseInt(s7);
        windingOffsetCoils = Float.parseFloat(s8);

        Log.d("Settings",inputYarnCountInNe +"," + outputYarnDia +"," + spindleSpeed +"," + twistPerInch
        + "," +packageHeight +"," + diaBuildFactor + "," + windingClosenessFactor +"," + windingOffsetCoils);
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

        String attr = Utility.convertIntToHexString(inputYarnCountInNe);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertFloatToHex(outputYarnDia);
        attrPayload.append(Utility.formatValueByPadding(attr,4));

        attr = Utility.convertIntToHexString(spindleSpeed);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertIntToHexString(twistPerInch);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertIntToHexString(packageHeight);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertFloatToHex(diaBuildFactor);
        attrPayload.append(Utility.formatValueByPadding(attr,4));

        attr = Utility.convertIntToHexString(windingClosenessFactor);
        attrPayload.append(Utility.formatValueByPadding(attr,2));

        attr = Utility.convertFloatToHex(windingOffsetCoils);
        attrPayload.append(Utility.formatValueByPadding(attr,4));

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

        Log.d("Setting",payload.toString());
        return payload.toString();

    }

    //==========GETTERS============
    public static int getSpindleSpeed() {
        return spindleSpeed;
    }
    public static int getYarnCount() {
        return inputYarnCountInNe;
    }
    public static float getOutputYarnDia() {
        return outputYarnDia;
    }

    public static String getYarnCountString() {
        return String.format("%d", inputYarnCountInNe);
    }
    public static String GetOutputYarnDiaString() {
        String s =  String.format("%f", outputYarnDia);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }
    public static String getSpindleSpeedString() {
        return String.format("%d", spindleSpeed);
    }
    public static String getTwistPerInchString() {
        return  String.format("%d", twistPerInch);
    }
    public static String getPackageHeightString() { return String.format("%d", packageHeight);}
    public static String getDiaBuildFactorString() {
        String s = String.format("%f", diaBuildFactor);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }
    public static String getWindingClosenessFactorString() {
        return String.format("%d", windingClosenessFactor);}

    public static String getWindingOffsetCoilsString() {
        String s = String.format("%f", windingOffsetCoils);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }


    public static String getDefaultSpindleSpeedString() {
        return String.format("%d", defaultspindleSpeed);
    }

    public static String getDefaultOutputYarnDiaString() {
        String s = String.format("%f", defaultOutputYarnDia);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static String getDefaultYarnCountString() {
        return String.format("%d", defaultInputYarnCountInNe);
    }

    public static String getDefaultTwistPerInchString() {
        return String.format("%d", defaultTPI);   }

    public static String defaultDiaBuildFactorString() {
        String s =  String.format("%f", defaultDiaBuildFactor);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

    public static String geDefaultPackageHeightString() {
        return String.format("%d", defaultPackageHeight);
    }

    public static String getDefaultWindingClosenessFactorString() {
        return String.format("%d", defaultWindingClosenessFactor);
    }
    public static String getDefaultWindingOffsetCoils() {
        String s =  String.format("%f", defaultWindingOffsetCoils);
        return !s.contains(".") ? s : s.replaceAll("0*$", "").replaceAll("\\.$", "");
    }

}
