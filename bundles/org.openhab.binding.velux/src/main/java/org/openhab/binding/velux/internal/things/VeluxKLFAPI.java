/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.velux.internal.things;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * The {@link VeluxKLFAPI} class defines common KLF200 API constants, which are
 * used across the whole binding.
 * <P>
 * It provides the Enumeration of available API message identifiers as well as
 * constants which describe the KLF200 API restrictions.
 * <P>
 * Classes/Enumeration available:
 * <UL>
 * <LI>Enumeration {@link Command} provides command name, coding and description.</LI>
 * <LI>Class {@link CommandName} to handle symbolic API names.</LI>
 * <LI>Class {@link CommandNumber} to handle API code.</LI>
 * </UL>
 * Constants available:
 * <UL>
 * <LI>{@link #KLF_SYSTEMTABLE_MAX} provides limits of the System table.</LI>
 * </UL>
 * <P>
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public class VeluxKLFAPI {

    // Constants

    /**
     * System table index parameter - an be a number from 0 to 203.
     *
     * See <a href=
     * "https://velcdn.azureedge.net/~/media/com/api/klf200/technical%20specification%20for%20klf%20200%20api.pdf#page=25">KLF200
     * System table</a>
     */
    public static final int KLF_SYSTEMTABLE_MAX = 203;

    // Type definitions

    /**
     * Handle symbolic names of the {@link VeluxKLFAPI}.
     * <P>
     * Methods available:
     * <UL>
     * <LI>Constructor {@link CommandName} by String.</LI>
     * <LI>Method {@link toString} to return a String.</LI>
     * </UL>
     */
    @NonNullByDefault
    public static class CommandName {
        private String name;

        CommandName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

    }

    /**
     * Handle API codings of the {@link VeluxKLFAPI}.
     * <P>
     * Methods available:
     * <UL>
     * <LI>CommandNumber {@link CommandName} by short.</LI>
     * <LI>Method {@link toShort} to return a short.</LI>
     * <LI>Method {@link toString} to return a well-formatted String.</LI>
     * </UL>
     */
    @NonNullByDefault
    public static class CommandNumber {
        private short commandNumber;

        public CommandNumber(short commandNumber) {
            this.commandNumber = commandNumber;
        }

        public short toShort() {
            return commandNumber;
        }

        @Override
        public String toString() {
            return "0x" + Integer.toHexString(new Short(commandNumber).intValue());
        }
    }

    /**
     * Enumeration of complete API as definition of a
     * List of Gateway commands.
     * <P>
     * See <a href=
     * "https://velcdn.azureedge.net/~/media/com/api/klf200/technical%20specification%20for%20klf%20200%20api.pdf#page=115">Appendix
     * 3: List of Gateway commands</a>
     * <P>
     * Methods available:
     * <UL>
     * <LI>Constructor {@link Command} by String.</LI>
     * <LI>Method {@link getCommand} to return a {@link CommandNumber}.</LI>
     * <LI>Method {@link getDescription} to return a description as String.</LI>
     * <LI>Method {@link get} to return a {@link Command} based on the given <B>int</B>.</LI>
     * </UL>
     */
    public enum Command {
        // Special item: unrecognized command
        UNDEFTYPE((short) -1, "Unknown command."),
        // Special item: Shutdown of the connection
        GW_OPENHAB_CLOSE((short) -2, "openHAB connection shutdown command."),
        // Special item: Shutdown of the connection
        GW_OPENHAB_RECEIVEONLY((short) -3, "openHAB receive command."),
        // Velux specific commands
        GW_ERROR_NTF((short) 0x0000, "Provides information on what triggered the error."),
        GW_REBOOT_REQ((short) 0x0001, "Request gateway to reboot."),
        GW_REBOOT_CFM((short) 0x0002, "Acknowledge to GW_REBOOT_REQ command."),
        GW_SET_FACTORY_DEFAULT_REQ((short) 0x0003,
                "Request gateway to clear system table, scene table and set Ethernet settings to factory default. Gateway will reboot."),
        GW_SET_FACTORY_DEFAULT_CFM((short) 0x0004, "Acknowledge to GW_SET_FACTORY_DEFAULT_REQ command."),
        GW_GET_VERSION_REQ((short) 0x0008, "Request version information."),
        GW_GET_VERSION_CFM((short) 0x0009, "Acknowledge to GW_GET_VERSION_REQ command."),
        GW_GET_PROTOCOL_VERSION_REQ((short) 0x000A, "Request KLF 200 API protocol version."),
        GW_GET_PROTOCOL_VERSION_CFM((short) 0x000B, "Acknowledge to GW_GET_PROTOCOL_VERSION_REQ command."),
        GW_GET_STATE_REQ((short) 0x000C, "Request the state of the gateway"),
        GW_GET_STATE_CFM((short) 0x000D, "Acknowledge to GW_GET_STATE_REQ command."),

        GW_LEAVE_LEARN_STATE_REQ((short) 0x000E, "Request gateway to leave learn state."),
        GW_LEAVE_LEARN_STATE_CFM((short) 0x000F, "Acknowledge to GW_LEAVE_LEARN_STATE_REQ command."),
        GW_GET_NETWORK_SETUP_REQ((short) 0x00E0, "Request network parameters."),
        GW_GET_NETWORK_SETUP_CFM((short) 0x00E1, "Acknowledge to GW_GET_NETWORK_SETUP_REQ."),
        GW_SET_NETWORK_SETUP_REQ((short) 0x00E2, "Set network parameters."),
        GW_SET_NETWORK_SETUP_CFM((short) 0x00E3, "Acknowledge to GW_SET_NETWORK_SETUP_REQ."),

        GW_CS_GET_SYSTEMTABLE_DATA_REQ((short) 0x0100, "Request a list of nodes in the gateways system table."),
        GW_CS_GET_SYSTEMTABLE_DATA_CFM((short) 0x0101, "Acknowledge to GW_CS_GET_SYSTEMTABLE_DATA_REQ"),
        GW_CS_GET_SYSTEMTABLE_DATA_NTF((short) 0x0102,
                "Acknowledge to GW_CS_GET_SYSTEM_TABLE_DATA_REQList of nodes in the gateways systemtable."),
        GW_CS_DISCOVER_NODES_REQ((short) 0x0103, "Start CS  DiscoverNodes macro in KLF200."),
        GW_CS_DISCOVER_NODES_CFM((short) 0x0104, "Acknowledge to GW_CS_DISCOVER_NODES_REQ command."),
        GW_CS_DISCOVER_NODES_NTF((short) 0x0105, "Acknowledge to GW_CS_DISCOVER_NODES_REQ command."),
        GW_CS_REMOVE_NODES_REQ((short) 0x0106, "Remove one or more nodes  in the systemtable."),
        GW_CS_REMOVE_NODES_CFM((short) 0x0107, "Acknowledge to GW_CS_REMOVE_NODES_REQ."),
        GW_CS_VIRGIN_STATE_REQ((short) 0x0108, "Clear systemtable and delete system key."),
        GW_CS_VIRGIN_STATE_CFM((short) 0x0109, "Acknowledge to GW_CS_VIRGIN_STATE_REQ."),
        GW_CS_CONTROLLER_COPY_REQ((short) 0x010A,
                "Setup KLF200 to get or give a system to or from another io-homecontrol®  remote control. By a system means all nodes in the systemtable and the system key."),
        GW_CS_CONTROLLER_COPY_CFM((short) 0x010B, "Acknowledge to GW_CS_CONTROLLER_COPY_REQ."),
        GW_CS_CONTROLLER_COPY_NTF((short) 0x010C, "Acknowledge to GW_CS_CONTROLLER_COPY_REQ."),
        GW_CS_CONTROLLER_COPY_CANCEL_NTF((short) 0x010D, "Cancellation of system copy to other controllers."),
        GW_CS_RECEIVE_KEY_REQ((short) 0x010E, "Receive system key from another controller."),
        GW_CS_RECEIVE_KEY_CFM((short) 0x010F, "Acknowledge to GW_CS_RECEIVE_KEY_REQ."),
        GW_CS_RECEIVE_KEY_NTF((short) 0x0110, "Acknowledge to GW_CS_RECEIVE_KEY_REQ with status."),
        GW_CS_PGC_JOB_NTF((short) 0x0111,
                "Information on Product Generic Configuration job initiated by  press on PGC button."),
        GW_CS_SYSTEM_TABLE_UPDATE_NTF((short) 0x0112,
                "Broadcasted to all clients and gives information about added and removed actuator nodes in system table."),
        GW_CS_GENERATE_NEW_KEY_REQ((short) 0x0113, "Generate new system key and update actuators in systemtable."),
        GW_CS_GENERATE_NEW_KEY_CFM((short) 0x0114, "Acknowledge to GW_CS_GENERATE_NEW_KEY_REQ."),
        GW_CS_GENERATE_NEW_KEY_NTF((short) 0x0115, "Acknowledge to GW_CS_GENERATE_NEW_KEY_REQ with status."),
        GW_CS_REPAIR_KEY_REQ((short) 0x0116, "Update key in actuators holding an old  key."),
        GW_CS_REPAIR_KEY_CFM((short) 0x0117, "Acknowledge to GW_CS_REPAIR_KEY_REQ."),
        GW_CS_REPAIR_KEY_NTF((short) 0x0118, "Acknowledge to GW_CS_REPAIR_KEY_REQ with status."),
        GW_CS_ACTIVATE_CONFIGURATION_MODE_REQ((short) 0x0119,
                "Request one or more actuator to open for configuration."),
        GW_CS_ACTIVATE_CONFIGURATION_MODE_CFM((short) 0x011A, "Acknowledge to GW_CS_ACTIVATE_CONFIGURATION_MODE_REQ."),

        GW_GET_NODE_INFORMATION_REQ((short) 0x0200, "Request extended information of one specific actuator node."),
        GW_GET_NODE_INFORMATION_CFM((short) 0x0201, "Acknowledge to GW_GET_NODE_INFORMATION_REQ."),
        GW_GET_NODE_INFORMATION_NTF((short) 0x0210, "Acknowledge to GW_GET_NODE_INFORMATION_REQ."),
        GW_GET_ALL_NODES_INFORMATION_REQ((short) 0x0202, "Request extended information of all nodes."),
        GW_GET_ALL_NODES_INFORMATION_CFM((short) 0x0203, "Acknowledge to GW_GET_ALL_NODES_INFORMATION_REQ"),
        GW_GET_ALL_NODES_INFORMATION_NTF((short) 0x0204,
                "Acknowledge to GW_GET_ALL_NODES_INFORMATION_REQ. Holds node information"),
        GW_GET_ALL_NODES_INFORMATION_FINISHED_NTF((short) 0x0205,
                "Acknowledge to GW_GET_ALL_NODES_INFORMATION_REQ. No more nodes."),
        GW_SET_NODE_VARIATION_REQ((short) 0x0206, "Set node variation."),
        GW_SET_NODE_VARIATION_CFM((short) 0x0207, "Acknowledge to GW_SET_NODE_VARIATION_REQ."),
        GW_SET_NODE_NAME_REQ((short) 0x0208, "Set node name."),
        GW_SET_NODE_NAME_CFM((short) 0x0209, "Acknowledge to GW_SET_NODE_NAME_REQ."),
        GW_SET_NODE_VELOCITY_REQ((short) 0x020A, "Set node velocity."),
        GW_SET_NODE_VELOCITY_CFM((short) 0x020B, "Acknowledge to GW_SET_NODE_VELOCITY_REQ."),
        GW_NODE_INFORMATION_CHANGED_NTF((short) 0x020C, "Information has been updated."),
        GW_NODE_STATE_POSITION_CHANGED_NTF((short) 0x0211, "Information has been updated."),
        GW_SET_NODE_ORDER_AND_PLACEMENT_REQ((short) 0x020D, "Set search order and room placement."),
        GW_SET_NODE_ORDER_AND_PLACEMENT_CFM((short) 0x020E, "Acknowledge to GW_SET_NODE_ORDER_AND_PLACEMENT_REQ."),

        GW_GET_GROUP_INFORMATION_REQ((short) 0x0220, "Request information about  all defined groups."),
        GW_GET_GROUP_INFORMATION_CFM((short) 0x0221, "Acknowledge to GW_GET_GROUP_INFORMATION_REQ."),
        GW_GET_GROUP_INFORMATION_NTF((short) 0x0230, "Acknowledge to GW_GET_NODE_INFORMATION_REQ."),
        GW_SET_GROUP_INFORMATION_REQ((short) 0x0222, "Change an existing group."),
        GW_SET_GROUP_INFORMATION_CFM((short) 0x0223, "Acknowledge to GW_SET_GROUP_INFORMATION_REQ."),
        GW_GROUP_INFORMATION_CHANGED_NTF((short) 0x0224,
                "Broadcast to all, about group information of a group has been changed."),
        GW_DELETE_GROUP_REQ((short) 0x0225, "Delete a group."),
        GW_DELETE_GROUP_CFM((short) 0x0226, "Acknowledge to GW_DELETE_GROUP_INFORMATION_REQ."),
        GW_NEW_GROUP_REQ((short) 0x0227, "Request new group to be created."),
        GW_NEW_GROUP_CFM((short) 0x0228, ""),
        GW_GET_ALL_GROUPS_INFORMATION_REQ((short) 0x0229, "Request information about  all defined groups."),
        GW_GET_ALL_GROUPS_INFORMATION_CFM((short) 0x022A, "Acknowledge to GW_GET_ALL_GROUPS_INFORMATION_REQ."),
        GW_GET_ALL_GROUPS_INFORMATION_NTF((short) 0x022B, "Acknowledge to GW_GET_ALL_GROUPS_INFORMATION_REQ."),
        GW_GET_ALL_GROUPS_INFORMATION_FINISHED_NTF((short) 0x022C, "Acknowledge to GW_GET_ALL_GROUPS_INFORMATION_REQ."),
        GW_GROUP_DELETED_NTF((short) 0x022D,
                "GW_GROUP_DELETED_NTF is broadcasted to all, when a group has been removed."),
        GW_HOUSE_STATUS_MONITOR_ENABLE_REQ((short) 0x0240, "Enable house status monitor."),
        GW_HOUSE_STATUS_MONITOR_ENABLE_CFM((short) 0x0241, "Acknowledge to GW_HOUSE_STATUS_MONITOR_ENABLE_REQ."),
        GW_HOUSE_STATUS_MONITOR_DISABLE_REQ((short) 0x0242, "Disable house status monitor."),
        GW_HOUSE_STATUS_MONITOR_DISABLE_CFM((short) 0x0243, "Acknowledge to GW_HOUSE_STATUS_MONITOR_DISABLE_REQ."),

        GW_COMMAND_SEND_REQ((short) 0x0300, "Send activating command direct to one or more io-homecontrol®  nodes."),
        GW_COMMAND_SEND_CFM((short) 0x0301, "Acknowledge to GW_COMMAND_SEND_REQ."),
        GW_COMMAND_RUN_STATUS_NTF((short) 0x0302, "Gives run status for io-homecontrol®  node."),
        GW_COMMAND_REMAINING_TIME_NTF((short) 0x0303,
                "Gives remaining time before io-homecontrol®  node enter target position."),
        GW_SESSION_FINISHED_NTF((short) 0x0304,
                "Command send, Status request, Wink, Mode or Stop session is finished."),
        GW_STATUS_REQUEST_REQ((short) 0x0305, "Get status request from one or more io-homecontrol®  nodes."),
        GW_STATUS_REQUEST_CFM((short) 0x0306, "Acknowledge to GW_STATUS_REQUEST_REQ."),
        GW_STATUS_REQUEST_NTF((short) 0x0307,
                "Acknowledge to GW_STATUS_REQUEST_REQ. Status request from one or more io-homecontrol®  nodes."),
        GW_WINK_SEND_REQ((short) 0x0308, "Request from one or more io-homecontrol®  nodes to Wink."),
        GW_WINK_SEND_CFM((short) 0x0309, "Acknowledge to GW_WINK_SEND_REQ"),
        GW_WINK_SEND_NTF((short) 0x030A, "Status info for performed wink request."),

        GW_SET_LIMITATION_REQ((short) 0x0310, "Set a parameter limitation in an actuator."),
        GW_SET_LIMITATION_CFM((short) 0x0311, "Acknowledge to GW_SET_LIMITATION_REQ."),
        GW_GET_LIMITATION_STATUS_REQ((short) 0x0312, "Get parameter limitation in an actuator."),
        GW_GET_LIMITATION_STATUS_CFM((short) 0x0313, "Acknowledge to GW_GET_LIMITATION_STATUS_REQ."),
        GW_LIMITATION_STATUS_NTF((short) 0x0314, "Hold  information about limitation."),
        GW_MODE_SEND_REQ((short) 0x0320, "Send Activate Mode to one or more io-homecontrol®  nodes."),
        GW_MODE_SEND_CFM((short) 0x0321, "Acknowledge to GW_MODE_SEND_REQ"),
        GW_MODE_SEND_NTF((short) 0x0322, "Notify with Mode activation info."),

        GW_INITIALIZE_SCENE_REQ((short) 0x0400, "Prepare gateway to record a scene."),
        GW_INITIALIZE_SCENE_CFM((short) 0x0401, "Acknowledge to GW_INITIALIZE_SCENE_REQ."),
        GW_INITIALIZE_SCENE_NTF((short) 0x0402, "Acknowledge to GW_INITIALIZE_SCENE_REQ."),
        GW_INITIALIZE_SCENE_CANCEL_REQ((short) 0x0403, "Cancel record scene process."),
        GW_INITIALIZE_SCENE_CANCEL_CFM((short) 0x0404, "Acknowledge to GW_INITIALIZE_SCENE_CANCEL_REQ command."),
        GW_RECORD_SCENE_REQ((short) 0x0405, "Store actuator positions changes since GW_INITIALIZE_SCENE, as a scene."),
        GW_RECORD_SCENE_CFM((short) 0x0406, "Acknowledge to GW_RECORD_SCENE_REQ."),
        GW_RECORD_SCENE_NTF((short) 0x0407, "Acknowledge to GW_RECORD_SCENE_REQ."),
        GW_DELETE_SCENE_REQ((short) 0x0408, "Delete a recorded scene."),
        GW_DELETE_SCENE_CFM((short) 0x0409, "Acknowledge to GW_DELETE_SCENE_REQ."),
        GW_RENAME_SCENE_REQ((short) 0x040A, "Request a scene to be renamed."),
        GW_RENAME_SCENE_CFM((short) 0x040B, "Acknowledge to GW_RENAME_SCENE_REQ."),
        GW_GET_SCENE_LIST_REQ((short) 0x040C, "Request a list of scenes."),
        GW_GET_SCENE_LIST_CFM((short) 0x040D, "Acknowledge to GW_GET_SCENE_LIST."),
        GW_GET_SCENE_LIST_NTF((short) 0x040E, "Acknowledge to GW_GET_SCENE_LIST."),
        GW_GET_SCENE_INFOAMATION_REQ((short) 0x040F, "Request extended information for one given scene."),
        GW_GET_SCENE_INFOAMATION_CFM((short) 0x0410, "Acknowledge to GW_GET_SCENE_INFOAMATION_REQ."),
        GW_GET_SCENE_INFOAMATION_NTF((short) 0x0411, "Acknowledge to GW_GET_SCENE_INFOAMATION_REQ."),
        GW_ACTIVATE_SCENE_REQ((short) 0x0412, "Request gateway to enter a scene."),
        GW_ACTIVATE_SCENE_CFM((short) 0x0413, "Acknowledge to GW_ACTIVATE_SCENE_REQ."),
        GW_STOP_SCENE_REQ((short) 0x0415, "Request  all nodes  in a given scene to stop at their current position."),
        GW_STOP_SCENE_CFM((short) 0x0416, "Acknowledge to GW_STOP_SCENE_REQ."),
        GW_SCENE_INFORMATION_CHANGED_NTF((short) 0x0419, "A scene has either been changed or removed."),

        GW_ACTIVATE_PRODUCTGROUP_REQ((short) 0x0447, "Activate a product  group in a given direction."),
        GW_ACTIVATE_PRODUCTGROUP_CFM((short) 0x0448, "Acknowledge to GW_ACTIVATE_PRODUCTGROUP_REQ."),
        GW_ACTIVATE_PRODUCTGROUP_NTF((short) 0x0449, "Acknowledge to GW_ACTIVATE_PRODUCTGROUP_REQ."),

        GW_GET_CONTACT_INPUT_LINK_LIST_REQ((short) 0x0460,
                "Get list of assignments to all Contact Input to scene  or product  group."),
        GW_GET_CONTACT_INPUT_LINK_LIST_CFM((short) 0x0461, "Acknowledge to GW_GET_CONTACT_INPUT_LINK_LIST_REQ."),
        GW_SET_CONTACT_INPUT_LINK_REQ((short) 0x0462, "Set a link from a Contact Input to a scene  or product  group."),
        GW_SET_CONTACT_INPUT_LINK_CFM((short) 0x0463, "Acknowledge to GW_SET_CONTACT_INPUT_LINK_REQ."),
        GW_REMOVE_CONTACT_INPUT_LINK_REQ((short) 0x0464, "Remove a link from a Contact Input to a scene."),
        GW_REMOVE_CONTACT_INPUT_LINK_CFM((short) 0x0465, "Acknowledge to GW_REMOVE_CONTACT_INPUT_LINK_REQ."),

        GW_GET_ACTIVATION_LOG_HEADER_REQ((short) 0x0500, "Request header from activation log."),
        GW_GET_ACTIVATION_LOG_HEADER_CFM((short) 0x0501, "Confirm header from activation log."),
        GW_CLEAR_ACTIVATION_LOG_REQ((short) 0x0502, "Request clear all data  in activation log."),
        GW_CLEAR_ACTIVATION_LOG_CFM((short) 0x0503, "Confirm clear all data  in activation log."),
        GW_GET_ACTIVATION_LOG_LINE_REQ((short) 0x0504, "Request line from activation log."),
        GW_GET_ACTIVATION_LOG_LINE_CFM((short) 0x0505, "Confirm line from activation log."),
        GW_ACTIVATION_LOG_UPDATED_NTF((short) 0x0506, "Confirm line from activation log."),
        GW_GET_MULTIPLE_ACTIVATION_LOG_LINES_REQ((short) 0x0507, "Request lines from activation log."),
        GW_GET_MULTIPLE_ACTIVATION_LOG_LINES_NTF((short) 0x0508, "Error log  data from activation log."),
        GW_GET_MULTIPLE_ACTIVATION_LOG_LINES_CFM((short) 0x0509, "Confirm lines from activation log."),

        GW_SET_UTC_REQ((short) 0x2000, "Request to set UTC time."),
        GW_SET_UTC_CFM((short) 0x2001, "Acknowledge to GW_SET_UTC_REQ."),
        GW_RTC_SET_TIME_ZONE_REQ((short) 0x2002, "Set time zone and daylight savings rules."),
        GW_RTC_SET_TIME_ZONE_CFM((short) 0x2003, "Acknowledge to GW_RTC_SET_TIME_ZONE_REQ."),
        GW_GET_LOCAL_TIME_REQ((short) 0x2004,
                "Request  the local time based on current time zone and daylight savings rules."),
        GW_GET_LOCAL_TIME_CFM((short) 0x2005, "Acknowledge to GW_RTC_SET_TIME_ZONE_REQ."),
        GW_PASSWORD_ENTER_REQ((short) 0x3000, "Enter password to authenticate request"),
        GW_PASSWORD_ENTER_CFM((short) 0x3001, "Acknowledge to GW_PASSWORD_ENTER_REQ"),
        GW_PASSWORD_CHANGE_REQ((short) 0x3002, "Request password change."),
        GW_PASSWORD_CHANGE_CFM((short) 0x3003, "Acknowledge to GW_PASSWORD_CHANGE_REQ."),
        GW_PASSWORD_CHANGE_NTF((short) 0x3004,
                "Acknowledge to GW_PASSWORD_CHANGE_REQ. Broadcasted to all connected clients."),

        ;

        // Class internal

        private CommandNumber command;
        private String description;

        // Reverse-lookup map for getting a Command from an TypeId
        private static final Map<Short, Command> LOOKUPTYPEID2ENUM = Stream.of(Command.values())
                .collect(Collectors.toMap(Command::getShort, Function.identity()));

        // Constructor

        private Command(short typeId, String description) {
            this.command = new CommandNumber(typeId);
            this.description = description;
        }

        // Class access methods

        public CommandNumber getCommand() {
            return command;
        }

        public short getShort() {
            return command.toShort();
        }

        public String getDescription() {
            return description;
        }

        public static Command get(short thisTypeId) {
            if (LOOKUPTYPEID2ENUM.containsKey(thisTypeId)) {
                return LOOKUPTYPEID2ENUM.get(thisTypeId);
            } else {
                return Command.UNDEFTYPE;
            }
        }

    }

    /**
     * Enumeration of complete API as definition of a List of ErrorNumber parameters.
     * <P>
     * See <a href=
     * "https://velcdn.azureedge.net/~/media/com/api/klf200/technical%20specification%20for%20klf%20200%20api.pdf#page=24">
     * Table 42 - StatusID parameter description.</a>
     * <P>
     * Methods available:
     * <UL>
     * <LI>Constructor {@link ErrorNumber} by String.</LI>
     * <LI>Method {@link getValue} to return a value of this status.</LI>
     * <LI>Method {@link getDescription} to return a description as String.</LI>
     * <LI>Method {@link get} to return a {@link ErrorNumber} based on the given <B>short</B>.</LI>
     * </UL>
     */
    public enum ErrorNumber {
        // Special item: unrecognized ErrorNumber
        UNDEFTYPE((short) -1, "Unknown ErrorNumber."),
        ERROR_UNSPECIFIED((short) 0, "Not further defined error."),
        ERROR_UNACCEPTED((short) 1, "Unknown Command or command is not accepted at this state."),
        ERROR_STRUCTURE((short) 2, "ERROR on Frame Structure."),
        ERROR_BUSY((short) 7, "Busy. Try again later."),
        ERROR_BADINDEX((short) 8, "Bad system table index."),
        ERROR_UNAUTH((short) 12, "Not authenticated."),;

        // Class internal

        private short errorValue;
        private String description;

        // Reverse-lookup map for getting a Command from an TypeId
        private static final Map<Short, ErrorNumber> LOOKUPTYPEID2ENUM = Stream.of(ErrorNumber.values())
                .collect(Collectors.toMap(ErrorNumber::getValue, Function.identity()));

        // Constructor

        private ErrorNumber(short typeId, String description) {
            this.errorValue = typeId;
            this.description = description;
        }

        // Class access methods

        public short getValue() {
            return errorValue;
        }

        public String getDescription() {
            return description;
        }

        public static ErrorNumber get(short thisTypeId) {
            if (LOOKUPTYPEID2ENUM.containsKey(thisTypeId)) {
                return LOOKUPTYPEID2ENUM.get(thisTypeId);
            } else {
                return ErrorNumber.UNDEFTYPE;
            }
        }

    }

    /**
     * Enumeration of complete API as definition of a List of StatusID parameters.
     * <P>
     * See <a href=
     * "https://velcdn.azureedge.net/~/media/com/api/klf200/technical%20specification%20for%20klf%20200%20api.pdf#page=66">
     * Table 172 - StatusID parameter description.</a>
     * <P>
     * Methods available:
     * <UL>
     * <LI>Constructor {@link StatusID} by String.</LI>
     * <LI>Method {@link getValue} to return a value of this status.</LI>
     * <LI>Method {@link getDescription} to return a description as String.</LI>
     * <LI>Method {@link get} to return a {@link StatusID} based on the given <B>short</B>.</LI>
     * </UL>
     */
    public enum StatusID {
        // Special item: unrecognized StatusID
        UNDEFTYPE((short) -1, "Unknown StatusID."),
        STATUS_USER((short) 0x01, "The status is from a user activation."),
        STATUS_RAIN((short) 0x02, "The status is from a rain sensor activation."),
        STATUS_TIMER((short) 0x03, "The status is from a timer generated action."),
        STATUS_UPS((short) 0x05, "The status is from a UPS generated action."),
        STATUS_PROGRAM((short) 0x08, "The status is from an automatic program generated action. (SAAC)"),
        STATUS_WIND((short) 0x09, "The status is from a Wind sensor generated action."),
        STATUS_MYSELF((short) 0x0A, "The status is from an actuator generated action."),
        STATUS_AUTOMATIC_CYCLE((short) 0x0B, "The status is from a automatic cycle generated action."),
        STATUS_EMERGENCY((short) 0x0C, "The status is from an emergency or a security generated action."),
        STATUS_UNKNOWN((short) 0xFF, "The status is from an unknown command originator action."),;

        // Class internal

        private short statusIDValue;
        private String description;

        // Reverse-lookup map for getting a Command from an TypeId
        private static final Map<Short, StatusID> LOOKUPTYPEID2ENUM = Stream.of(StatusID.values())
                .collect(Collectors.toMap(StatusID::getValue, Function.identity()));

        // Constructor

        private StatusID(short typeId, String description) {
            this.statusIDValue = typeId;
            this.description = description;
        }

        // Class access methods

        public short getValue() {
            return statusIDValue;
        }

        public String getDescription() {
            return description;
        }

        public static StatusID get(short thisTypeId) {
            if (LOOKUPTYPEID2ENUM.containsKey(thisTypeId)) {
                return LOOKUPTYPEID2ENUM.get(thisTypeId);
            } else {
                return StatusID.UNDEFTYPE;
            }
        }

    }

    /**
     * Enumeration of complete API as definition of a List of RunStatus parameters.
     * <P>
     * See <a href=
     * "https://velcdn.azureedge.net/~/media/com/api/klf200/technical%20specification%20for%20klf%20200%20api.pdf#page=67">
     * Table 174 - StatusReply parameter description.</a>
     * <P>
     * Methods available:
     * <UL>
     * <LI>Constructor {@link RunStatus} by String.</LI>
     * <LI>Method {@link getValue} to return a value of this status.</LI>
     * <LI>Method {@link getDescription} to return a description as String.</LI>
     * <LI>Method {@link get} to return a {@link RunStatus} based on the given <B>short</B>.</LI>
     * </UL>
     */
    public enum RunStatus {
        // Special item: unrecognized RunStatus
        UNDEFTYPE((short) -1, "Unknown RunStatus."),
        EXECUTION_COMPLETED((short) 0, "Execution is completed with no errors."),
        EXECUTION_FAILED((short) 1, "Execution has failed (Specifics revealed in StatusReply)."),
        EXECUTION_ACTIVE((short) 2, "Execution is still active."),;

        // Class internal

        private short runStatusValue;
        private String description;

        // Reverse-lookup map for getting a Command from an TypeId
        private static final Map<Short, RunStatus> LOOKUPTYPEID2ENUM = Stream.of(RunStatus.values())
                .collect(Collectors.toMap(RunStatus::getValue, Function.identity()));

        // Constructor

        private RunStatus(short typeId, String description) {
            this.runStatusValue = typeId;
            this.description = description;
        }

        // Class access methods

        public short getValue() {
            return runStatusValue;
        }

        public String getDescription() {
            return description;
        }

        public static RunStatus get(short thisTypeId) {
            if (LOOKUPTYPEID2ENUM.containsKey(thisTypeId)) {
                return LOOKUPTYPEID2ENUM.get(thisTypeId);
            } else {
                return RunStatus.UNDEFTYPE;
            }
        }

    }

    /**
     * Enumeration of complete API as definition of a List of StatusReply parameters.
     * <P>
     * See <a href=
     * "https://velcdn.azureedge.net/~/media/com/api/klf200/technical%20specification%20for%20klf%20200%20api.pdf#page=67">
     * Table 175 - StatusReply parameter description.</a>
     * <P>
     * Methods available:
     * <UL>
     * <LI>Constructor {@link StatusReply} by String.</LI>
     * <LI>Method {@link getShort} to return a value of this status.</LI>
     * <LI>Method {@link getDescription} to return a description as String.</LI>
     * <LI>Method {@link get} to return a {@link StatusReply} based on the given <B>short</B>.</LI>
     * </UL>
     */
    public enum StatusReply {
        // Special item: unrecognized command
        UNDEFTYPE((short) -1, "Unknown command."),
        UNKNOWN_STATUS_REPLY((short) 0x00, "Used to indicate unknown reply."),
        COMMAND_COMPLETED_OK((short) 0x01, "Indicates no errors detected."),
        NO_CONTACT((short) 0x02, "Indicates no communication to node."),
        MANUALLY_OPERATED((short) 0x03, "Indicates manually operated by a user."),
        BLOCKED((short) 0x04, "Indicates node has been blocked by an object."),
        WRONG_SYSTEMKEY((short) 0x05, "Indicates the node contains a wrong system key."),
        PRIORITY_LEVEL_LOCKED((short) 0x06, "Indicates the node is locked on this priority level."),
        REACHED_WRONG_POSITION((short) 0x07, "Indicates node has stopped in another position than expected."),
        ERROR_DURING_EXECUTION((short) 0x08, "Indicates an error has occurred during execution of command."),
        NO_EXECUTION((short) 0x09, "Indicates no movement of the node parameter."),
        CALIBRATING((short) 0x0A, "Indicates the node is calibrating the parameters."),
        POWER_CONSUMPTION_TOO_HIGH((short) 0x0B, "Indicates the node power consumption is too high."),
        POWER_CONSUMPTION_TOO_LOW((short) 0x0C, "Indicates the node power consumption is too low."),
        LOCK_POSITION_OPEN((short) 0x0D, "Indicates door lock errors. (Door open during lock command)"),
        MOTION_TIME_TOO_LONG__COMMUNICATION_ENDED((short) 0x0E, "Indicates the target was not reached in time."),
        THERMAL_PROTECTION((short) 0x0F, "Indicates the node has gone into thermal protection mode."),
        PRODUCT_NOT_OPERATIONAL((short) 0x10, "Indicates the node is not currently operational."),
        FILTER_MAINTENANCE_NEEDED((short) 0x11, "Indicates the filter needs maintenance."),
        BATTERY_LEVEL((short) 0x12, "Indicates the battery level is low."),
        TARGET_MODIFIED((short) 0x13, "Indicates the node has modified the target value of the command."),
        MODE_NOT_IMPLEMENTED((short) 0x14, "Indicates this node does not support the mode received."),
        COMMAND_INCOMPATIBLE_TO_MOVEMENT((short) 0x15, "Indicates the node is unable to move in the right direction."),
        USER_ACTION((short) 0x16, "Indicates dead bolt is manually locked during unlock command."),
        DEAD_BOLT_ERROR((short) 0x17, "Indicates dead bolt error."),
        AUTOMATIC_CYCLE_ENGAGED((short) 0x18, "Indicates the node has gone into automatic cycle mode."),
        WRONG_LOAD_CONNECTED((short) 0x19, "Indicates wrong load on node."),
        COLOUR_NOT_REACHABLE((short) 0x1A, "Indicates that node is unable to reach received colour code."),
        TARGET_NOT_REACHABLE((short) 0x1B, "Indicates the node is unable to reach received target position."),
        BAD_INDEX_RECEIVED((short) 0x1C, "Indicates io-protocol has received an invalid index."),
        COMMAND_OVERRULED((short) 0x1D, "Indicates that the command was overruled by a new command."),
        NODE_WAITING_FOR_POWER((short) 0x1E, "Indicates that the node reported waiting for power."),
        INFORMATION_CODE((short) 0xDF, "Indicates an unknown error code received. (Hex code is shown on display)"),
        PARAMETER_LIMITED((short) 0xE0,
                "Indicates the parameter was limited by an unknown device. (Same as LIMITATION_BY_UNKNOWN_DEVICE)"),
        LIMITATION_BY_LOCAL_USER((short) 0xE1, "Indicates the parameter was limited by local button."),
        LIMITATION_BY_USER((short) 0xE2, "Indicates the parameter was limited by a remote control."),
        LIMITATION_BY_RAIN((short) 0xE3, "Indicates the parameter was limited by a rain sensor."),
        LIMITATION_BY_TIMER((short) 0xE4, "Indicates the parameter was limited by a timer."),
        LIMITATION_BY_UPS((short) 0xE6, "Indicates the parameter was limited by a power supply."),
        LIMITATION_BY_UNKNOWN_DEVICE((short) 0xE7,
                "Indicates the parameter was limited by an unknown device. (Same as PARAMETER_LIMITED)"),
        LIMITATION_BY_SAAC((short) 0xEA, "Indicates the parameter was limited by a standalone automatic controller."),
        LIMITATION_BY_WIND((short) 0xEB, "Indicates the parameter was limited by a wind sensor."),
        LIMITATION_BY_MYSELF((short) 0xEC, "Indicates the parameter was limited by the node itself."),
        LIMITATION_BY_AUTOMATIC_CYCLE((short) 0xED, "Indicates the parameter was limited by an automatic cycle."),
        LIMITATION_BY_EMERGENCY((short) 0xEE, "Indicates the parameter was limited by an emergency."),;

        // Class internal

        private short statusReplyValue;
        private String description;

        // Reverse-lookup map for getting a Command from an TypeId
        private static final Map<Short, StatusReply> LOOKUPTYPEID2ENUM = Stream.of(StatusReply.values())
                .collect(Collectors.toMap(StatusReply::getValue, Function.identity()));

        // Constructor

        private StatusReply(short typeId, String description) {
            this.statusReplyValue = typeId;
            this.description = description;
        }

        // Class access methods

        public short getValue() {
            return statusReplyValue;
        }

        public String getDescription() {
            return description;
        }

        public static StatusReply get(short thisTypeId) {
            if (LOOKUPTYPEID2ENUM.containsKey(thisTypeId)) {
                return LOOKUPTYPEID2ENUM.get(thisTypeId);
            } else {
                return StatusReply.UNDEFTYPE;
            }
        }

    }

}
