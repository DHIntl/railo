package railo.runtime.cache.tag.smart;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import railo.print;
import railo.commons.io.cache.Cache;
import railo.commons.io.log.Log;
import railo.commons.io.log.LogUtil;
import railo.commons.io.res.Resource;
import railo.commons.lang.StringUtil;
import railo.runtime.PageContext;
import railo.runtime.cache.tag.CacheHandler;
import railo.runtime.cache.tag.CacheHandlerFactory;
import railo.runtime.cache.tag.CacheHandlerFilter;
import railo.runtime.cache.tag.request.CacheEntry;
import railo.runtime.cache.util.CacheKeyFilterAll;
import railo.runtime.config.Config;
import railo.runtime.config.ConfigImpl;
import railo.runtime.config.ConfigServer;
import railo.runtime.config.ConfigWebImpl;
import railo.runtime.engine.ThreadLocalPageContext;
import railo.runtime.exp.ApplicationException;
import railo.runtime.exp.PageException;
import railo.runtime.exp.PageRuntimeException;
import railo.runtime.functions.cache.Util;
import railo.runtime.op.Caster;
import railo.runtime.text.xml.XMLUtil;
import railo.runtime.type.Query;
import railo.runtime.type.QueryImpl;
import railo.runtime.type.Struct;
import railo.runtime.type.StructImpl;
import railo.runtime.type.dt.DateTimeImpl;
import railo.runtime.type.dt.TimeSpan;
import railo.runtime.type.util.KeyConstants;

public class SmartCacheHandler implements CacheHandler {

	private static boolean running;
	private static long startTime;
	
	private PageContext pc; 
	private Config config;
	
	private int cacheType;
	private Cache _cache;
	private Log log;
	private Map<String,SmartEntry> entries=new LinkedHashMap<String,SmartEntry>();
	private Map<String,TimeSpan> rules=new ConcurrentHashMap<String,TimeSpan>();
	//public static Map<String,CE> cachew=new ConcurrentHashMap<String,CE>();
	

	public SmartCacheHandler(int cacheType) {
		this.pc=ThreadLocalPageContext.get();
		this.config=ThreadLocalPageContext.getConfig(pc);
		this.cacheType=cacheType;
		
		// get default cache
		this._cache=Util.getDefault(pc, cacheType,null);
		
		// get smart cache
		if(_cache==null) {
			if(true)throw new PageRuntimeException(new ApplicationException("no default cache found"));
			try {
				this._cache=getCache();
			}
			catch (PageException pe) {
				throw new PageRuntimeException(pe); // TODO handle this in a better way
			}
		}
		log = ((ConfigImpl)config).getLog("smartcache");
	}
	

	private Cache getCache() throws PageException {
		String cacheName = getCacheName();
		try {
			return Util.getCache(config, cacheName);
		}
		catch (IOException e) {
			throw Caster.toPageException(e);
		}
	}

	private String getCacheName() throws ApplicationException {
		// get config dir
		Resource dir;
		if(config instanceof ConfigServer) dir=config.getConfigDir();
		else dir=((ConfigWebImpl)config).getServerConfigDir();
		
		// get xml
		Resource xmlFile=dir.getRealResource("smartcache/settings.xml");
		if(xmlFile.isFile()) {
			try{
				InputSource is = XMLUtil.toInputSource(xmlFile);
				Document xml = XMLUtil.parse(is, null,false);
				Element root = XMLUtil.getRootElement(xml, true);
				String cache = root.getAttribute("cache");
				if(!StringUtil.isEmpty(cache,true)) return cache.trim();
			}
			catch(Throwable t){
				Log log = ((ConfigWebImpl)config).getLog("smartcache", true);
				LogUtil.log(log, Log.LEVEL_ERROR, "smartcache", t);
			}
		}
		throw new ApplicationException("there is no cache connection defined for Smart Cache, please define a cache for Smart cache in the Railo Administrator at Smart cache/Settings.");
	}

	@Override
	public Object get(PageContext pc, String id) throws PageException {
		if(!running) return null;
		
		return _cache.getValue(id,null);
		
		/*
		CE ce = cache.get(id);
		if(value!=null) {
			if(ce.cacheUntil>System.currentTimeMillis()) 
				return ce.value;
			cache.remove(id);
		}
		return null;*/
	}

	@Override
	public boolean remove(PageContext pc, String id) {
		return rules.remove(id)!=null;
	}

	@Override
	public void set(PageContext pc, String id, Object cachedwithin, Object value) throws PageException {
		if(!running) return;
		
		// add do cache if necessary
		TimeSpan ts = rules.get(id);
		//print.e("set("+id+"):"+(ts!=null));
		if(ts!=null) {
			_cache.put(id, value, ts.getMillis(), null);
			// cache.put(id, new CE(value,System.currentTimeMillis()+ts.getMillis()));
		}

		// store info
		if(cacheType==ConfigImpl.CACHE_DEFAULT_QUERY) {
			// get Raw type
			if(value instanceof CacheEntry) {
				CacheEntry ce=(CacheEntry) value;
				value=ce.query;
			}
			if(value instanceof Query) setQuery(pc,id,(Query)value);
			// TODO handle storedproc
		}
		// TODO else handle all other types
	}

	private void setQuery(PageContext pc, String id, Query qry) {
		SmartEntry se = new QuerySmartEntry(pc,qry,id,cacheType);
		entries.put(se.getId(),se);
		
	}

