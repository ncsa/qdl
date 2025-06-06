package org.qdl_lang.evaluate;

import org.qdl_lang.exceptions.FunctionArgException;
import org.qdl_lang.exceptions.QDLException;
import org.qdl_lang.exceptions.QDLExceptionWithTrace;
import org.qdl_lang.exceptions.UndefinedFunctionException;
import org.qdl_lang.expressions.Polyad;
import org.qdl_lang.functions.FKey;
import org.qdl_lang.state.NamespaceAwareState;
import org.qdl_lang.state.State;

import java.util.*;

/**
 * This is charged with managing the build-in functions as well as any that the
 * user defines.
 * <p>Created by Jeff Gaynor<br>
 * on 1/16/20 at  6:14 AM
 */
/*

This inherits from all the other built in functions and does very little except be a facade for them.
 */
public class MetaEvaluator extends AbstractEvaluator {
    /**
     * Factory method. You should not override this class to add more of your own evaluators. Just
     * get the instance and {@link #addEvaluator(int, AbstractEvaluator)} to it at
     * index 0 (or it will get missed -- last index is for functions and throws and
     * exception if no function found. It will snoop
     * through your evaluator too. If you are writing your own evaluator, your type numbers
     * should be negative.
     *
     * @return
     */
    public static MetaEvaluator getInstance() {
        if (metaEvaluator == null) {
            metaEvaluator = new MetaEvaluator();
            // NS base value. Must be distinct for new evaluators
            addE(new ModuleEvaluator());  // 12000
            addE(new StringEvaluator());  //  3000
            addE(new StemEvaluator());    //  2000
            addE(new ListEvaluator());    // 10000
            addE(new IOEvaluator());      //  4000
            addE(new SystemEvaluator());  //  5000
            addE(new MathEvaluator());    //  1000
            addE(new TMathEvaluator());   //  7000
            addE(new FunctionEvaluator()); // 6000*//*
/*
            FunctionEvaluator functionEvaluator = new FunctionEvaluator();
            metaEvaluator.addEvaluator(functionEvaluator);
            systemNamespaces.add(functionEvaluator.FUNCTION_NAMESPACE);
            lookupByNS.put(functionEvaluator.getNamespace(), functionEvaluator);
*/
        }
        return metaEvaluator;
    }

    /**
     * Add evaluator at a given index in the list
     *
     * @param index
     * @param evaluator
     */
    static protected void addE(int index, AbstractEvaluator evaluator) {
        metaEvaluator.addEvaluator(index, evaluator);  //  3000
        lookupByNS.put(evaluator.getNamespace(), evaluator);
        systemNamespaces.add(evaluator.getNamespace());
    }

    /**
     * Add evaluator to the end of the list.
     *
     * @param evaluator
     */
    static protected void addE(AbstractEvaluator evaluator) {
        metaEvaluator.addEvaluator(evaluator);  //  3000
        lookupByNS.put(evaluator.getNamespace(), evaluator);
        systemNamespaces.add(evaluator.getNamespace());
    }

    static Map<String, AbstractEvaluator> lookupByNS = new HashMap<>();

    /**
     * Given the name of a built in function, find the number of possible arguments it
     * can take. This is needed for resolving function references.
     *
     * @param name
     * @return
     */
    public int[] getArgCount(String name) {
        int ndx = name.indexOf(NamespaceAwareState.NS_DELIMITER);
        String ns = null;
        if (0 < ndx) {
            // This is a fully qualified name.
            ns = name.substring(0, ndx);
            name = name.substring(ndx + 1);

        }
        Polyad polyad = new Polyad(name);
        polyad.setSizeQuery(true);
        if (0 < ndx) {
            lookupByNS.get(ns).evaluate(polyad, null);
        } else {
            evaluate(polyad, null);
        }
        return polyad.getAllowedArgCounts();
    }


    public FunctionEvaluator getFunctionEvaluator() {
        if (functionEvaluator == null) {
            functionEvaluator = new FunctionEvaluator();
        }
        return functionEvaluator;
    }

