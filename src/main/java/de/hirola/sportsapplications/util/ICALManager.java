package de.hirola.sportsapplications.util;

import de.hirola.sportsapplications.Global;
import de.hirola.sportsapplications.SportsLibrary;
import de.hirola.sportsapplications.model.RunningPlan;
import de.hirola.sportsapplications.model.RunningPlanEntry;
import de.hirola.sportsapplications.model.RunningUnit;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.component.CalendarComponent;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Summary;
import net.fortuna.ical4j.validate.ValidationException;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2022 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A util class for managing GPX files-
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 *
 */
public final class ICALManager {

    /**
     * Import a running plan in iCAL format from <a href="https://lauftipps.ch/">laufplan.ch</a>
     * and add it to the local datastore.
     *
     * @param sportsLibrary in which the track import should become
     * @param importFile with data in iCAL format
     * @throws IOException if the iCAL file not read or the data could not be imported.
     * @throws ValidationException has the file not a valid format
     */
    public static void importICAL(@NotNull SportsLibrary sportsLibrary, @NotNull File importFile)
            throws IOException, ValidationException {
        final ApplicationResources applicationResources = ApplicationResources.getInstance();
        if (importFile.exists()) {
            if (importFile.isFile() && importFile.canRead()) {
                try {
                    // reading the iCAL file using the ical4j library
                    final FileInputStream fin = new FileInputStream(importFile.getPath());
                    final CalendarBuilder builder = new CalendarBuilder();
                    final Calendar runningPlanCalendar = builder.build(fin);
                    runningPlanCalendar.validate();
                    final ComponentList<CalendarComponent> components = runningPlanCalendar.getComponents();

                    // get the name for the running plan
                    // e.g. "10-km-Trainingsplan Flex10 (lauftipps.ch/LT273)"
                    String name = applicationResources.getString("ical.event.runningplan.name");
                    String remarks = applicationResources.getString("ical.event.runningplan.remarks");
                    if (components.size() > 0) {
                        final VEvent event = (VEvent) components.get(0);
                        event.validate();
                        final String description = event.getDescription().getValue();
                        int startIndexOfNewLine =  description.indexOf('\n');
                        if (startIndexOfNewLine > -1) {
                            name = description.substring(0, startIndexOfNewLine);
                        }
                    }
                    // try to extract the data from event
                    final List<RunningPlanEntry> entries = new ArrayList<>();
                    for (CalendarComponent component: components) {
                        final VEvent event = (VEvent) component;
                        event.validate();
                        entries.add(getDataFromEvent(sportsLibrary, event));
                    }
                    // create running plan
                    RunningPlan runningPlan = new RunningPlan(name, remarks, 0, entries, false);
                    // add to local datastore

                } catch (ParserException exception) {
                    throw new IOException("Error while loading the iCAL.", exception);
                }
            } else {
                throw new IOException("The file " + importFile + " is not a file or could not be read.");
            }
        } else {
            throw new IOException("The file " + importFile + " does not exist.");
        }
    }

    /**
     * Export a given running plan from the sports library to a iCAL file.
     * Not implemented yet.
     *
     * @param runningPlan to be exported
     * @param exportFile for the iCAL
     * @throws IOException if the export failed
     */
    public static void exportICAL(@NotNull RunningPlan runningPlan,
                                  @NotNull File exportFile) throws IOException {
        final ApplicationResources applicationResources = ApplicationResources.getInstance();
        File parentDirectory = exportFile.getParentFile();
        if (parentDirectory.exists()) {
            if (parentDirectory.isDirectory() && parentDirectory.canWrite()) {
                throw new IOException("Sorry - Not implemented yet.");
            } else {
                throw new IOException("The file " + exportFile + " is not a file or is not writeable.");
            }
        } else {
            throw new IOException("The file " + exportFile + " does not exist.");
        }
    }

