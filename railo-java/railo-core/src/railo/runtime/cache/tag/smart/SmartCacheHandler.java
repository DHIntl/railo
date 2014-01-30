package railo.runtime.cache.tag.smart;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

import railo.print;
import railo.commons.io.CharsetUtil;
import railo.commons.io.IOUtil;
import railo.commons.io.cache.Cache;
import railo.commons.io.log.Log;
import railo.commons.io.log.LogUtil;
import railo.commons.io.res.Resource;
import railo.runtime.PageContext;
import railo.runtime.cache.tag.CacheHandler;
import railo.runtime.cache.tag.CacheHandlerFactory;
import railo.runtime.cache.tag.CacheHandlerFilter;
import railo.runtime.cache.tag.CacheItem;
import railo.runtime.cache.tag.query.QueryCacheItem;
import railo.runtime.cache.util.CacheKeyFilterAll;
import railo.runtime.config.Config;
import railo.runtime.config.ConfigImpl;
import railo.runtime.engine.ThreadLocalPageContext;
import railo.runtime.exp.ApplicationException;
import railo.runtime.exp.PageException;
import railo.runtime.exp.PageRuntimeException;
import railo.runtime.functions.cache.Util;
import railo.runtime.op.Caster;
import railo.runtime.op.Decision;
import railo.runtime.text.xml.XMLCaster;
import railo.runtime.text.xml.XMLUtil;
import railo.runtime.type.Query;
import railo.runtime.type.QueryImpl;
import railo.runtime.type.Struct;
import railo.runtime.type.StructImpl;
import railo.runtime.type.UDF;
import railo.runtime.type.dt.DateTimeImpl;
import railo.runtime.type.dt.TimeSpan;
import railo.runtime.type.dt.TimeSpanImpl;
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
	private Map<String,Rule> rules=new ConcurrentHashMap<String,Rule>();

	public SmartCacheHandler(int cacheType) {
		this.pc=ThreadLocalPageContext.get();
		this.config=ThreadLocalPageContext.getConfig(pc);
		this.cacheType=cacheType;
		
		// get default cache
		this._cache=Util.getDefault(pc, cacheType,null);
		log = ((ConfigImpl)config).getLog("smartcache");
		loadRules();
	}

	@Override
	public CacheItem get(PageContext pc, String id) throws PageException {
		if(!running) return null;
		log.debug("smartcache", "get("+id+")");
		CacheItem ci = (CacheItem) _cache.getValue(id,null);
		if(ci!=null) {
			// TODO async ?
			rules.get(id).used++;
		}
		return ci;
	}

	@Override
	public boolean remove(PageContext pc, String id) {
		log.debug("smartcache", "remove("+id+")");
		return rules.remove(id)!=null;
	}

	@Override
	public void set(PageContext pc, String id, Object cachedwithin, CacheItem ci) throws PageException {
		if(!running) return;
		
		// add do cache if necessary
		Rule rule = rules.get(id);
		if(rule!=null) {
			log.debug("smartcache", "add to cache ("+id+")");
			_cache.put(id, ci, null, Long.valueOf(rule.timespan.getMillis()));
		}
		SmartEntry se = new SmartEntryImpl(pc,ci,id,cacheType);
		log.debug("smartcache", "add to entries ("+id+")");
		entries.put(se.getId(),se);
		
		// TODO else handle all other types
	}

	@Override
	public void clear(PageContext pc) {
		log.debug("smartcache", "clear()");
		
		try {
			_cache.remove(CacheKeyFilterAll.getInstance());
		}
		catch (IOException e) {}
	}
	

	@Override
	public void clear(PageContext pc, CacheHandlerFilter filter) {
		log.debug("smartcache", "clear("+filter+")");
		
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
		log.debug("smartcache", "size()");
		
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
		log.debug("smartcache", "setRule("+id+","+timeSpan+")");
		// flush all cached elements for the old rule
		Rule rule = rules.get(id);
		if(rule!=null) {
			if(rule.timespan.equals(timeSpan)) return;
			try {
				log.debug("smartcache", "remove cache item with id: "+id);
				_cache.remove(id);
			}
			catch (IOException e) {}
		}		
		rules.put(id, new Rule(timeSpan));
		setRuleElement(id, timeSpan);
	}

	public static void clearAllRules() {
		clearRules(ConfigImpl.CACHE_DEFAULT_NONE);
	}
	public static void clearRules(int type) {
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_INCLUDE)
			CacheHandlerFactory.include.getSmartCacheHandler().clearRules();
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_QUERY)
			CacheHandlerFactory.query.getSmartCacheHandler().clearRules();
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_FUNCTION)
			CacheHandlerFactory.function.getSmartCacheHandler().clearRules();
	}
	
	public void clearRules(){
		rules.clear();
		clearRuleElements();
	}

	public static void removeRule(int type, String id) {
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_INCLUDE)
			CacheHandlerFactory.include.getSmartCacheHandler().removeRule(id);
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_QUERY)
			CacheHandlerFactory.query.getSmartCacheHandler().removeRule(id);
		if(type==ConfigImpl.CACHE_DEFAULT_NONE || type==ConfigImpl.CACHE_DEFAULT_FUNCTION)
			CacheHandlerFactory.function.getSmartCacheHandler().removeRule(id);
	}
	
	public void removeRule(String id){
		rules.remove(id);
		removeRuleElement(id);
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
		Iterator<Entry<String, Rule>> it = rules.entrySet().iterator();
		int row;
		Rule r;
		while(it.hasNext()){
			Entry<String, Rule> e = it.next();
			row=qry.addRow();
			r = e.getValue();
			qry.setAtEL("type", row, type);
			qry.setAtEL("entryHash", row, e.getKey());
			qry.setAtEL("timespan", row,r.timespan );
			qry.setAtEL("crated", row, new DateTimeImpl(r.created,false));
			qry.setAtEL("used", row, r.used);
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
	
	
	private void setRuleElement(String id, TimeSpan ts) {
		Element root = getRootElement();
		Document doc=XMLUtil.getDocument(root);
		Iterator<Element> it = getRuleElements(root).iterator();
		
		Element e;
		String _id;
		
		while(it.hasNext()){
			e = it.next();
			_id=dec(e.getAttribute("id"));
			// update
			if(id.equals(_id)) {
				e.setAttribute("timespan", enc(ts));
				store(doc);
				return ;
			}
		}
		
		e=doc.createElement("rule");
		e.setAttribute("id", enc(id));
		e.setAttribute("timespan", enc(ts));
		root.appendChild(e);
		store(doc);
	}
	
	private void removeRuleElement(String id) {
		Element root = getRootElement();
		Document doc=XMLUtil.getDocument(root);
		Iterator<Element> it = getRuleElements(root).iterator();
		
		Element e;
		String _id;
		
		while(it.hasNext()){
			e = it.next();
			_id=dec(e.getAttribute("id"));
			// update
			if(id.equals(_id)) {
				root.removeChild(e);
				store(doc);
				return ;
			}
		}
	}
	private void clearRuleElements() {
		Element root = getRootElement();
		Document doc=XMLUtil.getDocument(root);
		Iterator<Element> it = getRuleElements(root).iterator();
		
		Element e;
		String _id;
		
		while(it.hasNext()){
			e = it.next();
			root.removeChild(e);
		}
		store(doc);
	}
	




	private void loadRules() {
		try {
			Iterator<Element> it = getRuleElements(null).iterator();
			
			Element e;
			while(it.hasNext()){
				e = it.next();
				rules.put(
						dec(e.getAttribute("id")), 
						new Rule(TimeSpanImpl.fromMillis(Caster.toLongValue(dec(e.getAttribute("timespan")))))
				);
			}
		}
		catch (Throwable t) {
			LogUtil.log(log, Log.LEVEL_ERROR, "smartcache", t);
		}
	}


	private List<Element> getRuleElements(Element root) {
		List<Element> list=new ArrayList<Element>();
		if(root==null) root=getRootElement();
		Iterator<Node> it = XMLUtil.getChildNodes(root,Node.ELEMENT_NODE).iterator();
		Node n; 
		while(it.hasNext()){
			n = it.next();
			if(n.getNodeName().equals("rule")){
				list.add((Element) n);
			}
		}
		
		return list;
	}

	private Resource getXMLFile() {
		// get config dir
		Resource dir=config.getConfigDir();
		
		// get xml
		return dir.getRealResource("smartcache/"+CacheHandlerFactory.toStringCacheName(cacheType,"default")+".xml");
	}
	
	private Element getRootElement() {
		Resource xmlFile = getXMLFile();
		try{
			if(!xmlFile.isFile()) {
				xmlFile.getParentResource().mkdirs();
				IOUtil.write(xmlFile, "<smartcache/>", CharsetUtil.UTF8, false);
			}
			InputSource is = XMLUtil.toInputSource(xmlFile, CharsetUtil.UTF8);
			Document xml = XMLUtil.parse(is, null,false);
			return XMLUtil.getRootElement(xml, true);
		}
		catch(Throwable t){
			throw new PageRuntimeException(Caster.toPageException(t));
		}
		
	}
	

	private void store(Document doc) {
		Resource xmlFile = getXMLFile();
		try{
			String str = XMLCaster.toString(doc);
			IOUtil.write(xmlFile, str, CharsetUtil.UTF8, false);
		}
		catch(Throwable t){
			throw new PageRuntimeException(Caster.toPageException(t));
		}
	}
	
	private Object toRaw(Object value) {
		if(value instanceof QueryCacheItem) {
			QueryCacheItem ce=(QueryCacheItem) value;
			value=ce.query;
		}
		return value;
	}
	

	private String dec(String str) {
		return str;
	}
	private String enc(String str) {
		return str;
	}
	private String enc(TimeSpan ts) {
		return Caster.toString(ts.getMillis());
	}
}
