package org.qdl_lang.parsing;

import org.qdl_lang.evaluate.*;
import org.qdl_lang.exceptions.IntrinsicViolation;
import org.qdl_lang.exceptions.NamespaceException;
import org.qdl_lang.exceptions.ParsingException;
import org.qdl_lang.expressions.*;
import org.qdl_lang.generated.QDLParserListener;
import org.qdl_lang.generated.QDLParserParser;
import org.qdl_lang.expressions.module.QDLModule;
import org.qdl_lang.state.QDLConstants;
import org.qdl_lang.state.State;
import org.qdl_lang.variables.*;
import edu.uiuc.ncsa.security.core.util.StringUtils;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.Interval;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.TerminalNodeImpl;
import org.apache.commons.lang.StringEscapeUtils;
import org.qdl_lang.functions.*;
import org.qdl_lang.statements.*;
import org.qdl_lang.variables.values.QDLNullValue;
import org.qdl_lang.variables.values.QDLValue;
import org.qdl_lang.variables.values.StemValue;
import org.qdl_lang.variables.values.StringValue;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import static org.qdl_lang.exceptions.ParsingException.*;
import static org.qdl_lang.statements.ExpressionInterface.*;
import static org.qdl_lang.variables.QDLStem.STEM_INDEX_MARKER;
import static org.qdl_lang.variables.values.QDLValue.asQDLValue;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 1/15/20 at  6:17 AM
 */
public class QDLListener implements QDLParserListener {
    ParsingMap parsingMap = new ParsingMap();
    private QDLParserParser.FdocContext ctx, fdoc;

    public ParsingMap getParsingMap() {
        return parsingMap;
    }

    public void setParsingMap(ParsingMap parsingMap) {
        this.parsingMap = parsingMap;
    }

    State state;

    /**
     * Note that the state is supplied here not for evaluation, but because it has the evaluators needed
     * to resolve the various values of operators that are being produced by the parser. The state
     * should be injected at runtime where it is actually known.
     *
     * @param parsingMap
     * @param state
     */
    public QDLListener(ParsingMap parsingMap, State state) {
        this.parsingMap = parsingMap;
        this.state = state;
    }

    @Override
    public void enterElements(QDLParserParser.ElementsContext ctx) {
        checkLexer(ctx);

    }

    @Override
    public void exitElements(QDLParserParser.ElementsContext ctx) {
        checkLexer(ctx);
    }

    @Override
    public void enterVariable(QDLParserParser.VariableContext ctx) {
        stash(ctx, new VariableNode(tp(ctx)));
    }

    @Override
    public void exitVariable(QDLParserParser.VariableContext ctx) {
        StatementRecord p = (StatementRecord) parsingMap.get(IDUtils.createIdentifier(ctx));
        if (ctx.getText().equals(QDLConstants.RESERVED_TRUE) ||
                ctx.getText().equals(QDLConstants.RESERVED_FALSE)) {
            // SPECIAL CASE. The parse recognizes true and false, but does not know what to do with them.
            // We are here because it lumps them together with the variable values.
            ConstantNode cnode = new ConstantNode(asQDLValue(ctx.getText().equals(QDLConstants.RESERVED_TRUE)));
            p.statement = cnode;
            cnode.setSourceCode(getSource(ctx));
            return;
        }
        if (ctx.getText().equals(QDLConstants.RESERVED_NULL) || ctx.getText().equals(QDLConstants.RESERVED_NULL_SET)) {
            p.statement = new ConstantNode(QDLNullValue.getNullValue());
            return;
        }
        ((VariableNode) parsingMap.getStatementFromContext(ctx)).setVariableReference(ctx.getText());

    }

    protected void stash(ParseTree parseTree, Element element) {
        ElementRecord pr = new ElementRecord(parseTree, element);
        parsingMap.put(pr);
    }

    protected void stash(ParseTree parseTree, Statement statement) {
        StatementRecord pr = new StatementRecord(parseTree, statement);
        parsingMap.put(pr);
    }


    protected Statement resolveChild(ParseTree currentChild, boolean removeChild) {
        // the most common pattern is that a child node or one of its children is
        // the actual node we need. This checks if the argument is a child in the table
        // and returns that, if not it starts to look through descendents.
        String id = IDUtils.createIdentifier(currentChild);
        if (parsingMap.containsKey(id)) {
            Statement s = parsingMap.getStatementFromContext(currentChild);
            if (removeChild) {
                parsingMap.remove(IDUtils.createIdentifier(currentChild));
            }
            return s;
        }
        ParseRecord p = parsingMap.findFirstChild(currentChild);
        if (p != null && p instanceof StatementRecord) {
            Statement s = ((StatementRecord) p).statement;
            if (removeChild) {
                parsingMap.remove(IDUtils.createIdentifier(currentChild));
            }
            return s;
        }
        return null;

    }


    protected Statement resolveChild(ParseTree currentChild) {
        try {
            return resolveChild(currentChild, false);
        } catch (NullPointerException npe) {
            // This happens when it cannot actually resolve children so indicates a
            // parsing error

        }
        throw new ParsingException("could not parse the expression");
    }


    @Override
    public void enterAssignment(QDLParserParser.AssignmentContext ctx) {
        stash(ctx, new ANode2(tp(ctx)));
    }

    @Override
    public void exitAssignment(QDLParserParser.AssignmentContext assignmentContext) {
        exitAssignmentANode(assignmentContext);
    }

    public void exitAssignmentANode(QDLParserParser.AssignmentContext assignmentContext) {
        ANode2 currentA = (ANode2) parsingMap.getStatementFromContext(assignmentContext);
        currentA.setSourceCode(getSource(assignmentContext));
        currentA.setTokenPosition(tp(assignmentContext));
        currentA.setOp(assignmentContext.ASSIGN().getText());
        ExpressionInterface leftArg;
        ExpressionInterface rightArg;
        // swap them if it is a right assignment
        if (currentA.getAssignmentType() == ANode2.rightAssignmentType) {
            leftArg = (ExpressionInterface) resolveChild(assignmentContext.getChild(2));
            rightArg = (ExpressionInterface) resolveChild(assignmentContext.getChild(0));

        } else {
            leftArg = (ExpressionInterface) resolveChild(assignmentContext.getChild(0));
            rightArg = (ExpressionInterface) resolveChild(assignmentContext.getChild(2));
        }

        if (leftArg instanceof ConstantNode) {
            throw new IllegalArgumentException("error: cannot assign constant a value");
        }
        currentA.setLeftArg(leftArg);
        currentA.setRightArg(rightArg);
    }


    @Override
    public void enterFunction(QDLParserParser.FunctionContext ctx) {
        stash(ctx, new Polyad(tp(ctx)));
    }

    @Override
    public void exitFunction(QDLParserParser.FunctionContext ctx) {
        // Note that this resolves **all** functions, specifically the built in ones.
        Polyad polyad = (Polyad) parsingMap.getStatementFromContext(ctx);
        polyad.setSourceCode(getSource(ctx));
        polyad.setTokenPosition(tp(ctx));
        // need to process its argument list.
        // There are 3 children  "f(" arglist ")", so we want the middle one.

        String name = ctx.getChild(0).getText();
        if (name.endsWith("(")) {
            name = name.substring(0, name.length() - 1);
        }
        polyad.setName(name);
        // Generally built-in functions are resolved by their name. This next line figures out
        // if the given function is not a built-in and flags it. If this is not set
        // then no user-defined functions are going to be available.
        polyad.setBuiltIn(state.getMetaEvaluator().isBuiltInFunction(name));
        ParseTree kids = ctx.getChild(1); //
        for (int i = 0; i < kids.getChildCount(); i++) {
            ParseTree kid = kids.getChild(i);
            if (!kid.getText().equals(",")) {

                // add it.
                //        dyad.setLeftArgument((ExpressionNode) resolveChild(parseTree.getChild(0)));
                Statement s = resolveChild(kid);
                if (s instanceof ExpressionInterface) {
                    polyad.getArguments().add((ExpressionInterface) s);

                }
                if (s instanceof FunctionDefinitionStatement) {
                    LambdaDefinitionNode lambdaDefinitionNode = new LambdaDefinitionNode((FunctionDefinitionStatement) s);
                    lambdaDefinitionNode.setTokenPosition(tp(ctx));
                    polyad.getArguments().add(lambdaDefinitionNode);
                }
            }
        }
    }

    @Override
    public void enterVariables(QDLParserParser.VariablesContext ctx) {

    }

    @Override
    public void exitVariables(QDLParserParser.VariablesContext ctx) {
    }

    @Override
    public void enterFunctions(QDLParserParser.FunctionsContext ctx) {

    }

    @Override
    public void exitFunctions(QDLParserParser.FunctionsContext ctx) {
    }

    @Override
    public void enterPrefix(QDLParserParser.PrefixContext ctx) {
        // really hit or miss if the parser invokes this, so everything is in the exit method.
    }

    @Override
    public void exitPrefix(QDLParserParser.PrefixContext ctx) {
        Monad monad = new Monad(false);
        monad.setTokenPosition(tp(ctx));
        stash(ctx, monad);

        // in the context, the operator is the 0th child since it is prefixed.
        monad.setOperatorType(state.getOperatorType(ctx.getChild(0).getText()));
        List<String> source = new ArrayList<>();
        source.add(ctx.getText());
        monad.setSourceCode(source);
        finish(monad, ctx);
    }

    @Override
    public void enterNumber(QDLParserParser.NumberContext ctx) {

    }

    @Override
    public void exitNumber(QDLParserParser.NumberContext ctx) {
        String rawNumber = ctx.getText();
        ConstantNode constantNode;
/*       Maybe someday support complex numbers
        if(rawNumber.contains("J")){
            int complexIndex = ctx.getText().indexOf("J");

            BigComplex bigComplex = BigComplex.valueOf(new BigDecimal(rawNumber.substring(0,complexIndex)),
                    new BigDecimal(rawNumber.substring(complexIndex+1)));
            constantNode = new ConstantNode(bigComplex, Constant.COMPLEX_TYPE);

        } else {
            BigDecimal decimal = new BigDecimal(rawNumber);
            constantNode = new ConstantNode(decimal, Constant.DECIMAL_TYPE);
        }
*/
        BigDecimal decimal;
        try {
            // Fixes https://github.com/ncsa/qdl/issues/73
            // It is possible that there is a unary minus or plus for the exponent.
            // Though not terribly often used. Try it straight up, then try it with these
            // values replaced. This is done assuming that it is fairly low probability
            // hence we do not have to scan every decimal which would give a performance hit.
            // On the other hand, exception handling in Java is quite slow, so if there are
            // lots of substitutions, this might have to change.
            decimal = new BigDecimal(rawNumber);
        } catch (NumberFormatException nfx) {
            rawNumber = rawNumber.replace("¯", "-").replace("⁺", "+");
            decimal = new BigDecimal(rawNumber); // if it fails here, it fails.
        }
        constantNode = new ConstantNode(asQDLValue(decimal));
        stash(ctx, constantNode);
    }

    @Override
    public void enterNumbers(QDLParserParser.NumbersContext ctx) {

    }

    @Override
    public void exitNumbers(QDLParserParser.NumbersContext ctx) {
    }


    @Override
    public void enterAssociation(QDLParserParser.AssociationContext ctx) {

    }

    @Override
    public void exitAssociation(QDLParserParser.AssociationContext ctx) {
        ParenthesizedExpression parenthesizedExpression = new ParenthesizedExpression();
        parenthesizedExpression.setTokenPosition(tp(ctx));
        parenthesizedExpression.setSourceCode(getSource(ctx));
        ParseTree p = ctx.expression();
        Statement s = parsingMap.getStatementFromContext(p);
        if (s instanceof FunctionDefinitionStatement) {
            LambdaDefinitionNode lambdaDefinitionNode = new LambdaDefinitionNode((FunctionDefinitionStatement) s);
            lambdaDefinitionNode.setTokenPosition(tp(ctx));
            parenthesizedExpression.setExpression(lambdaDefinitionNode);
        } else {
            ExpressionInterface v = (ExpressionInterface) s;
            if (v == null) {
                v = (ExpressionInterface) resolveChild(p);
            }
            parenthesizedExpression.setExpression(v);
        }


        /*
          Statement s = parsingMap.getStatementFromContext(p);
        if (s instanceof FunctionDefinitionStatement) {
            LambdaDefinitionNode lambdaDefinitionNode = new LambdaDefinitionNode((FunctionDefinitionStatement) s);
            lambdaDefinitionNode.setTokenPosition(tp(ctx));
            parenthesizedExpression.setExpression(lambdaDefinitionNode);
        }
        if(s instanceof ExpressionInterface){

            ExpressionInterface v = (ExpressionInterface) parsingMap.getStatementFromContext(p);
            if (v == null) {
                v = (ExpressionInterface) resolveChild(p);
            }
            parenthesizedExpression.setExpression(v);
        }
         */
/*        ExpressionInterface v = (ExpressionInterface) parsingMap.getStatementFromContext(p);
        if (v == null) {
            v = (ExpressionInterface) resolveChild(p);
        }
        parenthesizedExpression.setExpression(v);*/

        stash(ctx, parenthesizedExpression);
    }

    @Override
    public void enterNotExpression(QDLParserParser.NotExpressionContext ctx) {
    }

    @Override
    public void exitNotExpression(QDLParserParser.NotExpressionContext ctx) {
        // For some weird reason, entering the NOT expression does not always happen, but exiting it does.
        stash(ctx, new Monad(OpEvaluator.NOT_VALUE, false));// automatically prefix
        Monad monad = (Monad) parsingMap.getStatementFromContext(ctx);
        monad.setTokenPosition(tp(ctx));
        finish(monad, ctx);
    }


    @Override
    public void enterLogical(QDLParserParser.LogicalContext ctx) {

    }

    @Override
    public void exitLogical(QDLParserParser.LogicalContext ctx) {

    }

