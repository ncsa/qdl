package org.qdl_lang.statements;

import org.qdl_lang.evaluate.OpEvaluator;
import org.qdl_lang.evaluate.StemEvaluator;
import org.qdl_lang.exceptions.*;
import org.qdl_lang.expressions.Dyad;
import org.qdl_lang.expressions.ExpressionNode;
import org.qdl_lang.expressions.Polyad;
import org.qdl_lang.expressions.VariableNode;
import org.qdl_lang.state.State;
import org.qdl_lang.state.XKey;
import org.qdl_lang.variables.QDLStem;
import org.qdl_lang.variables.QDLSet;
import org.qdl_lang.variables.QDLVariable;
import org.qdl_lang.variables.VThing;
import org.qdl_lang.variables.values.QDLValue;
import org.qdl_lang.vfs.VFSEntry;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.qdl_lang.evaluate.SystemEvaluator.*;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/17/20 at  4:44 PM
 */
public class WhileLoop implements Statement {
    TokenPosition tokenPosition = null;

    @Override
    public void setTokenPosition(TokenPosition tokenPosition) {
        this.tokenPosition = tokenPosition;
    }

    @Override
    public TokenPosition getTokenPosition() {
        return tokenPosition;
    }

    @Override
    public boolean hasTokenPosition() {
        return tokenPosition != null;
    }

    ExpressionNode conditional;

    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    public ExpressionNode getConditional() {
        return conditional;
    }

    public void setConditional(ExpressionNode conditional) {
        this.conditional = conditional;
    }

    List<Statement> statements = new ArrayList<>();

    @Override
    public QDLValue evaluate(State state) {
        State localState = state.newLocalState();
        //State localState = state.new;
        if (conditional instanceof Dyad) {
            Dyad d = (Dyad) conditional;
            if (d.getOperatorType() == OpEvaluator.EPSILON_VALUE) {
                return asQDLValue(forKeysOrValuesLoop(localState, false));
            }
            if (d.getOperatorType() == OpEvaluator.CONTAINS_KEY_VALUE) {
                return asQDLValue(forKeysOrValuesLoop(localState, true));
            }
        }
        if (conditional instanceof Polyad) {
            Polyad p = (Polyad) conditional;
            if (p.isBuiltIn()) {
                switch (p.getName()) {
                    case StemEvaluator.HAS_KEYS:
                    case FOR_KEYS:
                        return asQDLValue(forKeysOrValuesLoop(localState, true));
                    case StemEvaluator.HAS_VALUE:
                        return asQDLValue(forKeysOrValuesLoop(localState, false));
                    case FOR_NEXT:
                        return asQDLValue(doForLoop(localState));
                    case CHECK_AFTER:
                        return asQDLValue(doPostLoop(localState));
                    case FOR_LINES:
                        return asQDLValue(doForLines(localState));
                }
            }
        }

        // No built in looping function, so it is just some ordinary conditional,
        // like i < 5, or a user-defined function.
        // Just evaluate it.
        return asQDLValue(doBasicWhile(localState));

    }