    private static RunningPlanEntry getDataFromEvent(SportsLibrary sportsLibrary, VEvent event) {
        /*
        typical format - info in description:
        WOCHE 1 (ab 06.06.2022)
        ----------
        Lauf #2: > Intervalltraining: 10 min EL, IV, 10 min laDL [----> v0.1]
        Lauf #2: > IV= 3 x 3 min (5-km-Tempo), dazwischen 4 min TP [----> v0.1]
        Dauer: 37 min [----> v0.1]
        Puls: 2a: 115 bis 121
        Puls: 2b: 162 bis 168
        Tempo 2a: 08:59 min|km
        Tempo 2b: 05:57 min|km
        Distanz: 4.6 km [----> v0.1]
        ----------
        */
        final ApplicationResources applicationResources = ApplicationResources.getInstance();
        int day = 0, week = 0;
        long duration = 0L;
        double distance = 0.0;
        final String description = event.getDescription().getValue();
        final Summary eventTitle = event.getSummary();

        // get the week and day for the running entry
        try {
            eventTitle.validate();
            final String eventTitleValue = eventTitle.getValue(); // e. g. Lauftraining: Woche 1 - Lauf Nummer: 1
            final int indexOfWeek = eventTitleValue.indexOf(Global.ICALPattern.WEEK_PATTERN);
            final int indexOfDay = eventTitleValue.indexOf(Global.ICALPattern.DAY_PATTERN);
            try {
                week = Integer.parseInt(eventTitleValue.substring(indexOfWeek
                        + Global.ICALPattern.WEEK_PATTERN.length() + 1, indexOfDay -1));
            } catch(NumberFormatException exception) {
                if (sportsLibrary.isDebugMode()) {
                    sportsLibrary.debug(exception, "Could not parse the week from summary. Set default value.");
                }
            }
            try {
                day = Integer.parseInt(eventTitleValue.substring(indexOfDay
                        + Global.ICALPattern.DAY_PATTERN.length() + 1));
            } catch(NumberFormatException exception) {
                if (sportsLibrary.isDebugMode()) {
                    sportsLibrary.debug(exception, "Could not parse the day from summary. Set default value.");
                }
            }
        } catch (ValidationException exception) {
            if (sportsLibrary.isDebugMode()) {
                sportsLibrary.debug(exception, "Event summary not valid. Set default values for week and day.");
            }
        }

        // get the duration of running plan entry (in minutes) - the first occurrences of "Dauer:"
        int startIndexOfDuration = description.indexOf(Global.ICALPattern.DURATION_PATTERN);
        if (startIndexOfDuration > -1) {
            int startIndexOfNewLine =  description.indexOf('\n', startIndexOfDuration);
            if (startIndexOfNewLine > -1) {
                String completeDurationString = description.substring(startIndexOfDuration + Global.ICALPattern.DURATION_PATTERN.length(),
                        startIndexOfNewLine - 4); // -4: ' min'
                try {
                    duration = Integer.parseInt(completeDurationString);
                } catch(NumberFormatException exception) {
                    // e.g. "Dauer: Einlaufen|Auslaufen: 20 min || Wettkampf: 63 min"
                    // try to extract from string
                    int startIndexOfDurationSeparator = 0;
                    while (true) {
                        startIndexOfDurationSeparator = completeDurationString.indexOf(":", startIndexOfDurationSeparator);
                        int startIndexOfMinutes = completeDurationString.indexOf("min", startIndexOfDurationSeparator);
                        if (startIndexOfDurationSeparator == -1 || startIndexOfMinutes == -1) {
                            break;
                        }
                        String durationString = completeDurationString.substring(startIndexOfDurationSeparator + 2, startIndexOfMinutes - 1);
                        try {
                            duration += Integer.parseInt(durationString);
                            startIndexOfDurationSeparator+= 1;
                        } catch (NumberFormatException exception1) {
                            if (sportsLibrary.isDebugMode()) {
                                sportsLibrary.debug(exception, "Could not parse the duration from description. Set default value.");
                            }
                            break;
                        }
                    }
                }
            }
        }
        // get the distance of running plan entry (in kilometer) - the first occurrences of "Distanz:"
        int startIndexOfDistance = description.indexOf(Global.ICALPattern.DISTANCE_PATTERN);
        if (startIndexOfDistance > -1) {
            int startIndexOfNewLine =  description.indexOf('\n', startIndexOfDistance);
            if (startIndexOfNewLine > -1) {
                String distanceString = description.substring(startIndexOfDistance + Global.ICALPattern.DISTANCE_PATTERN.length(),
                        startIndexOfNewLine - 3); // -3: ' km'
                try {
                    distance = Double.parseDouble(distanceString);
                } catch(NumberFormatException exception) {
                    if (sportsLibrary.isDebugMode()) {
                        sportsLibrary.debug(exception, "Could not parse the distance from description. Set default value.");
                    }
                }
            }
        }
        // set the values for running plan entry
        final RunningPlanEntry runningPlanEntry = new RunningPlanEntry();
        runningPlanEntry.setWeek(week);
        runningPlanEntry.setDay(day);
        runningPlanEntry.setDuration(duration);
        runningPlanEntry.setDistance(distance);

        // create running plan units from description
        final List<RunningUnit> runningUnits = new ArrayList<>();
        // get the type of running string - occurrences of "Lauf:", can span multiple lines
        // get the (complete) pulse string - occurrences of "Puls: 2a: 115 bis 121"
        int startIndexOfNewLineForRunningString = 0, startIndexOfNewLineForPulseString = 0;
        String typeOfRunningString, completePulseString;
        while (true) {
            RunningUnit runningUnit = new RunningUnit();
            // running information
            int startIndexOfTypeOfRunningString = description.indexOf(Global.ICALPattern.TYPE_OF_RUNNING_STRING_PATTERN, startIndexOfNewLineForRunningString);
            startIndexOfNewLineForRunningString =  description.indexOf('\n', startIndexOfTypeOfRunningString);
            if (startIndexOfTypeOfRunningString > -1 && startIndexOfNewLineForRunningString > -1) {
                typeOfRunningString = description.substring(startIndexOfTypeOfRunningString, startIndexOfNewLineForRunningString);
                runningUnit.setTypeOfRunString(typeOfRunningString);
                // get the next occurrences of "Lauf:"
                startIndexOfNewLineForRunningString+=1;
            } else {
                break;
            }

            // pulse information
            int startIndexOfCompletePulseString = description.indexOf(Global.ICALPattern.PULSE_PATTERN, startIndexOfNewLineForPulseString);
            startIndexOfNewLineForPulseString =  description.indexOf('\n', startIndexOfCompletePulseString);

            if (startIndexOfCompletePulseString > -1 && startIndexOfNewLineForPulseString > -1) {
                completePulseString = description.substring(startIndexOfCompletePulseString, startIndexOfNewLineForPulseString);
                Number[] pulseValues = getPulseFromString(completePulseString); // 0... lower, 1... upper
                runningUnit.setLowerPulseLimit(pulseValues[0].intValue());
                runningUnit.setUpperPulseLimit(pulseValues[1].intValue());
                // get the next occurrences of "Puls:"
                startIndexOfNewLineForPulseString+=1;
            }
            // add to the list
            runningUnits.add(runningUnit);
        }
        // add the units to the running plan entry
        runningPlanEntry.setRunningUnits(runningUnits);
        return runningPlanEntry;
    }

