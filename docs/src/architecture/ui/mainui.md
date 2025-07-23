@page mainui Main UI Framework

It's kind of a mess. One of the main libraries underpinning the ui framework is Yoga. It handles laying out all elements in a flexbox fashion.
Grafting yoga onto the initial implementation after the fact has confused many parts of the classes.
A chief pain point currently is handing position, dimensions (width, height), and the different version of each.
IE there's a position relative to the screen, position relative to the parent, position relative to the yoga parent, etc.
Yoga typically returns the position of the yoga element relative to its parent yoga element.
The original implementation of the ui framework had absolute positions stored in all elements at all times.
This is something that will need to be untangled in the future most likely.