package machine.microspin.com.ringDoubler.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Interface for all constants patterns for the incoming/outgoing packets
 */

public interface Pattern {

    String ATTR_LENGTH_01 = "01";
    String ATTR_LENGTH_02 = "02";
    String ATTR_LENGTH_04 = "04";
    String COMMON_NONE_PARAM = "00";

    String DISABLE_MACHINE_START_SETTINGS = "7E020B010402030001010200017E";
    String DISABLE_MACHINE_START_DIAGNOSE = "7E020B010402030001010200027E";

    String UPDATE_RTF_ADD = "7E020B010302070001010200027E";
    String UPDATE_RTF_SUB = "7E020B010302070001010200037E";


    String ENABLE_MACHINE_START = "7E020B010402020001000200007E";
    String RF_MACHINE_RESTART = "7E020B010402060001000200007E";


    /* INCOMING PACKET PATTERNS */
    enum MachineType {
        CARDING_MACHINE, FLYER, DRAW_FRAME, RING_FRAME,RING_DOUBLER
    }

    enum Screen {
        IDLE, RUN, STOP, SETTING, DIAGNOSTICS, HW_CHANGE_VALS, UPDATE_RTF
    }

    enum ScreenSubState {
        NORMAL, RAMP_UP, PIECING, HOMING, PAUSE, HALT, IDLE, ACK, DOFFRESET, DIAG_SUCCESS,DIAG_FAIL,DIAG_INCOMPLETE,DIAG_LIFT_DOFF_ERR, RTF_CHANGE, RTF_ADD, RTF_SUBTRACT
    }

    enum Sender {
        MACHINE, HMI
    }

    enum MessageType {
        SCREEN_DATA, BACKGROUND_DATA
    }

    enum OperationParameter {
        CYLINDER, BEATER, PRODUCTION, PRODUCTION_SPEED, RTF, LAYERS
    }

    enum StopMessageType {
        REASON, MOTOR_ERROR_CODE, ERROR_VAL
    }

    enum StopReasonValue { // For ring doubler no Back roller
        FRONT_ROLLER_RIGHT, FRONT_ROLLER_LEFT, LIFT_RIGHT, LIFT_LEFT,SPINDLE,USER_PRESS_PAUSE,LAYERS_COMPLETE
    }


    enum MotorErrorCode {
        RPM_ERROR, MOTOR_VOLTAGE_ERROR, DRIVER_VOLTAGE_ERROR, SIGNAL_VOLTAGE_ERROR, OVERLOAD_CURRENT_ERROR, SLIVER_CUT_ERROR, BOBBIN_BED_LIFT
    }

    enum MotorTypes { // Specific to Flyer Frame. NOT GENERAL
        FRONT_ROLLER_RIGHT, FRONT_ROLLER_LEFT,LIFT_RIGHT,LIFT_LEFT,SPINDLE
    }

    /* OUTGOING PACKET PATTERNS */
    enum Information {
        PAIRED, DISABLE_MACHINE_START, SETTINGS_CHANGE_VALS, HW_CHANGE_VALS, DIAGNOSTICS
    }

    enum InformationSubType {
        NONE
    }

    enum DisableMachineStartType {
        SCREEN_ENTERED_FROM_IDLE
    }

    enum DisableMachineStartValue {
        SETTINGS_CHANGE, DIAGNOSTIC, HW_CHANGE
    }

    enum DiagnosticTestTypes {
        CLOSED_LOOP, OPEN_LOOP
    }

    enum Lift_DirectionTypes {
        UP, DOWN
    }

    enum DiagnosticAttrType {
        KIND_OF_TEST, MOTOR_ID, SIGNAL_VOLTAGE, TARGET_RPM, TEST_TIME,LIFT_DIRECTION,LIFT_DISTANCE
    }

    enum Setting {
        DELIVERY_SPEED, TENSION_DRAFT, CYLINDER_SPEED, CYLINDER_FEED, BEATER_SPEED, BEATER_FEED, CONVEYOR_SPEED, CONVEYOR_DELAY, CONVEYOR_DWELL
    }

    /* INCOMING PACKET PATTERNS MAPPING */
    Map<String, String> machineTypeMap = new HashMap<String, String>() {
        {
            put("01", MachineType.CARDING_MACHINE.name());
            put("02", MachineType.DRAW_FRAME.name());
            put("03", MachineType.FLYER.name());
            put("04", MachineType.RING_FRAME.name());
            put("05", MachineType.RING_DOUBLER.name());
        }
    };

    Map<String, String> screenMap = new HashMap<String, String>() {
        {
            put("01", Screen.IDLE.name());
            put("02", Screen.RUN.name());
            put("03", Screen.STOP.name());
            put("04", Screen.SETTING.name());
            put("05", Screen.DIAGNOSTICS.name());
            put("06", Screen.HW_CHANGE_VALS.name());
            // Flyer parameters:
            put("07", Screen.UPDATE_RTF.name());
        }
    };

