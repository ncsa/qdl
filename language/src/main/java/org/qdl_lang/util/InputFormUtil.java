package org.qdl_lang.util;

import ch.obermuhlner.math.big.BigDecimalMath;
import org.qdl_lang.evaluate.ModuleEvaluator;
import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.extensions.QDLFunction;
import org.qdl_lang.extensions.QDLFunctionRecord;
import org.qdl_lang.functions.FR_WithState;
import org.qdl_lang.functions.FunctionRecord;
import org.qdl_lang.functions.FunctionRecordInterface;
import org.qdl_lang.module.MTKey;
import org.qdl_lang.module.Module;
import org.qdl_lang.module.QDLModule;
import org.qdl_lang.state.State;
import org.qdl_lang.state.XKey;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.QDLSet;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import org.qdl_lang.variables.values.QDLValue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.text.DecimalFormat;


/**
 * Utility that converts various objects to their input form. Input form
 * is defined as the statement to create the object. This might
 * not reflect the current state of the object for modules since
 * that is highly mutable.
 * <p>Created by Jeff Gaynor<br>
 * on 3/10/21 at  8:38 AM
 */
public class InputFormUtil {
    public static String JAVA_CLASS_MARKER = "java:";

    public static String inputForm(QDLValue qdlValue) {
        return inputForm(qdlValue.getValue());
    }

    public static String inputForm(Object obj) {
        if (obj instanceof Boolean) return inputForm((Boolean) obj);
        if (obj instanceof Long) return inputForm((Long) obj);
        if (obj instanceof BigDecimal) return inputForm((BigDecimal) obj);
        if (obj instanceof String) return inputForm((String) obj);
        if (obj instanceof QDLStem) return inputForm((QDLStem) obj);
        if (obj instanceof QDLNull) return inputForm((QDLNull) obj);
        if(obj instanceof QDLSet) return inputForm((QDLSet)obj);
        if(obj instanceof Module) return inputForm((Module) obj);
        if(obj instanceof FunctionRecord) return inputForm((FunctionRecord) obj);
        throw new IllegalArgumentException("unknown object type: " + obj.getClass());
    }

    public static String inputForm(Boolean b) {
        return b ? "true" : "false";
    }

    public static String inputForm(Long myLong) {
        return myLong.toString();
    }

    /*
      f(x)->x*cos(x)*exp(-x);
  g(x)->x^2*sin(x)*exp(1-x)
    y. := pplot(@f(),@g(), -2,2,1000)
    list_subset(y., 5, 10)

     */
    public static String inputForm(BigDecimal d) {
        return formatBD2(d);
    }

    protected static String formatBD(BigDecimal d) {
        d = d.stripTrailingZeros();
        //  int newScale =   d.scale() - d.precision();
        //  d.setScale(OpEvaluator.getNumericDigits(), RoundingMode.HALF_DOWN);
        //d.setScale(OpEvaluator.getNumericDigits(), RoundingMode.HALF_DOWN);
        BigDecimal frac = BigDecimalMath.fractionalPart(d);

        String s = d.toString();
        int p = s.indexOf(".");
        if (s.toUpperCase().indexOf("E") != -1) {
            return s;
        }
        if (0 <= p) {
            if (s.length() - p - 1 <= OpEvaluator.getNumericDigits()) {
                // nix
            } else {
                s = s.substring(0, p + 1 + OpEvaluator.getNumericDigits());
            }
        }
        return s; // Or it spits out scientific noation like .123E-15 which bombs in the parser
    }

    public static BigDecimal top = new BigDecimal("1000000000"); // 1 billion
    public static BigDecimal bottom = new BigDecimal("-1000000000"); // -1 billion
    protected static DecimalFormat Dformatter = new DecimalFormat("0.0");
    protected static DecimalFormat Eformatter = new DecimalFormat("0.0E0");

    protected static boolean isDBInRange(BigDecimal d){
        int exp= BigDecimalMath.exponent(d);
        return -9 < exp && exp < 9;
        //return (0 <= d.compareTo(bottom)  ) && (  d.compareTo(top) <= 0);
    }
    protected static String formatBD2(BigDecimal d) {
        DecimalFormat formatter;
        if(isDBInRange(d)) {

            try {
                return Long.toString(d.longValueExact());
            } catch (ArithmeticException arithmeticException) {
                // rock on
                formatter = Dformatter;

            }
        }else{
            formatter = Eformatter;

        }

        // If the number is in a small range, just output the number. Otherwise, use
        // scientific notation.
        formatter.setRoundingMode(RoundingMode.HALF_UP);
        formatter.setMinimumFractionDigits(1);
        formatter.setMaximumFractionDigits(OpEvaluator.getNumericDigits());
        return formatter.format(d);

    }

