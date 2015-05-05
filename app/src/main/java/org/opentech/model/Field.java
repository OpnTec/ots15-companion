package org.opentech.model;

import java.util.ArrayList;

/**
 * Created by Abhishek on 12/04/15.
 */
public class Field {

    private String fieldName;
    private String type;
    private String defaultValue;

    public Field(String fieldName, String type, String defaultValue) {
        this.fieldName = fieldName;
        this.type = type;
        this.defaultValue = defaultValue;


    }

    public static String createDatabaseQuery(ArrayList<Field> fields, String tableName) {

        String intFormatter = "`%s` %s DEFAULT %s";
        String stringFormatter = "`%s` %s DEFAULT '%s'";

        String query = "CREATE TABLE " + tableName
                + " (";
        for(Field field : fields) {
            String columnFormatted;

            if(field.type.equals("INTEGER")) {

                try {
                    Integer.parseInt(field.defaultValue);
                    columnFormatted = String.format(intFormatter, field.fieldName, field.type, field.defaultValue);
                }
                catch (NumberFormatException e) {
                    columnFormatted = String.format(intFormatter, field.fieldName, field.type, 0);
                }

            }
            else {
                columnFormatted = String.format(stringFormatter, field.fieldName, field.type, field.defaultValue);
            }
            query = query + columnFormatted;

            if(fields.indexOf(field) != fields.size() - 1) {
                query = query + ", ";
            }

        }
        query = query + ");";
        return query;

    }

}
