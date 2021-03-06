package de.hirola.sportsapplications.model;

import de.hirola.sportsapplications.Global;
import de.hirola.sportsapplications.database.PersistentObject;
import de.hirola.sportsapplications.util.UUIDFactory;
import org.dizitart.no2.Document;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;
import org.dizitart.no2.objects.Index;
import org.dizitart.no2.objects.Indices;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import java.util.Objects;
import java.util.Optional;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * An object represents the type of training, currently bike and running training.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 *
 */
@Indices({
        @Index(value = "name", type = IndexType.Unique)
})
public class TrainingType extends PersistentObject {

    public static final String RUNNING = "running";
    public static final String HIKING = "hiking";
    public static final String BIKING = "biking";

    @Id
    private String uuid = UUIDFactory.generateUUID();
    private String name;
    private String imageName; // image for the kind of training
    private String remarks;
    private double speed;

    /**
     * Default constructor for reflection and database management.
     */
    public TrainingType() {
        name = UUIDFactory.generateTrainingType();
        //TODO: default image for JVM and Android in resources
        imageName = Global.Defaults.TRAINING_DEFAULT_IMAGE_NAME;
        speed = 0.0;
    }

    /**
     * Create a type of training.
     *
     * @param name of type
     * @param imageName of type
     * @param remarks of type
     * @param speed of type
     */
    public TrainingType(@NotNull String name, @Null String remarks, @Null String imageName, double speed) {
        this.name = name;
        this.remarks = remarks;
        this.imageName = Objects.requireNonNullElse(imageName, Global.Defaults.TRAINING_DEFAULT_IMAGE_NAME);
        this.speed = speed;
    }

    /**
     * Get the name of the training type.
     *
     * @return The name of training type
     */
    public String getName() {
        return name;
    }

    /**
     * Set the last name of the training type.
     *
     * @param name of the training type.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the name of the image for the training type.
     *
     * @return The name of image
     */
    public String getImageName() {
        return imageName;
    }

    /**
     * Set name of the image for the training type.
     *
     * @param imageName for the training type
     */
    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    /**
     * Get remarks of the training type.
     *
     * @return The remarks of the training type
     */
    public Optional<String> getRemarks() {
        return Optional.ofNullable(remarks);
    }

    /**
     * Set the remarks of the training type.
     *
     * @param remarks of the training type
     */
    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    /**
     * Get the speed of the training type.
     *
     * @return The speed of the training type
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Set the speed for the training type.
     * Approximate speed of the training type in km/h.
     * The type can thus be suggested on the basis of recorded training sessions.
     *
     * @param speed of the training
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("uuid", uuid);
        document.put("name", name);
        document.put("imageName", imageName);
        document.put("remarks", remarks);
        document.put("speed", speed);

        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            uuid = (String) document.get("uuid");
            name = (String) document.get("name");
            imageName = (String) document.get("imageName");
            remarks = (String) document.get("remarks");
            speed = (double) document.get("speed");
        }
    }
    
    @Override
    public boolean equals(Object o) {
        // gleicher Name = gleiches Objekt
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TrainingType that = (TrainingType) o;
        return uuid.equals(that.uuid) && name.equals(that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid, name);
    }

    @Override
    public UUID getUUID() {
        return new UUID(uuid);
    }

    
}

