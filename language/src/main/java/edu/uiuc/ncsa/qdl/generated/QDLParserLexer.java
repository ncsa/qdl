// Generated from QDLParser.g4 by ANTLR 4.9.1
package edu.uiuc.ncsa.qdl.generated;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.atn.*;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.*;

@SuppressWarnings({"all", "warnings", "unchecked", "unused", "cast"})
public class QDLParserLexer extends Lexer {
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
		IsDefined=64, ForAll=65, ContainsKey=66, Exponentiation=67, ExprDyadicOps=68, 
		FRefDyadicOps=69, And=70, Or=71, Backtick=72, Percent=73, Tilde=74, Backslash=75, 
		Backslash2=76, Backslash3=77, Backslash4=78, Hash=79, Stile=80, TildeRight=81, 
		StemDot=82, UnaryMinus=83, UnaryPlus=84, Floor=85, Ceiling=86, FunctionMarker=87, 
		AltIfMarker=88, ASSIGN=89, Identifier=90, FuncStart=91, F_REF=92, FDOC=93, 
		WS=94, COMMENT=95, LINE_COMMENT=96;
	public static String[] channelNames = {
		"DEFAULT_TOKEN_CHANNEL", "HIDDEN"
	};

	public static String[] modeNames = {
		"DEFAULT_MODE"
	};

	private static String[] makeRuleNames() {
		return new String[] {
			"T__0", "T__1", "T__2", "T__3", "T__4", "T__5", "T__6", "T__7", "T__8", 
			"T__9", "ConstantKeywords", "ASSERT", "ASSERT2", "BOOL_FALSE", "BOOL_TRUE", 
			"BLOCK", "LOCAL", "BODY", "CATCH", "DEFINE", "DO", "ELSE", "IF", "MODULE", 
			"Null", "Null_Set", "SWITCH", "THEN", "TRY", "WHILE", "Integer", "Decimal", 
			"SCIENTIFIC_NUMBER", "E", "SIGN", "Bool", "STRING", "ESC", "UnicodeEscape", 
			"HexDigit", "StringCharacters", "StringCharacter", "LeftBracket", "RightBracket", 
			"Comma", "Colon", "SemiColon", "LDoubleBracket", "RDoubleBracket", "LambdaConnector", 
			"Times", "Divide", "PlusPlus", "Plus", "MinusMinus", "Minus", "LessThan", 
			"GreaterThan", "SingleEqual", "DoubleQuote", "SingleQuote", "To_Set", 
			"LessEquals", "MoreEquals", "IsA", "Equals", "NotEquals", "RegexMatches", 
			"LogicalNot", "Membership", "IsDefined", "ForAll", "ContainsKey", "Exponentiation", 
			"ExprDyadicOps", "FRefDyadicOps", "And", "Or", "Backtick", "Percent", 
			"Tilde", "Backslash", "Backslash2", "Backslash3", "Backslash4", "Hash", 
			"Stile", "TildeRight", "StemDot", "UnaryMinus", "UnaryPlus", "Floor", 
			"Ceiling", "FunctionMarker", "AltIfMarker", "ASSIGN", "Identifier", "FuncStart", 
			"F_REF", "AllOps", "FUNCTION_NAME", "FDOC", "WS", "COMMENT", "LINE_COMMENT"
		};
	}
	public static final String[] ruleNames = makeRuleNames();

