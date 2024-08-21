# Steins;Gate/Chaos;Child VN Randomizer
Supported VNs (Tested for PC Steam):
- Steins;Gate
- Steins;Gate 0
- Chaos;Child (chars can be bugged for now)

MAY work on these VNs too (Didn't check):
- Steins;Gate: Linear Bounded Phenogram
- Steins;Gate: My Darling's Embrace

I'm planning to make graphical interface later, for now it's just command line app.

# How to use
- Go to releases folder and download .zip archive for the latest version. 
- Unpack .zip contents DIRECTLY in root folder of your VN.
- Run vn-randomizer. By default it randomizes character sprites, soundtrack and backgrounds that are packed in .mpk format.
- If everything's good then you'll see total amount of randomized files. (However, there may be errors in particular stages, check the output lines)

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

--help (Show help)

For example, if you want to randomize default characters, bgs and ost with sound effects, you have to specify all flags like this: 
```
vn-randomizer --char --music --bg --sound
```

To restore default resources of the vn, use --restore flag:
```
vn-randomizer --restore
```

If you have less than 4GB RAM and get OutOfMemoryError - run vn-randomizer with additional flag --lowmemory. For example:
```
vn-randomizer --char --music --bg --sound --lowmemory
```

# Chaos;Child
Well, there's a problem that I know how to fix and will do it later. Everything should randomize properly BUT character sprites in C;C. You may experience pure horror VN.exe thing in this situtation: Character X has, for instance, 9 different facial expressions and he gets replaced by character Y which sprite has 5 expressions. Eventually game chooses 8th expression on character X and guess what. The engine draws base sprite (body, part of hair) but doesn't find face sprites for 8th expression. As a result, character has blank transparent face.

I've fixed this problem in S;G/0 by modifying bytes in these .lay files, but C;C needs more attention because of blinking animations (And for that I need to refactor some things), so be aware of this if you're still willing to randomize characters in C;C.

