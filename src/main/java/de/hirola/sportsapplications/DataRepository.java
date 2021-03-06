package de.hirola.sportsapplications;

import de.hirola.sportsapplications.database.DatastoreDelegate;
import de.hirola.sportsapplications.database.PersistentObject;
import de.hirola.sportsapplications.model.*;

import org.dizitart.no2.Nitrite;
import org.dizitart.no2.exceptions.NotIdentifiableException;
import org.dizitart.no2.objects.Cursor;
import org.dizitart.no2.objects.ObjectRepository;
import org.dizitart.no2.objects.filters.ObjectFilters;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Adds a persistence layer, encapsulating the actual data storage technology used.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
final class DataRepository {

    private final int INSERT_ACTION = 0;
    private final int UPDATE_ACTION = 1;
    private final int REMOVE_ACTION = 2;

    private final SportsLibrary sportsLibrary;
    private final Nitrite database; // we use Nitrite database
    private final DatastoreDelegate delegate;

    /**
     * Create the local datastore access layer.
     *
     * @param sportsLibrary instance
     * @param databaseManager of this library
     * @param delegate to notify on events
     */
    public DataRepository(@NotNull SportsLibrary sportsLibrary, @NotNull DatabaseManager databaseManager, @NotNull DatastoreDelegate delegate) {
        this.sportsLibrary = sportsLibrary;
        this.delegate = delegate;
        database = databaseManager.getDatabase(); // can be null
    }

    /**
     * Get a flag to determine if the datastore is empty.
     * Some templates are required, which must be imported at the first start.
     * If an error occurred while determine the state, the result is true.
     *
     * @return A flag to determine if the datastore is empty
     */
    public boolean isEmpty() {
        // the datastore is "empty" if there are no movement [and training types] and running plans (yet)
        if (isOpen()) {
            if (!database.hasRepository(MovementType.class) &&
                    !database.hasRepository(TrainingType.class) &&
                            !database.hasRepository(RunningPlan.class)) {
                return true;
            }
            ObjectRepository<MovementType> movementTypeRepository = database.getRepository(MovementType.class);
            Cursor<MovementType> movementTypeCursor = movementTypeRepository.find(ObjectFilters.ALL);
            if (movementTypeCursor.size() == 0) {
                return true;
            }
            ObjectRepository<TrainingType> trainingTypeRepository = database.getRepository(TrainingType.class);
            Cursor<TrainingType> trainingTypeCursor = trainingTypeRepository.find(ObjectFilters.ALL);
            if (trainingTypeCursor.size() == 0) {
                return true;
            }
            ObjectRepository<RunningPlan> runningPlanRepository = database.getRepository(RunningPlan.class);
            Cursor<RunningPlan> runningPlanCursor = runningPlanRepository.find(ObjectFilters.ALL);
            return runningPlanCursor.size() == 0;
        }
        return false;
    }

    /**
     * Get a flag to determine if the datastore is open.
     *
     * @return A flag to determine if the datastore is open.
     */
    public boolean isOpen() {
        if (database == null) {
            return false;
        }
        return !database.isClosed();

    }

    /**
     * Add a new object.
     *
     * @param object to be added
     * @throws SportsLibraryException if an error occurred while adding
     */
    public void add(@NotNull PersistentObject object) throws SportsLibraryException {
        // the concrete type must be specified for each access to a repo
        if (isOpen()) {
            if (findByUUID(object.getClass(), object.getUUID()) == null) {
                // insert
                doActionWithObject(INSERT_ACTION, object);
                delegate.didObjectAdded(object);
            }
        } else {
            throw new SportsLibraryException("Database not available.");
        }
    }

    /**
     * Save an existing object.
     *
     * @param object to be saved
     * @throws SportsLibraryException if the object not exist or an error occurred while adding
     */
    public void update(@NotNull PersistentObject object) throws SportsLibraryException {
        // the concrete type must be specified for each access to a repo
        if (isOpen()) {
            if (findByUUID(object.getClass(), object.getUUID()) != null) {
                doActionWithObject(UPDATE_ACTION, object);
                delegate.didObjectUpdated(object);
            } else {
                throw new SportsLibraryException("The object must exist before the update.");
            }
        } else {
            throw new SportsLibraryException("Database not available.");
        }
    }

