# sisyphus-table-pattern-maker
## What does it do?
This app creates and displays .thr files for sand tables like the Sisyphus.

You can also run the display part separately, and display a previously generated or downloaded file.

## How do I run it?
 - First, make sure you have a version of Java in your path.  JDK 8-12 will work.  From a command line you should be able to type `java -version` and have that work
 - Second, you'll probably want to use an IDE (although you can also use a text editor) to create or modify source files.  I highly recommend JetBrains' IntelliJ IDEA.
 - The project uses Gradle to build and run.  Most IDEs know how to open Gradle projects (open the `build.gradle` file using the `open as project` option).  You may need a plugin for Eclipse. 

## What language is it written in?
The app is written in Kotlin (https://kotlinlang.org/), which is part of the JVM ecosystem. Gradle and Intellij know how to build/run Kotlin apps.  

## What are the classes to run?
There are 2 main classes - one lets you generate patterns, the other lets you pre-visualize them.
 - `Generator.kt` 
   - This generates the .thr file, based on a `Shape` that you've created (see below)
   - Modify the existing basic shapes, or create new ones
   - Output is trimmed so that rho never exceeds 1, nor is less than 0
   - Successive duplicate points are eliminated, as they're redundant
   - Shape settings are stored at the top of the file as comments, and the entire `Shape` file stored as a comment at the end
     - This lets you easily remember how you built that cool shape after you lost the settings :)
   - When the shape is generated (the file name is taken from the class fileName attribute), a very poor visualization window is brought up tracing the path of the ball.
     - Hit any key to close the window and end the program.
 - `GuiController.kt`
   - This class is what's used to visualize the paths created by the `Generator`.  If you run the app from the command line, you can use it to visualize any existing track.  
   -  Use the included script to run it, like so: 
   ```showThr.sh someThrFile.thr``` 
   - Press any key to close window and end the program


## Object domain
As mentioned the `Generator` uses `Shape`s to create patterns.  Basically, a `Shape` is drawn many times, with a certain reduction in size each time, a certain number of revolutions, etc.

### Shapes
 - Are composed of 2 or more `Segments` (app will auto-connect the end of one segment with the beginning of the next)
 - A `Shape` can be like a large triangle that spans the table (3 segments), or a smaller shape that starts at the edge and gets drawn over and over as it gets to the center

### Segments
 - Are defined by exactly 2 points
 - When drawing, may be broken down into smaller sub-segments
   - This is because a in the world of our table, the table connects two points with a path of constant delta rho and delta theta.  In Cartesian coordinates, that'd be a straight line.  But because the table uses polar coordinates, that makes the path a curve.
   - So to fake a straight line, we break each segment up into several smaller segments.  When small enough, the curvature in the little bits isn't noticeable.
   - I've found that for long lines (spanning the table), 20 segments seems good.  You can see the arcs with 10
   
### Points
A `Point` is a single theta-rho coordinate - it gets output as a single line in the `.thr` file

## Can I just write my own generator class and re-use the frameworks?
Sure - look at com.nurflugel.sisyphus.sunbursts.SunburstGenerator.java as an example - in this case, I'm generating the list of coordinates by a completely different
algorithm, but calling the GuiController to visualize it.

## Bugs, issues, suggestions
If you have bugs, please create an issue.  Better yet, create a branch and submit a pull request!


Enjoy!  


Douglas Bullard
