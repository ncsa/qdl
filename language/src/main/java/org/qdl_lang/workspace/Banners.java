package org.qdl_lang.workspace;

/**
 * <p>Created by Jeff Gaynor<br>
 * on 4/24/23 at  7:21 AM
 */
public class Banners {
    public static final String ROMAN_STYLE = "roman";
    public static final String TIMES_STYLE = "times";
    public static final String OS2_STYLE = "os2";
    public static final String FRAKTUR_STYLE = "fraktur";
    public static final String GOTHIC_STYLE = "gothic";
    public static final String PLAIN_STYLE = "plain";
    public static final String DEFAULT_STYLE = "default";
    public static final String SMALL_STYLE = "small";
    public static final String NONE_STYLE = "none";

    public static String getLogo(String style) {
        switch (style) {
            case ROMAN_STYLE:
                return ROMAN;
            case TIMES_STYLE:
                return TIMES;
            case OS2_STYLE:
                return OS2;
            case FRAKTUR_STYLE:
                return FRAKTUR;
            case GOTHIC_STYLE:
                return GOTHIC;
            case PLAIN_STYLE:
                return SMALL;
            case NONE_STYLE:
                return null;
            default:
                return DEFAULT;
        }
    }

    public static String STARS = "*****************************************************************************************";
    public static String DASHES = "----------------------------------------------------------------------------------------";

    public static String getDelimiter(String style) {
        switch (style) {
            case SMALL_STYLE:
            case PLAIN_STYLE:
            case OS2_STYLE:
                return DASHES;
        }
        return STARS;
    }

    public static int getLogoWidth(String style) {
        switch (style) {
            case ROMAN_STYLE:
                return ROMAN_WIDTH;
            case TIMES_STYLE:
                return TIMES_WIDTH;
            case OS2_STYLE:
                return OS2_WIDTH;
            case FRAKTUR_STYLE:
                return FRAKTUR_WIDTH;
            case GOTHIC_STYLE:
                return GOTH_WIDTH;
            case PLAIN_STYLE:
                return DEFAULT__WITH;
            case NONE_STYLE:
                return 0;
            case SMALL_STYLE:
                return SMALL_WIDTH;
        }
        return DEFAULT__WITH;
    }

    public static String MORSE_SHORT = "- - . -  / - . .  / . - . . ";
    public static String MORSE_LONG = "-- -- @ --  / -- @ @  / @ -- @ @ ";
    public static String GOTHIC = "                                                   \n" +
            "      # ###          ##### ##        ##### /       \n" +
            "    /  /###       /#####  /##     ######  /        \n" +
            "   /  /  ###    //    /  / ###   /#   /  /         \n" +
            "  /  ##   ###  /     /  /   ### /    /  /          \n" +
            " /  ###    ###      /  /     ###    /  /           \n" +
            "##   ##     ##     ## ##      ##   ## ##           \n" +
            "##   ##     ##     ## ##      ##   ## ##           \n" +
            "##   ##     ##     ## ##      ##   ## ##           \n" +
            "##   ##     ##     ## ##      ##   ## ##           \n" +
            "##   ##     ##     ## ##      ##   ## ##           \n" +
            " ##  ## ### ##     #  ##      ##   #  ##           \n" +
            "  ## #   ####         /       /       /            \n" +
            "   ###     /##   /###/       /    /##/           / \n" +
            "    ######/ ##  /   ########/    /  ############/  \n" +
            "      ###   ## /       ####     /     #########    \n" +
            "            ## #                #                  \n" +
            "            /   ##               ##                \n" +
            "           /                                       \n" +
            "          /                                        ";
    public static int GOTH_WIDTH = 60;
    public static int TIMES_WIDTH = 40;
    public static String TIMES = MORSE_LONG + "\n" +
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
            " -- -- @ --      -- @ @        @ -- @ @ \n" +
                    "  .oooooo.      oooooooooo.   ooooo        \n" +
                    " d8P'  `Y8b     `888'   `Y8b  `888'        \n" +
                    "888      888     888      888  888         \n" +
                    "888      888     888      888  888         \n" +
                    "888      888     888      888  888         \n" +
                    "`88b    d88b     888     d88'  888       o \n" +
                    " `Y8bood8P'Ybd' o888bood8P'   o888ooooood8 \n" +
                    " -- -- @ --      -- @ @        @ -- @ @ ";
    public static int ROMAN_WIDTH = 43;
    public static String DEFAULT =
            MORSE_SHORT + "\n" +
                    "(  ___  )(  __  \\ ( \\      \n" +
                    "| (   ) || (  \\  )| (      \n" +
                    "| |   | || |   | || |      \n" +
                    "| | /\\| || |   ) || |      \n" +
                    "| (_\\ \\ || (__/  )| (____/\\\n" +
                    "(____\\/_)(______/ (_______/\n" +
                    MORSE_SHORT;
    public static int DEFAULT__WITH = 30;
    public static String OS2 =
            "_____________________________\n" +
                    "___oooo____oooooo____oo______\n" +
                    "_oo____oo__oo____oo__oo______\n" +
                    "oo______oo_oo_____oo_oo______\n" +
                    "oo___o__oo_oo_____oo_oo______\n" +
                    "_oo___ooo__oo____oo__oo______\n" +
                    "___oooo_o__oooooo____ooooooo_\n" +
                    "_________oo__________________";
    public static int OS2_WIDTH = 29;
    public static String SMALL =
            "  ___  ____  _     \n" +
                    " / _ \\|  _ \\| |    \n" +
                    "| | | | | | | |    \n" +
                    "| |_| | |_| | |___ \n" +
                    " \\__\\_\\____/|_____|";
    public static int SMALL_WIDTH = 20;
    public static String FRAKTUR =
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
    public static int FRAKTUR_WIDTH = 60;
}
