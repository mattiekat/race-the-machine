Race the Machine
================

A Machine Learning project for CSCI489

Concept
-------

The idea of the project is to develop a neural network
to play the game "Race the Sun". An image processor will
first grab and process a snapshot of the game's screen,
and then the results of the processed image - which will
be basic geometric shapes as well as positional and size
differences from the last processed image - will be fed
into a neural network. The neural network will be told
it is doing a good job if it survives for a long distance
and gets a higher score, and will be told it is doing a bad
job if it crashes.