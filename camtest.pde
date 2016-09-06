import processing.video.*;

Capture cap;
PImage[] buffer;
PImage buff;
int FPS = 30;
int index = 0;
boolean filled = false;

CRT_Filter CRT;
Pixelator PIX;
Pointil PTL;
EdgeDetect EDGE;
Merger MRG;
FrameUpdater FRM;
Filter target;

boolean read_image = true;

int lagframes = 1;
void setup()
{
  size(900, 500);
  frameRate(FPS);

  cap = new Capture(this);
  cap.start();

  CRT = new CRT_Filter(2, 20);
  CRT.AddColor(color(0, 100, 0));

  PIX = new Pixelator();
  PTL = new Pointil();
  EDGE = new EdgeDetect();
  MRG = new Merger();
  FRM = new FrameUpdater();
  
  target = PIX;
  buffer = new PImage[lagframes];
}

int updateFilterTime = 0, FilterTime = 0;
void draw()
{
  target.Update(); 
}

int lastFrame = 0;
void captureEvent(Capture cam)
{
  
  /*if(frameCount == lastFrame)
  {
    return;
  }
  if(frameCount - lastFrame > 3)
  {
    lastFrame = frameCount;
    return;
  }*/
  lastFrame = frameCount;
  
  if (read_image)
  {
    cam.read();
    buff = cam.get();
    target.img_init = cam.get();
    //target.img_final = cam.get();
    filled = true;
  }
}

void keyPressed()
{
  if (key == CODED) {
    if (keyCode == UP) {
      target.Input(Input.Up);
      //PIX.pixel_target = Clamp(PIX.pixel_radius*1.2, 1, 200);
    } else if (keyCode == DOWN) {
      target.Input(Input.Down);
      //PIX.pixel_target = Clamp(PIX.pixel_radius/1.2, 1, 200);
    }
  } else
  {
    switch(key)
    {
    case 'z':
      target.Input(Input.A);
      break;
    case 'x':
      target.Input(Input.B);
      break;
    case 'c':
      target.Input(Input.C);
      break;
    }
  }
}


public float Clamp(float input, float min, float max)
{
  if (input > max) return max;
  else if (input < min) return min;
  else return input;
}

public enum Input
{
  Up, Down, A, B, C
};