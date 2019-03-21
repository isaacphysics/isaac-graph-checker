# isaac-graph-checker

`isaac-graph-checker` is the library for marking graphs in the [Isaac Physics project](https://isaacphysics.org/about).

The basic idea behind the graph checker is that a graph can be recognised by it having certain features, for example:

- being an odd or even function
- passing through certain areas in a certain order
- have a particular slope in a particular area

The library can also be used in reverse to generate the features that would recognise a particular graph.
This is useful for setting questions. 

## Project structure

There are three top-level modules:

- **library** The library itself.
- **demo** A demo wiring up of the library to an HTTP endpoint that can be jury-rigged to Isaac.
- **bluefin** A simple web application for tuning the settings and examining samples.

The demo application writes samples into the top-level samples directory, and the bluefin application reads its samples
from there.

## Library structure

The features.Features class is the starting point and provides methods to mark an answer and to generate a set of
features.

The remainder of the library is arranged in the follow subpackages:

- **dos** Classes representing external JSON objects that we input/output. Uses Jackson and should contain no logic.
- **data** Internal classes representing pure data for graphs. No complex computation should be done in here.
- **translation** Utility classes to translate between dos and data.
- **geometry** Any geometric calculations should be in here.
- **features** The individual features and the overall recogniser lives here.
- **features.internals** Various bits of internal wiring to abstract shared parts between features.
- **settings** Wiring for settings. If you want to customise the settings, look at bluefin.CustomSettings to see how.

There are two types of features: InputFeature and LineFeature. An InputFeature receives the whole input and can match
based on that. Currently, the only InputFeature just counts the number of submitted curves.
A LineFeature matches an individual line, for example looking for slope or symmetry.

There are also classes called LineSelector. These receive the input and a LineFeature, and can chose to apply the
LineFeature to any, all or none of the lines in the input and match based on that. Currently, the only LineSelector is
NthLineSelector, which just matches if the n-th line (ordered left to right by starting x co-ordinate) matches the
provided LineFeature.

All features are described with a feature specification with the following syntax:
```
  <features>    ::= <feature> { CRLF <features> }
  <feature>     ::= <inputFeatureTag> : <inputFeatureSpec>
                  | <lineFeature>
                  | <lineSelectorTag> : <lineSelectorSpec> ; <lineFeature>
  <lineFeature> ::= <lineFeatureTag> : <lineFeatureSpec>
```

## License

   Copyright 2019 University of Cambridge

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
