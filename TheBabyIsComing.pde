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

import processing.opengl.*; //Load OpenGL library for doing 3D
import ddf.minim.*; //The audio library
import java.awt.AWTException; //Exception check
import java.awt.Robot; //Move mouse
import java.awt.MouseInfo; //Get absolute mouse position

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

float maxBad = 10.0; //The number of enemies maximum if makingBad = true
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
void setup() {
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
void draw() {
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

                mSanityLimit = int(mSL);
              }

              if (data[0].indexOf("nummissions") != -1) { //For levels with lots of missions
                float mLM = Float.parseFloat(data[1]);

                mLastMission = int(mLM);
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
        maxBad = 5.0;
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

        gameTicks = gameTicks + 0.1;  //Used for timing (sin and similar functions)

      //Redraw background if you aren't too far gone
      if (mNum < mSanityLimit + 1) {
        background(backR, backG, backB);

        if (skyBox == true) {
          drawSkyBox(-2900, -1600, -2900, 0, p1.getRotY() * -1, 0, 4400, 3300, 4400, skyFront, skyBack, skyBottom, skyTop, skyRight, skyLeft);
          
        }
      }


      updateFPSCamera(); //Prepare the camera to follow the player, in MiscClasses

      //Check if we can spawn more enemies
      if (Math.round(Math.random() * 100) == 50.0 && enemies.size() - 1 < maxBad & makingBad == true) { //enemies.size() - 1 < 1) {
        enemies.add(new enemy(p1.getX() + random(-500.0, 500.0), p1.getY() - 320, p1.getZ() + random(-500.0, 500), 48, 32, 20));
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
          if (isInBox(p1.getX() + sin(camRotOff) * 3 * p1.getDepth(), p1.getY(), p1.getZ() + cos(camRotOff) * 3 * p1.getDepth(), ground.getX(), ground.getY() - 2.5, ground.getZ(), ground.getWidth(), ground.getHeight() + 5, ground.getDepth())) { //If camera is within an object at offset
            p1.moveX(-1 * p1.getSpeed() * cos(p1.getRotY() - radians(90))); //Move X component BACKWARD
            p1.moveZ(1 * p1.getSpeed() * sin(p1.getRotY()- radians(90))); //Move Z component BACKWARD
          }
        } 
        else if (p1.getY() > ground.getY()) { //Even if you're jumping, you shouldn't always be able to go through stuff
          if (isInBox(p1.getX() + sin(camRotOff) * 3 * p1.getDepth(), p1.getY(), p1.getZ() + cos(camRotOff) * 3 * p1.getDepth(), ground.getX(), ground.getY() - 2.5, ground.getZ(), ground.getWidth(), ground.getHeight() + 5, ground.getDepth())) { //If camera is within an object at offset
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
          gameTrack.skip(int(random(2, 5)) * (mNum - mSanityLimit)); //Start to distort audio
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
            endGameFade += 0.1;
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
              switch (int(mG.getGoalType())) {
              case 0:
                discussionImage = loadImage("giver.png");
                break;
              case 1:
                discussionImage = loadImage("person" + int(random(1, 4)) + ".png");
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

              talkSituation = int(mG.getGoalType());
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
              talkTimer -= 0.5;
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
      gameTicks -= 0.1; //Rewind
    }

    if (titleTicks < 255) { //Simple fade-in
      titleTicks += 1.5;
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

    titleTicks += 0.1; //Credits are timed
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
