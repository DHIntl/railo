package railo.runtime.functions.system;

import railo.runtime.PageContext;
import railo.runtime.exp.FunctionException;
import railo.runtime.exp.PageException;
import railo.runtime.functions.BIF;
import railo.runtime.functions.BIFProxy;
import railo.runtime.interpreter.ref.func.BIFCall;
import railo.runtime.op.Caster;
import railo.runtime.type.Array;

public class GetBuildInFunction extends BIF {
	
	private static final long serialVersionUID = 5639839935753070955L;

	public static Object call(PageContext pc , String name) throws PageException {
		
		return new railo.runtime.type.BIF(pc.getConfig(),name);
	}
	
    @Override
	public Object invoke(PageContext pc, Object[] args) throws PageException {
    	if(args.length==2)
			return call(pc, Caster.toString(args[0]));
    	
		throw new FunctionException(pc, "GetBuildInFunction", 1, 1, args.length);
	}

}
