QDL's SAS implementation. To test, make sure the following is in the system's web.xml
(or in the local one if you prefer):

   <context-param>
        <param-name>sat:server.config.file</param-name>
        <param-value>/home/ncsa/dev/csd/config/sat.xml</param-value>
    </context-param>

    <context-param>
        <param-name>sat:server.config.name</param-name>
        <param-value>default</param-value>
    </context-param>

The two files of interest are

       /home/ncsa/dev/csd/config/sas/sat.xml -- the server's configuration
/home/ncsa/dev/csd/config/sas/sas-config.xml -- the client's configuration

Typical client command line arguments to QDLSASTer:
-cfg /home/ncsa/dev/csd/config/sas/sas-config.xml -gui