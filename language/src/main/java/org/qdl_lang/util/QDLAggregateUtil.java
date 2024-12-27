package org.qdl_lang.util;

import org.qdl_lang.evaluate.AbstractEvaluator;
import org.qdl_lang.exceptions.UnknownTypeException;
import org.qdl_lang.expressions.ExpressionImpl;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.module.Module;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.QDLSet;
import org.qdl_lang.variables.QDLStem;

import java.math.BigDecimal;

/**
 * The QDL utility for processing aggregates (stems and sets). This allows for processing each <i>scalar</i> in
 * an aggregate and handles
 * navigating them in turn. Implement or extends the {@link ProcessScalar} interface.
 * <h2>Processing aggregates.</h2>
 * You can do them so they follow the standard QDL stem/set contracts when implementing a system function by
 * executing the correct {@link org.qdl_lang.evaluate.FunctionEvaluator}.processN
 * in an evaluator <b><i>or</i></b> if you have something specific address each scalar using this utility.
 * This utility in particular is useful when writing Java extensions that implement {@link org.qdl_lang.extensions.QDLFunction}
 * where the machinery of {@link org.qdl_lang.evaluate.FunctionEvaluator} is not available.
 * <h3>Usage</h3>
 * If you have an aggregate and need to process scalars, call this with the right overrides to
 * {@link ProcessScalarImpl}. That class simply passes back its arguments unchanged as a default behavior.
 * <h3>Handling exceptions</h3>
 * If you use this in an extension (so a Java class that extends {@link org.qdl_lang.extensions.QDLFunction})
 * then you don't need to do anything special exception throw Java exceptions.
 * If you are using this outside of that, then throws an {@link UnknownTypeException}  for an unknown type.
 * Since this does not have the actual
 * QDL statement that triggered the error, it is set to null. You should catch the exception when calling this
 * and fill that in with the right value so the user gets the right information.
 */
public class QDLAggregateUtil {
    /**
     * Top-level call for this set of utilities. Create the right scalar processor, then call this
     * on a generic object. A scalar is processed and returned as such, aggregates are processed
     * as aggregates.
     * @param object
     * @param processScalar
     * @return
     */
    public static Object process(Object object, ProcessScalar processScalar) {
        if (object instanceof QDLStem) {
            return processStem((QDLStem) object, processScalar);
        }
        if (object instanceof QDLSet) {
            return processSet((QDLSet) object, processScalar);
        }
        return getNewSetValue(processScalar, object);
    }

    public static QDLStem processStem(QDLStem inStem, ProcessScalar processScalar) {
        QDLStem outStem = new QDLStem();
        for (Object key : inStem.keySet()) {
            Object value = inStem.get(key);
            outStem.putLongOrString(key, getNewStemValue(processScalar, key, value));
        }
        return outStem;
    }

    private static Object getNewSetValue(ProcessScalar processScalar, Object value) {
        Object newValue = null;
        switch (Constant.getType(value)) {
            case Constant.BOOLEAN_TYPE:
                newValue = processScalar.process((Boolean) value);
                break;
            case Constant.DECIMAL_TYPE:
                newValue = processScalar.process((BigDecimal) value);
                break;
            case Constant.FUNCTION_TYPE:
                newValue = processScalar.process((FunctionReferenceNode) value);
                break;
            case Constant.LONG_TYPE:
                newValue = processScalar.process((Long) value);
                break;
            case Constant.MODULE_TYPE:
                newValue = processScalar.process((Module) value);
                break;
            case Constant.NULL_TYPE:
                newValue = processScalar.process((QDLNull) value);
                break;
            case Constant.SET_TYPE:
                newValue = processSet((QDLSet) value, processScalar);
                break;
            case Constant.STEM_TYPE:
                newValue = processStem((QDLStem) value, processScalar);
                break;
            case Constant.STRING_TYPE:
                newValue = processScalar.process((String) value);
                break;
            case Constant.UNKNOWN_TYPE:
                throw new UnknownTypeException("error processing value '" + value + "', unknown type", null);
        }
        return newValue;
    }

    private static Object getNewStemValue(ProcessScalar processScalar, Object key, Object value) {
        Object newValue = null;
        switch (Constant.getType(value)) {
            case Constant.BOOLEAN_TYPE:
                newValue = processScalar.process(key, (Boolean) value);
                break;
            case Constant.DECIMAL_TYPE:
                newValue = processScalar.process(key, (BigDecimal) value);
                break;
            case Constant.FUNCTION_TYPE:
                newValue = processScalar.process(key, (FunctionReferenceNode) value);
                break;
            case Constant.LONG_TYPE:
                newValue = processScalar.process(key, (Long) value);
                break;
            case Constant.MODULE_TYPE:
                newValue = processScalar.process(key, (Module) value);
                break;
            case Constant.NULL_TYPE:
                newValue = processScalar.process(key, (QDLNull) value);
                break;
            case Constant.SET_TYPE:
                newValue = processSet((QDLSet) value, processScalar);
                break;
            case Constant.STEM_TYPE:
                newValue = processStem((QDLStem) value, processScalar);
                break;
            case Constant.STRING_TYPE:
                newValue = processScalar.process(key, (String) value);
                break;
            case Constant.UNKNOWN_TYPE:
                // then it's a set
                throw new UnknownTypeException("error processing key='" + key + "', value '" + value + "', unknown type", null);
        }
        return newValue;
    }

    public static QDLSet processSet(QDLSet inSet, ProcessScalar processScalar) {
        QDLSet outSet = new QDLSet();
        for (Object value : inSet) {
            outSet.add(getNewSetValue(processScalar, value));
        }
        return outSet;
    }
}
