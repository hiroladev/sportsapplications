package de.hirola.sportsapplications.database;

import de.hirola.sportsapplications.SportsLibraryException;
import org.dizitart.no2.Document;
import org.dizitart.no2.mapper.Mappable;
import org.dizitart.no2.mapper.NitriteMapper;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * A helper class to handle lists with embedded objects in the database.
 *
 * @author Michael Schmidt (Hirola)
 * @since v0.1
 */
public final class ListMapper {

    @Null
    public static <T extends Mappable> List<Document> toDocumentsList(@NotNull NitriteMapper mapper,
                                                                      @Null List<T> elementsList)  {
        List<Document> documentsList = null;
        if (elementsList != null)
        {
            documentsList = new ArrayList<>(elementsList.size());
            for (T element : elementsList)
            {
                documentsList.add(element.write(mapper));
            }
        }
        return documentsList;
    }

    @Null
    public static <T extends Mappable> List<T> toElementsList(@NotNull NitriteMapper mapper,
                                                              @Null List<Document> documentsList,
                                                              Class<T> typeOfElement) throws SportsLibraryException {
        List<T> elementsList = null;
        if (documentsList != null) {
            try {
                elementsList = new ArrayList<>(documentsList.size());
                for (Document document : documentsList) {
                    T element = typeOfElement.getDeclaredConstructor().newInstance();
                    element.read(mapper, document);
                    elementsList.add(element);
                }
            } catch (IllegalAccessException | InstantiationException |
                    InvocationTargetException | NoSuchMethodException exception) {
                throw new SportsLibraryException(exception);
            }
        }
        return elementsList;
    }

}
