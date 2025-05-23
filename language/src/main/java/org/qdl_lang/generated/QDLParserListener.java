// Generated from QDLParser.g4 by ANTLR 4.9.3
package org.qdl_lang.generated;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link QDLParserParser}.
 */
public interface QDLParserListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#elements}.
	 * @param ctx the parse tree
	 */
	void enterElements(QDLParserParser.ElementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#elements}.
	 * @param ctx the parse tree
	 */
	void exitElements(QDLParserParser.ElementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#element}.
	 * @param ctx the parse tree
	 */
	void enterElement(QDLParserParser.ElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#element}.
	 * @param ctx the parse tree
	 */
	void exitElement(QDLParserParser.ElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterStatement(QDLParserParser.StatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitStatement(QDLParserParser.StatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#conditionalStatement}.
	 * @param ctx the parse tree
	 */
	void enterConditionalStatement(QDLParserParser.ConditionalStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#conditionalStatement}.
	 * @param ctx the parse tree
	 */
	void exitConditionalStatement(QDLParserParser.ConditionalStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfStatement(QDLParserParser.IfStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#ifStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfStatement(QDLParserParser.IfStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#ifElseStatement}.
	 * @param ctx the parse tree
	 */
	void enterIfElseStatement(QDLParserParser.IfElseStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#ifElseStatement}.
	 * @param ctx the parse tree
	 */
	void exitIfElseStatement(QDLParserParser.IfElseStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#loopStatement}.
	 * @param ctx the parse tree
	 */
	void enterLoopStatement(QDLParserParser.LoopStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#loopStatement}.
	 * @param ctx the parse tree
	 */
	void exitLoopStatement(QDLParserParser.LoopStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#switchStatement}.
	 * @param ctx the parse tree
	 */
	void enterSwitchStatement(QDLParserParser.SwitchStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#switchStatement}.
	 * @param ctx the parse tree
	 */
	void exitSwitchStatement(QDLParserParser.SwitchStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#defineStatement}.
	 * @param ctx the parse tree
	 */
	void enterDefineStatement(QDLParserParser.DefineStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#defineStatement}.
	 * @param ctx the parse tree
	 */
	void exitDefineStatement(QDLParserParser.DefineStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#lambdaStatement}.
	 * @param ctx the parse tree
	 */
	void enterLambdaStatement(QDLParserParser.LambdaStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#lambdaStatement}.
	 * @param ctx the parse tree
	 */
	void exitLambdaStatement(QDLParserParser.LambdaStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#moduleStatement}.
	 * @param ctx the parse tree
	 */
	void enterModuleStatement(QDLParserParser.ModuleStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#moduleStatement}.
	 * @param ctx the parse tree
	 */
	void exitModuleStatement(QDLParserParser.ModuleStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#tryCatchStatement}.
	 * @param ctx the parse tree
	 */
	void enterTryCatchStatement(QDLParserParser.TryCatchStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#tryCatchStatement}.
	 * @param ctx the parse tree
	 */
	void exitTryCatchStatement(QDLParserParser.TryCatchStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#assertStatement}.
	 * @param ctx the parse tree
	 */
	void enterAssertStatement(QDLParserParser.AssertStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#assertStatement}.
	 * @param ctx the parse tree
	 */
	void exitAssertStatement(QDLParserParser.AssertStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void enterBlockStatement(QDLParserParser.BlockStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#blockStatement}.
	 * @param ctx the parse tree
	 */
	void exitBlockStatement(QDLParserParser.BlockStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#localStatement}.
	 * @param ctx the parse tree
	 */
	void enterLocalStatement(QDLParserParser.LocalStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#localStatement}.
	 * @param ctx the parse tree
	 */
	void exitLocalStatement(QDLParserParser.LocalStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#assertStatement2}.
	 * @param ctx the parse tree
	 */
	void enterAssertStatement2(QDLParserParser.AssertStatement2Context ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#assertStatement2}.
	 * @param ctx the parse tree
	 */
	void exitAssertStatement2(QDLParserParser.AssertStatement2Context ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#statementBlock}.
	 * @param ctx the parse tree
	 */
	void enterStatementBlock(QDLParserParser.StatementBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#statementBlock}.
	 * @param ctx the parse tree
	 */
	void exitStatementBlock(QDLParserParser.StatementBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#docStatementBlock}.
	 * @param ctx the parse tree
	 */
	void enterDocStatementBlock(QDLParserParser.DocStatementBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#docStatementBlock}.
	 * @param ctx the parse tree
	 */
	void exitDocStatementBlock(QDLParserParser.DocStatementBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#expressionBlock}.
	 * @param ctx the parse tree
	 */
	void enterExpressionBlock(QDLParserParser.ExpressionBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#expressionBlock}.
	 * @param ctx the parse tree
	 */
	void exitExpressionBlock(QDLParserParser.ExpressionBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#conditionalBlock}.
	 * @param ctx the parse tree
	 */
	void enterConditionalBlock(QDLParserParser.ConditionalBlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#conditionalBlock}.
	 * @param ctx the parse tree
	 */
	void exitConditionalBlock(QDLParserParser.ConditionalBlockContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#fdoc}.
	 * @param ctx the parse tree
	 */
	void enterFdoc(QDLParserParser.FdocContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#fdoc}.
	 * @param ctx the parse tree
	 */
	void exitFdoc(QDLParserParser.FdocContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#iInterval}.
	 * @param ctx the parse tree
	 */
	void enterIInterval(QDLParserParser.IIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#iInterval}.
	 * @param ctx the parse tree
	 */
	void exitIInterval(QDLParserParser.IIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#rInterval}.
	 * @param ctx the parse tree
	 */
	void enterRInterval(QDLParserParser.RIntervalContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#rInterval}.
	 * @param ctx the parse tree
	 */
	void exitRInterval(QDLParserParser.RIntervalContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#set}.
	 * @param ctx the parse tree
	 */
	void enterSet(QDLParserParser.SetContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#set}.
	 * @param ctx the parse tree
	 */
	void exitSet(QDLParserParser.SetContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#stemVariable}.
	 * @param ctx the parse tree
	 */
	void enterStemVariable(QDLParserParser.StemVariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#stemVariable}.
	 * @param ctx the parse tree
	 */
	void exitStemVariable(QDLParserParser.StemVariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#stemEntry}.
	 * @param ctx the parse tree
	 */
	void enterStemEntry(QDLParserParser.StemEntryContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#stemEntry}.
	 * @param ctx the parse tree
	 */
	void exitStemEntry(QDLParserParser.StemEntryContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#stemList}.
	 * @param ctx the parse tree
	 */
	void enterStemList(QDLParserParser.StemListContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#stemList}.
	 * @param ctx the parse tree
	 */
	void exitStemList(QDLParserParser.StemListContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#stemValue}.
	 * @param ctx the parse tree
	 */
	void enterStemValue(QDLParserParser.StemValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#stemValue}.
	 * @param ctx the parse tree
	 */
	void exitStemValue(QDLParserParser.StemValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#function}.
	 * @param ctx the parse tree
	 */
	void enterFunction(QDLParserParser.FunctionContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#function}.
	 * @param ctx the parse tree
	 */
	void exitFunction(QDLParserParser.FunctionContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#op_ref}.
	 * @param ctx the parse tree
	 */
	void enterOp_ref(QDLParserParser.Op_refContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#op_ref}.
	 * @param ctx the parse tree
	 */
	void exitOp_ref(QDLParserParser.Op_refContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#f_args}.
	 * @param ctx the parse tree
	 */
	void enterF_args(QDLParserParser.F_argsContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#f_args}.
	 * @param ctx the parse tree
	 */
	void exitF_args(QDLParserParser.F_argsContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#my_integer}.
	 * @param ctx the parse tree
	 */
	void enterMy_integer(QDLParserParser.My_integerContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#my_integer}.
	 * @param ctx the parse tree
	 */
	void exitMy_integer(QDLParserParser.My_integerContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functions}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFunctions(QDLParserParser.FunctionsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functions}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFunctions(QDLParserParser.FunctionsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryApplyExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryApplyExpression(QDLParserParser.UnaryApplyExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryApplyExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryApplyExpression(QDLParserParser.UnaryApplyExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code keywords}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterKeywords(QDLParserParser.KeywordsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code keywords}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitKeywords(QDLParserParser.KeywordsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code prefix}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPrefix(QDLParserParser.PrefixContext ctx);
	/**
	 * Exit a parse tree produced by the {@code prefix}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPrefix(QDLParserParser.PrefixContext ctx);
	/**
	 * Enter a parse tree produced by the {@code tildeExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTildeExpression(QDLParserParser.TildeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code tildeExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTildeExpression(QDLParserParser.TildeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code numbers}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNumbers(QDLParserParser.NumbersContext ctx);
	/**
	 * Exit a parse tree produced by the {@code numbers}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNumbers(QDLParserParser.NumbersContext ctx);
	/**
	 * Enter a parse tree produced by the {@code squartExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSquartExpression(QDLParserParser.SquartExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code squartExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSquartExpression(QDLParserParser.SquartExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNotExpression(QDLParserParser.NotExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNotExpression(QDLParserParser.NotExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code multiplyExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMultiplyExpression(QDLParserParser.MultiplyExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code multiplyExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMultiplyExpression(QDLParserParser.MultiplyExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code axis}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAxis(QDLParserParser.AxisContext ctx);
	/**
	 * Exit a parse tree produced by the {@code axis}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAxis(QDLParserParser.AxisContext ctx);
	/**
	 * Enter a parse tree produced by the {@code floorOrCeilingExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFloorOrCeilingExpression(QDLParserParser.FloorOrCeilingExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code floorOrCeilingExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFloorOrCeilingExpression(QDLParserParser.FloorOrCeilingExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code integers}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIntegers(QDLParserParser.IntegersContext ctx);
	/**
	 * Exit a parse tree produced by the {@code integers}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIntegers(QDLParserParser.IntegersContext ctx);
	/**
	 * Enter a parse tree produced by the {@code epsilon}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterEpsilon(QDLParserParser.EpsilonContext ctx);
	/**
	 * Exit a parse tree produced by the {@code epsilon}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitEpsilon(QDLParserParser.EpsilonContext ctx);
	/**
	 * Enter a parse tree produced by the {@code setThing}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSetThing(QDLParserParser.SetThingContext ctx);
	/**
	 * Exit a parse tree produced by the {@code setThing}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSetThing(QDLParserParser.SetThingContext ctx);
	/**
	 * Enter a parse tree produced by the {@code frefDyadicOps}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFrefDyadicOps(QDLParserParser.FrefDyadicOpsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code frefDyadicOps}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFrefDyadicOps(QDLParserParser.FrefDyadicOpsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code compExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterCompExpression(QDLParserParser.CompExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code compExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitCompExpression(QDLParserParser.CompExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code intersectionOrUnion}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIntersectionOrUnion(QDLParserParser.IntersectionOrUnionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code intersectionOrUnion}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIntersectionOrUnion(QDLParserParser.IntersectionOrUnionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code moduleExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterModuleExpression(QDLParserParser.ModuleExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code moduleExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitModuleExpression(QDLParserParser.ModuleExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code operatorReference}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterOperatorReference(QDLParserParser.OperatorReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code operatorReference}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitOperatorReference(QDLParserParser.OperatorReferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dyadicFunctionRefernce}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterDyadicFunctionRefernce(QDLParserParser.DyadicFunctionRefernceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dyadicFunctionRefernce}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitDyadicFunctionRefernce(QDLParserParser.DyadicFunctionRefernceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dotOp2}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterDotOp2(QDLParserParser.DotOp2Context ctx);
	/**
	 * Exit a parse tree produced by the {@code dotOp2}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitDotOp2(QDLParserParser.DotOp2Context ctx);
	/**
	 * Enter a parse tree produced by the {@code lambdaDef}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLambdaDef(QDLParserParser.LambdaDefContext ctx);
	/**
	 * Exit a parse tree produced by the {@code lambdaDef}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLambdaDef(QDLParserParser.LambdaDefContext ctx);
	/**
	 * Enter a parse tree produced by the {@code notTildeExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNotTildeExpression(QDLParserParser.NotTildeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code notTildeExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNotTildeExpression(QDLParserParser.NotTildeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code containsKey}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterContainsKey(QDLParserParser.ContainsKeyContext ctx);
	/**
	 * Exit a parse tree produced by the {@code containsKey}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitContainsKey(QDLParserParser.ContainsKeyContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stemDefaultValue}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterStemDefaultValue(QDLParserParser.StemDefaultValueContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stemDefaultValue}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitStemDefaultValue(QDLParserParser.StemDefaultValueContext ctx);
	/**
	 * Enter a parse tree produced by the {@code switchExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSwitchExpression(QDLParserParser.SwitchExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code switchExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSwitchExpression(QDLParserParser.SwitchExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code appliesOperator}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAppliesOperator(QDLParserParser.AppliesOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code appliesOperator}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAppliesOperator(QDLParserParser.AppliesOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code regexMatches}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterRegexMatches(QDLParserParser.RegexMatchesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code regexMatches}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitRegexMatches(QDLParserParser.RegexMatchesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code altIFExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAltIFExpression(QDLParserParser.AltIFExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code altIFExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAltIFExpression(QDLParserParser.AltIFExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code powerExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPowerExpression(QDLParserParser.PowerExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code powerExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPowerExpression(QDLParserParser.PowerExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code eqExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterEqExpression(QDLParserParser.EqExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code eqExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitEqExpression(QDLParserParser.EqExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code extract}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExtract(QDLParserParser.ExtractContext ctx);
	/**
	 * Exit a parse tree produced by the {@code extract}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExtract(QDLParserParser.ExtractContext ctx);
	/**
	 * Enter a parse tree produced by the {@code null}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNull(QDLParserParser.NullContext ctx);
	/**
	 * Exit a parse tree produced by the {@code null}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNull(QDLParserParser.NullContext ctx);
	/**
	 * Enter a parse tree produced by the {@code addExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAddExpression(QDLParserParser.AddExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code addExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAddExpression(QDLParserParser.AddExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code is_a}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIs_a(QDLParserParser.Is_aContext ctx);
	/**
	 * Exit a parse tree produced by the {@code is_a}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIs_a(QDLParserParser.Is_aContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stemVar}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterStemVar(QDLParserParser.StemVarContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stemVar}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitStemVar(QDLParserParser.StemVarContext ctx);
	/**
	 * Enter a parse tree produced by the {@code dotOp}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterDotOp(QDLParserParser.DotOpContext ctx);
	/**
	 * Exit a parse tree produced by the {@code dotOp}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitDotOp(QDLParserParser.DotOpContext ctx);
	/**
	 * Enter a parse tree produced by the {@code isDefinedDyadicExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIsDefinedDyadicExpression(QDLParserParser.IsDefinedDyadicExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code isDefinedDyadicExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIsDefinedDyadicExpression(QDLParserParser.IsDefinedDyadicExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code forAll}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterForAll(QDLParserParser.ForAllContext ctx);
	/**
	 * Exit a parse tree produced by the {@code forAll}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitForAll(QDLParserParser.ForAllContext ctx);
	/**
	 * Enter a parse tree produced by the {@code excise}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExcise(QDLParserParser.ExciseContext ctx);
	/**
	 * Exit a parse tree produced by the {@code excise}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExcise(QDLParserParser.ExciseContext ctx);
	/**
	 * Enter a parse tree produced by the {@code association}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAssociation(QDLParserParser.AssociationContext ctx);
	/**
	 * Exit a parse tree produced by the {@code association}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAssociation(QDLParserParser.AssociationContext ctx);
	/**
	 * Enter a parse tree produced by the {@code andExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAndExpression(QDLParserParser.AndExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code andExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAndExpression(QDLParserParser.AndExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code strings}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterStrings(QDLParserParser.StringsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code strings}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitStrings(QDLParserParser.StringsContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryTildeExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryTildeExpression(QDLParserParser.UnaryTildeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryTildeExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryTildeExpression(QDLParserParser.UnaryTildeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code postfix}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPostfix(QDLParserParser.PostfixContext ctx);
	/**
	 * Exit a parse tree produced by the {@code postfix}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPostfix(QDLParserParser.PostfixContext ctx);
	/**
	 * Enter a parse tree produced by the {@code realInterval}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterRealInterval(QDLParserParser.RealIntervalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code realInterval}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitRealInterval(QDLParserParser.RealIntervalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code transposeOperator}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterTransposeOperator(QDLParserParser.TransposeOperatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code transposeOperator}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitTransposeOperator(QDLParserParser.TransposeOperatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code variables}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterVariables(QDLParserParser.VariablesContext ctx);
	/**
	 * Exit a parse tree produced by the {@code variables}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitVariables(QDLParserParser.VariablesContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignment}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAssignment(QDLParserParser.AssignmentContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignment}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAssignment(QDLParserParser.AssignmentContext ctx);
	/**
	 * Enter a parse tree produced by the {@code stemLi}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterStemLi(QDLParserParser.StemLiContext ctx);
	/**
	 * Exit a parse tree produced by the {@code stemLi}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitStemLi(QDLParserParser.StemLiContext ctx);
	/**
	 * Enter a parse tree produced by the {@code intInterval}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIntInterval(QDLParserParser.IntIntervalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code intInterval}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIntInterval(QDLParserParser.IntIntervalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logical}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLogical(QDLParserParser.LogicalContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logical}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLogical(QDLParserParser.LogicalContext ctx);
	/**
	 * Enter a parse tree produced by the {@code orExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterOrExpression(QDLParserParser.OrExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code orExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitOrExpression(QDLParserParser.OrExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code toSet}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterToSet(QDLParserParser.ToSetContext ctx);
	/**
	 * Exit a parse tree produced by the {@code toSet}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitToSet(QDLParserParser.ToSetContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryMinusExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryMinusExpression(QDLParserParser.UnaryMinusExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryMinusExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryMinusExpression(QDLParserParser.UnaryMinusExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code functionReference}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFunctionReference(QDLParserParser.FunctionReferenceContext ctx);
	/**
	 * Exit a parse tree produced by the {@code functionReference}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFunctionReference(QDLParserParser.FunctionReferenceContext ctx);
	/**
	 * Enter a parse tree produced by the {@code unaryTransposeExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterUnaryTransposeExpression(QDLParserParser.UnaryTransposeExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code unaryTransposeExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitUnaryTransposeExpression(QDLParserParser.UnaryTransposeExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code isDefinedExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterIsDefinedExpression(QDLParserParser.IsDefinedExpressionContext ctx);
	/**
	 * Exit a parse tree produced by the {@code isDefinedExpression}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitIsDefinedExpression(QDLParserParser.IsDefinedExpressionContext ctx);
	/**
	 * Enter a parse tree produced by the {@code decimalNumber2}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterDecimalNumber2(QDLParserParser.DecimalNumber2Context ctx);
	/**
	 * Exit a parse tree produced by the {@code decimalNumber2}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitDecimalNumber2(QDLParserParser.DecimalNumber2Context ctx);
	/**
	 * Enter a parse tree produced by the {@code extract2}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExtract2(QDLParserParser.Extract2Context ctx);
	/**
	 * Exit a parse tree produced by the {@code extract2}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExtract2(QDLParserParser.Extract2Context ctx);
	/**
	 * Enter a parse tree produced by the {@code extract3}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExtract3(QDLParserParser.Extract3Context ctx);
	/**
	 * Exit a parse tree produced by the {@code extract3}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExtract3(QDLParserParser.Extract3Context ctx);
	/**
	 * Enter a parse tree produced by the {@code extract4}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExtract4(QDLParserParser.Extract4Context ctx);
	/**
	 * Exit a parse tree produced by the {@code extract4}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExtract4(QDLParserParser.Extract4Context ctx);
	/**
	 * Enter a parse tree produced by the {@code expressionDyadicOps}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterExpressionDyadicOps(QDLParserParser.ExpressionDyadicOpsContext ctx);
	/**
	 * Exit a parse tree produced by the {@code expressionDyadicOps}
	 * labeled alternative in {@link QDLParserParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitExpressionDyadicOps(QDLParserParser.ExpressionDyadicOpsContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#variable}.
	 * @param ctx the parse tree
	 */
	void enterVariable(QDLParserParser.VariableContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#variable}.
	 * @param ctx the parse tree
	 */
	void exitVariable(QDLParserParser.VariableContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#number}.
	 * @param ctx the parse tree
	 */
	void enterNumber(QDLParserParser.NumberContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#number}.
	 * @param ctx the parse tree
	 */
	void exitNumber(QDLParserParser.NumberContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#integer}.
	 * @param ctx the parse tree
	 */
	void enterInteger(QDLParserParser.IntegerContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#integer}.
	 * @param ctx the parse tree
	 */
	void exitInteger(QDLParserParser.IntegerContext ctx);
	/**
	 * Enter a parse tree produced by {@link QDLParserParser#keyword}.
	 * @param ctx the parse tree
	 */
	void enterKeyword(QDLParserParser.KeywordContext ctx);
	/**
	 * Exit a parse tree produced by {@link QDLParserParser#keyword}.
	 * @param ctx the parse tree
	 */
	void exitKeyword(QDLParserParser.KeywordContext ctx);
}