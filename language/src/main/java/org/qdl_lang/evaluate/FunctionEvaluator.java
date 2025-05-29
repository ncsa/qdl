package org.qdl_lang.evaluate;

import org.qdl_lang.exceptions.*;
import org.qdl_lang.expressions.*;
import org.qdl_lang.extensions.JavaModule;
import org.qdl_lang.extensions.QDLFunctionRecord;
import org.qdl_lang.statements.ExpressionInterface;
import org.qdl_lang.statements.LocalBlockStatement;
import org.qdl_lang.statements.Statement;
import org.qdl_lang.util.QDLVersion;
import edu.uiuc.ncsa.security.core.exceptions.NFWException;
import edu.uiuc.ncsa.security.core.util.DebugUtil;
import org.qdl_lang.functions.*;
import org.qdl_lang.state.*;
import org.qdl_lang.variables.*;
import org.qdl_lang.variables.values.QDLValue;
import org.qdl_lang.variables.values.StringValue;
import software.amazon.awssdk.services.medialive.model.EpochLockingSettings;

import java.util.*;

import static org.qdl_lang.state.QDLConstants.FUNCTION_REFERENCE_MARKER;
import static org.qdl_lang.state.QDLConstants.FUNCTION_REFERENCE_MARKER2;
import static org.qdl_lang.variables.Constant.STEM_TYPE;
import static org.qdl_lang.variables.Constants.LIST_TYPE;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/22/20 at  10:53 AM
 */
public class FunctionEvaluator extends AbstractEvaluator {
    public static long serialVersionUID = 0xcafed00d2L;
    public static final String FUNCTION_NAMESPACE = "function";

    @Override
    public String getNamespace() {
        return FUNCTION_NAMESPACE;
    }

    public static final int BASE_FUNCTION_VALUE = 6000;
    public static final String IS_FUNCTION = "is_function";
    public static final int IS_FUNCTION_TYPE = 1 + BASE_FUNCTION_VALUE;

    public static final String APPLY = "apply";
    public static final int APPLY_TYPE = 2 + BASE_FUNCTION_VALUE;
    public static final String NAMES = "names";
    public static final int NAMES_TYPE = 3 + BASE_FUNCTION_VALUE;
    public static final String ARG_COUNT = "arg_count";
    public static final int ARG_COUNT_TYPE = 3 + BASE_FUNCTION_VALUE;

    @Override
    public int getType(String name) {
        switch (name) {
            case IS_FUNCTION:
                return IS_FUNCTION_TYPE;
            case APPLY:
                return APPLY_TYPE;
            case NAMES:
                return NAMES_TYPE;
            case ARG_COUNT:
                return ARG_COUNT_TYPE;
        }
        // At parsing time, the function definition class sets the value manually,
        // so call to this should ever get anything other than unknown value.
        return UNKNOWN_VALUE;
    }


    @Override
    public String[] getFunctionNames() {
        if (fNames == null) {
            fNames = new String[]{IS_FUNCTION, APPLY, NAMES, ARG_COUNT};
        }
        return fNames;
    }

    @Override
    public boolean evaluate(String alias, Polyad polyad, State state) {
        // Fix https://github.com/ncsa/qdl/issues/57 If it's a java module, check the name first.
        // We ended up here because we may have an overridden name for one of the functions
        // in this module. Check first if it is overridden.
        // However, we do NOT want to do this all the time, i.e. for every call,
        // since it really slows down system performance.
        // this next conditional won't apply unless the call has either
        // (1) a fully qualified call, like my_module#apply (but not apply, which is unqualified)
        // (2) the same name as one of these built-ins but in a Java module. QDL modules are
        //     handled with their overrides elsewhere (in the parser, actually, since there is a
        //     static list of built in system names, see tests at
        //     {@link ModuleTests#testOverloadOfSystemFunction)}
        //     {@link ModuleTests#testBadSystemNamespace}.
        if (state != null && state.hasModule() && (state.getModule() instanceof JavaModule)) {
            try {
                figureOutEvaluation(polyad, state, !polyad.hasAlias());
                return true;
            } catch (UndefinedFunctionException ufe) {
                // special case this one QDLException so it gives useful user feedback.
                QDLExceptionWithTrace qq = new QDLExceptionWithTrace(ufe, polyad);
                throw qq;
            } catch (QDLException qe) {
                throw qe;
            } catch (Throwable t) {
                QDLExceptionWithTrace qq = new QDLExceptionWithTrace(t, polyad);
                throw qq;
            }
        }
        switch (polyad.getName()) {
            case IS_FUNCTION:
                doIsFunction(polyad, state);
                return true;
            case APPLY:
                doApply(polyad, state);
                return true;
            case ARG_COUNT:
                doArgCount(polyad, state);
                return true;
            case NAMES:
                doArgNames(polyad, state);
                return true;

        }
        // not a module, not built in and see if the function is defined in the ambient space.
        try {
            figureOutEvaluation(polyad, state, !polyad.hasAlias());
            return true;
        } catch (UndefinedFunctionException ufe) {
            // special case this one QDLException so it gives useful user feedback.
            QDLExceptionWithTrace qq = new QDLExceptionWithTrace(ufe, polyad);
            throw qq;
        } catch (QDLException qe) {
            throw qe;
        } catch (Throwable t) {
            QDLExceptionWithTrace qq = new QDLExceptionWithTrace(t, polyad);
            throw qq;
        }
    }

