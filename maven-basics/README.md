## What is Maven?

Maven is a relatively barebones build tool compared to Gradle and SBT. Nonetheless, it is still nice, and if you like looking at XML you are especially in for a treat. Teasing a bit less, Maven is really great, its documentation is extremely well written and that you're constrained to xml means builds generally don't do anything crazy. Yours truly is actually a massive Apache fanboy.

## A refresher on XML

The basics of Maven start with the world-famous pom.xml file, which defines your entire build process. Let’s take a look at a minimal example and go through it line by line. Understanding this structure is helpful because genuinely one file (the xml schema) does tell you everything you need to know about configuring maven.
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
</project>
```

This is the root <project> tag, and it sets up the XML namespaces that define the context for the file:

- `xmlns`: This declares the default namespace for the document—in this case, Maven's POM (Project Object Model) namespace. Any child elements that don’t have a prefix are assumed to belong to this namespace. See more on XML namespaces.

- `xmlns:xsi`: This declares a namespace prefix `xsi`, which stands for XML Schema Instance. This is a standard name used when referencing or interacting with an XML Schema from within an instance document (like this one). This xsi namespace gives us access to the things defined in https://www.w3.org/2001/XMLSchema-instance actually trying to view this schema in your browser is a bit misleading, as you'll see what looks like an actual html page, in reality if you inspect page you can see it contains e.g. a line like `<xs:attribute name="schemaLocation"/>`

- `xsi:schemaLocation`: This attribute uses the xsi namespace knowing it defines a schemaLocation attribute maps the default namespace to a specific XML Schema file (an .xsd). It tells XML parsers, "this document should follow the rules defined in this schema file."

A common point of confusion is the distinction between xsi and xsd:

- `xsi` is used in instance documents (like pom.xml) to refer to schemas.
  
- `xsd` is a conventional prefix often used in the schema definitions themselves to define types and structure.

In practice, the prefixes (xsi, xsd, etc.) are arbitrary—you can name them anything—but these conventions are widespread and help make things more readable.

Knowing all this we can now see what goodies a pom lets us use, they are the things defined in http://maven.apache.org/xsd/maven-4.0.0.xsd. This is nice as the whole schema is thoroughly documented, just as an example snippet.

```xml
<xs:element minOccurs="0" name="dependencies">
    <xs:annotation>
        <xs:documentation source="version">3.0.0+</xs:documentation>
        <xs:documentation source="description">
            This element describes all of the dependencies associated with a project. These dependencies are used to construct a classpath for your project during the build process. They are automatically downloaded from the repositories defined in this project. See <a href="https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html">the dependency mechanism</a> for more information.
        </xs:documentation>
    </xs:annotation>
    <xs:complexType>
        <xs:sequence>
            <xs:element name="dependency" minOccurs="0" maxOccurs="unbounded" type="Dependency"/>
        </xs:sequence>
    </xs:complexType>
</xs:element>
```

Telling us dependencies tag is sequence of dependency tags, further down we can see definition of Dependency type

```xml
<xs:complexType name="Dependency">
  <xs:annotation>
      <xs:documentation source="version">3.0.0+</xs:documentation>
      <xs:documentation source="description">

          The &lt;code&gt;&amp;lt;dependency&amp;gt;&lt;/code&gt; element contains information about a dependency
          of the project.

      </xs:documentation>
  </xs:annotation>
  <xs:all>
      <xs:element minOccurs="0" name="groupId" type="xs:string">
          <xs:annotation>
              <xs:documentation source="version">3.0.0+</xs:documentation>
              <xs:documentation source="description">

                  The project group that produced the dependency, e.g.
                  &lt;code&gt;org.apache.maven&lt;/code&gt;.

              </xs:documentation>
          </xs:annotation>
      </xs:element>
```

## More practical documentation

Practically speaking https://maven.apache.org/pom.html tells you everything there is to know about the POM.

## Actually running Maven

You can download mvn cli, which is worth doing. A common thing done in Maven projects is to provide a 'maven wrapper' which is responsible for ensuring you obtain the correct version of maven for the project etc. That is how this project is set up.

## Built in 'phases' and 'goals'

What in SBT or Gradle are called tasks are called 'phases' and there's some notion of a 'lifecycle' which is collection of phases in some order, analogous to tasks depending on other tasks in Gradle or SBT. For instance the 'package' phase knows to run the 'validate', 'compile', 'test' phase, before actually running the 'package' phase. Lower level a phase binds to some 'goals' (which are also like tasks) which are the things plugin defines and do this binding, for instance there is a standard 'compiler' plugin that you get for free, and it has a 'compile' goal, and compiler:compile is bound to compile phase.

The built-in plugins all come with pleasant 'help' goals which essentially tell you documentation, so for instance you can run `./mvnw clean:help` and it tells you what goals the clean plugin does.

## A look at a plugin

Let's look at the clean plugin and make sense of it a bit to get a feel for what that looks like, as really plugins are the things that actually 'do stuff' so knowing how to read them means you can easily make sense of how to configure a plugin etc.

The code lives here https://github.com/apache/maven-clean-plugin/tree/master

A few key things to note, you know it's a plugin because its packaging field declares it 'maven-plugin'. I won't deliberate too much on it, but it uses one of the cooler Java features, SPI to find specific things to load. In particular, it finds this 'Mojo' thing

https://github.com/apache/maven-clean-plugin/blob/master/src/main/java/org/apache/maven/plugins/clean/CleanMojo.java#L47-L48

which has an execute method that contains the actual runtime logic. Picking this case is a little funny because we get this plugin (and some others) for free with Maven, and they circumvent the usual mechanism by which goals actually get bound to phases.

Anyway, reading CleanMojo.java it exposes to you how you can actually configure the plugin, you see lots of things like e.g. 

```java
/**
 * This is where build results go.
 */
