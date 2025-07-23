@page indexdocumentation Documentation

The engine uses doxygen to document its features, code, and organization.

## Building
On windows, doxywizard can be used to build the documentation in a gui format. Simply open Renderer/docs as the working directory from the top, then open the file "Doxyfile". The project should be preconfigured. Lastly move to the "Run" tab, click "Run doxygen" to generate documentation. You can then optionally click "Show HTML Output" to navigate to the documentation locally via native browser.

TODO: Linux instructions


## Usage Notes

### Images
Images have proven to be tricky with doxygen on windows. Currently, they need to be in the tree under docs/src/images


### Style (Doxygen-Awesome)
Doxygen knows to use the theme because a couple variables are set, and certain files exist under docs/src/doxystyle
 * HTML_HEADER is set to the path for the header file of doxygen-awesome
 * HTML_EXTRA_STYLESHEET is set to the main css file for doxygen-awesome
 * HTML_EXTRA_FILES contains all the js files of doxygen-awesome, which are called via the header html file