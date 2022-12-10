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

  void render() { //Draw the block
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
        State = State + 0.1; //Countdown to drop
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

  float getType() { //Get block type
    return BlockType;
  }

  float getState() { //Get state of block
    return State;
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

  void setType(float t) { //Set block type
    BlockType = t;
  }

  void setState(float s) { //Set block state
    State = s;
  }

  void resetPosition() { //Return to start position
    xPos = startX;
    yPos = startY;
    zPos = startZ;
    rotX = 0;
    rotY = 0;
    rotZ = 0;
    State = 0;
  }
}
