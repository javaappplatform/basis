/**
 * loomi.model Project at Loedige.
 * Closed Source. Not for licence.
 */
package github.javaappplatform.commons.collection;

import github.javaappplatform.commons.events.ITalker;
import github.javaappplatform.commons.util.StringID;

/**
 * @author funsheep
 *
 */
public interface IObservableCollection extends ITalker
{

	public static final int EVENT_NEW_ELEMENT = StringID.id("EVENT_NEW_ELEMENT");
	public static final int EVENT_NEW_ELEMENTS = StringID.id("EVENT_NEW_ELEMENTS");
	public static final int EVENT_UPDATED_ELEMENT = StringID.id("EVENT_ELEMENT_UPDATED");
	public static final int EVENT_UPDATED_ELEMENTS = StringID.id("EVENT_ELEMENTS_UPDATED");
	public static final int EVENT_REMOVED_ELEMENT = StringID.id("EVENT_REMOVED_ELEMENT");
	public static final int EVENT_REMOVED_ELEMENTS = StringID.id("EVENT_REMOVED_ELEMENTS");
	public static final int EVENT_COLLECTION_CLEARED = StringID.id("EVENT_COLLECTION_CLEARED");

}
