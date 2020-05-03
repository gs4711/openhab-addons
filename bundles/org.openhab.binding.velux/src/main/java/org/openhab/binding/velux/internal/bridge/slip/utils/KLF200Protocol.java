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
package org.openhab.binding.velux.internal.bridge.slip.utils;

import java.text.ParseException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;

/**
 * Utility class for handling of KLF200 state machine, based on request and response messages.
 *
 * <P>
 * Methods available:
 * <P>
 * Static methods are:
 * <UL>
 * <LI>{@link toString} for printing details of a message either request or response.</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public class KLF200Protocol implements Protocol {

    @Override
    public String toString(byte[] message) {
        // Transform transfer encoded byte array into command and data part
        byte[] response = new byte[0];
        try {
            response = new SlipRFC1055().decode(message);
        } catch (ParseException ef) {
            return new String("*** method SlipRFC1055() raised a decoding error for message: ")
                    .concat(message.toString());
        }
        SlipEncoding tr = new SlipEncoding(response);
        if (!tr.isValid()) {
            return new String("*** method SlipEncoding() raised a decoding error for message: ")
                    .concat(response.toString());
        }
        short responseCommand = tr.getCommand();
        byte[] responseData = tr.getData();
        Packet responseData2 = new Packet(responseData);
        // Extract common information
        String nodeId = "";
        String sessionId = "";
        switch (Command.get(responseCommand)) {
            case GW_SET_NODE_VARIATION_REQ:
            case GW_SET_NODE_NAME_REQ:
            case GW_GET_NODE_INFORMATION_REQ:
            case GW_GET_NODE_INFORMATION_NTF:
            case GW_NODE_INFORMATION_CHANGED_NTF:
            case GW_NODE_STATE_POSITION_CHANGED_NTF:
            case GW_SET_NODE_ORDER_AND_PLACEMENT_REQ:
            case GW_GET_ALL_NODES_INFORMATION_NTF:
                int cfmNodeID = -1;
                if (responseData2.length() > 0) {
                    cfmNodeID = responseData2.getOneByteValue(0);
                }
                nodeId = String.format("nodeId=%d,", cfmNodeID);
                break;
            case GW_SET_NODE_VARIATION_CFM:
            case GW_SET_NODE_NAME_CFM:
            case GW_GET_NODE_INFORMATION_CFM:
            case GW_SET_NODE_ORDER_AND_PLACEMENT_CFM:
                cfmNodeID = -1;
                if (responseData2.length() > 1) {
                    cfmNodeID = responseData2.getOneByteValue(1);
                }
                nodeId = String.format("nodeId=%d,", cfmNodeID);
                break;
            case GW_LIMITATION_STATUS_NTF:
                cfmNodeID = -1;
                if (responseData2.length() > 2) {
                    cfmNodeID = responseData2.getOneByteValue(2);
                }
                nodeId = String.format("nodeId=%d,", cfmNodeID);
                break;
            default:
        }
        switch (Command.get(responseCommand)) {
            case GW_COMMAND_SEND_REQ:
            case GW_COMMAND_SEND_CFM:
            case GW_COMMAND_RUN_STATUS_NTF:
            case GW_COMMAND_REMAINING_TIME_NTF:
            case GW_SESSION_FINISHED_NTF:
            case GW_ACTIVATE_PRODUCTGROUP_REQ:
            case GW_ACTIVATE_PRODUCTGROUP_CFM:
            case GW_WINK_SEND_REQ:
            case GW_WINK_SEND_CFM:
            case GW_MODE_SEND_REQ:
            case GW_MODE_SEND_CFM:
            case GW_STOP_SCENE_REQ:
            case GW_STOP_SCENE_CFM:
            case GW_GET_LIMITATION_STATUS_REQ:
            case GW_GET_LIMITATION_STATUS_CFM:
                int ntfSessionID = -1;
                if (responseData2.length() > 1) {
                    ntfSessionID = responseData2.getTwoByteValue(0);
                }
                sessionId = String.format("sessionid=%d,", ntfSessionID);
                break;
            default:
        }
        return String.format("%s (%s%slength=%s): %s.", Command.get(responseCommand).toString(), nodeId, sessionId,
                responseData.length, message);
    }

}
