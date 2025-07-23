@page btreeorganization Behavior Tree Organization


General design flow when thinking about behavior trees

1. Sequence node that checks initial conditions for this tree to activate at all
2. Selector node that selects between the different "phases", "goals", whatever you want to call them
3. Generally want to flow like:
4. Check conditions for this phase
5. If conditions pass, set variables to perform actions
6. Perform actions
7. If this phase is an action that we want to represent as ongoing, return a Runner node at the end of the phase