    @Override
    public void enterOrExpression(QDLParserParser.OrExpressionContext ctx) {
        Dyad dyad = new Dyad(OpEvaluator.OR_VALUE);
        dyad.setTokenPosition(tp(ctx));
        stash(ctx, dyad);
    }

    protected void finish(Dyad dyad, ParseTree parseTree) {
        dyad.setLeftArgument((ExpressionInterface) resolveChild(parseTree.getChild(0)));
        Statement s = resolveChild(parseTree.getChild(2));
/*     Attempt at having lambdas directly evaluated. If ∂ then it makes sense but might open the door
       to functions being defined everywhere on a whim -- which may be ok.
       Do we want (x)->x^2 + (y)->y^3 to be allowed?
       If no, we would need to intercept this misuse and flag it.
       For another thing, it makes the @ functor implicit everywhere.
       On the other hand, makes lamdas behave like generic expressions.


       Here it is just for dyads, but we'd also have to do this for monads and polyads.

       Tests in QDL:
       5∂(x)->x^2;
       [2,3]∂(x,y)->x*y

       You will then have to track down every place that a lamda might end
       up as an errant argument!!
       Needs some real thought.
       */
/*       if(s instanceof  FunctionDefinitionStatement){
            FunctionDefinitionStatement fds = (FunctionDefinitionStatement) s;

            if(fds.isLambda()){
                s = new LambdaDefinitionNode((FunctionDefinitionStatement) s);

       //       Alternate???
       //       DyadicFunctionReferenceNode frn = new DyadicFunctionReferenceNode();
       //       FunctionRecord fr = fds.getFunctionRecord();
       //       frn.setFunctionRecord(fr);
       //       ArrayList<ExpressionInterface> x = new ArrayList<>();
       //       x.add(new ConstantNode((long)fr.getArgCount()));
       //       x.add(new ConstantNode(fr.getName()==null?"":fr.getName()));
       //
       //       frn.setArguments(x);
       //       s = frn;
       //     }

        } */



        if(s instanceof ExpressionInterface){
            dyad.setRightArgument((ExpressionInterface) s);
            List<String> source = new ArrayList<>();
            source.add(parseTree.getText());
            dyad.setSourceCode(source);
            return;
        }
        if(s instanceof FunctionDefinitionStatement){
            // See note abouve for enabling these!
            throw new ParsingException("Function definition not supported here.");
        }
        throw new ParsingException(s.getClass().getSimpleName() + " not supported here.");
    }

    protected void finish(Monad monad, ParseTree parseTree) {
        int index = monad.isPostFix() ? 0 : 1; // post fix means 0th is the arg, prefix means 1 is the arg.
        monad.setArgument((ExpressionInterface) resolveChild(parseTree.getChild(index)));
        List<String> source = new ArrayList<>();
        source.add(parseTree.getText());
        monad.setSourceCode(source);
    }

    @Override
    public void exitOrExpression(QDLParserParser.OrExpressionContext ctx) {
        Dyad orStmt = (Dyad) parsingMap.getStatementFromContext(ctx);
        finish(orStmt, ctx);
    }

    boolean isUnaryMinus = false;

    @Override
    public void enterUnaryMinusExpression(QDLParserParser.UnaryMinusExpressionContext ctx) {
        // ARGH as per antlr4 spec, this need not be called, and whether it is seems to vary by compilation.
        // Do not use this. Put everything in the exit statement.
    }

    @Override
    public void exitUnaryMinusExpression(QDLParserParser.UnaryMinusExpressionContext ctx) {
        boolean isMinus = ctx.children.get(0).getText().equals(OpEvaluator.MINUS) || ctx.children.get(0).getText().equals(OpEvaluator.MINUS2);
        Monad monad = new Monad(isMinus ? OpEvaluator.MINUS_VALUE : OpEvaluator.PLUS_VALUE, false);
        monad.setSourceCode(getSource(ctx));
        monad.setTokenPosition(tp(ctx));
        stash(ctx, monad);
        finish(monad, ctx);
    }

    @Override
    public void enterEqExpression(QDLParserParser.EqExpressionContext ctx) {
        ComparisonDyad c = new ComparisonDyad(OpEvaluator.UNKNOWN_VALUE);
        c.setTokenPosition(tp(ctx));
        stash(ctx, c);
    }

    @Override
    public void exitEqExpression(QDLParserParser.EqExpressionContext ctx) {
        Dyad dyad = (Dyad) parsingMap.getStatementFromContext(ctx);
        dyad.setOperatorType(state.getOperatorType(ctx.op.getText()));
        finish(dyad, ctx);
    }

    @Override
    public void enterAndExpression(QDLParserParser.AndExpressionContext ctx) {
        stash(ctx, new Dyad(OpEvaluator.AND_VALUE, tp(ctx)));
    }

    @Override
    public void exitAndExpression(QDLParserParser.AndExpressionContext ctx) {
        Dyad andStmt = (Dyad) parsingMap.getStatementFromContext(ctx);
        finish(andStmt, ctx);
    }

    @Override
    public void enterPowerExpression(QDLParserParser.PowerExpressionContext ctx) {

    }

    @Override
    public void exitPowerExpression(QDLParserParser.PowerExpressionContext ctx) {
        Dyad dyad;
        if (ctx.Nroot() != null) {
            dyad = new Dyad(OpEvaluator.NROOT_VALUE);
        } else {
            dyad = new Dyad(OpEvaluator.POWER_VALUE);
        }
        dyad.setTokenPosition(tp(ctx));
        stash(ctx, dyad);
        finish(dyad, ctx);
    }

    @Override
    public void enterSquartExpression(QDLParserParser.SquartExpressionContext ctx) {

    }

    @Override
    public void exitSquartExpression(QDLParserParser.SquartExpressionContext ctx) {
        Polyad polyad = new Polyad(TMathEvaluator.N_ROOT);
        polyad.setTokenPosition(tp(ctx));
        polyad.setSourceCode(getSource(ctx));
        polyad.addArgument((ExpressionInterface) resolveChild(ctx.getChild(1)));
        polyad.addArgument(new ConstantNode(asQDLValue(2L))) ;
        stash(ctx, polyad);
    }

    @Override
    public void enterMultiplyExpression(QDLParserParser.MultiplyExpressionContext ctx) {

    }

    @Override
    public void exitMultiplyExpression(QDLParserParser.MultiplyExpressionContext ctx) {
        Dyad dyad = new Dyad(state.getOperatorType(ctx.op.getText()));
        dyad.setTokenPosition(tp(ctx));
        stash(ctx, dyad);
        finish(dyad, ctx);
    }

    @Override
    public void enterNull(QDLParserParser.NullContext ctx) {
    }

    @Override
    public void exitNull(QDLParserParser.NullContext ctx) {
        ConstantNode constantNode = new ConstantNode(QDLValue.getNullValue());
        constantNode.setTokenPosition(tp(ctx));
        stash(ctx, constantNode);
    }

    @Override
    public void enterStrings(QDLParserParser.StringsContext ctx) {
        // This is called when there is just a string.
    }

    @Override
    public void exitStrings(QDLParserParser.StringsContext ctx) {
        checkLexer(ctx);

        String value = ctx.getChild(0).getText().trim();
        // IF the parser is on the ball, then this is enclosed in single quotes, so we string them
        if (value.startsWith("'")) {
            value = value.substring(1);
        }
        if (value.endsWith("'")) {
            value = value.substring(0, value.length() - 1);
        }
        value = value.replace("\\'", "'");
        value = StringEscapeUtils.unescapeJava(value);
/*
        value = value.replace("\\n", "\n");
        value = value.replace("\\t", "\t");
        value = value.replace("\\r", "\r");
        value = value.replace("\\b", "\b");
        value = value.replace("\\\\", "\\");
*/
        ConstantNode node = new ConstantNode(asQDLValue(value));
        List<String> source = new ArrayList<>();
        source.add(ctx.getText());
        node.setTokenPosition(tp(ctx));
        node.setSourceCode(source);
        stash(ctx, node);
        // that's it. set the value and we're done.
    }

    @Override
    public void enterAddExpression(QDLParserParser.AddExpressionContext ctx) {

    }

    @Override
    public void exitAddExpression(QDLParserParser.AddExpressionContext ctx) {
        boolean isMinus = ctx.Minus() != null;
        Dyad dyad = new Dyad(isMinus ? OpEvaluator.MINUS_VALUE : OpEvaluator.PLUS_VALUE, tp(ctx));
        stash(ctx, dyad);
        finish(dyad, ctx);
    }

    @Override
    public void enterCompExpression(QDLParserParser.CompExpressionContext ctx) {
    }

    protected TokenPosition tp(ParserRuleContext ctx) {
        if (ctx == null || ctx.getStart() == null) {
            return null;
        }
        return new TokenPosition(ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine());
    }

    @Override
    public void exitCompExpression(QDLParserParser.CompExpressionContext ctx) {
        // only now can we determine the comparison type
        stash(ctx, new ComparisonDyad(state.getOperatorType(ctx.getChild(1).getText()), tp(ctx)));
        Dyad dyad = (Dyad) parsingMap.getStatementFromContext(ctx);
        finish(dyad, ctx);
    }

    @Override
    public void enterPostfix(QDLParserParser.PostfixContext ctx) {
        stash(ctx, new Monad(true, tp(ctx)));
    }

    @Override
    public void exitPostfix(QDLParserParser.PostfixContext ctx) {
        Monad monad = (Monad) parsingMap.getStatementFromContext(ctx);
        // in the context, the operator is the 1st (vs. 0th!) child since it is postfixed.
        monad.setOperatorType(state.getOperatorType(ctx.getChild(1).getText()));
        finish(monad, ctx);
    }


/*    @Override
    public void enterSemi_for_empty_expressions(QDLParserParser.Semi_for_empty_expressionsContext ctx) {

    }

    @Override
    public void exitSemi_for_empty_expressions(QDLParserParser.Semi_for_empty_expressionsContext ctx) {


    }*/

    @Override
    public void enterElement(QDLParserParser.ElementContext ctx) {
        stash(ctx, new Element());
    }

    @Override
    public void exitElement(QDLParserParser.ElementContext ctx) {
        // Find the correct statement and put it in the element
        StatementRecord statementRecord = (StatementRecord) parsingMap.findFirstChild(ctx, true);
        if (statementRecord == null) {
            return;
        }
        String id = IDUtils.createIdentifier(ctx);
        ElementRecord elementRecord = (ElementRecord) parsingMap.get(id);
        elementRecord.getElement().setStatement(statementRecord.statement);
    }

    @Override
    public void enterStatement(QDLParserParser.StatementContext ctx) {

    }

    @Override
    public void exitStatement(QDLParserParser.StatementContext ctx) {

    }

    @Override
    public void enterIfStatement(QDLParserParser.IfStatementContext ctx) {
        stash(ctx, new ConditionalStatement(tp(ctx)));
    }

    protected ConditionalStatement doConditional(ParseTree ctx) {
        ConditionalStatement conditionalStatement = (ConditionalStatement) parsingMap.getStatementFromContext(ctx);
        //#0 is if[ // #1 is conditional, #2 is ]then[. #3 starts the statements
        //conditionalStatement.setConditional((ExpressionNode) resolveChild(ctx.getChild(1))); // first child is always conditional
        List<String> source = new ArrayList<>();
        source.add(ctx.getText());
        conditionalStatement.setSourceCode(source);
        conditionalStatement.setTokenPosition(tp((ParserRuleContext) ctx));
        boolean addToIf = true;

        ParseExpressionBlockNode parseExpressionBlockNode = (ParseExpressionBlockNode) parsingMap.getStatementFromContext(ctx.getChild(1));
        conditionalStatement.setConditional(parseExpressionBlockNode.getExpressionNodes().get(0));
        for (int i = 2; i < ctx.getChildCount(); i++) {
            ParseTree p = ctx.getChild(i);
            if (p instanceof TerminalNodeImpl) {
                continue; // skip tokens like [, then, else
            }
            ParseStatementBlock parseStatementBlock = (ParseStatementBlock) parsingMap.getStatementFromContext(p);

            if (addToIf) {
                conditionalStatement.setIfArguments(parseStatementBlock.getStatements());
                addToIf = false;
            } else {
                conditionalStatement.setElseArguments(parseStatementBlock.getStatements());
            }
        }
        return conditionalStatement;
    }


    @Override
    public void exitIfStatement(QDLParserParser.IfStatementContext ctx) {
        doConditional(ctx);
    }

    @Override
    public void enterIfElseStatement(QDLParserParser.IfElseStatementContext ctx) {
        stash(ctx, new ConditionalStatement(tp(ctx)));

    }

    @Override
    public void exitIfElseStatement(QDLParserParser.IfElseStatementContext ctx) {
        doConditional(ctx);
    }

    @Override
    public void enterConditionalStatement(QDLParserParser.ConditionalStatementContext ctx) {

    }

    @Override
    public void exitConditionalStatement(QDLParserParser.ConditionalStatementContext ctx) {

    }

    @Override
    public void enterLoopStatement(QDLParserParser.LoopStatementContext ctx) {
        WhileLoop w = new WhileLoop();
        w.setTokenPosition(tp(ctx));
        stash(ctx, new WhileLoop());
    }

    @Override
    public void exitLoopStatement(QDLParserParser.LoopStatementContext ctx) {
        doLoop(ctx);
    }

