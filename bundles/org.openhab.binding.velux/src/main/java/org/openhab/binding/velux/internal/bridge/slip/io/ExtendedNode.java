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
package org.openhab.binding.velux.internal.bridge.slip.io;

import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An unbounded thread-safe {@linkplain Queue queue} based on linked nodes.
 * This queue orders elements FIFO (first-in-first-out).
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public class ExtendedNode<F> {

    /*
     * **********************************
     * ***** Private Static Objects *****
     */

    /**
     * Counter for number of created nodes
     */
    private static int sequenceNumber = 0;

    /*
     * ***************************
     * ***** Private Objects *****
     */

    /**
     * The node still to be processed.
     */
    private F node;
    /**
     * Timestamp of creation in milliseconds.
     */
    private Thread producer = Thread.currentThread();
    /**
     * Timestamp of creation in milliseconds.
     */
    private long generationInMSecs = System.currentTimeMillis();
    /**
     * Set of threads which had already evaluated this node.
     */
    private Set<Thread> alreadyInspected = new HashSet<>();
    /**
     * Counter for number of usages of this nodes
     */
    private int countOfUsages = 0;

    /*
     * **************************
     * ***** Public Methods *****
     */

    /**
     * Constructor
     * <P>
     *
     * @param thisnode as array of byte to be store for further processing.
     */
    ExtendedNode(F thisnode) {
        node = thisnode;
        setProcessedByThisThread();
        sequenceNumber++;
    }

    /**
     * Returns the sequence number of this node.
     *
     * @return sequenceNumber as int.
     */
    int getSequenceNumber() {
        return sequenceNumber;
    }

    /**
     * Returns the timestamp of creation of this node.
     *
     * @return receivedInMSecs as long.
     */
    F getNode() {
        return node;
    }

    /**
     * Returns the thread which created this node.
     *
     * @return createdBy as Thread.
     */
    Thread getProducer() {
        return producer;
    }

    /**
     * Returns the timestamp of generation in milliseconds.
     * <p>
     * Note that while the unit of time of the return value is a millisecond,
     * the granularity of the value depends on the underlying
     * operating system and may be larger. See {@link System#currentTimeMillis}.
     *
     * @return receivedInMSecs as long.
     */
    long getGenerationTimeMillis() {
        return generationInMSecs;
    }

    /**
     * Returns the number of usages of this node.
     * <p>
     * Note that the value is determined by the number of calls
     * to the method {@link #setProcessedByThisThread()}.
     *
     * @return countOfUsages as int.
     */
    int getCountOfUsages() {
        return countOfUsages;
    }

    /**
     * Returns whether this node is already processed by this thread.
     *
     * @return alreadyInspected as boolean.
     */
    boolean isProcessedByThisThread() {
        return alreadyInspected.contains(Thread.currentThread());
    }

    /**
     * Set the hash that this node is processed by this thread.
     */
    void setProcessedByThisThread() {
        countOfUsages++;
        alreadyInspected.add(Thread.currentThread());
    }

    /**
     * Set the hash that this node is processed by this thread.
     */
    void unsetProcessedByThisThread() {
        alreadyInspected.remove(Thread.currentThread());
    }

    /**
     * Returns the textual representation of the stored node.
     *
     * @return nodeAsString as String.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("%d:%d:%s:%d:", sequenceNumber, countOfUsages, producer, generationInMSecs));
        alreadyInspected.forEach((thread) -> {
            builder.append(thread.getName() + "/");
        });
        builder.append(String.format(":%s", node));
        return builder.toString();
    }

}
