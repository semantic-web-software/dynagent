package gdev.gawt.utils;

import dynagent.common.utils.GIdRow;

public interface ITableNavigation {

	public GIdRow nextRow();

	public GIdRow prevRow();
	
	public boolean hasNextRow();
	
	public boolean hasPrevRow();
}
