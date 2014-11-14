/**
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this library.  If not, see <http://www.gnu.org/licenses/>.
 * 
 **/
package railo.runtime.functions.system;

import railo.commons.io.SystemUtil;
import railo.runtime.PageContext;
import railo.runtime.exp.ApplicationException;
import railo.runtime.ext.function.Function;
import railo.runtime.op.Caster;

public class GetCPUUsage implements Function {

	private static final long serialVersionUID = 2264215038554428321L;
	
	public static double call(PageContext pc) throws ApplicationException {
		return call(pc, 1000);
	}
	public static double call(PageContext pc, double time) throws ApplicationException {
		return Caster.toDoubleValue(SystemUtil.getCpuUsage((long)time));
	}

}
