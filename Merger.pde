class Merger extends Filter
{
	float threshold = 70;
	int posdiff = 200;

	int [] Pixels2(PImage in)
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

				color c = fin[loc];

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
			color fincol = groups.get(g).FinalColor();
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
	int [] Pixels(PImage in)
	{	
		int inwidth = in.width;
		int inheight = in.height;
		int [] fin = GetImageArray(in);

		color [] final_colors = new color [colors];
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
			color highest = color(0,0,0);
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
		
		Merger_Group(int p, color c, int x, int y)
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

		public boolean InThreshold(int x, int y, color c)
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

		

		public void AddObj(int p, color col)
		{
			pixel.add(new Merger_Obj(p,col));
		}

		public color FinalColor()
		{
			color fin = pixel.get(0).col;
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
		public color col;
		public int x;
		public int y;
		Merger_Obj(int p, color c)
		{
			col = c;
			point = p;
		}
	}

}