// Generated from ini.g4 by ANTLR 4.9.3
package org.qdl_lang.ini_generated;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.*;
import org.antlr.v4.runtime.tree.*;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class iniParser extends Parser {
	static { RuntimeMetaData.checkVersion("4.9.3", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, ConstantKeywords=4, UnaryMinus=5, UnaryPlus=6, 
		Plus=7, Minus=8, Colon=9, Divide=10, Dot=11, Assign=12, Semicolon=13, 
		String=14, Identifier=15, Url=16, BOOL_FALSE=17, BOOL_TRUE=18, Bool=19, 
		Number=20, Integer=21, Decimal=22, SCIENTIFIC_NUMBER=23, LINE_COMMENT=24, 
		COMMENT=25, EOL=26, WS=27;
	public static final int
		RULE_ini = 0, RULE_section = 1, RULE_sectionheader = 2, RULE_line = 3, 
		RULE_entries = 4, RULE_entry = 5;
	private static String[] makeRuleNames() {
		return new String[] {
			"ini", "section", "sectionheader", "line", "entries", "entry"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'['", "']'", "','", null, "'\u00AF'", "'\u207A'", "'+'", "'-'", 
			"':'", "'/'", "'.'", null, "';'", null, null, null, "'false'", "'true'"
		};
	}
	private static final String[] _LITERAL_NAMES = makeLiteralNames();
	private static String[] makeSymbolicNames() {
		return new String[] {
			null, null, null, null, "ConstantKeywords", "UnaryMinus", "UnaryPlus", 
			"Plus", "Minus", "Colon", "Divide", "Dot", "Assign", "Semicolon", "String", 
			"Identifier", "Url", "BOOL_FALSE", "BOOL_TRUE", "Bool", "Number", "Integer", 
			"Decimal", "SCIENTIFIC_NUMBER", "LINE_COMMENT", "COMMENT", "EOL", "WS"
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
	public String getGrammarFileName() { return "ini.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public ATN getATN() { return _ATN; }

	public iniParser(TokenStream input) {
		super(input);
		_interp = new ParserATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	public static class IniContext extends ParserRuleContext {
		public List<SectionContext> section() {
			return getRuleContexts(SectionContext.class);
		}
		public SectionContext section(int i) {
			return getRuleContext(SectionContext.class,i);
		}
		public List<TerminalNode> EOL() { return getTokens(iniParser.EOL); }
		public TerminalNode EOL(int i) {
			return getToken(iniParser.EOL, i);
		}
		public IniContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_ini; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof iniListener ) ((iniListener)listener).enterIni(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof iniListener ) ((iniListener)listener).exitIni(this);
		}
	}

	public final IniContext ini() throws RecognitionException {
		IniContext _localctx = new IniContext(_ctx, getState());
		enterRule(_localctx, 0, RULE_ini);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(16);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__0 || _la==EOL) {
				{
				setState(14);
				_errHandler.sync(this);
				switch (_input.LA(1)) {
				case T__0:
					{
					setState(12);
					section();
					}
					break;
				case EOL:
					{
					setState(13);
					match(EOL);
					}
					break;
				default:
					throw new NoViableAltException(this);
				}
				}
				setState(18);
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

	public static class SectionContext extends ParserRuleContext {
		public SectionheaderContext sectionheader() {
			return getRuleContext(SectionheaderContext.class,0);
		}
		public List<LineContext> line() {
			return getRuleContexts(LineContext.class);
		}
		public LineContext line(int i) {
			return getRuleContext(LineContext.class,i);
		}
		public SectionContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_section; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof iniListener ) ((iniListener)listener).enterSection(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof iniListener ) ((iniListener)listener).exitSection(this);
		}
	}

	public final SectionContext section() throws RecognitionException {
		SectionContext _localctx = new SectionContext(_ctx, getState());
		enterRule(_localctx, 2, RULE_section);
		try {
			int _alt;
			enterOuterAlt(_localctx, 1);
			{
			setState(19);
			sectionheader();
			setState(23);
			_errHandler.sync(this);
			_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
			while ( _alt!=2 && _alt!=org.antlr.v4.runtime.atn.ATN.INVALID_ALT_NUMBER ) {
				if ( _alt==1 ) {
					{
					{
					setState(20);
					line();
					}
					} 
				}
				setState(25);
				_errHandler.sync(this);
				_alt = getInterpreter().adaptivePredict(_input,2,_ctx);
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

	public static class SectionheaderContext extends ParserRuleContext {
		public List<TerminalNode> Identifier() { return getTokens(iniParser.Identifier); }
		public TerminalNode Identifier(int i) {
			return getToken(iniParser.Identifier, i);
		}
		public TerminalNode EOL() { return getToken(iniParser.EOL, 0); }
		public List<TerminalNode> Dot() { return getTokens(iniParser.Dot); }
		public TerminalNode Dot(int i) {
			return getToken(iniParser.Dot, i);
		}
		public SectionheaderContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_sectionheader; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof iniListener ) ((iniListener)listener).enterSectionheader(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof iniListener ) ((iniListener)listener).exitSectionheader(this);
		}
	}

	public final SectionheaderContext sectionheader() throws RecognitionException {
		SectionheaderContext _localctx = new SectionheaderContext(_ctx, getState());
		enterRule(_localctx, 4, RULE_sectionheader);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(26);
			match(T__0);
			setState(27);
			match(Identifier);
			setState(32);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==Dot) {
				{
				{
				setState(28);
				match(Dot);
				setState(29);
				match(Identifier);
				}
				}
				setState(34);
				_errHandler.sync(this);
				_la = _input.LA(1);
			}
			setState(35);
			match(T__1);
			setState(36);
			match(EOL);
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

	public static class LineContext extends ParserRuleContext {
		public TerminalNode EOL() { return getToken(iniParser.EOL, 0); }
		public TerminalNode Url() { return getToken(iniParser.Url, 0); }
		public TerminalNode Identifier() { return getToken(iniParser.Identifier, 0); }
		public TerminalNode Assign() { return getToken(iniParser.Assign, 0); }
		public EntriesContext entries() {
			return getRuleContext(EntriesContext.class,0);
		}
		public TerminalNode Semicolon() { return getToken(iniParser.Semicolon, 0); }
		public LineContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_line; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof iniListener ) ((iniListener)listener).enterLine(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof iniListener ) ((iniListener)listener).exitLine(this);
		}
	}

	public final LineContext line() throws RecognitionException {
		LineContext _localctx = new LineContext(_ctx, getState());
		enterRule(_localctx, 6, RULE_line);
		int _la;
		try {
			setState(48);
			_errHandler.sync(this);
			switch (_input.LA(1)) {
			case Identifier:
			case Url:
				enterOuterAlt(_localctx, 1);
				{
				{
				setState(38);
				_la = _input.LA(1);
				if ( !(_la==Identifier || _la==Url) ) {
				_errHandler.recoverInline(this);
				}
				else {
					if ( _input.LA(1)==Token.EOF ) matchedEOF = true;
					_errHandler.reportMatch(this);
					consume();
				}
				{
				setState(39);
				match(Assign);
				setState(40);
				entries();
				}
				setState(43);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if (_la==Semicolon) {
					{
					setState(42);
					match(Semicolon);
					}
				}

				setState(45);
				match(EOL);
				}
				}
				break;
			case EOL:
				enterOuterAlt(_localctx, 2);
				{
				setState(47);
				match(EOL);
				}
				break;
			default:
				throw new NoViableAltException(this);
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

	public static class EntriesContext extends ParserRuleContext {
		public List<EntryContext> entry() {
			return getRuleContexts(EntryContext.class);
		}
		public EntryContext entry(int i) {
			return getRuleContext(EntryContext.class,i);
		}
		public EntriesContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_entries; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof iniListener ) ((iniListener)listener).enterEntries(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof iniListener ) ((iniListener)listener).exitEntries(this);
		}
	}

	public final EntriesContext entries() throws RecognitionException {
		EntriesContext _localctx = new EntriesContext(_ctx, getState());
		enterRule(_localctx, 8, RULE_entries);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(50);
			entry();
			setState(57);
			_errHandler.sync(this);
			_la = _input.LA(1);
			while (_la==T__2) {
				{
				{
				setState(51);
				match(T__2);
				setState(53);
				_errHandler.sync(this);
				_la = _input.LA(1);
				if ((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ConstantKeywords) | (1L << String) | (1L << Number))) != 0)) {
					{
					setState(52);
					entry();
					}
				}

				}
				}
				setState(59);
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

	public static class EntryContext extends ParserRuleContext {
		public TerminalNode ConstantKeywords() { return getToken(iniParser.ConstantKeywords, 0); }
		public TerminalNode Number() { return getToken(iniParser.Number, 0); }
		public TerminalNode String() { return getToken(iniParser.String, 0); }
		public EntryContext(ParserRuleContext parent, int invokingState) {
			super(parent, invokingState);
		}
		@Override public int getRuleIndex() { return RULE_entry; }
		@Override
		public void enterRule(ParseTreeListener listener) {
			if ( listener instanceof iniListener ) ((iniListener)listener).enterEntry(this);
		}
		@Override
		public void exitRule(ParseTreeListener listener) {
			if ( listener instanceof iniListener ) ((iniListener)listener).exitEntry(this);
		}
	}

	public final EntryContext entry() throws RecognitionException {
		EntryContext _localctx = new EntryContext(_ctx, getState());
		enterRule(_localctx, 10, RULE_entry);
		int _la;
		try {
			enterOuterAlt(_localctx, 1);
			{
			setState(60);
			_la = _input.LA(1);
			if ( !((((_la) & ~0x3f) == 0 && ((1L << _la) & ((1L << ConstantKeywords) | (1L << String) | (1L << Number))) != 0)) ) {
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

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\3\35A\4\2\t\2\4\3\t"+
		"\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\3\2\3\2\7\2\21\n\2\f\2\16\2\24\13\2"+
		"\3\3\3\3\7\3\30\n\3\f\3\16\3\33\13\3\3\4\3\4\3\4\3\4\7\4!\n\4\f\4\16\4"+
		"$\13\4\3\4\3\4\3\4\3\5\3\5\3\5\3\5\3\5\5\5.\n\5\3\5\3\5\3\5\5\5\63\n\5"+
		"\3\6\3\6\3\6\5\68\n\6\7\6:\n\6\f\6\16\6=\13\6\3\7\3\7\3\7\2\2\b\2\4\6"+
		"\b\n\f\2\4\3\2\21\22\5\2\6\6\20\20\26\26\2B\2\22\3\2\2\2\4\25\3\2\2\2"+
		"\6\34\3\2\2\2\b\62\3\2\2\2\n\64\3\2\2\2\f>\3\2\2\2\16\21\5\4\3\2\17\21"+
		"\7\34\2\2\20\16\3\2\2\2\20\17\3\2\2\2\21\24\3\2\2\2\22\20\3\2\2\2\22\23"+
		"\3\2\2\2\23\3\3\2\2\2\24\22\3\2\2\2\25\31\5\6\4\2\26\30\5\b\5\2\27\26"+
		"\3\2\2\2\30\33\3\2\2\2\31\27\3\2\2\2\31\32\3\2\2\2\32\5\3\2\2\2\33\31"+
		"\3\2\2\2\34\35\7\3\2\2\35\"\7\21\2\2\36\37\7\r\2\2\37!\7\21\2\2 \36\3"+
		"\2\2\2!$\3\2\2\2\" \3\2\2\2\"#\3\2\2\2#%\3\2\2\2$\"\3\2\2\2%&\7\4\2\2"+
		"&\'\7\34\2\2\'\7\3\2\2\2()\t\2\2\2)*\7\16\2\2*+\5\n\6\2+-\3\2\2\2,.\7"+
		"\17\2\2-,\3\2\2\2-.\3\2\2\2./\3\2\2\2/\60\7\34\2\2\60\63\3\2\2\2\61\63"+
		"\7\34\2\2\62(\3\2\2\2\62\61\3\2\2\2\63\t\3\2\2\2\64;\5\f\7\2\65\67\7\5"+
		"\2\2\668\5\f\7\2\67\66\3\2\2\2\678\3\2\2\28:\3\2\2\29\65\3\2\2\2:=\3\2"+
		"\2\2;9\3\2\2\2;<\3\2\2\2<\13\3\2\2\2=;\3\2\2\2>?\t\3\2\2?\r\3\2\2\2\n"+
		"\20\22\31\"-\62\67;";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}