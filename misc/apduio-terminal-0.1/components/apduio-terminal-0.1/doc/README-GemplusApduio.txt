
                                      
                                      
      Apduio CardTerminal for the Java Card Development Kit Simulator
                                      
`com.gemplus.opencard.terminal.apduio'

 

   Note: If you are reading the text (ASCII) version of this page
   (doc/README-GemplusApduio.txt), be informed that there is an hypertext
   (HTML) version which exactly matches the text version at the following
   location:
   components/gemplus-apduio-0.1/doc/README-GemplusApduio.html
     _________________________________________________________________
   
Abstract

   This page is intended for Java Card applications developers; it is
   dedicated to the Gemplus Apduio CardTerminal Java package(s) that can
   be used together with the Sun's Java Card 2.1.2 Development Kit and
   the "OpenCard Framework". Among other things, it describes how to
   download, install, and use this CardTerminal.
   
   Note: This is a first version and is provided AS-IS. See the Important
   Note below.
     _________________________________________________________________
   
   IMPORTANT NOTE: The presented Java software code
   is an alpha-version for developers and IS PROVIDED AS-IS by Gemplus.
   Please see the LICENSE file for more details. Therefore, this code has
   not been tested by many people yet; it may contain bugs or even
   semantic misbehaviour; and it has not been reviewed as a supported
   Gemplus product; so comments, suggestions, problem reports, or even
   enhancements are highly welcome.
   
    Related Reference Documentation:
    
   The LICENSE file for this packages.

   Opencard 1.2 Framework documentation: see the Opencard Web Site for
   Programmer's Guide, Reference Javadocs, etc.

   Java Card documentation: see the Sun Java Card Web Site for Tutorials,
   Programmer's Guide, Reference Javadocs, etc. See also the Java Card
   2.1.2 Development Kit download page. The kit includes its own
   documentation (e.g., Java Card 2.1.2 Development Kit User's Guide).

   com.gemplus.opencard.terminal.apduio package Reference Documents
   generated by the Javadoc tool from Sun Microsystems.

   com.gemplus.opencard.terminal.apduio package Sources generated by the
   Java2html tool.
     _________________________________________________________________
   
Distribution Availibility

License Policy

   This package is free software provided with sources by Gemplus. This
   means that you should have received a copy of the source code with the
   executable form of this component (i.e., the JAR files), or if it is
   not the case, you can get it at the location given just below. Also,
   you have the rights to use it, adapt it for your needs, and
   redistribute copies of the component, whether it has been modified or
   not. See our LICENSE file, compliant with the OpenSource definition,
   for more details on redistributions conditions (basically do not
   advertise using the Gemplus name and retain the original copyright,
   even in the case of modifications).
   
   This package is provided AS-IS. See the Important Note section above.
   A disclaimer of warranty and limitation of liability is included in
   the License.
   
   You are free to use the Gemplus Apduio CardTerminal, or write your
   own, starting from scratch, or adapt this one. In the case where you
   have fixed problems or make enhancements to this package, we would
   like to get those changes back in order to integrate them for the
   benefit of most people.
   
   
Primary Web Location

   This component can be downloaded from the Gemplus Developer's Web Site
   OpenCard Page (in the "Technologies" section).
   
   This web site is the developer's section of the general Gemplus web
   and it needs a registration before any downloading of code. It is a
   simple form requesting an email address and a password (similar to the
   Java developer's section).
   
   In the OpenCard Page, you will find links to the CardTerminals
   Download Page . In case you are reading the ASCII version of this
   README file, the download page location is the following URL:
   http://www.gemplus.fr/developers/technologies/opencard/cardterminals/d
   ownload.html
     _________________________________________________________________
   
Software Organization

Archive Contents

   The Apduio CardTerminal is distributed in a ZIP or in a TAR.GZ archive
   file. A detailed content of the archive can be found in the following
   automatically-generated Directory Hierarchy.
   
   To summarize this hierarchy, the major files and directories found in
   the "gemplus-apduio-0.1.{zip,tar.gz}" archive are:

    "doc/README-GemplusApduio.txt": the ASCII version of this file,

    "doc/LICENSE-GemplusApduio.txt": copyright statements,

    "lib/gemplus-apduio-0.1.jar": a ready to use Java JAR file,

    "components/gemplus-apduio-0.1/doc/": package documentation,

    "components/gemplus-apduio-0.1/src/": package source code,

    "components/gemplus-apduio-0.1/doc/README-GemplusApduio.html":
       this file (HTML version),

    "images/": miscellaneous icons.

Features

   The Gemplus Apduio CardTerminal is an implementation of an OCF
   CardTerminal to access the Java Card 2.1.2 Simulator distributed by
   Sun Microsystems in its Java Card 2.1.2 Development Kit
     _________________________________________________________________
   
Installation and User's Guide

For installing Apduio CardTerminal

   See the Download Page for instructions on how to download and install
   the Apduio CardTerminal component on your platform.
   
   
Additional Prerequisites

   In addition to the prerequisites listed for all OCF components (i.e.,
   JRE, Comm API, and OCF), there are some additional required software
   packages:

     * The Java Card 2.1.2 Simulator distributed by Sun Microsystems is
       of course mandatory in order to use this CardTerminal. Please
       download and install the complete Java Card 2.1.2 Development Kit.
       
   
For using Apduio CardTerminal

   This section shows how to run a simple test with this CardTerminal
   (Apduio).
   
    1. download and install the Java Card 2.1.2 Development Kit.
    2. execute the demo2 sample, and produce the "demoee" file, as
       described in the Chapter 3 of the Java Card 2.1.2 Development Kit
       Users Guide.
    3. download and install the distribution.
    4. configure your "opencard.properties" file in order to use the
       Gemplus Apduio factory (i.e., ApduioCardTerminalFactory) for the
       OpenCard.terminals property and the PassThruCardServiceFactory for
       OpenCard.services.
       Example: (warning: the lines are not cut!):

OpenCard.services = opencard.opt.util.
PassThruCardServiceFactory

OpenCard.terminals = com.gemplus.opencard.terminal.apduio.
ApduIOCardTerminalFactory|mySim|Socket|localhost:9025

       Note that "Socket" and the specification of port "9025" are
       mandatory. However, "localhost" can be replaced by any machine
       name that can be accessed by TCP/IP (and on which the simulator is
       running).
    5. test the component with the
       "com.gemplus.opencard.terminal.apduio.test.Test" program.
       Example:
         1. Type ('$' being a shell prompt):
     $ java com.gemplus.opencard.terminal.apduio.test.Test

         2. In another console, run the cref for demo3, as described in
            the Chapter 3 of the Java Card 2.1.2 Development Kit Users
            Guide.
            I.e., type ('$' being a shell prompt):
     $ cref -i demoee

         3. In the console running Test, you should see the following
            output:
     Card Inserted
     Balance = 0x0032
     Balance = 0x0022
         4. In the console running the cref, hit CTRL+C, then in the
            console running Test, you should see the following output:
     Card Removed

   Note that this code is currently in alpha version and so limited
   User's documentation is provided at this stage.
   
   Please See the Javadocs for more details on the package API.
   
   
Limitations

   There are no known limitations for this component.
     _________________________________________________________________
   
Developer's Notes

Sample Code

   In order to have an example of how to use the ApduioCardTerminal to
   communicate with a JavaCard applet, see the code of the following
   class: com.gemplus.opencard.terminal.apduio.test.Test.
   
   
Complete Sources

   Sources of the current version (V 0.1) have been compiled into
   hypertext (HTML) versions in the
   "components/apduio-terminal-0.1/doc/sources/" subdirectory.
   
   Otherwise, for looking at the sources of previous versions:
    1. $ cd components/apduio-terminal-0.1/src/
    2. and use RCS's command co(1) to check out the files.
       
   If you intend to work with RCS in order to retrieve, install, and/or
   edit previous versions, see also the RCS change logs that are provided
   in Appendix.
   
   
Troubleshooting

   This section provides tricks that are nice to know when using the
   component. It should also be enhanced in the future with a FAQ of
   common encountered problems.

     * No known problems,... yet.
     _________________________________________________________________
   
                                 Appendixes
     _________________________________________________________________
   
Appendix A. Change Logs

   In order to help tracking new features and bug fixes, a listing of
   previous RCS change logs for the main file of the package (i.e., for
   MANIFEST) is provided below.
============================================================
Log files for MANIFEST (from version 0.0 to now)
============================================================
----------------------------
revision 0.1
date: 2001/07/05 08:35:01
First distributed version
============================================================
     _________________________________________________________________
   
Appendix B. GLOSSARY

   This appendix is informative.
   
   
   APDU
   
   An APDU, Application Protocol Data Unit, is the basic command unit for
   a smart card. An APDU contains either a command message or a response
   message, sent from the card reader to the smart card or from the card
   to the reader.
     _________________________________________________________________
   
   
   API
   
   An API, Application Programming Interface, is an abstract definition
   of a service that can be provided either by a server, a component, an
   object class, etc. In the Java world, an API can be a Java Interface
   or a set of interfaces and abstract or concrete classes. For servers
   or other computing languages, it can be a set or functions (such as
   the C-based PKCS#11 API) or a language independent API defined in an
   IDL, such as the OMG IDL.
     _________________________________________________________________
   
   
   OCF, OpenCard Framework
   
   OCF, the OpenCard Framework, is a standardized, easy-to-use framework
   for implementing Smartcard-enabled solutions and Smartcard-based
   services. OpenCard Framework capitalizes on the broad, cross-platform
   benefits of Java, providing an open architecture and a set of common
   APIs (Application Program Interfaces) geared for this purpose. For
   more details about the basic architecture, concepts, and objectives of
   the OpenCard Framework, see the General Information Web Document and
   other documentation.
     _________________________________________________________________
     _________________________________________________________________
   
Appendix C. References

   This appendix is informative.
   
   [Java Intro]
          "The Java Tutorial Second Edition", by Mary Campione, Kathy
          Walrath, Addison-Wesley, March 1998.
          Available at:
          http://java.sun.com/docs/books/tutorial/index.html
          
   [Java Documentation]
          "Java Platform Documentation" Home-page, by Sun Microsystems.
          Available at: http://java.sun.com/docs/index.html
          
   [Opencard Intro]
          "OpenCard Framework -- General Information Web Document", by
          "The OpenCard Consortium", October 1998.
          Available at: http://www.opencard.org/docs/gim/ocfgim.html
          
   [Opencard Whitepaper]
          "OpenCard Framework", by Reto Hermann, Dirk Husemann (IBM
          Research Division).
          Available at:
          http://www.ibm.com/java/education/opencard-framework/
          
   [Opencard Programming]
          "OpenCard Framework 1.1.1 Programmer's Guide", by "The OpenCard
          Consortium", April 1999.
          Available at: http://www.opencard.org/docs/pguide/PGuide.html
          
   [Smart Card Application Development Using Java]
          "Smart Card Application Development Using Java", by Hansmann,
          Uwe; Nicklous, Martin S.; Sch?ck, Thomas and Seliger, Frank,
          ISBN 3-540-65829-7, Springer Heidelberg, 1999.
          Information about this book is available at:
          http://www.opencard.org/SCJavaBook
          
   [Javacard]
          "Java Card Technology" Home-Page, by Sun Microsystems.
          Available at:
          http://java.sun.com/products/javacard/index.html#presentations
     _________________________________________________________________
   
Contacts

   Several communications tools are available,
   please select the right one according to your needs, i.e.,:

     * For submitting source code enhancements or patches to problems,
       send an email directly to the component author.

     * For help on usage, or submissions of suggestions/problem reports,
       post your comments or questions to the Gemplus Developer's Site
       Forum (in the topic named "OCF Forum"),

     * At last, for any comments on the OpenCard framework (i.e., not
       directly related to the Java Card simulator CardTerminal issues),
       post your comments or questions to the OpenCard mailing-list
       (instructions on how to subscribe are available on the OpenCard
       Home-Page).
     _________________________________________________________________
   
   Last changes: Thu Jul 5 2001
