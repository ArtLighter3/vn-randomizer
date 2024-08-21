# Steins;Gate/Chaos;Child VN Randomizer
Supported VNs (Tested for PC Steam):
- Steins;Gate
- Steins;Gate 0
- Chaos;Child (may be bugged for now)

MAY work on these VNs too (Didn't check):
- Steins;Gate: Linear Bounded Phenogram
- Steins;Gate: My Darling's Embrace

I will make graphical interface later, for now it's just commandline program

# How to use
- Go to releases folder and download .zip archive for the latest version. 
- Unpack .zip contents DIRECTLY in root folder of your VN.
- Run vn-randomizer. By default it randomizes character sprites, soundtrack and backgrounds that are packed in .mpk format.

You can use command line and flags to configure randomize options. You can additionally randomize sound effects, CGs and voice lines. 

Usage: vn-randomizer [flags...]

Flags:

-C, --char (Randomize characters)

-m, --music           (Randomize soundtrack)

-b, --bg              (Randomize backgrounds)

-c, --cg              (Randomize CGs)

-s, --sound           (Randomize sound effects)

-v, --voice           (Randomize voice lines)

--lowmemory           (Run on low memory mode (if you get OutOfMemoryError))

--restore             (Restore default resource files)

For example, if you want to randomize default characters, bgs and ost with sound effects, you have to specify all flags like this: 
```
vn-randomizer --char --music --bg --sound
```

If you have less than 4GB RAM and get OutOfMemoryError - run vn-randomizer with additional flag --lowmemory. For example:
```
vn-randomizer --char --music --bg --sound --lowmemory
```
