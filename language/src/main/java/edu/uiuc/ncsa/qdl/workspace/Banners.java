package edu.uiuc.ncsa.qdl.workspace;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/24/23 at  7:21 AM
 */
public class Banners {
    public static final String ROMAN_STYLE = "roman";
    public static final String TIMES_STYLE = "times";
    public static final String OS2_STYLE = "os2";
    public static final String FRAKTUR_STYLE = "fraktur";
    public static final String PLAIN_STYLE = "plain";
    public static final String DEFAULT_STYLE = "default";
    public static final String SMALL_STYLE = "small";

    public static String MORSE_SHORT="- - . -  / - . .  / . - . . ";
    public static String MORSE_LONG="-- -- @ --  / -- @ @  / @ -- @ @ ";
    public static String TIMES=MORSE_LONG + "\n"+
            "  .g8\"\"8q. `7MM\"\"\"Yb. `7MMF'      \n" +
            ".dP'    `YM. MM    `Yb. MM        \n" +
            "dM'      `MM MM     `Mb MM        \n" +
            "MM        MM MM      MM MM        \n" +
            "MM.      ,MP MM     ,MP MM      , \n" +
            "`Mb.    ,dP' MM    ,dP' MM     ,M \n" +
            "  `\"bmmd\"' .JMMmmmdP' .JMMmmmmMMM \n" +
            "      MMb                         \n" +
            "       `bood'                     \n" +
            MORSE_LONG;

    public static String ROMAN =
             " -- -- @ --      -- @ @        @ -- @ @ \n"+
             "  .oooooo.      oooooooooo.   ooooo        \n" +
             " d8P'  `Y8b     `888'   `Y8b  `888'        \n" +
             "888      888     888      888  888         \n" +
             "888      888     888      888  888         \n" +
             "888      888     888      888  888         \n" +
             "`88b    d88b     888     d88'  888       o \n" +
             " `Y8bood8P'Ybd' o888bood8P'   o888ooooood8 \n" +
             " -- -- @ --      -- @ @        @ -- @ @ ";

    public static String DEFAULT=
            MORSE_SHORT + "\n" +
              "(  ___  )(  __  \\ ( \\      \n" +
              "| (   ) || (  \\  )| (      \n" +
              "| |   | || |   | || |      \n" +
              "| | /\\| || |   ) || |      \n" +
              "| (_\\ \\ || (__/  )| (____/\\\n" +
              "(____\\/_)(______/ (_______/\n" +
              MORSE_SHORT;

    public static String OS2=
            "_____________________________\n" +
            "___oooo____oooooo____oo______\n" +
            "_oo____oo__oo____oo__oo______\n" +
            "oo______oo_oo_____oo_oo______\n" +
            "oo___o__oo_oo_____oo_oo______\n" +
            "_oo___ooo__oo____oo__oo______\n" +
            "___oooo_o__oooooo____ooooooo_\n" +
            "_________oo__________________";

    public static String SMALL =
            "   ___  ____  _     \n" +
            "  / _ \\|  _ \\| |    \n" +
            " | | | | | | | |    \n" +
            " | |_| | |_| | |___ \n" +
            "  \\__\\_\\____/|_____|";

    public static String FRAKTUR=
            "        ....                   ....                ...      \n" +
            "    .n~8888888nx           .xH888888Hx.        .zf\"` `\"tu   \n" +
            "  :88>'8888888888:       .H8888888888888:     x88      '8N. \n" +
            " :8888 \"*888888888k      888*\"\"\"?\"\"*88888X    888k     d88& \n" +
            " '88888.         \"8>    'f     d8x.   ^%88k   8888N.  @888F \n" +
            "  ?88888          'X    '>    <88888X   '?8   `88888 9888%  \n" +
            "?  %888!           !     `:..:`888888>    8>    %888 \"88F   \n" +
            " \".:88\"            !            `\"*88     X      8\"   \"*h=~ \n" +
            "   xHH8Hx.        .X  :    .xHHhx..\"      !    z8Weu        \n" +
            " :888888888hx....x\\8..X   X88888888hx. ..!    \"\"88888i.   Z \n" +
            ":~  `\"8888888888!`'8888  !   \"*888888888\"    \"   \"8888888*  \n" +
            "       `\"\"*8*\"\"`   \"*\"          ^\"***\"`            ^\"**\"\"";
}