    /**
     * Removes an existing object from the local datastore.
     *
     * @param object to be removed
     * @throws SportsLibraryException if an error occurred while removing
     */
    public void delete(@NotNull PersistentObject object) throws SportsLibraryException {
        // the concrete type must be specified for each access to a repo
        if (isOpen()) {
            if (findByUUID(object.getClass(), object.getUUID()) != null) {
                // remove
                doActionWithObject(REMOVE_ACTION, object);
                delegate.didObjectRemoved(object);
            } else {
                throw new SportsLibraryException("The object was not found in database. Can not delete it.");
            }
        } else {
            throw new SportsLibraryException("Database not available.");
        }
    }

    /**
     * Get an object from given type with given UUID.
     *
     * @param withType of object to find
     * @param uuid of object to find
     * @return The object from given type and the given UUID or null if the object was not found
     *         or an error occurred while searching
     */
    @Null
    public PersistentObject findByUUID(@NotNull Class<? extends PersistentObject> withType, @NotNull UUID uuid) {
        if (isOpen()) {
            ObjectRepository<? extends PersistentObject> repository = database.getRepository(withType);
            Cursor<? extends PersistentObject> cursor;
            if (withType.getSimpleName().equals("MovementType")) {
                // movement type has a unique key
                cursor = repository.find(ObjectFilters.eq("key", uuid.getString()));
            } else {
                cursor = repository.find(ObjectFilters.eq("uuid", uuid.getString()));
            }
            if (cursor.size() == 1 ) {
                return cursor.firstOrDefault();
            }
            if (cursor.size() > 1) {
                // very bad
                if (sportsLibrary.isDebugMode()) {
                    sportsLibrary.debug("findByUUID has more than one result", cursor.size());
                }
            }
        }
        return null;

    }

    /**
     * Get all objects with a given type. If an error occurred while finding
     * the objects, the list is empty too.
     * Errors will be logged.
     *
     * @param fromType of object to get
     * @return A list of objects with the given type. The list can be empty.
     */
    public List<? extends PersistentObject> findAll(Class<? extends PersistentObject> fromType)  {
        List<? extends PersistentObject> results = new ArrayList<>();
        if (isOpen()) {
            ObjectRepository<? extends PersistentObject> repository = database.getRepository(fromType);
            Cursor<? extends PersistentObject> cursor = repository.find();
            return cursor.toList();
        }
        return results;
    }

    /**
     * Find objects with given name of attribute and value. List can bei empty.
     *
     * @param attributeName of object
     * @param value of attribute
     * @param fromType of object to find
     * @return A list of object where the attribute contains the desired value.
     */
    public List<? extends PersistentObject> findByAttribute(@NotNull String attributeName,
                                                            @NotNull Object value,
                                                            Class<? extends PersistentObject> fromType) {
        List<? extends PersistentObject> results = new ArrayList<>();
        if (isOpen()) {
            ObjectRepository<? extends PersistentObject> repository = database.getRepository(fromType);
            Cursor<? extends PersistentObject> cursor = repository.find(ObjectFilters.eq(attributeName, value));
            return  cursor.toList();
        }
        return results;
    }

    /**
     * Delete all objects from the database.
     */
    public void clearAll() {
        if (database != null) {
           for (Class<?> type: Global.PERSISTENT_CLASSES_LIST) {
               database.getRepository(type).remove(ObjectFilters.ALL);
           }
        }
    }

    /**
     * Close the database.
     */
    public void close() {
        if (database != null) {
            database.close();
        }
    }

