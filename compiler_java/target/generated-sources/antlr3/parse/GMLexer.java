// $ANTLR 3.4 parse/GMLexer.g 2012-08-13 00:25:56

import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings({"all", "warnings", "unchecked"})
public class GMLexer extends AbstractGMLexer {
    public static final int EOF=-1;
    public static final int ALPHANUM=4;
    public static final int BOOL_VAL=5;
    public static final int DIGIT=6;
    public static final int FLOAT_NUM=7;
    public static final int ID=8;
    public static final int INT_NUM=9;
    public static final int LETTER=10;
    public static final int T_ALL=11;
    public static final int T_AND=12;
    public static final int T_ANDEQ=13;
    public static final int T_AVG=14;
    public static final int T_BACK=15;
    public static final int T_BFS=16;
    public static final int T_BOOL=17;
    public static final int T_COLLECTION=18;
    public static final int T_COMMON_NBRS=19;
    public static final int T_COUNT=20;
    public static final int T_DFS=21;
    public static final int T_DO=22;
    public static final int T_DOUBLE=23;
    public static final int T_DOUBLE_COLON=24;
    public static final int T_DOWN_NBRS=25;
    public static final int T_EDGE=26;
    public static final int T_EDGEPROP=27;
    public static final int T_EDGES=28;
    public static final int T_ELSE=29;
    public static final int T_EQ=30;
    public static final int T_EXIST=31;
    public static final int T_FLOAT=32;
    public static final int T_FOR=33;
    public static final int T_FOREACH=34;
    public static final int T_FROM=35;
    public static final int T_GE=36;
    public static final int T_GRAPH=37;
    public static final int T_IF=38;
    public static final int T_INT=39;
    public static final int T_IN_NBRS=40;
    public static final int T_ITEMS=41;
    public static final int T_LE=42;
    public static final int T_LOCAL=43;
    public static final int T_LONG=44;
    public static final int T_MAX=45;
    public static final int T_MAXEQ=46;
    public static final int T_MIN=47;
    public static final int T_MINEQ=48;
    public static final int T_MULTEQ=49;
    public static final int T_M_INF=50;
    public static final int T_NBRS=51;
    public static final int T_NEQ=52;
    public static final int T_NIL=53;
    public static final int T_NODE=54;
    public static final int T_NODEPROP=55;
    public static final int T_NODES=56;
    public static final int T_NORDER=57;
    public static final int T_NSEQ=58;
    public static final int T_NSET=59;
    public static final int T_OR=60;
    public static final int T_OREQ=61;
    public static final int T_PLUSEQ=62;
    public static final int T_PLUSPLUS=63;
    public static final int T_POST=64;
    public static final int T_PROC=65;
    public static final int T_PRODUCT=66;
    public static final int T_P_INF=67;
    public static final int T_RARROW=68;
    public static final int T_RBFS=69;
    public static final int T_RETURN=70;
    public static final int T_SUM=71;
    public static final int T_TO=72;
    public static final int T_UP_NBRS=73;
    public static final int T_WHILE=74;

    // delegates
    // delegators
    public AbstractGMLexer[] getDelegates() {
        return new AbstractGMLexer[] {};
    }

