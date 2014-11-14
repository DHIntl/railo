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
package railo.runtime.video;

import railo.commons.io.res.Resource;
import railo.runtime.PageContext;
import railo.runtime.exp.PageException;

public interface VideoUtil {

	public VideoProfile createVideoProfile(); 
	public VideoOutput createVideoOutput(Resource output); 
	public VideoInput createVideoInput(Resource input); 	

	public long toBytes(String byt) throws PageException;
	
	public long toHerz(String byt) throws PageException;
	
	public long toMillis(String time) throws PageException;
	
	public int[] calculateDimension(PageContext pc,VideoInput[] sources,int width, String strWidth,int height, String strHeight) throws PageException;
}