	@Override
	public void clear(PageContext pc) {
		try {
			_cache.remove(CacheKeyFilterAll.getInstance());
		}
		catch (IOException e) {}
	}
	

	@Override
	public void clear(PageContext pc, CacheHandlerFilter filter) {
		try{
			Iterator<railo.commons.io.cache.CacheEntry> it = _cache.entries().iterator();
			railo.commons.io.cache.CacheEntry ce;
			while(it.hasNext()){
				ce = it.next();
				if(filter==null || filter.accept(ce.getValue()))
					_cache.remove(ce.getKey());
			}
		}
		catch (IOException e) {}
	}

	@Override
	public void clean(PageContext pc) {
		// no action necessary
	}

	@Override
	public int size(PageContext pc) {
		return size();
	}
	
	public int size() {
		if(!running)return 0;
		
		try {
			return _cache.keys().size();
		}
		catch (IOException e) {
			return 0;
		}
	}

	/*private void print(PageContext pc, String msg) {
		//print.e(CacheHandlerFactory.toStringCacheName(cacheType, null)+"->"+msg);
		((ConfigImpl)pc.getConfig()).getLog("application").error(CacheHandlerFactory.toStringCacheName(cacheType, null),msg);
	}*/

	public static void setRule(int type, String id, TimeSpan timeSpan) { 
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_INCLUDE)
			CacheHandlerFactory.include.getSmartCacheHandler().setRule(id, timeSpan);
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_QUERY)
			CacheHandlerFactory.query.getSmartCacheHandler().setRule(id, timeSpan);
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_FUNCTION)
			CacheHandlerFactory.function.getSmartCacheHandler().setRule(id, timeSpan);
	}
	public void setRule(String id, TimeSpan timeSpan) { 
		// flush all cached elements for the old rule
		if(rules.containsKey(id)) {
			try {
				_cache.remove(id);
			}
			catch (IOException e) {}
		}		
		rules.put(id, timeSpan);
	}

	public static void clearAllRules() {
		clearRules(ConfigImpl.CACHE_DEFAULT_NONE);
	}
	public static void clearRules(int type) {
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_INCLUDE)
			CacheHandlerFactory.include.getSmartCacheHandler().rules.clear();
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_QUERY)
			CacheHandlerFactory.query.getSmartCacheHandler().rules.clear();
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_FUNCTION)
			CacheHandlerFactory.function.getSmartCacheHandler().rules.clear();
	}

	public static void removeRule(int type, String id) {
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_INCLUDE)
			CacheHandlerFactory.include.getSmartCacheHandler().rules.remove(id);
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_QUERY)
			CacheHandlerFactory.query.getSmartCacheHandler().rules.remove(id);
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_FUNCTION)
			CacheHandlerFactory.function.getSmartCacheHandler().rules.remove(id);
	}
	
	public static Query getAllRules() {
		return getRules(ConfigImpl.CACHE_DEFAULT_NONE);
	}
	
	public static Query getRules(int type) {
		Query qry=new QueryImpl(new String[]{"type","entryHash","timespan"},0,"rules");
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_INCLUDE)
			CacheHandlerFactory.include.getSmartCacheHandler()._getRules(qry, "include");
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_QUERY)
			CacheHandlerFactory.query.getSmartCacheHandler()._getRules(qry, "query");
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_FUNCTION)
			CacheHandlerFactory.function.getSmartCacheHandler()._getRules(qry, "function");
		return qry;
	}
	
	public void _getRules(Query qry, String type) {
		Iterator<Entry<String, TimeSpan>> it = rules.entrySet().iterator();
		int row;
		while(it.hasNext()){
			Entry<String, TimeSpan> e = it.next();
			row=qry.addRow();
			qry.setAtEL("type", row, type);
			qry.setAtEL("entryHash", row, e.getKey());
			qry.setAtEL("timespan", row, e.getValue());
		}
	}
	
	public static Struct info(int type) {
		Struct info=new StructImpl();
		//Struct caches=new StructImpl();
		Query caches=new QueryImpl(new String[]{"type","entryHash","timespan"},0,"caches");
		
		info.setEL("starttime", running?new DateTimeImpl(startTime,true):"");
		info.setEL("running", running);
		info.setEL("caches", caches);
		
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_INCLUDE)
			CacheHandlerFactory.include.getSmartCacheHandler()._info(caches,"include");
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_QUERY)
			CacheHandlerFactory.query.getSmartCacheHandler()._info(caches,"query");
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_FUNCTION)
			CacheHandlerFactory.function.getSmartCacheHandler()._info(caches,"function");
		return info;
	}
	
	private void _info(Query qry, String type) {
		int row=qry.addRow();
		qry.setAtEL(KeyConstants._type, row, type);
		qry.setAtEL("rules", row, rules.size());
		qry.setAtEL("entries", row, entries.size());
	}

	public static void start() {
		startTime = System.currentTimeMillis();
		running=true;
	}

	public static void stop() {
		running=false;
	}


	public Map<String, SmartEntry> getEntries() {
		return entries;
	}

	
	/*private static class CE {

		private Object value;
		private long cacheUntil;

		public CE(Object value, long cacheUntil) {
			this.value=value;
			this.cacheUntil=cacheUntil;
		}
		
	}*/
}
