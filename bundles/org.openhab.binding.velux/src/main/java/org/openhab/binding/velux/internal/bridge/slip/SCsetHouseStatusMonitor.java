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
import org.openhab.binding.velux.internal.bridge.common.SetHouseStatusMonitor;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Handshake;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Modify HouseStatusMonitor</B>
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
 * <LI>{@link #serviceActivation} to define the new service activation state.</LI>
 * </UL>
 *
 * @see SetHouseStatusMonitor
 * @see SlipBridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SCsetHouseStatusMonitor extends SetHouseStatusMonitor implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCsetHouseStatusMonitor.class);

    private static final String DESCRIPTION = "Modify HouseStatusMonitor";

    /*
     * ===========================================================
     * Constant Objects
     */

    private static final Map<KLF200Handshake.State, Set<Command>> STATEMACHINE;
    static {
        STATEMACHINE = new HashMap<KLF200Handshake.State, Set<Command>>();
        STATEMACHINE.put(KLF200Handshake.State.IDLE, KLF200Handshake.build());
        STATEMACHINE.put(KLF200Handshake.State.WAIT4CONFIRMATION, KLF200Handshake
                .build(Command.GW_HOUSE_STATUS_MONITOR_ENABLE_CFM, Command.GW_HOUSE_STATUS_MONITOR_DISABLE_CFM));
    }

    /*
     * ===========================================================
     * Message Content Parameters
     */

    private boolean activateService = false;

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
        Command command = activateService ? Command.GW_HOUSE_STATUS_MONITOR_ENABLE_REQ
                : Command.GW_HOUSE_STATUS_MONITOR_DISABLE_REQ;
        setCommunicationUnfinishedAndUnsuccessful();
        currentState = KLF200Handshake.State.WAIT4CONFIRMATION;
        KLF200Response.requestLogging(logger, command);
        return command.getCommand();
    }

    @Override
    public byte[] getRequestDataAsArrayOfBytes() {
        logger.debug("getRequestDataAsArrayOfBytes() data is {}.", new Packet(requestData).toString());
        return requestData;
    }

    @Override
    public boolean setResponse(short responseCommand, byte[] thisResponseData) {
        KLF200Response.introLogging(logger, responseCommand, thisResponseData);
        setCommunicationUnfinishedAndUnsuccessful();
        if (!KLF200Response.isExpectedAnswer(logger, STATEMACHINE, currentState, responseCommand)) {
            return false;
        }
        switch (Command.get(responseCommand)) {
            case GW_HOUSE_STATUS_MONITOR_ENABLE_CFM:
                logger.trace("setResponse(): service enable confirmed by bridge.");
                setCommunicationSuccessfullyFinished();

                break;
            case GW_HOUSE_STATUS_MONITOR_DISABLE_CFM:
                logger.trace("setResponse(): service disable confirmed by bridge.");
                setCommunicationSuccessfullyFinished();
                break;

            default:
                KLF200Response.errorLogging(logger, responseCommand);
        }
        currentState = KLF200Handshake.State.IDLE;
        KLF200Response.outroLogging(logger, isCommunicationSuccessful, isHandshakeFinished);
        return true;
    }

    /*
     * ===========================================================
     * Methods in addition to the interface {@link BridgeCommunicationProtocol}
     * and the abstract class {@link SetHouseStatusMonitor}
     */

    @Override
    public SetHouseStatusMonitor serviceActivation(boolean enableService) {
        this.activateService = enableService;
        return this;
    }

}