    public GMLexer() {} 
    public GMLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public GMLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);
    }
    public String getGrammarFileName() { return "parse/GMLexer.g"; }

    // $ANTLR start "DIGIT"
    public final void mDIGIT() throws RecognitionException {
        try {
            // parse/GMLexer.g:9:16: ( '0' .. '9' )
            // parse/GMLexer.g:
            {
            if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "DIGIT"

    // $ANTLR start "LETTER"
    public final void mLETTER() throws RecognitionException {
        try {
            // parse/GMLexer.g:10:17: ( 'a' .. 'z' | 'A' .. 'Z' )
            // parse/GMLexer.g:
            {
            if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "LETTER"

    // $ANTLR start "ALPHANUM"
    public final void mALPHANUM() throws RecognitionException {
        try {
            // parse/GMLexer.g:11:19: ( ( LETTER ) ( LETTER | DIGIT | '_' )* )
            // parse/GMLexer.g:11:21: ( LETTER ) ( LETTER | DIGIT | '_' )*
            {
            if ( (input.LA(1) >= 'A' && input.LA(1) <= 'Z')||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
                input.consume();
            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;
            }


            // parse/GMLexer.g:11:30: ( LETTER | DIGIT | '_' )*
            loop1:
            do {
                int alt1=2;
                switch ( input.LA(1) ) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case 'A':
                case 'B':
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                case 'Y':
                case 'Z':
                case '_':
                case 'a':
                case 'b':
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                case 'y':
                case 'z':
                    {
                    alt1=1;
                    }
                    break;

                }

                switch (alt1) {
            	case 1 :
            	    // parse/GMLexer.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }


        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ALPHANUM"

    // $ANTLR start "ID"
    public final void mID() throws RecognitionException {
        try {
            int _type = ID;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:14:4: ( ALPHANUM )
            // parse/GMLexer.g:14:6: ALPHANUM
            {
            mALPHANUM(); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "ID"

    // $ANTLR start "FLOAT_NUM"
    public final void mFLOAT_NUM() throws RecognitionException {
        try {
            int _type = FLOAT_NUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:15:11: ( ( DIGIT )+ '.' ( DIGIT )* )
            // parse/GMLexer.g:15:13: ( DIGIT )+ '.' ( DIGIT )*
            {
            // parse/GMLexer.g:15:13: ( DIGIT )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                switch ( input.LA(1) ) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    {
                    alt2=1;
                    }
                    break;

                }

                switch (alt2) {
            	case 1 :
            	    // parse/GMLexer.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);


            match('.'); 

            // parse/GMLexer.g:15:26: ( DIGIT )*
            loop3:
            do {
                int alt3=2;
                switch ( input.LA(1) ) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    {
                    alt3=1;
                    }
                    break;

                }

                switch (alt3) {
            	case 1 :
            	    // parse/GMLexer.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    break loop3;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "FLOAT_NUM"

    // $ANTLR start "INT_NUM"
    public final void mINT_NUM() throws RecognitionException {
        try {
            int _type = INT_NUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:16:9: ( ( DIGIT )+ )
            // parse/GMLexer.g:16:11: ( DIGIT )+
            {
            // parse/GMLexer.g:16:11: ( DIGIT )+
            int cnt4=0;
            loop4:
            do {
                int alt4=2;
                switch ( input.LA(1) ) {
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    {
                    alt4=1;
                    }
                    break;

                }

                switch (alt4) {
            	case 1 :
            	    // parse/GMLexer.g:
            	    {
            	    if ( (input.LA(1) >= '0' && input.LA(1) <= '9') ) {
            	        input.consume();
            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;
            	    }


            	    }
            	    break;

            	default :
            	    if ( cnt4 >= 1 ) break loop4;
                        EarlyExitException eee =
                            new EarlyExitException(4, input);
                        throw eee;
                }
                cnt4++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "INT_NUM"

    // $ANTLR start "T_LOCAL"
    public final void mT_LOCAL() throws RecognitionException {
        try {
            int _type = T_LOCAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:19:9: ( 'Local' )
            // parse/GMLexer.g:19:11: 'Local'
            {
            match("Local"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_LOCAL"

    // $ANTLR start "T_PROC"
    public final void mT_PROC() throws RecognitionException {
        try {
            int _type = T_PROC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:20:8: ( 'Procedure' | 'Proc' )
            int alt5=2;
            switch ( input.LA(1) ) {
            case 'P':
                {
                switch ( input.LA(2) ) {
                case 'r':
                    {
                    switch ( input.LA(3) ) {
                    case 'o':
                        {
                        switch ( input.LA(4) ) {
                        case 'c':
                            {
                            switch ( input.LA(5) ) {
                            case 'e':
                                {
                                alt5=1;
                                }
                                break;
                            default:
                                alt5=2;
                            }

                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 5, 3, input);

                            throw nvae;

                        }

                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 5, 2, input);

                        throw nvae;

                    }

                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 1, input);

                    throw nvae;

                }

                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;

            }

            switch (alt5) {
                case 1 :
                    // parse/GMLexer.g:20:10: 'Procedure'
                    {
                    match("Procedure"); 



                    }
                    break;
                case 2 :
                    // parse/GMLexer.g:20:24: 'Proc'
                    {
                    match("Proc"); 



                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_PROC"

    // $ANTLR start "T_BFS"
    public final void mT_BFS() throws RecognitionException {
        try {
            int _type = T_BFS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:21:7: ( 'InBFS' )
            // parse/GMLexer.g:21:9: 'InBFS'
            {
            match("InBFS"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_BFS"

    // $ANTLR start "T_DFS"
    public final void mT_DFS() throws RecognitionException {
        try {
            int _type = T_DFS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:22:7: ( 'InDFS' )
            // parse/GMLexer.g:22:9: 'InDFS'
            {
            match("InDFS"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_DFS"

    // $ANTLR start "T_POST"
    public final void mT_POST() throws RecognitionException {
        try {
            int _type = T_POST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:23:8: ( 'InPost' )
            // parse/GMLexer.g:23:10: 'InPost'
            {
            match("InPost"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_POST"

    // $ANTLR start "T_RBFS"
    public final void mT_RBFS() throws RecognitionException {
        try {
            int _type = T_RBFS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:24:8: ( 'InRBFS' )
            // parse/GMLexer.g:24:10: 'InRBFS'
            {
            match("InRBFS"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_RBFS"

    // $ANTLR start "T_FROM"
    public final void mT_FROM() throws RecognitionException {
        try {
            int _type = T_FROM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:25:8: ( 'From' )
            // parse/GMLexer.g:25:10: 'From'
            {
            match("From"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_FROM"

    // $ANTLR start "T_TO"
    public final void mT_TO() throws RecognitionException {
        try {
            int _type = T_TO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:26:6: ( 'To' )
            // parse/GMLexer.g:26:8: 'To'
            {
            match("To"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_TO"

    // $ANTLR start "T_BACK"
    public final void mT_BACK() throws RecognitionException {
        try {
            int _type = T_BACK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:27:8: ( 'InReverse' )
            // parse/GMLexer.g:27:10: 'InReverse'
            {
            match("InReverse"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_BACK"

    // $ANTLR start "T_GRAPH"
    public final void mT_GRAPH() throws RecognitionException {
        try {
            int _type = T_GRAPH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:28:9: ( 'Graph' )
            // parse/GMLexer.g:28:11: 'Graph'
            {
            match("Graph"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_GRAPH"

    // $ANTLR start "T_NODE"
    public final void mT_NODE() throws RecognitionException {
        try {
            int _type = T_NODE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:29:8: ( 'Node' )
            // parse/GMLexer.g:29:10: 'Node'
            {
            match("Node"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_NODE"

    // $ANTLR start "T_EDGE"
    public final void mT_EDGE() throws RecognitionException {
        try {
            int _type = T_EDGE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:30:8: ( 'Edge' )
            // parse/GMLexer.g:30:10: 'Edge'
            {
            match("Edge"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_EDGE"

    // $ANTLR start "T_NODEPROP"
    public final void mT_NODEPROP() throws RecognitionException {
        try {
            int _type = T_NODEPROP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:31:12: ( 'Node_Property' | 'Node_Prop' | 'N_P' )
            int alt6=3;
            switch ( input.LA(1) ) {
            case 'N':
                {
                switch ( input.LA(2) ) {
                case 'o':
                    {
                    switch ( input.LA(3) ) {
                    case 'd':
                        {
                        switch ( input.LA(4) ) {
                        case 'e':
                            {
                            switch ( input.LA(5) ) {
                            case '_':
                                {
                                switch ( input.LA(6) ) {
                                case 'P':
                                    {
                                    switch ( input.LA(7) ) {
                                    case 'r':
                                        {
                                        switch ( input.LA(8) ) {
                                        case 'o':
                                            {
                                            switch ( input.LA(9) ) {
                                            case 'p':
                                                {
                                                switch ( input.LA(10) ) {
                                                case 'e':
                                                    {
                                                    alt6=1;
                                                    }
                                                    break;
                                                default:
                                                    alt6=2;
                                                }

                                                }
                                                break;
                                            default:
                                                NoViableAltException nvae =
                                                    new NoViableAltException("", 6, 9, input);

                                                throw nvae;

                                            }

                                            }
                                            break;
                                        default:
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 6, 8, input);

                                            throw nvae;

                                        }

                                        }
                                        break;
                                    default:
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 6, 7, input);

                                        throw nvae;

                                    }

                                    }
                                    break;
                                default:
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 6, 6, input);

                                    throw nvae;

                                }

                                }
                                break;
                            default:
                                NoViableAltException nvae =
                                    new NoViableAltException("", 6, 5, input);

                                throw nvae;

                            }

                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 6, 4, input);

                            throw nvae;

                        }

                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 6, 2, input);

                        throw nvae;

                    }

                    }
                    break;
                case '_':
                    {
                    alt6=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 6, 1, input);

                    throw nvae;

                }

                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;

            }

            switch (alt6) {
                case 1 :
                    // parse/GMLexer.g:31:14: 'Node_Property'
                    {
                    match("Node_Property"); 



                    }
                    break;
                case 2 :
                    // parse/GMLexer.g:31:32: 'Node_Prop'
                    {
                    match("Node_Prop"); 



                    }
                    break;
                case 3 :
                    // parse/GMLexer.g:31:46: 'N_P'
                    {
                    match("N_P"); 



                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_NODEPROP"

    // $ANTLR start "T_EDGEPROP"
    public final void mT_EDGEPROP() throws RecognitionException {
        try {
            int _type = T_EDGEPROP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:32:12: ( 'Edge_Property' | 'Edge_Prop' | 'E_P' )
            int alt7=3;
            switch ( input.LA(1) ) {
            case 'E':
                {
                switch ( input.LA(2) ) {
                case 'd':
                    {
                    switch ( input.LA(3) ) {
                    case 'g':
                        {
                        switch ( input.LA(4) ) {
                        case 'e':
                            {
                            switch ( input.LA(5) ) {
                            case '_':
                                {
                                switch ( input.LA(6) ) {
                                case 'P':
                                    {
                                    switch ( input.LA(7) ) {
                                    case 'r':
                                        {
                                        switch ( input.LA(8) ) {
                                        case 'o':
                                            {
                                            switch ( input.LA(9) ) {
                                            case 'p':
                                                {
                                                switch ( input.LA(10) ) {
                                                case 'e':
                                                    {
                                                    alt7=1;
                                                    }
                                                    break;
                                                default:
                                                    alt7=2;
                                                }

                                                }
                                                break;
                                            default:
                                                NoViableAltException nvae =
                                                    new NoViableAltException("", 7, 9, input);

                                                throw nvae;

                                            }

                                            }
                                            break;
                                        default:
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 7, 8, input);

                                            throw nvae;

                                        }

                                        }
                                        break;
                                    default:
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 7, 7, input);

                                        throw nvae;

                                    }

                                    }
                                    break;
                                default:
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 7, 6, input);

                                    throw nvae;

                                }

                                }
                                break;
                            default:
                                NoViableAltException nvae =
                                    new NoViableAltException("", 7, 5, input);

                                throw nvae;

                            }

                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 7, 4, input);

                            throw nvae;

                        }

                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 7, 2, input);

                        throw nvae;

                    }

                    }
                    break;
                case '_':
                    {
                    alt7=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 7, 1, input);

                    throw nvae;

                }

                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 7, 0, input);

                throw nvae;

            }

            switch (alt7) {
                case 1 :
                    // parse/GMLexer.g:32:14: 'Edge_Property'
                    {
                    match("Edge_Property"); 



                    }
                    break;
                case 2 :
                    // parse/GMLexer.g:32:32: 'Edge_Prop'
                    {
                    match("Edge_Prop"); 



                    }
                    break;
                case 3 :
                    // parse/GMLexer.g:32:46: 'E_P'
                    {
                    match("E_P"); 



                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_EDGEPROP"

    // $ANTLR start "T_NSET"
    public final void mT_NSET() throws RecognitionException {
        try {
            int _type = T_NSET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:33:8: ( 'Node_Set' | 'N_S' )
            int alt8=2;
            switch ( input.LA(1) ) {
            case 'N':
                {
                switch ( input.LA(2) ) {
                case 'o':
                    {
                    alt8=1;
                    }
                    break;
                case '_':
                    {
                    alt8=2;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;

                }

                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;

            }

            switch (alt8) {
                case 1 :
                    // parse/GMLexer.g:33:10: 'Node_Set'
                    {
                    match("Node_Set"); 



                    }
                    break;
                case 2 :
                    // parse/GMLexer.g:33:23: 'N_S'
                    {
                    match("N_S"); 



                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_NSET"

    // $ANTLR start "T_NORDER"
    public final void mT_NORDER() throws RecognitionException {
        try {
            int _type = T_NORDER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:34:10: ( 'Node_Order' | 'N_O' )
            int alt9=2;
            switch ( input.LA(1) ) {
            case 'N':
                {
                switch ( input.LA(2) ) {
                case 'o':
                    {
                    alt9=1;
                    }
                    break;
                case '_':
                    {
                    alt9=2;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 9, 1, input);

                    throw nvae;

                }

                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 9, 0, input);

                throw nvae;

            }

            switch (alt9) {
                case 1 :
                    // parse/GMLexer.g:34:12: 'Node_Order'
                    {
                    match("Node_Order"); 



                    }
                    break;
                case 2 :
                    // parse/GMLexer.g:34:27: 'N_O'
                    {
                    match("N_O"); 



                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_NORDER"

    // $ANTLR start "T_NSEQ"
    public final void mT_NSEQ() throws RecognitionException {
        try {
            int _type = T_NSEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:35:8: ( 'Node_Seq' | 'Node_Sequence' | 'N_Q' )
            int alt10=3;
            switch ( input.LA(1) ) {
            case 'N':
                {
                switch ( input.LA(2) ) {
                case 'o':
                    {
                    switch ( input.LA(3) ) {
                    case 'd':
                        {
                        switch ( input.LA(4) ) {
                        case 'e':
                            {
                            switch ( input.LA(5) ) {
                            case '_':
                                {
                                switch ( input.LA(6) ) {
                                case 'S':
                                    {
                                    switch ( input.LA(7) ) {
                                    case 'e':
                                        {
                                        switch ( input.LA(8) ) {
                                        case 'q':
                                            {
                                            switch ( input.LA(9) ) {
                                            case 'u':
                                                {
                                                alt10=2;
                                                }
                                                break;
                                            default:
                                                alt10=1;
                                            }

                                            }
                                            break;
                                        default:
                                            NoViableAltException nvae =
                                                new NoViableAltException("", 10, 8, input);

                                            throw nvae;

                                        }

                                        }
                                        break;
                                    default:
                                        NoViableAltException nvae =
                                            new NoViableAltException("", 10, 7, input);

                                        throw nvae;

                                    }

                                    }
                                    break;
                                default:
                                    NoViableAltException nvae =
                                        new NoViableAltException("", 10, 6, input);

                                    throw nvae;

                                }

                                }
                                break;
                            default:
                                NoViableAltException nvae =
                                    new NoViableAltException("", 10, 5, input);

                                throw nvae;

                            }

                            }
                            break;
                        default:
                            NoViableAltException nvae =
                                new NoViableAltException("", 10, 4, input);

                            throw nvae;

                        }

                        }
                        break;
                    default:
                        NoViableAltException nvae =
                            new NoViableAltException("", 10, 2, input);

                        throw nvae;

                    }

                    }
                    break;
                case '_':
                    {
                    alt10=3;
                    }
                    break;
                default:
                    NoViableAltException nvae =
                        new NoViableAltException("", 10, 1, input);

                    throw nvae;

                }

                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;

            }

            switch (alt10) {
                case 1 :
                    // parse/GMLexer.g:35:10: 'Node_Seq'
                    {
                    match("Node_Seq"); 



                    }
                    break;
                case 2 :
                    // parse/GMLexer.g:35:23: 'Node_Sequence'
                    {
                    match("Node_Sequence"); 



                    }
                    break;
                case 3 :
                    // parse/GMLexer.g:35:41: 'N_Q'
                    {
                    match("N_Q"); 



                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_NSEQ"

    // $ANTLR start "T_COLLECTION"
    public final void mT_COLLECTION() throws RecognitionException {
        try {
            int _type = T_COLLECTION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:36:14: ( 'Collection' )
            // parse/GMLexer.g:36:16: 'Collection'
            {
            match("Collection"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_COLLECTION"

    // $ANTLR start "T_INT"
    public final void mT_INT() throws RecognitionException {
        try {
            int _type = T_INT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:37:7: ( 'Int' )
            // parse/GMLexer.g:37:9: 'Int'
            {
            match("Int"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_INT"

    // $ANTLR start "T_LONG"
    public final void mT_LONG() throws RecognitionException {
        try {
            int _type = T_LONG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:38:8: ( 'Long' )
            // parse/GMLexer.g:38:10: 'Long'
            {
            match("Long"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_LONG"

    // $ANTLR start "T_FLOAT"
    public final void mT_FLOAT() throws RecognitionException {
        try {
            int _type = T_FLOAT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:39:9: ( 'Float' )
            // parse/GMLexer.g:39:11: 'Float'
            {
            match("Float"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_FLOAT"

    // $ANTLR start "T_DOUBLE"
    public final void mT_DOUBLE() throws RecognitionException {
        try {
            int _type = T_DOUBLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:40:10: ( 'Double' )
            // parse/GMLexer.g:40:12: 'Double'
            {
            match("Double"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_DOUBLE"

    // $ANTLR start "T_BOOL"
    public final void mT_BOOL() throws RecognitionException {
        try {
            int _type = T_BOOL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:41:8: ( 'Bool' )
            // parse/GMLexer.g:41:10: 'Bool'
            {
            match("Bool"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_BOOL"

    // $ANTLR start "T_NODES"
    public final void mT_NODES() throws RecognitionException {
        try {
            int _type = T_NODES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:42:9: ( 'Nodes' )
            // parse/GMLexer.g:42:11: 'Nodes'
            {
            match("Nodes"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_NODES"

    // $ANTLR start "T_EDGES"
    public final void mT_EDGES() throws RecognitionException {
        try {
            int _type = T_EDGES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:43:9: ( 'Edges' )
            // parse/GMLexer.g:43:11: 'Edges'
            {
            match("Edges"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_EDGES"

    // $ANTLR start "T_NBRS"
    public final void mT_NBRS() throws RecognitionException {
        try {
            int _type = T_NBRS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:44:8: ( 'Nbrs' | 'OutNbrs' )
            int alt11=2;
            switch ( input.LA(1) ) {
            case 'N':
                {
                alt11=1;
                }
                break;
            case 'O':
                {
                alt11=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;

            }

            switch (alt11) {
                case 1 :
                    // parse/GMLexer.g:44:10: 'Nbrs'
                    {
                    match("Nbrs"); 



                    }
                    break;
                case 2 :
                    // parse/GMLexer.g:44:19: 'OutNbrs'
                    {
                    match("OutNbrs"); 



                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_NBRS"

    // $ANTLR start "T_IN_NBRS"
    public final void mT_IN_NBRS() throws RecognitionException {
        try {
            int _type = T_IN_NBRS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:45:11: ( 'InNbrs' )
            // parse/GMLexer.g:45:13: 'InNbrs'
            {
            match("InNbrs"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_IN_NBRS"

    // $ANTLR start "T_UP_NBRS"
    public final void mT_UP_NBRS() throws RecognitionException {
        try {
            int _type = T_UP_NBRS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:46:11: ( 'UpNbrs' )
            // parse/GMLexer.g:46:13: 'UpNbrs'
            {
            match("UpNbrs"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_UP_NBRS"

    // $ANTLR start "T_DOWN_NBRS"
    public final void mT_DOWN_NBRS() throws RecognitionException {
        try {
            int _type = T_DOWN_NBRS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:47:13: ( 'DownNbrs' )
            // parse/GMLexer.g:47:15: 'DownNbrs'
            {
            match("DownNbrs"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_DOWN_NBRS"

    // $ANTLR start "T_ITEMS"
    public final void mT_ITEMS() throws RecognitionException {
        try {
            int _type = T_ITEMS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:48:9: ( 'Items' )
            // parse/GMLexer.g:48:11: 'Items'
            {
            match("Items"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_ITEMS"

    // $ANTLR start "T_COMMON_NBRS"
    public final void mT_COMMON_NBRS() throws RecognitionException {
        try {
            int _type = T_COMMON_NBRS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:49:15: ( 'CommonNbrs' )
            // parse/GMLexer.g:49:17: 'CommonNbrs'
            {
            match("CommonNbrs"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_COMMON_NBRS"

    // $ANTLR start "T_FOREACH"
    public final void mT_FOREACH() throws RecognitionException {
        try {
            int _type = T_FOREACH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:50:11: ( 'Foreach' )
            // parse/GMLexer.g:50:13: 'Foreach'
            {
            match("Foreach"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_FOREACH"

    // $ANTLR start "T_FOR"
    public final void mT_FOR() throws RecognitionException {
        try {
            int _type = T_FOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:51:7: ( 'For' )
            // parse/GMLexer.g:51:9: 'For'
            {
            match("For"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_FOR"

    // $ANTLR start "T_AND"
    public final void mT_AND() throws RecognitionException {
        try {
            int _type = T_AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:52:7: ( 'And' | '&&' )
            int alt12=2;
            switch ( input.LA(1) ) {
            case 'A':
                {
                alt12=1;
                }
                break;
            case '&':
                {
                alt12=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 12, 0, input);

                throw nvae;

            }

            switch (alt12) {
                case 1 :
                    // parse/GMLexer.g:52:9: 'And'
                    {
                    match("And"); 



                    }
                    break;
                case 2 :
                    // parse/GMLexer.g:52:17: '&&'
                    {
                    match("&&"); 



                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_AND"

    // $ANTLR start "T_OR"
    public final void mT_OR() throws RecognitionException {
        try {
            int _type = T_OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:53:6: ( 'Or' | '||' )
            int alt13=2;
            switch ( input.LA(1) ) {
            case 'O':
                {
                alt13=1;
                }
                break;
            case '|':
                {
                alt13=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 13, 0, input);

                throw nvae;

            }

            switch (alt13) {
                case 1 :
                    // parse/GMLexer.g:53:8: 'Or'
                    {
                    match("Or"); 



                    }
                    break;
                case 2 :
                    // parse/GMLexer.g:53:15: '||'
                    {
                    match("||"); 



                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_OR"

    // $ANTLR start "T_EQ"
    public final void mT_EQ() throws RecognitionException {
        try {
            int _type = T_EQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:54:6: ( '==' )
            // parse/GMLexer.g:54:8: '=='
            {
            match("=="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_EQ"

    // $ANTLR start "T_NEQ"
    public final void mT_NEQ() throws RecognitionException {
        try {
            int _type = T_NEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:55:7: ( '!=' )
            // parse/GMLexer.g:55:9: '!='
            {
            match("!="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_NEQ"

    // $ANTLR start "T_LE"
    public final void mT_LE() throws RecognitionException {
        try {
            int _type = T_LE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:56:6: ( '<=' )
            // parse/GMLexer.g:56:8: '<='
            {
            match("<="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_LE"

    // $ANTLR start "T_GE"
    public final void mT_GE() throws RecognitionException {
        try {
            int _type = T_GE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:57:6: ( '>=' )
            // parse/GMLexer.g:57:8: '>='
            {
            match(">="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_GE"

    // $ANTLR start "BOOL_VAL"
    public final void mBOOL_VAL() throws RecognitionException {
        try {
            int _type = BOOL_VAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:58:10: ( 'True' | 'False' )
            int alt14=2;
            switch ( input.LA(1) ) {
            case 'T':
                {
                alt14=1;
                }
                break;
            case 'F':
                {
                alt14=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;

            }

            switch (alt14) {
                case 1 :
                    // parse/GMLexer.g:58:12: 'True'
                    {
                    match("True"); 



                    }
                    break;
                case 2 :
                    // parse/GMLexer.g:58:21: 'False'
                    {
                    match("False"); 



                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "BOOL_VAL"

    // $ANTLR start "T_IF"
    public final void mT_IF() throws RecognitionException {
        try {
            int _type = T_IF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:59:6: ( 'If' )
            // parse/GMLexer.g:59:8: 'If'
            {
            match("If"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_IF"

    // $ANTLR start "T_ELSE"
    public final void mT_ELSE() throws RecognitionException {
        try {
            int _type = T_ELSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:60:8: ( 'Else' )
            // parse/GMLexer.g:60:10: 'Else'
            {
            match("Else"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_ELSE"

    // $ANTLR start "T_WHILE"
    public final void mT_WHILE() throws RecognitionException {
        try {
            int _type = T_WHILE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:61:9: ( 'While' )
            // parse/GMLexer.g:61:11: 'While'
            {
            match("While"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_WHILE"

    // $ANTLR start "T_RETURN"
    public final void mT_RETURN() throws RecognitionException {
        try {
            int _type = T_RETURN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:62:10: ( 'Return' )
            // parse/GMLexer.g:62:12: 'Return'
            {
            match("Return"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_RETURN"

    // $ANTLR start "T_DO"
    public final void mT_DO() throws RecognitionException {
        try {
            int _type = T_DO;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:63:6: ( 'Do' )
            // parse/GMLexer.g:63:8: 'Do'
            {
            match("Do"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_DO"

    // $ANTLR start "T_PLUSEQ"
    public final void mT_PLUSEQ() throws RecognitionException {
        try {
            int _type = T_PLUSEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:64:10: ( '+=' )
            // parse/GMLexer.g:64:12: '+='
            {
            match("+="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_PLUSEQ"

    // $ANTLR start "T_PLUSPLUS"
    public final void mT_PLUSPLUS() throws RecognitionException {
        try {
            int _type = T_PLUSPLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:65:12: ( '++' )
            // parse/GMLexer.g:65:14: '++'
            {
            match("++"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_PLUSPLUS"

    // $ANTLR start "T_MULTEQ"
    public final void mT_MULTEQ() throws RecognitionException {
        try {
            int _type = T_MULTEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:66:10: ( '*=' )
            // parse/GMLexer.g:66:12: '*='
            {
            match("*="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_MULTEQ"

    // $ANTLR start "T_ANDEQ"
    public final void mT_ANDEQ() throws RecognitionException {
        try {
            int _type = T_ANDEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:67:9: ( '&=' )
            // parse/GMLexer.g:67:11: '&='
            {
            match("&="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_ANDEQ"

    // $ANTLR start "T_OREQ"
    public final void mT_OREQ() throws RecognitionException {
        try {
            int _type = T_OREQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:68:8: ( '|=' )
            // parse/GMLexer.g:68:10: '|='
            {
            match("|="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_OREQ"

    // $ANTLR start "T_MINEQ"
    public final void mT_MINEQ() throws RecognitionException {
        try {
            int _type = T_MINEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:69:9: ( 'min=' )
            // parse/GMLexer.g:69:11: 'min='
            {
            match("min="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_MINEQ"

    // $ANTLR start "T_MAXEQ"
    public final void mT_MAXEQ() throws RecognitionException {
        try {
            int _type = T_MAXEQ;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:70:9: ( 'max=' )
            // parse/GMLexer.g:70:11: 'max='
            {
            match("max="); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_MAXEQ"

    // $ANTLR start "T_SUM"
    public final void mT_SUM() throws RecognitionException {
        try {
            int _type = T_SUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:71:7: ( 'Sum' )
            // parse/GMLexer.g:71:9: 'Sum'
            {
            match("Sum"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_SUM"

    // $ANTLR start "T_AVG"
    public final void mT_AVG() throws RecognitionException {
        try {
            int _type = T_AVG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:72:7: ( 'Avg' )
            // parse/GMLexer.g:72:9: 'Avg'
            {
            match("Avg"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_AVG"

    // $ANTLR start "T_COUNT"
    public final void mT_COUNT() throws RecognitionException {
        try {
            int _type = T_COUNT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:73:9: ( 'Count' )
            // parse/GMLexer.g:73:11: 'Count'
            {
            match("Count"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_COUNT"

    // $ANTLR start "T_PRODUCT"
    public final void mT_PRODUCT() throws RecognitionException {
        try {
            int _type = T_PRODUCT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:74:11: ( 'Product' )
            // parse/GMLexer.g:74:13: 'Product'
            {
            match("Product"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_PRODUCT"

    // $ANTLR start "T_MAX"
    public final void mT_MAX() throws RecognitionException {
        try {
            int _type = T_MAX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:75:7: ( 'Max' )
            // parse/GMLexer.g:75:9: 'Max'
            {
            match("Max"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_MAX"

    // $ANTLR start "T_MIN"
    public final void mT_MIN() throws RecognitionException {
        try {
            int _type = T_MIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:76:7: ( 'Min' )
            // parse/GMLexer.g:76:9: 'Min'
            {
            match("Min"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_MIN"

    // $ANTLR start "T_P_INF"
    public final void mT_P_INF() throws RecognitionException {
        try {
            int _type = T_P_INF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:77:9: ( '+INF' | 'INF' )
            int alt15=2;
            switch ( input.LA(1) ) {
            case '+':
                {
                alt15=1;
                }
                break;
            case 'I':
                {
                alt15=2;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;

            }

            switch (alt15) {
                case 1 :
                    // parse/GMLexer.g:77:11: '+INF'
                    {
                    match("+INF"); 



                    }
                    break;
                case 2 :
                    // parse/GMLexer.g:77:20: 'INF'
                    {
                    match("INF"); 



                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_P_INF"

    // $ANTLR start "T_M_INF"
    public final void mT_M_INF() throws RecognitionException {
        try {
            int _type = T_M_INF;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:78:9: ( '-INF' )
            // parse/GMLexer.g:78:11: '-INF'
            {
            match("-INF"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_M_INF"

    // $ANTLR start "T_DOUBLE_COLON"
    public final void mT_DOUBLE_COLON() throws RecognitionException {
        try {
            int _type = T_DOUBLE_COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:79:16: ( '::' )
            // parse/GMLexer.g:79:18: '::'
            {
            match("::"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_DOUBLE_COLON"

    // $ANTLR start "T_ALL"
    public final void mT_ALL() throws RecognitionException {
        try {
            int _type = T_ALL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:80:7: ( 'All' )
            // parse/GMLexer.g:80:9: 'All'
            {
            match("All"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_ALL"

    // $ANTLR start "T_EXIST"
    public final void mT_EXIST() throws RecognitionException {
        try {
            int _type = T_EXIST;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:81:9: ( 'Exist' )
            // parse/GMLexer.g:81:11: 'Exist'
            {
            match("Exist"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_EXIST"

    // $ANTLR start "T_NIL"
    public final void mT_NIL() throws RecognitionException {
        try {
            int _type = T_NIL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:82:7: ( 'NIL' )
            // parse/GMLexer.g:82:9: 'NIL'
            {
            match("NIL"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_NIL"

    // $ANTLR start "T_RARROW"
    public final void mT_RARROW() throws RecognitionException {
        try {
            int _type = T_RARROW;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // parse/GMLexer.g:83:10: ( '->' )
            // parse/GMLexer.g:83:12: '->'
            {
            match("->"); 



            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        	// do for sure before leaving
        }
    }
    // $ANTLR end "T_RARROW"

    public void mTokens() throws RecognitionException {
        // parse/GMLexer.g:1:8: ( ID | FLOAT_NUM | INT_NUM | T_LOCAL | T_PROC | T_BFS | T_DFS | T_POST | T_RBFS | T_FROM | T_TO | T_BACK | T_GRAPH | T_NODE | T_EDGE | T_NODEPROP | T_EDGEPROP | T_NSET | T_NORDER | T_NSEQ | T_COLLECTION | T_INT | T_LONG | T_FLOAT | T_DOUBLE | T_BOOL | T_NODES | T_EDGES | T_NBRS | T_IN_NBRS | T_UP_NBRS | T_DOWN_NBRS | T_ITEMS | T_COMMON_NBRS | T_FOREACH | T_FOR | T_AND | T_OR | T_EQ | T_NEQ | T_LE | T_GE | BOOL_VAL | T_IF | T_ELSE | T_WHILE | T_RETURN | T_DO | T_PLUSEQ | T_PLUSPLUS | T_MULTEQ | T_ANDEQ | T_OREQ | T_MINEQ | T_MAXEQ | T_SUM | T_AVG | T_COUNT | T_PRODUCT | T_MAX | T_MIN | T_P_INF | T_M_INF | T_DOUBLE_COLON | T_ALL | T_EXIST | T_NIL | T_RARROW )
        int alt16=68;
        alt16 = dfa16.predict(input);
        switch (alt16) {
            case 1 :
                // parse/GMLexer.g:1:10: ID
                {
                mID(); 


                }
                break;
            case 2 :
                // parse/GMLexer.g:1:13: FLOAT_NUM
                {
                mFLOAT_NUM(); 


                }
                break;
            case 3 :
                // parse/GMLexer.g:1:23: INT_NUM
                {
                mINT_NUM(); 


                }
                break;
            case 4 :
                // parse/GMLexer.g:1:31: T_LOCAL
                {
                mT_LOCAL(); 


                }
                break;
            case 5 :
                // parse/GMLexer.g:1:39: T_PROC
                {
                mT_PROC(); 


                }
                break;
            case 6 :
                // parse/GMLexer.g:1:46: T_BFS
                {
                mT_BFS(); 


                }
                break;
            case 7 :
                // parse/GMLexer.g:1:52: T_DFS
                {
                mT_DFS(); 


                }
                break;
            case 8 :
                // parse/GMLexer.g:1:58: T_POST
                {
                mT_POST(); 


                }
                break;
            case 9 :
                // parse/GMLexer.g:1:65: T_RBFS
                {
                mT_RBFS(); 


                }
                break;
            case 10 :
                // parse/GMLexer.g:1:72: T_FROM
                {
                mT_FROM(); 


                }
                break;
            case 11 :
                // parse/GMLexer.g:1:79: T_TO
                {
                mT_TO(); 


                }
                break;
            case 12 :
                // parse/GMLexer.g:1:84: T_BACK
                {
                mT_BACK(); 


                }
                break;
            case 13 :
                // parse/GMLexer.g:1:91: T_GRAPH
                {
                mT_GRAPH(); 


                }
                break;
            case 14 :
                // parse/GMLexer.g:1:99: T_NODE
                {
                mT_NODE(); 


                }
                break;
            case 15 :
                // parse/GMLexer.g:1:106: T_EDGE
                {
                mT_EDGE(); 


                }
                break;
            case 16 :
                // parse/GMLexer.g:1:113: T_NODEPROP
                {
                mT_NODEPROP(); 


                }
                break;
            case 17 :
                // parse/GMLexer.g:1:124: T_EDGEPROP
                {
                mT_EDGEPROP(); 


                }
                break;
            case 18 :
                // parse/GMLexer.g:1:135: T_NSET
                {
                mT_NSET(); 


                }
                break;
            case 19 :
                // parse/GMLexer.g:1:142: T_NORDER
                {
                mT_NORDER(); 


                }
                break;
            case 20 :
                // parse/GMLexer.g:1:151: T_NSEQ
                {
                mT_NSEQ(); 


                }
                break;
            case 21 :
                // parse/GMLexer.g:1:158: T_COLLECTION
                {
                mT_COLLECTION(); 


                }
                break;
            case 22 :
                // parse/GMLexer.g:1:171: T_INT
                {
                mT_INT(); 


                }
                break;
            case 23 :
                // parse/GMLexer.g:1:177: T_LONG
                {
                mT_LONG(); 


                }
                break;
            case 24 :
                // parse/GMLexer.g:1:184: T_FLOAT
                {
                mT_FLOAT(); 


                }
                break;
            case 25 :
                // parse/GMLexer.g:1:192: T_DOUBLE
                {
                mT_DOUBLE(); 


                }
                break;
            case 26 :
                // parse/GMLexer.g:1:201: T_BOOL
                {
                mT_BOOL(); 


                }
                break;
            case 27 :
                // parse/GMLexer.g:1:208: T_NODES
                {
                mT_NODES(); 


                }
                break;
            case 28 :
                // parse/GMLexer.g:1:216: T_EDGES
                {
                mT_EDGES(); 


                }
                break;
            case 29 :
                // parse/GMLexer.g:1:224: T_NBRS
                {
                mT_NBRS(); 


                }
                break;
            case 30 :
                // parse/GMLexer.g:1:231: T_IN_NBRS
                {
                mT_IN_NBRS(); 


                }
                break;
            case 31 :
                // parse/GMLexer.g:1:241: T_UP_NBRS
                {
                mT_UP_NBRS(); 


                }
                break;
            case 32 :
                // parse/GMLexer.g:1:251: T_DOWN_NBRS
                {
                mT_DOWN_NBRS(); 


                }
                break;
            case 33 :
                // parse/GMLexer.g:1:263: T_ITEMS
                {
                mT_ITEMS(); 


                }
                break;
            case 34 :
                // parse/GMLexer.g:1:271: T_COMMON_NBRS
                {
                mT_COMMON_NBRS(); 


                }
                break;
            case 35 :
                // parse/GMLexer.g:1:285: T_FOREACH
                {
                mT_FOREACH(); 


                }
                break;
            case 36 :
                // parse/GMLexer.g:1:295: T_FOR
                {
                mT_FOR(); 


                }
                break;
            case 37 :
                // parse/GMLexer.g:1:301: T_AND
                {
                mT_AND(); 


                }
                break;
            case 38 :
                // parse/GMLexer.g:1:307: T_OR
                {
                mT_OR(); 


                }
                break;
            case 39 :
                // parse/GMLexer.g:1:312: T_EQ
                {
                mT_EQ(); 


                }
                break;
            case 40 :
                // parse/GMLexer.g:1:317: T_NEQ
                {
                mT_NEQ(); 


                }
                break;
            case 41 :
                // parse/GMLexer.g:1:323: T_LE
                {
                mT_LE(); 


                }
                break;
            case 42 :
                // parse/GMLexer.g:1:328: T_GE
                {
                mT_GE(); 


                }
                break;
            case 43 :
                // parse/GMLexer.g:1:333: BOOL_VAL
                {
                mBOOL_VAL(); 


                }
                break;
            case 44 :
                // parse/GMLexer.g:1:342: T_IF
                {
                mT_IF(); 


                }
                break;
            case 45 :
                // parse/GMLexer.g:1:347: T_ELSE
                {
                mT_ELSE(); 


                }
                break;
            case 46 :
                // parse/GMLexer.g:1:354: T_WHILE
                {
                mT_WHILE(); 


                }
                break;
            case 47 :
                // parse/GMLexer.g:1:362: T_RETURN
                {
                mT_RETURN(); 


                }
                break;
            case 48 :
                // parse/GMLexer.g:1:371: T_DO
                {
                mT_DO(); 


                }
                break;
            case 49 :
                // parse/GMLexer.g:1:376: T_PLUSEQ
                {
                mT_PLUSEQ(); 


                }
                break;
            case 50 :
                // parse/GMLexer.g:1:385: T_PLUSPLUS
                {
                mT_PLUSPLUS(); 


                }
                break;
            case 51 :
                // parse/GMLexer.g:1:396: T_MULTEQ
                {
                mT_MULTEQ(); 


                }
                break;
            case 52 :
                // parse/GMLexer.g:1:405: T_ANDEQ
                {
                mT_ANDEQ(); 


                }
                break;
            case 53 :
                // parse/GMLexer.g:1:413: T_OREQ
                {
                mT_OREQ(); 


                }
                break;
            case 54 :
                // parse/GMLexer.g:1:420: T_MINEQ
                {
                mT_MINEQ(); 


                }
                break;
            case 55 :
                // parse/GMLexer.g:1:428: T_MAXEQ
                {
                mT_MAXEQ(); 


                }
                break;
            case 56 :
                // parse/GMLexer.g:1:436: T_SUM
                {
                mT_SUM(); 


                }
                break;
            case 57 :
                // parse/GMLexer.g:1:442: T_AVG
                {
                mT_AVG(); 


                }
                break;
            case 58 :
                // parse/GMLexer.g:1:448: T_COUNT
                {
                mT_COUNT(); 


                }
                break;
            case 59 :
                // parse/GMLexer.g:1:456: T_PRODUCT
                {
                mT_PRODUCT(); 


                }
                break;
            case 60 :
                // parse/GMLexer.g:1:466: T_MAX
                {
                mT_MAX(); 


                }
                break;
            case 61 :
                // parse/GMLexer.g:1:472: T_MIN
                {
                mT_MIN(); 


                }
                break;
            case 62 :
                // parse/GMLexer.g:1:478: T_P_INF
                {
                mT_P_INF(); 


                }
                break;
            case 63 :
                // parse/GMLexer.g:1:486: T_M_INF
                {
                mT_M_INF(); 


                }
                break;
            case 64 :
                // parse/GMLexer.g:1:494: T_DOUBLE_COLON
                {
                mT_DOUBLE_COLON(); 


                }
                break;
            case 65 :
                // parse/GMLexer.g:1:509: T_ALL
                {
                mT_ALL(); 


                }
                break;
            case 66 :
                // parse/GMLexer.g:1:515: T_EXIST
                {
                mT_EXIST(); 


                }
                break;
            case 67 :
                // parse/GMLexer.g:1:523: T_NIL
                {
                mT_NIL(); 


                }
                break;
            case 68 :
                // parse/GMLexer.g:1:529: T_RARROW
                {
                mT_RARROW(); 


                }
                break;

        }

    }


    protected DFA16 dfa16 = new DFA16(this);
    static final String DFA16_eotS =
        "\1\uffff\1\35\1\42\16\35\6\uffff\2\35\2\uffff\2\35\3\uffff\1\35"+
        "\2\uffff\3\35\1\uffff\5\35\1\uffff\16\35\1\uffff\5\35\4\uffff\3"+
        "\35\3\uffff\3\35\2\uffff\7\35\1\uffff\2\35\1\uffff\7\35\4\uffff"+
        "\1\35\1\uffff\1\35\1\uffff\12\35\3\uffff\4\35\3\uffff\1\35\1\uffff"+
        "\11\35\1\uffff\3\35\1\uffff\2\35\1\uffff\1\35\1\uffff\6\35\1\uffff"+
        "\4\35\3\uffff\2\35\2\uffff\4\35\2\uffff\1\35\2\uffff\1\35\1\uffff"+
        "\1\35\2\uffff\2\35\1\uffff\4\35\1\uffff\3\35\2\uffff\1\35\1\uffff"+
        "\7\35\1\uffff\2\35\2\uffff\1\35\1\uffff\1\35\1\uffff\7\35\1\uffff"+
        "\3\35\1\uffff\5\35\3\uffff\10\35\1\uffff\1\35\2\uffff\6\35\3\uffff";
    static final String DFA16_eofS =
        "\u00fb\uffff";
    static final String DFA16_minS =
        "\1\41\1\157\1\56\1\162\1\116\1\141\1\157\1\162\1\111\1\137\3\157"+
        "\1\162\1\160\1\154\1\150\1\46\1\75\4\uffff\1\145\1\141\1\53\1\uffff"+
        "\1\165\1\141\1\uffff\1\76\1\uffff\1\143\2\uffff\1\157\1\102\1\145"+
        "\1\uffff\1\106\2\157\1\162\1\154\1\uffff\1\165\1\141\1\144\1\117"+
        "\1\162\1\114\1\147\1\120\1\163\1\151\1\154\1\165\1\157\1\164\1\uffff"+
        "\1\116\1\144\1\147\1\154\1\151\4\uffff\1\164\1\156\1\170\3\uffff"+
        "\1\155\1\170\1\156\2\uffff\1\141\1\147\1\143\2\106\1\157\1\102\1"+
        "\uffff\1\142\1\155\1\uffff\1\155\1\141\1\145\1\163\1\145\1\160\1"+
        "\145\4\uffff\1\163\1\uffff\1\145\1\uffff\1\145\1\163\1\154\1\155"+
        "\1\156\1\142\1\156\1\154\1\116\1\142\3\uffff\1\154\1\165\2\75\3"+
        "\uffff\1\154\1\uffff\1\145\1\165\2\123\1\163\1\106\1\166\1\162\1"+
        "\163\1\uffff\1\164\1\141\1\145\1\uffff\1\150\1\137\1\uffff\1\137"+
        "\1\uffff\1\164\1\145\1\157\1\164\1\154\1\116\1\uffff\1\142\1\162"+
        "\1\145\1\162\3\uffff\1\144\1\143\2\uffff\1\164\1\123\1\145\1\163"+
        "\2\uffff\1\143\2\uffff\1\117\1\uffff\1\120\2\uffff\1\143\1\156\1"+
        "\uffff\1\145\1\142\1\162\1\163\1\uffff\1\156\1\165\1\164\2\uffff"+
        "\1\162\1\uffff\1\150\1\162\1\145\2\162\1\164\1\116\1\uffff\1\162"+
        "\1\163\2\uffff\1\162\1\uffff\1\163\1\uffff\1\157\1\161\1\144\1\157"+
        "\1\151\1\142\1\163\1\uffff\2\145\1\160\1\uffff\1\165\1\145\1\160"+
        "\1\157\1\162\3\uffff\2\145\1\162\1\145\1\156\1\163\1\162\1\156\1"+
        "\uffff\1\162\2\uffff\1\164\1\143\1\164\1\171\1\145\1\171\3\uffff";
    static final String DFA16_maxS =
        "\1\174\1\157\1\71\1\162\1\164\3\162\1\157\1\170\3\157\1\165\1\160"+
        "\1\166\1\150\1\75\1\174\4\uffff\1\145\1\151\1\111\1\uffff\1\165"+
        "\1\151\1\uffff\1\111\1\uffff\1\156\2\uffff\1\157\1\164\1\145\1\uffff"+
        "\1\106\2\157\1\162\1\154\1\uffff\1\165\1\141\1\144\1\123\1\162\1"+
        "\114\1\147\1\120\1\163\1\151\1\165\1\167\1\157\1\164\1\uffff\1\116"+
        "\1\144\1\147\1\154\1\151\4\uffff\1\164\1\156\1\170\3\uffff\1\155"+
        "\1\170\1\156\2\uffff\1\141\1\147\1\144\2\106\1\157\1\145\1\uffff"+
        "\1\142\1\155\1\uffff\1\155\1\141\1\145\1\163\1\145\1\160\1\145\4"+
        "\uffff\1\163\1\uffff\1\145\1\uffff\1\145\1\163\1\154\1\155\1\156"+
        "\1\142\1\156\1\154\1\116\1\142\3\uffff\1\154\1\165\2\75\3\uffff"+
        "\1\154\1\uffff\1\145\1\165\2\123\1\163\1\106\1\166\1\162\1\163\1"+
        "\uffff\1\164\1\141\1\145\1\uffff\1\150\1\163\1\uffff\1\163\1\uffff"+
        "\1\164\1\145\1\157\1\164\1\154\1\116\1\uffff\1\142\1\162\1\145\1"+
        "\162\3\uffff\1\144\1\143\2\uffff\1\164\1\123\1\145\1\163\2\uffff"+
        "\1\143\2\uffff\1\123\1\uffff\1\120\2\uffff\1\143\1\156\1\uffff\1"+
        "\145\1\142\1\162\1\163\1\uffff\1\156\1\165\1\164\2\uffff\1\162\1"+
        "\uffff\1\150\1\162\1\145\2\162\1\164\1\116\1\uffff\1\162\1\163\2"+
        "\uffff\1\162\1\uffff\1\163\1\uffff\1\157\1\164\1\144\1\157\1\151"+
        "\1\142\1\163\1\uffff\2\145\1\160\1\uffff\1\165\1\145\1\160\1\157"+
        "\1\162\3\uffff\2\145\1\162\1\145\1\156\1\163\1\162\1\156\1\uffff"+
        "\1\162\2\uffff\1\164\1\143\1\164\1\171\1\145\1\171\3\uffff";
    static final String DFA16_acceptS =
        "\23\uffff\1\47\1\50\1\51\1\52\3\uffff\1\63\2\uffff\1\1\1\uffff\1"+
        "\100\1\uffff\1\2\1\3\3\uffff\1\1\5\uffff\1\1\16\uffff\1\1\5\uffff"+
        "\1\45\1\64\1\46\1\65\3\uffff\1\61\1\62\1\76\3\uffff\1\77\1\104\7"+
        "\uffff\1\1\2\uffff\1\1\7\uffff\4\1\1\uffff\1\1\1\uffff\1\1\12\uffff"+
        "\3\1\4\uffff\3\1\1\uffff\1\1\11\uffff\1\1\3\uffff\1\1\2\uffff\1"+
        "\1\1\uffff\1\1\6\uffff\1\1\4\uffff\1\66\1\67\1\1\2\uffff\2\1\4\uffff"+
        "\2\1\1\uffff\2\1\1\uffff\1\1\1\uffff\2\1\2\uffff\1\1\4\uffff\1\1"+
        "\3\uffff\2\1\1\uffff\1\1\7\uffff\1\1\2\uffff\2\1\1\uffff\1\1\1\uffff"+
        "\1\1\7\uffff\1\1\3\uffff\1\1\5\uffff\3\1\10\uffff\1\1\1\uffff\2"+
        "\1\6\uffff\3\1";
    static final String DFA16_specialS =
        "\u00fb\uffff}>";
    static final String[] DFA16_transitionS = {
            "\1\24\4\uffff\1\21\3\uffff\1\32\1\31\1\uffff\1\36\2\uffff\12"+
            "\2\1\37\1\uffff\1\25\1\23\1\26\2\uffff\1\17\1\14\1\12\1\13\1"+
            "\11\1\5\1\7\1\35\1\4\2\35\1\1\1\34\1\10\1\15\1\3\1\35\1\27\1"+
            "\33\1\6\1\16\1\35\1\20\3\35\6\uffff\14\35\1\30\15\35\1\uffff"+
            "\1\22",
            "\1\40",
            "\1\41\1\uffff\12\2",
            "\1\43",
            "\1\47\27\uffff\1\46\7\uffff\1\44\5\uffff\1\45",
            "\1\53\12\uffff\1\51\2\uffff\1\52\2\uffff\1\50",
            "\1\54\2\uffff\1\55",
            "\1\56",
            "\1\62\25\uffff\1\60\2\uffff\1\61\14\uffff\1\57",
            "\1\64\4\uffff\1\63\7\uffff\1\65\13\uffff\1\66",
            "\1\67",
            "\1\70",
            "\1\71",
            "\1\73\2\uffff\1\72",
            "\1\74",
            "\1\77\1\uffff\1\75\7\uffff\1\76",
            "\1\100",
            "\1\101\26\uffff\1\102",
            "\1\104\76\uffff\1\103",
            "",
            "",
            "",
            "",
            "\1\105",
            "\1\107\7\uffff\1\106",
            "\1\111\21\uffff\1\110\13\uffff\1\112",
            "",
            "\1\113",
            "\1\114\7\uffff\1\115",
            "",
            "\1\117\12\uffff\1\116",
            "",
            "\1\120\12\uffff\1\121",
            "",
            "",
            "\1\122",
            "\1\123\1\uffff\1\124\11\uffff\1\130\1\uffff\1\125\1\uffff\1"+
            "\126\41\uffff\1\127",
            "\1\131",
            "",
            "\1\132",
            "\1\133",
            "\1\134",
            "\1\135",
            "\1\136",
            "",
            "\1\137",
            "\1\140",
            "\1\141",
            "\1\144\1\142\1\145\1\uffff\1\143",
            "\1\146",
            "\1\147",
            "\1\150",
            "\1\151",
            "\1\152",
            "\1\153",
            "\1\154\1\155\7\uffff\1\156",
            "\1\157\1\uffff\1\160",
            "\1\161",
            "\1\162",
            "",
            "\1\163",
            "\1\164",
            "\1\165",
            "\1\166",
            "\1\167",
            "",
            "",
            "",
            "",
            "\1\170",
            "\1\171",
            "\1\172",
            "",
            "",
            "",
            "\1\173",
            "\1\174",
            "\1\175",
            "",
            "",
            "\1\176",
            "\1\177",
            "\1\u0080\1\u0081",
            "\1\u0082",
            "\1\u0083",
            "\1\u0084",
            "\1\u0085\42\uffff\1\u0086",
            "",
            "\1\u0087",
            "\1\u0088",
            "",
            "\1\u0089",
            "\1\u008a",
            "\1\u008b",
            "\1\u008c",
            "\1\u008d",
            "\1\u008e",
            "\1\u008f",
            "",
            "",
            "",
            "",
            "\1\u0090",
            "",
            "\1\u0091",
            "",
            "\1\u0092",
            "\1\u0093",
            "\1\u0094",
            "\1\u0095",
            "\1\u0096",
            "\1\u0097",
            "\1\u0098",
            "\1\u0099",
            "\1\u009a",
            "\1\u009b",
            "",
            "",
            "",
            "\1\u009c",
            "\1\u009d",
            "\1\u009e",
            "\1\u009f",
            "",
            "",
            "",
            "\1\u00a0",
            "",
            "\1\u00a1",
            "\1\u00a2",
            "\1\u00a3",
            "\1\u00a4",
            "\1\u00a5",
            "\1\u00a6",
            "\1\u00a7",
            "\1\u00a8",
            "\1\u00a9",
            "",
            "\1\u00aa",
            "\1\u00ab",
            "\1\u00ac",
            "",
            "\1\u00ad",
            "\1\u00ae\23\uffff\1\u00af",
            "",
            "\1\u00b0\23\uffff\1\u00b1",
            "",
            "\1\u00b2",
            "\1\u00b3",
            "\1\u00b4",
            "\1\u00b5",
            "\1\u00b6",
            "\1\u00b7",
            "",
            "\1\u00b8",
            "\1\u00b9",
            "\1\u00ba",
            "\1\u00bb",
            "",
            "",
            "",
            "\1\u00bc",
            "\1\u00bd",
            "",
            "",
            "\1\u00be",
            "\1\u00bf",
            "\1\u00c0",
            "\1\u00c1",
            "",
            "",
            "\1\u00c2",
            "",
            "",
            "\1\u00c5\1\u00c3\2\uffff\1\u00c4",
            "",
            "\1\u00c6",
            "",
            "",
            "\1\u00c7",
            "\1\u00c8",
            "",
            "\1\u00c9",
            "\1\u00ca",
            "\1\u00cb",
            "\1\u00cc",
            "",
            "\1\u00cd",
            "\1\u00ce",
            "\1\u00cf",
            "",
            "",
            "\1\u00d0",
            "",
            "\1\u00d1",
            "\1\u00d2",
            "\1\u00d3",
            "\1\u00d4",
            "\1\u00d5",
            "\1\u00d6",
            "\1\u00d7",
            "",
            "\1\u00d8",
            "\1\u00d9",
            "",
            "",
            "\1\u00da",
            "",
            "\1\u00db",
            "",
            "\1\u00dc",
            "\1\u00de\2\uffff\1\u00dd",
            "\1\u00df",
            "\1\u00e0",
            "\1\u00e1",
            "\1\u00e2",
            "\1\u00e3",
            "",
            "\1\u00e4",
            "\1\u00e5",
            "\1\u00e6",
            "",
            "\1\u00e7",
            "\1\u00e8",
            "\1\u00e9",
            "\1\u00ea",
            "\1\u00eb",
            "",
            "",
            "",
            "\1\u00ec",
            "\1\u00ed",
            "\1\u00ee",
            "\1\u00ef",
            "\1\u00f0",
            "\1\u00f1",
            "\1\u00f2",
            "\1\u00f3",
            "",
            "\1\u00f4",
            "",
            "",
            "\1\u00f5",
            "\1\u00f6",
            "\1\u00f7",
            "\1\u00f8",
            "\1\u00f9",
            "\1\u00fa",
            "",
            "",
            ""
    };

    static final short[] DFA16_eot = DFA.unpackEncodedString(DFA16_eotS);
    static final short[] DFA16_eof = DFA.unpackEncodedString(DFA16_eofS);
    static final char[] DFA16_min = DFA.unpackEncodedStringToUnsignedChars(DFA16_minS);
    static final char[] DFA16_max = DFA.unpackEncodedStringToUnsignedChars(DFA16_maxS);
    static final short[] DFA16_accept = DFA.unpackEncodedString(DFA16_acceptS);
    static final short[] DFA16_special = DFA.unpackEncodedString(DFA16_specialS);
    static final short[][] DFA16_transition;

    static {
        int numStates = DFA16_transitionS.length;
        DFA16_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA16_transition[i] = DFA.unpackEncodedString(DFA16_transitionS[i]);
        }
    }

    class DFA16 extends DFA {

        public DFA16(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 16;
            this.eot = DFA16_eot;
            this.eof = DFA16_eof;
            this.min = DFA16_min;
            this.max = DFA16_max;
            this.accept = DFA16_accept;
            this.special = DFA16_special;
            this.transition = DFA16_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( ID | FLOAT_NUM | INT_NUM | T_LOCAL | T_PROC | T_BFS | T_DFS | T_POST | T_RBFS | T_FROM | T_TO | T_BACK | T_GRAPH | T_NODE | T_EDGE | T_NODEPROP | T_EDGEPROP | T_NSET | T_NORDER | T_NSEQ | T_COLLECTION | T_INT | T_LONG | T_FLOAT | T_DOUBLE | T_BOOL | T_NODES | T_EDGES | T_NBRS | T_IN_NBRS | T_UP_NBRS | T_DOWN_NBRS | T_ITEMS | T_COMMON_NBRS | T_FOREACH | T_FOR | T_AND | T_OR | T_EQ | T_NEQ | T_LE | T_GE | BOOL_VAL | T_IF | T_ELSE | T_WHILE | T_RETURN | T_DO | T_PLUSEQ | T_PLUSPLUS | T_MULTEQ | T_ANDEQ | T_OREQ | T_MINEQ | T_MAXEQ | T_SUM | T_AVG | T_COUNT | T_PRODUCT | T_MAX | T_MIN | T_P_INF | T_M_INF | T_DOUBLE_COLON | T_ALL | T_EXIST | T_NIL | T_RARROW );";
        }
    }
 

}