blog-rpncalc
============

## Description

This project contains a set of JVM implementations of a reverse-polish
style calculator.  They are written to be used as supporting materials
for a set of blog postings on the
[KSM Partners Blog](http://www.ksmpartners.com/blog/). Because of this
intent, the code is focused more on concision and readability than on
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
