package ru.asander.ws.adapter;


import javax.xml.bind.DatatypeConverter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Используется для маршалинга дат из XML.
 *
 * @see "bindings.xjb"
 */
public class DateAdapter extends XmlAdapter<String, Date> {

    /**
     * Date time
     *
     * @param s - A string containing lexical representation of xsd:Date.
     * @return - Date representing the time value.
     */
    @Override
    public Date unmarshal(String s) throws Exception {
        if (s == null) {
            return null;
        }
        return DatatypeConverter.parseDateTime(s).getTime();
    }

    /**
     * Convert data as dateTime in format Gregorian calendar
     *
     * @param dt - date the given Date
     * @return - A string containing a lexical representation of xsd:datetime
     */
    @Override
    public String marshal(Date dt) throws Exception {
        if (dt == null) {
            return null;
        }
        GregorianCalendar gc = new GregorianCalendar();
        gc.setTime(dt);
        return DatatypeConverter.printDateTime(gc);
    }
}
