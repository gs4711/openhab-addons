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
package org.openhab.binding.velux.internal.bridge.slip;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.bridge.common.BridgeCommunicationProtocol;
import org.openhab.binding.velux.internal.bridge.common.GetHouseStatus;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Handshake;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Retrieve House Status</B>
 * <P>
 * Common Message semantic: Communication from the bridge and storing returned information within the class itself.
 * <P>
 * As 3rd level class it defines informations how to receive answer through the
 * {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider VeluxBridgeProvider}
 * as described by the interface {@link org.openhab.binding.velux.internal.bridge.slip.SlipBridgeCommunicationProtocol
 * SlipBridgeCommunicationProtocol}.
 * <P>
 * Methods in addition to the mentioned interface:
 * <UL>
 * <LI>{@link #getNtfNodeID} to retrieve the node identifier which has been changed.</LI>
 * <LI>{@link #getNtfState} to retrieve the state of the node which has been changed.</LI>
 * <LI>{@link #getNtfCurrentPosition} to retrieve the actual position of this node.</LI>
 * <LI>{@link #getNtfTarget} to retrieve the target position of this node.</LI>
 * </UL>
 * <P>
 * NOTE: the class does NOT define a request as it only works as receiver.
 *
 * @see BridgeCommunicationProtocol
 * @see SlipBridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SCgetHouseStatus extends GetHouseStatus implements BridgeCommunicationProtocol, SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCgetHouseStatus.class);

    private static final String DESCRIPTION = "Retrieve House Status";
    private static final Command COMMAND = Command.GW_OPENHAB_RECEIVEONLY;

    /*
     * ===========================================================
     * Constant Objects
     */

    private static final Map<KLF200Handshake.State, Set<Command>> STATEMACHINE;
    static {
        STATEMACHINE = new HashMap<KLF200Handshake.State, Set<Command>>();
        STATEMACHINE.put(KLF200Handshake.State.WAIT4NOTIFICATION,
                KLF200Handshake.build(Command.GW_NODE_STATE_POSITION_CHANGED_NTF));
    }
    /*
     * ===========================================================
     * Message Objects
     */

    @SuppressWarnings("unused")
    private byte[] requestData = new byte[0];

    /*
     * ===========================================================
     * Result Objects
     */

    private KLF200Handshake.State currentState = KLF200Handshake.State.WAIT4NOTIFICATION;

    private int ntfNodeID;
    private int ntfState;
    private int ntfCurrentPosition;
    private int ntfTarget;

    /*
     * ===========================================================
     * Methods required for interface {@link SlipBridgeCommunicationProtocol}.
     */

    @Override
    public String name() {
        return DESCRIPTION;
    }

    @Override
    public CommandNumber getRequestCommand() {
        setCommunicationUnfinishedAndUnsuccessful();
        currentState = KLF200Handshake.State.WAIT4NOTIFICATION;
        KLF200Response.requestLogging(logger, COMMAND);
        return COMMAND.getCommand();
    }

    @Override
    public byte[] getRequestDataAsArrayOfBytes() {
        return new byte[0];
    }

    @Override
    public boolean setResponse(short responseCommand, byte[] thisResponseData, boolean isSequentialEnforced) {
        KLF200Response.introLogging(logger, responseCommand, thisResponseData);
        setCommunicationUnfinishedAndUnsuccessful();
        if (!KLF200Response.isExpectedAnswer(logger, STATEMACHINE, currentState, responseCommand)) {
            return false;
        }
        Packet responseData = new Packet(thisResponseData);
        switch (Command.get(responseCommand)) {
            case GW_NODE_STATE_POSITION_CHANGED_NTF:
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 20)) {
                    setCommunicationUnfinishedAndUnsuccessful();
                    break;
                }
                ntfNodeID = responseData.getOneByteValue(0);
                ntfState = responseData.getOneByteValue(1);
                ntfCurrentPosition = responseData.getTwoByteValue(2);
                ntfTarget = responseData.getTwoByteValue(4);
                int ntfFP1CurrentPosition = responseData.getTwoByteValue(6);
                int ntfFP2CurrentPosition = responseData.getTwoByteValue(8);
                int ntfFP3CurrentPosition = responseData.getTwoByteValue(10);
                int ntfFP4CurrentPosition = responseData.getTwoByteValue(12);
                int ntfRemainingTime = responseData.getTwoByteValue(14);
                int ntfTimeStamp = responseData.getFourByteValue(16);
                // Extracting information items
                logger.trace("setResponse(): ntfNodeID={}.", ntfNodeID);
                logger.trace("setResponse(): ntfState={}.", ntfState);
                logger.trace("setResponse(): ntfCurrentPosition={}.", ntfCurrentPosition);
                logger.trace("setResponse(): ntfFP1CurrentPosition={}.", ntfFP1CurrentPosition);
                logger.trace("setResponse(): ntfFP2CurrentPosition={}.", ntfFP2CurrentPosition);
                logger.trace("setResponse(): ntfFP3CurrentPosition={}.", ntfFP3CurrentPosition);
                logger.trace("setResponse(): ntfFP4CurrentPosition={}.", ntfFP4CurrentPosition);
                logger.trace("setResponse(): ntfTarget={}.", ntfTarget);
                logger.trace("setResponse(): ntfRemainingTime={}.", ntfRemainingTime);
                logger.trace("setResponse(): ntfTimeStamp={}.", ntfTimeStamp);
                setCommunicationSuccessfullyFinished();
                break;

            default:
                KLF200Response.errorLogging(logger, responseCommand);
                setCommunicationUnfinishedAndUnsuccessful();
        }
        KLF200Response.outroLogging(logger, isCommunicationSuccessful, isHandshakeFinished);
        return true;
    }

    /*
     * ===========================================================
     * Methods in addition to the interface {@link BridgeCommunicationProtocol}
     */

    /**
     * @return <b>ntfNodeID</b> returns the Actuator Id as int.
     */
    public int getNtfNodeID() {
        return ntfNodeID;
    }

    /**
     * @return <b>ntfState</b> returns the state of the Actuator as int.
     */
    public int getNtfState() {
        return ntfState;
    }

    /**
     * @return <b>ntfCurrentPosition</b> returns the current position of the Actuator as int.
     */
    public int getNtfCurrentPosition() {
        return ntfCurrentPosition;
    }

    /**
     * @return <b>ntfTarget</b> returns the target position of the Actuator as int.
     */
    public int getNtfTarget() {
        return ntfTarget;
    }

}
