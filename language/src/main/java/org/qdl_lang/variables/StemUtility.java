package org.qdl_lang.variables;

import net.sf.json.JSONNull;
import org.qdl_lang.exceptions.RankException;
import org.qdl_lang.expressions.AxisExpression;
import org.qdl_lang.expressions.ConstantNode;
import org.qdl_lang.expressions.ExpressionStemNode;
import org.qdl_lang.expressions.VariableNode;
import org.qdl_lang.state.QDLConstants;
import org.qdl_lang.state.State;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.variables.values.LongValue;
import org.qdl_lang.variables.values.QDLKey;
import org.qdl_lang.variables.values.QDLValue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.qdl_lang.variables.QDLStem.STEM_INDEX_MARKER;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/24/21 at  12:38 PM
 */
public class StemUtility {
    public static final Long LAST_AXIS_ARGUMENT_VALUE = (long) -0xcafed00d;

    /**
     * Action to be applied at a given axis.
     */
    public interface DyadAxisAction {
        /**
         * Action to be applied
         *
         * @param out       -  the result
         * @param key       - current key
         * @param leftStem  - The left hand stem's argument at this axis
         * @param rightStem - the right hand stem's argument at this axis.
         */
        void action(QDLStem out, QDLKey key, QDLStem leftStem, QDLStem rightStem);
    }

    public static boolean isStem(Object o) {
        if(o instanceof QDLValue) {
            o = ((QDLValue)o).getValue();
        }
        return (o instanceof QDLStem) || (o instanceof AxisExpression);
    }

    public static boolean areNoneStems(Object... objects) {
        for (Object arg : objects) {
            if (isStem(arg)) return false;
        }
        return true;
    }

    public static boolean areAllStems(Object... objects) {
        for (Object arg : objects) {
            if (!isStem(arg)) return false;
        }
        return true;
    }

    /**
     * Apply some action along an axis. This will recurse to a given axis and apply an action there
     *
     * @param out0
     * @param left0
     * @param right0
     * @param depth
     * @param maxDepth
     * @param axisAction
     */
    public static void axisDayadRecursion(QDLStem out0,
                                          QDLStem left0,
                                          QDLStem right0,
                                          int depth,
                                          boolean maxDepth,
                                          DyadAxisAction axisAction) {
        QDLStem commonKeys = left0.commonKeys(right0);
        for (QDLKey key0 : commonKeys.keySet()) {
            //boolean isKey0Long = key0 instanceof Long;
            QDLValue leftQV = left0.get(key0);
            QDLValue rightQV = right0.get(key0);

            QDLStem left1 = null;
            if (leftQV.isStem()) {
                left1 =  leftQV.asStem();
            } else {
                if (rightQV == null) {
                    throw new RankException("There are no more elements in the left argument.");
                }

                left1 = new QDLStem();
                left1.put(LongValue.Zero, leftQV);
            }
            QDLStem right1 = null;
            if (rightQV.isStem()) {
                right1 = right0.get(key0).asStem();
            } else {
                if (rightQV == null) {
                    throw new RankException("There are no more elements in the right argument.");
                }
                right1 = new QDLStem();
                right1.put(LongValue.Zero, rightQV);
            }
            boolean bottomedOut = areNoneStems(leftQV, rightQV) && maxDepth && 0 < depth;
            if (bottomedOut) {
                axisAction.action(out0, key0, left1, right1);
            } else {
                if (0 < depth) {
                    if (areNoneStems(leftQV, rightQV)) {
                        throw new RankException("rank error");
                    }
                    QDLStem out1 = new QDLStem();
                    out0.put(key0, asQDLValue(out1));
                    axisDayadRecursion(out1, left1, right1, depth - 1, maxDepth, axisAction);
                } else {
                    out0.put(key0, asQDLValue(left1.union(right1)));
                }
            }
        }
    }


    /**
     * For operations that return a stem.
     */
    public interface StemAxisWalkerAction1 {
        Object action(QDLStem inStem);
    }

