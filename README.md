# sisyphus-table-pattern-maker

Check out the Github Pages for this repo - [https://dbulla.github.io/sisyphus-table-pattern-maker/](https://dbulla.github.io/sisyphus-table-pattern-maker/)

## What does it do?

This app creates and displays .thr files for sand tables like the Sisyphus. It also can generate and display GUI
previews of tracks.

You can also run the display part separately, and display a previously generated or downloaded file.

## How do I run it?

* First, make sure you have a version of Java in your path. OpenJDK 8-16 will work. There are noticeable speed
  improvements in the later versions of Java - about 5x faster than JDK 8, so it's worthwhile to update!
* After checking out the repo, execute `./gradlew run` from the command line.
* The project uses Gradle to build and run. Most IDEs know how to open Gradle projects (open the `build.gradle` file
  using the `open as project` option). You may need a plugin for Eclipse.

## What language is it written in?

The app is written in Kotlin (https://kotlinlang.org/), which is part of the JVM ecosystem. Gradle and Intellij know how
to build/run Kotlin apps.

- `ClockworkWigglerGenerator.kt`
  ** This is the most recent generator I'm using - Imagine a second hand on a clock. Now, imagine that on the end of the
  second hand there was another, smaller clock with _it's_ second hand - what would the trace of the end of that small
  second hand look like as they both rotated at differing frequencies? That's what this program does.

- `GuiController.kt`
    - This class is what's used to visualize the paths created by the `Generator`. If you run the app from the command
      line, you can use it to visualize any existing track.
    - Press any key to close window and end the program

-`ImageWriterController.kt`

- This class is what's used to generate the .png files for the paths created by the `ClockworkWigglerGenerator`. If you
  run the app from the command line, you can use it to visualize any existing track.

## Object domain

As mentioned, the `ClockworkWigglerGenerator` uses `Shape`s to create patterns. Basically, a `Shape` is a series of
points which we want to draw lines through.

### Shapes

- Are composed of 2 or more `Segments` (app will auto-connect the end of one segment with the beginning of the next)
- A `Shape` can be like a large triangle that spans the table (3 segments), or a smaller shape that starts at the edge
  and gets drawn over and over as it gets to the center

### Segments

- Are defined by exactly 2 points
- When drawing, may be broken down into smaller sub-segments
    - This is because a in the world of our table, the table connects two points with a path of constant delta rho and
      delta theta. In Cartesian coordinates, that'd be a straight line. But because the table uses polar coordinates,
      that makes the path a curve.
    - So to fake a straight line, we break each segment up into several smaller segments. When small enough, the
      curvature in the little bits isn't noticeable.
   - I've found that for long lines (spanning the table), 20 segments seems good.  You can see the arcs with 10
   
### Points
A `Point` is a single theta-rho coordinate - it gets output as a single line in the `.thr` file

## Can I just write my own generator class and re-use the frameworks?
Sure - look at `com.nurflugel.sisyphus.sunbursts.SunburstGenerator.java` as an example - in this case, I'm generating the list of coordinates by a completely different
algorithm, but calling the GuiController to visualize it.

## Bugs, issues, suggestions
If you have bugs, please create an issue.  Better yet, create a branch and submit a pull request!


Enjoy!  


Douglas Bullard
