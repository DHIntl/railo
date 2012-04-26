package railo.runtime.type.cfc;

import java.util.Iterator;
import java.util.Set;
import java.util.Map.Entry;

import railo.runtime.Component;
import railo.runtime.PageContext;
import railo.runtime.component.Member;
import railo.runtime.dump.DumpData;
import railo.runtime.dump.DumpProperties;
import railo.runtime.exp.PageException;
import railo.runtime.type.Collection;
import railo.runtime.type.Struct;
import railo.runtime.type.Collection.Key;

public interface ComponentAccess extends Component {
	
	public boolean isPersistent();
	public Object getMetaStructItem(Collection.Key name);
	
	Set<Key> keySet(int access);
	Object call(PageContext pc, int access, Collection.Key name, Object[] args) throws PageException;
	Object callWithNamedValues(PageContext pc, int access, Collection.Key name, Struct args) throws PageException;
	int size(int access);
	String[] keysAsString(int access);
	Collection.Key[] keys(int access);

	Iterator<Collection.Key> keyIterator(int access);
	Iterator<String> keysAsStringIterator(int access);
	
	
	Iterator<Entry<Key, Object>> entryIterator(int access);
	
	Object get(int access, Collection.Key key) throws PageException;
	Object get(int access, Collection.Key key, Object defaultValue);
	DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties dp, int access);
	boolean contains(int access,Key name);
	Member getMember(int access,Collection.Key key, boolean dataMember,boolean superAccess);
	public ComponentAccess _base();// TODO do better impl
	//public boolean isRest();
	
	
	
}
