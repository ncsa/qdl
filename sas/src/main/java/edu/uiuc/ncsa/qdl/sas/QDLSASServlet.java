package edu.uiuc.ncsa.qdl.sas;

import edu.uiuc.ncsa.sas.Executable;
import edu.uiuc.ncsa.sas.SASServlet;

/**
 * The SAS Servlet.It really only needs to create the correct executable, so there is not
 * a lot to it.
 * <p>Created by Jeff Gaynor<br>
 * on 8/20/22 at  7:44 AM
 */
public class QDLSASServlet extends SASServlet {
    @Override
    protected Executable createExecutable() {
        return new QDLExe();
    }
}
