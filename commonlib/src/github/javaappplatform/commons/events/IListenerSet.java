package github.javaappplatform.commons.events;

import gnu.trove.procedure.TIntObjectProcedure;

public interface IListenerSet
{

	/**
	 * Returns the size of this set.
	 * @return The size of this set.
	 */
	public abstract int size();

	/**
	 * This method executes the given "function" for every <type, listener> pair currently in this set.
	 * @param func The function to execute.
	 */
	public abstract void foreachEntry(TIntObjectProcedure<IListener> func);

	/**
	 * Returns <code>true</code> when there are listeners for this type of event. <code>false</code>
	 * otherwise.
	 * @param type The event type to test.
	 * @return Returns whether there are listener to the given event type or not.
	 */
	public abstract boolean hasHooks(int type);

	/**
	 * Hooks a listener up for the given event type. It does not matter if the listener already
	 * listens to other event types or even to the same event type.
	 * @param type The type to listen to.
	 * @param listener The listener.
	 */
	public abstract void hookUp(int type, IListener listener);

	/**
	 * Unhooks the listener from the given event type.
	 * @param type The event type.
	 * @param listener The listener.
	 */
	public abstract void unhook(int type, IListener listener);

	/**
	 * Unhooks the listener from all event types he is hooked up to.
	 * @param listener The listener to unhook.
	 */
	public abstract void unhook(IListener listener);

	public abstract void removeAllListener();

}