    /**
     * Recurses through stem that has arbitrary rank.
     *
     * @param inStem
     * @param depth
     * @param walker
     * @return
     */
    public static Object axisWalker(QDLStem inStem, int depth, StemAxisWalkerAction1 walker) {
        if (inStem.getRank() < depth + 1) {
            throw new RankException("error: axis " + depth + " requested on stem of rank " + inStem.getRank());
        }
        if (depth <= 0) {
            return walker.action(inStem);
        }
        QDLStem outStem = new QDLStem();
        for (QDLKey key1 : inStem.keySet()) {
            QDLValue obj = inStem.get(key1);
            if (!obj.isStem()) {
                continue;
            }
            //outStem.putLongOrString(key1, axisWalker(obj.asStem(), depth - 1, walker));
            outStem.put(key1, axisWalker(obj.asStem(), depth - 1, walker));
        }
        return outStem;
    }

    /*
             a.b.c.d.i(0).j := 1

             This exists because the parser was changed to try and get rid of a lot of
             annoying parentheses, e.g.
             (a.).(0).i(x).(2)

             which can now be entered as
             a.0.i(x).2
             However, the machinery for accessing stems still treats the variable references to stem
             markers as units, so 'a.b.c' is by the stem and resolved -- only the stem knows about b.c.
             Now this is a tree of elements
                  /\
               /\   c
             a   b

             So this idea si to try and reverse the process here and pass along what used to be the case to the
             system. This is horribly inefficient, since Antlr prases it, we deparse it, then reparse it in the
             stem, but the alternative is a rewrite of how stems are managed which would be a large-scale
             undertaking.
         */
    public static void doNodeSurgery(ExpressionStemNode ESN, State state) {

        ArrayList<ExpressionInterface> leftArgs = new ArrayList<>();
        ArrayList<ExpressionInterface> rightArgs = new ArrayList<>();
        ExpressionInterface swri = ESN;
        while (swri != null) {
            leftArgs.add(swri);
            if (swri instanceof ExpressionStemNode) {
                ExpressionStemNode esn = (ExpressionStemNode) swri;
                rightArgs.add(esn.getRightArg());
                swri = esn.getLeftArg();
            } else {
                swri = null;
            }
        }
        if (!(leftArgs.get(leftArgs.size() - 1) instanceof VariableNode)) {
            return; // do nothing
        }
        VariableNode actualStem = (VariableNode) leftArgs.get(leftArgs.size() - 1);
        int i = 0;
        String newVariableReference = actualStem.getVariableReference();
        boolean isFirst = true;
        for (i = rightArgs.size() - 1; 0 <= i; i--) {
            swri = rightArgs.get(i);
            boolean didIt = false;
            String nextToken = "";
            if (swri instanceof VariableNode) {
                VariableNode vNode = (VariableNode) swri;
                nextToken = vNode.getVariableReference();
                didIt = true;
            }
            if (swri instanceof ConstantNode) {
                ConstantNode cNode = (ConstantNode) swri;
                cNode.evaluate(state);
                nextToken = cNode.getResult().toString();
                didIt = true;
            }
            if (!didIt) {

                break; // jump out at first non-variable node
            }
            if (isFirst) {
                isFirst = false;
                newVariableReference = newVariableReference + nextToken;
            } else {
                newVariableReference = newVariableReference + STEM_INDEX_MARKER + nextToken;
            }
        }

        // If this ended with a "." then the r arg is set to a Java null.  This means
        // we add it back in to the new variable reference or we'll get an error
        // about setting a non-stem value.
        if (rightArgs.get(0) == null) {
            newVariableReference = newVariableReference + STEM_INDEX_MARKER;
        }
        VariableNode variableNode = new VariableNode(newVariableReference);
        ExpressionStemNode newESN;
        if (i <= 0) {
            ESN.setLeftArg(variableNode);
        } else {
            newESN = (ExpressionStemNode) leftArgs.get(i);
            newESN.setLeftArg(variableNode);
        }

        state.setValue(newVariableReference, null);
        // last one
    }

