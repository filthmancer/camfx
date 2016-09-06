class Filter
{
  color [] col_array;
  PImage img_init, img_final;
  int [] Pixels(PImage p)
  {
    int [] fin = p.pixels;
    return fin;
  }
  
  void Update()
  {
    if (img_init != null && filled) 
    {
      filled = false;
      read_image = true;
      img_init.loadPixels();
      img_final = img_init;
      img_final.loadPixels();
      img_final.pixels = Pixels(img_init);
      img_final.updatePixels();
      image(img_final, 0, 0, width, height);
    }
  }

  void AddColor(color c)
  {
    if (col_array == null || col_array.length == 0)
    {
      col_array = new color[1]; 
      col_array[0] = c;
    } else
    {
      color [] fin = new color[col_array.length + 1];
      for (int i = 0; i < col_array.length; i++)
      {
        fin[i] = col_array[i];
      }
      fin[fin.length-1] = c;
      col_array = fin;
    }
  }
  
  public void Input(Input in)
  {

  }
  
  public int [] GetImageArray(PImage p)
  {
    p.loadPixels();
    int [] fin = new int [p.pixels.length]; 
    for(int i = 0; i < p.pixels.length; i++)
    {
      fin[i] = p.pixels[i]; 
    }
    return fin;
  }

  public float colorDist(color c1, color c2)
    {
      float rmean =(red(c1) + red(c2)) / 2;
      float r = red(c1) - red(c2);
      float g = green(c1) - green(c2);
      float b = blue(c1) - blue(c2);
      return sqrt((int(((512+rmean)*r*r))>>8)+(4*g*g)+(int(((767-rmean)*b*b))>>8));
    } // colorDist()
}

class CRT_Filter extends Filter
{
  float thickness, rate;
  int line_size = 15;
  CRT_Filter(float _thickness, float _rate)
  {
    thickness = _thickness;
    rate = _rate;
  }
  int [] Pixels(PImage init)
  {
    int imgwidth = init.width;
    int imgheight = init.height;
    int [] pix = GetImageArray(init);

    int lines_skipped = 0;
    int lines_drawn = 0;
    boolean drawscan = false;

    for(int column = 0; column < imgheight-line_size; column++)
    {
      for(int row = 0; row < imgwidth; row+=line_size)
      {
        float r =0, g = 0, b = 0;
        int loc = row+column*imgwidth;

        for(int i = 0; i < line_size; i++)
        {
          color tg = pix[loc + i];
          r += red(tg);
          g += green(tg);
          b += blue(tg);
        }
        r /= line_size;
       g /= line_size;
        b /= line_size;

        color [] fincol = new color[]{color(r,0,0), color(0,g,0), color(0,0,b)};
        int t = 0;
        for(int i = 0; i < line_size; i+=line_size/3)
        {
          color targ = fincol[t];
          //println(targ);
          for(int x = 0; x < line_size/3; x++)
          {
            pix[loc+i+x] = targ;
          }
          t++;
        }
      }
    }
    return pix;
  }

  public void Input(Input in)
  {
    switch(in)
    {
    case Up:
      line_size = int(Clamp(line_size+3,3,90));
      break;
    case Down:
      line_size = int(Clamp(line_size-3,3,90));
      break;
     case A:
       
     break;
    }
  }
}