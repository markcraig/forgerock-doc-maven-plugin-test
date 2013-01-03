<!--
  ! CCPL HEADER START
  !
  ! This work is licensed under the Creative Commons
  ! Attribution-NonCommercial-NoDerivs 3.0 Unported License.
  ! To view a copy of this license, visit
  ! http://creativecommons.org/licenses/by-nc-nd/3.0/
  ! or send a letter to Creative Commons, 444 Castro Street,
  ! Suite 900, Mountain View, California, 94041, USA.
  !
  ! See the License for the specific language governing permissions
  ! and limitations under the License.
  !
  ! If applicable, add the following below this CCPL HEADER, with the fields
  ! enclosed by brackets "[]" replaced with your own identifying information:
  !      Portions Copyright [yyyy] [name of copyright owner]
  !
  ! CCPL HEADER END
  !
  !      Copyright 2011-2013 ForgeRock AS
  !
-->
# OpenICF Developer's Guide

> IMPORTANT: This is just some Markdown I had lying around. The real
> OpenICF dev guide in progress is at
> <http://openicf.forgerock.org/doc/dev-guide/index.html>.

Hands-on guide to developing and configuring OpenICF. Open Identity
Connector Framework provides connectors for a consistent generic layer
between applications and target resources.

You can download OpenICF software from the
[download page](http://www.forgerock.org/openicf.html). OpenICF is free
to download, evaluate, and use. You can also check out and modify the
source code to build your own version if you prefer.

## About Connectors

Connectors give you a way to decouple applications from data resources. Open
Identity Connector Framework (OpenICF) focuses on provisioning and identity
management, but also provides general purpose capabilities including 
authentication, create, read, update, delete, search, scripting, and
synchronization. Connector bundles rely on the Connector Framework, but
applications remain completely separate from connector bundles, so that
you can change and update connectors without changing your application or
its dependencies.

OpenICF provides many capabilities.

*   Connection pooling
*   Timeouts on all operations
*   Search filtering
*   Search and synchronization buffering and results streaming
*   Scripting with Groovy and Boo .NET
*   Classloader and JVM isolation
*   An independent logging API/SPI similar to Apache Commons logging
*   Java and .NET platform support
*   Opt-in operations that support both simple and advanced implementations
    for the same CRUD operation
*   A logging proxy that captures all API calls
*   A connector archetype to create connectors with support for Ant, NetBeans
    and Eclipse

## Getting Started

To use connectors in your application, first download the
[Connector Framework](http://openicf.forgerock.org/downloads.html). Next, add
the following jars to your classpath:

*   `connector-framework.jar` (compile and runtime dependency)
*   `connector-framework-internal.jar` (runtime dependency only)

Also download the Connector Bundles you need, and add them to the classpath
as well. At this point you can start using the connectors in your application.

You can also download builds from the
[OpenICF download page](http://openicf.forgerock.org/downloads.html).
 
## Using Connectors

Your application access the connector API through the `ConnectorFacade` class,
performing all interaction with the connector through the `ConnectorFacade`
instance. To create a `ConnectorFacade`, follow these steps:

1.  Create a `ConnectorInfoManager` and a `ConnectorKey` for the connector.
2.  Using the manager and the key, create a `ConnectorInfo` instance.
3.  From the `ConnectorInfo` object, create the default `APIConfiguration`.
4.  From the default `APIConfiguration`, retrieve the `ConfigurationProperties`.
5.  Set all of the `ConfigurationProperties` you need for the connector using
    `setPropertyValue()`.
6.  Use the `ConnectorFacadeFactory.newInstance()` method to create a new
    instance of the connector.

The following example demonstrates how to create the `ConnectorFacade`.

    // Use the ConnectorInfoManager to retrieve a ConnectorInfo object for
    // the connector.
    ConnectorInfoManagerFactory fact = ConnectorInfoManagerFactory
            .getInstance();
    File bundleDirectory = new File("/connectorDir/bundles/foobar");
    URL url = IOUtil.makeURL(bundleDirectory,
            "/dist/org.identityconnectors.foobar-1.0.jar");
    ConnectorInfoManager manager = fact.getLocalManager(url);
    ConnectorKey key = new ConnectorKey("org.identityconnectors.foobar",
            "1.0", "FooBarConnector");
    ConnectorInfo info = manager.findConnectorInfo(key);

    // From the ConnectorInfo object, create the default APIConfiguration.
    APIConfiguration apiConfig = info.createDefaultAPIConfiguration();

    // From the default APIConfiguration, retrieve the ConfigurationProperties.
    ConfigurationProperties properties = apiConfig
            .getConfigurationProperties();

    // Print out what the properties are (not necessary).
    List<String> propertyNames = properties.getPropertyNames();
    for(String propName : propertyNames) {
        ConfigurationProperty prop = properties.getProperty(propName);
        System.out.println("Property Name: " + prop.getName() +
                "\tProperty Type: " + prop.getType());
    }

    // Set all of the ConfigurationProperties needed by the connector.
    properties.setPropertyValue("host", FOOBAR_HOST);
    properties.setPropertyValue("adminName", FOOBAR_ADMIN);
    properties.setPropertyValue("adminPassword", FOOBAR_PASSWORD);
    properties.setPropertyValue("useSSL", false);

    // Use the ConnectorFacadeFactory's newInstance() method to get a
    // new connector.
    ConnectorFacade conn = ConnectorFacadeFactory.getInstance()
            .newInstance(apiConfig);

    // Make sure you have set up the Configuration properly.
    conn.validate();

    // Start using the Connector.
    conn.[authenticate|create|update|delete|search|...]

### Getting Supported Operations

When your connector is ready to use, you can find out from the
`ConnectorFacade` what operations it supports. The quickest check involves
determining whether a particular operation is part of the set of supported
operations.

    Set<Class< ? extends APIOperation>> ops = conn.getSupportedOperations();
    // Check whether CreateApiOp is supported, for example.
    return ops.contains(CreateApiOp.class);

Connectors might support operations only for specific object classes,
however. For example, you might be able to create a person, but not a
group. The more correct way to check whether the connector supports a
particular operation also involves the object class on which you plan to
perform the operation.

    Schema schema = conn.schema();
    Set<ObjectClassInfo> objectClasses = schema.getObjectClassInfo();
    Set<ObjectClassInfo> ocinfos = schema
            .getSupportedObjectClassesByOperation(CreateApiOp.class);

    for(ObjectClassInfo oci : objectClasses) {
        // Check that the operation is supported for your object class.
        if (ocinfos.contains(ocinfo)) {
            // object class is supported
        }
    }

### Checking Schema

In addition to supported operations for an object class, your application
can find out which attributes are required and allowed for a particular
object class. `ObjectClassInfo` contains this information as a set of
`AttributeInfo` objects.

    Schema schema = conn.schema();
    Set<ObjectClassInfo> objectClasses = schema.getObjectClassInfo();
    for(ObjectClassInfo oci : objectClasses) {
        Set<AttributeInfo> attributeInfos = oci.getAttributeInfo();
        String type = oci.getType();
        if(ObjectClass.ACCOUNT_NAME.equals(type)) {
            for(AttributeInfo info : attributeInfos) {
                System.out.println(info.toString());
            }
        }
    }

### Creating Objects

You create a new object by using `ConnectorFacade.create()` after setting
up the `ConnectorFacade`, checking that the operation is supported, and
making sure you can set at least all the required attributes. The
`ConnectorFacade.create()` method takes the object class and the set of
attributes as its arguments. The object class specifies the type of
`ConnectorObject` (account, group, and so forth) to create. The attributes
describe the connector object (name, password, members, and so forth).
The object class and allowed and required attributes are identified in
the connector schema. The following example shows a code fragement that
creates an account.

    Set<Attribute> attrs = new HashSet<Attribute>();
    attrs.add(new Name("MyUser"));
    attrs.add(AttributeBuilder.buildPassword("secret12"));
    attrs.add(AttributeBuilder.build("Company", "ForgeRock AS"));
    Uid userUid = conn.create(ObjectClass.ACCOUNT, attrs);

### Updating Passwords

Passwords are of a special type, called an operational attribute. Another
operational attribute is `Enabled`. You create these attributes using
static `AttributeBuilder` methods. The following example shows a code
fragment to update the password of the account created in the previous
example.

    Set<Attribute> attrs = new HashSet<Attribute>();
    attrs.add(new Name("MyUser"));
    attrs.add(AttributeBuilder.buildPassword("newPassword"));
    Uid userUid = conn.update(ObjectClass.ACCOUNT, attrs);

### Deleting Objects

You delete accounts by object class and `Uid`. If the connector supports
the search operation for accounts, you can look up the UID using an
attibute value that you know already, such as the `username`.

    Uid userUid = findUid(userName);
    conn.delete(ObjectClass.ACCOUNT, userUid);

### Searching

If the connector supports searching for your object class, then you can
use `ConnectorFacade.search()`.  To set up your search, create a `Filter`
and also a `ResultsHandler`. The filter sets up what to match on the
connected resource, and must fit the schema for the object class. The
following code fragment uses a compound filter and handler to display
the results found.

    Filter leftFilter = FilterBuilder.equalTo(AttributeBuilder
            .build("FIRSTNAME", "John"));
    Filter rightFilter = FilterBuilder.equalTo(AttributeBuilder
            .build("DEPARTMENT", "Engineering"));
    Filter filter = FilterBuilder.and(leftFilter, rightFilter);
    final List<ConnectorObject> results = new ArrayList<ConnectorObject>();
    ResultsHandler handler = new ResultsHandler() {
        public boolean handle(ConnectorObject obj) {
            results.add(obj);
            return true;
        }
    };

    conn.search(ObjectClass.ACCOUNT, filter, handler);
    for(ConnectorObject obj : results ) {
        System.out.println("Name: " + obj.getName() +
                "\tUID: " + obj.getUid());
    }

You can also search with an operational attribute in the filter, such
as a search for disabled accounts

    Filter nameFilter = FilterBuilder.startsWith(new Name("John"));
    Filter enabledFilter = FilterBuilder.equalTo(AttributeBuilder
            .buildEnabled(false));     // Account is disabled.
    Filter filter = FilterBuilder.or(nameFilter, enabledFilter);
    final List<ConnectorObject> results = new ArrayList<ConnectorObject>();
    ResultsHandler handler = new ResultsHandler() {
        public boolean handle(ConnectorObject obj) {
            results.add(obj);
            return true;
        }
    };

    conn.search(ObjectClass.ACCOUNT, filter, handler);
    for(ConnectorObject obj : results ) {
        System.out.println("Name: " + obj.getName() +
                "\tUID: " + obj.getUid());
    }

### Getting the Object Class

When you have an `ObjectClassInfo` and want to get the `ObjectClass`,
do the following in your application.

    ObjectClass objectClass = new ObjectClass(objectClassInfo.getType());

## Connector Servers

A Connector Server lets your application run provisioning operations on
a connector bundle that is deployed on a remote system. A key feature
of connector servers is the ability to run connector bundles written in
C# on a .NET platform, and access them over the network from a Java
application.

Connector servers are available for both Java and .NET platforms.

*   .NET connector servers let your Java application access C# connector
    bundles. You deploy the C# bundles on a .NET connector server.
    Then your Java application can communicate with the C# connector
    server over the network. The C# connector server serves as a proxy
    to provide to authenticated application access to the C# bundles
    deployed within the C# connector server.

*   Java connector servers let you execute a Java connector bundle in
    a different JVM from your application. You can also run a Java
    connector on a different host for performance reasons. You might
    choose to use a Java connector server with a Java remote connector
    server to avoid the possibility of crashing an application JVM
    due to a fault in a JNI-based connector.

### Installing a .NET Connector Server

1.  [Download](http://openicf.forgerock.org/downloads.html) the .NET
    Connector Server .zip and the ServiceInstall .msi.
2.  Run the .msi wizard to install the Connector Server as a Windows
    Service.

#### Configuring a .NET Connector Server

1.  Start Microsoft Services Console, and then check to see if the
    Connector Server is currently running. If so, stop the Connector
    Server.
2.  From a command prompt, set the key for the Connector Server.
    You set the key by changing to the directory where you installed
    the Connector Server, such as `C:\Program Files\Identity
    Connectors\Connector Server`, and then by executing the following
    command.
    
        ConnectorServer /setkey <newkey>
    Where `<newkey>` is the value for the key required by any client
    that connects to this Connector Server.
3.  Read the configuration file, inspecting all settings. You might
    choose to change port number, trace, and SSL settings, for example.

You can find port, address, and SSL settings are in the AppSettings
element.

	<add key="connectorserver.port" value="8759" />
	<add key="connectorserver.usessl" value="false" />
	
	<add key="connectorserver.certificatestorename"
	     value="ConnectorServerSSLCertificate" />
	<add key="connectorserver.ifaddress" value="0.0.0.0" />

Change the port number by setting the value of `connectorserver.port`.
You can bind the listening socket to a particular address, or leave it
set to `0.0.0.0`. For SSL, set the value of `connectorserver.usessl` to
`true`, and the value of `connectorserver.certifacatestorename` to the
name of your certificate store.

Record the following information for use when connecting to the
Connector Server.

*   Host name or IP address
*   Connector server port
*   Connector server key
*   Whether SSL is enabled

Trace settings look like this:

	<system.diagnostics>
	  <trace autoflush="true" indentsize="4">
	     <listeners>
	       <remove name="Default" />
	       <add name="myListener"
	            type="System.Diagnostics.TextWriterTraceListener"
	            initializeData="C:\connectorserver2.log"
	            traceOutputOptions="DateTime">
	         <filter type="System.Diagnostics.EventTypeFilter"
	                 initializeData="Information" />
	       </add>
	    </listeners>
	  </trace>
	</system.diagnostics>

Connector Servers use the .NET trace mechanism as described in
Microsoft's .NET documentation for `System.Diagnostics`.

Start with default settings. For less tracing, change the
`EventTypeFilter`'s `initializeData` to `Warning` or `Error`.
For verbose logging, set the value to `Verbose` or `All`. Log
level has a direct effect on Connector Server performance, so
avoid verbose logging in production systems.

After making configuration changes, stop and restart the Connector
Server for your changes to take effect.

#### Running a .NET Connector Server

In most cases, let the Connector Server run as a Windows Service.

If you must, you can `/uninstall` the Connector Server as a
Windows Service (or `/install` it again). Then, to run the .NET
Connector Server manually, use the following command.

    ConnectorServer /run

#### Installing Connectors on a .NET Connector Server

1. Change to the directory where you installed the Connector Server.
2. Unzip the connector .zip delivery.
3. Restart the Connector Server.

#### Running Multiple Connector Servers

To run multiple connector servers on the same host, make sure that
you unpack them into different directories, that you use different
port number settings for each server, and that you use different
trace files.

You can run each Connector Server either interactively, or as a
Windows Service.

### Installing a Java Connector Server

1. Create a new directory such as `/path/to/jconnsrv`.
2. Copy `connector-framework.jar`, `connector-framework-internal.jar`,
   and `groovy-all.jar` to the new directory.
3. Create a `bundles/` directory in the new directory.
4. Extract `org/identityconnectors/framework/server/connectorserver.properties`
   from `connector-framework-internal.jar` into the new directory.
5. Test your installation by running the Java Connector Server with
   no arguments, in order to display the usage message.
   
        $ cd /path/to/jconnsrv
        $ java -jar connector-framework-internal.jar
        Usage: Main -run -properties <connectorserver.properties>
        Main -setKey -key <key> -properties <connectorserver.properties>
        Main -setDefaults -properties <connectorserver.properties>
        NOTE: If using SSL, you must specify the system config
        properties:
         -Djavax.net.ssl.keyStore
         -Djavax.net.ssl.keyStoreType (optional)
         -Djavax.net.ssl.keyStorePassword

#### Configuring a Java Connector Server


1.  Run the Connector Server with the `-setKey` option to set the key
    in the `connectorserver.properties` file.
2.  Edit other properties directly in the `connectorserver.properties`
    file.

A sample `connectorserver.properties` file follows, where the clear text
for the key is `changeit`.

    ##
	## Port number to listen on
	##
	connectorserver.port=8759
	
	##
	## Connector install directory (relative path)
	##
	connectorserver.bundleDir=bundles
	
	##
	## Directory for runtime libraries needed by connectors
	##
	connectorserver.libDir=lib
	
	##
	## Set to true to use SSL.
	## NOTE: If using SSL, you must specify the following system
	##       config properties (on the command line):
	##      -Djavax.net.ssl.keyStore
	##      -Djavax.net.ssl.keyStoreType (optional);
	##      -Djavax.net.ssl.keyStorePassword;
	##
	connectorserver.usessl=false
	
	##
	## Optionally specify a specific address to bind to
	##
	#connectorserver.ifaddress=localhost
	
	##
	## Secure hash of the gateway key. Set this by using the
	## -setKey flag.
	##
	connectorserver.key=lmA6bMfENJGlIDbfrVtklXFK32s\=
	
	##
	## Logger class.
	##

#### Running a Java Connector Server

Use the `-run` option.

    java \
     -cp "connector-framework.jar:connector-framework-internal.jar:groovy-all.jar" \
     org.identityconnectors.framework.server.Main \
     -run -properties connectorserver.properties

#### Installing Connectors on a Java Connector Server

1.  Copy the connector bundle .jar to the `bundles/` directory.
2.  Add any required third-party .jar files to the classpath.
3.  Restart the Java Connector Server.

### Accessing Connector Servers Over SSL

1.  Configure the Connector Server to use an SSL certificate.
2.  Configure the Connector Server to provide SSL sockets.
3.  Configure your application to communicate with the Connector Server
    over SSL.
    If you use self-signed or other certificates that Java cannot verify
    automatically, either import such certificates into
    `$JAVA_HOME/lib/security/cacerts`, or use Java properties to
    specify your own, properly configured, truststore:
    *    `-Djavax.net.ssl.trustStore=/path/to/myApp_cacerts`
    *    `-Djavax.net.ssl.trustStorePassword=changeit`

## Writing Connectors

Use the Maven connector archetype to get started developing your own
connector bundle. First, familiarize yourself with the structure of the
SPI, and then read the Javadoc for the framework. You can also study source
code for other connectors.

If you are thinking of contributing your connector to OpenICF, review the
[coding conventions](https://wikis.forgerock.org/confluence/display/devcom/Coding+Style+and+Guidelines)
and the [contract tests](http://openicf.forgerock.org/connector-framework-contract/index.html)
documentation. The contract tests are an automated suite of tests to make
sure your connector generally looks, feels, and behaves like a connector
should.

You are welcome to join and ask questions on the
[mailing list](https://lists.forgerock.org/mailman/listinfo/openicf-dev),
to [report bugs](https://bugster.forgerock.org/jira/browse/OPENICF), and
submit patches when you
[get involved](http://www.forgerock.org/get_involved.html).

To get started with the Connector Archtetype, first execute the following
command to import the archetype and generate a new connector project.

    $ mvn archetype:generate \
    -DarchetypeGroupId=org.forgerock.openicf \
    -DarchetypeArtifactId=connector-archetype \
    -DarchetypeVersion=1.0.0-SNAPSHOT \
    -DremoteRepositories=http://maven.forgerock.org/repo/snapshots \
    -DarchetypeRepository=http://maven.forgerock.org/repo/snapshots \
    -DgroupId=org.forgerock.openicf.connectors.misc \
    -DartifactId=sample \
    -Dversion=1.0-SNAPSHOT \
    -Dpackage=org.forgerock.openicf.sample \
    -Dconnector_name=Sample

You can customize `-DartifactId=sample`, `-Dversion=1.0-SNAPSHOT`,
`-Dpackage=org.forgerock.openicf.sample`, and `-Dconnector_name=Sample`.

After you import the archetype, you can use the local version.

    $ mvn archetype:generate -DarchetypeCatalog=local
    $ cd sample
    $ mvn install

## Reference

OpenICF provides the following connectors.

    foreach (available connector)
    ### Installation
    ### Interfaces Supported
    ### Connector Schema
    ### Configuration

---------------------------------------

This work is licensed under the Creative Commons
Attribution-NonCommercial-NoDerivs 3.0 Unported License.
To view a copy of this license, visit
<http://creativecommons.org/licenses/by-nc-nd/3.0/>
or send a letter to Creative Commons, 444 Castro Street,
Suite 900, Mountain View, California, 94041, USA.

Copyright 2012-2013 ForgeRock AS