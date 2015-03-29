Maven TypeScript Compiler Plugin
================================

This plugin adds [TypeScript](http://typescriptlang.org/) compilation to your Maven project without adding any
prerequisites (e.g. installing Node.js) other than JDK 8. It unpacks the TypeScript compiler from a Maven artifact,
makes the [Avatar.js](https://avatar-js.java.net/) native library available with the expected filename, then executes
tsc on the Avatar.js server, which uses Nashorn to execute JavaScript.

Why?
----

There are a variety of reasons that a development team can't require Node.js and NPM for their build process, including
company policy, regulatory and audit compliance, and inertia. While individual developers may be able to work around
these roadblocks, a requirement to install new software on the build server is more likely to remain a problem.

Also, a well-integrated, all-JVM build is a neat idea.

Why not?
--------

It's slow. Startup time for tsc is somewhere around 12 seconds.

What about watching the directory and recompiling when a file changes?
----------------------------------------------------------------------

That's your IDE's job. If you want to do it anyway, run the tsc:watch goal.

Alternatives
------------

Where no such restrictions apply, consider other plugins such as
[frontend-maven-plugin](https://github.com/eirslett/frontend-maven-plugin) or
[grunt-maven-plugin](https://github.com/allegro/grunt-maven-plugin).

How to use
----------

As the Maven artifacts provided by this project are unlikely to be available to your Maven instance, first
install/deploy the [TypeScript redistribution](typescript/) and [tsc-maven-plugin](tsc-maven-plugin/) artifacts,
either to your local Maven repository ($HOME/.m2/repository) or your local Maven repository manager (e.g. Artifactory
or Nexus). Next, take a look at the included [demo project](tsc-maven-demo-project/) for usage information.

Reasonable defaults have been provided for every user-serviceable configuration option. Refer to
[AbstractTypeScriptMojo](tsc-maven-plugin/src/main/java/ca/eqv/maven/plugins/tsc/mojo/AbstractTypeScriptMojo.java)
(or your IDE's tooltips while editing your POM) to see what can be changed on the fly.

Project status
--------------

This project is currently on hold pending two matters:

* Completion of the [Angular 2](http://angular.io/) switchover from Traceur/AtScript to TypeScript. The [information
available at this time](https://github.com/Microsoft/TypeScript/issues/1557) suggests that the upcoming TypeScript 1.5
release may support the required language changes for this to take place. Until then, a suitable demo project for
exploring the second matter cannot be completed.

* A team decision on whether to adopt Angular 2 for front-end development, and whether we are willing to tie together
two build processes (Maven triggering a Grunt build that uses [grunt-ts](https://github.com/TypeStrong/grunt-ts) for
TypeScript compilation) or would prefer to use only Maven.

License
-------

Except where otherwise noted, everything contained in this repository is

    Copyright 2015 William Ono

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
