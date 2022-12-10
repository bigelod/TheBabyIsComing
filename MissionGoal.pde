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
    goalID = int(i);
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
      mLastMission = int(i); //The final mission that appears
    }
  }

  void initializeMission() {
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

  void render() { //Draw the block
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

  float getX() { //Get X position
    return xPos;
  }

  float getY() { //Get the Y position
    return yPos;
  }

  float getZ() {
    return zPos;
  }

  float getWidth() { //Get the object Width
    return Width;
  }

  float getHeight() { //Get the object Height
    return Height;
  }

  float getDepth() { //Get the object depth
    return Depth;
  }

  int getID() { //Get the object ID
    return goalID;
  }

  String getDescription() { //Send back current description
    return description;
  }

  String getResponse() { //Send back the current response at end of quest
    return response;
  }

  float getGoalType() { //Send back the type of goal this is
    return goalType;
  }

  int getInitialized() { //Return if initialized
    return initialized;
  }

  void setX(float x) { //Set block X
    xPos = x;
  }

  void setY(float y) { //Set block Y
    yPos = y;
  }

  void setWidth(float w) { //Set block Width
    Width = w;
  }

  void setHeight(float h) { //Set block Height
    Height = h;
  }

  void setDepth(float d) { //Set block depth
    Depth = d;
  }

  void generateDescription(boolean sane) {
    int randomMission = int(random(4));
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