	private static String[] makeLiteralNames() {
		return new String[] {
			null, "'{'", "'}'", "')'", "'('", "'\\/'", "'\u2229'", "'/\\'", "'\u222A'", 
			"'!'", "'\u00AC'", null, "'assert'", "'\u22A8'", "'false'", "'true'", 
			"'block'", "'local'", "'body'", "'catch'", "'define'", "'do'", "'else'", 
			"'if'", "'module'", "'null'", "'\u2205'", "'switch'", "'then'", "'try'", 
			"'while'", null, null, null, null, null, "'['", "']'", "','", "':'", 
			"';'", null, null, null, null, null, "'++'", "'+'", "'--'", "'-'", "'<'", 
			"'>'", "'='", "'\"'", "'''", null, null, null, "'<<'", null, null, null, 
			null, null, null, "'\u2200'", null, "'^'", null, null, null, null, "'`'", 
			null, "'~'", null, null, null, null, "'#'", "'|'", null, "'.'", "'\u00AF'", 
			"'\u207A'", "'\u230A'", "'\u2308'"
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
			"ExprDyadicOps", "FRefDyadicOps", "And", "Or", "Backtick", "Percent", 
			"Tilde", "Backslash", "Backslash2", "Backslash3", "Backslash4", "Hash", 
			"Stile", "TildeRight", "StemDot", "UnaryMinus", "UnaryPlus", "Floor", 
			"Ceiling", "FunctionMarker", "AltIfMarker", "ASSIGN", "Identifier", "FuncStart", 
			"F_REF", "FDOC", "WS", "COMMENT", "LINE_COMMENT"
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


	public QDLParserLexer(CharStream input) {
		super(input);
		_interp = new LexerATNSimulator(this,_ATN,_decisionToDFA,_sharedContextCache);
	}

	@Override
	public String getGrammarFileName() { return "QDLParser.g4"; }

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
		"\3\u608b\ua72a\u8133\ub9ed\u417c\u3be7\u7786\u5964\2b\u02b1\b\1\4\2\t"+
		"\2\4\3\t\3\4\4\t\4\4\5\t\5\4\6\t\6\4\7\t\7\4\b\t\b\4\t\t\t\4\n\t\n\4\13"+
		"\t\13\4\f\t\f\4\r\t\r\4\16\t\16\4\17\t\17\4\20\t\20\4\21\t\21\4\22\t\22"+
		"\4\23\t\23\4\24\t\24\4\25\t\25\4\26\t\26\4\27\t\27\4\30\t\30\4\31\t\31"+
		"\4\32\t\32\4\33\t\33\4\34\t\34\4\35\t\35\4\36\t\36\4\37\t\37\4 \t \4!"+
		"\t!\4\"\t\"\4#\t#\4$\t$\4%\t%\4&\t&\4\'\t\'\4(\t(\4)\t)\4*\t*\4+\t+\4"+
		",\t,\4-\t-\4.\t.\4/\t/\4\60\t\60\4\61\t\61\4\62\t\62\4\63\t\63\4\64\t"+
		"\64\4\65\t\65\4\66\t\66\4\67\t\67\48\t8\49\t9\4:\t:\4;\t;\4<\t<\4=\t="+
		"\4>\t>\4?\t?\4@\t@\4A\tA\4B\tB\4C\tC\4D\tD\4E\tE\4F\tF\4G\tG\4H\tH\4I"+
		"\tI\4J\tJ\4K\tK\4L\tL\4M\tM\4N\tN\4O\tO\4P\tP\4Q\tQ\4R\tR\4S\tS\4T\tT"+
		"\4U\tU\4V\tV\4W\tW\4X\tX\4Y\tY\4Z\tZ\4[\t[\4\\\t\\\4]\t]\4^\t^\4_\t_\4"+
		"`\t`\4a\ta\4b\tb\4c\tc\4d\td\4e\te\4f\tf\4g\tg\4h\th\4i\ti\4j\tj\3\2\3"+
		"\2\3\3\3\3\3\4\3\4\3\5\3\5\3\6\3\6\3\6\3\7\3\7\3\b\3\b\3\b\3\t\3\t\3\n"+
		"\3\n\3\13\3\13\3\f\3\f\3\f\3\f\5\f\u00f0\n\f\3\r\3\r\3\r\3\r\3\r\3\r\3"+
		"\r\3\16\3\16\3\17\3\17\3\17\3\17\3\17\3\17\3\20\3\20\3\20\3\20\3\20\3"+
		"\21\3\21\3\21\3\21\3\21\3\21\3\22\3\22\3\22\3\22\3\22\3\22\3\23\3\23\3"+
		"\23\3\23\3\23\3\24\3\24\3\24\3\24\3\24\3\24\3\25\3\25\3\25\3\25\3\25\3"+
		"\25\3\25\3\26\3\26\3\26\3\27\3\27\3\27\3\27\3\27\3\30\3\30\3\30\3\31\3"+
		"\31\3\31\3\31\3\31\3\31\3\31\3\32\3\32\3\32\3\32\3\32\3\33\3\33\3\34\3"+
		"\34\3\34\3\34\3\34\3\34\3\34\3\35\3\35\3\35\3\35\3\35\3\36\3\36\3\36\3"+
		"\36\3\37\3\37\3\37\3\37\3\37\3\37\3 \6 \u0154\n \r \16 \u0155\3!\3!\3"+
		"!\3!\3\"\3\"\3\"\5\"\u015f\n\"\3\"\3\"\5\"\u0163\n\"\3#\3#\3$\3$\3$\3"+
		"$\5$\u016b\n$\3%\3%\5%\u016f\n%\3&\3&\5&\u0173\n&\3&\3&\3\'\3\'\3\'\5"+
		"\'\u017a\n\'\3(\3(\6(\u017e\n(\r(\16(\u017f\3(\3(\3(\3(\3(\3)\3)\3*\6"+
		"*\u018a\n*\r*\16*\u018b\3+\3+\5+\u0190\n+\3,\3,\3-\3-\3.\3.\3/\3/\3\60"+
		"\3\60\3\61\3\61\3\61\5\61\u019f\n\61\3\62\3\62\3\62\5\62\u01a4\n\62\3"+
		"\63\3\63\3\63\5\63\u01a9\n\63\3\64\3\64\3\65\3\65\3\66\3\66\3\66\3\67"+
		"\3\67\38\38\38\39\39\3:\3:\3;\3;\3<\3<\3=\3=\3>\3>\3?\3?\3?\5?\u01c6\n"+
		"?\3@\3@\3@\5@\u01cb\n@\3A\3A\3A\5A\u01d0\nA\3B\3B\3B\3C\3C\3C\5C\u01d8"+
		"\nC\3D\3D\3D\5D\u01dd\nD\3E\3E\3E\5E\u01e2\nE\3F\3F\3G\3G\3H\3H\3I\3I"+
		"\3J\3J\3K\3K\3L\3L\3M\3M\3N\3N\3N\5N\u01f7\nN\3O\3O\3O\5O\u01fc\nO\3P"+
		"\3P\3Q\3Q\3R\3R\3S\3S\3S\5S\u0207\nS\3T\3T\3T\3T\3T\5T\u020e\nT\3U\3U"+
		"\3U\3U\3U\5U\u0215\nU\3V\3V\3V\3V\3V\3V\3V\5V\u021e\nV\3W\3W\3X\3X\3Y"+
		"\3Y\3Y\5Y\u0227\nY\3Z\3Z\3[\3[\3\\\3\\\3]\3]\3^\3^\3_\3_\3`\3`\3a\3a\3"+
		"a\3a\3a\3a\3a\3a\3a\3a\3a\3a\3a\3a\3a\3a\3a\3a\3a\3a\5a\u024b\na\3b\3"+
		"b\7b\u024f\nb\fb\16b\u0252\13b\3c\3c\3c\3d\3d\3d\3d\3d\7d\u025c\nd\fd"+
		"\16d\u025f\13d\3d\3d\3d\3d\5d\u0265\nd\3e\3e\3e\3e\3e\3e\3e\3e\3e\3e\3"+
		"e\3e\3e\3e\3e\3e\3e\3e\3e\3e\3e\3e\3e\3e\3e\3e\3e\5e\u0282\ne\3f\3f\7"+
		"f\u0286\nf\ff\16f\u0289\13f\3g\3g\7g\u028d\ng\fg\16g\u0290\13g\3h\6h\u0293"+
		"\nh\rh\16h\u0294\3h\3h\3i\3i\3i\3i\7i\u029d\ni\fi\16i\u02a0\13i\3i\3i"+
		"\3i\3i\3i\3j\3j\3j\3j\7j\u02ab\nj\fj\16j\u02ae\13j\3j\3j\3\u029e\2k\3"+
		"\3\5\4\7\5\t\6\13\7\r\b\17\t\21\n\23\13\25\f\27\r\31\16\33\17\35\20\37"+
		"\21!\22#\23%\24\'\25)\26+\27-\30/\31\61\32\63\33\65\34\67\359\36;\37="+
		" ?!A\"C#E\2G\2I$K%M\2O\2Q\2S\2U\2W&Y\'[(])_*a+c,e-g.i/k\60m\61o\62q\63"+
		"s\64u\65w\66y\67{8}9\177:\u0081;\u0083<\u0085=\u0087>\u0089?\u008b@\u008d"+
		"A\u008fB\u0091C\u0093D\u0095E\u0097F\u0099G\u009bH\u009dI\u009fJ\u00a1"+
		"K\u00a3L\u00a5M\u00a7N\u00a9O\u00abP\u00adQ\u00afR\u00b1S\u00b3T\u00b5"+
		"U\u00b7V\u00b9W\u00bbX\u00bdY\u00bfZ\u00c1[\u00c3\\\u00c5]\u00c7^\u00c9"+
		"\2\u00cb\2\u00cd_\u00cf`\u00d1a\u00d3b\3\2\23\3\2\62;\4\2GGgg\t\2))^^"+
		"ddhhppttvv\5\2\62;CHch\6\2\f\f\17\17))^^\4\2,,\u00d9\u00d9\4\2\61\61\u00f9"+
		"\u00f9\4\2##\u00ae\u00ae\4\2\u2308\u2308\u29b2\u29b2\4\2\u2297\u2297\u229b"+
		"\u229b\4\2\'\'\u2208\u2208\4\2BB\u2299\u2299\4\2AA\u21d4\u21d4\13\2&&"+
		"C\\aac|\u0393\u03ab\u03b3\u03cb\u03d3\u03d3\u03d8\u03d8\u03f2\u03f3\n"+
		"\2&&\62;C\\aac|\u0393\u03ab\u03b3\u03cb\u03d3\u03d3\4\2\f\f\17\17\5\2"+
		"\13\f\16\17\"\"\2\u02f2\2\3\3\2\2\2\2\5\3\2\2\2\2\7\3\2\2\2\2\t\3\2\2"+
		"\2\2\13\3\2\2\2\2\r\3\2\2\2\2\17\3\2\2\2\2\21\3\2\2\2\2\23\3\2\2\2\2\25"+
		"\3\2\2\2\2\27\3\2\2\2\2\31\3\2\2\2\2\33\3\2\2\2\2\35\3\2\2\2\2\37\3\2"+
		"\2\2\2!\3\2\2\2\2#\3\2\2\2\2%\3\2\2\2\2\'\3\2\2\2\2)\3\2\2\2\2+\3\2\2"+
		"\2\2-\3\2\2\2\2/\3\2\2\2\2\61\3\2\2\2\2\63\3\2\2\2\2\65\3\2\2\2\2\67\3"+
		"\2\2\2\29\3\2\2\2\2;\3\2\2\2\2=\3\2\2\2\2?\3\2\2\2\2A\3\2\2\2\2C\3\2\2"+
		"\2\2I\3\2\2\2\2K\3\2\2\2\2W\3\2\2\2\2Y\3\2\2\2\2[\3\2\2\2\2]\3\2\2\2\2"+
		"_\3\2\2\2\2a\3\2\2\2\2c\3\2\2\2\2e\3\2\2\2\2g\3\2\2\2\2i\3\2\2\2\2k\3"+
		"\2\2\2\2m\3\2\2\2\2o\3\2\2\2\2q\3\2\2\2\2s\3\2\2\2\2u\3\2\2\2\2w\3\2\2"+
		"\2\2y\3\2\2\2\2{\3\2\2\2\2}\3\2\2\2\2\177\3\2\2\2\2\u0081\3\2\2\2\2\u0083"+
		"\3\2\2\2\2\u0085\3\2\2\2\2\u0087\3\2\2\2\2\u0089\3\2\2\2\2\u008b\3\2\2"+
		"\2\2\u008d\3\2\2\2\2\u008f\3\2\2\2\2\u0091\3\2\2\2\2\u0093\3\2\2\2\2\u0095"+
		"\3\2\2\2\2\u0097\3\2\2\2\2\u0099\3\2\2\2\2\u009b\3\2\2\2\2\u009d\3\2\2"+
		"\2\2\u009f\3\2\2\2\2\u00a1\3\2\2\2\2\u00a3\3\2\2\2\2\u00a5\3\2\2\2\2\u00a7"+
		"\3\2\2\2\2\u00a9\3\2\2\2\2\u00ab\3\2\2\2\2\u00ad\3\2\2\2\2\u00af\3\2\2"+
		"\2\2\u00b1\3\2\2\2\2\u00b3\3\2\2\2\2\u00b5\3\2\2\2\2\u00b7\3\2\2\2\2\u00b9"+
		"\3\2\2\2\2\u00bb\3\2\2\2\2\u00bd\3\2\2\2\2\u00bf\3\2\2\2\2\u00c1\3\2\2"+
		"\2\2\u00c3\3\2\2\2\2\u00c5\3\2\2\2\2\u00c7\3\2\2\2\2\u00cd\3\2\2\2\2\u00cf"+
		"\3\2\2\2\2\u00d1\3\2\2\2\2\u00d3\3\2\2\2\3\u00d5\3\2\2\2\5\u00d7\3\2\2"+
		"\2\7\u00d9\3\2\2\2\t\u00db\3\2\2\2\13\u00dd\3\2\2\2\r\u00e0\3\2\2\2\17"+
		"\u00e2\3\2\2\2\21\u00e5\3\2\2\2\23\u00e7\3\2\2\2\25\u00e9\3\2\2\2\27\u00ef"+
		"\3\2\2\2\31\u00f1\3\2\2\2\33\u00f8\3\2\2\2\35\u00fa\3\2\2\2\37\u0100\3"+
		"\2\2\2!\u0105\3\2\2\2#\u010b\3\2\2\2%\u0111\3\2\2\2\'\u0116\3\2\2\2)\u011c"+
		"\3\2\2\2+\u0123\3\2\2\2-\u0126\3\2\2\2/\u012b\3\2\2\2\61\u012e\3\2\2\2"+
		"\63\u0135\3\2\2\2\65\u013a\3\2\2\2\67\u013c\3\2\2\29\u0143\3\2\2\2;\u0148"+
		"\3\2\2\2=\u014c\3\2\2\2?\u0153\3\2\2\2A\u0157\3\2\2\2C\u015b\3\2\2\2E"+
		"\u0164\3\2\2\2G\u016a\3\2\2\2I\u016e\3\2\2\2K\u0170\3\2\2\2M\u0179\3\2"+
		"\2\2O\u017b\3\2\2\2Q\u0186\3\2\2\2S\u0189\3\2\2\2U\u018f\3\2\2\2W\u0191"+
		"\3\2\2\2Y\u0193\3\2\2\2[\u0195\3\2\2\2]\u0197\3\2\2\2_\u0199\3\2\2\2a"+
		"\u019e\3\2\2\2c\u01a3\3\2\2\2e\u01a8\3\2\2\2g\u01aa\3\2\2\2i\u01ac\3\2"+
		"\2\2k\u01ae\3\2\2\2m\u01b1\3\2\2\2o\u01b3\3\2\2\2q\u01b6\3\2\2\2s\u01b8"+
		"\3\2\2\2u\u01ba\3\2\2\2w\u01bc\3\2\2\2y\u01be\3\2\2\2{\u01c0\3\2\2\2}"+
		"\u01c5\3\2\2\2\177\u01ca\3\2\2\2\u0081\u01cf\3\2\2\2\u0083\u01d1\3\2\2"+
		"\2\u0085\u01d7\3\2\2\2\u0087\u01dc\3\2\2\2\u0089\u01e1\3\2\2\2\u008b\u01e3"+
		"\3\2\2\2\u008d\u01e5\3\2\2\2\u008f\u01e7\3\2\2\2\u0091\u01e9\3\2\2\2\u0093"+
		"\u01eb\3\2\2\2\u0095\u01ed\3\2\2\2\u0097\u01ef\3\2\2\2\u0099\u01f1\3\2"+
		"\2\2\u009b\u01f6\3\2\2\2\u009d\u01fb\3\2\2\2\u009f\u01fd\3\2\2\2\u00a1"+
		"\u01ff\3\2\2\2\u00a3\u0201\3\2\2\2\u00a5\u0206\3\2\2\2\u00a7\u020d\3\2"+
		"\2\2\u00a9\u0214\3\2\2\2\u00ab\u021d\3\2\2\2\u00ad\u021f\3\2\2\2\u00af"+
		"\u0221\3\2\2\2\u00b1\u0226\3\2\2\2\u00b3\u0228\3\2\2\2\u00b5\u022a\3\2"+
		"\2\2\u00b7\u022c\3\2\2\2\u00b9\u022e\3\2\2\2\u00bb\u0230\3\2\2\2\u00bd"+
		"\u0232\3\2\2\2\u00bf\u0234\3\2\2\2\u00c1\u024a\3\2\2\2\u00c3\u024c\3\2"+
		"\2\2\u00c5\u0253\3\2\2\2\u00c7\u0256\3\2\2\2\u00c9\u0281\3\2\2\2\u00cb"+
		"\u0283\3\2\2\2\u00cd\u028a\3\2\2\2\u00cf\u0292\3\2\2\2\u00d1\u0298\3\2"+
		"\2\2\u00d3\u02a6\3\2\2\2\u00d5\u00d6\7}\2\2\u00d6\4\3\2\2\2\u00d7\u00d8"+
		"\7\177\2\2\u00d8\6\3\2\2\2\u00d9\u00da\7+\2\2\u00da\b\3\2\2\2\u00db\u00dc"+
		"\7*\2\2\u00dc\n\3\2\2\2\u00dd\u00de\7^\2\2\u00de\u00df\7\61\2\2\u00df"+
		"\f\3\2\2\2\u00e0\u00e1\7\u222b\2\2\u00e1\16\3\2\2\2\u00e2\u00e3\7\61\2"+
		"\2\u00e3\u00e4\7^\2\2\u00e4\20\3\2\2\2\u00e5\u00e6\7\u222c\2\2\u00e6\22"+
		"\3\2\2\2\u00e7\u00e8\7#\2\2\u00e8\24\3\2\2\2\u00e9\u00ea\7\u00ae\2\2\u00ea"+
		"\26\3\2\2\2\u00eb\u00f0\5\37\20\2\u00ec\u00f0\5\35\17\2\u00ed\u00f0\5"+
		"\63\32\2\u00ee\u00f0\5\65\33\2\u00ef\u00eb\3\2\2\2\u00ef\u00ec\3\2\2\2"+
		"\u00ef\u00ed\3\2\2\2\u00ef\u00ee\3\2\2\2\u00f0\30\3\2\2\2\u00f1\u00f2"+
		"\7c\2\2\u00f2\u00f3\7u\2\2\u00f3\u00f4\7u\2\2\u00f4\u00f5\7g\2\2\u00f5"+
		"\u00f6\7t\2\2\u00f6\u00f7\7v\2\2\u00f7\32\3\2\2\2\u00f8\u00f9\7\u22aa"+
		"\2\2\u00f9\34\3\2\2\2\u00fa\u00fb\7h\2\2\u00fb\u00fc\7c\2\2\u00fc\u00fd"+
		"\7n\2\2\u00fd\u00fe\7u\2\2\u00fe\u00ff\7g\2\2\u00ff\36\3\2\2\2\u0100\u0101"+
		"\7v\2\2\u0101\u0102\7t\2\2\u0102\u0103\7w\2\2\u0103\u0104\7g\2\2\u0104"+
		" \3\2\2\2\u0105\u0106\7d\2\2\u0106\u0107\7n\2\2\u0107\u0108\7q\2\2\u0108"+
		"\u0109\7e\2\2\u0109\u010a\7m\2\2\u010a\"\3\2\2\2\u010b\u010c\7n\2\2\u010c"+
		"\u010d\7q\2\2\u010d\u010e\7e\2\2\u010e\u010f\7c\2\2\u010f\u0110\7n\2\2"+
		"\u0110$\3\2\2\2\u0111\u0112\7d\2\2\u0112\u0113\7q\2\2\u0113\u0114\7f\2"+
		"\2\u0114\u0115\7{\2\2\u0115&\3\2\2\2\u0116\u0117\7e\2\2\u0117\u0118\7"+
		"c\2\2\u0118\u0119\7v\2\2\u0119\u011a\7e\2\2\u011a\u011b\7j\2\2\u011b("+
		"\3\2\2\2\u011c\u011d\7f\2\2\u011d\u011e\7g\2\2\u011e\u011f\7h\2\2\u011f"+
		"\u0120\7k\2\2\u0120\u0121\7p\2\2\u0121\u0122\7g\2\2\u0122*\3\2\2\2\u0123"+
		"\u0124\7f\2\2\u0124\u0125\7q\2\2\u0125,\3\2\2\2\u0126\u0127\7g\2\2\u0127"+
		"\u0128\7n\2\2\u0128\u0129\7u\2\2\u0129\u012a\7g\2\2\u012a.\3\2\2\2\u012b"+
		"\u012c\7k\2\2\u012c\u012d\7h\2\2\u012d\60\3\2\2\2\u012e\u012f\7o\2\2\u012f"+
		"\u0130\7q\2\2\u0130\u0131\7f\2\2\u0131\u0132\7w\2\2\u0132\u0133\7n\2\2"+
		"\u0133\u0134\7g\2\2\u0134\62\3\2\2\2\u0135\u0136\7p\2\2\u0136\u0137\7"+
		"w\2\2\u0137\u0138\7n\2\2\u0138\u0139\7n\2\2\u0139\64\3\2\2\2\u013a\u013b"+
		"\7\u2207\2\2\u013b\66\3\2\2\2\u013c\u013d\7u\2\2\u013d\u013e\7y\2\2\u013e"+
		"\u013f\7k\2\2\u013f\u0140\7v\2\2\u0140\u0141\7e\2\2\u0141\u0142\7j\2\2"+
		"\u01428\3\2\2\2\u0143\u0144\7v\2\2\u0144\u0145\7j\2\2\u0145\u0146\7g\2"+
		"\2\u0146\u0147\7p\2\2\u0147:\3\2\2\2\u0148\u0149\7v\2\2\u0149\u014a\7"+
		"t\2\2\u014a\u014b\7{\2\2\u014b<\3\2\2\2\u014c\u014d\7y\2\2\u014d\u014e"+
		"\7j\2\2\u014e\u014f\7k\2\2\u014f\u0150\7n\2\2\u0150\u0151\7g\2\2\u0151"+
		">\3\2\2\2\u0152\u0154\t\2\2\2\u0153\u0152\3\2\2\2\u0154\u0155\3\2\2\2"+
		"\u0155\u0153\3\2\2\2\u0155\u0156\3\2\2\2\u0156@\3\2\2\2\u0157\u0158\5"+
		"? \2\u0158\u0159\7\60\2\2\u0159\u015a\5? \2\u015aB\3\2\2\2\u015b\u0162"+
		"\5A!\2\u015c\u015e\5E#\2\u015d\u015f\5G$\2\u015e\u015d\3\2\2\2\u015e\u015f"+
		"\3\2\2\2\u015f\u0160\3\2\2\2\u0160\u0161\5? \2\u0161\u0163\3\2\2\2\u0162"+
		"\u015c\3\2\2\2\u0162\u0163\3\2\2\2\u0163D\3\2\2\2\u0164\u0165\t\3\2\2"+
		"\u0165F\3\2\2\2\u0166\u016b\5m\67\2\u0167\u016b\5\u00b7\\\2\u0168\u016b"+
		"\5q9\2\u0169\u016b\5\u00b5[\2\u016a\u0166\3\2\2\2\u016a\u0167\3\2\2\2"+
		"\u016a\u0168\3\2\2\2\u016a\u0169\3\2\2\2\u016bH\3\2\2\2\u016c\u016f\5"+
		"\37\20\2\u016d\u016f\5\35\17\2\u016e\u016c\3\2\2\2\u016e\u016d\3\2\2\2"+
		"\u016fJ\3\2\2\2\u0170\u0172\7)\2\2\u0171\u0173\5S*\2\u0172\u0171\3\2\2"+
		"\2\u0172\u0173\3\2\2\2\u0173\u0174\3\2\2\2\u0174\u0175\7)\2\2\u0175L\3"+
		"\2\2\2\u0176\u0177\7^\2\2\u0177\u017a\t\4\2\2\u0178\u017a\5O(\2\u0179"+
		"\u0176\3\2\2\2\u0179\u0178\3\2\2\2\u017aN\3\2\2\2\u017b\u017d\7^\2\2\u017c"+
		"\u017e\7w\2\2\u017d\u017c\3\2\2\2\u017e\u017f\3\2\2\2\u017f\u017d\3\2"+
		"\2\2\u017f\u0180\3\2\2\2\u0180\u0181\3\2\2\2\u0181\u0182\5Q)\2\u0182\u0183"+
		"\5Q)\2\u0183\u0184\5Q)\2\u0184\u0185\5Q)\2\u0185P\3\2\2\2\u0186\u0187"+
		"\t\5\2\2\u0187R\3\2\2\2\u0188\u018a\5U+\2\u0189\u0188\3\2\2\2\u018a\u018b"+
		"\3\2\2\2\u018b\u0189\3\2\2\2\u018b\u018c\3\2\2\2\u018cT\3\2\2\2\u018d"+
		"\u0190\n\6\2\2\u018e\u0190\5M\'\2\u018f\u018d\3\2\2\2\u018f\u018e\3\2"+
		"\2\2\u0190V\3\2\2\2\u0191\u0192\7]\2\2\u0192X\3\2\2\2\u0193\u0194\7_\2"+
		"\2\u0194Z\3\2\2\2\u0195\u0196\7.\2\2\u0196\\\3\2\2\2\u0197\u0198\7<\2"+
		"\2\u0198^\3\2\2\2\u0199\u019a\7=\2\2\u019a`\3\2\2\2\u019b\u019c\7]\2\2"+
		"\u019c\u019f\7~\2\2\u019d\u019f\7\u27e8\2\2\u019e\u019b\3\2\2\2\u019e"+
		"\u019d\3\2\2\2\u019fb\3\2\2\2\u01a0\u01a1\7~\2\2\u01a1\u01a4\7_\2\2\u01a2"+
		"\u01a4\7\u27e9\2\2\u01a3\u01a0\3\2\2\2\u01a3\u01a2\3\2\2\2\u01a4d\3\2"+
		"\2\2\u01a5\u01a6\7/\2\2\u01a6\u01a9\7@\2\2\u01a7\u01a9\7\u2194\2\2\u01a8"+
		"\u01a5\3\2\2\2\u01a8\u01a7\3\2\2\2\u01a9f\3\2\2\2\u01aa\u01ab\t\7\2\2"+
		"\u01abh\3\2\2\2\u01ac\u01ad\t\b\2\2\u01adj\3\2\2\2\u01ae\u01af\7-\2\2"+
		"\u01af\u01b0\7-\2\2\u01b0l\3\2\2\2\u01b1\u01b2\7-\2\2\u01b2n\3\2\2\2\u01b3"+
		"\u01b4\7/\2\2\u01b4\u01b5\7/\2\2\u01b5p\3\2\2\2\u01b6\u01b7\7/\2\2\u01b7"+
		"r\3\2\2\2\u01b8\u01b9\7>\2\2\u01b9t\3\2\2\2\u01ba\u01bb\7@\2\2\u01bbv"+
		"\3\2\2\2\u01bc\u01bd\7?\2\2\u01bdx\3\2\2\2\u01be\u01bf\7$\2\2\u01bfz\3"+
		"\2\2\2\u01c0\u01c1\7)\2\2\u01c1|\3\2\2\2\u01c2\u01c3\7~\2\2\u01c3\u01c6"+
		"\7`\2\2\u01c4\u01c6\7\u22a4\2\2\u01c5\u01c2\3\2\2\2\u01c5\u01c4\3\2\2"+
		"\2\u01c6~\3\2\2\2\u01c7\u01c8\7>\2\2\u01c8\u01cb\7?\2\2\u01c9\u01cb\7"+
		"\u2266\2\2\u01ca\u01c7\3\2\2\2\u01ca\u01c9\3\2\2\2\u01cb\u0080\3\2\2\2"+
		"\u01cc\u01cd\7@\2\2\u01cd\u01d0\7?\2\2\u01ce\u01d0\7\u2267\2\2\u01cf\u01cc"+
		"\3\2\2\2\u01cf\u01ce\3\2\2\2\u01d0\u0082\3\2\2\2\u01d1\u01d2\7>\2\2\u01d2"+
		"\u01d3\7>\2\2\u01d3\u0084\3\2\2\2\u01d4\u01d5\7?\2\2\u01d5\u01d8\7?\2"+
		"\2\u01d6\u01d8\7\u2263\2\2\u01d7\u01d4\3\2\2\2\u01d7\u01d6\3\2\2\2\u01d8"+
		"\u0086\3\2\2\2\u01d9\u01da\7#\2\2\u01da\u01dd\7?\2\2\u01db\u01dd\7\u2262"+
		"\2\2\u01dc\u01d9\3\2\2\2\u01dc\u01db\3\2\2\2\u01dd\u0088\3\2\2\2\u01de"+
		"\u01df\7?\2\2\u01df\u01e2\7\u0080\2\2\u01e0\u01e2\7\u224a\2\2\u01e1\u01de"+
		"\3\2\2\2\u01e1\u01e0\3\2\2\2\u01e2\u008a\3\2\2\2\u01e3\u01e4\t\t\2\2\u01e4"+
		"\u008c\3\2\2\2\u01e5\u01e6\4\u220a\u220b\2\u01e6\u008e\3\2\2\2\u01e7\u01e8"+
		"\4\u2205\u2206\2\u01e8\u0090\3\2\2\2\u01e9\u01ea\7\u2202\2\2\u01ea\u0092"+
		"\3\2\2\2\u01eb\u01ec\4\u220d\u220e\2\u01ec\u0094\3\2\2\2\u01ed\u01ee\7"+
		"`\2\2\u01ee\u0096\3\2\2\2\u01ef\u01f0\t\n\2\2\u01f0\u0098\3\2\2\2\u01f1"+
		"\u01f2\t\13\2\2\u01f2\u009a\3\2\2\2\u01f3\u01f4\7(\2\2\u01f4\u01f7\7("+
		"\2\2\u01f5\u01f7\7\u2229\2\2\u01f6\u01f3\3\2\2\2\u01f6\u01f5\3\2\2\2\u01f7"+
		"\u009c\3\2\2\2\u01f8\u01f9\7~\2\2\u01f9\u01fc\7~\2\2\u01fa\u01fc\7\u222a"+
		"\2\2\u01fb\u01f8\3\2\2\2\u01fb\u01fa\3\2\2\2\u01fc\u009e\3\2\2\2\u01fd"+
		"\u01fe\7b\2\2\u01fe\u00a0\3\2\2\2\u01ff\u0200\t\f\2\2\u0200\u00a2\3\2"+
		"\2\2\u0201\u0202\7\u0080\2\2\u0202\u00a4\3\2\2\2\u0203\u0204\7^\2\2\u0204"+
		"\u0207\7#\2\2\u0205\u0207\7^\2\2\u0206\u0203\3\2\2\2\u0206\u0205\3\2\2"+
		"\2\u0207\u00a6\3\2\2\2\u0208\u0209\7^\2\2\u0209\u020a\7#\2\2\u020a\u020e"+
		"\7,\2\2\u020b\u020c\7^\2\2\u020c\u020e\7,\2\2\u020d\u0208\3\2\2\2\u020d"+
		"\u020b\3\2\2\2\u020e\u00a8\3\2\2\2\u020f\u0210\7^\2\2\u0210\u0211\7#\2"+
		"\2\u0211\u0215\7@\2\2\u0212\u0213\7^\2\2\u0213\u0215\7@\2\2\u0214\u020f"+
		"\3\2\2\2\u0214\u0212\3\2\2\2\u0215\u00aa\3\2\2\2\u0216\u0217\7^\2\2\u0217"+
		"\u0218\7#\2\2\u0218\u0219\7@\2\2\u0219\u021e\7,\2\2\u021a\u021b\7^\2\2"+
		"\u021b\u021c\7@\2\2\u021c\u021e\7,\2\2\u021d\u0216\3\2\2\2\u021d\u021a"+
		"\3\2\2\2\u021e\u00ac\3\2\2\2\u021f\u0220\7%\2\2\u0220\u00ae\3\2\2\2\u0221"+
		"\u0222\7~\2\2\u0222\u00b0\3\2\2\2\u0223\u0224\7\u0080\2\2\u0224\u0227"+
		"\7~\2\2\u0225\u0227\7\u2243\2\2\u0226\u0223\3\2\2\2\u0226\u0225\3\2\2"+
		"\2\u0227\u00b2\3\2\2\2\u0228\u0229\7\60\2\2\u0229\u00b4\3\2\2\2\u022a"+
		"\u022b\7\u00b1\2\2\u022b\u00b6\3\2\2\2\u022c\u022d\7\u207c\2\2\u022d\u00b8"+
		"\3\2\2\2\u022e\u022f\7\u230c\2\2\u022f\u00ba\3\2\2\2\u0230\u0231\7\u230a"+
		"\2\2\u0231\u00bc\3\2\2\2\u0232\u0233\t\r\2\2\u0233\u00be\3\2\2\2\u0234"+
		"\u0235\t\16\2\2\u0235\u00c0\3\2\2\2\u0236\u024b\7\u2256\2\2\u0237\u0238"+
		"\7<\2\2\u0238\u024b\7?\2\2\u0239\u024b\7\u2257\2\2\u023a\u023b\7?\2\2"+
		"\u023b\u024b\7<\2\2\u023c\u023d\7-\2\2\u023d\u024b\7?\2\2\u023e\u023f"+
		"\7/\2\2\u023f\u024b\7?\2\2\u0240\u0241\5g\64\2\u0241\u0242\7?\2\2\u0242"+
		"\u024b\3\2\2\2\u0243\u0244\5i\65\2\u0244\u0245\7?\2\2\u0245\u024b\3\2"+
		"\2\2\u0246\u0247\7\'\2\2\u0247\u024b\7?\2\2\u0248\u0249\7`\2\2\u0249\u024b"+
		"\7?\2\2\u024a\u0236\3\2\2\2\u024a\u0237\3\2\2\2\u024a\u0239\3\2\2\2\u024a"+
		"\u023a\3\2\2\2\u024a\u023c\3\2\2\2\u024a\u023e\3\2\2\2\u024a\u0240\3\2"+
		"\2\2\u024a\u0243\3\2\2\2\u024a\u0246\3\2\2\2\u024a\u0248\3\2\2\2\u024b"+
		"\u00c2\3\2\2\2\u024c\u0250\t\17\2\2\u024d\u024f\t\20\2\2\u024e\u024d\3"+
		"\2\2\2\u024f\u0252\3\2\2\2\u0250\u024e\3\2\2\2\u0250\u0251\3\2\2\2\u0251"+
		"\u00c4\3\2\2\2\u0252\u0250\3\2\2\2\u0253\u0254\5\u00cbf\2\u0254\u0255"+
		"\7*\2\2\u0255\u00c6\3\2\2\2\u0256\u0264\5\u00bd_\2\u0257\u0265\5\u00c9"+
		"e\2\u0258\u0259\5\u00c3b\2\u0259\u025a\5\u00adW\2\u025a\u025c\3\2\2\2"+
		"\u025b\u0258\3\2\2\2\u025c\u025f\3\2\2\2\u025d\u025b\3\2\2\2\u025d\u025e"+
		"\3\2\2\2\u025e\u0260\3\2\2\2\u025f\u025d\3\2\2\2\u0260\u0265\5\u00cbf"+
		"\2\u0261\u0262\5\u00c5c\2\u0262\u0263\7+\2\2\u0263\u0265\3\2\2\2\u0264"+
		"\u0257\3\2\2\2\u0264\u025d\3\2\2\2\u0264\u0261\3\2\2\2\u0265\u00c8\3\2"+
		"\2\2\u0266\u0282\5g\64\2\u0267\u0282\5i\65\2\u0268\u0282\5m\67\2\u0269"+
		"\u0282\5q9\2\u026a\u0282\5s:\2\u026b\u0282\5\177@\2\u026c\u0282\5u;\2"+
		"\u026d\u0282\5\u0095K\2\u026e\u0282\5\177@\2\u026f\u0282\5\u0081A\2\u0270"+
		"\u0282\5\u0085C\2\u0271\u0282\5\u0087D\2\u0272\u0282\5\u009bN\2\u0273"+
		"\u0282\5\u009dO\2\u0274\u0282\5\u00a1Q\2\u0275\u0282\5\u00a3R\2\u0276"+
		"\u0282\5\u00b1Y\2\u0277\u0282\5\u008bF\2\u0278\u0282\5\u0089E\2\u0279"+
		"\u0282\5\u00b9]\2\u027a\u0282\5\u00bb^\2\u027b\u0282\5\u008dG\2\u027c"+
		"\u0282\5}?\2\u027d\u0282\5\u0083B\2\u027e\u0282\5\u008fH\2\u027f\u0282"+
		"\5\u0093J\2\u0280\u0282\5\u0091I\2\u0281\u0266\3\2\2\2\u0281\u0267\3\2"+
		"\2\2\u0281\u0268\3\2\2\2\u0281\u0269\3\2\2\2\u0281\u026a\3\2\2\2\u0281"+
		"\u026b\3\2\2\2\u0281\u026c\3\2\2\2\u0281\u026d\3\2\2\2\u0281\u026e\3\2"+
		"\2\2\u0281\u026f\3\2\2\2\u0281\u0270\3\2\2\2\u0281\u0271\3\2\2\2\u0281"+
		"\u0272\3\2\2\2\u0281\u0273\3\2\2\2\u0281\u0274\3\2\2\2\u0281\u0275\3\2"+
		"\2\2\u0281\u0276\3\2\2\2\u0281\u0277\3\2\2\2\u0281\u0278\3\2\2\2\u0281"+
		"\u0279\3\2\2\2\u0281\u027a\3\2\2\2\u0281\u027b\3\2\2\2\u0281\u027c\3\2"+
		"\2\2\u0281\u027d\3\2\2\2\u0281\u027e\3\2\2\2\u0281\u027f\3\2\2\2\u0281"+
		"\u0280\3\2\2\2\u0282\u00ca\3\2\2\2\u0283\u0287\t\17\2\2\u0284\u0286\t"+
		"\20\2\2\u0285\u0284\3\2\2\2\u0286\u0289\3\2\2\2\u0287\u0285\3\2\2\2\u0287"+
		"\u0288\3\2\2\2\u0288\u00cc\3\2\2\2\u0289\u0287\3\2\2\2\u028a\u028e\7\u00bd"+
		"\2\2\u028b\u028d\n\21\2\2\u028c\u028b\3\2\2\2\u028d\u0290\3\2\2\2\u028e"+
		"\u028c\3\2\2\2\u028e\u028f\3\2\2\2\u028f\u00ce\3\2\2\2\u0290\u028e\3\2"+
		"\2\2\u0291\u0293\t\22\2\2\u0292\u0291\3\2\2\2\u0293\u0294\3\2\2\2\u0294"+
		"\u0292\3\2\2\2\u0294\u0295\3\2\2\2\u0295\u0296\3\2\2\2\u0296\u0297\bh"+
		"\2\2\u0297\u00d0\3\2\2\2\u0298\u0299\7\61\2\2\u0299\u029a\7,\2\2\u029a"+
		"\u029e\3\2\2\2\u029b\u029d\13\2\2\2\u029c\u029b\3\2\2\2\u029d\u02a0\3"+
		"\2\2\2\u029e\u029f\3\2\2\2\u029e\u029c\3\2\2\2\u029f\u02a1\3\2\2\2\u02a0"+
		"\u029e\3\2\2\2\u02a1\u02a2\7,\2\2\u02a2\u02a3\7\61\2\2\u02a3\u02a4\3\2"+
		"\2\2\u02a4\u02a5\bi\2\2\u02a5\u00d2\3\2\2\2\u02a6\u02a7\7\61\2\2\u02a7"+
		"\u02a8\7\61\2\2\u02a8\u02ac\3\2\2\2\u02a9\u02ab\n\21\2\2\u02aa\u02a9\3"+
		"\2\2\2\u02ab\u02ae\3\2\2\2\u02ac\u02aa\3\2\2\2\u02ac\u02ad\3\2\2\2\u02ad"+
		"\u02af\3\2\2\2\u02ae\u02ac\3\2\2\2\u02af\u02b0\bj\2\2\u02b0\u00d4\3\2"+
		"\2\2(\2\u00ef\u0155\u015e\u0162\u016a\u016e\u0172\u0179\u017f\u018b\u018f"+
		"\u019e\u01a3\u01a8\u01c5\u01ca\u01cf\u01d7\u01dc\u01e1\u01f6\u01fb\u0206"+
		"\u020d\u0214\u021d\u0226\u024a\u0250\u025d\u0264\u0281\u0287\u028e\u0294"+
		"\u029e\u02ac\3\b\2\2";
	public static final ATN _ATN =
		new ATNDeserializer().deserialize(_serializedATN.toCharArray());
	static {
		_decisionToDFA = new DFA[_ATN.getNumberOfDecisions()];
		for (int i = 0; i < _ATN.getNumberOfDecisions(); i++) {
			_decisionToDFA[i] = new DFA(_ATN.getDecisionState(i), i);
		}
	}
}