/*************************************************************************
 *  Compilation:  javac LZW.java
 *  Execution:    java LZW - < input.txt   (compress)
 *  Execution:    java LZW + < input.txt   (expand)
 *  Dependencies: BinaryIn.java BinaryOut.java
 *
 *  Compress or expand binary input from standard input using LZW.
 *
 *  WARNING: STARTING WITH ORACLE JAVA 6, UPDATE 7 the SUBSTRING
 *  METHOD TAKES TIME AND SPACE LINEAR IN THE SIZE OF THE EXTRACTED
 *  SUBSTRING (INSTEAD OF CONSTANT SPACE AND TIME AS IN EARLIER
 *  IMPLEMENTATIONS).
 *
 *  See <a href = "http://java-performance.info/changes-to-string-java-1-7-0_06/">this article</a>
 *  for more details.
 *
 * Dannah Gersh
 * CoE 1501
 * MW 9:30-10:45AM
 *************************************************************************/

public class myLZW {
    private static final int R = 256;        // number of input chars
    private static int L = 512;       // number of codewords = 2^W
    private static int W = 9;         // codeword width
    private static String mode = "n";
    private static double sRatio = 0;
    private static double curRatio = 0;
    private static double num = 0;
    private static double den = 0;
    private static boolean noRatio = true;

    public static void compress() {
        if(mode.equals("r")) BinaryStdOut.write('r', 8);
        if(mode.equals("n")) BinaryStdOut.write('n', 8);
        if(mode.equals("m")) BinaryStdOut.write('m', 8);
        
        String input = BinaryStdIn.readString();
        TST<Integer> st = new TST<Integer>();
        for (int i = 0; i < R; i++){
            st.put("" + (char) i, i);
        }
        int code = R+1;  // R is codeword for EOF

        while (input.length() > 0) {
            String s = st.longestPrefixOf(input);  // Find max prefix match s.
            BinaryStdOut.write(st.get(s), W);      // Print s's encoding.
            int t = s.length();
            
            num += s.length()*8;
            den += W;
            curRatio = num/den;
            
            if (t < input.length() && code < L)    // Add s to symbol table.
                st.put(input.substring(0, t + 1), code++);
            if(W<16 && code == L){
                st.put(input.substring(0, t + 1), code++);
                L = L*2;
                W++;
            }
            //else if(t<input.length()){
                if(mode.equals("r") && code == 65536){
                    st = new TST<Integer>();
                    for(int i=0; i<R; i++){
                        st.put("" + (char)i , i);
                    }
                    code = R+1;
                    W = 9;
                    L = 512;
                }
                if(mode.equals("m") && code == 65536){
                    if(noRatio){
                        sRatio = curRatio;
                        noRatio = false;
                    }
                    
                    if(sRatio/curRatio > 1.1){
                        st = new TST<Integer>();
                        for(int i=0; i<R; i++){
                            st.put("" + (char)i , i);
                        }
                        code = R+1;
                        W = 9;
                        L = 512;
                        sRatio = 0;
                        curRatio = 0;
                        noRatio = true;
                    }
                }
            //}
            
            input = input.substring(t);            // Scan past s in input.
            
        }
        BinaryStdOut.write(R, W);
        BinaryStdOut.close();
    } 


    public static void expand() {
        
        char c;
        c = BinaryStdIn.readChar(8);
        if(c == 'r') mode = "r";
        if(c == 'm') mode = "m";
        if(c == 'n') mode = "n";
        L = 2*2*2*2*2*2*2*2*2;
        String[] st = new String[65536];
        int i; // next available codeword value

        // initialize symbol table with all 1-character strings
        for (i = 0; i < R; i++)
            st[i] = "" + (char)i;
        st[i] = "";  // (unused) lookahead for EOF
        i++;
        //StdOut.print(i);
            
        
        //StdOut.print(W);
        int codeword = BinaryStdIn.readInt(W);
        if (codeword == R) return;           // expanded message is empty string
        String val = st[codeword];
        //StdOut.print(val);

        while (true) {
            BinaryStdOut.write(val);
            num += val.length()*8;
            codeword = BinaryStdIn.readInt(W);
            den += W;
            curRatio = num/den;
            if (codeword == R) break;
            String s = st[codeword];
            if (i == codeword) s = val + val.charAt(0);   // special case hack
            //StdOut.print(s);
            /*System.err.print(i);
            System.err.print("\t");
            System.err.print(L);
            System.err.print("\t");
            System.err.print(W);
            System.err.print("\t");*/
 
            //StdOut.print(st[i]);
            
            if(i < L-1){ 
                
                /*System.err.print(s);
                System.err.print("\n");*/
                st[i] = val + s.charAt(0);
                i++;
            }
            
            if(i == L-1 && W < 16){
                st[i] = val + s.charAt(0);
                i++;
                W++;
                L = L*2;
            }
            val = s;
            
            if(mode.equals("r") && i == 65535){
                W = 9;
                L = 512;
                st = new String[65536];
                for(i = 0; i<R; i++){
                    st[i] = ""+ (char)i;
                }
                st[i++] = "";
                
                codeword = BinaryStdIn.readInt(W);
                if(codeword == R) return;
                val = st[codeword];
               
            }
            
            if(i == 65535 && mode.equals("m")){
                if(noRatio){
                    sRatio = curRatio;
                    noRatio = false;
                }
                if(sRatio/curRatio > 1.1){
                    W = 9;
                    L = 512;
                    st = new String[65536];
                    for(i = 0; i<R; i++){
                        st[i] = ""+ (char)i;
                    }
                    st[i] = "";
                    i++;

                    codeword = BinaryStdIn.readInt(W);
                    if(codeword == R) return;
                    val = st[codeword];
                    sRatio = 0;
                    curRatio = 0;
                    noRatio = true;
                }
            }
        }
        BinaryStdOut.close();
    }



    public static void main(String[] args) {
        if(args[0].equals("+")) expand();
        else if (args[0].equals("-") && args[1].equals("n")){ 
            mode = "n";
            compress();
        }
        else if (args[0].equals("-") && args[1].equals("r")){ 
            mode = "r";
            compress();
        }
        else if (args[0].equals("-") && args[1].equals("m")){ 
            mode = "m";
            compress();
        }
        else throw new IllegalArgumentException("Illegal command line argument");
    }

}
