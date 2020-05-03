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
package org.openhab.binding.velux.internal.bridge.common;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Protocol independent bridge communication supported by the Velux bridge.
 * <P>
 * Common Message semantic: Communication with the bridge and (optionally) storing returned information within the class
 * itself.
 * <P>
 * As 2nd level interface it defines the methods to help in sending a query and
 * processing the received answer.
 * <P>
 * Two query methods for determination the result of a handshake:
 * <UL>
 * <LI>{@link isCommunicationFinished} to provide information about a completed set of transactions.</LI>
 * <LI>{@link isCommunicationSuccessful} to provide information whether the completed set of transactions was
 * successful.</LI>
 * </UL>
 * Three manipulation Methods for setting the result of a handshake:
 * <UL>
 * <LI>{@link setCommunicationUnfinishedAndUnsuccessful} to set the transaction to unfinished and unsuccessful.</LI>
 * <LI>{@link setCommunicationUnsuccessfullyFinished} to set the transaction to finished and unsuccessful.</LI>
 * <LI>{@link setCommunicationSuccessfullyFinished} to set the transaction to finished and successful.</LI>
 * </UL>
 * 
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public class BridgeCommunication {

    /*
     * ===========================================================
     * Result Objects
     */

    protected boolean isCommunicationSuccessful = false;
    /**
     * Returns the communication handshake status.
     */
    protected boolean isHandshakeFinished = false;

    /**
     * Returns the communication handshake status.
     *
     * @return true if the communication is finished, and false otherwise.
     */
    public boolean isCommunicationFinished() {
        return isHandshakeFinished;
    }

    /**
     * Returns the communication status included within the response message.
     *
     * @return true if the communication was successful, and false otherwise.
     */
    public boolean isCommunicationSuccessful() {
        return isCommunicationSuccessful;
    }

    /**
     * Reset all flags to default
     */
    public void setCommunicationUnfinishedAndUnsuccessful() {
        isCommunicationSuccessful = false;
        isHandshakeFinished = false;
    }

    /**
     * Reset all flags to default
     */
    public void setCommunicationUnsuccessfullyFinished() {
        isCommunicationSuccessful = false;
        isHandshakeFinished = true;
    }

    /**
     * Reset all flags to default
     */
    public void setCommunicationSuccessfullyFinished() {
        isCommunicationSuccessful = true;
        isHandshakeFinished = true;
    }

}