    private void doActionWithObject(int action, PersistentObject object) throws SportsLibraryException {
        // the concrete type must be specified for each access to a repo
        try {
            if (object instanceof User) {
                ObjectRepository<User> objectRepository = database.getRepository(User.class);
                switch (action) {
                    case INSERT_ACTION: objectRepository.insert((User) object); return;
                    case UPDATE_ACTION: objectRepository.update((User) object); return;
                    case REMOVE_ACTION: objectRepository.remove((User) object); return;
                }

            }
            if (object instanceof Track) {
                switch (action) {
                    case INSERT_ACTION: doActionWithTrack(INSERT_ACTION, (Track) object); return;
                    case UPDATE_ACTION: doActionWithTrack(UPDATE_ACTION, (Track) object); return;
                    case REMOVE_ACTION: doActionWithTrack(REMOVE_ACTION, (Track) object); return;
                }
            }
            if (object instanceof TrainingType) {
                ObjectRepository<TrainingType> objectRepository = database.getRepository(TrainingType.class);
                switch (action) {
                    case INSERT_ACTION: objectRepository.insert((TrainingType) object); return;
                    case UPDATE_ACTION: objectRepository.update((TrainingType) object); return;
                    default: break;  // training types must be not delete
                }
            }
            if (object instanceof Training) {
                switch (action) {
                    case INSERT_ACTION: doActionWithTraining(INSERT_ACTION, (Training) object); return;
                    case UPDATE_ACTION: doActionWithTraining(UPDATE_ACTION, (Training) object); return;
                    case REMOVE_ACTION: doActionWithTraining(REMOVE_ACTION, (Training) object); return;
                }
            }
            if (object instanceof MovementType) {
                ObjectRepository<MovementType> objectRepository = database.getRepository(MovementType.class);
                switch (action) {
                    case INSERT_ACTION: objectRepository.insert((MovementType) object); return;
                    case UPDATE_ACTION: objectRepository.update((MovementType) object); return;
                    default: break;  // movement types must be not delete
                }
            }
            if (object instanceof RunningPlan) {
                switch (action) {
                    case INSERT_ACTION: doActionWithRunningPlan(INSERT_ACTION, (RunningPlan) object); return;
                    case UPDATE_ACTION: doActionWithRunningPlan(UPDATE_ACTION, (RunningPlan) object); return;
                    case REMOVE_ACTION: doActionWithRunningPlan(REMOVE_ACTION, (RunningPlan) object); return;
                }
            }
            throw new SportsLibraryException("Unsupported direct datastore operations.");
        } catch (Exception exception) {
            String errorMessage = "Operation "+ action +" with the object from type "
                    + object.getClass().getSimpleName()
                    +" and with id " + object.getUUID() + " failed.";
            if (sportsLibrary.isDebugMode()) {
                sportsLibrary.debug(errorMessage, exception);
            }
            throw new SportsLibraryException(exception);
        }
    }

    // handle a track with embedded locations
    private void doActionWithTrack(int action, @NotNull Track track) throws SportsLibraryException {
        // create or get the repositories
        ObjectRepository<Track> trackRepository = database.getRepository(Track.class);
        ObjectRepository<LocationData> locationsRepository = database.getRepository(LocationData.class);
        List<LocationData> locationDataList = track.getLocations();
        try {
            switch (action) {
                case INSERT_ACTION:
                    // rollback on error
                    List<UUID> locationUUIDs = new ArrayList<>();
                    // add locationData
                    for (LocationData locationData : locationDataList) {
                        UUID locationUUID = locationData.getUUID();
                        if (findByUUID(Track.class, locationUUID) == null) {
                            // insert
                            locationsRepository.insert(locationData);
                            // save for rollback
                            locationUUIDs.add(locationUUID);
                        } else {
                            // existing locationData cannot be added to a new track
                            // rollback
                            rollback(LocationData.class, locationUUIDs);
                            locationUUIDs.clear();
                            String errorMessage = "LocationData data of new track already exist in the database.";
                            if (sportsLibrary.isDebugMode()) {
                                sportsLibrary.debug(errorMessage);
                            }
                            throw new SportsLibraryException(errorMessage);
                        }
                    }
                    // add the track
                    trackRepository.insert(track);
                    return;

                case UPDATE_ACTION:
                    // update locationData
                    for (LocationData locationData : locationDataList) {
                        if (findByUUID(Track.class, locationData.getUUID()) == null) {
                            // insert a new locationData in the list
                            locationsRepository.insert(locationData);
                        } else {
                            // update
                            locationsRepository.update(locationData);
                        }
                    }
                    // update the track
                    trackRepository.update(track);
                    return;

                case REMOVE_ACTION:
                    // remove locationData
                    for (LocationData locationData : locationDataList) {
                        if (findByUUID(LocationData.class, locationData.getUUID()) != null) {
                            // remove the locationData
                            locationsRepository.remove(locationData);
                        }
                    }
                    // remove the track
                    trackRepository.remove(track);
            }
        } catch (Exception exception) {
            throw new SportsLibraryException(exception);
        }
    }

