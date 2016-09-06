class Pointil extends Filter
{
  int rate =300;
  float radius_min = 2;
  float radius_max = 10;
  boolean generate;

  void Update()
  {
    if (generate && img_init != null) 
    {
      img_init.loadPixels();
      img_final = img_init;
      img_final.loadPixels();
      img_final.pixels = Pixels(img_final);
      img_final.updatePixels();
      image(img_final, 0, 0, width, height);
    }
  }
  int [] Pixels(PImage in)
  {
    if (!generate)
    {
      return in.pixels;
    }
    int [] fin = GetImageArray(in);

    for (int i = 0; i < rate; i++)
    {
      int x = int(random(0, in.width));
      int y = int(random(0, in.height));
      int loc = x + y * in.width;

      color target = in.pixels[loc];
      float radfinal = random(radius_min, radius_max);
      int[] pix = PixelsInRadius(x, y, int(radfinal), in.width);
      for (int p = 0; p < pix.length; p++)
      {
        if (pix[p] >= fin.length) continue;
        if (pix[p] != 0) fin[pix[p]] = target;
      }
    }
    return fin;
  }

  int [] PixelsInRadius(int targx, int targy, int radius, int in_width)
  {
    int [] fin = new int[radius*2*radius*2];
    int ax = 0, ay = 0;
    for (int x = 0; x < radius; x++)
    {
      int check_x = targx+x;
      int check_x_b = targx-x;
      if (check_x >= 0 || check_x_b >= 0)
      {
        for (int y = 0; y < radius; y++)
        {
          int check_y = targy+y;
          int check_y_b = targy-y;
          if (check_y >= 0 || check_y_b >= 0)
          {
            fin[ax+ay] = check_x + check_y*in_width;
            if (check_x_b > 0)
            {
              fin[ax*radius + ay] = check_x_b + check_y*in_width;
            }
            if (check_y_b > 0)
            {
              fin[ax + ay*radius] = check_x + check_y_b*in_width;
            }
            if (check_y_b > 0 && check_x_b > 0)
            {
              fin[ax*radius + ay*radius] = check_x_b + check_y_b*in_width;
            }
          }

          ay++;
        }
      }

      ax++;
      ay = 0;
    }
    return fin;
  }

  public void Input(Input in)
  {
    switch(in)
    {
    case Up:
      radius_max += 1;
      break;
    case Down:
      radius_max -= 1;
      break;
    case A:
      generate = true;
      read_image = true;
      break;
    case B:
      generate = false;
      break;
    }
  }
}