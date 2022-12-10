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

  void render() { //Draw the bullet
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

  void setX(float x) { //Set X position
    xPos = x;
  }

  void setY(float y) { //Set Y position
    yPos = y;
  }

  void setDir(float d) { //Set Direction
    dir = d;
  }

  void setSpeed(float s) { //Set Speed
    speed = s;
  }

  void setOwner(float o) { //Set Owner
    owner = o;
  }

  void setMaxDist(float d) { //Set max distance
    maxDist = d;
  }

  float getX() { //Get X position
    return xPos;
  }

  float getY() { //Get Y position
    return yPos;
  }
  
  float getZ() { //Get Z position
    return zPos;
  }

  float getDir() { //Get Direction
    return dir;
  }

  float getSpeed() { //Get Speed
    return speed;
  }

  float getOwner() { //Get Owner
    return owner;
  }

  float getDist() { //Travel distance
    return flyDist;
  }

  float getMaxDist() { //Max travel distance
    return maxDist;
  }
}