    public void setFunctionEvaluator(FunctionEvaluator functionEvaluator) {
        this.functionEvaluator = functionEvaluator;
    }

    FunctionEvaluator functionEvaluator;

    public static boolean isSystemNS(String name) {
        return getSystemNamespaces().contains(name);
    }

    @Override
    public String getNamespace() {
        // not implemented at this level.
        return null;
    }

    public static void setMetaEvaluator(MetaEvaluator metaEvaluator) {
        MetaEvaluator.metaEvaluator = metaEvaluator;
    }

    static MetaEvaluator metaEvaluator = null;

    public static final int META_BASE_VALUE = 6000;
    List<AbstractEvaluator> evaluators = new ArrayList<>();
    Map<String, AbstractEvaluator> evaluatorsByAlias = new HashMap<>();

    public void addEvaluator(AbstractEvaluator evaluator) {
        evaluators.add(evaluator);
        evaluatorsByAlias.put(evaluator.getNamespace(), evaluator);
    }

    public void addEvaluator(int index, AbstractEvaluator evaluator) {
        evaluators.add(index, evaluator);
        evaluatorsByAlias.put(evaluator.getNamespace(), evaluator);
    }


    @Override
    public int getType(String name) {
        for (AbstractEvaluator evaluator : evaluators) {
            int type = evaluator.getType(name);
            if (type != UNKNOWN_VALUE) {
                return type;
            }
        }
        return EvaluatorInterface.UNKNOWN_VALUE;
    }

    @Override
    public boolean evaluate(Polyad polyad, State state) {
        if (state == null || !state.isAllowBaseFunctionOverrides()) {
            return evaluateOLD(polyad, state);
        }
        return evaluateNEW(polyad, state);
    }

    @Override
    public boolean dispatch(Polyad polyad, State state) {
        // Unused in this implementation
        return false;
    }

    /**
     * Proposed method to allow for overriding base system functions. Maybe. Still
     * have to decide if this is really a great idea. This can actually the
     * evaluteOLD completely <b>BUT</b> because this requires exception handling
     * to decide if a function is user defined, the performance is slower by a good 15%.
     *
     * <p>To do this right will require that the function Evaluator handle missing functions
     * differently. Probably a fairly substantial rewrite for not much of a clear purpose
     * at this point (4/27/2022)</p>
     *
     * @param polyad
     * @param state
     * @return
     */
    public boolean evaluateNEW(Polyad polyad, State state) {
        // state can be null if there is a query for the number of arguments.
        // fall through to old case since these are for built in functions only.
        if (state != null && state.isAllowBaseFunctionOverrides()) {
            try {
                if (getFunctionEvaluator().evaluate(polyad, state)) {
                    return true;
                }
            } catch (UndefinedFunctionException ufx) {
                //ok, rock on
            } catch (QDLExceptionWithTrace qdlExceptionWithTrace) {
                if (!(qdlExceptionWithTrace.getCause() instanceof UndefinedFunctionException)) {
                    throw qdlExceptionWithTrace;
                }
            }
            for (AbstractEvaluator evaluator : evaluators) {
                if (evaluator.evaluate(polyad, state)) return true;
            }

        } else {
            try {
                for (AbstractEvaluator evaluator : evaluators) {
                    if (evaluator.evaluate(polyad, state)) return true;
                }
                return getFunctionEvaluator().evaluate(polyad, state);

            } catch (FunctionArgException fax) {
                return getFunctionEvaluator().evaluate(polyad, state);
            }

        }

        throw new UndefinedFunctionException("unknown function '" + polyad.getName() + "'", polyad);
    }

    /**
     * Check that a FQ function exists. This is used withj {@link  #isSystemNS(String)} to check if
     * the namespace exists in the first place.
     * @param moduleName
     * @param functionName
     * @return
     */
    public  boolean isBuiltIin(String moduleName, String functionName) {
        if (!isSystemNS(moduleName)) return false;
        for (AbstractEvaluator evaluator : evaluators) {
            if (evaluator.getNamespace().equals(moduleName)) {
                for(String name : evaluator.getFunctionNames()){
                    if(name.equals(functionName)) return true;
                }
            }
        }
        return false;

    }

