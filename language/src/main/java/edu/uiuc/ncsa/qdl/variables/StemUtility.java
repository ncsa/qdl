package edu.uiuc.ncsa.qdl.variables;

import edu.uiuc.ncsa.qdl.exceptions.RankException;
import edu.uiuc.ncsa.qdl.expressions.ConstantNode;
import edu.uiuc.ncsa.qdl.expressions.ExpressionStemNode;
import edu.uiuc.ncsa.qdl.expressions.VariableNode;
import edu.uiuc.ncsa.qdl.state.State;
import edu.uiuc.ncsa.qdl.statements.StatementWithResultInterface;

import java.util.ArrayList;

import static edu.uiuc.ncsa.qdl.variables.QDLStem.STEM_INDEX_MARKER;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 3/24/21 at  12:38 PM
 */
public class StemUtility {
    public static final Long LAST_AXIS_ARGUMENT_VALUE = new Long(-0xcafed00d);

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
        void action(QDLStem out, Object key, QDLStem leftStem, QDLStem rightStem);
    }

    public static boolean isStem(Object o) {
        return o instanceof QDLStem;
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
        for (Object key0 : commonKeys.keySet()) {
            boolean isKey0Long = key0 instanceof Long;
            Object leftObj = left0.get(key0);
            Object rightObj = right0.get(key0);

            QDLStem left1 = null;
            if (isStem(leftObj)) {
                left1 = (QDLStem) leftObj;
            } else {
                if (rightObj == null) {
                    throw new RankException("There are no more elements in the left argument.");
                }

                left1 = new QDLStem();
                left1.put(0L, leftObj);
            }
            QDLStem right1 = null;
            if (isStem(rightObj)) {
                right1 = (QDLStem) right0.get(key0);
            } else {
                if (rightObj == null) {
                    throw new RankException("There are no more elements in the right argument.");
                }
                right1 = new QDLStem();
                right1.put(0L, rightObj);
            }
            boolean bottomedOut = areNoneStems(leftObj, rightObj) && maxDepth && 0 < depth;
            if (bottomedOut) {
                axisAction.action(out0, key0, left1, right1);
            } else {
                if (0 < depth) {
                    if (areNoneStems(leftObj, rightObj)) {
                        throw new RankException("rank error");
                    }
                    QDLStem out1 = new QDLStem();
                    out0.putLongOrString(key0, out1);
                    axisDayadRecursion(out1, left1, right1, depth - 1, maxDepth, axisAction);
                } else {
                    out0.putLongOrString(key0, left1.union(right1));
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
     * @param inStem
     * @param depth
     * @param walker
     * @return
     */
    public static Object axisWalker(QDLStem inStem, int depth, StemAxisWalkerAction1 walker) {
        if (inStem.getRank() < depth+1) {
            throw new RankException("error: axis " + depth + " requested on stem of rank " + inStem.getRank() );
        }
        if (depth <= 0) {
            return walker.action(inStem);
        }
        QDLStem outStem = new QDLStem();
        for (Object key1 : inStem.keySet()) {
            Object obj = inStem.get(key1);
            if (!isStem(obj)) {
                continue;
            }
            outStem.putLongOrString(key1, axisWalker((QDLStem) obj, depth - 1, walker));
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

            ArrayList<StatementWithResultInterface> leftArgs = new ArrayList<>();
            ArrayList<StatementWithResultInterface> rightArgs = new ArrayList<>();
            StatementWithResultInterface swri = ESN;
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
    public static void formatList(QDLStem stem){
        if(!stem.isList()){
             throw new IllegalArgumentException("cannot format general stem");
        }
             for(Object key : stem.keySet()){
                 Object obj = stem.get(key);
                 if(obj instanceof QDLStem){
                     QDLStem stemVariable = (QDLStem) obj;
                     String row = "";
                     for(Object key2: stemVariable.keySet()){
                         row = row + stemVariable.get(key2) + " ";
                     }

                     System.out.println(row);
                 }else {
                     System.out.println(stem.get(key));
                 }
             }
             return;
      //   }
    }
    public static void main(String[] args){
        QDLStem outerStem = new QDLStem();
        for(int k = 0; k <5l ; k++){
            QDLStem stemVariable0 = new QDLStem();
            for(int j = 0 ;j < 4; j++){
                QDLStem stemVariable = new QDLStem();
                for(int i = 0; i<5; i++){
                    stemVariable.put(i, k + "_" + j + "_" + i);
                }
                stemVariable0.put(j, stemVariable);
            }
            outerStem.put(k, stemVariable0);

        }
                formatList(outerStem);
    }

    /**
     * Convert a stem list to a string, separated by line feeds.
     * If <code>forceToString</code> is true, then every elements toString is
     * called, otherwise, if the element is not a string, an exception is thrown.
     * @param contents
     * @param forceToString
     * @return
     */
    public static String stemListToString(QDLStem contents, boolean forceToString){
        if(contents.isEmpty()){
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        if (!contents.containsKey("0") && !contents.isEmpty()) {
            throw new IllegalArgumentException("Error: The given stem is not a list.");
        }
        for (int i = 0; i < contents.size(); i++) {
            Object object = contents.get(Integer.toString(i));
            if(forceToString){
                stringBuilder.append(contents.get(Integer.toString(i)) + "\n");

            }else {
                if(object instanceof String) {
                    stringBuilder.append((String)object + "\n");
                }else{
                    throw new IllegalArgumentException("object '" + object + "' is not a string");
                }
            }
        }
        return stringBuilder.toString();
    }
}