    protected void doLoop(QDLParserParser.LoopStatementContext ctx) {
        // single line loop tests
        //a:=3; while[ a < 6 ]do[ q:= 6; say(q + a++); ]; // prints 9 10 11
        //a:=3; while[ a < 6 ]do[ q:= 6; say(q+a); ++a;]; // prints 9 10 11
        //a:=8; while[ a > 6 ]do[ q:= 6; say(q+a); a--;]; // prints 14 13
        //a:=8; while[ a > 6 ]do[ q:= 6; say(q + --a); ]; // prints 13 12
        QDLParserParser.ConditionalBlockContext conditionalBlockContext = ctx.conditionalBlock();
        QDLParserParser.StatementBlockContext statementBlockContext = ctx.statementBlock();
        WhileLoop whileLoop = (WhileLoop) parsingMap.getStatementFromContext(ctx);
        whileLoop.setConditional((ExpressionNode) resolveChild(conditionalBlockContext.expression()));
        for (QDLParserParser.StatementContext stmt : statementBlockContext.statement()) {
            whileLoop.getStatements().add(resolveChild(stmt));
        }
        whileLoop.setSourceCode(getSource(ctx));
        whileLoop.setTokenPosition(tp(ctx));
    }

    @Override
    public void enterSwitchStatement(QDLParserParser.SwitchStatementContext ctx) {
        SwitchStatement s = new SwitchStatement();
        s.setTokenPosition(tp(ctx));
        stash(ctx, s);
    }

    @Override
    public void exitSwitchStatement(QDLParserParser.SwitchStatementContext ctx) {
        SwitchStatement switchStatement = (SwitchStatement) parsingMap.getStatementFromContext(ctx);
        switchStatement.setSourceCode(getSource(ctx));
        switchStatement.setTokenPosition(tp(ctx));
        for (QDLParserParser.IfStatementContext ifc : ctx.ifStatement()) {
            ConditionalStatement cs = (ConditionalStatement) parsingMap.getStatementFromContext(ifc);
            switchStatement.getArguments().add(cs);
        }
    }

    @Override
    public void enterFdoc(QDLParserParser.FdocContext ctx) {
    }

    @Override
    public void exitFdoc(QDLParserParser.FdocContext ctx) {
    }

    @Override
    public void enterDefineStatement(QDLParserParser.DefineStatementContext ctx) {
        //     parsingMap.startMark();
        FunctionDefinitionStatement fds = new FunctionDefinitionStatement();
        fds.setTokenPosition(tp(ctx));
        fds.setLambda(false);
        stash(ctx, fds);
    }

    protected void doDefine2(QDLParserParser.DefineStatementContext defineContext) {
        FunctionRecord functionRecord = new FunctionRecord();
        functionRecord.setTokenPosition(tp(defineContext));
        FunctionDefinitionStatement fds = (FunctionDefinitionStatement) parsingMap.getStatementFromContext(defineContext);
        fds.setFunctionRecord(functionRecord);
        //FunctionDefinitionStatement fds =
        // not quite the original source... The issue is that this comes parsed and stripped of any original
        // end of line markers, so we cannot tell what was there. Since it may include documentation lines
        // we have to add back in EOLs at the end of every statement so the comments don't get lost.
        // Best we can do with ANTLR...

        List<String> stringList = new ArrayList<>();
        for (int i = 0; i < defineContext.getChildCount() - 1; i++) {
            //stringList.add((i == 0 ? "" : "\n") + defineContext.getChild(i).getText());
            stringList.add(defineContext.getChild(i).getText());
        }
        //
        QDLParserParser.DocStatementBlockContext docStatementBlockContext = defineContext.docStatementBlock();
        for (int i = 0; i < docStatementBlockContext.getChildCount(); i++) {
            stringList.add(docStatementBlockContext.getChild(i).getText());
        }

        // grab the FDOC

        // ANTLR may strip final terminator. Put it back as needed.

        if (!stringList.get(stringList.size() - 1).endsWith(";")) {
            stringList.set(stringList.size() - 1, stringList.get(stringList.size() - 1) + ";");
        }

        functionRecord.sourceCode = stringList;
        QDLParserParser.FunctionContext nameAndArgsNode = defineContext.function();
        String name = nameAndArgsNode.getChild(0).getText();
        if (name.endsWith("(")) {
            name = name.substring(0, name.length() - 1);
        }
        functionRecord.setName(name);
        if (nameAndArgsNode.f_args() != null) {
            QDLParserParser.F_argsContext argListContext = nameAndArgsNode.f_args();
            // this is a comma delimited list of arguments.
            String allArgs = argListContext.getText();

            StringTokenizer st = new StringTokenizer(allArgs, ",");
            while (st.hasMoreElements()) {
                functionRecord.argNames.add(st.nextToken());
            }
        }
/*
        for (QDLParserParser.F_argsContext argListContext : nameAndArgsNode.f_args()) {
            // this is a comma delimited list of arguments.
            String allArgs = argListContext.getText();

            StringTokenizer st = new StringTokenizer(allArgs, ",");
            while (st.hasMoreElements()) {
                functionRecord.argNames.add(st.nextToken());
            }
        }
*/
        //docStatementBlockContext = defineContext.docStatementBlock();
        for (QDLParserParser.FdocContext fd : docStatementBlockContext.fdoc()) {
            functionRecord.documentation.add(getFdocLine(fd.getText()));
        }
        for (QDLParserParser.StatementContext sc : docStatementBlockContext.statement()) {
            functionRecord.statements.add(resolveChild(sc));
        }
    }


    @Override
    public void exitDefineStatement(QDLParserParser.DefineStatementContext ctx) {
        doDefine2(ctx);
    }


    @Override
    public void enterLambdaStatement(QDLParserParser.LambdaStatementContext ctx) {
        // don't clear the functions added already.
        // Special case since f(x) -> ... means f is added before we get here and won't get picked
        // up. That means that the system will think f needs to be evaluated asap and you will
        // get errors.
        //   parsingMap.startMark(false);
        FunctionDefinitionStatement fds = new FunctionDefinitionStatement();
        fds.setTokenPosition(tp(ctx));
        fds.setLambda(true);

        stash(ctx, fds);

    }
    //     define[g(x)][z:=x+1;return(x);]
    //     h(x) -> [z:=x+1;return(z);];

    @Override
    public void exitLambdaStatement(QDLParserParser.LambdaStatementContext lambdaContext) {
        //createLambdaStatement(lambdaContext);
        createLambdaStatementNEW(lambdaContext);
    }

    protected void createLambdaStatementNEW(QDLParserParser.LambdaStatementContext lambdaContext) {
        QDLParserParser.FunctionContext nameAndArgsNode = lambdaContext.function();

        if (nameAndArgsNode == null) {
            return; // do nothing.
        }
        FunctionRecord functionRecord = new FunctionRecord();
        functionRecord.setTokenPosition(tp(lambdaContext));
        //functionRecord.setLambda(true);
        functionRecord.setLambda(lambdaContext.LOCAL() == null);
        //boolean isBlock = lambdaContext.LOCAL() == null;
        // not quite the original source... The issue is that this comes parsed and stripped of any original
        // end of line markers, so we cannot tell what was there. Since it may include documentation lines
        // we have to add back in EOLs at the end of every statement so the comments don't get lost.
        // Best we can do with ANTLR...
        FunctionDefinitionStatement fds = (FunctionDefinitionStatement) parsingMap.getStatementFromContext(lambdaContext);
        fds.setFunctionRecord(functionRecord);

        List<String> stringList = getSource(lambdaContext);
        // ANTLR may strip final terminator. Put it back as needed.
        if (!stringList.get(stringList.size() - 1).endsWith(";")) {
            stringList.set(stringList.size() - 1, stringList.get(stringList.size() - 1) + ";");
        }
        functionRecord.sourceCode = stringList;

        String name = nameAndArgsNode.getChild(0).getText();
        if (name.endsWith("(")) {
            name = name.substring(0, name.length() - 1);
        }
        if (nameAndArgsNode.f_args() != null) {

            String allArgs = nameAndArgsNode.f_args().getText();

            StringTokenizer st = new StringTokenizer(allArgs, ",");
            while (st.hasMoreElements()) {
                functionRecord.argNames.add(st.nextToken());
            }
        }
/*
        for (QDLParserParser.F_argsContext argListContext : nameAndArgsNode.f_args()) {
            // this is a comma delimited list of arguments.
            String allArgs = argListContext.getText();

            StringTokenizer st = new StringTokenizer(allArgs, ",");
            while (st.hasMoreElements()) {
                functionRecord.argNames.add(st.nextToken());
            }
        }
*/
        functionRecord.setName(name);
        functionRecord.setArgCount(functionRecord.argNames.size()); // Just set it here and be done with it.

        QDLParserParser.DocStatementBlockContext docStatmentBlockContext = lambdaContext.docStatementBlock();
        for (QDLParserParser.FdocContext fd : docStatmentBlockContext.fdoc()) {
            functionRecord.documentation.add(getFdocLine(fd.getText()));
        }
        List<QDLParserParser.StatementContext> statements = docStatmentBlockContext.statement();
        if (1 < statements.size()) {
            // super simple case -- just snarf up the statements.
            for (QDLParserParser.StatementContext statementContext : statements) {
                functionRecord.statements.add(resolveChild(statementContext));
            }
            return;
        }
        // harder is to decide if there is a single statement that gets wrapped in a return();
        Statement stmt = resolveChild(statements.get(0));
        // Contract: Wrap simple expressions in a return.
        if (stmt instanceof ExpressionInterface) {
            if (stmt instanceof ExpressionImpl) {
                ExpressionImpl expr = (ExpressionImpl) stmt;
                if (expr.getOperatorType() != SystemEvaluator.RETURN_TYPE) {
                    Polyad expr1 = new Polyad(SystemEvaluator.RETURN_TYPE);
                    expr1.setName(SystemEvaluator.RETURN);
                    expr1.addArgument(expr);
                    functionRecord.statements.add(expr1); // wrapped in a return
                } else {
                    functionRecord.statements.add(expr); // already has a return
                }
            } else {
                Polyad expr1 = new Polyad(SystemEvaluator.RETURN_TYPE);
                expr1.setName(SystemEvaluator.RETURN);
                expr1.addArgument((ExpressionInterface) stmt);
                functionRecord.statements.add(expr1); // wrapped in a return
            }
        } else {
            functionRecord.statements.add(stmt); // a statement, not merely an expression
        }
    }


    protected String getFdocLine(String doc) {

        int pos = doc.indexOf(FDOC_MARKER);
        if (pos != -1) {
            if (pos + 1 < doc.length()) {
                doc = doc.substring(pos + FDOC_MARKER.length());
            } else {
                doc = "";// blank line
            }
            return doc;
        }
        if (pos == -1) {
            pos = doc.indexOf(FDOC_MARKER2);
        }
        if (pos != -1) {
            if (pos + 1 < doc.length()) {
                doc = doc.substring(pos + FDOC_MARKER2.length());
            } else {
                doc = "";// blank line
            }
            return doc;
        }
        return doc;
    }

    @Override
    public void visitTerminal(TerminalNode terminalNode) {

    }

    @Override
    public void visitErrorNode(ErrorNode errorNode) {

    }

    @Override
    public void enterEveryRule(ParserRuleContext parserRuleContext) {

    }

    @Override
    public void exitEveryRule(ParserRuleContext parserRuleContext) {
    }

    protected String stripSingleQuotes(String rawString) {
        String out = rawString.trim();
        if (out.startsWith("'")) {
            out = out.substring(1);
        }
        if (out.endsWith("'")) {
            out = out.substring(0, out.length() - 1);
        }
        return out;
    }

    boolean isModule = false;
    QDLModule currentModule = null;
    ModuleStatement moduleStatement = null;

    /*
      The conditional in the enterModuleStatement(QDLParserParser.ModuleStatementContext)
      method prevents defining a module inside another. Note that in ModuleTest#testNestedFunctionImport
      a module defined outside of the module can be imported and used successfully, so this
      issue relates to handling of module[] statements inside of module statements.
      This is mostly seems to be an annoyance, but at some point all the state issues need to be
      tracked down.
      @param ctx the parse tree
     */
    /*
    Comment out conditional in the enterModuleStatement method to use this basic of
    how nested module[] statement fails. Nesting of modules parses fine, just the state
    does not seem coherent afterwards. Issue is that modules are all global.
    To do this right will need local module stack (like functions and variables).
   FAILS -- want to work:
   //Cannot have the definition and the import inside the same module.
   module['a:a','a'][module['b:b','b'][f(x)->x;];module_import('b:b');g(x)->b#f(x^2);];
   module_import('a:a')
a
   a#g(2)
illegal argument:no module named "b" was  imported at (1, 67)

   NOTE that the above is in ModuleTest#testNestedModule2 and commented out. To enable this,
   comment out the first conditional below, uncomment the test.

   WORKS:
   module['b:b','b'][f(x)->(x);]
   module_import('b:b'); //<-- key is that this has to be imported  outside of module first.
   module['a:a','a'][module_import('b:b');g(x)->b#f(x^2);]; // <- actually, the import does nothing...
   module_import('a:a')
   a#g(2)

   ALSO, intrinsic functions and variables require substantial(?) reworking.
*/
    @Override
    public void enterModuleStatement(QDLParserParser.ModuleStatementContext ctx) {
        moduleStatement = new ModuleStatement();
        moduleStatement.setTokenPosition(tp(ctx));
        stash(ctx, moduleStatement);

    }

    /**
     * There are various ways to try and get the source. Most of them strip off line feeds so
     * if there is line comment (// ... ) the rest of the source turns into a comment *if*
     * the source ever gets reparsed. NOTE this is exactly what happens with module.
     * This tries to get the original source with line feeds
     * and all. It may be more fragile than the ANTLR documentation lets on, so be advised.
     * The ctx.getText() method, however, is definitely broken...
     *
     * @param ctx
     * @return
     */
    protected List<String> getSource(ParserRuleContext ctx) {
        List<String> text = new ArrayList<>();
        if (ctx == null) {
            text.add("no source");
            return text;
        }
        if (ctx.start == null || ctx.stop == null) {
            // odd ball case
            text.add("no source");
            return text;
        }
        int a = ctx.start.getStartIndex();
        int b = ctx.stop.getStopIndex();
        if (b < a) {
            text.add("no source");
            return text;
        }
        Interval interval = new Interval(a, b);
        // Next line actually gets the original source:
        String txt = ctx.start.getInputStream().getText(interval);
        if (!StringUtils.isTrivial(txt)) {
            // Sometimes the parser strips the final ;. If the source is used later
            // this causes re-parsing it to reliably bomb. Add it back as needed.
            txt = txt + (txt.endsWith(";") ? "" : ";");
        }
        StringTokenizer stringTokenizer = new StringTokenizer(txt, "\n");
        while (stringTokenizer.hasMoreTokens()) {
            text.add(stringTokenizer.nextToken());
        }
        return text;
    }


