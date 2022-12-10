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
  float enemySpeed = 0.8; //Local enemySpeed value
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

  void render() { //Draw the enemy object

    stroke(0); //Enemy has black outline
    strokeWeight(1); //Enemy outline is weighted 1

      if (jumpTime > 0) { //If the player is jumping
      yPos = yPos - enemyJumpStr; //Move up at jump strength

      jumpTime = jumpTime - 0.1; //Decrease jump time
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

  void setGravStr(float str) { //Set the player gravity strength
    enemyGravStr = str;
  }

  void setJumpStr(float str) { //Set the player jump strength
    enemyJumpStr = str;
  }

  float getSpeed() { //Return the player speed
    return enemySpeed;
  }

  void setSpeed(float newSpeed) { //Set the player speed
    enemySpeed = newSpeed;
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

  float getGravStr() { //Return the player gravity strength
    return enemyGravStr;
  }

  float getJumpStr() { //Return the player jump strength
    return enemyJumpStr;
  }

  boolean isOnGround() { //Return if the player is on ground or not
    return onGround;
  }

  boolean getDidTouch() { //Return if the player is touching
    return didTouch;
  }

  float getDir() { //Get players direction
    return direction;
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

  void setDir(float d) { //Set direction
    direction = d;
  }

  void setDidTouch(boolean d) { //Set didTouch
    didTouch = d;
  }
}
