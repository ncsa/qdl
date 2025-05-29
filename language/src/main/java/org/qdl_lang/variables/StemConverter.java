package org.qdl_lang.variables;

import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.util.InputFormUtil;
import edu.uiuc.ncsa.security.core.util.Iso8601;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.qdl_lang.variables.values.QDLValue;

import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 10/30/20 at  6:13 AM
 */
public class StemConverter implements Constants {
    /**
     * Does the grunt work of taking an entry (generaic objects!) from a JSON object and converting it to something QDL can
     * understand. Used mostly in the toString methods.
     *
     * @param obj
     * @return
     */
    public static Object convert(Object obj) {
        if (obj == null) return null;
        if (obj instanceof Integer) return Long.valueOf(obj.toString());
        if (obj instanceof Double) return new BigDecimal(obj.toString(), OpEvaluator.getMathContext());
        if (obj instanceof Boolean) return obj;
        if (obj instanceof Long) return obj;
        if (obj instanceof Date) return Iso8601.date2String((Date) obj);
        if (obj instanceof String) return obj;
        if (obj instanceof JSONArray) return convert((JSONArray) obj);
        if (obj instanceof JSONObject) return convert((JSONObject) obj);
        if (obj instanceof BigDecimal) return InputFormUtil.inputForm((BigDecimal) obj);
        return obj.toString(); // catches things like URIs too...
    }


    public static QDLStem convert(JSONArray array) {
        QDLStem out = new QDLStem();
        QDLList qdlList = new QDLList();
        qdlList.addAll(array);
        out.setQDLList(qdlList);
        return out;
    }

    public static QDLStem convert(JSONObject object) {
        QDLStem out = new QDLStem();
        for (Object key : object.keySet()) {
            Object obj = object.get(key);
            out.put(key.toString(), new QDLValue(convert(obj)));
        }
        return out;
    }
}
