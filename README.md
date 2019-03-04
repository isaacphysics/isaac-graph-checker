# isaac-graph-checker

`isaac-graph-checker` is the library for marking graphs in the [Isaac Physics project](https://isaacphysics.org/about).

The basic idea behind the graph checker is that a graph can be recognised by it having certain features, for example:

- being an odd or even function
- passing through certain areas in a certain order
- have a particular slope in a particular area

The library can also be used in reverse to generate the features that would recognise a particular graph.
This is useful for setting questions. 

## Project structure

The Marker top-level class provides methods to mark an answer and to generate a set of features.
The remainder of the project is arranged in the follow subpackages:

- **data** Internal classes representing pure data for graphs. No complex computation should be done in here.
- **dos** Classes representing external JSON objects that we input/output. Uses Jackson and should contain no logic.
- **translation** Utility classes to translate between dos and data.
- **geometry** Any geometric calculations should be in here.
- **features** The individual features and the overall recogniser lives here.
- **standalone** A temporary web application for testing purposes.