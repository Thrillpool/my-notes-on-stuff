## What is Lombok?

In short it's a compile time annotation processor i.e. a form of metaprogramming, a fancy one! You put some annotations in your code, the compiler is then told in some way or another to load the Lombok annotation processor (lombok.launch.AnnotationProcessorHider$AnnotationProcessor) and this does lots of random stuff at compile time to your code.

More details can be found here https://projectlombok.org/contributing/lombok-execution-path.

## Where are the official docs?

Everything you can do is concisely summarised here https://projectlombok.org/features/

## Why does it exist?

Reading what you can do with it, it's pretty evident, this is a library that makes it so you don't get to write as much Java boilerplate (but surely as a Java programmer boilerplate is your one true love?).

## What do the transformed class files look like

That simple enough, just compile and decompile the class files.