package org.qdl_lang.extensions.dynamodb;

import org.apache.commons.codec.binary.Base64;
import org.qdl_lang.variables.*;
import org.qdl_lang.variables.values.QDLValue;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.qdl_lang.variables.values.QDLKey.from;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * A utility class that handles converting between QDL data types and
 * Dynamo DB data types. Data conversion is
 * <pre>
 *
 +---------+-----------------+----------+
 |   QDL   |    Dynamo DB    | API code |
 +---------+-----------------+----------+
 | integer | numeric         | N        |
 | string  | string          | S        |
 | boolean | bool            | BOOL     |
 | binary  | binary          | B        |
 | null    | null            | NULL     |
 | list    | list            | L        |
 | stem    | map             | M        |
 | set     | string set      | SS       |
 | set     | number set      | NS       |
 | set     | binary set      | BS       |
 +---------+-----------------+----------+


 * </pre>
 */
public class DynamoCodec {

    /*
  Below starts the outline for writing values. There are some technical issues involved
  since roundtripping values would seem impossible. The fly in the ointment is binary values
  which end up as b64 encoded strings. Unless there is some way to track these by type,
  then they are identical to just messy strings. This implies that, perhaps, values should be
  added to them, so you get {'value':v, 'type':'b64'} or some such. Don't want to add values for
  all values since that would be redundant and make stems hugely messy.
 */
    protected Map<String, AttributeValue> stemToMap(QDLStem stem) {
        Map<String, AttributeValue> outMap = new HashMap<>();

        return outMap;

    }

    protected AttributeValue convertToAV(Long lValue) {
        return AttributeValue.builder().n(lValue.toString()).build();
    }

    protected AttributeValue convertToAV(String sValue) {
        return AttributeValue.builder().s(sValue).build();
    }

    protected AttributeValue convertToAV(BigDecimal bigDecimal) {
        return AttributeValue.builder().n(bigDecimal.toString()).build();
    }

    protected AttributeValue convertToAV(Boolean bValue) {
        return AttributeValue.builder().bool(bValue).build();
    }

    protected AttributeValue convertToAV(QDLNull bValue) {
        return AttributeValue.builder().nul(Boolean.TRUE).build();
    }

    protected AttributeValue convertToAV(QDLList<QDLValue> qdlList) {
        List<? extends QDLValue> list = qdlList.values();
        int type = -1;
        boolean isMixedType = false;
        boolean isFirstPass = true;
        for (QDLValue qdlValue : list) {
            if (isFirstPass) {
                isFirstPass = false;
                type = qdlValue.getType();
            } else {
                if (type != qdlValue.getType()) {
                    isMixedType = true;
                    break;
                }
            }
        }
        // So now we can decide how to create this.
        if (isMixedType) {

            //return AttributeValue.builder().l(sArray).build();

        }
        String[] sArray;
        switch (type) {
            case Constant.STRING_TYPE:
                sArray = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    sArray[i] = list.get(i).asString();
                }
                return AttributeValue.builder().ss(sArray).build();
            case Constant.LONG_TYPE:
            case Constant.DECIMAL_TYPE:
                sArray = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    sArray[i] = list.get(i).asString();
                }
                return AttributeValue.builder().ns(sArray).build();
            case Constant.BOOLEAN_TYPE:
                // Actually Dynamo DB only supports single boolean, so a list/set of them
                // is impossible.
                //     return AttributeValue.builder().bs(sArray).build();

        }

        return null;
    }


    /**
     * Convert a map type in Dynamo to a stem. This will follow all entries and convert
     * them throughout the map.
     * @param items
     * @return
     */
    protected QDLStem mapToStem(Map<String, AttributeValue> items) {
        QDLStem out = new QDLStem();
        for (String key : items.keySet()) {
            AttributeValue attributeValue = items.get(key);
            out.put(from(key), convertToQDL(attributeValue));
        }
        return out;
    }

    /**
     * Convert Dynamo DB attributes to corresponding QDL types.
     * @param attributeValue
     * @return
     */
    protected Object convertToQDL(AttributeValue attributeValue) {
        QDLList list;
        QDLStem stem;
        QDLSet set;
        switch (attributeValue.type()) {
            case S:
                return attributeValue.s();
            case BOOL:
                return attributeValue.bool();
            case B:
                return convertToB64(attributeValue);
            case NUL:
                return QDLNull.getInstance();
            case N:
                return convertToNumber(attributeValue.n());
            case BS: // list of bytes, each entry is a byte array
                List<SdkBytes> bytesList = attributeValue.bs();
                set = new QDLSet(bytesList.size());
                for (SdkBytes bbb : bytesList) {
                    set.add(asQDLValue(convertToB64(bbb)));
                }
                return set;
            case SS: // set of strings
                set = new QDLSet(attributeValue.ss().size());
                List<Object> ll = new ArrayList<>(attributeValue.ss().size());
                ll.add(attributeValue.s());
                set.addAll(QDLValue.castToQDLValues(ll));
/*
              list = new QDLList();
                list.addAll(attributeValue.ss());
                stem = new QDLStem();
                stem.setQDLList(list);
*/
                return set;
            case NS: // set of numbers
                set = new QDLSet(attributeValue.ns().size());
                for(String n : attributeValue.ns()) {
                    // Note that the result is always a set of string, so we have to convert.
                    BigDecimal bigDecimal = new BigDecimal(n);
                    try{
                        set.add(asQDLValue(bigDecimal.longValueExact()));
                    }
                    catch(ArithmeticException ax){
                        set.add(asQDLValue(bigDecimal));
                    }
                }
                List<Object> lll = new ArrayList<>(attributeValue.ns().size());
                lll.addAll(attributeValue.ns());
                set.addAll(QDLValue.castToQDLValues(lll));
                return set;
            case L: // mixed list type
                List<AttributeValue> listValues = attributeValue.l();
                list = new QDLList();
                for (AttributeValue n : listValues) {
                    list.add(asQDLValue(convertToQDL(n)));
                }
                stem = new QDLStem();
                stem.setQDLList(list);
                return stem;
            case M: // mixed map
                stem = new QDLStem();
                Map<String, AttributeValue> m = attributeValue.m();
                for (String key : m.keySet()) {
                    stem.put(from(key), convertToQDL(m.get(key)));
                }
                return stem;
            default:
                throw new IllegalArgumentException("unknown attribute type '" + attributeValue.type() + "'");
        }
    }

    Object convertToNumber(String v) {
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException nfx) {

        }
        return new BigDecimal(v);
    }

    String convertToB64(SdkBytes sdkBytes) {
        return Base64.encodeBase64URLSafeString(sdkBytes.asByteArray());
    }

    String convertToB64(AttributeValue attributeValue) {
        return convertToB64(attributeValue.b());
    }

}