    @Override
    public void exitModuleStatement(QDLParserParser.ModuleStatementContext moduleContext) {
        // module['uri:/foo','bar']body[ say( 'hi');]; // single line test
        // )load module_example.qdl
        isModule = false;
        ModuleStatement moduleStatement = (ModuleStatement) parsingMap.getStatementFromContext(moduleContext);
        URI namespace = URI.create(stripSingleQuotes(moduleContext.STRING(0).toString()));
        if (moduleContext.STRING().size() == 2) {
            String alias = stripSingleQuotes(moduleContext.STRING(1).toString());
            moduleStatement.setAlias(alias);
        }
        moduleStatement.setNamespace(namespace);
        moduleStatement.setSourceCode(getSource(moduleContext));
        QDLParserParser.DocStatementBlockContext docStatementBlockContext = moduleContext.docStatementBlock();
        if (docStatementBlockContext == null) {
            TokenPosition tp = tp(moduleContext);
            throw new ParsingException("malformed module statement", tp.line, tp.col, SYNTAX_TYPE);
        }
        for (QDLParserParser.StatementContext stmt : docStatementBlockContext.statement()) {
            // Issue is that resolving children as we do gets the function definitions.
            // that are in any define contexts. So if this is a define context,
            // skip it. They are handled elsewhere and the definitions are never evaluated
            // just stashed.

            Statement kid = resolveChild(stmt);
            moduleStatement.getStatements().add(kid);
        }
        //       For fdoc support.  Probably not, but maybe
        for (QDLParserParser.FdocContext fd : docStatementBlockContext.fdoc()) {
            moduleStatement.getDocumentation().add(getFdocLine(fd.getText()));
        }
        // Parser strips off trailing ; which in turn causes a parser error later when we are runnign the import command.
        moduleStatement.setSourceCode(getSource(moduleContext));
    }

    static String FDOC_MARKER = "»";
    static String FDOC_MARKER2 = "===";

    /*
    module['a:a','A'][module['b:b','B'][u:=2;f(x)->x+1;];];module_import('b:b');say(B#u);];
     */
    @Override
    public void enterTryCatchStatement(QDLParserParser.TryCatchStatementContext ctx) {
        TryCatch tryCatch = new TryCatch();
        tryCatch.setTokenPosition(tp(ctx));
        stash(ctx, tryCatch);
    }

    // try[to_number('foo');]catch[say('That is no number!');];
    // try[my_number := to_number(scan('enter value>'));]catch[say('That is no number!');];
    @Override
    public void exitTryCatchStatement(QDLParserParser.TryCatchStatementContext tcContext) {
        TryCatch tryCatch = (TryCatch) parsingMap.getStatementFromContext(tcContext);
        if (tcContext.getChildCount() == 2) {
            throw new ParsingException("missing catch clause");
        }
        tryCatch.setSourceCode(getSource(tcContext));
        tryCatch.setTokenPosition(tp(tcContext));
        // new in 1.4 -- much simpler handling here because of parser rewrite.
        QDLParserParser.StatementBlockContext statementBlockContext = tcContext.statementBlock(0);
        ParseStatementBlock parseStatementBlock = (ParseStatementBlock) resolveChild(statementBlockContext);
        tryCatch.setTryStatements(parseStatementBlock.getStatements());

        statementBlockContext = tcContext.statementBlock(1);
        parseStatementBlock = (ParseStatementBlock) resolveChild(statementBlockContext);
        tryCatch.setCatchStatements(parseStatementBlock.getStatements());
    }

    @Override
    public void enterStemVariable(QDLParserParser.StemVariableContext ctx) {
        StemVariableNode stemVariableNode = new StemVariableNode();
        stemVariableNode.setTokenPosition(tp(ctx));
        stash(ctx, stemVariableNode);
    }

