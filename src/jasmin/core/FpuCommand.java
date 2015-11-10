package jasmin.core;

public abstract class FpuCommand extends JasminCommand {
	
	protected Fpu fpu;
	
	/**
	 * @param mnemo
	 *        the mnemo of the command whose default operation size is requested
	 */
	public int defaultSize(String mnemo) {
		return 8;
	}
	
	public boolean signed() {
		return true;
	}
	
	public void setDataSpace(DataSpace newdataspace) {
		super.setDataSpace(newdataspace);
		fpu = dataspace.fpu;
	}
	
}
