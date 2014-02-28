package github.javaappplatform.network.internal;

import github.javaappplatform.commons.events.IListener;
import github.javaappplatform.network.server.IRemoteClientUnit;
import github.javaappplatform.network.server.IServer;

/**
 * TODO javadoc
 * @author funsheep
 */
public interface IInternalServer extends IServer, IListener
{

	public void register(IRemoteClientUnit unit) throws IllegalArgumentException;

}
