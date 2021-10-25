## Welcome to my Sisyphus table pattern generator!

### What is it?

Here are some videos of what this can do

- My latest batch - https://youtu.be/aETkSvVGNjo
  - the previews were stitched in to an 8+ hour long video!  Unfortuately, that exceeded the 125 GB limit for YouTube,
    so I had to split it into 2 "smaller" 115 GB chunks
  - Pt 1: https://youtu.be/AToWMzoTWOk
  - Pt 2: https://youtu.be/QgAuZkCiKb4
  - A 2x speed complete video, but it's faster than I like, so that's why I split it - https://youtu.be/cMcXebgzvUk
- An earlier video I did, shows a week's worth of my work, plus a bunch of other folks stuff
  - https://youtu.be/oxXi2cRQTB0

### ffmpg

`ffmpg` is the tool I used to stitch the thousands of images together into a .mp4 MPEG.

I used [Name Mangler](https://manytricks.com/namemangler) to take all the thousands of files with the descriptive names,
and rename them into sequential files that `ffmpeg` needs.  `Name Mangler` does this handily, including inserting
prefacing "0"s, so you get files like `image_00001` instead of `image_1`.

You can install `ffmpeg` via Homebrew - simply do
```brew install ffmpeg```
I LOVE Homebrew for stuff like this!

Once installed, say you've got an `image` directory where all your images are output. Then, you'd type in something like
this:

```
ffmpeg  -r 20 -f image2 -s 600x600 -i images/image_%5d.png -vcodec libx264 -crf 25 -n -pix_fmt yuv420p my_movie.mp4
```

NOTE:  the bit `images/image_%5d.png` above tells it to take any files with 5 numbers in it (that's what that `%5d`
means - It's very important that you do NOT use `*` instead, as the shell will expand that out, and that's not what the
program needs.

Once generated, the size seems independent of what I tried to specify above with the `-s 600x600`, so I do another run
which will resize it:

```
ffmpeg -i my_movie.mp4 -vf scale=600:600 my_movie_600x600.mp4
```

### Video Quality & Sharing

YouTube is very particular about the size of your animations, 2560 x 1440 is "HD". Anything OTHER than what they like
will result in your video being resampled, and it'll look like crap.

Also worth noting, is that it took YouTube a _week_ to process the HD version of these - regular videos get processed in
minutes, so the crisp computer images need more processing and don't get processed promptly.