    public boolean evaluateOLD(Polyad polyad, State state) {
        FunctionArgException farg = null;
        // If there is state (so not loading a template), it is built in and there is a like-named top-level
        // function. throw and ambiguous function error
        if (state != null &&
                      !state.hasModule() && // allow overrides in modules. So foo#bar() should resolve even if there is a system function bar()
                polyad.isBuiltIn() &&
                null != state.getFTStack().get(new FKey(polyad.getName(), polyad.getArgCount()))) {
            throw new QDLExceptionWithTrace("ambiguous function. There are multiple definitions of " + polyad.getName() + "(" + polyad.getArgCount() + ")",
                    polyad);
        }
        for (AbstractEvaluator evaluator : evaluators) {
            try {
          /*  See also FunctionEvaluator.evaluate
              'a:a' no such built in causes stack over flow. Should just stop

          )ws set java_trace on
          module['a:a'][size(x)->function#size(x)+1;];
          a≔import('a:a');
          a#size([;5])
          
          module['a:b'][size(x)->stem#size(x)+1;];
          b≔import('a:b');
          b#size([;5])

          eg := j_load('eg');
          eg#concat('a','b');
          */
              if(state!= null && state.hasModule()){
                  try {
                      if (getFunctionEvaluator().evaluate(polyad, state)) {
                          return true;
                      }
                  }catch(QDLException qdlException){
                      // Fix for https://github.com/ncsa/qdl/issues/66
                      // handles this edge case: In Java Modules, a built-in function is constructed
                      // then its evaluate method is called. Hand;ing this via intercepting
                      // a thrown exception scales terribly though. // If the function is not in
                      // defined in the module (what this tests for), fall through and try it as
                      // a regular function.
                      if(!(qdlException.getCause() instanceof UndefinedFunctionException)){
                          throw qdlException;
                      }
                  }
                }
                if (evaluator.evaluate(polyad, state)) return true;
            } catch (FunctionArgException functionArgException) {
                farg = functionArgException;
                // So maybe it's overloaded??? Check user defined function.
                // If not then blow up for real
                try {
                    if (getFunctionEvaluator().evaluate(polyad, state)) {
                        return true;
                    }
                } catch (Throwable t) {
                    throw farg;
                }
            }
        }
        if (getFunctionEvaluator().evaluate(polyad, state)) {
            return true;
        }

        if (farg != null) {
            throw farg;
        }
        throw new UndefinedFunctionException("unknown function '" + polyad.getName() + "'", polyad);
    }

    public boolean evaluate(String alias, Polyad polyad, State state) {
        if (evaluatorsByAlias.containsKey(alias)) { // Explicit request, honor it
            if (evaluatorsByAlias.get(alias).evaluate(alias, polyad, state)) return true;
            throw new UndefinedFunctionException("unknown function '" + polyad.getName() + "' in module " + alias, polyad);
        }
        for (AbstractEvaluator evaluator : evaluators) {
            if (evaluator.evaluate(alias, polyad, state)) return true;
        }
        throw new UndefinedFunctionException("unknown function '" + polyad.getName() + "'", polyad);
    }

    public TreeSet<String> listFunctions(boolean listFQ) {
        TreeSet<String> names = new TreeSet<>();
        for (AbstractEvaluator evaluator : evaluators) {
            names.addAll(evaluator.listFunctions(listFQ));
        }
        return names;
    }

    String[] allNames = null;

    @Override
    public String[] getFunctionNames() {
        if (allNames == null) {
            ArrayList<String> namesList = new ArrayList<>();
            for (AbstractEvaluator evaluator : evaluators) {
                namesList.addAll(Arrays.asList(evaluator.getFunctionNames()));
            }
            allNames = new String[namesList.size()];
            int i = 0;
            for (String x : namesList) {
                allNames[i++] = x;
            }
        }
        return allNames;
    }

    static protected List<String> systemNamespaces = new ArrayList<>();

    public static List<String> getSystemNamespaces() {
        return systemNamespaces;
    }
}
