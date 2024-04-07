This project consists of two parts, **a java app** to convert any image into shapes for OCs 3D Printer
and a **LUA script** which handles the actual printing of the jobs including error detection and progress reporting. Each 3D Print is one block big and can have up to 16x16 pixels, we call each individual block a cell.

The java program tries to make the shapes that make up the cells as big as possible, to do this
it first combines pixels with the same color horizontally and then vertically, this improves performance for very large print jobs.

## The LUA Part

**Requirements:**
The LUA script requires a basic tier 1 computer with a monitor attached. 
An internet card is highly recommended.

To download the program simply type in this command after installing OpenOS:
**pastebin get {PLACE_HOLDER} 3dbatchprinter.lua**

You can run the script like this:
![[Pasted image 20240407075639.png]]

Running the script without any parameters will show that help.
Any optional parameter you don't supply will make the program prompt you for it once it starts.
For easiest usage just use it with a pastebin code.
![[optionsExample.gif]]

**Error detection**
Should you run out of ink or if your output storage fills up the program pauses and waits for you to fix the issue. It will automatically resume when the problem is resolved.

**Orientations:**
You can print in three orientations: **wall, ceiling and floor**
Here is an example for a wall print (works in all directions) and a floor print.
![[Pasted image 20240407080203.png]]

![[Pasted image 20240407080230.png]]


### The Java Part

**Notable Features:**
Drag and drop an image into the "Input Image" view to load a new imagine

**Cosmetic Image zoom**
You can zoom into both the input and output image with the scroll wheel, this is entirely cosmetic no transformations are being applied to the actual image. You can use this to zoom in to fine tune the alpha threshold for example.
Upon loading an image or committing changes to it it tries to scale itself to fit the view.
![[zoom.gif]]

**Alpha Threshold**
The 3D Printer cannot display semi-transparency. 
The slider sets any pixel with a transparency less than the threshold to be entirely transparent and sets any pixel above the threshold to be fully opaque. 
This is mostly necessary for pictures with bad dithering or anti-aliasing.
Moving the slider automatically updates the output image.
Use the zoom to check for any artifacting, the default suits most images.
This test image has many different transparency levels to show the effect.
![[alphathreshold.gif]]

**Resizing**
Resizes the image to a multiple of 16 pixels, padding will be supported soon.
The output does not have to be square it can also be rectangular.
![[resize.gif]]

**Print placeholders for empty cells**
If a cell (16x16 pixels) is entirely transparent you can either discard it and skip printing it
or alternatively you can print a placeholder for those cells.
This is useful if you want to use a chest to lay the printed cells out layer by layer.
To learn more about this check out the Tipps and Tricks section.

**Upload to pastebin**
Upload the exported file to pastebin and copy the command you need to run on
the OpenComputer to the clipboard. Especially useful when playing on servers.
![[Pasted image 20240407081346.png]]

**Save to file**
If you have access to the filesystem of your world you can also save the file directly
and manually move it to the desired computer. This is not recommended.
The pastebin method is more comfortable to use.
Enter the source image location as the first command-line arg, and the output file location as the second
