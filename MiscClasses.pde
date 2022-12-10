void update3rdPersonCamera() { //3rd person camera, this code is not fully functional after modification into an FPS game
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

void updateFPSCamera() { //FPS view

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
  if (camLookOff < 9.5) camLookOff = 9.5;
  if (camLookOff > 12.5) camLookOff = 12.5;

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

void drawInsaneHUD() {

  textFont(insaneFont);
  textSize(24); //Set text size

  fill(0); //Black fill
  text(mGoal, 10, 30); //Draw the current goal

  if (thoughts.size() < 3 + (mNum - mSanityLimit) * 5) { //Not too distracting, but more so each time
    int randText = int(random(50));


    if (mNum == mLastMission) {

      if (randText > 35) {
        thoughts.add(new CrazyText(random(width), random(height), "PLEASE CALL 2BR02B!", int(random(18, 32)), random(0, 64), 255));
      } 
      else if (randText > 25) {
        thoughts.add(new CrazyText(random(width), random(height), "THE BABY IS COMING!", int(random(18, 32)), random(0, 64), 255));
      } 
      else if (randText > 15) {
        thoughts.add(new CrazyText(random(width), random(height), "I HAVE TO SAVE THE BABY!", int(random(18, 32)), random(0, 64), 255));
      } 
      else if (randText > 5) {
        thoughts.add(new CrazyText(random(width), random(height), "The gas is taking effect", int(random(18, 32)), 255, 255));
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

void drawHUD(int modeText) {
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

void mouseWheel(MouseEvent event) { //When mouse wheel scrolled
  float e = event.getCount();
  camRadius = constrain(camRadius + e * 50, 100, 600); //Scroll up or down to increase or decrease zoom between 100 and 600
}

void keyPressed() { //Triggered every time a key is first pressed down
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

void keyReleased() { //Triggered every time a key is released
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

void mousePressed() {
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

void mouseReleased() {
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

  String getLevel() { //Return the level filename
    return levelFile;
  }
}

void loadFonts() { //Load up the fonts into memory
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

