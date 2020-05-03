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

import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.velux.internal.bridge.slip.utils.KLF200Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An unbounded thread-safe {@linkplain Queue queue} based on linked nodes.
 * This queue orders elements FIFO (first-in-first-out). This class takes care
 * that each element is only returned once to a specific thread.
 *
 * @author Guenther Schreiner - Initial contribution.
 */
@NonNullByDefault
public class ExtendedConcurrentLinkedQueue<E> extends AbstractQueue<E> implements Queue<E> {
    private final Logger logger = LoggerFactory.getLogger(ExtendedConcurrentLinkedQueue.class);

    /*
     * ***************************
     * ***** Private Objects *****
     */

    /**
     * Maximum number of usages of a stored node before an automatic purge is enforced.
     * <P>
     * Assuming that the openHAB default thread pool deals with five threads and the
     * usage is evenly distributed, all threads will have processed the node twice.
     */
    private static final int MAX_USAGE_COUNTER = 10;

    /**
     * Queue for storing nodes which are not yet processed
     */
    private ConcurrentLinkedQueue<ExtendedNode<E>> extendedNodes = new ConcurrentLinkedQueue<ExtendedNode<E>>();

    /*
     * ***************************
     * ***** Private Methods *****
     */

    /**
     * Purges all nodes from queue which have already have been heavily used.
     * <P>
     * The behaviour in controlled by constant {@link #MAX_USAGE_COUNTER}.
     */
    private void autopurge() {
        Iterator<ExtendedNode<E>> iterator = extendedNodes.iterator();
        while (iterator.hasNext()) {
            ExtendedNode<E> eNode = iterator.next();
            if (eNode.getCountOfUsages() > MAX_USAGE_COUNTER) {
                logger.trace("autopurge() purging {}.", eNode.getNode());
                // TODO
                logger.trace("autopurge() purging {}.", new KLF200Protocol().toString((byte[]) eNode.getNode()));
                extendedNodes.remove(eNode);
            }
        }
        logger.trace("autopurge() done.");
    }

    /*
     * **************************
     * ***** Public Methods *****
     */

    @Override
    public synchronized boolean isEmpty() {
        if (extendedNodes.isEmpty()) {
            return true;
        }
        Iterator<ExtendedNode<E>> iterator = extendedNodes.iterator();
        while (iterator.hasNext()) {
            if (!iterator.next().isProcessedByThisThread()) {
                logger.trace("isEmpty() returns false.");
                return false;
            }
        }
        return true;
    }

    @Override
    public synchronized E poll() {
        if (isEmpty()) {
            logger.warn("poll() returning null.");
            return (E) null;
        }
        Iterator<ExtendedNode<E>> iterator = extendedNodes.iterator();
        while (iterator.hasNext()) {
            ExtendedNode<E> eNode = iterator.next();
            if (!eNode.isProcessedByThisThread()) {
                logger.trace("poll() returns {}.", eNode.getNode());
                extendedNodes.remove(eNode);
                return eNode.getNode();
            }
        }
        logger.warn("poll() returning null.");
        return (E) null;
    }

    @Override
    public synchronized E peek() {
        if (isEmpty()) {
            logger.warn("peek() returning null.");
            return (E) null;
        }
        Iterator<ExtendedNode<E>> iterator = extendedNodes.iterator();
        while (iterator.hasNext()) {
            ExtendedNode<E> eNode = iterator.next();
            if (!eNode.isProcessedByThisThread()) {
                logger.trace("peek() returns {}.", eNode.getNode());
                // TODO
                logger.trace("peek() returns {}.", new KLF200Protocol().toString((byte[]) eNode.getNode()));
                eNode.setProcessedByThisThread();
                return eNode.getNode();
            }
        }
        logger.warn("peek() returning null.");
        return (E) null;
    }

    @Override
    public synchronized int size() {
        return extendedNodes.size();
    }

    @Override
    public synchronized boolean add(E node) {
        Iterator<ExtendedNode<E>> iterator = extendedNodes.iterator();
        while (iterator.hasNext()) {
            ExtendedNode<E> eNode = iterator.next();
            // TODO
            // KLF200Protocol.printMessage(logger, "add ", (byte[]) eNode.getNode());
            if (eNode.getNode() == node) {
                logger.trace("add() not storing as it already exists as {}.", node);
                return true;
            }
        }
        ExtendedNode<E> eNode = new ExtendedNode<E>(node);
        eNode.setProcessedByThisThread();
        // TODO
        logger.trace("add() storing it as new entry {}.", node);
        logger.trace("add() storing {}.", new KLF200Protocol().toString((byte[]) eNode.getNode()));
        return extendedNodes.add(eNode);
    }

    @Override
    public synchronized boolean offer(E node) {
        return add(node);
    }

    /*
     * *************************************
     * ***** Public Methods - Iterator *****
     */

    @Override
    public synchronized Iterator<E> iterator() {
        return new Itr();
    }

    @NonNullByDefault
    private class Itr implements Iterator<E> {

        @Override
        public boolean hasNext() {
            return extendedNodes.iterator().hasNext();
        }

        @Override
        public E next() {
            return extendedNodes.iterator().next().getNode();
        }
    }

    /*
     * **************************
     * ***** Public Methods *****
     */

    /**
     * Remove all flags for the current thread.
     * Usually it will be used after a completed/aborted handshake.
     */
    public synchronized void clean() {
        Iterator<ExtendedNode<E>> iterator = extendedNodes.iterator();
        while (iterator.hasNext()) {
            ExtendedNode<E> eNode = iterator.next();
            eNode.unsetProcessedByThisThread();
        }
        if (logger.isDebugEnabled()) {
            if (extendedNodes.size() > 0) {
                logger.debug("clean():  remaining elements {}.", this.toString());
            }
        }
        autopurge();
    }

    /**
     * Purge the given node from queue.
     *
     * @param node The item passed as type {@link E} which is to be purged from the queue.
     */
    public synchronized void purge(E node) {
        Iterator<ExtendedNode<E>> iterator = extendedNodes.iterator();
        while (iterator.hasNext()) {
            ExtendedNode<E> eNode = iterator.next();
            if (eNode.getNode().equals(node)) {
                logger.trace("purge() purging {}.", eNode.getNode());
                // TODO
                logger.trace("purge() purging {}.", new KLF200Protocol().toString((byte[]) eNode.getNode()));
                extendedNodes.remove(eNode);
            }
        }
    }

    /**
     * Purge all stored nodes
     */
    public synchronized void purge() {
        extendedNodes = new ConcurrentLinkedQueue<ExtendedNode<E>>();
    }

    /**
     * Print the complete content of the queue.
     *
     * @return String describing the content of the complete queue.
     */
    @Override
    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        Iterator<ExtendedNode<E>> iterator = extendedNodes.iterator();
        while (iterator.hasNext()) {
            ExtendedNode<E> eNode = iterator.next();
            sb.append(eNode.toString());
            sb.append(new KLF200Protocol().toString((byte[]) eNode.getNode()));
            sb.append("\n");
        }
        return sb.toString();
    }

}
