package ij.plugin.frame;

/**
 * This class is a workaround to get public access to the Roi managers open(filename) method. 
 * This was needed to allow VolumeManager and Label3DManager to load ZIP files.
 * 
 * Furthermore, this class implements a correct Singleton.
 * 
 * @author Robert Haase, Scientific Computing Facility, MPI CBG, rhaase@mpi-cbg.de
 * @version 1.0.1, 2015-08-26
 */
public class ModRoiManager extends RoiManager {



	
	private static ModRoiManager instance = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = 8262679514562244550L;

	public void open(String path)
	{
		super.open(path);
	}

	/**
	 * Singleton implementation
	 * @return This function ALWAYS returns a ModRoiManager. If there is no instance, it will create one and return it.
	 */
	public static ModRoiManager getInstance()
	{
		synchronized (ModRoiManager.class)
		{
			if (instance == null)
			{
				instance = new ModRoiManager();
			}
		}
		return instance;
	}
	
	public void close()
	{
		super.close();
		if (instance == this)
		{
			instance = null;
		}
	}
	
}
