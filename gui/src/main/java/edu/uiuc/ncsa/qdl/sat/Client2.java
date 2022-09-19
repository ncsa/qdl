package edu.uiuc.ncsa.qdl.sat;

/**
 * This client pairs with {@link edu.uiuc.ncsa.qdl.sat.Server2}. Run both at the same time.
 * It is a toy (example from web) and just sends the line typed to the server then blocks until you type
 * a response in the server. Nice to see the back and forth and about as simple as possible.
 * <p>Created by Jeff Gaynor<br>
 * on 9/7/22 at  6:55 AM
 */


import java.io.*;
import java.net.*;

class Client2 {

    public static void main(String args[])
        throws Exception
    {

        // Create client socket
        Socket s = new Socket("localhost", 5555);

        // to send data to the server
        DataOutputStream dos
            = new DataOutputStream(
                s.getOutputStream());

        // to read data coming from the server
        BufferedReader br
            = new BufferedReader(
                new InputStreamReader(
                    s.getInputStream()));

        // to read data from the keyboard
        BufferedReader kb
            = new BufferedReader(
                new InputStreamReader(System.in));
        String str, str1;

        // repeat as long as exit
        // is not typed at client
        while (!(str = kb.readLine()).equals("exit")) {

            // send to the server
            dos.writeBytes(str + "\n");

            // receive from the server
            str1 = br.readLine();

            System.out.println(str1);
        }

        // close connection.
        dos.close();
        br.close();
        kb.close();
        s.close();
    }
}

