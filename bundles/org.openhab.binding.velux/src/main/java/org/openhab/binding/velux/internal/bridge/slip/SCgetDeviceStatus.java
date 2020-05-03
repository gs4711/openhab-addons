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
import org.openhab.binding.velux.internal.bridge.common.GetDeviceStatus;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Handshake;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxGwState;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Get Bridge Device Status</B>
 * <P>
 * Common Message semantic: Communication with the bridge and (optionally) storing returned information within the class
 * itself.
 * <P>
 * As 3rd level class it defines informations how to send query and receive answer through the
 * {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider VeluxBridgeProvider}
 * as described by the interface {@link org.openhab.binding.velux.internal.bridge.slip.SlipBridgeCommunicationProtocol
 * SlipBridgeCommunicationProtocol}.
 * <P>
 * Methods in addition to the mentioned interface:
 * <UL>
 * <LI>{@link #getState} to retrieve the Velux gateway status.</LI>
 * </UL>
 *
 * @see GetDeviceStatus
 * @see SlipBridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SCgetDeviceStatus extends GetDeviceStatus implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCgetDeviceStatus.class);

    private static final String DESCRIPTION = "Get Bridge Device Status";
    private static final Command COMMAND = Command.GW_GET_STATE_REQ;

    /*
     * ===========================================================
     * Constant Objects
     */

    private static final Map<KLF200Handshake.State, Set<Command>> STATEMACHINE;
    static {
        STATEMACHINE = new HashMap<KLF200Handshake.State, Set<Command>>();
        STATEMACHINE.put(KLF200Handshake.State.WAIT4CONFIRMATION, KLF200Handshake.build(Command.GW_GET_STATE_CFM));
    }

    /*
     * ===========================================================
     * Message Content Parameters
     */

    private int cfmGatewayState;
    private int cfmSubState;
    private int cfmStateData;

    /*
     * ===========================================================
     * Message Objects
     */

    private byte[] requestData = new byte[0];

    /*
     * ===========================================================
     * Result Objects
     */

    private KLF200Handshake.State currentState = KLF200Handshake.State.IDLE;

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
        currentState = KLF200Handshake.State.WAIT4CONFIRMATION;
        KLF200Response.requestLogging(logger, COMMAND);
        return COMMAND.getCommand();
    }

    @Override
    public byte[] getRequestDataAsArrayOfBytes() {
        logger.trace("getRequestDataAsArrayOfBytes() returns data.");
        requestData = new byte[0];
        return requestData;
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
            case GW_GET_STATE_CFM:
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 6)) {
                    setCommunicationUnsuccessfullyFinished();
                    break;
                }
                cfmGatewayState = responseData.getOneByteValue(0);
                cfmSubState = responseData.getOneByteValue(1);
                cfmStateData = responseData.getFourByteValue(2);
                logger.trace("setResponse(): cfmGatewayState={}.", cfmGatewayState);
                logger.trace("setResponse(): cfmSubState={}.", cfmSubState);
                logger.trace("setResponse(): cfmStateData={}.", cfmStateData);
                setCommunicationSuccessfullyFinished();
                break;

            default:
                KLF200Response.errorLogging(logger, responseCommand);
                setCommunicationUnsuccessfullyFinished();
        }
        KLF200Response.outroLogging(logger, isCommunicationSuccessful, isHandshakeFinished);
        return true;
    }

    /*
     * ===========================================================
     * Methods in addition to interface {@link BridgeCommunicationProtocol}.
     */

    @Override
    public VeluxGwState getState() {
        VeluxGwState thisGwState = new VeluxGwState((byte) cfmGatewayState, (byte) cfmSubState);
        logger.trace("getState() returns {} ({}).", thisGwState, thisGwState.toDescription());
        return thisGwState;
    }

}
