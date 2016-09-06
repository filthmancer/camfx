class FrameUpdater extends Filter
{
	PImage prev;
	int [] Pixels(PImage in)
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