@Parameter(defaultValue = "${project.build.directory}", readonly = true, required = true)
private Path directory;
```

This tells you can configure where clean goal understands the build results as being something like so

```xml
      <plugin>
        <artifactId>maven-clean-plugin</artifactId>
        <configuration>
          <directory>
            localFolder
          </directory>
        </configuration>
      </plugin>
```

Now if you make a 'localFolder' folder (inside the project) and run clean, maven will think to delete this localFolder.

In general plugins have an xml in them which contains the documentation for configuring a plugin, which is nice! This mirrors what we find in the actual java file.

## A look at a non built-in plugin

I said the built-in plugins are a little special, a useful typical plugin is the 'maven-jar-plugin' which makes a jar for you.

See https://github.com/apache/maven-jar-plugin/blob/master/src/main/java/org/apache/maven/plugins/jar/AbstractJarMojo.java#L51, again it has this special Mojo thing, this one is abstract and used to define two actual goals (although it has a generic used by both execute method).

https://github.com/apache/maven-jar-plugin/blob/master/src/main/java/org/apache/maven/plugins/jar/JarMojo.java#L32-L33 defines jar goal and by default binds it to package phase.
https://github.com/apache/maven-jar-plugin/blob/master/src/main/java/org/apache/maven/plugins/jar/TestJarMojo.java#L34-L36 defines test-jar goal and by default binds it to package phase.

As discussed this package phase inherently depends on compile phase, so it's inherent that the actual classes etc. are there to package (nice design!).

You might think that since the goal is bound to the phase, when you run the package phase both goals will execute. It is not so, in reality when you run package only the jar goal executes, the test-jar goal knows it's associated with package phase, but it is not actually 'enabled', to enable it you would add like

```xml
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-jar-plugin</artifactId>
        <executions>
          <execution>
            <id>build-test-jar</id>
            <goals>
              <goal>test-jar</goal>
            </goals>
          </execution>
        </executions>
      </plugin>
```

If you are wondering how to bind goal to a phase, it can be done in the execution configuration also.

OK so how does the jar goal bind and execute by default? That is a bit of real chicanery, again it gets special treatment. There's this binding that maven do https://maven.apache.org/ref/3.9.9/maven-core/default-bindings.html#plugin-bindings-for-jar-packaging, i.e. since we specified jar packaging type it adds

```xml
  <package>
    org.apache.maven.plugins:maven-jar-plugin:3.4.1:jar
  </package>
```

## Installing

This funny word install is how you can publish jar from project into your local maven repo (i.e. ~/.m2). This is endlessly useful when making a change to a dependency of another repo and you want to check both changes together. For Maven the local .m2 repo acts in a nice two fold way, it's not only where Maven will look for repos, it is also the local cache of dependencies it has resolved from elsewhere.

The main interesting thing again here is how it achieves it, with our current configuration if we run install, it copies the pom.xml over, it copies the artifact jar over, and it copies the test artifact jar over! How does it know! The answer lies here

https://github.com/apache/maven-jar-plugin/blob/master/src/main/java/org/apache/maven/plugins/jar/AbstractJarMojo.java#L324

```java
projectManager.attachArtifact(project, artifact, jarFile);
```

when the jar is created it attaches itself to the project, then the install plugin can do

```java
Collection<ProducedArtifact> attachedArtifacts = projectManager.getAttachedArtifacts(project);
```

So we see that plugins can share things between each other.

## Where is a good place to find documentation for plugins

It sort of depends, the plugin.xml / the source code is a good source of info. The apache plugins are all well documented, e.g. the one built-in for running tests has documentation here https://maven.apache.org/surefire/maven-surefire-plugin/index.html. Other plugins have docs of varying quality.

## Profiles

It would be remiss to not mention profiles, profiles are essentially switch statements for your build to facilitate fancy behaviour. Details are here https://maven.apache.org/guides/introduction/introduction-to-profiles.html

## Seeing dependency tree

This is another inevitably useful thing to be able to do, to help understand why some version is coming through etc. The dependency plugin provides a great many useful tasks. Details are here https://maven.apache.org/plugins/maven-dependency-plugin/index.html
