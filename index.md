## Welcome to my Sisyphus table pattern generator!

### What is it?

Sample animation
- [8,000 image animation - page with autoplay](http://www.nurflugel.com/Home/temp/video/fullSize.html)
- [10,000 image animation - page with autoplay](http://www.nurflugel.com/Home/temp/video/plots1.html)
- [10,000 image animation - direct link](http://www.nurflugel.com/Home/temp/video/sisyphus_plots_1.mp4)

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

The `x264` codec makes everything look nice - but because of the graphics nature, the files get large - gigabytes.  

I've not found any way to share these on Flickr or YouTube that doesn't result in them resampling them and making them
look like crap - but I have my own web domain, so uploading them there and linking works just fine.