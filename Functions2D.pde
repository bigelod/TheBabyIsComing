//Most of these are no longer needed but are here for reference

int isOnRect(float x1, float y1, float objWidth, float objHeight, float x2, float y2, float recWidth) { //If an object is on top of a rectangle
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

int isCollideCircle(float x1, float y1, float rad1, float x2, float y2, float rad2) { //Return an integer to check if two circles collide
  float distance = sqrt(sq((x1 - x2)) + sq(y1 - y2)); //Distance between the two circles

  if (distance < rad1 + rad2) {
    return 1;//Intersection
  } 
  else if (distance == rad1 + rad2) {
    return 2; //Touching only
  }

  return 0; //No collide
}


float chkDist(float x1, float y1, float x2, float y2) { //Return distance between two points
  float distance = sqrt(sq((x1 - x2)) + sq(y1 - y2)); //Distance between the two points

  return distance;
}


boolean isInRect(float x1, float y1, float x2, float y2, float recWidth, float recHeight) { //If the object is within a rectangle
  if (x1 >= x2 & y1 >= y2 & x1 <= x2 + recWidth & y1 <= y2 + recHeight) { //Check conditions for being within the rectangle
    return true; //Point is within rectangle
  }

  return false; //No collide
}

int whereInRect(float x1, float y1, float objWidth, float objHeight, float x2, float y2, float recWidth, float recHeight, float chkType) { //Find where exactly the player is
  if (objWidth <= 0) { //If no width supplied, make it minimal
    objWidth = 0.1;
  }

  if (objHeight <= 0) { //If no height supplied, make it minimal
    objHeight = 0.1;
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
