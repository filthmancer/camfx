class EdgeDetect extends Filter
{
  float threshold = 20F;

  int [] Pixels(PImage in)
  {
    int [] fin = new int[in.pixels.length];
    int in_w = in.width;
    int in_h = in.height;

    for (int x = 1; x < in_w; x++)
    {
      for (int y = 0; y < in_h; y++)
      {
        int loc = x + y * in_w;
        color target = in.pixels[loc];
        color nbour = in.pixels[loc-1];
        float diff = abs(brightness(target) - brightness(nbour));
        
        if(diff > threshold) fin[loc] = color(100,100,100);
        else fin[loc] = color(0,0,0);
      }
    }

    return fin;
  }
  
 
  
  public void Input(Input in)
  {
    switch(in)
    {
      case Up:
      threshold += 1;
      break;
      case Down:
      threshold -= 1;
      break;
    }
  }
}