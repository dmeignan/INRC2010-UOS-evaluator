INRC 2010 evaluator
===================

Evaluator, developed by the Combinatorial Optimization Team of the University of Osnabrück, for the INRC2010 benchmark [Haspeslagh et al 2014]. The INRC2010 benchmark has been proposed for the first International Nurse Rostering Competition held in 2010. The evaluator is based on the model given in [Lü and Hao 2012] and has been used in [Meignan et al. 2015], [Meignan 2014] and  [Meignan 2015].

Usage
-----

With the JAR file (available in the list of releases), you can run the evaluator directly from the command line using the command:
> java -jar inrc2010evaluator-v1.0.jar -p [PROBLEM-FILE.xml] -s [SOLUTION-FILE.xml]

Unit tests of constraint
------------------------

A series of tests has been developed to ensure that the evaluator is conform to the model presented in [Lü and Hao 2012]. These "unit test of constraints" are pairs of problem/solution that each tests a specific constraint. The test files and results are in the directory: <https://github.com/dmeignan/INRC2010-UOS-evaluator/tree/master/fr.lalea.inrc2010evaluator/src/test/resources/inrc2010/constraint_unit_tests> 

References
----------

> [Haspeslagh et al 2014] S. Haspeslagh, P. D. Causmaecker, A. Schaerf, M. Stølevik, "The first international nurse rostering competition 2010", Annals of Operations Research, pp. 221-236, vol. 218, 2014.

> [Lü and Hao 2009] Z. Lü, J.-K. Hao, "Adaptive neighborhood search for nurse rostering", European Journal of Operational Research, pp. 865-876, vol. 218, 2012. DOI: 10.1016/j.ejor.2011.12.016

> [Meignan et al. 2015] D. Meignan, S. Schwarze and S. Voß, "Improving Local-Search Metaheuristics Through Look-Ahead Policies", Annals of Mathematics and Artificial Intelligence. DOI: 10.1007/s10472-015-9453-y

> [Meignan 2014] D. Meignan, "A Heuristic Approach to Schedule Reoptimization in the Context of Interactive Optimization", In Proceedings of the 2014 Conference on Genetic and Evolutionary Computation (GECCO'14), pp. 461-468, 2014. DOI: 10.1145/2576768.2598213

> [Meignan 2015] D. Meignan, "An Experimental Investigation of Reoptimization for Shift Scheduling", In Proceedings of the 11th Metaheuristics International Conference (MIC'15), Agadir, Morocco, June 7-10, 2015.

License
=======
    Copyright 2011-2016 David Meignan

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