    /*
           x := 'h.i.j'
          x1 := 'h.i.j.'
         w.x := 5
        w.x1 := 10
        is_defined(w.h.i.j); // false
     w.h.i.j := 100
     w.x == w.'h.i.j'; // true
     w.x1 == (w.).'h.i.j.';  // true
     */
    // See list_formatting.txt for possible improvement to display stems.
    public static void formatList(QDLStem stem) {
        if (!stem.isList()) {
            throw new IllegalArgumentException("cannot format general stem");
        }
        for (QDLKey key : stem.keySet()) {
            Object obj = stem.get(key);
            if (obj instanceof QDLStem) {
                QDLStem stemVariable = (QDLStem) obj;
                String row = "";
                for (QDLKey key2 : stemVariable.keySet()) {
                    row = row + stemVariable.get(key2).toString() + " ";
                }

                System.out.println(row);
            } else {
                System.out.println(stem.get(key));
            }
        }
        return;
        //   }
    }

    public static void main(String[] args) {
        QDLStem outerStem = new QDLStem();
        for (int k = 0; k < 5l; k++) {
            QDLStem stemVariable0 = new QDLStem();
            for (int j = 0; j < 4; j++) {
                QDLStem stemVariable = new QDLStem();
                for (int i = 0; i < 5; i++) {
                    stemVariable.put(i, k + "_" + j + "_" + i);
                }
                stemVariable0.put(j, stemVariable);
            }
            outerStem.put(k, stemVariable0);

        }
        formatList(outerStem);
        Map map = new HashMap();
        map.put(5, 5);
        map.put("foo", true);
        map.put("bar", "woof");
        map.put(17L, "arf");
        Map map2 = new HashMap();
        List list = new ArrayList();
        list.add(3.141);
        list.add("string");
        list.add(true);
        map2.put("my_list", list);
        map2.put(11, 5);
        map2.put("foo2", true);
        map2.put("bar2", "woof");
        map.put("my_map", map2);
        System.out.println(mapToStem(map));
    }

