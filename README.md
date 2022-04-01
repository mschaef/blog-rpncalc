blog-rpncalc
============

## Description

This project contains a set of JVM implementations of a simple
[reverse polish notation](https://en.wikipedia.org/wiki/Reverse_Polish_notation)
calculator. They were originally written as supporting materials for a
discussion on the relationship between functional programming and the
command pattern that I wrote for the [KSM Partners](http://www.ksmpartners.com/)
blog. However, that blog has since changed its format, and KSM has graciously
allowed me to re-home the articles myself.

* [Part 0: Introduction](https://www.mschaef.com/ksm/rpncalc_00)
* [Part 1: A Simple RPN Calcualtor in Java and the Command Pattern](https://www.mschaef.com/ksm/rpncalc_01)
* [Part 2: Composite Commands](https://www.mschaef.com/ksm/rpncalc_02)
* [Part 3: Implementing Undo](https://www.mschaef.com/ksm/rpncalc_03)
* [Part 4: A Noun for State](https://www.mschaef.com/ksm/rpncalc_04)
* [Part 5: Eliminating the Globals](https://www.mschaef.com/ksm/rpncalc_05)
* [Part 6: Refactoring the REPL](https://www.mschaef.com/ksm/rpncalc_06)
* [Part 7: Refactoring Loops with Reduce](https://www.mschaef.com/ksm/rpncalc_07)
* [Part 8: Moving to Clojure](https://www.mschaef.com/ksm/rpncalc_08)
* [Part 9: State and Commands in Clojure](https://www.mschaef.com/ksm/rpncalc_09)
* [Part 10: Macros and the Intent of the Code](https://www.mschaef.com/ksm/rpncalc_10)

Because of the intent for this code to support a series of blog posts,
the code is focused more on concision and readability than on
robustness.

## Project Structure

There are two top level sub-projects at the root of the distribution:
`java` and `clj`. The `java` project contains the
[Java](http://www.java.com/) implementations of the calculator and is
built with [Maven](http://maven.apache.org/). The `clj` project
contains the [Clojure](http://clojure.org) implementations, and is
built with [Leiningen](http://leiningen.org/).

For both sub-projects, there is a top-level shell script, `run.sh`,
that will build and then execute the project.

### Java implemenations

* `basic` - A simple RPN calculator with support for the four basic
  mathematical operations and not much else.
* `composite` - `basic` extended with composite commands that can be
  composed of a sequence of other commands.
* `undoable` - `composite` extended with the ability to reverse the
  effects of a command that's already been executed.
* `stateobject` - `undoable`, with all calculator state moved into a
  first-class object.
* `functional` - `stateobject` without global variable.
* `functionalrf` - A minor refactoring of `functional` that simplifies
  command state management.
* `reducer` - A version of `functionalrf` that uses functional
  reduction to replace explicit looping.

### Clojure implementations

* `functionalrf` - A reimplementation of the Java `functionalrf` build
  in Clojure.
* `macro` - `functionalrf` extended with a macro to automate
  generation of stack transformation commands.
* `dfcompile` - A version of `macro` that adds a simple compiler that
  translates from sequences of calculator operators into Clojure
  function definitions.

## License and copyright

Copyright (c) KSM Technology Partners. All rights reserved.

The use and distribution terms for this software are covered by the
Eclipse Public License 1.0
(http://opensource.org/licenses/eclipse-1.0.php) which can be found in
the file epl-v10.html at the root of this distribution.  By using this
software in any fashion, you are agreeing to be bound by the terms of
this license.  You must not remove this notice, or any other, from
this software.
