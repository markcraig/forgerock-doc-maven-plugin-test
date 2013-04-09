/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 *
 * If applicable, add the following below this MPL 2.0 HEADER, replacing
 * the fields enclosed by brackets "[]" replaced with your own identifying
 * information:
 *     Portions Copyright [yyyy] [name of copyright owner]
 *
 *     Copyright 2012 ForgeRock AS
 *
 */

package org.forgerock.doc.jcite.test;

/**
 * Test class for trying JCite.
 */
public final class Test {
    /**
     * Print a message, then print args one per line.
     * <p>
     * Here is a test using JCite in Javadoc:
     * {@.jcite --- mainMethod}
     * @param args Program arguments.
     */
  // --- mainMethod
    public static void main(final String[] args) {
        System.out.println("This is just a test.");
        for (String arg : args) {
            System.out.println("\t" + arg);
        }
    }
  // --- mainMethod
}
