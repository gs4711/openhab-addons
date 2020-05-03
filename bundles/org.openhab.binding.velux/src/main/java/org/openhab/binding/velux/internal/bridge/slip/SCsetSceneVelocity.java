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
import org.openhab.binding.velux.internal.bridge.common.SetSceneVelocity;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Handshake;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.openhab.binding.velux.internal.things.VeluxProductVelocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Modify Velocity of an Actuator</B>
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
 * <LI>{@link #setMode} to define the new silence mode of the intended actuator.</LI>
 * </UL>
 *
 * @see SetSceneVelocity
 * @see SlipBridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
// ToDo: THIS MESSAGE EXCHANGE IS AN UNDOCUMENTED FEATURE. Check the updated Velux doc against this implementation.
@NonNullByDefault
class SCsetSceneVelocity extends SetSceneVelocity implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCsetSceneVelocity.class);

    private static final String DESCRIPTION = "Modify Velocity of an Actuator";
    private static final Command COMMAND = Command.GW_SET_NODE_VELOCITY_REQ;

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

    private int reqNodeID = -1;
    private int reqNodeVelocity = -1;

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
     * Methods required for interface {@link BridgeCommunicationProtocol}.
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
        Packet request = new Packet(new byte[2]);
        request.setOneByteValue(0, reqNodeID);
        request.setOneByteValue(1, reqNodeVelocity);
        requestData = request.toByteArray();
        logger.trace("getRequestDataAsArrayOfBytes() data is {}.", new Packet(requestData).toString());
        return requestData;
    }

    @Override
    public boolean setResponse(short responseCommand, byte[] thisResponseData, boolean isSequentialEnforced) {
        KLF200Response.introLogging(logger, responseCommand, thisResponseData);
        if (!KLF200Response.isExpectedAnswer(logger, STATEMACHINE, currentState, responseCommand)) {
            return false;
        }
        Packet responseData = new Packet(thisResponseData);
        switch (Command.get(responseCommand)) {
            case GW_SET_NODE_VELOCITY_CFM:
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 3)) {
                    setCommunicationUnsuccessfullyFinished();
                    break;
                }
                int cfmStatus = responseData.getOneByteValue(0);
                switch (cfmStatus) {
                    case 0:
                        logger.trace("setResponse(): returned status: Error - Wink is rejected.");
                        setCommunicationUnsuccessfullyFinished();
                        break;
                    case 1:
                        logger.trace("setResponse(): returned status: OK – Wink is accepted.");
                        setCommunicationSuccessfullyFinished();
                        break;
                    default:
                        logger.warn("setResponse({}): returned status={} (Reserved/unknown).",
                                Command.get(responseCommand).toString(), cfmStatus);
                        setCommunicationUnsuccessfullyFinished();
                        break;
                }
                break;

            default:
                setCommunicationUnsuccessfullyFinished();
                KLF200Response.errorLogging(logger, responseCommand);
        }
        KLF200Response.outroLogging(logger, isCommunicationSuccessful, isHandshakeFinished);
        return true;
    }

    /*
     * ===========================================================
     * Methods in addition to the interface {@link BridgeCommunicationProtocol}
     * and the abstract class {@link RunProductIdentification}
     */

    /**
     * Constructor Addon Method.
     * <P>
     * Passes the intended Actuator Identifier towards this class for building the request lateron.
     *
     * @param actuatorId as type int describing the scene to be processed.
     * @param silent as type boolean describing the silence mode of this node.
     * @return <b>this</b> of type {@link SCsetSceneVelocity} as class itself.
     */
    @Override
    public SCsetSceneVelocity setMode(int actuatorId, boolean silent) {
        logger.trace("setProductId({},{}) called.", actuatorId, silent);
        this.reqNodeID = actuatorId;
        this.reqNodeVelocity = silent ? VeluxProductVelocity.SILENT.getVelocity()
                : VeluxProductVelocity.FAST.getVelocity();
        return this;
    }

}
