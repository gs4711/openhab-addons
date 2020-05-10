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
import org.openhab.binding.velux.internal.bridge.common.GetScenes;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Handshake;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.openhab.binding.velux.internal.things.VeluxProductState;
import org.openhab.binding.velux.internal.things.VeluxScene;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Retrieve Scenes</B>
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
 * <LI>{@link #getScenes()} to retrieve the set of current scenes.</LI>
 * </UL>
 *
 * @see GetScenes
 * @see SlipBridgeCommunicationProtocol
 *
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SCgetScenes extends GetScenes implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCgetScenes.class);

    private static final String DESCRIPTION = "Retrieve Scenes";
    private static final Command COMMAND = Command.GW_GET_SCENE_LIST_REQ;

    /*
     * ===========================================================
     * Constant Objects
     */

    private static final Map<KLF200Handshake.State, Set<Command>> STATEMACHINE;
    static {
        STATEMACHINE = new HashMap<KLF200Handshake.State, Set<Command>>();
        STATEMACHINE.put(KLF200Handshake.State.IDLE, KLF200Handshake.build());
        STATEMACHINE.put(KLF200Handshake.State.WAIT4CONFIRMATION, KLF200Handshake.build(Command.GW_GET_SCENE_LIST_CFM));
        STATEMACHINE.put(KLF200Handshake.State.WAIT4NOTIFICATION, KLF200Handshake.build(Command.GW_GET_SCENE_LIST_NTF));
    }

    /*
     * Message Objects
     */

    private KLF200Handshake.State currentState = KLF200Handshake.State.IDLE;

    private VeluxScene[] scenes = new VeluxScene[0];
    private int totalNumberOfScenes;
    private int nextSceneArrayItem;

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
        return EMPTYDATA;
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
            case GW_GET_SCENE_LIST_CFM:
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 1)) {
                    setCommunicationUnsuccessfullyFinished();
                    break;
                }
                int ntfTotalNumberOfObjects = responseData.getOneByteValue(0);
                scenes = new VeluxScene[ntfTotalNumberOfObjects];
                if (ntfTotalNumberOfObjects == 0) {
                    logger.trace("setResponse(): no scenes defined.");
                    setCommunicationSuccessfullyFinished();
                }
                logger.trace("setResponse(): {} scenes defined.", ntfTotalNumberOfObjects);
                currentState = KLF200Handshake.State.WAIT4NOTIFICATION;
                nextSceneArrayItem = 0;
                totalNumberOfScenes = ntfTotalNumberOfObjects;
                break;

            case GW_GET_SCENE_LIST_NTF:
                if (thisResponseData.length < 1) {
                    logger.trace("setResponse(): malformed response packet (length is {} less than one).",
                            thisResponseData.length);
                    setCommunicationUnsuccessfullyFinished();
                    break;
                }
                int ntfNumberOfObject = responseData.getOneByteValue(0);
                logger.trace("setResponse(): NTF number of objects={}.", ntfNumberOfObject);
                if (ntfNumberOfObject == 0) {
                    logger.trace("setResponse(): finished.");
                    setCommunicationSuccessfullyFinished();
                }
                if (thisResponseData.length != (2 + 65 * ntfNumberOfObject)) {
                    logger.trace("setResponse(): malformed response packet (real length {}, expected length {}).",
                            thisResponseData.length, (2 + 65 * ntfNumberOfObject));
                    setCommunicationUnsuccessfullyFinished();
                    break;
                }
                for (int objectIndex = 0; objectIndex < ntfNumberOfObject; objectIndex++) {
                    int ntfSceneID = responseData.getOneByteValue(1 + 65 * objectIndex);
                    int beginOfString = 2 + 65 * objectIndex;
                    String ntfSceneName = responseData.getString(beginOfString, 64);
                    logger.trace("setResponse(): scene {}, name {}.", ntfSceneID, ntfSceneName);
                    if (nextSceneArrayItem < totalNumberOfScenes) {
                        scenes[nextSceneArrayItem++] = new VeluxScene(ntfSceneName, ntfSceneID, false,
                                new VeluxProductState[0]);
                    } else {
                        logger.warn("setResponse(): expected {} scenes, received one more, ignoring it.",
                                totalNumberOfScenes);
                    }
                }
                int ntfRemainingNumberOfObject = responseData.getOneByteValue(1 + 65 * ntfNumberOfObject);
                logger.trace("setResponse(): {} scenes remaining.", ntfRemainingNumberOfObject);
                if (ntfRemainingNumberOfObject == 0) {
                    logger.trace("setResponse(): finished.");
                    setCommunicationSuccessfullyFinished();
                }
                break;
            default:
                KLF200Response.errorLogging(logger, responseCommand);
                setCommunicationUnsuccessfullyFinished();
        }
        KLF200Response.outroLogging(logger, isCommunicationSuccessful, isHandshakeFinished);
        return true;
    }

    /**
     * ===========================================================
     * <P>
     * Public Methods required for abstract class {@link GetScenes}.
     */
    @Override
    public VeluxScene[] getScenes() {
        logger.trace("getScenes(): returning {} scenes.", scenes.length);
        return scenes;
    }

}