    // handle a training with embedded training type and track (and the locations of the track)
    // training type must exist in database
    private void doActionWithTraining(int action, @NotNull Training training) throws SportsLibraryException {
        // create or get the repositories
        ObjectRepository<Training> trainingRepository = database.getRepository(Training.class);
        Optional<UUID> trainingTypeUUID = training.getTrainingTypeUUID();
        Optional<UUID> trackUUID = training.getTrackUUID();
        try {
            switch (action) {
                case INSERT_ACTION:
                    // type must be existed before saving
                    if (trainingTypeUUID.isPresent()) {
                        if (findByUUID(TrainingType.class, trainingTypeUUID.get()) == null) {
                            throw new SportsLibraryException("The type of the training must be existed before inserting.");
                        }
                    }
                    // track must be existed before saving
                    if (trackUUID.isPresent()) {
                        if (findByUUID(Track.class, trackUUID.get()) == null) {
                            // track must be existed before saving
                            throw new SportsLibraryException("The track of the training must be existed before inserting.");
                        }
                    }
                    // add the training
                    trainingRepository.insert(training);
                    return;

                case UPDATE_ACTION:
                    // training type must be existed before saving
                    if (trainingTypeUUID.isPresent()) {
                        if (findByUUID(TrainingType.class, trainingTypeUUID.get()) == null) {
                            throw new SportsLibraryException("The type of the training must be existed before inserting.");
                        }
                    }
                    // track must be existed before saving
                    if (trackUUID.isPresent()) {
                        if (findByUUID(Track.class, trackUUID.get()) == null) {
                            // track must be existed before saving
                            throw new SportsLibraryException("The track of the training must be existed before inserting.");
                        }
                    }
                    // update the training
                    trainingRepository.update(training);
                    return;

                case REMOVE_ACTION:
                    // training type and track will be not remove
                    // they may have references to other objects
                    // remove the training
                    trainingRepository.remove(training);
            }
        } catch (Exception exception) {
            throw new SportsLibraryException(exception);
        }
    }

