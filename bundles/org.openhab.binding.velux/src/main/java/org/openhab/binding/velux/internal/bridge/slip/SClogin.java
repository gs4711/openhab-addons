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
import org.openhab.binding.velux.internal.VeluxBindingConstants;
import org.openhab.binding.velux.internal.bridge.common.Login;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Handshake;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Authenticate / login</B>
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
 * <LI>{@link #setPassword(String)} to define the authentication reqPassword to be used.</LI>
 * <LI>{@link #getAuthToken()} to retrieve the authentication reqPassword.</LI>
 * </UL>
 *
 * @see Login
 * @see SlipBridgeCommunicationProtocol
 *
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SClogin extends Login implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SClogin.class);

    private static final String DESCRIPTION = "Authenticate / login";
    private static final Command COMMAND = Command.GW_PASSWORD_ENTER_REQ;

    /*
     * ===========================================================
     * Constant Objects
     */

    private static final Map<KLF200Handshake.State, Set<Command>> STATEMACHINE;
    static {
        STATEMACHINE = new HashMap<KLF200Handshake.State, Set<Command>>();
        STATEMACHINE.put(KLF200Handshake.State.WAIT4CONFIRMATION, KLF200Handshake.build(Command.GW_PASSWORD_ENTER_CFM));
    }

    /*
     * ===========================================================
     * Message Content Parameters
     */

    private String reqPassword = "";

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
        requestData = new byte[32];
        byte[] password = reqPassword.getBytes();
        System.arraycopy(password, 0, requestData, 0, password.length);
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
            case GW_PASSWORD_ENTER_CFM:
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 1)) {
                    setCommunicationUnsuccessfullyFinished();
                    break;
                }
                int cfmStatus = responseData.getOneByteValue(0);
                switch (cfmStatus) {
                    case 0:
                        logger.info("{} bridge connection successfully established (login succeeded).",
                                VeluxBindingConstants.BINDING_ID);
                        logger.debug("setResponse(): returned status: The request was successful.");
                        setCommunicationSuccessfullyFinished();
                        break;
                    case 1:
                        logger.warn("{} bridge connection successfully established but login failed.",
                                VeluxBindingConstants.BINDING_ID);
                        logger.debug("setResponse(): returned status: The request failed.");
                        setCommunicationUnsuccessfullyFinished();
                        break;
                    default:
                        logger.warn("setResponse(): returned status={} (not defined).", cfmStatus);
                        setCommunicationUnsuccessfullyFinished();
                        break;
                }
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
    public void setPassword(String thisPassword) {
        logger.trace("setPassword({}) called.", thisPassword.replaceAll(".", "*"));
        reqPassword = thisPassword;
        return;
    }

    @Override
    public String getAuthToken() {
        logger.trace("getAuthToken() called, returning {}.", reqPassword.replaceAll(".", "*"));
        return reqPassword;
    }

}
