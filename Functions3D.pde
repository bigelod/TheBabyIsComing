int isCollideSphere(float x1, float y1, float z1, float rad1, float x2, float y2, float z2, float rad2) { //Return an integer to check if two circles collide
  float distance = sqrt(sq((x1 - x2)) + sq(y1 - y2) + sq(z1 - z2)); //Distance between the two spheres

  if (distance < rad1 + rad2) {
    return 1;//Intersection
  } 
  else if (distance == rad1 + rad2) {
    return 2; //Touching only
  }

  return 0; //No collide
}

float chk3Dist(float x1, float y1, float z1, float x2, float y2, float z2) { //Return distance between two 3D points
  float distance = sqrt(sq(x1 - x2) + sq(y1 - y2) + sq(z1 - z2));

  return distance;
}

boolean isInBox(float x1, float y1, float z1, float x2, float y2, float z2, float recWidth, float recHeight, float recDepth) { //If the object is within a box
  if (x1 >= x2 & y1 >= y2 & z1 >= z2 & x1 <= x2 + recWidth & y1 <= y2 + recHeight & z1 <= z2 + recDepth) { //Check conditions for being within the box
    return true;
  }

  return false;
}

int isOnBox(float x1, float y1, float z1, float objWidth, float objHeight, float objDepth, float x2, float y2, float z2, float recWidth, float recDepth) { //If an object is on top of a box
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


int whereInBox(float x1, float y1, float z1, float objWidth, float objHeight, float objDepth, float x2, float y2, float z2, float recWidth, float recHeight, float recDepth, float chkType) { //Find where exactly the player is
  if (objWidth <= 0) { //If no width supplied, make it minimal
    objWidth = 0.1;
  }

  if (objHeight <= 0) { //If no height supplied, make it minimal
    objHeight = 0.1;
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
void texturedBox(float x, float y, float z, float rx, float ry, float rz, float w, float h, float d, PImage tex, PImage tex2, PImage tex3, PImage tex4, PImage tex5, PImage tex6) {

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

void drawSkyBox(float x, float y, float z, float rx, float ry, float rz, float w, float h, float d, PImage tex, PImage tex2, PImage tex3, PImage tex4, PImage tex5, PImage tex6) {

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
