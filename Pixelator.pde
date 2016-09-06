class Pixelator extends Filter
{

  int grouping_radius = 200;
  float pixel_radius = 20;
  float pixel_target = 0;
  public boolean LerpedPixels;
  Pixelator()
  {
    pixel_radius = 20;
    pixel_target = 20;
  }

  void Update()
  {
    pixel_radius = lerp(pixel_radius, pixel_target, 0.2);
    if (img_init != null && filled) 
    {
      filled = false;
      read_image = true;
      img_init.loadPixels();
      img_final = img_init;
      img_final.loadPixels();

      if(LerpedPixels) img_final.pixels = Pixels(img_init);
      else img_final.pixels = Pixels2(img_init);

      img_final.updatePixels();
      image(img_final, 0, 0, width, height);
    }
  }
  int [] Pixels2(PImage in)
  {
    int in_width = in.width;
    int in_height = in.height;
    int [] fin = new int[in.pixels.length];

    int point_x = 0;
    int point_y = 0;
    int point_oneD = point_x + point_y*in_width;
    color point_col = in.pixels[point_oneD];
    for (int x = 0; x < in_width; x ++)
    {
      for (int y = 0; y < in_height; y ++)
      {
        int loc = x + y*in_width;
        if (abs(y-point_y) >= pixel_radius)
        {
          point_y += pixel_radius;
          point_oneD = point_x + point_y*in_width;
          point_col = in.pixels[point_oneD];
        }
        fin[loc] = point_col;
      }

      if (abs(x-point_x) > pixel_radius)
      {
        point_x += pixel_radius;
      }
      point_y = 0;
      point_oneD = point_x + point_y*in_width;
      point_col = in.pixels[point_oneD];
    }

    return fin;
  }

  int [] Pixels(PImage in)
  {
    int in_width = in.width;
    int in_height = in.height;
    int [] fin = new int[in.pixels.length];

    int point_x = 0;
    int point_y = 0;
    int point_oneD = point_x + point_y*in_width;
    color point_col = in.pixels[point_oneD];

    int pix_rad_int = int(pixel_radius);
    int min_x = int(Clamp(point_x-pix_rad_int, 0, in.width-1));
    int max_x = int(Clamp(point_x+pix_rad_int, 0, in.width-1));
    int min_y = int(Clamp(point_y-pix_rad_int, 0, in.height-1));
    int max_y = int(Clamp(point_y+pix_rad_int, 0, in.height-1));
    color top_left = in.pixels[min_x + min_y*in_width];
    color top_right = in.pixels[max_x + min_y * in_width];
    color bot_left = in.pixels[min_x + max_y*in_width];
    color bot_right = in.pixels[max_x + max_y*in_width];
    color middle = lerpColor(lerpColor(top_left, top_right, 0.5F), lerpColor(bot_left, bot_right, 0.5F), 0.5F);
    for (int x = 0; x < in_width; x ++)
    {
      for (int y = 0; y < in_height; y ++)
      {
        int loc = x + y*in_width;
        if (abs(y-point_y) >= pixel_radius)
        {
          point_y += pixel_radius;
          point_oneD = point_x + point_y*in_width;
          min_x = int(Clamp(point_x-pix_rad_int, 0, in.width-1));
          max_x = int(Clamp(point_x+pix_rad_int, 0, in.width-1));
          min_y = int(Clamp(point_y-pix_rad_int, 0, in.height-1));
          max_y = int(Clamp(point_y+pix_rad_int, 0, in.height-1));
          top_left = in.pixels[min_x + min_y*in_width];
          top_right = in.pixels[max_x + min_y * in_width];
          bot_left = in.pixels[min_x + max_y*in_width];
          bot_right = in.pixels[max_x + max_y*in_width];
          middle = lerpColor(lerpColor(top_left, top_right, 0.5F), lerpColor(bot_left, bot_right, 0.5F), 0.5F);
        }
        fin[loc] = middle;
      }

      if (abs(x-point_x) > pixel_radius)
      {
        point_x += pixel_radius;
      }
      point_y = 0;
      point_oneD = point_x + point_y*in_width;
      min_x = int(Clamp(point_x-pix_rad_int, 0, in.width-1));
      max_x = int(Clamp(point_x+pix_rad_int, 0, in.width-1));
      min_y = int(Clamp(point_y-pix_rad_int, 0, in.height-1));
      max_y = int(Clamp(point_y+pix_rad_int, 0, in.height-1));
      top_left = in.pixels[min_x + min_y*in_width];
      top_right = in.pixels[max_x + min_y * in_width];
      bot_left = in.pixels[min_x + max_y*in_width];
      bot_right = in.pixels[max_x + max_y*in_width];
      middle = lerpColor(lerpColor(top_left, top_right, 0.5F), lerpColor(bot_left, bot_right, 0.5F), 0.5F);
    }

    return fin;
  }

  public void Input(Input in)
  {
    switch(in)
    {
    case Up:
      pixel_target = Clamp(pixel_target+1,1,100);
      break;
    case Down:
      pixel_target = Clamp(pixel_target-1,1,100);
      break;
     case A:
       LerpedPixels = !LerpedPixels;
     break;
    }
  }
}