    private void doArgNames(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() == 1) {
            doMonadicArgNames(polyad, state);
            return;
        }
        if (polyad.getArgCount() == 2) {
            doDyadicArgNames(polyad, state);
            return;
        }
        throw new ExtraArgException("extra argument(s) in " + NAMES, polyad.getArgAt(2));
    }

    private void doDyadicArgNames(Polyad polyad, State state) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                if (!(objects[0] instanceof FunctionReferenceNode)) {
                    throw new QDLExceptionWithTrace(NAMES + " requires an function reference as its first argument.", polyad.getArgAt(0));
                }
                FunctionReferenceNode fNode = (FunctionReferenceNode) objects[0];
                if (!(objects[1] instanceof Long)) {
                    throw new QDLExceptionWithTrace(NAMES + " requires an integer as its second argument.", polyad.getArgAt(1));
                }
                int argCount = ((Long) objects[1]).intValue();
                FunctionRecordInterface fRec = fNode.getByArgCount(argCount);
                QDLStem out = new QDLStem();
                if (fRec == null) {
                    throw new BadArgException("no such function '" + fNode.getFunctionName() + "' with " + argCount + " arguments", polyad.getArgAt(1));
                }
                out.getQDLList().addAll(fRec.getArgNames());
                r.result = asQDLValue(out);
                return r;
            }
        };
        process2(polyad, pointer, NAMES, state);

    }

    public static final String DUMMY_BUILT_IN_FUNCTION_NAME_CAPUT = "x_";

    private void doMonadicArgNames(Polyad polyad, State state) {
        fPointer pointer = new fPointer() {
            @Override
            public fpResult process(Object... objects) {
                fpResult r = new fpResult();
                if (!(objects[0] instanceof FunctionReferenceNodeInterface)) {
                    throw new QDLExceptionWithTrace(NAMES + " requires an function reference as its argument.", polyad.getArgAt(0));
                }
                if(objects[0] instanceof DyadicFunctionReferenceNode){
                    DyadicFunctionReferenceNode df = (DyadicFunctionReferenceNode) objects[0];
                    QDLStem current = new QDLStem();
                    List argNames = df.getFunctionRecord().getArgNames();
                    current.getQDLList().addAll(argNames);
                    r.result = asQDLValue(current);
                    return r;
                }
                FunctionReferenceNode fNode = (FunctionReferenceNode) objects[0];
                TreeMap<Integer, QDLStem> args = new TreeMap<>(); // sort them!
                if (fNode.getFunctionRecords().isEmpty()) {
                    //...
                    int[] argCounts;
                    if (state.getMetaEvaluator().isBuiltInFunction(fNode.getFunctionName())) {
                        Polyad polyad = new Polyad(fNode.getFunctionName());
                        polyad.setSizeQuery(true);
                        state.getMetaEvaluator().evaluate(polyad, state);
                        argCounts = polyad.getAllowedArgCounts();

                    } else {
                        // final case, this might be just an operator. Operators like +
                        // are processed in the OpEvaluator, so check there.
                        if (!state.getOpEvaluator().isMathOperator(fNode.getFunctionName())) {
                            throw new UnknownSymbolException("no such function named '" + fNode.getFunctionName() + "'", polyad.getArgAt(0));
                        }
                        argCounts = state.getOpEvaluator().getArgCount(fNode.getFunctionName());
                    }
                    for (int argCount : argCounts) {
                        QDLStem current = new QDLStem();
                        for (int i = 0; i < argCount; i++) {
                            current.getQDLList().add(asQDLValue(DUMMY_BUILT_IN_FUNCTION_NAME_CAPUT + i));
                        }
                        args.put(argCount, current);
                    }
                    //...

                } else {
                    for (FunctionRecordInterface fRec : fNode.getFunctionRecords()) {
                        QDLStem current = new QDLStem();
                        List argNames = fRec.getArgNames();
                        current.getQDLList().addAll(argNames);
                        args.put(argNames.size(), current);
                    }
                }
                QDLStem out = new QDLStem();
                for (Integer ndx : args.keySet()) {
                    out.getQDLList().add(asQDLValue(args.get(ndx)));

                }
                r.result = asQDLValue(out);
                return r;
            }
        };
        process1(polyad, pointer, NAMES, state);
    }


    private void doArgCount(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1});
            polyad.setEvaluated(true);
            return;
        }
        Monad monad = new Monad(OpEvaluator.APPLY_OP_VALUE, false);
        monad.setArgument(polyad.getArgAt(0));
        ExpressionImpl newPoly = monad;
        Object result = newPoly.evaluate(state);
        polyad.setEvaluated(true);
        polyad.setResult(result);
    }

    private void doApply(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        ExpressionImpl newPoly = null;
        if(polyad.getArgCount() == 1){
            Monad monad = new Monad(OpEvaluator.APPLY_OP_VALUE, false);
            monad.setArgument(polyad.getArgAt(0));
            newPoly = monad;
        }
        if(polyad.getArgCount() == 2){
            Dyad dyad = new Dyad(OpEvaluator.APPLY_OP_VALUE);
            dyad.setLeftArgument(polyad.getArgAt(1));
            dyad.setRightArgument(polyad.getArgAt(0));
            newPoly = dyad;
        }
        if(newPoly == null){
            throw new NFWException("Unknown number of arguments to " + OpEvaluator.APPLY_OP_KEY + " allowed");
        }
        // The arguments swap when in function notation.
        Object result = newPoly.evaluate(state);
        polyad.setEvaluated(true);
        polyad.setResult(result);
    }


    protected void doIsFunction(Polyad polyad, State state) {
        if (polyad.isSizeQuery()) {
            polyad.setAllowedArgCounts(new int[]{1, 2});
            polyad.setEvaluated(true);
            return;
        }
        if (polyad.getArgCount() < 1) {
            throw new MissingArgException(IS_FUNCTION + " requires at least 1 argument", polyad);
        }

        if (2 < polyad.getArgCount()) {
            throw new ExtraArgException(IS_FUNCTION + " requires at most 2 argument", polyad.getArgAt(1));
        }
        boolean isScalarArgCount = false;
        Long argCount = -1L;
        QDLStem argCounts = null;
        if (polyad.getArgCount() == 2) {
            QDLValue object2 = polyad.evalArg(1, state);
            switch (object2.getType()) {
                case Constant.LONG_TYPE:
                    isScalarArgCount = true;
                    argCount = object2.asLong();
                    argCounts = new QDLStem();
                    argCounts.put(0L, asQDLValue(object2));
                    break;
                case LIST_TYPE:
                case STEM_TYPE:
                    argCounts = object2.asStem();
                    isScalarArgCount = false;
                    // ok
                    break;
                case Constant.NULL_TYPE:
                    argCount = -1L;
                    isScalarArgCount = true;
                    break;
                default:
                    throw new BadArgException(" The argument count must be a number.", polyad.getArgAt(1));
            }
        } else {
            argCount = -1L;
            isScalarArgCount = true;
        }

        if (polyad.getArgAt(0) instanceof ModuleExpression) {
            ModuleExpression me = (ModuleExpression) polyad.getArgAt(0);
            State lastState = me.getModuleState(state);
            ModuleExpression lastME = me;
            while (lastME.getExpression() instanceof ModuleExpression) {
                lastME = (ModuleExpression) lastME.getExpression();
                lastState = lastME.getModuleState(lastState);
            }
            Polyad pp = new Polyad(IS_FUNCTION);
            pp.addArgument(lastME.getExpression());
            if (polyad.getArgCount() == 2) {
                pp.addArgument(polyad.getArgAt(1));
            }
            lastState.getMetaEvaluator().evaluate(pp, lastState);
            polyad.setResult(pp.getResult());
            polyad.setEvaluated(true);
            return;

        }
        switch (polyad.getArgAt(0).getNodeType()) {
            case ExpressionImpl.VARIABLE_NODE:
                VariableNode vNode = (VariableNode) polyad.getArgAt(0);
                if (isScalarArgCount) {
                    polyad.setResult(checkIsFunction(vNode.getVariableReference(), argCount.intValue(), state));
                } else {
                    QDLStem x = new QDLStem();
                    for (Object k : argCounts.keySet()) {
                        QDLValue v = argCounts.get(k);
//                        boolean gotOne = false;
                        switch (v.getType()){
                            case Constant.LONG_TYPE:
                                x.putLongOrString(k, asQDLValue(checkIsFunction(vNode.getVariableReference(), v.asLong().intValue(), state)));
                                break;
                                case Constant.NULL_TYPE:
                                    x.putLongOrString(k, asQDLValue(checkIsFunction(vNode.getVariableReference(), -1, state)));
                                    break;
                            default:
                                throw new BadArgException("arg count element at " + k + " is not a valid", polyad.getArgAt(1));

                        }

                    }
                    polyad.setResult(x);
                }
                polyad.setEvaluated(true);
                return;
            case ExpressionInterface.LIST_NODE:
                StemListNode stemListNode = (StemListNode) polyad.getArgAt(0);
                QDLList out = new QDLList();
                for (int i = 0; i < stemListNode.getStatements().size(); i++) {
                    ExpressionInterface ei = stemListNode.getStatements().get(i);
                    if (ei.getNodeType() == ExpressionImpl.VARIABLE_NODE) {
                        VariableNode vNode2 = (VariableNode) ei;
                        if (isScalarArgCount) {
                            out.add(asQDLValue(checkIsFunction(vNode2.getVariableReference(), argCount.intValue(), state)));
                        } else {
                            Long longKey = (long) i;
                            // process as list
                            if (argCounts.containsKey(longKey)) {
                                QDLValue v = argCounts.get(longKey);
                                if (v.isLong()) {
                                    out.add(asQDLValue(checkIsFunction(vNode2.getVariableReference(), v.asLong().intValue(), state)));
                                } else {
                                    throw new BadArgException("arg count element at " + i + " is not a valid", polyad.getArgAt(1));
                                }

                            }
                        }
                    }
                }
                // It's really a list and that means all the keys are just longs
                polyad.setResult(new QDLStem(out));
                polyad.setEvaluated(true);
                return;
            case ExpressionInterface.STEM_NODE:
                StemVariableNode stemVariableNode = (StemVariableNode) polyad.getArgAt(0);
                QDLStem out2 = new QDLStem();
                for (StemEntryNode stemEntryNode : stemVariableNode.getStatements()) {
                    QDLValue key = stemEntryNode.getKey().evaluate(state);
                    if (stemEntryNode.getValue() instanceof VariableNode) {
                        VariableNode vNode2 = (VariableNode) stemEntryNode.getValue();
                        if (isScalarArgCount) {
                            out2.putLongOrString(key, asQDLValue(checkIsFunction(vNode2.getVariableReference(), argCount.intValue(), state)));
                        } else {
                            // do subsetting directly
                            if (argCounts.containsKey(key.getValue())) {
                                QDLValue v = argCounts.get(key);
                                if (v.isLong()) {
                                    out2.putLongOrString(key.getValue(), asQDLValue(checkIsFunction(vNode2.getVariableReference(), v.asLong().intValue(), state)));
                                } else {
                                    throw new BadArgException("arg count element at " + key + " is not a valid", polyad.getArgAt(1));
                                }
                            }
                        }
                    } else {
                        throw new BadArgException("left hand element at " + key + " is not a valid argument", polyad.getArgAt(0));
                    }
                }
                polyad.setResult(out2);
                polyad.setEvaluated(true);
                return;
            default:
                throw new BadArgException("left hand element is not a valid argument", polyad.getArgAt(0));
        }
    }

    protected Boolean checkIsFunction(String fName, int argCount, State state) {
        try {
            if (argCount < 0) {
                return state.getFTStack().containsKey(new FKey(fName, argCount));
            } else {
                return state.resolveFunction(fName, argCount, true).functionRecord != null;
            }
        } catch (UndefinedFunctionException ufx) {

        }
        return Boolean.FALSE;
    }

    /*
      m := '/home/ncsa/dev/ncsa-git/qdl/language/src/main/resources/modules/test.mdl'
  q :=module_load(m)
  )ws set debug on
   module_import(q)
     X#get_private()

     */
    @Override
    public boolean evaluate(Polyad polyad, State state) {
        return dispatch(polyad, state);
    }

    public boolean dispatch(Polyad polyad, State state) {
        return evaluate(null, polyad, state);
    }

    protected boolean isFDef(Statement statement) {
        return (statement instanceof LambdaDefinitionNode) || (statement instanceof FunctionDefinitionStatement) || (statement instanceof FunctionReferenceNode);
    }

    /**
     * Executes a user-defined function, defined in Java as an implementation of {@link org.qdl_lang.extensions.QDLFunction}.
     * @param polyad
     * @param state
     * @param frs
     * @throws Throwable
     */
    protected void doJavaFunction(Polyad polyad, State state, FR_WithState frs) throws Throwable {
        // Contains a java function that is wrapped in a QDLFunction. The polyad here contains the
        // arguments that are needed to unpack this.
        QDLValue[] argList = new QDLValue[polyad.getArgCount()];
        for (int i = 0; i < polyad.getArgCount(); i++) {
            if (isFDef(polyad.getArguments().get(i))) {
                // Can't do getOperator since we do not know how many other arguments
                // are functions or constants.
                argList[i] = new QDLValue(getFunctionReferenceNode(state, polyad.getArguments().get(i))); // should resolve to one fo 2 node types for QDL value
            } else {
                if (polyad.hasEvaluatedArgs()) {
                    argList[i] = polyad.getEvaluatedArgs().get(i);
                } else {
                    argList[i] = polyad.getArguments().get(i).evaluate(state);
                }
            }
        }
        QDLFunctionRecord qfr = (QDLFunctionRecord) frs.functionRecord;
        if (qfr == null) {
            throw new UndefinedFunctionException("this function is not defined", polyad);
        }Object result = null;
        try {
            // This is the direct analog of func(polyad, state):
            result = qfr.qdlFunction.evaluate(argList, state);
        }catch(BadArgException badArgException){
            if(!badArgException.hasStatement()){
                if(-1 < badArgException.getArgIndex()){
                    badArgException.setStatement(polyad.getArguments().get(badArgException.getArgIndex()));
                }
            }
            throw badArgException;
        }catch(QDLExceptionWithTrace qdlExceptionWithTrace){
            if(!qdlExceptionWithTrace.hasStatement()){
                qdlExceptionWithTrace.setStatement(polyad);
            }
            throw qdlExceptionWithTrace;
        }
        if(result == null){
            // Functions should never return a Java null, but iit does happen by accident, so better
            // to bomb here rather than get a strange, completely unrelated NPE later.
            throw new QDLExceptionWithTrace("illegal java return value of null", polyad);
        }
        polyad.setResult(result);
        polyad.setEvaluated(true);
    }

    protected boolean tryScript(Polyad polyad, State state) {
        if (!state.isEnableLibrarySupport() || state.getLibPath().isEmpty()) {
            return false;
        }
        String scriptName = polyad.getName() + QDLVersion.DEFAULT_FILE_EXTENSION;
        Polyad tryScript = new Polyad(SystemEvaluator.RUN_COMMAND);
        ConstantNode constantNode = new ConstantNode(new StringValue(scriptName));
        tryScript.getArguments().add(constantNode);
        for (int i = 0; i < polyad.getArgCount(); i++) {
            tryScript.getArguments().add(polyad.getArguments().get(i));
        }
        try {
            SystemEvaluator.runnit(tryScript, state, state.getLibPath(), true, false);
            tryScript.evaluate(state);
            polyad.setResult(tryScript.getResult());
            polyad.setEvaluated(true);
            return true;
        } catch (Throwable t) {
            DebugUtil.trace(this, ".tryScript failed:");
            if (DebugUtil.isEnabled()) {
                t.printStackTrace();
            }
        }
        return false;
    }

    protected void figureOutEvaluation(Polyad polyad, State state, boolean checkForDuplicates) throws Throwable {
        FR_WithState frs = null;
        try {
            if (AbstractState.isIntrinsic(polyad.getName()) && polyad.hasAlias()) {
                // if it is in a module and at the top of the stack, then this is an access violation
                if (state.getIntrinsicFunctions().localHas(new FKey(polyad.getName(), polyad.getArgCount()))) {
           //     if (state.getFTStack().localHas(new FKey(polyad.getName(), polyad.getArgCount()))) {
                    throw new IntrinsicViolation("cannot access intrinsic function directly.", polyad);
                }
            }
            /*
            f(x,y)→x*y;
2@f∀[[1;5],[2;6],[3;7]]
             */
            if(polyad instanceof UserFunction ){
                UserFunction userFunction = (UserFunction) polyad;
                if(userFunction.hasFunctionRecord()){
                    if (((UserFunction) polyad).hasFR_WithState()){
                        frs = (FR_WithState) ((UserFunction) polyad).getFunctionRecord();
                    }else{
                        frs = new FR_WithState(userFunction.getFunctionRecord(),state);
                    }
                }
            }else {
                frs = state.resolveFunction(polyad, checkForDuplicates); // Do the heavy work of getting it
            }
        } catch (UndefinedFunctionException udx) {
            if (!state.isEnableLibrarySupport()) {
                throw udx; // don't try to resolve libraries if support is off.
            }
            if (!tryScript(polyad, state)) {
                throw udx;
            }
            return; // if it gets here, then the script worked, exit gracefully.
        }
        if (frs.isJavaFunction()) {
            doJavaFunction(polyad, state, frs);
        } else {
            doFunctionEvaluation(polyad, state, frs);
        }
    }

    /**
     * Executes a user-defined function, defined in QDL., E.g.
     * <pre>
     *     f(x) → x;
     * </pre>
     * @param polyad
     * @param state
     * @param frs
     * @throws Throwable
     */
    protected void doFunctionEvaluation(Polyad polyad, State state, FR_WithState frs) throws Throwable {
        FunctionRecordInterface functionRecord = frs.functionRecord;
        State moduleState = null;
        if (frs.isModule) {
            moduleState = (State) frs.state;
        }
        if (functionRecord == null) {
            // see if it's a reference instead
            functionRecord = state.getFTStack().getFunctionReference(polyad.getName());
            if (functionRecord == null) {
                throw new UndefinedFunctionException(" the function '" + polyad.getName() + "' with "
                        + polyad.getArgCount() + " arguments was not found.", polyad);
            }
        }

        State localState;
        if (moduleState == null) {

            if (functionRecord.isLambda() || functionRecord.isFuncRef()) {
                localState = state.newLocalState();
            } else {
                localState = state.newFunctionState();
            }

        } else {
            if (functionRecord.isLambda()) {
                localState = state.newLocalState(moduleState);
            } else {
                /*
                 At this point there is not much of a chance to avoid java serialization here.
                 The reason is that the state at this point may have function references with state
                 (as arguments to functions) which contain the full set of executable statements
                 and the local state it should run over. It takes a lot of work to figure out the
                 right scope, state object and function reference and that is why it is captured.
                 However, evaluating it means that it may be used repeatedly (e.g. in an embedded loop
                 or a recursive function that needs its own stack)
                 and we cannot have the original state altered, hence we need a local copy for
                 local operations.

                 These statements are the body of the function parsed from QDL and therefore are
                 an arbitrary network of arbitrary complexity. To get another
                 way of serializing them would involve essentially writing a complete implementation
                 of a parser to handle all possible edge cases -- at least as complex as writing the
                 QDL parser by hand. And that would of course break the next time the parser
                 gets updated.
                 */
                localState = StateUtils.javaClone(state).newFunctionState();
            }
        }
        localState.setWorkspaceCommands(state.getWorkspaceCommands());
        localState.setModuleState(state.isModuleState() || localState.isModuleState()); // it might have been set,
        // we are going to write local variables here and they MUST get priority over already exiting ones
        // but without actually changing them (or e.g., recursion is impossible).
        for (int i = 0; i < polyad.getArgCount(); i++) {
            if (polyad.getArguments().get(i) instanceof LambdaDefinitionNode) {
                LambdaDefinitionNode ldn = (LambdaDefinitionNode) polyad.getArguments().get(i);
                if (!ldn.hasName()) {
                    ldn.getFunctionRecord().setName(tempFname(state));
                    // This is anonymous
                }
                ldn.evaluate(localState);
            }
        }

        // now we populate the local state with the variables.

        /*
        Note that the paramList is a listing of all variables and possible overloaded functions
        There may be lots of overloaded functions. These are then systematically added to the
        states later. This is not the argument list passed in to the function -- that is not changed.
         */
        ArrayList<XThing> foundParameters = resolveArguments(functionRecord, polyad, state, localState);


        if (functionRecord.isFuncRef()) {
            String realName = functionRecord.getfRefName();
            if (state.getOpEvaluator().isMathOperator(realName)) {
                // Monads and Dyads are reserved for math operations and are smart enough
                // to grab the OpEvaluator and invoke it, so if the user is sending along
                // a polyad, that requires going through the evaluator.
                // Note that monads, dyads and polyads are all subclasses of
                // ExpressionImpl and so we can't cast from one to the other, we
                // have to copy stuff.
                if (polyad.getArgCount() == 1) {
                    Monad monad = new Monad(localState.getOperatorType(realName), false);
                    monad.setArgument(polyad.getArguments().get(0));
                    monad.evaluate(localState);
                    polyad.setEvaluated(true);
                    polyad.setResult(monad.getResult());
                    polyad.getArguments().set(0, monad.getArgument());
                    return;
                } else {
                    Dyad dyad = new Dyad(localState.getOperatorType(realName));
                    dyad.setLeftArgument(polyad.getArguments().get(0));
                    dyad.setRightArgument(polyad.getArguments().get(1));
                    dyad.evaluate(localState);
                    polyad.setEvaluated(true);
                    polyad.setResult(dyad.getResult());
                    polyad.getArguments().set(0, dyad.getLeftArgument());
                    polyad.getArguments().set(1, dyad.getRightArgument());
                    return;
                }
            } else {
                // Easy case, just run it as a polyad.
                polyad.setName(realName);
                polyad.evaluate(localState);
                return;
            }
        }
        for (Statement statement : functionRecord.getStatements()) {
            // Fix for https://github.com/ncsa/qdl/issues/43 -- hard to isolate and track down!
            // The polyad may have a null alias in certain use cases, such as
            // verify()->block[mm#f(x,y);];
            // where the function is inheriting mm, x and y. In that case, the
            // alias of the function verify is null (as it should be), but
            // the alias of f is not and should not be overwritten.
            // The net effect will be much later, mm will be resolved against the
            // ambient space.
            if (statement instanceof ExpressionInterface && polyad.getAlias() != null) {
                ((ExpressionInterface) statement).setAlias(polyad.getAlias());
            }
            try {
                if (statement instanceof LocalBlockStatement) {
                    // Can't tell when you get a function block, so have to do this
                    ((LocalBlockStatement) statement).setFunctionParameters(foundParameters);
                }
                statement.evaluate(localState);
            } catch (ReturnException rx) {
                polyad.setResult(rx.result);
                polyad.setEvaluated(true);

                for (int i = 0; i < functionRecord.getArgCount(); i++) {
                    localState.getVStack().localRemove(new XKey(functionRecord.getArgNames().get(i)));
                }
                return;
            } catch (java.lang.StackOverflowError sx) {
                throw new RecursionException();
            }
        }

        polyad.setResult(Boolean.TRUE);
        polyad.setEvaluated(true);
    }

    /**
     * This will take the function record and polyad and find the arguments that are requested
     * in the function record vs. what's in the polyad and stash them in the correct state
     * objects.
     *
     * @param functionRecord
     * @param polyad
     * @param state
     * @param localState
     */
    protected ArrayList<XThing> resolveArguments(FunctionRecordInterface functionRecord,
                                                 Polyad polyad,
                                                 State state,
                                                 State localState) {
        ArrayList<XThing> paramList = new ArrayList<>();
        if (functionRecord.isFuncRef()) {
            return paramList;// implicit parameter list since this is an operator or built in function.
        }
        if (polyad.hasEvaluatedArgs()) {
      /*
      module['a:b'][module['a:a'][f(x)->x^2;];w:=import('a:a');]
      z:=import('a:b');
  h(@g, x)->g(x)
     h(z#w#@f)

        module['a:a','A'][f(x)->x^2;];
  z:=import('a:a')
  h(@g, x)->g(x)
  h(z#@f, 4)
  h((x)->x^2, 4)
  q(x)->x^3
  h(@q, 4)
    h(z#@f, 4)
      */
        }
        HashMap<UUID, UUID> localStateLookup = new HashMap<>();
        localStateLookup.put(state.getUuid(), localState.getUuid());

        HashMap<UUID, State> referencedStates = new HashMap<>();
        referencedStates.put(localState.getUuid(), localState);
        state.setTargetState(localState);
        for (int i = 0; i < functionRecord.getArgCount(); i++) {
            // note that the call evaluates the state in the non-local environment as per contract,
            // but the named result goes in to the localState.
            String localName = functionRecord.getArgNames().get(i);

            if (isFunctionReference(localName)) {
                int argCount = -1;
                String[] dereffed = dereferenceFunctionName(localName);
                localName = dereffed[1];
                if(dereffed[0] != null){
                    // this might blow up? Parser should prevent that
                    argCount = Integer.parseInt(dereffed[0]);
                }
                // This is the local name of the function.
                FunctionReferenceNodeInterface frn = getFunctionReferenceNode(state, polyad.getArguments().get(i), false);

                String xname = frn.getFunctionName(); // dereferenced in the parser
                boolean isOp = state.getOpEvaluator().isMathOperator(xname);
                boolean isFunc = state.getMetaEvaluator().isBuiltInFunction(xname);
                List<FR_WithState> functionRecordList = null;
                if (isOp || isFunc) {
                    functionRecordList = new ArrayList<>();
                    int airity[];
                    if (isOp) {
                        // operator like + or *
                        airity = state.getOpEvaluator().getArgCount(xname);
                    } else {
                        airity = state.getMetaEvaluator().getArgCount(xname);
                    }
                    for (int j = 0; j < airity.length; j++) {
                        FunctionRecord functionRecord1 = new FunctionRecord();
                        functionRecord1.setName(localName);
                        functionRecord1.setfRefName(xname);
                        functionRecord1.setFuncRef(true);
                        functionRecord1.setOperator(isOp);
                        functionRecord1.setArgCount(airity[j]);
                        FR_WithState frs0 = new FR_WithState();
                        frs0.functionRecord = functionRecord1;
                        functionRecordList.add(frs0);
                    }

                } else {
                    try {
                        // function records come back cloned
                        if (frn.hasModuleState()) {
                            // so we're getting the function from a module
                            functionRecordList = frn.getModuleState().getAllFunctionsByName(xname);
                            /* surgery. The current variable state is
                               table0,table1,... <- from ambient state
                               localTable         <- added when local state created.

                               The local table contains the overrides for things like function and
                               function arguments. If there is a module, we need to have its state
                               found before the ambient state.  Table should look like
                               table0,table1,... <- from ambient state
                               moduleTable       <- from module state.
                               localTable        <- added when local state created.


                            */
                            List<XTable> vStack = localState.getVStack().getStack();
                            if (!frn.getModuleState().getVStack().isEmpty()) {
                                for (Object xTable : frn.getModuleState().getVStack().getStack()) {
                                    vStack.add(vStack.size() - 1, (XTable) xTable);
                                }
                                //vStack.add(vStack.size() - 1, frn.getModuleState().getVStack().getLocal());
                            }
                            //localState.getVStack().push(frn.getModuleState().getVStack().getLocal());
                        } else {
                            if(-1 < argCount){
                                functionRecordList=new ArrayList<>();
                                FR_WithState frWithState = localState.resolveFunction(xname, argCount, false);
                                if(frWithState == null){
                                    throw new BadArgException("no such function '" + xname + "' with " + argCount + " arguments", polyad);
                                }
                                // Next bit clones the record so it can be used as a template for functions.
                                frWithState = new FR_WithState(frWithState.functionRecord.clone(),frWithState.state, frWithState.isModule );
                                functionRecordList.add(frWithState);
                            }else {
                                functionRecordList = localState.getAllFunctionsByName(xname);
                            }
                        }
                    } catch (CloneNotSupportedException e) {
                        e.printStackTrace();
                        throw new NFWException("Somehow function records are no longer clonable. Check signatures.");
                    }

                 /*
                    f(x)->x^2
                    f(1@z,p)->z(p)
                    ff(@z,p)->z(p)
                    f(@f,5)
                    ff(@f,5)
                    f(1@f, 5)
                    ff(1@f,5)


                  */
                }
                // The next block only gets used if there are references to function in modules
                // E.g. @w#z#f
                for (FR_WithState fr_withState : functionRecordList) {
                    fr_withState.functionRecord.setName(localName);
                    paramList.add(fr_withState);
                    if (fr_withState.hasState()) {
                        State s = (State) fr_withState.state;
                        if (!localStateLookup.containsKey(s.getUuid())) {
                            State ss = s.newLocalState();
                            localStateLookup.put(s.getUuid(), ss.getUuid());
                            referencedStates.put(ss.getUuid(), ss);
                        }
                    }
                }
                ;
                // This had better be a function reference or this should blow up.
            } else {
                // This had better be a function reference or this should blow up.
                VThing vThing;
                //if(polyad.getArgAt(i) instanceof ANode2){

                //    vThing = new VThing(new XKey(functionRecord.argNames.get(i)), polyad.getArguments().get(i).evaluate(localState));
                //} else{
                if (polyad.hasEvaluatedArgs()) {
                    // in the case that the arguments were evaluated in some local context that cannot be
                    // available to us.
                    vThing = new VThing(new XKey(functionRecord.getArgNames().get(i)),new QDLVariable( polyad.getEvaluatedArgs().get(i)));
                } else {
                    vThing = new VThing(new XKey(functionRecord.getArgNames().get(i)), new QDLVariable( polyad.getArguments().get(i).evaluate(state)));
                }
                //}
                paramList.add(vThing);
            }
        }
        state.setTargetState(null);
        // Add the arguments to the state object(s). At the least, there is always
        // localState (derived from the original state argument to this method).
        for (UUID uuid : referencedStates.keySet()) {
            State s = referencedStates.get(uuid);
            for (XThing xThing : paramList) {
                if (xThing instanceof VThing) {
                    s.getVStack().localPut((VThing) xThing);
                } else {
                    if (xThing instanceof FunctionRecordInterface) {
                        // A side-effect of Java erasure is that stacks can have XThings added to them
                        // regardless of the actual parameterization of the class. This caused a nasty
                        // hard to track bug so an explicit test is added here to make sure we only have
                        // the right things in the function stack.
                        s.getFTStack().localPut(xThing);
                    } else {
                        throw new NFWException("internal error. Function records only can be stored as functions, but an instance of type " +
                                xThing.getClass().getSimpleName() + " was found");
                    }
                }
            }
        }
        return paramList;
    }

    /*

          g(@h(), x, y)->h(x+'pqr', y+1) + h(x+'tuv', y)
          define[g(@h(), x, y)][return(h(x+'pqr', y+1) + h(x+'tuv', y));]
  g(@substring, 'abcd', 2); // result == dpqrcdtuv
  
  h(@g,x)->g(x)
  g(@substring, 'abcd', 2)
    Test function references to things in modules.

      define[f(x)]body[return(x+100);];
  module['a:/t','a']body[define[f(x)]body[return(x+1);];];
  module['q:/z','w']body[zz:=17;module_import('a:/t');g(x)->a#f(x)+zz;];
  module_import('q:/z');
  w#a#f(3); // returns 20
  w#g(2); // returns 6
  hh(@g, x)->g(x)

  hh(@w#g, 2); // == w#g(2)
  hh(@w#a#f, 3); // == w#a#f(3)

    
      qq(x)->x^2
      qq(3); // returns 9
     ww(@p, x)->p(x)
     ww(@-, 2); // returns -2
     ww((x)->x^3, 4)

     */
    protected boolean isFunctionReference(String name) {
        // return name.startsWith(FUNCTION_REFERENCE_MARKER) || name.startsWith(FUNCTION_REFERENCE_MARKER2);
        // @ is now dyadic, so 3@f is perfectly fine as a name.
        return name.contains(FUNCTION_REFERENCE_MARKER) || name.contains(FUNCTION_REFERENCE_MARKER2);
    }

    protected String[] dereferenceFunctionName(String name) {
        //String x = name.substring(FUNCTION_REFERENCE_MARKER.length());
        int index = name.indexOf(FUNCTION_REFERENCE_MARKER);
        if (index < 0) { // not found, try the other marker
            index = name.indexOf(FUNCTION_REFERENCE_MARKER2);
        }
        String x = name.substring(index + 1);

        //String x = name.substring(FUNCTION_REFERENCE_MARKER.length());
        if (x.endsWith("()")) {
            x = x.substring(0, x.length() - 2); // * ... ( are bookends for the reference
        }
        String argCount = index <= 0 ? null : name.substring(0, index);
        return new String[]{argCount, x};
    }
}

/*
Support for apply operator. Close to introspection...
 	U+2202 ∂
 	 ∂f = return list of arg counts, e.g. [0,1,3]
 	 3∂f = return list of arg names e.g., 2∂f = ['x','pressure']
 	 list.∂f = invoke f with arg list, e.g. [1/2, 14]∂f <==> f(1/2, 14)
 	 stem.∂f = invoke with named args, e.g. {'pressure':14,'x':1/2}∂f <==> f(1/2, 14)
 */