roboducks: an animal herding simulation
=======================================

roboducks is an undergraduate research project I completed in 2001,
focused on the application of neural networks and genetic algorithms
to the herding of simulated flocking animals.

This repository contains the complete Java source code of the project
and a final report on its planning, implementation, and results.

Manifest
--------

* `src`: the final Java code for the project, fairly well documented
  there and in the report. neurotic and ducksim contain the useful stuff,
  most of the rest is just random rubbish and obsolete bits.

* `report`: the final report on the project

* `misc`: various related files, including earlier versions of some
  of the code (the original project was not under version control!)

Building and running
--------------------

An ant build file is provided for the code. Provided that a JDK and
ant are installed, `ant run` in the project's root directory will
compile the source, create a jar file, and run it.

The report is written in LaTeX. It can be built by running `make.sh`
in the `report` subdirectory.

Copyright and license
---------------------

Copyright 2001, 2016 Pontus Lurcock (pont at talvi dot net).

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
Public License for more details.

You should have received a copy of the GNU General Public License along
with this program. If not, see <http://www.gnu.org/licenses/>.