    @Override
    public void exitStemVariable(QDLParserParser.StemVariableContext ctx) {
        StemVariableNode svn = (StemVariableNode) parsingMap.getStatementFromContext(ctx);
        svn.setSourceCode(getSource(ctx));
        svn.setTokenPosition(tp(ctx));
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree p = ctx.getChild(i);
            if (p instanceof QDLParserParser.StemEntryContext) {
                StemEntryNode stmt = (StemEntryNode) resolveChild(p);
                svn.getStatements().add(stmt);
            }
        }
    }

    @Override
    public void enterStemEntry(QDLParserParser.StemEntryContext ctx) {
        StemEntryNode sen = new StemEntryNode();
        sen.setTokenPosition(tp(ctx));
        stash(ctx, sen);
    }

    @Override
    public void exitStemEntry(QDLParserParser.StemEntryContext ctx) {
        StemEntryNode svn = (StemEntryNode) parsingMap.getStatementFromContext(ctx);
        if (ctx.getChild(0).getText().equals("*")) {
            ExpressionInterface value = (ExpressionInterface) resolveChild(ctx.getChild(2));
            svn.setDefaultValue(true);
            svn.setValue(value);
            return;
        }
        ExpressionInterface key = (ExpressionInterface) resolveChild(ctx.getChild(0));
        ExpressionInterface value = (ExpressionInterface) resolveChild(ctx.getChild(2));
        svn.setKey(key);
        svn.setValue(value);
        svn.setSourceCode(getSource(ctx));

    }

    @Override
    public void enterStemList(QDLParserParser.StemListContext ctx) {
        StemListNode sln = new StemListNode();
        sln.setTokenPosition(tp(ctx));
        stash(ctx, sln);
    }

    @Override
    public void exitStemList(QDLParserParser.StemListContext ctx) {
        StemListNode sln = (StemListNode) parsingMap.getStatementFromContext(ctx);
        sln.setSourceCode(getSource(ctx));
        for (int i = 0; i < ctx.getChildCount(); i++) {
            ParseTree pt = ctx.getChild(i);
            if (pt instanceof QDLParserParser.StemValueContext) {
                Statement statement = resolveChild(pt);
                if(statement instanceof ExpressionInterface) {
                    ExpressionInterface stmt = (ExpressionInterface) resolveChild(pt);
                    sln.getStatements().add(stmt);
                }else{
                    throw new ParsingException("unexpected statement of type " + statement.getClass().getSimpleName());
                }
            }
        }
    }

    @Override
    public void enterStemValue(QDLParserParser.StemValueContext ctx) {

    }

    @Override
    public void exitStemValue(QDLParserParser.StemValueContext ctx) {

    }

    @Override
    public void enterStemLi(QDLParserParser.StemLiContext ctx) {
        System.out.println("entering stemli");
    }

    @Override
    public void exitStemLi(QDLParserParser.StemLiContext ctx) {

    }

    @Override
    public void enterStemVar(QDLParserParser.StemVarContext ctx) {

    }

    @Override
    public void exitStemVar(QDLParserParser.StemVarContext ctx) {

    }

    @Override
    public void enterTildeExpression(QDLParserParser.TildeExpressionContext ctx) {
    }

    @Override
    public void exitTildeExpression(QDLParserParser.TildeExpressionContext ctx) {
        String x = ctx.getChild(1).getText();
        Dyad dyad;
        if (x.equals(OpEvaluator.TILDE_STILE) || x.equals(OpEvaluator.TILDE_STILE2)) {
            dyad = new Dyad(OpEvaluator.TILDE_STILE_VALUE);
        } else {
            dyad = new Dyad(OpEvaluator.TILDE_VALUE);

        }
        dyad.setTokenPosition(tp(ctx));
        stash(ctx, dyad);
        finish(dyad, ctx);
    }


    @Override
    public void enterDotOp(QDLParserParser.DotOpContext ctx) {
    }

    /*
         x. := [-5/8, -5/7, -5/6, -1, -5/4]
         y. := [-2/3, 2, -4/7, 3, -3/2]
        x. - y.
     */

    /**
     * Takes the place of having variables with .'s baked in to them and promotes
     * the 'childOf' operator to a proper first class citizen of QDL. There are some
     * edge cases though.
     * <pre>
     * First case of  parsing is that in our grammar, x. - y. (and for that matter
     * x. ~ y. with unary join) gets parsed as
     *            dotOp
     *         /    |   \
     *         x    .    unaryMinus
     *                       \
     *                       y.
     * I.e. it thinks it is x.(-y.) The reason is that we allow for a. as an expression
     * and ~b. as a monad, so it cannot tell a. (~b.) from (a.) ~ b. and the former certainly
     * could also occur, but the latter is much more intuitive. We do not
     * have this problem with x. * y. since there is no monadic *y. We don't want x.^y.
     * to behave differently from x.-y. for instance.
     *
     * A lot of reading about possible solutions convinced me that fixing it
     * after the fact was the way to go. Another options is a very careful restructuring
     * of the grammar, but that is apt to be quite painstaking and require a near complete
     * rewrite of large parts of the code.
     * </pre>
     *
     * @param dotOpContext
     */
    @Override
    public void exitDotOp(QDLParserParser.DotOpContext dotOpContext) {
        /*
           Special case monadic operators
         */
        if (dotOpContext.getChild(2) instanceof QDLParserParser.UnaryTildeExpressionContext) {
            QDLParserParser.UnaryTildeExpressionContext um = (QDLParserParser.UnaryTildeExpressionContext) dotOpContext.getChild(2);
            Dyad d = new Dyad(OpEvaluator.TILDE_VALUE);
            d.setTokenPosition(tp(dotOpContext));
            d.setSourceCode(getSource(dotOpContext));
            d.setRightArgument((ExpressionInterface) resolveChild(um.getChild(1)));
            ESN2 leftArg = new ESN2();
            Statement s = resolveChild(dotOpContext.getChild(0));
            if (s instanceof VariableNode) {
                VariableNode vNode = (VariableNode) s;
                vNode.setVariableReference(vNode.getVariableReference() + STEM_INDEX_MARKER);
                leftArg.setLeftArg(vNode);
            } else {
                leftArg.setLeftArg((ExpressionInterface) s);
            }
            leftArg.setRightArg(null);
            d.setLeftArgument(leftArg);
            stash(dotOpContext, d);
            return;
        }

        if (dotOpContext.getChild(2) instanceof QDLParserParser.NotTildeExpressionContext) {
            QDLParserParser.NotTildeExpressionContext um = (QDLParserParser.NotTildeExpressionContext) dotOpContext.getChild(2);
            Dyad d = new Dyad(OpEvaluator.EXCISE_VALUE);
            d.setTokenPosition(tp(dotOpContext));
            d.setSourceCode(getSource(dotOpContext));
            d.setRightArgument((ExpressionInterface) resolveChild(um.getChild(1)));
            ESN2 leftArg = new ESN2();
            Statement s = resolveChild(dotOpContext.getChild(0));
            if (s instanceof VariableNode) {
                VariableNode vNode = (VariableNode) s;
                vNode.setVariableReference(vNode.getVariableReference() + STEM_INDEX_MARKER);
                leftArg.setLeftArg(vNode);
            } else {
                leftArg.setLeftArg((ExpressionInterface) s);
            }
            leftArg.setRightArg(null);
            d.setLeftArgument(leftArg);
            stash(dotOpContext, d);
            return;
        }

        if (dotOpContext.getChild(2) instanceof QDLParserParser.UnaryMinusExpressionContext) {
            QDLParserParser.UnaryMinusExpressionContext um = (QDLParserParser.UnaryMinusExpressionContext) dotOpContext.getChild(2);
            // Important bit: If the user actually uses leading high minus ¯ or plus ⁺ then they really do want a negative
            // value. Pass it along. The problem is the - is ambiguous as an monadic or dyadic operator in some contexts.
            if ((!um.getChild(0).getText().equals(OpEvaluator.MINUS2)) && !um.getChild(0).getText().equals(OpEvaluator.PLUS2)) {
                Dyad d = new Dyad(um.Plus() == null ? OpEvaluator.MINUS_VALUE : OpEvaluator.PLUS_VALUE);
                d.setTokenPosition(tp(ctx));
                d.setSourceCode(getSource(ctx));
                d.setRightArgument((ExpressionInterface) resolveChild(um.getChild(1)));
                ESN2 leftArg = new ESN2();
                leftArg.setTokenPosition(tp(ctx)); // This is not right, it is the start of the expression,, best we can do
                Statement s = resolveChild(dotOpContext.getChild(0));
                if (s instanceof VariableNode) {
                    VariableNode vNode = (VariableNode) s;
                    vNode.setVariableReference(vNode.getVariableReference() + STEM_INDEX_MARKER);
                    leftArg.setLeftArg(vNode);
                } else {
                    leftArg.setLeftArg((ExpressionInterface) s);
                }
                leftArg.setRightArg(null);
                d.setLeftArgument(leftArg);
                stash(dotOpContext, d);
                return;
            }
        }
        ESN2 expressionStemNode = new ESN2();
        expressionStemNode.setTokenPosition(tp(dotOpContext));
        expressionStemNode.setSourceCode(getSource(dotOpContext));
        stash(dotOpContext, expressionStemNode);

        List<QDLParserParser.ExpressionContext> list = dotOpContext.expression();
        for (int i = 0; i < dotOpContext.getChildCount(); i++) {
            ParseTree p = dotOpContext.getChild(i);
            // If it is a termminal node (a node consisting of just the stem marker) skip it
            if (!(p instanceof TerminalNodeImpl)) {
                ExpressionInterface swri = (ExpressionInterface) resolveChild(p);
                if (i == 0) {
                    if (swri instanceof VariableNode) {
                        VariableNode vNode = (VariableNode) swri;
                        vNode.setVariableReference(vNode.getVariableReference() + STEM_INDEX_MARKER);
                        expressionStemNode.getArguments().add(vNode);
                    } else {
                        if (swri instanceof ModuleExpression) {
                            // If it's a module (e.g. a#b.i) then mark the variable, b here, as b. so
                            // it is interpreted as a stem in later resolution.
                            ModuleExpression moduleExpression = (ModuleExpression) swri;
                            if (moduleExpression.getExpression() instanceof VariableNode) {
                                VariableNode vNode = (VariableNode) moduleExpression.getExpression();
                                vNode.setVariableReference(vNode.getVariableReference() + STEM_INDEX_MARKER);
                            }
                            expressionStemNode.getArguments().add(moduleExpression);

                        } else {
                            expressionStemNode.getArguments().add(swri);
                        }
                    }
                } else {
                    expressionStemNode.getArguments().add(swri);
                }
/*                if ((i == 0) && (swri instanceof VariableNode)) {
                    VariableNode vNode = (VariableNode) swri;
                    vNode.setVariableReference(vNode.getVariableReference() + STEM_INDEX_MARKER);
                    expressionStemNode.getArguments().add(vNode);
                } else {
                    expressionStemNode.getArguments().add(swri);
                }*/
            }
            // surgery might be needed. Getting the parser to see c.1.2.3.4 as not (c.).(1.2).(3.4)

        }

        if (expressionStemNode.getRightArg() instanceof ConstantNode) {
            if (expressionStemNode.getRightArg().getResult().isDecimal()) {
                String a = expressionStemNode.getRightArg().getResult().toString();
                String[] lr = a.split("\\.");
                ESN2 childESN = new ESN2();
                childESN.setTokenPosition(tp(ctx));
                childESN.setLeftArg(expressionStemNode.getLeftArg());
                childESN.setRightArg(new ConstantNode(new QDLValue(Long.parseLong(lr[0]))));
                expressionStemNode.setLeftArg(childESN);
                expressionStemNode.setRightArg(new ConstantNode(new QDLValue(Long.parseLong(lr[1]))));
                // ISSUE: Parser needs some way (probably predicates???) to know that in the course of
                // a stem, decimals are not allowed. This is hard, since we want to allow arbitrary expression.
                // which should evaluate to integers. What is next is a temporary
                // workaround to get everything working.
                // Need to replace this node with
                /*
                       c.1.2                   c.1.2
                         .                       .
                        / \       with          / \
                       c   1.2                 .    2
                                              / \
                                             c   1
                      Trick is that this instance of the stem is tied to the ctx so it is what
                      the parser thinks "exists".
                 */
            }
        }
    }


    @Override
    public void enterInteger(QDLParserParser.IntegerContext ctx) {

    }

    @Override
    public void exitInteger(QDLParserParser.IntegerContext ctx) {
        ConstantNode constantNode = null;
        Object value;
        try {
            value = Long.parseLong(ctx.getChild(0).getText());
        } catch (NumberFormatException nfx) {
            // it is possible the user is entering a simply huge number, try plan B
            value = new BigDecimal(ctx.getChild(0).getText());
        }
        constantNode = new ConstantNode(asQDLValue(value));
        constantNode.setTokenPosition(tp(ctx));
        constantNode.setSourceCode(getSource(ctx));
        stash(ctx, constantNode);
    }

    @Override
    public void enterIntegers(QDLParserParser.IntegersContext ctx) {

    }

    @Override
    public void exitIntegers(QDLParserParser.IntegersContext ctx) {

    }


    @Override
    public void enterF_args(QDLParserParser.F_argsContext ctx) {
        // System.out.println("enter f_argS");

    }

    @Override
    public void exitF_args(QDLParserParser.F_argsContext ctx) {
        //   System.out.println("exit f_argS");

    }

    @Override
    public void enterRegexMatches(QDLParserParser.RegexMatchesContext ctx) {
        stash(ctx, new ComparisonDyad(OpEvaluator.UNKNOWN_VALUE, tp(ctx)));
    }

    @Override
    public void exitRegexMatches(QDLParserParser.RegexMatchesContext ctx) {
        Dyad dyad = (Dyad) parsingMap.getStatementFromContext(ctx);
        dyad.setOperatorType(state.getOperatorType(ctx.op.getText()));
        finish(dyad, ctx);
    }

    @Override
    public void enterAltIFExpression(QDLParserParser.AltIFExpressionContext ctx) {
        AltIfExpressionNode a = new AltIfExpressionNode();
        a.setTokenPosition(tp(ctx));
        stash(ctx, a);

    }

    @Override
    public void exitAltIFExpression(QDLParserParser.AltIFExpressionContext ctx) {
        AltIfExpressionNode altIfExpressionNode = (AltIfExpressionNode) parsingMap.getStatementFromContext(ctx);
        //#0 is if[ // #1 is conditional, #2 is ]then[. #3 starts the statements
        // Includes fixes for https://github.com/ncsa/qdl/issues/86
        Object object = resolveChild(ctx.getChild(0));
        if (object instanceof ExpressionNode) {
            altIfExpressionNode.setIF((ExpressionNode) object);
        } else {
            throw new IllegalArgumentException("left argument must be a boolean, not a " + object);
        }
        object = resolveChild(ctx.getChild(2));
        if (object instanceof ExpressionInterface) {

            altIfExpressionNode.setTHEN((ExpressionInterface) resolveChild(ctx.getChild(2)));
        } else {
            throw new IllegalArgumentException("target of if argument must be an expression, not a " + object);
        }
        if (3 < ctx.getChildCount()) {
            object = resolveChild(ctx.getChild(4));
            if (object instanceof ExpressionInterface) {
                altIfExpressionNode.setELSE((ExpressionInterface) object);
            } else {
                throw new IllegalArgumentException("alternate of if argument must be an expression, not a " + object);
            }
        } else {
            altIfExpressionNode.setELSE(new ConstantNode(QDLNullValue.getNullValue()));
        }
        altIfExpressionNode.setTokenPosition(tp(ctx));
        altIfExpressionNode.setSourceCode(getSource(ctx));
    }


    @Override
    public void enterExpressionBlock(QDLParserParser.ExpressionBlockContext ctx) {
        stash(ctx, new ParseExpressionBlockNode());

    }

    @Override
    public void exitExpressionBlock(QDLParserParser.ExpressionBlockContext ctx) {
        ParseExpressionBlockNode parseExpressionBlockNode = (ParseExpressionBlockNode) parsingMap.getStatementFromContext(ctx);
        // Now we fill it up with expressions
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof TerminalNodeImpl) {
                continue;
            }
            ParseTree expr = ctx.getChild(i);
            Statement s = parsingMap.getStatementFromContext(expr);
            if (s instanceof FunctionDefinitionStatement) {
                LambdaDefinitionNode lds = new LambdaDefinitionNode((FunctionDefinitionStatement) s);
                parseExpressionBlockNode.getExpressionNodes().add((lds));
            } else {

                parseExpressionBlockNode.getExpressionNodes().add((ExpressionNode) s);
            }
        }
        parseExpressionBlockNode.setSourceCode(getSource(ctx));
        parseExpressionBlockNode.setTokenPosition(tp(ctx));
    }

    @Override
    public void enterConditionalBlock(QDLParserParser.ConditionalBlockContext ctx) {
        stash(ctx, new ParseExpressionBlockNode());

    }

    @Override
    public void exitConditionalBlock(QDLParserParser.ConditionalBlockContext ctx) {
        ParseExpressionBlockNode parseExpressionBlockNode = (ParseExpressionBlockNode) parsingMap.getStatementFromContext(ctx);
        ParseTree expr = ctx.getChild(1); // only one
        parseExpressionBlockNode.getExpressionNodes().add((ExpressionNode) resolveChild(expr));
        parseExpressionBlockNode.setTokenPosition(tp(ctx));
        parseExpressionBlockNode.setSourceCode(getSource(ctx));
    }


    @Override
    public void enterRealInterval(QDLParserParser.RealIntervalContext ctx) {

    }

    @Override
    public void exitRealInterval(QDLParserParser.RealIntervalContext ctx) {

    }

    @Override
    public void enterIntInterval(QDLParserParser.IntIntervalContext ctx) {

    }

    @Override
    public void exitIntInterval(QDLParserParser.IntIntervalContext ctx) {

    }

    @Override
    public void enterStatementBlock(QDLParserParser.StatementBlockContext ctx) {
        stash(ctx, new ParseStatementBlock());
    }

    @Override
    public void exitStatementBlock(QDLParserParser.StatementBlockContext ctx) {
        ParseStatementBlock parseStatementBlock = (ParseStatementBlock) parsingMap.getStatementFromContext(ctx);
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof TerminalNodeImpl) {
                // typically these are just parse tokens like [, ;, ], comma
                continue;
            }
            // Typically this is populated with statement contexts that hold exactly what we want.

            ParseTree expr = ctx.getChild(i).getChild(0);
            parseStatementBlock.getStatements().add(resolveChild(expr));
        }
        parseStatementBlock.setTokenPosition(tp(ctx));
        parseStatementBlock.setSourceCode(getSource(ctx));
    }

    @Override
    public void enterDocStatementBlock(QDLParserParser.DocStatementBlockContext ctx) {

    }

    @Override
    public void exitDocStatementBlock(QDLParserParser.DocStatementBlockContext ctx) {
    }

    @Override
    public void enterIInterval(QDLParserParser.IIntervalContext ctx) {
        stash(ctx, new OpenSliceNode(tp(ctx)));
    }

    @Override
    public void exitIInterval(QDLParserParser.IIntervalContext ctx) {
        OpenSliceNode sliceNode = (OpenSliceNode) parsingMap.getStatementFromContext(ctx);
        // Missing zero-th argument
        if (ctx.getChild(1) instanceof TerminalNodeImpl) {
            // Missing first argument, supply it
            sliceNode.getArguments().add(new ConstantNode(asQDLValue(0L)));
        }
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof TerminalNodeImpl) {
                continue;
            }
            sliceNode.getArguments().add((ExpressionInterface) resolveChild(ctx.getChild(i)));
        }
        sliceNode.setTokenPosition(tp(ctx));
        sliceNode.setSourceCode(getSource(ctx));
    }

    @Override
    public void enterRInterval(QDLParserParser.RIntervalContext ctx) {
        stash(ctx, new ClosedSliceNode(tp(ctx)));
    }

    @Override
    public void exitRInterval(QDLParserParser.RIntervalContext ctx) {
        ClosedSliceNode realIntervalNode = (ClosedSliceNode) parsingMap.getStatementFromContext(ctx);
        if (ctx.getChild(1) instanceof TerminalNodeImpl) {
            // Missing first argument, supply it
            realIntervalNode.getArguments().add(new ConstantNode(asQDLValue(0L)));
        }
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof TerminalNodeImpl) {
                continue;
            }
            realIntervalNode.getArguments().add((ExpressionInterface) resolveChild(ctx.getChild(i)));
        }
        realIntervalNode.setTokenPosition(tp(ctx));
        realIntervalNode.setSourceCode(getSource(ctx));
    }

    @Override
    public void enterKeywords(QDLParserParser.KeywordsContext ctx) {
    }

    @Override
    public void exitKeywords(QDLParserParser.KeywordsContext ctx) {

    }

    @Override
    public void enterKeyword(QDLParserParser.KeywordContext ctx) {
        ConstantNode constantNode = new ConstantNode(new StringValue(null));
        constantNode.setTokenPosition(tp(ctx));
        stash(ctx, constantNode);

    }

    @Override
    public void exitKeyword(QDLParserParser.KeywordContext ctx) {
        StatementRecord p = (StatementRecord) parsingMap.get(IDUtils.createIdentifier(ctx));
        ConstantNode constantNode = (ConstantNode) p.statement;
        switch (ctx.getText()) {
            case QDLConstants.RESERVED_FALSE:
                constantNode.setResult(Boolean.FALSE);
                break;
            case QDLConstants.RESERVED_TRUE:
                constantNode.setResult(Boolean.TRUE);
                break;
            case QDLConstants.RESERVED_NULL:
                constantNode.setResult(QDLNull.getInstance());
                break;
            case QDLConstants.RESERVED_NULL_SET:
                constantNode.setResult(new QDLSet());
                break;
/*            case QDLConstants.RESERVED_COMPLEX_I:
                throw new IllegalArgumentException("Complex numbers not supported.");*/
            default:
                throw new IllegalArgumentException("error: unknown reserved keyword constant");
        }
        constantNode.setSourceCode(getSource(ctx));

    }

    @Override
    public void enterAssertStatement(QDLParserParser.AssertStatementContext ctx) {
        stash(ctx, new AssertStatement(tp(ctx)));
    }

    @Override
    public void exitAssertStatement(QDLParserParser.AssertStatementContext ctx) {
        AssertStatement assertStatement = (AssertStatement) parsingMap.getStatementFromContext(ctx);
        boolean isFirst = true;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof TerminalNodeImpl) {
                continue;
            }
            if (isFirst) {
                assertStatement.setConditional((ExpressionInterface) resolveChild(ctx.getChild(i)));
                isFirst = false;
            } else {
                assertStatement.setMesssge((ExpressionNode) resolveChild(ctx.getChild(i)));
            }
        }
        assertStatement.setTokenPosition(tp(ctx));
        List<String> source = getSource(ctx);
        assertStatement.setSourceCode(source);
    }

    @Override
    public void enterAssertStatement2(QDLParserParser.AssertStatement2Context ctx) {
        stash(ctx, new AssertStatement(tp(ctx)));

    }

    @Override
    public void exitAssertStatement2(QDLParserParser.AssertStatement2Context ctx) {
        AssertStatement assertStatement = (AssertStatement) parsingMap.getStatementFromContext(ctx);
        boolean isFirst = true;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof TerminalNodeImpl) {
                continue;
            }
            if (isFirst) {
                assertStatement.setConditional((ExpressionInterface) resolveChild(ctx.getChild(i)));
                isFirst = false;
            } else {
                // Fix https://github.com/ncsa/qdl/issues/39
                assertStatement.setMesssge((ExpressionInterface) resolveChild(ctx.getChild(i)));

            }
        }
        assertStatement.setTokenPosition(tp(ctx));
        List<String> source = getSource(ctx);
        assertStatement.setSourceCode(source);
    }


    @Override
    public void enterExtract(QDLParserParser.ExtractContext ctx) {

    }

    @Override
    public void exitExtract(QDLParserParser.ExtractContext extractContext) {
        doSubset(extractContext, false);
    }

    @Override
    public void enterExtract2(QDLParserParser.Extract2Context ctx) {

    }

    @Override
    public void exitExtract2(QDLParserParser.Extract2Context ctx) {
        doSubset(ctx, true);
    }

    @Override
    public void enterExtract3(QDLParserParser.Extract3Context ctx) {

    }

    @Override
    public void exitExtract3(QDLParserParser.Extract3Context ctx) {
        doSubset(ctx, false);

    }

    @Override
    public void enterExtract4(QDLParserParser.Extract4Context ctx) {

    }

    @Override
    public void exitExtract4(QDLParserParser.Extract4Context ctx) {
        doSubset(ctx, true);

    }

    protected void doSubset(ParserRuleContext parseTree, boolean isWildcard) {
        StemExtractionNode ssn = new StemExtractionNode();
        ssn.setTokenPosition(tp(parseTree));
        ssn.setSourceCode(getSource(parseTree));
        stash(parseTree, ssn);
        boolean isStrict = false;
        IndexArg indexArg = new IndexArg();
        for (int i = 0; i < parseTree.getChildCount(); i++) {
            ParseTree p = parseTree.getChild(i);

            if ((p instanceof TerminalNodeImpl)) {
                switch (p.getText()) {
                    default:
                        throw new IllegalArgumentException("unknown extraction operator");
                    case StemExtractionNode.EXTRACT:
                        break;
                    case StemExtractionNode.EXTRACT_UNIQUE:
                        indexArg.strictOrder = true; // This gets set in a different pass, so must be stored
                        break;
                    case StemExtractionNode.EXTRACT_UNIQUE_STAR:
                    case StemExtractionNode.EXTRACT_LIST_UNIQUE_STAR:
                        indexArg.strictOrder = true; // This gets set in a different pass, so must be stored
                    case StemExtractionNode.EXTRACT_STAR:
                    case StemExtractionNode.EXTRACT_LIST_STAR:
                        indexArg.swri = new AllIndices();
                        ssn.addArgument(indexArg);
                        break;
                    case StemExtractionNode.EXTRACT_LIST:
                        indexArg.interpretListArg = true;
                        break;
                    case StemExtractionNode.EXTRACT_LIST_UNIQUE:
                        indexArg.interpretListArg = true;
                        indexArg.strictOrder = true;

                }

            } else {
                Statement s = resolveChild(p);
                if (s instanceof ExpressionInterface) {
                    indexArg = doSubsetExpressionInterface((ExpressionInterface) s, i, indexArg, ssn);
                }
                if (s instanceof FunctionDefinitionStatement) {

                }
            }
        }
    }

    private IndexArg doSubsetExpressionInterface(ExpressionInterface swri, int i, IndexArg indexArg, StemExtractionNode ssn) {
        if (i == 0) {
            if (swri instanceof VariableNode) {
                VariableNode vNode = (VariableNode) swri;
                vNode.setVariableReference(vNode.getVariableReference() + STEM_INDEX_MARKER);
                indexArg.swri = vNode;
                ssn.addArgument(indexArg);
            } else {
                if (swri instanceof ModuleExpression) {
                    // If it's a module (e.g. a#b.i) then mark the variable, b here, as b. so
                    // it is interpreted as a stem in later resolution.
                    ModuleExpression moduleExpression = (ModuleExpression) swri;
                    if (moduleExpression.getExpression() instanceof VariableNode) {
                        VariableNode vNode = (VariableNode) moduleExpression.getExpression();
                        vNode.setVariableReference(vNode.getVariableReference() + STEM_INDEX_MARKER);
                    }
                    indexArg.swri = moduleExpression;
                    ssn.addArgument(indexArg);

                } else {
                    indexArg.swri = swri;

                    ssn.addArgument(indexArg);
                }
            }
        } else {
            indexArg.swri = swri;
            ssn.addArgument(indexArg);
        }
        indexArg = new IndexArg();
        return indexArg;
    }

    /*
        a. := n(2,3,4,5)
   a\[1,2]\*\[;3]
  a\![2,1]\*\!*

    a\![;3]\[2,1]\!*
      a\![;3]\[2,1]\!*
  a\![3]\![2,1]\![2,3]

          a. := n(2,3,4,5)
  a\![3]\[2,1]\[2,3]\!*\[;4]\![|;4|]
  a\![3]\[2,1]\[2,3]\![5,7]\[;4]\![|;4|]
    a\1\*\!*\2\3\*\!*\!*\*\*

     */
    @Override
    public void enterDotOp2(QDLParserParser.DotOp2Context ctx) {


    }

    @Override
    public void exitDotOp2(QDLParserParser.DotOp2Context ctx) {
        QDLParserParser.ExpressionContext expressionContext = ctx.expression();
        if (expressionContext instanceof QDLParserParser.VariablesContext) {
            VariableNode vn = new VariableNode();
            vn.setSourceCode(getSource(ctx));
            vn.setTokenPosition(tp(ctx));
            stash(ctx, vn);
            QDLParserParser.VariablesContext variablesContext = (QDLParserParser.VariablesContext) ctx.expression();
            vn.setVariableReference(variablesContext.variable().getText() + STEM_INDEX_MARKER);
        }
        if (expressionContext instanceof QDLParserParser.DotOpContext) {
            ESN2 esn = new ESN2();
            esn.setSourceCode(getSource(ctx));
            esn.setTokenPosition(tp(ctx));
            stash(ctx, esn);
            esn.setLeftArg((ExpressionInterface) resolveChild(expressionContext));
        }
        if (expressionContext instanceof QDLParserParser.ModuleExpressionContext) {
            QDLParserParser.ModuleExpressionContext moduleExpressionContext = (QDLParserParser.ModuleExpressionContext) expressionContext;
            ModuleExpression moduleExpression = (ModuleExpression) resolveChild(moduleExpressionContext);

            ESN2 esn = new ESN2();
            // Have to make a new one or we get a recursive structure.
            ModuleExpression moduleExpression1 = new ModuleExpression();
            moduleExpression1.setAlias(moduleExpression.getAlias());
            if (moduleExpression.getExpression() instanceof VariableNode) {
                VariableNode vn = (VariableNode) moduleExpression.getExpression();
                vn.setVariableReference(vn.getVariableReference() + STEM_INDEX_MARKER);
                esn.setLeftArg(vn);
                esn.setSourceCode(vn.getSourceCode());
            } else {
                esn.setLeftArg(moduleExpression.getExpression()); // if it's a function or constant,leave alone.
            }
            moduleExpression1.setExpression(esn);
            //esn.setSourceCode(getSource(ctx));
            //esn.setTokenPosition(tp(ctx));
            stash(ctx, esn);
            stash(ctx, moduleExpression1);
            //esn.setLeftArg((StatementWithResultInterface) resolveChild(expressionContext));
            moduleExpression1.setTokenPosition(tp(ctx));
            //stash(ctx, moduleExpression);
            List<String> source = new ArrayList<>();
            source.add(ctx.getText());
            moduleExpression1.setSourceCode(source);
            moduleExpression1.setTokenPosition(tp(ctx));
        }
    }

    @Override
    public void enterLambdaDef(QDLParserParser.LambdaDefContext ctx) {

    }

    /**
     * Support for lambda expressions as first class objects in QDL. This is largely identical to the
     * older lambda function definition (which was only in terms of statements, hence could not
     * be used as an argument. At some point this should be refactored.
     *
     * @param lambdaContext
     */
    @Override
    public void exitLambdaDef(QDLParserParser.LambdaDefContext lambdaContext) {
        // Github https://github.com/ncsa/qdl/issues/99 change to parser yields 2 cases
        QDLParserParser.FunctionContext nameAndArgsNode;
        List<QDLParserParser.F_argsContext> justArgs = null;
        String name = null;
        if (lambdaContext.function() == null) {
            // case 1: This is an anonymous lambda function and has only a (possibly empty) argument list
            justArgs = lambdaContext.f_args();
        } else {
            // Case 2: there is a named function with a (possibly empty) f_args
            nameAndArgsNode = lambdaContext.function();
            name = nameAndArgsNode.getChild(0).getText();
            if (name.endsWith("(")) {
                name = name.substring(0, name.length() - 1);
            }
            justArgs = new ArrayList<>();
            //justArgs = nameAndArgsNode.f_args();
            if (nameAndArgsNode.f_args() != null) {
                // null means no argument. Setting it as per next line results
                // in a null as the element, which we don't want. We want an empty
                // list if there are no arguments.
                justArgs.add(nameAndArgsNode.f_args());
            }
        }


        FunctionRecord functionRecord = new FunctionRecord();
        functionRecord.setTokenPosition(tp(lambdaContext));
        // not quite the original source... The issue is that this comes parsed and stripped of any original
        // end of line markers, so we cannot tell what was there. Since it may include documentation lines
        // we have to add back in EOLs at the end of every statement so the comments don't get lost.
        // Best we can do with ANTLR...
        FunctionDefinitionStatement fds = new FunctionDefinitionStatement();
        fds.setTokenPosition(tp(ctx));
        fds.setLambda(true);
        functionRecord.setLambda(true);
        fds.setFunctionRecord(functionRecord);

        stash(lambdaContext, fds);
        // or things that need this later cannot find it -- enterLambdaDef is not reliably executing
        List<String> stringList = new ArrayList<>();
        for (int i = 0; i < lambdaContext.getChildCount(); i++) {
            stringList.add(lambdaContext.getChild(i).getText());
        }
        // ANTLR may strip final terminator. Put it back as needed.
        if (!stringList.get(stringList.size() - 1).endsWith(";")) {
            stringList.set(stringList.size() - 1, stringList.get(stringList.size() - 1) + ";");
        }
        functionRecord.sourceCode = stringList;


        functionRecord.setName(name);
        //for (QDLParserParser.ArgListContext argListContext : nameAndArgsNode.argList()) {
        if (justArgs.isEmpty()) {
            functionRecord.argNames = new ArrayList<>();
        } else {
            for (QDLParserParser.F_argsContext argListContext : justArgs) {
                // this is a comma delimited list of arguments.
                String allArgs = argListContext.getText();

                StringTokenizer st = new StringTokenizer(allArgs, ",");
                while (st.hasMoreElements()) {
                    functionRecord.argNames.add(st.nextToken());
                }
            }
        }
        functionRecord.setArgCount(functionRecord.argNames.size());

   /*     QDLParserParser.ExpressionBlockContext expressionBlockContext = lambdaContext.expressionBlock();
        if (expressionBlockContext != null && !expressionBlockContext.isEmpty()) {
            for (int i = 0; i < expressionBlockContext.getChildCount(); i++) {
                //resolveChild(expressionBlockContext.getChild(i));
                ParseTree p = expressionBlockContext.getChild(i);
                if (p instanceof TerminalNodeImpl) {
                    continue;
                }
                functionRecord.statements.add(resolveChild(expressionBlockContext.getChild(i)));
            }
        }*/

        QDLParserParser.ExpressionContext expressionContext = lambdaContext.expression();
        if (expressionContext != null && !expressionContext.isEmpty()) {

            // it's a single expression most likely. Check to see if it needs wrapped in
            // a return
            Statement stmt = resolveChild(expressionContext);
            // Contract: Wrap simple expressions in a return.
            if (stmt instanceof ExpressionInterface) {
                if (stmt instanceof ExpressionImpl) {
                    ExpressionImpl expr = (ExpressionImpl) stmt;
                    if (expr.getOperatorType() != SystemEvaluator.RETURN_TYPE) {
                        Polyad expr1 = new Polyad(SystemEvaluator.RETURN_TYPE);
                        expr1.setTokenPosition(tp(lambdaContext)); // best we can do
                        expr1.setName(SystemEvaluator.RETURN);
                        expr1.addArgument(expr);
                        functionRecord.statements.add(expr1); // wrapped in a return
                    } else {
                        functionRecord.statements.add(expr); // already has a return
                    }
                } else {
                    Polyad expr1 = new Polyad(SystemEvaluator.RETURN_TYPE);
                    expr1.setTokenPosition(tp(lambdaContext)); // best we can do

                    expr1.setName(SystemEvaluator.RETURN);
                    expr1.addArgument((ExpressionInterface) stmt);
                    functionRecord.statements.add(expr1); // wrapped in a return
                }
            } else {
                functionRecord.statements.add(stmt); // a statement, not merely an expression
            }

        }
    }

    @Override
    public void enterBlockStatement(QDLParserParser.BlockStatementContext ctx) {
        BlockStatement b = new BlockStatement();
        stash(ctx, b);
    }

    @Override
    public void exitBlockStatement(QDLParserParser.BlockStatementContext ctx) {
        BlockStatement block = (BlockStatement) parsingMap.getStatementFromContext(ctx);
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof TerminalNodeImpl) {
                continue;
            }
            // has a single parse statement block.
            QDLParserParser.StatementBlockContext statementBlockContext = (QDLParserParser.StatementBlockContext) ctx.getChild(i);
            ParseStatementBlock parseStatementBlock = (ParseStatementBlock) resolveChild(statementBlockContext);
            block.setStatements(parseStatementBlock.getStatements());
        }
        block.setSourceCode(getSource(ctx));
        block.setTokenPosition(tp(ctx));
    }

    @Override
    public void enterFloorOrCeilingExpression(QDLParserParser.FloorOrCeilingExpressionContext ctx) {

    }

    @Override
    public void exitFloorOrCeilingExpression(QDLParserParser.FloorOrCeilingExpressionContext ctx) {
        boolean isFloor = ctx.children.get(0).getText().equals(OpEvaluator.FLOOR);
        Monad monad = new Monad(isFloor ? OpEvaluator.FLOOR_VALUE : OpEvaluator.CEILING_VALUE, false);
        monad.setSourceCode(getSource(ctx));
        monad.setTokenPosition(tp(ctx));
        stash(ctx, monad);
        finish(monad, ctx);
    }

    @Override
    public void enterUnaryTildeExpression(QDLParserParser.UnaryTildeExpressionContext ctx) {

    }

    /**
     * Syntactic sugar. Unary ~ means join to an empty stem so re-order the elements
     * of the list.
     *
     * @param ctx the parse tree
     */
    @Override
    public void exitUnaryTildeExpression(QDLParserParser.UnaryTildeExpressionContext ctx) {
        Dyad dyad;
        dyad = new Dyad(OpEvaluator.TILDE_VALUE);
        dyad.setUnary(true);
        dyad.setTokenPosition(tp(ctx));
        stash(ctx, dyad);
        dyad.setLeftArgument(new ConstantNode(new StemValue()));
        dyad.setRightArgument((ExpressionInterface) resolveChild(ctx.expression()));
        List<String> source = new ArrayList<>();
        source.add(ctx.getText());
        dyad.setSourceCode(source);
    }

    @Override
    public void enterModuleExpression(QDLParserParser.ModuleExpressionContext ctx) {

    }

    @Override
    public void exitModuleExpression(QDLParserParser.ModuleExpressionContext ctx) {
        boolean defaultNS = ctx.getChildCount() == 2;
/*
        if (ctx.getChildCount() == 2) {
            // This is of the form #expression, so it is just an expression
            // Don't wrap it and no special processing needed.
            stash(ctx, resolveChild(ctx.expression()));
            return;
        }
*/
        /**
         * Check for intrinsic violations here rather than try to set things up and check later.
         * Basically any varliable or function that starts with a __ cannot have an alias attached to
         * it, so X#__a, X#__f(3) will fail. 
         */
        Statement statement = resolveChild(ctx.expression());
        //Statement statement = resolveChild(ctx.expression(1));
        if (statement instanceof FunctionDefinitionStatement) {
            throw new IntrinsicViolation("cannot define function in an existing module", statement);
        }
        if (statement instanceof VariableNode) {
            if (State.isIntrinsic(((VariableNode) statement).getVariableReference())) {
                throw new IntrinsicViolation("cannot access intrinsic variable outside of module.", statement);
            }
        }
        if (statement instanceof Polyad) {
            if (State.isIntrinsic(((Polyad) statement).getName())) {
                throw new IntrinsicViolation("cannot access intrinsic function outside of module.", statement);
            }
        }

        ModuleExpression moduleExpression = new ModuleExpression();
        moduleExpression.setTokenPosition(tp(ctx));
        stash(ctx, moduleExpression);
        List<String> source = new ArrayList<>();
        source.add(ctx.getText());
        moduleExpression.setSourceCode(source);
        if (defaultNS) {
            moduleExpression.setAlias("");
            moduleExpression.setDefaultNamespace(true);
        } else {
            ExpressionInterface var = (ExpressionInterface) resolveChild(ctx.variable());
            //ExpressionInterface var = (ExpressionInterface) resolveChild(ctx.expression(0));
            if (!(var instanceof VariableNode)) {
                throw new IllegalArgumentException("unexpected argument for alias");
            }
            // Fix https://github.com/ncsa/qdl/issues/56
            String moduleName = ((VariableNode) var).getVariableReference();
            if (MetaEvaluator.isSystemNS(moduleName)) {
                if (statement instanceof Polyad) {
                    String functionName = ((Polyad) statement).getName();
                    if (!state.getMetaEvaluator().isBuiltIin(moduleName, functionName)) {
                        throw new NamespaceException("no function named '" + functionName + "' exists in the system module '" + moduleName + "'");
                    }
                }
            }
            moduleExpression.setAlias(moduleName);
        }
        moduleExpression.setExpression((ExpressionInterface) statement);

    }

    @Override
    public void enterSet(QDLParserParser.SetContext ctx) {

    }

    @Override
    public void exitSet(QDLParserParser.SetContext ctx) {

    }

    @Override
    public void enterSetThing(QDLParserParser.SetThingContext ctx) {


    }

    @Override
    public void exitSetThing(QDLParserParser.SetThingContext ctx) {
        QDLSetNode setNode = new QDLSetNode();
        setNode.setTokenPosition(tp(ctx));
        setNode.setSourceCode(getSource(ctx));
        stash(ctx, setNode);
        for (int i = 0; i < ctx.set().getChildCount(); i++) {
            ParseTree pt = ctx.set().getChild(i);
            if (!(pt instanceof TerminalNode)) {
                ExpressionInterface stmt = (ExpressionInterface) resolveChild(pt);
                setNode.getStatements().add(stmt);

            }
        }
    }

    @Override
    public void enterEpsilon(QDLParserParser.EpsilonContext ctx) {

    }

    @Override
    public void exitEpsilon(QDLParserParser.EpsilonContext ctx) {
        String x = ctx.getChild(1).getText();
        Dyad dyad;
        if (x.equals(OpEvaluator.EPSILON)) {
            dyad = new Dyad(OpEvaluator.EPSILON_VALUE);
        } else {
            dyad = new Dyad(OpEvaluator.EPSILON_NOT_VALUE);
        }
        dyad.setTokenPosition(tp(ctx));
        stash(ctx, dyad);
        finish(dyad, ctx);
    }

    @Override
    public void enterLocalStatement(QDLParserParser.LocalStatementContext ctx) {
        LocalBlockStatement b = new LocalBlockStatement();
        b.setTokenPosition(tp(ctx));
        stash(ctx, b);
    }

    @Override
    public void exitLocalStatement(QDLParserParser.LocalStatementContext ctx) {
        // This is really identical to the block statement in every way except that the
        // state is local.
        LocalBlockStatement localBlock = (LocalBlockStatement) parsingMap.getStatementFromContext(ctx);
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof TerminalNodeImpl) {
                continue;
            }
            // has a single parse statement block.
            QDLParserParser.StatementBlockContext statementBlockContext = (QDLParserParser.StatementBlockContext) ctx.getChild(i);
            ParseStatementBlock parseStatementBlock = (ParseStatementBlock) resolveChild(statementBlockContext);
            localBlock.setStatements(parseStatementBlock.getStatements());
        }
        localBlock.setTokenPosition(tp(ctx));
        localBlock.setSourceCode(getSource(ctx));
    }

    @Override
    public void enterIntersectionOrUnion(QDLParserParser.IntersectionOrUnionContext ctx) {
        stash(ctx, new Dyad(OpEvaluator.AND_VALUE, tp(ctx)));

    }

    @Override
    public void exitIntersectionOrUnion(QDLParserParser.IntersectionOrUnionContext ctx) {
        Dyad dyad = (Dyad) parsingMap.getStatementFromContext(ctx);
        dyad.setOperatorType(state.getOperatorType(ctx.op.getText()));
        dyad.setTokenPosition(tp(ctx));
        finish(dyad, ctx);
    }

    @Override
    public void enterToSet(QDLParserParser.ToSetContext ctx) {

    }

    @Override
    public void exitToSet(QDLParserParser.ToSetContext ctx) {

        stash(ctx, new Monad(OpEvaluator.TO_SET_VALUE, false));// automatically prefix
        Monad monad = (Monad) parsingMap.getStatementFromContext(ctx);
        monad.setTokenPosition(tp(ctx));
        finish(monad, ctx);
    }

    @Override
    public void enterIs_a(QDLParserParser.Is_aContext ctx) {

    }

    @Override
    public void exitIs_a(QDLParserParser.Is_aContext ctx) {
        checkLexer(ctx);
        Dyad dyad = new Dyad(OpEvaluator.IS_A_VALUE);
        dyad.setTokenPosition(tp(ctx));
        stash(ctx, dyad);
        finish(dyad, ctx);
    }

    @Override
    public void enterIsDefinedExpression(QDLParserParser.IsDefinedExpressionContext ctx) {

    }

    @Override
    public void exitIsDefinedExpression(QDLParserParser.IsDefinedExpressionContext ctx) {
        checkLexer(ctx);
        String x = ctx.getChild(0).getText();
        Monad monad = null;
        if (x.equals(OpEvaluator.IS_DEFINED)) {
            monad = new Monad(OpEvaluator.IS_DEFINED_VALUE, false);
        } else {
            monad = new Monad(OpEvaluator.IS_NOT_DEFINED_VALUE, false);
        }
        monad.setTokenPosition(tp(ctx));
        stash(ctx, monad);
        finish(monad, ctx);
    }

    @Override
    public void enterIsDefinedDyadicExpression(QDLParserParser.IsDefinedDyadicExpressionContext ctx) {

    }

    @Override
    public void exitIsDefinedDyadicExpression(QDLParserParser.IsDefinedDyadicExpressionContext ctx) {
        checkLexer(ctx);
        String x = ctx.getChild(1).getText();

        Dyad dyad;
        if (x.equals(OpEvaluator.IS_DEFINED)) {
            dyad = new Dyad(OpEvaluator.IS_DEFINED_VALUE);
        } else {
            dyad = new Dyad(OpEvaluator.IS_NOT_DEFINED_VALUE);
        }

        dyad.setTokenPosition(tp(ctx));
        stash(ctx, dyad);
        finish(dyad, ctx);
    }

    @Override
    public void enterContainsKey(QDLParserParser.ContainsKeyContext ctx) {

    }

    @Override
    public void exitContainsKey(QDLParserParser.ContainsKeyContext ctx) {
        checkLexer(ctx);
        String x = ctx.getChild(1).getText();
        Dyad dyad;
        if (x.equals(OpEvaluator.CONTAINS_KEY)) {
            dyad = new Dyad(OpEvaluator.CONTAINS_KEY_VALUE);
        } else {
            dyad = new Dyad(OpEvaluator.NOT_CONTAINS_KEY_VALUE);
        }
        dyad.setTokenPosition(tp(ctx));
        stash(ctx, dyad);
        finish(dyad, ctx);
    }

    @Override
    public void enterForAll(QDLParserParser.ForAllContext ctx) {
    }

    @Override
    public void exitForAll(QDLParserParser.ForAllContext ctx) {
        checkLexer(ctx);
        Dyad dyad = new Dyad(OpEvaluator.FOR_ALL_KEY_VALUE);
        dyad.setTokenPosition(tp(ctx));
        stash(ctx, dyad);
        finish(dyad, ctx);
    }

    /**
     * First cut of catching lexer exceptions and handling them
     *
     * @param pre
     */
    protected void checkLexer(ParserRuleContext pre) {
        if (pre.exception == null) {
            return;
        }
        RecognitionException re = pre.exception;
        String type = SYNTAX_TYPE;
        if (re instanceof InputMismatchException) {
            // Any type of mismatch, when the current token does not match the expected token
            type = MISMATCH_TYPE;
        }
        if (re instanceof LexerNoViableAltException) {
            // Lexer cannot figure out which of two or more possible alternatives to resolve a token there are
            type = AMBIGUOUS_TYPE;
        }
        if (re instanceof NoViableAltException) {
            // Parser cannot figure out which of two or more possible alternatives to resolve a token there are
            type = SYNTAX_TYPE;
        }

        if (re instanceof FailedPredicateException) {
            // A token was found but it could not be validated as the correct one to use.
            type = AMBIGUOUS_TYPE;
        }
        String text = re.getOffendingToken().getText();
        ParsingException px;
        if (KEYWORDS.contains(text)) {
            px = new ParsingException(
                    "keyword error for '" + text + "'",
                    re.getOffendingToken().getLine(),
                    re.getOffendingToken().getCharPositionInLine(),
                    type
            );
            px.setKeywordError(true);

        } else {
            px = new ParsingException("parsing error, got " + text,
                    re.getOffendingToken().getLine(),
                    re.getOffendingToken().getCharPositionInLine(),
                    type
            );

        }
        throw px;
    }

    List<String> KEYWORDS = Arrays.asList(QDLConstants.KEYWORDS);

    @Override
    public void enterExpressionDyadicOps(QDLParserParser.ExpressionDyadicOpsContext ctx) {

    }

    @Override
    public void exitExpressionDyadicOps(QDLParserParser.ExpressionDyadicOpsContext ctx) {
        exitDyadicOps(ctx);
/*
        String x = ctx.getChild(1).getText();
        Dyad dyad;
        dyad = new Dyad(getOpEvaluator().getType(x));
        dyad.setTokenPosition(tp(ctx));
        stash(ctx, dyad);
        finish(dyad, ctx);
*/
    }

    protected void exitDyadicOps(ParserRuleContext ctx) {
        String x = ctx.getChild(1).getText();
        Dyad dyad;
        dyad = new Dyad(getOpEvaluator().getType(x));
        dyad.setTokenPosition(tp(ctx));
        stash(ctx, dyad);
        finish(dyad, ctx);
    }

    @Override
    public void enterFrefDyadicOps(QDLParserParser.FrefDyadicOpsContext ctx) {

    }

    @Override
    public void exitFrefDyadicOps(QDLParserParser.FrefDyadicOpsContext ctx) {
        String op = ctx.op.getText();
        checkLexer(ctx);
        Dyad dyad = new Dyad(getOpEvaluator().getType(op));
        dyad.setTokenPosition(tp(ctx));
        stash(ctx, dyad);
        finish(dyad, ctx);
    }

    @Override
    public void enterUnaryTransposeExpression(QDLParserParser.UnaryTransposeExpressionContext ctx) {

    }

    @Override
    public void exitUnaryTransposeExpression(QDLParserParser.UnaryTransposeExpressionContext ctx) {
        Monad monad = new Monad(getOpEvaluator().getType(ctx.Transpose().getText()), false);
        monad.setSourceCode(getSource(ctx));
        monad.setTokenPosition(tp(ctx));
        stash(ctx, monad);
        finish(monad, ctx);
    }

    @Override
    public void enterTransposeOperator(QDLParserParser.TransposeOperatorContext ctx) {

    }

    @Override
    public void exitTransposeOperator(QDLParserParser.TransposeOperatorContext ctx) {
        exitDyadicOps(ctx);
    }

    /**
     * Only need this for type lookup. Don't use for anything else
     *
     * @return
     */
    public OpEvaluator getOpEvaluator() {
        return opEvaluator;
    }

    OpEvaluator opEvaluator = new OpEvaluator();

    @Override
    public void enterSwitchExpression(QDLParserParser.SwitchExpressionContext ctx) {
        SelectExpressionNode s = new SelectExpressionNode();
        stash(ctx, s);
    }

    @Override
    public void exitSwitchExpression(QDLParserParser.SwitchExpressionContext ctx) {
        SelectExpressionNode selectExpressionNode = (SelectExpressionNode) parsingMap.getStatementFromContext(ctx);
        selectExpressionNode.setSWITCH((ExpressionInterface) resolveChild(ctx.getChild(0)));
        selectExpressionNode.setCASE((ExpressionInterface) resolveChild(ctx.getChild(2)));
        if (3 < ctx.getChildCount()) {
            selectExpressionNode.setDEFAULT((ExpressionInterface) resolveChild(ctx.getChild(4)));
        } else {
            selectExpressionNode.setDEFAULT(new ConstantNode(QDLNullValue.getNullValue()));
        }
        selectExpressionNode.setTokenPosition(tp(ctx));
        selectExpressionNode.setSourceCode(getSource(ctx));
    }

    @Override
    public void enterUnaryApplyExpression(QDLParserParser.UnaryApplyExpressionContext ctx) {

    }

    @Override
    public void exitUnaryApplyExpression(QDLParserParser.UnaryApplyExpressionContext ctx) {
        Monad monad;
        monad = new Monad(OpEvaluator.APPLY_OP_VALUE, false);
        //monad.setUnary(true);
        monad.setTokenPosition(tp(ctx));
        stash(ctx, monad);
        finish(monad, ctx);
    }


    @Override
    public void enterAppliesOperator(QDLParserParser.AppliesOperatorContext ctx) {

    }

    @Override
    public void exitAppliesOperator(QDLParserParser.AppliesOperatorContext ctx) {
        exitDyadicOps(ctx);
    }

    @Override
    public void enterFunctionReference(QDLParserParser.FunctionReferenceContext ctx) {
    }

    @Override
    public void enterOp_ref(QDLParserParser.Op_refContext ctx) {
        FunctionReferenceNode frn = new FunctionReferenceNode();
        frn.setTokenPosition(tp(ctx));
        stash(ctx, frn);
    }

    @Override
    public void exitOp_ref(QDLParserParser.Op_refContext ctx) {
        FunctionReferenceNode frn = (FunctionReferenceNode) parsingMap.getStatementFromContext(ctx);
        String name = ctx.getText();
        if (StringUtils.isTrivial(name)) {
            throw new ParsingException("could not resolve function reference name in this scope");
        }
        frn.setTokenPosition(tp(ctx));
        frn.setSourceCode(getSource(ctx));
        if (name.contains(QDLConstants.FUNCTION_REFERENCE_MARKER2)) {
            name = name.substring(QDLConstants.FUNCTION_REFERENCE_MARKER2.length());
        } else {
            name = name.substring(QDLConstants.FUNCTION_REFERENCE_MARKER.length());
        }
//   if(name.contains(NS_DELIMITER)){
//     name = name.substring(1+name.lastIndexOf(NS_DELIMITER));
// }
        int parenIndex = name.indexOf("(");
        if (-1 < parenIndex) {
            // whack off any dangling parenthese
            name = name.substring(0, name.indexOf("("));
        }
        frn.setFunctionName(name);
    }

    @Override
    public void enterOperatorReference(QDLParserParser.OperatorReferenceContext ctx) {
    }

    @Override
    public void exitOperatorReference(QDLParserParser.OperatorReferenceContext ctx) {
    }

    @Override
    public void exitFunctionReference(QDLParserParser.FunctionReferenceContext ctx) {
        FunctionReferenceNode fNode = null;
        fNode = new FunctionReferenceNode();
        // The symbol always includes the @ or ⊗ for the function reference. Strip it.
        String marker = ctx.FunctionMarker().getText();
        String symbol = null;
        Statement statement =  resolveChild(ctx.expression());
        if(statement instanceof ExpressionInterface) {
            ExpressionInterface expression = (ExpressionInterface) resolveChild(ctx.expression());
            switch (expression.getNodeType()) {
                case VARIABLE_NODE:
                    symbol = ((VariableNode) expression).getVariableReference();
                    break;
                case POLYAD_NODE:
                    symbol = ((Polyad) expression).getName();
                    break;
                case MODULE_NODE:
                    symbol = ((ModuleExpression) expression).getAlias();
                    break;
                default:
                    throw new ParsingException("unknown node type:" + expression.getClass().getCanonicalName());
            }
            int parenIndex = symbol.indexOf("(");
            if (-1 < parenIndex) {
                // whack off any dangling parenthes
                symbol = symbol.substring(0, symbol.indexOf("("));
            }
            fNode.setFunctionName(symbol);
        }else{
            throw new ParsingException("unknown node type:" + statement.getClass().getCanonicalName());

        }
        fNode.setTokenPosition(tp(ctx));
        fNode.setSourceCode(getSource(ctx));
        stash(ctx, fNode);
    }



 /*     @Override
      public void enterF_ref(QDLParserParser.F_refContext ctx) {
          FunctionReferenceNode frn = new FunctionReferenceNode();
          frn.setTokenPosition(tp(ctx));
          stash(ctx, frn);

      }*/

