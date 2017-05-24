Race the Machine
================

A Machine Learning project for CSCI489

## Concept ##

The idea of the project is to develop a neural network
to play "Race the Sun". An image processor will
first grab and process a snapshot of the game's screen,
and then the results of the processed image - which will
be basic geometric shapes as well as positional and size
differences from the last processed image - will be fed
into a neural network.

After, the system is told how well the network performed, allowing
it to perform "natural" selection based on the score as a fitness
value. The score is a natrual choice as it represents both the
distance traveled, and the extra points collected during that time.
Because of multipliers, a well performing AI could see quadratically
increasing score, further helping the system to decide.


## Building our Code ##
To build our code, you will need Java and Gradle installed. If you
are not running on Linux or Windows, you will also need to
[build OpenCV](http://docs.opencv.org/2.4.11/doc/tutorials/introduction/desktop_java/java_dev_intro.html)
for your platform. The command we used was `cmake -DBUILD_SHARED_LIBS=OFF -DCMAKE_BUILD_TYPE=Release -DBUILD_TESTS=OFF ..`
Note: You will need `ant` installed to build OpenCV for Java.

Unfortunately due to updates, you may need to rebuild on linux to run
depending on what system you use. All tests were run with Arch, so
chances are we used newer versions of some libraries.

Once built, stick the binary library in the root of race-the-machine.

To run OpenAI Gym, you will need to install it with pip; we recommend
installing both `gym` and `'gym[box2d]'`. Take a look at their
[github page](https://github.com/openai/gym) for more information.


## Running our Code ##
Currently HyperNEAT does not fully work, and AHNI is not provided with
reliable score information making it incapable of learning as well.
That said, to give a test to our NEAT algorithm, there are two main
tests worth trying. The first is the XOR test, which is under the
`neural-net/plu.teamtwo.rtm.experiments` package.

To run any of the NEAT OpenAI Gym tests, first run the PythonServer in
the same experiments directory, and then run `python/openai_gym_neat.py`.
In the python file, you can change out what environment is being tested.

Unfortunately Py4J in its current configuration requires that only one
server-client pair is run at a time, and if you stop one, make sure to
stop both because the Java server will not reset its information otherwise,
even if it accepts a new connection.

To run the Race The Sun portion which will demonstrate graphics
processing, run the `client/plu.teamtwo.rtm.client.Main` class. If
you get an error when trying to run it, it is probably because you
need to compile OpenCV for your system.


## Tuning Parameters ##
There are a lot of parameters that can be tweaked within the
system, unfortunately we have not gotten around to creating a
configuration file yet, so I will list the locations to check
for the constant parameters - note that all of these are under
the `neural-net/plu.teamtwo.rtm` package.

- `neat.GAController` is the main set of configurations, and control
   the environment the individuals and species exist in.
- `neat.Species` has configurations for determining species behavior
   and or membership.
- `genome.graph.GraphEncoding` has configurations for the base
   NEAT encoding, these parameters will also alter how HyperNEAT
   works as a `GraphEncoding` is used by it internally.
- `genome.graph.MultilayerSubstrateEncoding` has configurations
   for HyperNEAT only parameters.