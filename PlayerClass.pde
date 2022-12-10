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

  void render() { //Draw the player object
    if (coolDown > 0) { //If player waiting for attack cooldown
      coolDown = coolDown - 0.2; //Lower value
    }

    stroke(0); //Player has black outline
    strokeWeight(2); //Player outline is weighted 2

      if (jumpTime > 0) { //If the player is jumping
      yPos = yPos - playerJumpStr; //Move up at jump strength

      jumpTime = jumpTime - 0.1; //Decrease jump time
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

  void moveX(float xOffset) { //Move the player by offset amount in x axis
    xPos = xPos + xOffset;
  }

  void moveY(float yOffset) { //Move player by offset amount in y axis
    yPos = yPos + yOffset;
  }

  void moveZ(float zOffset) { //Move player by offset amount in z axis
    zPos = zPos + zOffset;
  }

  void setX(float x) { //Set the player x position exactly to a value
    xPos = x;
  }

  void setY(float y) { //Set the player y position exactly to a value
    yPos = y;
  }

  void setZ(float z) { //Set the player z position exactly to a value
    zPos = z;
  }
  
  void setWidth(float w) { // Set the player width
    Width = w;
  }
  
  void setHeight(float h) { //Set the player height
    Height = h;
  }
  
  void setDepth(float d) { //Set the player depth
    Depth = d;
  }
  
  void setPlayerID(float id) { //Set the player's ID
    playerID = id;  
  }

  void spinX(float rotxoff) { //Rotate a certain number of degrees on X axis
    rotX = rotX + radians(rotxoff);
  }

  void spinY(float rotyoff) { //Rotate a certain number of degrees on Y axis
    rotY = rotY + radians(rotyoff);
  }

  void spinZ(float rotzoff) { //Rotate a certain number of degrees on Z axis
    rotZ = rotZ + radians(rotzoff);
  }

  void setXrot(float x) { //Set rotation of X axis
    rotX = radians(x);
  }

  void setYrot(float y) { //Set rotation of Y axis
    rotY = radians(y);
  }

  void setZrot(float z) { //Set rotation of Z axis
    rotZ = radians(z);
  }

  void setGravStr(float str) { //Set the player gravity strength
    playerGravStr = str;
  }

  void setJumpStr(float str) { //Set the player jump strength
    playerJumpStr = str;
  }

  float getSpeed() { //Return the player speed
    return playerSpeed;
  }

  void setSpeed(float newSpeed) { //Set the player speed
    playerSpeed = newSpeed;
  }

  void touchGround() { //Called to set onGround to true
    onGround = true;
  }

  void leaveGround() { //Called to set onGround to false
    onGround = false;
  }

  float getX() { //Return the current x position
    return xPos;
  }

  float getY() { //Return the current y position
    return yPos;
  }

  float getZ() { //Return the current z position
    return zPos;
  }

  float getWidth() { //Return the current width
    return Width;
  }

  float getHeight() { //Return the current height
    return Height;
  }
  
  float getDepth() { //Return the current depth
    return Depth;
  }
  
  float getID() { //Return the player ID
    return playerID;  
  }

  float getRotX() { //Return the X rotation axis
    return rotX;
  }

  float getRotY() { //Return the Y rotation axis
    return rotY;
  }
  
  float getRotZ() { //Return the Z rotation axis
    return rotZ;
  }

  float getGravStr() { //Return the player gravity strength
    return playerGravStr;
  }

  float getJumpStr() { //Return the player jump strength
    return playerJumpStr;
  }

  boolean isOnGround() { //Return if the player is on ground or not
    return onGround;
  }

  float getCoolDown() { //Get player cooldown
    return coolDown;
  }

  void Jump(float time) { //Set the player jump time
    if (onGround & jumpTime <= 0) { //Check if they are on the ground and not jumping
      jumpTime = time;
    }
  }

  boolean isJumping() { //Return if the player is jumping or not
    if (jumpTime > 0) {
      return true;
    } 
    else {
      return false;
    }
  }
  
  void setCoolDown(float c) { //Set weapon cooldown
    coolDown = c;
  }
}
