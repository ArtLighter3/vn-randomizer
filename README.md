# Steins;Gate/Chaos;Child Visual Novel Randomizer
Just a fun way to replay your favorite VNs. This program randomizes different assets of some Mages Visual Novels (Actually, planning to do more VNs in future). Characters can be shuffled with each other as well as sounds, backgrounds, music. Skins and poses of one character after shuffling with each other are randomized too.

Supported VNs (Tested for Windows Steam):
- Steins;Gate
- Steins;Gate 0
- Chaos;Child (chars can be bugged for now)

SHOULD work on these VNs too (Didn't test though):
- Steins;Gate: Linear Bounded Phenogram
- Steins;Gate: My Darling's Embrace

I'm planning to make graphical interface later, for now it's just command line app.

# How to use
- Go to the releases page on the right, find the latest version, click on Assets and download .zip archive. 
- Unpack .zip contents DIRECTLY in root folder of your VN.
- Run vn-randomizer. By default it randomizes character sprites, soundtrack and backgrounds that are packed in .mpk format in USRDIR folder of VN.
- Wait till the command line closes. If you're running this directly from terminal then you'll see total amount of randomized files in the end.

You can use command line and flags to configure randomize options. You can additionally randomize sound effects, CGs and voice lines. So, in Windows open cmd and use cd to locate folder with the randomizer and VN.

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

--help (Show help)

For example, if you want to randomize default characters, bgs and ost with sound effects, you have to specify all flags like this: 
```
vn-randomizer --char --music --bg --sound
```

To restore default resources of the vn, use --restore flag (OR just launch restore.bat):
```
vn-randomizer --restore
```

If you have less than 4GB RAM and get OutOfMemoryError - run vn-randomizer with additional flag --lowmemory. For example:
```
vn-randomizer --char --music --bg --sound --lowmemory
```

# Chaos;Child
Well, there's a problem that I know how to fix and will do it later. Everything should randomize properly BUT character sprites in C;C. You may experience pure horror VN.exe thing in this situtation: Character X has, for instance, 9 different facial expressions and he gets replaced by character Y which sprite has 5 expressions. Eventually game chooses 8th expression on character X and guess what. The engine draws base sprite (body, part of hair) but doesn't find face sprites for 8th expression. As a result, character has blank transparent face.

I've fixed this problem in S;G/0 by modifying these .lay files that define sprites and adding there bytes that represent default first expressions but with new indices. However, C;C needs more attention because of blinking animations (And for that I need to refactor some things), so be aware of this if you're still willing to randomize characters in C;C.

