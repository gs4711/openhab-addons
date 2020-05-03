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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility class for handling of packets i.e. array of bytes.
 *
 * <P>
 * Public methods are:
 * <UL>
 * <LI>{@link #toString} converts a packet into a human-readable String.</LI>
 * </UL>
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
@FunctionalInterface
public interface Protocol {

    /**
     * Returns the complete packet as human-readable String.
     *
     * @param packet as of array of bytes.
     * @return <b>packetString</b> of type String.
     */
    public String toString(byte[] packet);

}