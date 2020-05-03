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

import java.util.Arrays;
import java.util.HashSet;

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
 * <LI>{@link State} for definition of general states.</LI>
 * <LI>{@link build} as helper for state machine definitions.</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public class KLF200Handshake {

    /**
     * Enumeration of generic handshake states.
     */
    public enum State {
        IDLE,
        WAIT4CONFIRMATION,
        WAIT4NOTIFICATION,
        WAIT4NOTIFICATION2,
        WAIT4FINISH,
    };

    public static HashSet<Command> build(Command... expectedResponses) {
        return new HashSet<Command>(Arrays.asList(expectedResponses));
    }

}
