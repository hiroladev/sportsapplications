package de.hirola.sportslibrary.util;

import java.util.UUID;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Creates UUID for the objects, needed as key in datastore
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 *
 */
public final class UUIDFactory {
    public static String generateUUID() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase().substring(0, 15);
    }
}
