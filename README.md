# Steins;Gate/Chaos;Child Visual Novel Randomizer
Just a fun way to replay your favorite VNs. This program randomizes different assets of some Mages Visual Novels (Actually, planning to do more VNs in future). Characters can be shuffled with each other as well as sounds, backgrounds, CGs, music. Skins and poses of one character after shuffling of characters between each other are randomized too.

Supported VNs (Tested for Windows Steam):
- Steins;Gate
- Steins;Gate 0

SHOULD work on these VNs too (Didn't test though):
- Steins;Gate: Linear Bounded Phenogram
- Steins;Gate: My Darling's Embrace

Works partially:
- Chaos;Child (Character randomizing is somewhat bugged)

# How to use
- Go to the releases page on the right, find the latest version, click on Assets and download .zip archive. 
- Unpack .zip contents in folder without any semicolons in path.
- Run vn-randomizer. Select ROOT DIRECTORY of the game you want to modify by clicking Open VN Directory.
- By default it randomizes character sprites, soundtrack and backgrounds that are packed in .mpk format in USRDIR folder of VN. You can optionally choose to randomize CGs, sounds and voice lines or, for example, only characters. Just check needed checkboxes to customize randomization.
- Wait till the log text area shows total amount of randomized files.
- To restore default resources just click Restore backups.

# Chaos;Child
Well, there's a problem that I know how to fix and will do it later. Everything should randomize properly BUT character sprites in C;C. You may experience pure horror VN.exe thing in this situtation: Character X has, for instance, 9 different facial expressions and he gets replaced by character Y which sprite has 5 expressions. Eventually game chooses 8th expression on character X and guess what. The engine draws base sprite (body, part of hair) but doesn't find face sprites for 8th expression. As a result, character has blank transparent face.

I've fixed this problem in S;G/0 by modifying these .lay files that define sprites and adding there bytes that represent default first expressions but with new indices. However, C;C needs more attention because of blinking animations (And for that I need to refactor some things), so be aware of this if you're still willing to randomize characters in C;C.