    Map<String, String> screenSubStateMap = new HashMap<String, String>() {
        {
            put("00", ScreenSubState.IDLE.name());
            put("01", ScreenSubState.NORMAL.name());
            put("02", ScreenSubState.RAMP_UP.name());
            put("03", ScreenSubState.PIECING.name());
            put("04", ScreenSubState.HOMING.name());
            put("11", ScreenSubState.PAUSE.name());
            put("12", ScreenSubState.HALT.name());
            put("94", ScreenSubState.DOFFRESET.name());
            put("95", ScreenSubState.DIAG_LIFT_DOFF_ERR.name());
            put("96", ScreenSubState.DIAG_INCOMPLETE.name());
            put("97", ScreenSubState.DIAG_FAIL.name());
            put("98", ScreenSubState.DIAG_SUCCESS.name());
            put("99", ScreenSubState.ACK.name());
            // Flyer parameters:
            //put("13", ScreenSubState.RTF_CHANGE.name()); // Currently in Attr Type in TLV
            //put("14", ScreenSubState.RTF_ADD.name()); // Currently in Attr Value in TLV
            //put("15", ScreenSubState.RTF_SUBTRACT.name()); // Currently in Attr Value in TLV
        }
    };

    Map<String, String> senderMap = new HashMap<String, String>() {
        {
            put("01", Sender.MACHINE.name());
            put("02", Sender.HMI.name());
        }
    };

    Map<String, String> messageTypeMap = new HashMap<String, String>() {
        {
            put("01", MessageType.SCREEN_DATA.name());
            put("02", MessageType.BACKGROUND_DATA.name());
        }
    };

    Map<String, String> operationParameterMap = new HashMap<String, String>() {
        {
            put("00", "");
            put("01", OperationParameter.CYLINDER.name());
            put("02", OperationParameter.BEATER.name());
            put("05", OperationParameter.PRODUCTION.name());
            //--Draw-frame parameters:
            put("06", OperationParameter.PRODUCTION_SPEED.name());
            // Flyer parameters:
            put("06", OperationParameter.RTF.name());
            put("07", OperationParameter.LAYERS.name());
            //--ring Frame Parameters---//

        }
    };

    Map<String, String> stopMessageTypeMap = new HashMap<String, String>() {
        {
            put("01", StopMessageType.REASON.name());
            put("02", StopMessageType.MOTOR_ERROR_CODE.name());
            put("03", StopMessageType.ERROR_VAL.name());
            put("00", "");
        }
    };

    Map<String, String> stopReasonValueMap = new HashMap<String, String>() {
        {
            put("0006", StopReasonValue.LAYERS_COMPLETE.name());
            put("0008", StopReasonValue.USER_PRESS_PAUSE.name());
            // Flyer parameters:
            put("0070", StopReasonValue.FRONT_ROLLER_RIGHT.name());
            put("0072", StopReasonValue.FRONT_ROLLER_LEFT.name());
            put("0074", StopReasonValue.LIFT_RIGHT.name());
            put("0075", StopReasonValue.LIFT_LEFT.name());
            put("0076", StopReasonValue.SPINDLE.name());

        }
    };

    Map<String, String> motorErrorCodeMap = new HashMap<String, String>() {
        {
            put("0002", MotorErrorCode.RPM_ERROR.name());
            put("0003", MotorErrorCode.MOTOR_VOLTAGE_ERROR.name());
            put("0004", MotorErrorCode.DRIVER_VOLTAGE_ERROR.name());
            put("0005", MotorErrorCode.BOBBIN_BED_LIFT.name());
            put("0007", MotorErrorCode.SLIVER_CUT_ERROR.name());
        }
    };

    Map<String, String> motorMap = new HashMap<String, String>() {
        {
            put(MotorTypes.FRONT_ROLLER_RIGHT.name(), "70");
            put(MotorTypes.FRONT_ROLLER_LEFT.name(), "72");
            put(MotorTypes.LIFT_RIGHT.name(), "74");
            put(MotorTypes.LIFT_LEFT.name(), "75");
            put(MotorTypes.SPINDLE.name(), "76");
        }
    };

    Map<String, String> diagnoseTestTypesMap = new HashMap<String, String>() {
        {
            put(DiagnosticTestTypes.CLOSED_LOOP.name(), "01");
            put(DiagnosticTestTypes.OPEN_LOOP.name(), "02");
        }
    };

    Map<String, String> liftDirectionTypesMap = new HashMap<String, String>() {
        {
            put(Lift_DirectionTypes.UP.name(), "01");
            put(Lift_DirectionTypes.DOWN.name(), "02");
        }
    };

    Map<String, String> diagnoseAttrTypesMap = new HashMap<String, String>() {
        {
            put(DiagnosticAttrType.KIND_OF_TEST.name(), "01");
            put(DiagnosticAttrType.MOTOR_ID.name(), "02");
            put(DiagnosticAttrType.SIGNAL_VOLTAGE.name(), "03");
            put(DiagnosticAttrType.TARGET_RPM.name(), "04");
            put(DiagnosticAttrType.TEST_TIME.name(), "05");
            put(DiagnosticAttrType.LIFT_DIRECTION.name(), "06");
            put(DiagnosticAttrType.LIFT_DISTANCE.name(), "07");
        }
    };


}

