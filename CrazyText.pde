class CrazyText { //Class for bullets
  float xPos = 0; //Local X position
  float yPos = 0; //Local Y position
  String strText = "";
  float tSize = 18;
  float fillAmnt = 0;
  float alpha = 0;

  CrazyText ( float x, float y, String t, float tS, float fA, float a) { //Set up bullet object
    xPos = x; //X
    yPos = y; //Y
    strText = t;
    tSize = tS;
    fillAmnt = fA;
    alpha = a;
  }

  void render() { //Draw the bullet

    textFont(insaneFont);
    
    textSize(tSize);
    
    fill(fillAmnt, alpha);
    
    text(strText, xPos, yPos);
    
    alpha -= 20;

  }

  float getAge() { //Get alpha
    return 255 - alpha;
  }

}