/*     @Override
      public void exitF_ref(QDLParserParser.F_refContext ctx) {
          FunctionReferenceNode frn = (FunctionReferenceNode) parsingMap.getStatementFromContext(ctx);
          String name = ctx.getText();
          if (StringUtils.isTrivial(name)) {
              throw new ParsingException("could not resolve function reference name in this scope");
          }
          frn.setTokenPosition(tp(ctx));
          frn.setSourceCode(getSource(ctx));
          if (name.contains(QDLConstants.FUNCTION_REFERENCE_MARKER2)) {
              name = name.substring(QDLConstants.FUNCTION_REFERENCE_MARKER2.length());
          } else {
              name = name.substring(QDLConstants.FUNCTION_REFERENCE_MARKER.length());
          }
         //   if(name.contains(NS_DELIMITER)){
         //     name = name.substring(1+name.lastIndexOf(NS_DELIMITER));
         // }
          int parenIndex = name.indexOf("(");
          if (-1 < parenIndex) {
              // whack off any dangling parenthese
              name = name.substring(0, name.indexOf("("));
          }
          frn.setFunctionName(name);
      }*/

  /*    @Override
      public void enterF_arg(QDLParserParser.F_argContext ctx) {
          //  System.out.println("enter f_arg");

      }

      @Override
      public void exitF_arg(QDLParserParser.F_argContext ctx) {
          //  System.out.println("exit f_arg");

      }*/

    @Override
    public void enterDyadicFunctionRefernce(QDLParserParser.DyadicFunctionRefernceContext ctx) {
        DyadicFunctionReferenceNode frn = new DyadicFunctionReferenceNode();
        frn.setTokenPosition(tp(ctx));
        stash(ctx, frn);
    }

    @Override
    public void exitDyadicFunctionRefernce(QDLParserParser.DyadicFunctionRefernceContext ctx) {
        DyadicFunctionReferenceNode fNode = (DyadicFunctionReferenceNode) resolveChild(ctx);
        ExpressionInterface lArg = (ExpressionInterface) resolveChild(ctx.expression(0));
        ExpressionInterface expression = (ExpressionInterface) resolveChild(ctx.expression(1));
        ArrayList<ExpressionInterface> args = new ArrayList<>();
        args.add(lArg);
        args.add(expression);
        fNode.setArguments(args);
        // The symbol always includes the @ or ⊗ for the function reference. Strip it.
        String marker = ctx.FunctionMarker().getText();
        String symbol;
        switch (expression.getNodeType()) {
            case VARIABLE_NODE:
                symbol = ((VariableNode) expression).getVariableReference();
                break;
            case POLYAD_NODE:
                symbol = ((Polyad) expression).getName();
                break;
            case MODULE_NODE:
                symbol = ((ModuleExpression) expression).getAlias();
                break;
            default:
                throw new ParsingException("unknown node type:" + expression.getClass().getCanonicalName());
        }

/*
        int parenIndex = symbol.indexOf("(");
        if (-1 < parenIndex) {
            // whack off any dangling parenthese
            symbol = symbol.substring(0, symbol.indexOf("("));
        }
*/
        fNode.setFunctionName(symbol);
        fNode.setTokenPosition(tp(ctx));
        fNode.setSourceCode(getSource(ctx));
        stash(ctx, fNode);
    }
