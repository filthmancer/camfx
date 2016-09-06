import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.video.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class camtest extends PApplet {



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
public void setup()
{
  
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
  target = FRM;
  buffer = new PImage[lagframes];
}

int updateFilterTime = 0, FilterTime = 0;
public void draw()
{
  target.Update(); 
}

int lastFrame = 0;
public void captureEvent(Capture cam)
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

public void keyPressed()
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
class EdgeDetect extends Filter
{
  float threshold = 20F;

  public int [] Pixels(PImage in)
  {
    int [] fin = new int[in.pixels.length];
    int in_w = in.width;
    int in_h = in.height;

    for (int x = 1; x < in_w; x++)
    {
      for (int y = 0; y < in_h; y++)
      {
        int loc = x + y * in_w;
        int target = in.pixels[loc];
        int nbour = in.pixels[loc-1];
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
class Filter
{
  int [] col_array;
  PImage img_init, img_final;
  public int [] Pixels(PImage p)
  {
    int [] fin = p.pixels;
    return fin;
  }
  
  public void Update()
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

  public void AddColor(int c)
  {
    if (col_array == null || col_array.length == 0)
    {
      col_array = new int[1]; 
      col_array[0] = c;
    } else
    {
      int [] fin = new int[col_array.length + 1];
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

  public float colorDist(int c1, int c2)
    {
      float rmean =(red(c1) + red(c2)) / 2;
      float r = red(c1) - red(c2);
      float g = green(c1) - green(c2);
      float b = blue(c1) - blue(c2);
      return sqrt((PApplet.parseInt(((512+rmean)*r*r))>>8)+(4*g*g)+(PApplet.parseInt(((767-rmean)*b*b))>>8));
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
  public int [] Pixels(PImage init)
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
          int tg = pix[loc + i];
          r += red(tg);
          g += green(tg);
          b += blue(tg);
        }
        r /= line_size;
       g /= line_size;
        b /= line_size;

        int [] fincol = new int[]{color(r,0,0), color(0,g,0), color(0,0,b)};
        int t = 0;
        for(int i = 0; i < line_size; i+=line_size/3)
        {
          int targ = fincol[t];
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
      line_size = PApplet.parseInt(Clamp(line_size+3,3,90));
      break;
    case Down:
      line_size = PApplet.parseInt(Clamp(line_size-3,3,90));
      break;
     case A:
       
     break;
    }
  }
}
class FrameUpdater extends Filter
{
	PImage prev;
	public int [] Pixels(PImage in)
	{
		if(prev == null) 
			{
				prev = in;
				return in.pixels;
			}

		int [] fin = GetImageArray(prev);
		prev.loadPixels();
		for(int x = 0; x < in.width; x++)
		{
			for(int y = 0; y < in.height; y++)
			{
				int loc = x + y * in.width;

				if(colorDist(fin[loc], in.pixels[loc]) > 130)
				{
					fin[loc] = in.pixels[loc];
				}
			}
		}
		prev = in;
		return fin;
	}
}

class Triangulate extends Filter
{
	/*int [] Pixels(PImage in)
	{

	}*/
}
class Merger extends Filter
{
	float threshold = 70;
	int posdiff = 200;

	public int [] Pixels2(PImage in)
	{
		int [] fin = GetImageArray(in);
		int inwidth = in.width;
		int inheight = in.height;

		ArrayList<Merger_Group> groups = new ArrayList<Merger_Group>();

		for(int x = 0; x < inwidth; x++)
		{
			fin[x] = color(0,0,0);
			for(int y = 0; y < inheight; y++)
			{
				int loc = x + y * inwidth;

				int c = fin[loc];

				boolean addgroup = true;
				for(int g = 0; g < groups.size(); g++)
				{
					if(groups.get(g).InThreshold(x, y, c))
					{
						groups.get(g).AddObj(loc, c);
						addgroup = false;
						break;
					}
				}
				
				if(addgroup)
				{
					groups.add(new Merger_Group(loc, c, x, y));
				}
			}
		}

		//print(fin[100]);
		for(int g = 0; g < groups.size(); g++)
		{
			int fincol = groups.get(g).FinalColor();
			int [] points = groups.get(g).GetPixels();
		//	println(fincol + ":" + points.length);
			for(int i = 0; i < points.length; i++)
			{
				fin[points[i]] = fincol;
			}
		}
		//println(fin[100]);

		return fin;
	}

	int colors = 3;
	public int [] Pixels(PImage in)
	{	
		int inwidth = in.width;
		int inheight = in.height;
		int [] fin = GetImageArray(in);

		int [] final_colors = new int [colors];
		ArrayList<Merger_Obj> obj = new ArrayList<Merger_Obj>();
		for(int i = 0; i < fin.length; i++)
		{
			boolean add = true;
			for(int x = 0; x < obj.size(); x++)
			{
				if(colorDist(fin[i], obj.get(x).col) < threshold)  
				{
					obj.get(x).point ++;
					add = false;
					break;
				}
			}
			if(add)
			{
				obj.add(new Merger_Obj(1, fin[i]));
			}
		}

		if(obj.size() == 0) return in.pixels;
		for(int c = 0; c < colors; c++)
		{
			int highest = color(0,0,0);
			int highest_num = 0;
			int num_actual = 0;

			for(int i = 0; i < obj.size(); i++)
			{
				if(colorDist(obj.get(i).col, color(0,0,0)) < 100) obj.remove(i);
				if(obj.size() < i) break;
				if(obj.get(i).point > highest_num)
				{
					highest_num = obj.get(i).point;
					highest = obj.get(i).col;
					num_actual = i;
				}
			}
			if(obj.size() > 0) obj.remove(num_actual);
			final_colors[c] = highest;
		}

		for(int i = 0; i < fin.length; i++)
		{
			float curr_dist = 100;
			int curr_num = 0;
			for(int x = 0; x < final_colors.length; x++)
			{
				float new_dist = colorDist(fin[i], final_colors[x]);
				if(new_dist < curr_dist)
				{
					curr_dist = new_dist;
					curr_num = x;
				}
			}
			
			fin[i] = final_colors[curr_num];
		}
		
		return fin;
	}

	class Merger_Group
	{
		public ArrayList<Merger_Obj> pixel = new ArrayList<Merger_Obj>();
		
		Merger_Group(int p, int c, int x, int y)
		{
			pixel = new ArrayList<Merger_Obj>();
			pixel.add(new Merger_Obj(p,c));
			pixel.get(0).x = x;
			pixel.get(0).y = y;
		}
		public boolean IsConnected(int p)
		{
			for(int i = 0; i < pixel.size(); i++)
			{
				if(abs(pixel.get(i).point-p) <= 1) return true;
			}
			return false;
		}

		public boolean InThreshold(int x, int y, int c)
		{
			boolean nbour = false;

			if((abs(pixel.get(0).x - x) + abs(pixel.get(0).y - y)) < posdiff) nbour = true;
			/*for(int i = 0; i < pixel.size(); i++)
			{
				if(abs(pixel.get(i).point-p) <= 1000) nbour = true;
			}*/
			if(!nbour) return false;

			if(colorDist(c, pixel.get(0).col) > threshold) return false;
			return true;
		}

		

		public void AddObj(int p, int col)
		{
			pixel.add(new Merger_Obj(p,col));
		}

		public int FinalColor()
		{
			int fin = pixel.get(0).col;
			/*for(int i = 0; i < pixel.length; i++)
			{
				fin = lerpColor(pixel[i].col, fin, 0.5F);
			}*/
			return fin;
		}

		public int [] GetPixels()
		{
			int [] fin = new int [pixel.size()];
			for(int i = 0; i < fin.length; i++)
			{
				fin[i] = pixel.get(i).point;
			}
			return fin;
		}
	}

	class Merger_Obj
	{
		public int point;
		public int col;
		public int x;
		public int y;
		Merger_Obj(int p, int c)
		{
			col = c;
			point = p;
		}
	}

}
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

  public void Update()
  {
    pixel_radius = lerp(pixel_radius, pixel_target, 0.2f);
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
  public int [] Pixels2(PImage in)
  {
    int in_width = in.width;
    int in_height = in.height;
    int [] fin = new int[in.pixels.length];

    int point_x = 0;
    int point_y = 0;
    int point_oneD = point_x + point_y*in_width;
    int point_col = in.pixels[point_oneD];
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

  public int [] Pixels(PImage in)
  {
    int in_width = in.width;
    int in_height = in.height;
    int [] fin = new int[in.pixels.length];

    int point_x = 0;
    int point_y = 0;
    int point_oneD = point_x + point_y*in_width;
    int point_col = in.pixels[point_oneD];

    int pix_rad_int = PApplet.parseInt(pixel_radius);
    int min_x = PApplet.parseInt(Clamp(point_x-pix_rad_int, 0, in.width-1));
    int max_x = PApplet.parseInt(Clamp(point_x+pix_rad_int, 0, in.width-1));
    int min_y = PApplet.parseInt(Clamp(point_y-pix_rad_int, 0, in.height-1));
    int max_y = PApplet.parseInt(Clamp(point_y+pix_rad_int, 0, in.height-1));
    int top_left = in.pixels[min_x + min_y*in_width];
    int top_right = in.pixels[max_x + min_y * in_width];
    int bot_left = in.pixels[min_x + max_y*in_width];
    int bot_right = in.pixels[max_x + max_y*in_width];
    int middle = lerpColor(lerpColor(top_left, top_right, 0.5F), lerpColor(bot_left, bot_right, 0.5F), 0.5F);
    for (int x = 0; x < in_width; x ++)
    {
      for (int y = 0; y < in_height; y ++)
      {
        int loc = x + y*in_width;
        if (abs(y-point_y) >= pixel_radius)
        {
          point_y += pixel_radius;
          point_oneD = point_x + point_y*in_width;
          min_x = PApplet.parseInt(Clamp(point_x-pix_rad_int, 0, in.width-1));
          max_x = PApplet.parseInt(Clamp(point_x+pix_rad_int, 0, in.width-1));
          min_y = PApplet.parseInt(Clamp(point_y-pix_rad_int, 0, in.height-1));
          max_y = PApplet.parseInt(Clamp(point_y+pix_rad_int, 0, in.height-1));
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
      min_x = PApplet.parseInt(Clamp(point_x-pix_rad_int, 0, in.width-1));
      max_x = PApplet.parseInt(Clamp(point_x+pix_rad_int, 0, in.width-1));
      min_y = PApplet.parseInt(Clamp(point_y-pix_rad_int, 0, in.height-1));
      max_y = PApplet.parseInt(Clamp(point_y+pix_rad_int, 0, in.height-1));
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
class Pointil extends Filter
{
  int rate =300;
  float radius_min = 2;
  float radius_max = 10;
  boolean generate;

  public void Update()
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
  public int [] Pixels(PImage in)
  {
    if (!generate)
    {
      return in.pixels;
    }
    int [] fin = GetImageArray(in);

    for (int i = 0; i < rate; i++)
    {
      int x = PApplet.parseInt(random(0, in.width));
      int y = PApplet.parseInt(random(0, in.height));
      int loc = x + y * in.width;

      int target = in.pixels[loc];
      float radfinal = random(radius_min, radius_max);
      int[] pix = PixelsInRadius(x, y, PApplet.parseInt(radfinal), in.width);
      for (int p = 0; p < pix.length; p++)
      {
        if (pix[p] >= fin.length) continue;
        if (pix[p] != 0) fin[pix[p]] = target;
      }
    }
    return fin;
  }

  public int [] PixelsInRadius(int targx, int targy, int radius, int in_width)
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
  public void settings() {  size(900, 500); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "camtest" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