    public static String inputForm(String s) {
        char[] chars = s.toCharArray();
        StringBuilder sb = new StringBuilder(chars.length + 25); // pad it
        for(int i = 0; i < chars.length; i++){
            char current = chars[i];
            switch(current){
                case '\\':
                    sb.append("\\\\");
                    break;
                case '\'':
                    sb.append("\\'");
                    break;
                case '\b':
                    sb.append("\\b");
                    break ;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\n':
                    sb.append("\\n");
                    break   ;
                case '\r':
                    sb.append("\\r");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                default:
                    sb.append(current);
            }
        }
/*
        s = s.replace("'", "\\'");
        s = s.replace("\b", "\\b");
        s = s.replace("\f", "\\f");
        s = s.replace("\n", "\\n");
        s = s.replace("\r", "\\r");
        s = s.replace("\t", "\\t");
*/
        return "'" + sb.toString() + "'";
    }

    /**
     * Look up a function for the given state by arg count. If there is no such function,
     * then an null string is returned.
     *
     * @param fName
     * @param argCount
     * @param state
     * @return
     */
    public static String inputForm(String fName, int argCount, State state) {
        FR_WithState fr_withState = state.resolveFunction(fName, argCount, true);

        if (fr_withState == null) {
            // no such critter
            return null;
        }
        FunctionRecordInterface fr = fr_withState.functionRecord;
        return inputForm(fr);
    }

    public  static String inputForm(FunctionRecord fr) {
        String output = null;
        if (fr != null) {
            if (fr instanceof QDLFunctionRecord) {
                QDLFunction qf = ((QDLFunctionRecord) fr).qdlFunction;
                if (qf != null) {
                    output = JAVA_CLASS_MARKER + qf.getClass().getCanonicalName();
                }
            } else {
                output = StringUtils.listToString(fr.sourceCode);
            }
        }
        return output;
    }


    public static String inputForm(QDLNull qdlNull) {
        return "null";
    }

    public static String inputForm(QDLStem stemVariable) {
        return stemVariable.inputForm();
    }

    public static String inputForm(QDLStem stemVariable, int indentFactor) {
        return stemVariable.inputForm(indentFactor);
    }

    public static String inputForm(QDLSet set){
        return set.inputForm();
    }
    /**
     * Finds the input form for a module. Note that the name is either
     * an alias, like acl or acl# (trailing # is optional) or the namespace
     * (like oa4mp:/util/acl)
     *
     * @param moduleNS
     * @param state
     * @return
     */
    public static String inputFormModule(URI moduleNS, State state) {
        return inputForm(state.getMTemplates().getModule(new MTKey(moduleNS)));
    }
    public static String inputForm(Module m) {
           if (m == null) {
               return null;
           }
           if (m instanceof JavaModule) {
               return JAVA_CLASS_MARKER + ((JavaModule) m).getClassname();
           }
           if (m instanceof QDLModule) {
               return StringUtils.listToString(((QDLModule) m).getSource());
           }
           return null;
       }

    public static String inputFormModule(String moduleName, State state) {
        // new way: get from symbol table
        Object obj = state.getValue(moduleName);
        if(obj instanceof Module){
            return inputForm((Module)obj);
        }
        // Old way: look up in MInstances
        URI moduleNS;

        if (moduleName.endsWith(State.NS_DELIMITER)) {
            moduleName = moduleName.substring(0, moduleName.length() - 1);
        }
        Module m = state.getMInstances().getModule(new XKey(moduleName));
        if(m == null){
            return null;
        }

        return inputFormModule(m.getMTKey().getUriKey(), state);
    }

    /**
     * Finds the input form for a variable from the state.
     *
     * @param varName
     * @param state
     * @return
     */
    public static String inputFormVar(String varName, int indentFactor, State state) {
        Object object = state.getValue(varName);
        if (object == null) return null;
        if (object instanceof QDLStem && 0 <= indentFactor) {
            return inputForm((QDLStem) object, indentFactor);
        }
        if(object instanceof Module){
            Module m = (Module) object;
            if(m instanceof QDLModule){
                return ModuleEvaluator.IMPORT + "('" + m.getNamespace() + "')";
            }
        }
        return inputForm(object);

    }

    public static String inputFormVar(String varName, State state) {
        return inputFormVar(varName, -1, state);
    }

    public static void main(String[] args) {
        QDLStem stemVariable = new QDLStem();
        stemVariable.put(0L, "foo0");
        stemVariable.put(1L, "foo1");
        stemVariable.put(2L, "foo2");
        stemVariable.put("long", 123L);
        stemVariable.put("string", "abc'def'786%$%$#");
        stemVariable.put("boolean", Boolean.TRUE);
        stemVariable.put("decimal", new BigDecimal("-123.3456"));
        stemVariable.put("null", QDLNull.getInstance());
        System.out.println(stemVariable.inputForm());
    }
}