    private static Number[] getPulseFromString(@NotNull String completePulseString) {
        // get the pulse from string e.g. "Puls: 2a: 115 bis 121"
        Number[] pulseValues = new Number[2];
        int indexOfPulseString = completePulseString.indexOf(Global.ICALPattern.PULSE_PATTERN);
        if (indexOfPulseString == -1) { // pulse string was not found
            pulseValues[0] = 0;
            pulseValues[1] = 0;
            return pulseValues;
        }
        int startIndexOfOfPulseValues = completePulseString.lastIndexOf(":") + 1; // 115 bis 121
        int startIndexOfPulseSeparatorPattern = completePulseString.indexOf(Global.ICALPattern.PULSE_SEPARATOR_PATTERN);
        if (startIndexOfOfPulseValues == 0 || startIndexOfPulseSeparatorPattern == -1) {
            pulseValues[0] = 0;
            pulseValues[1] = 0;
            return pulseValues;
        }
        String lowerPulseLimitString = completePulseString.substring(0,startIndexOfPulseSeparatorPattern - 1);
        String upperPulseLimitString = completePulseString.substring(startIndexOfPulseSeparatorPattern + 1);
        try {
            pulseValues[0] = Integer.parseInt(lowerPulseLimitString);
            pulseValues[1] = Integer.parseInt(upperPulseLimitString);
            System.out.println(lowerPulseLimitString + " - " + upperPulseLimitString);
            return pulseValues;
        } catch (NumberFormatException exception) {
            pulseValues[0] = 0;
            pulseValues[1] = 0;
            return pulseValues;
        }
    }
}