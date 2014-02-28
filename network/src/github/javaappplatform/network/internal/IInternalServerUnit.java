package github.javaappplatform.network.internal;

import java.io.IOException;

/**
 * TODO javadoc
 * @author funsheep
 */
public interface IInternalServerUnit
{
	
	public void start(IInternalServer server) throws IOException;
	
	public boolean isShutdown();
	
	public void shutdown();

}
