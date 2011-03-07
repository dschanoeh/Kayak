To build Kayak you must install a recent JDK and Maven 2. Make sure that both are set up correct and then open a terminal, navigate to the Kayak folder and build Kayak with
    mvn clean install
The first time this step will take a long time because Maven will download all dependencies for Kayak (especially the Netbeans RCP components).
When the build has finished you can navigate to Kayak/application/target/kayak/bin/ and run ./kayak or kayak.exe if you are on a Windows machine.
