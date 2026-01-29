package org.qdl_lang.variables;

import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.qdl_lang.state.State;
import org.qdl_lang.util.InputFormUtil;
import net.sf.json.JSONArray;
import org.qdl_lang.variables.values.QDLNullValue;
import org.qdl_lang.variables.values.QDLValue;

import java.math.BigDecimal;
import java.util.*;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/6/22 at  4:02 PM
 */
public class QDLSet<K extends QDLValue> extends HashSet<K>  {
    public QDLSet() {
    }

    @Override
    public boolean contains(Object o) {
        // Fix https://github.com/ncsa/qdl/issues/134
        return super.contains(QDLValue.asQDLValue(o));
    }

    /**
     * Constructor to initialize a set from another non-QDL set. This adds every
     * element of the argument, not just the argument.
     * @param set
     */
    public QDLSet(Set set) {
        addAll(set);
    }


    /**
     * JSON does not have sets, so this is a bit klugy: It will take an array and
     * stick the values into a set. This is lossy.
     *
     * @param array
     */
    public void fromJSON(JSONArray array) {
        for(Object object : array) {
            if(object instanceof JSONNull) {
                add((K)new QDLNullValue());
            }else{
                if(object instanceof JSONArray) {
                    QDLSet qdlSet = new QDLSet();
                    qdlSet.fromJSON((JSONArray) object);
                }else{
                    if(object instanceof JSONObject){
                        QDLStem stem = new QDLStem();
                        stem.fromJSON((JSONObject) object);
                        add((K)QDLValue.asQDLValue(stem));
                    }else{
                        QDLValue value = QDLValue.asQDLValue(object);
                        add((K)value);

                    }
                }
            }
        }
    }

    public JSONArray toJSON() {
        JSONArray jsonArray = new JSONArray();
        for(QDLValue element: this) {
            switch (element.getType()) {
                case QDLValue.STEM_TYPE:
                    jsonArray.add(element.asStem().toJSON());
                    break;
                    case QDLValue.SET_TYPE:
                        jsonArray.add(element.asSet().toJSON());
                        break;
                case QDLValue.NULL_TYPE:
                    jsonArray.add(JSONNull.getInstance());
                    break;
                default:
                    jsonArray.add(element.getValue());
            }
        }
        return jsonArray;
    }

    public String inputForm() {
        String out = "{";
        boolean isFirst = true;
        for (Object obj : this) {
            out = out + (isFirst ? "" : ",") + InputFormUtil.inputForm(obj);
            isFirst = false;
        }
        return out + "}";
    }

    @Override
    public String toString() {
        if(isEmpty()){
            if(State.isPrintUnicode()){
                return "∅";
            }
            return "{}";
        }
        String out = "{";
        boolean isFirst = true;
        for (Object obj : this) {
            String value;
            if (obj instanceof BigDecimal) {
                value = InputFormUtil.inputForm((BigDecimal) obj);
            } else {
                value = obj.toString();
            }
            out = out + (isFirst ? "" : ",") + value;
            isFirst = false;
        }
        return out + "}";
    }


    public QDLSet intersection(QDLSet arg) {
        QDLSet outSet = new QDLSet();
        for (QDLValue element : this) {
            if (arg.contains(element)) {
                outSet.add(element);
            }
        }
        return outSet;
    }

    public QDLSet difference(QDLSet arg) {
        QDLSet outSet = new QDLSet();
        for (QDLValue element : this) {
            if (!arg.contains(element)) {
                outSet.add(element);
            }
        }
        return outSet;
    }

    public QDLSet union(QDLSet arg) {
        QDLSet outSet = new QDLSet();
        outSet.addAll(this);
        outSet.addAll(arg);
        return outSet;
    }

    public boolean isSubsetOf(QDLSet arg) {
        for (QDLValue element : this) {
            if (!arg.contains(element)) {
                return false;
            }
        }
        return true;
    }

    public boolean isEqualTo(QDLSet arg) {
        return isSubsetOf(arg) && (size() == arg.size());
    }

    public QDLSet symmetricDifference(QDLSet arg) {
        return difference(arg).union(arg.difference(this));
    }


  protected List convertToList(){
       ArrayList list = new ArrayList();
       for(QDLValue element: this) {
           if (element.isStem()) {
               list.add(element.asSet().convertToList());
           } else {
               list.add(element);
           }
       }
        return list;
   }
    public QDLStem toStem(){
        QDLStem outStem = new QDLStem();
        outStem.getQDLList().addAll(this);
//        outStem.addList(convertToList());
        return outStem;
    }
}
