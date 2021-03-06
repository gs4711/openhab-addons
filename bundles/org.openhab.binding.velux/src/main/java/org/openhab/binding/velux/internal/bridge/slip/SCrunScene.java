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
import java.util.Random;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.bridge.common.RunScene;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Handshake;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Response;
import org.openhab.binding.velux.internal.bridge.slip.utils.Packet;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.Command;
import org.openhab.binding.velux.internal.things.VeluxKLFAPI.CommandNumber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Protocol specific bridge communication supported by the Velux bridge:
 * <B>Run Scene</B>
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
 * <LI>{@link #setSceneId} to define the scene to be executed.</LI>
 * </UL>
 *
 * @see RunScene
 * @see SlipBridgeCommunicationProtocol
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
class SCrunScene extends RunScene implements SlipBridgeCommunicationProtocol {
    private final Logger logger = LoggerFactory.getLogger(SCrunScene.class);

    private static final String DESCRIPTION = "Run Scene";
    private static final Command COMMAND = Command.GW_ACTIVATE_SCENE_REQ;

    /*
     * ===========================================================
     * Constant Objects
     */

    private static final Map<KLF200Handshake.State, Set<Command>> STATEMACHINE;
    static {
        STATEMACHINE = new HashMap<KLF200Handshake.State, Set<Command>>();
        STATEMACHINE.put(KLF200Handshake.State.IDLE, KLF200Handshake.build());
        STATEMACHINE.put(KLF200Handshake.State.WAIT4CONFIRMATION, KLF200Handshake.build(Command.GW_ACTIVATE_SCENE_CFM));
        STATEMACHINE.put(KLF200Handshake.State.WAIT4NOTIFICATION,
                KLF200Handshake.build(Command.GW_COMMAND_RUN_STATUS_NTF, Command.GW_COMMAND_REMAINING_TIME_NTF,
                        Command.GW_COMMAND_REMAINING_TIME_NTF));
        STATEMACHINE.put(KLF200Handshake.State.WAIT4FINISH, KLF200Handshake.build(Command.GW_SESSION_FINISHED_NTF));
    }

    /*
     * ===========================================================
     * Message Content Parameters
     */

    private int reqSessionID = 0;
    private int reqCommandOriginator = 8; // SAAC
    private int reqPriorityLevel = 5; // Comfort Level 2
    private int reqSceneID = -1; // SceneID as one unsigned byte number
    private int reqVelocity = 0; // The product group operates by its default velocity.

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
     * Constructor Method
     */

    /**
     * Constructor.
     * <P>
     * Initializes the session id {@link #reqSessionID} with a random start value.
     */
    public SCrunScene() {
        logger.debug("SCrunScene(Constructor) called.");
        Random rand = new Random();
        reqSessionID = rand.nextInt(0x0fff);
        logger.debug("SCrunScene(): starting sessions with the random number {}.", reqSessionID);
    }

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
        Packet request = new Packet(new byte[6]);
        reqSessionID = (reqSessionID + 1) & 0xffff;
        request.setTwoByteValue(0, reqSessionID);
        request.setOneByteValue(2, reqCommandOriginator);
        request.setOneByteValue(3, reqPriorityLevel);
        request.setOneByteValue(4, reqSceneID);
        request.setOneByteValue(5, reqVelocity);
        requestData = request.toByteArray();
        logger.trace("getRequestCommand(): SessionID={}.", reqSessionID);
        logger.trace("getRequestCommand(): CommandOriginator={}.", reqCommandOriginator);
        logger.trace("getRequestCommand(): PriorityLevel={}.", reqPriorityLevel);
        logger.trace("getRequestCommand(): SceneID={}.", reqSceneID);
        logger.trace("getRequestCommand(): Velocity={}.", reqVelocity);
        logger.debug("getRequestCommand() returns {} ({}) with  SceneID {}.", COMMAND.name(), COMMAND.getCommand(),
                reqSceneID);
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
            case GW_ACTIVATE_SCENE_CFM:
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 3)) {
                    setCommunicationUnsuccessfullyFinished();
                    break;
                }
                int cfmStatus = responseData.getOneByteValue(0);
                int cfmSessionID = responseData.getTwoByteValue(1);
                switch (cfmStatus) {
                    case 0:
                        logger.trace("setResponse(): returned status: OK - Request accepted.");
                        if (!KLF200Response.check4matchingSessionID(logger, cfmSessionID, reqSessionID)) {
                            return false;
                        }
                        break;
                    case 1:
                        setCommunicationUnsuccessfullyFinished();
                        logger.trace("setResponse(): returned status: Error – Invalid parameter.");
                        break;
                    case 2:
                        setCommunicationUnsuccessfullyFinished();
                        logger.trace("setResponse(): returned status: Error – Request rejected.");
                        break;
                    default:
                        setCommunicationUnsuccessfullyFinished();
                        logger.warn("setResponse({}): returned status={} (Reserved/unknown).",
                                Command.get(responseCommand).toString(), cfmStatus);
                        break;
                }
                currentState = KLF200Handshake.State.WAIT4NOTIFICATION;
                break;

            case GW_COMMAND_RUN_STATUS_NTF:
                logger.trace("setResponse(): received GW_COMMAND_RUN_STATUS_NTF, continuing.");
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 13)) {
                    setCommunicationUnsuccessfullyFinished();
                    break;
                }
                // Extracting information items
                int ntfSessionID = responseData.getTwoByteValue(0);
                int ntfStatusID = responseData.getOneByteValue(2);
                int ntfIndex = responseData.getOneByteValue(3);
                int ntfNodeParameter = responseData.getOneByteValue(4);
                int ntfParameterValue = responseData.getTwoByteValue(5);
                int ntfRunStatus = responseData.getOneByteValue(7);
                int ntfStatusReply = responseData.getOneByteValue(8);
                int ntfInformationCode = responseData.getFourByteValue(9);

                logger.trace("setResponse(): SessionID={}.", ntfSessionID);
                logger.trace("setResponse(): StatusID={}.", ntfStatusID);
                logger.trace("setResponse(): Index={}.", ntfIndex);
                logger.trace("setResponse(): NodeParameter={}.", ntfNodeParameter);
                logger.trace("setResponse(): ParameterValue={}.", ntfParameterValue);
                logger.trace("setResponse(): RunStatus={}.", ntfRunStatus);
                logger.trace("setResponse(): StatusReply={}.", ntfStatusReply);
                logger.trace("setResponse(): InformationCode={}.", ntfInformationCode);

                currentState = KLF200Handshake.State.WAIT4NOTIFICATION2;
                break;

            case GW_COMMAND_REMAINING_TIME_NTF:
                logger.trace("setResponse(): received GW_COMMAND_REMAINING_TIME_NTF, continuing.");
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 6)) {
                    setCommunicationUnsuccessfullyFinished();
                    break;
                }
                // Extracting information items
                ntfSessionID = responseData.getTwoByteValue(0);
                ntfIndex = responseData.getOneByteValue(2);
                ntfNodeParameter = responseData.getOneByteValue(3);
                int ntfSeconds = responseData.getTwoByteValue(4);

                logger.trace("setResponse(): SessionID={}.", ntfSessionID);
                logger.trace("setResponse(): Index={}.", ntfIndex);
                logger.trace("setResponse(): NodeParameter={}.", ntfNodeParameter);
                logger.trace("setResponse(): Seconds={}.", ntfSeconds);

                currentState = KLF200Handshake.State.WAIT4NOTIFICATION2;
                break;

            case GW_SESSION_FINISHED_NTF:
                logger.trace("setResponse(): received GW_SESSION_FINISHED_NTF.");
                if (!KLF200Response.isLengthValid(logger, responseCommand, thisResponseData, 2)) {
                    setCommunicationUnsuccessfullyFinished();
                    break;
                }
                // Extracting information items
                ntfSessionID = responseData.getTwoByteValue(0);

                logger.trace("setResponse(): SessionID={}.", ntfSessionID);
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
     * Methods in addition to the interface {@link BridgeCommunicationProtocol}
     * and the abstract class {@link RunScene}
     */

    @Override
    public SCrunScene setSceneId(int sceneId) {
        logger.trace("setProductId({}) called.", sceneId);
        this.reqSceneID = sceneId;
        return this;
    }

    @Override
    public SCrunScene setVelocity(int velocity) {
        logger.trace("setVelocity({}) called.", velocity);
        this.reqVelocity = velocity;
        return this;
    }

}