    // handle a running plan with embedded entries and units
    // movement type must exist in database
    private void doActionWithRunningPlan(int action, @NotNull RunningPlan runningPlan) throws SportsLibraryException {
        // create or get the repositories
        ObjectRepository<RunningPlan> runningPlanRepository = database.getRepository(RunningPlan.class);
        ObjectRepository<RunningPlanEntry> runningPlanEntryRepository = database.getRepository(RunningPlanEntry.class);
        ObjectRepository<RunningUnit> runningUnitRepository = database.getRepository(RunningUnit.class);
        ObjectRepository<MovementType> movementTypeRepository = database.getRepository(MovementType.class);
        List<RunningPlanEntry> entries = runningPlan.getEntries();
        try {
            switch (action) {
                case INSERT_ACTION:
                    // rollback on error
                    List<UUID> runningEntryUUIDs = new ArrayList<>();
                    List<UUID> runningUnitUUIDs = new ArrayList<>();
                    List<UUID> movementTypeUUIDs = new ArrayList<>();
                    // add running plan entries
                    for (RunningPlanEntry entry : entries) {
                        UUID entryUUID = entry.getUUID();
                        if (findByUUID(RunningPlanEntry.class, entryUUID) == null) {
                            // insert the units
                            List<RunningUnit> units = entry.getRunningUnits();
                            for (RunningUnit unit : units) {
                                UUID unitUUID = unit.getUUID();
                                if (findByUUID(RunningUnit.class, unitUUID) == null) {
                                    // insert the unit
                                    runningUnitRepository.insert(unit);
                                    // save for rollback
                                    runningUnitUUIDs.add(unitUUID);
                                } else {
                                    // error - running plan contains an existing unit
                                    // rollback
                                    rollback(RunningPlanEntry.class, runningEntryUUIDs);
                                    rollback(RunningUnit.class, runningUnitUUIDs);
                                    rollback(MovementType.class, movementTypeUUIDs);
                                    runningEntryUUIDs.clear();
                                    runningUnitUUIDs.clear();
                                    movementTypeUUIDs.clear();
                                    String errorMessage = "The new running plan contains existing units.";
                                    if (sportsLibrary.isDebugMode()) {
                                        sportsLibrary.debug(errorMessage);
                                    }
                                    throw new SportsLibraryException(errorMessage);
                                }

                                // insert a new movement type
                                MovementType movementType = unit.getMovementType();
                                UUID uuid = movementType.getUUID();
                                if (findByUUID(MovementType.class, uuid) == null) {
                                    movementTypeRepository.insert(movementType);
                                    // save for rollback
                                    movementTypeUUIDs.add(uuid);
                                }
                            }
                            // insert the entry
                            runningPlanEntryRepository.insert(entry);
                            // save for rollback
                            runningEntryUUIDs.add(entryUUID);
                        } else {
                            // error - running plan contains an existing entry
                            // rollback
                            rollback(RunningPlanEntry.class, runningEntryUUIDs);
                            rollback(RunningUnit.class, runningUnitUUIDs);
                            rollback(MovementType.class, movementTypeUUIDs);
                            String errorMessage = "The new running plan contains existing entries.";
                            if (sportsLibrary.isDebugMode()) {
                                sportsLibrary.debug(errorMessage);
                            }
                            throw new SportsLibraryException(errorMessage);
                        }
                    }
                    // add the running plan
                    runningPlanRepository.insert(runningPlan);
                    return;

                case UPDATE_ACTION:
                    // add or update running plan entries
                    for (RunningPlanEntry entry : entries) {
                        if (findByUUID(RunningPlanEntry.class, entry.getUUID()) == null) {
                            // insert the new entry and the units
                            List<RunningUnit> units = entry.getRunningUnits();
                            for (RunningUnit unit : units) {
                                // insert a new movement type
                                MovementType movementType = unit.getMovementType();
                                if (findByUUID(MovementType.class, movementType.getUUID()) == null) {
                                    movementTypeRepository.insert(movementType);
                                }
                                // insert the new unit
                                runningUnitRepository.insert(unit);
                            }
                            // insert the entry
                            runningPlanEntryRepository.insert(entry);
                        } else {
                            // update the entry and insert or update the units
                            runningPlanEntryRepository.update(entry);
                            List<RunningUnit> units = entry.getRunningUnits();
                            for (RunningUnit unit : units) {
                                // insert a new movement type
                                MovementType movementType = unit.getMovementType();
                                if (findByUUID(MovementType.class, movementType.getUUID()) == null) {
                                    movementTypeRepository.insert(movementType);
                                }
                                if (findByUUID(RunningUnit.class, unit.getUUID()) == null) {
                                    // insert the unit
                                    runningUnitRepository.insert(unit);
                                } else {
                                    // update the unit
                                    runningUnitRepository.update(unit);
                                }
                            }
                        }
                    }
                    // update the running plan
                    runningPlanRepository.update(runningPlan);
                    return;

                case REMOVE_ACTION:
                    // add or update running plan entries
                    for (RunningPlanEntry entry : entries) {
                        if (findByUUID(RunningPlanEntry.class, entry.getUUID()) != null) {
                            // insert the units
                            List<RunningUnit> units = entry.getRunningUnits();
                            for (RunningUnit unit : units) {
                                if (findByUUID(RunningUnit.class, unit.getUUID()) != null) {
                                    // remove the unit
                                    // movement type are not deleted
                                    runningUnitRepository.remove(unit);
                                }
                            }
                            // remove the entry
                            runningPlanEntryRepository.remove(entry);
                        }
                    }
                    // remove the running plan
                    runningPlanRepository.remove(runningPlan);
            }
        } catch (Exception exception) {
            throw new SportsLibraryException(exception);
        }
    }

    // rollback
    private void rollback(@NotNull Class<? extends PersistentObject> type, @NotNull List<UUID> objectUUIDs) {
        ObjectRepository<? extends PersistentObject> repository = database.getRepository(type);
        try {
            for (UUID uuid : objectUUIDs) {
                // delete all objects with given uuid
                if (type.getSimpleName().equals("MovementType")) {
                    // movement type has a unique key
                    repository.remove(ObjectFilters.eq("key", uuid.getString()));
                } else if (type.getSimpleName().equals("TrainingType")) {
                    // training type has a unique name
                    repository.remove(ObjectFilters.eq("name", uuid.getString()));
                } else {
                    repository.remove(ObjectFilters.eq("uuid", uuid.getString()));
                }
            }
        } catch (NotIdentifiableException exception) {
            if (sportsLibrary.isDebugMode()) {
                sportsLibrary.debug("Error while rollback.", exception);
            }
        }
    }
}
