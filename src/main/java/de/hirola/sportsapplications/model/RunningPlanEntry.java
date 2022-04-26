package de.hirola.sportsapplications.model;

import de.hirola.sportsapplications.SportsLibraryException;
import de.hirola.sportsapplications.database.ListMapper;
import de.hirola.sportsapplications.database.PersistentObject;
import de.hirola.sportsapplications.util.UUIDFactory;
import org.dizitart.no2.Document;
import org.dizitart.no2.mapper.NitriteMapper;
import org.dizitart.no2.objects.Id;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * An entry in the running plan contains the day and the respective training sections for the individual weeks.
 * An example:
 * Week: 3, Day: 1 (Monday), 7 min total, 2 min run, 3 min slow walk, 2 min run
 *
 * A running plan entry cannot exist without a running plan.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */

public class RunningPlanEntry extends PersistentObject implements Comparable<RunningPlanEntry> {

    @Id
    private String uuid = UUIDFactory.generateUUID();
    private int week; // number of week, begins with 1
    private int day; // day of week, begins with 1 (monday)
    private List<RunningUnit> runningUnits; // units if training day

    /**
     * Default constructor for reflection and database management.
     */
    public RunningPlanEntry() {
        super();
        day = 1;
        week = 1;
        runningUnits = new ArrayList<>();
    }

    /**
     * Create a running plan entry.
     *
     * @param day of entry
     * @param week of entry
     * @param runningUnits of entry
     * @see RunningUnit
     */
    public RunningPlanEntry (int day, int week, @NotNull List<RunningUnit> runningUnits) {
        this.day = day;
        this.week = week;
        this.runningUnits = runningUnits;
    }

    /**
     * Get the day (number) of training.
     * The 1 represents the monday and the 7 the sunday
     * of a week.
     *
     * @return The number of day in the week for the training.
     */
    public int getDay() {
        return day;
    }

    /**
     * Set the day (number) of training.
     * The 1 represents the monday and the 7 the sunday
     * of a week.
     * If a number less than zero or greater than 7, the 1
     * is set as day.
     *
     * @param day number of day in the week for the training
     */
    public void setDay(int day) {
        if (day < 1 || day > 7) {
            this.day = 1;
        } else {
            this.day = day;
        }
    }

    /**
     * Get the week (number) of training.
     *
     * @return The number of week for the training.
     */
    public int getWeek() {
        return week;
    }

    /**
     * Set the week (number) of training.
     * If the week number less than zero or greater than 52,
     * the 1 is set as week.
     *
     * @param week number of week for the training
     */
    public void setWeek(int week) {
        if (week < 1 || week > 52) {
            this.week = 1;
        } else {
            this.week = week;
        }
    }

    /**
     * Get a list of all running units of this entry.
     *
     * @return A list of all running units of this entry.
     */
    public List<RunningUnit> getRunningUnits() {
        return runningUnits;
    }

    /**
     * Get a list of all running units of this entry.
     *
     * @param runningUnits of this entry.
     */
    public void setRunningUnits(@NotNull List<RunningUnit> runningUnits) {
        this.runningUnits = runningUnits;
    }

    /**
     * The total duration of the training session in minutes,
     * i.e. the sum of the individual training sections.
     *
     * @return Sum of the individual training sections in minutes
     */
    public long getDuration() {
        return runningUnits.stream().map(RunningUnit::getDuration).reduce(0L, Long::sum);
    }

    /**
     * Indicates whether the run plan entry (training day) has been completed.
     * Is automatically true if all training sections are completed.
     *
     * @return A flag to determine if all units of entry completed
     */
    public boolean isCompleted() {
        return runningUnits.stream().allMatch(RunningUnit::isCompleted);
    }

    /**
     * (Rounded) percentage of run segments completed.
     *
     * @return Percentage of run segments completed
     */
    public int percentCompleted() {
        int countOfEntries = runningUnits.size();
        int percent = 0;
        int completed = 0;

        if (countOfEntries > 0) {
            for (RunningUnit runningUnit : runningUnits) {
                if (runningUnit.isCompleted()) {
                    completed += 1;
                }
            }
            completed = (completed * 100) / countOfEntries;
        }
        return completed;
    }

    @Override
    public Document write(NitriteMapper mapper) {
        Document document = new Document();
        document.put("uuid", uuid);
        document.put("week", week);
        document.put("day", day);

        if (runningUnits != null) {
            document.put("runningUnits", ListMapper.toDocumentsList(mapper, runningUnits));
        }

        return document;
    }

    @Override
    public void read(NitriteMapper mapper, Document document) {
        if (document != null) {
            uuid = (String) document.get("uuid");
            week = (int) document.get("week");
            day = (int) document.get("day");

            try {
                @SuppressWarnings("unchecked")
                List<Document> objectsDocument = (List<Document>) document.get("runningUnits");
                runningUnits = ListMapper.toElementsList(mapper, objectsDocument, RunningUnit.class);
            } catch (ClassCastException | SportsLibraryException exception) {
                //TODO: logging?
                runningUnits = new ArrayList<>();
            }
        }
    }
    
    @Override
    public boolean equals(Object o) {
        // gleicher Tag in gleicher Woche = Object ist identisch
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        RunningPlanEntry that = (RunningPlanEntry) o;
        return uuid.equals(that.uuid) && week == that.week && day == that.day;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), uuid, week, day);
    }

    @Override
    public UUID getUUID() {
        return new UUID(uuid);
    }

    @Override
    public int compareTo(@NotNull RunningPlanEntry other) {
        // first week, then day
        int weekCompare = Integer.compare(this.week, other.week);
        int dayCompare = Integer.compare(this.day, other.day);
        if (weekCompare == 0 && dayCompare == 0) {
            return 0;
        }
        if (weekCompare == 0 && dayCompare == -1) {
            return -1;
        }
        if (weekCompare == 0 && dayCompare == 1) {
            return 1;
        }
        if (weekCompare == 1 && dayCompare == 0) {
            return 0;
        }
        if (weekCompare == -1) {
            return -1;
        }
        if (weekCompare == 1) {
            return 1;
        }
        return 0;
    }

}