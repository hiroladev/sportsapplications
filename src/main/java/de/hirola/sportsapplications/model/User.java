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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * The user of the app, here the runner.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
@Indices({
        @Index(value = "emailAddress", type = IndexType.Unique)
})
public class User extends PersistentObject {

    @Id
    private String uuid = UUIDFactory.generateUUID();
    private String firstName;
    private String lastName;
    private String emailAddress = UUIDFactory.generateEMailAddress();
    private Date birthday; // required to calculate the heart rate
    private int gender; // required to calculate the heart rate
    private int trainingLevel; // from Global
    private int maxPulse; // calculate with birthday and gender
    private UUID activeRunningPlanUUID; // current training

    /**
     * Default constructor for reflection and database management.
     */
    public User() {
        gender = 0;
        trainingLevel = 0;
        maxPulse = 0;
        activeRunningPlanUUID = null;
    }

    /**
     * Get the first name of the user.
     *
     * @return The first name of the user
     */
    public Optional<String> getFirstName() {
        return Optional.ofNullable(firstName);
    }

    /**
     * Set the first name of the user.
     *
     * @param firstName of the user
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Get the last name of the user.
     *
     * @return The last name of the user
     */
    public Optional<String> getLastName() {
        return Optional.ofNullable(lastName);
    }

    /**
     * Set the last name of the user.
     *
     * @param lastName of the user
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Get the email address of the user.
     *
     * @return The first name of the user
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Set the email address of the user.
     * The address will be not validate.
     *
     * @param emailAddress of the user
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Get the birthday of the user. The year ist need to calculate the max pulse.
     *
     * @return The birthday of the user.
     */
    public Optional<LocalDate> getBirthday() {
        return Optional.ofNullable(birthday
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate());
    }

    /**
     * Set the birthday of the user.
     *
     * @param birthday of the user
     */
    public void setBirthday(@NotNull LocalDate birthday) {
        this.birthday = Date
                .from(birthday.atStartOfDay()
                .atZone(ZoneId.systemDefault())
                .toInstant());
    }

    /**
     * Get the gender of the user.
     *
     * @return The gender of the user
     * @see Global
     */
    public int getGender() {
        return gender;
    }

    /**
     * Set the gender of the user.
     * The gender ist need to calculate the max pulse.
     *
     * @param gender of the user
     */
    public void setGender(int gender) {
        if (Global.TrainingParameter.genderValues.containsKey(gender)) {
            this.gender = gender;
        }
    }

    /**
     * Get the training level of user.
     *
     * @return The training level of the user
     * @see Global
     */
    public int getTrainingLevel() {
        return trainingLevel;
    }

    /**
     * Set the training level of the user.
     *
     * @param trainingLevel of the user
     */
    public void setTrainingLevel(int trainingLevel) {
        this.trainingLevel = trainingLevel;
    }

    /**
     * Get the max pulse of user.
     *
     * @return The max pulse of the user
     */
    public int getMaxPulse() {
        return maxPulse;
    }

    /**
     * Set the max pulse of the user.
     *
     * @param maxPulse of the user
     */
    public void setMaxPulse(int maxPulse) {
        this.maxPulse = maxPulse;
    }

    /**
     * Get the uuid of active running plan or null if not plan assigned.
     *
     * @return The uuid if the active running plan.
     */
    @Null
    public Optional<UUID> getActiveRunningPlanUUID() {
        return Optional.ofNullable(activeRunningPlanUUID);
    }

    /**
     * Set the uuid of active training plan.
     *
     * @param uuid of the active running plan
     */
    public void setActiveRunningPlanUUID(@NotNull UUID uuid) {
        activeRunningPlanUUID = uuid;
    }

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("uuid", uuid);
        document.put("firstName", firstName);
        document.put("lastName", lastName);
        document.put("emailAddress", emailAddress);
        document.put("birthday", birthday);
        document.put("gender", gender);
        document.put("trainingLevel", trainingLevel);
        document.put("maxPulse", maxPulse);
        document.put("activeRunningPlanUUID", activeRunningPlanUUID);

        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            uuid = (String) document.get("uuid");
            firstName = (String) document.get("firstName");
            lastName = (String) document.get("lastName");
            emailAddress = (String) document.get("emailAddress");
            birthday = (Date) document.get("birthday");
            gender = (int) document.get("gender");
            trainingLevel = (int) document.get("trainingLevel");
            maxPulse = (int) document.get("maxPulse");
            activeRunningPlanUUID = (UUID) document.get("activeRunningPlanUUID");
        }
    }

    @Override
    public boolean equals(Object o) {
        // gleiche User-ID und gleiche E-Mail-Adresse = gleiches Objekt
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User that = (User) o;
        return uuid.equals(that.uuid) && Objects.equals(emailAddress, that.emailAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid, emailAddress);
    }

    @Override
    public UUID getUUID() {
        return new UUID(uuid);
    }

}
