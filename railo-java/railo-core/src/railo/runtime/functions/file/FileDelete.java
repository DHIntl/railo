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
package railo.runtime.functions.file;

import java.io.IOException;

import railo.commons.io.res.Resource;
import railo.runtime.PageContext;
import railo.runtime.exp.FunctionException;
import railo.runtime.exp.PageException;
import railo.runtime.op.Caster;

public class FileDelete {

	public static String call(PageContext pc, Object oSrc) throws PageException {
		Resource src = Caster.toResource(pc,oSrc,false);

        pc.getConfig().getSecurityManager().checkFileLocation(src);
		if(!src.exists()) 
			throw new FunctionException(pc,"FileDelete",1,"source",
					"source file ["+src+"] does not exist");
		try {
			src.remove(false);
		} catch (IOException e) {
			throw Caster.toPageException(e);
		}
		return null;
	}
}
