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
import org.openhab.binding.velux.internal.bridge.common.GetLANConfig;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Handshake;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxGwLAN;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Retrieve LAN configuration</B>
 * <P>
 * Common Message semantic: Communication with the bridge and (optionally) storing returned information within the class
 * itself.
 * <P>
 * As 3rd level class it defines informations how to send query and receive answer through the
 * {@link org.openhab.binding.velux.internal.bridge.VeluxBridgeProvider VeluxBridgeProvider}
 * as described by the {@link org.openhab.binding.velux.internal.bridge.slip.SlipBridgeCommunicationProtocol
 * SlipBridgeCommunicationProtocol}.
 * <P>
 * Methods in addition to the mentioned interface:
 * <UL>
 * <LI>{@link #getLANConfig} to retrieve the current LAN configuration.</LI>
 * </UL>
 *
 * @see GetLANConfig
 * @see SlipBridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SCgetLANConfig extends GetLANConfig implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCgetLANConfig.class);

    private static final String DESCRIPTION = "Retrieve LAN configuration";
    private static final Command COMMAND = Command.GW_GET_NETWORK_SETUP_REQ;

    /*
     * ===========================================================
     * Constant Objects
     */

    private static final Map<KLF200Handshake.State, Set<Command>> STATEMACHINE;
    static {
        STATEMACHINE = new HashMap<KLF200Handshake.State, Set<Command>>();
        STATEMACHINE.put(KLF200Handshake.State.WAIT4CONFIRMATION,
                KLF200Handshake.build(Command.GW_GET_NETWORK_SETUP_CFM));
    }

    /*
     * ===========================================================
     * Message Content Parameters
     */

    private int cfmIpAddress;
    private int cfmMask;
    private int cfmDefGW;
    private boolean cfmDHCP;

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
        requestData = new byte[1];
        return requestData;
    }

    @Override
    public boolean setResponse(short responseCommand, byte[] thisResponseData) {
        KLF200Response.introLogging(logger, responseCommand, thisResponseData);
        setCommunicationUnfinishedAndUnsuccessful();
        if (!KLF200Response.isExpectedAnswer(logger, STATEMACHINE, currentState, responseCommand)) {
            return false;
        }
        Packet responseData = new Packet(thisResponseData);
        switch (Command.get(responseCommand)) {
            case GW_GET_NETWORK_SETUP_CFM:
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 13)) {
                    setCommunicationUnsuccessfullyFinished();
                    break;
                }
                cfmIpAddress = responseData.getFourByteValue(0);
                cfmMask = responseData.getFourByteValue(4);
                cfmDefGW = responseData.getFourByteValue(8);
                cfmDHCP = responseData.getOneByteValue(12) == 0 ? false : true;
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
    public VeluxGwLAN getLANConfig() {
        logger.trace("getLANConfig() called.");
        VeluxGwLAN result = new VeluxGwLAN(Packet.intToIPAddressString(cfmIpAddress),
                Packet.intToIPAddressString(cfmMask), Packet.intToIPAddressString(cfmDefGW), cfmDHCP);
        logger.debug("getLANConfig() returns {}.", result);
        return result;
    }

}