/*
      module['a:x'][g(x,y)->x*y;]
  z:=import('a:x')
  ∂z#@g
     */
    /*
           ConstantNode constantNode = null;
        Object value;
        try {
            value = Long.parseLong(ctx.getChild(0).getText());
        } catch (NumberFormatException nfx) {
            // it is possible the user is entering a simply huge number, try plan B
            value = new BigDecimal(ctx.getChild(0).getText());
        }
        constantNode = new ConstantNode(value);
        constantNode.setTokenPosition(tp(ctx));
        constantNode.setSourceCode(getSource(ctx));
        stash(ctx, constantNode);
     */

    @Override
    public void enterAxis(QDLParserParser.AxisContext ctx) {

    }

    @Override
    public void exitAxis(QDLParserParser.AxisContext ctx) {
        AxisExpression expr = new AxisExpression();
        expr.setStar(null != ctx.Times());
        ArrayList<ExpressionInterface> args = new ArrayList<>();
        ExpressionInterface lArg = (ExpressionInterface) resolveChild(ctx.expression(0));
        args.add(lArg);
        if (!expr.isStar()) {
            ExpressionInterface rArg = (ExpressionInterface) resolveChild(ctx.expression(1));
            args.add(rArg);
        }
        expr.setArguments(args);
        expr.setTokenPosition(tp(ctx));
        expr.setSourceCode(getSource(ctx));
        stash(ctx, expr);
    }

    @Override
    public void enterExcise(QDLParserParser.ExciseContext ctx) {

    }

    @Override
    public void exitExcise(QDLParserParser.ExciseContext ctx) {

        Dyad dyad = new Dyad(OpEvaluator.UNKNOWN_VALUE, tp(ctx));
        stash(ctx, dyad);
        dyad.setOperatorType(state.getOperatorType(ctx.op.getText()));
        finish(dyad, ctx);
    }

    @Override
    public void enterNotTildeExpression(QDLParserParser.NotTildeExpressionContext ctx) {

    }

    // Fix https://github.com/ncsa/qdl/issues/114 Note that without qualification that !~ is only dyadic
    // there will be parser errors for the monadic case which is, in fact ! ~list (reorder, neagate).
    // This explicitly adds this important case to the parser.
    @Override
    public void exitNotTildeExpression(QDLParserParser.NotTildeExpressionContext ctx) {
        // create the join
        Dyad dyad;
        dyad = new Dyad(OpEvaluator.TILDE_VALUE);
        dyad.setUnary(true);
        dyad.setTokenPosition(tp(ctx));
        dyad.setLeftArgument(new ConstantNode(new StemValue()));
        dyad.setRightArgument((ExpressionInterface) resolveChild(ctx.expression()));

        // create the wrapping logical not.
        stash(ctx, new Monad(OpEvaluator.NOT_VALUE, false));// automatically prefix
        Monad monad = (Monad) parsingMap.getStatementFromContext(ctx);
        monad.setArgument(dyad);
        monad.setTokenPosition(tp(ctx));
        monad.setPostFix(false);
        List<String> source = new ArrayList<>();
        source.add(ctx.getText());
        monad.setSourceCode(source);
    }

    @Override
    public void enterMy_integer(QDLParserParser.My_integerContext ctx) {

    }

    @Override
    public void exitMy_integer(QDLParserParser.My_integerContext ctx) {
// Only exists as part of a decimal. If not a string of  digits, ANTLR sets Integer() to null.
        if (ctx.Integer() == null) {
            TokenPosition tp = tp(ctx);
            throw new ParsingException("invalid decimal number: " + ctx.getText(), tp.line, tp.col, SYNTAX_TYPE);

        }

    }

    @Override
    public void enterDecimalNumber2(QDLParserParser.DecimalNumber2Context ctx) {

    }

    @Override
    public void exitDecimalNumber2(QDLParserParser.DecimalNumber2Context ctx) {
        String text = ctx.my_integer().getText();

        BigDecimal bd = new BigDecimal("." + text);
        ConstantNode constantNode = new ConstantNode(asQDLValue(bd));
        stash(ctx, constantNode);
    }

    @Override
    public void enterStemDefaultValue(QDLParserParser.StemDefaultValueContext ctx) {

    }

    @Override
    public void exitStemDefaultValue(QDLParserParser.StemDefaultValueContext ctx) {
        ESN2 esn = new ESN2();
        esn.setTokenPosition(tp(ctx));
        esn.setSourceCode(getSource(ctx));
        esn.setDefaultValueNode(true);
        stash(ctx, esn);
        Statement s = resolveChild(ctx.getChild(0));
        if (s instanceof VariableNode) {
            VariableNode vNode = (VariableNode) s;
            vNode.setVariableReference(vNode.getVariableReference() + STEM_INDEX_MARKER);
            esn.setLeftArg(vNode);
        } else {
            esn.setLeftArg((ExpressionInterface) s);
        }
    }
}


