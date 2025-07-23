@page Fonts Fonts




## High Level Overview
The font loading mechanism loads all fonts at engine startup. It leverages java.awt.Font to read font data from ttf files on disk, render them to a bitmap, and then display appropriately.
Globals has a font manager that can be leveraged to get fonts based on identification strings.










## Major Usage Notes























## Main Classes

[Font.java](@ref #electrosphere.renderer.ui.font.Font) - Holds all the data about a font: the glyphs and metadata about them, as well as the material containing the actual bitmap

[FontManager.java](@ref #electrosphere.renderer.ui.font.FontManager) - Keeps track of all fonts loaded into the engine.

[FontUtils.java](@ref #electrosphere.renderer.ui.font.FontUtils) - Utilities to load fonts from disk

[BitmapCharacter.java](@ref #electrosphere.renderer.ui.font.bitmapchar.BitmapCharacter) - The main rendering component for text. Renders a single character of text based on a given font, font size, and character.












## Code Organization and Best Practices

#### Startup


#### Usage











## Terminology









## Known Bugs To Fix




## Future Goals

 - Scan assets/fonts and load all fonts automatically
 - Ability to specify font for a given label (currently it's always hard-set to "default")