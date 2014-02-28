/*
	This file is part of the javaappplatform common library.
	Copyright (C) 2013 funsheep
	Copyright (C) 2005-2013 d3fact Project Team (www.d3fact.de)

	This library is subject to the terms of the Mozilla Public License, v. 2.0.
	You should have received a copy of the MPL along with this library; see the
	file LICENSE. If not, you can obtain one at http://mozilla.org/MPL/2.0/.
*/
package github.javaappplatform.commons.log;

import java.util.HashMap;
import java.util.Map;

/**
 * Can be configured with rules to rename class names. Uses a cache for class names <-> aliases to
 * remove overhead of creation.
 *
 * There are three types of rules:
 * <ul>
 * <li> PLUS: The substitution rule is added to current substitution.</li>
 * <li> STAR: The substitution replaces the current substitution.</li>
 * <li> Everything else: Is only used when the current substitution is empty (like an introductory rule).</li>
 * </ul>
 * To further explain the inner workings of the formatter we use examples.
 *
 * Example Ruleset:
 * <code>de.d3fact.util.Tools=Close
 * de.d3fact=+[d3f]
 * de.d3fact.util=[util]
 * </code>
 *
 * Classnames to substitute, plus the result:
 * <code>
 * de.d3fact.util.Tools -> [d3f]Close
 * de.d3fact.util.TextA -> [d3f][util].TextA
 * de.d3fact.modelapi.Test -> [d3f].modelapi.Test
 * com.d3fact.test.ABC -> [com]
 * </code>
 *
 * The formatter searches for the largest possible substitution (any rule type is permitted). Then it searches for
 * PLUS rules which substitute smaller pieces (e.g. the second rule). These substitutions are added
 * in front of the current string.
 *
 * Ruleset:
 * <code>
 * com.d3fact=[d3f]
 * com=*[com]
 * </code>
 * Result:
 * <code>
 * com.d3fact.test.ABC -> [com]
 * </code>
 * If the formatter finds a STAR rule the current substitution string is replaced. In the
 * above example first the [d3f] rule is applied (this is the largest possible substitution), but
 * then it finds the overriding rule for [com], so everything is replaced by that string. The
 * formatter can also be configured during runtime. The actual renaming algorithm, located in
 * github.javaappplatform.commons.platform.log.ClassRenamer does provide a handy .addRuleset(Map<String,String>)
 * method to extend the loaded rules during runtime
 *
 * @author funsheep
 */
public class ClassRenamer
{

	private static final class Entry
	{

		public static final int TYPE_PLUS = 1;
		public static final int TYPE_STAR = 2;
		public static final int TYPE_REPLACE = 3;


		public final int type;
		public final String alias;

		public Entry(int type, String alias)
		{
			this.type = type;
			this.alias = alias;
		}

		public Entry(String toParse)
		{
			this(toParse.charAt(0) == '+' ? TYPE_PLUS : toParse.charAt(0) == '*' ? TYPE_STAR : TYPE_REPLACE, toParse.charAt(0) == '+' || toParse.charAt(0) == '*' ? toParse.substring(1) : toParse);
		}
	}


	private static final HashMap<String, Entry> ENTRY_BY_NAME = new HashMap<String, Entry>();

	private static final HashMap<String, String> ALIAS_CACHE = new HashMap<String, String>();


	/**
	 * Adds a the given rule set to the known rules. Note that known rules are overridden.
	 * @param rules the rules to add.
	 */
	public static final void addRuleset(Map<String, String> rules)
	{
		for (java.util.Map.Entry<String, String> entry : rules.entrySet())
			ENTRY_BY_NAME.put(entry.getKey(), new Entry(entry.getValue()));
		ALIAS_CACHE.clear();
	}

	/**
	 * Returns the current rule set.
	 * @return The current rule set.
	 */
	public static final Map<String, String> getRuleset()
	{
		final HashMap<String, String> ruleset = new HashMap<String, String>(ENTRY_BY_NAME.size());
		for (java.util.Map.Entry<String, Entry> entry : ENTRY_BY_NAME.entrySet())
			ruleset.put(entry.getKey(), (entry.getValue().type == Entry.TYPE_PLUS ? "+" : entry.getValue().type == Entry.TYPE_STAR ? "*" : "") + entry.getValue().alias);
		return ruleset;
	}

	/**
	 * Returns the alias for the given class name according to the rules. The class name is cached to avoid the ongoing extensive evaluation of the rules.
	 * @param classname The class name to alias.
	 * @return The alias for the class name.
	 */
	public static final String alias(String classname)
	{
		String alias = ALIAS_CACHE.get(classname);
		if (alias == null)
		{
			alias = lookUp(classname);
			ALIAS_CACHE.put(classname, alias);
		}
		return alias;
	}


	private static final String lookUp(final String classname)
	{
		String tmp_classname = classname;
		String sb = new String();
		//try to find one first complete substitution for at least a subset of the string
		Entry e = ENTRY_BY_NAME.get(tmp_classname);
		while (e == null && tmp_classname != null && tmp_classname.length() > 0)
		{
			final int lastdot = Math.max(0, tmp_classname.lastIndexOf('.'));
			sb = tmp_classname.substring(lastdot, tmp_classname.length()) + sb;
			tmp_classname = tmp_classname.substring(0, lastdot);
			e = ENTRY_BY_NAME.get(tmp_classname);
		}
		if (e == null)	//there is no substitution available - just returned the classname itself as alias
			return classname;
		if (e.type == Entry.TYPE_STAR)
			sb = e.alias;
		else
			sb = e.alias + sb;
		//now try to find a "plus" rule for the classname "rest" to attach at the beginning, all other rules are ignored

		final int _lastdot = tmp_classname.lastIndexOf('.');
		if (_lastdot == -1) //no more "dots" available just return what we have already substituted at least once...
			return sb;
		tmp_classname = tmp_classname.substring(0, _lastdot); //try to find "plus" rules for this remaining classname

		while (tmp_classname.length() > 0)
		{
			e = ENTRY_BY_NAME.get(tmp_classname);
			if (e != null && e.type == Entry.TYPE_PLUS)
				sb = e.alias + sb;
			else if (e != null && e.type == Entry.TYPE_STAR)
				sb = e.alias;
			final int lastdot = Math.max(0, tmp_classname.lastIndexOf('.'));
			tmp_classname = tmp_classname.substring(0, lastdot);
		}
		return sb.toString();
	}


	private ClassRenamer()
	{
		//no instance
	}

}
