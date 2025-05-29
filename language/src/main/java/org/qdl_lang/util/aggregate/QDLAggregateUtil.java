package org.qdl_lang.util.aggregate;

import org.qdl_lang.exceptions.BadArgException;
import org.qdl_lang.exceptions.BadStemValueException;
import org.qdl_lang.exceptions.UnknownTypeException;
import org.qdl_lang.functions.FunctionReferenceNode;
import org.qdl_lang.module.Module;
import org.qdl_lang.variables.Constant;
import org.qdl_lang.variables.QDLNull;
import org.qdl_lang.variables.QDLSet;
import org.qdl_lang.variables.QDLStem;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * The QDL utility for processing aggregates (stems and sets). This allows for processing each <i>scalar</i> in
 * an aggregate and handles
 * navigating them in turn. Implement or extends the {@link ProcessScalar} interface.
 * <h2>Processing aggregates.</h2>
 * You can do them so they follow the standard QDL stem/set contracts when implementing a system function by
 * executing the correct {@link org.qdl_lang.evaluate.AbstractEvaluator}.processN
 * in an evaluator <b><i>or</i></b> if you have something specific address each scalar using this utility.
 * This utility in particular is useful when writing Java extensions that implement {@link org.qdl_lang.extensions.QDLFunction}
 * where the machinery of {@link org.qdl_lang.evaluate.FunctionEvaluator} is not available.
 * <h3>Usage</h3>
 * If you have an aggregate and need to process scalars, call this with the right overrides to
 * {@link IdentityScalarImpl}. That class simply passes back its arguments unchanged as a default behavior.
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
     * @param processScalar implementation of {@link ProcessScalar}
     * @return
     */
    public static Object process(Object object, ProcessScalar processScalar) {
        if (object instanceof QDLStem) {
            return processStem((QDLStem) object, new LinkedList<>(), processScalar);
        }
        if (object instanceof QDLSet) {
            return processSet((QDLSet) object, processScalar);
        }
        return processSetValue( new LinkedList<>(),processScalar, object);
    }

    public static QDLStem processStem(QDLStem inStem, List<Object> index, ProcessScalar processScalar) {
        QDLStem outStem = new QDLStem();
        for (Object key : inStem.keySet()) {
            Object value = inStem.get(key);
            try {
                outStem.putLongOrString(key, processStemValue(processScalar, index, key, value));
            }catch(BadStemValueException badStemValueException){
                badStemValueException.getIndices().add(key);
                throw badStemValueException;
            }
        }
        return outStem;
    }

    private static Object processSetValue(List index, ProcessScalar processScalar, Object value) {
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
                newValue = processStem((QDLStem) value, index, processScalar);
                break;
            case Constant.STRING_TYPE:
                newValue = processScalar.process((String) value);
                break;
            case Constant.UNKNOWN_TYPE:
                throw new UnknownTypeException("error processing value '" + value + "', unknown type", null);
        }
        return newValue;
    }

    private static Object processStemValue(ProcessScalar processScalar, List index, Object key, Object value) {
        Object newValue = null;
        switch (Constant.getType(value)) {
            case Constant.BOOLEAN_TYPE:
                newValue = processScalar.process(index, key, (Boolean) value);
                break;
            case Constant.DECIMAL_TYPE:
                newValue = processScalar.process(index, key, (BigDecimal) value);
                break;
            case Constant.FUNCTION_TYPE:
                newValue = processScalar.process(index, key, (FunctionReferenceNode) value);
                break;
            case Constant.LONG_TYPE:
                newValue = processScalar.process(index, key, (Long) value);
                break;
            case Constant.MODULE_TYPE:
                newValue = processScalar.process(index, key, (Module) value);
                break;
            case Constant.NULL_TYPE:
                newValue = processScalar.process(index, key, (QDLNull) value);
                break;
            case Constant.SET_TYPE:
                newValue = processSet((QDLSet) value, processScalar);
                break;
            case Constant.STEM_TYPE:
                List newIndex = new LinkedList<>();
                newIndex.addAll(index);
                newIndex.add(key);
                newValue = processStem((QDLStem) value, newIndex, processScalar);
                break;
            case Constant.STRING_TYPE:
                newValue = processScalar.process(index, key, (String) value);
                break;
            case Constant.UNKNOWN_TYPE:
                // then it's a set
                throw new UnknownTypeException("error processing key='" + key + "', value '" + value + "', unknown type", null);
        }
        return newValue;
    }

    /**
     * Processes a value in a stem, i.e., a scalar
     * @param processStemValues
     * @param index
     * @param key
     * @param value
     * @param currentDepth
     * @return
     */
    private static Object processStemValue(ProcessStemAxisRestriction processStemValues,
                                           List index,
                                           Object key,
                                           Object value,
                                           int currentDepth) {
        Object newValue = null;
        switch (Constant.getType(value)) {
            case Constant.BOOLEAN_TYPE:
                newValue = processStemValues.process(index, key, (Boolean) value);
                break;
            case Constant.DECIMAL_TYPE:
                newValue = processStemValues.process(index, key, (BigDecimal) value);
                break;
            case Constant.FUNCTION_TYPE:
                newValue = processStemValues.process(index, key, (FunctionReferenceNode) value);
                break;
            case Constant.LONG_TYPE:
                newValue = processStemValues.process(index, key, (Long) value);
                break;
            case Constant.MODULE_TYPE:
                newValue = processStemValues.process(index, key, (Module) value);
                break;
            case Constant.NULL_TYPE:
                newValue = processStemValues.process(index, key, (QDLNull) value);
                break;
            case Constant.SET_TYPE:
                newValue = processStemValues.process(index, key, (QDLSet) value);
                break;
            case Constant.STEM_TYPE:
                newValue = processStemValues.process(index, key, (QDLStem) value);
                break;
            case Constant.STRING_TYPE:
                newValue = processStemValues.process(index, key, (String) value);
                break;
            case Constant.UNKNOWN_TYPE:
                // then it's a set
                throw new UnknownTypeException("error processing key='" + key + "', value '" + value + "', unknown type", null);
        }
        return newValue;
    }

    /**
     * Process the elements in a set, including nested sets. This will access every element in every set
     * eventually.
     * @param inSet
     * @param processScalar
     * @return
     */
    public static QDLSet processSet(QDLSet inSet, ProcessScalar processScalar) {
        QDLSet outSet = new QDLSet();
        for (Object value : inSet) {
            outSet.add(asQDLValue(processSetValue(new LinkedList(), processScalar, value)));
        }
        return outSet;
    }

    /**
     * Start processing for an axis restricted function. This will throw an exception if the argument is not a stem.

     * @param processRankRestriction
     * @return
     */
    public static QDLStem process(QDLStem stem, ProcessStemAxisRestriction processRankRestriction) {
        // start recursion
            return processStem(stem, new LinkedList<>(), processRankRestriction, 0);
    }

    public static QDLStem processStem(QDLStem inStem, List index, ProcessStemAxisRestriction processRankRestriction, int currentDepth) {
        QDLStem outStem = new QDLStem();
        for (Object key : inStem.keySet()) {
            Object value = inStem.get(key);
            try {
                if(currentDepth == processRankRestriction.getAxis()
                || processRankRestriction.getAxis() == ProcessStemAxisRestriction.ALL_AXES && !((value instanceof QDLStem))) {
                    outStem.putLongOrString(key, processStemValue(processRankRestriction, index, key, value, currentDepth + 1));
                }else{
                    if(value instanceof QDLStem){
                        List newIndex = new LinkedList<>();
                        newIndex.addAll(index);
                        newIndex.add(key);
                        outStem.putLongOrString(key,processStem((QDLStem) value, newIndex, processRankRestriction, currentDepth + 1));
                    }
                }
            }catch(BadStemValueException badStemValueException){
                badStemValueException.getIndices().add(key);
                throw badStemValueException;
            }
        }
        return outStem;
    }
}
