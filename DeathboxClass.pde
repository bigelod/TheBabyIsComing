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

  boolean inDeathBox(float x, float y, float z) { //If something is in the deathbox
    if (x >= minX & y >= minY & x <= maxX & y <= maxY & z >= zPos - (Depth / 2) & z <= zPos + (Depth / 2)) {
      return true; //Yes, that point is within the box
    } 
    else {
      return false; //No, that point is not within the box
    }
  }

  void render() { //Draw the deathbox
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
