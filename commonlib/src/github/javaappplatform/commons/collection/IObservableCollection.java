/**
 * loomi.model Project at Loedige.
 * Closed Source. Not for licence.
 */
package github.javaappplatform.commons.collection;

import java.util.Collection;

import github.javaappplatform.commons.events.ITalker;
import github.javaappplatform.commons.util.StringID;

/**
 * @author funsheep
 *
 */
public interface IObservableCollection<E> extends ITalker, Collection<E>
{

	public static final int E_NEW_ELEMENT = StringID.id("E_NEW_ELEMENT");
	public static final int E_NEW_ELEMENTS = StringID.id("E_NEW_ELEMENTS");
	public static final int E_ELEMENT_UPDATED = StringID.id("E_ELEMENT_UPDATED");
	public static final int E_REMOVED_ELEMENT = StringID.id("E_REMOVED_ELEMENT");
	public static final int E_REMOVED_ELEMENTS = StringID.id("E_REMOVED_ELEMENTS");

}
