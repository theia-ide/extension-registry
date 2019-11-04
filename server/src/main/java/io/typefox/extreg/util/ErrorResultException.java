/********************************************************************************
 * Copyright (c) 2019 TypeFox
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 ********************************************************************************/
package io.typefox.extreg.util;

/**
 * Throw this exception to reply with a JSON object of the form
 * <pre>
 * { "error": "«message»" }
 * </pre
 */
public class ErrorResultException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public ErrorResultException(String message) {
        super(message);
    }

    public ErrorResultException(String message, Throwable cause) {
        super(message, cause);
    }

}