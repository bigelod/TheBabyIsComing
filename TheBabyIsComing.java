import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.opengl.*; 
import ddf.minim.*; 
import java.awt.AWTException; 
import java.awt.Robot; 
import java.awt.MouseInfo; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class TheBabyIsComing extends PApplet {

//The Baby Is Coming - A tale in the world of 2BR02B, a public domain story by Kurt Vonnegut (1962)
//This tells a story within the world of 2BR02B, where population control and advanced medicine allows all to live eternally without poverty but not without fears and danger

//3D FPS Engine and game created by Daven Bigelow (2014). Engine code is free for non-commercial use, credit appreciated. Contact bigelod@mcmaster.ca for further information

/*
CONTROLS:
 ---------
 A, D / Left and Right Arrow Keys - Movement
 Mouse - Aim
 Spacebar or Right Mouse Button - Jump
 V or Left Mouse Button- Shoot (ONLY IF THERE ARE ENEMIES IN THE MAP)
 P - Quit
 Enter - Pause
*/

//Just below the imports are a few changable values like resolution and volume

//KNOWN ISSUES:
//------------------------------------------------
//This engine was made in a fairly quick time period for a University project. There are a few unresolved issues that may not change unless I find time to return to this game.
//-Clipping view
//-Able to push through invisible walls at certain angles (For some reason this doesn't happen with ground objects?)
//-Skybox doesn't rotate on Z axis (up/down), and attempting to rotate causes it to rotate only by the corner point
//-Rotation doesn't fully work for textured ground objects, seems off-center
//-Text does not scale/refit based on resolution
//-Poorly written code/work-arounds
//-Text not showing over the 3D elements that are closer to player
//-Not a true fog at the ending/Everything is full-bright/lacking lights

//FIXED ISSUES:
//------------------------------------------------
//-Screen drags in a direction, new system based on absolute mouse positions works now (tested only on a single monitor PC!)

//EDITING ADVICE:
//------------------------------------------------
//I hope that this engine can help others out in some ways, even if as an example of some of the power that Processing contains (it's still Java under the covers :) )
//The game required some changes to the engine that bypassed the built-in features like level-changing, and not-screwing-up-the-screen-at-the-end :D (game needed it)
//Anyway, here's a break-down of the code: (I tried to comment most of it, and yes, I do prefer the // commenting :D )
/*
The game loads maps as plain-text like an INI or CSV format, there is a Windows program called LevelEditor3D.exe (made in the Open Source program Construct Classic) to help develop them
(Check out leveleditor_readme.txt for the instructions on how to use the editor tool)


A maplist is loaded in setup(), and then actual map data is collected at the starting of a game or if the player has lives left during draw(), thus there is a levelist3D.txt and level(s)

The program goes in stages:
0 = Title screen - This is where the title screen/intro is handled
1 = In-Game - This is where maingame code runs
2 = Game Over - This is where the final outcome screen runs for either winning or losing the game
3 = Credits - This is just a credits screen about the game

Here's a bit about the classes:
BulletClass - The class code for bullets. The player can only shoot when enemies are in a map.
CrazyText - The class code specific to the game to create random pieces of text that fade out over time
DeathboxClass - The class code for objects that destroy the player when they are within it
EnemyClass - The class code for enemies that will chase after the player and jump after them, they can be placed in maps or automatically spawn
Functions2D - Covers the functions used for 2D collisions and checking positions within rectangles
Functions3D - Covers the functions used for 3D collisions and checking positions within cubes, also has the code for rendering textured cubes and skyBox
GroundClass - The class code for ground objects, these can be moving platforms or just standard ground blocks
MiscClasses - A selection of code is in here, including FPS camera or (old) 3rd person camera, also has HUD/pause text and controls code, fonts are loaded there too
MissionGoal - The class code for a mission goal block, the tall blue pillars of light
PlayerClass - The class code for the player themself

Aside of that, I tried to keep most code commented, but as time constraints came I may have left some of the less obvious bits uncommented, but the grade turned out great!
You can send me an e-mail at bigelod@mcmaster.ca if you have any other questions!
*/

//Newgrounds Audio - Music Licenses
//------------------------------------------------
//Attribution: You must give credit to the artist.
//Noncommercial: You may not use this work for commercial purposes unless you make specific arrangements with the artist.
//Share Alike: If you alter, transform, or build upon this image, you may distribute the resulting creation only under a license identical to this one. 
//http://www.newgrounds.com/audio/listen/591002
//http://www.newgrounds.com/audio/listen/561743
//http://www.newgrounds.com/audio/listen/585871
//http://www.newgrounds.com/audio/listen/593282

 //Load OpenGL library for doing 3D
 //The audio library
 //Exception check
 //Move mouse
 //Get absolute mouse position

//CHANGEABLE/GLOBAL VALUES
float gameVolume = -12; //Change this to make music louder/quieter as needed
int winWidth = 1024; //Width
int winHeight = 768; //Height

//Prevent screen drag & center mouse
int frameCenterX = 0;
int frameCenterY = 0;

//Engine declarations
Minim minim; //Define audio object

//Define audio elements
AudioPlayer titleTrack;
AudioPlayer gameTrack;
AudioPlayer winTrack;
AudioPlayer endTrack;

Robot robby; //Create an instance of Robot class to move mouse
String osName; //For determining pixel offsets of Windows/Mac frames

//Defining font objects for preparation later
PFont mainFont;
PFont objectiveFont;
PFont insaneFont;

PImage questGiver; //Holds the image to load and display for the quest giver
PImage discussionImage; //Holds the image to load and display

//Skybox textures
PImage skyFront;
PImage skyBack;
PImage skyTop;
PImage skyBottom;
PImage skyLeft;
PImage skyRight;
boolean skyBox = true; //Draw a skybox or not

int programStage = 0; //0 = Title Screen, 1 = In-Game, 2 = Game Over, 3 = Credits

float titleTicks = 0; //A count-down used on title and credits for animation
int titleTextStage = 0; //A count of which text is currently drawing
float titleFadeSpeed = 2; //Speed to fade in text

float gameTicks = 0; //Current game time, used for sin and cos calculations and etc
float currLevel = 1; //Current game level value
boolean inGame = false; //A boolean value used to say if the game is currently playing (used for level switching and restarts)
boolean wonGame = false; //If player has won the game

boolean talkScreen = false; //If player is talking to someone
boolean missionComplete = false; //If the player has beaten the mission or taken one
int talkSituation = 0; //The current conversation situation
float talkTimer = 0; //Timer before user can continue

int mNum = 0; //The current player mission
String mGoal = ""; //The current player mission goal
int mSanityLimit = 2; //The mission number when things are less sane
int mLastMission = 0; //The final mission a player will have
boolean flipControls = false; //Towards the end life gets hard...
float endGameFade = 0; //A variable used to decide how much to fade out of the game

boolean pauseGame = false; //If the game is paused

float maxBad = 10.0f; //The number of enemies maximum if makingBad = true
boolean makingBad = false; //A boolean value to determine if enemies should spawn from above

float backR = 255; //A variable for the background Red value
float backG = 255; //A variable for the background Green value
float backB = 255; //A variable for the background Blue value

Player p1 = new Player(32, 32, 0, 48, 48, 48); //The player object, based on Player class
float p1Lives = 1; //The players' lives
float p1MaxLives = 1; //The players' max lives
float p1Score = 0; //The players' score

float camX = 0; //Camera Eye X position
float camY = 0; //Camera Eye Y position
float camZ = 0; //Camera Eye Z position
float camLastX = 0; //Camera last Eye X position
float camLastY = 0; //Camera last Eye Y position
float camLastZ = 0; //Camera last Eye Z position
float camTargetX = 0; //Camera target X position (to look at)
float camTargetY = 0; //Camera target Y position (to look at)
float camTargetZ = 0; //Camera target Z position (to look at)
float camRotOff = 0; //Camera X axis "rotation" offset from player
float camRadius = 500; //Camera initial radius from player in 3rd person view
float camHeightOff = 25; //Camera height offset from player in FPS person view
float camLookOff = 0; //Camera look angle offset from player in FPS person view
float deathPlaneY = 2000; //The max Y position downward a player can go before they die (infinite falling fix)
float viewDistance = 1000; //The distance we can see blocks at
float mouseRatioX = 0; //For FPS view
float mouseRatioY = 0; //For FPS view
boolean pauseResetCam = false; //Reset the cam when unpaused


ArrayList<gameLevel> levels; //A dynamic array of levels in the game, loaded from file levellist.txt
ArrayList<gameGround> grounds; //A dynamic array of the ground objects in the game, loaded from each level file
ArrayList<deathBox> deathboxes; //A dynamic array of rectangles that kill the player, loaded from each level file
ArrayList<bullet> bullets; //A dynamic array of rectangles to represent bullets, created and destroyed in-game

ArrayList<enemy> enemies; //A dynamic array of the circles to represent enemies, created and destroyed in-game

ArrayList<MissionGoal> missions; //An array list for missions
ArrayList<CrazyText> thoughts; //An array for crazy text thoughts

boolean keysDown[] = new boolean [9];
// 0 = up/w (forward)
// 1 = down/s (backward)
// 2 = left/a (strafe left)
// 3 = right/d (strafe right)
// 4 = spacebar (jump)
// 5 = p (quit)
// 6 = q (rotate left)
// 7 = e (rotate right)
// 8 = v (shoot)

//---------------------------------------------------------------------------------------------------------------
//END OF GLOBAL VARIABLE SETUP
//---------------------------------------------------------------------------------------------------------------

//---------------------------------------------------------------------------------------------------------------
//START OF SETUP()
//---------------------------------------------------------------------------------------------------------------
public void setup() {
  size(winWidth, winHeight, OPENGL); //Set the screen size and tell it we're going 3D for this one
  noSmooth(); //Seems faster without smoothing of edges

  frame.setTitle("The Baby Is Coming - A short story in the world of 2BR02B");

  osName = System.getProperty("os.name");

  try //Needed for positioning the mouse at the center of the screen!
  {
    robby = new Robot();
  }
  catch (AWTException e)
  {
    println("Robot class not supported by your system!");
    exit();
  }
  
  frameCenterX = frame.getX()+this.getX()+round(width/2);
  frameCenterY = frame.getY()+this.getY()+round(height/2);

  minim = new Minim(this); //Start the audio engine

  skyTop = loadImage("skyTop.png");
  skyBottom = loadImage("skyBottom.png");
  skyFront = loadImage("skyFront.png");
  skyBack = loadImage("skyBack.png");
  skyLeft = loadImage("skyLeft.png");
  skyRight = loadImage("skyRight.png");

  questGiver = loadImage("giver.png"); //This image doesn't change in quests

  //Load sounds into players
  titleTrack = minim.loadFile("Moment-Of-Desperation.mp3");
  gameTrack = minim.loadFile("Flames-of-Industry_Suicide.mp3");
  winTrack = minim.loadFile("christoko_Dreams.mp3");
  endTrack = minim.loadFile("DFDCinematic_Not-Even-A-Goodbye.mp3");

  currLevel = 1; //The starting level
  p1Lives = 1; //Set player level

  loadFonts(); //Prepare fonts for use in the game

  levels = new ArrayList<gameLevel>(); //We will soon fill the level array

  boolean levelsFromFile = false; //Boolean to check if the level list was loaded

  try {
    String levelsFileLines[] = loadStrings("levellist3D.txt"); //Load the text from the list of levels

      for (int i = 0; i < levelsFileLines.length; i++) { //For each level in the list


      levels.add(new gameLevel(levelsFileLines[i])); //Add the level to the array
    }
  } 
  catch (Exception e) {
    //Error loading levels data, print stack trace to console and let program know it didn't work
    e.printStackTrace();
    levelsFromFile = false;
  }


  if (levelsFromFile == false) {
    //If there is n  o text file saying which levels to load, try default
    levels.add(new gameLevel("level1.txt"));
  }
}

//---------------------------------------------------------------------------------------------------------------
//END OF SETUP()
//---------------------------------------------------------------------------------------------------------------

//---------------------------------------------------------------------------------------------------------------
//START OF MAIN ENGINE LOOP
//---------------------------------------------------------------------------------------------------------------
public void draw() {
  if (programStage == 0) { //Title
    if (titleTrack.isLooping() == false) {
      winTrack.rewind();
      winTrack.pause();
      gameTrack.rewind();
      gameTrack.pause();
      endTrack.rewind();
      endTrack.pause();

      titleTrack.rewind();
      titleTrack.loop();
      titleTrack.setGain(gameVolume);
    }



    cursor(0); //Switch to a regular cursor
    inGame = false; //Set inGame = false
    p1Lives = p1MaxLives; //Reset lives
    p1Score = 0; //Reset score

    talkScreen = false;
    talkSituation = 0;
    missionComplete = false;

    mNum = 0; 
    mGoal = "";
    flipControls = false;

    //Now draw the title page

      if (titleTicks < 255) {
      titleTicks += titleFadeSpeed;
    }

    background(255, 255, 255); //Paint white background

    textFont(mainFont);
    textSize(24); //Text size

    //Title
    if (titleTextStage >= 0) {
      if (titleTextStage > 0) {
        fill(0); //Solid text
      } 
      else fill(0, 0, 0, titleTicks); //Text fill

      text("Everything was perfectly swell.", 10, 40);
    }

    if (titleTextStage >= 1) {
      if (titleTextStage > 1) {
        fill(0); //Solid text
      } 
      else fill(0, 0, 0, titleTicks); //Text fill

      text("All diseases were conquered. So was old age.", 10, 80);
    }

    if (titleTextStage >= 2) {
      if (titleTextStage > 2) {
        fill(0);
      } 
      else fill(0, 0, 0, titleTicks);

      text("Death,barring accidents,was an adventure for volunteers.", 10, 120);
    }

    if (titleTextStage >= 3) {
      if (titleTextStage > 3) {
        fill(0); //Solid text
      } 
      else fill(0, 0, 0, titleTicks); //Text fill

      text("The law said that no newborn child could survive", 10, 200);
    }

    if (titleTextStage >= 4) {
      if (titleTextStage > 4) {
        fill(0); //Solid text
      } 
      else fill(0, 0, 0, titleTicks); //Text fill

      text("unless the parents of the child could find someone", 10, 240);
    }

    if (titleTextStage >= 5) {
      if (titleTextStage > 5) {
        fill(0); //Solid text
      } 
      else fill(0, 0, 0, titleTicks); //Text fill

      textSize(32);
      text("Someone who would volunteer to die.", 10, 320);
    }

    if (titleTextStage >= 6) {

      textFont(objectiveFont);
      textSize(24); //Set text size

      stroke(128);
      strokeWeight(2);
      fill(0);
      rect(10, height-80, 500, 40);

      fill(255);
      text("Find a volunteer to save your baby...", 20, height - 50); //Render press any key
    }

    boolean playerStart = false;

    if (keyPressed && key != 10) playerStart = true;

    if (mousePressed && isInRect(mouseX, mouseY, 10, height-80, 500, height-40) ) playerStart = true;

    if (playerStart & titleTextStage >= 6 & titleTicks >= 255) {
      noStroke();
      fill(255, 255, 255);
      rect(0, height-100, width, height);

      fill(0);
      textFont(objectiveFont);
      textSize(32); //Set text size

      text("LOADING...PLEASE WAIT...", 10, height - 50);

      titleTicks = 0;
      titleTextStage = 0;

      discussionImage = loadImage("giver.png");
      
      robby.mouseMove(frameCenterX, frameCenterY);

      currLevel = 1; //Go to first level
      programStage = 1; //Start game
    }

    if (keyPressed & titleTextStage <= 6 & titleTicks < 250) {
      titleTextStage = 6;
      titleTicks = 250;
    }

    if (titleTicks >= 255 & titleTextStage < 6) {
      titleTicks = 0;
      titleTextStage += 1;
    }
  } 
  else if (programStage == 1) { //Game

    //At the start of a new life/level/of the game, we create the objects
    if (inGame == false) {
      titleTrack.rewind();
      titleTrack.pause();

      gameTrack.rewind();
      gameTrack.loop();
      gameTrack.setGain(gameVolume); //Quiet volume

        talkScreen = false;
      talkSituation = 0;
      missionComplete = false;

      mNum = 0; 
      mGoal = "";

      if (p1Lives <= 0) { //If the player got a game-over
        wonGame = false; //Didn't win :(
        programStage = 2; //Go to game over
      }

      gameTicks = 0; //Reset game timer, keeps the levels consistent

      boolean levelFromFile = false; //Set up boolean to see if level was loaded properly
      boolean playerFromFile = false; //Set up boolean to see if player was loaded properly
      boolean backgroundFromFile = false; //Set up boolean to see if custom background was set
      boolean winBlockFromFile = false; //Set up boolean to see if a "winning block" was placed for player
      boolean deathPlaneFromFile = false; //Set up a boolean to see if a deathplane Y is set in the file
      boolean viewDistFromFile = false; //Set up a boolean to see if the viewdistance is loaded from the file

        grounds = new ArrayList<gameGround>(); //Prepare the grounds array list for objects
      deathboxes = new ArrayList<deathBox>(); //Prepare the deathboxes array list for objects
      bullets = new ArrayList<bullet>(); //Prepare bullet array for game

      enemies = new ArrayList<enemy>(); //Prepare enemy array for game

      missions = new ArrayList<MissionGoal>();

      thoughts = new ArrayList<CrazyText>();

      try {
        for (int i = levels.size () - 1; i >= 0; i--) { //For each level
          //Check game levels
          gameLevel level = levels.get(i); //Load the level object into memory

          if (currLevel - 1 == i) { //If this level object is the level we're playing now
            String levellines[] = loadStrings(level.getLevel()); //Load level data from its file

            //Loaded the level data, lets make a map!
            for (int n = 0; n < levellines.length; n++) { //For each line in the file
              String[] data = split(levellines[n], ','); //Split the file data in CSV format

              if (data[0].indexOf("ground") != -1) { //If the level object is ground
                float xPos = Float.parseFloat(data[1]); //Load x position
                float yPos = Float.parseFloat(data[2]); //Load y position
                float zPos = Float.parseFloat(data[3]); //Load z position
                float Width = Float.parseFloat(data[4]); //Load width of object
                float Height = Float.parseFloat(data[5]); //Load height of object
                float Depth = Float.parseFloat(data[6]); //Load depth of object
                float Type = Float.parseFloat(data[7]); //Load type of object

                float Move; //Load movement amount

                try {
                  Move = Float.parseFloat(data[8]); //Error-proofing for different format files
                } 
                catch (Exception e) { //If data doesn't exist
                  Move = 100;
                } 
                catch (Throwable t) { //Protect against IO error?
                  Move = 100;
                }

                boolean usesTextures = false;

                String frontTex = "";
                String backTex = "";
                String topTex = "";
                String bottomTex = "";
                String leftTex = "";
                String rightTex = "";

                try {//Error-proofing for different format files
                  topTex = data[9];
                  bottomTex = data[10];
                  frontTex = data[11]; 
                  backTex = data[12];
                  leftTex = data[13];
                  rightTex = data[14];

                  usesTextures = true;
                } 
                catch (Exception e) { //If data doesn't exist
                  usesTextures = false;
                } 
                catch (Throwable t) { //Protect against IO error?
                  usesTextures = false;
                }

                if (usesTextures) {
                  grounds.add(new gameGround(xPos, yPos, zPos, Width, Height, Depth, Type, Move, topTex, bottomTex, frontTex, backTex, leftTex, rightTex)); //Add the ground object to its array
                } 
                else {
                  grounds.add(new gameGround(xPos, yPos, zPos, Width, Height, Depth, Type, Move)); //Add the ground object to its array
                }
              }

              if (data[0].indexOf("player") != -1 & playerFromFile == false) { //If the level object is player, and we do not already have a player
                float xPos = Float.parseFloat(data[1]); //Load x position
                float yPos = Float.parseFloat(data[2]); //Load y position
                float zPos = Float.parseFloat(data[3]); //Load z position
                float Width = Float.parseFloat(data[4]); //Load width
                float Height = Float.parseFloat(data[5]); //Load height
                float Depth = Float.parseFloat(data[6]); //Load depth

                p1 = new Player(xPos, yPos, zPos, Width, Height, Depth); //Set up player

                playerFromFile = true; //We created a player
              }

              if (data[0].indexOf("backcolor") != -1 & backgroundFromFile == false) { // If the background color is set for the first time
                float r = Float.parseFloat(data[1]); //Get R color
                float g = Float.parseFloat(data[2]); //Get G color
                float b = Float.parseFloat(data[3]); //Get B color

                backR = r; //Set the global background R value
                backG = g; //Set the global background G value
                backB = b; //Set the global background B value

                backgroundFromFile = true; //We've loaded the background
              }

              if (data[0].indexOf("deathbox") != -1) { //If we're creating a deathbox, used for world boundaries
                float minX = Float.parseFloat(data[1]); //Load x of top left corner
                float minY = Float.parseFloat(data[2]); //Load y of top left corner
                float maxX = Float.parseFloat(data[3]); //Load x of bottom right corner
                float maxY = Float.parseFloat(data[4]); //Load y of bottom right corner
                float zPos = Float.parseFloat(data[5]); //Load z position of deathbox
                float Depth = Float.parseFloat(data[6]); //Load depth of deathbox

                deathboxes.add(new deathBox(minX, minY, maxX, maxY, zPos, Depth)); //Add to deathboxes array
              }

              if (data[0].indexOf("winblock") != -1 & winBlockFromFile == false) { //If we're creating a win block, used for going to the next level
                float xPos = Float.parseFloat(data[1]); //Load x position
                float yPos = Float.parseFloat(data[2]); //Load y position
                float zPos = Float.parseFloat(data[3]); //Load z position
                float Width = Float.parseFloat(data[4]); //Load width of object
                float Height = Float.parseFloat(data[5]); //Load height of object
                float Depth = Float.parseFloat(data[6]); //Load depth of object

                grounds.add(new gameGround(xPos, yPos, zPos, Width, Height, Depth, 9, 0)); //Add the winBlock object to ground array

                winBlockFromFile = true; //We've loaded a win block
              }

              if (data[0].indexOf("deathplane") != -1) { //If we're creating a deathbox, used for world boundaries
                float y = Float.parseFloat(data[1]); //Load y of deathplane

                deathPlaneY = y; //Save value

                deathPlaneFromFile = true; //We loaded custom deathplaney
              }
              if (data[0].indexOf("viewdist") != -1) { //If we're creating a deathbox, used for world boundaries
                float v = Float.parseFloat(data[1]); //Load y of deathplane

                viewDistance = v; //Save value

                viewDistFromFile = true; //We loaded custom deathplaney
              }
              if (data[0].indexOf("infinitebots") != -1) { //If making bad guys
                float doFloat = Float.parseFloat(data[1]); //Load yes/no value

                if (doFloat == 1) { //Make bad guys forever
                  makingBad = true;
                } 
                else { //Otherwise don't make bad guys forever
                  makingBad = false;
                }
              }

              if (data[0].indexOf("skybox") != -1) { //If making bad guys
                float doFloat = Float.parseFloat(data[1]); //Load yes/no value

                if (doFloat == 1) { //Make bad guys forever
                  skyBox = true;
                } 
                else { //Otherwise don't make bad guys forever
                  skyBox = false;
                }
              }

              if (data[0].indexOf("maxbots") != -1) { //If making bad guys
                float doFloat = Float.parseFloat(data[1]); //Load number max

                maxBad = doFloat;
              }

              if (data[0].indexOf("spawnbot") != -1) { //Make a bot in the map
                float xPos = Float.parseFloat(data[1]); //Load x of bad
                float yPos = Float.parseFloat(data[2]); //Load y of bad
                float zPos = Float.parseFloat(data[3]); //Load z of bad
                float Width = Float.parseFloat(data[4]); //Load width of bad
                float Height = Float.parseFloat(data[5]); //Load height of bad
                float Depth = Float.parseFloat(data[6]); //Load depth of bad

                enemies.add(new enemy(xPos, yPos, zPos, Width, Height, Depth)); //Add enemy to map
              }
              if (data[0].indexOf("missiongoal") != -1) { //Make a bot in the map
                float xPos = Float.parseFloat(data[1]); //Load x of bad
                float yPos = Float.parseFloat(data[2]); //Load y of bad
                float zPos = Float.parseFloat(data[3]); //Load z of bad
                float Width = Float.parseFloat(data[4]); //Load width of bad
                float Height = Float.parseFloat(data[5]); //Load height of bad
                float Depth = Float.parseFloat(data[6]); //Load depth of bad
                float ID = Float.parseFloat(data[7]); //Load ID

                String Descript;
                String Response;
                float gType = 0;

                try {//Error-proofing for different format files
                  Descript = data[8];
                  Response = data[9];
                  gType = Float.parseFloat(data[10]);
                } 
                catch (Exception e) { //If data doesn't exist
                  Descript = "random";
                  Response = "random";
                } 
                catch (Throwable t) { //Protect against IO error?
                  Descript = "random";
                  Response = "random";
                }



                missions.add(new MissionGoal(xPos, yPos, zPos + (Depth / 2), Width, Height, Depth, ID, Descript, Response, gType)); //Add mission to map
              }

              if (data[0].indexOf("sanitylapse") != -1) { //For levels with lots of missions
                float mSL = Float.parseFloat(data[1]);

                mSanityLimit = PApplet.parseInt(mSL);
              }

              if (data[0].indexOf("nummissions") != -1) { //For levels with lots of missions
                float mLM = Float.parseFloat(data[1]);

                mLastMission = PApplet.parseInt(mLM);
              }
            }

            levelFromFile = true; //We have loaded the file successfully
          }
        }
      } 
      catch (Exception e) {
        //An error has occured, print the stack trace to console for debug and set file booleans to false.
        e.printStackTrace();
        levelFromFile = false;
        playerFromFile = false;
        backgroundFromFile = false;
        winBlockFromFile = false;
        deathPlaneFromFile = false;
        viewDistFromFile = false;

        grounds.clear();
        deathboxes.clear();
        bullets.clear();

        enemies.clear();
        missions.clear();
        thoughts.clear();

        deathboxes.add(new deathBox(-100, 800, 5000, 2000, 0, 40)); //Create a default deathbox
      }


      if (levelFromFile == false) {
        //Test level data if no file is loaded
        grounds.add(new gameGround(10, 300, 0, 128, 32, 40, 1, 0));
        grounds.add(new gameGround(150, 225, 0, 128, 32, 40, 2, 100));
        grounds.add(new gameGround(300, 275, 0, 128, 32, 40, 1, 0));
        grounds.add(new gameGround(450, 350, 0, 128, 32, 40, 1, 0));
        grounds.add(new gameGround(600, 450, 0, 128, 32, 40, 1, 0));
        grounds.add(new gameGround(750, 525, 0, 128, 32, 40, 1, 0));

        makingBad = true;
        maxBad = 5.0f;
      }

      if (playerFromFile == false) {
        //Default player start if no data loaded
        p1 = new Player(32, 32, 0, 48, 48, 24);
      }

      if (backgroundFromFile == false) {
        //Default background color (white) if not loaded
        backR = 255;
        backG = 255;
        backB = 255;
      }

      if (winBlockFromFile == false) {
        //Default win block placement

        grounds.add(new gameGround(100, 600, 0, 256, 32, 40, 9, 0)); //Add the ground object to its array
      }

      if (deathPlaneFromFile == false) {
        deathPlaneY = 2000; //Set default value
      }

      if (viewDistFromFile == false) {
        viewDistance = 2000; //Set default value
      }

      //Finally initialize the missions

      for (int i = missions.size () - 1; i >= 0; i--) { //For each mission
        MissionGoal mG = missions.get(i);

        if (mG.getID() > 0 && mG.getInitialized() == 0) mG.initializeMission();
      }


      inGame = true; //Start the game
    }

    if (pauseGame == false & talkScreen == false) { //If the game isn't paused
      if (pauseResetCam == false) { //If it wasn't paused then do normal update
        
        mouseRatioX = (MouseInfo.getPointerInfo().getLocation().x - frameCenterX);
        mouseRatioY = (MouseInfo.getPointerInfo().getLocation().y - frameCenterY);
        
      } 
      else { //If it was paused, reposition the camera view
        if (chkDist(mouseX, mouseY, width / 2, height / 2) <= 30) { //Reset the variable after position reset
          pauseResetCam = false;
        }

        robby.mouseMove(frameCenterX, frameCenterY); //Position mouse to center of screen

        //Reset the values
        mouseRatioX = 0;
        mouseRatioY = 0;
      }

      pushMatrix(); //Store fresh matrix

        gameTicks = gameTicks + 0.1f;  //Used for timing (sin and similar functions)

      //Redraw background if you aren't too far gone
      if (mNum < mSanityLimit + 1) {
        background(backR, backG, backB);

        if (skyBox == true) {
          drawSkyBox(-2900, -1600, -2900, 0, p1.getRotY() * -1, 0, 4400, 3300, 4400, skyFront, skyBack, skyBottom, skyTop, skyRight, skyLeft);
          
        }
      }


      updateFPSCamera(); //Prepare the camera to follow the player, in MiscClasses

      //Check if we can spawn more enemies
      if (Math.round(Math.random() * 100) == 50.0f && enemies.size() - 1 < maxBad & makingBad == true) { //enemies.size() - 1 < 1) {
        enemies.add(new enemy(p1.getX() + random(-500.0f, 500.0f), p1.getY() - 320, p1.getZ() + random(-500.0f, 500), 48, 32, 20));
      }

      //Next draw level and check if player is on any grounds
      boolean didTouch = false; //Boolean to see if player is touching any ground objects

      for (int i = grounds.size () - 1; i >= 0; i--) { //Loop through each ground object
        gameGround ground = grounds.get(i); //Store it into memory

        if (chk3Dist(p1.getX(), p1.getY(), p1.getZ(), ground.getX(), ground.getY(), ground.getZ()) <= ground.getWidth() + viewDistance) { //If the ground object is close enough that player should see it (possible speed boost on big levels?)

          if (isInBox(camLastX, camLastY, camLastZ, ground.getX(), ground.getY(), ground.getZ(), ground.getWidth(), ground.getHeight(), ground.getDepth()) == false) { //If camera is not within the ground object
            ground.render(); //Draw the ground
          }

          for (int n = bullets.size () - 1; n >= 0; n--) { //For each bullet 
            bullet bull = bullets.get(n); //Get the object

            if (isInBox(bull.getX(), bull.getY(), bull.getZ(), ground.getX(), ground.getY(), ground.getZ(), ground.getWidth(), ground.getHeight(), ground.getDepth())) { //If the bullet is inside the ground object
              bullets.remove(n); //Destroy it
            }
          }
        }

        //The win block is a region that does not render, the player can enter it to progress to the next level, or otherwise end the game
        //This can be used as a method to increase the size of a level to a large amount
        if (ground.getType() == 9 && isInBox(p1.getX(), p1.getY(), p1.getZ(), ground.getX(), ground.getY(), ground.getZ(), ground.getWidth(), ground.getHeight(), ground.getDepth())) { //If we are touching the win block

          if (currLevel < levels.size() - 1) { //If there is another level
            currLevel = currLevel + 1; //Go to it
          } 
          else {
            wonGame = true; //Congratulations, you've won!
            programStage = 2; //Go to game over screen
          }
          inGame = false; //End of the level
          break; //Exit for
        }

        //Now, calculate if the player is standing on the ground
        //if (isOnRect(p1.getX(), p1.getY(), p1.getWidth() / 2, p1.getHeight(), ground.getX(), ground.getY(), ground.getWidth()) == 1) {
        if (isOnBox(p1.getX(), p1.getY(), p1.getZ(), p1.getWidth() / 2, p1.getHeight(), p1.getDepth() / 2, ground.getX(), ground.getY(), ground.getZ(), ground.getWidth(), ground.getDepth()) == 1) {
          didTouch = true; //Yes, player on ground

          if (p1.isOnGround() == false) { //Touching this block first time
            if (ground.getType() == 5 && ground.getState() <= 0) { //If the ground is of type 5 and not yet falling
              ground.setState(1); //Start fall sequence
            }
          }
        }

        //Extra check, if a falling platform passes the deathplane, reset it, to allow revisiting of an area
        if (ground.getType() == 5 && ground.getY() > deathPlaneY + 200) {
          ground.resetPosition();
        }

        //Otherwise, if not on the top of the rectangle as calculated above:
        if (whereInBox(p1.getX(), p1.getY() + (p1.getHeight() / 2), p1.getZ(), p1.getWidth(), p1.getHeight(), p1.getDepth() / 2, ground.getX(), ground.getY(), ground.getZ(), ground.getWidth(), ground.getHeight() / 3, ground.getDepth(), 1) == 1) {
          //On top half of ground
          didTouch = true; //Still touching
          p1.setY(ground.getY() - (p1.getHeight() / 2)); //Align with top of block, used to stay on a block when it is moving
        } 
        else if (p1.isJumping() == false) { //Otherwise, if the player isn't jumping and seems to be in the bottom half

          float chkLeftRight = whereInBox(p1.getX(), p1.getY(), p1.getZ(), p1.getWidth(), p1.getHeight(), p1.getDepth() / 2, ground.getX(), ground.getY(), ground.getZ(), ground.getWidth(), ground.getHeight() / 3, ground.getDepth(), 2);

          if (chkLeftRight == 1) {
            //Left side of block
            p1.moveX(-1 * p1.getSpeed()); // Move to the left side of the block
          } 
          else if (chkLeftRight == 2) {
            //Right side of block
            p1.moveX(p1.getSpeed()); //Move to ther right side of the block
          }

          float chkFrontBack = whereInBox(p1.getX(), p1.getY(), p1.getZ(), p1.getWidth(), p1.getHeight(), p1.getDepth(), ground.getX(), ground.getY(), ground.getZ(), ground.getWidth(), ground.getHeight() / 3, ground.getDepth(), 3);

          if (chkFrontBack == 1) {
            p1.moveZ(-1 * p1.getSpeed()); //Push them away from front of block
          } 
          else if (chkFrontBack == 2) {
            p1.moveZ(p1.getSpeed()); //Push them away from back of block
          }
        } 
        else if (p1.getY() > ground.getY()) { //Even if the player is jumping, they should no longer be able to jump through stuff
          float chkLeftRight = whereInBox(p1.getX(), p1.getY(), p1.getZ(), p1.getWidth(), p1.getHeight(), p1.getDepth() / 2, ground.getX(), ground.getY(), ground.getZ(), ground.getWidth(), ground.getHeight() / 3, ground.getDepth(), 2);

          if (chkLeftRight == 1) {
            //Left side of block
            p1.moveX(-1 * p1.getSpeed()); // Move to the left side of the block
          } 
          else if (chkLeftRight == 2) {
            //Right side of block
            p1.moveX(p1.getSpeed()); //Move to ther right side of the block
          }

          float chkFrontBack = whereInBox(p1.getX(), p1.getY(), p1.getZ(), p1.getWidth(), p1.getHeight(), p1.getDepth(), ground.getX(), ground.getY(), ground.getZ(), ground.getWidth(), ground.getHeight() / 3, ground.getDepth(), 3);

          if (chkFrontBack == 1) {
            p1.moveZ(-1 * p1.getSpeed()); //Push them away from front of block
          } 
          else if (chkFrontBack == 2) {
            p1.moveZ(p1.getSpeed()); //Push them away from back of block
          }
        }

        //Check if the camera is inside a block and push player backward when not jumping ONLY NEEDED FOR FPS!
        if (p1.isJumping() == false) { 
          if (isInBox(p1.getX() + sin(camRotOff) * 3 * p1.getDepth(), p1.getY(), p1.getZ() + cos(camRotOff) * 3 * p1.getDepth(), ground.getX(), ground.getY() - 2.5f, ground.getZ(), ground.getWidth(), ground.getHeight() + 5, ground.getDepth())) { //If camera is within an object at offset
            p1.moveX(-1 * p1.getSpeed() * cos(p1.getRotY() - radians(90))); //Move X component BACKWARD
            p1.moveZ(1 * p1.getSpeed() * sin(p1.getRotY()- radians(90))); //Move Z component BACKWARD
          }
        } 
        else if (p1.getY() > ground.getY()) { //Even if you're jumping, you shouldn't always be able to go through stuff
          if (isInBox(p1.getX() + sin(camRotOff) * 3 * p1.getDepth(), p1.getY(), p1.getZ() + cos(camRotOff) * 3 * p1.getDepth(), ground.getX(), ground.getY() - 2.5f, ground.getZ(), ground.getWidth(), ground.getHeight() + 5, ground.getDepth())) { //If camera is within an object at offset
            p1.moveX(-1 * p1.getSpeed() * cos(p1.getRotY() - radians(90))); //Move X component BACKWARD
            p1.moveZ(1 * p1.getSpeed() * sin(p1.getRotY()- radians(90))); //Move Z component BACKWARD
          }
        }

        //Extra checks for block type 7, AKA solid invisible wall
        if (ground.getType() == 7 & p1.getY() > ground.getY() + 5 & p1.getY() < ground.getY() + ground.getHeight()) { //In the height zone
          if (isInRect(p1.getX(), p1.getZ(), ground.getX(), ground.getZ(), ground.getWidth(), ground.getDepth())) { //Only need to check 2D now

            if (dist(p1.getX(), 0, ground.getX() + (ground.getWidth() / 2), 0) > 5) { //If not close to center on X axis
              if (dist(p1.getX(), 0, ground.getX(), 0) <= dist(p1.getX(), 0, ground.getX() + ground.getWidth(), 0)) {
                //Closer to the left side
                p1.moveX(-2 * p1.getSpeed());
              } 
              else {
                //Closer to the right side
                p1.moveX(2 * p1.getSpeed());
              }
            }

            if (dist(p1.getZ(), 0, ground.getZ() + (ground.getDepth() / 2), 0) > 5) { //If not close to center on Z axis
              if (dist(p1.getZ(), 0, ground.getZ(), 0) <= dist(p1.getZ(), 0, ground.getZ() + ground.getDepth(), 0)) {
                //Closer to the back side
                p1.moveZ(-2 * p1.getSpeed());
              } 
              else {
                //Closer to the front side
                p1.moveZ(2 * p1.getSpeed());
              }
            }
          }
        }

        //Now check for the Enemies if standing on the ground
        for (int n = enemies.size () - 1; n >= 0; n--) { //For each enemy 
          enemy bad = enemies.get(n); //Get the object

          bad.setDidTouch(false); //Reset the did touch variable

            if (isOnBox(bad.getX(), bad.getY(), bad.getZ(), bad.getWidth() / 2, bad.getHeight(), bad.getDepth() / 2, ground.getX(), ground.getY(), ground.getZ(), ground.getWidth(), ground.getDepth()) == 1) {
            bad.setDidTouch(true); //Yes, enemy on ground
          }

          //Otherwise, if not on the top of the rectangle as calculated above:
          if (whereInBox(bad.getX(), bad.getY() + (bad.getHeight() / 2), bad.getZ(), bad.getWidth(), bad.getHeight(), bad.getDepth() / 2, ground.getX(), ground.getY(), ground.getZ(), ground.getWidth(), ground.getHeight() / 3, ground.getDepth(), 1) == 1) {
            //On top half of ground
            bad.setDidTouch(true); //Still touching
            bad.setY(ground.getY() - (bad.getHeight() / 2)); //Align with top of block, used to stay on a block when it is moving
          } 
          else if (bad.isJumping() == false) { //Otherwise, if the player isn't jumping and seems to be in the bottom half
            float chkLeftRight = whereInBox(bad.getX(), bad.getY(), bad.getZ(), bad.getWidth(), bad.getHeight(), bad.getDepth(), ground.getX(), ground.getY(), ground.getZ(), ground.getWidth(), ground.getHeight() / 3, ground.getDepth(), 2);

            if (chkLeftRight == 1) {
              //Left side of block
              bad.moveX(-1 * bad.getSpeed()); // Move to the left side of the block
            } 
            else if (chkLeftRight == 2) {
              //Right side of block
              bad.moveX(bad.getSpeed()); //Move to ther right side of the block
            }

            float chkFrontBack = whereInBox(bad.getX(), bad.getY(), bad.getZ(), bad.getWidth(), bad.getHeight(), bad.getDepth(), ground.getX(), ground.getY(), ground.getZ(), ground.getWidth(), ground.getHeight() / 3, ground.getDepth(), 3);

            if (chkFrontBack == 1) {
              bad.moveZ(-1 * bad.getSpeed()); //Push them away from front of block
            } 
            else if (chkFrontBack == 2) {
              bad.moveZ(bad.getSpeed()); //Push them away from back of block
            }
          }
        } //End of enemy stuff with ground
      }

      if (p1.isJumping() == false) { //If the player isn't jumping
        if (didTouch) { //And they did touch the ground
          p1.touchGround(); //They are not falling
        } 
        else { //Otherwise if the player did not touch the ground
          p1.leaveGround(); //They are falling
        }
      }

      //Now check for the Enemies if standing on the ground
      for (int i = enemies.size () - 1; i >= 0; i--) { //For each enemy 
        enemy bad = enemies.get(i); //Get the object

        if (bad.isJumping() == false) { //If the enemy isn't jumping
          if (bad.getDidTouch() == true) { //And they did touch the ground
            bad.touchGround(); //They are not falling
          } 
          else { //Otherwise if the enemy did not touch the ground
            bad.leaveGround(); //They are falling
          }
        }

        if (chk3Dist(p1.getX(), p1.getY(), p1.getZ(), bad.getX(), bad.getY(), bad.getZ()) <= bad.getWidth() + viewDistance) { //If the enemy object is close enough that player should see it (possible speed boost on big levels)
          bad.render(); //Draw the enemy

          for (int n = bullets.size () - 1; n >= 0; n--) { //For each bullet 
            bullet bull = bullets.get(n); //Get the object

            if (isInBox(bull.getX(), bull.getY(), bull.getZ(), bad.getX() - (bad.getWidth() / 2), bad.getY() - (bad.getHeight() / 2), bad.getZ() - (bad.getDepth() / 2), bad.getWidth(), bad.getHeight(), bad.getDepth())) { //If the bullet is inside the enemy object
              enemies.remove(i); //Destroy the enemy        
              bullets.remove(n); //Destroy the bullet

              p1Score = p1Score + 1; //Player gets a score
            }
          }

          if (isInBox(p1.getX(), p1.getY(), p1.getZ(), bad.getX() - (bad.getWidth() / 2), bad.getY() - (bad.getHeight() / 2), bad.getZ() - (bad.getDepth() / 2), bad.getWidth(), bad.getHeight(), bad.getDepth())) { //If the player is overlapping the bad guy
            p1Lives = p1Lives - 1; //Lose a life
            inGame = false; //End of game
            break; //Exit for
          }
        }
        if (chk3Dist(p1.getX(), 0, p1.getZ(), bad.getX(), 0, bad.getZ()) <= bad.getWidth() + bad.getDepth() + 250 & chkDist(0, p1.getY(), 0, bad.getY()) <= 100) { //If close to player, move towards them
          if (p1.getX() < bad.getX()) {
            bad.moveX(-1 * bad.getSpeed()); //move left
          }

          if (p1.getX() > bad.getX()) {
            bad.moveX(1 * bad.getSpeed()); //move right
          }

          if (p1.getZ() < bad.getZ()) {
            bad.moveZ(-1 * bad.getSpeed()); //move back
          }

          if (p1.getZ() > bad.getZ()) {
            bad.moveZ(1 * bad.getSpeed()); //move forward
          }

          if (p1.getY() < bad.getY() - bad.getHeight()) { //If the player is above us
            bad.Jump(5); //Jump - (Will only work if on ground)
          }

          bad.setDir(atan2(p1.getX() - bad.getX(), p1.getZ() - bad.getZ())); //Set angle towards player on a "2D" plane of the ground
        }
      }

      //Now check what keys are being pressed
      if (keysDown[0] == true) {
        p1.moveX(-1 * p1.getSpeed() * cos(p1.getRotY() + radians(90))); //Move X component FORWARD
        p1.moveZ(1 * p1.getSpeed() * sin(p1.getRotY() + radians(90))); //Move Z component FORWARD
      }

      if (keysDown[1] == true) {
        p1.moveX(-1 * p1.getSpeed() * cos(p1.getRotY() - radians(90))); //Move X component BACKWARD
        p1.moveZ(1 * p1.getSpeed() * sin(p1.getRotY()- radians(90))); //Move Z component BACKWARD
      }

      if (keysDown[2] == true) {
        p1.moveX(1 * p1.getSpeed() * cos(p1.getRotY())); //Strafe X component LEFT
        p1.moveZ(-1 * p1.getSpeed() * sin(p1.getRotY())); //Strafe Z component LEFT
      }

      if (keysDown[3] == true) {
        p1.moveX(-1 * p1.getSpeed() * cos(p1.getRotY())); //Strafe X component RIGHT
        p1.moveZ(1 * p1.getSpeed() * sin(p1.getRotY())); //Strafe Z component RIGHT
      } 

      if (keysDown[6] == true) {
        p1.spinY(5); //Turn left
      }

      if (keysDown[7] == true) {
        p1.spinY(-5); //Turn right
      }

      //Check if player is jumping
      if (keysDown[4] == true) {
        p1.Jump(5); //If the space key is down, jump
      } 

      //Check if player is shooting
      if (keysDown[8] == true && (enemies.size() > 0)) { //If there's no enemies in the game, then we don't allow player to shoot
        if (p1.getCoolDown() <= 0) { //If the player hasn't shot a bullet yet

          bullets.add(new bullet(p1.getX(), p1.getY(), p1.getZ(), p1.getRotY(), radians(camLookOff * 45), p1.getID())); //Shoot a bullet for the player

          p1.setCoolDown(2); //Set the player cool down for attacking
        }
      } 

      //Check if player pressed quit
      if (keysDown[5] == true) {
        exit(); //Quit game
      }

      //Draw the bullets
      for (int i = bullets.size () - 1; i >= 0; i--) { //For each bullet 
        bullet bull = bullets.get(i); //Get the object

        if (abs(bull.flyDist) >= bull.maxDist) { //If the bullet is past the distance limit
          bullets.remove(i); //Destroy it
        } 
        else {
          bull.render(); //Otherwise draw it
        }
      }

      //Draw the player
      p1.render(); //In FPS you can't see yourself, but there is other code that runs here

      //Check mission goals
      for (int i = missions.size () - 1; i >= 0; i--) { //For each mission
        MissionGoal mG = missions.get(i);

        if (mG.getID() == mNum) { //The current mission
          mGoal = mG.getDescription();

          if (mG.getID() < mLastMission) {
            mG.render(); //Draw it

            //Do checks if within it
            if (isInBox(p1.getX(), p1.getY(), p1.getZ(), mG.getX() - (mG.getWidth() / 2), mG.getY() - (mG.getHeight() / 2), mG.getZ() - (mG.getDepth() / 2), mG.getWidth(), mG.getHeight(), mG.getDepth())) { //If the player is overlapping the mission box
              missionComplete = true;
              talkScreen = true; //Mission completed
              talkTimer = 30; //Reset timer
            }
          }

          break; //Exit for
        }
      }

      //Check for gameover
      for (int i = deathboxes.size () - 1; i >= 0; i--) { //For each deatbox object
        deathBox dBox = deathboxes.get(i); //Get the object

        dBox.render(); //Render at the player offset

        if (dBox.inDeathBox(p1.getX(), p1.getY(), p1.getZ())) { //If the player is in the deathbox
          p1Lives = p1Lives - 1; //Lose a life
          inGame = false; //End of game, restart
          break; //Exit for
        }

        for (int n = enemies.size () - 1; n >= 0; n--) { //For each enemy 
          enemy bad = enemies.get(n); //Get the object

          if (dBox.inDeathBox(bad.getX(), bad.getY(), bad.getZ())) { //Destroy enemy if it's inside deathbox
            enemies.remove(n);
          }
        }
      }

      if (p1.getY() >= deathPlaneY) { //Max level size/floor
        p1Lives = p1Lives - 1; //Lose a life
        inGame = false; //End of game, restart
      }

      //Now draw the Heads Up Display (HUD)

      popMatrix(); //Return to fresh matrix

        if (mNum >= mSanityLimit) { //Nearing the end

        if (frameCount % 2 == 1) { //Every 2 frames
          gameTrack.skip(PApplet.parseInt(random(2, 5)) * (mNum - mSanityLimit)); //Start to distort audio
        }

        if (mNum >= mLastMission) { //Flip controls when it's near the end

          if (flipControls == false) {

            for (int i = 0; i < 9; i++) { //Quick refresh to stop bunnyhop/infinite shooting
              keysDown[i] = false;
            }

            flipControls = true;
          }

          noStroke();
          fill(255, 255, 255, endGameFade); //White transparent
          rect(0, 0, width, height); //Full screen rectangle

          if (endGameFade < 255) {
            endGameFade += 0.1f;
          } 
          else {
            wonGame = true; //Congratulations, you've won!..kinda, but at least the baby lives!!!
            programStage = 2; //Go to game over screen

            inGame = false; //End of the level
          }
        }

        drawInsaneHUD(); //More text
      } 
      else {
        drawHUD(0); //Draw the Heads Up Display with regular text, in MiscClasses
      }
    } 
    else { //Game is paused OR we are talking to someone
      cursor(0); //Switch to a regular cursor

      if (talkScreen) { //If we're in a talk screen
        pauseResetCam = true; //Simulate the pause reset cam value

          //Code goes here to talk to people and get missions!
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        for (int i = missions.size () - 1; i >= 0; i--) { //For each mission
          MissionGoal mG = missions.get(i);

          if (mG.getID() == mNum) { //The current mission
            if (mG.getGoalType() != talkSituation) { //A response scene
              switch (PApplet.parseInt(mG.getGoalType())) {
              case 0:
                discussionImage = loadImage("giver.png");
                break;
              case 1:
                discussionImage = loadImage("person" + PApplet.parseInt(random(1, 4)) + ".png");
                break;
              case 2:
                discussionImage = loadImage("mailbox.png");
                break; 
              case 3:
                discussionImage = loadImage("insane.png");
                break;
              default:
                discussionImage = loadImage("giver.png");
              }

              talkSituation = PApplet.parseInt(mG.getGoalType());
            }

            if (mG.getGoalType() != 3) {
              textFont(mainFont);
              fill(0);
              textSize(24);
            } 
            else {
              textFont(insaneFont);
              fill(0);
              textSize(18);
            }

            if (missionComplete) { //Description of next mission
              if (mG.getGoalType() == 3) {
                textFont(insaneFont);
                fill(255);
                textSize(18);
              }

              image(discussionImage, 0, 0, width, height);
              text(mG.getResponse(), 10, 30);
            } 
            else { //Response
              image(questGiver, 0, 0, width, height);
              text(mG.getDescription(), 10, 30);
            }

            if (talkTimer <= 0) {
              textFont(objectiveFont);
              textSize(32);

              float btnX, btnY, btnWidth, btnHeight;

              btnX = 15;
              btnY = height - 80;
              btnWidth = 150;
              btnHeight = 60;

              stroke(128);

              if (mG.getGoalType() != 3) {
                fill(0);
                rect(btnX, btnY, btnWidth, btnHeight);

                fill(255);
                text("Okay", btnX + 10, btnY + 40); //Render press any key
              } 
              else {
                fill(255);
                rect(btnX, btnY, btnWidth, btnHeight);

                fill(0);
                text("...", btnX + 10, btnY + 40); //Render press any key
              }



              if (mousePressed & isInRect(mouseX, mouseY, btnX, btnY, btnX + btnWidth, btnY + btnHeight)) {
                talkTimer = 25;

                if (missionComplete == false) { //Return to game
                  keysDown[8] = false; //Don't shoot
                  talkScreen = false;
                } 
                else {
                  mNum += 1;
                  missionComplete = false;
                }
              }
            } 
            else {
              talkTimer -= 0.5f;
            }

            break; //Exit for
          }
        }
      } 
      else {
        drawHUD(1); //Draw the Heads Up Display with pause fade + text
      }
    }
  } 
  else if (programStage == 2) { //Game Over

    inGame = false;
    cursor(0); //Switch to a regular cursor

    if (gameTicks > 30) gameTicks = 30; //If gameTicks is greater than our "max" countdown time, limit it  

    if (gameTicks > 0) { //Simple reverse timer
      gameTicks -= 0.1f; //Rewind
    }

    if (titleTicks < 255) { //Simple fade-in
      titleTicks += 1.5f;
    } 
    else {
      titleTicks = 255;
    }

    background(255 - titleTicks, 255 - titleTicks, 255 - titleTicks); //Paint white background that fades to reveal text

    fill(255); //Text fill

    textFont(mainFont);
    textSize(24); //Set text size

    String endText = "";

    if (wonGame == true) {
      if (winTrack.isLooping() == false) {
        gameTrack.rewind();
        gameTrack.pause();
        titleTrack.rewind();
        titleTrack.pause();
        endTrack.rewind();
        endTrack.pause();

        winTrack.rewind();
        winTrack.loop();
        winTrack.setGain(gameVolume);
      } 

      endText += "\"Thank you,\" said the hostess. \"Your city thanks you;\n";
      endText += "your country thanks you; your planet thanks you. But\n";
      endText += "the deepest thanks of all is from future generations.\"\n\n";

      endText += "\"This child of yours\" said Dr. Hitz.\n";
      endText += "\"He or she is going to live on a happy, roomy,\n";
      endText += "clean, rich planet, thanks to population control.\"\n\n";

      text(endText, 10, 40); //Render YOU WON TEXT!
    } 
    else {
      if (endTrack.isLooping() == false) {
        gameTrack.rewind();
        gameTrack.pause();
        titleTrack.rewind();
        titleTrack.pause();
        winTrack.rewind();
        winTrack.pause();

        endTrack.rewind();
        endTrack.loop();
        endTrack.setGain(gameVolume);
      }

      endText += "\"Do the parents have a volunteer?\" said Leora Duncan.\n";
      endText += "\"Last I heard,\" said Dr. Hitz.\n\n";
      endText += "\"I don't think they made it,\" she said.\n";
      endText += "\"Nobody made appointments with us.\"\n\n";

      text(endText, 10, 40); //Render GAME OVER TEXT
      
      endText = ""; //Reset endText for next part
      
      endText += "The radio played a popular song...";
      
      text(endText, 10, 290);

      endText = ""; //Reset endText for next part

      endText += "\"If you don't like my kisses, honey,\n";
      endText += "Here's what I will do:\n";
      endText += "I'll go see a girl in purple,\n";
      endText += "Kiss this sad world toodle-oo.\n";
      endText += "If you don't want my lovin',\n";
      endText += "Why should I take up all this space?\n";
      endText += "I'll get off this old planet,\n";
      endText += "Let some sweet baby have my place.\"";

      textFont(objectiveFont); //Swap font for radio tune
      textSize(18);

      text(endText, 100, 330);
    }

    if (gameTicks <= 0 & titleTicks >= 255) { //Wait a bit

      textFont(objectiveFont);
      textSize(32); //Set text size

      text("Press any key to continue...", 10, height - 40); //Render press any key

      if (keyPressed) { //Press any key to go to credits
        //Clear arrays that are filled at start of main game

        grounds.clear();
        deathboxes.clear();
        enemies.clear();
        bullets.clear();
        missions.clear();
        thoughts.clear();

        wonGame = false; //Reset wonGame
        gameTicks = 0; //Reset ticks
        titleTicks = 0; //Reset title ticks
        programStage = 3; //Credits phase
      }
    }
  } 
  else if (programStage == 3) { //Credits
    if (endTrack.isLooping() == false) {
      gameTrack.rewind();
      gameTrack.pause();
      winTrack.rewind();
      winTrack.pause();

      endTrack.rewind();
      endTrack.loop();
      endTrack.setGain(gameVolume);
    }

    background(0, 0, 0); //Paint white background

    titleTicks += 0.1f; //Credits are timed
    float textScrollRate = 15;

    fill(255); //Text fill

    textFont(objectiveFont);
    textSize(32); //Set text size

    //Render credits
    text("Created by Daven Bigelow #1230379", 10, height - (titleTicks * textScrollRate));
    text("bigelod@mcmaster.ca", 10, height - (titleTicks * textScrollRate) + 40);

    text("Based on 2BR02B by Kurt Vonnegut (1962)", 10, height - (titleTicks * textScrollRate) + 120);
    text("www.gutenberg.org/ebooks/21279", 10, height - (titleTicks * textScrollRate) + 160);

    text("Newgrounds Music:", 10, height - (titleTicks * textScrollRate) + 240);
    text("Suicide by Flames-of-Industry", 10, height - (titleTicks * textScrollRate) + 280);
    text("Not Even A Goodbye by DFDCinematic", 10, height - (titleTicks * textScrollRate) + 320);
    text("Dreams by christoko", 10, height - (titleTicks * textScrollRate) + 360);
    text("Moment Of Desparation by Fyresale", 10, height - (titleTicks * textScrollRate) + 400);

    text("Thank you for playing!", 10, height - (titleTicks * textScrollRate) + 480);

    if (titleTicks >= 80) { //After a certain amount of time
      titleTicks = 0;
      titleTextStage = 0;
      programStage = 0; //Back to title
    }
  } 
  else { //Error, go to Title
    programStage = 0;
  }
}
//---------------------------------------------------------------------------------------------------------------
//END OF MAIN ENGINE LOOP
//---------------------------------------------------------------------------------------------------------------

//---------------------------------------------------------------------------------------------------------------
//END OF ENGINE
//(CLASSES AND FUNCTIONS ARE NOW SEPARATE TABS)
//---------------------------------------------------------------------------------------------------------------
class bullet { //Class for bullets
  float xPos = 0; //Local X position
  float yPos = 0; //Local Y position
  float zPos = 0; //Local Z position
  float Width = 5; //Local width value
  float Height = 5; //Local height value
  float Depth = 5; //Local depth value  
  float dir = 1; //Direction of travel (Y axis angle)
  float angle = 0; //Direction of travel (X axis angle)
  float speed = 10; //Speed of movement
  float owner = 0; //Owner of object
  float flyDist = 0; //Distance travelled
  float maxDist = 800; //Max bullet distance

  bullet ( float x, float y, float z, float d, float a, float o) { //Set up bullet object
    xPos = x; //X
    yPos = y; //Y
    zPos = z; //Z
    angle = a; //Angle
    dir = d; //Dir
    owner = o; //Owner
  }

  public void render() { //Draw the bullet
    xPos = xPos + sin(dir) * speed; //Update X position
    zPos = zPos + cos(dir) * speed; //Update Z position
    yPos = yPos + sin(angle) * speed; //Update Y position

    flyDist = flyDist + speed; //Update distance travelled

    strokeWeight(1); //Thin line

    fill(232, 215, 0); //Gold color
    
    pushMatrix(); //Store previous matrix

    translate(xPos + (Width / 2), yPos + (Height / 2), zPos + (Depth / 2)); //Apply translation
    rotateY(dir); //Apply rotation
    
    box(Width, Height, Depth); //Draw bullet box

    popMatrix(); //Return matrix to stage before transformation

  }

  public void setX(float x) { //Set X position
    xPos = x;
  }

  public void setY(float y) { //Set Y position
    yPos = y;
  }

  public void setDir(float d) { //Set Direction
    dir = d;
  }

  public void setSpeed(float s) { //Set Speed
    speed = s;
  }

  public void setOwner(float o) { //Set Owner
    owner = o;
  }

  public void setMaxDist(float d) { //Set max distance
    maxDist = d;
  }

  public float getX() { //Get X position
    return xPos;
  }

  public float getY() { //Get Y position
    return yPos;
  }
  
  public float getZ() { //Get Z position
    return zPos;
  }

  public float getDir() { //Get Direction
    return dir;
  }

  public float getSpeed() { //Get Speed
    return speed;
  }

  public float getOwner() { //Get Owner
    return owner;
  }

  public float getDist() { //Travel distance
    return flyDist;
  }

  public float getMaxDist() { //Max travel distance
    return maxDist;
  }
}
class CrazyText { //Class for bullets
  float xPos = 0; //Local X position
  float yPos = 0; //Local Y position
  String strText = "";
  float tSize = 18;
  float fillAmnt = 0;
  float alpha = 0;

  CrazyText ( float x, float y, String t, float tS, float fA, float a) { //Set up bullet object
    xPos = x; //X
    yPos = y; //Y
    strText = t;
    tSize = tS;
    fillAmnt = fA;
    alpha = a;
  }

  public void render() { //Draw the bullet

    textFont(insaneFont);
    
    textSize(tSize);
    
    fill(fillAmnt, alpha);
    
    text(strText, xPos, yPos);
    
    alpha -= 20;

  }

  public float getAge() { //Get alpha
    return 255 - alpha;
  }

}
class deathBox {
  //Kills player
  float minX = 0; //Top-left x
  float minY = 0; //Top-left y
  float maxX = 0; //Bottom-right x
  float maxY = 0; //Bottom-right y
  float Depth = 40; //Local depth
  float zPos = 0; //Z position
  float rotX = 0;
  float rotY = 0;
  float rotZ = 0;

  deathBox (float x1, float y1, float x2, float y2, float z, float d) { //Setup the object
    minX = x1; //X1
    minY = y1; //Y1
    maxX = x2; //X2
    maxY = y2; //Y2
    zPos = z; //z
    Depth = d; //depth
  }

  public boolean inDeathBox(float x, float y, float z) { //If something is in the deathbox
    if (x >= minX & y >= minY & x <= maxX & y <= maxY & z >= zPos - (Depth / 2) & z <= zPos + (Depth / 2)) {
      return true; //Yes, that point is within the box
    } 
    else {
      return false; //No, that point is not within the box
    }
  }

  public void render() { //Draw the deathbox
    stroke(100);
    fill(50, 50, 50); //The box is black
    
    pushMatrix(); //Save current matrix

    translate((minX + maxX) / 2, (minY + maxY) / 2, zPos); //Apply translation
    rotateX(rotX); //Remember, this is a visual rotation, and does not affect the math itself!
    rotateY(rotY); //Remember, this is a visual rotation, and does not affect the math itself!
    rotateZ(rotZ); //Remember, this is a visual rotation, and does not affect the math itself!

    box(maxX - minX, maxY - minY, Depth); //Draw box

    popMatrix(); //Return matrix
    
  }
}
class enemy {
  //Enemy object class
  float xPos = 32; //Local X position value
  float startX = 32; //Local start x position in the level
  float yPos = 32; //Local y position value
  float startY = 32; //Local start Y position
  float zPos = 0; //Local z position value
  float startZ = 0; //Local start z position
  float Width = 48; //Local width value
  float Height = 48; //Local height value
  float Depth = 0; //Local depth value
  float enemySpeed = 0.8f; //Local enemySpeed value
  float jumpTime = -1; //Local jump time, used to calculate jumping time left
  boolean onGround = false; //If the enemy is on the ground
  float enemyGravStr = 2; //Enemy gravity strength
  float enemyJumpStr = 2; //Enemy jump strength
  float direction = 0; //Enemy direction
  boolean didTouch = false; //If the enemy is touching ground

  enemy (float x, float y, float z, float w, float h, float d) { //Set up the enemy object with x, y, width, height
    xPos = x; //X
    startX = x; //X
    yPos = y; //Y
    startY = y; //Y
    zPos = z; //Z
    startZ = z; //Z
    Width = w; //Width
    Height = h; //Height
    Depth = d; //Depth
  }

  public void render() { //Draw the enemy object

    stroke(0); //Enemy has black outline
    strokeWeight(1); //Enemy outline is weighted 1

      if (jumpTime > 0) { //If the player is jumping
      yPos = yPos - enemyJumpStr; //Move up at jump strength

      jumpTime = jumpTime - 0.1f; //Decrease jump time
    } 
    else { //Otherwise if they should be falling
      jumpTime = -1; //Make sure jump time is below 0

      if (onGround == false) { //If they are not touching the ground
        yPos = yPos + enemyGravStr; //Enemy falls at grav strength
      }
    }

    fill(255, 0, 255); //The enemy is pink
    
    pushMatrix(); //Store previous matrix

    translate(xPos + (Width / 2), yPos, zPos + (Depth / 2)); //Apply translation
    rotateY(direction + (PI / 2)); //Apply rotation
    
    box(Width, Height, Depth);

    popMatrix(); //Return matrix to stage before transformation
    
  }

  public void moveX(float xOffset) { //Move the player by offset amount in x axis
    xPos = xPos + xOffset;
  }

  public void moveY(float yOffset) { //Move player by offset amount in y axis
    yPos = yPos + yOffset;
  }
  
  public void moveZ(float zOffset) { //Move player by offset amount in z axis
    zPos = zPos + zOffset;
  }

  public void setX(float x) { //Set the player x position exactly to a value
    xPos = x;
  }

  public void setY(float y) { //Set the player y position exactly to a value
    yPos = y;
  }

  public void setZ(float z) { //Set the player z position exactly to a value
    zPos = z;
  }

  public void setGravStr(float str) { //Set the player gravity strength
    enemyGravStr = str;
  }

  public void setJumpStr(float str) { //Set the player jump strength
    enemyJumpStr = str;
  }

  public float getSpeed() { //Return the player speed
    return enemySpeed;
  }

  public void setSpeed(float newSpeed) { //Set the player speed
    enemySpeed = newSpeed;
  }

  public void touchGround() { //Called to set onGround to true
    onGround = true;
  }

  public void leaveGround() { //Called to set onGround to false
    onGround = false;
  }

  public float getX() { //Return the current x position
    return xPos;
  }

  public float getY() { //Return the current y position
    return yPos;
  }
  
  public float getZ() { //Return the current z position
    return zPos;
  }

  public float getWidth() { //Return the current width
    return Width;
  }

  public float getHeight() { //Return the current height
    return Height;
  }
  
  public float getDepth() { //Return the current depth
    return Depth;
  }

  public float getGravStr() { //Return the player gravity strength
    return enemyGravStr;
  }

  public float getJumpStr() { //Return the player jump strength
    return enemyJumpStr;
  }

  public boolean isOnGround() { //Return if the player is on ground or not
    return onGround;
  }

  public boolean getDidTouch() { //Return if the player is touching
    return didTouch;
  }

  public float getDir() { //Get players direction
    return direction;
  }

  public void Jump(float time) { //Set the player jump time
    if (onGround & jumpTime <= 0) { //Check if they are on the ground and not jumping
      jumpTime = time;
    }
  }

  public boolean isJumping() { //Return if the player is jumping or not
    if (jumpTime > 0) {
      return true;
    } 
    else {
      return false;
    }
  }

  public void setDir(float d) { //Set direction
    direction = d;
  }

  public void setDidTouch(boolean d) { //Set didTouch
    didTouch = d;
  }
}
//Most of these are no longer needed but are here for reference

public int isOnRect(float x1, float y1, float objWidth, float objHeight, float x2, float y2, float recWidth) { //If an object is on top of a rectangle
  float minX = x2 - (objWidth / 2); //The minimum x position of the block to check
  float maxX = (x2 + recWidth) + (objWidth / 2); //The maximum x position of the block to check
  float objBaseY = y1 + (objHeight / 2); //The object base Y (bottom)

  if (x1 < minX) { //If the object is left of the rectangle
    return 0;
  } 
  else if (x1 > maxX) { //If the object is right of the rectangle
    return 0;
  }

  if (objBaseY <= (y2 + 3) & objBaseY >= (y2 - 3)) { //If the object is on the block
    return 1; //On a block
  }
  return 0; //No collide
}

public int isCollideCircle(float x1, float y1, float rad1, float x2, float y2, float rad2) { //Return an integer to check if two circles collide
  float distance = sqrt(sq((x1 - x2)) + sq(y1 - y2)); //Distance between the two circles

  if (distance < rad1 + rad2) {
    return 1;//Intersection
  } 
  else if (distance == rad1 + rad2) {
    return 2; //Touching only
  }

  return 0; //No collide
}


public float chkDist(float x1, float y1, float x2, float y2) { //Return distance between two points
  float distance = sqrt(sq((x1 - x2)) + sq(y1 - y2)); //Distance between the two points

  return distance;
}


public boolean isInRect(float x1, float y1, float x2, float y2, float recWidth, float recHeight) { //If the object is within a rectangle
  if (x1 >= x2 & y1 >= y2 & x1 <= x2 + recWidth & y1 <= y2 + recHeight) { //Check conditions for being within the rectangle
    return true; //Point is within rectangle
  }

  return false; //No collide
}

public int whereInRect(float x1, float y1, float objWidth, float objHeight, float x2, float y2, float recWidth, float recHeight, float chkType) { //Find where exactly the player is
  if (objWidth <= 0) { //If no width supplied, make it minimal
    objWidth = 0.1f;
  }

  if (objHeight <= 0) { //If no height supplied, make it minimal
    objHeight = 0.1f;
  }


  if (chkType == 1) {
    //Top bottom check

    if (x1 >= x2 & y1 >= y2 & x1 <= x2 + recWidth & y1 <= y2 + recHeight) { //Point is in rectangle

      if ( y1 <= (y2 + (recHeight / 2))) {
        return 1; //Top
      }  
      else if ( y1 > (y2 + (recHeight / 2))) {
        return 2; //Bottom
      }
    }
  } 
  else if (chkType == 2) {
    //Left right check

    if (x1 + (objWidth / 2) >= x2 & y1 + (objHeight / 2) >= y2 & x1 - (objWidth / 2) <= x2 + recWidth & y1 - (objHeight / 2) <= y2 + recHeight) { //Point is in rectangle

      if ( x1 <= (x2 + (recWidth / 2))) {
        return 1; //Left
      }  
      else if ( x1 > (x2 + (recWidth / 2))) {
        return 2; //Right
      }
    }
  }

  return 0; //No collide
}
public int isCollideSphere(float x1, float y1, float z1, float rad1, float x2, float y2, float z2, float rad2) { //Return an integer to check if two circles collide
  float distance = sqrt(sq((x1 - x2)) + sq(y1 - y2) + sq(z1 - z2)); //Distance between the two spheres

  if (distance < rad1 + rad2) {
    return 1;//Intersection
  } 
  else if (distance == rad1 + rad2) {
    return 2; //Touching only
  }

  return 0; //No collide
}

public float chk3Dist(float x1, float y1, float z1, float x2, float y2, float z2) { //Return distance between two 3D points
  float distance = sqrt(sq(x1 - x2) + sq(y1 - y2) + sq(z1 - z2));

  return distance;
}

public boolean isInBox(float x1, float y1, float z1, float x2, float y2, float z2, float recWidth, float recHeight, float recDepth) { //If the object is within a box
  if (x1 >= x2 & y1 >= y2 & z1 >= z2 & x1 <= x2 + recWidth & y1 <= y2 + recHeight & z1 <= z2 + recDepth) { //Check conditions for being within the box
    return true;
  }

  return false;
}

public int isOnBox(float x1, float y1, float z1, float objWidth, float objHeight, float objDepth, float x2, float y2, float z2, float recWidth, float recDepth) { //If an object is on top of a box
  float minX = x2 - (objWidth / 2); //The minimum x position of the block to check
  float maxX = (x2 + recWidth) + (objWidth / 2); //The maximum x position of the block to check
  float minZ = z2 - (objDepth / 2);
  float maxZ = (z2 + recDepth) + (objDepth / 2);
  float objBaseY = y1 + (objHeight / 2); //The object base Y (bottom)

  if (x1 < minX) { //If the object is left of the rectangle
    return 0;
  } 
  else if (x1 > maxX) { //If the object is right of the rectangle
    return 0;
  }

  if (z1 < minZ) {
    return 0;
  }
  else if (z1 > maxZ) {
    return 0;
  }

  if (objBaseY <= (y2 + 3) & objBaseY >= (y2 - 3)) { //If the object is on the block
    return 1; //On a block
  }
  return 0; //No collide
}


public int whereInBox(float x1, float y1, float z1, float objWidth, float objHeight, float objDepth, float x2, float y2, float z2, float recWidth, float recHeight, float recDepth, float chkType) { //Find where exactly the player is
  if (objWidth <= 0) { //If no width supplied, make it minimal
    objWidth = 0.1f;
  }

  if (objHeight <= 0) { //If no height supplied, make it minimal
    objHeight = 0.1f;
  }

  if (chkType == 1) {
    //Top bottom check

    if (x1 >= x2 & y1 >= y2 & z1 >= z2 & x1 <= x2 + recWidth & y1 <= y2 + recHeight & z1 <= z2 + recDepth) { //Point is in rectangle

      if ( y1 <= (y2 + (recHeight / 2))) {
        return 1; //Top
      }  
      else if ( y1 > (y2 + (recHeight / 2))) {
        return 2; //Bottom
      }
    }
  } 
  else if (chkType == 2) {
    //Left right check

    if (x1 + (objWidth / 2) >= x2 & y1 + (objHeight / 2) >= y2 & z1 >= z2 & x1 - (objWidth / 2) <= x2 + recWidth & y1 - (objHeight / 2) <= y2 + recHeight & z1 <= z2 + recDepth) { //Point is in rectangle

      if ( x1 <= (x2 + (recWidth / 2))) {
        return 1; //Left
      }  
      else if ( x1 > (x2 + (recWidth / 2))) {
        return 2; //Right
      }
    }
  }
  else if (chkType == 3) {
    //Front back check

    if (x1 >= x2 & y1 + (objHeight / 2) >= y2 & z1 + objDepth >= z2 & x1 <= x2 + recWidth & y1 - (objHeight / 2) <= y2 + recHeight & z1 - objDepth <= z2 + recDepth) { //Point is in rectangle

      if ( z1 <= (z2 + (recDepth / 2))) {
        return 1; //Front
      }  
      else if ( z1 > (z2 + (recDepth / 2))) {
        return 2; //Back
      }
    }
  }

  return 0; //No collide
}

//Based on the example Texture Cube by Dave Bollinger
//https://processing.org/examples/texturecube.html
public void texturedBox(float x, float y, float z, float rx, float ry, float rz, float w, float h, float d, PImage tex, PImage tex2, PImage tex3, PImage tex4, PImage tex5, PImage tex6) {

  pushMatrix();

  noStroke();

  translate(x, y, z);

  rotateX(rx);
  rotateY(ry);
  rotateZ(rz); 

  textureMode(NORMAL);

  // +Z "front" face
  beginShape(QUADS);
  texture(tex);

  //X, Y, Z, U, V

  vertex(0, 0, d, 0, 0);
  vertex( w, 0, d, 1, 0);
  vertex( w, h, d, 1, 1);
  vertex(0, h, d, 0, 1);

  endShape();

  // -Z "back" face
  beginShape(QUADS);
  texture(tex2);

  vertex( w, 0, 0, 0, 0);
  vertex(0, 0, 0, 1, 0);
  vertex(0, h, 0, 1, 1);
  vertex( w, h, 0, 0, 1);

  endShape();

  // +Y "bottom" face
  beginShape(QUADS);
  texture(tex3);

  vertex(0, h, d, 0, 0);
  vertex( w, h, d, 1, 0);
  vertex( w, h, 0, 1, 1);
  vertex(0, h, 0, 0, 1);

  endShape();

  // -Y "top" face
  beginShape(QUADS);
  texture(tex4);

  vertex(0, 0, 0, 0, 0);
  vertex( w, 0, 0, 1, 0);
  vertex( w, 0, d, 1, 1);
  vertex(0, 0, d, 0, 1);

  endShape();

  // +X "right" face
  beginShape(QUADS);
  texture(tex5);

  vertex( w, 0, d, 0, 0);
  vertex( w, 0, 0, 1, 0);
  vertex( w, h, 0, 1, 1);
  vertex( w, h, d, 0, 1);

  endShape();

  // -X "left" face
  beginShape(QUADS);
  texture(tex6);

  vertex(0, 0, 0, 0, 0);
  vertex(0, 0, d, 1, 0);
  vertex(0, h, d, 1, 1);
  vertex(0, h, 0, 0, 1);

  endShape();

  textureMode(IMAGE);

  popMatrix();
}

public void drawSkyBox(float x, float y, float z, float rx, float ry, float rz, float w, float h, float d, PImage tex, PImage tex2, PImage tex3, PImage tex4, PImage tex5, PImage tex6) {

  pushMatrix();

  noStroke();

  rotateX(rx);
  rotateY(ry);
  rotateZ(rz); 

  translate(x, y, z);



  textureMode(NORMAL);

  // +Z "front" face
  beginShape(QUADS);
  texture(tex);

  //X, Y, Z, U, V
  vertex(0, 0, d, 0, 0);
  vertex( w, 0, d, 1, 0);
  vertex( w, h, d, 1, 1);
  vertex(0, h, d, 0, 1);

  endShape();

  // -Z "back" face
  beginShape(QUADS);
  texture(tex2);

  vertex( w, 0, 0, 0, 0);
  vertex(0, 0, 0, 1, 0);
  vertex(0, h, 0, 1, 1);
  vertex( w, h, 0, 0, 1);

  endShape();

  // +Y "bottom" face
  beginShape(QUADS);
  texture(tex3);

  vertex(0, h, d, 0, 0);
  vertex( w, h, d, 1, 0);
  vertex( w, h, 0, 1, 1);
  vertex(0, h, 0, 0, 1);

  endShape();

  // -Y "top" face
  beginShape(QUADS);
  texture(tex4);

  vertex(0, 0, 0, 0, 0);
  vertex( w, 0, 0, 1, 0);
  vertex( w, 0, d, 1, 1);
  vertex(0, 0, d, 0, 1);

  endShape();

  // +X "right" face
  beginShape(QUADS);
  texture(tex5);

  vertex( w, 0, d, 0, 0);
  vertex( w, 0, 0, 1, 0);
  vertex( w, h, 0, 1, 1);
  vertex( w, h, d, 0, 1);

  endShape();

  // -X "left" face
  beginShape(QUADS);
  texture(tex6);

  vertex(0, 0, 0, 0, 0);
  vertex(0, 0, d, 1, 0);
  vertex(0, h, d, 1, 1);
  vertex(0, h, 0, 0, 1);

  endShape();

  textureMode(IMAGE);

  popMatrix();
}
class gameGround {
  //Ground objects
  float yPos = 0; //Local current Y position
  float startY = 0; //Local start Y position
  float xPos = 0; //Local current X position
  float startX = 0; //Local start X position
  float zPos = 0; //Local current Z position
  float startZ = 0; //Local start Z position
  float Width = 128; //Local width
  float Height = 32; //Local height
  float Depth = 40; //Local depth
  float rotX = 0; //Rotation X axis
  float rotY = 0; //Rotation Y axis
  float rotZ = 0; //Rotation Z axis
  float BlockType = 1; //Local blocktype
  float moveAmnt = 100; //How far to move it
  float State = 0; //Local state of block
  Boolean hasTexture = false;
  PImage topTexture;
  PImage bottomTexture;
  PImage frontTexture;
  PImage backTexture;
  PImage leftTexture;
  PImage rightTexture;

  gameGround (float x, float y, float z, float w, float h, float d, float t, float a) { //Set up the ground block
    yPos = y; //Y
    startY = y; //Y
    xPos = x; //X
    startX = x; //X
    zPos = z - d/2; //Z
    startZ = z - d/2; //Z
    Width = w; //Width
    Height = h; //Height
    Depth = d;
    BlockType = t; //Type
    moveAmnt = a;

    hasTexture = false;
  }

  gameGround (float x, float y, float z, float w, float h, float d, float t, float a, String tex1, String tex2, String tex3, String tex4, String tex5, String tex6) { //Textured ground block with different texture on each face
    yPos = y; //Y
    startY = y; //Y
    xPos = x; //X
    startX = x; //X
    zPos = z - d/2; //Z
    startZ = z - d/2; //Z
    Width = w; //Width
    Height = h; //Height
    Depth = d;
    BlockType = t; //Type
    moveAmnt = a;

    hasTexture = true;

    topTexture = loadImage(tex1);
    bottomTexture = loadImage(tex2);
    frontTexture = loadImage(tex3);
    backTexture = loadImage(tex4);
    leftTexture = loadImage(tex5);
    rightTexture = loadImage(tex6);
  }

  public void render() { //Draw the block
    stroke(0); //Black outline
    strokeWeight(2); //Weight of 2

      if (BlockType == 1) { //Stationary block type
      fill(0, 255, 0); //Green
    } 
    else if (BlockType == 2) { //Up-Down block
      fill(255, 0, 0); //Red
      yPos = startY + (sin(gameTicks / 4) * moveAmnt); //Move the block up and down
    } 
    else if (BlockType == 3) { //Left-Right
      fill(255, 0, 0); //Red
      xPos = startX + (sin(gameTicks / 4) * moveAmnt); //Move the block left and right
    } 
    else if (BlockType == 4) { //Circular
      fill(0, 0, 255); //Blue

      //Move the block circular
      xPos = startX + (cos(gameTicks / 4) * moveAmnt);
      yPos = startY + (sin(gameTicks / 4) * moveAmnt);
    } 
    else if (BlockType == 5) { //Falling block
      fill(0, 255, 255); //Light blue

      if (State == 1) { //If first touched
        State = 2; //Start countdown
      } 
      else if (State >= 2) { //Decrease countdown time
        State = State + 0.1f; //Countdown to drop
      }

      if (State >= 4) { //Drop the block after countdown time reached
        yPos = yPos + 8; //Fall at a fairly fast rate

        //Animate falling with some rotations on X and Z axis, Does not work so well with textures!
        rotX += radians(5);
        rotZ += radians(2);
      }
    } 
    else if (BlockType == 9) { //Win block
      fill(255, 255, 0, 220); //Yellow block
    }

    if (BlockType != 7 && BlockType != 9) { //Wall blocks don't render, winblocks don't render any more either

      if (hasTexture == false) {
        pushMatrix(); //Store matrix

          translate(xPos + (Width / 2), yPos + (Height / 2), zPos + (Depth / 2)); //Apply transformation
        rotateX(rotX); //Remember, this is a visual rotation, and does not affect the math itself!
        rotateY(rotY); //Remember, this is a visual rotation, and does not affect the math itself!
        rotateZ(rotZ); //Remember, this is a visual rotation, and does not affect the math itself!

        box(Width, Height, Depth); //Draw box

        popMatrix(); //Restore matrix
      } 
      else {
        //Draw the shape textured
        texturedBox(xPos, yPos, zPos, rotX, rotY, rotZ, Width, Height, Depth, frontTexture, backTexture, bottomTexture, topTexture, rightTexture, leftTexture);
      }
    }
  }

  public float getX() { //Get X position
    return xPos;
  }

  public float getY() { //Get the Y position
    return yPos;
  }

  public float getZ() {
    return zPos;
  }

  public float getWidth() { //Get the object Width
    return Width;
  }

  public float getHeight() { //Get the object Height
    return Height;
  }

  public float getDepth() { //Get the object depth
    return Depth;
  }

  public float getType() { //Get block type
    return BlockType;
  }

  public float getState() { //Get state of block
    return State;
  }

  public void setX(float x) { //Set block X
    xPos = x;
  }

  public void setY(float y) { //Set block Y
    yPos = y;
  }

  public void setWidth(float w) { //Set block Width
    Width = w;
  }

  public void setHeight(float h) { //Set block Height
    Height = h;
  }

  public void setDepth(float d) { //Set block depth
    Depth = d;
  }

  public void setType(float t) { //Set block type
    BlockType = t;
  }

  public void setState(float s) { //Set block state
    State = s;
  }

  public void resetPosition() { //Return to start position
    xPos = startX;
    yPos = startY;
    zPos = startZ;
    rotX = 0;
    rotY = 0;
    rotZ = 0;
    State = 0;
  }
}
public void update3rdPersonCamera() { //3rd person camera, this code is not fully functional after modification into an FPS game
  cursor(0); //Switch to a regular cursor
  camRotOff = p1.getRotY() + radians(180); //Set the angle offset from the players' Y rotation axis

  camX = p1.getX() + sin(camRotOff) * camRadius; //Set X component of positioning for cam "EYE
  camY = p1.getY() - 250; //Set Y component of positioning for cam "EYE"
  camZ = p1.getZ() + cos(camRotOff) * camRadius; //Set Z component of positioning for cam "EYE"

  camTargetX = p1.getX(); //Get the target X (player X)
  camTargetY = p1.getY(); //Get the target Y (player Y)
  camTargetZ = p1.getZ(); //Get the target Z (player Z)

  camLastX = lerp(camX, camLastX, 1 / 1000); //Lerp makes things smooth
  camLastY = lerp(camY, camLastY, 1 / 1000); //Lerp makes things smooth
  camLastZ = lerp(camZ, camLastZ, 1 / 1000); //Lerp makes things smooth

  camera(camLastX, camLastY, camLastZ, camTargetX, camTargetY, camTargetZ, 0, 1, 0); //Altered camera position
}

public void updateFPSCamera() { //FPS view

  // Position mouse to center of screen code based on example from http://www.openprocessing.org/sketch/20675
  
  robby.mouseMove(frameCenterX, frameCenterY);
  noCursor(); //Hide the cursor

  //Set camera look offset on Y axis
  if (flipControls == false) {
    camLookOff -= mouseRatioY / 100;
  } 
  else {
    camLookOff += mouseRatioY / 100;
  }

  //Clamp down on the value of camLookOff to stay between values
  if (camLookOff < 9.5f) camLookOff = 9.5f;
  if (camLookOff > 12.5f) camLookOff = 12.5f;

  //Allow turning with mouse
  if (flipControls == false) {
    p1.spinY(-1 * mouseRatioX);
  } 
  else {
    p1.spinY(mouseRatioX);
  }

  camRotOff = p1.getRotY(); //Set the angle offset from the players' Y rotation axis

  camHeightOff = (p1.getHeight() / 4); //Set camera eye Y offset

  camX = p1.getX(); //Set X component of positioning for cam "EYE
  camY = p1.getY() - camHeightOff; // - 250; //Set Y component of positioning for cam "EYE"
  camZ = p1.getZ(); //Set Z component of positioning for cam "EYE"

  camTargetX = p1.getX() + sin(camRotOff); //Get the target X (player X)
  camTargetY = p1.getY() - camLookOff; //Get the target Y (player Y)
  camTargetZ = p1.getZ() + cos(camRotOff); //Get the target Z (player Z)

  camLastX = lerp(camX, camLastX, 1 / 1000); //Lerp makes things smooth
  camLastY = lerp(camY, camLastY, 1 / 1000); //Lerp makes things smooth
  camLastZ = lerp(camZ, camLastZ, 1 / 1000); //Lerp makes things smooth

  camera(camLastX, camLastY, camLastZ, camTargetX, camTargetY, camTargetZ, 0, 1, 0); //Altered camera position
}

public void drawInsaneHUD() {

  textFont(insaneFont);
  textSize(24); //Set text size

  fill(0); //Black fill
  text(mGoal, 10, 30); //Draw the current goal

  if (thoughts.size() < 3 + (mNum - mSanityLimit) * 5) { //Not too distracting, but more so each time
    int randText = PApplet.parseInt(random(50));


    if (mNum == mLastMission) {

      if (randText > 35) {
        thoughts.add(new CrazyText(random(width), random(height), "PLEASE CALL 2BR02B!", PApplet.parseInt(random(18, 32)), random(0, 64), 255));
      } 
      else if (randText > 25) {
        thoughts.add(new CrazyText(random(width), random(height), "THE BABY IS COMING!", PApplet.parseInt(random(18, 32)), random(0, 64), 255));
      } 
      else if (randText > 15) {
        thoughts.add(new CrazyText(random(width), random(height), "I HAVE TO SAVE THE BABY!", PApplet.parseInt(random(18, 32)), random(0, 64), 255));
      } 
      else if (randText > 5) {
        thoughts.add(new CrazyText(random(width), random(height), "The gas is taking effect", PApplet.parseInt(random(18, 32)), 255, 255));
      }
    } 
    else {

      if (randText > 30) {
        thoughts.add(new CrazyText(random(width), random(height), "I'm running out of time.", 24, random(0, 128), 255));
      } 
      else if (randText > 20) {
        thoughts.add(new CrazyText(random(width), random(height), "The baby is coming.", 24, random(0, 128), 255));
      } 
      else if (randText > 10) {
        fill(255, 255, 255, 20);
        thoughts.add(new CrazyText(random(width), random(height), "The gas is taking effect", 24, 255, 20));
      }
    }
  }


  //Update thoughts
  for (int i = thoughts.size () - 1; i >= 0; i--) { //For each thought
    CrazyText cT = thoughts.get(i);

    cT.render();

    if (cT.getAge() >= 255) {
      thoughts.remove(i);
    }
  }
}

public void drawHUD(int modeText) {
  pushMatrix();

  textFont(objectiveFont);
  textSize(32); //Set text size

  if (modeText == 0) {
    fill(0); //Black fill
    text(mGoal, 10, 30); //Draw the current goal
  } 
  else if (modeText == 1) { //Game is paused
    fill(255, 255, 255, 5); //White transparent
    rect(0, 0, width, height); //Full screen rectangle

    fill(0); //Back to black fill
    text("PAUSED", 260, 240); //Paused text
  }

  popMatrix();
}

public void mouseWheel(MouseEvent event) { //When mouse wheel scrolled
  float e = event.getCount();
  camRadius = constrain(camRadius + e * 50, 100, 600); //Scroll up or down to increase or decrease zoom between 100 and 600
}

public void keyPressed() { //Triggered every time a key is first pressed down
  if (flipControls == false) {
    if (key == CODED) { //If the key pressed is coded (not a normal key input)
      if (keyCode == UP) {
        keysDown[0] = true; //Up arrow pressed
      }

      if (keyCode == DOWN) {
        keysDown[1] = true; //Down arrow pressed
      }

      if (keyCode == LEFT) {
        keysDown[2] = true; //Left arrow pressed
      } 

      if (keyCode == RIGHT) {
        keysDown[3] = true; //Right arrow pressed
      }
    } 
    else {

      if (key == 'w' || key == 'W')  keysDown[0] = true; //W - up FORWARD
      if (key == 's' || key == 'S')  keysDown[1] = true; //S - down BACKWARD
      if (key == 'a' || key == 'A')  keysDown[2] = true; //A - left TURN
      if (key == 'd' || key == 'D')  keysDown[3] = true; //D - right TURN
      if (key == 32)   keysDown[4] = true; //Spacebar
      if (key == 'q' || key == 'Q')  keysDown[6] = true; //LEFT
      if (key == 'e' || key == 'E')  keysDown[7] = true; //RIGHT
      //if (key == 'q')  keysDown[5] = true; //Q
      if (key == 'p' || key == 'P') keysDown[5] = true; //QUIT
      if (key == 'v' || key == 'V') keysDown[8] = true; //v (SHOOT)
    }
  } 
  else {
    if (key == CODED) { //If the key pressed is coded (not a normal key input)
      if (keyCode == UP) {
        keysDown[1] = true; //Up arrow pressed
      }

      if (keyCode == DOWN) {
        keysDown[0] = true; //Down arrow pressed
      }

      if (keyCode == LEFT) {
        keysDown[3] = true; //Left arrow pressed
      } 

      if (keyCode == RIGHT) {
        keysDown[2] = true; //Right arrow pressed
      }
    } 
    else {

      if (key == 's' || key == 'S')  keysDown[0] = true; //W - up FORWARD
      if (key == 'w' || key == 'W')  keysDown[1] = true; //S - down BACKWARD
      if (key == 'd' || key == 'D')  keysDown[2] = true; //A - left TURN
      if (key == 'a' || key == 'A')  keysDown[3] = true; //D - right TURN
      if (key == 'v' || key == 'V')   keysDown[4] = true; //Spacebar
      if (key == 'e' || key == 'E')  keysDown[6] = true; //LEFT
      if (key == 'q' || key == 'Q')  keysDown[7] = true; //RIGHT
      //if (key == 'q')  keysDown[5] = true; //Q
      if (key == 'p' || key == 'P') keysDown[5] = true; //QUIT
      if (key == 32) keysDown[8] = true; //v (SHOOT)
    }
  }
}

public void keyReleased() { //Triggered every time a key is released
  if (flipControls == false) {
    if (key == CODED) {
      if (keyCode == UP) {
        keysDown[0] = false; //Up arrow released
      }

      if (keyCode == DOWN) {
        keysDown[1] = false; //Down arrow released
      }

      if (keyCode == LEFT) {
        keysDown[2] = false; //Left arrow released
      } 

      if (keyCode == RIGHT) {
        keysDown[3] = false; //Right arrow released
      }

      if (keyCode == SHIFT) {
        keysDown[8] = false; //Shift released
      }
    } 
    else {  
      if (key == 'w' || key == 'W')  keysDown[0] = false; //W - FORWARD
      if (key == 's' || key == 'S')  keysDown[1] = false; //S - BACKWARD
      if (key == 'a' || key == 'A')  keysDown[2] = false; //A - left TURN
      if (key == 'd' || key == 'D')  keysDown[3] = false; //D - right TURN
      if (key == 32)   keysDown[4] = false; //Spacebar JUMP
      if (key == 'q' || key == 'Q')  keysDown[6] = false; //LEFT STRAFE
      if (key == 'e' || key == 'E')  keysDown[7] = false; //RIGHT STRAFE
      //if (key == 'q')  keysDown[5] = true; //Q
      if (key == 'p' || key == 'P') keysDown[5] = false; //QUIT
      if (key == 'v' || key == 'V') keysDown[8] = false; //v (SHOOT)

      if (key == 10 & inGame == true & talkScreen == false) { //The enter key, we only check this on release because it is something we only check once at a time
        if (pauseGame == true ) {
          pauseGame = false; //If already paused, unpause
          pauseResetCam = true; //Reset the camera variables
        } 
        else {
          pauseGame = true; //If not paused, pause
        }
      }
    }
  } 
  else {
    if (key == CODED) {
      if (keyCode == UP) {
        keysDown[1] = false; //Up arrow released
      }

      if (keyCode == DOWN) {
        keysDown[0] = false; //Down arrow released
      }

      if (keyCode == LEFT) {
        keysDown[3] = false; //Left arrow released
      } 

      if (keyCode == RIGHT) {
        keysDown[2] = false; //Right arrow released
      }

      if (keyCode == SHIFT) {
        keysDown[8] = false; //Shift released
      }
    } 
    else {  
      if (key == 's' || key == 'S')  keysDown[0] = false; //W - FORWARD
      if (key == 'w' || key == 'W')  keysDown[1] = false; //S - BACKWARD
      if (key == 'd' || key == 'D')  keysDown[2] = false; //A - left TURN
      if (key == 'a' || key == 'A')  keysDown[3] = false; //D - right TURN
      if (key == 'v' || key == 'V')   keysDown[4] = false; //Spacebar JUMP
      if (key == 'e' || key == 'E')  keysDown[6] = false; //LEFT STRAFE
      if (key == 'q' || key == 'Q')  keysDown[7] = false; //RIGHT STRAFE
      //if (key == 'q')  keysDown[5] = true; //Q
      if (key == 'p' || key == 'P') keysDown[5] = false; //QUIT
      if (key == 32) keysDown[8] = false; //v (SHOOT)

      if (key == 10 & inGame == true & talkScreen == false) { //The enter key, we only check this on release because it is something we only check once at a time
        if (pauseGame == true ) {
          pauseGame = false; //If already paused, unpause
          pauseResetCam = true; //Reset the camera variables
        } 
        else {
          pauseGame = true; //If not paused, pause
        }
      }
    }
  }
}

public void mousePressed() {
  if (flipControls == false) {
    if (mouseButton == LEFT) {
      keysDown[8] = true;
    }

    if (mouseButton == RIGHT) {
      keysDown[4] = true;
    }
  } 
  else {
    if (mouseButton == LEFT) {
      keysDown[4] = true;
    }

    if (mouseButton == RIGHT) {
      keysDown[8] = true;
    }
  }
}

public void mouseReleased() {
  if (flipControls == false) {
    if (mouseButton == LEFT) {
      keysDown[8] = false;
    }

    if (mouseButton == RIGHT) {
      keysDown[4] = false;
    }
  } 
  else {
    if (mouseButton == LEFT) {
      keysDown[4] = false;
    }

    if (mouseButton == RIGHT) {
      keysDown[8] = false;
    }
  }
}

class gameLevel { //Stores data about level  
  String levelFile = "level.txt"; //Default value of level file

  gameLevel (String lf) { //Initial setup of a level file
    levelFile = lf;
  }

  public String getLevel() { //Return the level filename
    return levelFile;
  }
}

public void loadFonts() { //Load up the fonts into memory
  if (osName.indexOf("Mac") > -1) { //In case any fonts need to be renamed for different OS's
    mainFont = createFont("Courier New", 32);
    objectiveFont = createFont("Verdana", 32);
    insaneFont = createFont("Impact", 32);
  } 
  else {
    mainFont = createFont("Courier New", 32);
    objectiveFont = createFont("Verdana", 32);
    insaneFont = createFont("Impact", 32);
  }
}

class MissionGoal {
  //Used for the missions
  float yPos = 0; //Local current Y position
  float startY = 0; //Local start Y position
  float xPos = 0; //Local current X position
  float startX = 0; //Local start X position
  float zPos = 0; //Local current Z position
  float startZ = 0; //Local start Z position
  float Width = 128; //Local width
  float Height = 32; //Local height
  float Depth = 40; //Local depth
  float rotX = 0; //Rotation X axis
  float rotY = 0; //Rotation Y axis
  float rotZ = 0; //Rotation Z axis
  float innerAngle = 0; //Inner object angle
  int initialized = 0; //If it's ready to go
  int goalID = 0; //The ID of the goal (0 = default)
  String description = ""; //The description of the mission
  String response = ""; //The response to the person talked to
  float goalType = 0; // 0 - giver, 1 - person, 2 - mail, 3 - insane


  MissionGoal (float x, float y, float z, float w, float h, float d, float i, String desc, String resp, float t) { //Setup the object
    yPos = y; //Y
    startY = y; //Y
    xPos = x; //X
    startX = x; //X
    zPos = z - d/2; //Z
    startZ = z - d/2; //Z
    Width = w; //Width
    Height = h; //Height
    Depth = d;
    goalID = PApplet.parseInt(i);
    innerAngle = 0;

    if (i == 0) { //Special set up of the first mission
      description = "Find the volunteer.";
      response = "Yes, I will help you. But you\nwill need to do some\nthings for me first.";
      goalType = 0;
      initialized = 1;
    } 
    else {
      if (desc.indexOf("random") > -1 || resp.indexOf("random") > -1) {
        initialized = 0;
      } 
      else {
        description = desc;
        response = resp;
        goalType = t;
        initialized = 1;
      }
    }

    if (i > mLastMission) { //Dynamic limits on mission count    
      mLastMission = PApplet.parseInt(i); //The final mission that appears
    }
  }

  public void initializeMission() {
    if (goalID >= mSanityLimit && goalID < mLastMission) {
      generateDescription(false);
    } 
    else if (goalID >= mLastMission) {
      description = "Calm down. Try to breathe normally";
      response = "The gas might make you see...things...";
      goalType = 3;
    } 
    else {
      generateDescription(true);
    }
    
    initialized = 1;
  }

  public void render() { //Draw the block
    noStroke(); //No outline
    fill(0, 80, 180, 120); //Transparent fill

    innerAngle += 1;

    pushMatrix(); //Store matrix

      translate(xPos, yPos, zPos); //Apply transformation
    rotateX(rotX); //Remember, this is a visual rotation, and does not affect the math itself!
    rotateY(rotY); //Remember, this is a visual rotation, and does not affect the math itself!
    rotateZ(rotZ); //Remember, this is a visual rotation, and does not affect the math itself!

    box(Width, Height, Depth); //Draw box

    rotateY(radians(innerAngle));
    box(Width + 10, Height, Depth + 10);

    popMatrix(); //Restore matrix
  }

  public float getX() { //Get X position
    return xPos;
  }

  public float getY() { //Get the Y position
    return yPos;
  }

  public float getZ() {
    return zPos;
  }

  public float getWidth() { //Get the object Width
    return Width;
  }

  public float getHeight() { //Get the object Height
    return Height;
  }

  public float getDepth() { //Get the object depth
    return Depth;
  }

  public int getID() { //Get the object ID
    return goalID;
  }

  public String getDescription() { //Send back current description
    return description;
  }

  public String getResponse() { //Send back the current response at end of quest
    return response;
  }

  public float getGoalType() { //Send back the type of goal this is
    return goalType;
  }

  public int getInitialized() { //Return if initialized
    return initialized;
  }

  public void setX(float x) { //Set block X
    xPos = x;
  }

  public void setY(float y) { //Set block Y
    yPos = y;
  }

  public void setWidth(float w) { //Set block Width
    Width = w;
  }

  public void setHeight(float h) { //Set block Height
    Height = h;
  }

  public void setDepth(float d) { //Set block depth
    Depth = d;
  }

  public void generateDescription(boolean sane) {
    int randomMission = PApplet.parseInt(random(4));
    if (sane) {
      switch (randomMission) { //Goal types: 0 - giver, 1 - person, 2 - mail, 3 - insane
      case 0:
        description = "Ship a package";
        response = "You delivered the package";
        goalType = 2;
        break;
      case 1:
        description = "See how my child is doing";
        response = "I'm fine, thanks.";
        goalType = 1;
        break;
      case 2:
        description = "Mail a letter";
        response = "You delivered the letter";
        goalType = 2;
        break; 
      case 3:
        description = "Give this to my child";
        response = "It's a photo of my mother!\nThank you!";
        goalType = 1;
        break;
      default:
        break;
      }
    } 
    else {
      goalType = 3;
      switch (randomMission) {
      case 0:
        description = "Bury my slippers";
        response = "Why are you doing these crazy things?";
        break;
      case 1:
        description = "Steal a doorbell";
        response = "Your baby is coming soon.\nAren't you worried?";
        break;
      case 2:
        description = "Collect some grass";
        response = "I think you're fighting\na losing battle...";
        break; 
      case 3:
        description = "Go to the next location";
        response = "Are you in a hurry?";
        break;
      default:
        break;
      }
    }
  }
}
class Player {
  //Player object class
  float xPos = 32; //Local X position value
  float startX = 32; //Local start x position in the level
  float yPos = 32; //Local y position value
  float startY = 32; //Local start Y position
  float zPos = 0; //Local Z position value
  float startZ = 0; //Local start Z position
  float Width = 48; //Local width value
  float Height = 48; //Local height value
  float Depth = 0; //Depth of player object
  float rotX = 0; //Rotation on X axis
  float rotY = PI / 2; //Rotation on Y axis
  float rotZ = 0; //Rotation on Z axis
  float playerSpeed = 3; //Local playerSpeed value
  float jumpTime = -1; //Local jump time, used to calculate jumping time left
  boolean onGround = false; //If the player is on the ground
  float playerGravStr = 2; //Player gravity strength
  float playerJumpStr = 2; //Player jump strength
  float coolDown = 0; //Cool-down time between shooting
  float playerID = 0; //The player ID to assign the bullets they shoot

  Player (float x, float y, float z, float w, float h, float d) { //Set up the player object with x, y, width, height
    xPos = x; //X
    startX = x; //X
    yPos = y; //Y
    startY = y; //Y
    zPos = z; //Z
    startZ = z; //Z
    Width = w; //Width
    Height = h; //Height
    Depth = d; //Depth
  }

  public void render() { //Draw the player object
    if (coolDown > 0) { //If player waiting for attack cooldown
      coolDown = coolDown - 0.2f; //Lower value
    }

    stroke(0); //Player has black outline
    strokeWeight(2); //Player outline is weighted 2

      if (jumpTime > 0) { //If the player is jumping
      yPos = yPos - playerJumpStr; //Move up at jump strength

      jumpTime = jumpTime - 0.1f; //Decrease jump time
    } 
    else { //Otherwise if they should be falling
      jumpTime = -1; //Make sure jump time is below 0

      if (onGround == false) { //If they are not touching the ground
        yPos = yPos + playerGravStr; //Player falls at grav strength
      }
    }

    fill(255, 255, 255); //The player is white

    pushMatrix(); //Store matrix
    
    translate(xPos, yPos, zPos); //Apply transformation
    rotateX(rotX); //Remember, this is a visual X rotation, and does not affect the math itself!
    rotateY(rotY); //Remember, this is a visual Y rotation, and does not affect the math itself!
    rotateZ(rotZ); //Remember, this is a visual Z rotation, and does not affect the math itself!

    sphereDetail(7); //Sphere detail
    box(Width, Height, Depth); //Draw sphere

    popMatrix(); //Restore matrix
  }

  public void moveX(float xOffset) { //Move the player by offset amount in x axis
    xPos = xPos + xOffset;
  }

  public void moveY(float yOffset) { //Move player by offset amount in y axis
    yPos = yPos + yOffset;
  }

  public void moveZ(float zOffset) { //Move player by offset amount in z axis
    zPos = zPos + zOffset;
  }

  public void setX(float x) { //Set the player x position exactly to a value
    xPos = x;
  }

  public void setY(float y) { //Set the player y position exactly to a value
    yPos = y;
  }

  public void setZ(float z) { //Set the player z position exactly to a value
    zPos = z;
  }
  
  public void setWidth(float w) { // Set the player width
    Width = w;
  }
  
  public void setHeight(float h) { //Set the player height
    Height = h;
  }
  
  public void setDepth(float d) { //Set the player depth
    Depth = d;
  }
  
  public void setPlayerID(float id) { //Set the player's ID
    playerID = id;  
  }

  public void spinX(float rotxoff) { //Rotate a certain number of degrees on X axis
    rotX = rotX + radians(rotxoff);
  }

  public void spinY(float rotyoff) { //Rotate a certain number of degrees on Y axis
    rotY = rotY + radians(rotyoff);
  }

  public void spinZ(float rotzoff) { //Rotate a certain number of degrees on Z axis
    rotZ = rotZ + radians(rotzoff);
  }

  public void setXrot(float x) { //Set rotation of X axis
    rotX = radians(x);
  }

  public void setYrot(float y) { //Set rotation of Y axis
    rotY = radians(y);
  }

  public void setZrot(float z) { //Set rotation of Z axis
    rotZ = radians(z);
  }

  public void setGravStr(float str) { //Set the player gravity strength
    playerGravStr = str;
  }

  public void setJumpStr(float str) { //Set the player jump strength
    playerJumpStr = str;
  }

  public float getSpeed() { //Return the player speed
    return playerSpeed;
  }

  public void setSpeed(float newSpeed) { //Set the player speed
    playerSpeed = newSpeed;
  }

  public void touchGround() { //Called to set onGround to true
    onGround = true;
  }

  public void leaveGround() { //Called to set onGround to false
    onGround = false;
  }

  public float getX() { //Return the current x position
    return xPos;
  }

  public float getY() { //Return the current y position
    return yPos;
  }

  public float getZ() { //Return the current z position
    return zPos;
  }

  public float getWidth() { //Return the current width
    return Width;
  }

  public float getHeight() { //Return the current height
    return Height;
  }
  
  public float getDepth() { //Return the current depth
    return Depth;
  }
  
  public float getID() { //Return the player ID
    return playerID;  
  }

  public float getRotX() { //Return the X rotation axis
    return rotX;
  }

  public float getRotY() { //Return the Y rotation axis
    return rotY;
  }
  
  public float getRotZ() { //Return the Z rotation axis
    return rotZ;
  }

  public float getGravStr() { //Return the player gravity strength
    return playerGravStr;
  }

  public float getJumpStr() { //Return the player jump strength
    return playerJumpStr;
  }

  public boolean isOnGround() { //Return if the player is on ground or not
    return onGround;
  }

  public float getCoolDown() { //Get player cooldown
    return coolDown;
  }

  public void Jump(float time) { //Set the player jump time
    if (onGround & jumpTime <= 0) { //Check if they are on the ground and not jumping
      jumpTime = time;
    }
  }

  public boolean isJumping() { //Return if the player is jumping or not
    if (jumpTime > 0) {
      return true;
    } 
    else {
      return false;
    }
  }
  
  public void setCoolDown(float c) { //Set weapon cooldown
    coolDown = c;
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "TheBabyIsComing" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
