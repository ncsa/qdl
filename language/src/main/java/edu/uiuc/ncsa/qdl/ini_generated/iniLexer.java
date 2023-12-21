// Generated from ini.g4 by ANTLR 4.9.1
package edu.uiuc.ncsa.qdl.ini_generated;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class iniLexer extends Lexer {
	static { RuntimeMetaData.checkVersion("4.9.1", RuntimeMetaData.VERSION); }

	protected static final DFA[] _decisionToDFA;
	protected static final PredictionContextCache _sharedContextCache =
		new PredictionContextCache();
	public static final int
		T__0=1, T__1=2, T__2=3, ConstantKeywords=4, UnaryMinus=5, UnaryPlus=6, 
		Plus=7, Minus=8, Colon=9, Divide=10, Dot=11, Assign=12, Semicolon=13, 
		String=14, Identifier=15, Url=16, BOOL_FALSE=17, BOOL_TRUE=18, Bool=19, 
		Number=20, Integer=21, Decimal=22, SCIENTIFIC_NUMBER=23, LINE_COMMENT=24, 
		COMMENT=25, EOL=26, WS=27;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "ConstantKeywords", "UnaryMinus", "UnaryPlus", 
			"Plus", "Minus", "Colon", "Divide", "Dot", "Assign", "Semicolon", "String", 
			"ESC", "UnicodeEscape", "HexDigit", "StringCharacters", "StringCharacter", 
			"Identifier", "Url", "BOOL_FALSE", "BOOL_TRUE", "Bool", "Number", "Integer", 
			"Decimal", "SCIENTIFIC_NUMBER", "E", "SIGN", "LINE_COMMENT", "COMMENT", 
			"EOL", "WS"
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


	public iniLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "ini.g4"; }

	@Override
	public String[] getRuleNames() { return ruleNames; }

	@Override
	public String getSerializedATN() { return _serializedATN; }

	@Override
	public String[] getChannelNames() { return channelNames; }

	@Override
	public String[] getModeNames() { return modeNames; }

	@Override
	public ATN getATN() { return _ATN; }

	public static final String _serializedATN =
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2\35\u0101\b\1\4\2"+
		"\t\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4"+
		"\13\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22"+
		"\t\22\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31"+
		"\t\31\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t"+
		" \4!\t!\4\"\t\"\4#\t#\3\2\3\2\3\3\3\3\3\4\3\4\3\5\3\5\5\5P\n\5\3\6\3\6"+
		"\3\7\3\7\3\b\3\b\3\t\3\t\3\n\3\n\3\13\3\13\3\f\3\f\3\r\3\r\3\r\3\r\5\r"+
		"d\n\r\3\16\3\16\3\17\3\17\5\17j\n\17\3\17\3\17\3\20\3\20\3\20\5\20q\n"+
		"\20\3\21\3\21\6\21u\n\21\r\21\16\21v\3\21\3\21\3\21\3\21\3\21\3\22\3\22"+
		"\3\23\6\23\u0081\n\23\r\23\16\23\u0082\3\24\3\24\5\24\u0087\n\24\3\25"+
		"\3\25\7\25\u008b\n\25\f\25\16\25\u008e\13\25\3\26\3\26\3\26\6\26\u0093"+
		"\n\26\r\26\16\26\u0094\3\26\3\26\5\26\u0099\n\26\3\26\3\26\7\26\u009d"+
		"\n\26\f\26\16\26\u00a0\13\26\3\26\3\26\6\26\u00a4\n\26\r\26\16\26\u00a5"+
		"\3\27\3\27\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3\30\3\30\3\31\3\31\5\31"+
		"\u00b5\n\31\3\32\5\32\u00b8\n\32\3\32\3\32\3\32\5\32\u00bd\n\32\3\33\6"+
		"\33\u00c0\n\33\r\33\16\33\u00c1\3\34\5\34\u00c5\n\34\3\34\3\34\3\34\3"+
		"\35\5\35\u00cb\n\35\3\35\3\35\3\35\5\35\u00d0\n\35\3\35\3\35\5\35\u00d4"+
		"\n\35\3\36\3\36\3\37\3\37\3\37\3\37\5\37\u00dc\n\37\3 \3 \3 \3 \7 \u00e2"+
		"\n \f \16 \u00e5\13 \3 \3 \3 \3 \3!\3!\3!\3!\7!\u00ef\n!\f!\16!\u00f2"+
		"\13!\3!\3!\3!\3!\3!\3\"\3\"\3#\6#\u00fc\n#\r#\16#\u00fd\3#\3#\3\u00f0"+
		"\2$\3\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35"+
		"\20\37\2!\2#\2%\2\'\2)\21+\22-\23/\24\61\25\63\26\65\27\67\309\31;\2="+
		"\2?\32A\33C\34E\35\3\2\13\t\2))^^ddhhppttvv\5\2\62;CHch\6\2\f\f\17\17"+
		"))^^\f\2&&\61\61B\\aac|\u0393\u03ab\u03b3\u03cb\u03d3\u03d3\u03d8\u03d8"+
		"\u03f2\u03f3\r\2&&//\61;B\\aac|\u0393\u03ab\u03b3\u03cb\u03d3\u03d3\u03d8"+
		"\u03d8\u03f2\u03f3\3\2\62;\4\2GGgg\4\2\f\f\17\17\5\2\13\13\16\16\"\"\2"+
		"\u0116\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2\2\2\13\3\2\2\2\2"+
		"\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25\3\2\2\2\2\27\3"+
		"\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2)\3\2\2\2\2+\3\2\2\2\2"+
		"-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3\2\2"+
		"\2\29\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2\2\2E\3\2\2\2\3G\3\2\2\2\5"+
		"I\3\2\2\2\7K\3\2\2\2\tO\3\2\2\2\13Q\3\2\2\2\rS\3\2\2\2\17U\3\2\2\2\21"+
		"W\3\2\2\2\23Y\3\2\2\2\25[\3\2\2\2\27]\3\2\2\2\31c\3\2\2\2\33e\3\2\2\2"+
		"\35g\3\2\2\2\37p\3\2\2\2!r\3\2\2\2#}\3\2\2\2%\u0080\3\2\2\2\'\u0086\3"+
		"\2\2\2)\u0088\3\2\2\2+\u0092\3\2\2\2-\u00a7\3\2\2\2/\u00ad\3\2\2\2\61"+
		"\u00b4\3\2\2\2\63\u00bc\3\2\2\2\65\u00bf\3\2\2\2\67\u00c4\3\2\2\29\u00ca"+
		"\3\2\2\2;\u00d5\3\2\2\2=\u00db\3\2\2\2?\u00dd\3\2\2\2A\u00ea\3\2\2\2C"+
		"\u00f8\3\2\2\2E\u00fb\3\2\2\2GH\7]\2\2H\4\3\2\2\2IJ\7_\2\2J\6\3\2\2\2"+
		"KL\7.\2\2L\b\3\2\2\2MP\5/\30\2NP\5-\27\2OM\3\2\2\2ON\3\2\2\2P\n\3\2\2"+
		"\2QR\7\u00b1\2\2R\f\3\2\2\2ST\7\u207c\2\2T\16\3\2\2\2UV\7-\2\2V\20\3\2"+
		"\2\2WX\7/\2\2X\22\3\2\2\2YZ\7<\2\2Z\24\3\2\2\2[\\\7\61\2\2\\\26\3\2\2"+
		"\2]^\7\60\2\2^\30\3\2\2\2_d\7?\2\2`a\7<\2\2ad\7?\2\2bd\7\u2256\2\2c_\3"+
		"\2\2\2c`\3\2\2\2cb\3\2\2\2d\32\3\2\2\2ef\7=\2\2f\34\3\2\2\2gi\7)\2\2h"+
		"j\5%\23\2ih\3\2\2\2ij\3\2\2\2jk\3\2\2\2kl\7)\2\2l\36\3\2\2\2mn\7^\2\2"+
		"nq\t\2\2\2oq\5!\21\2pm\3\2\2\2po\3\2\2\2q \3\2\2\2rt\7^\2\2su\7w\2\2t"+
		"s\3\2\2\2uv\3\2\2\2vt\3\2\2\2vw\3\2\2\2wx\3\2\2\2xy\5#\22\2yz\5#\22\2"+
		"z{\5#\22\2{|\5#\22\2|\"\3\2\2\2}~\t\3\2\2~$\3\2\2\2\177\u0081\5\'\24\2"+
		"\u0080\177\3\2\2\2\u0081\u0082\3\2\2\2\u0082\u0080\3\2\2\2\u0082\u0083"+
		"\3\2\2\2\u0083&\3\2\2\2\u0084\u0087\n\4\2\2\u0085\u0087\5\37\20\2\u0086"+
		"\u0084\3\2\2\2\u0086\u0085\3\2\2\2\u0087(\3\2\2\2\u0088\u008c\t\5\2\2"+
		"\u0089\u008b\t\6\2\2\u008a\u0089\3\2\2\2\u008b\u008e\3\2\2\2\u008c\u008a"+
		"\3\2\2\2\u008c\u008d\3\2\2\2\u008d*\3\2\2\2\u008e\u008c\3\2\2\2\u008f"+
		"\u0090\5)\25\2\u0090\u0091\5\23\n\2\u0091\u0093\3\2\2\2\u0092\u008f\3"+
		"\2\2\2\u0093\u0094\3\2\2\2\u0094\u0092\3\2\2\2\u0094\u0095\3\2\2\2\u0095"+
		"\u009e\3\2\2\2\u0096\u0099\5\65\33\2\u0097\u0099\5)\25\2\u0098\u0096\3"+
		"\2\2\2\u0098\u0097\3\2\2\2\u0099\u009a\3\2\2\2\u009a\u009b\5\25\13\2\u009b"+
		"\u009d\3\2\2\2\u009c\u0098\3\2\2\2\u009d\u00a0\3\2\2\2\u009e\u009c\3\2"+
		"\2\2\u009e\u009f\3\2\2\2\u009f\u00a3\3\2\2\2\u00a0\u009e\3\2\2\2\u00a1"+
		"\u00a4\5\65\33\2\u00a2\u00a4\5)\25\2\u00a3\u00a1\3\2\2\2\u00a3\u00a2\3"+
		"\2\2\2\u00a4\u00a5\3\2\2\2\u00a5\u00a3\3\2\2\2\u00a5\u00a6\3\2\2\2\u00a6"+
		",\3\2\2\2\u00a7\u00a8\7h\2\2\u00a8\u00a9\7c\2\2\u00a9\u00aa\7n\2\2\u00aa"+
		"\u00ab\7u\2\2\u00ab\u00ac\7g\2\2\u00ac.\3\2\2\2\u00ad\u00ae\7v\2\2\u00ae"+
		"\u00af\7t\2\2\u00af\u00b0\7w\2\2\u00b0\u00b1\7g\2\2\u00b1\60\3\2\2\2\u00b2"+
		"\u00b5\5/\30\2\u00b3\u00b5\5-\27\2\u00b4\u00b2\3\2\2\2\u00b4\u00b3\3\2"+
		"\2\2\u00b5\62\3\2\2\2\u00b6\u00b8\5=\37\2\u00b7\u00b6\3\2\2\2\u00b7\u00b8"+
		"\3\2\2\2\u00b8\u00b9\3\2\2\2\u00b9\u00bd\5\65\33\2\u00ba\u00bd\5\67\34"+
		"\2\u00bb\u00bd\59\35\2\u00bc\u00b7\3\2\2\2\u00bc\u00ba\3\2\2\2\u00bc\u00bb"+
		"\3\2\2\2\u00bd\64\3\2\2\2\u00be\u00c0\t\7\2\2\u00bf\u00be\3\2\2\2\u00c0"+
		"\u00c1\3\2\2\2\u00c1\u00bf\3\2\2\2\u00c1\u00c2\3\2\2\2\u00c2\66\3\2\2"+
		"\2\u00c3\u00c5\5\65\33\2\u00c4\u00c3\3\2\2\2\u00c4\u00c5\3\2\2\2\u00c5"+
		"\u00c6\3\2\2\2\u00c6\u00c7\7\60\2\2\u00c7\u00c8\5\65\33\2\u00c88\3\2\2"+
		"\2\u00c9\u00cb\5=\37\2\u00ca\u00c9\3\2\2\2\u00ca\u00cb\3\2\2\2\u00cb\u00cc"+
		"\3\2\2\2\u00cc\u00d3\5\67\34\2\u00cd\u00cf\5;\36\2\u00ce\u00d0\5=\37\2"+
		"\u00cf\u00ce\3\2\2\2\u00cf\u00d0\3\2\2\2\u00d0\u00d1\3\2\2\2\u00d1\u00d2"+
		"\5\65\33\2\u00d2\u00d4\3\2\2\2\u00d3\u00cd\3\2\2\2\u00d3\u00d4\3\2\2\2"+
		"\u00d4:\3\2\2\2\u00d5\u00d6\t\b\2\2\u00d6<\3\2\2\2\u00d7\u00dc\5\17\b"+
		"\2\u00d8\u00dc\5\r\7\2\u00d9\u00dc\5\21\t\2\u00da\u00dc\5\13\6\2\u00db"+
		"\u00d7\3\2\2\2\u00db\u00d8\3\2\2\2\u00db\u00d9\3\2\2\2\u00db\u00da\3\2"+
		"\2\2\u00dc>\3\2\2\2\u00dd\u00de\7\61\2\2\u00de\u00df\7\61\2\2\u00df\u00e3"+
		"\3\2\2\2\u00e0\u00e2\n\t\2\2\u00e1\u00e0\3\2\2\2\u00e2\u00e5\3\2\2\2\u00e3"+
		"\u00e1\3\2\2\2\u00e3\u00e4\3\2\2\2\u00e4\u00e6\3\2\2\2\u00e5\u00e3\3\2"+
		"\2\2\u00e6\u00e7\5C\"\2\u00e7\u00e8\3\2\2\2\u00e8\u00e9\b \2\2\u00e9@"+
		"\3\2\2\2\u00ea\u00eb\7\61\2\2\u00eb\u00ec\7,\2\2\u00ec\u00f0\3\2\2\2\u00ed"+
		"\u00ef\13\2\2\2\u00ee\u00ed\3\2\2\2\u00ef\u00f2\3\2\2\2\u00f0\u00f1\3"+
		"\2\2\2\u00f0\u00ee\3\2\2\2\u00f1\u00f3\3\2\2\2\u00f2\u00f0\3\2\2\2\u00f3"+
		"\u00f4\7,\2\2\u00f4\u00f5\7\61\2\2\u00f5\u00f6\3\2\2\2\u00f6\u00f7\b!"+
		"\2\2\u00f7B\3\2\2\2\u00f8\u00f9\t\t\2\2\u00f9D\3\2\2\2\u00fa\u00fc\t\n"+
		"\2\2\u00fb\u00fa\3\2\2\2\u00fc\u00fd\3\2\2\2\u00fd\u00fb\3\2\2\2\u00fd"+
		"\u00fe\3\2\2\2\u00fe\u00ff\3\2\2\2\u00ff\u0100\b#\2\2\u0100F\3\2\2\2\34"+
		"\2Ocipv\u0082\u0086\u008c\u0094\u0098\u009e\u00a3\u00a5\u00b4\u00b7\u00bc"+
		"\u00c1\u00c4\u00ca\u00cf\u00d3\u00db\u00e3\u00f0\u00fd\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}