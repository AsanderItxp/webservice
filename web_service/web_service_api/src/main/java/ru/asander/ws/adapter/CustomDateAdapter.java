package ru.asander.ws.adapter;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Используется для конвертации даты в файлах с ключевой информацией, так как
 * даты в этих файлах приходят в не корректном формате.
 * @see "bindings.xjb"
 */
public class CustomDateAdapter extends XmlAdapter<String, Date> {

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
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(s);
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
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(dt);
    }
}