    private Object doForLines(State localState) {
        if (conditional.getArgCount() < 2) {
            throw new MissingArgException(FOR_LINES + " requires two arguments", conditional.getArgCount() == 1 ? conditional.getArgAt(0) : conditional);
        }
        if (2 < conditional.getArgCount()) {
            throw new ExtraArgException(FOR_LINES + " requires at most two arguments", conditional.getArgAt(2));

        }
        ExpressionInterface swri = conditional.getArgAt(0);
        if (!(swri instanceof VariableNode)) {
            throw new BadArgException(FOR_LINES + " requires a variable as its first argument", conditional.getArgAt(0));
        }
        VariableNode variableNode = (VariableNode) swri;
        String loopArg = variableNode.getVariableReference();
        Object arg2 = conditional.getArgAt(1).evaluate(localState);
        if (!(arg2 instanceof String)) {
            throw new BadArgException(FOR_LINES + " requires a string as its second argument", conditional.getArgAt(1));
        }
        String fileName = (String) arg2;
        VFSEntry vfsEntry = null;
        boolean hasVF = false;
        if (localState.isVFSFile(fileName)) {
            vfsEntry = localState.getMetaEvaluator().resolveResourceToFile(fileName, 1, localState);
            if (vfsEntry == null) {
                throw new QDLException("The resource '" + fileName + "' was not found in the virtual file system");
            }
            hasVF = true;
        } else {
            // Only allow for virtual file reads in server mode.
            // If the file does not live in a VFS throw an exception.
            if (localState.isServerMode()) {
                throw new QDLServerModeException("File system operations not permitted in server mode.");
            }
        }
        if (hasVF) {
            /*
             This is really bad -- it just reads it as a stem and iterates over that.
             What should happen is that FileEntries have a case (probably op type that is
             not available to the general public) that reads the file with a stream and
             returns a Reader. For small files this is not important, but if this is to
             scale up, this functionality needs to be added.
            */
            for (String lineIn : vfsEntry.getLines()) {
                localState.setValue(loopArg, asQDLValue(lineIn));
                for (Statement statement : getStatements()) {
                    try {
                        statement.evaluate(localState);
                    } catch (BreakException b) {
                        return Boolean.TRUE;
                    } catch (ContinueException cx) {
                        // just continue.
                        break;
                    } catch (ReturnException rx) {
                        return Boolean.TRUE;
                    }
                }
            }
        } else {
            try {
                FileReader fileReader = new FileReader(fileName);
                BufferedReader bufferedReader = new BufferedReader(fileReader);
                String lineIn = bufferedReader.readLine();
                while (lineIn != null) {
                    localState.setValue(loopArg, asQDLValue(lineIn));
                    for (Statement statement : getStatements()) {
                        try {
                            statement.evaluate(localState);
                        } catch (BreakException b) {
                            return Boolean.TRUE;
                        } catch (ContinueException cx) {
                            // just continue.
                            break;
                        } catch (ReturnException rx) {
                            return Boolean.TRUE;
                        }
                    }
                    lineIn = bufferedReader.readLine();
                }
                bufferedReader.close();
            } catch (FileNotFoundException e) {
                throw new BadArgException(FOR_LINES + " could not find file '" + fileName + "'", conditional.getArgAt(1));
            } catch (IOException e) {
                throw new BadArgException(FOR_LINES + " error reading file '" + fileName + "'", conditional.getArgAt(1));
            }
        }
        return Boolean.TRUE;
    }

    protected Object doPostLoop(State localState) {

        do {
            for (Statement statement : getStatements()) {
                try {
                    statement.evaluate(localState);
                } catch (BreakException b) {
                    return Boolean.TRUE;
                } catch (ContinueException cx) {
                    // just continue.
                    break;
                } catch (ReturnException rx) {
                    return Boolean.TRUE;
                }
            }
        } while (conditional.evaluate(localState).asBoolean());
        return Boolean.TRUE;
    }


    protected Object doForLoop(State localState) {
        int increment = 1;
        int start = 0;
        int endValue = 0;
        boolean hasEndvalue = false;
        String loopArg = null;
        QDLValue arg = null;
        boolean doList = false;
        QDLStem list = null;
        switch (conditional.getArgCount()) {
            case 4:
                arg = conditional.getArguments().get(3).evaluate(localState);
                if (!arg.isLong()) {
                    throw new QDLExceptionWithTrace("Error: the loop increment must be a number", conditional.getArgAt(3));
                }
                Long zzz = arg.asLong();
                increment = zzz.intValue();
            case 3:
                arg = conditional.getArguments().get(2).evaluate(localState);
                if (!arg.isLong()) {
                    throw new QDLExceptionWithTrace("Error: the loop starting value must be a number", conditional.getArgAt(2));
                }
                Long yyy = arg.asLong();
                start = yyy.intValue();
            case 2:
                arg = conditional.getArguments().get(1).evaluate(localState);
                boolean gotArg2 = false;
                if (arg.isLong()) {
                    hasEndvalue = true;
                    Long xxx = arg.asLong();
                    endValue = xxx.intValue();
                    gotArg2 = true;
                }
                if (arg.isStem()) {
                    list = arg.asStem();
                    doList = true;
                    gotArg2 = true;
                    hasEndvalue = true;

                }
                if (!gotArg2) {
                    throw new QDLExceptionWithTrace("error: the argument \"" + arg + "\" must be a stem or long value", conditional.getArgAt(1));
                }
            case 1:
                if (!hasEndvalue) {
                    throw new QDLExceptionWithTrace("Error: You did not specify the ending value for this loop!", conditional.getArgAt(1));
                }
                // Now, the first argument is supposed to be a variable. We don't evaluate it since
                // we are going to set the value in the local state table and increment it manually.
                if (!(conditional.getArguments().get(0) instanceof VariableNode)) {
                    throw new IllegalArgumentException("Error: You have not specified a variable for looping.");
                }
                VariableNode node = (VariableNode) conditional.getArguments().get(0);
                loopArg = node.getVariableReference();
                break;
            default:
                throw new QDLExceptionWithTrace("Error: incorrect number of arguments for " + FOR_NEXT + ".", conditional.getArgAt(0));
        }
        if (doList) {
            for (Object key : list.keySet()) {
                localState.setValue(loopArg, list.get(key));
                for (Statement statement : getStatements()) {
                    try {
                        statement.evaluate(localState);
                    } catch (BreakException b) {
                        return Boolean.TRUE;
                    } catch (ContinueException cx) {
                        // just continue.
                        break;
                    } catch (ReturnException rx) {
                        return Boolean.TRUE;
                    }
                }
            }
            return Boolean.TRUE;
        }
        for (int i = start; i != endValue; i = i + increment) {
            localState.setValue(loopArg, asQDLValue( i));
            for (Statement statement : getStatements()) {
                try {
                    statement.evaluate(localState);
                } catch (BreakException b) {
                    return Boolean.TRUE; // abort whole thing
                } catch (ContinueException cx) {
                    // Skip rest of statements
                    break;
                } catch (ReturnException rx) {
                    return Boolean.TRUE; // abort whole thing
                }

            }
        }
        return Boolean.TRUE;
    }