    /**
     * Convert a stem list to a string, separated by line feeds.
     * If <code>forceToString</code> is true, then every elements toString is
     * called, otherwise, if the element is not a string, an exception is thrown.
     *
     * @param contents
     * @param forceToString
     * @return
     */
    public static String stemListToString(QDLStem contents, boolean forceToString) {
        if (contents.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (!contents.containsKey("0") && !contents.isEmpty()) {
            throw new IllegalArgumentException("Error: The given stem is not a list.");
        }
        for (int i = 0; i < contents.size(); i++) {
            QDLValue object = contents.get(Integer.toString(i));
            if (forceToString) {
                stringBuilder.append(contents.get(Integer.toString(i)) + "\n");

            } else {
                if (object.isString()) {
                    stringBuilder.append(object.asString() + "\n");
                } else {
                    throw new IllegalArgumentException("object '" + object + "' is not a string");
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Convert a generic map to a stem
     *
     * @param map
     * @return
     */
    public static QDLStem mapToStem(Map map) {
        QDLStem out = new QDLStem();
        return mapToStem(out, map);
    }

    public static QDLStem listToStem(List list) {
        QDLStem outStem = new QDLStem();
        QDLList<? extends QDLValue> out  = outStem.getQDLList();
        int i = 0;
      for(Object value : list){
          out.add((QDLValue)value);

/*
          if(value instanceof QDLValue){
              out.add((QDLValue)value);
          }else{
              out.add(asQDLValue(convert(value)));
          }
*/
      }
      return outStem;
    }
    public static QDLStem mapToStem(QDLStem out, Map map) {
        for (Object k : map.keySet()) {
            QDLKey key = QDLKey.from(k);
/*
            switch (key)) {
                case STRING_TYPE:
                case LONG_TYPE:
                    realKey = key;
                    break;
                case INT_TYPE:
                    realKey = ((Integer) key).longValue();
                    break;
                default:
                    realKey = key.toString(); // crappy, but...
            }
*/
            Object value = map.get(k);
            out.put(key, convert(value));
        }
        return out;
    }

    /**
     * Converts Java objects to one of the Java objects that QDL uses.
     * @param value
     * @return
     */
    protected static Object convert(Object value) {
        QDLStem out2;
        switch (getType(value)) {
            case INT_TYPE:
                return ((Integer) value).longValue(); // convert to a long.
            case STRING_TYPE:
            case BOOLEAN_TYPE:
            case LONG_TYPE:
                return value; // convert to a long.
            case FLOAT_TYPE:
            case DOUBLE_TYPE:
                return new BigDecimal(value.toString());
            case LIST_TYPE:
                return listToStem((List) value);
            case MAP_TYPE:
                return mapToStem( new QDLStem(), (Map) value);
            default:
            case UNKNON_TYPE:
                throw new IllegalArgumentException("unknown map entry type " + value.getClass().getCanonicalName());
        }
    }

    /*
    These values are for use by this utility only.
     */
    protected static final int UNKNON_TYPE = -1;
    protected static final int INT_TYPE = 0;
    protected static final int FLOAT_TYPE = 1;
    protected static final int DOUBLE_TYPE = 2;
    protected static final int LONG_TYPE = 3;
    protected static final int BOOLEAN_TYPE = 4;
    protected static final int STRING_TYPE = 5;
    protected static final int LIST_TYPE = 6;
    protected static final int MAP_TYPE = 7;

    protected static int getType(Object obj) {
        if (obj instanceof String) return STRING_TYPE;
        if (obj instanceof Integer) return INT_TYPE;
        if (obj instanceof Long) return LONG_TYPE;
        if (obj instanceof Boolean) return BOOLEAN_TYPE;
        if (obj instanceof Double) return DOUBLE_TYPE;
        if (obj instanceof List) return LIST_TYPE;
        if (obj instanceof Map) return MAP_TYPE;
        if (obj instanceof Float) return FLOAT_TYPE;
        return UNKNON_TYPE;
    }

    /**
     * Floats a map of values to {@link QDLValue}s in the stem. This way you don't have to mess with conversions
     * @param stem
     * @param values
     */
    public static void setStemValue(QDLStem stem, Map<String, Object> values) {
        for (String key : values.keySet()) {
            Object value = values.get(key);
            if (value instanceof QDLValue) {
                stem.put(key, (QDLValue) value);
            } else {
                stem.put(key, asQDLValue(value));
            }
        }
    }

    /**
     * Since Java's use of generics precludes a general {@link QDLStem} put on Objects, this utility does that.
     * @param stem
     * @param key
     * @param value
     */
    public static void put(QDLStem stem, Object key, Object value) {
        stem.put(QDLKey.from(key), QDLValue.asQDLValue(value));
    }

    /**
     * For use in filtering {@link QDLValue}s to POJOs (plain old java object) that can be put into JSON.
     * @param element
     * @param escapeNames
     * @param type
     * @return
     */
    public static Object convertToPOJO(QDLValue element, boolean escapeNames, int type) {
        switch (type) {
            case Constants.BOOLEAN_TYPE:
                return element.asBoolean();
            case Constants.LONG_TYPE:
                return element.asLong();
            case Constants.DECIMAL_TYPE:
                return element.asDecimal().doubleValue();
            case Constants.STRING_TYPE:
                return element.asString();
            case Constants.STEM_TYPE:
                return element.asStem().toJSON(escapeNames, type);
            case Constants.NULL_TYPE:
                //return QDLConstants.JSON_QDL_NULL;
                return JSONNull.getInstance();
            case Constants.UNKNOWN_TYPE:
                if(element.isDecimal()) return element.asDecimal().doubleValue();
                if(element.isStem()) return element.asStem().toJSON(escapeNames, type);
                if(element.isNull()) return QDLConstants.JSON_QDL_NULL;
                return element.getValue();
            case Constants.AXIS_RESTRICTION_TYPE:
            case Constants.DYADIC_FUNCTION_TYPE:
            case Constants.FUNCTION_TYPE:
            case Constants.SET_TYPE:
            case Constants.ALL_INDICES_TYPE:
            case Constants.MODULE_TYPE:
                return null;

            default:
                return null; // omit
        }
    }

}
