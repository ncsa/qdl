// Generated from QDLParser.g4 by ANTLR 4.9.1
package edu.uiuc.ncsa.qdl.generated;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class QDLParserParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, T__3=4, T__4=5, T__5=6, T__6=7, T__7=8, T__8=9, 
		T__9=10, ConstantKeywords=11, ASSERT=12, ASSERT2=13, BOOL_FALSE=14, BOOL_TRUE=15, 
		BLOCK=16, LOCAL=17, BODY=18, CATCH=19, DEFINE=20, DO=21, ELSE=22, IF=23, 
		MODULE=24, Null=25, Null_Set=26, SWITCH=27, THEN=28, TRY=29, WHILE=30, 
		Integer=31, Decimal=32, SCIENTIFIC_NUMBER=33, Bool=34, STRING=35, LeftBracket=36, 
		RightBracket=37, Comma=38, Colon=39, SemiColon=40, LDoubleBracket=41, 
		RDoubleBracket=42, LambdaConnector=43, Times=44, Divide=45, PlusPlus=46, 
		Plus=47, MinusMinus=48, Minus=49, LessThan=50, GreaterThan=51, SingleEqual=52, 
		DoubleQuote=53, SingleQuote=54, To_Set=55, LessEquals=56, MoreEquals=57, 
		IsA=58, Equals=59, NotEquals=60, RegexMatches=61, LogicalNot=62, Membership=63, 
		IsDefined=64, ForAll=65, ContainsKey=66, Exponentiation=67, Transpose=68, 
		Apply=69, ExprDyadicOps=70, FRefDyadicOps=71, And=72, Or=73, Backtick=74, 
		Percent=75, Tilde=76, Backslash=77, Backslash2=78, Backslash3=79, Backslash4=80, 
		Hash=81, Stile=82, TildeRight=83, StemDot=84, UnaryMinus=85, UnaryPlus=86, 
		Floor=87, Ceiling=88, FunctionMarker=89, AltIfMarker=90, SwitchMarker=91, 
		ASSIGN=92, Identifier=93, FuncStart=94, F_REF=95, FDOC=96, WS=97, COMMENT=98, 
		LINE_COMMENT=99;
	public static final int
		RULE_elements = 0, RULE_element = 1, RULE_statement = 2, RULE_conditionalStatement = 3, 
		RULE_ifStatement = 4, RULE_ifElseStatement = 5, RULE_loopStatement = 6, 
		RULE_switchStatement = 7, RULE_defineStatement = 8, RULE_lambdaStatement = 9, 
		RULE_moduleStatement = 10, RULE_tryCatchStatement = 11, RULE_assertStatement = 12, 
		RULE_blockStatement = 13, RULE_localStatement = 14, RULE_assertStatement2 = 15, 
		RULE_statementBlock = 16, RULE_docStatementBlock = 17, RULE_expressionBlock = 18, 
		RULE_conditionalBlock = 19, RULE_fdoc = 20, RULE_iInterval = 21, RULE_rInterval = 22, 
		RULE_set = 23, RULE_stemVariable = 24, RULE_stemEntry = 25, RULE_stemList = 26, 
		RULE_stemValue = 27, RULE_function = 28, RULE_f_arg = 29, RULE_f_args = 30, 
		RULE_f_ref = 31, RULE_expression = 32, RULE_variable = 33, RULE_number = 34, 
		RULE_integer = 35, RULE_keyword = 36;
	private static String[] makeRuleNames() {
		return new String[] {
			"elements", "element", "statement", "conditionalStatement", "ifStatement", 
			"ifElseStatement", "loopStatement", "switchStatement", "defineStatement", 
			"lambdaStatement", "moduleStatement", "tryCatchStatement", "assertStatement", 
			"blockStatement", "localStatement", "assertStatement2", "statementBlock", 
			"docStatementBlock", "expressionBlock", "conditionalBlock", "fdoc", "iInterval", 
			"rInterval", "set", "stemVariable", "stemEntry", "stemList", "stemValue", 
			"function", "f_arg", "f_args", "f_ref", "expression", "variable", "number", 
			"integer", "keyword"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "')'", "'\\/'", "'\u2229'", "'/\\'", "'\u222A'", 
			"'!'", "'\u00AC'", "'('", null, "'assert'", "'\u22A8'", "'false'", "'true'", 
			"'block'", "'local'", "'body'", "'catch'", "'define'", "'do'", "'else'", 
			"'if'", "'module'", "'null'", "'\u2205'", "'switch'", "'then'", "'try'", 
			"'while'", null, null, null, null, null, "'['", "']'", "','", "':'", 
			"';'", null, null, null, null, null, "'++'", "'+'", "'--'", "'-'", "'<'", 
			"'>'", "'='", "'\"'", "'''", null, null, null, "'<<'", null, null, null, 
			null, null, null, "'\u2200'", null, "'^'", "'\u29B0'", "'\u237A'", "'\u2306'", 
			null, null, null, "'`'", null, "'~'", null, null, null, null, "'#'", 
			"'|'", null, "'.'", "'\u00AF'", "'\u207A'", "'\u230A'", "'\u2308'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, null, null, null, null, null, null, null, "ConstantKeywords", 
			"ASSERT", "ASSERT2", "BOOL_FALSE", "BOOL_TRUE", "BLOCK", "LOCAL", "BODY", 
			"CATCH", "DEFINE", "DO", "ELSE", "IF", "MODULE", "Null", "Null_Set", 
			"SWITCH", "THEN", "TRY", "WHILE", "Integer", "Decimal", "SCIENTIFIC_NUMBER", 
			"Bool", "STRING", "LeftBracket", "RightBracket", "Comma", "Colon", "SemiColon", 
			"LDoubleBracket", "RDoubleBracket", "LambdaConnector", "Times", "Divide", 
			"PlusPlus", "Plus", "MinusMinus", "Minus", "LessThan", "GreaterThan", 
			"SingleEqual", "DoubleQuote", "SingleQuote", "To_Set", "LessEquals", 
			"MoreEquals", "IsA", "Equals", "NotEquals", "RegexMatches", "LogicalNot", 
			"Membership", "IsDefined", "ForAll", "ContainsKey", "Exponentiation", 
			"Transpose", "Apply", "ExprDyadicOps", "FRefDyadicOps", "And", "Or", 
			"Backtick", "Percent", "Tilde", "Backslash", "Backslash2", "Backslash3", 
			"Backslash4", "Hash", "Stile", "TildeRight", "StemDot", "UnaryMinus", 
			"UnaryPlus", "Floor", "Ceiling", "FunctionMarker", "AltIfMarker", "SwitchMarker", 
			"ASSIGN", "Identifier", "FuncStart", "F_REF", "FDOC", "WS", "COMMENT", 
			"LINE_COMMENT"
		};
	}
	private static final String[] _SYMBOLIC_NAMES = makeSymbolicNames();
	public static final Vocabulary VOCABULARY = new VocabularyImpl(_LITERAL_NAMES, _SYMBOLIC_NAMES);

	/**
	 * @deprecated Use {@link #VOCABULARY} instead.
	 */
	@Deprecated
	public static final String[] tokenNames;
	static {
		tokenNames = new String[_SYMBOLIC_NAMES.length];
		for (int i = 0; i < tokenNames.length; i++) {
			tokenNames[i] = VOCABULARY.getLiteralName(i);
			if (tokenNames[i] == null) {
				tokenNames[i] = VOCABULARY.getSymbolicName(i);
			}

			if (tokenNames[i] == null) {
				tokenNames[i] = "<INVALID>";
			}
		}
	}

	@Override
	@Deprecated
	public String[] getTokenNames() {
		return tokenNames;
	}

	@Override

	public Vocabulary getVocabulary() {
		return VOCABULARY;
	}

	@Override
	public String getGrammarFileName() { return "QDLParser.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public QDLParserParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class ElementsContext extends ParserRuleContext {
		public TerminalNode EOF() { return getToken(QDLParserParser.EOF, 0); }
		public List<ElementContext> element() {
			return getRuleContexts(ElementContext.class);
		}
		public ElementContext element(int i) {
			return getRuleContext(ElementContext.class,i);
		}
		public ElementsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_elements; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterElements(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitElements(this);
		}
	}

	public final ElementsContext elements() throws RecognitionException {
		ElementsContext _localctx = new ElementsContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_elements);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(77);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << ConstantKeywords) | (1L << ASSERT) | (1L << ASSERT2) | (1L << BLOCK) | (1L << LOCAL) | (1L << DEFINE) | (1L << IF) | (1L << MODULE) | (1L << Null) | (1L << SWITCH) | (1L << TRY) | (1L << WHILE) | (1L << Integer) | (1L << Decimal) | (1L << SCIENTIFIC_NUMBER) | (1L << Bool) | (1L << STRING) | (1L << LeftBracket) | (1L << LDoubleBracket) | (1L << PlusPlus) | (1L << Plus) | (1L << MinusMinus) | (1L << Minus) | (1L << To_Set))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (IsDefined - 64)) | (1L << (Transpose - 64)) | (1L << (Apply - 64)) | (1L << (Tilde - 64)) | (1L << (Hash - 64)) | (1L << (TildeRight - 64)) | (1L << (UnaryMinus - 64)) | (1L << (UnaryPlus - 64)) | (1L << (Floor - 64)) | (1L << (Ceiling - 64)) | (1L << (Identifier - 64)) | (1L << (FuncStart - 64)) | (1L << (F_REF - 64)))) != 0)) {
				{
				{
				setState(74);
				element();
				}
				}
				setState(79);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(80);
			match(EOF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ElementContext extends ParserRuleContext {
		public StatementContext statement() {
			return getRuleContext(StatementContext.class,0);
		}
		public TerminalNode SemiColon() { return getToken(QDLParserParser.SemiColon, 0); }
		public ElementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_element; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterElement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitElement(this);
		}
	}

	public final ElementContext element() throws RecognitionException {
		ElementContext _localctx = new ElementContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_element);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(82);
			statement();
			setState(83);
			match(SemiColon);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StatementContext extends ParserRuleContext {
		public DefineStatementContext defineStatement() {
			return getRuleContext(DefineStatementContext.class,0);
		}
		public ConditionalStatementContext conditionalStatement() {
			return getRuleContext(ConditionalStatementContext.class,0);
		}
		public LoopStatementContext loopStatement() {
			return getRuleContext(LoopStatementContext.class,0);
		}
		public SwitchStatementContext switchStatement() {
			return getRuleContext(SwitchStatementContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TryCatchStatementContext tryCatchStatement() {
			return getRuleContext(TryCatchStatementContext.class,0);
		}
		public LambdaStatementContext lambdaStatement() {
			return getRuleContext(LambdaStatementContext.class,0);
		}
		public AssertStatementContext assertStatement() {
			return getRuleContext(AssertStatementContext.class,0);
		}
		public AssertStatement2Context assertStatement2() {
			return getRuleContext(AssertStatement2Context.class,0);
		}
		public BlockStatementContext blockStatement() {
			return getRuleContext(BlockStatementContext.class,0);
		}
		public LocalStatementContext localStatement() {
			return getRuleContext(LocalStatementContext.class,0);
		}
		public ModuleStatementContext moduleStatement() {
			return getRuleContext(ModuleStatementContext.class,0);
		}
		public StatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitStatement(this);
		}
	}

	public final StatementContext statement() throws RecognitionException {
		StatementContext _localctx = new StatementContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_statement);
		try {
			setState(97);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,1,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(85);
				defineStatement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(86);
				conditionalStatement();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(87);
				loopStatement();
				}
				break;
			case 4:
				enterOuterAlt(_localctx, 4);
				{
				setState(88);
				switchStatement();
				}
				break;
			case 5:
				enterOuterAlt(_localctx, 5);
				{
				setState(89);
				expression(0);
				}
				break;
			case 6:
				enterOuterAlt(_localctx, 6);
				{
				setState(90);
				tryCatchStatement();
				}
				break;
			case 7:
				enterOuterAlt(_localctx, 7);
				{
				setState(91);
				lambdaStatement();
				}
				break;
			case 8:
				enterOuterAlt(_localctx, 8);
				{
				setState(92);
				assertStatement();
				}
				break;
			case 9:
				enterOuterAlt(_localctx, 9);
				{
				setState(93);
				assertStatement2();
				}
				break;
			case 10:
				enterOuterAlt(_localctx, 10);
				{
				setState(94);
				blockStatement();
				}
				break;
			case 11:
				enterOuterAlt(_localctx, 11);
				{
				setState(95);
				localStatement();
				}
				break;
			case 12:
				enterOuterAlt(_localctx, 12);
				{
				setState(96);
				moduleStatement();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConditionalStatementContext extends ParserRuleContext {
		public IfStatementContext ifStatement() {
			return getRuleContext(IfStatementContext.class,0);
		}
		public IfElseStatementContext ifElseStatement() {
			return getRuleContext(IfElseStatementContext.class,0);
		}
		public ConditionalStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditionalStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterConditionalStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitConditionalStatement(this);
		}
	}

	public final ConditionalStatementContext conditionalStatement() throws RecognitionException {
		ConditionalStatementContext _localctx = new ConditionalStatementContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_conditionalStatement);
		try {
			setState(101);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,2,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(99);
				ifStatement();
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(100);
				ifElseStatement();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IfStatementContext extends ParserRuleContext {
		public TerminalNode IF() { return getToken(QDLParserParser.IF, 0); }
		public ConditionalBlockContext conditionalBlock() {
			return getRuleContext(ConditionalBlockContext.class,0);
		}
		public StatementBlockContext statementBlock() {
			return getRuleContext(StatementBlockContext.class,0);
		}
		public TerminalNode THEN() { return getToken(QDLParserParser.THEN, 0); }
		public IfStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterIfStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitIfStatement(this);
		}
	}

	public final IfStatementContext ifStatement() throws RecognitionException {
		IfStatementContext _localctx = new IfStatementContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_ifStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(103);
			match(IF);
			setState(104);
			conditionalBlock();
			setState(106);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THEN) {
				{
				setState(105);
				match(THEN);
				}
			}

			setState(108);
			statementBlock();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IfElseStatementContext extends ParserRuleContext {
		public TerminalNode IF() { return getToken(QDLParserParser.IF, 0); }
		public ConditionalBlockContext conditionalBlock() {
			return getRuleContext(ConditionalBlockContext.class,0);
		}
		public List<StatementBlockContext> statementBlock() {
			return getRuleContexts(StatementBlockContext.class);
		}
		public StatementBlockContext statementBlock(int i) {
			return getRuleContext(StatementBlockContext.class,i);
		}
		public TerminalNode ELSE() { return getToken(QDLParserParser.ELSE, 0); }
		public TerminalNode THEN() { return getToken(QDLParserParser.THEN, 0); }
		public IfElseStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ifElseStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterIfElseStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitIfElseStatement(this);
		}
	}

	public final IfElseStatementContext ifElseStatement() throws RecognitionException {
		IfElseStatementContext _localctx = new IfElseStatementContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_ifElseStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(110);
			match(IF);
			setState(111);
			conditionalBlock();
			setState(113);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==THEN) {
				{
				setState(112);
				match(THEN);
				}
			}

			setState(115);
			statementBlock();
			setState(116);
			match(ELSE);
			setState(117);
			statementBlock();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LoopStatementContext extends ParserRuleContext {
		public TerminalNode WHILE() { return getToken(QDLParserParser.WHILE, 0); }
		public ConditionalBlockContext conditionalBlock() {
			return getRuleContext(ConditionalBlockContext.class,0);
		}
		public StatementBlockContext statementBlock() {
			return getRuleContext(StatementBlockContext.class,0);
		}
		public TerminalNode DO() { return getToken(QDLParserParser.DO, 0); }
		public LoopStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_loopStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterLoopStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitLoopStatement(this);
		}
	}

	public final LoopStatementContext loopStatement() throws RecognitionException {
		LoopStatementContext _localctx = new LoopStatementContext(_ctx, getState());
		enterRule(_localctx, 12, RULE_loopStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(119);
			match(WHILE);
			setState(120);
			conditionalBlock();
			setState(122);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==DO) {
				{
				setState(121);
				match(DO);
				}
			}

			setState(124);
			statementBlock();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SwitchStatementContext extends ParserRuleContext {
		public TerminalNode SWITCH() { return getToken(QDLParserParser.SWITCH, 0); }
		public TerminalNode LeftBracket() { return getToken(QDLParserParser.LeftBracket, 0); }
		public TerminalNode RightBracket() { return getToken(QDLParserParser.RightBracket, 0); }
		public List<IfStatementContext> ifStatement() {
			return getRuleContexts(IfStatementContext.class);
		}
		public IfStatementContext ifStatement(int i) {
			return getRuleContext(IfStatementContext.class,i);
		}
		public List<TerminalNode> SemiColon() { return getTokens(QDLParserParser.SemiColon); }
		public TerminalNode SemiColon(int i) {
			return getToken(QDLParserParser.SemiColon, i);
		}
		public SwitchStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_switchStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterSwitchStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitSwitchStatement(this);
		}
	}

	public final SwitchStatementContext switchStatement() throws RecognitionException {
		SwitchStatementContext _localctx = new SwitchStatementContext(_ctx, getState());
		enterRule(_localctx, 14, RULE_switchStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(126);
			match(SWITCH);
			setState(127);
			match(LeftBracket);
			setState(133);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==IF) {
				{
				{
				setState(128);
				ifStatement();
				setState(129);
				match(SemiColon);
				}
				}
				setState(135);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(136);
			match(RightBracket);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DefineStatementContext extends ParserRuleContext {
		public TerminalNode DEFINE() { return getToken(QDLParserParser.DEFINE, 0); }
		public TerminalNode LeftBracket() { return getToken(QDLParserParser.LeftBracket, 0); }
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public TerminalNode RightBracket() { return getToken(QDLParserParser.RightBracket, 0); }
		public DocStatementBlockContext docStatementBlock() {
			return getRuleContext(DocStatementBlockContext.class,0);
		}
		public TerminalNode BODY() { return getToken(QDLParserParser.BODY, 0); }
		public DefineStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_defineStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterDefineStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitDefineStatement(this);
		}
	}

	public final DefineStatementContext defineStatement() throws RecognitionException {
		DefineStatementContext _localctx = new DefineStatementContext(_ctx, getState());
		enterRule(_localctx, 16, RULE_defineStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(138);
			match(DEFINE);
			setState(139);
			match(LeftBracket);
			setState(140);
			function();
			setState(141);
			match(RightBracket);
			setState(143);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==BODY) {
				{
				setState(142);
				match(BODY);
				}
			}

			setState(145);
			docStatementBlock();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LambdaStatementContext extends ParserRuleContext {
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public TerminalNode LambdaConnector() { return getToken(QDLParserParser.LambdaConnector, 0); }
		public DocStatementBlockContext docStatementBlock() {
			return getRuleContext(DocStatementBlockContext.class,0);
		}
		public TerminalNode BLOCK() { return getToken(QDLParserParser.BLOCK, 0); }
		public TerminalNode LOCAL() { return getToken(QDLParserParser.LOCAL, 0); }
		public LambdaStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_lambdaStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterLambdaStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitLambdaStatement(this);
		}
	}

	public final LambdaStatementContext lambdaStatement() throws RecognitionException {
		LambdaStatementContext _localctx = new LambdaStatementContext(_ctx, getState());
		enterRule(_localctx, 18, RULE_lambdaStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(147);
			function();
			setState(148);
			match(LambdaConnector);
			setState(150);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==BLOCK || _la==LOCAL) {
				{
				setState(149);
				_la = _input.LA(1);
				if ( !(_la==BLOCK || _la==LOCAL) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				}
			}

			setState(152);
			docStatementBlock();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ModuleStatementContext extends ParserRuleContext {
		public TerminalNode MODULE() { return getToken(QDLParserParser.MODULE, 0); }
		public TerminalNode LeftBracket() { return getToken(QDLParserParser.LeftBracket, 0); }
		public List<TerminalNode> STRING() { return getTokens(QDLParserParser.STRING); }
		public TerminalNode STRING(int i) {
			return getToken(QDLParserParser.STRING, i);
		}
		public TerminalNode RightBracket() { return getToken(QDLParserParser.RightBracket, 0); }
		public DocStatementBlockContext docStatementBlock() {
			return getRuleContext(DocStatementBlockContext.class,0);
		}
		public TerminalNode Comma() { return getToken(QDLParserParser.Comma, 0); }
		public TerminalNode BODY() { return getToken(QDLParserParser.BODY, 0); }
		public ModuleStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_moduleStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterModuleStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitModuleStatement(this);
		}
	}

	public final ModuleStatementContext moduleStatement() throws RecognitionException {
		ModuleStatementContext _localctx = new ModuleStatementContext(_ctx, getState());
		enterRule(_localctx, 20, RULE_moduleStatement);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(154);
			match(MODULE);
			setState(155);
			match(LeftBracket);
			setState(156);
			match(STRING);
			setState(159);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Comma) {
				{
				setState(157);
				match(Comma);
				setState(158);
				match(STRING);
				}
			}

			setState(161);
			match(RightBracket);
			setState(163);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==BODY) {
				{
				setState(162);
				match(BODY);
				}
			}

			setState(165);
			docStatementBlock();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class TryCatchStatementContext extends ParserRuleContext {
		public TerminalNode TRY() { return getToken(QDLParserParser.TRY, 0); }
		public List<StatementBlockContext> statementBlock() {
			return getRuleContexts(StatementBlockContext.class);
		}
		public StatementBlockContext statementBlock(int i) {
			return getRuleContext(StatementBlockContext.class,i);
		}
		public TerminalNode CATCH() { return getToken(QDLParserParser.CATCH, 0); }
		public TryCatchStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_tryCatchStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterTryCatchStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitTryCatchStatement(this);
		}
	}

	public final TryCatchStatementContext tryCatchStatement() throws RecognitionException {
		TryCatchStatementContext _localctx = new TryCatchStatementContext(_ctx, getState());
		enterRule(_localctx, 22, RULE_tryCatchStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(167);
			match(TRY);
			setState(168);
			statementBlock();
			setState(169);
			match(CATCH);
			setState(170);
			statementBlock();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AssertStatementContext extends ParserRuleContext {
		public TerminalNode ASSERT() { return getToken(QDLParserParser.ASSERT, 0); }
		public List<TerminalNode> LeftBracket() { return getTokens(QDLParserParser.LeftBracket); }
		public TerminalNode LeftBracket(int i) {
			return getToken(QDLParserParser.LeftBracket, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> RightBracket() { return getTokens(QDLParserParser.RightBracket); }
		public TerminalNode RightBracket(int i) {
			return getToken(QDLParserParser.RightBracket, i);
		}
		public AssertStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assertStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterAssertStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitAssertStatement(this);
		}
	}

	public final AssertStatementContext assertStatement() throws RecognitionException {
		AssertStatementContext _localctx = new AssertStatementContext(_ctx, getState());
		enterRule(_localctx, 24, RULE_assertStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(172);
			match(ASSERT);
			setState(173);
			match(LeftBracket);
			setState(174);
			expression(0);
			setState(175);
			match(RightBracket);
			setState(176);
			match(LeftBracket);
			setState(177);
			expression(0);
			setState(178);
			match(RightBracket);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class BlockStatementContext extends ParserRuleContext {
		public TerminalNode BLOCK() { return getToken(QDLParserParser.BLOCK, 0); }
		public StatementBlockContext statementBlock() {
			return getRuleContext(StatementBlockContext.class,0);
		}
		public BlockStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_blockStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterBlockStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitBlockStatement(this);
		}
	}

	public final BlockStatementContext blockStatement() throws RecognitionException {
		BlockStatementContext _localctx = new BlockStatementContext(_ctx, getState());
		enterRule(_localctx, 26, RULE_blockStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(180);
			match(BLOCK);
			setState(181);
			statementBlock();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class LocalStatementContext extends ParserRuleContext {
		public TerminalNode LOCAL() { return getToken(QDLParserParser.LOCAL, 0); }
		public StatementBlockContext statementBlock() {
			return getRuleContext(StatementBlockContext.class,0);
		}
		public LocalStatementContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_localStatement; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterLocalStatement(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitLocalStatement(this);
		}
	}

	public final LocalStatementContext localStatement() throws RecognitionException {
		LocalStatementContext _localctx = new LocalStatementContext(_ctx, getState());
		enterRule(_localctx, 28, RULE_localStatement);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(183);
			match(LOCAL);
			setState(184);
			statementBlock();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class AssertStatement2Context extends ParserRuleContext {
		public TerminalNode ASSERT2() { return getToken(QDLParserParser.ASSERT2, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Colon() { return getToken(QDLParserParser.Colon, 0); }
		public AssertStatement2Context(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_assertStatement2; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterAssertStatement2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitAssertStatement2(this);
		}
	}

	public final AssertStatement2Context assertStatement2() throws RecognitionException {
		AssertStatement2Context _localctx = new AssertStatement2Context(_ctx, getState());
		enterRule(_localctx, 30, RULE_assertStatement2);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(186);
			match(ASSERT2);
			setState(187);
			expression(0);
			setState(190);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if (_la==Colon) {
				{
				setState(188);
				match(Colon);
				setState(189);
				expression(0);
				}
			}

			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StatementBlockContext extends ParserRuleContext {
		public TerminalNode LeftBracket() { return getToken(QDLParserParser.LeftBracket, 0); }
		public TerminalNode RightBracket() { return getToken(QDLParserParser.RightBracket, 0); }
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public List<TerminalNode> SemiColon() { return getTokens(QDLParserParser.SemiColon); }
		public TerminalNode SemiColon(int i) {
			return getToken(QDLParserParser.SemiColon, i);
		}
		public StatementBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_statementBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterStatementBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitStatementBlock(this);
		}
	}

	public final StatementBlockContext statementBlock() throws RecognitionException {
		StatementBlockContext _localctx = new StatementBlockContext(_ctx, getState());
		enterRule(_localctx, 32, RULE_statementBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(192);
			match(LeftBracket);
			setState(198);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << ConstantKeywords) | (1L << ASSERT) | (1L << ASSERT2) | (1L << BLOCK) | (1L << LOCAL) | (1L << DEFINE) | (1L << IF) | (1L << MODULE) | (1L << Null) | (1L << SWITCH) | (1L << TRY) | (1L << WHILE) | (1L << Integer) | (1L << Decimal) | (1L << SCIENTIFIC_NUMBER) | (1L << Bool) | (1L << STRING) | (1L << LeftBracket) | (1L << LDoubleBracket) | (1L << PlusPlus) | (1L << Plus) | (1L << MinusMinus) | (1L << Minus) | (1L << To_Set))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (IsDefined - 64)) | (1L << (Transpose - 64)) | (1L << (Apply - 64)) | (1L << (Tilde - 64)) | (1L << (Hash - 64)) | (1L << (TildeRight - 64)) | (1L << (UnaryMinus - 64)) | (1L << (UnaryPlus - 64)) | (1L << (Floor - 64)) | (1L << (Ceiling - 64)) | (1L << (Identifier - 64)) | (1L << (FuncStart - 64)) | (1L << (F_REF - 64)))) != 0)) {
				{
				{
				setState(193);
				statement();
				setState(194);
				match(SemiColon);
				}
				}
				setState(200);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(201);
			match(RightBracket);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class DocStatementBlockContext extends ParserRuleContext {
		public TerminalNode LeftBracket() { return getToken(QDLParserParser.LeftBracket, 0); }
		public TerminalNode RightBracket() { return getToken(QDLParserParser.RightBracket, 0); }
		public List<FdocContext> fdoc() {
			return getRuleContexts(FdocContext.class);
		}
		public FdocContext fdoc(int i) {
			return getRuleContext(FdocContext.class,i);
		}
		public List<StatementContext> statement() {
			return getRuleContexts(StatementContext.class);
		}
		public StatementContext statement(int i) {
			return getRuleContext(StatementContext.class,i);
		}
		public List<TerminalNode> SemiColon() { return getTokens(QDLParserParser.SemiColon); }
		public TerminalNode SemiColon(int i) {
			return getToken(QDLParserParser.SemiColon, i);
		}
		public DocStatementBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_docStatementBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterDocStatementBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitDocStatementBlock(this);
		}
	}

	public final DocStatementBlockContext docStatementBlock() throws RecognitionException {
		DocStatementBlockContext _localctx = new DocStatementBlockContext(_ctx, getState());
		enterRule(_localctx, 34, RULE_docStatementBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(203);
			match(LeftBracket);
			setState(207);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==FDOC) {
				{
				{
				setState(204);
				fdoc();
				}
				}
				setState(209);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(213); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(210);
				statement();
				setState(211);
				match(SemiColon);
				}
				}
				setState(215); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << ConstantKeywords) | (1L << ASSERT) | (1L << ASSERT2) | (1L << BLOCK) | (1L << LOCAL) | (1L << DEFINE) | (1L << IF) | (1L << MODULE) | (1L << Null) | (1L << SWITCH) | (1L << TRY) | (1L << WHILE) | (1L << Integer) | (1L << Decimal) | (1L << SCIENTIFIC_NUMBER) | (1L << Bool) | (1L << STRING) | (1L << LeftBracket) | (1L << LDoubleBracket) | (1L << PlusPlus) | (1L << Plus) | (1L << MinusMinus) | (1L << Minus) | (1L << To_Set))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (IsDefined - 64)) | (1L << (Transpose - 64)) | (1L << (Apply - 64)) | (1L << (Tilde - 64)) | (1L << (Hash - 64)) | (1L << (TildeRight - 64)) | (1L << (UnaryMinus - 64)) | (1L << (UnaryPlus - 64)) | (1L << (Floor - 64)) | (1L << (Ceiling - 64)) | (1L << (Identifier - 64)) | (1L << (FuncStart - 64)) | (1L << (F_REF - 64)))) != 0) );
			setState(217);
			match(RightBracket);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionBlockContext extends ParserRuleContext {
		public TerminalNode LeftBracket() { return getToken(QDLParserParser.LeftBracket, 0); }
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> SemiColon() { return getTokens(QDLParserParser.SemiColon); }
		public TerminalNode SemiColon(int i) {
			return getToken(QDLParserParser.SemiColon, i);
		}
		public TerminalNode RightBracket() { return getToken(QDLParserParser.RightBracket, 0); }
		public ExpressionBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expressionBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterExpressionBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitExpressionBlock(this);
		}
	}

	public final ExpressionBlockContext expressionBlock() throws RecognitionException {
		ExpressionBlockContext _localctx = new ExpressionBlockContext(_ctx, getState());
		enterRule(_localctx, 36, RULE_expressionBlock);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(219);
			match(LeftBracket);
			setState(220);
			expression(0);
			setState(221);
			match(SemiColon);
			setState(225); 
			_errHandler.sync(this);
			_la = _input.LA(1);
			do {
				{
				{
				setState(222);
				expression(0);
				setState(223);
				match(SemiColon);
				}
				}
				setState(227); 
				_errHandler.sync(this);
				_la = _input.LA(1);
			} while ( (((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << ConstantKeywords) | (1L << Null) | (1L << Integer) | (1L << Decimal) | (1L << SCIENTIFIC_NUMBER) | (1L << Bool) | (1L << STRING) | (1L << LeftBracket) | (1L << LDoubleBracket) | (1L << PlusPlus) | (1L << Plus) | (1L << MinusMinus) | (1L << Minus) | (1L << To_Set))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (IsDefined - 64)) | (1L << (Transpose - 64)) | (1L << (Apply - 64)) | (1L << (Tilde - 64)) | (1L << (Hash - 64)) | (1L << (TildeRight - 64)) | (1L << (UnaryMinus - 64)) | (1L << (UnaryPlus - 64)) | (1L << (Floor - 64)) | (1L << (Ceiling - 64)) | (1L << (Identifier - 64)) | (1L << (FuncStart - 64)) | (1L << (F_REF - 64)))) != 0) );
			setState(229);
			match(RightBracket);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ConditionalBlockContext extends ParserRuleContext {
		public TerminalNode LeftBracket() { return getToken(QDLParserParser.LeftBracket, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode RightBracket() { return getToken(QDLParserParser.RightBracket, 0); }
		public ConditionalBlockContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_conditionalBlock; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterConditionalBlock(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitConditionalBlock(this);
		}
	}

	public final ConditionalBlockContext conditionalBlock() throws RecognitionException {
		ConditionalBlockContext _localctx = new ConditionalBlockContext(_ctx, getState());
		enterRule(_localctx, 38, RULE_conditionalBlock);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(231);
			match(LeftBracket);
			setState(232);
			expression(0);
			setState(233);
			match(RightBracket);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FdocContext extends ParserRuleContext {
		public TerminalNode FDOC() { return getToken(QDLParserParser.FDOC, 0); }
		public FdocContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_fdoc; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterFdoc(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitFdoc(this);
		}
	}

	public final FdocContext fdoc() throws RecognitionException {
		FdocContext _localctx = new FdocContext(_ctx, getState());
		enterRule(_localctx, 40, RULE_fdoc);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(235);
			match(FDOC);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IIntervalContext extends ParserRuleContext {
		public TerminalNode LeftBracket() { return getToken(QDLParserParser.LeftBracket, 0); }
		public List<TerminalNode> SemiColon() { return getTokens(QDLParserParser.SemiColon); }
		public TerminalNode SemiColon(int i) {
			return getToken(QDLParserParser.SemiColon, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode RightBracket() { return getToken(QDLParserParser.RightBracket, 0); }
		public IIntervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_iInterval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterIInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitIInterval(this);
		}
	}

	public final IIntervalContext iInterval() throws RecognitionException {
		IIntervalContext _localctx = new IIntervalContext(_ctx, getState());
		enterRule(_localctx, 42, RULE_iInterval);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(237);
			match(LeftBracket);
			setState(239);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << ConstantKeywords) | (1L << Null) | (1L << Integer) | (1L << Decimal) | (1L << SCIENTIFIC_NUMBER) | (1L << Bool) | (1L << STRING) | (1L << LeftBracket) | (1L << LDoubleBracket) | (1L << PlusPlus) | (1L << Plus) | (1L << MinusMinus) | (1L << Minus) | (1L << To_Set))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (IsDefined - 64)) | (1L << (Transpose - 64)) | (1L << (Apply - 64)) | (1L << (Tilde - 64)) | (1L << (Hash - 64)) | (1L << (TildeRight - 64)) | (1L << (UnaryMinus - 64)) | (1L << (UnaryPlus - 64)) | (1L << (Floor - 64)) | (1L << (Ceiling - 64)) | (1L << (Identifier - 64)) | (1L << (FuncStart - 64)) | (1L << (F_REF - 64)))) != 0)) {
				{
				setState(238);
				expression(0);
				}
			}

			setState(241);
			match(SemiColon);
			setState(242);
			expression(0);
			setState(246);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,17,_ctx) ) {
			case 1:
				{
				setState(243);
				match(SemiColon);
				}
				break;
			case 2:
				{
				{
				setState(244);
				match(SemiColon);
				setState(245);
				expression(0);
				}
				}
				break;
			}
			setState(248);
			match(RightBracket);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class RIntervalContext extends ParserRuleContext {
		public TerminalNode LDoubleBracket() { return getToken(QDLParserParser.LDoubleBracket, 0); }
		public List<TerminalNode> SemiColon() { return getTokens(QDLParserParser.SemiColon); }
		public TerminalNode SemiColon(int i) {
			return getToken(QDLParserParser.SemiColon, i);
		}
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode RDoubleBracket() { return getToken(QDLParserParser.RDoubleBracket, 0); }
		public RIntervalContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_rInterval; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterRInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitRInterval(this);
		}
	}

	public final RIntervalContext rInterval() throws RecognitionException {
		RIntervalContext _localctx = new RIntervalContext(_ctx, getState());
		enterRule(_localctx, 44, RULE_rInterval);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(250);
			match(LDoubleBracket);
			setState(252);
			_errHandler.sync(this);
			_la = _input.LA(1);
			if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << ConstantKeywords) | (1L << Null) | (1L << Integer) | (1L << Decimal) | (1L << SCIENTIFIC_NUMBER) | (1L << Bool) | (1L << STRING) | (1L << LeftBracket) | (1L << LDoubleBracket) | (1L << PlusPlus) | (1L << Plus) | (1L << MinusMinus) | (1L << Minus) | (1L << To_Set))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (IsDefined - 64)) | (1L << (Transpose - 64)) | (1L << (Apply - 64)) | (1L << (Tilde - 64)) | (1L << (Hash - 64)) | (1L << (TildeRight - 64)) | (1L << (UnaryMinus - 64)) | (1L << (UnaryPlus - 64)) | (1L << (Floor - 64)) | (1L << (Ceiling - 64)) | (1L << (Identifier - 64)) | (1L << (FuncStart - 64)) | (1L << (F_REF - 64)))) != 0)) {
				{
				setState(251);
				expression(0);
				}
			}

			setState(254);
			match(SemiColon);
			setState(255);
			expression(0);
			setState(259);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,19,_ctx) ) {
			case 1:
				{
				setState(256);
				match(SemiColon);
				}
				break;
			case 2:
				{
				{
				setState(257);
				match(SemiColon);
				setState(258);
				expression(0);
				}
				}
				break;
			}
			setState(261);
			match(RDoubleBracket);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class SetContext extends ParserRuleContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(QDLParserParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(QDLParserParser.Comma, i);
		}
		public SetContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_set; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitSet(this);
		}
	}

	public final SetContext set() throws RecognitionException {
		SetContext _localctx = new SetContext(_ctx, getState());
		enterRule(_localctx, 46, RULE_set);
		int _la;
		try {
			setState(276);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,21,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(263);
				match(T__0);
				setState(264);
				expression(0);
				setState(269);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==Comma) {
					{
					{
					setState(265);
					match(Comma);
					setState(266);
					expression(0);
					}
					}
					setState(271);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(272);
				match(T__1);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(274);
				match(T__0);
				setState(275);
				match(T__1);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StemVariableContext extends ParserRuleContext {
		public List<StemEntryContext> stemEntry() {
			return getRuleContexts(StemEntryContext.class);
		}
		public StemEntryContext stemEntry(int i) {
			return getRuleContext(StemEntryContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(QDLParserParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(QDLParserParser.Comma, i);
		}
		public StemVariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stemVariable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterStemVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitStemVariable(this);
		}
	}

	public final StemVariableContext stemVariable() throws RecognitionException {
		StemVariableContext _localctx = new StemVariableContext(_ctx, getState());
		enterRule(_localctx, 48, RULE_stemVariable);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(278);
			match(T__0);
			setState(279);
			stemEntry();
			setState(284);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(280);
				match(Comma);
				setState(281);
				stemEntry();
				}
				}
				setState(286);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(287);
			match(T__1);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StemEntryContext extends ParserRuleContext {
		public TerminalNode Colon() { return getToken(QDLParserParser.Colon, 0); }
		public StemValueContext stemValue() {
			return getRuleContext(StemValueContext.class,0);
		}
		public TerminalNode Times() { return getToken(QDLParserParser.Times, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public StemEntryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stemEntry; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterStemEntry(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitStemEntry(this);
		}
	}

	public final StemEntryContext stemEntry() throws RecognitionException {
		StemEntryContext _localctx = new StemEntryContext(_ctx, getState());
		enterRule(_localctx, 50, RULE_stemEntry);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(291);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Times:
				{
				setState(289);
				match(Times);
				}
				break;
			case T__0:
			case T__7:
			case T__8:
			case T__9:
			case ConstantKeywords:
			case Null:
			case Integer:
			case Decimal:
			case SCIENTIFIC_NUMBER:
			case Bool:
			case STRING:
			case LeftBracket:
			case LDoubleBracket:
			case PlusPlus:
			case Plus:
			case MinusMinus:
			case Minus:
			case To_Set:
			case IsDefined:
			case Transpose:
			case Apply:
			case Tilde:
			case Hash:
			case TildeRight:
			case UnaryMinus:
			case UnaryPlus:
			case Floor:
			case Ceiling:
			case Identifier:
			case FuncStart:
			case F_REF:
				{
				setState(290);
				expression(0);
				}
				break;
			default:
				throw new NoViableAltException(this);
			}
			setState(293);
			match(Colon);
			setState(294);
			stemValue();
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StemListContext extends ParserRuleContext {
		public TerminalNode LeftBracket() { return getToken(QDLParserParser.LeftBracket, 0); }
		public List<StemValueContext> stemValue() {
			return getRuleContexts(StemValueContext.class);
		}
		public StemValueContext stemValue(int i) {
			return getRuleContext(StemValueContext.class,i);
		}
		public TerminalNode RightBracket() { return getToken(QDLParserParser.RightBracket, 0); }
		public List<TerminalNode> Comma() { return getTokens(QDLParserParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(QDLParserParser.Comma, i);
		}
		public StemListContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stemList; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterStemList(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitStemList(this);
		}
	}

	public final StemListContext stemList() throws RecognitionException {
		StemListContext _localctx = new StemListContext(_ctx, getState());
		enterRule(_localctx, 52, RULE_stemList);
		int _la;
		try {
			setState(309);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,25,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(296);
				match(LeftBracket);
				setState(297);
				stemValue();
				setState(302);
				_errHandler.sync(this);
				_la = _input.LA(1);
				while (_la==Comma) {
					{
					{
					setState(298);
					match(Comma);
					setState(299);
					stemValue();
					}
					}
					setState(304);
					_errHandler.sync(this);
					_la = _input.LA(1);
				}
				setState(305);
				match(RightBracket);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(307);
				match(LeftBracket);
				setState(308);
				match(RightBracket);
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class StemValueContext extends ParserRuleContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public StemVariableContext stemVariable() {
			return getRuleContext(StemVariableContext.class,0);
		}
		public StemListContext stemList() {
			return getRuleContext(StemListContext.class,0);
		}
		public StemValueContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_stemValue; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterStemValue(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitStemValue(this);
		}
	}

	public final StemValueContext stemValue() throws RecognitionException {
		StemValueContext _localctx = new StemValueContext(_ctx, getState());
		enterRule(_localctx, 54, RULE_stemValue);
		try {
			setState(314);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,26,_ctx) ) {
			case 1:
				enterOuterAlt(_localctx, 1);
				{
				setState(311);
				expression(0);
				}
				break;
			case 2:
				enterOuterAlt(_localctx, 2);
				{
				setState(312);
				stemVariable();
				}
				break;
			case 3:
				enterOuterAlt(_localctx, 3);
				{
				setState(313);
				stemList();
				}
				break;
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class FunctionContext extends ParserRuleContext {
		public TerminalNode FuncStart() { return getToken(QDLParserParser.FuncStart, 0); }
		public List<F_argsContext> f_args() {
			return getRuleContexts(F_argsContext.class);
		}
		public F_argsContext f_args(int i) {
			return getRuleContext(F_argsContext.class,i);
		}
		public FunctionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_function; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterFunction(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitFunction(this);
		}
	}

	public final FunctionContext function() throws RecognitionException {
		FunctionContext _localctx = new FunctionContext(_ctx, getState());
		enterRule(_localctx, 56, RULE_function);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(316);
			match(FuncStart);
			setState(320);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << ConstantKeywords) | (1L << Null) | (1L << Integer) | (1L << Decimal) | (1L << SCIENTIFIC_NUMBER) | (1L << Bool) | (1L << STRING) | (1L << LeftBracket) | (1L << LDoubleBracket) | (1L << PlusPlus) | (1L << Plus) | (1L << MinusMinus) | (1L << Minus) | (1L << To_Set))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (IsDefined - 64)) | (1L << (Transpose - 64)) | (1L << (Apply - 64)) | (1L << (Tilde - 64)) | (1L << (Hash - 64)) | (1L << (TildeRight - 64)) | (1L << (UnaryMinus - 64)) | (1L << (UnaryPlus - 64)) | (1L << (Floor - 64)) | (1L << (Ceiling - 64)) | (1L << (Identifier - 64)) | (1L << (FuncStart - 64)) | (1L << (F_REF - 64)))) != 0)) {
				{
				{
				setState(317);
				f_args();
				}
				}
				setState(322);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(323);
			match(T__2);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class F_argContext extends ParserRuleContext {
		public StemValueContext stemValue() {
			return getRuleContext(StemValueContext.class,0);
		}
		public F_refContext f_ref() {
			return getRuleContext(F_refContext.class,0);
		}
		public F_argContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_f_arg; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterF_arg(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitF_arg(this);
		}
	}

	public final F_argContext f_arg() throws RecognitionException {
		F_argContext _localctx = new F_argContext(_ctx, getState());
		enterRule(_localctx, 58, RULE_f_arg);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(327);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,28,_ctx) ) {
			case 1:
				{
				setState(325);
				stemValue();
				}
				break;
			case 2:
				{
				setState(326);
				f_ref();
				}
				break;
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class F_argsContext extends ParserRuleContext {
		public List<F_argContext> f_arg() {
			return getRuleContexts(F_argContext.class);
		}
		public F_argContext f_arg(int i) {
			return getRuleContext(F_argContext.class,i);
		}
		public List<TerminalNode> Comma() { return getTokens(QDLParserParser.Comma); }
		public TerminalNode Comma(int i) {
			return getToken(QDLParserParser.Comma, i);
		}
		public F_argsContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_f_args; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterF_args(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitF_args(this);
		}
	}

	public final F_argsContext f_args() throws RecognitionException {
		F_argsContext _localctx = new F_argsContext(_ctx, getState());
		enterRule(_localctx, 60, RULE_f_args);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(329);
			f_arg();
			setState(334);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Comma) {
				{
				{
				setState(330);
				match(Comma);
				setState(331);
				f_arg();
				}
				}
				setState(336);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class F_refContext extends ParserRuleContext {
		public TerminalNode F_REF() { return getToken(QDLParserParser.F_REF, 0); }
		public F_refContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_f_ref; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterF_ref(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitF_ref(this);
		}
	}

	public final F_refContext f_ref() throws RecognitionException {
		F_refContext _localctx = new F_refContext(_ctx, getState());
		enterRule(_localctx, 62, RULE_f_ref);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(337);
			match(F_REF);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class ExpressionContext extends ParserRuleContext {
		public ExpressionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_expression; }
	 
		public ExpressionContext() { }
		public void copyFrom(ExpressionContext ctx) {
			super.copyFrom(ctx);
		}
	}
	public static class FunctionsContext extends ExpressionContext {
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public FunctionsContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterFunctions(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitFunctions(this);
		}
	}
	public static class UnaryApplyExpressionContext extends ExpressionContext {
		public TerminalNode Apply() { return getToken(QDLParserParser.Apply, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public UnaryApplyExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterUnaryApplyExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitUnaryApplyExpression(this);
		}
	}
	public static class KeywordsContext extends ExpressionContext {
		public KeywordContext keyword() {
			return getRuleContext(KeywordContext.class,0);
		}
		public KeywordsContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterKeywords(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitKeywords(this);
		}
	}
	public static class PrefixContext extends ExpressionContext {
		public Token prefix;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode PlusPlus() { return getToken(QDLParserParser.PlusPlus, 0); }
		public TerminalNode MinusMinus() { return getToken(QDLParserParser.MinusMinus, 0); }
		public PrefixContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterPrefix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitPrefix(this);
		}
	}
	public static class TildeExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Tilde() { return getToken(QDLParserParser.Tilde, 0); }
		public TerminalNode TildeRight() { return getToken(QDLParserParser.TildeRight, 0); }
		public TildeExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterTildeExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitTildeExpression(this);
		}
	}
	public static class NumbersContext extends ExpressionContext {
		public NumberContext number() {
			return getRuleContext(NumberContext.class,0);
		}
		public NumbersContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterNumbers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitNumbers(this);
		}
	}
	public static class NotExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public NotExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterNotExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitNotExpression(this);
		}
	}
	public static class MultiplyExpressionContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Times() { return getToken(QDLParserParser.Times, 0); }
		public TerminalNode Divide() { return getToken(QDLParserParser.Divide, 0); }
		public TerminalNode Percent() { return getToken(QDLParserParser.Percent, 0); }
		public MultiplyExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterMultiplyExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitMultiplyExpression(this);
		}
	}
	public static class FloorOrCeilingExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode Floor() { return getToken(QDLParserParser.Floor, 0); }
		public TerminalNode Ceiling() { return getToken(QDLParserParser.Ceiling, 0); }
		public FloorOrCeilingExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterFloorOrCeilingExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitFloorOrCeilingExpression(this);
		}
	}
	public static class IntegersContext extends ExpressionContext {
		public IntegerContext integer() {
			return getRuleContext(IntegerContext.class,0);
		}
		public IntegersContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterIntegers(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitIntegers(this);
		}
	}
	public static class EpsilonContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Membership() { return getToken(QDLParserParser.Membership, 0); }
		public EpsilonContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterEpsilon(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitEpsilon(this);
		}
	}
	public static class SetThingContext extends ExpressionContext {
		public SetContext set() {
			return getRuleContext(SetContext.class,0);
		}
		public SetThingContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterSetThing(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitSetThing(this);
		}
	}
	public static class FrefDyadicOpsContext extends ExpressionContext {
		public Token op;
		public F_refContext f_ref() {
			return getRuleContext(F_refContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode FRefDyadicOps() { return getToken(QDLParserParser.FRefDyadicOps, 0); }
		public FrefDyadicOpsContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterFrefDyadicOps(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitFrefDyadicOps(this);
		}
	}
	public static class CompExpressionContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode LessThan() { return getToken(QDLParserParser.LessThan, 0); }
		public TerminalNode GreaterThan() { return getToken(QDLParserParser.GreaterThan, 0); }
		public TerminalNode LessEquals() { return getToken(QDLParserParser.LessEquals, 0); }
		public TerminalNode MoreEquals() { return getToken(QDLParserParser.MoreEquals, 0); }
		public CompExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterCompExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitCompExpression(this);
		}
	}
	public static class IntersectionOrUnionContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public IntersectionOrUnionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterIntersectionOrUnion(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitIntersectionOrUnion(this);
		}
	}
	public static class ModuleExpressionContext extends ExpressionContext {
		public TerminalNode Hash() { return getToken(QDLParserParser.Hash, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public ModuleExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterModuleExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitModuleExpression(this);
		}
	}
	public static class DotOp2Context extends ExpressionContext {
		public Token postfix;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode StemDot() { return getToken(QDLParserParser.StemDot, 0); }
		public DotOp2Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterDotOp2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitDotOp2(this);
		}
	}
	public static class LambdaDefContext extends ExpressionContext {
		public TerminalNode LambdaConnector() { return getToken(QDLParserParser.LambdaConnector, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public FunctionContext function() {
			return getRuleContext(FunctionContext.class,0);
		}
		public List<F_argsContext> f_args() {
			return getRuleContexts(F_argsContext.class);
		}
		public F_argsContext f_args(int i) {
			return getRuleContext(F_argsContext.class,i);
		}
		public LambdaDefContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterLambdaDef(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitLambdaDef(this);
		}
	}
	public static class ContainsKeyContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode ContainsKey() { return getToken(QDLParserParser.ContainsKey, 0); }
		public ContainsKeyContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterContainsKey(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitContainsKey(this);
		}
	}
	public static class SwitchExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode SwitchMarker() { return getToken(QDLParserParser.SwitchMarker, 0); }
		public TerminalNode Colon() { return getToken(QDLParserParser.Colon, 0); }
		public SwitchExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterSwitchExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitSwitchExpression(this);
		}
	}
	public static class AppliesOperatorContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Apply() { return getToken(QDLParserParser.Apply, 0); }
		public AppliesOperatorContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterAppliesOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitAppliesOperator(this);
		}
	}
	public static class RegexMatchesContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode RegexMatches() { return getToken(QDLParserParser.RegexMatches, 0); }
		public RegexMatchesContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterRegexMatches(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitRegexMatches(this);
		}
	}
	public static class AltIFExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode AltIfMarker() { return getToken(QDLParserParser.AltIfMarker, 0); }
		public TerminalNode Colon() { return getToken(QDLParserParser.Colon, 0); }
		public AltIFExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterAltIFExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitAltIFExpression(this);
		}
	}
	public static class PowerExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Exponentiation() { return getToken(QDLParserParser.Exponentiation, 0); }
		public PowerExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterPowerExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitPowerExpression(this);
		}
	}
	public static class EqExpressionContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Equals() { return getToken(QDLParserParser.Equals, 0); }
		public TerminalNode NotEquals() { return getToken(QDLParserParser.NotEquals, 0); }
		public EqExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterEqExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitEqExpression(this);
		}
	}
	public static class ExtractContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Backslash() { return getToken(QDLParserParser.Backslash, 0); }
		public ExtractContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterExtract(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitExtract(this);
		}
	}
	public static class NullContext extends ExpressionContext {
		public TerminalNode Null() { return getToken(QDLParserParser.Null, 0); }
		public NullContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterNull(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitNull(this);
		}
	}
	public static class AddExpressionContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Plus() { return getToken(QDLParserParser.Plus, 0); }
		public TerminalNode Minus() { return getToken(QDLParserParser.Minus, 0); }
		public AddExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterAddExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitAddExpression(this);
		}
	}
	public static class Is_aContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode IsA() { return getToken(QDLParserParser.IsA, 0); }
		public Is_aContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterIs_a(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitIs_a(this);
		}
	}
	public static class StemVarContext extends ExpressionContext {
		public StemVariableContext stemVariable() {
			return getRuleContext(StemVariableContext.class,0);
		}
		public StemVarContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterStemVar(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitStemVar(this);
		}
	}
	public static class DotOpContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public List<TerminalNode> StemDot() { return getTokens(QDLParserParser.StemDot); }
		public TerminalNode StemDot(int i) {
			return getToken(QDLParserParser.StemDot, i);
		}
		public DotOpContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterDotOp(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitDotOp(this);
		}
	}
	public static class IsDefinedDyadicExpressionContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode IsDefined() { return getToken(QDLParserParser.IsDefined, 0); }
		public IsDefinedDyadicExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterIsDefinedDyadicExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitIsDefinedDyadicExpression(this);
		}
	}
	public static class ForAllContext extends ExpressionContext {
		public Token op;
		public F_refContext f_ref() {
			return getRuleContext(F_refContext.class,0);
		}
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode ForAll() { return getToken(QDLParserParser.ForAll, 0); }
		public ForAllContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterForAll(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitForAll(this);
		}
	}
	public static class AssociationContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public AssociationContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterAssociation(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitAssociation(this);
		}
	}
	public static class AndExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode And() { return getToken(QDLParserParser.And, 0); }
		public AndExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterAndExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitAndExpression(this);
		}
	}
	public static class StringsContext extends ExpressionContext {
		public TerminalNode STRING() { return getToken(QDLParserParser.STRING, 0); }
		public StringsContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterStrings(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitStrings(this);
		}
	}
	public static class UnaryTildeExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode Tilde() { return getToken(QDLParserParser.Tilde, 0); }
		public TerminalNode TildeRight() { return getToken(QDLParserParser.TildeRight, 0); }
		public UnaryTildeExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterUnaryTildeExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitUnaryTildeExpression(this);
		}
	}
	public static class PostfixContext extends ExpressionContext {
		public Token postfix;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode PlusPlus() { return getToken(QDLParserParser.PlusPlus, 0); }
		public TerminalNode MinusMinus() { return getToken(QDLParserParser.MinusMinus, 0); }
		public PostfixContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterPostfix(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitPostfix(this);
		}
	}
	public static class RealIntervalContext extends ExpressionContext {
		public RIntervalContext rInterval() {
			return getRuleContext(RIntervalContext.class,0);
		}
		public RealIntervalContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterRealInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitRealInterval(this);
		}
	}
	public static class TransposeOperatorContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Transpose() { return getToken(QDLParserParser.Transpose, 0); }
		public TransposeOperatorContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterTransposeOperator(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitTransposeOperator(this);
		}
	}
	public static class VariablesContext extends ExpressionContext {
		public VariableContext variable() {
			return getRuleContext(VariableContext.class,0);
		}
		public VariablesContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterVariables(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitVariables(this);
		}
	}
	public static class AssignmentContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode ASSIGN() { return getToken(QDLParserParser.ASSIGN, 0); }
		public AssignmentContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterAssignment(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitAssignment(this);
		}
	}
	public static class StemLiContext extends ExpressionContext {
		public StemListContext stemList() {
			return getRuleContext(StemListContext.class,0);
		}
		public StemLiContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterStemLi(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitStemLi(this);
		}
	}
	public static class IntIntervalContext extends ExpressionContext {
		public IIntervalContext iInterval() {
			return getRuleContext(IIntervalContext.class,0);
		}
		public IntIntervalContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterIntInterval(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitIntInterval(this);
		}
	}
	public static class LogicalContext extends ExpressionContext {
		public TerminalNode Bool() { return getToken(QDLParserParser.Bool, 0); }
		public LogicalContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterLogical(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitLogical(this);
		}
	}
	public static class OrExpressionContext extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Or() { return getToken(QDLParserParser.Or, 0); }
		public OrExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterOrExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitOrExpression(this);
		}
	}
	public static class ToSetContext extends ExpressionContext {
		public TerminalNode To_Set() { return getToken(QDLParserParser.To_Set, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public ToSetContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterToSet(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitToSet(this);
		}
	}
	public static class UnaryMinusExpressionContext extends ExpressionContext {
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode Plus() { return getToken(QDLParserParser.Plus, 0); }
		public TerminalNode UnaryPlus() { return getToken(QDLParserParser.UnaryPlus, 0); }
		public TerminalNode Minus() { return getToken(QDLParserParser.Minus, 0); }
		public TerminalNode UnaryMinus() { return getToken(QDLParserParser.UnaryMinus, 0); }
		public UnaryMinusExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterUnaryMinusExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitUnaryMinusExpression(this);
		}
	}
	public static class FunctionReferenceContext extends ExpressionContext {
		public F_refContext f_ref() {
			return getRuleContext(F_refContext.class,0);
		}
		public FunctionReferenceContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterFunctionReference(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitFunctionReference(this);
		}
	}
	public static class UnaryTransposeExpressionContext extends ExpressionContext {
		public TerminalNode Transpose() { return getToken(QDLParserParser.Transpose, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public UnaryTransposeExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterUnaryTransposeExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitUnaryTransposeExpression(this);
		}
	}
	public static class IsDefinedExpressionContext extends ExpressionContext {
		public TerminalNode IsDefined() { return getToken(QDLParserParser.IsDefined, 0); }
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public IsDefinedExpressionContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterIsDefinedExpression(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitIsDefinedExpression(this);
		}
	}
	public static class Extract2Context extends ExpressionContext {
		public Token postfix;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode Backslash2() { return getToken(QDLParserParser.Backslash2, 0); }
		public Extract2Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterExtract2(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitExtract2(this);
		}
	}
	public static class Extract3Context extends ExpressionContext {
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode Backslash3() { return getToken(QDLParserParser.Backslash3, 0); }
		public Extract3Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterExtract3(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitExtract3(this);
		}
	}
	public static class Extract4Context extends ExpressionContext {
		public Token postfix;
		public ExpressionContext expression() {
			return getRuleContext(ExpressionContext.class,0);
		}
		public TerminalNode Backslash4() { return getToken(QDLParserParser.Backslash4, 0); }
		public Extract4Context(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterExtract4(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitExtract4(this);
		}
	}
	public static class ExpressionDyadicOpsContext extends ExpressionContext {
		public Token op;
		public List<ExpressionContext> expression() {
			return getRuleContexts(ExpressionContext.class);
		}
		public ExpressionContext expression(int i) {
			return getRuleContext(ExpressionContext.class,i);
		}
		public TerminalNode ExprDyadicOps() { return getToken(QDLParserParser.ExprDyadicOps, 0); }
		public ExpressionDyadicOpsContext(ExpressionContext ctx) { copyFrom(ctx); }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterExpressionDyadicOps(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitExpressionDyadicOps(this);
		}
	}

	public final ExpressionContext expression() throws RecognitionException {
		return expression(0);
	}

	private ExpressionContext expression(int _p) throws RecognitionException {
		ParserRuleContext _parentctx = _ctx;
		int _parentState = getState();
		ExpressionContext _localctx = new ExpressionContext(_ctx, _parentState);
		ExpressionContext _prevctx = _localctx;
		int _startState = 64;
		enterRecursionRule(_localctx, 64, RULE_expression, _p);
		int _la;
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(402);
			_errHandler.sync(this);
			switch ( getInterpreter().adaptivePredict(_input,33,_ctx) ) {
			case 1:
				{
				_localctx = new FunctionsContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;

				setState(340);
				function();
				}
				break;
			case 2:
				{
				_localctx = new ModuleExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(342);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Identifier) {
					{
					setState(341);
					variable();
					}
				}

				setState(344);
				match(Hash);
				setState(345);
				expression(54);
				}
				break;
			case 3:
				{
				_localctx = new UnaryApplyExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(346);
				match(Apply);
				setState(347);
				expression(46);
				}
				break;
			case 4:
				{
				_localctx = new StemVarContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(348);
				stemVariable();
				}
				break;
			case 5:
				{
				_localctx = new StemLiContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(349);
				stemList();
				}
				break;
			case 6:
				{
				_localctx = new SetThingContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(350);
				set();
				}
				break;
			case 7:
				{
				_localctx = new RealIntervalContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(351);
				rInterval();
				}
				break;
			case 8:
				{
				_localctx = new IntIntervalContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(352);
				iInterval();
				}
				break;
			case 9:
				{
				_localctx = new ToSetContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(353);
				match(To_Set);
				setState(354);
				expression(40);
				}
				break;
			case 10:
				{
				_localctx = new PrefixContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(355);
				((PrefixContext)_localctx).prefix = _input.LT(1);
				_la = _input.LA(1);
				if ( !(_la==PlusPlus || _la==MinusMinus) ) {
					((PrefixContext)_localctx).prefix = (Token)_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(356);
				expression(37);
				}
				break;
			case 11:
				{
				_localctx = new FloorOrCeilingExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(357);
				_la = _input.LA(1);
				if ( !(_la==Floor || _la==Ceiling) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(358);
				expression(33);
				}
				break;
			case 12:
				{
				_localctx = new UnaryMinusExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(359);
				_la = _input.LA(1);
				if ( !(((((_la - 47)) & ~0x3f) == 0 && ((1L << (_la - 47)) & ((1L << (Plus - 47)) | (1L << (Minus - 47)) | (1L << (UnaryMinus - 47)) | (1L << (UnaryPlus - 47)))) != 0)) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(360);
				expression(32);
				}
				break;
			case 13:
				{
				_localctx = new IsDefinedExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(361);
				match(IsDefined);
				setState(362);
				expression(31);
				}
				break;
			case 14:
				{
				_localctx = new NotExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(363);
				_la = _input.LA(1);
				if ( !(_la==T__7 || _la==T__8) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(364);
				expression(22);
				}
				break;
			case 15:
				{
				_localctx = new AssociationContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(365);
				match(T__9);
				setState(366);
				expression(0);
				setState(367);
				match(T__2);
				}
				break;
			case 16:
				{
				_localctx = new ForAllContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(369);
				f_ref();
				setState(370);
				((ForAllContext)_localctx).op = match(ForAll);
				setState(371);
				expression(16);
				}
				break;
			case 17:
				{
				_localctx = new FrefDyadicOpsContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(373);
				f_ref();
				setState(374);
				((FrefDyadicOpsContext)_localctx).op = match(FRefDyadicOps);
				setState(375);
				expression(13);
				}
				break;
			case 18:
				{
				_localctx = new UnaryTildeExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(377);
				_la = _input.LA(1);
				if ( !(_la==Tilde || _la==TildeRight) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				setState(378);
				expression(12);
				}
				break;
			case 19:
				{
				_localctx = new UnaryTransposeExpressionContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(379);
				match(Transpose);
				setState(380);
				expression(11);
				}
				break;
			case 20:
				{
				_localctx = new StringsContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(381);
				match(STRING);
				}
				break;
			case 21:
				{
				_localctx = new FunctionReferenceContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(382);
				f_ref();
				}
				break;
			case 22:
				{
				_localctx = new IntegersContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(383);
				integer();
				}
				break;
			case 23:
				{
				_localctx = new NumbersContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(384);
				number();
				}
				break;
			case 24:
				{
				_localctx = new VariablesContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(385);
				variable();
				}
				break;
			case 25:
				{
				_localctx = new KeywordsContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(386);
				keyword();
				}
				break;
			case 26:
				{
				_localctx = new LogicalContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(387);
				match(Bool);
				}
				break;
			case 27:
				{
				_localctx = new NullContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(388);
				match(Null);
				}
				break;
			case 28:
				{
				_localctx = new LambdaDefContext(_localctx);
				_ctx = _localctx;
				_prevctx = _localctx;
				setState(398);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case FuncStart:
					{
					setState(389);
					function();
					}
					break;
				case T__9:
					{
					setState(390);
					match(T__9);
					setState(394);
					_errHandler.sync(this);
					_la = _input.LA(1);
					while ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__0) | (1L << T__7) | (1L << T__8) | (1L << T__9) | (1L << ConstantKeywords) | (1L << Null) | (1L << Integer) | (1L << Decimal) | (1L << SCIENTIFIC_NUMBER) | (1L << Bool) | (1L << STRING) | (1L << LeftBracket) | (1L << LDoubleBracket) | (1L << PlusPlus) | (1L << Plus) | (1L << MinusMinus) | (1L << Minus) | (1L << To_Set))) != 0) || ((((_la - 64)) & ~0x3f) == 0 && ((1L << (_la - 64)) & ((1L << (IsDefined - 64)) | (1L << (Transpose - 64)) | (1L << (Apply - 64)) | (1L << (Tilde - 64)) | (1L << (Hash - 64)) | (1L << (TildeRight - 64)) | (1L << (UnaryMinus - 64)) | (1L << (UnaryPlus - 64)) | (1L << (Floor - 64)) | (1L << (Ceiling - 64)) | (1L << (Identifier - 64)) | (1L << (FuncStart - 64)) | (1L << (F_REF - 64)))) != 0)) {
						{
						{
						setState(391);
						f_args();
						}
						}
						setState(396);
						_errHandler.sync(this);
						_la = _input.LA(1);
					}
					setState(397);
					match(T__2);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				setState(400);
				match(LambdaConnector);
				setState(401);
				expression(1);
				}
				break;
			}
			_ctx.stop = _input.LT(-1);
			setState(495);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,38,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					if ( _parseListeners!=null ) triggerExitRuleEvent();
					_prevctx = _localctx;
					{
					setState(493);
					_errHandler.sync(this);
					switch ( getInterpreter().adaptivePredict(_input,37,_ctx) ) {
					case 1:
						{
						_localctx = new DotOpContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(404);
						if (!(precpred(_ctx, 53))) throw new FailedPredicateException(this, "precpred(_ctx, 53)");
						setState(406); 
						_errHandler.sync(this);
						_la = _input.LA(1);
						do {
							{
							{
							setState(405);
							match(StemDot);
							}
							}
							setState(408); 
							_errHandler.sync(this);
							_la = _input.LA(1);
						} while ( _la==StemDot );
						setState(410);
						expression(54);
						}
						break;
					case 2:
						{
						_localctx = new ExtractContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(411);
						if (!(precpred(_ctx, 51))) throw new FailedPredicateException(this, "precpred(_ctx, 51)");
						setState(412);
						match(Backslash);
						setState(413);
						expression(52);
						}
						break;
					case 3:
						{
						_localctx = new Extract3Context(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(414);
						if (!(precpred(_ctx, 49))) throw new FailedPredicateException(this, "precpred(_ctx, 49)");
						setState(415);
						match(Backslash3);
						setState(416);
						expression(50);
						}
						break;
					case 4:
						{
						_localctx = new AppliesOperatorContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(417);
						if (!(precpred(_ctx, 47))) throw new FailedPredicateException(this, "precpred(_ctx, 47)");
						setState(418);
						((AppliesOperatorContext)_localctx).op = match(Apply);
						setState(419);
						expression(48);
						}
						break;
					case 5:
						{
						_localctx = new TildeExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(420);
						if (!(precpred(_ctx, 39))) throw new FailedPredicateException(this, "precpred(_ctx, 39)");
						setState(421);
						_la = _input.LA(1);
						if ( !(_la==Tilde || _la==TildeRight) ) {
						_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(422);
						expression(40);
						}
						break;
					case 6:
						{
						_localctx = new PowerExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(423);
						if (!(precpred(_ctx, 36))) throw new FailedPredicateException(this, "precpred(_ctx, 36)");
						setState(424);
						match(Exponentiation);
						setState(425);
						expression(37);
						}
						break;
					case 7:
						{
						_localctx = new IntersectionOrUnionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(426);
						if (!(precpred(_ctx, 35))) throw new FailedPredicateException(this, "precpred(_ctx, 35)");
						setState(427);
						((IntersectionOrUnionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << T__3) | (1L << T__4) | (1L << T__5) | (1L << T__6))) != 0)) ) {
							((IntersectionOrUnionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(428);
						expression(36);
						}
						break;
					case 8:
						{
						_localctx = new MultiplyExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(429);
						if (!(precpred(_ctx, 34))) throw new FailedPredicateException(this, "precpred(_ctx, 34)");
						setState(430);
						((MultiplyExpressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(((((_la - 44)) & ~0x3f) == 0 && ((1L << (_la - 44)) & ((1L << (Times - 44)) | (1L << (Divide - 44)) | (1L << (Percent - 44)))) != 0)) ) {
							((MultiplyExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(431);
						expression(35);
						}
						break;
					case 9:
						{
						_localctx = new IsDefinedDyadicExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(432);
						if (!(precpred(_ctx, 30))) throw new FailedPredicateException(this, "precpred(_ctx, 30)");
						setState(433);
						((IsDefinedDyadicExpressionContext)_localctx).op = match(IsDefined);
						setState(434);
						expression(31);
						}
						break;
					case 10:
						{
						_localctx = new AddExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(435);
						if (!(precpred(_ctx, 29))) throw new FailedPredicateException(this, "precpred(_ctx, 29)");
						setState(436);
						((AddExpressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==Plus || _la==Minus) ) {
							((AddExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(437);
						expression(30);
						}
						break;
					case 11:
						{
						_localctx = new CompExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(438);
						if (!(precpred(_ctx, 28))) throw new FailedPredicateException(this, "precpred(_ctx, 28)");
						setState(439);
						((CompExpressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << LessThan) | (1L << GreaterThan) | (1L << LessEquals) | (1L << MoreEquals))) != 0)) ) {
							((CompExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(440);
						expression(29);
						}
						break;
					case 12:
						{
						_localctx = new EqExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(441);
						if (!(precpred(_ctx, 27))) throw new FailedPredicateException(this, "precpred(_ctx, 27)");
						setState(442);
						((EqExpressionContext)_localctx).op = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==Equals || _la==NotEquals) ) {
							((EqExpressionContext)_localctx).op = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						setState(443);
						expression(28);
						}
						break;
					case 13:
						{
						_localctx = new RegexMatchesContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(444);
						if (!(precpred(_ctx, 26))) throw new FailedPredicateException(this, "precpred(_ctx, 26)");
						setState(445);
						((RegexMatchesContext)_localctx).op = match(RegexMatches);
						setState(446);
						expression(27);
						}
						break;
					case 14:
						{
						_localctx = new Is_aContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(447);
						if (!(precpred(_ctx, 25))) throw new FailedPredicateException(this, "precpred(_ctx, 25)");
						setState(448);
						match(IsA);
						setState(449);
						expression(26);
						}
						break;
					case 15:
						{
						_localctx = new AndExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(450);
						if (!(precpred(_ctx, 24))) throw new FailedPredicateException(this, "precpred(_ctx, 24)");
						setState(451);
						match(And);
						setState(452);
						expression(25);
						}
						break;
					case 16:
						{
						_localctx = new OrExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(453);
						if (!(precpred(_ctx, 23))) throw new FailedPredicateException(this, "precpred(_ctx, 23)");
						setState(454);
						match(Or);
						setState(455);
						expression(24);
						}
						break;
					case 17:
						{
						_localctx = new EpsilonContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(456);
						if (!(precpred(_ctx, 18))) throw new FailedPredicateException(this, "precpred(_ctx, 18)");
						setState(457);
						((EpsilonContext)_localctx).op = match(Membership);
						setState(458);
						expression(19);
						}
						break;
					case 18:
						{
						_localctx = new ContainsKeyContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(459);
						if (!(precpred(_ctx, 17))) throw new FailedPredicateException(this, "precpred(_ctx, 17)");
						setState(460);
						((ContainsKeyContext)_localctx).op = match(ContainsKey);
						setState(461);
						expression(18);
						}
						break;
					case 19:
						{
						_localctx = new TransposeOperatorContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(462);
						if (!(precpred(_ctx, 15))) throw new FailedPredicateException(this, "precpred(_ctx, 15)");
						setState(463);
						((TransposeOperatorContext)_localctx).op = match(Transpose);
						setState(464);
						expression(16);
						}
						break;
					case 20:
						{
						_localctx = new ExpressionDyadicOpsContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(465);
						if (!(precpred(_ctx, 14))) throw new FailedPredicateException(this, "precpred(_ctx, 14)");
						setState(466);
						((ExpressionDyadicOpsContext)_localctx).op = match(ExprDyadicOps);
						setState(467);
						expression(15);
						}
						break;
					case 21:
						{
						_localctx = new AssignmentContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(468);
						if (!(precpred(_ctx, 2))) throw new FailedPredicateException(this, "precpred(_ctx, 2)");
						setState(469);
						((AssignmentContext)_localctx).op = match(ASSIGN);
						setState(470);
						expression(3);
						}
						break;
					case 22:
						{
						_localctx = new DotOp2Context(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(471);
						if (!(precpred(_ctx, 52))) throw new FailedPredicateException(this, "precpred(_ctx, 52)");
						setState(472);
						((DotOp2Context)_localctx).postfix = match(StemDot);
						}
						break;
					case 23:
						{
						_localctx = new Extract2Context(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(473);
						if (!(precpred(_ctx, 50))) throw new FailedPredicateException(this, "precpred(_ctx, 50)");
						setState(474);
						((Extract2Context)_localctx).postfix = match(Backslash2);
						}
						break;
					case 24:
						{
						_localctx = new Extract4Context(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(475);
						if (!(precpred(_ctx, 48))) throw new FailedPredicateException(this, "precpred(_ctx, 48)");
						setState(476);
						((Extract4Context)_localctx).postfix = match(Backslash4);
						}
						break;
					case 25:
						{
						_localctx = new PostfixContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(477);
						if (!(precpred(_ctx, 38))) throw new FailedPredicateException(this, "precpred(_ctx, 38)");
						setState(478);
						((PostfixContext)_localctx).postfix = _input.LT(1);
						_la = _input.LA(1);
						if ( !(_la==PlusPlus || _la==MinusMinus) ) {
							((PostfixContext)_localctx).postfix = (Token)_errHandler.recoverInline(this);
						}
						else {
							if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
							_errHandler.reportMatch(this);
							consume();
						}
						}
						break;
					case 26:
						{
						_localctx = new AltIFExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(479);
						if (!(precpred(_ctx, 20))) throw new FailedPredicateException(this, "precpred(_ctx, 20)");
						setState(480);
						match(AltIfMarker);
						setState(481);
						expression(0);
						setState(484);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,35,_ctx) ) {
						case 1:
							{
							setState(482);
							match(Colon);
							setState(483);
							expression(0);
							}
							break;
						}
						}
						break;
					case 27:
						{
						_localctx = new SwitchExpressionContext(new ExpressionContext(_parentctx, _parentState));
						pushNewRecursionContext(_localctx, _startState, RULE_expression);
						setState(486);
						if (!(precpred(_ctx, 19))) throw new FailedPredicateException(this, "precpred(_ctx, 19)");
						setState(487);
						match(SwitchMarker);
						setState(488);
						expression(0);
						setState(491);
						_errHandler.sync(this);
						switch ( getInterpreter().adaptivePredict(_input,36,_ctx) ) {
						case 1:
							{
							setState(489);
							match(Colon);
							setState(490);
							expression(0);
							}
							break;
						}
						}
						break;
					}
					} 
				}
				setState(497);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,38,_ctx);
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			unrollRecursionContexts(_parentctx);
		}
		return _localctx;
	}

	public static class VariableContext extends ParserRuleContext {
		public TerminalNode Identifier() { return getToken(QDLParserParser.Identifier, 0); }
		public VariableContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_variable; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterVariable(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitVariable(this);
		}
	}

	public final VariableContext variable() throws RecognitionException {
		VariableContext _localctx = new VariableContext(_ctx, getState());
		enterRule(_localctx, 66, RULE_variable);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(498);
			match(Identifier);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class NumberContext extends ParserRuleContext {
		public TerminalNode Decimal() { return getToken(QDLParserParser.Decimal, 0); }
		public TerminalNode SCIENTIFIC_NUMBER() { return getToken(QDLParserParser.SCIENTIFIC_NUMBER, 0); }
		public NumberContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_number; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterNumber(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitNumber(this);
		}
	}

	public final NumberContext number() throws RecognitionException {
		NumberContext _localctx = new NumberContext(_ctx, getState());
		enterRule(_localctx, 68, RULE_number);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(500);
			_la = _input.LA(1);
			if ( !(_la==Decimal || _la==SCIENTIFIC_NUMBER) ) {
			_errHandler.recoverInline(this);
			}
			else {
				if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
				_errHandler.reportMatch(this);
				consume();
			}
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class IntegerContext extends ParserRuleContext {
		public TerminalNode Integer() { return getToken(QDLParserParser.Integer, 0); }
		public IntegerContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_integer; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterInteger(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitInteger(this);
		}
	}

	public final IntegerContext integer() throws RecognitionException {
		IntegerContext _localctx = new IntegerContext(_ctx, getState());
		enterRule(_localctx, 70, RULE_integer);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(502);
			match(Integer);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public static class KeywordContext extends ParserRuleContext {
		public TerminalNode ConstantKeywords() { return getToken(QDLParserParser.ConstantKeywords, 0); }
		public KeywordContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_keyword; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).enterKeyword(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof QDLParserListener ) ((QDLParserListener)listener).exitKeyword(this);
		}
	}

	public final KeywordContext keyword() throws RecognitionException {
		KeywordContext _localctx = new KeywordContext(_ctx, getState());
		enterRule(_localctx, 72, RULE_keyword);
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(504);
			match(ConstantKeywords);
			}
		}
		catch (RecognitionException re) {
			_localctx.exception = re;
			_errHandler.reportError(this, re);
			_errHandler.recover(this, re);
		}
		finally {
			exitRule();
		}
		return _localctx;
	}

	public boolean sempred(RuleContext _localctx, int ruleIndex, int predIndex) {
		switch (ruleIndex) {
		case 32:
			return expression_sempred((ExpressionContext)_localctx, predIndex);
		}
		return true;
	}
	private boolean expression_sempred(ExpressionContext _localctx, int predIndex) {
		switch (predIndex) {
		case 0:
			return precpred(_ctx, 53);
		case 1:
			return precpred(_ctx, 51);
		case 2:
			return precpred(_ctx, 49);
		case 3:
			return precpred(_ctx, 47);
		case 4:
			return precpred(_ctx, 39);
		case 5:
			return precpred(_ctx, 36);
		case 6:
			return precpred(_ctx, 35);
		case 7:
			return precpred(_ctx, 34);
		case 8:
			return precpred(_ctx, 30);
		case 9:
			return precpred(_ctx, 29);
		case 10:
			return precpred(_ctx, 28);
		case 11:
			return precpred(_ctx, 27);
		case 12:
			return precpred(_ctx, 26);
		case 13:
			return precpred(_ctx, 25);
		case 14:
			return precpred(_ctx, 24);
		case 15:
			return precpred(_ctx, 23);
		case 16:
			return precpred(_ctx, 18);
		case 17:
			return precpred(_ctx, 17);
		case 18:
			return precpred(_ctx, 15);
		case 19:
			return precpred(_ctx, 14);
		case 20:
			return precpred(_ctx, 2);
		case 21:
			return precpred(_ctx, 52);
		case 22:
			return precpred(_ctx, 50);
		case 23:
			return precpred(_ctx, 48);
		case 24:
			return precpred(_ctx, 38);
		case 25:
			return precpred(_ctx, 20);
		case 26:
			return precpred(_ctx, 19);
		}
		return true;
	}

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3e\u01fd\4\2\t\2\4"+
		"\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13\t"+
		"\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\3\2\7\2N\n\2\f\2\16\2Q\13\2\3\2\3"+
		"\2\3\3\3\3\3\3\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\3\4\5\4d\n"+
		"\4\3\5\3\5\5\5h\n\5\3\6\3\6\3\6\5\6m\n\6\3\6\3\6\3\7\3\7\3\7\5\7t\n\7"+
		"\3\7\3\7\3\7\3\7\3\b\3\b\3\b\5\b}\n\b\3\b\3\b\3\t\3\t\3\t\3\t\3\t\7\t"+
		"\u0086\n\t\f\t\16\t\u0089\13\t\3\t\3\t\3\n\3\n\3\n\3\n\3\n\5\n\u0092\n"+
		"\n\3\n\3\n\3\13\3\13\3\13\5\13\u0099\n\13\3\13\3\13\3\f\3\f\3\f\3\f\3"+
		"\f\5\f\u00a2\n\f\3\f\3\f\5\f\u00a6\n\f\3\f\3\f\3\r\3\r\3\r\3\r\3\r\3\16"+
		"\3\16\3\16\3\16\3\16\3\16\3\16\3\16\3\17\3\17\3\17\3\20\3\20\3\20\3\21"+
		"\3\21\3\21\3\21\5\21\u00c1\n\21\3\22\3\22\3\22\3\22\7\22\u00c7\n\22\f"+
		"\22\16\22\u00ca\13\22\3\22\3\22\3\23\3\23\7\23\u00d0\n\23\f\23\16\23\u00d3"+
		"\13\23\3\23\3\23\3\23\6\23\u00d8\n\23\r\23\16\23\u00d9\3\23\3\23\3\24"+
		"\3\24\3\24\3\24\3\24\3\24\6\24\u00e4\n\24\r\24\16\24\u00e5\3\24\3\24\3"+
		"\25\3\25\3\25\3\25\3\26\3\26\3\27\3\27\5\27\u00f2\n\27\3\27\3\27\3\27"+
		"\3\27\3\27\5\27\u00f9\n\27\3\27\3\27\3\30\3\30\5\30\u00ff\n\30\3\30\3"+
		"\30\3\30\3\30\3\30\5\30\u0106\n\30\3\30\3\30\3\31\3\31\3\31\3\31\7\31"+
		"\u010e\n\31\f\31\16\31\u0111\13\31\3\31\3\31\3\31\3\31\5\31\u0117\n\31"+
		"\3\32\3\32\3\32\3\32\7\32\u011d\n\32\f\32\16\32\u0120\13\32\3\32\3\32"+
		"\3\33\3\33\5\33\u0126\n\33\3\33\3\33\3\33\3\34\3\34\3\34\3\34\7\34\u012f"+
		"\n\34\f\34\16\34\u0132\13\34\3\34\3\34\3\34\3\34\5\34\u0138\n\34\3\35"+
		"\3\35\3\35\5\35\u013d\n\35\3\36\3\36\7\36\u0141\n\36\f\36\16\36\u0144"+
		"\13\36\3\36\3\36\3\37\3\37\5\37\u014a\n\37\3 \3 \3 \7 \u014f\n \f \16"+
		" \u0152\13 \3!\3!\3\"\3\"\3\"\5\"\u0159\n\"\3\"\3\"\3\"\3\"\3\"\3\"\3"+
		"\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\""+
		"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3"+
		"\"\3\"\3\"\3\"\3\"\3\"\3\"\7\"\u018b\n\"\f\"\16\"\u018e\13\"\3\"\5\"\u0191"+
		"\n\"\3\"\3\"\5\"\u0195\n\"\3\"\3\"\6\"\u0199\n\"\r\"\16\"\u019a\3\"\3"+
		"\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\""+
		"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3"+
		"\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\""+
		"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3\"\3"+
		"\"\3\"\3\"\5\"\u01e7\n\"\3\"\3\"\3\"\3\"\3\"\5\"\u01ee\n\"\7\"\u01f0\n"+
		"\"\f\"\16\"\u01f3\13\"\3#\3#\3$\3$\3%\3%\3&\3&\3&\2\3B\'\2\4\6\b\n\f\16"+
		"\20\22\24\26\30\32\34\36 \"$&(*,.\60\62\64\668:<>@BDFHJ\2\16\3\2\22\23"+
		"\4\2\60\60\62\62\3\2YZ\5\2\61\61\63\63WX\3\2\n\13\4\2NNUU\3\2\6\t\4\2"+
		"./MM\4\2\61\61\63\63\4\2\64\65:;\3\2=>\3\2\"#\2\u023e\2O\3\2\2\2\4T\3"+
		"\2\2\2\6c\3\2\2\2\bg\3\2\2\2\ni\3\2\2\2\fp\3\2\2\2\16y\3\2\2\2\20\u0080"+
		"\3\2\2\2\22\u008c\3\2\2\2\24\u0095\3\2\2\2\26\u009c\3\2\2\2\30\u00a9\3"+
		"\2\2\2\32\u00ae\3\2\2\2\34\u00b6\3\2\2\2\36\u00b9\3\2\2\2 \u00bc\3\2\2"+
		"\2\"\u00c2\3\2\2\2$\u00cd\3\2\2\2&\u00dd\3\2\2\2(\u00e9\3\2\2\2*\u00ed"+
		"\3\2\2\2,\u00ef\3\2\2\2.\u00fc\3\2\2\2\60\u0116\3\2\2\2\62\u0118\3\2\2"+
		"\2\64\u0125\3\2\2\2\66\u0137\3\2\2\28\u013c\3\2\2\2:\u013e\3\2\2\2<\u0149"+
		"\3\2\2\2>\u014b\3\2\2\2@\u0153\3\2\2\2B\u0194\3\2\2\2D\u01f4\3\2\2\2F"+
		"\u01f6\3\2\2\2H\u01f8\3\2\2\2J\u01fa\3\2\2\2LN\5\4\3\2ML\3\2\2\2NQ\3\2"+
		"\2\2OM\3\2\2\2OP\3\2\2\2PR\3\2\2\2QO\3\2\2\2RS\7\2\2\3S\3\3\2\2\2TU\5"+
		"\6\4\2UV\7*\2\2V\5\3\2\2\2Wd\5\22\n\2Xd\5\b\5\2Yd\5\16\b\2Zd\5\20\t\2"+
		"[d\5B\"\2\\d\5\30\r\2]d\5\24\13\2^d\5\32\16\2_d\5 \21\2`d\5\34\17\2ad"+
		"\5\36\20\2bd\5\26\f\2cW\3\2\2\2cX\3\2\2\2cY\3\2\2\2cZ\3\2\2\2c[\3\2\2"+
		"\2c\\\3\2\2\2c]\3\2\2\2c^\3\2\2\2c_\3\2\2\2c`\3\2\2\2ca\3\2\2\2cb\3\2"+
		"\2\2d\7\3\2\2\2eh\5\n\6\2fh\5\f\7\2ge\3\2\2\2gf\3\2\2\2h\t\3\2\2\2ij\7"+
		"\31\2\2jl\5(\25\2km\7\36\2\2lk\3\2\2\2lm\3\2\2\2mn\3\2\2\2no\5\"\22\2"+
		"o\13\3\2\2\2pq\7\31\2\2qs\5(\25\2rt\7\36\2\2sr\3\2\2\2st\3\2\2\2tu\3\2"+
		"\2\2uv\5\"\22\2vw\7\30\2\2wx\5\"\22\2x\r\3\2\2\2yz\7 \2\2z|\5(\25\2{}"+
		"\7\27\2\2|{\3\2\2\2|}\3\2\2\2}~\3\2\2\2~\177\5\"\22\2\177\17\3\2\2\2\u0080"+
		"\u0081\7\35\2\2\u0081\u0087\7&\2\2\u0082\u0083\5\n\6\2\u0083\u0084\7*"+
		"\2\2\u0084\u0086\3\2\2\2\u0085\u0082\3\2\2\2\u0086\u0089\3\2\2\2\u0087"+
		"\u0085\3\2\2\2\u0087\u0088\3\2\2\2\u0088\u008a\3\2\2\2\u0089\u0087\3\2"+
		"\2\2\u008a\u008b\7\'\2\2\u008b\21\3\2\2\2\u008c\u008d\7\26\2\2\u008d\u008e"+
		"\7&\2\2\u008e\u008f\5:\36\2\u008f\u0091\7\'\2\2\u0090\u0092\7\24\2\2\u0091"+
		"\u0090\3\2\2\2\u0091\u0092\3\2\2\2\u0092\u0093\3\2\2\2\u0093\u0094\5$"+
		"\23\2\u0094\23\3\2\2\2\u0095\u0096\5:\36\2\u0096\u0098\7-\2\2\u0097\u0099"+
		"\t\2\2\2\u0098\u0097\3\2\2\2\u0098\u0099\3\2\2\2\u0099\u009a\3\2\2\2\u009a"+
		"\u009b\5$\23\2\u009b\25\3\2\2\2\u009c\u009d\7\32\2\2\u009d\u009e\7&\2"+
		"\2\u009e\u00a1\7%\2\2\u009f\u00a0\7(\2\2\u00a0\u00a2\7%\2\2\u00a1\u009f"+
		"\3\2\2\2\u00a1\u00a2\3\2\2\2\u00a2\u00a3\3\2\2\2\u00a3\u00a5\7\'\2\2\u00a4"+
		"\u00a6\7\24\2\2\u00a5\u00a4\3\2\2\2\u00a5\u00a6\3\2\2\2\u00a6\u00a7\3"+
		"\2\2\2\u00a7\u00a8\5$\23\2\u00a8\27\3\2\2\2\u00a9\u00aa\7\37\2\2\u00aa"+
		"\u00ab\5\"\22\2\u00ab\u00ac\7\25\2\2\u00ac\u00ad\5\"\22\2\u00ad\31\3\2"+
		"\2\2\u00ae\u00af\7\16\2\2\u00af\u00b0\7&\2\2\u00b0\u00b1\5B\"\2\u00b1"+
		"\u00b2\7\'\2\2\u00b2\u00b3\7&\2\2\u00b3\u00b4\5B\"\2\u00b4\u00b5\7\'\2"+
		"\2\u00b5\33\3\2\2\2\u00b6\u00b7\7\22\2\2\u00b7\u00b8\5\"\22\2\u00b8\35"+
		"\3\2\2\2\u00b9\u00ba\7\23\2\2\u00ba\u00bb\5\"\22\2\u00bb\37\3\2\2\2\u00bc"+
		"\u00bd\7\17\2\2\u00bd\u00c0\5B\"\2\u00be\u00bf\7)\2\2\u00bf\u00c1\5B\""+
		"\2\u00c0\u00be\3\2\2\2\u00c0\u00c1\3\2\2\2\u00c1!\3\2\2\2\u00c2\u00c8"+
		"\7&\2\2\u00c3\u00c4\5\6\4\2\u00c4\u00c5\7*\2\2\u00c5\u00c7\3\2\2\2\u00c6"+
		"\u00c3\3\2\2\2\u00c7\u00ca\3\2\2\2\u00c8\u00c6\3\2\2\2\u00c8\u00c9\3\2"+
		"\2\2\u00c9\u00cb\3\2\2\2\u00ca\u00c8\3\2\2\2\u00cb\u00cc\7\'\2\2\u00cc"+
		"#\3\2\2\2\u00cd\u00d1\7&\2\2\u00ce\u00d0\5*\26\2\u00cf\u00ce\3\2\2\2\u00d0"+
		"\u00d3\3\2\2\2\u00d1\u00cf\3\2\2\2\u00d1\u00d2\3\2\2\2\u00d2\u00d7\3\2"+
		"\2\2\u00d3\u00d1\3\2\2\2\u00d4\u00d5\5\6\4\2\u00d5\u00d6\7*\2\2\u00d6"+
		"\u00d8\3\2\2\2\u00d7\u00d4\3\2\2\2\u00d8\u00d9\3\2\2\2\u00d9\u00d7\3\2"+
		"\2\2\u00d9\u00da\3\2\2\2\u00da\u00db\3\2\2\2\u00db\u00dc\7\'\2\2\u00dc"+
		"%\3\2\2\2\u00dd\u00de\7&\2\2\u00de\u00df\5B\"\2\u00df\u00e3\7*\2\2\u00e0"+
		"\u00e1\5B\"\2\u00e1\u00e2\7*\2\2\u00e2\u00e4\3\2\2\2\u00e3\u00e0\3\2\2"+
		"\2\u00e4\u00e5\3\2\2\2\u00e5\u00e3\3\2\2\2\u00e5\u00e6\3\2\2\2\u00e6\u00e7"+
		"\3\2\2\2\u00e7\u00e8\7\'\2\2\u00e8\'\3\2\2\2\u00e9\u00ea\7&\2\2\u00ea"+
		"\u00eb\5B\"\2\u00eb\u00ec\7\'\2\2\u00ec)\3\2\2\2\u00ed\u00ee\7b\2\2\u00ee"+
		"+\3\2\2\2\u00ef\u00f1\7&\2\2\u00f0\u00f2\5B\"\2\u00f1\u00f0\3\2\2\2\u00f1"+
		"\u00f2\3\2\2\2\u00f2\u00f3\3\2\2\2\u00f3\u00f4\7*\2\2\u00f4\u00f8\5B\""+
		"\2\u00f5\u00f9\7*\2\2\u00f6\u00f7\7*\2\2\u00f7\u00f9\5B\"\2\u00f8\u00f5"+
		"\3\2\2\2\u00f8\u00f6\3\2\2\2\u00f8\u00f9\3\2\2\2\u00f9\u00fa\3\2\2\2\u00fa"+
		"\u00fb\7\'\2\2\u00fb-\3\2\2\2\u00fc\u00fe\7+\2\2\u00fd\u00ff\5B\"\2\u00fe"+
		"\u00fd\3\2\2\2\u00fe\u00ff\3\2\2\2\u00ff\u0100\3\2\2\2\u0100\u0101\7*"+
		"\2\2\u0101\u0105\5B\"\2\u0102\u0106\7*\2\2\u0103\u0104\7*\2\2\u0104\u0106"+
		"\5B\"\2\u0105\u0102\3\2\2\2\u0105\u0103\3\2\2\2\u0105\u0106\3\2\2\2\u0106"+
		"\u0107\3\2\2\2\u0107\u0108\7,\2\2\u0108/\3\2\2\2\u0109\u010a\7\3\2\2\u010a"+
		"\u010f\5B\"\2\u010b\u010c\7(\2\2\u010c\u010e\5B\"\2\u010d\u010b\3\2\2"+
		"\2\u010e\u0111\3\2\2\2\u010f\u010d\3\2\2\2\u010f\u0110\3\2\2\2\u0110\u0112"+
		"\3\2\2\2\u0111\u010f\3\2\2\2\u0112\u0113\7\4\2\2\u0113\u0117\3\2\2\2\u0114"+
		"\u0115\7\3\2\2\u0115\u0117\7\4\2\2\u0116\u0109\3\2\2\2\u0116\u0114\3\2"+
		"\2\2\u0117\61\3\2\2\2\u0118\u0119\7\3\2\2\u0119\u011e\5\64\33\2\u011a"+
		"\u011b\7(\2\2\u011b\u011d\5\64\33\2\u011c\u011a\3\2\2\2\u011d\u0120\3"+
		"\2\2\2\u011e\u011c\3\2\2\2\u011e\u011f\3\2\2\2\u011f\u0121\3\2\2\2\u0120"+
		"\u011e\3\2\2\2\u0121\u0122\7\4\2\2\u0122\63\3\2\2\2\u0123\u0126\7.\2\2"+
		"\u0124\u0126\5B\"\2\u0125\u0123\3\2\2\2\u0125\u0124\3\2\2\2\u0126\u0127"+
		"\3\2\2\2\u0127\u0128\7)\2\2\u0128\u0129\58\35\2\u0129\65\3\2\2\2\u012a"+
		"\u012b\7&\2\2\u012b\u0130\58\35\2\u012c\u012d\7(\2\2\u012d\u012f\58\35"+
		"\2\u012e\u012c\3\2\2\2\u012f\u0132\3\2\2\2\u0130\u012e\3\2\2\2\u0130\u0131"+
		"\3\2\2\2\u0131\u0133\3\2\2\2\u0132\u0130\3\2\2\2\u0133\u0134\7\'\2\2\u0134"+
		"\u0138\3\2\2\2\u0135\u0136\7&\2\2\u0136\u0138\7\'\2\2\u0137\u012a\3\2"+
		"\2\2\u0137\u0135\3\2\2\2\u0138\67\3\2\2\2\u0139\u013d\5B\"\2\u013a\u013d"+
		"\5\62\32\2\u013b\u013d\5\66\34\2\u013c\u0139\3\2\2\2\u013c\u013a\3\2\2"+
		"\2\u013c\u013b\3\2\2\2\u013d9\3\2\2\2\u013e\u0142\7`\2\2\u013f\u0141\5"+
		"> \2\u0140\u013f\3\2\2\2\u0141\u0144\3\2\2\2\u0142\u0140\3\2\2\2\u0142"+
		"\u0143\3\2\2\2\u0143\u0145\3\2\2\2\u0144\u0142\3\2\2\2\u0145\u0146\7\5"+
		"\2\2\u0146;\3\2\2\2\u0147\u014a\58\35\2\u0148\u014a\5@!\2\u0149\u0147"+
		"\3\2\2\2\u0149\u0148\3\2\2\2\u014a=\3\2\2\2\u014b\u0150\5<\37\2\u014c"+
		"\u014d\7(\2\2\u014d\u014f\5<\37\2\u014e\u014c\3\2\2\2\u014f\u0152\3\2"+
		"\2\2\u0150\u014e\3\2\2\2\u0150\u0151\3\2\2\2\u0151?\3\2\2\2\u0152\u0150"+
		"\3\2\2\2\u0153\u0154\7a\2\2\u0154A\3\2\2\2\u0155\u0156\b\"\1\2\u0156\u0195"+
		"\5:\36\2\u0157\u0159\5D#\2\u0158\u0157\3\2\2\2\u0158\u0159\3\2\2\2\u0159"+
		"\u015a\3\2\2\2\u015a\u015b\7S\2\2\u015b\u0195\5B\"8\u015c\u015d\7G\2\2"+
		"\u015d\u0195\5B\"\60\u015e\u0195\5\62\32\2\u015f\u0195\5\66\34\2\u0160"+
		"\u0195\5\60\31\2\u0161\u0195\5.\30\2\u0162\u0195\5,\27\2\u0163\u0164\7"+
		"9\2\2\u0164\u0195\5B\"*\u0165\u0166\t\3\2\2\u0166\u0195\5B\"\'\u0167\u0168"+
		"\t\4\2\2\u0168\u0195\5B\"#\u0169\u016a\t\5\2\2\u016a\u0195\5B\"\"\u016b"+
		"\u016c\7B\2\2\u016c\u0195\5B\"!\u016d\u016e\t\6\2\2\u016e\u0195\5B\"\30"+
		"\u016f\u0170\7\f\2\2\u0170\u0171\5B\"\2\u0171\u0172\7\5\2\2\u0172\u0195"+
		"\3\2\2\2\u0173\u0174\5@!\2\u0174\u0175\7C\2\2\u0175\u0176\5B\"\22\u0176"+
		"\u0195\3\2\2\2\u0177\u0178\5@!\2\u0178\u0179\7I\2\2\u0179\u017a\5B\"\17"+
		"\u017a\u0195\3\2\2\2\u017b\u017c\t\7\2\2\u017c\u0195\5B\"\16\u017d\u017e"+
		"\7F\2\2\u017e\u0195\5B\"\r\u017f\u0195\7%\2\2\u0180\u0195\5@!\2\u0181"+
		"\u0195\5H%\2\u0182\u0195\5F$\2\u0183\u0195\5D#\2\u0184\u0195\5J&\2\u0185"+
		"\u0195\7$\2\2\u0186\u0195\7\33\2\2\u0187\u0191\5:\36\2\u0188\u018c\7\f"+
		"\2\2\u0189\u018b\5> \2\u018a\u0189\3\2\2\2\u018b\u018e\3\2\2\2\u018c\u018a"+
		"\3\2\2\2\u018c\u018d\3\2\2\2\u018d\u018f\3\2\2\2\u018e\u018c\3\2\2\2\u018f"+
		"\u0191\7\5\2\2\u0190\u0187\3\2\2\2\u0190\u0188\3\2\2\2\u0191\u0192\3\2"+
		"\2\2\u0192\u0193\7-\2\2\u0193\u0195\5B\"\3\u0194\u0155\3\2\2\2\u0194\u0158"+
		"\3\2\2\2\u0194\u015c\3\2\2\2\u0194\u015e\3\2\2\2\u0194\u015f\3\2\2\2\u0194"+
		"\u0160\3\2\2\2\u0194\u0161\3\2\2\2\u0194\u0162\3\2\2\2\u0194\u0163\3\2"+
		"\2\2\u0194\u0165\3\2\2\2\u0194\u0167\3\2\2\2\u0194\u0169\3\2\2\2\u0194"+
		"\u016b\3\2\2\2\u0194\u016d\3\2\2\2\u0194\u016f\3\2\2\2\u0194\u0173\3\2"+
		"\2\2\u0194\u0177\3\2\2\2\u0194\u017b\3\2\2\2\u0194\u017d\3\2\2\2\u0194"+
		"\u017f\3\2\2\2\u0194\u0180\3\2\2\2\u0194\u0181\3\2\2\2\u0194\u0182\3\2"+
		"\2\2\u0194\u0183\3\2\2\2\u0194\u0184\3\2\2\2\u0194\u0185\3\2\2\2\u0194"+
		"\u0186\3\2\2\2\u0194\u0190\3\2\2\2\u0195\u01f1\3\2\2\2\u0196\u0198\f\67"+
		"\2\2\u0197\u0199\7V\2\2\u0198\u0197\3\2\2\2\u0199\u019a\3\2\2\2\u019a"+
		"\u0198\3\2\2\2\u019a\u019b\3\2\2\2\u019b\u019c\3\2\2\2\u019c\u01f0\5B"+
		"\"8\u019d\u019e\f\65\2\2\u019e\u019f\7O\2\2\u019f\u01f0\5B\"\66\u01a0"+
		"\u01a1\f\63\2\2\u01a1\u01a2\7Q\2\2\u01a2\u01f0\5B\"\64\u01a3\u01a4\f\61"+
		"\2\2\u01a4\u01a5\7G\2\2\u01a5\u01f0\5B\"\62\u01a6\u01a7\f)\2\2\u01a7\u01a8"+
		"\t\7\2\2\u01a8\u01f0\5B\"*\u01a9\u01aa\f&\2\2\u01aa\u01ab\7E\2\2\u01ab"+
		"\u01f0\5B\"\'\u01ac\u01ad\f%\2\2\u01ad\u01ae\t\b\2\2\u01ae\u01f0\5B\""+
		"&\u01af\u01b0\f$\2\2\u01b0\u01b1\t\t\2\2\u01b1\u01f0\5B\"%\u01b2\u01b3"+
		"\f \2\2\u01b3\u01b4\7B\2\2\u01b4\u01f0\5B\"!\u01b5\u01b6\f\37\2\2\u01b6"+
		"\u01b7\t\n\2\2\u01b7\u01f0\5B\" \u01b8\u01b9\f\36\2\2\u01b9\u01ba\t\13"+
		"\2\2\u01ba\u01f0\5B\"\37\u01bb\u01bc\f\35\2\2\u01bc\u01bd\t\f\2\2\u01bd"+
		"\u01f0\5B\"\36\u01be\u01bf\f\34\2\2\u01bf\u01c0\7?\2\2\u01c0\u01f0\5B"+
		"\"\35\u01c1\u01c2\f\33\2\2\u01c2\u01c3\7<\2\2\u01c3\u01f0\5B\"\34\u01c4"+
		"\u01c5\f\32\2\2\u01c5\u01c6\7J\2\2\u01c6\u01f0\5B\"\33\u01c7\u01c8\f\31"+
		"\2\2\u01c8\u01c9\7K\2\2\u01c9\u01f0\5B\"\32\u01ca\u01cb\f\24\2\2\u01cb"+
		"\u01cc\7A\2\2\u01cc\u01f0\5B\"\25\u01cd\u01ce\f\23\2\2\u01ce\u01cf\7D"+
		"\2\2\u01cf\u01f0\5B\"\24\u01d0\u01d1\f\21\2\2\u01d1\u01d2\7F\2\2\u01d2"+
		"\u01f0\5B\"\22\u01d3\u01d4\f\20\2\2\u01d4\u01d5\7H\2\2\u01d5\u01f0\5B"+
		"\"\21\u01d6\u01d7\f\4\2\2\u01d7\u01d8\7^\2\2\u01d8\u01f0\5B\"\5\u01d9"+
		"\u01da\f\66\2\2\u01da\u01f0\7V\2\2\u01db\u01dc\f\64\2\2\u01dc\u01f0\7"+
		"P\2\2\u01dd\u01de\f\62\2\2\u01de\u01f0\7R\2\2\u01df\u01e0\f(\2\2\u01e0"+
		"\u01f0\t\3\2\2\u01e1\u01e2\f\26\2\2\u01e2\u01e3\7\\\2\2\u01e3\u01e6\5"+
		"B\"\2\u01e4\u01e5\7)\2\2\u01e5\u01e7\5B\"\2\u01e6\u01e4\3\2\2\2\u01e6"+
		"\u01e7\3\2\2\2\u01e7\u01f0\3\2\2\2\u01e8\u01e9\f\25\2\2\u01e9\u01ea\7"+
		"]\2\2\u01ea\u01ed\5B\"\2\u01eb\u01ec\7)\2\2\u01ec\u01ee\5B\"\2\u01ed\u01eb"+
		"\3\2\2\2\u01ed\u01ee\3\2\2\2\u01ee\u01f0\3\2\2\2\u01ef\u0196\3\2\2\2\u01ef"+
		"\u019d\3\2\2\2\u01ef\u01a0\3\2\2\2\u01ef\u01a3\3\2\2\2\u01ef\u01a6\3\2"+
		"\2\2\u01ef\u01a9\3\2\2\2\u01ef\u01ac\3\2\2\2\u01ef\u01af\3\2\2\2\u01ef"+
		"\u01b2\3\2\2\2\u01ef\u01b5\3\2\2\2\u01ef\u01b8\3\2\2\2\u01ef\u01bb\3\2"+
		"\2\2\u01ef\u01be\3\2\2\2\u01ef\u01c1\3\2\2\2\u01ef\u01c4\3\2\2\2\u01ef"+
		"\u01c7\3\2\2\2\u01ef\u01ca\3\2\2\2\u01ef\u01cd\3\2\2\2\u01ef\u01d0\3\2"+
		"\2\2\u01ef\u01d3\3\2\2\2\u01ef\u01d6\3\2\2\2\u01ef\u01d9\3\2\2\2\u01ef"+
		"\u01db\3\2\2\2\u01ef\u01dd\3\2\2\2\u01ef\u01df\3\2\2\2\u01ef\u01e1\3\2"+
		"\2\2\u01ef\u01e8\3\2\2\2\u01f0\u01f3\3\2\2\2\u01f1\u01ef\3\2\2\2\u01f1"+
		"\u01f2\3\2\2\2\u01f2C\3\2\2\2\u01f3\u01f1\3\2\2\2\u01f4\u01f5\7_\2\2\u01f5"+
		"E\3\2\2\2\u01f6\u01f7\t\r\2\2\u01f7G\3\2\2\2\u01f8\u01f9\7!\2\2\u01f9"+
		"I\3\2\2\2\u01fa\u01fb\7\r\2\2\u01fbK\3\2\2\2)Ocgls|\u0087\u0091\u0098"+
		"\u00a1\u00a5\u00c0\u00c8\u00d1\u00d9\u00e5\u00f1\u00f8\u00fe\u0105\u010f"+
		"\u0116\u011e\u0125\u0130\u0137\u013c\u0142\u0149\u0150\u0158\u018c\u0190"+
		"\u0194\u019a\u01e6\u01ed\u01ef\u01f1";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}