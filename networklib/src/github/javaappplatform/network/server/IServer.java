package github.javaappplatform.network.server;

import java.io.IOException;
import java.util.Collection;

public interface IServer
{

	public abstract int state();

	public abstract void start() throws IOException;

	public abstract void close();

	public abstract void shutdown();

	public int reserveClientID();

	public abstract IRemoteClientUnit getClient(int id);

	public abstract Collection<IRemoteClientUnit> getAllClients();

}