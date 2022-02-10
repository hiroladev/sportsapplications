package de.hirola.sportslibrary.database;

import com.onyx.exception.InitializationException;
import com.onyx.persistence.factory.PersistenceManagerFactory;
import com.onyx.persistence.factory.impl.EmbeddedPersistenceManagerFactory;
import com.onyx.persistence.manager.PersistenceManager;
import de.hirola.sportslibrary.Global;
import de.hirola.sportslibrary.util.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Objects;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A layer to abstract the used data management library.
 * We use the <a href="https://onyx.dev/products#embedded">Onyx</a> embedded database.
 *
 * @author Michael Schmidt (Hirola)
 * @since 0.0.1
 */

public class DatabaseManager {

    private final String TAG = DatabaseManager.class.getSimpleName();

    private static DatabaseManager instance;
    private final Logger logger = Logger.getInstance(null);
    private PersistenceManagerFactory factory = null;
    private PersistenceManager persistenceManager = null;

    /**
     * Get an instance of database manager.
     *
     * @param appName of the using app or library, used for the database name
     * @return An instance of the database manager.
     */
    public static DatabaseManager getInstance(@Nullable String appName) {
        if (instance == null) {
            instance = new DatabaseManager(appName);
        }
        return instance;
    }

    /**
     * Get the manager to handle with data, e.g. save, update or delete.
     * Can be null if while initialize an error occurred. The errors
     * are logged.
     *
     * @return The manger for data management
     */
    @Nullable
    public PersistenceManager getPersistenceManager() {
        return persistenceManager;
    }

    public void close() {
        if (factory != null) {
            factory.close();
        }
    }

    private DatabaseManager(@Nullable String appName) {
        //TODO: alternative user defined path
        String databasePath;
        //TODO: check valid appName
        String databaseName = Objects.requireNonNullElse(appName, Global.LIBRARY_NAME);

        // build the path, determine if android or jvm
        // see https://developer.android.com/reference/java/lang/System#getProperties()
        try {
            String vendor = System.getProperty("java.vm.vendor"); // can be null
            if (vendor != null) {
                if (vendor.equals("The Android Project")) {
                    // path for local database on Android
                    databasePath = "/data/data"
                            + File.separatorChar
                            + databaseName
                            + File.separatorChar
                            + databaseName + ".db";
                } else {
                    //  path for local database on JVM
                    String userHomeDir = System.getProperty("user.home");
                    databasePath = userHomeDir
                            + File.separatorChar
                            + databaseName
                            + File.separatorChar
                            + databaseName + ".db";
                }

                factory = new EmbeddedPersistenceManagerFactory(databasePath);
                factory.initialize();

                persistenceManager = factory.getPersistenceManager();

            } else {
                persistenceManager = null;
                logger.log(Logger.ERROR, TAG,"Could not determine the runtime environment. Manager is null.",null);
            }
        } catch (SecurityException | InitializationException exception){
            logger.log(Logger.ERROR, TAG,"Could not determine the runtime environment. Manager is null.",exception);
        }
    }
}
