package de.hirola.sportsapplications;

import de.hirola.sportsapplications.database.PersistentObject;
import de.hirola.sportsapplications.model.*;

import java.util.*;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Global library settings.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public final class Global {

    /**
     * Name of library, used as name for database and logging files.
     */
    public static final String LIBRARY_PACKAGE_NAME = "de.hirola.sportsapplications";
    public static final String ROOT_RESOURCE_BUNDLE_BASE_NAME = SportsLibrary.class.getSimpleName();
    public static final Locale DEFAULT_LOCALE = new Locale("en");

    public static final String MOVEMENT_TYPE_KEY_PREFIX = "movement.type.name.";
    public static final String JSON_RESOURCES = "/json";
    public static final String MOVEMENT_TYPES_JSON = "/json"
            + "/"
            + "movement-types.json";
    public static final String TRAINING_TYPES_JSON = "/json"
            + "/"
            + "training-types.json";
    public static final String RUNNING_PLAN_TEMPLATE_INDEX_JSON = "/json"
            + "/"
            + "index-of-templates.json";

    /**
     * A list of all types handled by the database.
     */
    public static final List<Class<? extends PersistentObject>> PERSISTENT_CLASSES_LIST;
    static {
        PERSISTENT_CLASSES_LIST = new ArrayList<>();
        PERSISTENT_CLASSES_LIST.add(LocationData.class);
        PERSISTENT_CLASSES_LIST.add(MovementType.class);
        PERSISTENT_CLASSES_LIST.add(RunningPlan.class);
        PERSISTENT_CLASSES_LIST.add(RunningPlanEntry.class);
        PERSISTENT_CLASSES_LIST.add(RunningUnit.class);
        PERSISTENT_CLASSES_LIST.add(Track.class);
        PERSISTENT_CLASSES_LIST.add(Training.class);
        PERSISTENT_CLASSES_LIST.add(TrainingType.class);
        PERSISTENT_CLASSES_LIST.add(User.class);
    }

    /**
    * A list of all types to be deleted as embedded objects.
     */
    public static final List<Class<? extends PersistentObject>> CASCADING_DELETED_CLASSES;
    static {
        CASCADING_DELETED_CLASSES = new ArrayList<>();
        CASCADING_DELETED_CLASSES.add(LocationData.class);
        CASCADING_DELETED_CLASSES.add(RunningPlanEntry.class);
        CASCADING_DELETED_CLASSES.add(RunningUnit.class);
    }

    /**
     * Training parameter
     */
    public static final class TrainingParameter {
        public static final Map<Integer, String> genderValues;
        static {
            genderValues = new HashMap<>();
            genderValues.put(0, "gender_undefined");
            genderValues.put(1, "gender_diverse");
            genderValues.put(2, "gender_male");
            genderValues.put(3, "gender_female");
        }

        public static final Map<Integer, String> trainingLevel;
        static {
            trainingLevel = new HashMap<>();
            trainingLevel.put(0, "training_beginner");
            trainingLevel.put(1, "training_amateur");
            trainingLevel.put(2, "training_profi");
        }

        public static final Map<String, String> movementTypes;
        static {
            movementTypes = new HashMap<>();
            movementTypes.put("P", "movement_type_pause");
            movementTypes.put("LG", "movement_type_slowing_going");
            movementTypes.put("ZG", "movement_type_speedy_going");
            movementTypes.put("L", "movement_type_running");
            movementTypes.put("R", "movement_type_sprint");
        }
    }

    /**
     * App parameter
     */
    public static final class AppSettings {

        // Nutzung der Cloud-Synchronisation, ermöglicht den Zugriff auf die Daten von verschiedenen Endgeräten aus.
        public static final boolean useSync = false;
    }

    /**
     * Validation parameter for locations
     */
    public static final class ValidLocationValues {
        // Minimaler Wert der Latitude.
        public static final double latitudeMinValue = -90.0;
        // Maximaler Wert der Latitude.
        public static final double latitudeMaxValue = 90.0;
        // Minimaler Wert der Longitude.
        public static final double longitudeMinValue = -180.0;
        // Maximaler Wert der Longitude.
        public static final double longitudeMaxValue = 180.0;
    }

    // Programm-Vorgaben (Wertebereiche)
    public static final class Defaults {
        /**
         * Default color for movement types
         */
        public static final String DEFAULT_MOVEMENT_TYPE_COLOR = "green";
        // Beginn der GPS-Aufzeichnung ab Anzahl von Location-Updates. Erhöhung der Genauigkeit.
        public static final int locationUpdatesRecordsBeginsAt = 5;
        // nach wie viel Sekunden soll ein Track gespeichert werden
        public static final int trackSaveInterval = 15;
        // Zeitspanne in Sekunden, um welche die "gemessene" Trainingszeit von der berechneten Trainingszeit
        // nach "unten" abweichen kann.
        public static final int activityTimeDifferenz = 900;
        // Toleranzbereich **X** beim Lauftraining
        // **X** < Messwert < **X**
        public static final double movementTolerance = 2.0;

        // Der Maximal-Puls wird nach der Formel 220 (Männer) bzw. 226 (Frauen) - Alter berechnet.
        // 2 = Frau, 3 = Mann
        public static final Map<Integer, Integer> valuesForCalculateMaxPulse;
        static {
            valuesForCalculateMaxPulse = new HashMap<>();
            valuesForCalculateMaxPulse.put(2, 226);
            valuesForCalculateMaxPulse.put(3, 220);
        }

        // Aktuelles Jahr - falls es über Kalender und Datum nicht ermittelt werden kann.
        public static final int actualYear = 2022;
        // Anzahl an anzuzeigenden Trainingswochen bei Laufplänen, wenn noch kein Startdatum gewählt wurde.
        // Aktuell 3 Monate.
        public static final int numberOfSelectableTrainingStartWeeks = 12;

    }

    /**
     * User settings keys
     */
    public static final class UserPreferencesKeys {
        public static final String USER_ROOT_NODE = SportsLibrary.class.getName();
        public static final String USED_LOCALE = "used_locale";
        public static final String LAST_USED_DATABASE = "last_used_database";
        public static final String SAVE_TRAINING = "save_trainings";
        public static final String USER_TRAINING_LEVEL = "user_training_level";
        public static final String USER_BIRTHDAY = "user_birthday";
        public static final String USER_MAX_PULSE = "user_max_pulse";
        public static final String USER_EMAIL_ADDRESS = "user_email_address";
        public static final String USER_GENDER = "user_gender";
        public static final String USE_LOCATIONS = "use_locations";
        public static final String USE_FINE_LOCATIONS = "use_fine_locations";
        public static final String USE_NOTIFICATION = "use_notifications";
        public static final String USE_SYNC = "use_sync";
        public static final String HIDE_TEMPLATES = "hide_templates";
        public static final String DEBUG_MODE = "debug_mode";
        public static final String SEND_DEBUG_LOG = "send_debug_log";
    }

    // Vorgaben für Farben und Texte (ohne Lokalisierung)
    public static final class ViewDefaults {
        // Standardfarbe für Bewegungsarten, zur grafischen Darstellung
        public static final String movementTypeDefaultColorString ="green";
        //Tabellen-Header
        public static final List<String> routeCollectionTableHeader;
        static {
            routeCollectionTableHeader = new ArrayList<>();
            routeCollectionTableHeader.add("Name der Route");
            routeCollectionTableHeader.add("importiert am");
            routeCollectionTableHeader.add("Länge");
            routeCollectionTableHeader.add("Höhenmeter");
            routeCollectionTableHeader.add("Anzahl Koordinaten");
        }

        public static final List<String> monthlyBikeTrainingTableHeader;
        static {
            monthlyBikeTrainingTableHeader = new ArrayList<>();
            monthlyBikeTrainingTableHeader.add("Tag");
            monthlyBikeTrainingTableHeader.add("Länge");
            monthlyBikeTrainingTableHeader.add("Dauer");
            monthlyBikeTrainingTableHeader.add("Höhenmeter");
        }

        public static final List<String> yearlyBikeTrainingTableHeader;
        static {
            yearlyBikeTrainingTableHeader = new ArrayList<>();
            yearlyBikeTrainingTableHeader.add("Jahr");
            yearlyBikeTrainingTableHeader.add("Länge");
            yearlyBikeTrainingTableHeader.add("Dauer");
            yearlyBikeTrainingTableHeader.add("Höhenmeter");
        }

        public static final List<String> runningPlanTableHeader;
        static {
            runningPlanTableHeader = new ArrayList<>();
            runningPlanTableHeader.add("");
            runningPlanTableHeader.add("Dauer des Trainings");
            runningPlanTableHeader.add("Übersicht");
        }
    }
}