    protected Object doBasicWhile(State localState) {
        try {
            while (conditional.evaluate(localState).asBoolean()) {
                for (Statement statement : getStatements()) {
                    try {
                        statement.evaluate(localState);
                    } catch (BreakException b) {
                        return Boolean.TRUE;
                    } catch (ContinueException cx) {
                        // just continue.
                        break;
                    } catch (ReturnException rx) {
                        return Boolean.TRUE;
                    }
                }
            }
            return Boolean.TRUE;
        } catch (ClassCastException | WrongValueException cce) {
            throw new QDLExceptionWithTrace("Error: You must have a boolean value for your conditional", conditional);
        }
    }

    /**
     * a ∈ X or jas_value(j, X) loop over the values
     * @param localState
     * @return
     */

    /**
     * for_keys(var, stem.) --  Loop over the keys in a given stem, assigning each to the var.
     *
     * @param localState
     * @return
     */
    protected Object forKeysOrValuesLoop(State localState, boolean doKeys) {
        if (conditional.getArgCount() != 2) {
            throw new IllegalArgumentException("Error: You must supply two arguments for " + FOR_KEYS);
        }
        String loopVar = null;
        QDLStem stemVariable = null;
        QDLSet qdlSet = null;
        QDLValue arg = conditional.getArguments().get(1).evaluate(localState);
        boolean isSet = false;
        if (arg.isStem()) {
            stemVariable =  arg.asStem();
        } else {
            if (arg.isSet()) {
                qdlSet = arg.asSet() ;
                isSet = true;
            } else {
                throw new IllegalArgumentException("Error: The target of the command must be a stem or set");
            }
        }

        // Don't evaluate -- we just want the name of the variable.
        Object arg0 = conditional.getArguments().get(0); // reuse arg for variable
        if (arg0 instanceof VariableNode) {
            loopVar = ((VariableNode) arg0).getVariableReference();
        } else {
            throw new IllegalArgumentException("Error: The command requires a variable ");
        }
      /* Test -- oops should never print.
      key_set := {'a','b','c','d'};
      while[k∈key_set]
      do[
         if[k=='b'][continue();];
         if[k=='b']
         then[say('oops');]
         else[say('ok');];
      ];
       */
        if (isSet) {
            for (Object element : qdlSet) {
                localState.getVStack().localPut(new VThing(new XKey(loopVar), new QDLVariable( element)));
                for (Statement statement : getStatements()) {
                    try {
                        statement.evaluate(localState);
                    } catch (BreakException b) {
                        return Boolean.TRUE;
                    } catch (ContinueException cx) {
                        // just continue.
                        break;
                    } catch (ReturnException rx) {
                        return Boolean.TRUE;
                    }
                }
            }

        } else {
            Iterator iterator;
            if (doKeys) {
                iterator = stemVariable.keySet().iterator();
            } else {
                iterator = stemVariable.valuesIterator();
            }

            while (iterator.hasNext()) {
                localState.getVStack().localPut(new VThing(new XKey(loopVar), new QDLVariable(iterator.next())));
                for (Statement statement : getStatements()) {
                    try {
                        statement.evaluate(localState);
                    } catch (BreakException b) {
                        return Boolean.TRUE;
                    } catch (ContinueException cx) {
                        // just continue.
                        break;
                    } catch (ReturnException rx) {
                        return Boolean.TRUE;
                    }
                }
            }

        }
        return Boolean.TRUE;
    }

    @Override
    public List<String> getSourceCode() {
        return sourceCode;
    }

    @Override
    public void setSourceCode(List<String> sourceCode) {
        this.sourceCode = sourceCode;
    }

    List<String> sourceCode;
}
